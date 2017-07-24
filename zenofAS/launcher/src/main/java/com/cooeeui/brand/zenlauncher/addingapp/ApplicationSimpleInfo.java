package com.cooeeui.brand.zenlauncher.addingapp;

import android.content.ComponentName;
import android.graphics.Bitmap;

import com.cooeeui.brand.zenlauncher.android.adapter.ObjectEntity;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;

public class ApplicationSimpleInfo extends ObjectEntity {

    public ComponentName componentName;
    private Bitmap mIcon;
    private String mTitle;

    public ApplicationSimpleInfo() {

    }

    public ApplicationSimpleInfo(AppInfo info) {
        this.componentName = info.componentName;
        this.mTitle = info.title.toString();
        this.mIcon = info.iconBitmap;

    }

    public Bitmap getmIcon() {
        return mIcon;
    }

    public void setmIcon(Bitmap mIcon) {
        this.mIcon = mIcon;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

}
