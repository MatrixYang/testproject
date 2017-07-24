package com.cooeeui.nanobooster.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.cooeeui.nanobooster.MainActivity;
import com.cooeeui.nanobooster.R;

public class MyWidget extends AppWidgetProvider {
	  @Override    
	    public void onUpdate(Context context, AppWidgetManager appWidgetManager,     
	            int[] appWidgetIds) {     

              for(int i= 0;i<appWidgetIds.length;i++){
                  //新intent
                  Intent intent = new Intent(context,MainActivity.class);
                  PendingIntent pendingIntent = PendingIntent.getActivity(
                      context, 0, intent, 0);
                  //创建一个remoteViews。
                  RemoteViews remoteViews  = new RemoteViews(
                      context.getPackageName(), R.layout.widget_view);
                  //绑定处理器，表示控件单击后，会启动pendingIntent。
                  remoteViews.setOnClickPendingIntent(R.id.ll_widget, pendingIntent);
                  appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
              }
	        super.onUpdate(context, appWidgetManager, appWidgetIds);
	    }

}
