package com.cooeeui.brand.zenlauncher.tips;

import android.graphics.drawable.Drawable;

import com.cooeeui.zenlauncher.R;

public class TopAppInfo {

    private Drawable appIcon;
    private String appName;
    private int appTime;
    private int percent;
    private long max;
    private int[] COLORS = {
        R.color.circle_red, R.color.circle_yellow, R.color.circle_green
    };

    public int getProgressColor() {
        float hour = (float) (getAppUsedTimeWithMinute() / 60.0);
        if (hour >= 1.5) {
            return COLORS[0];
        } else if (hour >= 1.0) {
            return COLORS[1];
        }
        return COLORS[2];
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getAppUsedTime() {
        return appTime;
    }

    public float getAppUsedTimeWithMinute() {
        float f = (float) (Math.round((float) appTime / 60 * 10)) / 10;
        return f;
    }

    public void setAppUsedTime(int appUsedTime) {
        this.appTime = appUsedTime;
        double d1 = appUsedTime;
        double d2 = max;
        double r = (d1 / d2) * 100;
        this.percent = (int) r;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

}
