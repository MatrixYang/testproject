package com.cooeeui.iconui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.util.List;

public class StartActivity extends Activity {

    private static final String ZEN_LAUNCHER_PACKAGE_NAME = "com.cooeeui.zenlauncher";
    private static final String
        ZEN_LAUNCHER_ACTIVITY_NAME =
        "com.cooeeui.brand.zenlauncher.Launcher";

    @Override
    protected void onResume() {
        super.onResume();
        isWorking(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void isWorking(Context context) {
        if (!isAPKInstalled(context, ZEN_LAUNCHER_PACKAGE_NAME)) {
            startDownloadAPKActivity(context);
            return;
        } else if (isAPKRunning(context, ZEN_LAUNCHER_PACKAGE_NAME)) {
            return;
        } else {
            try {
                startZenlauncherActivity(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 启动下载APK的界�?
     *
     * @param context 上下文对�?
     */
    private void startDownloadAPKActivity(Context context) {
        Intent intent = new Intent(this, DownAPKActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 启动Zen launcher
     *
     * @param context 上下文对�?
     */
    private void startZenlauncherActivity(Context context) {
        ComponentName componetName = new ComponentName(ZEN_LAUNCHER_PACKAGE_NAME,
                                                       ZEN_LAUNCHER_ACTIVITY_NAME);
        Intent intent = new Intent();
        intent.setComponent(componetName);
        context.startActivity(intent);
        finish();
    }

    /**
     * 此函数是判断手机上是否有指定的APP
     *
     * @param context     上下文对�?
     * @param packageName app的包�?
     * @return 如果手机上已经存在指定的app则返回true 否则返回false
     */
    public static boolean isAPKInstalled(
        Context context,
        String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 此函数是判断手机的某个service是否在运�?
     *
     * @param context     上下文对�?
     * @param packageName app的包�?
     * @return 此函数是判断手机的某个service是否在运行，如果在运行则返回true 否则返回false
     */
    public static boolean isAPKRunning(
        Context context,
        String packageName) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context
            .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
            .getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(packageName) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

}
