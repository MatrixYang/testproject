package com.cooeeui.brand.zenlauncher.scenes;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.cooeeui.zenlauncher.R;

public class FitCenterLayout extends ViewGroup {

    float mChildAspectRatio = 0.0f;
    Rect mRect = new Rect();
    Rect mChildRect = new Rect();
    float mWidthWeight = 1.0f;
    float mHeightWeight = 1.0f;

    public FitCenterLayout(Context context) {
        super(context);
    }

    public FitCenterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        // get the custom attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.zenlauncher);
        int indexCount = a.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = a.getIndex(i);
            switch (index) {
                case R.styleable.zenlauncher_aspect_ratio:
                    mChildAspectRatio = a.getFloat(index, 1);
                    break;
                case R.styleable.zenlauncher_width_weight:
                    mWidthWeight = a.getFloat(index, 1);
                    break;
                case R.styleable.zenlauncher_height_weight:
                    mHeightWeight = a.getFloat(index, 1);
                    break;
            }
        }
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // calculate rectangle of child
        mRect.set(l, t, r, b);
        applyAspectRatio(mChildAspectRatio);
        // offset to local coordinate.
        mChildRect.offset(-mRect.left, -mRect.top);

        // layout child
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v != null) {
                v.layout(mChildRect.left, mChildRect.top, mChildRect.right, mChildRect.bottom);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // calculate rectangle of child
        mRect.set(0, 0, width, height);
        applyAspectRatio(mChildAspectRatio);

        // measure child
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v != null) {
                int childWidthSpec = MeasureSpec.makeMeasureSpec(mChildRect.width(), widthMode);
                int childHeightSpec = MeasureSpec.makeMeasureSpec(mChildRect.height(), heightMode);
                v.measure(childWidthSpec, childHeightSpec);
            }
        }

        setMeasuredDimension(width, height);
    }

    /**
     * Apply child aspect ratio, save the result in mChildRect.
     */
    void applyAspectRatio(float aspect) {
        mChildRect.set(mRect);

        // guard
        if (mRect.height() <= 0) {
            return;
        }

        float srcAspect = (float) mRect.width() / (float) mRect.height();
        int width = mRect.width();
        int height = mRect.height();

        if (aspect == 0.0f || srcAspect == aspect) {
            // not need aspect ratio
            width = (int) (width * mWidthWeight);
            height = (int) (height * mHeightWeight);
        } else if (aspect == 1f) {
            width = (int) (width * mWidthWeight);
            height = (int) (height * mHeightWeight);
            if (width > height) {
                width = height;
            } else {
                height = width;
            }
        } else if (srcAspect < aspect) {
            // portrait
            width = (int) (mRect.width() * mWidthWeight);
            height = (int) (width / aspect);
        } else if (srcAspect > aspect) {
            // landscape
            height = (int) (mRect.height() * mHeightWeight);
            width = (int) (height * aspect);
        }

        mChildRect.set(mRect.centerX() - width / 2,
                       mRect.centerY() - height / 2,
                       mRect.centerX() + width / 2,
                       mRect.centerY() + height / 2);
    }
}
