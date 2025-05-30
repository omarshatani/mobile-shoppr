package com.shoppr.post;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.button.MaterialButton;
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

	// Adapter for the image carousel
	private SelectedImagesCarouselAdapter selectedImagesAdapter;
	private ActivityResultLauncher<Intent> pickImagesLauncher;
	// Removed local postCreationLocation field. ViewModel is the source of truth.

	public CreatePostFragment() { /* Required empty public constructor */ }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(CreatePostViewModel.class);

		pickImagesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
				List<Uri> newlySelectedUris = new ArrayList<>();
				if (result.getData().getClipData() != null) { // Multiple images selected
					int count = result.getData().getClipData().getItemCount();
					for (int i = 0; i < count; i++) {
						Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
						if (imageUri != null) {
							newlySelectedUris.add(imageUri);
						}
					}
				} else if (result.getData().getData() != null) { // Single image selected
					Uri imageUri = result.getData().getData();
					if (imageUri != null) {
						newlySelectedUris.add(imageUri);
					}
				}
				// ViewModel handles combining/updating its list
				viewModel.onUserSelectedLocalImageUris(newlySelectedUris);
				Toast.makeText(getContext(), newlySelectedUris.size() + " image(s) added.", Toast.LENGTH_SHORT).show();
				// Adapter will be updated by observing viewModel.selectedImageUris
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

		// Inset handling for AppBarLayout removed as per your request.
		// The AppBarLayout's android:paddingTop="16dp" from your XML will apply.

		setupCarousel();
		setupCurrencySpinner();
		setupInputListeners();
		setupButtonListeners();
		observeViewModel();
	}

	private void setupCarousel() {
		if (getContext() == null || binding == null) return;
		// Initialize with an empty list; adapter will be updated by LiveData from ViewModel
		selectedImagesAdapter = new SelectedImagesCarouselAdapter(new ArrayList<>(), uriToRemove -> {
			viewModel.removeSelectedImageUri(uriToRemove); // Tell ViewModel to remove
			Toast.makeText(getContext(), "Image removed", Toast.LENGTH_SHORT).show();
			// updateCarouselVisibility will be called by the LiveData observer
		});
		binding.carouselSelectedImages.setLayoutManager(new CarouselLayoutManager());
		CarouselSnapHelper snapHelper = new CarouselSnapHelper();
		snapHelper.attachToRecyclerView(binding.carouselSelectedImages);
		binding.carouselSelectedImages.setAdapter(selectedImagesAdapter);
		updateCarouselVisibility(viewModel.selectedImageUris.getValue()); // Initial visibility
	}

	private void updateCarouselVisibility(@Nullable List<Uri> uris) {
		if (binding == null) return;
		boolean isEmpty = uris == null || uris.isEmpty();
		binding.carouselSelectedImages.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
		binding.labelImagesCarousel.setText(isEmpty ? "Add Photos (Optional)" : "Selected Photos (" + uris.size() + ") Tap photo to remove");
	}

	private void setupCurrencySpinner() {
		if (binding == null || getContext() == null) return;
		String[] currencies = new String[]{"USD", "EUR", "CHF", "GBP", "JPY", "CAD"};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(
				requireContext(),
				android.R.layout.simple_dropdown_item_1line,
				currencies
		);
		binding.autocompleteCurrency.setAdapter(adapter);
		// Set initial value from ViewModel or default
		String currentCurrency = viewModel.baseOfferCurrency.getValue();
		binding.autocompleteCurrency.setText(currentCurrency != null ? currentCurrency : "USD", false);
	}

	private void setupInputListeners() {
		if (binding == null) return;
		binding.editTextRawText.addTextChangedListener(createTextWatcher(viewModel::onRawTextChanged));
		binding.editTextBaseOffer.addTextChangedListener(createTextWatcher(viewModel::onBaseOfferPriceChanged));
		binding.autocompleteCurrency.addTextChangedListener(createTextWatcher(viewModel::onBaseOfferCurrencyChanged));
	}

	private TextWatcher createTextWatcher(TextChangeCallback callback) {
		return new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				callback.onTextChanged(s.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		};
	}

	interface TextChangeCallback {
		void onTextChanged(String text);
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
			Log.d(TAG, "Toolbar Create Post button clicked.");
			// ViewModel now uses its internal state for location and image URIs
			viewModel.onCreatePostClicked();
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
			if (binding == null || event == null) return;
			String errorMessage = event.getContentIfNotHandled();
			if (errorMessage != null) {
				Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
			}
		});

		viewModel.successMessage.observe(getViewLifecycleOwner(), event -> {
			if (binding == null || event == null) return;
			String message = event.getContentIfNotHandled();
			if (message != null) {
				Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
			}
		});

		viewModel.navigationCommand.observe(getViewLifecycleOwner(), event -> {
			if (binding == null || event == null) return;
			NavigationRoute route = event.getContentIfNotHandled();
			if (route != null) {
				Log.d(TAG, "Navigation command received: " + route.getClass().getSimpleName());
				if (route instanceof NavigationRoute.Map || route.getClass().getSimpleName().contains("Map")) {
					NavHostFragment.findNavController(this).popBackStack();
				} else {
					navigator.navigate(route);
				}
			}
		});

		// Observe currentListerLiveData to know if user is logged in
		viewModel.currentListerLiveData.observe(getViewLifecycleOwner(), lister -> {
			if (binding == null) return;
			MaterialButton createPostButton = binding.buttonToolbarCreatePost;
			if (lister == null) {
				Log.w(TAG, "Lister is null. Disabling create post button.");
				createPostButton.setEnabled(false);
				Toast.makeText(getContext(), "Please log in to create a post.", Toast.LENGTH_LONG).show();
			}
			// Further enabling based on location is handled by observing postCreationLocation
		});

		// Observe the derived postCreationLocation from ViewModel
		viewModel.postCreationLocation.observe(getViewLifecycleOwner(), locationData -> {
			if (binding == null) return;
			MaterialButton createPostButton = binding.buttonToolbarCreatePost;
			// Fragment no longer needs to store postCreationLocation locally for submission.
			// It just uses this observer to enable/disable the button.
			if (locationData != null && locationData.latitude != null && locationData.longitude != null) {
				Log.d(TAG, "ViewModel provided postCreationLocation: " + locationData.toString());
				if (viewModel.currentListerLiveData.getValue() != null) {
					createPostButton.setEnabled(true);
				}
			} else {
				Log.w(TAG, "ViewModel provided null or invalid postCreationLocation.");
				createPostButton.setEnabled(false);
				if (viewModel.currentListerLiveData.getValue() != null) { // Only show if user is logged in but location is missing
					Toast.makeText(getContext(), "Your default location isn't set. Please visit the map screen.", Toast.LENGTH_LONG).show();
				}
			}
		});

		// Observe selectedImageUris from ViewModel to update the carousel adapter
		viewModel.selectedImageUris.observe(getViewLifecycleOwner(), uris -> {
			if (selectedImagesAdapter != null) {
				Log.d(TAG, "Updating carousel adapter with URIs from ViewModel, count: " + (uris != null ? uris.size() : 0));
				selectedImagesAdapter.submitList(uris != null ? uris : new ArrayList<>()); // Use submitList
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
