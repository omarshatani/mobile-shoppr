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
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.shoppr.model.LocationData;
import com.shoppr.navigation.NavigationRoute;
import com.shoppr.navigation.Navigator;
import com.shoppr.post.databinding.FragmentCreatePostBinding;
import com.shoppr.ui.BaseFragment;

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

    private final List<Uri> currentSelectedImageUris = new ArrayList<>();
    // private SelectedImagesAdapter selectedImagesAdapter; // Conceptual

    private ActivityResultLauncher<Intent> pickImagesLauncher;
    // private ActivityResultLauncher<String> requestLocationPermissionLauncher; // Not used if location is assumed

    // This will hold the location data derived from the user's profile for the post.
    private LocationData postCreationLocation = null;


    public CreatePostFragment() {
        // Required empty public constructor
    }

    public static CreatePostFragment newInstance() {
        return new CreatePostFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CreatePostViewModel.class);

        pickImagesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                currentSelectedImageUris.clear();
                if (result.getData().getClipData() != null) {
                    int count = result.getData().getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                        if (imageUri != null) {
                            currentSelectedImageUris.add(imageUri);
                        }
                    }
                } else if (result.getData().getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        currentSelectedImageUris.add(imageUri);
                    }
                }
                Log.d(TAG, "Selected " + currentSelectedImageUris.size() + " image(s).");
                viewModel.onUserSelectedLocalImageUris(new ArrayList<>(currentSelectedImageUris));
                Toast.makeText(getContext(), currentSelectedImageUris.size() + " image(s) selected.", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Image selection cancelled or failed.");
            }
        });

        // requestLocationPermissionLauncher is not needed if we don't have a button to fetch current location here
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
        NavigationUI.setupWithNavController(toolbar, NavHostFragment.findNavController(this));

//        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> { // Changed view to toolbar
//            v.setPadding(v.getPaddingLeft(), InsetUtils.getTopInset(insets) + v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
//            return insets;
//        });

        setupCurrencySpinner();
        setupInputListeners();
        setupButtonListeners();
        observeViewModel();
    }

    private void setupCurrencySpinner() {
        if (binding == null) return;
        String[] currencies = new String[]{"USD", "EUR", "CHF", "GBP", "JPY", "CAD"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                currencies
        );
        binding.autocompleteCurrency.setAdapter(adapter);
        if (viewModel.baseOfferCurrency.getValue() != null) {
            binding.autocompleteCurrency.setText(viewModel.baseOfferCurrency.getValue(), false);
        } else {
            binding.autocompleteCurrency.setText("USD", false);
        }
    }

    private void setupInputListeners() {
        if (binding == null) return;
        binding.editTextRawText.addTextChangedListener(createTextWatcher(viewModel::onRawTextChanged));
        binding.editTextBaseOffer.addTextChangedListener(createTextWatcher(viewModel::onBaseOfferPriceChanged));
        binding.autocompleteCurrency.addTextChangedListener(createTextWatcher(viewModel::onBaseOfferCurrencyChanged));
        // No listeners for editTextLocation as it's removed
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
        binding.buttonAddImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickImagesLauncher.launch(Intent.createChooser(intent, "Select Pictures"));
        });

        // No listener for location icon as it's removed from layout

        binding.buttonCreatePostSubmit.setOnClickListener(v -> {
            Log.d(TAG, "Create Post button clicked.");
            if (postCreationLocation == null) { // Check the fragment's stored location
                Toast.makeText(getContext(), "User location not yet available. Please ensure it's set from your profile.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Cannot create post: postCreationLocation is null.");
                return;
            }
            viewModel.onCreatePostClicked(postCreationLocation, new ArrayList<>(currentSelectedImageUris));
        });
    }

    private void observeViewModel() {
        if (binding == null) return;

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;
            binding.progressBarCreatePost.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.buttonCreatePostSubmit.setEnabled(!isLoading);
        });

        viewModel.operationError.observe(getViewLifecycleOwner(), event -> {
            if (binding == null) return;
            if (event == null) return;
            String errorMessage = event.peekContent();
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.successMessage.observe(getViewLifecycleOwner(), event -> {
            if (binding == null) return;
            if (event == null) return;
            String message = event.peekContent();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.navigationCommand.observe(getViewLifecycleOwner(), event -> {
            if (binding == null) return;
            if (event == null) return;
            NavigationRoute route = event.peekContent();
            if (route != null) {
                Log.d(TAG, "Navigation command received: " + route.getClass().getSimpleName());
                if (route instanceof NavigationRoute.Map || route.getClass().getSimpleName().contains("Map")) {
                    NavHostFragment.findNavController(this).popBackStack();
                } else {
                    navigator.navigate(route);
                }
            }
        });

        viewModel.currentListerLiveData.observe(getViewLifecycleOwner(), lister -> {
            if (binding == null) return;
            if (lister != null) {
                Log.d(TAG, "Current lister profile loaded: " + lister.getName());
                if (lister.getLastLatitude() != null && lister.getLastLongitude() != null) {
                    // Set the location to be used for the post
                    postCreationLocation = new LocationData(
                            lister.getLastLatitude(),
                            lister.getLastLongitude(),
                            lister.getLastLocationAddress()
                    );
                    Log.d(TAG, "Post location set from user profile: " + postCreationLocation.toString());
                    // No UI to update for location display in this simplified version
                } else {
                    postCreationLocation = null; // Ensure it's null if not available
                    Log.w(TAG, "Lister profile loaded, but no last known location available. Post creation might be blocked.");
                    Toast.makeText(getContext(), "Your default location isn't set. Please visit the map screen to set it.", Toast.LENGTH_LONG).show();
                }
                binding.buttonCreatePostSubmit.setEnabled(postCreationLocation != null); // Enable button only if location is available
            } else {
                Log.w(TAG, "Lister is null. User needs to be logged in to create a post.");
                postCreationLocation = null;
                binding.buttonCreatePostSubmit.setEnabled(false);
                Toast.makeText(getContext(), "Please log in to create a post.", Toast.LENGTH_LONG).show();
            }
        });

        viewModel.selectedImageUris.observe(getViewLifecycleOwner(), uris -> {
            Log.d(TAG, "Observed selectedImageUris in Fragment (from VM), count: " + (uris != null ? uris.size() : 0));
            // Update your RecyclerView adapter here if it's implemented
            // if (selectedImagesAdapter != null && uris != null) {
            //    selectedImagesAdapter.updateUris(uris);
            // }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}