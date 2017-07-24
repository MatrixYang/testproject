package com.cooee.notificationservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NoticeService extends Service {

    private GetCount count;

    public NoticeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        count = new GetCount(getApplication());
        Log.i("service11", "onBind");
        return count;
    }

    @Override
    public void onCreate() {
        Log.i("service11", "onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent
            intent =
            new Intent("com.cooeeui.notificationservice.NoticeService")
                .setPackage("com.cooeeui.notificationservice");
        startService(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}
