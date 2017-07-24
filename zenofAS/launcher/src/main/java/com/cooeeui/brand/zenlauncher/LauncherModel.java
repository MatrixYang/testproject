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

package com.cooeeui.brand.zenlauncher;

import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.util.Log;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.alarmUpdate.service.DataService;
import com.cooeeui.brand.zenlauncher.apps.AllAppsList;
import com.cooeeui.brand.zenlauncher.apps.AppFilter;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.apps.AppUsedTimeInfo;
import com.cooeeui.brand.zenlauncher.apps.CommonTimeInfo;
import com.cooeeui.brand.zenlauncher.apps.IconCache;
import com.cooeeui.brand.zenlauncher.apps.ItemInfo;
import com.cooeeui.brand.zenlauncher.apps.ShortcutInfo;
import com.cooeeui.brand.zenlauncher.debug.Logger;
import com.cooeeui.brand.zenlauncher.favorite.FavoritesData;
import com.cooeeui.brand.zenlauncher.favorite.MonitorService;
import com.cooeeui.brand.zenlauncher.favorite.usagestats.UsageUtil;
import com.cooeeui.brand.zenlauncher.scenes.utils.BitmapUtils;
import com.cooeeui.brand.zenlauncher.scenes.utils.IconNameOrId;
import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;
import com.cooeeui.brand.zenlauncher.wallpaper.util.PreferencesUtils;
import com.cooeeui.brand.zenlauncher.widgets.LauncherAppWidgetInfo;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Maintains in-memory state of the Launcher. It is expected that there should be only one
 * LauncherModel object held in a static. Also provide APIs for updating the database state for the
 * Launcher.
 */
public class LauncherModel extends BroadcastReceiver {

    static final boolean DEBUG_LOADERS = false;
    static final String TAG = "Launcher.Model";

    private static final int ITEMS_CHUNK = 6; // batch size for the workspace
    // icons

    private final LauncherAppState mApp;
    private final Object mLock = new Object();
    private DeferredHandler mHandler = new DeferredHandler();
    private LoaderTask mLoaderTask;
    private boolean mIsLoaderTaskRunning;
    private volatile boolean mFlushingWorkerThread;

    // Specific runnable types that are run on the main thread deferred handler,
    // this allows us to clear all queued binding runnables when the Launcher
    // activity is destroyed.
    private static final int MAIN_THREAD_NORMAL_RUNNABLE = 0;
    private static final int MAIN_THREAD_BINDING_RUNNABLE = 1;

    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");

    static {
        sWorkerThread.start();
    }

    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    // We start off with everything not loaded. After that, we assume that
    // our monitoring of the package manager provides all updates and we never
    // need to do a requery. These are only ever touched from the loader thread.
    private boolean mWorkspaceLoaded;
    private boolean mAllAppsLoaded;

    private WeakReference<Callbacks> mCallbacks;

    // < only access in worker thread >
    AllAppsList mBgAllAppsList;

    // The lock that must be acquired before referencing any static bg data
    // structures. Unlike other locks, this one can generally be held long-term
    // because we never expect any of these static data structures to be
    // referenced outside of the worker thread except on the first load after
    // configuration change.
    static final Object sBgLock = new Object();

    // sBgWorkspaceItems is passed to bindItems, which expects a list of all
    // shortcuts created by LauncherModel that are directly on the home screen.
    static final ArrayList<ShortcutInfo> sBgWorkspaceItems = new ArrayList<ShortcutInfo>();

    static final ArrayList<LauncherAppWidgetInfo>
        sBgWidgetItems =
        new ArrayList<LauncherAppWidgetInfo>();

    private static int mMaxShortcutPos;
    private static int mMaxWidgetPos;

    private IconCache mIconCache;
//    private Bitmap mDefaultIcon;

    protected int mPreviousConfigMcc;

    private int mYear;
    private int mMonth;
    private int mDay;
    public static boolean isScreenOff = false;

    public interface Callbacks {

        public boolean setLoadOnResume();

        public void startBinding();

        public void bindItems(ArrayList<ShortcutInfo> shortcuts, int start, int end,
                              boolean forceAnimateIcons);

        public void finishBindingItems();

        public void bindAllApplications(ArrayList<AppInfo> apps);

        public void bindAppsAdded(ArrayList<AppInfo> addedApps);

        public void bindAppsUpdated(ArrayList<AppInfo> apps);

        public void bindComponentsRemoved(ArrayList<String> packageNames,
                                          ArrayList<AppInfo> appInfos,
                                          boolean matchPackageNamesOnly);

        public void bindSearchablesChanged();

        public void bindAppWidget(LauncherAppWidgetInfo info);

        public void finishBindWidget();

        public void bindHidesApp(ArrayList<AppInfo> hidens);

        public void onRemoveApp(ArrayList<AppInfo> removes);
    }

    LauncherModel(LauncherAppState app, IconCache iconCache, AppFilter appFilter) {
        final Context context = app.getContext();
        mApp = app;
        mBgAllAppsList = new AllAppsList(iconCache, appFilter);
        mIconCache = iconCache;

//        mDefaultIcon = Utilities.createIconBitmap(
//            mIconCache.getFullResDefaultActivityIcon(), context);

        final Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        mPreviousConfigMcc = config.mcc;

        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Runs the specified runnable immediately if called from the main thread, otherwise it is
     * posted on the main thread handler.
     */
    private void runOnMainThread(Runnable r) {
        runOnMainThread(r, 0);
    }

    private void runOnMainThread(Runnable r, int type) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            // If we are on the worker thread, post onto the main handler
            mHandler.post(r);
        } else {
            r.run();
        }
    }

    /**
     * Runs the specified runnable immediately if called from the worker thread, otherwise it is
     * posted on the worker thread handler.
     */
    private static void runOnWorkerThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            // If we are not on the worker thread, then post to the worker
            // handler
            sWorker.post(r);
        }
    }

//    public void addAndBindAddedApps(final Context context, final ArrayList<AppInfo> allAddedApps) {
//        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
//        addAndBindAddedApps(context, cb, allAddedApps);
//    }

    public void addAndBindAddedApps(final Context context, final Callbacks callbacks,
                                    final ArrayList<AppInfo> allAddedApps) {
        if (allAddedApps.isEmpty()) {
            return;
        }

        FavoritesData.mApps.addAll(allAddedApps);
        FavoritesData.filterApps(context);
        loadFavoritesFromDb(context);

        // Process the newly added applications and add them to the database
        // first
        runOnMainThread(new Runnable() {
            public void run() {
                Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                if (callbacks == cb && cb != null) {
                    callbacks.bindAppsAdded(allAddedApps);
                }
            }
        });
    }

