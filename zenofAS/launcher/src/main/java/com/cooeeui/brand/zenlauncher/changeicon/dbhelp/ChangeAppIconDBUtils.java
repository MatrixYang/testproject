package com.cooeeui.brand.zenlauncher.changeicon.dbhelp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ChangeAppIconDBUtils {

    private static ChangeAppIconDBHelp appIconDBHelp;
    private static final String DB_NAME = "theme.db";
    private static final int DB_VERSION = 1;
    private static ContentValues values;
    public static ArrayList<ChangeAppIconDBEntity> appIconDBEntities;

    // 查询所有的图标
    public static final int QUERY_TYPE_ICON_CHANGE_TYPE_ALL = 1;

    // 查询首页的图标
    public static final int QUERY_TYPE_ICON_CHANGE_TYPE_FRIST_PAGE = 2;

    // 查询常用页的图标
    public static final int QUERY_TYPE_ICON_CHANGE_TYPE_MOST_USED_PAGE = 3;

    // 查询最新安装页的图标
    public static final int QUERY_TYPE_ICON_CHANGE_TYPE_LATEST_INSTALLED_PAGE = 4;

    // 查询All apps页的图标
    public static final int QUERY_TYPE_ICON_CHANGE_TYPE_ALL_APPS_PAGE = 5;

    // 删除对应的包名的所有记录
    public static final int DELETE_TYPE_ALL = 1;
    // 删除对应包名的所在容器的记录
    public static final int DELETE_TYPE_BY_CHANGE_TYPE = 2;
    // 删除首页对应包名的所在容器的记录
    public static final int DELETE_TYPE_BY_CHANGE_TYPE_FRIST_PAGE = 3;

    public ChangeAppIconDBUtils(Context context) {
        appIconDBHelp = new ChangeAppIconDBHelp(context, DB_NAME, DB_VERSION);
    }

    public void insertAppIcon(ChangeAppIconDBEntity appIconEntity) {
        SQLiteDatabase database = appIconDBHelp.getWritableDatabase();
        try {
            values = new ContentValues();
            onInsertToDatabase(values, appIconEntity);
            database.insert(ChangeAppIconDBHelp.TABLE_NAME,
                            ChangeAppIconDBHelp.COLUMN_NAME_ID, values);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ChangeAppIcon", "插入出错了么么哒！");
        } finally {
            database.close();
        }
    }

    public ArrayList<ChangeAppIconDBEntity> queryAppIcons(int queryiconType) {
        SQLiteDatabase database = appIconDBHelp.getWritableDatabase();
        Cursor cursor = database.query(ChangeAppIconDBHelp.TABLE_NAME, null,
                                       null, null, null, null, null);
        appIconDBEntities = new ArrayList<ChangeAppIconDBEntity>();
        ArrayList<ChangeAppIconDBEntity> queryAppIconDBEntities = null;
        try {
            ChangeAppIconDBEntity appIconDBEntity;
            while (cursor != null && cursor.moveToNext()) {
                appIconDBEntity = new ChangeAppIconDBEntity();
                appIconDBEntity
                    .setIconTitle(cursor.getString(cursor
                                                       .getColumnIndex(
                                                           ChangeAppIconDBHelp.COLUMN_NAME_ICON_TITLE)));
                appIconDBEntity
                    .setIconChangeType(cursor.getInt(cursor
                                                         .getColumnIndex(
                                                             ChangeAppIconDBHelp.COLUMN_NAME_ICON_CHANGE_TYPE)));
                appIconDBEntity
                    .setIconPosition(cursor.getInt(cursor
                                                       .getColumnIndex(
                                                           ChangeAppIconDBHelp.COLUMN_NAME_ICON_POSITION)));
                appIconDBEntity
                    .setIconType(cursor.getInt(cursor
                                                   .getColumnIndex(
                                                       ChangeAppIconDBHelp.COLUMN_NAME_ICON_TYPE)));
                appIconDBEntity
                    .setIconPackage(cursor.getString(cursor
                                                         .getColumnIndex(
                                                             ChangeAppIconDBHelp.COLUMN_NAME_ICON_PACKAGE)));
                appIconDBEntity
                    .setIcon(BitmapFactory.decodeByteArray(
                        cursor.getBlob(cursor
                                           .getColumnIndex(ChangeAppIconDBHelp.COLUMN_NAME_ICON)),
                        0,
                        cursor.getBlob(cursor
                                           .getColumnIndex(
                                               ChangeAppIconDBHelp.COLUMN_NAME_ICON)).length));
                appIconDBEntities.add(appIconDBEntity);
            }
            queryAppIconDBEntities = new ArrayList<ChangeAppIconDBEntity>();
            switch (queryiconType) {
                case QUERY_TYPE_ICON_CHANGE_TYPE_ALL:
                    queryAppIconDBEntities = appIconDBEntities;
                    break;
                case QUERY_TYPE_ICON_CHANGE_TYPE_FRIST_PAGE:
                    for (int i = 0; i < appIconDBEntities.size(); i++) {
                        if (appIconDBEntities.get(i).getIconChangeType()
                            == ChangeAppIconDBEntity.ICON_CHANGE_TYPE_FRIST_PAGE) {
                            queryAppIconDBEntities.add(appIconDBEntities.get(i));
                        }
                    }
                    break;
                case QUERY_TYPE_ICON_CHANGE_TYPE_MOST_USED_PAGE:
                    for (int i = 0; i < appIconDBEntities.size(); i++) {
                        if (appIconDBEntities.get(i).getIconChangeType()
                            == ChangeAppIconDBEntity.ICON_CHANGE_TYPE_MOST_USED_PAGE) {
                            queryAppIconDBEntities.add(appIconDBEntities.get(i));
                        }
                    }
                    break;
                case QUERY_TYPE_ICON_CHANGE_TYPE_LATEST_INSTALLED_PAGE:
                    for (int i = 0; i < appIconDBEntities.size(); i++) {
                        if (appIconDBEntities.get(i).getIconChangeType()
                            == ChangeAppIconDBEntity.ICON_CHANGE_TYPE_LATEST_INSTALLED_PAGE) {
                            queryAppIconDBEntities.add(appIconDBEntities.get(i));
                        }
                    }
                    break;
                case QUERY_TYPE_ICON_CHANGE_TYPE_ALL_APPS_PAGE:
                    for (int i = 0; i < appIconDBEntities.size(); i++) {
                        if (appIconDBEntities.get(i).getIconChangeType()
                            == ChangeAppIconDBEntity.ICON_CHANGE_TYPE_ALL_APPS_PAGE) {
                            queryAppIconDBEntities.add(appIconDBEntities.get(i));
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ChangeAppIcon", "查询出错了么么哒！");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            database.close();

        }
        return queryAppIconDBEntities;
    }

    public int updateAppIcon(ChangeAppIconDBEntity appIconDBEntity) {
        SQLiteDatabase database = appIconDBHelp.getWritableDatabase();
        int updateCount = 0;
        try {
            onInsertToDatabase(values, appIconDBEntity);
            updateCount = database
                .update(ChangeAppIconDBHelp.TABLE_NAME,
                        values,
                        ChangeAppIconDBHelp.COLUMN_NAME_ICON_PACKAGE
                        + "=? and "
                        + ChangeAppIconDBHelp.COLUMN_NAME_ICON_CHANGE_TYPE
                        + "=? and "
                        + ChangeAppIconDBHelp.COLUMN_NAME_ICON_POSITION
                        + "=?",
                        new String[]{
                            appIconDBEntity.getIconPackage(),
                            String.valueOf(appIconDBEntity
                                               .getIconChangeType()),
                            String.valueOf(appIconDBEntity
                                               .getIconPosition())
                        });
        } catch (Exception e) {
            Log.e("ChangeAppIcon", "更新出错了么么哒！");
            e.printStackTrace();
        } finally {
            database.close();
        }
        return updateCount;

    }

    public int deleteAppIcon(ChangeAppIconDBEntity appIconEntity, int deleteType) {
        int deleteCount = 0;
        SQLiteDatabase database = appIconDBHelp.getWritableDatabase();
        try {
            switch (deleteType) {
                case DELETE_TYPE_ALL:
                    deleteCount = database.delete(ChangeAppIconDBHelp.TABLE_NAME,
                                                  ChangeAppIconDBHelp.COLUMN_NAME_ICON_PACKAGE
                                                  + "=?",
                                                  new String[]{
                                                      appIconEntity.getIconPackage()
                                                  });
                    break;
                case DELETE_TYPE_BY_CHANGE_TYPE:
                    deleteCount = database
                        .delete(ChangeAppIconDBHelp.TABLE_NAME,
                                ChangeAppIconDBHelp.COLUMN_NAME_ICON_PACKAGE
                                + "=? and "
                                + ChangeAppIconDBHelp.COLUMN_NAME_ICON_CHANGE_TYPE
                                + "=?",
                                new String[]{
                                    appIconEntity.getIconPackage(),
                                    String.valueOf(appIconEntity
                                                       .getIconChangeType())
                                });
                    break;
                case DELETE_TYPE_BY_CHANGE_TYPE_FRIST_PAGE:
                    deleteCount = database
                        .delete(ChangeAppIconDBHelp.TABLE_NAME,
                                ChangeAppIconDBHelp.COLUMN_NAME_ICON_PACKAGE
                                + "=? and "
                                + ChangeAppIconDBHelp.COLUMN_NAME_ICON_CHANGE_TYPE
                                + "=? and "
                                + ChangeAppIconDBHelp.COLUMN_NAME_ICON_POSITION
                                + "=?",
                                new String[]{
                                    appIconEntity.getIconPackage(),
                                    String.valueOf(appIconEntity
                                                       .getIconChangeType()),
                                    String.valueOf(appIconEntity
                                                       .getIconPosition())
                                });
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ChangeAppIcon", "删除出错了么么哒！");
        } finally {
            database.close();
        }
        return deleteCount;
    }

    private void onInsertToDatabase(ContentValues values,
                                    ChangeAppIconDBEntity appIconEntity) {
        values.put(ChangeAppIconDBHelp.COLUMN_NAME_ICON_TITLE,
                   appIconEntity.getIconTitle());
        values.put(ChangeAppIconDBHelp.COLUMN_NAME_ICON_CHANGE_TYPE,
                   appIconEntity.getIconChangeType());
        values.put(ChangeAppIconDBHelp.COLUMN_NAME_ICON_POSITION,
                   appIconEntity.getIconPosition());
        values.put(ChangeAppIconDBHelp.COLUMN_NAME_ICON_PACKAGE,
                   appIconEntity.getIconPackage());
        values.put(ChangeAppIconDBHelp.COLUMN_NAME_ICON_TYPE,
                   appIconEntity.getIconType());
        Bitmap photo = appIconEntity.getIcon();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bArray = bos.toByteArray();
        values.put(ChangeAppIconDBHelp.COLUMN_NAME_ICON, bArray);
    }

    public void refreshDatabase() {
        SQLiteDatabase database = appIconDBHelp.getWritableDatabase();
        database.close();
    }
}
