package com.autoarkaive.autoarkaive;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.autoarkaive.communications.*;
import com.moba11y.androida11yutils.A11yNodeInfo;
import eu.chainfire.libsuperuser.Shell;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service which controls the Arkaive app UI
 */
public class AAService extends AccessibilityService implements LocationListener
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

	// set during checkin to indicate that the course currently shown is the correct one to check in to
	boolean correctCourseFound = false;

	// list of classes being built for a class list request
	ArrayList<ArkaiveClass> classList = new ArrayList<>();

	// when a request is active, it's stored in the corresponding variable
	private CheckinRequest checkinRequest;
	private ClassListRequest classListRequest;
	private LoginCheckRequest loginCheckRequest;

	// thread which monitors the socket to the server, kicks off checkins, and reports the results back to the server
	private Thread controllerThread;

	// socket for communications with AutoArkaive server
	Socket serverSocket;
	ObjectInputStream requestStream;
	ObjectOutputStream resultStream;

	// socket to accept connections
	ServerSocket acceptorSocket;
	final static int PORT = 31280;

	// location manager for location services
	LocationManager locationManager;

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

	private synchronized void setState(State state)
	{
		Log.i(TAG, "Set state: " + state.toString());
		this.state = state;
	}

	@Override
	protected void onServiceConnected()
	{
		super.onServiceConnected();
		Log.d(TAG, "onServiceConnected() called");

		setState(State.IDLE);

		// create socket
		try
		{
			acceptorSocket = new ServerSocket(PORT);
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}

		// start a new controller thread
		controllerThread = new Thread(new CheckinControllerThread());
		controllerThread.start();

		// from https://stackoverflow.com/questions/38251741/how-to-set-android-mock-gps-location
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false,
				false, false, true, true, true, 0, 5);
		locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);


		// for testing
		//checkinRequest = new CheckinRequest(0, 0, 0, "smit109@usc.edu", "xxxxxxx", new ArkaiveClass("", "QN7K"), null, null);
		//requestType = RequestType.CHECKIN;

		//requestType = RequestType.LOGIN_CHECK;
		//loginCheckRequest = new LoginCheckRequest("smit109@usc.edu", "xxxxxxxx");

		//requestType = RequestType.CLASS_LIST;
		//classListRequest = new ClassListRequest("smit109@usc.edu", "xxxxxxx");

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.w(TAG, "Destroyed!");
		stopForeground(true);
		controllerThread.interrupt();
		try
		{
			controllerThread.join();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}

		// close sockets
		try
		{
			serverSocket.close();
			acceptorSocket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
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
			case AccessibilityEvent.TYPE_VIEW_SCROLLED:
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
			else if(currentScreen == ArkaiveUIScreen.NAV_DRAWER)
			{
				// prevent crash that appears to be due to clicking this too quickly
				Thread.sleep(500);

				// GET ME OUT OF HERE!
				// click close button
				A11yNodeInfo closeButton = rootNode.getChild(0);

				closeButton.performAction(A11yNodeInfo.Actions.CLICK);
			}
			else if(currentScreen == ArkaiveUIScreen.COURSE_SELECTION || currentScreen == ArkaiveUIScreen.COURSE_SELECTION_ENTRIES)  // sometimes the courses got loaded later in a fragment, so we handle both cases
			{
				if(state != State.LOGOUT)
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
						A11yNodeInfo courseListNode;

						if(currentScreen == ArkaiveUIScreen.COURSE_SELECTION)
						{
							A11yNodeInfo drawerLayout = rootNode.getChild(rootNode.getChildCount() - 1);
							A11yNodeInfo relativeLayout = drawerLayout.getChild(0);
							courseListNode = relativeLayout.getChild(0);
						}
						else
						{
							if(rootNode.getChildCount() > 1)
							{
								courseListNode = rootNode;
							}
							else
							{
								A11yNodeInfo relativeLayout = rootNode.getChild(0);
								courseListNode = relativeLayout.getChild(0);
							}

						}

						if(courseListNode.getChildCount() < 2)
						{
							Log.i(TAG, "Course selection not fully loaded yet");
							return;
						}

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

				ArkaiveClass classEntry = new ArkaiveClass(courseName, enrollmentCode);
				boolean seenBefore = classList.contains(classEntry);

				if(requestType == RequestType.CLASS_LIST)
				{
					moveToNextCourse = true;

					if(!seenBefore)
					{
						checkingCourseIndex++;
					}

				}
				else if(requestType == RequestType.CHECKIN)
				{
					// check if this is the course we want
					moveToNextCourse = !enrollmentCode.equals(checkinRequest.course.courseCode);

					if(moveToNextCourse && !seenBefore)
					{
						checkingCourseIndex++;
					}

					if(!moveToNextCourse)
					{
						Log.i(TAG, "This is the course we're trying to check in to!");
					}
				}

				if(!seenBefore)
				{
					classList.add(classEntry);
				}

				if(moveToNextCourse)
				{

					performGlobalAction(GLOBAL_ACTION_BACK);
					Log.i(TAG, "Going back!");
					Thread.sleep(500);
				}
				else
				{
					// set flag telling checkin handler to check in
					correctCourseFound = true;
					Log.i(TAG, "Staying on this course screen");
				}
			}
			else if(currentScreen == ArkaiveUIScreen.CHECKIN_BUTTON)
			{
				if(requestType == RequestType.CHECKIN && correctCourseFound)
				{
					Log.i(TAG, "Clicking checkin!");
					rootNode.performAction(A11yNodeInfo.Actions.CLICK);

					setState(State.LOGOUT);
				}
			}
			else if(currentScreen == ArkaiveUIScreen.CLASS_NOT_IN_SESSION || currentScreen == ArkaiveUIScreen.CLASS_IN_PROGRESS)
			{
				if(requestType == RequestType.CHECKIN && correctCourseFound)
				{
					if(state == State.EXECUTING)
					{
						// since we're seeing this UI element, we know we screwed up

						errorInRequest = true;
						failureMessage = "Checkin for this class is not open!";
					}

					performGlobalAction(GLOBAL_ACTION_BACK);
					setState(State.LOGOUT);
				}
			}
			else if(currentScreen == ArkaiveUIScreen.BILLING_ERROR_DIALOG)
			{
				// Arkaive doesn't seem to like running on an emulator, and shows an error dialog when it tries to ask for money.
				// make it go away by clicking on the OK button
				A11yNodeInfo scrollView = rootNode.getChild(1);
				A11yNodeInfo okButton = scrollView.getChild(0);

				okButton.performAction(A11yNodeInfo.Actions.CLICK);
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
	private class CheckinControllerThread implements Runnable
	{
		public void run()
		{
			// wait for server to connect
			try
			{
				Log.v(TAG, "Waiting for server connection...");
				serverSocket = acceptorSocket.accept();
				requestStream = new ObjectInputStream(serverSocket.getInputStream());
				resultStream = new ObjectOutputStream(serverSocket.getOutputStream());
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				while(!Thread.interrupted())
				{
					switch(state)
					{
						case IDLE:
							// wait for next request from server
							Log.v(TAG, "Waiting for request from server");
							Object request = requestStream.readObject();

							if(request instanceof CheckinRequest)
							{
								requestType = RequestType.CHECKIN;
								checkinRequest = (CheckinRequest) request;
							}
							else if(request instanceof ClassListRequest)
							{
								requestType = RequestType.CLASS_LIST;
								classListRequest = (ClassListRequest) request;
							}
							else if(request instanceof LoginCheckRequest)
							{
								requestType = RequestType.LOGIN_CHECK;
								loginCheckRequest = (LoginCheckRequest) request;
							}
							else
							{
								throw new RuntimeException("Unknown request type received!");
							}

							setState(State.EXECUTING);

							// start Arkaive app
							Intent arkaiveIntent = new Intent(Intent.ACTION_VIEW);
							arkaiveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
							arkaiveIntent.setComponent(new ComponentName("com.arkaive.arkaive", "com.arkaive.arkaive.HomeActivity"));
							arkaiveIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
							startActivity(arkaiveIntent);

							// reset all state machine variables
							errorInRequest = false;
							failureMessage = "";
							checkingCourseIndex = 0;
							classList.clear();
							correctCourseFound = false;

							break;


						case EXECUTING:

							// spinlock until the UI actions have finished
							Thread.sleep(100);

							if(requestType == RequestType.CHECKIN)
							{
								sendMockLocation(checkinRequest.latitude, checkinRequest.longitude, checkinRequest.altitude);
							}

							break;


						case DONE:
							Log.v(TAG, "Sending DONE result...");
							if(errorInRequest)
							{
								// report failure to server
								ResultFailure failureResult = new ResultFailure(failureMessage);
								resultStream.writeObject(failureResult);
							}
							else
							{
								if(requestType == RequestType.CLASS_LIST)
								{
									// return class list
									ResultClassList classListResult = new ResultClassList(classList);
									resultStream.writeObject(classListResult);
								}
								else
								{
									// report success to server
									ResultSuccess successResult = new ResultSuccess();
									resultStream.writeObject(successResult);
								}

							}
							resultStream.flush();

							setState(State.IDLE);

							break;
					}
				}
			}
			catch(InterruptedException ex)
			{
				// do nothing
			}
			catch(IOException | ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Send a mock location update with the given parameters
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	private void sendMockLocation(double latitude, double longitude, int altitude)
	{

		// from here: https://stackoverflow.com/questions/38251741/how-to-set-android-mock-gps-location
		Location mockLocation = new Location(LocationManager.GPS_PROVIDER);
		mockLocation.setLatitude(latitude);
		mockLocation.setLongitude(longitude);
		mockLocation.setAltitude(altitude);
		mockLocation.setTime(System.currentTimeMillis());
		mockLocation.setAccuracy(1);
		mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

		locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation);
	}

	@Override
	public void onLocationChanged(Location location)
	{
		Log.i(TAG, "Got location update: " + location.toString());
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{

	}

	@Override
	public void onProviderEnabled(String provider)
	{
		Log.i(TAG, "Location provider enabled: " + provider);
	}

	@Override
	public void onProviderDisabled(String provider)
	{
		Log.i(TAG, "Location provider disabled: " + provider);
	}
}
