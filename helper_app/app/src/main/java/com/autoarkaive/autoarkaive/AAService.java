package com.autoarkaive.autoarkaive;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.autoarkaive.communications.*;
import com.moba11y.androida11yutils.A11yNodeInfo;
import eu.chainfire.libsuperuser.Shell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service which controls the Arkaive app UI
 */
public class AAService extends AccessibilityService
{
	private final String TAG = "AAService";

	public static final int FOREGROUND_SERVICE_ID = 101;

	// regex for reading the enrollment code out of the UI
	public Pattern enrollmentCodePattern = Pattern.compile("Enrollment Code: (\\w+)");

	// enum used to keep track of the service's state, e.g. which UI screen is currently showing or whether or not there was an error
	enum State
	{
		IDLE,
		EXECUTING,
		LOGOUT,
		DONE
	}

	enum RequestType
	{
		LOGIN_CHECK,
		CLASS_LIST,
		CHECKIN
	}

	private State state = State.IDLE;
	private RequestType requestType = null;

	// true if the current request failed with an error
	private boolean errorInRequest = false;
	// stores failure message when in the FAILED state
	private String failureMessage = null;

	// index of the course that we are currently clicking on in the course selection menu
	int checkingCourseIndex = 0;

	// number of courses that were found on the courses screen
	int numCourses = 0;

	// list of classes being built for a class list request
	ArrayList<ArkaiveClass> classList = new ArrayList<>();

	// when a request is active, it's stored in the corresponding variable
	private CheckinRequest checkinRequest;
	private ClassListRequest classListRequest;
	private LoginCheckRequest loginCheckRequest;

	// thread which monitors the socket to the server, kicks off checkins, and reports the results back to the server
	private Thread controllerThread;

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

	private void setState(State state)
	{
		Log.i(TAG, "Set state: " + state.toString());
		this.state = state;
	}

	@Override
	protected void onServiceConnected()
	{
		super.onServiceConnected();
		Log.d(TAG, "onServiceConnected() called");

		setState(State.EXECUTING);

		// for testing
		//checkin = new CheckinRequest(0, 0, 0, "smit109@usc.edu", "xxxxxxxx", "Principles of Software Development", null, null);
		//requestType = RequestType.LOGIN_CHECK;
		//loginCheckRequest = new LoginCheckRequest("smit109@usc.edu", "xxxxxxxx");

		requestType = RequestType.CLASS_LIST;
		classListRequest = new ClassListRequest("smit109@usc.edu", "xxxxxxx");

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.w(TAG, "Destroyed!");
		stopForeground(true);
	}

	@Override
	/**
	 * Called by Android whenever the UI is changed to update accessibility information.
	 * XML settings restrict this funciton to events generated by the Arkaive app.
	 */
    public void onAccessibilityEvent(AccessibilityEvent event)
    {

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
			case AccessibilityEvent.TYPE_VIEW_FOCUSED:
			case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
				Log.d(TAG, "Caught accessibility event: " + event.toString());
				Log.d(TAG, "New UI has been created: ");

				String[] logLines = A11yNodeInfo.wrap(event.getSource()).toViewHeirarchy().split("\n");
				for(String line : logLines)
				{
					Log.d(TAG, line);
				}
				onScreenLoad(A11yNodeInfo.wrap(event.getSource()));
				break;
			default: {
				Log.i(TAG, "Caught accessibility event: " + event.toString());
				//if(event.getSource() != null)
				//{
				//	Log.d(TAG, A11yNodeInfo.wrap(event.getSource()).toViewHeirarchy());
				//}
			}
		}

