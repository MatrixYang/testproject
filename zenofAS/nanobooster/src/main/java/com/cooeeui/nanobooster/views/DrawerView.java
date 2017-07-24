package com.cooeeui.nanobooster.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.cooeeui.nanobooster.common.util.DensityUtils;

/**
 * Created by hugo.ye on 2016/3/8. 简单的仿抽屉布局
 */
public class DrawerView extends FrameLayout {

    private final String TAG = DrawerView.class.getSimpleName();
    private View mSecond_child;
    private int mSecond_child_width;
    private int titlebar_height;
    //private RelativeLayout mTitlebarIcon;

    public DrawerView(Context context) {
        super(context);
        init();
    }

    public DrawerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DrawerView(Context context, AttributeSet attrs, int defStyleAttr,
                      int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        titlebar_height = DensityUtils.dp2px(getContext(), 42);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //mTitlebarIcon = (RelativeLayout) findViewById(R.id.titlebar_icon);
        // mTitlebarIcon.setOnClickListener(this);
        mSecond_child = getChildAt(1);

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int widthh = mSecond_child.getMeasuredWidth();

        //mSecond_child.layout(l,t+titlebar_height,r,b);
        mSecond_child.layout(-widthh, t + titlebar_height, 0, b);

    }
}
