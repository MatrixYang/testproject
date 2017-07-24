package com.cooeeui.brand.zenlauncher.widget.zenicon;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.cooeeui.zenlauncher.R;

public class ZenIconProvider extends AppWidgetProvider {

    private static final String CLICK_NAME_ACTION = "com.cooeeui.zenlauncher.widget.click";

    private static RemoteViews rv;

    public ZenIconProvider() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // TODO Auto-generated method stub
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);

        if (rv == null) {
            rv = new RemoteViews(context.getPackageName(), R.layout.widget_zen_icon);
        }
        if (intent.getAction().equals(CLICK_NAME_ACTION)) {
            Intent launch = new Intent();
            launch
                .setClassName("com.cooeeui.zenlauncher", "com.cooeeui.brand.zenlauncher.Launcher");
            launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launch);
        }
    }

    public static void updateAppWidget(Context context,
                                       AppWidgetManager appWidgeManger, int appWidgetId) {
        rv = new RemoteViews(context.getPackageName(), R.layout.widget_zen_icon);
        Intent intentClick = new Intent(CLICK_NAME_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                                                                 intentClick, 0);
        rv.setOnClickPendingIntent(R.id.widget_zen_icon, pendingIntent);
        appWidgeManger.updateAppWidget(appWidgetId, rv);
    }
}
