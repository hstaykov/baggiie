package com.example.baggiiinterfaces;

import android.provider.BaseColumns;

public final class BaggiiItemContract {
	public BaggiiItemContract() {
	}

	public static abstract class BaggiiEntry implements BaseColumns {
		public static final String TABLE_NAME = "baggiies";
		public static final String COLUMN_NAME_ENTRY_ID = "baggiiId";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_IS_ACTIVE = "isActive";
		public static final String COLUMN_NAME_DISTANCE = "distance";
		public static final String COLUMN_NAME_PICTURE = "picture";
		public static final String COLUMN_NAME_ADDRESS = "address";
		
	}
}
