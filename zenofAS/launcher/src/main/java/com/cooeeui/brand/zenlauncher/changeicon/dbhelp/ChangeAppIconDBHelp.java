package com.cooeeui.brand.zenlauncher.changeicon.dbhelp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChangeAppIconDBHelp extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "changeappicons";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_ICON_TITLE = "iconTitle";
    public static final String COLUMN_NAME_ICON_CHANGE_TYPE = "iconChangeType";
    public static final String COLUMN_NAME_ICON_POSITION = "iconPosition";
    public static final String COLUMN_NAME_ICON_PACKAGE = "iconPackage";
    public static final String COLUMN_NAME_ICON_TYPE = "iconType";
    public static final String COLUMN_NAME_ICON = "icon";

    private final String CREATE_TABLE_SQL = "CREATE TABLE changeappicons" +
                                            "(" +
                                            "_id INTEGER PRIMARY KEY autoincrement," +
                                            "iconTitle TEXT," +
                                            "iconChangeType INTEGER," +
                                            "iconPosition INTEGER NOT NULL DEFAULT 100," +
                                            "iconPackage TEXT," +
                                            "iconType TEXT," +
                                            "icon BLOB " +
                                            ")";

    public ChangeAppIconDBHelp(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
