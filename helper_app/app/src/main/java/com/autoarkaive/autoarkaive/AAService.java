package com.autoarkaive.autoarkaive;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.moba11y.androida11yutils.A11yNodeInfo;

/**
 * Service which controls the Arkaive app UI
 */
public class AAService extends AccessibilityService
{
	private final String TAG = "AAService";

	public static final int FOREGROUND_SERVICE_ID = 101;

    public AAService()
    {
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Notification notification = new Notification.Builder(this)
				.setContentTitle("AutoArkaive")
				.setContentText("Service Running")
				.setSmallIcon(R.drawable.ic_launcher_foreground)
				.build();
		startForeground(FOREGROUND_SERVICE_ID, notification);

		return START_STICKY;
	}

	@Override
	protected void onServiceConnected()
	{
		super.onServiceConnected();
		Log.d(TAG, "onServiceConnected() called");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.w(TAG, "Destroyed!");
		stopForeground(true);
	}

	@Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
		Log.i(TAG, "Caught accessibility event: " + event.toString());

		switch (event.getEventType())
		{
			//On Gesture events print out the entire view heirarchy!
			case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
				Log.d(TAG, A11yNodeInfo.wrap(getRootInActiveWindow()).toViewHeirarchy());
				break;
			case AccessibilityEvent.TYPE_VIEW_CLICKED:
				Log.d(TAG, event.getSource().toString());
				break;
			default: {
				//The event has different types, for you, you want to look for "action clicked"
				if (event.getSource() != null) {
					Log.d(TAG, A11yNodeInfo.wrap(event.getSource()).toViewHeirarchy());
				}
			}
		}
    }

    @Override
    public void onInterrupt()
	{
		// do nothing
	}
}
