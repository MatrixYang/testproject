package com.cooeeui.brand.zenlauncher.scenes;

import android.app.WallpaperManager;
import android.content.Context;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.android.view.DefinedScrollView;
import com.cooeeui.brand.zenlauncher.favorite.usagestats.UsageUtil;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.brand.zenlauncher.tips.ViewConfigurationCompat;
import com.cooeeui.zenlauncher.R;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Method;

public class SwitchPagedView extends ViewGroup {

    private Context mContext;
    private int mWidth;
    private int mHeight;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;
    private final static int TOUCH_STATE_STOP = 2;
    private int mTouchState;

    private float mLastMotionX;
    private float mLastMotionY;
    private float mDownMotionX;
    private static final int INVALID_POINTER = -1;
    private int mActivePointerId = INVALID_POINTER;

    private int mMaximumVelocity;
    private float mTouchSlop;

    private static final int PAGE_SNAP_ANIMATION_DURATION = 500;

    private int MOVE_SHORT_LENGTH;

    private int FLING_SLOW_SPEED;

    private int mCurrentPage;

    private int mpageNum;

    private Launcher mLauncher;

    private DefinedScrollView mDefinedScrollView = null;

    private WallpaperManager mWallpaperManager;

    private boolean mNotificationLaunched;

    public SwitchPagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mWallpaperManager = WallpaperManager.getInstance(mContext);
        init();

    }

    public void setup(Launcher launcher) {
        mLauncher = launcher;
    }

    private static class ScrollInterpolator implements Interpolator {

        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1;
        }
    }

    private void init() {
        mScroller = new Scroller(getContext(), new ScrollInterpolator());
        mTouchState = TOUCH_STATE_REST;
        mCurrentPage = Launcher.STATE_HOMESCREEN;

        scrollTo(mCurrentPage * DeviceUtils.getScreenPixelsWidth(mContext), 0);

        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        MOVE_SHORT_LENGTH = mContext.getResources()
            .getDimensionPixelSize(R.dimen.move_short_length);
        FLING_SLOW_SPEED = mContext.getResources()
            .getDimensionPixelSize(R.dimen.fling_slow_speed);
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

    private void determineScrollingStart(MotionEvent ev) {
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
        if (pointerIndex == -1 || mTouchState != TOUCH_STATE_REST) {
            return;
        }

        final float x = ev.getX(pointerIndex);
        final int xDiff = (int) Math.abs(x - mLastMotionX);

        final float y = ev.getY(pointerIndex);
        final int yDiff = (int) Math.abs(y - mLastMotionY);

        if (xDiff < MOVE_SHORT_LENGTH && yDiff < MOVE_SHORT_LENGTH) {
            return;
        }

        if (xDiff < yDiff) {
            mTouchState = TOUCH_STATE_STOP;
        } else {
            mTouchState = TOUCH_STATE_SCROLLING;
            mLastMotionX = x;
        }
    }

    private void resetTouchState() {
        releaseVelocityTracker();
        mTouchState = TOUCH_STATE_REST;
        mActivePointerId = INVALID_POINTER;
    }

    public void launchNotification() {
        Method expand = null;
        int curApiVersion = android.os.Build.VERSION.SDK_INT;
        try {
            Object service = Launcher.getInstance().getSystemService("statusbar");
            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            if (statusbarManager == null) {
                return;
            }
            if (curApiVersion <= 16) {
                expand = statusbarManager.getMethod("expand", new Class[0]);
            } else {
                expand = statusbarManager.getMethod("expandNotificationsPanel", new Class[0]);
            }
            if (expand != null) {
                expand.setAccessible(true);
                expand.invoke(service, new Object[0]);
                mNotificationLaunched = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDefinedScrollView = (DefinedScrollView) findViewById(R.id.defined_scroll_view);
    }

    private boolean isNotificationEnable(float move) {
        return (
            (mCurrentPage == Launcher.STATE_HOMESCREEN || mCurrentPage == Launcher.STATE_FAVORITE)
            && !mDefinedScrollView.getTouchDefinedScrollView()
            && move > 0
            && move > mContext.getResources().getDimension(R.dimen.notification_enable_dis));
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mLauncher.isNotScroll() || mLauncher.Allapp.isShowedAll()) {
            return false;
        }

        acquireVelocityTrackerAndAddMovement(ev);

        if (mpageNum <= 0) {
            return super.onInterceptTouchEvent(ev);
        }

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) &&
            (mTouchState == TOUCH_STATE_SCROLLING)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId != INVALID_POINTER) {
                    determineScrollingStart(ev);
                }
                if (isNotificationEnable(ev.getY() - mLastMotionY)) {
                    if (!mNotificationLaunched) {
                        launchNotification();
                    }
                }
                break;

            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();

                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = ev.getPointerId(0);
                mNotificationLaunched = false;
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetTouchState();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                releaseVelocityTracker();
                break;
        }

        return mTouchState == TOUCH_STATE_SCROLLING;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mpageNum <= 0 || mLauncher.Allapp.isShowedAll()) {
            return super.onTouchEvent(event);
        }

        acquireVelocityTrackerAndAddMovement(event);

        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                if ((int) Math.abs(event.getX() - mLastMotionX) < this.mTouchSlop) {
                    break;
                }

                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                mDownMotionX = mLastMotionX;

                mActivePointerId = event.getPointerId(0);

                break;

            case MotionEvent.ACTION_MOVE:
                if (mTouchState == TOUCH_STATE_SCROLLING) {

                    final int pointerIndex = event.findPointerIndex(mActivePointerId);

                    if (pointerIndex == -1) {
                        return true;
                    }

                    final float x = event.getX(pointerIndex);
                    final float deltaX = mLastMotionX - x;

                    updateScroll((int) deltaX);

                    mLastMotionX = x;
                } else {
                    determineScrollingStart(event);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_SCROLLING) {

                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityX = (int) mVelocityTracker.getXVelocity(mActivePointerId);

                    boolean isFling = Math.abs(mLastMotionX - mDownMotionX) > MOVE_SHORT_LENGTH;
                    if (isFling && velocityX > FLING_SLOW_SPEED && mCurrentPage > 0) {
                        snapToPage(mCurrentPage - 1);
                        if ((mCurrentPage - 1) == 0) {
                            // 进入插件页次数
                            MobclickAgent.onEvent(Launcher.getInstance(), "IntoWidget");
                        }
                    } else if (isFling && velocityX < -FLING_SLOW_SPEED
                               && mCurrentPage < (mpageNum - 1)) {
                        snapToPage(mCurrentPage + 1);
                    } else {
                        snapToDestination();
                    }
                }

                resetTouchState();
                break;

            case MotionEvent.ACTION_CANCEL:
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

    public void updateScroll(int deltaX) {
        int width = 0;

        if ((getScrollX() + deltaX) < width) {
            scrollTo(width, 0);
            return;
        } else if ((getScrollX() + deltaX) > ((mpageNum - 1) * mWidth)) {
            scrollTo((mpageNum - 1) * mWidth, 0);
            return;
        }
        scrollBy(deltaX, 0);
        updateWallpaperOffset();

    }

    public boolean isScrolling() {
        return getScrollX() % mWidth != 0 ? true : false;
    }

    private void snapToPage(int whichPage, int duration) {

        if (whichPage != Launcher.STATE_HOMESCREEN) {
            mLauncher.hideAreaPop();
        }

        if (whichPage == Launcher.STATE_FAVORITE) {
            mLauncher.showLoadingDialog();
            if (mLauncher.mFinishBindApp && SettingPreference.getGuideFavorite()) {
                mLauncher.mGuiding = true;
                guideRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mLauncher.showGuidePage(Launcher.STATE_FAVORITE);
                    }
                };
                postDelayed(guideRunnable, PAGE_SNAP_ANIMATION_DURATION + 20);
            }
        }

        mCurrentPage = whichPage;

        int dx = mCurrentPage * mWidth - getScrollX();

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        if (dx != 0) {
            mScroller.startScroll(getScrollX(), 0, dx, 0, duration);
        }

        invalidate();

        if (whichPage == Launcher.STATE_FAVORITE) {
            if (UsageUtil.isNoOption(mLauncher)) {
                UsageUtil.showUsageTwoButtonAlert(mLauncher);
            }
        }
    }

    private Runnable guideRunnable = null;

    public void removeGuide() {
        if (guideRunnable != null) {
            removeCallbacks(guideRunnable);
            mLauncher.mGuiding = false;
            guideRunnable = null;
        }
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public void snapToPage(int whichPage) {
        snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION);
    }

    public void snapToDestination() {
        int nextPage = mCurrentPage;
        int s = mCurrentPage * mWidth - getScrollX();

        if (mCurrentPage > 0 && s > mWidth / 4) {
            nextPage--;
            if (nextPage == 0) {
                // 进入插件页次数
                MobclickAgent.onEvent(Launcher.getInstance(), "IntoWidget");
            }
        } else if (mCurrentPage < mpageNum - 1 && -s > mWidth / 4) {
            nextPage++;
        }
        snapToPage(nextPage, PAGE_SNAP_ANIMATION_DURATION);

    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            updateWallpaperOffset();

            invalidate();
        }
    }

    private void updateWallpaperOffset() {
        int scrollRange = getChildAt(getChildCount() - 1).getRight() - getWidth();
        IBinder token = getWindowToken();
        if (token != null) {
            mWallpaperManager.setWallpaperOffsetSteps(1.0f / (getChildCount() - 1), 0);
            mWallpaperManager.setWallpaperOffsets(getWindowToken(),
                                                  Math.max(0.f, Math.min(
                                                      getScrollX() / (float) scrollRange, 1.f)), 0);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        mWidth = widthSize;
        mHeight = heightSize;

        setMeasuredDimension(widthSize, heightSize);

        mpageNum = getChildCount();

        for (int i = 0; i < mpageNum; i++) {
            View child = getChildAt(i);

            child.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int startLeft = 0;
        int startTop = 0;
        mpageNum = getChildCount();

        for (int i = 0; i < mpageNum; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() != View.GONE) {
                child.layout(startLeft, startTop,
                             startLeft + mWidth,
                             startTop + mHeight);
            }

            startLeft += mWidth;
        }
        updateWallpaperOffset();
    }

}
