package com.cooeeui.brand.zenlauncher.widgets;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.widgets.WidgetsListView.TouchCompleteListener;

public class LauncherAppWidgetHostView extends AppWidgetHostView implements TouchCompleteListener {

    private CheckLongPressHelper mLongPressHelper;
    private Context mContext;
    private Launcher mLauncher;
    private int mPreviousOrientation;
    private int mDownY = -1;
    private final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mTouchSlop;

    public LauncherAppWidgetHostView(Context context) {
        super(context);
        mContext = context;
        mLauncher = (Launcher) context;
        mLongPressHelper = new CheckLongPressHelper(this);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mTouchSlop += mTouchSlop / 2;
    }

    @Override
    public void updateAppWidget(RemoteViews remoteViews) {
        mPreviousOrientation = mContext.getResources().getConfiguration().orientation;
        super.updateAppWidget(remoteViews);
    }

    public boolean orientationChangedSincedInflation() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (mPreviousOrientation != orientation) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mLongPressHelper.hasPerformedLongPress()) {
            mLongPressHelper.cancelLongPress();
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = (int) event.getY();
                mActivePointerId = event.getPointerId(0);
                if (mActivePointerId != INVALID_POINTER_ID) {
                    mLongPressHelper.postCheckForLongPress();
                    mLauncher.getWidgetsView().setTouchCompleteListener(this);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER_ID) {
                    break;
                }

                int pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex == INVALID_POINTER_ID) {
                    break;
                }

                int deltaY = Math.abs((int) event.getY(pointerIndex) - mDownY);
                if (deltaY > mTouchSlop) {
                    mLongPressHelper.cancelLongPress();
                    mLauncher.getWidgetsView().requestDisallowInterceptTouchEvent(false);
                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLongPressHelper.cancelLongPress();
                break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLongPressHelper.cancelLongPress();
                break;
        }
        return false;
    }

    @Override
    public void onTouchComplete() {
        mLongPressHelper.cancelLongPress();
    }

    @Override
    public int getDescendantFocusability() {
        return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
    }

    public void checkForLongPress() {
        mLongPressHelper.postCheckForLongPress();
        mLauncher.getWidgetsView().setTouchCompleteListener(this);
    }
}
