/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baggie.ble;

import android.R.bool;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.baggiiinterfaces.LostActivity;
import com.example.baggiiinterfaces.MainActivity;
import com.example.baggiiinterfaces.R;

public class BluetoothLeService extends Service {
	private final static String TAG = BluetoothLeService.class.getSimpleName();

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
	public final static String SEND_BAGGIE_INFO = "com.example.bluetooth.le.SEND_BAGGIE_INFO";
	public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID
			.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

	public static final String BROADCAST_ACTION = "com.websmithing.broadcasttest.displayevent";
	private static final ScheduledExecutorService worker = Executors
			.newSingleThreadScheduledExecutor();
	// Implements callback methods for GATT events that the app cares about. For
	// example,
	// connection change and services discovered.
	Intent myItent = new Intent(SEND_BAGGIE_INFO);
	private static List<Integer> samples = new ArrayList<Integer>();
	private static int avgSample = 0;
	private SharedPreferences sharedPref;
	PendingIntent pendIntent;
	private boolean toNotify = true;

	private static void filter() {

		int AVERAGE_SAMPLE_COUNT = 10;

		for (int i = 0; i < samples.size() - AVERAGE_SAMPLE_COUNT; i++) {
			avgSample = 0;
			for (int a = 0; a < AVERAGE_SAMPLE_COUNT; a++) {
				avgSample += samples.get(i + a);
			}
			avgSample /= AVERAGE_SAMPLE_COUNT;
			samples.set(i, avgSample);
		}
	}

	private boolean isForeground(String myPackage) {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager
				.getRunningTasks(1);

		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		if (componentInfo.getClassName().equals(myPackage))
			return true;
		return false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String baggieAddress = intent.getStringExtra("baggieAddress");
		Log.d("RIC", "Starting service");
		initialize();
		connect(baggieAddress);

		Intent in = new Intent(this, MainActivity.class);
		in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		pendIntent = PendingIntent.getActivity(this, 0, in, 0);

		Notification noti = new Notification.Builder(this)
				.setContentTitle("Baggie notification")
				.setContentText("Your baggie service is running...")
				.setSmallIcon(R.drawable.baggii).build();
		startForeground(123, noti);
		return Service.START_REDELIVER_INTENT;
	}

	private void showNotification() {

		Uri alarmSound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		Notification noti = new Notification.Builder(this)
				.setContentTitle("Baggie notification")
				.setContentText("Your baggie is gone")
				.setSmallIcon(R.drawable.baggii).setSound(alarmSound).build();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notificationManager.notify("asd", 123, noti);
	}

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			myItent.putExtra("BAGGIE", String.valueOf(rssi));

			samples.add(rssi);
			filter();
			myItent.putExtra("BAGGIEFILTER", String.valueOf(avgSample));
			Log.d("RIC", "Addres: " + mBluetoothDeviceAddress + " Avg Sample"
					+ String.valueOf(avgSample) + " Notify " + toNotify);
			sharedPref = getBaseContext().getSharedPreferences("myPrefs",
					MODE_PRIVATE);
			// boolean toNotify = sharedPref.getBoolean("toNotify", true);

