package com.cooeeui.wallpaper.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2016/4/13.
 */
public class CommonTools {

    private static final String TAG = "CommonTools";

    /**
     * 此函数是判断手机上是否有指定的APP
     *
     * @param context     上下文对象
     * @param packageName app的包名
     * @return 如果手机上已经存在指定的app则返回true 否则返回false
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断网络是否可用
     *
     * @param context 上下文对象
     * @return 可用返回true，反之返回false
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
        } else {
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * android4.0以前，display.getMetrics(dm);就能够获取正确的屏幕分辨率，4.0、4.1的就不行了
     */
    public static int getRealScreenPixelsHeight(Activity activity) {
        if (activity != null) {
            int height = activity.getResources().getDisplayMetrics().heightPixels;
            if (Build.VERSION.SDK_INT == 13) {
                try {
                    Display display = activity.getWindowManager().getDefaultDisplay();
                    Method mt = display.getClass().getMethod("getRealHeight");
                    height = (Integer) mt.invoke(display);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "getRealScreenPixelsHeight failed --- SDK_INT == 13 !", e);
                }
            } else if (Build.VERSION.SDK_INT > 13 && Build.VERSION.SDK_INT < 17) {
                try {
                    Display display = activity.getWindowManager().getDefaultDisplay();
                    Method mt = display.getClass().getMethod("getRawHeight");
                    height = (Integer) mt.invoke(display);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "getRealScreenPixelsHeight failed --- getRawHeight !", e);
                }
            } else if (Build.VERSION.SDK_INT >= 17) {
                DisplayMetrics dm = new DisplayMetrics();
                Display display = activity.getWindowManager().getDefaultDisplay();
                display.getRealMetrics(dm);
                height = dm.heightPixels;
            }
            return height;
        }
        return -1;
    }

    /**
     * 获取当前设备屏幕宽度（分辨率）
     */
    public static int getScreenPixelsWidth(Context context) {

        if (context != null) {
            int width = context.getResources().getDisplayMetrics().widthPixels;
            return width;
        }
        return -1;
    }
}
