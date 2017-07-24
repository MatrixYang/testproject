package com.cooeeui.brand.zenlauncher.favorite;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.brand.zenlauncher.LauncherModel;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FavoritesData {

    public static ArrayList<AppInfo> datas = new ArrayList<AppInfo>();

    public static ArrayList<AppInfo> mApps = new ArrayList<AppInfo>();

    public static String SCAN_PACKAGE_NAME = "com.cooeeui.nanoqrcodescan";

    public static boolean isUpdate = false;

    public static boolean isNewAdd = false;

    public static AppInfo getAppInfo(String name) {
        for (AppInfo app : mApps) {
            String pn = app.componentName.getPackageName();
            if (pn.equals(name)) {
                return app;
            }
        }
        return null;
    }

    public static void filterApps(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_HOME);

        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);

        if (apps == null || apps.isEmpty()) {
            return;
        }

        for (int i = 0; i < apps.size(); i++) { // filter home app
            ResolveInfo app = apps.get(i);
            if (app.activityInfo != null) {
                String pkgName = app.activityInfo.applicationInfo.packageName;
                AppInfo info = getAppInfo(pkgName);
                if (info != null) {
                    mApps.remove(info);
                }
            }
        }
        for (AppInfo appInfo : mApps) {// filter notice app
            if (appInfo.componentName.getPackageName() != null
                && Launcher.isNoticeApp(appInfo.componentName.getPackageName())) {
                mApps.remove(appInfo);
                break;
            }
        }
        for (AppInfo appInfo : mApps) {
            if (appInfo.componentName.getPackageName() != null
                && Launcher.isIconUIApp(appInfo.componentName.getPackageName())) {
                mApps.remove(appInfo);
                break;
            }
        }
        for (AppInfo appInfo : mApps) {// filter qrcodescan
            if (appInfo.componentName.getPackageName() != null
                && SCAN_PACKAGE_NAME.equals(appInfo.componentName.getPackageName())) {
                mApps.remove(appInfo);
                break;
            }
        }
    }

    private static AppInfo getDatasApp(String name) {
        for (AppInfo app : datas) {
            String pn = app.componentName.getPackageName();
            if (pn.equals(name)) {
                return app;
            }
        }

        return null;
    }

    private static boolean isNew() {
        int num = datas.size();
        if (num <= LauncherAppState.DEFAULT_FAVORITE_NUM) {
            return false;
        }

        for (int i = LauncherAppState.DEFAULT_FAVORITE_NUM; i < num; i++) {
            if (datas.get(i).launchTimes > datas
                .get(LauncherAppState.DEFAULT_FAVORITE_NUM - 1).launchTimes) {
                return true;
            }
        }

        return false;
    }

    public static void updateTimes(String name) {
        AppInfo app = getDatasApp(name);
        if (app == null) {
            app = getAppInfo(name);
            if (app != null) {
                app.launchTimes++;
                if (datas.size() < LauncherAppState.DEFAULT_FAVORITE_NUM) {
                    isNewAdd = true;
                }
                datas.add(app);
            }
        } else {
            app.launchTimes++;
            if (isNew()) {
                isNewAdd = true;
            }
            isUpdate = true;
        }
    }

    public static boolean add(String name) {
        AppInfo app = getAppInfo(name);
        if (app != null && app.launchTimes > 0) {
            add(app);
            return true;
        }
        return false;
    }

    public static void add(AppInfo app) {
        if (getDatasApp(app.componentName.getPackageName()) == null) {
            datas.add(app);
        }
    }

    public static void removeAll(ArrayList<String> names) {
        for (String name : names) {
            AppInfo app = getAppInfo(name);
            if (app != null) {
                datas.remove(app);
            }
        }
    }

    public static void remove(AppInfo app) {
        datas.remove(app);
    }

    public static void clear() {
        datas.clear();
        isUpdate = false;
        isNewAdd = false;
    }

    public static void dayDecrease() {
        AppInfo app = null;
        int num = datas.size();
        int max = LauncherAppState.DEFAULT_FAVORITE_NUM + 1;

        if (num < LauncherAppState.DEFAULT_FAVORITE_NUM) {
            max = num + 1;
        }

        for (int i = 0; i < num; i++) {
            app = datas.get(i);
            app.launchTimes -= 3;
            if (i < LauncherAppState.DEFAULT_FAVORITE_NUM) {
                if (app.launchTimes < max - i) {
                    app.launchTimes = max - i;
                }
            } else if (app.launchTimes < 1) {
                app.launchTimes = 1;
            }
        }
    }

    public static void saveFavoritesToDatabase(Context context) {
        ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
        for (AppInfo app : mApps) {
            if (app.launchTimes > 0) {
                apps.add(app);
            }
        }

        LauncherModel.saveFavoritesToDatabase(context, apps);
    }

    public static void sort() {
        Collections.sort(datas, new DatasComparator());
    }

    public static class DatasComparator implements Comparator<AppInfo> {

        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            if (lhs.launchTimes > rhs.launchTimes) {
                return -1;
            } else if (lhs.launchTimes < rhs.launchTimes) {
                return 1;
            }
            return 0;
        }
    }
}
