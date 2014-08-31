package com.example.baggiiinterfaces;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private TextView baggiiTextView;
	private TextView addItemTextView;
	private RelativeLayout rel;
	private ListView itemsList;
	private List<BaggiiItem> items;
	private BaggiiDB db;
	private ToggleButton btnToggleNotifications;
	private SharedPreferences sharedPref;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;

	private MenuAdapter menuAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		db = new BaggiiDB(this);

		// Code for drawer...
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		ArrayList<MenuItem> menuItems = MenuHelper.getMenuItems();
		menuAdapter = new MenuAdapter(getApplicationContext(), menuItems);

		mDrawerList.setAdapter(menuAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		// End of drawer code...

		String incomingMessageFromOtherActivity = this.getIntent()
				.getStringExtra("ItemCreated");
		if (incomingMessageFromOtherActivity != null) {
			Toast.makeText(this, incomingMessageFromOtherActivity,
					Toast.LENGTH_LONG).show();
		}

		// baggiiTextView = (TextView) findViewById(R.id.textViewBaggii);
		// baggiiTextView.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Intent intent = new Intent(MainActivity.this,
		// LostActivity.class);
		// startActivity(intent);
		//
		// }
		// });

		// addItemTextView = (TextView) findViewById(R.id.txtAddItem);
		findViewById(R.id.btnAdd).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						AddItemActivity.class);
				startActivity(intent);
			}
		});

		items = createItemsList();
		itemsList = (ListView) findViewById(R.id.list_view_items);
		itemsList.setAdapter(new ItemAdapter(this, R.layout.list_item_layout,
				1, items));
		itemsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(MainActivity.this,
						ItemActivity.class);
				intent.putExtra("ItemId", items.get(arg2).getId());
				//finish();
				startActivity(intent);

			}
		});

		// btnToggleNotifications = (ToggleButton)
		// findViewById(R.id.btnToggleNotify);
		sharedPref = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
		boolean toNotify = sharedPref.getBoolean("toNotify", false);

		if (btnToggleNotifications != null) {
			if (!toNotify) {
				btnToggleNotifications.setChecked(false);
			} else {
				btnToggleNotifications.setChecked(true);
			}
		}

	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		Intent intent;
		switch (position) {
		case 0:
			break;
		case 1:
			mDrawerLayout.closeDrawers();
			intent = new Intent(MainActivity.this, MainActivity.class);
			finish();
			startActivity(intent);
			break;
		case 2:
			mDrawerLayout.closeDrawers();
			intent = new Intent(MainActivity.this, AddItemActivity.class);
			finish();
			startActivity(intent);
			break;
		case 3:
			break;
		case 4:
			mDrawerLayout.closeDrawers();
			intent = new Intent(MainActivity.this, LoginActivity.class);
			finish();
			startActivity(intent);
			break;
		}
	}

	public void onToggleClicked(View view) {
		// Is the toggle on?
		boolean on = btnToggleNotifications.isChecked();
		SharedPreferences.Editor editor = sharedPref.edit();
		if (on) {
			editor.putBoolean("toNotify", true);
		} else {
			editor.putBoolean("toNotify", false);
		}
		editor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private List<BaggiiItem> createItemsList() {
		List<BaggiiItem> outItems = new ArrayList<BaggiiItem>();

		db.open();
		outItems = db.getAllBaggiies();
		db.close();

		return outItems;
	}
}
