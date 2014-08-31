package com.example.baggiiinterfaces;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class LostActivity extends Activity {
	private RelativeLayout screen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lost);
		AudioManager audioManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);
		final MediaPlayer mp = MediaPlayer.create(this, R.raw.alarm);
		// mp.start();

		screen = (RelativeLayout) findViewById(R.id.lostScreen);
		screen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lost, menu);
		return true;
	}

}
