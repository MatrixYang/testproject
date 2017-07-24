package com.cooeeui.brand.zenlauncher.settings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.cooeeui.basecore.utilities.DateUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;

import java.util.UUID;

public class VersionUpdateNotification {

    private static NotificationManager notificationManager;
    private static Notification notification;
    private static RemoteViews notificationView;

    public static void showNotification(Context context) {
        // 创建一个NotificationManager的引用
        if (notificationManager == null) {
            notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        // 定义Notification的各种属性
        if (notification == null) {
            notification = new Notification();
            notification.icon = R.drawable.search_engine_nano;
            notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
        }
        notification.tickerText = StringUtil.getString(context, R.string.zen_launcher);

        if (notificationView == null) {
            notificationView =
                new RemoteViews(context.getPackageName(), R.layout.version_update_notification);
            notification.contentView = notificationView;
        }
        notification.contentView.setTextViewText(R.id.tv_version_notification_prompt, StringUtil
            .getString(context, R.string.version_update_prompt));

        String nowTime = DateUtil.getTime(DateUtil.getNowTime(), DateUtil.DateStyle.HH_MM);
        notification.contentView.setTextViewText(R.id.tv_version_notification_time, nowTime);

        Intent notificationIntent = new Intent(context, Launcher.class); // 点击该通知后要跳转的Activity
        notificationIntent.putExtra("just_alert", true);
        PendingIntent contentItent = PendingIntent
            .getActivity(context, UUID.randomUUID().hashCode(), notificationIntent,
                         PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentIntent = contentItent;

        notificationManager.notify(0, notification);
    }

    public static void clearNotification(Context context) {
        // 启动后删除之前我们定义的通知
        if (notificationManager == null) {
            notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        notificationManager.cancel(0);
    }

}
