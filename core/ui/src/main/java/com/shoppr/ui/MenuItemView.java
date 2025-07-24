package com.shoppr.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.shoppr.core.ui.R;

public class MenuItemView extends ConstraintLayout {

    private ImageView iconView;
    private TextView textView;
    private ImageView chevronView;

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

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        LayoutInflater.from(context).inflate(com.shoppr.core.ui.R.layout.view_menu_item, this, true);

        setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.5f);
                    return true;
                case MotionEvent.ACTION_UP:
                    v.setAlpha(1.0f);
                    v.performClick();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1.0f);
                    return true;
            }
            return false;
        });

        iconView = findViewById(com.shoppr.core.ui.R.id.menu_item_icon);
        textView = findViewById(R.id.menu_item_text);
        chevronView = findViewById(R.id.menu_item_chevron);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ProfileMenuItemView,
                0, 0);
            try {
                Drawable iconDrawable = a.getDrawable(R.styleable.ProfileMenuItemView_menuItemIcon);
                if (iconDrawable != null) {
                    setIcon(iconDrawable);
                }

                String itemText = a.getString(R.styleable.ProfileMenuItemView_menuItemText);
                if (itemText != null) {
                    setText(itemText);
                }

                boolean showChevron = a.getBoolean(R.styleable.ProfileMenuItemView_menuItemShowChevron, true);
                if (chevronView != null) {
                    chevronView.setVisibility(showChevron ? VISIBLE : GONE);
                }

            } finally {
                a.recycle();
            }
        }
    }

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
}