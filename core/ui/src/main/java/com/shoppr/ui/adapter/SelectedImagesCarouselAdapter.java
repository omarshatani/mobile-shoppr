package com.shoppr.ui.adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.carousel.MaskableFrameLayout;
import com.shoppr.core.ui.R;

import java.util.ArrayList;
import java.util.List;

public class SelectedImagesCarouselAdapter extends RecyclerView.Adapter<SelectedImagesCarouselAdapter.ImageViewHolder> {

	private static final String TAG = "SelectedImagesCarouselAdapter";

	static class ImageViewHolder extends RecyclerView.ViewHolder {
		MaskableFrameLayout itemContainer;
		ImageView imageView;
		ImageView removeButton;

		ImageViewHolder(@NonNull View itemView) {
			super(itemView);
			itemContainer = itemView.findViewById(R.id.carousel_item_container);
			imageView = itemView.findViewById(R.id.carousel_image_view);
			removeButton = itemView.findViewById(R.id.button_remove_image);
		}

		void bind(Uri imageUri, OnImageRemoveListener removeListener) {
			Glide.with(imageView.getContext())
					.load(imageUri)
					// .placeholder(R.drawable.ic_placeholder_image) // Replace with your placeholder
					// .error(R.drawable.ic_error_image) // Replace with your error drawable
					.into(imageView);

			removeButton.setOnClickListener(v -> {
				int position = getBindingAdapterPosition();
				if (removeListener != null && position != RecyclerView.NO_POSITION) {
					removeListener.onRemoveClicked(imageUri);
				}
			});
		}
	}

	private final List<Uri> imageUrisInternal; // Internal list for the adapter
	private final OnImageRemoveListener removeListener;

	public interface OnImageRemoveListener {
		void onRemoveClicked(Uri imageUri);
	}

	public SelectedImagesCarouselAdapter(List<Uri> initialImageUris, OnImageRemoveListener removeListener) {
		this.imageUrisInternal = new ArrayList<>(initialImageUris != null ? initialImageUris : List.of());
		this.removeListener = removeListener;
	}

	@NonNull
	@Override
	public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_carousel_image, parent, false);
		return new ImageViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
		Uri imageUri = imageUrisInternal.get(position);
		holder.bind(imageUri, removeListener);
	}

	@Override
	public int getItemCount() {
		return imageUrisInternal.size();
	}

	// Changed from submitList to a simpler update method that uses notifyDataSetChanged()
	public void updateUris(List<Uri> newUris) {
		this.imageUrisInternal.clear();
		if (newUris != null) {
			this.imageUrisInternal.addAll(newUris);
		}
		// This forces a full redraw, which should fix the corner radius animation bug.
		notifyDataSetChanged();
		Log.d(TAG, "URIs updated, count: " + this.imageUrisInternal.size());
	}
}