			if (avgSample * -1 > 75 && toNotify) {
				// if
				// (!isForeground("com.example.baggiiinterfaces.LostActivity"))
				// {

				showNotification();
				// Intent intent = new Intent(getBaseContext(),
				// LostActivity.class);
				// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// getApplication().startActivity(intent);

				samples.clear();
				avgSample = 0;
				toNotify = false;
				Runnable task = new Runnable() {
					public void run() {
						toNotify = true;
					}
				};
				worker.schedule(task, 25, TimeUnit.SECONDS);
				// }
			}
			sendBroadcast(myItent);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			String intentAction;
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED;
				mConnectionState = STATE_CONNECTED;
				broadcastUpdate(intentAction);
				Log.i(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				Log.i(TAG, "Attempting to start service discovery:"
						+ mBluetoothGatt.discoverServices());
				TimerTask task = new TimerTask() {
					@Override
					public void run() {
						if (mBluetoothGatt != null)
							mBluetoothGatt.readRemoteRssi();
					}
				};
				Timer mRssiTimer = new Timer();
				mRssiTimer.schedule(task, 1000, 1000);

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
			
				Log.i(TAG, "Disconnected from GATT server.");
				broadcastUpdate(intentAction);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}

	};

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action,
			final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);

		// This is special handling for the Heart Rate Measurement profile. Data
		// parsing is
		// carried out as per profile specifications:
		// http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
		if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
			int flag = characteristic.getProperties();
			int format = -1;
			if ((flag & 0x01) != 0) {
				format = BluetoothGattCharacteristic.FORMAT_UINT16;
				Log.d(TAG, "Heart rate format UINT16.");
			} else {
				format = BluetoothGattCharacteristic.FORMAT_UINT8;
				Log.d(TAG, "Heart rate format UINT8.");
			}
			final int heartRate = characteristic.getIntValue(format, 1);
			Log.d(TAG, String.format("Received heart rate: %d", heartRate));
			intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
		} else {
			// For all other profiles, writes the data formatted in HEX.
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(
						data.length);
				for (byte byteChar : data)
					stringBuilder.append(String.format("%02X ", byteChar));
				intent.putExtra(EXTRA_DATA, new String(data) + "\n"
						+ stringBuilder.toString());
			}
		}
		sendBroadcast(intent);
	}

	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that
		// BluetoothGatt.close() is called
		// such that resources are cleaned up properly. In this particular
		// example, close() is
		// invoked when the UI is disconnected from the Service.
		// close();
		// return super.onUnbind(intent);
		return false;
	}

	private final IBinder mBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 * 
	 * @param address
	 *            The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			Log.w(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device. Try to reconnect.
		if (mBluetoothDeviceAddress != null
				&& address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.d(TAG,
					"Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				mConnectionState = STATE_CONNECTING;
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
		Log.d(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		mConnectionState = STATE_CONNECTING;
		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect(String address) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		// final BluetoothDevice device = mBluetoothAdapter
		// .getRemoteDevice(address);
		// if (device == null) {
		// Log.w(TAG, "Device not found.  Unable to connect.");
		// }
		// // We want to directly connect to the device, so we are setting the
		// // autoConnect
		// // parameter to false.
		// mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
		mBluetoothGatt.disconnect();
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	// /**
	// * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	// * result is reported asynchronously through the
	// * {@code
	// BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt,
	// android.bluetooth.BluetoothGattCharacteristic, int)}
	// * callback.
	// *
	// * @param characteristic
	// * The characteristic to read from.
	// */
	// public void readCharacteristic(BluetoothGattCharacteristic
	// characteristic) {
	// if (mBluetoothAdapter == null || mBluetoothGatt == null) {
	// Log.w(TAG, "BluetoothAdapter not initialized");
	// return;
	// }
	// mBluetoothGatt.readCharacteristic(characteristic);
	// }

	// /**
	// * Enables or disables notification on a give characteristic.
	// *
	// * @param characteristic
	// * Characteristic to act on.
	// * @param enabled
	// * If true, enable notification. False otherwise.
	// */
	// public void setCharacteristicNotification(
	// BluetoothGattCharacteristic characteristic, boolean enabled) {
	// if (mBluetoothAdapter == null || mBluetoothGatt == null) {
	// Log.w(TAG, "BluetoothAdapter not initialized");
	// return;
	// }
	// mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
	//
	// // This is specific to Heart Rate Measurement.
	// if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
	// BluetoothGattDescriptor descriptor = characteristic
	// .getDescriptor(UUID
	// .fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
	// descriptor
	// .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	// mBluetoothGatt.writeDescriptor(descriptor);
	// }
	// }

	// /**
	// * Retrieves a list of supported GATT services on the connected device.
	// This
	// * should be invoked only after {@code BluetoothGatt#discoverServices()}
	// * completes successfully.
	// *
	// * @return A {@code List} of supported services.
	// */
	// public List<BluetoothGattService> getSupportedGattServices() {
	// if (mBluetoothGatt == null)
	// return null;
	// return mBluetoothGatt.getServices();
	// }

}