//    public Bitmap getFallbackIcon() {
//        return Bitmap.createBitmap(mDefaultIcon);
//    }

    public static int genNewShortcutPosition() {
        mMaxShortcutPos++;
        return mMaxShortcutPos;
    }

    public static int genNewWidgetPosition() {
        mMaxWidgetPos++;
        return mMaxWidgetPos;
    }

    public void unbindItemInfosAndClearQueuedBindRunnables() {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            throw new RuntimeException("Expected unbindLauncherItemInfos() to be called from the " +
                                       "main thread");
        }

        // Remove any queued bind runnables
        mHandler.cancelAllRunnablesOfType(MAIN_THREAD_BINDING_RUNNABLE);
        // Unbind all the workspace items
        unbindWorkspaceItemsOnMainThread();
    }

    /**
     * Unbinds all the sBgWorkspaceItems on the main thread
     */
    void unbindWorkspaceItemsOnMainThread() {
        // Ensure that we don't use the same workspace items data structure on
        // the main thread by making a copy of workspace items first.
        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();

        synchronized (sBgLock) {
            tmpWorkspaceItems.addAll(sBgWorkspaceItems);
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (ItemInfo item : tmpWorkspaceItems) {
                    item.unbind();
                }
            }
        };
        runOnMainThread(r);
    }

//    public void flushWorkerThread() {
//        mFlushingWorkerThread = true;
//        Runnable waiter = new Runnable() {
//            public void run() {
//                synchronized (this) {
//                    notifyAll();
//                    mFlushingWorkerThread = false;
//                }
//            }
//        };
//
//        synchronized (waiter) {
//            runOnWorkerThread(waiter);
//            if (mLoaderTask != null) {
//                synchronized (mLoaderTask) {
//                    mLoaderTask.notify();
//                }
//            }
//            boolean success = false;
//            while (!success) {
//                try {
//                    waiter.wait();
//                    success = true;
//                } catch (InterruptedException e) {
//                }
//            }
//        }
//    }

    /**
     * Update an item to the database in a specified container.
     */
    public static void updateItemInDatabase(Context context, final ItemInfo item) {
        if (item == null) {
            return;
        }

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        Uri uriToUpdate = null;

        switch (item.itemType) {
            case LauncherSettings.ITEM_TYPE_SHORTCUT:
                uriToUpdate = LauncherSettings.Shortcuts.getContentUri(item.id, false);
                ((ShortcutInfo) item).onAddToDatabase(values);
                break;

            case LauncherSettings.ITEM_TYPE_APPWIDGET:
                uriToUpdate = LauncherSettings.Widgets.getContentUri(item.id, false);
                ((LauncherAppWidgetInfo) item).onAddToDatabase(values);
                break;

            case LauncherSettings.ITEM_TYPE_APPLICATION:
                uriToUpdate = LauncherSettings.Applications.getContentUri(item.id, false);
                ((AppInfo) item).onAddToDatabase(values);
                break;

            case LauncherSettings.ITEM_TYPE_APPS_TIME:
                uriToUpdate = LauncherSettings.AppsUsedTime.getContentUri(item.id, false);
                ((AppUsedTimeInfo) item).onAddToDatabase(values);
                break;

            case LauncherSettings.ITEM_TYPE_COMMON_TIME:
                uriToUpdate = LauncherSettings.Common_Time.getContentUri(item.id, false);
                ((CommonTimeInfo) item).onAddToDatabase(values);
                break;
        }

        if (uriToUpdate != null) {
            final Uri uri = uriToUpdate;
            Runnable r = new Runnable() {
                public void run() {
                    cr.update(uri, values, null, null);
                }
            };
            runOnWorkerThread(r);
        }
    }

    /**
     * Add the specified item to the database .
     */
    public static void addItemToDatabase(Context context, final ItemInfo item) {
        if (item == null || LauncherAppState.getLauncherProvider() == null) {
            return;
        }

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        Uri uriToAdd = null;

        switch (item.itemType) {
            case LauncherSettings.ITEM_TYPE_SHORTCUT:
                ShortcutInfo shortcut = (ShortcutInfo) item;
                shortcut.onAddToDatabase(values);
                shortcut.id = LauncherAppState.getLauncherProvider().generateNewShortcutId();
                shortcut.position = genNewShortcutPosition();
                values.put(LauncherSettings.Shortcuts._ID, shortcut.id);
                values.put(LauncherSettings.Shortcuts.POSITION, shortcut.position);
                uriToAdd = LauncherSettings.Shortcuts.CONTENT_URI;
                sBgWorkspaceItems.add(shortcut);
                break;

            case LauncherSettings.ITEM_TYPE_APPWIDGET:
                LauncherAppWidgetInfo widget = (LauncherAppWidgetInfo) item;
                widget.onAddToDatabase(values);
                widget.id = LauncherAppState.getLauncherProvider().generateNewWidgetId();
                widget.position = genNewWidgetPosition();
                values.put(LauncherSettings.Widgets._ID, widget.id);
                values.put(LauncherSettings.Widgets.POSITION, widget.position);
                uriToAdd = LauncherSettings.Widgets.CONTENT_URI;
                sBgWidgetItems.add(widget);
                break;

            case LauncherSettings.ITEM_TYPE_APPLICATION:
                final AppInfo app = (AppInfo) item;
                app.onAddToDatabase(values);
                app.id = LauncherAppState.getLauncherProvider().generateNewAppId();
                values.put(LauncherSettings.Applications._ID, app.id);
                uriToAdd = LauncherSettings.Applications.CONTENT_URI;
                break;

            case LauncherSettings.ITEM_TYPE_COMMON_TIME:
                final CommonTimeInfo info = (CommonTimeInfo) item;
                info.onAddToDatabase(values);
                info.id = LauncherAppState.getLauncherProvider().generateNewCommonTimeId();
                values.put(LauncherSettings.Common_Time._ID, info.id);
                uriToAdd = LauncherSettings.Common_Time.CONTENT_URI;
                break;

            case LauncherSettings.ITEM_TYPE_APPS_TIME:
                final AppUsedTimeInfo usedtime = (AppUsedTimeInfo) item;
                usedtime.onAddToDatabase(values);
                usedtime.id = LauncherAppState.getLauncherProvider().generateNewAppUsedTimeId();
                values.put(LauncherSettings.AppsUsedTime._ID, usedtime.id);
                uriToAdd = LauncherSettings.AppsUsedTime.CONTENT_URI;
                break;
        }

        if (uriToAdd != null) {
            final Uri uri = uriToAdd;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    cr.insert(uri, values);
                }
            };
            runOnWorkerThread(r);

        }
    }

    public static void saveFavoritesToDatabase(Context context, final ArrayList<AppInfo> items) {
        for (int i = 0; i < items.size(); i++) {
            AppInfo appInfo = items.get(i);
            if (appInfo.id == ItemInfo.NO_ID) {
                addItemToDatabase(context, appInfo);
            } else {
                updateItemInDatabase(context, appInfo);
            }
        }
    }

