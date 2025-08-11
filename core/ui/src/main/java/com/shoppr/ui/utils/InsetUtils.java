package com.shoppr.ui.utils;


import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InsetUtils {

    /**
     * The definitive method to apply insets for screens with a BottomNavigationView.
     * It correctly handles the status bar, the system navigation bar, and the app's
     * custom BottomNavigationView, and it prevents the re-padding bug.
     *
     * @param view The scrolling view to apply padding to (e.g., RecyclerView).
     */
    public static void applyTopAndBottomInsets(View view) {
        final int initialPaddingLeft = view.getPaddingLeft();
        final int initialPaddingTop = view.getPaddingTop();
        final int initialPaddingRight = view.getPaddingRight();
        final int initialPaddingBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                initialPaddingLeft,
                initialPaddingTop + systemBarInsets.top,
                initialPaddingRight,
                // The final padding is the original + system nav bar + your app's nav bar
								initialPaddingBottom
            );

            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(view);
    }

    public static void applyTopInsets(View view) {
        final int initialPaddingLeft = view.getPaddingLeft();
        final int initialPaddingTop = view.getPaddingTop();
        final int initialPaddingRight = view.getPaddingRight();
        final int initialPaddingBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                initialPaddingLeft,
                initialPaddingTop + systemBarInsets.top,
                initialPaddingRight,
                initialPaddingBottom
            );
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(view);
    }

    public static void applyBottomInsets(View view) {
        final int initialPaddingLeft = view.getPaddingLeft();
        final int initialPaddingTop = view.getPaddingTop();
        final int initialPaddingRight = view.getPaddingRight();
        final int initialPaddingBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            v.setPadding(
                initialPaddingLeft,
                initialPaddingTop, // No top padding
                initialPaddingRight,
								initialPaddingBottom
            );
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(view);
    }
}