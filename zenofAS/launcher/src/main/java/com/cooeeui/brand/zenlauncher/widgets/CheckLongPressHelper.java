package com.cooeeui.brand.zenlauncher.widgets;

import android.os.Handler;
import android.view.View;

public class CheckLongPressHelper {

    private View mView;
    private boolean mHasPerformedLongPress;
    private CheckForLongPress mPendingCheckForLongPress;

    private Handler mHandler = new Handler();

    class CheckForLongPress implements Runnable {

        public void run() {
            if ((mView.getParent() != null) && !mHasPerformedLongPress) {
                if (mView.performLongClick()) {
                    mView.clearFocus();
                    mView.setPressed(false);
                    mHasPerformedLongPress = true;
                }
            }
        }
    }

    public CheckLongPressHelper(View v) {
        mView = v;
    }

    public void postCheckForLongPress() {
        if (mPendingCheckForLongPress == null) {
            mHasPerformedLongPress = false;
            mPendingCheckForLongPress = new CheckForLongPress();
            mHandler.postDelayed(mPendingCheckForLongPress, 500);
        }
    }

    public void cancelLongPress() {
        mHasPerformedLongPress = false;
        if (mPendingCheckForLongPress != null) {
            mHandler.removeCallbacks(mPendingCheckForLongPress);
            mPendingCheckForLongPress = null;
        }
    }

    public boolean hasPerformedLongPress() {
        return mHasPerformedLongPress;
    }
}
