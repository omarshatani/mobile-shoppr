package com.shoppr.post;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetLLMSuggestionsUseCase;
import com.shoppr.domain.usecase.SavePostUseCase;
import com.shoppr.model.Event;
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
public class CreatePostViewModel extends ViewModel {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final SavePostUseCase savePostUseCase;
    private final GetLLMSuggestionsUseCase getLLMSuggestionsUseCase;

    public final LiveData<User> currentListerLiveData;
    public final MutableLiveData<List<Uri>> selectedImageUris = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<Event<String>> operationError = new MutableLiveData<>();
    public final MutableLiveData<Event<String>> successMessage = new MutableLiveData<>();
    public final MutableLiveData<Event<NavigationRoute>> navigationCommand = new MutableLiveData<>();
    public final MutableLiveData<LocationData> postCreationLocation = new MutableLiveData<>();

    @Inject
    public CreatePostViewModel(
        GetCurrentUserUseCase getCurrentUserUseCase,
        SavePostUseCase savePostUseCase,
        GetLLMSuggestionsUseCase getLLMSuggestionsUseCase
    ) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.savePostUseCase = savePostUseCase;
        this.getLLMSuggestionsUseCase = getLLMSuggestionsUseCase;
        this.currentListerLiveData = this.getCurrentUserUseCase.getFullUserProfile();
    }

    public void onUserSelectedLocalImageUris(List<Uri> uris) {
        List<Uri> currentUris = new ArrayList<>(selectedImageUris.getValue());
        currentUris.addAll(uris);
        selectedImageUris.setValue(currentUris);
    }

    public void removeSelectedImageUri(Uri uri) {
        List<Uri> currentUris = new ArrayList<>(selectedImageUris.getValue());
        currentUris.remove(uri);
        selectedImageUris.setValue(currentUris);
    }

    /**
     * This is the main entry point called when the "Create Post" button is clicked.
     * It orchestrates the entire flow: get suggestions, then create the post.
     */
    public void onCreatePostClicked(String rawText, String price, String currency) {
        User currentUser = currentListerLiveData.getValue();
        if (currentUser == null) {
            operationError.setValue(new Event<>("You must be logged in to create a post."));
            return;
        }

        if (TextUtils.isEmpty(rawText)) {
            operationError.setValue(new Event<>("Please provide a description."));
            return;
        }

        isLoading.setValue(true);

        getLLMSuggestionsUseCase.execute(rawText, null, price, currency, new GetLLMSuggestionsUseCase.LLMAnalysisCallbacks() {
            @Override
            public void onSuccess(SuggestedPostDetails suggestions) {
                createAndSavePost(suggestions, currentUser, price, currency);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                operationError.postValue(new Event<>(message));
            }
        });
    }

    private void createAndSavePost(SuggestedPostDetails suggestions, User currentUser, String originalPrice, String originalCurrency) {
        List<Uri> images = selectedImageUris.getValue();
        if (images == null || images.isEmpty()) {
            operationError.setValue(new Event<>("Please add at least one image."));
            isLoading.setValue(false);
            return;
        }

        String finalPrice = suggestions.getPrice() != null ? String.valueOf(suggestions.getPrice()) : originalPrice;
        String finalCurrency = suggestions.getCurrency() != null ? suggestions.getCurrency() : originalCurrency;

        Post newPost = new Post.Builder()
            .title(suggestions.getTitle())
            .description(suggestions.getDescription())
            .price(finalPrice)
            .currency(finalCurrency)
            .categories(suggestions.getCategories())
            .lister(currentUser)
            .build();

        savePostUseCase.execute(newPost, images, new SavePostUseCase.SavePostCallback() {
            @Override
            public void onSuccess(@NonNull Post createdPost) {
                isLoading.postValue(false);
                successMessage.postValue(new Event<>("Post created successfully!"));
                navigationCommand.postValue(new Event<>(new NavigationRoute.Map()));
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                operationError.postValue(new Event<>(message));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        getCurrentUserUseCase.stopObserving();
    }
}