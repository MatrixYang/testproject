package com.cooeeui.basecore.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Device utility functions
 *
 * @author jexa7410
 */
public class DeviceUtils {

    private static final String TAG = "DeviceUtils";

    public static final int SDK_INT = Build.VERSION.SDK_INT;
    private static final String DEVICE_MANUFACTURER = Build.MANUFACTURER.toLowerCase();
    private static final String DEVICE_PRODUCT = Build.PRODUCT.toLowerCase();
    private static final String DEVICE_MODEL = Build.MODEL.toLowerCase();
    private static final String DEVICE_DISPLAY = Build.DISPLAY.toLowerCase();
    /**
     * UUID
     */
    private static UUID uuid = null;

    /**
     * 获取当前设备状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        if (context != null) {
            int statusBarHeight = 0;
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("status_bar_height", "dimen",
                                               "android");
            if (resourceId > 0) {
                statusBarHeight = res.getDimensionPixelSize(resourceId);
            }

            return statusBarHeight;
        }
        return -1;
    }


    /**
     * 获取手机下方虚拟导航键的高度
     *
     * @param context context
     * @return 返回高度
     */
    public static int getVirtualNavigationBar(Context context) {
        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        if (!hasMenuKey && !hasBackKey) {
            // Do whatever you need to do, this device has a navigation bar
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }

    /**
     * 获取当前设备屏幕高度（分辨率）
     */
    public static int getScreenPixelsHeight(Context context) {

        if (context != null) {
            int height = context.getResources().getDisplayMetrics().heightPixels;
            return height;
        }
        return -1;
    }


    /**
     * android4.0以前，display.getMetrics(dm);就能够获取正确的屏幕分辨率，4.0、4.1的就不行了
     */
    public static int getRealScreenPixelsHeight(Activity activity) {
        if (activity != null) {
            int height = activity.getResources().getDisplayMetrics().heightPixels;
            if (SDK_INT == 13) {
                try {
                    Display display = activity.getWindowManager().getDefaultDisplay();
                    Method mt = display.getClass().getMethod("getRealHeight");
                    height = (Integer) mt.invoke(display);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "getRealScreenPixelsHeight failed --- SDK_INT == 13 !", e);
                }
            } else if (SDK_INT > 13 && SDK_INT < 17) {
                try {
                    Display display = activity.getWindowManager().getDefaultDisplay();
                    Method mt = display.getClass().getMethod("getRawHeight");
                    height = (Integer) mt.invoke(display);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "getRealScreenPixelsHeight failed --- getRawHeight !", e);
                }
            } else if (SDK_INT >= 17) {
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

    /**
     * Returns unique UUID of the device
     *
     * @param context Context
     * @return UUID
     */
    public static UUID getDeviceUUID(Context context) {
        if (context == null) {
            return null;
        }

        if (uuid == null) {
            TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
            String id = tm.getDeviceId();
            if (id != null) {
                uuid = UUID.nameUUIDFromBytes(id.getBytes());
            }
        }

        return uuid;
    }

    public static boolean isHuaWeiDevices() {
        if (DEVICE_MANUFACTURER.equalsIgnoreCase("huawei")) {
            return true;
        }
        return false;
    }

    public static boolean isXiaomiDevices() {
        if (DEVICE_MANUFACTURER.equalsIgnoreCase("xiaomi")) {
            return true;
        }
        return false;
    }

    public static boolean isDoovL1() {
        if (DEVICE_MANUFACTURER.equalsIgnoreCase("doov") && DEVICE_MODEL.contains("doov l1")) {
            return true;
        }
        return false;
    }

    //针对802w机型
    public static boolean isHTC() {
        if (DEVICE_MANUFACTURER.equalsIgnoreCase("HTC")) {
            return true;
        }
        return false;
    }


    /**
     * 判断是否魅族机型是否有其定制的smartbar
     */
    public static boolean hasMeiZuSmartBar() {
        boolean result = false;
        try {
            // 新型号可用反射调用Build.hasSmartBar()
            Method method = Class.forName("android.os.Build").getMethod("hasSmartBar");
            return ((Boolean) method.invoke(null)).booleanValue();
        } catch (Exception e) {
            Log.w(TAG, "hasMeiZuSmartBar--noSuchMethod(hasSmartBar)");
        }
        // 反射不到Build.hasSmartBar()，则用Build.DEVICE判断
        if (Build.DEVICE.equals("mx2")) {
            result = true;
        } else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
            result = false;
        }
        return result;
    }

    /**
     * 方法一:uc等在使用的方法(新旧版flyme均有效)， 此方法需要配合requestWindowFeature(Window.FEATURE_NO_TITLE
     * )使用,缺点是程序无法使用系统actionbar
     *
     * @param decorView window.getDecorView
     */
    public static void hideNavigationBar(View decorView) {
        try {
            @SuppressWarnings("rawtypes")
            Class[] arrayOfClass = new Class[1];
            arrayOfClass[0] = Integer.TYPE;
            Method localMethod = View.class.getMethod("setSystemUiVisibility", arrayOfClass);
            Field localField = View.class.getField("SYSTEM_UI_FLAG_HIDE_NAVIGATION");
            Object[] arrayOfObject = new Object[1];
            try {
                arrayOfObject[0] = localField.get(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            localMethod.invoke(decorView, arrayOfObject);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isSpecialDevicesForNavigationbar() {

        if (hasMeiZuSmartBar()) {
            return true;
        }

        if (isDoovL1()) {
            return true;
        }

        return false;
    }

    public static boolean isSpecialDevicesForDefaultLauncherGuide() {

        if (isHuaWeiDevices()) {
            return true;
        }

        if (isXiaomiDevices()) {
            return true;
        }

        return false;
    }
}