//    public static void saveGneralInfoToDatabase(Context context, final ItemInfo info) {
//        if (info.id == ItemInfo.NO_ID) {
//            addItemToDatabase(context, info);
//        } else {
//            updateItemInDatabase(context, info);
//        }
//    }

    public static final String LAUNCHER_ZEN = "com.cooeeui.zenlauncher";

    public static boolean isZenApp(String name) {
        if (LAUNCHER_ZEN.equals(name)) {
            return true;
        }
        return false;
    }

    /**
     * Removes the specified item from the database
     */
    public static void deleteItemFromDatabase(Context context, final ItemInfo item) {
        if (item == null) {
            return;
        }

        final ContentResolver cr = context.getContentResolver();
        Uri uriToDelete = null;

        switch (item.itemType) {
            case LauncherSettings.ITEM_TYPE_SHORTCUT:
                uriToDelete = LauncherSettings.Shortcuts.getContentUri(item.id, false);
                sBgWorkspaceItems.remove(item);
                break;

            case LauncherSettings.ITEM_TYPE_APPWIDGET:
                uriToDelete = LauncherSettings.Widgets.getContentUri(item.id, false);
                sBgWidgetItems.remove(item);
                break;

            case LauncherSettings.ITEM_TYPE_APPLICATION:
                uriToDelete = LauncherSettings.Applications.getContentUri(item.id, false);
                break;
        }

        if (uriToDelete != null) {
            final Uri uri = uriToDelete;
            Runnable r = new Runnable() {
                public void run() {
                    cr.delete(uri, null, null);
                }
            };
            runOnWorkerThread(r);
        }
    }

    private static ArrayList<AppInfo> loadHideAppsFromDB(Context context, ArrayList<AppInfo> apps) {
        ArrayList<AppInfo> hidens = new ArrayList<AppInfo>();
        final ContentResolver contentResolver = context.getContentResolver();
        synchronized (sBgLock) {
            final Uri contentUri = LauncherSettings.Applications.CONTENT_URI;
            String where = " hide = 1 ";
            final Cursor c = contentResolver.query(contentUri, null, where, null, null);
            try {
                final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Applications._ID);
                final int intentIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.INTENT);
                final int launchTimesIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.Applications.LAUNCH_TIMES);
                final int hideIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.Applications.HIDE);
                AppInfo appInfo;
                Intent intent;
                String intentDescription;
                long id;
                while (c.moveToNext()) {
                    try {
                        id = c.getLong(idIndex);
                        intentDescription = c.getString(intentIndex);
                        intent = Intent.parseUri(intentDescription, 0);
                        appInfo = LauncherModel.getAppInfo(intent.getComponent()
                                                               .getPackageName(), apps);
                        if (appInfo != null) {
                            appInfo.id = id;
                            appInfo.launchTimes = c.getLong(launchTimesIndex);
                            appInfo.hide = c.getInt(hideIndex) == 1 ? true : false;
                            appInfo.hideTemp = appInfo.hide;
                            hidens.add(appInfo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        return hidens;

    }

    public static AppInfo getAppInfo(String name, ArrayList<AppInfo> apps) {
        for (AppInfo app : apps) {
            String pn = app.componentName.getPackageName();
            if (pn.equals(name)) {
                return app;
            }
        }
        return null;
    }

    private static void loadFavoritesFromDb(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();

        FavoritesData.clear();
        String where = " hide = 0 AND  launchTimes > 0 ";
        synchronized (sBgLock) {
            final Uri contentUri = LauncherSettings.Applications.CONTENT_URI;
            final Cursor c = contentResolver.query(contentUri, null, where, null, null);

            try {
                final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Applications._ID);
                final int intentIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.INTENT);
                final int launchTimesIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.Applications.LAUNCH_TIMES);

                AppInfo appInfo;
                Intent intent;
                String intentDescription;
                long id;

                while (c.moveToNext()) {
                    try {
                        id = c.getLong(idIndex);
                        intentDescription = c.getString(intentIndex);
                        intent = Intent.parseUri(intentDescription, 0);

                        appInfo = FavoritesData.getAppInfo(intent.getComponent()
                                                               .getPackageName());
                        if (appInfo != null) {
                            appInfo.id = id;
                            appInfo.launchTimes = c.getLong(launchTimesIndex);
                            FavoritesData.add(appInfo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(Callbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<Callbacks>(callbacks);
        }
    }

    private static final int MESSAGE_SERVICE_START = 0;

    private static final int MESSAGE_SERVICE_STOP = 1;

    private static final int MESSAGE_DELAY_TIME = 30000;

    private Handler mTipsHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_SERVICE_START:
                    if (mApp.getContext() != null) {
                        mApp.getContext().startService(
                            new Intent(mApp.getContext(), MonitorService.class));
                    }
                    break;

                case MESSAGE_SERVICE_STOP:
                    if (mApp.getContext() != null) {
                        mApp.getContext().stopService(
                            new Intent(mApp.getContext(), MonitorService.class));
                    }
                    break;
            }
        }
    };

    private void timeChanged() {
        boolean isChanged = false;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (mYear != year || mMonth != month || mDay != day) {
            mYear = year;
            mMonth = month;
            mDay = day;
            isChanged = true;
            MonitorService.isDataChanged = true;
            if (mApp != null){
                DataService.startDataService(mApp.getContext(), DataService.WEATHER);
            }

            UsageUtil.setDefalt();
        }

        if (isChanged) {
            mApp.tipsDataChanged();
            FavoritesData.dayDecrease();

            if (!isScreenOff) {
                mTipsHandler.removeMessages(MESSAGE_SERVICE_START);
                mTipsHandler.removeMessages(MESSAGE_SERVICE_STOP);
                mTipsHandler.sendEmptyMessageDelayed(MESSAGE_SERVICE_STOP, 0);
                mTipsHandler.sendEmptyMessageDelayed(MESSAGE_SERVICE_START, MESSAGE_DELAY_TIME);
            }
        } else {
            mApp.tipsShowView();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
            || Intent.ACTION_PACKAGE_REMOVED.equals(action)
            || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            int op = PackageUpdatedTask.OP_NONE;

            if (packageName == null || packageName.length() == 0) {
                // they sent us a bad intent
                return;
            }

            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                op = PackageUpdatedTask.OP_UPDATE;
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_REMOVE;
                }
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_ADD;
                } else {
                    op = PackageUpdatedTask.OP_UPDATE;
                }

            }

            if (op != PackageUpdatedTask.OP_NONE) {
                enqueuePackageUpdated(new PackageUpdatedTask(op, new String[]{
                    packageName
                }));
            }
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
            // First, schedule to add these apps back in.
            String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_ADD, packages));
            // Then, rebind everything.
            startLoaderFromBackground();
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(
                PackageUpdatedTask.OP_UNAVAILABLE, packages));
        } else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            // If we have changed locale we need to clear out the labels in all
            // apps/workspace.
            forceReload();
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            if (mApp != null) {
                mApp.tipsUserPresent();
            }
        } else if (Intent.ACTION_TIME_TICK.equals(action)
                   || Intent.ACTION_TIME_CHANGED.equals(action)
                   || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            if (mApp != null) {
                timeChanged();
            }
        } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
            isScreenOff = false;
            if (mApp != null) {
                mTipsHandler.removeMessages(MESSAGE_SERVICE_START);
                mTipsHandler.removeMessages(MESSAGE_SERVICE_STOP);
                mApp.tipsScreenOn();
                mTipsHandler.sendEmptyMessageDelayed(MESSAGE_SERVICE_START, MESSAGE_DELAY_TIME);
            }
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            isScreenOff = true;
            if (mApp != null) {
                mTipsHandler.removeMessages(MESSAGE_SERVICE_START);
                mTipsHandler.removeMessages(MESSAGE_SERVICE_STOP);
                mApp.tipsScreenOff();
                mTipsHandler.sendEmptyMessageDelayed(MESSAGE_SERVICE_STOP, 600000l);
            }
        } else if (LauncherConstants.ACTION_APP_TIPS.equals(action)) {
            if (mApp != null) {
                Bundle bundle = intent.getExtras();
                String name = bundle.getString("name");
                long time = bundle.getLong("time");

                mApp.tipsShowApp(name, time);
            }
        } else if (LauncherConstants.ACTION_USAGE_SETTING_TIP_SHOW.equals(action)) {
            if (mApp != null) {
                mApp.showUsageSettingTip();
            }
        } else if (LauncherConstants.ACTION_USAGE_SETTING_TIP_REMOVE.equals(action)) {
            if (mApp != null) {
                mApp.removeUsageSettingTip();
            }
        } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
            // Check if configuration change was an mcc/mnc change which would
            // affect app resources
            // and we would need to clear out the labels in all apps/workspace.
            // Same handling as
            // above for ACTION_LOCALE_CHANGED
            Configuration currentConfig = context.getResources().getConfiguration();
            if (mPreviousConfigMcc != currentConfig.mcc) {
                forceReload();
            }
            mPreviousConfigMcc = currentConfig.mcc;
        } else if (DeviceUtils.SDK_INT > 15
                   && SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED.equals(action) ||
                   SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED.equals(action)) {
            if (mCallbacks != null) {
                Callbacks callbacks = mCallbacks.get();
                if (callbacks != null) {
                    callbacks.bindSearchablesChanged();
                }
            }
        }
    }

    private void forceReload() {
        resetLoadedState(true, true);

        // Do this here because if the launcher activity is running it will be
        // restarted.
        // If it's not running startLoaderFromBackground will merely tell it
        // that it needs to reload.
        startLoaderFromBackground();
    }

    public void resetLoadedState(boolean resetAllAppsLoaded, boolean resetWorkspaceLoaded) {
        synchronized (mLock) {
            // Stop any existing loaders first, so they don't set mAllAppsLoaded
            // or mWorkspaceLoaded to true later
            stopLoaderLocked();
            if (resetAllAppsLoaded) {
                mAllAppsLoaded = false;
            }
            if (resetWorkspaceLoaded) {
                mWorkspaceLoaded = false;
            }
        }
    }

    /**
     * When the launcher is in the background, it's possible for it to miss paired configuration
     * changes. So whenever we trigger the loader from the background tell the launcher that it
     * needs to re-run the loader when it comes back instead of doing it now.
     */
    public void startLoaderFromBackground() {
        boolean runLoader = false;
        if (mCallbacks != null) {
            Callbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
                // Only actually run the loader if they're not paused.
                if (!callbacks.setLoadOnResume()) {
                    runLoader = true;
                }
            }
        }
        if (runLoader) {
            startLoader(false);
        }
    }

    // If there is already a loader task running, tell it to stop.
    // returns true if isLaunching() was true on the old task
    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        return isLaunching;
    }

    public void startLoader(boolean isLaunching) {
        synchronized (mLock) {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "startLoader isLaunching=" + isLaunching);
            }

            // Don't bother to start the thread if we know it's not going to do
            // anything
            if (mCallbacks != null && mCallbacks.get() != null) {
                // If there is already one running, tell it to stop. also, don't
                // downgrade isLaunching if we're already running
                isLaunching = isLaunching || stopLoaderLocked();
                mLoaderTask = new LoaderTask(mApp.getContext(), isLaunching);
                if (mAllAppsLoaded && mWorkspaceLoaded) {
                    mLoaderTask.runBindWorkspace();
                } else {
                    sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                    sWorker.post(mLoaderTask);
                }
            }
        }
    }

