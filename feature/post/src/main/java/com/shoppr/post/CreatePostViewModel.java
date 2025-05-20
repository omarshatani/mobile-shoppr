package com.shoppr.post;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shoppr.domain.AnalyzePostTextUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.ListingType;
import com.shoppr.model.LocationData;
import com.shoppr.model.SuggestedPostDetails;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CreatePostViewModel extends AndroidViewModel {
	private static final String TAG = "CreatePostViewModel";

	private final AnalyzePostTextUseCase analyzePostTextUseCase;
	// private final SavePostUseCase savePostUseCase; // Will be injected later
	// private final GetCurrentUserUseCase getCurrentUserUseCase; // Will be injected later

	// --- LiveData for LLM Suggestions & UI State ---

	private final MutableLiveData<SuggestedPostDetails> _suggestedPostDetails = new MutableLiveData<>();
	public LiveData<SuggestedPostDetails> suggestedPostDetails = _suggestedPostDetails;

	private final MutableLiveData<Boolean> _isLoadingSuggestions = new MutableLiveData<>(false);
	public LiveData<Boolean> isLoadingSuggestions = _isLoadingSuggestions;

	private final MutableLiveData<Event<String>> _analysisError = new MutableLiveData<>();
	public LiveData<Event<String>> analysisError = _analysisError;

	// --- LiveData for user inputs that are part of the final Post ---

	private final MutableLiveData<String> _postTitle = new MutableLiveData<>();
	public LiveData<String> postTitle = _postTitle;

	private final MutableLiveData<String> _postDescription = new MutableLiveData<>();
	public LiveData<String> postDescription = _postDescription;

	private final MutableLiveData<String> _postPrice = new MutableLiveData<>(); // User's base offer
	public LiveData<String> postPrice = _postPrice;

	private final MutableLiveData<ListingType> _postListingType = new MutableLiveData<>();
	public LiveData<ListingType> postListingType = _postListingType;

	private final MutableLiveData<String> _postCategory = new MutableLiveData<>();
	public LiveData<String> postCategory = _postCategory;

	private final MutableLiveData<List<String>> _postImageUrls = new MutableLiveData<>();
	public LiveData<List<String>> postImageUrls = _postImageUrls;

	// LiveData for location
	private final MutableLiveData<LocationData> _postLocation = new MutableLiveData<>();
	public LiveData<LocationData> postLocation = _postLocation;


	@Inject
	public CreatePostViewModel(@NonNull Application application,
							   AnalyzePostTextUseCase analyzePostTextUseCase
			/*, SavePostUseCase savePostUseCase, GetCurrentUserUseCase getCurrentUserUseCase */) {
		super(application);
		this.analyzePostTextUseCase = analyzePostTextUseCase;
		// this.savePostUseCase = savePostUseCase;
		// this.getCurrentUserUseCase = getCurrentUserUseCase;
	}

	/**
	 * Called by the Fragment when the user submits their raw text and base offer
	 * for LLM analysis.
	 */
	public void analyzeUserInputs(@NonNull String rawText, @Nullable String baseOfferPriceStr, @Nullable String baseOfferCurrency) {
		Log.d(TAG, "analyzeUserInputs called. Text: " + rawText + ", Price: " + baseOfferPriceStr);
		_isLoadingSuggestions.setValue(true);
		_analysisError.setValue(null); // Clear previous errors
		_suggestedPostDetails.setValue(null); // Clear previous suggestions

		Double baseOfferPrice = null;
		if (baseOfferPriceStr != null && !baseOfferPriceStr.trim().isEmpty()) {
			try {
				baseOfferPrice = Double.parseDouble(baseOfferPriceStr.trim());
			} catch (NumberFormatException e) {
				Log.w(TAG, "Invalid base offer price format: " + baseOfferPriceStr);
			}
		}

		analyzePostTextUseCase.execute(rawText, baseOfferPrice, baseOfferCurrency, new AnalyzePostTextUseCase.AnalysisCallbacks() {
			@Override
			public void onSuccess(@NonNull SuggestedPostDetails suggestions) {
				Log.d(TAG, "LLM Analysis Success: " + suggestions);
				_isLoadingSuggestions.postValue(false);
				_suggestedPostDetails.postValue(suggestions);

				_postTitle.postValue(suggestions.getSuggestedTitle());
				_postDescription.postValue(suggestions.getSuggestedDescription());
				_postCategory.postValue(suggestions.getSuggestedCategory());
				try {
					ListingType type = ListingType.valueOf(suggestions.getListingType().toString().toUpperCase());
					_postListingType.postValue(type);
				} catch (IllegalArgumentException e) {
					Log.e(TAG, "Invalid listingType received from LLM: " + suggestions.getListingType(), e);
					_analysisError.postValue(new Event<>("Received invalid post type from AI."));
				}
			}

			@Override
			public void onError(@NonNull String message) {
				Log.e(TAG, "LLM Analysis Error: " + message);
				_isLoadingSuggestions.postValue(false);
				_analysisError.postValue(new Event<>(message));
			}
		});
	}

	// --- Methods to be called by the Fragment to update user inputs ---
	public void updateTitle(String title) {
		_postTitle.setValue(title);
	}

	public void updateDescription(String description) {
		_postDescription.setValue(description);
	}

	public void updatePrice(String price) {
		_postPrice.setValue(price);
	}

	public void updateListingType(ListingType type) {
		_postListingType.setValue(type);
	}

	public void updateCategory(String category) {
		_postCategory.setValue(category);
	}

	public void updateImageUrls(List<String> urls) {
		_postImageUrls.setValue(urls);
	}

	public void updateLocation(LocationData location) {
		_postLocation.setValue(location);
	}


	/**
	 * Called when the user is ready to create the post after reviewing/editing.
	 */
	public void createPost() {
		// User currentLister = getCurrentUserUseCase.execute(); // Get current authenticated user
		// if (currentLister == null) {
		//     _analysisError.setValue(new Event<>("User not authenticated. Cannot create post."));
		//     return;
		// }

		String title = _postTitle.getValue();
		ListingType type = _postListingType.getValue();

		if (title == null || title.trim().isEmpty() || type == null) {
			_analysisError.setValue(new Event<>("Title and Post Type are required."));
			return;
		}

		LocationData location = _postLocation.getValue();
		if (location == null) {
			_analysisError.setValue(new Event<>("Location is required."));
			return;
		}
		Log.d(TAG, "Attempting to create post. Title: " + title + ", Type: " + type.name() + ", Location: " + location.latitude + "," + location.longitude);

		// Conceptual: Your Post model would need fields for location.
		// For example, if your Post model had `latitude`, `longitude`, and `addressString` fields:
		// Post.Builder postBuilder = new Post.Builder()
		//        .title(title)
		//        .description(_postDescription.getValue())
		//        .price(_postPrice.getValue())
		//        .type(type)
		//        .state(ListingState.ACTIVE) // Default state
		//        .category(_postCategory.getValue())
		//        .imageUrl(_postImageUrls.getValue() != null ? _postImageUrls.getValue().toArray(new String[0]) : null)
		//        .lister(currentLister)
		//        .requests(new String[0]) // Initialize empty
		//        .latitude(location.latitude) // Example
		//        .longitude(location.longitude) // Example
		//        .address(location.addressString); // Example

		// Post postToSave = postBuilder.build();

		// _isLoadingSuggestions.setValue(true); // Can reuse for saving progress
		// savePostUseCase.execute(postToSave, new SavePostUseCase.SaveCallbacks() {
		//    @Override
		//    public void onSaveSuccess() {
		//        _isLoadingSuggestions.postValue(false);
		//        Log.d(TAG, "Post saved successfully!");
		//        // Navigate or show success message
		//    }
		//    @Override
		//    public void onSaveError(String message) {
		//        _isLoadingSuggestions.postValue(false);
		//        _analysisError.postValue(new Event<>("Failed to save post: " + message));
		//    }
		// });

		Log.d(TAG, "Placeholder: SavePostUseCase would be called here with location data.");
		// For now, let's use the existing toast message LiveData for feedback
		_analysisError.setValue(new Event<>("Post creation logic (with location) not fully implemented yet."));
	}
}