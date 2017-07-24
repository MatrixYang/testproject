/**
 *
 */

package com.cooeeui.brand.zenlauncher.scenes;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.brand.zenlauncher.android.view.QsListViewLetters;
import com.cooeeui.zenlauncher.R;

/**
 * @author user
 */
public class AllappsListview extends ListView implements OnScrollListener {

    private QsListViewLetters mLettersSection;
    private QsListViewAdapter mAdapter;
    private LinearLayout mHeaderView;
    private int mHeaderViewWidth;
    private int mHeaderViewHeight;

    /**
     * @param context
     */
    public AllappsListview(Context context) {
        super(context);

        if (!DeviceUtils.isSpecialDevicesForNavigationbar() && Build.VERSION.SDK_INT >= 19
            && LauncherAppState.HasNavigationBar(context)) {
            int navigationHeight = 0;
            navigationHeight = getResources().getDimensionPixelSize(R.dimen.navigation_height);
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom()
                                                                             + navigationHeight);
        }
        setOnScrollListener(this);
    }

    /**
     * @param context
     * @param attrs
     */
    public AllappsListview(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!DeviceUtils.isSpecialDevicesForNavigationbar() && Build.VERSION.SDK_INT >= 19
            && LauncherAppState.HasNavigationBar(context)) {
            int navigationHeight = 0;
            navigationHeight = getResources().getDimensionPixelSize(R.dimen.navigation_height);
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom()
                                                                             + navigationHeight);
        }
        setOnScrollListener(this);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public AllappsListview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!DeviceUtils.isSpecialDevicesForNavigationbar() && Build.VERSION.SDK_INT >= 19
            && LauncherAppState.HasNavigationBar(context)) {
            int navigationHeight = 0;
            navigationHeight = getResources().getDimensionPixelSize(R.dimen.navigation_height);
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom()
                                                                             + navigationHeight);
        }
        setOnScrollListener(this);
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();

        mHeaderView = (LinearLayout) LayoutInflater.from(getContext()).inflate(
            R.layout.all_apps_section_header, this, false);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        // TODO Auto-generated method stub
        super.setAdapter(adapter);
        mAdapter = (QsListViewAdapter) adapter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.dispatchDraw(canvas);
        if (mHeaderView != null) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }

    private void configureHeaderView(int position) {
        LinearLayout firstView = (LinearLayout) getChildAt(0);
        if (firstView != null) {
            int bottom = firstView.getBottom();
            int y = 0;
            if (position > 0) {
                int headerHeight = mHeaderViewHeight + getDividerHeight();
                if (bottom < headerHeight) {
                    y = (bottom - headerHeight);
                    if (mHeaderView.getTop() != y) {
                        mHeaderView
                            .layout(getPaddingLeft(), getDividerHeight() + y,
                                    mHeaderViewWidth, headerHeight + y);
                    }
                } else {
                    y = getDividerHeight();
                    if (mHeaderView.getTop() != y) {
                        mHeaderView
                            .layout(getPaddingLeft(), y, mHeaderViewWidth,
                                    headerHeight + y);
                    }
                }
            } else {
                int headerHeight = mHeaderViewHeight + getPaddingTop();
                if (bottom < headerHeight) {
                    y = (bottom - headerHeight);
                    if (mHeaderView.getTop() != y) {
                        mHeaderView
                            .layout(getPaddingLeft(), getPaddingTop() + y,
                                    mHeaderViewWidth, headerHeight + y);
                    }
                } else {
                    y = getPaddingTop();
                    if (mHeaderView.getTop() != y) {
                        mHeaderView
                            .layout(getPaddingLeft(), y, mHeaderViewWidth,
                                    headerHeight + y);
                    }
                }

            }

            if (mAdapter != null) {
                mAdapter.configureHeaderView(mHeaderView, position);
            }
        }

    }

    private void configureVisibleItemAlpha(int firstVisibleItem, int visibleItemCount) {
        LinearLayout child;
        TextView textView;
        child = (LinearLayout) getChildAt(0);
        if (child != null) {
            textView = (TextView) child.getChildAt(0);
            textView.setAlpha(0);
        }

        for (int i = 1; i < visibleItemCount; i++) {
            child = (LinearLayout) getChildAt(i);
            if (child != null) {
                textView = (TextView) child.getChildAt(0);
                textView.setAlpha(1);
            }
        }
    }

    public void setup(QsListViewLetters letters) {
        this.mLettersSection = letters;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        // TODO Auto-generated method stub

        if (mLettersSection != null) {
            mLettersSection
                .setVisiblityIndex(firstVisibleItem, getLastVisiblePosition());
            mLettersSection.invalidate();
        }
        if (mHeaderView != null) {
            configureHeaderView(firstVisibleItem);
        }
        configureVisibleItemAlpha(firstVisibleItem, visibleItemCount);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub

    }

}
