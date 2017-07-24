package com.cooeeui.brand.zenlauncher.favorite.usagestats;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.cooeeui.brand.zenlauncher.preferences.LauncherPreference;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.ui.AlertDialogUtil;

import java.util.List;

/**
 * Created by cuiqian on 2016/1/18.
 */
public class UsageUtil {

    /**
     * 常用功能需要开启设置项“有权查看使用情况的应用”的开始版本
     */
//    public static int USAGE_SETTING_API = 23;
    public static int USAGE_DISPLAY_ALERT_TIMES = 5;

    public static boolean isUsageAllowed(Context context) {
        return getUsageMode(context) == AppOpsManager.MODE_ALLOWED;
    }

    /**
     * 系统有无“有权查看使用情况的应用”菜单
     * @param context
     * @return
     */
    public static boolean isNoOption(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        List<ResolveInfo>
            list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static int getUsageMode(Context context) {
        int mode = AppOpsManager.MODE_DEFAULT;
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo
                applicationInfo =
                packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager
                appOpsManager = (AppOpsManager) context.getSystemService(
                Context.APP_OPS_SERVICE);
            mode = appOpsManager
                .checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid,
                                applicationInfo.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.fillInStackTrace();
        }
        return mode;
    }

    public static void startUsageSettingActivity(Activity activity, int requestCode) {

        try {
            activity.startActivityForResult(
                new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity,
                           StringUtil.getString(activity, R.string.usage_setting_activity_error),
                           Toast.LENGTH_SHORT).show();
        }
    }

    public static void setDefalt() {
        LauncherPreference.setFavoritePageDisplayTimes(0);
    }

    public static void showUsageTwoButtonAlert(Activity activity) {
        int times = LauncherPreference.getFavoritePageDisplayTimes();

        //USAGE_DISPLAY_ALERT_TIMES + 1 just a max limit
        if (times < (USAGE_DISPLAY_ALERT_TIMES + 1)) {
            times++;
        }
        LauncherPreference.setFavoritePageDisplayTimes(times);

        if (times == USAGE_DISPLAY_ALERT_TIMES) {
            if (!UsageUtil.isUsageAllowed(activity)) {
                AlertDialogUtil alertDialogUtil = new AlertDialogUtil(activity);
                alertDialogUtil.showAlertDialog(true,false,
                                                AlertDialogUtil.AlertDialogType.TYPE_FAVORITE_PROMPT,
                                                R.layout.alter_dialog_usage_start);
            }
        }
    }

    public static void showUsageAlert(Activity activity) {
        AlertDialogUtil alertDialogUtil = new AlertDialogUtil(activity);
        alertDialogUtil.showAlertDialog(true,false, AlertDialogUtil.AlertDialogType.TYPE_FAVORITE_OK,
                                        R.layout.alter_dialog_usage_ok);
    }
}