//    public void stopLoader() {
//        synchronized (mLock) {
//            if (mLoaderTask != null) {
//                mLoaderTask.stopLocked();
//            }
//        }
//    }

//    public boolean isAllAppsLoaded() {
//        return mAllAppsLoaded;
//    }

//    boolean isLoadingWorkspace() {
//        synchronized (mLock) {
//            if (mLoaderTask != null) {
//                return mLoaderTask.isLoadingWorkspace();
//            }
//        }
//        return false;
//    }

    /**
     * Runnable for the thread that loads the contents of the launcher: - workspace icons - widgets
     * - all apps icons
     */
    private class LoaderTask implements Runnable {

        private Context mContext;
        private boolean mIsLaunching;
        private boolean mIsLoadingAndBindingWorkspace;
        private boolean mStopped;
        private boolean mLoadAndBindStepFinished;

        private HashMap<Object, CharSequence> mLabelCache;

        LoaderTask(Context context, boolean isLaunching) {
            mContext = context;
            mIsLaunching = isLaunching;
            mLabelCache = new HashMap<Object, CharSequence>();
        }

        boolean isLaunching() {
            return mIsLaunching;
        }

        boolean isLoadingWorkspace() {
            return mIsLoadingAndBindingWorkspace;
        }

        /**
         * Returns whether this is an upgrade path
         */
        private void loadAndBindWorkspace() {
            mIsLoadingAndBindingWorkspace = true;

            // Load the workspace
            if (DEBUG_LOADERS) {
                Log.d(TAG, "loadAndBindWorkspace mWorkspaceLoaded=" + mWorkspaceLoaded);
            }

            if (!mWorkspaceLoaded) {
                loadWorkspace();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mWorkspaceLoaded = true;
                }
            }

            // Bind the workspace
            bindWorkspace();
        }

        private void waitForIdle() {
            // Wait until the either we're stopped or the other threads are
            // done.
            // This way we don't start loading all apps until the workspace has
            // settled
            // down.
            synchronized (LoaderTask.this) {
                final long workspaceWaitTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                mHandler.postIdle(new Runnable() {
                    public void run() {
                        synchronized (LoaderTask.this) {
                            mLoadAndBindStepFinished = true;
                            if (DEBUG_LOADERS) {
                                Log.d(TAG, "done with previous binding step");
                            }
                            LoaderTask.this.notify();
                        }
                    }
                });

                while (!mStopped && !mLoadAndBindStepFinished && !mFlushingWorkerThread) {
                    try {
                        // Just in case mFlushingWorkerThread changes but we
                        // aren't woken up,
                        // wait no longer than 1sec at a time
                        this.wait(1000);
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "waited "
                               + (SystemClock.uptimeMillis() - workspaceWaitTime)
                               + "ms for previous step to finish binding");
                }
            }
        }

        void runBindWorkspace() {
            if (!mAllAppsLoaded || !mWorkspaceLoaded) {
                // Ensure that we don't try and bind a specified page when the
                // pages have not been loaded already (we should load everything
                // asynchronously in that case)
                throw new RuntimeException("Expecting AllApps and Workspace to be loaded");
            }

            synchronized (mLock) {
                if (mIsLoaderTaskRunning) {
                    // Ensure that we are never running the background loading
                    // at this point since we also touch the background
                    // collections
                    throw new RuntimeException("Error! Background loading is already running");
                }
            }

            // XXX: Throw an exception if we are already loading (since we touch
            // the worker thread data structures, we can't allow any other
            // thread to touch that data, but because this call is synchronous,
            // we can get away with not locking).

            // The LauncherModel is static in the LauncherAppState and mHandler
            // may have queued operations from the previous activity. We need to
            // ensure that all queued operations are executed before any
            // synchronous binding work is done.
            mHandler.flush();

            // Divide the set of loaded items into those that we are binding
            // synchronously, and everything else that is to be bound normally
            // (asynchronously).
            bindWorkspace();

            // XXX: For now, continue posting the binding of AllApps as there
            // are other issues that arise from that.
            onlyBindAllApps();
        }

        public void run() {

            synchronized (mLock) {
                mIsLoaderTaskRunning = true;
            }
            // Optimize for end-user experience: if the Launcher is up and //
            // running with the
            // All Apps interface in the foreground, load All Apps first.
            // Otherwise, load the
            // workspace first (default).
            keep_running:
            {
                // Elevate priority when Home launches for the first time to
                // avoid
                // starving at boot time. Staring at a blank home is not cool.
                synchronized (mLock) {
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "Setting thread priority to " +
                                   (mIsLaunching ? "DEFAULT" : "BACKGROUND"));
                    }
                    android.os.Process.setThreadPriority(mIsLaunching
                                                         ? Process.THREAD_PRIORITY_DEFAULT
                                                         : Process.THREAD_PRIORITY_BACKGROUND);
                }

                if (DEBUG_LOADERS) {
                    Log.d(TAG, "step 1: loading workspace");
                }
                loadAndBindWorkspace();

                if (mStopped) {
                    break keep_running;
                }

                // Whew! Hard work done. Slow us down, and wait until the UI
                // thread has
                // settled down.
                synchronized (mLock) {
                    if (mIsLaunching) {
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "Setting thread priority to BACKGROUND");
                        }
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    }
                }

