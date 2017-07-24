package com.cooee.localsearch.history;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class DBOpenHelper extends SQLiteOpenHelper
{
	
	private static String DBNAME = "hr.db";
	
	public DBOpenHelper(
			Context context )
	{
		super( context , DBNAME , null , 1 );
	}
	
	public static final String HISTORY_RECORD = "history_record";
	
	@Override
	public void onCreate(
			SQLiteDatabase db )
	{
		// TODO Auto-generated method stub
		String sql = "create table " + HISTORY_RECORD + " (_id integer primary key autoincrement , name text );";
		db.execSQL( sql );
	}
	
	@Override
	public void onUpgrade(
			SQLiteDatabase db ,
			int oldVersion ,
			int newVersion )
	{
		// TODO Auto-generated method stub
	}
}
