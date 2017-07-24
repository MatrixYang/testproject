package com.cooeeui.brand.zenlauncher.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;

public class WidgetsListView extends ListView implements OnLongClickListener {

    private final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
    private final int MOVE_DURATION = 150;

    private int mLastEventY = -1;

    private int mDownY = -1;
    private int mDownX = -1;

    private int mTotalOffset = 0;

    private boolean mIsDown = false;
    private boolean mCellIsMobile = false;
    private boolean mIsMobileScrolling = false;
    private int mSmoothScrollAmountAtEdge = 0;

    private final int INVALID_ID = -1;
    private long mAboveItemId = INVALID_ID;
    private long mMobileItemId = INVALID_ID;
    private long mBelowItemId = INVALID_ID;

    private BitmapDrawable mHoverCell;
    private Rect mHoverCellCurrentBounds;
    private Rect mHoverCellOriginalBounds;

    private Drawable mResizeSelect;
    private Drawable mResizeFocus;
    private Drawable mResizeBottom;
    private Rect mResizeBounds;
    private Rect mResizeRect;
    private int mBottomWidth;
    private int mBottomHeight;
    private int mTopPadding;
    private boolean mLongState = false;

    private final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private boolean mIsWaitingForScrollFinish = false;
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    private TouchCompleteListener mTouchCompleteListener;
    private int mTouchSlop;

    private Launcher mLauncher;
    private float mDensity;

    private Rect mRect;

    private WidgetAdapter mWidgetAdapter;

    private LauncherAppWidgetInfo mSelectInfo;

    private int mEndViewVisible;

    private ObjectAnimator mHoverViewAnimator;

    private LauncherAppWidgetInfo mDownInfo;

    private long mDownItemId = INVALID_ID;

    private boolean mClicked = false;

    private int MIN_BLANK_HEIGHT;

    public WidgetsListView(Context context) {
        super(context);
    }

    public WidgetsListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WidgetsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(Launcher launcher) {
        mLauncher = launcher;
        mResizeBounds = new Rect();
        mResizeRect = new Rect();
        mResizeSelect = mLauncher.getResources().getDrawable(R.drawable.widget_select);
        mResizeFocus = mLauncher.getResources().getDrawable(R.drawable.widget_focus);
        mResizeBottom = mLauncher.getResources().getDrawable(R.drawable.widget_resize_bottom);
        mHoverViewAnimator = null;

        setOnScrollListener(mScrollListener);
        mDensity = mLauncher.getResources().getDisplayMetrics().density;
        mSmoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE / mDensity);
        final ViewConfiguration configuration = ViewConfiguration.get(mLauncher);
        mTouchSlop = configuration.getScaledTouchSlop() * 3 / 2;
        mBottomWidth = (int) (mDensity * 17);
        mBottomHeight = (int) (mDensity * 17);
        mTopPadding = (int) (mDensity * 5);
        MIN_BLANK_HEIGHT = (int) (mDensity * 25);

        getViewTreeObserver().addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    if (mCellIsMobile || mLauncher.isWidgetOptionShow()) {
                        return;
                    }

                    int count = WidgetsListView.this.getChildCount();

                    if (count < 1) {
                        mWidgetAdapter.setEndViewVisibility(View.INVISIBLE);
                        mLauncher.hideBottomView();
                        return;
                    }

