package com.cooeeui.brand.zenlauncher.favorite;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * 实际上这个服务是为了让调用它的服务成为前台服务，名字应该改为Daemon类似的，但为了让用户反感，随意取的名字
 * */
public class WallpaperMonitorService extends Service {

    public static void startDaemon(Context paramContext) {
        Intent localIntent = new Intent(paramContext, WallpaperMonitorService.class);
        paramContext.startService(localIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(410401, new Notification());
        stopForeground(true);

        return START_STICKY;
    }
}
