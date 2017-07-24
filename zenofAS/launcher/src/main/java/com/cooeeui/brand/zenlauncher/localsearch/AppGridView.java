package com.cooeeui.brand.zenlauncher.localsearch;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by cuiqian on 2016/2/22.
 */
public class AppGridView extends GridView{

    public AppGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppGridView(Context context) {
        super(context);
    }

    public AppGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(
            Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
