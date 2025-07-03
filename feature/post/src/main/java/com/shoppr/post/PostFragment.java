package com.shoppr.post;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shoppr.model.Event;
import com.shoppr.navigation.NavigationRoute;
import com.shoppr.navigation.Navigator;
import com.shoppr.post.databinding.FragmentPostBinding;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.utils.InsetUtils;

import javax.inject.Inject;

import adapter.MyPostsAdapter;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PostFragment extends BaseFragment {
    private static final String TAG = "PostFragment";
    private FragmentPostBinding binding;
    private PostFragmentViewModel viewModel;
    private MyPostsAdapter adapter;

    @Inject
    Navigator navigator;

    public static PostFragment newInstance() {
        return new PostFragment();
    }

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        setupRecyclerView();
        setupSwipeToRefresh();
        setupBindings();
        setupRootViewInsets(view);
        observeViewModel();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.startObservingUser();
        // The ViewModel will automatically fetch posts when the user is observed.
        // An explicit refresh might still be useful here if you want to guarantee fresh data on every start.
        // viewModel.refreshPosts();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.stopObservingUser();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.recyclerViewMyPosts.setAdapter(null);
        }
        adapter = null;
        binding = null;
    }

    private void setupRecyclerView() {
        adapter = new MyPostsAdapter(post -> {
            Log.d(TAG, "Clicked on post: " + post.getTitle());
            Toast.makeText(getContext(), "Clicked: " + post.getTitle() + " (TODO: Navigate to detail)", Toast.LENGTH_SHORT).show();
            // NavDirections action = PostFragmentDirections.actionPostFragmentToPostDetailFragment(post.getId());
            // NavHostFragment.findNavController(this).navigate(action);
        });
        binding.recyclerViewMyPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewMyPosts.setAdapter(adapter);
    }

    private void setupSwipeToRefresh() {
        if (binding == null) return;
        // Set the listener for the swipe gesture
        binding.swipeRefreshLayoutMyPosts.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe to refresh triggered.");
            viewModel.refreshPosts();
        });
    }

    private void observeViewModel() {
        viewModel.posts.observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                Log.d(TAG, "Observed posts. Count: " + posts.size());
                adapter.submitList(posts);
                updateEmptyState(posts.isEmpty());
            } else {
                Log.d(TAG, "Observed posts list was null.");
                updateEmptyState(true);
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

        viewModel.getNavigationCommand().observe(getViewLifecycleOwner(), event -> {
            if (event == null) return;
            NavigationRoute route = event.getContentIfNotHandled();
            if (route != null) {
                if (route instanceof NavigationRoute.PostsToCreatePost) {
                    NavDirections directions = PostFragmentDirections.actionPostFragmentToCreatePostFragment();
                    NavHostFragment.findNavController(this).navigate(directions);
                } else {
                    navigator.navigate(route);
                }
            }
        });
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

    private void setupRootViewInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            InsetUtils.applyBottomNavPadding(
                v,
                windowInsets,
                com.shoppr.core.ui.R.dimen.bottom_nav_height
            );
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }
}