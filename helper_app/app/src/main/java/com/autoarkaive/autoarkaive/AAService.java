package com.autoarkaive.autoarkaive;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.autoarkaive.checkindata.CheckinEntry;
import com.autoarkaive.checkindata.CheckinResultFailure;
import com.moba11y.androida11yutils.A11yNodeInfo;

/**
 * Service which controls the Arkaive app UI
 */
public class AAService extends AccessibilityService
{
	private final String TAG = "AAService";

	public static final int FOREGROUND_SERVICE_ID = 101;

	// enum used to keep track of the service's state
	enum State
	{
		IDLE,
		LOGIN_SCREEN,
		COURSE_SCREEN,
		CHECKIN_SCREEN,
		DONE,
		FAILED
	}

	private State state = State.IDLE;

	// stores failure message when in the FAILED state
	private String failureMessage = null;

	// stores current checkin when not in the IDLE state
	private CheckinEntry checkin;

    public AAService()
    {
    	// for testing
		checkin = new CheckinEntry(0, 0, 0, "smit109@usc.edu", "xxxxxxx", "Principles of Software Development", null, null);
		state = State.LOGIN_SCREEN;
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
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
				Log.d(TAG, A11yNodeInfo.wrap(event.getSource()).toViewHeirarchy());
				onScreenLoad(A11yNodeInfo.wrap(event.getSource()));
			default: {
				if (event.getSource() != null) {
					Log.d(TAG, A11yNodeInfo.wrap(event.getSource()).toViewHeirarchy());
				}
			}
		}
    }

    private void onScreenLoad(A11yNodeInfo rootNode)
	{
		if(rootNode.getChildCount() > 0) // wait for the app to add children
		{
			if(state == State.LOGIN_SCREEN)
			{
				A11yNodeInfo layout = rootNode.getChild(0).getChild(0);

				A11yNodeInfo usernameText = layout.getChild(1);
				A11yNodeInfo passwordText = layout.getChild(2);
				A11yNodeInfo loginButton = layout.getChild(3);

				// type in login info
				Bundle usernameArgs = new Bundle();
				usernameArgs.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, checkin.username);
				usernameText.getAccessibilityNodeInfoCompat().performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, usernameArgs);

				Bundle passwordArgs = new Bundle();
				passwordArgs.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, checkin.password);
				passwordText.getAccessibilityNodeInfoCompat().performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, passwordArgs);

				// now, hit it!
				loginButton.performAction(A11yNodeInfo.Actions.CLICK);

				state = State.COURSE_SCREEN;
			}
		}
	}

    @Override
    public void onInterrupt()
	{
		// do nothing
	}

	// thread which monitors the socket to the server, kicks off a checkin, and reports back to the server when it happens
	private class CheckinListenerThread
	{
		public void run()
		{
			try
			{
				switch(state)
				{
					case IDLE:
						// wait for next request from server

						// start Arkaive app

						state = State.LOGIN_SCREEN;
						break;

					case LOGIN_SCREEN:
					case COURSE_SCREEN:
					case CHECKIN_SCREEN:
						// spinlock until the UI actions have finished
						Thread.sleep(50);

						break;

					case DONE:
						// report success to server

						break;

					case FAILED:
						// report failure to server
						CheckinResultFailure failureResult = new CheckinResultFailure(failureMessage);

						break;
				}
			}
			catch(InterruptedException ex)
			{
				// do nothing
			}
		}
	}
}
