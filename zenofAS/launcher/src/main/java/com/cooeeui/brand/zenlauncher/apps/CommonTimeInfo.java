package com.cooeeui.brand.zenlauncher.apps;

import android.content.ContentValues;

import com.cooeeui.brand.zenlauncher.LauncherSettings;

/**
 * tips中的两个圆圈需要使用的model
 *
 * @author Steve
 */
public class CommonTimeInfo extends ItemInfo {

    public static final int SCREEN_ON = 0;
    public static final int SCREEN_OFF = 1;
    public static final int UNLOCK = 2;

    public String input_time;
    public int lock_type;
    public float phone_time;

    public CommonTimeInfo() {
        super();
        itemType = LauncherSettings.ITEM_TYPE_COMMON_TIME;
    }

    public CommonTimeInfo(CommonTimeInfo info) {
        this.itemType = LauncherSettings.ITEM_TYPE_COMMON_TIME;
        this.input_time = info.input_time;
        this.phone_time = info.phone_time;
        this.lock_type = info.lock_type;
        this.id = info.id;
    }

    @Override
    public void onAddToDatabase(ContentValues values) {
        values.put(LauncherSettings.Common_Time.INPUT_TIME, input_time);
        values.put(LauncherSettings.Common_Time.LOCK_TYPE, lock_type);
        values.put(LauncherSettings.Common_Time.PHONE_TIME, phone_time);
    }

    @Override
    public String toString() {
        return "CommonTimeInfo(id=" + this.id + ", input_time=" + this.input_time + ", phone_time="
               + phone_time + ", lock_type=" + lock_type + " )";
    }
}
