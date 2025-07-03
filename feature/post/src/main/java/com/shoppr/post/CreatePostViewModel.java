package com.shoppr.post;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetLLMSuggestionsUseCase;
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
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CreatePostViewModel extends AndroidViewModel {
    private static final String TAG = "CreatePostViewModel";

    private final GetLLMSuggestionsUseCase getLLMSuggestionsUseCase;
    private final SavePostUseCase savePostUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public final LiveData<User> currentListerLiveData;
    public final LiveData<Event<String>> currentUserErrorEvents;

    // Derived location from the current lister's profile
    private final MutableLiveData<LocationData> _postCreationLocation = new MutableLiveData<>();
    public LiveData<LocationData> postCreationLocation = _postCreationLocation;


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

    // These LiveData fields will be populated by the ViewModel after LLM analysis
    // and can be observed by the UI if needed for a review step (though current flow is direct).
    public final MutableLiveData<String> postTitle = new MutableLiveData<>("");
    public final MutableLiveData<String> postDescription = new MutableLiveData<>("");
    public final MutableLiveData<ListingType> postListingType = new MutableLiveData<>();
    public final MutableLiveData<String> postCategory = new MutableLiveData<>("");

    private final MutableLiveData<List<Uri>> _selectedImageUris = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Uri>> selectedImageUris = _selectedImageUris;

    private final Observer<User> listerObserver;


    @Inject
    public CreatePostViewModel(@NonNull Application application,
                               GetLLMSuggestionsUseCase getLLMSuggestionsUseCase,
                               SavePostUseCase savePostUseCase,
                               GetCurrentUserUseCase getCurrentUserUseCase) {
        super(application);
        this.getLLMSuggestionsUseCase = getLLMSuggestionsUseCase;
        this.savePostUseCase = savePostUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;

        this.currentListerLiveData = this.getCurrentUserUseCase.getFullUserProfile();
        this.currentUserErrorEvents = this.getCurrentUserUseCase.getProfileErrorEvents();

        listerObserver = this::getListerLocation; // Using method reference
        this.currentListerLiveData.observeForever(listerObserver);
        this.getCurrentUserUseCase.startObserving();
    }

    private void getListerLocation(User user) {
        if (user != null && user.getLastLatitude() != null && user.getLastLongitude() != null) {
            LocationData newLocation = new LocationData(
                    user.getLastLatitude(),
                    user.getLastLongitude(),
                    user.getLastLocationAddress()
            );
            boolean changed = isChanged(newLocation);
            if (changed) {
                _postCreationLocation.postValue(newLocation);
                Log.d(TAG, "Updated _postCreationLocation from lister: " + newLocation);
            }
        } else {
            if (_postCreationLocation.getValue() != null) {
                _postCreationLocation.postValue(null);
                Log.d(TAG, "_postCreationLocation set to null as lister or location is missing.");
            }
        }
    }

    private boolean isChanged(LocationData newLocation) {
        LocationData currentLocation = _postCreationLocation.getValue();
        if (currentLocation == null && newLocation == null) return false;
        if (currentLocation == null || newLocation == null) return true;

        boolean latChanged = !Objects.equals(currentLocation.latitude, newLocation.latitude);
        boolean lonChanged = !Objects.equals(currentLocation.longitude, newLocation.longitude);
        boolean addressChanged = !Objects.equals(currentLocation.addressString, newLocation.addressString);
        return latChanged || lonChanged || addressChanged;
    }


    private void triggerLLMAnalysisAndPostCreation(
            String currentRawText,
            String currentBaseOfferPriceFromInput,
            String currentBaseOfferCurrencyFromInput,
            final User lister,
            final List<Uri> localImageUris,
            final LocationData creationLocation) {

        Log.d(TAG, "triggerLLMAnalysisAndPostCreation called. Text: " + currentRawText + ", Location: " + creationLocation);
        _isLoading.setValue(true);
        _operationError.setValue(null);

        getLLMSuggestionsUseCase.execute(currentRawText, null, currentBaseOfferPriceFromInput, currentBaseOfferCurrencyFromInput, new GetLLMSuggestionsUseCase.AnalysisCallbacks() {
            @Override
            public void onSuccess(@NonNull SuggestedPostDetails suggestions) {
                Log.d(TAG, "LLM Analysis Success: " + suggestions);
                // Update LiveData fields (optional if UI doesn't show them before save, but good for consistency)
                postTitle.postValue(suggestions.getSuggestedTitle());
                postDescription.postValue(suggestions.getSuggestedDescription());
                postListingType.postValue(suggestions.getListingType());
                postCategory.postValue(suggestions.getSuggestedCategory());

                // Now construct and save the post, passing the fresh suggestions directly
                constructAndSavePost(suggestions, lister, localImageUris, creationLocation);
            }

            @Override
            public void onError(@NonNull String message) {
                Log.e(TAG, "LLM Analysis Error: " + message);
                _isLoading.postValue(false);
                _operationError.postValue(new Event<>("AI Suggestion Error: " + message));
            }
        });
    }

    public void onCreatePostClicked() {
        User currentLister = currentListerLiveData.getValue();
        LocationData currentPostLoc = _postCreationLocation.getValue();
        List<Uri> currentUris = _selectedImageUris.getValue();

        if (currentLister == null) {
            _operationError.setValue(new Event<>("User not authenticated. Please log in."));
            return;
        }

        String currentRawTextVal = rawUserInputText.getValue();
        if (currentRawTextVal == null || currentRawTextVal.trim().isEmpty()) {
            _operationError.setValue(new Event<>("Please describe what you want to post."));
            return;
        }

        if (currentPostLoc == null || currentPostLoc.latitude == null || currentPostLoc.longitude == null) {
            _operationError.setValue(new Event<>("Your location is not set. Please visit the map to update it."));
            return;
        }

        String priceVal = baseOfferPrice.getValue();
        String currencyVal = baseOfferCurrency.getValue();
        List<Uri> imageUrisForPost = currentUris != null ? currentUris : new ArrayList<>();

        triggerLLMAnalysisAndPostCreation(currentRawTextVal, priceVal, currencyVal, currentLister, imageUrisForPost, currentPostLoc);
    }


    private void constructAndSavePost(
            SuggestedPostDetails suggestions, // Now receiving suggestions directly
            User lister,
            List<Uri> localImageUris,
            LocationData locationData
    ) {
        Log.d(TAG, "Constructing Post object using direct suggestions.");

        Post.Builder postBuilder = new Post.Builder();

        // Use values directly from the suggestions object
        String title = suggestions.getSuggestedTitle();
        String description = suggestions.getSuggestedDescription();
        ListingType type = suggestions.getListingType();
        String category = suggestions.getSuggestedCategory();
        String price = baseOfferPrice.getValue(); // Price still comes from user's direct input

        if (title.trim().isEmpty() || description.trim().isEmpty()) {
            Log.e(TAG, "Cannot construct post, essential LLM-derived fields are missing from suggestions object.");
            _operationError.postValue(new Event<>("AI failed to suggest essential post details. Please try rephrasing your input."));
            _isLoading.postValue(false);
            return;
        }

        postBuilder.title(title);
        postBuilder.description(description);
        postBuilder.type(type);
        if (category != null) {
            postBuilder.category(category);
        }
        postBuilder.price(price);


        if (localImageUris != null && !localImageUris.isEmpty()) {
            List<String> imageUriStrings = new ArrayList<>();
            for (Uri uri : localImageUris) {
                imageUriStrings.add(uri.toString());
            }
            postBuilder.imageUrl(imageUriStrings);
        } else {
            postBuilder.imageUrl(new ArrayList<>());
        }

        postBuilder.lister(lister);
        postBuilder.state(ListingState.NEW); // As per your Post model
        postBuilder.requests(new ArrayList<>());

        if (locationData.latitude != null) {
            postBuilder.latitude(locationData.latitude);
        }
        if (locationData.longitude != null) {
            postBuilder.longitude(locationData.longitude);
        }
        if (locationData.addressString != null) {
            postBuilder.postAddress(locationData.addressString);
        }

        Post newPostToSave = postBuilder.build();

        Log.d(TAG, "Attempting to save post: " + newPostToSave.getTitle() + ", Type: " + newPostToSave.getType() + ", Category: " + newPostToSave.getCategory());
        // _isLoading is already true from triggerLLMAnalysisAndPostCreation

        savePostUseCase.execute(newPostToSave, new SavePostUseCase.SavePostCallbacks() {
            @Override
            public void onSaveSuccess() {
                _isLoading.postValue(false);
                _successMessage.postValue(new Event<>("Post created successfully!"));
                _navigationCommand.postValue(new Event<>(new NavigationRoute.Map()));
            }

            @Override
            public void onSaveError(@NonNull String message) {
                _isLoading.postValue(false);
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
        List<Uri> currentList = _selectedImageUris.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        List<Uri> updatedList = new ArrayList<>(currentList);
        boolean changed = false;
        for (Uri newUri : uris) {
            if (!updatedList.contains(newUri)) {
                updatedList.add(newUri);
                changed = true;
            }
        }
        if (changed) {
            _selectedImageUris.setValue(updatedList);
        }
    }

    public void removeSelectedImageUri(Uri uri) {
        List<Uri> currentUris = _selectedImageUris.getValue();
        if (currentUris != null) {
            List<Uri> updatedUris = new ArrayList<>(currentUris);
            if (updatedUris.remove(uri)) {
                _selectedImageUris.setValue(updatedUris);
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "CreatePostViewModel onCleared. Stopping user observation.");
        if (listerObserver != null) { // Check if observer was initialized
            currentListerLiveData.removeObserver(listerObserver);
        }
        getCurrentUserUseCase.stopObserving();
    }
}