package com.googlecalendar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class CalDatabase {

	//TAG for debugging
	public static final String TAG = "CalDatabase";

	//Singleton instance
	private static CalDatabase database;

	//database name
	public static String DATABASE_NAME = "Schedule.db";

	//table name for BOOK_INFO
	public static String TABLE_INFO = "Schedule";

    //version
	public static int DATABASE_VERSION = 1;

    //Helper class defined
    private DatabaseHelper dbHelper;

    //Database object
    private SQLiteDatabase db;

    private Context context;

    //Constructor
	private CalDatabase(Context context) {
		this.context = context;
	}

	public static CalDatabase getInstance(Context context) {
		if (database == null) {
			database = new CalDatabase(context);
		}
		return database;
	}


	//open database @return
    public boolean open() {
    	println("opening database [" + DATABASE_NAME + "].");
    	dbHelper = new DatabaseHelper(context);
    	db = dbHelper.getWritableDatabase();
    	return true;
    }

    //close database
    public void close() {
    	println("closing database [" + DATABASE_NAME + "].");
    	db.close();
    	database = null;
    }

    //execute raw query using the input SQL
	//close the cursor after fetching any result
    //@param SQL
    //@return
    public Cursor rawQuery(String SQL) {
		println("\nexecuteQuery called.\n");
		Cursor c1 = null;
		try {
			c1 = db.rawQuery(SQL, null);
			println("cursor count : " + c1.getCount());
		} catch(Exception ex) {
    		Log.e(TAG, "Exception in executeQuery", ex);
    	}
		return c1;
	}

    public boolean execSQL(String SQL) {
		println("\nexecute called.\n");
		try {
			Log.d(TAG, "SQL : " + SQL);
			db.execSQL(SQL);
	    } catch(Exception ex) {
			Log.e(TAG, "Exception in executeQuery", ex);
			return false;
		}
		return true;
	}




    private class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase _db) {
        	// TABLE_BOOK_INFO
        	println("creating table [" + TABLE_INFO + "].");

        	// drop existing table
        	String DROP_SQL = "drop table if exists " + TABLE_INFO;
        	try {
        		_db.execSQL(DROP_SQL);
        	} catch(Exception ex) {
        		Log.e(TAG, "Exception in DROP_SQL", ex);
        	}

        	// create table
        	String CREATE_SQL = "create table " + TABLE_INFO + "("
		        			+ "  _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
		        			+ "  schedule TEXT NOT NULL DEFAULT '제목없음', "
		        			+ "  address TEXT, "
		        			+ "  memo TEXT, "
							+ "  date1 TEXT NOT NULL DEFAULT '0000-00-00', "
							+ "  time1 TEXT, "
							+ "  date2 TEXT NOT NULL DEFAULT '0000-00-00', "
							+ "  time2 TEXT, "
							+ "  alarm TEXT NOT NULL DEFAULT '없음', "
							+ "  type INTEGER, "
							+ "  attendee TEXT"
		        			+ ")";
            try {
            	_db.execSQL(CREATE_SQL);
            } catch(Exception ex) {
        		Log.e(TAG, "Exception in CREATE_SQL", ex);
        	}
		}

        public void onOpen(SQLiteDatabase db) {
        	println("opened database [" + DATABASE_NAME + "].");

        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	println("Upgrading database from version " + oldVersion + " to " + newVersion + ".");

        	if (oldVersion < 2) {   // version 1

        	}

        }

		private void insertRecord(SQLiteDatabase _db, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8, int type, String s9) {
			try {
				_db.execSQL( "insert into " + TABLE_INFO + "(schedule, address, memo, date1, time1, date2, time2, alarm, type, attendee) values ('" + s1 + "', '" + s2  + "', '" + s3  + "', '" + s4 + "', '" + s5  + "', '" + s6 + "', '" + s7 + "', '" + s8 + "', " + type + ", '" + s9 + "');" );
			} catch(Exception ex) {
				Log.e(TAG, "Exception in executing insert SQL.", ex);
			}
		}

		private void updateRecord(SQLiteDatabase _db, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8, int type, String s9, int date3) {
			try {
				_db.execSQL( "update " + TABLE_INFO + " SET schedule='"+ s1 +"', address='"+ s2 +"', memo='"+ s3 +"', date1='"+ s4 +"', time1='"+ s5 +"', date2='"+ s6 +"', time2='"+ s7 +"', alarm='"+ s8 +"', type="+ type +", attendee='"+ s9 +"' WHERE _id=" + date3 + ";" );
			} catch(Exception ex) {
				Log.e(TAG, "Exception in executing insert SQL.", ex);
			}
		}

		private void deleteRecord(SQLiteDatabase _db, int id) {
			try {
				_db.execSQL( "delete FROM " + TABLE_INFO + " WHERE _id="+ id + ";" );
			} catch(Exception ex) {
				Log.e(TAG, "Exception in executing insert SQL.", ex);
			}
		}


    }

	public void insertRecord(String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8, int ty, String s9) {
		try {
			db.execSQL( "insert into " + TABLE_INFO + "(schedule, address, memo, date1, time1, date2, time2, alarm, type, attendee) values ('" + s1 + "', '" + s2  + "', '" + s3  + "', '" + s4  + "', '" + s5 + "', '" + s6 + "', '" + s7 + "', '" + s8 + "', " + ty + ", '" + s9 + "' );" );
		} catch(Exception ex) {
			Log.e(TAG, "Exception in executing insert SQL.", ex);
		}
	}

	public void updateRecord(String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8, int type, String s9, int date3) {
		try {
			db.execSQL( "update " + TABLE_INFO + " SET schedule='"+ s1 +"', address='"+ s2 +"', memo='"+ s3 +"', date1='"+ s4 +"', time1='"+ s5 +"', date2='"+ s6 +"', time2='"+ s7 +"', alarm='"+ s8 +"', type="+ type +", alarm='"+ s9 +"' WHERE _id=" + date3 + ";" );
		} catch(Exception ex) {
			Log.e(TAG, "Exception in executing insert SQL.", ex);
		}
	}

	public void deleteRecord(int id) {
		try {
			db.execSQL( "delete FROM " + TABLE_INFO + " WHERE _id="+ id + ";" );
		} catch(Exception ex) {
			Log.e(TAG, "Exception in executing insert SQL.", ex);
		}
	}

	public int dateinfo(String date) {
		int info = 0;
		try {
			Cursor cursor = db.rawQuery("select * from " + TABLE_INFO + " where _id=" + Integer.parseInt(date), null);
			info = cursor.getCount();
		} catch(Exception ex) {
			Log.e(TAG, "Exception in executing insert SQL.", ex);
		}
		return info;
	}

	public ArrayList<ListViewItem> selectDate(String date) {
		ArrayList<ListViewItem> result = new ArrayList<ListViewItem>();
		try {
			Cursor cursor = db.rawQuery("select * from " + TABLE_INFO + " where date1='" + date +"'", null);
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToNext();
				int id = cursor.getInt(0);					//id
				String sch = cursor.getString(1);
				String add = cursor.getString(2);
				String memo = cursor.getString(3);
				String date1 = cursor.getString(4);
				String time1 = cursor.getString(5);
				String date2 = cursor.getString(6);
				String time2 = cursor.getString(7);
				String al = cursor.getString(8);
				int ty = cursor.getInt(9);

				String dt = date1 +", " + time1;
				ListViewItem info = new ListViewItem(id, sch, dt, al, add);
				result.add(info);
			}
		} catch(Exception ex) {
			Log.e(TAG, "Exception in executing insert SQL.", ex);
		}

		return result;
	}


	public ArrayList<ListViewItem> selectAll(){
		ArrayList<ListViewItem> result = new ArrayList<ListViewItem>();

		try {
			Cursor cursor = db.rawQuery("select * from " + TABLE_INFO, null);
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToNext();
				int id = cursor.getInt(0);					//id
				String sch = cursor.getString(1);
				String add = cursor.getString(2);
				String memo = cursor.getString(3);
				String date1 = cursor.getString(4);
				String time1 = cursor.getString(5);
				String date2 = cursor.getString(6);
				String time2 = cursor.getString(7);
				String al = cursor.getString(8);
				int ty = cursor.getInt(9);

				String dt = date1 +", " + time1;
				ListViewItem info = new ListViewItem(id, sch, dt, al, add);
				result.add(info);
			}
		} catch(Exception ex) {
			Log.e(TAG, "Exception in executing insert SQL.", ex);
		}
		return result;
	}

	//id 값 증가시키기
	public void dateid() {
		try {
			Cursor cursor = db.rawQuery("select _id from " + TABLE_INFO + " where _id<10000", null);
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToNext();
				int id = cursor.getInt(0);					//id
				int setid = 11300;
				setid += id;
				db.execSQL( "update Schedule SET _id=" + setid + " WHERE _id=" + id + ";" );
			}
		} catch(Exception ex) {
			Log.e(TAG, "Exception in executing insert SQL.", ex);
		}
	}

	//id 값 찾기
	public int maxid() {
		int id = 0;
		try {
			Cursor cursor = db.rawQuery("select MAX(_id) AS id from " + TABLE_INFO, null);
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToNext();
				id = cursor.getInt(0);					//id
			}
		} catch(Exception ex) {
			Log.e(TAG, "Exception in executing insert SQL.", ex);
		}
		return id;
	}

    private void println(String msg) {
    	Log.d(TAG, msg);
    }


}
