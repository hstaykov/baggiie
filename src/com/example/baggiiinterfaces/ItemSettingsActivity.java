package com.example.baggiiinterfaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.baggie.ble.BluetoothLeService;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

public class ItemSettingsActivity extends Activity {

	private EditText editTexTitle;
	private ImageView imgBaggii;
	private ImageView btnDelete;
	private ImageView btnUpdate;
	private Spinner spinner;
	private Spinner spinnerImages;
	private Context context = this;
	private BaggiiDB db;
	private BaggiiItem currentBaggii;
	private BluetoothLeService mBluetoothLeService;
	private BluetoothAdapter mBluetoothAdapter;
	private static final int REQUEST_ENABLE_BT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_settings);

		Long itemId = getIntent().getLongExtra("ItemId", 0);
		db = new BaggiiDB(context);
		db.open();
		currentBaggii = db.getBaggiiById(itemId);
		Log.d("RIC", currentBaggii.toString());
		editTexTitle = (EditText) findViewById(R.id.editTextTitle);
		editTexTitle.setText(currentBaggii.getTitle());

		imgBaggii = (ImageView) findViewById(R.id.imgBaggii);
		btnDelete = (ImageView) findViewById(R.id.imgDelete);

		btnDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteBaggii();
			}
		});

		btnUpdate = (ImageView) findViewById(R.id.imgUpdate);

		btnUpdate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				updateBaggii();
			}
		});

		Resources res = this.getResources();
		if (currentBaggii.getPicture().equals("keys"))
			imgBaggii.setImageDrawable(res.getDrawable(R.drawable.keys));
		else if (currentBaggii.getPicture().equals("shoes")) {
			imgBaggii.setImageDrawable(res.getDrawable(R.drawable.shoes));
		} else if (currentBaggii.getPicture().equals("bag"))
			imgBaggii.setImageDrawable(res.getDrawable(R.drawable.bag));
		else if (currentBaggii.getPicture().equals("phone"))
			imgBaggii.setImageDrawable(res.getDrawable(R.drawable.phone));

		spinner = (Spinner) findViewById(R.id.spinnerDistance);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.distances_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		String[] distnaces = res.getStringArray(R.array.distances_array);
		List<String> distnacesList = new ArrayList<String>(
				Arrays.asList(distnaces));
		spinner.setAdapter(adapter);
		spinner.setSelection(distnacesList.indexOf(String.valueOf(currentBaggii
				.getDistnace())));

		spinnerImages = (Spinner) findViewById(R.id.spinnerImage);
		ArrayAdapter<CharSequence> adapterImages = ArrayAdapter
				.createFromResource(this, R.array.images_array,
						android.R.layout.simple_spinner_item);
		adapterImages
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		String[] images = res.getStringArray(R.array.images_array);
		List<String> imagesList = new ArrayList<String>(Arrays.asList(images));
		spinnerImages.setAdapter(adapterImages);
		spinnerImages.setSelection(imagesList.indexOf(currentBaggii
				.getPicture()));

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		gattServiceIntent.putExtra("baggieAddress", currentBaggii.getAddress());
		boolean connected = bindService(gattServiceIntent, mServiceConnection,
				BIND_AUTO_CREATE);
		startService(gattServiceIntent);

	}

	private void deleteBaggii() {

		Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra("ItemCreated", currentBaggii.getTitle()
				+ " was deleted...");

		mBluetoothLeService.disconnect(currentBaggii.getAddress());
		db.deleteBaggii(currentBaggii.getId());
		startActivity(intent);
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

			}
		}
	};

	private void updateBaggii() {
		BaggiiItem updatedBaggii = new BaggiiItem();
		updatedBaggii.setDistnace(Long.parseLong(spinner.getSelectedItem()
				.toString()));
		updatedBaggii.setId(currentBaggii.getId());
		updatedBaggii.setPicture((spinnerImages.getSelectedItem().toString()));
		updatedBaggii.setTitle(editTexTitle.getText().toString());

		if (db.updateBaggii(updatedBaggii)) {

			Intent intent = new Intent(context, MainActivity.class);
			intent.putExtra("ItemCreated", currentBaggii.getTitle()
					+ " was updated...");
			startActivity(intent);
		}
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				// finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(currentBaggii.getAddress());
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.item_settings, menu);
		return true;
	}

}
