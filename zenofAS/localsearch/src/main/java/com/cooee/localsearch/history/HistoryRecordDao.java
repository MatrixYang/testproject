package com.cooee.localsearch.history;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


public class HistoryRecordDao
{
	
	private Context context;
	private DBOpenHelper helper;
	
	public HistoryRecordDao(
			Context context )
	{
		// TODO Auto-generated constructor stub
		this.context = context;
		helper = new DBOpenHelper( context );
	}
	
	/*
	 * 插入最近搜索
	 */
	public long insert(
			String name )
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put( "name" , name );
		long result = db.insert( DBOpenHelper.HISTORY_RECORD , "_id" , values );
		db.close();
		return result;
	}
	
	public long delete(
			String name )
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		String where = "name" + " = ?";
		String[] whereValue = { name };
		long result = db.delete( DBOpenHelper.HISTORY_RECORD , where , whereValue );
		db.close();
		return result;
	}
	
	public int getCount()
	{
		int count = 0;
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cr = db.query( DBOpenHelper.HISTORY_RECORD , null , null , null , null , null , null );
		count = cr.getCount();
		cr.close();
		db.close();
		return count;
	}
	
	/*
	 * 查询所有最近搜索的
	 */
	public List<String> queryAllSearch()
	{
		int count = 0;
		List<String> names = new ArrayList<String>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query( DBOpenHelper.HISTORY_RECORD , null , null , null , null , null , "_id desc" );
		while( cursor.moveToNext() )
		{
			if( count >= 10 )
			{
				cursor.close();
				db.close();
				return names;
			}
			else
			{
				String name = cursor.getString( cursor.getColumnIndex( "name" ) );
				names.add( name );
			}
			count++;
		}
		cursor.close();
		db.close();
		return names;
	}
	
	/*
	 * 清空所有的记录
	 */
	public void clearAllRecord()
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete( DBOpenHelper.HISTORY_RECORD , null , null );
		db.close();
	}
}
