package com.cooeeui.brand.zenlauncher.scenes;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.android.view.DefinedScrollView;
import com.cooeeui.brand.zenlauncher.favorite.FavoriteScene;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.zenlauncher.R;

public class Workspace extends FrameLayout {

    private static final float ALPHA_SWITCH_PAGED_VIEW = 0f;
    private static final float SCALE_SWITCH_PAGED_VIEW = 0.9f;
    private static final float ALPHA_BLURED_VIEW = 1f;
    private static final int MAX_SETTLE_DURATION = 600; // ms
    private static final int INVALID_POINTER = -1;
    private static final int SCROLL_DERECTION_LEFT = 0;
    private static final int SCROLL_DERECTION_RIGHT = 1;
    private static final int SCROLL_DERECTION_NONE = -1;
    private Context mContext;
    private Launcher mLauncher;
    private SwitchPagedView mSwitchPagedView;
    private DefinedScrollView mDefinedScrollView;
    private Allapps mAllapp;
    private Scroller mAllappScroller;
    private float mLastMotionX;
    private float mLastMotionY;
    private int mActivePointerId = INVALID_POINTER;
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMoveShortLength;
    private int mFlingSlowSpeed;
    private boolean mIsFavoriteStatus;
    private boolean mAllappCanScroll;
    private boolean mAllappQuickReturn;
    private int scrollDerection = SCROLL_DERECTION_NONE;
    private FavoriteScene mFavoriteScene;
    private ValueAnimator mAutoAnimator;
    private AnimatorUpdateListener mAutoAnimatorUpdateListener = new AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            // TODO Auto-generated method stub
            Float value = (Float) animation.getAnimatedValue();
            mFavoriteScene.container.setAlpha(value.floatValue());
            float scale = (1f - value.floatValue()) * (1f - SCALE_SWITCH_PAGED_VIEW);
            mFavoriteScene.container.setScaleX(1f - scale);
            mFavoriteScene.container.setScaleY(1f - scale);
            if (mFavoriteScene.isBlurEnable()) {
                mFavoriteScene.bluredView.setAlpha((1f - value.floatValue()) * 1.5f);
            }
        }

    };
    private AnimatorListener mAutiAnimatorListener = new AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!isAllappShowed()) {
                if (isEditAllApp()) {
                    postHideApp();
                }
                if (mAllapp.popujarTemp != null && mAllapp.popujarTemp.isShow()) {
                    mAllapp.popujarTemp.dismiss();
                    mAllapp.popujarTemp = null;
                }
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

    };

    public Workspace(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.mContext = context;
    }

    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        this.mContext = context;
    }

    public Workspace(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        this.mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        mSwitchPagedView = (SwitchPagedView) findViewById(R.id.switch_page);
        mAllapp = (Allapps) findViewById(R.id.all_app);
        mDefinedScrollView = (DefinedScrollView) findViewById(R.id.defined_scroll_view);
        mFavoriteScene = (FavoriteScene) findViewById(R.id.rl_favorite_scene);
        init();
    }

    private void init() {
        mAllappScroller = new Scroller(this.mContext, new ScrollInterpolator());
        ViewConfiguration configuration = ViewConfiguration.get(this.mContext);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mMoveShortLength = mContext.getResources()
            .getDimensionPixelSize(R.dimen.move_short_length);
        mFlingSlowSpeed = mContext.getResources()
            .getDimensionPixelSize(R.dimen.fling_slow_speed);

        if (mAutoAnimator == null) {
            mAutoAnimator = ValueAnimator.ofFloat(0, 1f);
            mAutoAnimator.setInterpolator(new ScrollInterpolator());
            mAutoAnimator.setDuration(MAX_SETTLE_DURATION);
            mAutoAnimator.addUpdateListener(mAutoAnimatorUpdateListener);
            mAutoAnimator.addListener(mAutiAnimatorListener);
        }

    }

    public void setup(Launcher launcher) {
        mLauncher = launcher;
    }

    public boolean isAllappShowed() {
        return mAllapp.getScrollX() != 0;
    }

    public boolean isEditAllApp() {
        return mAllapp.isEdit;
    }

    public void postHideApp() {
        mAllapp.onHideApps();
    }

    public void hideAllappWithAnim() {
        smoothToOrigin();
        autoAnim(false, MAX_SETTLE_DURATION);
    }

    public void smoothToOrigin() {
        if (mAllapp.popujarTemp != null && mAllapp.popujarTemp.isShow()) {
            mAllapp.popujarTemp.dismiss();
        }
        smoothScrollTo(0, 0);
    }

    public void hideAllapp() {
        if (!mAllappScroller.isFinished()) {
            mAllappScroller.abortAnimation();
        }
        if (mAutoAnimator.isRunning()) {
            mAutoAnimator.end();
        }
        mAllapp.scrollTo(0, 0);
        mFavoriteScene.container.setAlpha(1f);
        mFavoriteScene.container.setScaleX(1f);
        mFavoriteScene.container.setScaleY(1f);
        mFavoriteScene.bluredView.setAlpha(0f);
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
        if (pointerIndex == INVALID_POINTER || mDefinedScrollView.isScrolling()
            || scrollDerection == SCROLL_DERECTION_RIGHT
            || Launcher.getInstance().isNotScroll()) {
            return;
        }

        final float x = ev.getX(pointerIndex);
        final int xDiff = (int) Math.abs(x - mLastMotionX);
        final float y = ev.getY(pointerIndex);
        final int yDiff = (int) Math.abs(y - mLastMotionY);
        if (xDiff < mMoveShortLength && yDiff < mMoveShortLength) {
            return;
        }

        if (xDiff < yDiff) {
            mAllappCanScroll = false;
        } else {
            if (mAllapp.getScrollX() > 0) {
                if (mAllapp.getScrollX() > mAllapp.getWidth() / 2) {
                    if (x - mLastMotionX > 0) {
                        mAllappCanScroll = true;
                        mAllappQuickReturn = false;
                        scrollDerection = SCROLL_DERECTION_RIGHT;
                    }
                }
            } else if (x - mLastMotionX < 0) {
                mAllappCanScroll = true;
                mAllappQuickReturn = false;
                scrollDerection = SCROLL_DERECTION_LEFT;
            } else if (mAllapp.getScrollX() <= 0 && x - mLastMotionX > 0) {
                scrollDerection = SCROLL_DERECTION_RIGHT;
            }
        }
        mLastMotionX = x;
        mLastMotionY = y;
    }

    private void resetTouchState() {
        releaseVelocityTracker();
        mActivePointerId = INVALID_POINTER;
        mIsFavoriteStatus = false;
        mAllappCanScroll = false;
        mAllappQuickReturn = false;
        scrollDerection = SCROLL_DERECTION_NONE;
    }

    private void quickReturnDetect() {
        if (mLastMotionX >= 0
            && mLastMotionX <= (DeviceUtils.getScreenPixelsWidth(mContext) - mAllapp
            .getWidth()) && mAllapp.getScrollX() == mAllapp.getWidth()) {
            mAllappQuickReturn = true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub

        if (mLauncher.mGuiding) {
            return false;
        }

        acquireVelocityTrackerAndAddMovement(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mSwitchPagedView.getCurrentPage() == Launcher.STATE_FAVORITE) {
                    mIsFavoriteStatus = true;
                    mLastMotionX = ev.getX();
                    mLastMotionY = ev.getY();
                    mActivePointerId = ev.getPointerId(0);
                    scrollDerection = SCROLL_DERECTION_NONE;
                    quickReturnDetect();
                } else {
                    mIsFavoriteStatus = false;
                    if (mAllapp.getScrollX() > 0) {
                        smoothToOrigin();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsFavoriteStatus && mActivePointerId != INVALID_POINTER) {
                    determineScrollingStart(ev);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetTouchState();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                resetTouchState();
                break;
        }
        return (mAllappCanScroll || mAllappQuickReturn) && !isEditAllApp();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        acquireVelocityTrackerAndAddMovement(event);

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mSwitchPagedView.getCurrentPage() == Launcher.STATE_FAVORITE) {
                    mIsFavoriteStatus = true;
                    mLastMotionX = event.getX();
                    mLastMotionY = event.getY();
                    mActivePointerId = event.getPointerId(0);
                    scrollDerection = SCROLL_DERECTION_NONE;
                    quickReturnDetect();
                } else {
                    mIsFavoriteStatus = false;
                    if (mAllapp.getScrollX() > 0) {
                        smoothToOrigin();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsFavoriteStatus) {
                    if (mAllappCanScroll) {
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
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mIsFavoriteStatus) {
                    if (mAllappCanScroll) {
                        mVelocityTracker.computeCurrentVelocity(1000,
                                                                mMaximumVelocity);
                        int velocityX = (int)
                            mVelocityTracker.getXVelocity(mActivePointerId);

                        if (velocityX > mFlingSlowSpeed) {
                            int duration = smoothScrollTo(0, 0, velocityX);
                            autoAnim(false, duration / 5);
                        } else if (velocityX < -mFlingSlowSpeed) {
                            int duration = smoothScrollTo(mAllapp.getWidth(), 0, velocityX);
                            // 进入allapp页面次数
                            autoAnim(true, duration / 5);
                        } else {
                            snapToDestination();
                        }
                    } else if (mAllappQuickReturn) {
                        smoothToOrigin();
                        autoAnim(false, MAX_SETTLE_DURATION);
                    } else {
                        smoothToOrigin();
                        autoAnim(false, MAX_SETTLE_DURATION);
                    }
                }
                resetTouchState();
                break;

            case MotionEvent.ACTION_CANCEL:
                if (mIsFavoriteStatus && mAllappCanScroll) {
                    snapToDestination();
                }
                resetTouchState();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (mIsFavoriteStatus && mAllappCanScroll) {
                    snapToDestination();
                }
                resetTouchState();
                break;
        }

        return true;
    }

    @Override
    public void computeScroll() {
        // TODO Auto-generated method stub
        if (!mAllappScroller.isFinished()) {
            if (mAllappScroller.computeScrollOffset()) {
                int oldX = mAllapp.getScrollX();
                int x = mAllappScroller.getCurrX();
                if (oldX != x) {
                    mAllapp.scrollTo(x, 0);
                }
                invalidate();
            }

        }

    }

    private void updateScroll(int deltaX) {
        if (mAutoAnimator.isRunning()) {
            mAutoAnimator.end();
        }
        if ((mAllapp.getScrollX() + deltaX) < 0) {
            mAllapp.scrollTo(0, 0);
            mFavoriteScene.container.setAlpha(1f);
            mFavoriteScene.container.setScaleX(1f);
            mFavoriteScene.container.setScaleY(1f);
            if (mFavoriteScene.isBlurEnable()) {
                mFavoriteScene.bluredView.setAlpha(0f);
            }
            return;
        } else if ((mAllapp.getScrollX() + deltaX) > mAllapp.getWidth()) {
            mAllapp.scrollTo(mAllapp.getWidth(), 0);
            mFavoriteScene.container.setAlpha(ALPHA_SWITCH_PAGED_VIEW);
            mFavoriteScene.container.setScaleX(SCALE_SWITCH_PAGED_VIEW);
            mFavoriteScene.container.setScaleY(SCALE_SWITCH_PAGED_VIEW);
            if (mFavoriteScene.isBlurEnable()) {
                mFavoriteScene.bluredView.setAlpha(ALPHA_BLURED_VIEW);
            }
            return;
        }
        mAllapp.scrollBy(deltaX, 0);

        float ratio = (float) mAllapp.getScrollX() / (float)
            mAllapp.getWidth();
        float alpha = ratio * (1f - ALPHA_SWITCH_PAGED_VIEW);
        mFavoriteScene.container.setAlpha(1f - alpha);
        float scale = ratio * (1f - SCALE_SWITCH_PAGED_VIEW);
        mFavoriteScene.container.setScaleX(1f - scale);
        mFavoriteScene.container.setScaleY(1f - scale);
        if (mFavoriteScene.isBlurEnable()) {
            alpha = ratio * 1.5f;
            mFavoriteScene.bluredView.setAlpha(alpha);
        }
    }

    private void snapToDestination() {
        switch (scrollDerection) {
            case SCROLL_DERECTION_LEFT:
                int distance = mAllapp.getScrollX();
                if (distance > mAllapp.getWidth() / 4) {
                    smoothScrollTo(mAllapp.getWidth(), 0);
                    // 进入allapp页面次数
                    autoAnim(true, MAX_SETTLE_DURATION);
                } else {
                    smoothToOrigin();
                    autoAnim(false, MAX_SETTLE_DURATION);
                }
                break;

            case SCROLL_DERECTION_RIGHT:
                int ds = mAllapp.getWidth() - mAllapp.getScrollX();
                if (ds > mAllapp.getWidth() / 4) {
                    smoothToOrigin();
                    autoAnim(false, MAX_SETTLE_DURATION);
                } else {
                    smoothScrollTo(mAllapp.getWidth(), 0);
                    autoAnim(true, MAX_SETTLE_DURATION);
                }
                break;

            default:
                smoothToOrigin();
                autoAnim(false, MAX_SETTLE_DURATION);
                break;
        }
    }

    /**
     * We want the duration of the page snap animation to be influenced by the distance that the
     * screen has to travel, however, we don't want this duration to be effected in a purely linear
     * fashion. Instead, we use this method to moderate the effect that the distance of travel has
     * on the overall snap duration.
     */
    float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) FloatMath.sin(f);
    }

    private void smoothScrollTo(int destX, int destY) {
        smoothScrollTo(destX, destY, 0);
    }

    private int smoothScrollTo(int destX, int destY, int velocity) {
        int sx = mAllapp.getScrollX();
        int dx = destX - sx;

        final int width = mAllapp.getWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1.0f, 1.0f * Math.abs(dx) / width);
        final float distance = halfWidth + halfWidth *
                                           distanceInfluenceForSnapDuration(distanceRatio);

        int duration = MAX_SETTLE_DURATION;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        }
        duration = Math.min(duration, MAX_SETTLE_DURATION);

        if (!mAllappScroller.isFinished()) {
            mAllappScroller.abortAnimation();
        }
        mAllappScroller.startScroll(sx, 0, dx, 0, duration);

        invalidate();

        return duration;
    }

    private void autoAnim(boolean showAllApp, int duration) {
        if (mAutoAnimator.isRunning()) {
            mAutoAnimator.end();
        }

        if (showAllApp) {
            mAutoAnimator
                .setFloatValues(mFavoriteScene.container.getAlpha(), ALPHA_SWITCH_PAGED_VIEW);

            if (SettingPreference.getGuideAllApp()) {
                mLauncher.mGuiding = true;
                guideRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mLauncher.showGuidePage(Launcher.STATE_ALLAPP);
                    }
                };
                postDelayed(guideRunnable, MAX_SETTLE_DURATION + 20);
            }
        } else {
            mAutoAnimator.setFloatValues(mFavoriteScene.container.getAlpha(), 1f);
        }
        mAutoAnimator.start();
    }

    private Runnable guideRunnable = null;

    public void removeGuide() {
        if (guideRunnable != null) {
            removeCallbacks(guideRunnable);
            mLauncher.mGuiding = false;
            guideRunnable = null;
        }
    }

    private static class ScrollInterpolator implements Interpolator {

        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1;
        }
    }

}
