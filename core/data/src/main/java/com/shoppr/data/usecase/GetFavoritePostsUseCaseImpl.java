package com.shoppr.data.usecase; // Or your implementation package

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.shoppr.domain.repository.PostRepository;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetFavoritePostsUseCase;
import com.shoppr.model.Post;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class GetFavoritePostsUseCaseImpl implements GetFavoritePostsUseCase {

	private final GetCurrentUserUseCase getCurrentUserUseCase;
	private final PostRepository postRepository;

	@Inject
	public GetFavoritePostsUseCaseImpl(
			GetCurrentUserUseCase getCurrentUserUseCase,
			PostRepository postRepository
	) {
		this.getCurrentUserUseCase = getCurrentUserUseCase;
		this.postRepository = postRepository;
	}

	@Override
	public LiveData<List<Post>> execute() {
		// This is a powerful pattern. We observe the user's profile.
		// Whenever it changes, we start a new query to get their favorite posts.
		return Transformations.switchMap(getCurrentUserUseCase.getFullUserProfile(), user -> {
			if (user == null || user.getFavoritePosts() == null || user.getFavoritePosts().isEmpty()) {
				// If there's no user or they have no favorites, return an empty list.
				MutableLiveData<List<Post>> emptyResult = new MutableLiveData<>();
				emptyResult.setValue(Collections.emptyList());
				return emptyResult;
			}
			// If they have favorites, ask the PostRepository to fetch them.
			return postRepository.getPostsByIds(user.getFavoritePosts());
		});
	}
}