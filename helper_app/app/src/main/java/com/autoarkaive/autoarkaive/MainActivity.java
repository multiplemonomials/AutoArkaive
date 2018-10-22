package com.autoarkaive.autoarkaive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

	final static String TAG = "AAMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start the accessibility service
        startService(new Intent(this, AAService.class));
        Log.d(TAG, "Service Started!");
    }
}
