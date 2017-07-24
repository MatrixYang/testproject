package com.cooeeui.brand.zenlauncher.widgets.hotapp;

import android.content.Context;
import android.view.View;

/**
 * Created by Administrator on 2016/3/14.
 */
public class HotappWidgetProxy implements IWidgetProxy {

    private Context mContext;
    private boolean mInWidgetPage;

    public HotappWidgetProxy(Context context){
        mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public View getView(Integer id) {
        return new HotappWidgetView(mContext,id);
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }
}
