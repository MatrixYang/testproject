/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cooeeui.brand.zenlauncher.apps;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View.OnClickListener;

import com.cooeeui.brand.zenlauncher.LauncherSettings;
import com.cooeeui.brand.zenlauncher.LauncherSettings.Applications;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents an app in AllAppsView.
 */
public class AppInfo extends ItemInfo {

    private static final String TAG = "ZenLauncher.AppInfo";

    /**
     * The originalCategory of the favorite category.
     */
    public int originalCategory;
    /**
     * The category of the application.
     */
    public int category;
    /**
     * The times of the application launch.
     */
    public long launchTimes;

    /**
     * Whether the application is hide.
     */
    public boolean hide;

    public ComponentName componentName;

    private OnClickListener mOnClickListenerImpl;

    /**
     * A bitmap version of the application icon.
     */
    public Bitmap iconBitmap;

    /**
     * The time at which the application was first installed.
     */
    public long firstInstallTime;

    public static final int DOWNLOADED_FLAG = 1;
    public static final int UPDATED_SYSTEM_APP_FLAG = 2;
    /**
     * The flag of this application.
     */
    public int flags = 0;
    // 标识 当前的APP的显示和隐藏的状态变化
    public boolean isChanged = false;
    // 记录下点击时的状态
    public boolean hideTemp;

    public AppInfo() {
        super();
        originalCategory = -1;
        category = -1;
        itemType = LauncherSettings.ITEM_TYPE_APPLICATION;
        launchTimes = 0;
        hide = false;
        hideTemp = hide;
    }

    public AppInfo(AppInfo info) {
        super(info);
        originalCategory = info.originalCategory;
        category = info.category;
        launchTimes = info.launchTimes;
        hide = info.hide;
        hideTemp = info.hideTemp;
        componentName = new ComponentName(info.componentName.getPackageName(),
                                          info.componentName.getClassName());
        // TODO: How do we handle the icon bitmap?
        iconBitmap = info.iconBitmap;
        firstInstallTime = info.firstInstallTime;
        flags = info.flags;
        itemType = LauncherSettings.ITEM_TYPE_APPLICATION;
    }

    /**
     * Must not hold the Context.
     */
    public AppInfo(PackageManager pm, ResolveInfo info, IconCache iconCache,
                   HashMap<Object, CharSequence> labelCache) {
        final String packageName = info.activityInfo.applicationInfo.packageName;

        this.componentName = new ComponentName(packageName, info.activityInfo.name);
        this.setActivity(componentName,
                         Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            flags = initFlags(pi);
            firstInstallTime = initFirstInstallTime(pi);
        } catch (NameNotFoundException e) {
            Log.d(TAG, "PackageManager.getApplicationInfo failed for " + packageName);
        }

        iconCache.getTitleAndIcon(this, info, labelCache);
        itemType = LauncherSettings.ITEM_TYPE_APPLICATION;
    }

    /**
     * Return the flags by package info.
     */
    public static int initFlags(PackageInfo pi) {
        int appFlags = pi.applicationInfo.flags;
        int flags = 0;
        if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
            flags |= DOWNLOADED_FLAG;

            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                flags |= UPDATED_SYSTEM_APP_FLAG;
            }
        }
        return flags;
    }

    /**
     * Return the first install time of a package info.
     */
    public static long initFirstInstallTime(PackageInfo pi) {
        return pi.firstInstallTime;
    }

    /**
     * Creates the application intent based on a component name and various launch flags. Sets
     * {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     *
     * @param className   the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.ITEM_TYPE_APPLICATION;
    }

    @Override
    public void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);
        if (hide) {
            values.put(Applications.HIDE, 1);
        } else {
            values.put(Applications.HIDE, 0);
        }
        values.put(Applications.LAUNCH_TIMES, launchTimes);
    }

    @Override
    public String toString() {
        return "ApplicationInfo(title=" + this.title.toString() + " id=" + this.id
               + " type=" + this.itemType + " originalCategory=" + this.originalCategory
               + " category=" + this.category + " componentName="
               + this.componentName + ")";
    }

    public static void dumpApplicationInfoList(String tag, String label, ArrayList<AppInfo> list) {
        Log.d(tag, label + " size=" + list.size());
        for (AppInfo info : list) {
            Log.d(tag, "   title=\"" + info.title + "\" iconBitmap="
                       + info.iconBitmap + " firstInstallTime="
                       + info.firstInstallTime);
        }
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }

    // 增加这两个接口是为了适配抽屉中自定义AppIcon的点击事件
    public void setOnClickListener(OnClickListener clickListener) {
        mOnClickListenerImpl = clickListener;
    }

    public OnClickListener getOnClickListener() {
        return mOnClickListenerImpl;
    }
}
