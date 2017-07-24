package com.cooeeui.brand.zenlauncher.preferences;

import android.content.Context;

import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;

/**
 * 为了后面看代码方便，将具体的设置细化到各模块文件中。
 */
public class SharedPreferencesUtil {

    private static Preferences sharedPerf;

    /**
     * 在使用SharedPreferencesUtil前必须初始化
     */
    public static void init(Context context) {
        sharedPerf = new Preferences(context.getSharedPreferences(
            LauncherConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE));
    }

    /**
     * 获取Preferences实例
     *
     * @return sharedPerf
     */
    public static Preferences get() {
        return sharedPerf;
    }
}
