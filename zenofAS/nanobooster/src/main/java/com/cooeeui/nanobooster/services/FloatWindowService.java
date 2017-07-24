package com.cooeeui.nanobooster.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.cooeeui.nanobooster.broadcast.BoosterCompleteReceiver;
import com.cooeeui.nanobooster.views.FloatWindow;

public class FloatWindowService extends Service {

    private static final String TAG = FloatWindowService.class.getSimpleName();

    private BoosterCompleteReceiver mReceiver;


    public FloatWindowService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FloatWindow.getInstance().initial(getApplicationContext());
        mReceiver = new BoosterCompleteReceiver();
        IntentFilter filter =
            new IntentFilter(BoosterCompleteReceiver.INTENT_ACTION_BOOSTER_COMPLETE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getBooleanExtra("showDeepCleanWindow", false)) {
            FloatWindow.getInstance().showDeepCleanWindow();
        } else if (intent.getBooleanExtra("removeDeepCleanWindow", false)) {
            FloatWindow.getInstance().removeDeepCleanWindow();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }
}
