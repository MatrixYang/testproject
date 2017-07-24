/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.cooeeui.brand.zenlauncher;

import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.ViewConfiguration;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.appIntentUtils.AppIntentUtil;
import com.cooeeui.brand.zenlauncher.apps.AppFilter;
import com.cooeeui.brand.zenlauncher.apps.IconCache;
import com.cooeeui.brand.zenlauncher.debug.MemoryTracker;
import com.cooeeui.brand.zenlauncher.favorite.usagestats.UsagePopup;
import com.cooeeui.brand.zenlauncher.tips.TipsPopup;
import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;
import com.cooeeui.zenlauncher.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

public class LauncherAppState {

    public interface OnUnlockTimeChangedListener {

        public void onUnlockChanged();
    }

    private static final String TAG = "LauncherAppState";
    private static final String SHARED_PREFERENCES_KEY = "com.cooee.zenlauncher.prefs";
    public static boolean isNeedRefresh = false;

    private LauncherModel mModel;
    private IconCache mIconCache;
    private AppFilter mAppFilter;
    private boolean mIsScreenLarge;
    private float mScreenDensity;
    private int mLongPressTimeout = 300;
    public TipsPopup tipsPopup;
    private UsagePopup mUsagePopup;

    private static WeakReference<LauncherProvider> sLauncherProvider;
    private static Context sContext;

    private static LauncherAppState INSTANCE;
    private static AppIntentUtil appIntentUtil = null;

    public static final int DEFAULT_FAVORITE_NUM = 16;
    public static OnUnlockTimeChangedListener mOnUnlockTimeChangedListener;

    private static String sNavBarOverride;

    static {
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
                sNavBarOverride = null;
            }
        }
    }

    public static LauncherAppState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LauncherAppState();
        }
        return INSTANCE;
    }

    public Context getContext() {
        return sContext;
    }

    public static void setApplicationContext(Context context) {
        if (sContext != null) {
            Log.w(Launcher.TAG, "setApplicationContext called twice! old=" + sContext + " new="
                                + context);
        }
        sContext = context.getApplicationContext();
    }

    private LauncherAppState() {
        if (sContext == null) {
            throw new IllegalStateException("LauncherAppState inited before app context set");
        }

        Log.v(Launcher.TAG, "LauncherAppState inited");

        if (DeviceUtils.SDK_INT > 15
            && sContext.getResources().getBoolean(R.bool.debug_memory_enabled)) {
            MemoryTracker.startTrackingMe(sContext, "L");
        }

        // set mIsScreenXLarge and mScreenDensity *before* creating icon cache
        mIsScreenLarge = isScreenLarge(sContext.getResources());
        mScreenDensity = sContext.getResources().getDisplayMetrics().density;

        // mWidgetPreviewCacheDb = new WidgetPreviewLoader.CacheDb(sContext);
        mIconCache = new IconCache(sContext);

        tipsPopup = new TipsPopup(sContext);
        mUsagePopup = new UsagePopup(sContext);
        mAppFilter = AppFilter.loadByName(sContext.getString(R.string.app_filter_class));
        mModel = new LauncherModel(this, mIconCache, mAppFilter);

        // Register intent receivers
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        sContext.registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(LauncherConstants.ACTION_APP_TIPS);
        filter.addAction(LauncherConstants.ACTION_USAGE_SETTING_TIP_SHOW);
        filter.addAction(LauncherConstants.ACTION_USAGE_SETTING_TIP_REMOVE);
        sContext.registerReceiver(mModel, filter);
        if (DeviceUtils.SDK_INT > 15) {
            filter = new IntentFilter();
            filter.addAction(SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED);
            sContext.registerReceiver(mModel, filter);
        }
        filter = new IntentFilter();
        filter.addAction(SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED);
        sContext.registerReceiver(mModel, filter);
    }

    /**
     * Call from Application.onTerminate(), which is not guaranteed to ever be called.
     */
    public void onTerminate() {
        if(mModel != null){
            sContext.unregisterReceiver(mModel);
        }
    }

    LauncherModel setLauncher(Launcher launcher) {
        if (mModel == null) {
            throw new IllegalStateException("setLauncher() called before init()");
        }
        mModel.initialize(launcher);
        return mModel;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    boolean shouldShowAppOrWidgetProvider(ComponentName componentName) {
        return mAppFilter == null || mAppFilter.shouldShowApp(componentName);
    }

    static void setLauncherProvider(LauncherProvider provider) {
        sLauncherProvider = new WeakReference<LauncherProvider>(provider);
    }

    static LauncherProvider getLauncherProvider() {
        if (sLauncherProvider == null) {
            return null;
        }
        return sLauncherProvider.get();
    }

    public static String getSharedPreferencesKey() {
        return SHARED_PREFERENCES_KEY;
    }

    public boolean isScreenLarge() {
        return mIsScreenLarge;
    }

    public void tipsUserPresent() {
        tipsPopup.userPresent();
        if (isNeedRefresh && mOnUnlockTimeChangedListener != null) {
            mOnUnlockTimeChangedListener.onUnlockChanged();
        }
    }

    public void tipsScreenOn() {
        tipsPopup.screenOn();
    }

    public void tipsScreenOff() {
        tipsPopup.screenOff();
    }

    public void tipsShowView() {
        tipsPopup.showViewTips();

    }

    public void tipsShowApp(String name, long time) {
        tipsPopup.updateTipsApp(name, time);
    }

    public void showUsageSettingTip() {
        mUsagePopup.showViewTips();
    }
    public void removeUsageSettingTip() {
        mUsagePopup.remove();
    }

    public void tipsDataChanged() {
        tipsPopup.dataChanged();
    }

    // Need a version that doesn't require an instance of LauncherAppState for
    // the wallpaper picker
    public static boolean isScreenLarge(Resources res) {
        return res.getBoolean(R.bool.is_large_tablet);
    }

    public static boolean isScreenLandscape(Context context) {
        return context.getResources().getConfiguration().orientation
               == Configuration.ORIENTATION_LANDSCAPE;
    }

    public float getScreenDensity() {
        return mScreenDensity;
    }

    public int getLongPressTimeout() {
        return mLongPressTimeout;
    }

    public static void setAppIntentUtil(AppIntentUtil appIntent) {
        appIntentUtil = appIntent;
    }

    public static AppIntentUtil getAppIntentUtil() {
        return appIntentUtil;
    }




    public static boolean HasNavigationBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else {
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    public static int getNavigationBarHeight(Context context) {
        int height = 0;
        if (HasNavigationBar(context)) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen",
                                               "android");
            if (resourceId > 0) {
                height = res.getDimensionPixelSize(resourceId);
            }
        }
        return height;
    }

    public static boolean isZenLauncherOnTheTop() {
        ActivityManager manager = (ActivityManager) sContext
            .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName;
        if (Build.VERSION.SDK_INT >= 21) {
            packageName = manager.getRunningAppProcesses().get(0).processName+manager.getRunningTasks(1).get(0).topActivity.getClassName();
        } else {
            packageName = manager.getRunningTasks(1).get(0).topActivity.getPackageName()+manager.getRunningTasks(1).get(0).topActivity.getClassName();
        }
        String s=sContext.getPackageName()+"com.cooeeui.brand.zenlauncher.Launcher";
        if ((s).equals(packageName)) {
            return true;
        }
        return false;
    }
}
