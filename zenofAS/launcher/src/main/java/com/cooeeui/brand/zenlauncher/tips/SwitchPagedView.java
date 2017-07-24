package com.cooeeui.brand.zenlauncher.tips;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.cooeeui.zenlauncher.R;

public class SwitchPagedView extends ViewGroup {

    interface onPageChangedListener {

        public void setCurrentPageDate(int position);
    }

    private onPageChangedListener monPageChangedListener;

    public void setOnPageChangedListener(
        onPageChangedListener monSwitchPagedListener) {
        this.monPageChangedListener = monSwitchPagedListener;
    }

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

    private static final int INVALID_POINTER = -1;
    private int mActivePointerId = INVALID_POINTER;

    private int mMaximumVelocity;
    private float mTouchSlop;

    private static final int PAGE_SNAP_ANIMATION_DURATION = 500;

    private int MOVE_SHORT_LENGTH;

    private int FLING_SLOW_SPEED;

    private int mCurrentPage;

    private int mpageNum;

    public SwitchPagedView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        init();
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
        mCurrentPage = 8;

        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        this.mTouchSlop = ViewConfigurationCompat
            .getScaledPagingTouchSlop(configuration);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        MOVE_SHORT_LENGTH = mContext.getResources().getDimensionPixelSize(
            R.dimen.move_short_length);
        FLING_SLOW_SPEED = mContext.getResources().getDimensionPixelSize(
            R.dimen.fling_slow_speed);
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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        acquireVelocityTrackerAndAddMovement(ev);

        if (mpageNum <= 0) {
            return super.onInterceptTouchEvent(ev);
        }

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE)
            && (mTouchState == TOUCH_STATE_SCROLLING)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId != INVALID_POINTER) {
                    determineScrollingStart(ev);
                }
                break;

            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();
                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = ev.getPointerId(0);
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

        if (mpageNum <= 0) {
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

                mActivePointerId = event.getPointerId(0);

                break;

            case MotionEvent.ACTION_MOVE:
                if (mTouchState == TOUCH_STATE_SCROLLING) {

                    final int pointerIndex = event
                        .findPointerIndex(mActivePointerId);

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
                    int velocityX = (int) mVelocityTracker
                        .getXVelocity(mActivePointerId);

                    if (velocityX > FLING_SLOW_SPEED && mCurrentPage > 0) {
                        snapToPage(mCurrentPage - 1);
                    } else if (velocityX < -FLING_SLOW_SPEED
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

        if ((getScrollX() + deltaX) < 0) {
            scrollTo(0, 0);
            return;
        } else if ((getScrollX() + deltaX) > ((mpageNum - 1) * mWidth)) {
            scrollTo((mpageNum - 1) * mWidth, 0);
            return;
        }
        scrollBy(deltaX, 0);

    }

    private void snapToPage(int whichPage, int duration) {

        System.out.println("page:" + whichPage);
        mCurrentPage = whichPage;
        int dx = mCurrentPage * mWidth - getScrollX();
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        if (dx != 0) {
            mScroller.startScroll(getScrollX(), 0, dx, 0, duration);
        }
        invalidate();
        monPageChangedListener.setCurrentPageDate(whichPage);
    }

    public int getCurrentPage() {
        if (mWidth == 0) {
            return 0;
        }
        if (getScrollX() % mWidth != 0) {
            return -1;
        }
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
        } else if (mCurrentPage < mpageNum - 1 && -s > mWidth / 4) {
            nextPage++;
        }
        snapToPage(nextPage, PAGE_SNAP_ANIMATION_DURATION);

    }

    public void setCurrentPage(int curPage) {
        mCurrentPage = curPage;
        int targetX = mCurrentPage * getWidth();
        scrollTo(targetX, 0);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
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
                child.layout(startLeft, startTop, startLeft + mWidth, startTop
                                                                      + mHeight);
            }

            startLeft += mWidth;
        }
    }

}
