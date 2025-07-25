package com.shoppr.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.chip.Chip;
import com.shoppr.core.ui.R;
import com.shoppr.post.databinding.FragmentPostDetailBinding;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.utils.ImageLoader;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PostDetailFragment extends BaseFragment {

	private FragmentPostDetailBinding binding;
	private PostDetailViewModel viewModel;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = FragmentPostDetailBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setupToolbar();

		String postId = PostDetailFragmentArgs.fromBundle(getArguments()).getPostId();
		viewModel.loadPostDetails(postId);

		observeViewModel();
	}

	@Override
	public void onStart() {
		super.onStart();
		viewModel.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		viewModel.onStop();
	}

	private void setupToolbar() {
		NavController navController = NavHostFragment.findNavController(this);
		NavigationUI.setupWithNavController(binding.topAppBar, navController);

		binding.topAppBar.setOnMenuItemClickListener(menuItem -> {
			if (menuItem.getItemId() == com.shoppr.post.R.id.action_favorite) {
				viewModel.toggleFavorite();
				return true;
			}
			return false;
		});
	}

	private void observeViewModel() {
		viewModel.getSelectedPost().observe(getViewLifecycleOwner(), post -> {
			if (post != null) {
				binding.detailPostHeadline.setText(post.getTitle());
				binding.detailPostDescription.setText(post.getDescription());
				binding.detailPostPrice.setText(String.format("%s %s", post.getPrice(), post.getCurrency()));

				binding.detailChipGroupCategory.removeAllViews();
				List<String> categories = post.getCategories();
				if (categories != null && !categories.isEmpty()) {
					binding.detailChipGroupCategory.setVisibility(View.VISIBLE);
					for (String categoryName : categories) {
						Chip chip = new Chip(getContext());
						chip.setText(categoryName);
						binding.detailChipGroupCategory.addView(chip);
					}
				} else {
					binding.detailChipGroupCategory.setVisibility(View.GONE);
				}

				if (post.getLister() != null) {
					binding.detailListerName.setText("by " + post.getLister().getName());
					binding.detailListerName.setVisibility(View.VISIBLE);
				} else {
					binding.detailListerName.setVisibility(View.GONE);
				}

				String imageUrl = (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) ? post.getImageUrl().get(0) : null;
				ImageLoader.loadImage(binding.detailPostImage, imageUrl);
			}
		});

		viewModel.isFavorite().observe(getViewLifecycleOwner(), isFavorite -> {
			if (isFavorite != null && binding != null && binding.topAppBar.getMenu().findItem(com.shoppr.post.R.id.action_favorite) != null) {
				binding.topAppBar.getMenu().findItem(com.shoppr.post.R.id.action_favorite)
						.setIcon(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
			}
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}