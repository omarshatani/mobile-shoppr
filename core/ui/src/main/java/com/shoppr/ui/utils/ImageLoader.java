package com.shoppr.ui.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.shoppr.core.ui.R;

public class ImageLoader {
    public static void loadImage(ImageView imageView, String url) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_placeholder_image);
            return;
        }

        Glide.with(imageView.getContext())
            .load(url)
            // This listener is the key fix. It catches loading errors.
            .error(R.drawable.ic_error_image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(R.drawable.ic_placeholder_image)
            .into(imageView);
    }
}