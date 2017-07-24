package com.cooeeui.brand.zenlauncher.widgets;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.ContentValues;
import android.os.Build;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherSettings;
import com.cooeeui.brand.zenlauncher.apps.ItemInfo;

public class LauncherAppWidgetInfo extends ItemInfo {

    public static final int NO_ID = -1;

    public int appWidgetId = NO_ID;

    public ComponentName providerName;

    public AppWidgetHostView hostView = null;

    public int width;

    public int height;

    public int spanX;

    public int spanY;

    public int position;

    public String type = "system";

    public LauncherAppWidgetInfo(int appWidgetId, ComponentName providerName) {
        itemType = LauncherSettings.ITEM_TYPE_APPWIDGET;
        this.appWidgetId = appWidgetId;
        this.providerName = providerName;
    }

    @SuppressLint("NewApi")
    public void notifyWidgetSizeChanged(Launcher launcher) {
        if (Build.VERSION.SDK_INT >= 16) {
            float density = launcher.getResources().getDisplayMetrics().density;
            hostView.updateAppWidgetSize(null, (int) (width / density), (int) (height / density),
                                         (int) (width / density), (int) (height / density));
        }
    }

    @Override
    public void onAddToDatabase(ContentValues values) {
        values.put(LauncherSettings.Widgets.APPWIDGET_ID, appWidgetId);
        values.put(LauncherSettings.Widgets.APPWIDGET_PROVIDER, providerName.flattenToString());
        values.put(LauncherSettings.Widgets.WIDTH, width);
        values.put(LauncherSettings.Widgets.HEIGHT, height);
        values.put(LauncherSettings.Widgets.SPANX, spanX);
        values.put(LauncherSettings.Widgets.SPANY, spanY);
        values.put(LauncherSettings.Widgets.POSITION, position);
        values.put(LauncherSettings.Widgets.TYPE, type);
    }

    @Override
    public void unbind() {
        super.unbind();
        hostView = null;
    }
}
