package com.cooeeui.brand.zenlauncher.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.cooeeui.brand.zenlauncher.android.adapter.PinnedHeaderAdapter;

public class PinnedHeaderListView extends ListView implements OnScrollListener {

    private static final int MAX_ALPHA = 255;
    private PinnedHeaderAdapter mAdapter;
    private View mHeaderView;
    private boolean mHeaderViewVisible;
    private int mHeaderViewWidth;
    private int mHeaderViewHeight;
    protected int mFirstVisibleItem;

    public static int mHeight;

    public PinnedHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnScrollListener(this);
    }

    public PinnedHeaderListView(Context context) {
        super(context);
        setOnScrollListener(this);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnScrollListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (view instanceof PinnedHeaderListView) {
            ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = (PinnedHeaderAdapter) adapter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();

        }

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderViewVisible) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }

    public void setPinnedHeaderView(View view) {
        mHeaderView = view;
        if (mHeaderView != null) {
            setFadingEdgeLength(0);
        }
        requestLayout();

        mHeight = getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.setDividerHeight(0);
        if (mHeaderView != null) {
            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
            configureHeaderView(getFirstVisiblePosition());
            mHeight = getMeasuredHeight();
        }

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    public void configureHeaderView(int position) {
        if (mHeaderView == null) {
            return;
        }
        int state = mAdapter.getPinnedHeaderState(position);
        switch (state) {
            case PinnedHeaderAdapter.PINNED_HEADER_GONE: {
                mHeaderViewVisible = false;
                break;
            }

            case PinnedHeaderAdapter.PINNED_HEADER_VISIBLE: {
                mAdapter.configurePinnedHeader(mHeaderView, position, MAX_ALPHA);
                if (mHeaderView.getTop() != 0) {
                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
                }
                mHeaderViewVisible = true;
                break;
            }

            case PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP: {
                View firstView = getChildAt(0);
                int bottom = firstView.getBottom();
                // int itemHeight = firstView.getHeight();
                int headerHeight = mHeaderView.getHeight();
                int y;
                int alpha;
                if (bottom < headerHeight) {
                    y = (bottom - headerHeight);
                    alpha = MAX_ALPHA * (headerHeight + y) / headerHeight;
                } else {
                    y = 0;
                    alpha = MAX_ALPHA;
                }
                mAdapter.configurePinnedHeader(mHeaderView, position, alpha);
                if (mHeaderView.getTop() != y) {
                    mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight
                                                               + y);
                }
                mHeaderViewVisible = true;
                break;
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {

    }

    public boolean ismHeaderViewVisible() {
        return mHeaderViewVisible;
    }

    public int getFirstItemPosition() {
        return mFirstVisibleItem;
    }
}
