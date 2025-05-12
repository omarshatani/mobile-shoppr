package com.shoppr.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.shoppr.core.ui.R;

public class MenuItemView extends ConstraintLayout { // Extend the root layout type used in XML

    private ImageView iconView;
    private TextView textView;
    private ImageView chevronView; // Optional, if you need to interact with it

    public MenuItemView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public MenuItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MenuItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    // Optional: Constructor for defStyleRes (API 21+)
    // public ProfileMenuItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    //    super(context, attrs, defStyleAttr, defStyleRes);
    //    init(context, attrs);
    // }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        // Inflate the layout resource into this custom view
        LayoutInflater.from(context).inflate(com.shoppr.core.ui.R.layout.view_menu_item, this, true);

        // Get references to the child views
        iconView = findViewById(com.shoppr.core.ui.R.id.menu_item_icon);
        textView = findViewById(R.id.menu_item_text);
        chevronView = findViewById(R.id.menu_item_chevron);

        // --- Handle Custom XML Attributes (Optional) ---
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ProfileMenuItemView,
                    0, 0);
            try {
                // Get icon attribute
                Drawable iconDrawable = a.getDrawable(R.styleable.ProfileMenuItemView_menuItemIcon);
                if (iconDrawable != null) {
                    setIcon(iconDrawable);
                }

                // Get text attribute
                String itemText = a.getString(R.styleable.ProfileMenuItemView_menuItemText);
                if (itemText != null) {
                    setText(itemText);
                }

                // Get visibility of chevron (example)
                boolean showChevron = a.getBoolean(R.styleable.ProfileMenuItemView_menuItemShowChevron, true);
                if (chevronView != null) {
                    chevronView.setVisibility(showChevron ? VISIBLE : GONE);
                }

            } finally {
                a.recycle();
            }
        }
        // -------------------------------------------------
    }

    // --- Public methods to customize the view ---

    public void setIcon(@DrawableRes int resId) {
        if (iconView != null) {
            iconView.setImageResource(resId);
        }
    }

    public void setIcon(Drawable drawable) {
        if (iconView != null) {
            iconView.setImageDrawable(drawable);
        }
    }

    public void setText(@NonNull String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }

    public void setText(int resId) {
        if (textView != null) {
            textView.setText(resId);
        }
    }

    public void showChevron(boolean show) {
        if (chevronView != null) {
            chevronView.setVisibility(show ? VISIBLE : GONE);
        }
    }

    // The whole view is clickable by default due to attributes in its XML.
    // The OnClickListener should be set on instances of MenuItemView
    // from the Fragment.
    // @Override
    // public void setOnClickListener(@Nullable OnClickListener l) {
    //     super.setOnClickListener(l); // The entire ConstraintLayout will be clickable
    // }
}