		try
		{
			event.recycle();
		}
		catch(IllegalStateException ex)
		{
			// this happens sometimes... not sure what to do about it but it can probly be ignored
		}
    }


	/**
	 * Processes the load of a new UI screen and moves the checkin process forward
	 * @param rootNode
	 */
	private void onScreenLoad(A11yNodeInfo rootNode)
	{


		try
		{
			ArkaiveUIScreen currentScreen = ArkaiveUIScreen.detectUIScreen(rootNode);

			if(currentScreen != null)
			{
				Log.i(TAG, "Matched UI screen: " + currentScreen.toString());
			}

			if(state == State.IDLE || state == State.DONE)
			{
				// do nothing
				return;
			}

			if(currentScreen == ArkaiveUIScreen.LOGIN)
			{
				if(state == State.EXECUTING)
				{
					Log.i(TAG, "Logging in...");

					A11yNodeInfo layout;
					if(rootNode.getChild(0).getChildCount() > 0)
					{
						layout = rootNode.getChild(0).getChild(0);
					}
					else
					{
						layout = rootNode.getChild(0);
					}

					A11yNodeInfo usernameText = layout.getChild(1);
					A11yNodeInfo passwordText = layout.getChild(2);
					A11yNodeInfo loginButton = layout.getChild(3);

					// figure out credentials
					String username = null, password = null;
					if(requestType == RequestType.CHECKIN)
					{
						username = checkinRequest.username;
						password = checkinRequest.password;
					}
					else if(requestType == RequestType.CLASS_LIST)
					{
						username = classListRequest.username;
						password = classListRequest.password;
					}
					else if(requestType == RequestType.LOGIN_CHECK)
					{
						username = loginCheckRequest.username;
						password = loginCheckRequest.password;
					}

					// type in login info
					Bundle usernameArgs = new Bundle();
					usernameArgs.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, username);
					usernameText.getAccessibilityNodeInfoCompat().performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, usernameArgs);

					Bundle passwordArgs = new Bundle();
					passwordArgs.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, password);
					passwordText.getAccessibilityNodeInfoCompat().performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, passwordArgs);

					// now, hit it!
					loginButton.performAction(A11yNodeInfo.Actions.CLICK);
				}
			}
			else if(currentScreen == ArkaiveUIScreen.LOGIN_ERROR)
			{
				Log.i(TAG, "Login failed");

				// click OK
				A11yNodeInfo okButton = rootNode.getChild(2).getChild(0);
				okButton.performAction(A11yNodeInfo.Actions.CLICK);

				// this username and password don't work!
				failureMessage = "Incorrect Arkaive login information";
				errorInRequest = true;
				setState(State.DONE);
			}
			else if(currentScreen == ArkaiveUIScreen.OPTIONS_MENU)
			{
				if(state == State.LOGOUT)
				{
					Log.i(TAG, "Logging out...");

					// log out!
					A11yNodeInfo signoutButton = rootNode.getChild(0).getChild(0);
					signoutButton.performAction(A11yNodeInfo.Actions.CLICK);

					setState(State.DONE);
				}
			}
			else if(currentScreen == ArkaiveUIScreen.COURSE_SELECTION)
			{
				// if we're doing a login check, we're done!
				if(requestType == RequestType.LOGIN_CHECK)
				{
					Log.i(TAG, "Done with login check, going to logout");
					setState(State.LOGOUT);
				}
				else if(requestType == RequestType.CLASS_LIST || requestType == RequestType.CHECKIN)
				{
					// find course list node
					A11yNodeInfo drawerLayout = rootNode.getChild(rootNode.getChildCount() - 1);
					A11yNodeInfo relativeLayout = drawerLayout.getChild(0);
					A11yNodeInfo courseListNode = relativeLayout.getChild(0);


					int numCourses = courseListNode.getChildCount() - 2;

					if(checkingCourseIndex >= numCourses)
					{
						Log.i(TAG, "Done listing " + numCourses + " courses!");

						// done looking through all courses
						state = State.LOGOUT;
						if(requestType == RequestType.CHECKIN)
						{
							errorInRequest = true;
							failureMessage = "Failed to find requested class in Arkaive app";
						}
						else if(requestType == RequestType.CLASS_LIST)
						{
							Log.i(TAG, "Got classes: " + classList.toString());
						}
					}
					else
					{
						Log.i(TAG, "Clicking on course " + checkingCourseIndex);

						A11yNodeInfo courseEntry = courseListNode.getChild(checkingCourseIndex + 1);
						courseEntry.performAction(A11yNodeInfo.Actions.CLICK);
					}
				}

				// log the user out now that we have the opportunity
				if(state == State.LOGOUT)
				{
					// click on More Options
					A11yNodeInfo menuButton = rootNode.getChild(2);
					menuButton.performAction(A11yNodeInfo.Actions.CLICK);
				}
			}
			else if(currentScreen == ArkaiveUIScreen.COURSE_ACTIVITY)
			{
				// read out class name and enrollment code
				A11yNodeInfo drawerLayout = rootNode.getChild(rootNode.getChildCount() - 1);
				A11yNodeInfo relativeLayout = drawerLayout.getChild(0);

				// find the ScrollView child of relativeLayout, because its children can be in varying orders
				A11yNodeInfo scrollView = null;
				for(int index = 0; index < relativeLayout.getChildCount(); ++index)
				{
					if(relativeLayout.getChild(index).getClassName().contains("ScrollView"))
					{
						scrollView = relativeLayout.getChild(index);
					}
				}

				A11yNodeInfo innerRelativeLayout = scrollView.getChild(0);
				String courseName = innerRelativeLayout.getChild(0).getTextAsString();

				String enrollmentCodeText = innerRelativeLayout.getChild(4).getTextAsString();
				Log.i(TAG, enrollmentCodeText);
				Matcher enrollmentCodeMatcher = enrollmentCodePattern.matcher(enrollmentCodeText);

				if(!enrollmentCodeMatcher.matches())
				{
					Log.w(TAG,"Unable to match enrollment code string!");
					return;
				}

				String enrollmentCode = enrollmentCodeMatcher.group(1);

				Log.i(TAG, "Found course " + courseName + " with code " + enrollmentCode);

				boolean moveToNextCourse = false;

				if(requestType == RequestType.CLASS_LIST)
				{
					ArkaiveClass classEntry = new ArkaiveClass();
					classEntry.courseCode = enrollmentCode;
					classEntry.className = courseName;

					if(!classList.contains(classEntry))
					{
						// we haven't seen this course before
						classList.add(classEntry);
						moveToNextCourse = true;
					}


				}

				if(moveToNextCourse)
				{
					// move to the next course and go back
					checkingCourseIndex++;
					performGlobalAction(GLOBAL_ACTION_BACK);
				}
			}

		}
		catch(Exception ex)
		{
			Log.w(TAG, "Caught " + ex.getClass().getSimpleName() + " in onScreenLoad(): " + ex.getMessage());
			ex.printStackTrace();
		}
	}

    @Override
    public void onInterrupt()
	{
		// do nothing
	}

	// thread which monitors the socket to the server, kicks off checkins, and reports the results back to the server
	private class CheckinControllerThread
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

						state = State.EXECUTING;

						// reset all state machine variables
						errorInRequest = false;
						failureMessage = "";
						checkingCourseIndex = 0;
						classList.clear();

						break;


					case EXECUTING:

						// spinlock until the UI actions have finished
						Thread.sleep(50);

						break;


					case DONE:
						if(errorInRequest)
						{
							// report failure to server
							ResultFailure failureResult = new ResultFailure(failureMessage);
						}
						else
						{
							// report success to server

						}
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
