package com.autoarkaive.autoarkaive;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity
{
	private static final Intent sSettingsIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

	final static String TAG = "AAMainActivity";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		// start the accessibility service
		startService(new Intent(this, AAService.class));
		Toast.makeText(this, "Service started!", Toast.LENGTH_SHORT).show();


    }

	@Override
	protected void onStart()
	{
		super.onStart();

	}
}