//                waitForIdle();

                // second step
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "step 2: loading all apps");
                }
                loadAndBindAllApps();

                // Restore the default thread priority after we are done loading
                // items
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                }
            }

            // Clear out this reference, otherwise we end up holding it until
            // all of the callback runnables are done.
            mContext = null;

            synchronized (mLock) {
                // If we are still the last one to be scheduled, remove
                // ourselves.
                if (mLoaderTask == this) {
                    mLoaderTask = null;
                }
                mIsLoaderTaskRunning = false;
            }
        }

        public void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }

        /**
         * Gets the callbacks object. If we've been stopped, or if the launcher object has somehow
         * been garbage collected, return null instead. Pass in the Callbacks object that was around
         * when the deferred message was scheduled, and if there's a new Callbacks object around
         * then also return null. This will save us from calling onto it with data that will be
         * ignored.
         */
        Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
            synchronized (mLock) {
                if (mStopped) {
                    return null;
                }

                if (mCallbacks == null) {
                    return null;
                }

                final Callbacks callbacks = mCallbacks.get();
                if (callbacks != oldCallbacks) {
                    return null;
                }
                if (callbacks == null) {
                    Log.w(TAG, "no mCallbacks");
                    return null;
                }

                return callbacks;
            }
        }

        /**
         * Returns whether this is an upgrade path
         */
        private void loadWorkspace() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            final Context context = mContext;
            final ContentResolver contentResolver = context.getContentResolver();
            final PackageManager manager = context.getPackageManager();

            synchronized (sBgLock) {
                // Make sure the default workspace is loaded, if needed
                if (LauncherAppState.getLauncherProvider() != null) {
                    LauncherAppState.getLauncherProvider().loadDefaultWorkspaceIfNecessary(0);
                }

                sBgWorkspaceItems.clear();
                mMaxShortcutPos = -1;

                final Uri contentUri = LauncherSettings.Shortcuts.CONTENT_URI;
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "loading model from " + contentUri);
                }
                final Cursor c = contentResolver.query(contentUri, null, null, null, null);

                try {
                    final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Shortcuts._ID);
                    final int intentIndex = c.getColumnIndexOrThrow
                        (LauncherSettings.Shortcuts.INTENT);
                    final int iconNameIndex = c.getColumnIndexOrThrow(
                        LauncherSettings.Shortcuts.ICON_NAME);
                    final int positionIndex = c.getColumnIndexOrThrow(
                        LauncherSettings.Shortcuts.POSITION);

                    ShortcutInfo info;
                    String intentDescription;
                    long id;
                    Intent intent;
                    String iconName;
                    int iconId;
                    int position;

                    while (!mStopped && c.moveToNext()) {
                        try {

                            intentDescription = c.getString(intentIndex);

                            if ("*BROWSER*".equalsIgnoreCase(intentDescription)) {
//                                Log.d("ymctest", "*BROWSER*");

                                intent = null;
                            } else if ("*CAMERA*".equalsIgnoreCase(intentDescription)) {
                                intent = manager.getLaunchIntentForPackage("com.android.camera");
//                                Log.d("ymctest", "CAMERA");
                                if (intent == null) {
                                    continue;
                                }
                            } else {
                                try {
                                    intent = Intent.parseUri(intentDescription, 0);
                                    ComponentName cn = intent.getComponent();

                                    if (cn == null) {
                                        List<ResolveInfo> apps =
                                            manager.queryIntentActivities(intent, 0);
//                                        Log.d("ymctest", "loadWorkspace   apps :" + apps.toString());
                                        if (apps == null || apps.isEmpty()) {
                                            continue;
                                        }
//                                        Log.d("ymctest","loadWorkspace  componentName : "+ cn );
                                        String
                                            pkg =
                                            apps.get(0).activityInfo.applicationInfo.packageName;
                                        String cls = apps.get(0).activityInfo.name;
                                        cn = new ComponentName(pkg, cls);
                                        intent = new Intent(Intent.ACTION_MAIN);
                                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                        intent.setComponent(cn);
                                        Log.d("ymctest", "loadWorkspace  componentName  pkg :  " + pkg + "  cls :" + cls);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                    }
                                } catch (URISyntaxException e) {
                                    Logger.addDumpLog(TAG, "Invalid uri: "
                                                           + intentDescription, true);
                                    continue;
                                }
                            }

                            id = c.getLong(idIndex);
                            iconName = c.getString(iconNameIndex);
                            position = c.getInt(positionIndex);

                            //got it !!!
                            iconId = IconNameOrId.getIconId(iconName);

                            info = getShortcutInfo(intent, iconId);

                            Log.d("ymctest", "iconName :  "+iconName );
                            if (info != null) {
                                info.id = id;
                                info.intent = intent;
                                info.position = position;
                                mMaxShortcutPos++;

                                sBgWorkspaceItems.add(info);
                                for (ShortcutInfo shortcutInfo : sBgWorkspaceItems) {
                                    Log.d("ymctest", "shortcutinfo  : " + shortcutInfo.toString());
                                }
                                if ("*BROWSER*".equalsIgnoreCase(intentDescription)) {
                                    info.title = "*BROWSER*";
                                }
                            } else {
                                throw new RuntimeException("Unexpected null ShortcutInfo");
                            }
                        } catch (Exception e) {
                            Logger.addDumpLog(TAG,
                                              "Desktop items loading interrupted: " + e, true);
                        }
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }

                // Break early if we've stopped loading
                if (mStopped) {
                    sBgWorkspaceItems.clear();
                    return;
                }

                if (DEBUG_LOADERS) {
                    Logger.debug(TAG, "loaded workspace in " + (SystemClock.uptimeMillis() - t)
                                      + "ms");
                }
            }

            final AppWidgetManager widgets = AppWidgetManager.getInstance(context);

            synchronized (sBgLock) {
                sBgWidgetItems.clear();
                mMaxWidgetPos = -1;

                final Uri contentUri = LauncherSettings.Widgets.CONTENT_URI;

                final Cursor c = contentResolver.query(contentUri, null, null, null, null);

                try {
                    final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Shortcuts._ID);
                    final int widgetIdIndex = c.getColumnIndexOrThrow
                        (LauncherSettings.Widgets.APPWIDGET_ID);
                    final int widgetProviderIndex = c.getColumnIndexOrThrow
                        (LauncherSettings.Widgets.APPWIDGET_PROVIDER);
                    final int widthIndex = c.getColumnIndexOrThrow
                        (LauncherSettings.Widgets.WIDTH);
                    final int heightIndex = c.getColumnIndexOrThrow(
                        LauncherSettings.Widgets.HEIGHT);
                    final int spanxIndex = c.getColumnIndexOrThrow(
                        LauncherSettings.Widgets.SPANX);
                    final int spanyIndex = c.getColumnIndexOrThrow(
                        LauncherSettings.Widgets.SPANY);
                    final int positionIndex = c.getColumnIndexOrThrow(
                        LauncherSettings.Widgets.POSITION);
                    final int typeIndex = c.getColumnIndexOrThrow(
                        LauncherSettings.Widgets.TYPE);

                    long id;
                    int widgetId;
                    String widgetType;
                    String widgetProvider;
                    LauncherAppWidgetInfo appWidgetInfo;
                    PreferencesUtils.putBoolean(mContext, "weather_widget", false);
                    while (!mStopped && c.moveToNext()) {
                        try {
                            id = c.getLong(idIndex);
                            widgetId = c.getInt(widgetIdIndex);
                            widgetType = c.getString(typeIndex);
                            if ("nano".equals(widgetType)) {
                                widgetProvider = c.getString(widgetProviderIndex);
                                String[] providerStr = widgetProvider.split("/");
                                if (providerStr != null && providerStr.length == 2) {
                                    ComponentName
                                        providerName =
                                        new ComponentName(providerStr[0], providerStr[1]);
                                    appWidgetInfo =
                                        new LauncherAppWidgetInfo(widgetId, providerName);
                                    appWidgetInfo.id = id;
                                    appWidgetInfo.width = c.getInt(widthIndex);
                                    appWidgetInfo.height = c.getInt(heightIndex);
                                    appWidgetInfo.spanX = c.getInt(spanxIndex);
                                    appWidgetInfo.spanY = c.getInt(spanyIndex);
                                    appWidgetInfo.position = c.getInt(positionIndex);
                                    appWidgetInfo.type = widgetType;
                                    mMaxWidgetPos++;
                                    sBgWidgetItems.add(appWidgetInfo);
                                    PreferencesUtils.putBoolean(mContext, "weather_widget", true);
                                }
                            } else {
                                final AppWidgetProviderInfo provider =
                                    widgets.getAppWidgetInfo(widgetId);
                                if (provider == null || provider.provider == null ||
                                    provider.provider.getPackageName() == null) {
                                    //
                                } else {
                                    appWidgetInfo = new LauncherAppWidgetInfo(widgetId,
                                                                              provider.provider);
                                    appWidgetInfo.id = id;
                                    appWidgetInfo.width = c.getInt(widthIndex);
                                    appWidgetInfo.height = c.getInt(heightIndex);
                                    appWidgetInfo.spanX = c.getInt(spanxIndex);
                                    appWidgetInfo.spanY = c.getInt(spanyIndex);
                                    appWidgetInfo.position = c.getInt(positionIndex);

                                    widgetProvider = c.getString(widgetProviderIndex);
                                    String providerName = provider.provider.flattenToString();
                                    if (!providerName.equals(widgetProvider)) {
                                        ContentValues values = new ContentValues();
                                        values.put(LauncherSettings.Widgets.APPWIDGET_PROVIDER,
                                                   providerName);
                                        String where = BaseColumns._ID + "= ?";
                                        String[] args = {
                                            Long.toString(id)
                                        };
                                        contentResolver.update(contentUri, values, where, args);
                                    }
                                    mMaxWidgetPos++;
                                    sBgWidgetItems.add(appWidgetInfo);
                                }
                            }
                        } catch (Exception e) {
                            Logger.addDumpLog(TAG,
                                              "widget items loading interrupted: " + e, true);
                        }
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }

                // Break early if we've stopped loading
                if (mStopped) {
                    sBgWidgetItems.clear();
                    return;
                }
            }
        }

        private void sortWidgetItemsSpatially(ArrayList<LauncherAppWidgetInfo> widgetItems) {
            Collections.sort(widgetItems, new Comparator<LauncherAppWidgetInfo>() {
                @Override
                public int compare(LauncherAppWidgetInfo lhs, LauncherAppWidgetInfo rhs) {
                    return (int) (lhs.position - rhs.position);
                }
            });
        }

        private void bindWidgetItems(final Callbacks oldCallbacks,
                                     final ArrayList<LauncherAppWidgetInfo> widgetItems) {
            int N = widgetItems.size();
            for (int i = 0; i < N; i++) {
                final LauncherAppWidgetInfo widget = widgetItems.get(i);
                final Runnable r = new Runnable() {
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindAppWidget(widget);
                        }
                    }
                };

                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            }
        }

        private void sortWorkspaceItemsSpatially(ArrayList<ShortcutInfo> workspaceItems) {
            Collections.sort(workspaceItems, new Comparator<ShortcutInfo>() {
                @Override
                public int compare(ShortcutInfo lhs, ShortcutInfo rhs) {
                    return (int) (lhs.position - rhs.position);
                }
            });
        }

        private void bindWorkspaceItems(final Callbacks oldCallbacks,
                                        final ArrayList<ShortcutInfo> workspaceItems) {
            // Bind the workspace items
            int N = workspaceItems.size();
            for (int i = 0; i < N; i += ITEMS_CHUNK) {
                final int start = i;
                final int chunkSize = (i + ITEMS_CHUNK <= N) ? ITEMS_CHUNK : (N - i);
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindItems(workspaceItems, start, start + chunkSize,
                                                false);
                        }
                    }
                };
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            }
        }

        /**
         * Binds all loaded data to actual views on the main thread.
         */
        private void bindWorkspace() {
            final long t = SystemClock.uptimeMillis();
            Runnable r;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us. Just
                // bail.
                Log.w(TAG, "LoaderTask running with no launcher");
                return;
            }

            // Load all the items that are on the current page first (and in the
            // process, unbind all the existing workspace items before we call
            // startBinding() below.
            unbindWorkspaceItemsOnMainThread();

            ArrayList<ShortcutInfo> workspaceItems = new ArrayList<ShortcutInfo>();

            synchronized (sBgLock) {
                workspaceItems.addAll(sBgWorkspaceItems);
            }

            sortWorkspaceItemsSpatially(workspaceItems);

            // Tell the workspace that we're about to start binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.startBinding();
                    }
                }
            };
            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);

            // Load items on workspace
            bindWorkspaceItems(oldCallbacks, workspaceItems);

            // Tell the workspace that we're done binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.finishBindingItems();
                    }

                    // If we're profiling, ensure this is the last thing in the
                    // queue.
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound workspace in "
                                   + (SystemClock.uptimeMillis() - t) + "ms");
                    }

                    mIsLoadingAndBindingWorkspace = false;
                }
            };
            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);

            if (sBgWidgetItems.size() <= 0) {
                return;
            }

            ArrayList<LauncherAppWidgetInfo> widgetItems = new ArrayList<LauncherAppWidgetInfo>();

            synchronized (sBgLock) {
                widgetItems.addAll(sBgWidgetItems);
            }

            sortWidgetItemsSpatially(widgetItems);

            bindWidgetItems(oldCallbacks, widgetItems);

            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.finishBindWidget();
                    }
                }
            };
            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
        }

        private void loadAndBindAllApps() {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "loadAndBindAllApps mAllAppsLoaded=" + mAllAppsLoaded);
            }
            if (!mAllAppsLoaded) {
                loadAllApps();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mAllAppsLoaded = true;
                }
            } else {
                onlyBindAllApps();
            }
        }

        private void onlyBindAllApps() {
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us. Just
                // bail.
                Log.w(TAG, "LoaderTask running with no launcher (onlyBindAllApps)");
                return;
            }

            // shallow copy
            @SuppressWarnings("unchecked")
            final ArrayList<AppInfo> list = (ArrayList<AppInfo>) mBgAllAppsList.data.clone();
            Runnable r = new Runnable() {
                public void run() {
                    final long t = SystemClock.uptimeMillis();
                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindAllApplications(list);
                    }
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound all " + list.size() + " apps from cache in "
                                   + (SystemClock.uptimeMillis() - t) + "ms");
                    }
                }
            };
            boolean isRunningOnMainThread = !(sWorkerThread.getThreadId() == Process.myTid());
            if (isRunningOnMainThread) {
                r.run();
            } else {
                mHandler.post(r);
            }
        }

        private void loadAllApps() {
            final long loadTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us. Just
                // bail.
                Log.w(TAG, "LoaderTask running with no launcher (loadAllApps)");
                return;
            }

            final PackageManager packageManager = mContext.getPackageManager();
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            // Clear the list of apps
            mBgAllAppsList.clear();

            // Query for the set of apps
            final long qiaTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
            List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
            if (DEBUG_LOADERS) {
                Log.d(TAG, "queryIntentActivities took "
                           + (SystemClock.uptimeMillis() - qiaTime) + "ms");
                Log.d(TAG, "queryIntentActivities got " + apps.size() + " apps");
            }
            // Fail if we don't have any apps
            if (apps == null || apps.isEmpty()) {
                return;
            }

            // Create the ApplicationInfos
            for (int i = 0; i < apps.size(); i++) {
                ResolveInfo app = apps.get(i);
                if (isZenApp(app.activityInfo.applicationInfo.packageName)) {
                    continue;
                }
                // This builds the icon bitmaps.
                mBgAllAppsList.add(new AppInfo(packageManager, app,
                                               mIconCache, mLabelCache));
            }

            // Huh? Shouldn't this be inside the Runnable below?
            final ArrayList<AppInfo> added = mBgAllAppsList.added;
            mBgAllAppsList.added = new ArrayList<AppInfo>();

            FavoritesData.mApps.clear();
            FavoritesData.mApps.addAll(added);
            FavoritesData.filterApps(mContext);

            // Make sure the default favorites is loaded, if needed
            LauncherAppState.getLauncherProvider().loadDefaultFavoritesIfNecessary(added);
            loadFavoritesFromDb(mContext);
            final ArrayList<AppInfo> hidens = loadHideAppsFromDB(mContext, added);
            // Post callback on main thread
            mHandler.post(new Runnable() {
                public void run() {
                    final long bindTime = SystemClock.uptimeMillis();
                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindAllApplications(added);
                        callbacks.bindHidesApp(hidens);
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "bound " + added.size() + " apps in "
                                       + (SystemClock.uptimeMillis() - bindTime) + "ms");
                        }
                    } else {
                        Log.i(TAG, "not binding apps: no Launcher activity");
                    }
                }
            });

            if (DEBUG_LOADERS) {
                Log.d(TAG, "Icons processed in "
                           + (SystemClock.uptimeMillis() - loadTime) + "ms");
            }
        }

