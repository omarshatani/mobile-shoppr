package com.shoppr.post;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
import java.util.Arrays;
import java.util.Collections;
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

	public final MutableLiveData<String> postTitle = new MutableLiveData<>("");
	public final MutableLiveData<String> postDescription = new MutableLiveData<>("");
	public final MutableLiveData<ListingType> postListingType = new MutableLiveData<>();
	public final MutableLiveData<String> postCategory = new MutableLiveData<>("");

	private final MutableLiveData<List<Uri>> _selectedImageUris = new MutableLiveData<>(new ArrayList<>());
	public LiveData<List<Uri>> selectedImageUris = _selectedImageUris;


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

		// Observe currentListerLiveData to derive postCreationLocation
		// Using observeForever here needs careful handling in onCleared if ViewModel can outlive observer's lifecycle owner.
		// However, for AndroidViewModel, it's tied to Application lifecycle, so less risky.
		// Alternatively, use Transformations.map if you prefer.
		this.currentListerLiveData.observeForever(this::getLocationFromLoggedUser);
		this.getCurrentUserUseCase.startObserving();
	}

	private void getLocationFromLoggedUser(User user) {
		if (user != null && user.getLastLatitude() != null && user.getLastLongitude() != null) {
			LocationData newLocation = new LocationData(
					user.getLastLatitude(),
					user.getLastLongitude(),
					user.getLastLocationAddress()
			);
			// Only update if it's different to avoid unnecessary emissions
			// This requires LocationData to have a proper equals method or compare fields.
			LocationData currentLocation = _postCreationLocation.getValue();
			if (currentLocation == null ||
					!Objects.equals(currentLocation.latitude, newLocation.latitude) ||
					!Objects.equals(currentLocation.longitude, newLocation.longitude) ||
					!Objects.equals(currentLocation.addressString, newLocation.addressString)) {
				_postCreationLocation.postValue(newLocation);
			}
		} else {
			if (_postCreationLocation.getValue() != null) {
				_postCreationLocation.postValue(null); // Lister or their location is gone
			}
		}
	}

	private void triggerLLMAnalysisAndPostCreation(
			String currentRawText,
			String currentBaseOfferPrice,
			String currentBaseOfferCurrency,
			final User lister,
			final List<Uri> localImageUris,
			final LocationData creationLocation) {

		Log.d(TAG, "triggerLLMAnalysisAndPostCreation called. Text: " + currentRawText + ", Location: " + creationLocation);
		_isLoading.setValue(true);
		_operationError.setValue(null);

		getLLMSuggestionsUseCase.execute(currentRawText, null, currentBaseOfferPrice, currentBaseOfferCurrency, new GetLLMSuggestionsUseCase.AnalysisCallbacks() {
			@Override
			public void onSuccess(@NonNull SuggestedPostDetails suggestions) {
				Log.d(TAG, "LLM Analysis Success: " + suggestions);
				constructAndSavePost(suggestions, currentBaseOfferPrice, lister, localImageUris, creationLocation);
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

		String currentRawText = rawUserInputText.getValue();
		if (currentRawText == null || currentRawText.trim().isEmpty()) {
			_operationError.setValue(new Event<>("Please describe what you want to post."));
			return;
		}

		if (currentPostLoc == null || currentPostLoc.latitude == null || currentPostLoc.longitude == null) {
			_operationError.setValue(new Event<>("Your location is not set. Please visit the map to update it."));
			return;
		}

		String price = baseOfferPrice.getValue();
		String currency = baseOfferCurrency.getValue();
		List<Uri> imageUrisForPost = currentUris != null ? currentUris : new ArrayList<>();

		triggerLLMAnalysisAndPostCreation(currentRawText, price, currency, currentLister, imageUrisForPost, currentPostLoc);
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
		// ID will be generated by Firestore or PostDataSource when saving a new post.
		// If updating an existing post, the ID would be set.

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
			postBuilder.imageUrl(Arrays.asList(imageUriStrings)); // Storing local URIs as strings for now
		} else {
			postBuilder.imageUrl(Collections.emptyList());
		}

		postBuilder.lister(lister);
		postBuilder.state(ListingState.NEW); // Default state
		postBuilder.requests(Collections.emptyList());   // Initialize empty

		// Set location fields using the Post.Builder methods
		if (locationData.latitude != null) {
			postBuilder.latitude(locationData.latitude);
		}
		if (locationData.longitude != null) {
			postBuilder.longitude(locationData.longitude);
		}
		if (locationData.addressString != null) {
			postBuilder.postAddress(locationData.addressString);
		}
		Log.d(TAG, "Location data set on Post.Builder: " + locationData);

		Post newPostToSave = postBuilder.build();

		Log.d(TAG, "Attempting to save post: " + newPostToSave.getTitle());
		_isLoading.setValue(true); // Already set by triggerLLMAnalysisAndPostCreation

		savePostUseCase.execute(newPostToSave, new SavePostUseCase.SavePostCallbacks() {
			@Override
			public void onSaveSuccess() {
				_isLoading.postValue(false);
				Log.i(TAG, "Post saved successfully! Title: " + newPostToSave.getTitle());
				_successMessage.postValue(new Event<>("Post created successfully!"));
				// Optionally navigate after success
				_navigationCommand.postValue(new Event<>(new NavigationRoute.Map()));
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
		currentListerLiveData.removeObserver(this::getLocationFromLoggedUser);
		getCurrentUserUseCase.stopObserving();
	}
}