package com.alicegary.hackersonthego;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "Hackers";   
    private static final String TABLE_NAME = "messages";
    private static final int 	DATABASE_VERSION = 2;
    
    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (messageid INTEGER PRIMARY KEY, readflag TEXT, sendername TEXT, senderid TEXT, time TEXT, preview TEXT, message TEXT);");
    }

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) { }
	
	public void addRow(int messageid, String readflag, String sendername, String senderid, String time, String preview, String message) {
		ContentValues cv = new ContentValues();
    	cv.put("messageid",  messageid);
		cv.put("readflag", readflag);
    	cv.put("sendername", sendername);
    	cv.put("senderid", senderid);
    	cv.put("time", time);
    	cv.put("preview", preview);
    	cv.put("message", message);
    	this.getWritableDatabase().insert(TABLE_NAME, null, cv);
	}
	
	public String getSenderByMsgID(String in_id) {
		String the_sender = "";
		Cursor c = null;
		try {
			c = this.getReadableDatabase().rawQuery("SELECT sendername FROM " + TABLE_NAME + " WHERE messageid = " + in_id, null);
			c.moveToFirst();
			the_sender = c.getString(0);
		}
		catch (Exception e) { MainApplication.alertbox("SQL ERROR", e.getMessage()); }
		
		return the_sender;
	}
	
	public String getTimeByMsgID(String in_id) {
		String the_time = "";
		Cursor c = null;
		try {
			c = this.getReadableDatabase().rawQuery("SELECT time FROM " + TABLE_NAME + " WHERE messageid = " + in_id, null);
			c.moveToFirst();
			the_time = c.getString(0);
		}
		catch (Exception e) { MainApplication.alertbox("SQL ERROR", e.getMessage()); }
		
		return the_time;		
	}
	
	public void markMessageRead(String in_id) {
		Cursor c = null;
		ContentValues cv = new ContentValues();
    	cv.put("readflag",  "[-]");
    	try {
			this.getWritableDatabase().update(TABLE_NAME, cv, "messageid = " + in_id, null);
		}
		catch (Exception e) { MainApplication.alertbox("SQL ERROR", e.getMessage()); }
	}
	
	public void emptyTable() {
		this.getWritableDatabase().delete(TABLE_NAME, null, null);
	}
}
