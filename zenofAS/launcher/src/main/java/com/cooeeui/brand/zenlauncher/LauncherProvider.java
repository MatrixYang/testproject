/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cooeeui.brand.zenlauncher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.TrafficStats;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.LauncherSettings.Applications;
import com.cooeeui.brand.zenlauncher.LauncherSettings.BaseLauncherColumns;
import com.cooeeui.brand.zenlauncher.LauncherSettings.Shortcuts;
import com.cooeeui.brand.zenlauncher.LauncherSettings.Widgets;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.config.ProviderConfig;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.brand.zenlauncher.widgets.LauncherAppWidgetInfo;
import com.cooeeui.brand.zenlauncher.widgets.NanoWidgetUtils;
import com.cooeeui.zenlauncher.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class LauncherProvider extends ContentProvider {

    private static final String TAG = "Launcher.LauncherProvider";
    private static final boolean LOGD = false;

    private static final String DATABASE_NAME = "launcher.db";
    private static final int DATABASE_VERSION = 5;
    static final String AUTHORITY = ProviderConfig.AUTHORITY;

    static final String TABLE_SHORTCUTS = "shortcuts";
    static final String TABLE_WIDGETS = "widgets";
    static final String TABLE_APPS = "apps";
    static final String TABLE_APPS_USEDTIME = "apps_usedtime";
    static final String TABLE_COMMON_TIME = "common_time";
    static final String PARAMETER_NOTIFY = "notify";
    static final String EMPTY_TABLE_SHORTCUTS_CREATED = "EMPTY_TABLE_SHORTCUTS_CREATED";
    public static final String EMPTY_TABLE_APPS_CREATED = "EMPTY_TABLE_APPS_CREATED";
    static final String DEFAULT_WORKSPACE_RESOURCE_ID =
        "DEFAULT_WORKSPACE_RESOURCE_ID";

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        mOpenHelper = new DatabaseHelper(context);
        LauncherAppState.setLauncherProvider(this);
        return true;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        addModifiedTime(initialValues);
        final long rowId = dbInsertAndCheck(mOpenHelper, db, args.table, null, initialValues);
        if (rowId <= 0) {
            return null;
        }

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                addModifiedTime(values[i]);
                if (dbInsertAndCheck(mOpenHelper, db, args.table, null, values[i]) < 0) {
                    return 0;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        sendNotify(uri);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0) {
            sendNotify(uri);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        addModifiedTime(values);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);
        if (count > 0) {
            sendNotify(uri);
        }

        return count;
    }

    /**
     * Insert values to database with _id column check.
     */
    private static long dbInsertAndCheck(DatabaseHelper helper,
                                         SQLiteDatabase db, String table, String nullColumnHack,
                                         ContentValues values) {
        if (!values.containsKey(LauncherSettings.Shortcuts._ID)
            || !values.containsKey(LauncherSettings.Applications._ID) || !values
            .containsKey(LauncherSettings.Widgets._ID)) {
            throw new RuntimeException("Error: attempting to add item without specifying an id");
        }
        return db.insert(table, nullColumnHack, values);
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    private void addModifiedTime(ContentValues values) {
        values.put(LauncherSettings.ChangeLogColumns.MODIFIED, System.currentTimeMillis());
    }

    public void clearFavoriteTable() {
        mOpenHelper.clearFavoriteTable();
    }

    public long generateNewShortcutId() {
        return mOpenHelper.generateNewShortcutId();
    }

    public long generateNewWidgetId() {
        return mOpenHelper.generateNewWidgetId();
    }

    public long generateNewAppId() {
        return mOpenHelper.generateNewAppId();
    }

    public long generateNewCommonTimeId() {
        return mOpenHelper.generateNewCommonTimeId();
    }

    public void updateMaxCommonTimeId(long id) {
        mOpenHelper.updateMaxCommonTimeId(id);
    }

    public long generateNewAppUsedTimeId() {
        return mOpenHelper.generateAppUsedTimeId();
    }

    public void updateMaxAppUsedTimeId(long id) {
        mOpenHelper.updateMaxAppUsedTimeId(id);
    }

    /**
     * @param workspaceResId that can be 0 to use default or non-zero for specific resource
     */
    synchronized public void loadDefaultWorkspaceIfNecessary(int origWorkspaceResId) {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = getContext().getSharedPreferences(spKey, Context.MODE_PRIVATE);

        if (sp.getBoolean(EMPTY_TABLE_SHORTCUTS_CREATED, false)) {
            int workspaceResId = origWorkspaceResId;

            // Use default workspace resource if none provided
            if (workspaceResId == 0) {
                workspaceResId = sp.getInt(DEFAULT_WORKSPACE_RESOURCE_ID, R.xml.default_workspace);
            }

            // Populate shortcuts table with initial shortcuts
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(EMPTY_TABLE_SHORTCUTS_CREATED);
            if (origWorkspaceResId != 0) {
                editor.putInt(DEFAULT_WORKSPACE_RESOURCE_ID, origWorkspaceResId);
            }

            mOpenHelper.loadDefaultWorkspace(mOpenHelper.getWritableDatabase(), workspaceResId);
            editor.commit();
        }
    }

    synchronized public void loadDefaultFavoritesIfNecessary(ArrayList<AppInfo> allApps) {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = getContext().getSharedPreferences(spKey, Context.MODE_PRIVATE);

        if (sp.getBoolean(EMPTY_TABLE_APPS_CREATED, false)) {

            // Populate shortcuts table with initial shortcuts
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(EMPTY_TABLE_APPS_CREATED);
            mOpenHelper.loadDefaultFavorites(allApps, mOpenHelper.getWritableDatabase(),
                                             R.xml.default_favorites);
            editor.commit();
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String TAG_DEFAULT_WORKSPACE = "default_workspace";
        private static final String TAG_SHORTCUT = "shortcut";
        private static final String TAG_DEFAULT_FAVORITES = "default_favorites";
        private static final String TAG_FAVORITE = "favorite";
        private static final String TAG_APPWIDGET = "appwidget";
        private final Context mContext;

        private long mMaxShortcutId = -1;

        private long mMaxWidgetId = -1;

        private long mMaxAppId = -1;
        /**
         * table common_time id
         */
        private long mMaxCommonTimeId = -1;
        /**
         * table apps_usedtime id
         */
        private long mMaxAppsUsedTimeId = -1;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            initializeItemsId(getWritableDatabase());
        }

        private void initializeItemsId(SQLiteDatabase db) {
            // In the case where neither onCreate nor onUpgrade gets called, we
            // read the maxId from
            // the DB here
            if (mMaxShortcutId == -1) {
                mMaxShortcutId = initializeMaxShortcutId(db);
            }

            if (mMaxWidgetId == -1) {
                mMaxWidgetId = initializeMaxWidgetId(db);
            }

            if (mMaxAppId == -1) {
                mMaxAppId = initializeMaxAppId(db);
            }

            if (mMaxAppsUsedTimeId == -1) {
                mMaxAppsUsedTimeId = initializeMaxAppsUsedTimeId(db);
            }

            if (mMaxCommonTimeId == -1) {
                mMaxCommonTimeId = initializeMaxCommonTimeId(db);
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (LOGD) {
                Log.d(TAG, "creating new launcher database");
            }

            mMaxShortcutId = 1;
            mMaxWidgetId = 1;
            mMaxAppId = 1;
            mMaxCommonTimeId = 1;
            mMaxAppsUsedTimeId = 1;

            createTables(db);
            setFlagEmptyTablesCreated();
        }

        private void createTables(SQLiteDatabase db) {
            // Create table shortcuts.
            StringBuilder sqlString = new StringBuilder();
            sqlString.append("CREATE TABLE ");
            sqlString.append(TABLE_SHORTCUTS);
            sqlString.append("(");
            sqlString.append(LauncherSettings.BaseLauncherColumns._ID);
            sqlString.append(" INTEGER PRIMARY KEY,");
            sqlString.append(LauncherSettings.ChangeLogColumns.MODIFIED);
            sqlString.append(" INTEGER NOT NULL DEFAULT 0,");
            sqlString.append(LauncherSettings.BaseLauncherColumns.INTENT);
            sqlString.append(" TEXT,");
            sqlString.append(LauncherSettings.Shortcuts.POSITION);
            sqlString.append(" INTEGER,");
            sqlString.append(LauncherSettings.Shortcuts.ICON_NAME);
            sqlString.append(" TEXT");
            sqlString.append(");");
            db.execSQL(sqlString.toString());

            // Create table widgets.
            sqlString.delete(0, sqlString.length());
            sqlString.append("CREATE TABLE ");
            sqlString.append(TABLE_WIDGETS);
            sqlString.append("(");
            sqlString.append(LauncherSettings.BaseLauncherColumns._ID);
            sqlString.append(" INTEGER PRIMARY KEY,");
            sqlString.append(LauncherSettings.ChangeLogColumns.MODIFIED);
            sqlString.append(" INTEGER NOT NULL DEFAULT 0,");
            sqlString.append(LauncherSettings.Widgets.APPWIDGET_ID);
            sqlString.append(" INTEGER NOT NULL DEFAULT -1,");
            sqlString.append(LauncherSettings.Widgets.APPWIDGET_PROVIDER);
            sqlString.append(" TEXT,");
            sqlString.append(LauncherSettings.Widgets.WIDTH);
            sqlString.append(" INTEGER,");
            sqlString.append(LauncherSettings.Widgets.HEIGHT);
            sqlString.append(" INTEGER,");
            sqlString.append(LauncherSettings.Widgets.SPANX);
            sqlString.append(" INTEGER,");
            sqlString.append(LauncherSettings.Widgets.SPANY);
            sqlString.append(" INTEGER,");
            sqlString.append(LauncherSettings.Widgets.POSITION);
            sqlString.append(" INTEGER,");
            sqlString.append(LauncherSettings.Widgets.TYPE);
            sqlString.append(" TEXT");
            sqlString.append(");");
            db.execSQL(sqlString.toString());

            // Create table apps.
            sqlString.delete(0, sqlString.length());
            sqlString.append("CREATE TABLE ");
            sqlString.append(TABLE_APPS);
            sqlString.append("(");
            sqlString.append(LauncherSettings.BaseLauncherColumns._ID);
            sqlString.append(" INTEGER PRIMARY KEY,");
            sqlString.append(LauncherSettings.ChangeLogColumns.MODIFIED);
            sqlString.append(" INTEGER NOT NULL DEFAULT 0,");
            sqlString.append(LauncherSettings.BaseLauncherColumns.INTENT);
            sqlString.append(" TEXT,");
            sqlString.append(LauncherSettings.Applications.LAUNCH_TIMES);
            sqlString.append(" INTEGER,");
            sqlString.append(LauncherSettings.Applications.HIDE);
            sqlString.append(" INTEGER NOT NULL DEFAULT 0");
            sqlString.append(");");
            db.execSQL(sqlString.toString());

            // Create table common time.
            sqlString.delete(0, sqlString.length());
            sqlString.append("CREATE TABLE ");
            sqlString.append(TABLE_COMMON_TIME);
            sqlString.append("(");
            sqlString.append(LauncherSettings.BaseLauncherColumns._ID);
            sqlString.append(" INTEGER PRIMARY KEY ,");
            sqlString.append(LauncherSettings.ChangeLogColumns.MODIFIED);
            sqlString.append(" INTEGER NOT NULL DEFAULT 0 ,");
            sqlString.append(LauncherSettings.Common_Time.INPUT_TIME);
            sqlString.append(" TEXT ,");
            sqlString.append(LauncherSettings.Common_Time.PHONE_TIME);
            sqlString.append(" INTEGER ,");
            sqlString.append(LauncherSettings.Common_Time.LOCK_TYPE);
            sqlString.append(" INTEGER ");
            sqlString.append(" );");
            db.execSQL(sqlString.toString());

            // Create table app used time.
            sqlString.delete(0, sqlString.length());
            sqlString.append("CREATE TABLE ");
            sqlString.append(TABLE_APPS_USEDTIME);
            sqlString.append("(");
            sqlString.append(LauncherSettings.BaseLauncherColumns._ID);
            sqlString.append(" INTEGER PRIMARY KEY,");
            sqlString.append(LauncherSettings.ChangeLogColumns.MODIFIED);
            sqlString.append(" INTEGER NOT NULL DEFAULT 0,");
            sqlString.append(LauncherSettings.AppsUsedTime.START_TIME);
            sqlString.append(" TEXT,");
            sqlString.append(LauncherSettings.AppsUsedTime.QUIT_TIME);
            sqlString.append(" TEXT,");
            sqlString.append(LauncherSettings.AppsUsedTime.TIME);
            sqlString.append(" INTEGER,");
            sqlString.append(LauncherSettings.AppsUsedTime.PCK_NAME);
            sqlString.append(" TEXT ");
            sqlString.append(" );");
            db.execSQL(sqlString.toString());

        }

        private void setFlagEmptyTablesCreated() {
            String spKey = LauncherAppState.getSharedPreferencesKey();
            SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(EMPTY_TABLE_SHORTCUTS_CREATED, true);
            editor.putBoolean(EMPTY_TABLE_APPS_CREATED, true);
            editor.commit();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            System.out.println("onUpgrade");
            // 日后跨版本升级准备
            for (int i = oldVersion; i < newVersion; i++) {
                switch (i) {
                    case 1:

                        break;
                    case 2:
                        String sql = "ALTER TABLE apps add COLUMN hide INTEGER DEFAULT 0 NOT NULL";
                        db.execSQL(sql);
                        break;
                    case 3:
                        String widgetsSql = "ALTER TABLE widgets add COLUMN type TEXT";
                        db.execSQL(widgetsSql);
                        break;
                    case 4:
                        String
                            oldprovider =
                            "com.cooeeui.zenlauncher/com.cooeeui.brand.zenlauncher.widget.weatherclock.WeatherWidget";
                        String
                            newprovider =
                            "com.cooeeui.zenlauncher/com.cooeeui.brand.zenlauncher.widgets.weather.WeatherWidget";
                        Cursor result = db.query(TABLE_WIDGETS, null,
                                                 LauncherSettings.Widgets.APPWIDGET_PROVIDER + "=?",
                                                 new String[]{oldprovider}, null, null, null);
                        if (result != null) {
                            while (result.moveToNext()) {
                                ContentValues newValues = new ContentValues();
                                newValues.put(LauncherSettings.Widgets.APPWIDGET_ID, result
                                    .getString(result.getColumnIndexOrThrow(
                                        LauncherSettings.Widgets.APPWIDGET_ID)));
                                newValues
                                    .put(LauncherSettings.Widgets.APPWIDGET_PROVIDER, newprovider);
                                newValues.put(LauncherSettings.Widgets.WIDTH, result.getString(
                                    result.getColumnIndexOrThrow(LauncherSettings.Widgets.WIDTH)));
                                newValues.put(LauncherSettings.Widgets.HEIGHT, result.getString(
                                    result.getColumnIndexOrThrow(LauncherSettings.Widgets.HEIGHT)));
                                newValues.put(LauncherSettings.Widgets.SPANX, result.getString(
                                    result.getColumnIndexOrThrow(LauncherSettings.Widgets.SPANX)));
                                newValues.put(LauncherSettings.Widgets.SPANY, result.getString(
                                    result.getColumnIndexOrThrow(LauncherSettings.Widgets.SPANY)));
                                newValues.put(LauncherSettings.Widgets.POSITION, result.getString(
                                    result
                                        .getColumnIndexOrThrow(LauncherSettings.Widgets.POSITION)));
                                newValues.put(LauncherSettings.Widgets.TYPE, result.getString(
                                    result.getColumnIndexOrThrow(LauncherSettings.Widgets.TYPE)));
                                db.update(TABLE_WIDGETS, newValues,
                                          LauncherSettings.Widgets.APPWIDGET_PROVIDER + "=?",
                                          new String[]{oldprovider});
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        public void clearFavoriteTable() {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM " + TABLE_APPS);
            mMaxAppId = 1;
        }

        // Generates a new ID to use for an object in your database. This method
        // should be only called from the main UI thread. As an exception, we do
        // call it when we call the constructor from the worker thread; however,
        // this doesn't extend until after the constructor is called, and we
        // only pass a reference to LauncherProvider to LauncherApp after that
        // point
        public long generateNewShortcutId() {
            if (mMaxShortcutId < 0) {
                throw new RuntimeException("Error: max item id was not initialized");
            }
            mMaxShortcutId += 1;
            return mMaxShortcutId;
        }

        private long initializeMaxShortcutId(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT MAX(_id) FROM " + TABLE_SHORTCUTS, null);

            // get the result
            final int maxIdIndex = 0;
            long id = -1;
            if (c != null && c.moveToNext()) {
                id = c.getLong(maxIdIndex);
            }
            if (c != null) {
                c.close();
            }

            if (id == -1) {
                throw new RuntimeException("Error: could not query max shortcut id");
            }

            return id;
        }

        public long generateNewWidgetId() {
            if (mMaxWidgetId < 0) {
                throw new RuntimeException("Error: max item id was not initialized");
            }
            mMaxWidgetId += 1;
            return mMaxWidgetId;
        }

        private long initializeMaxWidgetId(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT MAX(_id) FROM " + TABLE_WIDGETS, null);

            // get the result
            final int maxIdIndex = 0;
            long id = -1;
            if (c != null && c.moveToNext()) {
                id = c.getLong(maxIdIndex);
            }
            if (c != null) {
                c.close();
            }

            if (id == -1) {
                throw new RuntimeException("Error: could not query max shortcut id");
            }

            return id;
        }

        public long generateNewAppId() {
            if (mMaxAppId < 0) {
                throw new RuntimeException("Error: max app id was not initialized");
            }
            mMaxAppId += 1;
            return mMaxAppId;
        }

        private long initializeMaxAppId(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT MAX(_id) FROM " + TABLE_APPS, null);

            // get the result
            final int maxIdIndex = 0;
            long id = -1;
            if (c != null && c.moveToNext()) {
                id = c.getLong(maxIdIndex);
            }
            if (c != null) {
                c.close();
            }

            if (id == -1) {
                throw new RuntimeException("Error: could not query max app id");
            }

            return id;
        }

        private long initializeMaxCommonTimeId(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT MAX(_id) FROM " + TABLE_COMMON_TIME, null);

            // get the result
            final int maxIdIndex = 0;
            long id = -1;
            if (c != null && c.moveToNext()) {
                id = c.getLong(maxIdIndex);
            }
            if (c != null) {
                c.close();
            }

            if (id == -1) {
                throw new RuntimeException("Error: could not query max common time id");
            }

            return id;
        }

        public long generateNewCommonTimeId() {
            if (mMaxCommonTimeId < 0) {
                throw new RuntimeException("Error: max item id was not initialized");
            }
            mMaxCommonTimeId += 1;
            return mMaxCommonTimeId;
        }

        public void updateMaxCommonTimeId(long id) {
            mMaxCommonTimeId = id + 1;
        }

        private long initializeMaxAppsUsedTimeId(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT MAX(_id) FROM " + TABLE_APPS_USEDTIME, null);

            // get the result
            final int maxIdIndex = 0;
            long id = -1;
            if (c != null && c.moveToNext()) {
                id = c.getLong(maxIdIndex);
            }
            if (c != null) {
                c.close();
            }

            if (id == -1) {
                throw new RuntimeException("Error: could not query max apps usedtime id");
            }

            return id;
        }

        public long generateAppUsedTimeId() {
            if (mMaxAppsUsedTimeId < 0) {
                throw new RuntimeException("Error: max item id was not initialized");
            }
            mMaxAppsUsedTimeId += 1;
            return mMaxAppsUsedTimeId;
        }

        public void updateMaxAppUsedTimeId(long id) {
            mMaxAppsUsedTimeId += 1;
        }

        private static final void beginDocument(XmlPullParser parser, String firstElementName)
            throws XmlPullParserException, IOException {
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG
                   && type != XmlPullParser.END_DOCUMENT) {
                ;
            }

            if (type != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("No start tag found");
            }

            if (!parser.getName().equals(firstElementName)) {
                throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                                                 ", expected " + firstElementName);
            }
        }

        /**
         * Loads the default set of shortcut or favorite packages from an xml file.
         *
         * @param db                  The database to write the values into
         * @param workspaceResourceId the xml resource id, chould not be 0
         * @return the item count be loaded
         */
        private int loadDefaultWorkspace(SQLiteDatabase db, int workspaceResourceId) {
            ContentValues values = new ContentValues();
            if (LOGD) {
                Log.v(TAG, String
                    .format("Loading default shortcut from resid=0x%08x", workspaceResourceId));
            }

            int i = 0;
            try {
                XmlResourceParser parser = mContext.getResources().getXml(workspaceResourceId);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                beginDocument(parser, TAG_DEFAULT_WORKSPACE);
                final int depth = parser.getDepth();
                int type;
                while (((type = parser.next()) != XmlPullParser.END_TAG ||
                        parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }

                    boolean added = false;
                    final String name = parser.getName();
                    TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Default);
                    values.clear();

                    if (LOGD) {
                        final String title = a.getString(R.styleable.Default_title);
                        final String pkg = a.getString(R.styleable.Default_packageName);
                        final String something = title != null ? title : pkg;
                        Log.v(TAG, String.format(
                            ("%" + (2 * (depth + 1)) + "s<%s%s pos=%s>"),
                            "", name,
                            (something == null ? "" : (" \"" + something + "\"")),
                            ""));
                    }

                    if (TAG_SHORTCUT.equals(name)) {
                        long id = addAppShortcut(db, values, a);
                        added = id >= 0;
                    } else if (TAG_APPWIDGET.equals(name)) {
                        long id = addAppWidget(db, values, a);
                        added = id >= 0;
                    }
                    if (added) {
                        i++;
                    }
                    a.recycle();
                }
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Got exception parsing default shortcut.", e);
            } catch (IOException e) {
                Log.w(TAG, "Got exception parsing default shortcut.", e);
            } catch (RuntimeException e) {
                Log.w(TAG, "Got exception parsing default shortcut.", e);
            }
            // Update the max shortcut id after we have loaded the database
            if (mMaxShortcutId == -1) {
                mMaxShortcutId = initializeMaxShortcutId(db);
            }
            return i;
        }

        private long addAppShortcut(SQLiteDatabase db, ContentValues values, TypedArray a) {

            long id = -1;
            String position = a.getString(R.styleable.Default_position);
            String iconName = a.getString(R.styleable.Default_iconName);
            Intent intent = LauncherAppState.getAppIntentUtil().getIntentByUri(
                a.getString(R.styleable.Default_intent),
                iconName);
            values.put(Shortcuts.ICON_NAME, iconName);
            values.put(Shortcuts.POSITION, position);
            if (intent != null) {
                values.put(Shortcuts.INTENT, intent.toUri(0));
            } else {
                values.put(Shortcuts.INTENT, a.getString(R.styleable.Default_intent));
            }
            id = generateNewShortcutId();
            values.put(Shortcuts._ID, id);
            Log.d("ymctest","intent  componentName :"+intent.getComponent());
            if (dbInsertAndCheck(this, db, TABLE_SHORTCUTS, null, values) < 0) {
                return -1;
            }
            return id;
        }

        private long addAppWidget(SQLiteDatabase db, ContentValues values, TypedArray a) {
            long id = -1;
            String packageName = a.getString(R.styleable.Default_packageName);
            String className = a.getString(R.styleable.Default_className);
            String position = a.getString(R.styleable.Default_position);
            String widgetType = a.getString(R.styleable.Default_widgetType);
            ComponentName providerName = new ComponentName(packageName, className);

            if ("nano".equals(widgetType)) {
                AppWidgetHost appWidgetHost = Launcher.getInstance().getAppWidgetHost();
                int appWidgetId = appWidgetHost.allocateAppWidgetId();
                AppWidgetProviderInfo
                    appWidgetInfo =
                    NanoWidgetUtils.getNanoWidgetProviderInfo(Launcher
                                                                  .getInstance(),
                                                              providerName);
                LauncherAppWidgetInfo
                    info =
                    new LauncherAppWidgetInfo(appWidgetId, appWidgetInfo.provider);
                int[] size = Launcher.getSizeForWidget(Launcher.getInstance(), appWidgetInfo);
                info.width = size[0];
                info.height = size[1];
                info.spanX = size[2];
                info.spanY = size[3];
                info.type = widgetType;
                info.onAddToDatabase(values);
                id = generateNewWidgetId();
                values.put(Widgets._ID, id);
                values.put(Widgets.POSITION, position);
                if (dbInsertAndCheck(this, db, TABLE_WIDGETS, null, values) < 0) {
                    return -1;
                }
            }

            return id;
        }

        private class TrafficComparator implements Comparator<String> {

            private HashMap<String, Long> mMap;

            public TrafficComparator(HashMap<String, Long> map) {
                mMap = map;
            }

            @Override
            public int compare(String lhs, String rhs) {
                if (mMap.get(lhs) < mMap.get(rhs)) {
                    return -1;
                }
                if (mMap.get(lhs) > mMap.get(rhs)) {
                    return 1;
                }
                return 0;
            }
        }

        private static final int TRAFFIC_MAX_NUM = 50;

        private HashMap<String, Long> getTrafficInfos() {
            final PackageManager pm = mContext.getPackageManager();
            List<PackageInfo> packinfos = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

            HashMap<String, Long> trafficMap = new HashMap<String, Long>();
            HashSet<Integer> uidSet = new HashSet<Integer>();
            ArrayList<String> names = new ArrayList<String>();

            for (PackageInfo packinfo : packinfos) {
                String[] permissions = packinfo.requestedPermissions;
                if (permissions != null && permissions.length > 0) {
                    for (String permission : permissions) {
                        if ("android.permission.INTERNET".equals(permission)) {
                            int uid = packinfo.applicationInfo.uid;
                            long num = TrafficStats.getUidRxBytes(uid);
                            if (num > 150000 && !uidSet.contains(uid)) {
                                uidSet.add(uid);
                                trafficMap.put(packinfo.packageName, num);
                            }
                            break;
                        }
                    }
                }
            }

            for (String n : trafficMap.keySet()) {
                names.add(n);
            }

            Collections.sort(names, new TrafficComparator(trafficMap));

            trafficMap.clear();

            for (int i = 0; i < names.size(); i++) {
                if (i >= TRAFFIC_MAX_NUM) {
                    break;
                }
                trafficMap.put(names.get(i), (long) (TRAFFIC_MAX_NUM - i) * 7);
            }

            return trafficMap;
        }

        private void loadDefaultFavorites(ArrayList<AppInfo> allApps, SQLiteDatabase db,
                                          int workspaceResourceId) {
            ContentValues values = new ContentValues();
            String packageName;

            HashMap<String, Long> trafficMap = getTrafficInfos();
            ArrayList<String> names = new ArrayList<String>();

            try {
                XmlResourceParser parser = mContext.getResources().getXml(workspaceResourceId);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                beginDocument(parser, TAG_DEFAULT_FAVORITES);
                final int depth = parser.getDepth();
                int type;
                int favoriteCount = 0;
                while (((type = parser.next()) != XmlPullParser.END_TAG ||
                        parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }

                    final String name = parser.getName();
                    TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Default);

                    if (TAG_FAVORITE.equals(name)
                        && favoriteCount < LauncherAppState.DEFAULT_FAVORITE_NUM) {
                        packageName = a.getString(R.styleable.Default_packageName);
                        long num = 0;
                        String pn = null;
                        for (AppInfo app : allApps) {
                            if (app.componentName.getPackageName().equals(packageName)) {
                                pn = app.componentName.getPackageName();
                                num = (LauncherAppState.DEFAULT_FAVORITE_NUM - favoriteCount) * 3;
                                if (trafficMap.containsKey(pn)) {
                                    num += trafficMap.get(pn);
                                    trafficMap.put(pn, num);
                                } else {
                                    trafficMap.put(pn, num);
                                }
                                favoriteCount++;
                                break;
                            }
                        }
                    }

                    a.recycle();
                }

                for (String key : trafficMap.keySet()) {
                    names.add(key);
                }

                Collections.sort(names, new TrafficComparator(trafficMap));

                int num = 0;
                int count = 0;
                String pn = null;
                for (int i = 0; i < names.size(); i++) {
                    if (count >= LauncherAppState.DEFAULT_FAVORITE_NUM) {
                        break;
                    }
                    for (AppInfo app : allApps) {
                        pn = app.componentName.getPackageName();
                        if (pn.equals(names.get(i))) {
                            num = (LauncherAppState.DEFAULT_FAVORITE_NUM - count) * 2;
                            values.clear();
                            values.put(Applications._ID, generateNewAppId());
                            values.put(BaseLauncherColumns.INTENT, app.intent.toUri(0));
                            values.put(Applications.LAUNCH_TIMES, num);
                            dbInsertAndCheck(this, db, TABLE_APPS, null, values);

                            count++;
                            break;
                        }
                    }
                }
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Got exception parsing default favorite.", e);
            } catch (IOException e) {
                Log.w(TAG, "Got exception parsing default favorite.", e);
            } catch (RuntimeException e) {
                Log.w(TAG, "Got exception parsing default favorite.", e);
            }
            // Update the max favorite id after we have loaded the database
            if (mMaxAppId == -1) {
                mMaxAppId = initializeMaxAppId(db);
            }
        }

    }

    /**
     * Build a query string that will match any row where the column matches anything in the values
     * list.
     */
    static String buildOrWhereString(String column, int[] values) {
        StringBuilder selectWhere = new StringBuilder();
        for (int i = values.length - 1; i >= 0; i--) {
            selectWhere.append(column).append("=").append(values[i]);
            if (i > 0) {
                selectWhere.append(" OR ");
            }
        }
        return selectWhere.toString();
    }

    static class SqlArguments {

        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
}
