package com.shoppr.ui.utils;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

public class InsetsUtils {
    private InsetsUtils() {
    }

    public static void applySystemBarInsetsAsPadding(View view, WindowInsetsCompat windowInsets) {
        // Apply padding for both system bars and ime (keyboard) if you want content
        // to adjust even when keyboard is open. Often just systemBars is needed for basic padding.
        Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());

        // Preserve original padding (especially horizontal)
        int originalPaddingLeft = view.getPaddingLeft();
        int originalPaddingRight = view.getPaddingRight();

        // Apply insets as padding
        view.setPadding(
                originalPaddingLeft,
                insets.top,           // Apply top inset (status bar)
                originalPaddingRight,
                insets.bottom         // Apply bottom inset (nav bar / gesture bar / keyboard)
        );
    }

    /**
     * More flexible version allowing selective application of padding.
     *
     * @param view         The view to apply padding to.
     * @param windowInsets The WindowInsetsCompat object received by the listener.
     * @param applyTop     Apply top system bar inset as padding.
     * @param applyBottom  Apply bottom system bar inset as padding.
     * @param applyLeft    Apply left system bar inset as padding.
     * @param applyRight   Apply right system bar inset as padding.
     */
    public static void applySystemBarInsetsAsPadding(View view, WindowInsetsCompat windowInsets,
                                                     boolean applyTop, boolean applyBottom,
                                                     boolean applyLeft, boolean applyRight) {

        Insets systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

        int paddingLeft = applyLeft ? systemBarInsets.left : view.getPaddingLeft();
        int paddingTop = applyTop ? systemBarInsets.top : view.getPaddingTop();
        int paddingRight = applyRight ? systemBarInsets.right : view.getPaddingRight();
        int paddingBottom = applyBottom ? systemBarInsets.bottom : view.getPaddingBottom();

        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }
}
