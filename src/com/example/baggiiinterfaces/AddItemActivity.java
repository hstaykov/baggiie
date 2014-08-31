package com.example.baggiiinterfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.datatype.Duration;

import com.example.baggiiinterfaces.BaggiiItemContract.BaggiiEntry;
import com.baggie.ble.SampleGattAttributes;
import com.baggie.ble.BluetoothLeService;

import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddItemActivity extends Activity {
	private boolean myConnState = false;

	private Context currentContext = this;
	private BluetoothAdapter bluetoothAdapter;
	private ListView listDevices;
	private ArrayAdapter<String> listBluetoothAdapter;
	private static final int REQUEST_ENABLE_BT = 1;
	private ImageView btnFind;
	private EditText txtName;
	private ImageView btnAdd;
	private Spinner spinner;
	private Spinner spinnerImages;

	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;

	private String mDeviceAddress;
	private ExpandableListView mGattServicesList;
	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";

	private TextView mConnectionState;
	private TextView mDataField;
	private TextView mAddressText;
	private String mDeviceName;
	private String picturePath;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private boolean baggieAdded = false;
	private Resources res;
	private MenuAdapter menuAdapter;
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;
	static final int RESULT_LOAD_IMAGE = 1;
	static final int REQUEST_IMAGE_CAPTURE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_item);
		res = this.getResources();

		// Code for drawer...
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		ArrayList<MenuItem> menuItems = MenuHelper.getMenuItems();
		menuAdapter = new MenuAdapter(getApplicationContext(), menuItems);

		mDrawerList.setAdapter(menuAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		// End of drawer code...

		mAddressText = ((TextView) findViewById(R.id.device_address));

		listDevices = (ListView) findViewById(R.id.list_bluetooth_devs);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		listBluetoothAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);

		txtName = (EditText) findViewById(R.id.txtName);

		if (!bluetoothAdapter.isEnabled()) {
			Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(turnOn, 0);
			Toast.makeText(getApplicationContext(), "Bluetooth turned on",
					Toast.LENGTH_LONG).show();
		}
		listDevices.setAdapter(listBluetoothAdapter);
		listDevices.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final BluetoothDevice device = mLeDeviceListAdapter
						.getDevice(0);
				if (device == null)
					return;

				mDeviceName = device.getName();
				mDeviceAddress = device.getAddress();

				mConnectionState = (TextView) findViewById(R.id.connection_state);
				mDataField = (TextView) findViewById(R.id.data_value);

				Intent gattServiceIntent = new Intent(currentContext,
						BluetoothLeService.class);
				gattServiceIntent.putExtra("baggieAddress", mDeviceAddress);

				bindService(gattServiceIntent, mServiceConnection,
						BIND_AUTO_CREATE);
				startService(gattServiceIntent);
				myConnState = true;

				registerReceiver(mGattUpdateReceiver,
						makeGattUpdateIntentFilter());

				listDevices.setVisibility(View.GONE);

				// mAddressText.setText("Address: " + mDeviceAddress + " Name: "
				// + mDeviceName);
				// addBaggii(mDeviceAddress);
				findViewById(R.id.layout_find_item).setVisibility(View.GONE);
				findViewById(R.id.layout_add_item).setVisibility(View.VISIBLE);

			}
		});

		findViewById(R.id.btnAddItem).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addBaggii(mDeviceAddress);
				finish();
			}
		});

		findViewById(R.id.btn_choose_gallery).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent i = new Intent(
								Intent.ACTION_PICK,
								android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

						startActivityForResult(i, RESULT_LOAD_IMAGE);

					}
				});
		findViewById(R.id.btn_take_photo).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						dispatchTakePictureIntent();

					}
				});

		// spinnerImages = (Spinner) findViewById(R.id.spinnerImage);
		// ArrayAdapter<CharSequence> adapterImages = ArrayAdapter
		// .createFromResource(this, R.array.images_array,
		// android.R.layout.simple_spinner_item);
		// adapterImages
		// .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// spinnerImages.setAdapter(adapterImages);
		//
		// spinner = (Spinner) findViewById(R.id.spinnerDistance);
		// ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		// this, R.array.distances_array,
		// android.R.layout.simple_spinner_item);
		// adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// spinner.setAdapter(adapter);

		mHandler = new Handler();

		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_item, menu);
		return true;
	}

	private void find(View view) {
		mLeDeviceListAdapter.clear();
		scanLeDevice(true);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			picturePath = cursor.getString(columnIndex);
			cursor.close();

			ImageView imageView = (ImageView) findViewById(R.id.add_baggie_pic);
			imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

		}

		// if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
		// {
		// Bundle extras = data.getExtras();
		// Bitmap imageBitmap = (Bitmap) extras.get("data");
		// ImageView imageView = (ImageView) findViewById(R.id.add_baggie_pic);
		// imageView.setImageBitmap(imageBitmap);
		// }

		super.onActivityResult(requestCode, resultCode, data);
	}

	final BroadcastReceiver bReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// add the name and the MAC address of the object to the
				// arrayAdapter
				// int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,
				// Short.MIN_VALUE);
				// device.get
				listBluetoothAdapter.add(device.getName() + "\n"
						+ device.getAddress());
				listBluetoothAdapter.notifyDataSetChanged();
			}
		}
	};

	private void addBaggii(String address) {

		BaggiiDB baggiiDb = new BaggiiDB(this);
		baggiiDb.open();

		BaggiiItem baggii = new BaggiiItem();
		baggii.setTitle(txtName.getText().toString());
		baggii.setActive(true);
		baggii.setDistnace(90);
		baggii.setPicture(picturePath);
		baggii.setAddress(address);
		try {
			baggiiDb.addBaggii(baggii);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("ItemCreated",
					"Your new Baggii " + baggii.getTitle() + " was created");
			startActivity(intent);

			unregisterReceiver(mGattUpdateReceiver);
			unbindService(mServiceConnection);
			baggieAdded = true;
			finish();
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (!baggieAdded && mBluetoothLeService != null)
			mBluetoothLeService.disconnect(mDeviceAddress);

	}

	// //////////////////////////////////////////////////////////////////

	@Override
	protected void onResume() {
		super.onResume();

		// Ensures Bluetooth is enabled on the device. If Bluetooth is not
		// currently enabled,
		// fire an intent to display a dialog asking the user to grant
		// permission to enable it.
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		if (myConnState) {
			registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
			if (mBluetoothLeService != null) {
				final boolean result = mBluetoothLeService
						.connect(mDeviceAddress);
			}
		}

		// Initializes list view adapter.
		mLeDeviceListAdapter = new LeDeviceListAdapter();
		listDevices.setAdapter(mLeDeviceListAdapter);
		scanLeDevice(true);

	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		mLeDeviceListAdapter.clear();
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();
	}

	// Adapter for holding devices found through scanning.
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private ArrayList<Integer> mLeDevicesRssi;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mLeDevicesRssi = new ArrayList<Integer>();
			mInflator = AddItemActivity.this.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device, int rssi) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
				mLeDevicesRssi.add(rssi);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView) view
						.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view
						.findViewById(R.id.device_name);
				viewHolder.deviceSignal = (ImageView) view
						.findViewById(R.id.device_signal);

				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = mLeDevices.get(i);

			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText(R.string.unknown_device);
			viewHolder.deviceAddress.setText(device.getAddress());
			int strength = mLeDevicesRssi.get(i);

			if (strength * -1 > 0 && strength * -1 < 60) {
				viewHolder.deviceSignal.setImageDrawable(res
						.getDrawable(R.drawable.signal_3));
			}
			if (strength * -1 > 60 && strength * -1 < 85) {
				viewHolder.deviceSignal.setImageDrawable(res
						.getDrawable(R.drawable.signal_2));

			}
			if (strength * -1 > 85) {
				viewHolder.deviceSignal.setImageDrawable(res
						.getDrawable(R.drawable.singal_1));

			}

			return view;
		}
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// Log.d("RIC", String.valueOf(rssi));
					mLeDeviceListAdapter.addDevice(device, rssi);
					mLeDeviceListAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		ImageView deviceSignal;
	}

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				updateConnectionState(R.string.connected);
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				mConnected = false;
				updateConnectionState(R.string.disconnected);
				invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				// Show all the supported services and characteristics on the
				// user interface.
				// displayGattServices(mBluetoothLeService
				// .getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayData(intent
						.getStringExtra(BluetoothLeService.EXTRA_DATA));
			} else if (BluetoothLeService.SEND_BAGGIE_INFO.equals(action)) {
				// Log.d("RIC", intent.getStringExtra("BAGGIE"));
				if ((Integer.parseInt(intent.getStringExtra("BAGGIEFILTER")) * -1) > 99) {

					Toast.makeText(getApplicationContext(),
							"Baggie out of range...", Toast.LENGTH_LONG).show();
				}
				mDataField
						.setText("Real : " + intent.getStringExtra("BAGGIE")
								+ " Filtered: "
								+ intent.getStringExtra("BAGGIEFILTER"));
			}
		}
	};

	// // If a given GATT characteristic is selected, check for supported
	// features.
	// // This sample
	// // demonstrates 'Read' and 'Notify' features. See
	// // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html
	// for
	// // the complete
	// // list of supported characteristic features.
	// private final ExpandableListView.OnChildClickListener
	// servicesListClickListner = new ExpandableListView.OnChildClickListener()
	// {
	// @Override
	// public boolean onChildClick(ExpandableListView parent, View v,
	// int groupPosition, int childPosition, long id) {
	// if (mGattCharacteristics != null) {
	// final BluetoothGattCharacteristic characteristic = mGattCharacteristics
	// .get(groupPosition).get(childPosition);
	// final int charaProp = characteristic.getProperties();
	// if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
	// // If there is an active notification on a characteristic,
	// // clear
	// // it first so it doesn't update the data field on the user
	// // interface.
	// if (mNotifyCharacteristic != null) {
	// mBluetoothLeService.setCharacteristicNotification(
	// mNotifyCharacteristic, false);
	// mNotifyCharacteristic = null;
	// }
	// mBluetoothLeService.readCharacteristic(characteristic);
	// }
	// if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
	// mNotifyCharacteristic = characteristic;
	// mBluetoothLeService.setCharacteristicNotification(
	// characteristic, true);
	// }
	// return true;
	// }
	// return false;
	// }
	// };

	private void clearUI() {
		// mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
		// mDataField.setText(R.string.no_data);
	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState.setText(resourceId);
				mConnectionState.setTextColor(Color.GREEN);
				findViewById(R.id.btnAddItem).setVisibility(View.VISIBLE);
			}
		});
	}

	private void displayData(String data) {
		if (data != null) {
			mDataField.setText(data);
		}
	}

	// Demonstrates how to iterate through the supported GATT
	// Services/Characteristics.
	// In this sample, we populate the data structure that is bound to the
	// ExpandableListView
	// on the UI.
	// private void displayGattServices(List<BluetoothGattService> gattServices)
	// {
	// if (gattServices == null)
	// return;
	// String uuid = null;
	// String unknownServiceString = getResources().getString(
	// R.string.unknown_service);
	// String unknownCharaString = getResources().getString(
	// R.string.unknown_characteristic);
	// ArrayList<HashMap<String, String>> gattServiceData = new
	// ArrayList<HashMap<String, String>>();
	// ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData =
	// new ArrayList<ArrayList<HashMap<String, String>>>();
	// mGattCharacteristics = new
	// ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	//
	// // Loops through available GATT Services.
	// for (BluetoothGattService gattService : gattServices) {
	// HashMap<String, String> currentServiceData = new HashMap<String,
	// String>();
	// uuid = gattService.getUuid().toString();
	// currentServiceData.put(LIST_NAME,
	// SampleGattAttributes.lookup(uuid, unknownServiceString));
	// currentServiceData.put(LIST_UUID, uuid);
	// gattServiceData.add(currentServiceData);
	//
	// ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new
	// ArrayList<HashMap<String, String>>();
	// List<BluetoothGattCharacteristic> gattCharacteristics = gattService
	// .getCharacteristics();
	// ArrayList<BluetoothGattCharacteristic> charas = new
	// ArrayList<BluetoothGattCharacteristic>();
	//
	// // Loops through available Characteristics.
	// for (BluetoothGattCharacteristic gattCharacteristic :
	// gattCharacteristics) {
	// charas.add(gattCharacteristic);
	// HashMap<String, String> currentCharaData = new HashMap<String, String>();
	// uuid = gattCharacteristic.getUuid().toString();
	// currentCharaData.put(LIST_NAME,
	// SampleGattAttributes.lookup(uuid, unknownCharaString));
	// currentCharaData.put(LIST_UUID, uuid);
	// gattCharacteristicGroupData.add(currentCharaData);
	// }
	// mGattCharacteristics.add(charas);
	// gattCharacteristicData.add(gattCharacteristicGroupData);
	// }
	//
	// SimpleExpandableListAdapter gattServiceAdapter = new
	// SimpleExpandableListAdapter(
	// this, gattServiceData,
	// android.R.layout.simple_expandable_list_item_2, new String[] {
	// LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
	// android.R.id.text2 }, gattCharacteristicData,
	// android.R.layout.simple_expandable_list_item_2, new String[] {
	// LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
	// android.R.id.text2 });
	// mGattServicesList.setAdapter(gattServiceAdapter);
	// }

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
