package com.shoppr.ui.adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.carousel.MaskableFrameLayout;
import com.shoppr.core.ui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the image carousel (RecyclerView) using DiffUtil and Glide.
 */
public class SelectedImagesCarouselAdapter extends RecyclerView.Adapter<SelectedImagesCarouselAdapter.ImageViewHolder> {

	private static final String TAG = "SelectedImagesCarouselAdapter";

	static class ImageViewHolder extends RecyclerView.ViewHolder {
		MaskableFrameLayout itemContainer;
		ImageView imageView;
		MaterialButton removeButton;

		ImageViewHolder(@NonNull View itemView) {
			super(itemView);
			itemContainer = itemView.findViewById(R.id.carousel_item_container);
			imageView = itemView.findViewById(R.id.carousel_image_view);
			removeButton = itemView.findViewById(R.id.button_remove_image);
		}

		void bind(Uri imageUri, OnImageRemoveListener removeListener) {
			Glide.with(imageView.getContext())
					.load(imageUri)
					.placeholder(R.drawable.ic_placeholder_image) // Replace with your placeholder
					.error(R.drawable.ic_error_image) // Replace with your error drawable
					.into(imageView);

			removeButton.setOnClickListener(v -> {
				int position = getBindingAdapterPosition();
				if (removeListener != null && position != RecyclerView.NO_POSITION) {
					// Pass the originally bound imageUri to ensure consistency,
					// especially if the list could change between bind and click.
					removeListener.onRemoveClicked(imageUri);
				}
			});
		}
	}

	private final List<Uri> imageUris;
	private final OnImageRemoveListener removeListener;

	public interface OnImageRemoveListener {
		void onRemoveClicked(Uri imageUri);
	}

	public SelectedImagesCarouselAdapter(List<Uri> initialImageUris, OnImageRemoveListener removeListener) {
		this.imageUris = new ArrayList<>(initialImageUris != null ? initialImageUris : List.of());
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
		Uri imageUri = imageUris.get(position);
		holder.bind(imageUri, removeListener);
	}

	@Override
	public int getItemCount() {
		return imageUris.size();
	}

	public void submitList(List<Uri> newUris) {
		final List<Uri> oldUris = new ArrayList<>(this.imageUris);
		final List<Uri> newList = newUris != null ? new ArrayList<>(newUris) : new ArrayList<>();

		ImageUriDiffCallback diffCallback = new ImageUriDiffCallback(oldUris, newList);
		DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

		this.imageUris.clear();
		this.imageUris.addAll(newList);
		diffResult.dispatchUpdatesTo(this);
		Log.d(TAG, "URIs submitted, old count: " + oldUris.size() + ", new count: " + this.imageUris.size());
	}

	public List<Uri> getCurrentList() {
		return new ArrayList<>(imageUris); // Return a copy
	}


	private static class ImageUriDiffCallback extends DiffUtil.Callback {
		private final List<Uri> oldList;
		private final List<Uri> newList;

		public ImageUriDiffCallback(List<Uri> oldList, List<Uri> newList) {
			this.oldList = oldList;
			this.newList = newList;
		}

		@Override
		public int getOldListSize() {
			return oldList.size();
		}

		@Override
		public int getNewListSize() {
			return newList.size();
		}

		@Override
		public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
			// URIs are unique identifiers for the items in this case.
			return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
		}

		@Override
		public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
			// If the URIs are the same, we assume the content (the image) is the same.
			// If the image content could change while the URI remains identical,
			// this method would need a more sophisticated check.
			return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
			// For more complex objects, you would compare all relevant fields.
			// Using Objects.equals() for null safety if properties could be null.
			// e.g. return Objects.equals(oldList.get(oldItemPosition).getPath(), newList.get(newItemPosition).getPath());
		}

		@Nullable
		@Override
		public Object getChangePayload(int oldItemPosition, int newItemPosition) {
			// Implement this if you want to differentiate between partial and full updates
			// for the same item. For now, returning null will trigger a full rebind
			// if areItemsTheSame is true but areContentsTheSame is false.
			return super.getChangePayload(oldItemPosition, newItemPosition);
		}
	}
}