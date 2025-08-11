package com.shoppr.post;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shoppr.model.Event;
import com.shoppr.model.Post;
import com.shoppr.navigation.NavigationRoute;
import com.shoppr.navigation.Navigator;
import com.shoppr.post.databinding.FragmentPostBinding;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.adapter.MyPostsAdapter;

import java.util.Collections;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PostFragment extends BaseFragment implements MyPostsAdapter.OnPostClickListener, MyPostsAdapter.OnFavoriteClickListener {

    private static final String TAG = "PostFragment";
    private FragmentPostBinding binding;
    private PostFragmentViewModel viewModel;
    private MyPostsAdapter myPostsAdapter;
    private NavController navController;

    @Inject
    Navigator navigator;

    public PostFragment() {
    }

    public static PostFragment newInstance() {
        return new PostFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PostFragmentViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupNavigation();
        setupRecyclerView();
        setupSwipeToRefresh();
        setupBindings();
        observeViewModel();
    }

    @Override
    protected InsetType getInsetType() {
        return InsetType.TOP_AND_BOTTOM;
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.startObservingUser();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.stopObservingUser();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupNavigation() {
        navController = NavHostFragment.findNavController(this);
        navigator.setNavController(navController);
    }

    private void observeViewModel() {
        viewModel.getNavigationCommand().observe(getViewLifecycleOwner(), event -> {
            NavigationRoute route = event.peekContent();
            if (route instanceof NavigationRoute.CreateNewPost) {
                NavigationRoute consumedRoute = event.getContentIfNotHandled();
                if (consumedRoute == null) {
                    return;
                }
                navigator.navigate(route);
            }
        });

        viewModel.posts.observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                Log.d(TAG, "Observed posts. Count: " + posts.size());
                myPostsAdapter.submitList(posts);
                updateEmptyState(posts.isEmpty());
            } else {
                Log.d(TAG, "Observed posts list was null.");
                updateEmptyState(true);
            }
        });

        // 2. Add a new observer for the user's profile to get their favorites list
        viewModel.currentUserProfileLiveData.observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getFavoritePosts() != null) {
                myPostsAdapter.setFavoritePostIds(user.getFavoritePosts());
            } else {
                myPostsAdapter.setFavoritePostIds(Collections.emptyList());
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null || isLoading == null) return;
            Log.d(TAG, "isLoading changed: " + isLoading);
            // We only react when loading is FINISHED to hide the spinner.
            // The user's swipe gesture is responsible for SHOWING it.
            // This prevents the spinner from flashing on initial load.
            if (!isLoading) {
                binding.swipeRefreshLayoutMyPosts.setRefreshing(false);
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), new Event.EventObserver<>(errorMessage -> {
            Log.e(TAG, "Error message: " + errorMessage);
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
        }));
    }

    private void setupSwipeToRefresh() {
        if (binding == null) return;
        // Set the listener for the swipe gesture
        binding.swipeRefreshLayoutMyPosts.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe to refresh triggered.");
            viewModel.refreshPosts();
        });
    }

    private void setupRecyclerView() {
        myPostsAdapter = new MyPostsAdapter(this, this);
        binding.recyclerViewMyPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewMyPosts.setAdapter(myPostsAdapter);
    }

    @Override
    public void onPostClicked(@NonNull Post post) {
        if (post.getId() == null) return;
        NavDirections action = PostFragmentDirections.actionPostToPostDetail(post.getId());
        navController.navigate(action);
    }

    @Override
    public void onFavoriteClick(@NonNull Post post) {
        viewModel.onFavoriteClicked(post);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (binding == null) return;
        if (isEmpty) {
            binding.recyclerViewMyPosts.setVisibility(View.GONE);
            binding.fabCreatePost.setVisibility(View.GONE);
            binding.layoutEmptyStateMyPosts.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewMyPosts.setVisibility(View.VISIBLE);
            binding.fabCreatePost.setVisibility(View.VISIBLE);
            binding.layoutEmptyStateMyPosts.setVisibility(View.GONE);
        }
    }

    private void setupBindings() {
        binding.buttonCreateFirstPost.setOnClickListener(v -> viewModel.navigateToCreatePost());
        binding.fabCreatePost.setOnClickListener(v -> viewModel.navigateToCreatePost());
    }

}