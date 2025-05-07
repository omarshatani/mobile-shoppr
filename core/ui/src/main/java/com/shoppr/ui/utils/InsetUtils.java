package com.shoppr.ui.utils;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class InsetUtils {
    private InsetUtils() {}

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

    /**
     * Applies system bar insets (status bar, navigation bar) and IME insets
     * as padding to the top and bottom of the provided view.
     * Preserves the view's original left and right padding.
     * Best for root views or non-scrolling content containers.
     *
     * @param view         The view to apply padding to.
     * @param windowInsets The WindowInsetsCompat object received by the listener.
     */
    public static void applySystemBarsAndImePadding(View view, WindowInsetsCompat windowInsets) {
        Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
        view.setPadding(
            view.getPaddingLeft(),
            insets.top,
            view.getPaddingRight(),
            insets.bottom
        );
    }

    /**
     * Applies ONLY the top system bar inset (status bar) as top padding.
     * Useful for views anchored to the top like Toolbars within CoordinatorLayout.
     * Preserves other padding.
     *
     * @param view         The view to apply top padding to.
     * @param windowInsets The WindowInsetsCompat object received by the listener.
     */
    public static void applyStatusBarInsetAsPaddingTop(View view, WindowInsetsCompat windowInsets) {
        Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
        view.setPadding(
            view.getPaddingLeft(),
            insets.top,
            view.getPaddingRight(),
            view.getPaddingBottom() // Keep original bottom padding
        );
    }

    /**
     * Applies bottom padding to account for the system navigation bar/gesture area AND
     * a BottomNavigationView. Designed for scrolling content views like RecyclerView
     * or ScrollView to prevent content from going under the bottom bars.
     * Preserves other padding.
     *
     * @param view         The scrolling view (e.g., RecyclerView) to apply padding to.
     * @param windowInsets The WindowInsetsCompat object received by the listener.
     * @param bottomNavView The BottomNavigationView instance. Can be null if not applicable.
     */
    public static void applyNavigationBarAndBottomNavPadding(View view, WindowInsetsCompat windowInsets, @Nullable BottomNavigationView bottomNavView) {
        Insets systemNavInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
        Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

        int bottomNavHeight = 0;
        // Check visibility and if it's laid out to get a valid height
        if (bottomNavView != null && bottomNavView.getVisibility() == View.VISIBLE && bottomNavView.isLaidOut()) {
            bottomNavHeight = bottomNavView.getHeight();
        }
        // If not laid out yet, height might be 0. Consider using post() or a fixed dimen as fallback.

        // Use the larger of the system nav inset or IME inset, then add bottom nav height
        int bottomPadding = Math.max(systemNavInsets.bottom, imeInsets.bottom) + bottomNavHeight;

        view.setPadding(
            view.getPaddingLeft(),
            view.getPaddingTop(), // Keep original top padding
            view.getPaddingRight(),
            bottomPadding
        );

        // Crucial for scrolling views to draw under the padding area
        if (view instanceof androidx.recyclerview.widget.RecyclerView ||
            view instanceof androidx.core.widget.NestedScrollView ||
            view instanceof android.widget.ScrollView) {
            ((ViewGroup) view).setClipToPadding(false);
        }
    }

    public static int getTopInset(WindowInsetsCompat windowInsets) {
        return windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
    }

    public static int getBottomInset(WindowInsetsCompat windowInsets) {
        return windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
    }

    public static int getLeftInset(WindowInsetsCompat windowInsets) {
        return windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).left;
    }

    public static int getRightInset(WindowInsetsCompat windowInsets) {
        return windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).right;
    }
}
