package com.shoppr.ui.utils;

import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class InsetUtils {
    private static final String TAG = "InsetUtils";
    private InsetUtils() {}

    /**
     * Applies system bar insets (status bar, navigation bar) and IME insets
     * as padding to the top and bottom of the provided view.
     * Also adds padding for a standard BottomNavigationView height defined by a dimension resource.
     * Preserves the view's original left and right padding.
     * Ideal for applying to the root view of fragments that should respect system bars
     * AND the bottom navigation bar.
     *
     * @param rootView     The root view of the fragment to apply padding to.
     * @param windowInsets The WindowInsetsCompat object received by the listener.
     * @param bottomNavHeightDimenRes The dimension resource ID (e.g., R.dimen.bottom_nav_height)
     * for the expected height of the BottomNavigationView.
     */
    public static void applyBottomNavPadding(
        @NonNull View rootView,
        @NonNull WindowInsetsCompat windowInsets,
        @DimenRes int bottomNavHeightDimenRes // Pass the dimension resource ID
    ) {
        Insets systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
        Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

        // --- Get expected BottomNav height from dimension resource ---
        int bottomNavHeight = 0;
        try {
            bottomNavHeight = rootView.getResources().getDimensionPixelSize(bottomNavHeightDimenRes);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Dimension resource ID " + bottomNavHeightDimenRes + " not found!", e);
            // Optionally use a fallback pixel value
            // bottomNavHeight = (int) (56 * rootView.getResources().getDisplayMetrics().density);
        }
        // -------------------------------------------------------------

        // Calculate total bottom padding needed
        // Use max of system nav bar or keyboard inset, then add bottom nav height
        int totalBottomPadding = Math.max(systemBarsInsets.bottom, imeInsets.bottom) + bottomNavHeight;

        // Apply padding to the root view
        // Preserve original horizontal padding, apply calculated top/bottom
        Log.d(TAG, "Applying padding to view ID " + rootView.getId() + ": Left=" + rootView.getPaddingLeft() +
            ", Top=" + systemBarsInsets.top + ", Right=" + rootView.getPaddingRight() +
            ", Bottom=" + totalBottomPadding);
        rootView.setPadding(
            rootView.getPaddingLeft(),
            rootView.getPaddingTop(),
            rootView.getPaddingRight(),
            totalBottomPadding  // Apply calculated bottom padding
        );
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
