package com.cooeeui.brand.zenlauncher.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class SimpleGridView extends GridView {

    public SimpleGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
