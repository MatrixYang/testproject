package com.cooeeui.brand.zenlauncher.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cooeeui.brand.zenlauncher.Launcher;

/**
 * 监听壁纸改变
 *
 * @author user
 */
public class WallpaperChangedReceiver extends BroadcastReceiver {

    public WallpaperChangedReceiver() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if (intent.getAction().equals(Intent.ACTION_WALLPAPER_CHANGED)) {
            if (Launcher.getInstance() != null && Launcher.getInstance().favoriteScene != null) {
                Launcher.getInstance().favoriteScene.blurFavoriteScene();
            }
        }
    }

}
