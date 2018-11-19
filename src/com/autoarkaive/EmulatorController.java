package com.autoarkaive;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import com.autoarkaive.communications.ArkaiveClass;
import com.autoarkaive.communications.CheckinRequest;
import com.autoarkaive.communications.ClassListRequest;
import com.autoarkaive.communications.LoginCheckRequest;
import com.autoarkaive.communications.ResultClassList;
import com.autoarkaive.communications.ResultFailure;
import com.autoarkaive.communications.ResultSuccess;


/**
 * Class to manage AutoArkaive's Android emulator and the aps running on it
 * @author jamie
 *
 */
public class EmulatorController 
{
	// Configure below variables before deploying
	// ----------------------------------------------------------------
	
	// path to Android SDK on the host system
	final static private String ANDROID_SDK_PATH = "C:/android-sdk";
	
	// name of pre-created Android Virtual Device to run
	final static private String AVD_NAME = "Nexus_5_API_25";
	
	// Following variables should not need configuration
	// ----------------------------------------------------------------
	
	// port that emulator's control shell will run on 
	final static int EMULATOR_SHELL_PORT = 5554;
	
	// executable file suffix of host system
	final static String EXE_SUFFIX = System.getProperty("os.name").contains("Windows") ? ".exe" : "";
	
	// path to dotfile on the local machine where the emulator writes its authentication passphrase
	// (I think it does this so you can't mess with someone's emulator from another machine)
	final static Path AUTH_FILE_PATH = Paths.get(System.getProperty("user.home"), ".emulator_console_auth_token");
	
	// port that the AutoArkaive app will listen on 
	final static int AA_APP_PORT = 3128;
	
	// package of AA app
	final static String AA_APP_PACKAGE = "com.autoarkaive.autoarkaive";
	
	// main class of AA app (starts service when run)
	final static String AA_APP_MAIN_CLASS = AA_APP_PACKAGE + ".MainActivity";
	
	// service class of AA app
	final static String AA_APP_SERVICE_CLASS = AA_APP_PACKAGE + ".AAService";
	
	// socket to emulator control shell
	private Socket emulatorSocket;
	private PrintWriter emulatorShell;
	
	// socket to AA app
	private Socket appSocket;
	private ObjectInputStream appSocketDeserializer;
	private ObjectOutputStream appSocketSerializer;

	
	public EmulatorController()
	{
		Process emulatorProcess = null;
		
		try 
		{
			
			System.out.println("Starting emulator...");
			
			// start emulator
			emulatorProcess = new ProcessBuilder(ANDROID_SDK_PATH + "/tools/emulator" + EXE_SUFFIX, "-avd", AVD_NAME, "-port", Integer.toString(EMULATOR_SHELL_PORT)).start();
			
			// give it some time to get going
			System.out.println("Started emulator.  Waiting 50s for startup.");
			Thread.sleep(50000);
			
			
			// connect to control socket
			emulatorSocket = new Socket("localhost", EMULATOR_SHELL_PORT);
			emulatorShell = new PrintWriter(emulatorSocket.getOutputStream());
						
			// perform authentication
			FileInputStream authInputStream = new FileInputStream(AUTH_FILE_PATH.toFile());
			Scanner authScanner = new Scanner(authInputStream);
			String authToken = authScanner.nextLine();
			authScanner.close();
			
			String authCommand = "auth " + authToken + "\n";
			System.out.println("Sending: " + authCommand);
			emulatorShell.print(authCommand);
			
			// set up port forwarding so that we can talk to the app
			String redirCommand = String.format("redir add tcp:%d:%d\n", AA_APP_PORT, AA_APP_PORT);
			System.out.println("Sending: " + redirCommand);
			emulatorShell.print(redirCommand);
			emulatorShell.flush();
			
			System.out.println("Successfully connected to emulator!");
			System.out.println("Connecting to AutoArkaive service...");

			// open socket to app
			appSocket = new Socket("localhost", AA_APP_PORT);
			appSocketSerializer = new ObjectOutputStream(appSocket.getOutputStream());
			appSocketDeserializer = new ObjectInputStream(appSocket.getInputStream());
			
			System.out.println("Emulator setup complete!");
		} 
		catch (Exception e)
		{
			System.err.printf("Caught %s during EmulatorController starup: %s\n", e.getClass().getSimpleName(), e.getMessage());
			e.printStackTrace();
			
			if(emulatorProcess != null)
			{
				emulatorProcess.destroy();
			}
		}
	}
	
