package com.cooeeui.nanobooster.common.util;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

/**
 * Created by hugo.Ye on 2016/4/13. 辅助功能开关工具类
 */
final public class AccessibilityServiceUtil {

    /**
     * 判断辅助功能开关是否打开
     *
     * @param context 上下文环境
     */
    public static boolean isAccessibleEnabled(Context context) {
        AccessibilityManager manager =
            (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = manager.getEnabledAccessibilityServiceList(
            AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo info : runningServices) {
            if (info.getId().equals(context.getPackageName()
                                    + "/.services.BoosterAccessibilityService")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 进入系统辅助功能设置界面
     *
     * @param context 上下文环境
     */
    public static void openAccessibilitySetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public static boolean isAccessibilityEnabled(Context context) {
        int accessibilityEnabled = 0;
        final String ACCESSIBILITY_SERVICE_NAME =
            "com.example.test/com.example.text.ccessibilityService";
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled =
                Settings.Secure.getInt(context.getContentResolver(),
                                       android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d("", "ACCESSIBILITY: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.d("",
                  "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter =
            new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.d("", "***ACCESSIBILIY IS ENABLED***: ");

            String settingValue = Settings.Secure.getString(context.getContentResolver(),
                                                            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Log.d("", "Setting: " + settingValue);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                    Log.d("", "Setting: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(ACCESSIBILITY_SERVICE_NAME)) {
                        Log.d("",
                              "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }

            Log.d("", "***END***");
        } else {
            Log.d("", "***ACCESSIBILIY IS DISABLED***");
        }
        return accessibilityFound;
    }

}
