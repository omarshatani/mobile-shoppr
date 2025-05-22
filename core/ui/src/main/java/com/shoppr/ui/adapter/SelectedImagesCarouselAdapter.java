package com.shoppr.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.carousel.MaskableFrameLayout;
import com.shoppr.core.ui.R;

import java.util.List;

/**
 * Adapter for the image carousel (RecyclerView).
 */
public class SelectedImagesCarouselAdapter extends RecyclerView.Adapter<SelectedImagesCarouselAdapter.ImageViewHolder> {

    private final Context context;
    private final List<Uri> imageUris;
    private final OnImageRemoveListener removeListener;

    public interface OnImageRemoveListener {
        void onRemoveClicked(Uri imageUri);
    }

    public SelectedImagesCarouselAdapter(Context context, List<Uri> imageUris, OnImageRemoveListener removeListener) {
        this.context = context;
        this.imageUris = imageUris; // Use the passed list directly
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure you have R.layout.list_item_carousel_image defined
        View view = LayoutInflater.from(context).inflate(com.shoppr.core.ui.R.layout.list_item_carousel_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        // Use Glide or Coil for efficient image loading and caching in a production app
        holder.imageView.setImageURI(imageUri);
        holder.removeButton.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemoveClicked(imageUri);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris != null ? imageUris.size() : 0;
    }

    // Call this method when the list of URIs changes in the Fragment
    public void updateUris(List<Uri> newUris) {
        // this.imageUris = newUris; // If directly replacing the list reference
        this.imageUris.clear();
        this.imageUris.addAll(newUris);
        notifyDataSetChanged(); // Or use DiffUtil for better performance
        Log.d("CarouselAdapter", "URIs updated, count: " + this.imageUris.size());
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        MaskableFrameLayout itemContainer;
        ImageView imageView;
        MaterialButton removeButton;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ensure these IDs match your list_item_carousel_image.xml
            itemContainer = itemView.findViewById(R.id.carousel_item_container);
            imageView = itemView.findViewById(R.id.carousel_image_view);
            removeButton = itemView.findViewById(R.id.button_remove_image);
        }
    }
}