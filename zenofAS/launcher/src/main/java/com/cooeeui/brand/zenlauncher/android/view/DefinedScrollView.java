package com.cooeeui.brand.zenlauncher.android.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.favorite.SpeedySetting;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.zenlauncher.R;

public class DefinedScrollView extends ViewGroup {

    private static final int TOUCH_STATE_RESET = 0;
    private static final int TOUCH_STATE_SCROLLING = 1;
    // Delay time
    private static final int THE_DELAY_TIME = 5000;
    private static final int THE_DELAY_AD_TIME = 5000;
    private static final int INVALID_POINTER = -1;
    private static final int SNAP_ANIMATION_DURATION = 400;
    public static final String
        NO_RECOMMEND_ACTION =
        "com.cooeeui.brand.zenlauncher.android.view.DefinedScrollView.NO_RECOMMEND_ACTION";

    private Context mContext;
    private int mTouchState = TOUCH_STATE_RESET;
    private int mActivePointerId = INVALID_POINTER;
    private Scroller mScroller;
    private boolean mScrollerStarted;

    //touchEvent patch to DefinedScrollView
    private boolean mTouchDefinedScrollView = false;

    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mFlingSlowSpeed;
    private int mCurrentScreen;
    private int mTouchSlop;
    private float mLastMotionY;
    private int mLastScrollY;

    private LinearLayout mRecentlyView;
    private FrameLayout mRecommendView;

    private MyReceiver myReceiver;
    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {

        @Override
        public void run() {
            try {
                upDate();
                if (mCurrentScreen == 0) {
                    handler.postDelayed(this, THE_DELAY_TIME);
                } else if (mCurrentScreen == 1) {
                    handler.postDelayed(this, THE_DELAY_AD_TIME);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private static class ScrollInterpolator implements Interpolator {

        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1;
        }
    }

    public DefinedScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        registerMyReceiver();
    }

    /**
     * 注册广播
     */
    private void registerMyReceiver() {
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NO_RECOMMEND_ACTION);
        mContext.registerReceiver(myReceiver, filter);
    }

    private void unregisterMyReceiver() {
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NO_RECOMMEND_ACTION);
        mContext.unregisterReceiver(myReceiver);
    }

    public DefinedScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRecentlyView = (LinearLayout) findViewById(R.id.ll_recently);
        mRecommendView = (FrameLayout) findViewById(R.id.ll_recommend);

