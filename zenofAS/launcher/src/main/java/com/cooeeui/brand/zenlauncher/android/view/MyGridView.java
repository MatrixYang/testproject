package com.cooeeui.brand.zenlauncher.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * Created by cuiqian on 2015/11/19.
 * use to enable Parent View to exe onInterceptTouchEvent
 */
public class MyGridView extends GridView{

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}
