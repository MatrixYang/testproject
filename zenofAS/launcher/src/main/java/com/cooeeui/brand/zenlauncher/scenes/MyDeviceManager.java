package com.cooeeui.brand.zenlauncher.scenes;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2016/3/31.
 * 使用设备管理者,监听广播,设置管理者的特殊操作
 */
public class MyDeviceManager extends DeviceAdminReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
    }

}
