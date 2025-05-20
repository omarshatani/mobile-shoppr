package com.shoppr.post;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shoppr.domain.AnalyzePostTextUseCase;
import com.shoppr.domain.GetCurrentUserUseCase;
import com.shoppr.domain.SavePostUseCase;
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

	// LiveData for the currently authenticated user (with full profile)
	public final LiveData<User> currentListerLiveData;

	// --- LiveData for UI State & LLM Interaction ---
	private final MutableLiveData<Boolean> _isLoadingSuggestions = new MutableLiveData<>(false);
	public LiveData<Boolean> isLoadingSuggestions = _isLoadingSuggestions;

	private final MutableLiveData<Event<String>> _operationError = new MutableLiveData<>();
	public LiveData<Event<String>> operationError = _operationError;

	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();
	public LiveData<Event<NavigationRoute>> navigationCommand = _navigationCommand;

	private final MutableLiveData<Event<String>> _successMessage = new MutableLiveData<>();
	public LiveData<Event<String>> successMessage = _successMessage;


	// --- LiveData for Form Fields (can be bound two-way or updated via methods) ---
	public final MutableLiveData<String> rawUserInputText = new MutableLiveData<>("");
	public final MutableLiveData<String> baseOfferPrice = new MutableLiveData<>("");
	public final MutableLiveData<String> baseOfferCurrency = new MutableLiveData<>("USD"); // Default currency

	public final MutableLiveData<String> postTitle = new MutableLiveData<>("");
	public final MutableLiveData<String> postDescription = new MutableLiveData<>("");
	public final MutableLiveData<ListingType> postListingType = new MutableLiveData<>();
	public final MutableLiveData<String> postCategory = new MutableLiveData<>("");
	public final MutableLiveData<List<String>> postImageUrls = new MutableLiveData<>(new ArrayList<>());
	public final MutableLiveData<LocationData> postLocation = new MutableLiveData<>();


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
		// Ensure the underlying observation for GetCurrentUserUseCase is started,
		// typically by a higher-level ViewModel (e.g., MainViewModel) or in MainActivity.
		// If GetCurrentUserUseCase needs explicit start/stop, this ViewModel might call it in onActive/onInactive
		// or tied to the Fragment's lifecycle if this ViewModel is scoped to the Fragment.
		// For simplicity, we assume GetCurrentUserUseCase.startObserving() is called elsewhere appropriately.
	}

	private void triggerLLMAnalysisForSuggestions(String currentRawText, String currentBaseOfferPrice, String currentBaseOfferCurrency,
																								final User lister, final List<String> currentImageUrls, final LocationData currentLocation) {
		Log.d(TAG, "triggerLLMAnalysisForSuggestions called. Text: " + currentRawText);
		_isLoadingSuggestions.setValue(true);
		_operationError.setValue(null);

		analyzePostTextUseCase.execute(currentRawText, null, currentBaseOfferPrice, currentBaseOfferCurrency, new AnalyzePostTextUseCase.AnalysisCallbacks() {
			@Override
			public void onSuccess(@NonNull SuggestedPostDetails suggestions) {
				Log.d(TAG, "LLM Analysis Success: " + suggestions.toString());
				constructAndSavePost(suggestions, currentBaseOfferPrice, currentBaseOfferCurrency, lister, currentImageUrls, currentLocation);
			}

			@Override
			public void onError(@NonNull String message) {
				Log.e(TAG, "LLM Analysis Error: " + message);
				_isLoadingSuggestions.postValue(false);
				_operationError.postValue(new Event<>("AI Suggestion Error: " + message));
			}
		});
	}

	public void onCreatePostClicked() {
		User currentLister = currentListerLiveData.getValue(); // Get the current user

		if (currentLister == null) {
			_operationError.setValue(new Event<>("User not authenticated or profile not loaded. Please wait or try logging in again."));
			Log.e(TAG, "onCreatePostClicked: Lister is null. User might not be authenticated or GetCurrentUserUseCase hasn't emitted yet.");
			return;
		}

		String currentRawText = rawUserInputText.getValue();
		if (currentRawText == null || currentRawText.trim().isEmpty()) {
			_operationError.setValue(new Event<>("Please describe what you want to post."));
			return;
		}

		String price = baseOfferPrice.getValue();
		String currency = baseOfferCurrency.getValue();
		List<String> imageUrls = postImageUrls.getValue() != null ? postImageUrls.getValue() : new ArrayList<>();
		LocationData location = postLocation.getValue();

		if (location == null || location.latitude == null || location.longitude == null) {
			_operationError.setValue(new Event<>("Please set a location for your post."));
			return;
		}

		triggerLLMAnalysisForSuggestions(currentRawText, price, currency, currentLister, imageUrls, location);
	}


	private void constructAndSavePost(
			SuggestedPostDetails suggestions,
			String userEnteredPrice,
			String userEnteredCurrency,
			User lister,
			List<String> imageUrls,
			LocationData locationInput
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
		if (imageUrls != null && !imageUrls.isEmpty()) {
			postBuilder.imageUrl(imageUrls.toArray(new String[0]));
		}
		postBuilder.lister(lister);
		postBuilder.state(ListingState.NEW);
		postBuilder.requests(new String[0]);

		// Conceptual: Add location to Post object.
		// Your Post model and builder need to support these fields.
		// For example:
		// if (yourPostModelSupportsGeoPoint) {
		//     postBuilder.location(new com.google.firebase.firestore.GeoPoint(locationInput.latitude, locationInput.longitude));
		// }
		// if (yourPostModelSupportsAddressString) {
		//     postBuilder.address(locationInput.addressString);
		// }
		// For now, we assume you'll adapt your Post.Builder to accept location details.
		Log.d(TAG, "Location to be added to post: Lat " + locationInput.latitude + ", Lon " + locationInput.longitude);


		Post newPostToSave = postBuilder.build();

		Log.d(TAG, "Attempting to save post: " + newPostToSave.getTitle());
		_isLoadingSuggestions.setValue(true); // Re-use loading state for save operation

		savePostUseCase.execute(newPostToSave, new SavePostUseCase.SavePostCallbacks() {
			@Override
			public void onSaveSuccess() {
				_isLoadingSuggestions.postValue(false);
				Log.i(TAG, "Post saved successfully! Title: " + newPostToSave.getTitle());
				_successMessage.postValue(new Event<>("Post created successfully!"));
				// Optionally navigate:
				// _navigationCommand.postValue(new Event<>(new NavigationRoute.Map())); // Or to "My Posts"
			}

			@Override
			public void onSaveError(@NonNull String message) {
				_isLoadingSuggestions.postValue(false);
				Log.e(TAG, "Failed to save post: " + message);
				_operationError.postValue(new Event<>("Failed to save post: " + message));
			}
		});
	}

	public void onRawTextChanged(String text) { rawUserInputText.setValue(text); }
	public void onBaseOfferPriceChanged(String price) { baseOfferPrice.setValue(price); }
	public void onBaseOfferCurrencyChanged(String currency) { baseOfferCurrency.setValue(currency); }
	public void onUserSelectedImages(List<String> urls) { postImageUrls.setValue(urls); }
	public void onUserSelectedLocation(LocationData location) { postLocation.setValue(location); }

}