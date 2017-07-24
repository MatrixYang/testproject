package com.cooeeui.brand.zenlauncher;

import android.app.Application;

import com.cooeeui.brand.zenlauncher.preferences.SharedPreferencesUtil;
import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.MobVistaSDKFactory;

import java.util.Map;

public class LauncherApplication extends Application {

    /*会被调用三次 2015-12-29*/
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Class.forName("android.os.AsyncTask");
        } catch (Throwable ignore) {
            // ignored
        }

        SharedPreferencesUtil.init(this);
        LauncherAppState.setApplicationContext(this);
        LauncherAppState.getInstance();

        initMobvista();
    }

    public void initMobvista() {
        MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
        Map<String, String>
            map =
            sdk.getMVConfigurationMap("22466", "686dfddcac68d078f4de704b947cff0c");
        sdk.init(map, this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LauncherAppState.getInstance().onTerminate();
    }
}
