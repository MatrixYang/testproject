package com.cooeeui.brand.zenlauncher.favorite;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.cooeeui.basecore.utilities.DateUtil;
import com.cooeeui.basecore.utilities.RunningAppHelper;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.brand.zenlauncher.LauncherModel;
import com.cooeeui.brand.zenlauncher.apps.AppUsedTimeInfo;
import com.cooeeui.brand.zenlauncher.config.FlavorController;
import com.cooeeui.brand.zenlauncher.tips.TipsPopup;
import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;

import java.util.HashMap;

public class MonitorService extends Service {

    private static String TAG = "MonitorService";
    private static final int TIME_INTERVAL_ONCE = 600;

    private static final int TIME_INTERVAL_FLUSH = 1800;

    private static boolean isRun;

    private static int times = 0;

    private static String lastName = null;

    public static HashMap<String, Long> countMap = new HashMap<String, Long>();

    private static AppUsedTimeInfo mAppUsedTimeInfo;

    private static AppUsedTimeInfo mAppTipsInfo = null;

    private static Context mContext;

    public static boolean isDataChanged = false;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static int TIPS_APP_TIME = 1800;

    public void updateTimes(String name) {
        if (countMap.containsKey(name)) {
            long c = countMap.get(name);
            c++;
            if (c % TIME_INTERVAL_ONCE == 0) {
                FavoritesData.updateTimes(name);
            }
            countMap.put(name, c);

            if (c % TIPS_APP_TIME == 0) {
                if (mAppTipsInfo == null) {
                    addToDatabase();
                    mAppTipsInfo = mAppUsedTimeInfo;
                } else {
                    mAppTipsInfo.quit_time = DateUtil.getNowTime();
                    LauncherModel.updateItemInDatabase(mContext, mAppTipsInfo);
                }
                Intent intent = new Intent(LauncherConstants.ACTION_APP_TIPS);
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putLong("time", c);
                intent.putExtras(bundle);
                sendBroadcast(intent);
            }
        } else {
            countMap.put(name, 1l);
        }
    }

    public static void addToDatabase() {
        mAppUsedTimeInfo.quit_time = DateUtil.getNowTime();
        mAppUsedTimeInfo.pck_name = lastName;
        LauncherModel.addItemToDatabase(mContext, mAppUsedTimeInfo);
    }
    @Override
    public void onCreate() {
        super.onCreate();

        // 使服务成为前台进程，这样减少应用在后台被kill的几率
        startForeground(410401, new Notification());
        WallpaperMonitorService.startDaemon(this);

        if (FlavorController.testVersion) {
            TIPS_APP_TIME = 300;
        }

        isRun = true;
        mAppUsedTimeInfo = new AppUsedTimeInfo();
        mContext = LauncherAppState.getInstance().getContext();

        if (isDataChanged) {
            countMap.clear();
            isDataChanged = false;
        }

        new Thread("MonitorService thread") {
            @Override
            public void run() {
                while (isRun) {

                    if (LauncherModel.isScreenOff) {
                        if (lastName != null && FavoritesData.getAppInfo(lastName) != null) {
                            addToDatabase();
                            lastName = null;
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            //
                        }
                        continue;
                    }

                    String pn = RunningAppHelper.getTopAppPckageName(mContext);

                    if (pn == null){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            //
                        }
                        continue;
                    }

                    if (pn != null) {

                        if (pn.equals(lastName)) {
                            mAppUsedTimeInfo.time++;
                        } else {

                            if (lastName != null && FavoritesData.getAppInfo(lastName) != null) {

                                if (mAppTipsInfo != null) {
                                    mAppTipsInfo.quit_time = DateUtil.getNowTime();
                                    LauncherModel.updateItemInDatabase(mContext, mAppTipsInfo);
                                    mAppTipsInfo = null;
                                } else {
                                    addToDatabase();
                                }
                            }
                            mAppUsedTimeInfo.start_time = DateUtil.getNowTime();
                            mAppUsedTimeInfo.time = 1;
                        }

                        if (FavoritesData.getAppInfo(pn) != null) {
                            if (pn.equals(lastName)) {
                                updateTimes(pn);
                            } else {
                                FavoritesData.updateTimes(pn);
                            }
                        }

                        lastName = pn;

                        if (FavoritesData.isNewAdd) {
                            Intent intent = new Intent(LauncherConstants.ACTION_FAVOTITE_UPDATE);
                            sendBroadcast(intent);
                            FavoritesData.isNewAdd = false;
                        }

                        if (times > TIME_INTERVAL_FLUSH) {
                            times = 0;
                            if (FavoritesData.isUpdate) {
                                Intent
                                    intent =
                                    new Intent(LauncherConstants.ACTION_FAVOTITE_UPDATE);
                                sendBroadcast(intent);
                                FavoritesData.isUpdate = false;
                            }
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            continue;
                        }

                        times++;

                        TipsPopup.userTime++;
                    }
                }

                //存在疑问的代码？
                if (isDataChanged) {
                    if (!LauncherModel.isScreenOff && lastName != null
                        && FavoritesData.getAppInfo(lastName) != null) {
                        mAppUsedTimeInfo.quit_time = DateUtil.getEndTime();
                        mAppUsedTimeInfo.pck_name = lastName;
                        LauncherModel.addItemToDatabase(mContext, mAppUsedTimeInfo);
                    }
                    countMap.clear();
                    isDataChanged = false;
                }

                lastName = null;
                times = 0;
                mAppUsedTimeInfo = null;
            }
        }.start();
    }
    @Override
    public void onDestroy() {

        stopForeground(true);
        super.onDestroy();
        isRun = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
