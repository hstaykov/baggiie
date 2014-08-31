package com.example.baggiiinterfaces;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

public class LoginActivity extends Activity {
	private Context currentContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login1);

		findViewById(R.id.btnLogin).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(currentContext, MainActivity.class);
				startActivity(intent);
				finish();

			}
		});
	}

}
