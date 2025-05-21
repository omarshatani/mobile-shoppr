package com.shoppr.post;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shoppr.domain.usecase.AnalyzePostTextUseCase;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.SavePostUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.ListingState;
import com.shoppr.model.ListingType;
import com.shoppr.model.LocationData;
import com.shoppr.model.Post;
import com.shoppr.model.SuggestedPostDetails;
import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CreatePostViewModel extends AndroidViewModel {
    private static final String TAG = "CreatePostViewModel";

    private final AnalyzePostTextUseCase analyzePostTextUseCase;
    private final SavePostUseCase savePostUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    // LiveData for the currently authenticated user (with full profile)
    public final LiveData<User> currentListerLiveData;
    public final LiveData<Event<String>> currentUserErrorEvents;


    // --- LiveData for UI State & LLM Interaction ---
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false); // General loading state
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<Event<String>> _operationError = new MutableLiveData<>();
    public LiveData<Event<String>> operationError = _operationError;

    private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();
    public LiveData<Event<NavigationRoute>> navigationCommand = _navigationCommand;

    private final MutableLiveData<Event<String>> _successMessage = new MutableLiveData<>();
    public LiveData<Event<String>> successMessage = _successMessage;


    // --- LiveData for Form Fields ---
    public final MutableLiveData<String> rawUserInputText = new MutableLiveData<>("");
    public final MutableLiveData<String> baseOfferPrice = new MutableLiveData<>("");
    public final MutableLiveData<String> baseOfferCurrency = new MutableLiveData<>("USD"); // Default currency

    // These will be populated/updated after LLM analysis OR directly by user if LLM is skipped
    public final MutableLiveData<String> postTitle = new MutableLiveData<>("");
    public final MutableLiveData<String> postDescription = new MutableLiveData<>("");
    public final MutableLiveData<ListingType> postListingType = new MutableLiveData<>();
    public final MutableLiveData<String> postCategory = new MutableLiveData<>("");

    // To hold local URIs of selected images. Uploading and getting URLs is deferred.
    private final MutableLiveData<List<Uri>> _selectedImageUris = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Uri>> selectedImageUris = _selectedImageUris;


    @Inject
    public CreatePostViewModel(@NonNull Application application,
                               AnalyzePostTextUseCase analyzePostTextUseCase,
                               SavePostUseCase savePostUseCase,
                               GetCurrentUserUseCase getCurrentUserUseCase) {
        super(application);
        this.analyzePostTextUseCase = analyzePostTextUseCase;
        this.savePostUseCase = savePostUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;

        // Get the LiveData for the current user from the use case
        this.currentListerLiveData = this.getCurrentUserUseCase.getFullUserProfile();
        this.currentUserErrorEvents = this.getCurrentUserUseCase.getProfileErrorEvents();

        // Start observing the user state when ViewModel is created.
        // The Fragment will call start/stop on its lifecycle for the use case.
        this.getCurrentUserUseCase.startObserving();
    }

    private void triggerLLMAnalysisAndPostCreation(
            String currentRawText,
            String currentBaseOfferPrice,
            String currentBaseOfferCurrency,
            final User lister,
            final List<Uri> localImageUris,
            final LocationData currentLocation) {

        Log.d(TAG, "triggerLLMAnalysisAndPostCreation called. Text: " + currentRawText);
        _isLoading.setValue(true);
        _operationError.setValue(null); // Clear previous errors

        // Image URIs are not sent to LLM for analysis in this simplified version.
        // The Cloud Function will only receive text and base offer details.
        analyzePostTextUseCase.execute(currentRawText, null, currentBaseOfferPrice, currentBaseOfferCurrency, new AnalyzePostTextUseCase.AnalysisCallbacks() {
            @Override
            public void onSuccess(@NonNull SuggestedPostDetails suggestions) {
                Log.d(TAG, "LLM Analysis Success: " + suggestions.toString());
                // Now construct and save the post using these suggestions and other user inputs
                constructAndSavePost(suggestions, currentBaseOfferPrice, lister, localImageUris, currentLocation);
            }

            @Override
            public void onError(@NonNull String message) {
                Log.e(TAG, "LLM Analysis Error: " + message);
                _isLoading.postValue(false);
                _operationError.postValue(new Event<>("AI Suggestion Error: " + message));
            }
        });
    }

    /**
     * Called when the user clicks the final "Create Post" button.
     * The Fragment is responsible for providing the current user location and selected image URIs.
     */
    public void onCreatePostClicked(@Nullable LocationData currentPostLocation, @Nullable List<Uri> currentSelectedImageUris) {
        User currentLister = currentListerLiveData.getValue();

        if (currentLister == null) {
            _operationError.setValue(new Event<>("User not authenticated or profile not loaded. Please try again."));
            Log.e(TAG, "onCreatePostClicked: Lister is null. Cannot create post.");
            return;
        }

        String currentRawText = rawUserInputText.getValue();
        if (currentRawText == null || currentRawText.trim().isEmpty()) {
            _operationError.setValue(new Event<>("Please describe what you want to post."));
            return;
        }

        if (currentPostLocation == null || currentPostLocation.latitude == null || currentPostLocation.longitude == null) {
            _operationError.setValue(new Event<>("Location is required for the post."));
            return;
        }

        String price = baseOfferPrice.getValue();
        String currency = baseOfferCurrency.getValue(); // Not directly on Post model, but price string might include it
        List<Uri> imageUris = currentSelectedImageUris != null ? currentSelectedImageUris : new ArrayList<>();


        triggerLLMAnalysisAndPostCreation(currentRawText, price, currency, currentLister, imageUris, currentPostLocation);
    }


    private void constructAndSavePost(
            SuggestedPostDetails suggestions,
            String userEnteredPrice,
            User lister,
            List<Uri> localImageUris,
            LocationData locationData
    ) {
        Log.d(TAG, "Constructing Post object with LLM suggestions and user inputs.");

        Post.Builder postBuilder = new Post.Builder();

        // From LLM Suggestions
        postBuilder.title(suggestions.getSuggestedTitle());
        postBuilder.description(suggestions.getSuggestedDescription());
        postBuilder.type(suggestions.getListingType()); // This is ListingType enum
        if (suggestions.getSuggestedCategory() != null) {
            postBuilder.category(suggestions.getSuggestedCategory());
        }

        // From Direct User Inputs / App Logic
        postBuilder.price(userEnteredPrice); // Price is a String in your Post model

        // Image handling: Store local URIs as strings. Uploading is deferred.
        if (localImageUris != null && !localImageUris.isEmpty()) {
            String[] imageUriStrings = new String[localImageUris.size()];
            for (int i = 0; i < localImageUris.size(); i++) {
                imageUriStrings[i] = localImageUris.get(i).toString();
            }
            postBuilder.imageUrl(imageUriStrings);
            Log.d(TAG, "Local Image URIs to be conceptually associated: " + Arrays.toString(imageUriStrings));
        } else {
            postBuilder.imageUrl(new String[0]); // Empty array if no images
        }


        postBuilder.lister(lister); // The authenticated user
        postBuilder.state(ListingState.NEW); // Default state
        postBuilder.requests(new String[0]);   // Initialize empty

        // Add location to Post object.
        // Your Post model needs fields like latitude, longitude, addressString.
        // This is conceptual until Post model is updated.
        Log.d(TAG, "Location to be added to post: " + locationData.toString());
        // Example (assuming Post model has these setters or builder methods):
        // postBuilder.latitude(locationData.latitude);
        // postBuilder.longitude(locationData.longitude);
        // if (locationData.addressString != null) {
        //     postBuilder.address(locationData.addressString); // Assuming an address field in Post
        // }


        Post newPostToSave = postBuilder.build();
        // TODO: Before saving, ensure your Post object in Firestore can store location (e.g., GeoPoint for lat/lon, and a string for address).
        // For now, the Post object is constructed but the savePostUseCase will need the Post model to support these fields.

        Log.d(TAG, "Attempting to save post: " + newPostToSave.getTitle());
        // _isLoading is already true from triggerLLMAnalysisAndPostCreation

        savePostUseCase.execute(newPostToSave, new SavePostUseCase.SavePostCallbacks() {
            @Override
            public void onSaveSuccess() {
                _isLoading.postValue(false);
                Log.i(TAG, "Post saved successfully! Title: " + newPostToSave.getTitle());
                _successMessage.postValue(new Event<>("Post created successfully!"));
                // Optionally navigate:
                // _navigationCommand.postValue(new Event<>(new NavigationRoute.Map())); // Or to "My Posts"
            }

            @Override
            public void onSaveError(@NonNull String message) {
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to save post: " + message);
                _operationError.postValue(new Event<>("Failed to save post: " + message));
            }
        });
    }

    // Methods for Fragment to update LiveData values
    public void onRawTextChanged(String text) {
        rawUserInputText.setValue(text);
    }

    public void onBaseOfferPriceChanged(String price) {
        baseOfferPrice.setValue(price);
    }

    public void onBaseOfferCurrencyChanged(String currency) {
        baseOfferCurrency.setValue(currency);
    }

    public void onUserSelectedLocalImageUris(List<Uri> uris) {
        _selectedImageUris.setValue(uris);
    }
    // Location is now passed directly to onCreatePostClicked by the Fragment

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "CreatePostViewModel onCleared. Stopping auth observation.");
        getCurrentUserUseCase.stopObserving(); // Stop observing when ViewModel is cleared
    }
}