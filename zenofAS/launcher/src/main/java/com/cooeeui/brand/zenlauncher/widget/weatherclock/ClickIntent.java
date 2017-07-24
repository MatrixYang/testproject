package com.cooeeui.brand.zenlauncher.widget.weatherclock;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.zenlauncher.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClickIntent implements OnClickListener {

    private Intent clockIntent = null;
    private String clockpkgName = "com.android.deskclocks";
    private String clockclsName = "com.android.deskclock.DeskClock";
    private Intent calendarIntent = null;
    private String calendarpkgName = "com.android.providers.calendars";
    private String calendarclsName = "com.android.providers.calendar.CalendarProvider2";
    private Context context = null;
    private HashMap<String, ResolveInfo> allAppsMap = new HashMap<String, ResolveInfo>();
    private Launcher mLauncher;

    public ClickIntent(Context context) {
        this.context = context;
        getAllAppList();
    }

    public void setup(Launcher launcher) {
        mLauncher = launcher;
    }

    /**
     * 获得手机中所有的应用，并将类名和ResolveInfo存在allAppsMap中
     */
    private void getAllAppList() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        Iterator<ResolveInfo> ite = apps.iterator();
        ResolveInfo info = null;
        while (ite.hasNext()) {
            info = ite.next();
            if (info != null && info.loadLabel(packageManager) != null) {
                String clsName = info.activityInfo.name;
                allAppsMap.put(clsName, info);
            }

        }
    }

    @Override
    public void onClick(View v) {
        if (mLauncher.getSpeedDial().isDragState()) {
            mLauncher.getSpeedDial().stopDrag();
            return;
        }

        // TODO Auto-generated method stub
        Object tag = v.getTag();
        if (tag instanceof String) {
            String keyName = (String) tag;

            if (WeatherClockGroup.TAG_CLOCK.equals(keyName)) {
                if (clockIntent == null) {
                    clockIntent = getIntentByValue(keyName, clockpkgName, clockclsName);
                }
                if (clockIntent != null) {
                    startActivitySafely(context, clockIntent);
                }
            } else if (WeatherClockGroup.TAG_CALENDAR.equals(keyName)) {
                if (calendarIntent == null) {
                    calendarIntent = getIntentByValue(keyName, calendarpkgName, calendarclsName);
                }
                if (calendarIntent != null) {
                    startActivitySafely(context, calendarIntent);
                }
            }

        }

    }

    public boolean startActivitySafely(Context context, Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, StringUtil.getString(context, R.string.activity_not_found),
                           Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private Intent getIntentByValue(String keyName, String pkgName, String clsName) {
        // TODO Auto-generated method stub
        Intent intent = null;
        ComponentName componentName = new ComponentName(pkgName, clsName);
        intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        if (!isIntentAvailable(context, intent)) {
            intent = getAppIntent(keyName);
        }
        return intent;
    }

    /**
     * 获得手机中关于appName（例如camera）的intent
     */
    private Intent getAppIntent(String appName) {
        // TODO Auto-generated method stub
        Intent intent = null;
        Iterator iter = allAppsMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            if (key instanceof String && val instanceof ResolveInfo) {
                String clsKey = (String) key;
                ResolveInfo resolveInfo = (ResolveInfo) val;
                if (clsKey.contains(appName)) {
                    String pkgName = resolveInfo.activityInfo.packageName;
                    ComponentName cp = new ComponentName(pkgName, clsKey);
                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(cp);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    return intent;
                }
            }
        }
        return intent;
    }

    /**
     * 通过该intent查找该应用是否存在于该手机中
     */
    private boolean isIntentAvailable(Context context, Intent intent) {
        if (context == null || intent == null) {
            return false;
        }
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                                                                      PackageManager.GET_ACTIVITIES);
        return list.size() > 0;
    }
}
