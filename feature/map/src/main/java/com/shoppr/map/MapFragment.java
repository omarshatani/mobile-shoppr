package com.shoppr.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.SupportMapFragment;
import com.shoppr.map.databinding.FragmentMapBinding;
import com.shoppr.ui.BaseFragment;

public class MapFragment extends BaseFragment {
	private FragmentMapBinding binding;

	public MapFragment() {};

	@Override
	protected boolean shouldApplyBaseInsetPadding() {
		return false;
	}

	@Override
	protected boolean isLightStatusBarRequired() {
		return false;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = FragmentMapBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
		if (mapFragment != null) {
			mapFragment.getMapAsync(googleMap -> {
				googleMap.setPadding(16, 0, 0, 64);
			});
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
}