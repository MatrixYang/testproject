package com.cooeeui.brand.zenlauncher.widgets.weather;

import android.content.Context;
import android.view.View;

/**
 * Created by Administrator on 2016/3/14.
 */
public class WeatherWidgetProxy implements IWidgetProxy {

    private Context mContext;

    public WeatherWidgetProxy(Context context) {
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
        return new WeatherWidgetView(mContext,id);
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }
}
