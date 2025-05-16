package com.shoppr.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.shoppr.navigation.Navigator;
import com.shoppr.post.databinding.FragmentCreatePostBinding;

import javax.inject.Inject;

public class CreatePostFragment extends Fragment {
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

	private void observeViewModel() {

	}

}