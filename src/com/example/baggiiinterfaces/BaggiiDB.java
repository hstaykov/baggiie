package com.example.baggiiinterfaces;

import java.util.ArrayList;
import java.util.List;

import com.example.baggiiinterfaces.BaggiiItemContract.BaggiiEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BaggiiDB {
	private SQLiteDatabase db;
	private BaggiiDbHelper dbHelper;

	public BaggiiDB(Context context) {
		this.dbHelper = new BaggiiDbHelper(context);
	}

	public void open() {
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void addBaggii(BaggiiItem baggii) throws Exception {
		if (baggii == null) {
			throw new Exception("Inserting baggii error. Baggii is null.");
		}
		ContentValues values = new ContentValues();
		values.put(BaggiiEntry.COLUMN_NAME_TITLE, baggii.getTitle());
		values.put(BaggiiEntry.COLUMN_NAME_PICTURE, baggii.getPicture());
		values.put(BaggiiEntry.COLUMN_NAME_IS_ACTIVE, baggii.isActive());
		values.put(BaggiiEntry.COLUMN_NAME_DISTANCE, baggii.getDistnace());
		values.put(BaggiiEntry.COLUMN_NAME_ADDRESS, baggii.getAddress());

		db.insert(BaggiiEntry.TABLE_NAME, null, values);
		Log.d("RIC", "Baggii added to db...");
		this.getAllBaggiies();

	}

	public BaggiiItem getBaggiiById(Long id) {
		String[] projection = { BaggiiEntry.COLUMN_NAME_ENTRY_ID,
				BaggiiEntry.COLUMN_NAME_TITLE,
				BaggiiEntry.COLUMN_NAME_DISTANCE,
				BaggiiEntry.COLUMN_NAME_IS_ACTIVE,
				BaggiiEntry.COLUMN_NAME_PICTURE,
				BaggiiEntry.COLUMN_NAME_ADDRESS };

		Cursor cursor = db.query(BaggiiEntry.TABLE_NAME, projection,
				BaggiiEntry.COLUMN_NAME_ENTRY_ID + " = ?",
				new String[] { String.valueOf(id) }, null, null, null, null);

		if (cursor != null)
			cursor.moveToFirst();

		String itemId = cursor.getString(cursor
				.getColumnIndexOrThrow(BaggiiEntry.COLUMN_NAME_ENTRY_ID));
		String itemTitle = cursor.getString(cursor
				.getColumnIndexOrThrow(BaggiiEntry.COLUMN_NAME_TITLE));
		long isActive = cursor.getLong(cursor
				.getColumnIndexOrThrow(BaggiiEntry.COLUMN_NAME_IS_ACTIVE));
		long distance = cursor.getLong(cursor
				.getColumnIndexOrThrow(BaggiiEntry.COLUMN_NAME_DISTANCE));
		String pic = cursor.getString(cursor
				.getColumnIndexOrThrow(BaggiiEntry.COLUMN_NAME_PICTURE));
		String address = cursor.getString(cursor
				.getColumnIndex(BaggiiEntry.COLUMN_NAME_ADDRESS));

		BaggiiItem currentBaggii = new BaggiiItem();
		currentBaggii.setTitle(itemTitle);
		currentBaggii.setId(Long.parseLong(itemId));
		currentBaggii.setPicture(pic);
		currentBaggii.setDistnace(distance);
		currentBaggii.setActive(isActive == 1 ? true : false);
		currentBaggii.setAddress(address);
		return currentBaggii;

	}

	public Boolean updateBaggii(BaggiiItem baggii) {
		ContentValues values = new ContentValues();
		values.put(BaggiiEntry.COLUMN_NAME_TITLE, baggii.getTitle());
		values.put(BaggiiEntry.COLUMN_NAME_PICTURE, baggii.getPicture());
		values.put(BaggiiEntry.COLUMN_NAME_IS_ACTIVE, baggii.isActive());
		values.put(BaggiiEntry.COLUMN_NAME_DISTANCE, baggii.getDistnace());
		values.put(BaggiiEntry.COLUMN_NAME_ADDRESS, baggii.getAddress());
		return db
				.update(BaggiiEntry.TABLE_NAME,
						values,
						BaggiiEntry.COLUMN_NAME_ENTRY_ID + " = "
								+ baggii.getId(), null) == 0 ? false : true;
	}

	public List<BaggiiItem> getAllBaggiies() {
		List<BaggiiItem> baggiies = new ArrayList<BaggiiItem>();
		String[] allColumns = { BaggiiEntry.COLUMN_NAME_ENTRY_ID,
				BaggiiEntry.COLUMN_NAME_TITLE,
				BaggiiEntry.COLUMN_NAME_IS_ACTIVE,
				BaggiiEntry.COLUMN_NAME_DISTANCE,
				BaggiiEntry.COLUMN_NAME_PICTURE,
				BaggiiEntry.COLUMN_NAME_ADDRESS };

		Cursor cursor = db.query(BaggiiEntry.TABLE_NAME, allColumns, null,
				null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			BaggiiItem baggii = cursorToBaggii(cursor);
			baggiies.add(baggii);
			cursor.moveToNext();
		}
		cursor.close();
		return baggiies;
	}

	public boolean deleteBaggii(Long id) {
		return db.delete(BaggiiEntry.TABLE_NAME,
				BaggiiEntry.COLUMN_NAME_ENTRY_ID + "=" + id, null) > 0;
	}

	private BaggiiItem cursorToBaggii(Cursor cursor) {

		BaggiiItem baggii = new BaggiiItem();
		baggii.setId(cursor.getLong(0));
		baggii.setDistnace(cursor.getLong(3));
		baggii.setTitle(cursor.getString(1));
		baggii.setActive(cursor.getLong(2) == 1 ? true : false);
		baggii.setPicture(cursor.getString(4));
		baggii.setAddress(cursor.getString(5));
		return baggii;
	}

}
