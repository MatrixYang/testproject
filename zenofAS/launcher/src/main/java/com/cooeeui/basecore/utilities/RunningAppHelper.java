package com.cooeeui.basecore.utilities;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;

import com.cooeeui.brand.zenlauncher.favorite.usagestats.UsageUtil;

import java.util.List;

public class RunningAppHelper {

    private static ActivityManager manager = null;
    private static UsageStatsManager mUsageStatsManager = null;

    public static String getTopAppPckageName(Context context) {
        if (manager == null) {
            manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        String mPackageName = null;
        if (UsageUtil.isNoOption(context)){
            mPackageName = getForegroundApp(context);
        }
        if (mPackageName == null){
            if (Build.VERSION.SDK_INT >= 21) {
                // 取出第一个正在运行的进程
                List<RunningAppProcessInfo> list = manager.getRunningAppProcesses();
                if (list != null && list.size() > 0) {
                    RunningAppProcessInfo runningAppProcessInfo = list.get(0);
                    // TODO 目前仅通过importance确定该进程是否正在前台运行，后期寻找被隐藏的flags字段的获取方法，进一步确定
                    if (runningAppProcessInfo.importance
                        == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        // 获取该进程包含的第一个包名
                        mPackageName = runningAppProcessInfo.pkgList[0];

                    }
                }

            } else {
                List<ActivityManager.RunningTaskInfo> list = manager.getRunningTasks(1);
                if (list != null && list.size() > 0) {
                    mPackageName = list.get(0).topActivity.getPackageName();
                }
            }
        }
        return mPackageName;
    }

    private static String mLastPackageName = null;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getForegroundApp(Context context) {
        if (!UsageUtil.isUsageAllowed(context)){
            return null;
        }

        if (mUsageStatsManager == null) {
            mUsageStatsManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
        }

        long ts = System.currentTimeMillis();
        List<UsageStats> queryUsageStats =
            mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 2000, ts);

        //进入某应用一段时间后，queryUsageStats.isEmpty()
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return mLastPackageName;
        }
        UsageStats recentStats = null;
        for (UsageStats usageStats : queryUsageStats) {
            if (recentStats == null || recentStats.getLastTimeUsed() <
                                       usageStats.getLastTimeUsed()) {
                recentStats = usageStats;
            }
        }
        mLastPackageName = recentStats.getPackageName();
        return recentStats.getPackageName();
    }
}
