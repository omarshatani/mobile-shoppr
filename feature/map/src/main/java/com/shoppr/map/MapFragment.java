package com.shoppr.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.shoppr.map.databinding.FragmentMapBinding;

public class MapFragment extends Fragment {

	private FragmentMapBinding binding;

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
				googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 10));
			});
		}
	}
}