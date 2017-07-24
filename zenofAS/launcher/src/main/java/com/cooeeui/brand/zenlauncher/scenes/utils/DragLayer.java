package com.cooeeui.brand.zenlauncher.scenes.utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.widgets.WidgetResizeFrame;

public class DragLayer extends FrameLayout {

    private Launcher mLauncher;

    private DragController mDragController;

    private WidgetResizeFrame mWidgetResizeFrame;

    private WidgetResizeFrame mCurrentResizeFrame;

    private boolean isResize = false;

    private int mYDown;

    private Rect mRect = new Rect();

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(Launcher launcher, DragController controller) {
        mLauncher = launcher;
        mDragController = controller;
        mWidgetResizeFrame = new WidgetResizeFrame(mLauncher);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mWidgetResizeFrame.setLayoutParams(lp);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragController.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (handleTouchDown(ev)) {
            return true;
        }
        return mDragController.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mCurrentResizeFrame != null) {
            int y = (int) ev.getY();

            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    mCurrentResizeFrame.resizeForDelta(y - mYDown);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mCurrentResizeFrame.resizeForDelta(y - mYDown);
                    mCurrentResizeFrame.onTouchUp();
                    mCurrentResizeFrame = null;
            }

            return true;
        }

        return mDragController.onTouchEvent(ev);
    }

    private boolean handleTouchDown(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            int y = (int) ev.getY();
            if (isResize && mCurrentResizeFrame == null && mWidgetResizeFrame.isInRegion(y)) {
                mYDown = y;
                mCurrentResizeFrame = mWidgetResizeFrame;
                requestDisallowInterceptTouchEvent(true);
                return true;
            }
        }
        return false;
    }

    public void addResizeFrame(Rect rect) {
        if (!isResize) {
            isResize = true;
            addView(mWidgetResizeFrame);
        }
        getGlobalVisibleRect(mRect);
        rect.offset(-mRect.left, -mRect.top);
        mWidgetResizeFrame.setWidgetView(rect);
    }

    public void moveResizeFrame(Rect rect) {
        if (isResize) {
            getGlobalVisibleRect(mRect);
            rect.offset(-mRect.left, -mRect.top);
            mWidgetResizeFrame.moveWidgetView(rect);
        }
    }

    public void clearResizeFrame() {
        if (isResize) {
            isResize = false;
            removeView(mWidgetResizeFrame);
        }
    }
}
