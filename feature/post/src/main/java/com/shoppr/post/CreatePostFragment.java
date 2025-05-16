package com.shoppr.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.shoppr.navigation.Navigator;
import com.shoppr.post.databinding.FragmentCreatePostBinding;
import com.shoppr.ui.BaseFragment;

import javax.inject.Inject;

public class CreatePostFragment extends BaseFragment {
	private static final String TAG = "CreatePostFragment";
	private CreatePostViewModel viewModel;
	private FragmentCreatePostBinding binding;

	@Inject
	Navigator navigator;


	public static CreatePostFragment newInstance() {
		return new CreatePostFragment();
	}

	public CreatePostFragment() {
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
													 @Nullable Bundle savedInstanceState) {
		binding = FragmentCreatePostBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(CreatePostViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		MaterialToolbar toolbar = view.findViewById(R.id.toolbar_create_post);
		NavigationUI.setupWithNavController(toolbar, NavHostFragment.findNavController(this));
	}

}