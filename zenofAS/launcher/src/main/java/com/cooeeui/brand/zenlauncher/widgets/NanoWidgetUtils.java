package com.cooeeui.brand.zenlauncher.widgets;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;

import com.cooeeui.zenlauncher.R;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Administrator on 2016/3/11.
 */
public class NanoWidgetUtils {

    public static final String ACTION_WIDGET_VIEW = "com.cooee.widget";
    public static final String ACTION_WIDGET_DELETE = "com.cooee.widget.delete";
    public static final String
        ACTION_WIDGET_LOAD_MOBVISTA_NATIVE_AD =
        "com.cooee.widget.load.mobvista.native.ad";
    public static final String PROXY_CLASS = "proxyclass";
    public static final String MIN_WIDTH = "minwidth";
    public static final String MIN_HEIGHT = "minheight";

    public static AppWidgetProviderInfo getNanoWidgetProviderInfo(Context context,
                                                                  ComponentName componentName) {

        AppWidgetProviderInfo appWidgetInfo = new AppWidgetProviderInfo();
        Intent intentLockView = new Intent(ACTION_WIDGET_VIEW);
        intentLockView.setComponent(componentName);
        List<ResolveInfo>
            infoList = context.getPackageManager().queryBroadcastReceivers(intentLockView,
                                                                           PackageManager.GET_META_DATA);
        ActivityInfo activityInfo = infoList.get(0).activityInfo;
        try {
            Field field = appWidgetInfo.getClass().getDeclaredField("providerInfo");
            field.set(appWidgetInfo, activityInfo);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Bundle bundle = activityInfo.metaData;
        appWidgetInfo.provider = componentName;
        appWidgetInfo.initialLayout = R.layout.nano_widget_default_layout;
        appWidgetInfo.label = activityInfo.loadLabel(context.getPackageManager()).toString();
        appWidgetInfo.icon = infoList.get(0).getIconResource();
        appWidgetInfo.configure =
            new ComponentName(context.getPackageName(), bundle.getString(PROXY_CLASS));
        appWidgetInfo.minWidth =
            (int) context.getResources().getDimension(bundle.getInt(MIN_WIDTH));
        appWidgetInfo.minHeight =
            (int) context.getResources().getDimension(bundle.getInt(MIN_HEIGHT));
        return appWidgetInfo;
    }

    public static View getNanoWidgetView(Context context, int id, String packageName,
                                         String className) {
        View view = null;
        boolean
            loadRst =
            WidgetProxyManager.getInstance().loadProxy(context, packageName, className);
        if (loadRst) {
            view = WidgetProxyManager.getInstance().getView(id);
        }
        return view;
    }

}