                    View v = getChildAt(count - 1);
                    if (v.getBottom() < getHeight()) {
                        mWidgetAdapter.setEndViewVisibility(View.INVISIBLE);
                        mLauncher.showBottomView();
                    } else {
                        mWidgetAdapter.setEndViewVisibility(View.VISIBLE);
                        mLauncher.hideBottomView();
                    }
                }
            });
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        mWidgetAdapter = (WidgetAdapter) adapter;
    }

    @Override
    public boolean onLongClick(View view) {
        mTotalOffset = 0;

        if (mLongState && mMobileItemId != INVALID_ID) {
            mSelectInfo = mDownInfo;
            mMobileItemId = mDownItemId;
            View selectedView = getViewForID(mMobileItemId);
            if (selectedView == null) {
                return true;
            }
            mHoverCell = getAndAddHoverView(selectedView);
        } else {
            int position = pointToPosition(mDownX, mDownY);
            int itemNum = position - getFirstVisiblePosition();

            mSelectInfo = (LauncherAppWidgetInfo) mWidgetAdapter.getItem(position);

            if (mSelectInfo == null) {
                return true;
            }

            View selectedView = getChildAt(itemNum);

            if (selectedView == null) {
                return true;
            }

            mMobileItemId = mWidgetAdapter.getItemId(position);
            mHoverCell = getAndAddHoverView(selectedView);
        }

        mSelectInfo.hostView.setVisibility(View.INVISIBLE);

        mEndViewVisible = mWidgetAdapter.getEndViewVisibility();
        mWidgetAdapter.setEndViewVisibility(View.INVISIBLE);
        mLauncher.hideBottomView();
        setBlankHeight(MIN_BLANK_HEIGHT);

        WidgetsListView.this.setPressed(false);

        mCellIsMobile = true;

        mLongState = true;

        updateNeighborViewsForID(mMobileItemId);

        mLauncher.hideWidgetOption();
        mLauncher.showDragText();

        invalidate();

        return true;
    }

    public boolean isBlankResize() {
        return getLastVisiblePosition() >= mWidgetAdapter.getCount() - 1;
    }

    public int getBlankHeight() {
        return mWidgetAdapter.getEndHeight();
    }

    public void setBlankHeight(int h) {
        if (h < MIN_BLANK_HEIGHT) {
            h = MIN_BLANK_HEIGHT;
        }
        mWidgetAdapter.setEndHeight(h);
    }

    public LauncherAppWidgetInfo getSelectInfo() {
        return mSelectInfo;
    }

    public void resetSelect() {
        mHoverCell = null;
        mCellIsMobile = false;
        if (mSelectInfo != null) {
            mSelectInfo.hostView.setVisibility(View.VISIBLE);
        }
        mWidgetAdapter.setEndViewVisibility(mEndViewVisible);
        if (mEndViewVisible != View.VISIBLE) {
            mLauncher.showBottomView();
        }
        mWidgetAdapter.reSetEndHeight();

        mLauncher.getDragLayer().clearResizeFrame();
        mMobileItemId = INVALID_ID;
        mLongState = false;
        invalidate();
    }

    private BitmapDrawable getAndAddHoverView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        int top = v.getTop();
        int left = v.getLeft();

        Bitmap b = getBitmap(v);

        BitmapDrawable drawable = new BitmapDrawable(mLauncher.getResources(), b);

        mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
        mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);

        drawable.setBounds(mHoverCellCurrentBounds);

        return drawable;
    }

    private Bitmap getBitmap(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        Canvas can = new Canvas(bitmap);
        Rect rect = new Rect(0, mTopPadding, bitmap.getWidth(), bitmap.getHeight());

        can.drawBitmap(bitmap, 0, 0, null);
        Drawable d = mLauncher.getResources().getDrawable(R.drawable.widget_select);
        d.setBounds(rect);
        d.draw(can);

        return bitmap;
    }

    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    private void updateNeighborViewsForID(long itemID) {
        int position = getPositionForID(itemID);
        mAboveItemId = mWidgetAdapter.getItemId(position - 1);
        mBelowItemId = mWidgetAdapter.getItemId(position + 1);
    }

    public View getViewForID(long itemID) {
        if (itemID < 0) {
            return null;
        }

        int firstVisiblePosition = getFirstVisiblePosition();

        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            int position = firstVisiblePosition + i;
            long id = mWidgetAdapter.getItemId(position);
            if (id == itemID) {
                return v;
            }
        }
        return null;
    }

    public int getPositionForID(long itemID) {
        return mWidgetAdapter.getPosition(itemID);
    }

    private static final int ADD_FONT_SIZE = 40;

    private static final int ADD_TEXT_FONT_SIZE = 18;

    private static final int ADD_INTERNAL = 6;

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (getChildCount() <= 0) {
            int width = DeviceUtils.getScreenPixelsWidth(mLauncher);
            int height = DeviceUtils.getScreenPixelsHeight(mLauncher);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            float radius = getResources().getInteger(R.integer.shadow_radius);
            float dy = getResources().getInteger(R.integer.shadow_dy);
            paint.setShadowLayer(radius, 0, dy, R.color.text_shadow_color);
            paint.setStrokeWidth(2);
            if (mIsDown) {
                paint.setColor(Color.rgb(201, 201, 201));
            } else {
                paint.setColor(Color.WHITE);
            }

            int top = (height -
                       (int) (mDensity * (ADD_FONT_SIZE + ADD_TEXT_FONT_SIZE + ADD_INTERNAL))) / 2;
            Rect r = new Rect(0, top, width, (int) (top + mDensity * ADD_FONT_SIZE));
            paint.setTextSize(mDensity * ADD_FONT_SIZE);
            FontMetricsInt fMetrics = paint.getFontMetricsInt();
            int bline = r.top + (r.bottom - r.top - fMetrics.bottom + fMetrics.top) / 2
                        - fMetrics.top;
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("+", r.centerX(), bline, paint);

            Rect targetRect = new Rect(
                0,
                r.bottom + (int) (mDensity * ADD_INTERNAL),
                width,
                r.bottom + (int) (mDensity * (ADD_INTERNAL + ADD_TEXT_FONT_SIZE)));

            String testString = StringUtil.getString(mLauncher, R.string.widget_long_press);
            paint.setStrokeWidth(3);
            paint.setTextSize(mDensity * ADD_TEXT_FONT_SIZE);
            FontMetricsInt fontMetrics = paint.getFontMetricsInt();
            int baseline = targetRect.top
                           + (targetRect.bottom - targetRect.top - fontMetrics.bottom
                              + fontMetrics.top)
                             / 2 - fontMetrics.top;
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(testString, targetRect.centerX(), baseline, paint);

            mRect = new Rect((int) (mDensity * 50), r.top, (int) (width - mDensity * 50),
                             targetRect.bottom);
        }

        Rect r = null;

        if (mHoverCell != null) {
            mHoverCell.draw(canvas);
            r = mHoverCell.getBounds();
            mResizeFocus.setBounds(r.left + (int) (mDensity * 2), r.top + mTopPadding,
                                   r.right - (int) (mDensity * 2), r.bottom - (int) (mDensity * 2));
            mResizeFocus.draw(canvas);
        } else if (mLongState && isMoveResizeFrame()) {
            r = mResizeSelect.getBounds();
            mResizeFocus.setBounds(r.left + (int) (mDensity * 2), r.top,
                                   r.right - (int) (mDensity * 2), r.bottom - (int) (mDensity * 2));
            mResizeFocus.draw(canvas);
            mResizeSelect.draw(canvas);
        }
        if (r != null) {
            int x = (r.right + r.left - mBottomWidth) / 2;
            mResizeBottom.setBounds(x, r.bottom - (int) (mDensity * 3) - mBottomHeight / 2,
                                    x + mBottomWidth,
                                    r.bottom - (int) mDensity + mBottomHeight / 2);
            mResizeBottom.draw(canvas);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                mActivePointerId = event.getPointerId(0);
                mIsDown = true;
                if (mRect != null && getChildCount() <= 0 && mRect.contains(mDownX, mDownY)) {
                    invalidate();
                }

                if (mLongState) {
                    mClicked = false;
                    if (mHoverViewAnimator != null && mHoverViewAnimator.isRunning()) {
                        mHoverViewAnimator.end();
                    }
                    if (mHoverCell != null) {
                        break;
                    }

                    int position = pointToPosition(mDownX, mDownY);
                    LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) mWidgetAdapter
                        .getItem(position);
                    if (info != null) {
                        mClicked = true;
                        mDownInfo = info;
                        mDownItemId = mWidgetAdapter.getItemId(position);
                        LauncherAppWidgetHostView view = (LauncherAppWidgetHostView) info.hostView;
                        view.checkForLongPress();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mCellIsMobile) {
                    break;
                }

                if (mActivePointerId == INVALID_POINTER_ID) {
                    break;
                }

                int pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex == INVALID_POINTER_ID) {
                    break;
                }

                int deltaY = Math.abs((int) event.getY(pointerIndex) - mDownY);

                if (deltaY > 12 && deltaY < mTouchSlop) {
                    onTouchComplete();
                    requestDisallowInterceptTouchEvent(true);
                }

                if (deltaY > 12) {
                    mIsDown = false;
                    mClicked = false;
                    if (getChildCount() <= 0) {
                        invalidate();
                    }
                    onTouchComplete();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mIsDown && mRect != null && getChildCount() <= 0
                    && mRect.contains(mDownX, mDownY)) {
                    mLauncher.showAddWidget(false);
                }

                if (mCellIsMobile) {
                    mLauncher.hideDragText();
                    mLauncher.showWidgetOption();
                }

                if (mLongState && mClicked) {
                    mSelectInfo = mDownInfo;
                    mMobileItemId = mDownItemId;
                }

                onTouchComplete();
                touchEventsEnded();
                requestDisallowInterceptTouchEvent(false);
                mIsDown = false;
                mClicked = false;
                break;

            case MotionEvent.ACTION_CANCEL:
                onTouchComplete();
                touchEventsCancelled();
                requestDisallowInterceptTouchEvent(false);
                mIsDown = false;
                mClicked = false;
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mCellIsMobile || mLongState) {
            return true;
        }

        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                mActivePointerId = event.getPointerId(0);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER_ID) {
                    break;
                }

                int pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex == INVALID_POINTER_ID) {
                    break;
                }

                mLastEventY = (int) event.getY(pointerIndex);
                int deltaY = mLastEventY - mDownY;

                if (mCellIsMobile) {
                    mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left,
                                                     mHoverCellOriginalBounds.top + deltaY
                                                     + mTotalOffset);
                    mHoverCell.setBounds(mHoverCellCurrentBounds);
                    invalidate();

                    handleCellSwitch();

                    mIsMobileScrolling = false;
                    handleMobileCellScroll();

                    return false;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                               MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    touchEventsEnded();
                }
                break;

            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    private void handleCellSwitch() {
        final int deltaY = mLastEventY - mDownY;
        int deltaYTotal = mHoverCellOriginalBounds.top + mTotalOffset + deltaY;

        View belowView = getViewForID(mBelowItemId);
        View aboveView = getViewForID(mAboveItemId);

        boolean isBelow = (belowView != null) && (deltaYTotal > belowView.getTop())
                          && (belowView.getBottom() < getHeight());
        boolean isAbove = (aboveView != null) && (deltaYTotal < aboveView.getTop());

        if (isBelow || isAbove) {

            final long switchItemID = isBelow ? mBelowItemId : mAboveItemId;
            View switchView = isBelow ? belowView : aboveView;
            final int originalItem = getPositionForID(mMobileItemId);

            if (switchView == null) {
                updateNeighborViewsForID(mMobileItemId);
                return;
            }

            final SwappableList adapter = (SwappableList) mWidgetAdapter;
            adapter.swap(originalItem, getPositionForID(switchItemID));

            mDownY = mLastEventY;

            final int switchViewStartTop = switchView.getTop();

            updateNeighborViewsForID(mMobileItemId);

            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);

                    View switchView = getViewForID(switchItemID);

                    mTotalOffset += deltaY;

                    if (switchView != null) {
                        int switchViewNewTop = switchView.getTop();
                        int delta = switchViewStartTop - switchViewNewTop;

                        switchView.setTranslationY(delta);

                        ObjectAnimator animator = ObjectAnimator.ofFloat(switchView,
                                                                         View.TRANSLATION_Y, 0);
                        animator.setDuration(MOVE_DURATION);
                        animator.start();
                    }

                    return true;
                }
            });
        }
    }

    private void addResizeFrame() {
        if (mLongState && mMobileItemId != INVALID_ID) {
            final View resizeView = getViewForID(mMobileItemId);
            if (resizeView != null) {
                mResizeBounds.left = resizeView.getLeft();
                mResizeBounds.top = resizeView.getTop() + mTopPadding;
                mResizeBounds.right = resizeView.getRight();
                mResizeBounds.bottom = resizeView.getBottom();
                mResizeSelect.setBounds(mResizeBounds);

                resizeView.getGlobalVisibleRect(mResizeRect);
                mResizeRect.top += (int) mTopPadding;
                mLauncher.getDragLayer().addResizeFrame(mResizeRect);
            }
        }
    }

    private boolean isMoveResizeFrame() {
        if (mMobileItemId != INVALID_ID) {
            final View resizeView = getViewForID(mMobileItemId);
            if (resizeView != null) {
                mResizeBounds.left = resizeView.getLeft();
                mResizeBounds.top = resizeView.getTop() + mTopPadding;
                mResizeBounds.right = resizeView.getRight();
                mResizeBounds.bottom = resizeView.getBottom();
                mResizeSelect.setBounds(mResizeBounds);

                resizeView.getGlobalVisibleRect(mResizeRect);
                mResizeRect.top += mTopPadding;
                mLauncher.getDragLayer().moveResizeFrame(mResizeRect);

                return true;
            }
        }

        return false;
    }

    private void touchEventsEnded() {
        if (mCellIsMobile || mIsWaitingForScrollFinish) {
            mCellIsMobile = false;
            mIsWaitingForScrollFinish = false;
            mIsMobileScrolling = false;
            mActivePointerId = INVALID_POINTER_ID;

            if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                mIsWaitingForScrollFinish = true;
                return;
            }

            if (mHoverCell == null) {
                return;
            }

            final View mobileView = getViewForID(mMobileItemId);
            if (mobileView != null) {
                mHoverCellCurrentBounds
                    .offsetTo(mHoverCellOriginalBounds.left, mobileView.getTop());
            } else {
                mHoverCellCurrentBounds
                    .offsetTo(mHoverCellOriginalBounds.left, getHeight());
            }

            mHoverViewAnimator = ObjectAnimator.ofObject(mHoverCell, "bounds",
                                                         sBoundEvaluator, mHoverCellCurrentBounds);
            mHoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            mHoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    addResizeFrame();
                    if (mSelectInfo != null) {
                        mSelectInfo.hostView.setVisibility(View.VISIBLE);
                    }
                    mAboveItemId = INVALID_ID;
                    mBelowItemId = INVALID_ID;
                    mHoverCell = null;
                    setEnabled(true);
                    invalidate();
                }
            });
            mHoverViewAnimator.start();
        } else {
            touchEventsCancelled();
        }
    }

    private void touchEventsCancelled() {
        addResizeFrame();
        mAboveItemId = INVALID_ID;
        mBelowItemId = INVALID_ID;
        if (mHoverCell != null) {
            if (mSelectInfo != null) {
                mSelectInfo.hostView.setVisibility(View.VISIBLE);
            }
            mHoverCell = null;
            invalidate();
        }
        mCellIsMobile = false;
        mIsMobileScrolling = false;
        mActivePointerId = INVALID_POINTER_ID;
    }

    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction),
                            interpolate(startValue.top, endValue.top, fraction),
                            interpolate(startValue.right, endValue.right, fraction),
                            interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int) (start + fraction * (end - start));
        }
    };

    private void handleMobileCellScroll() {
        mIsMobileScrolling = handleMobileCellScroll(mHoverCellCurrentBounds);
    }

    public boolean handleMobileCellScroll(Rect r) {
        int offset = computeVerticalScrollOffset();
        int height = getHeight();
        int extent = computeVerticalScrollExtent();
        int range = computeVerticalScrollRange();
        int hoverViewTop = r.top;
        int hoverHeight = r.height();

        if (hoverViewTop <= 0 && offset > 0) {
            smoothScrollBy(-mSmoothScrollAmountAtEdge, 0);
            return true;
        }

        if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
            smoothScrollBy(mSmoothScrollAmountAtEdge, 0);
            return true;
        }

        return false;
    }

    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {

        private int mPreviousFirstVisibleItem = -1;
        private int mPreviousVisibleItemCount = -1;
        private int mCurrentFirstVisibleItem;
        private int mCurrentVisibleItemCount;
        private int mCurrentScrollState;

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            mCurrentFirstVisibleItem = firstVisibleItem;
            mCurrentVisibleItemCount = visibleItemCount;

            mPreviousFirstVisibleItem = (mPreviousFirstVisibleItem == -1) ? mCurrentFirstVisibleItem
                                                                          : mPreviousFirstVisibleItem;
            mPreviousVisibleItemCount = (mPreviousVisibleItemCount == -1) ? mCurrentVisibleItemCount
                                                                          : mPreviousVisibleItemCount;

            checkAndHandleFirstVisibleCellChange();
            checkAndHandleLastVisibleCellChange();

            mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
            mPreviousVisibleItemCount = mCurrentVisibleItemCount;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mCurrentScrollState = scrollState;
            mScrollState = scrollState;
            isScrollCompleted();
        }

        private void isScrollCompleted() {
            if (mCurrentVisibleItemCount > 0 && mCurrentScrollState == SCROLL_STATE_IDLE) {
                if (mCellIsMobile && mIsMobileScrolling) {
                    handleMobileCellScroll();
                } else if (mIsWaitingForScrollFinish) {
                    touchEventsEnded();
                }
            }
        }

        public void checkAndHandleFirstVisibleCellChange() {
            if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
                if (mCellIsMobile && mMobileItemId != INVALID_ID) {
                    updateNeighborViewsForID(mMobileItemId);
                    handleCellSwitch();
                }
            }
        }

        public void checkAndHandleLastVisibleCellChange() {
            int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
            int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
            if (currentLastVisibleItem != previousLastVisibleItem) {
                if (mCellIsMobile && mMobileItemId != INVALID_ID) {
                    updateNeighborViewsForID(mMobileItemId);
                    handleCellSwitch();
                }
            }
        }
    };

    private void onTouchComplete() {
        if (mTouchCompleteListener != null) {
            mTouchCompleteListener.onTouchComplete();
        }
        mTouchCompleteListener = null;
    }

    public void setTouchCompleteListener(TouchCompleteListener listener) {
        mTouchCompleteListener = listener;
    }

    public interface SwappableList {

        public void swap(int pos1, int pos2);
    }

    public interface TouchCompleteListener {

        public void onTouchComplete();
    }

}
