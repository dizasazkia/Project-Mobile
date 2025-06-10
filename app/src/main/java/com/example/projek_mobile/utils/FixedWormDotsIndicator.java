package com.example.projek_mobile.utils;

import static android.widget.RelativeLayout.CENTER_HORIZONTAL;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.projek_mobile.R;

public class FixedWormDotsIndicator extends LinearLayout {
    private ViewPager2 viewPager;
    private int dotCount = 0;

    private final int dotSize = dpToPx(8);
    private final int selectedDotWidth = dpToPx(24);
    private final int dotHeight = dpToPx(8);
    private final int spacing = dpToPx(8);

    public FixedWormDotsIndicator(Context context) {
        super(context);
        init();
    }

    public FixedWormDotsIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(CENTER_HORIZONTAL);
    }

    public void setViewPager(ViewPager2 vp) {
        this.viewPager = vp;
        dotCount = vp.getAdapter().getItemCount();
        createDots();
        vp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
    }

    private void createDots() {
        removeAllViews();
        for (int i = 0; i < dotCount; i++) {
            View dot = new View(getContext());
            LayoutParams params = new LayoutParams(dotSize, dotHeight);
            params.setMargins(spacing / 2, 0, spacing / 2, 0);
            dot.setLayoutParams(params);
            dot.setBackground(createDotDrawable(false));
            addView(dot);
        }
        updateDots(0);
    }

    private void updateDots(int selected) {
        for (int i = 0; i < getChildCount(); i++) {
            View dot = getChildAt(i);
            LayoutParams params;
            if (i == selected) {
                params = new LayoutParams(selectedDotWidth, dotHeight);
                dot.setBackground(createDotDrawable(true));
            } else {
                params = new LayoutParams(dotSize, dotHeight);
                dot.setBackground(createDotDrawable(false));
            }
            params.setMargins(spacing / 2, 0, spacing / 2, 0);
            dot.setLayoutParams(params);
        }
    }

    private GradientDrawable createDotDrawable(boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dpToPx(4));

        int color = ContextCompat.getColor(
                getContext(), selected ? R.color.dot_active : R.color.dot_inactive
        );
        drawable.setColor(color);

        return drawable;
    }


    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}

