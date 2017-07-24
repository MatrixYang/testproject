package com.cooeeui.brand.zenlauncher.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherModel;

public class WidgetResizeFrame extends FrameLayout {

    private Launcher mLauncher;
    private WidgetsListView mWidgetsView;

    private int mHeight;
    private int mWidgetWidth;
    private int mWidgetHeight;
    private int mWidgetMinHeight;
    private int mWidgetMaxHeight;
    private int mBlankHeight;

    private LauncherAppWidgetInfo mWidgetInfo;
    private LinearLayout.LayoutParams mWidgetParams;
    private FrameLayout.LayoutParams mDragParams;

    private int TOUCH_PADDING;
    private int WIDGET_MIN_HEIGHT;

    public WidgetResizeFrame(Context context) {
        super(context);

        mLauncher = (Launcher) context;
        float density = mLauncher.getResources().getDisplayMetrics().density;
        TOUCH_PADDING = (int) (density * 50);
    }

    public void setWidgetView(Rect r) {
        mWidgetsView = mLauncher.getWidgetsView();
        mWidgetInfo = mWidgetsView.getSelectInfo();
        if (mWidgetInfo == null) {
            return;
        }

        mBlankHeight = mWidgetsView.getBlankHeight();
        mHeight = r.bottom - r.top;
        mWidgetWidth = mWidgetInfo.width;
        mWidgetHeight = mWidgetInfo.height;
        mWidgetMinHeight = mWidgetInfo.hostView.getAppWidgetInfo().minResizeHeight;
        WIDGET_MIN_HEIGHT = mLauncher.getDragLayer().getHeight() / 7;
        if (mWidgetMinHeight < WIDGET_MIN_HEIGHT) {
            mWidgetMinHeight = WIDGET_MIN_HEIGHT;
        }
        mWidgetMaxHeight = mLauncher.getDragLayer().getHeight() * 8 / 10;
        mWidgetParams = (LinearLayout.LayoutParams) mWidgetInfo.hostView.getLayoutParams();

        mDragParams = (FrameLayout.LayoutParams) getLayoutParams();
        mDragParams.width = r.right - r.left;
        mDragParams.height = mHeight;
        requestLayout();
        setTranslationX(r.left);
        setTranslationY(r.top);
    }

    public void moveWidgetView(Rect r) {
        setTranslationX(r.left);
        setTranslationY(r.top);
    }

    public boolean isInRegion(int y) {
        int curY = y - (int) getTranslationY();
        if (curY > mHeight - TOUCH_PADDING && curY < mHeight + TOUCH_PADDING / 2) {
            return true;
        }
        return false;
    }

    public void resizeForDelta(int deltaY) {
        if (mWidgetHeight + deltaY > mWidgetMinHeight
            && mWidgetHeight + deltaY < mWidgetMaxHeight) {
            mDragParams.height = mHeight + deltaY;
            requestLayout();

            mWidgetParams.width = mWidgetWidth;
            mWidgetParams.height = mWidgetHeight + deltaY;
            mWidgetInfo.hostView.requestLayout();

            if (mWidgetsView.isBlankResize()) {
                mWidgetsView.setBlankHeight(mBlankHeight - deltaY);
            } else {
                mWidgetsView.setBlankHeight(1);
            }
        }
    }

    public void onTouchUp() {
        mHeight = mDragParams.height;
        mWidgetHeight = mWidgetParams.height;
        if (mWidgetInfo != null) {
            mWidgetInfo.height = mWidgetHeight;
            LauncherModel.updateItemInDatabase(mLauncher, mWidgetInfo);
        }
        mBlankHeight = mWidgetsView.getBlankHeight();
    }
}