//        public void dumpState() {
//            synchronized (sBgLock) {
//                Log.d(TAG, "mLoaderTask.mContext=" + mContext);
//                Log.d(TAG, "mLoaderTask.mIsLaunching=" + mIsLaunching);
//                Log.d(TAG, "mLoaderTask.mStopped=" + mStopped);
//                Log.d(TAG, "mLoaderTask.mLoadAndBindStepFinished=" + mLoadAndBindStepFinished);
//                Log.d(TAG, "mItems size=" + sBgWorkspaceItems.size());
//            }
//        }
    }

    void enqueuePackageUpdated(PackageUpdatedTask task) {
        sWorker.post(task);
    }

    private class PackageUpdatedTask implements Runnable {

        int mOp;
        String[] mPackages;

        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted

        public PackageUpdatedTask(int op, String[] packages) {
            mOp = op;
            mPackages = packages;
        }

        public void run() {
            final Context context = mApp.getContext();

            final String[] packages = mPackages;
            final int N = packages.length;
            switch (mOp) {
                case OP_ADD:
                    for (int i = 0; i < N; i++) {
                        if (isZenApp(packages[i])) {
                            continue;
                        }
                        mBgAllAppsList.addPackage(context, packages[i]);
                    }
                    break;
                case OP_UPDATE:
                    for (int i = 0; i < N; i++) {
                        if (isZenApp(packages[i])) {
                            continue;
                        }
                        mBgAllAppsList.updatePackage(context, packages[i]);
                    }
                    break;
                case OP_REMOVE:
                    for (int i = 0; i < N; i++) {
                        if (isZenApp(packages[i])) {
                            continue;
                        }
                        mBgAllAppsList.removePackage(packages[i]);
                    }
                case OP_UNAVAILABLE:
                    for (int i = 0; i < N; i++) {
                        if (isZenApp(packages[i])) {
                            continue;
                        }
                        mBgAllAppsList.removePackage(packages[i]);
                    }
                    break;
            }

            ArrayList<AppInfo> added = null;
            ArrayList<AppInfo> modified = null;
            final ArrayList<AppInfo> removedApps = new ArrayList<AppInfo>();

            if (mBgAllAppsList.added.size() > 0) {
                added = new ArrayList<AppInfo>(mBgAllAppsList.added);
                mBgAllAppsList.added.clear();
            }
            if (mBgAllAppsList.modified.size() > 0) {
                modified = new ArrayList<AppInfo>(mBgAllAppsList.modified);
                mBgAllAppsList.modified.clear();
            }
            if (mBgAllAppsList.removed.size() > 0) {
                removedApps.addAll(mBgAllAppsList.removed);
                mBgAllAppsList.removed.clear();
            }

            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            if (callbacks == null) {
                Log.w(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                return;
            }

            if (added != null) {
                // Ensure that we add all the workspace applications to the db
                Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                addAndBindAddedApps(context, cb, added);
            }
            if (modified != null) {
                final ArrayList<AppInfo> modifiedFinal = modified;

                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindAppsUpdated(modifiedFinal);
                        }
                    }
                });
            }
            // If a package has been removed, or an app has been removed as a
            // result of
            // an update (for example), make the removed callback.
            if (mOp == OP_REMOVE || !removedApps.isEmpty()) {
                final boolean packageRemoved = (mOp == OP_REMOVE);
                final ArrayList<String> removedPackageNames =
                    new ArrayList<String>(Arrays.asList(packages));

                FavoritesData.datas.removeAll(removedApps);

                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            ArrayList<AppInfo> removed = new ArrayList<AppInfo>();
                            removed.addAll(removedApps);
                            callbacks.bindComponentsRemoved(removedPackageNames,
                                                            removedApps, packageRemoved);
                            callbacks.onRemoveApp(removed);
                        }
                    }
                });
            }
        }
    }

    public ShortcutInfo getShortcutInfo(Intent intent, int iconId) {
        final ShortcutInfo info = new ShortcutInfo();
        Bitmap icon = null;

        if (iconId != -1) {
            icon = BitmapUtils.getIcon(mApp.getContext().getResources(), iconId);
            info.mRecycle = true;
            info.mIconId = iconId;
        } else if (intent != null) {
            icon = mIconCache.getIcon(intent);
            info.mRecycle = false;
            info.mIconId = ShortcutInfo.NO_ID;
        }

        info.mIcon = icon;
        return info;
    }

    public static final Comparator<AppInfo> getAppNameComparator() {
        final Collator collator = Collator.getInstance();
        return new Comparator<AppInfo>() {
            public final int compare(AppInfo a, AppInfo b) {
                int result = collator.compare(a.title.toString().trim(),
                                              b.title.toString().trim());
                if (result == 0) {
                    result = a.componentName.compareTo(b.componentName);
                }
                return result;
            }
        };
    }

    public static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }

    public static class WidgetsComparator implements Comparator<AppWidgetProviderInfo> {

        @Override
        public int compare(AppWidgetProviderInfo lhs, AppWidgetProviderInfo rhs) {
            return Collator.getInstance().compare(lhs.label, rhs.label);

        }
    }

    public static ArrayList<AppWidgetProviderInfo> getSortedWidgets(Context context) {
        final ArrayList<AppWidgetProviderInfo> widgets = new ArrayList<AppWidgetProviderInfo>();
        widgets.addAll(AppWidgetManager.getInstance(context).getInstalledProviders());
        // filter zen widget self
        for (AppWidgetProviderInfo info : widgets) {
            if (LAUNCHER_ZEN.equals(info.provider.getPackageName())) {
                widgets.remove(info);
                break;
            }
        }
        Collections.sort(widgets, new WidgetsComparator());
        return widgets;
    }

//    public void dumpState() {
//        Log.d(TAG, "mCallbacks=" + mCallbacks);
//        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.data", mBgAllAppsList.data);
//        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.added", mBgAllAppsList.added);
//        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.removed", mBgAllAppsList.removed);
//        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.modified", mBgAllAppsList.modified);
//        if (mLoaderTask != null) {
//            mLoaderTask.dumpState();
//        } else {
//            Log.d(TAG, "mLoaderTask=null");
//        }
//    }
}