        mScroller = new Scroller(mContext, new ScrollInterpolator());
        mTouchState = TOUCH_STATE_RESET;
        mCurrentScreen = 0;
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop() / 8;
        mMaximumVelocity = ViewConfiguration.get(mContext).getScaledMaximumFlingVelocity();
        mFlingSlowSpeed = mContext.getResources()
            .getDimensionPixelSize(R.dimen.fling_slow_speed);
        handler.postDelayed(runnable, THE_DELAY_TIME);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        mRecentlyView.layout(0, 0, mRecentlyView.getMeasuredWidth(),
                             mRecentlyView.getMeasuredHeight());
        mRecommendView.layout(0, 0, mRecommendView.getMeasuredWidth(),
                              mRecommendView.getMeasuredHeight());
        final View child = mRecommendView.getChildAt(0);
        int childTop = mRecommendView.getMeasuredHeight();
        child.layout(0, childTop, mRecommendView.getMeasuredWidth(),
                     childTop + mRecommendView.getMeasuredHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        if (widthMode != MeasureSpec.EXACTLY) {
//            throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");
//        }
//
//        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        if (heightMode != MeasureSpec.EXACTLY) {
//            throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");
//        }

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public boolean getTouchDefinedScrollView() {
        return mTouchDefinedScrollView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (Launcher.getInstance().Allapp.isShowedAll()
            || isOrNotAdMiss()) {
            return true;
        }
        acquireVelocityTrackerAndAddMovement(event);

        int action = event.getAction();
        if (event.getY() < -getHeight() / 4) {
            action = MotionEvent.ACTION_CANCEL;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mTouchDefinedScrollView = true;
                if ((int) Math.abs(event.getY() - mLastMotionY) < mTouchSlop) {
                    break;
                }
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                mLastMotionY = event.getY();
                mTouchState = TOUCH_STATE_RESET;
                mActivePointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                mTouchDefinedScrollView = true;
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    float y = event.getY();
                    int deltaY = (int) (mLastMotionY - y);
                    mLastMotionY = y;

                    int recentlyCurScrolly = mRecentlyView.getScrollY();
                    int recommendCurScrolly = mRecommendView.getScrollY();
                    int height = getHeight();

                    if (mCurrentScreen == 0) {
                        if (recentlyCurScrolly + deltaY < 0 && recommendCurScrolly + deltaY < 0) {
                            mRecentlyView.scrollTo(0, 0);
                            mRecommendView.scrollTo(0, height * 2);
                        } else if (recentlyCurScrolly + deltaY > 0
                                   && recommendCurScrolly + deltaY > height * 2) {
                            mRecentlyView.scrollTo(0, 0);
                            mRecommendView.scrollTo(0, 0);
                        }
                    } else if (mCurrentScreen == 1) {
                        if (recommendCurScrolly + deltaY > height
                            && recentlyCurScrolly + deltaY > height) {
                            mRecentlyView.scrollTo(0, -height);
                            mRecommendView.scrollTo(0, height);
                        } else if (recommendCurScrolly + deltaY < height
                                   && recentlyCurScrolly + deltaY < -height) {
                            mRecentlyView.scrollTo(0, height);
                            mRecommendView.scrollTo(0, height);
                        }
                    }

                    mRecentlyView.scrollBy(0, deltaY);
                    mRecommendView.scrollBy(0, deltaY);
                } else {
                    if (mActivePointerId != INVALID_POINTER) {
                        determineScrollingStart(event);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mTouchDefinedScrollView = false;
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityY = (int) mVelocityTracker.getYVelocity(mActivePointerId);
                    if (velocityY > mFlingSlowSpeed) {
                        flingToDown(velocityY);
                    } else if (velocityY < -mFlingSlowSpeed) {
                        flingToUp(velocityY);
                    } else {
                        snapToDestination();
                    }
                    handler.postDelayed(runnable, THE_DELAY_TIME);
                }
                resetTouchState();
                break;

            case MotionEvent.ACTION_CANCEL:
                mTouchDefinedScrollView = false;
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    snapToDestination();
                }
                resetTouchState();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                releaseVelocityTracker();
                break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (Launcher.getInstance().getmPopupRecently().isShow()) {
            return false;
        }


        if (Launcher.getInstance().Allapp.isShowedAll()
            || isOrNotAdMiss()) {
            return false;
        }
        acquireVelocityTrackerAndAddMovement(ev);

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_RESET)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId != INVALID_POINTER) {
                    determineScrollingStart(ev);
                }
                break;

            case MotionEvent.ACTION_DOWN:
                mLastMotionY = ev.getY();
                mActivePointerId = ev.getPointerId(0);
                mTouchState = TOUCH_STATE_RESET;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                resetTouchState();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                releaseVelocityTracker();
                break;
        }
        return mTouchState != TOUCH_STATE_RESET;
    }

    private void determineScrollingStart(MotionEvent ev) {
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
        if (pointerIndex == -1 || mTouchState != TOUCH_STATE_RESET) {
            return;
        }

        final float y = ev.getY(pointerIndex);
        final int yDiff = (int) Math.abs(y - mLastMotionY);

        if (yDiff < mTouchSlop) {
            return;
        }
        mTouchState = TOUCH_STATE_SCROLLING;
        mLastMotionY = y;
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        handler.removeCallbacks(runnable);
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void resetTouchState() {
        releaseVelocityTracker();
        mTouchState = TOUCH_STATE_RESET;
        mActivePointerId = INVALID_POINTER;
    }

    public boolean isScrolling() {
        return mTouchState == TOUCH_STATE_SCROLLING ? true : false;
    }

    public void snapToDestination() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        if (mCurrentScreen == 0) {
            mLastScrollY = mRecentlyView.getScrollY();

            int dy;
            if (Math.abs(mRecentlyView.getScrollY()) > getHeight() / 3) {
                if (mRecentlyView.getScrollY() > 0) {
                    dy = getHeight() - mRecentlyView.getScrollY();
                } else {
                    dy = Math.abs(mRecentlyView.getScrollY()) - getHeight();
                }
                mCurrentScreen = 1;
            } else {
                dy = -mRecentlyView.getScrollY();
            }
            mScroller.startScroll(0, mLastScrollY, 0, dy, SNAP_ANIMATION_DURATION);
            mScrollerStarted = true;
        } else if (mCurrentScreen == 1) {
            mLastScrollY = mRecommendView.getScrollY();

            int distance = Math.abs(mRecommendView.getScrollY() - getHeight());
            int dy;
            if (distance > getHeight() / 3) {
                if (mRecommendView.getScrollY() > getHeight()) {
                    dy = getHeight() * 2 - mRecommendView.getScrollY();
                } else {
                    dy = -mRecommendView.getScrollY();
                }
                mCurrentScreen = 0;
            } else {
                dy = getHeight() - mRecommendView.getScrollY();
            }
            mScroller.startScroll(0, mLastScrollY, 0, dy, SNAP_ANIMATION_DURATION);
            mScrollerStarted = true;
        }
        invalidate();
    }

    private void flingToUp(int velocity) {

        int dy = 0;

        if (mCurrentScreen == 0) {
            mLastScrollY = mRecentlyView.getScrollY();
            dy = getHeight() - mLastScrollY;
            mCurrentScreen = 1;
            mRecommendView.scrollTo(0, mLastScrollY);
        } else {
            mLastScrollY = mRecommendView.getScrollY();
            dy = getHeight() * 2 - mLastScrollY;
            mCurrentScreen = 0;
            mRecentlyView.scrollTo(0, -getHeight() + (mLastScrollY - getHeight()));
        }

        final int height = getHeight();
        final int halfHeight = height / 2;
        final float distanceRatio = Math.min(1.0f, 1.0f * Math.abs(dy) / height);
        final float distance = halfHeight + halfHeight *
                                            distanceInfluenceForSnapDuration(distanceRatio);

        int duration = SNAP_ANIMATION_DURATION;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        }
        duration = Math.min(duration, SNAP_ANIMATION_DURATION);

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        mScroller.startScroll(0, mLastScrollY, 0, dy, duration);
        mScrollerStarted = true;
        invalidate();
    }

    private void flingToDown(int velocity) {
        int dy = 0;

        if (mCurrentScreen == 0) {
            mLastScrollY = mRecentlyView.getScrollY();
            dy = Math.abs(mLastScrollY) - getHeight();
            mCurrentScreen = 1;
            mRecommendView.scrollTo(0, 2 * getHeight() + mLastScrollY);
        } else {
            mLastScrollY = mRecommendView.getScrollY();
            dy = -mLastScrollY;
            mCurrentScreen = 0;
            mRecentlyView.scrollTo(0, getHeight() + (mLastScrollY - getHeight()));
        }

        final int height = getHeight();
        final int halfHeight = height / 2;
        final float distanceRatio = Math.min(1.0f, 1.0f * Math.abs(dy) / height);
        final float distance = halfHeight + halfHeight *
                                            distanceInfluenceForSnapDuration(distanceRatio);

        int duration = SNAP_ANIMATION_DURATION;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        }
        duration = Math.min(duration, SNAP_ANIMATION_DURATION);

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        mScroller.startScroll(0, mLastScrollY, 0, dy, duration);
        mScrollerStarted = true;
        invalidate();
    }

    @Override
    public void computeScroll() {

        if (mScrollerStarted) {
            if (mScroller.computeScrollOffset()) {
                int curY = mScroller.getCurrY();
                int deltaY = curY - mLastScrollY;
                mLastScrollY = curY;
                mRecentlyView.scrollBy(0, deltaY);
                mRecommendView.scrollBy(0, deltaY);
                postInvalidate();
            } else {
                mScrollerStarted = false;
                if (mCurrentScreen == 0) {
                    mRecentlyView.scrollTo(0, 0);
                    mRecommendView.scrollTo(0, 0);
                } else {
                    mRecommendView.scrollTo(0, getHeight());
                    mRecentlyView.scrollTo(0, getHeight());
                }
            }
        }

    }

    /**
     * We want the duration of the page snap animation to be influenced by the distance that the
     * screen has to travel, however, we don't want this duration to be effected in a purely linear
     * fashion. Instead, we use this method to moderate the effect that the distance of travel has
     * on the overall snap duration.
     */
    @SuppressLint("FloatMath")
    float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) FloatMath.sin(f);
    }

    @SuppressWarnings("static-access")
    private void upDate() {
        // Log.i("upDate", "AdGetJson.adEntities.size():   "
        // + AdGetJson.adEntities.size());

        PowerManager pm = (PowerManager) Launcher.getInstance().getSystemService(
            Context.POWER_SERVICE);
        @SuppressWarnings("deprecation")
        boolean isScreenLight = pm.isScreenOn();
        Launcher launcher = Launcher.getInstance();

        if (launcher.switchPage.getCurrentPage() == launcher.STATE_FAVORITE
            && !launcher.switchPage.isScrolling()
            && !launcher.workspace.isAllappShowed()
            && !launcher.getmPopupRecently().isShow()
            && isScreenLight
            && hasWindowFocus()
            && getVisibility() == View.VISIBLE
            && isShown()
            && !SpeedySetting.isBrightControlShow) {
            if (isOrNotAdMiss()) {
                if (mCurrentScreen == 1) {
                    autoFlingToUp();
                }
            } else if (Launcher.getInstance().isDefaultDaysAgo()
                ) {
                if (mCurrentScreen == 0) {
                    autoFlingToUp();
                }
                return;
            } else {
                if (mScroller.isFinished() && mTouchState != TOUCH_STATE_SCROLLING
                    && SettingPreference.getAutoScrollValue()) {
                    autoFlingToUp();
                }
            }
        }
    }

    /**
     * 判断广告是否丢失了
     */
    private boolean isOrNotAdMiss() {
        boolean isMiss = true;
        try {
            if (Launcher.getInstance() != null
                && Launcher.getInstance().getRecommendGridView() != null) {
                GridView gridView = Launcher.getInstance().getRecommendGridView();
                int childCount = gridView.getChildCount();
                isMiss = (childCount == 0 ? true : false);
            } else {
                isMiss = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isMiss = true;
        }
        return isMiss;
    }

    private void autoFlingToUp() {
        if (mCurrentScreen == 0) {
            mRecentlyView.scrollTo(0, 0);
            mRecommendView.scrollTo(0, 0);
        } else {
            mRecentlyView.scrollTo(0, getHeight());
            mRecommendView.scrollTo(0, getHeight());
        }
        flingToUp(0);
    }

    /**
     * 不显示广告的广播接收器 当用户不想显示广告时操作setting tb_recommend_switch开关时，会发此广播让其界面跳转到最新安装界面！
     *
     * @author Administrator
     */
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mCurrentScreen == 1) {
                autoFlingToUp();
            }
        }
    }

}
