package com.cooeeui.brand.zenlauncher.alarmUpdate.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.cooeeui.brand.zenlauncher.mobvista.MobvistaAdHelper;
import com.cooeeui.brand.zenlauncher.searchbar.SearchHotWords;
import com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata.NumberClockHelper;
import com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata.Parameter;

public class DataService extends IntentService {

    public static String PARAMETER = "type";
    public static String WEATHER = "weather";
    public static String HOTWORDS = "hotwords";
    public static String MOBVISTA_APPWALL_PRELOAD = "mobvista_appwall_preload";

    public static String ACTION = "action";

    public static int ALARM_WEATHER = 1;
    public static int ALARM_HOTWORDS = 2;
    public static int ALARM_MOBVISTA = 3;

    public DataService() {
        super("CooeeDataService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String type = intent.getStringExtra(PARAMETER);
        if (type == null) {
            type = WEATHER;
        }
        if (type.equals(WEATHER)) {
            NumberClockHelper.updateWeather(this, Parameter.FLUSH_ALARM);
        } else if (type.equals(HOTWORDS)) {
            SearchHotWords.hotWords(this);
        } else if (type.equals(MOBVISTA_APPWALL_PRELOAD)) {
            MobvistaAdHelper.mobvistaAppWallPreload(this);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void startDataService(Context context, String param) {
        Intent intent = new Intent(context, DataService.class);
        intent.putExtra(DataService.PARAMETER, param);
        context.startService(intent);
    }

    public static void startAlarmDateService(Context context, long nextUpdate, int requestCode,
                                             String param) {
        Intent updateIntent = new Intent();
        updateIntent.setClass(context, DataService.class);
        updateIntent.putExtra(DataService.PARAMETER, param);
        PendingIntent pendingIntent = PendingIntent.getService(context, requestCode,
                                                               updateIntent, 0);
        AlarmManager
            alarmManager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent);
    }
}
