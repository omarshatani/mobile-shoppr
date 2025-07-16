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

import com.shoppr.post.databinding.FragmentPostDetailBinding;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.utils.ImageLoader;

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

    private void setupToolbar() {
        NavController navController = NavHostFragment.findNavController(this);
        // We leave the toolbar title empty, as the headline TextView will serve as the title.
        NavigationUI.setupWithNavController(binding.topAppBar, navController);
    }

    private void observeViewModel() {
        viewModel.getSelectedPost().observe(getViewLifecycleOwner(), post -> {
            if (post != null) {
                // Set the title in the new headline TextView
                binding.detailPostHeadline.setText(post.getTitle());

                binding.detailPostDescription.setText(post.getDescription());
                binding.detailPostPrice.setText(String.format("%s %s", post.getPrice(), post.getCurrency()));
                binding.detailPostCategoryChip.setText(post.getCategory());
                binding.detailPostTypeChip.setText(post.getType().getLabel().toUpperCase());

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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}