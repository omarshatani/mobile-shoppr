package com.shoppr.ui.utils;


import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.shoppr.core.ui.R;

/**
 * A utility class to centralize image loading logic using Glide.
 * This can be called from any module that depends on core:ui,
 * without that module needing a direct dependency on Glide.
 */
public class ImageLoader {

    /**
     * Loads an image from a URL into an ImageView.
     *
     * @param view The ImageView to load the image into.
     * @param url  The URL of the image to load.
     */
    public static void loadImage(ImageView view, @Nullable String url) {
        if (view == null) return;

        // Use default placeholders defined in core:ui
        int placeholderResId = R.drawable.ic_placeholder_image;
        int errorResId = R.drawable.ic_placeholder_image;

        if (url != null && !url.isEmpty()) {
            Glide.with(view.getContext())
                    .load(url)
                    .placeholder(placeholderResId)
                    .error(errorResId)
                    .centerCrop()
                    .into(view);
        } else {
            // Set a default placeholder if the URL is null or empty
            view.setImageResource(placeholderResId);
        }
    }

    /**
     * Overloaded method to allow specifying custom placeholders.
     */
    public static void loadImage(ImageView view, @Nullable String url, @DrawableRes int placeholderResId, @DrawableRes int errorResId) {
        if (view == null) return;

        if (url != null && !url.isEmpty()) {
            Glide.with(view.getContext())
                    .load(url)
                    .placeholder(placeholderResId)
                    .error(errorResId)
                    .centerCrop()
                    .into(view);
        } else {
            view.setImageResource(placeholderResId);
        }
    }
}
