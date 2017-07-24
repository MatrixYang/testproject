package com.cooee.notificationservice;

import android.app.Application;
import android.content.Intent;

public class MyApplication extends Application {

    private Intent intent;

    public MyApplication() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        intent =
            new Intent("com.cooeeui.notificationservice.NoticeService")
                .setPackage("com.cooeeui.notificationservice");
        startService(intent);
        super.onCreate();
    }

}
