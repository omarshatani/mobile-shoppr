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
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CreatePostViewModel extends AndroidViewModel {
    private static final String TAG = "CreatePostViewModel";

    private final AnalyzePostTextUseCase analyzePostTextUseCase;
    private final SavePostUseCase savePostUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    // GetCurrentDeviceLocationUseCase might not be directly used by this VM anymore
    // if location is always sourced from User profile for post creation.
    // private final GetCurrentDeviceLocationUseCase getCurrentDeviceLocationUseCase;

    public final LiveData<User> currentListerLiveData;

    // --- LiveData for UI State & LLM Interaction ---
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
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
    public final MutableLiveData<String> baseOfferCurrency = new MutableLiveData<>("USD");

    public final MutableLiveData<String> postTitle = new MutableLiveData<>("");
    public final MutableLiveData<String> postDescription = new MutableLiveData<>("");
    public final MutableLiveData<ListingType> postListingType = new MutableLiveData<>();
    public final MutableLiveData<String> postCategory = new MutableLiveData<>("");

    private final MutableLiveData<List<Uri>> _selectedImageUris = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Uri>> selectedImageUris = _selectedImageUris;

    // Removed _currentPostLocation LiveData as Fragment will derive it from currentListerLiveData


    @Inject
    public CreatePostViewModel(@NonNull Application application,
                               AnalyzePostTextUseCase analyzePostTextUseCase,
                               SavePostUseCase savePostUseCase,
                               GetCurrentUserUseCase getCurrentUserUseCase
            /*, GetCurrentDeviceLocationUseCase getCurrentDeviceLocationUseCase */) {
        super(application);
        this.analyzePostTextUseCase = analyzePostTextUseCase;
        this.savePostUseCase = savePostUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        // this.getCurrentDeviceLocationUseCase = getCurrentDeviceLocationUseCase;

        this.currentListerLiveData = this.getCurrentUserUseCase.getFullUserProfile();
        this.getCurrentUserUseCase.startObserving();
    }

    // fetchCurrentDeviceLocation is removed as Fragment won't directly trigger this for post location.
    // Location for post is assumed to be from User's profile.

    private void triggerLLMAnalysisAndPostCreation(
            String currentRawText,
            String currentBaseOfferPrice,
            String currentBaseOfferCurrency,
            final User lister,
            final List<Uri> localImageUris,
            final LocationData postCreationLocation) { // Location is passed in

        Log.d(TAG, "triggerLLMAnalysisAndPostCreation called. Text: " + currentRawText + ", Location: " + postCreationLocation);
        _isLoading.setValue(true);
        _operationError.setValue(null);

        analyzePostTextUseCase.execute(currentRawText, null, currentBaseOfferPrice, currentBaseOfferCurrency, new AnalyzePostTextUseCase.AnalysisCallbacks() {
            @Override
            public void onSuccess(@NonNull SuggestedPostDetails suggestions) {
                Log.d(TAG, "LLM Analysis Success: " + suggestions.toString());
                constructAndSavePost(suggestions, currentBaseOfferPrice, lister, localImageUris, postCreationLocation);
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
     * The Fragment is responsible for providing the user's current/default location.
     */
    public void onCreatePostClicked(@NonNull LocationData postLocation, @Nullable List<Uri> currentSelectedImageUris) {
        User currentLister = currentListerLiveData.getValue();

        if (currentLister == null) {
            _operationError.setValue(new Event<>("User not authenticated. Please log in."));
            Log.e(TAG, "onCreatePostClicked: Lister is null. Cannot create post.");
            return;
        }

        String currentRawText = rawUserInputText.getValue();
        if (currentRawText == null || currentRawText.trim().isEmpty()) {
            _operationError.setValue(new Event<>("Please describe what you want to post."));
            return;
        }

        // Location is now a required parameter passed by the Fragment
        if (postLocation.latitude == null || postLocation.longitude == null) {
            _operationError.setValue(new Event<>("Location is missing for the post."));
            return;
        }

        String price = baseOfferPrice.getValue();
        String currency = baseOfferCurrency.getValue();
        List<Uri> imageUris = currentSelectedImageUris != null ? currentSelectedImageUris : new ArrayList<>();

        triggerLLMAnalysisAndPostCreation(currentRawText, price, currency, currentLister, imageUris, postLocation);
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

        postBuilder.title(suggestions.getSuggestedTitle());
        postBuilder.description(suggestions.getSuggestedDescription());
        postBuilder.type(suggestions.getListingType());
        if (suggestions.getSuggestedCategory() != null) {
            postBuilder.category(suggestions.getSuggestedCategory());
        }

        postBuilder.price(userEnteredPrice);
        if (localImageUris != null && !localImageUris.isEmpty()) {
            String[] imageUriStrings = new String[localImageUris.size()];
            for (int i = 0; i < localImageUris.size(); i++) {
                imageUriStrings[i] = localImageUris.get(i).toString();
            }
            postBuilder.imageUrl(imageUriStrings);
        } else {
            postBuilder.imageUrl(new String[0]);
        }

        postBuilder.lister(lister);
        postBuilder.state(ListingState.NEW);
        postBuilder.requests(new String[0]);

        Log.d(TAG, "Location to be added to post: " + locationData.toString());
        // TODO: Update your Post.Builder and Post model to accept and store locationData
        // Example:
        // postBuilder.latitude(locationData.latitude);
        // postBuilder.longitude(locationData.longitude);
        // if (locationData.addressString != null) {
        //     postBuilder.address(locationData.addressString);
        // }

        Post newPostToSave = postBuilder.build();

        Log.d(TAG, "Attempting to save post: " + newPostToSave.getTitle());
        _isLoading.setValue(true);

        savePostUseCase.execute(newPostToSave, new SavePostUseCase.SavePostCallbacks() {
            @Override
            public void onSaveSuccess() {
                _isLoading.postValue(false);
                Log.i(TAG, "Post saved successfully! Title: " + newPostToSave.getTitle());
                _successMessage.postValue(new Event<>("Post created successfully!"));
                // _navigationCommand.postValue(new Event<>(new NavigationRoute.Map()));
            }

            @Override
            public void onSaveError(@NonNull String message) {
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to save post: " + message);
                _operationError.postValue(new Event<>("Failed to save post: " + message));
            }
        });
    }

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
    // Removed onUserSelectedLocation as Fragment will pass location directly to onCreatePostClicked


    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "CreatePostViewModel onCleared. Stopping user observation.");
        getCurrentUserUseCase.stopObserving();
    }
}