package com.shoppr.profile;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shoppr.model.Post;
import com.shoppr.profile.adapter.FavoritesAdapter;
import com.shoppr.profile.databinding.FragmentFavoritesBinding;
import com.shoppr.ui.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FavoritesFragment extends BaseFragment<FragmentFavoritesBinding> implements FavoritesAdapter.OnPostClickListener, FavoritesAdapter.OnFavoriteClickListener {

	private FragmentFavoritesBinding binding;
	private FavoritesViewModel viewModel;
	private FavoritesAdapter favoritesAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);
	}

	@Override
	protected FragmentFavoritesBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		return FragmentFavoritesBinding.inflate(inflater, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setupToolbar();
		setupRecyclerView();
		observeViewModel();
	}

	@Override
	protected InsetType getInsetType() {
		return InsetType.TOP;
	}

	@Override
	public void onStart() {
		super.onStart();
		viewModel.onFragmentStarted();
	}

	@Override
	public void onStop() {
		super.onStop();
		viewModel.onFragmentStopped();
	}

	private void setupToolbar() {
		NavController navController = NavHostFragment.findNavController(this);
		NavigationUI.setupWithNavController(binding.toolbarFavorites, navController);
	}

	private void setupRecyclerView() {
		favoritesAdapter = new FavoritesAdapter(this, this); // Use the new adapter
		binding.recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
		binding.recyclerViewFavorites.setAdapter(favoritesAdapter);
	}

	private void observeViewModel() {
		viewModel.getFavoritePosts().observe(getViewLifecycleOwner(), posts -> {
			favoritesAdapter.submitList(posts);
			binding.textViewNoFavorites.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
			binding.recyclerViewFavorites.setVisibility(posts.isEmpty() ? View.GONE : View.VISIBLE);
		});
	}

	@Override
	public void onPostClicked(@NonNull Post post) {
		// TODO: add navigation action from FavoritesFragment to PostDetailFragment
	}

	@Override
	public void onFavoriteClick(@NonNull Post post) {
		viewModel.unfavoritePost(post);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
}