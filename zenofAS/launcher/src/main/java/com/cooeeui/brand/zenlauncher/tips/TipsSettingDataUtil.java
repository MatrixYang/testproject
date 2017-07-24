package com.cooeeui.brand.zenlauncher.tips;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;

import com.cooeeui.basecore.utilities.DateUtil;
import com.cooeeui.brand.zenlauncher.LauncherSettings;
import com.cooeeui.brand.zenlauncher.apps.CommonTimeInfo;

import java.util.ArrayList;

public class TipsSettingDataUtil {

    public static final int DAY = 1;

    // 获取数据库中最早的时间
    public static String getMinTime(Context context) {
        String maxTime = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = LauncherSettings.Common_Time.CONTENT_URI;
        String[] projection = new String[]{
            "date(min(input_time),'start of day') "
        };
        String where = "input_time IS NOT NULL";
        Cursor cursor = cr.query(uri, projection, where, null, null);
        while (cursor.moveToNext()) {
            maxTime = cursor.getString(0);
        }
        // release resouce
        if(cursor!=null){
            cursor.close();
        }
        return maxTime;
    }

    // 根据相对于当天的时间差获取当天两个圆的信息
    public static TextCircleViewInfo geteTipCircleInfoByTime(Context context, int dValue) {
        TextCircleViewInfo info = new TextCircleViewInfo();
        String nowTime = DateUtil.getNowDate();
        String start = DateUtil.getDateByAddDay(nowTime, dValue);
        String end = DateUtil.getDateByAddDay(nowTime, dValue + 1);
        int infotype = DAY;
        ContentResolver cr = context.getContentResolver();
        Uri uri = LauncherSettings.Common_Time.CONTENT_URI;
        String[] projection = new String[]{
            "sum(1) as sums "
        };
        String where = " input_time >'" + start + "' AND input_time< '" + end
                       + "' AND lock_type =" + CommonTimeInfo.UNLOCK
                       + ") GROUP BY  date(input_time, 'start of day' ";
        String orderby = "sums LIMIT 1";
        Cursor cursor = cr.query(uri, projection, where, null, orderby);
        long unlocktimes_tmp = 0;
        float phonetime_tmp = 0f;
        while (cursor.moveToNext()) {
            unlocktimes_tmp += cursor.getLong(0);
        }
        info.setUnlock_times(unlocktimes_tmp);
        cursor.close();
        projection = new String[]{
            "sum(phone_time) "
        };
        where = " input_time >'" + start + "' AND input_time< '" + end
                + "' AND lock_type=" + CommonTimeInfo.SCREEN_OFF
                + ") GROUP BY  date(input_time, 'start of day' ";
        cursor = cr.query(uri, projection, where, null, null);
        while (cursor.moveToNext()) {
            phonetime_tmp += cursor.getFloat(0);
        }
        cursor.close();
        info.setPhone_time(phonetime_tmp);
        info.type = infotype;
        return info;
    }

    // 根据时间，获取当天的TOP10的显示信息
    public static ArrayList<TopAppInfo> getTopAppInfoByDay(Context context, int dValue) {
        PackageManager manager = context.getPackageManager();
        ContentResolver cr = context.getContentResolver();
        String start = DateUtil.getDateByAddDay(DateUtil.getNowTime(), dValue);
        String end = DateUtil.getDateByAddDay(DateUtil.getNowTime(), dValue + 1);
        Uri uri = LauncherSettings.AppsUsedTime.CONTENT_URI;
        String[] projection = new String[]{
            "pck_name, sum(time) as sums"
        };
        String orderby = "sums desc  limit 10";
        ApplicationInfo pckInfo;
        Cursor cursor;
        ArrayList<TopAppInfo> infos = new ArrayList<TopAppInfo>();
        long time_max = 100;
        String where = " start_time > '" + start + "' AND start_time< '" + end
                       + "' ) GROUP BY (pck_name ";
        // 查找出耗时最大的
        cursor = cr.query(uri, projection, where, null, orderby);
        if (cursor.moveToFirst()) {
            if (cursor.isFirst()) {
                time_max = cursor.getLong(1);
            }
        }
        try {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                if (cursor.getInt(1) < 60) {
                    continue;
                }
                TopAppInfo info = new TopAppInfo();
                String app_pckName = cursor.getString(0);
                pckInfo = manager.getApplicationInfo(app_pckName, 0);
                info.setAppIcon(manager.getApplicationIcon(pckInfo));
                info.setAppName(manager.getApplicationLabel(pckInfo).toString());
                info.setMax(time_max);
                info.setAppUsedTime(cursor.getInt(1));
                infos.add(info);
            }
            cursor.close();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return infos;
    }

}
