package com.cooeeui.brand.zenlauncher.changeicon.dbhelp;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;

public class ChangeAppIconDBSearcherApp {

    public static ArrayList<ChangeAppIconDBEntity> appIconDBEntities;
    public ChangeAppIconDBUtils appIconDBUtils;

    public ChangeAppIconDBSearcherApp(Context context) {
        appIconDBUtils = new ChangeAppIconDBUtils(context);
        appIconDBEntities = appIconDBUtils
            .queryAppIcons(ChangeAppIconDBUtils.QUERY_TYPE_ICON_CHANGE_TYPE_ALL);
    }

    public Bitmap ChangeAppIconDBSearcherApps(String packageName, int iconChangeType,
                                              int iconPosition) {
        Bitmap bitmap = null;
        for (ChangeAppIconDBEntity appIconDBEntity : appIconDBEntities) {
            if (appIconDBEntity.getIconPackage().equals(packageName)) {
                bitmap = appIconDBEntity.getIcon();
            }
        }
        return bitmap;

    }
}
