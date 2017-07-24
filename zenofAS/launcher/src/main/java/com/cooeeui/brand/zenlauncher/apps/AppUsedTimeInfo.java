package com.cooeeui.brand.zenlauncher.apps;

import android.content.ContentValues;

import com.cooeeui.brand.zenlauncher.LauncherSettings;

/**
 * tips中的Top20列表需要使用的model
 *
 * @author Steve
 */

public class AppUsedTimeInfo extends ItemInfo {

    public String start_time;
    public String quit_time;
    public String pck_name;
    public long time;

    public AppUsedTimeInfo() {
        super();
        itemType = LauncherSettings.ITEM_TYPE_APPS_TIME;
    }

    public AppUsedTimeInfo(AppUsedTimeInfo info) {
        this.itemType = LauncherSettings.ITEM_TYPE_APPS_TIME;
        this.id = info.id;
        this.start_time = info.start_time;
        this.quit_time = info.quit_time;
        this.pck_name = info.pck_name;
        this.time = info.time;
    }

    @Override
    public void onAddToDatabase(ContentValues values) {
        values.put(LauncherSettings.AppsUsedTime.START_TIME, start_time);
        values.put(LauncherSettings.AppsUsedTime.QUIT_TIME, quit_time);
        values.put(LauncherSettings.AppsUsedTime.PCK_NAME, pck_name);
        values.put(LauncherSettings.AppsUsedTime.TIME, time);
    }

    @Override
    public String toString() {
        return "AppUsedTimeInfo(id=" + this.id + ", start_time=" + this.start_time + ", quit_time="
               + quit_time + " time " + time + ", pck_name=" + pck_name + " )";
    }
}
