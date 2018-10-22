package com.autoarkaive.autoarkaive;

import android.accessibilityservice.AccessibilityService;
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

    public AAService()
    {
    }

	@Override
	protected void onServiceConnected()
	{
		super.onServiceConnected();
	}

	@Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
		Log.d(TAG, "Caught accessibility event: " + event.toString());

		switch (event.getEventType())
		{
			//On Gesture events print out the entire view heirarchy!
			case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
				Log.d(TAG, A11yNodeInfo.wrap(getRootInActiveWindow()).toViewHeirarchy());

			case AccessibilityEvent.TYPE_VIEW_CLICKED:
				Log.d(TAG, event.getSource().toString());

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
