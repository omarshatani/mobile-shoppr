package com.shoppr.post;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.shoppr.navigation.NavigationRoute;
import com.shoppr.navigation.Navigator;
import com.shoppr.post.databinding.FragmentCreatePostBinding;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.adapter.SelectedImagesCarouselAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreatePostFragment extends BaseFragment {
	private static final String TAG = "CreatePostFragment";

	private CreatePostViewModel viewModel;
	private FragmentCreatePostBinding binding;

	@Inject
	Navigator navigator;

	private SelectedImagesCarouselAdapter selectedImagesAdapter;
	private ActivityResultLauncher<Intent> pickImagesLauncher;

	public CreatePostFragment() {
	}

	@Override
	protected boolean shouldHideBottomNav() {
		return true;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(CreatePostViewModel.class);

		pickImagesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
				List<Uri> newlySelectedUris = new ArrayList<>();
				if (result.getData().getClipData() != null) {
					int count = result.getData().getClipData().getItemCount();
					for (int i = 0; i < count; i++) {
						Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
						if (imageUri != null) {
							newlySelectedUris.add(imageUri);
						}
					}
				} else if (result.getData().getData() != null) {
					Uri imageUri = result.getData().getData();
					if (imageUri != null) {
						newlySelectedUris.add(imageUri);
					}
				}
				viewModel.onUserSelectedLocalImageUris(newlySelectedUris);
				Toast.makeText(getContext(), newlySelectedUris.size() + " image(s) added.", Toast.LENGTH_SHORT).show();
			} else {
				Log.d(TAG, "Image selection cancelled or failed.");
			}
		});
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
													 Bundle savedInstanceState) {
		binding = FragmentCreatePostBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		MaterialToolbar toolbar = binding.toolbarCreatePost;
		NavController navController = NavHostFragment.findNavController(this);
		NavigationUI.setupWithNavController(toolbar, navController);

		setupCarousel();
		setupCurrencySpinner();
		setupButtonListeners();
		observeViewModel();
	}

	@Override
	protected InsetType getInsetType() {
		return InsetType.TOP;
	}

	private void setupCarousel() {
		if (getContext() == null || binding == null) return;
		selectedImagesAdapter = new SelectedImagesCarouselAdapter(new ArrayList<>(), uriToRemove -> {
			viewModel.removeSelectedImageUri(uriToRemove);
			Toast.makeText(getContext(), "Image removed", Toast.LENGTH_SHORT).show();
		});
		binding.carouselSelectedImages.setLayoutManager(new CarouselLayoutManager());
		CarouselSnapHelper snapHelper = new CarouselSnapHelper();
		snapHelper.attachToRecyclerView(binding.carouselSelectedImages);
		binding.carouselSelectedImages.setAdapter(selectedImagesAdapter);
		updateCarouselVisibility(viewModel.selectedImageUris.getValue());
	}

	private void updateCarouselVisibility(@Nullable List<Uri> uris) {
		if (binding == null) return;
		boolean isEmpty = uris == null || uris.isEmpty();
		binding.carouselSelectedImages.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
		binding.labelImagesCarousel.setText(isEmpty ? "Add Photos (Optional)" : "Selected Photos (" + uris.size() + ") Tap photo to remove");
	}

	private void setupCurrencySpinner() {
		if (binding == null || getContext() == null) return;
		String[] currencies = new String[]{"USD", "EUR", "CHF"};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(
				requireContext(),
				android.R.layout.simple_dropdown_item_1line,
				currencies
		);
		binding.autocompleteCurrency.setAdapter(adapter);
		binding.autocompleteCurrency.setText("USD", false);
	}

	private void setupButtonListeners() {
		if (binding == null) return;

		binding.buttonToolbarAddPhoto.setOnClickListener(v -> {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
			pickImagesLauncher.launch(Intent.createChooser(intent, "Select Pictures"));
		});

		binding.buttonToolbarCreatePost.setOnClickListener(v -> {
			String rawText = binding.editTextRawText.getText().toString();
			String price = binding.editTextBaseOffer.getText().toString();
			String currency = binding.autocompleteCurrency.getText().toString();
			viewModel.onCreatePostClicked(rawText, price, currency);
		});
	}

	private void observeViewModel() {
		if (binding == null || getViewLifecycleOwner() == null) return;

		viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
			if (binding == null || isLoading == null) return;
			binding.progressBarCreatePost.setVisibility(isLoading ? View.VISIBLE : View.GONE);
			binding.buttonToolbarAddPhoto.setEnabled(!isLoading);
			binding.buttonToolbarCreatePost.setEnabled(!isLoading);
		});

		viewModel.operationError.observe(getViewLifecycleOwner(), event -> {
			String errorMessage = event.getContentIfNotHandled();
			if (errorMessage != null) {
				Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
			}
		});

		viewModel.successMessage.observe(getViewLifecycleOwner(), event -> {
			String message = event.getContentIfNotHandled();
			if (message != null) {
				Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
			}
		});

		viewModel.navigationCommand.observe(getViewLifecycleOwner(), event -> {
			NavigationRoute route = event.getContentIfNotHandled();
			if (route != null) {
				NavHostFragment.findNavController(this).popBackStack();
			}
		});

		viewModel.selectedImageUris.observe(getViewLifecycleOwner(), uris -> {
			if (selectedImagesAdapter != null) {
				selectedImagesAdapter.updateUris(uris != null ? uris : new ArrayList<>());
				updateCarouselVisibility(uris);
			}
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}