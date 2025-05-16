package com.shoppr.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.shoppr.navigation.NavigationRoute;
import com.shoppr.navigation.Navigator;
import com.shoppr.post.databinding.FragmentPostBinding;
import com.shoppr.ui.BaseFragment;

import javax.inject.Inject;

public class PostFragment extends BaseFragment {
    private static final String TAG = "PostFragment";
    private FragmentPostBinding binding;
    private PostFragmentViewModel viewModel;
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

        setupBindings();
        observeViewModel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void observeViewModel() {
        viewModel.getNavigationCommand().observe(getViewLifecycleOwner(), event -> {
            NavigationRoute route = event.peekContent();

            if (route instanceof NavigationRoute.PostsToCreatePost) {
                navigator.navigate(new NavigationRoute.PostsToCreatePost());
            }
        });
    }

    private void setupBindings() {
        binding.buttonCreateNewPostEmptyState.setOnClickListener(v -> {
            NavDirections directions = PostFragmentDirections.actionPostFragmentToCreatePostFragment();
            NavHostFragment.findNavController(this).navigate(directions);
        });
    }

}