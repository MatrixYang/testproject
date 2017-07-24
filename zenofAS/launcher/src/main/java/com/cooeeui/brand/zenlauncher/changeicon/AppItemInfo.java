package com.cooeeui.brand.zenlauncher.changeicon;

public class AppItemInfo {

    private String packageName;
    private String appName;

    public AppItemInfo(String packageName, String appName) {
        super();
        this.packageName = packageName;
        this.appName = appName;
    }

    public AppItemInfo() {
        super();
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

}
