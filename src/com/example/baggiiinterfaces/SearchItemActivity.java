package com.example.baggiiinterfaces;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchItemActivity extends Activity {

	private ImageView imgItem;
	private ImageView btnShowSettings;
	private Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_item);
		Long itemId = getIntent().getLongExtra("ItemId", 0);
		TextView txtItemName = (TextView) findViewById(R.id.txt_searched_item);
		BaggiiDB db = new BaggiiDB(this);
		db.open();
		final BaggiiItem currentBaggii = db.getBaggiiById(itemId);
		txtItemName.setText("Find my  " + currentBaggii.getTitle());

		imgItem = (ImageView) findViewById(R.id.btnLogin);
		Resources res = this.getResources();
		if (currentBaggii.getPicture().equals("keys"))
			imgItem.setImageDrawable(res.getDrawable(R.drawable.keys));
		else if (currentBaggii.getPicture().equals("shoes")) {
			imgItem.setImageDrawable(res.getDrawable(R.drawable.shoes));
		} else if (currentBaggii.getPicture().equals("bag"))
			imgItem.setImageDrawable(res.getDrawable(R.drawable.bag));
		else if (currentBaggii.getPicture().equals("phone"))
			imgItem.setImageDrawable(res.getDrawable(R.drawable.phone));

		btnShowSettings = (ImageView) findViewById(R.id.imgBtnShowItemSettings);
		btnShowSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, ItemSettingsActivity.class);
				intent.putExtra("ItemId", currentBaggii.getId());
				startActivity(intent);

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_item, menu);
		return true;
	}

}
