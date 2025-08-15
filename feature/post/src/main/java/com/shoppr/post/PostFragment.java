package com.shoppr.post;

import android.os.Bundle;
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

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PostFragment extends BaseFragment implements MyPostsAdapter.OnPostClickListener {

	private FragmentPostBinding binding;
	private PostFragmentViewModel viewModel;
	private MyPostsAdapter myPostsAdapter;
	private NavController localNavigator;

	@Inject
	Navigator navigator;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(PostFragmentViewModel.class);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = FragmentPostBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setupLocalNavigation();
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

	private void setupLocalNavigation() {
		localNavigator = NavHostFragment.findNavController(this);
	}

	private void observeViewModel() {
		viewModel.getNavigationCommand().observe(getViewLifecycleOwner(), event -> {
			NavigationRoute route = event.peekContent();
			if (route instanceof NavigationRoute.CreatePost) {
				NavigationRoute consumedRoute = event.getContentIfNotHandled();
				if (consumedRoute == null) {
				}
				// You might need to adjust this navigation action based on your graph
				// localNavigator.navigate(R.id.action_post_fragment_to_create_post_fragment);
			}
		});

		viewModel.posts.observe(getViewLifecycleOwner(), posts -> {
			if (posts != null) {
				myPostsAdapter.submitList(posts);
				updateEmptyState(posts.isEmpty());
			} else {
				updateEmptyState(true);
			}
		});

		viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
			if (binding != null && isLoading != null && !isLoading) {
				binding.swipeRefreshLayoutMyPosts.setRefreshing(false);
			}
		});

		viewModel.errorMessage.observe(getViewLifecycleOwner(), new Event.EventObserver<>(errorMessage -> {
			Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
		}));
	}

	private void setupSwipeToRefresh() {
		if (binding == null) return;
		binding.swipeRefreshLayoutMyPosts.setOnRefreshListener(() -> viewModel.refreshPosts());
	}

	private void setupRecyclerView() {
		myPostsAdapter = new MyPostsAdapter(this);
		binding.recyclerViewMyPosts.setLayoutManager(new LinearLayoutManager(getContext()));
		binding.recyclerViewMyPosts.setAdapter(myPostsAdapter);
	}

	@Override
	public void onPostClicked(@NonNull Post post) {
		if (post.getId() == null) return;
		NavDirections action = PostFragmentDirections.actionPostToPostDetail(post.getId());
		localNavigator.navigate(action);
	}

	private void updateEmptyState(boolean isEmpty) {
		if (binding == null) return;
		binding.layoutEmptyStateMyPosts.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
		binding.fabCreatePost.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
		binding.recyclerViewMyPosts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
	}

	private void setupBindings() {
		binding.buttonCreateFirstPost.setOnClickListener(v -> viewModel.navigateToCreatePost());
		binding.fabCreatePost.setOnClickListener(v -> viewModel.navigateToCreatePost());
	}
}