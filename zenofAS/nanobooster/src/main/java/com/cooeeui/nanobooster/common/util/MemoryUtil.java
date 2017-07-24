package com.cooeeui.nanobooster.common.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.provider.Settings;
import android.util.Xml;

import com.cooeeui.nanobooster.model.domain.AppInfo;
import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hugo.ye on 2016/1/13. 类说明：内存相关数据获取工具类
 */
public class MemoryUtil {


    /**
     * 获取系统可使用的总内存大小，单位为KB
     *
     * @return long 单位为KB
     **/
    public static long getTotalMemory(Context context) {

        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine
                                                            .indexOf("MemTotal:"));
            br.close();
            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll(
                "\\D+", ""));

            return totalMemorySize;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取已使用内存大小，单位为KB
     *
     * @return long 单位为KB
     */
    public static long getUsedMemory(Context context) {
        long usedMemory = -1;
        usedMemory = getTotalMemory(context) - getAvailableMemory(context);
        return usedMemory;
    }

    /**
     * 计算已使用内存的百分比
     *
     * @return String
     */
    public static String getUsedPercentValue(Context context) {

        long totalMemorySize = getTotalMemory(context);
        long availableSize = getAvailableMemory(context);
        int percent = (int) ((totalMemorySize - availableSize)
                             / (float) totalMemorySize * 100);
        return percent + "%";
    }


    /**
     * 获取剩余可用内存大小，单位为KB
     *
     * @return long 单位为KB
     */
    public static long getAvailableMemory(Context context) {

        ActivityManager activityManager = (ActivityManager) context
            .getSystemService(Context.ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mi);

        return mi.availMem / 1024;
    }

    /**
     * 清理内存,这种方法的清理工作很简易，很多应用会立马重启
     */
    public static void cleanMemory(Context context) {
        ActivityManager activityManger =
            (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= 21) {
            List<AndroidAppProcess> list = ProcessManager.getRunningAppProcesses();

            if (list != null) {

                int myPid = android.os.Process.myPid();

                for (AndroidAppProcess processInfo : list) {
                    if (myPid == processInfo.pid) {
                        continue;
                    }

                    if (!processInfo.foreground) {
                        activityManger.killBackgroundProcesses(processInfo.getPackageName());
                    }
                }
            }
        } else {
            List<RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();

            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    RunningAppProcessInfo appProcessInfo = list.get(i);

                    String[] pkgList = appProcessInfo.pkgList;

                    if (appProcessInfo.importance >= RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {

                        for (int j = 0; j < pkgList.length; j++) {

                            if (pkgList[j].equals(context.getPackageName())) {
                                continue;
                            }

                            activityManger.killBackgroundProcesses(pkgList[j]);
                        }
                    }
                }
            }
        }
    }

    /**
     * 深度清理,需要依赖于“辅助功能”，本质是模拟点击，强行停止正在运行的应用
     *
     * @param context     上下文环境
     * @param runningList 正在运行的应用列表
     */
    public static void deepCleanMemory(Context context, List<AppInfo> runningList) {
        for (AppInfo info : runningList) {
            String packName = info.getPackName();
            Intent killIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri packageURI = Uri.parse("package:" + packName);
            killIntent.setData(packageURI);
            killIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(killIntent);
        }
    }

    /**
     * 获取指定进程应用已使用的内存大小，5.0+使用开源的android-processes解决方案，5.0以下使用系统api
     *
     * @param processNameList 指定的应用进程名称列表
     * @return 返回各个进程名称所使用的内存大小，单位KB
     */
    public static HashMap<String, Long> getTotalPss(Context context,
                                                    ArrayList<String> processNameList) {

        HashMap<String, Long> resultMap = new HashMap<>();
        ActivityManager activityMgr =
            (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= 21) {
            List<AndroidAppProcess> list = ProcessManager.getRunningAppProcesses();

            if (list != null) {
                for (AndroidAppProcess processInfo : list) {

                    if (processNameList.contains(processInfo.name)) {
                        int pid = processInfo.pid;
                        Debug.MemoryInfo[] memoryInfos =
                            activityMgr.getProcessMemoryInfo(new int[]{pid});

                        Debug.MemoryInfo memoryInfo = memoryInfos[0];
                        int totalPss = memoryInfo.getTotalPss();

                        resultMap.put(processInfo.name, new Long(totalPss));
                    }
                }
            }
        } else {
            List<RunningAppProcessInfo> list = activityMgr.getRunningAppProcesses();
            if (list != null) {
                for (RunningAppProcessInfo processInfo : list) {

                    if (processNameList.contains(processInfo.processName)) {
                        int pid = processInfo.pid;
                        Debug.MemoryInfo[] memoryInfos =
                            activityMgr.getProcessMemoryInfo(new int[]{pid});

                        Debug.MemoryInfo memoryInfo = memoryInfos[0];
                        int totalPss = memoryInfo.getTotalPss();

                        resultMap.put(processInfo.processName, new Long(totalPss));
                    }
                }
            }
        }

        return resultMap;
    }

    /**
     * 获取指定进程应用已使用的内存大小，5.0+使用开源的android-processes解决方案，5.0以下使用系统api
     *
     * @param processName 指定的应用进程名
     * @return long 返回使用的内存大小，单位为KB
     */
    public static long getTotalPss(Context context, String processName) {

        ActivityManager activityMgr =
            (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= 21) {
            List<AndroidAppProcess> list = ProcessManager.getRunningAppProcesses();

            if (list != null) {
                for (AndroidAppProcess processInfo : list) {
                    if (processInfo.name.equals(processName)) {
                        int pid = processInfo.pid;
                        Debug.MemoryInfo[] memoryInfos =
                            activityMgr.getProcessMemoryInfo(new int[]{pid});

                        Debug.MemoryInfo memoryInfo = memoryInfos[0];
                        int totalPss = memoryInfo.getTotalPss();

                        return totalPss;
                    }
                }
            }
        } else {
            List<RunningAppProcessInfo> list = activityMgr.getRunningAppProcesses();
            if (list != null) {
                for (RunningAppProcessInfo processInfo : list) {

                    if (processInfo.processName.equals(processName)) {
                        int pid = processInfo.pid;
                        Debug.MemoryInfo[] memoryInfos =
                            activityMgr.getProcessMemoryInfo(new int[]{pid});

                        Debug.MemoryInfo memoryInfo = memoryInfos[0];
                        int totalPss = memoryInfo.getTotalPss();

                        return totalPss;
                    }
                }
            }
        }

        return -1;
    }

    // code of xiangxiang （start）

    public static List<AppInfo> getRunningAppInfos(Context context) {

        PackageManager packageManager = context.getPackageManager();
        ActivityManager activityManager =
            (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);

        List<AppInfo> appInfos = new ArrayList<AppInfo>();

        if (Build.VERSION.SDK_INT >= 21) {

            List<AndroidAppProcess> list = ProcessManager.getRunningAppProcesses();

            for (AndroidAppProcess appProcess : list) {

                AppInfo appInfo = new AppInfo();

                try {
                    String processName = appProcess.name;
                    int processId = appProcess.pid;

                    PackageInfo packageInfo = packageManager.getPackageInfo(processName, 0);

                    Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
                    String appName =
                        packageInfo.applicationInfo.loadLabel(packageManager).toString();

                    Debug.MemoryInfo[] memoryInfo =
                        activityManager.getProcessMemoryInfo(new int[]{processId});
                    int totalPrivateDirty = memoryInfo[0].getTotalPrivateDirty() * 1024;

                    int flags = packageInfo.applicationInfo.flags;
                    if ((ApplicationInfo.FLAG_SYSTEM & flags) != 0) {
                        appInfo.setUserApp(false); //系统应用
                        continue;
                    } else {
                        appInfo.setUserApp(true); //用户应用
                    }

                    appInfo.setPackName(processName);
                    appInfo.setAppName(appName);
                    appInfo.setIcon(icon);
                    appInfo.setMemorySize(totalPrivateDirty);

                    appInfos.add(appInfo);

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }

        } else {
            List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

            for (RunningAppProcessInfo runningAppProcessInfo : appProcesses) {

                AppInfo appInfo = new AppInfo();
                try {

                    String processName = runningAppProcessInfo.processName;
                    int processId = runningAppProcessInfo.pid;

                    PackageInfo packageInfo = packageManager.getPackageInfo(processName, 0);

                    Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
                    String appName =
                        packageInfo.applicationInfo.loadLabel(packageManager).toString();

                    Debug.MemoryInfo[] memoryInfo =
                        activityManager.getProcessMemoryInfo(new int[]{processId});
                    int totalPrivateDirty = memoryInfo[0].getTotalPrivateDirty() * 1024;

                    int flags = packageInfo.applicationInfo.flags;
                    if ((ApplicationInfo.FLAG_SYSTEM & flags) != 0) {
                        appInfo.setUserApp(false); //系统应用
                        continue;
                    } else {
                        appInfo.setUserApp(true); //用户应用

                    }
                    appInfo.setPackName(processName);
                    appInfo.setAppName(appName);
                    appInfo.setIcon(icon);
                    appInfo.setMemorySize(totalPrivateDirty);
                    appInfos.add(appInfo);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }

        }
        FilterMyApp(context, appInfos);
        return appInfos;
    }

    private static void FilterMyApp(Context context, List<AppInfo> appInfos) {
        List<String> MyAppInfoPacknames = readFromAssets(context);

        if (MyAppInfoPacknames != null && MyAppInfoPacknames.size() != 0) {

            Iterator<AppInfo> iterator = appInfos.iterator();

            while (iterator.hasNext()) {
                AppInfo appInfo = iterator.next();
                if (MyAppInfoPacknames.contains(appInfo.getPackName())) {
                    iterator.remove();
                }
            }

        }

    }

    public static List<String> readFromAssets(Context context) {
        List<String> packageNames = new ArrayList<String>();
        try {
            InputStream is = context.getAssets().open("config.xml");
            XmlPullParser xp = Xml.newPullParser();
            xp.setInput(is, "utf-8");
            int type = xp.getEventType();
            while (type != XmlPullParser.END_DOCUMENT) {
                switch (type) {
                    case XmlPullParser.START_TAG:
                        if ("PackageName".equals(xp.getName())) {
                            String tmp = xp.nextText();
                            packageNames.add(tmp);
                        }
                        break;
                }
                type = xp.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageNames;
    }

    //code of  xiangxiang  (end)

}
