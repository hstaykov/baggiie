package com.example.baggiiinterfaces;

import com.example.baggiiinterfaces.BaggiiItemContract.BaggiiEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BaggiiDbHelper extends SQLiteOpenHelper {

	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
			+ BaggiiEntry.TABLE_NAME + " (" + BaggiiEntry.COLUMN_NAME_ENTRY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP
			+ BaggiiEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP
			+ BaggiiEntry.COLUMN_NAME_IS_ACTIVE + INTEGER_TYPE + COMMA_SEP
			+ BaggiiEntry.COLUMN_NAME_DISTANCE + INTEGER_TYPE + COMMA_SEP
			+ BaggiiEntry.COLUMN_NAME_PICTURE + TEXT_TYPE + COMMA_SEP
			+ BaggiiEntry.COLUMN_NAME_ADDRESS + TEXT_TYPE + " );";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
			+ BaggiiEntry.TABLE_NAME;

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Baggii.db";

	public BaggiiDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public SQLiteDatabase open() {
		return getWritableDatabase();
	}

}
