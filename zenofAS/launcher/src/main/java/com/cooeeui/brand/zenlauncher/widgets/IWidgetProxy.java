package com.cooeeui.brand.zenlauncher.widgets;

import android.view.View;

/**
 * Created by Administrator on 2016/3/14.
 */
interface IWidgetProxy {

    void onCreate();

    void onDestroy();

    void onResume();

    void onPause();

    View getView(Integer id);
}