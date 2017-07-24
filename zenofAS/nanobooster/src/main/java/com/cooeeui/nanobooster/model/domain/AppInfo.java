package com.cooeeui.nanobooster.model.domain;

import android.graphics.drawable.Drawable;

public class AppInfo {


    private Drawable icon;//应用程序图标
    private long memorySize;//应用程序大小
    private String appName;//应用程序名称
    private String packName;//应用程序包名
    private boolean igonreApp;//应用程序是否为Ignore
    private boolean isChecked;//判断checkbox是否选中
    private boolean isCouldUse;//判断checkbox是否可以
    private boolean userApp;//判断是否为系统应用程序

    public boolean isUserApp() {
        return userApp;
    }

    public void setUserApp(boolean userApp) {
        this.userApp = userApp;
    }


    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public long getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(long memorySize) {
        this.memorySize = memorySize;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public boolean isIgonreApp() {
        return igonreApp;
    }

    public void setIgonreApp(boolean igonreApp) {
        this.igonreApp = igonreApp;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean isCouldUse() {
        return isCouldUse;
    }

    public void setIsCouldUse(boolean isCouldUse) {
        this.isCouldUse = isCouldUse;
    }
}
