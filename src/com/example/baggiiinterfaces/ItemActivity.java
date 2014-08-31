package com.example.baggiiinterfaces;

import java.util.ArrayList;

import com.baggie.ble.BluetoothLeService;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ItemActivity extends Activity {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private MenuAdapter menuAdapter;

	private TextView mDebugText;
	private ImageView mImageSignal;
	private Resources res;
	private ImageView mImageCircle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item1);
		res = this.getResources();
		mImageCircle = (ImageView) findViewById(R.id.imgCircle);
		mImageSignal = (ImageView) findViewById(R.id.img_signal);

		findViewById(R.id.btnHot).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				findViewById(R.id.itemButtons).setVisibility(View.GONE);
				findViewById(R.id.layoutCircle).setVisibility(View.VISIBLE);
			}
		});
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

		Long itemId = getIntent().getLongExtra("ItemId", 0);
		TextView txtItemName = (TextView) findViewById(R.id.txt_searched_item);
		BaggiiDB db = new BaggiiDB(this);
		db.open();
		final BaggiiItem currentBaggii = db.getBaggiiById(itemId);
		// BitmapDrawable back = new BitmapDrawable(currentBaggii.getPicture());

		BitmapDrawable back;
		// Resources res = this.getResources();
		if (currentBaggii.getPicture() != null) {
			back = new BitmapDrawable(currentBaggii.getPicture());
		} else {
			back = (BitmapDrawable) res.getDrawable(R.drawable.baggii);
		}
		findViewById(R.id.rel_item_background).setBackground(back);

		// Code for drawer...
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		ArrayList<MenuItem> menuItems = MenuHelper.getMenuItems();
		menuAdapter = new MenuAdapter(getApplicationContext(), menuItems);

		mDrawerList.setAdapter(menuAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		// End of drawer code...

		mDebugText = (TextView) findViewById(R.id.data_value);

	}

	private void changeSignalView(int strength) {
		if (strength * -1 > 0 && strength * -1 < 60) {
			mImageSignal.setImageDrawable(res.getDrawable(R.drawable.signal_3));
		}
		if (strength * -1 > 60 && strength * -1 < 85) {
			mImageSignal.setImageDrawable(res.getDrawable(R.drawable.signal_2));

		}
		if (strength * -1 > 85) {
			mImageSignal.setImageDrawable(res.getDrawable(R.drawable.singal_1));

		}
		mImageSignal.refreshDrawableState();
	}

	private void changeSignalCircleView(int strength) {
		if (strength * -1 > 0 && strength * -1 < 45) {
			mImageCircle.setImageDrawable(res.getDrawable(R.drawable.circle11));
		}
		if (strength * -1 > 45 && strength * -1 < 55) {
			mImageCircle.setImageDrawable(res.getDrawable(R.drawable.circle12));
		}
		if (strength * -1 > 55 && strength * -1 < 65) {
			mImageCircle.setImageDrawable(res.getDrawable(R.drawable.circle13));
		}
		if (strength * -1 > 65 && strength * -1 < 75) {
			mImageCircle.setImageDrawable(res.getDrawable(R.drawable.circle14));
		}
		if (strength * -1 > 75 && strength * -1 < 95) {
			mImageCircle.setImageDrawable(res.getDrawable(R.drawable.circle15));
		}
		if (strength * -1 > 95) {
			mImageCircle.setImageDrawable(res.getDrawable(R.drawable.circle));
		}
	}

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.SEND_BAGGIE_INFO.equals(action)) {
				// Log.d("RIC", intent.getStringExtra("BAGGIE"));
				if ((Integer.parseInt(intent.getStringExtra("BAGGIEFILTER")) * -1) > 99) {
					Toast.makeText(getApplicationContext(),
							"Baggie out of range...", Toast.LENGTH_LONG).show();
				}
				mDebugText.setText(" Filtered: "
						+ intent.getStringExtra("BAGGIEFILTER"));
				changeSignalView(Integer.parseInt(intent
						.getStringExtra("BAGGIEFILTER")));
				changeSignalCircleView(Integer.parseInt(intent
						.getStringExtra("BAGGIEFILTER")));
				// Log.d("RIC",
				// (" Filtered: " + intent.getStringExtra("BAGGIEFILTER")));
			}
		}
	};

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.SEND_BAGGIE_INFO);
		return intentFilter;
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
			intent = new Intent(this, MainActivity.class);
			finish();
			startActivity(intent);
			break;
		case 2:
			mDrawerLayout.closeDrawers();
			intent = new Intent(this, AddItemActivity.class);
			finish();
			startActivity(intent);
			break;
		case 3:
			break;
		case 4:
			mDrawerLayout.closeDrawers();
			intent = new Intent(this, LoginActivity.class);
			finish();
			startActivity(intent);
			break;
		}
	}

}
