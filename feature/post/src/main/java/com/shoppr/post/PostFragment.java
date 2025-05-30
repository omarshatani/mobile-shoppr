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

        setupRecyclerView(); // Initialize RecyclerView and Adapter
        setupBindings();    // Your existing method, now also handles FAB
        observeViewModel();
        setupRootViewInsets(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Tell ViewModel to start observing user and fetching posts
        viewModel.startObservingUser();
        // Optionally, trigger a refresh if needed, though ViewModel might do it on user observation
        // viewModel.refreshPosts();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.stopObservingUser();
    }

    @Override
    public void onDestroyView() { // Changed from onDestroy to onDestroyView
        super.onDestroyView();
        binding.recyclerViewMyPosts.setAdapter(null); // Important for RecyclerView cleanup
        adapter = null;
        binding = null;
    }

    private void setupRecyclerView() {
        // Assuming MyPostsAdapter and its PostDiffCallback are accessible
        // (e.g., in this package or an imported one)
        adapter = new MyPostsAdapter(post -> { // Using the constructor that only takes OnPostClickListener
            Log.d(TAG, "Clicked on post: " + post.getTitle());
            Toast.makeText(getContext(), "Clicked: " + post.getTitle() + " (TODO: Navigate to detail)", Toast.LENGTH_SHORT).show();
            // TODO: Implement navigation to post detail screen
            // Example:
            // NavDirections action = PostFragmentDirections.actionPostFragmentToPostDetailFragment(post.getId());
            // NavHostFragment.findNavController(this).navigate(action);
        });
        binding.recyclerViewMyPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewMyPosts.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Observe the list of posts
        viewModel.posts.observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                Log.d(TAG, "Observed posts. Count: " + posts.size());
                adapter.submitList(posts); // Update the adapter with the new list
                updateEmptyState(posts.isEmpty());
            } else {
                Log.d(TAG, "Observed posts list was null.");
                updateEmptyState(true); // Treat null list as empty
            }
        });

        // Observe loading state
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "isLoading changed: " + isLoading);
            // TODO: Implement a more sophisticated loading indicator if needed
            // For example, a SwipeRefreshLayout or a ProgressBar in the center
            // binding.swipeRefreshLayout.setRefreshing(isLoading);
        });

        // Observe error messages
        viewModel.errorMessage.observe(getViewLifecycleOwner(), new Event.EventObserver<>(errorMessage -> {
            Log.e(TAG, "Error message: " + errorMessage);
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
        }));

        // Your existing navigation command observer
        viewModel.getNavigationCommand().observe(getViewLifecycleOwner(), event -> {
            if (event == null) return;
            NavigationRoute route = event.getContentIfNotHandled(); // Consume event
            if (route != null) {
                if (route instanceof NavigationRoute.PostsToCreatePost) {
                    // Navigate using NavController directly as per your existing setupBindings
                    NavDirections directions = PostFragmentDirections.actionPostFragmentToCreatePostFragment();
                    NavHostFragment.findNavController(this).navigate(directions);
                } else {
                    // Handle other potential navigation commands if any
                    navigator.navigate(route);
                }
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (binding == null) return; // View already destroyed
        if (isEmpty) {
            binding.recyclerViewMyPosts.setVisibility(View.GONE);
            binding.fabCreatePost.setVisibility(View.GONE); // Hide FAB when list is empty
            binding.layoutEmptyStateMyPosts.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewMyPosts.setVisibility(View.VISIBLE);
            binding.fabCreatePost.setVisibility(View.VISIBLE); // Show FAB when list has items
            binding.layoutEmptyStateMyPosts.setVisibility(View.GONE);
        }
    }

    private void setupBindings() {
        // This is the button in your empty state layout
        binding.buttonCreateNewPostEmptyState.setOnClickListener(v -> {
            viewModel.navigateToCreatePost();
        });

        // This is the FloatingActionButton
        binding.fabCreatePost.setOnClickListener(v -> {
            viewModel.navigateToCreatePost();
        });
    }

    private void setupRootViewInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            InsetUtils.applyBottomNavPadding(
                    v,
                    windowInsets,
                    com.shoppr.core.ui.R.dimen.bottom_nav_height
            );
            InsetUtils.applyStatusBarInsetAsPaddingTop(v, windowInsets);
            return windowInsets;
        });
    }
}