	/**
	 * Execute the given checkin.  Blocks until complete.
	 * @param checkin
	 */
	public void performCheckin(CheckinRequest checkin)
	{
		try
		{
			appSocketSerializer.writeObject(checkin);
			appSocketSerializer.flush();
			
			// wait for app to (try to) perform the checkin and send back the result
			Object result = appSocketDeserializer.readObject();
			
			if(result instanceof ResultFailure)
			{
				System.err.println("checkin for course " + checkin.course +" for username " + checkin.username + 
						" failed on device due to error: " + ((ResultFailure)result).getFailureMessage());
			}
		} 
		catch (Exception e) 
		{
			System.err.printf("Caught %s while sending checkin to emulator: %s\n", e.getClass().getSimpleName(), e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Perform the given class list request.  Blocks until complete.
	 * @param checkin
	 */
	public ArrayList<ArkaiveClass> listClasses(ClassListRequest classListRequest)
	{
		try
		{			
			appSocketSerializer.writeObject(classListRequest);
			appSocketSerializer.flush();
			
			// wait for app to (try to) perform the checkin and send back the result
			Object result = appSocketDeserializer.readObject();
			
			if(result instanceof ResultClassList)
			{
				return ((ResultClassList)result).getClassList();
			}
			if(result instanceof ResultFailure)
			{
				System.err.println("class list for username " + classListRequest.username + 
						" failed on device due to error: " + ((ResultFailure)result).getFailureMessage());
			}
		} 
		catch (Exception e) 
		{
			System.err.printf("Caught %s while sending class list request to emulator: %s\n", e.getClass().getSimpleName(), e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns whether or not the given login works.  Blocks until complete.
	 * @param checkin
	 */
	public boolean testLogin(LoginCheckRequest checkRequest)
	{
		try
		{			
			appSocketSerializer.writeObject(checkRequest);
			appSocketSerializer.flush();
			
			// wait for app to (try to) perform the checkin and send back the result
			Object result = appSocketDeserializer.readObject();
			
			if(result instanceof ResultSuccess)
			{
				return true;
			}
		} 
		catch (Exception e) 
		{
			System.err.printf("Caught %s while sending login check request to emulator: %s\n", e.getClass().getSimpleName(), e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	/**
	 * Kill the emulator instance
	 */
	public void shutdown()
	{
		emulatorShell.println("kill");
		emulatorShell.flush();
		try 
		{
			emulatorSocket.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Run a given command on Android Debug Bridge (ADB), and block until it finishes.
	 * 
	 * Note: if AA is ever modified to allow multiple emulators, this function will need to specify the current emulator's device ID.
	 */
	private void runADBCommand(String... ADBArgs)
	{
		// build full command line
		String[] commandLine = new String[ADBArgs.length + 1];
		commandLine[0] = ANDROID_SDK_PATH + "/platform-tools/adb" + EXE_SUFFIX;
		System.arraycopy(ADBArgs, 0, commandLine, 1, ADBArgs.length);
		
		System.out.println("Executing: " + Arrays.toString(commandLine));
		
		try 
		{
			Process ADBProcess = new ProcessBuilder(commandLine).start();
			ADBProcess.waitFor();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Set the simulated location of the emulator
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	private void setEmulatorLocation(double latitude, double longitude, int altitude)
	{
		String emulatorCommand = String.format("geo fix %.05f %.05f %d\n",  longitude, latitude, altitude);
		System.out.println("Sending: " + emulatorCommand);
		emulatorShell.printf(emulatorCommand);
	}
}
