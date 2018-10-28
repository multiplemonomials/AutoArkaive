package com.autoarkaive;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import com.autoarkaive.checkindata.CheckinEntry;
import com.autoarkaive.checkindata.CheckinResult;
import com.autoarkaive.checkindata.CheckinResultFailure;

/**
 * Class to manage AutoArkaive's Android emulator and the aps running on it
 * @author jamie
 *
 */
public class EmulatorController 
{
	// path to Android SDK on the host system
	final static private String ANDROID_SDK_PATH = "C:/android-sdk"; 
	
	// name of pre-created Android Virtual Device to run
	final static private String AVD_NAME = "Nexus_5_API_25";
	
	// port that emulator's control shell will run on 
	final static int EMULATOR_SHELL_PORT = 5554;
	
	// executable file suffix of host system
	final static String EXE_SUFFIX = System.getProperty("os.name").contains("Windows") ? ".exe" : "";
	
	// path to dotfile on the local machine where the emulator writes its authentication passphrase
	// (I think it does this so you can't mess with someone's emulator from another machine)
	final static Path AUTH_FILE_PATH = Paths.get(System.getProperty("user.home"), ".emulator_console_auth_token");
	
	// port that the AutoArkaive app will listen on 
	final static int AA_APP_PORT = 3129;
	
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
		try 
		{
			System.out.println("Starting emulator...");
			
			// start emulator
			new ProcessBuilder(ANDROID_SDK_PATH + "/tools/emulator" + EXE_SUFFIX, "-avd", AVD_NAME, "-port", Integer.toString(EMULATOR_SHELL_PORT)).start();
			
			// give it some time to get going
			Thread.sleep(100);
			
			// connect to control socket
			emulatorSocket = new Socket("localhost", EMULATOR_SHELL_PORT);
			emulatorShell = new PrintWriter(emulatorSocket.getOutputStream());
			
			// wait for emulator startup [wait for data to be printed over telnet]
			emulatorSocket.getInputStream().read();
			
			// perform authentication
			FileInputStream authInputStream = new FileInputStream(AUTH_FILE_PATH.toFile());
			Scanner authScanner = new Scanner(authInputStream);
			String authToken = authScanner.nextLine();
			authScanner.close();
			
			emulatorShell.println("auth " + authToken);
			
			// set up port forwarding so that we can talk to the app
			emulatorShell.printf("redir add %d:%d\n", AA_APP_PORT);
			
			// TODO: install AA APK & grant accessibility permission
			// from here: https://stackoverflow.com/questions/46899547/granting-accessibility-service-permission-for-debug-purposes
			runADBCommand("shell", "settings", "put", "secure", "enabled_accessibility_services", "%accessibility:" + AA_APP_PACKAGE + "/" + AA_APP_SERVICE_CLASS);
			
			// start AA app
			runADBCommand("shell", "am", "start", "-n", AA_APP_PACKAGE + "/" + AA_APP_MAIN_CLASS);
			
			// open socket to app
			appSocket = new Socket("localhost", AA_APP_PORT);
			
		} 
		catch (Exception e)
		{
			System.err.printf("Caught %s during EmulatorController starup: %s\n", e.getClass().getSimpleName(), e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Execute the given checkin.  Blocks until complete.
	 * @param checkin
	 */
	public void performCheckin(CheckinEntry checkin)
	{
		try
		{
			appSocketSerializer.writeObject(checkin);
			
			// wait for app to (try to) perform the checkin and send back the result
			CheckinResult result = (CheckinResult) appSocketDeserializer.readObject();
			
			if(!result.succeeded())
			{
				System.err.println("checkin" + checkin.courseName +" for username " + checkin.username + 
						" failed on device due to error: " + ((CheckinResultFailure)result).getFailureMessage());
			}
		} 
		catch (Exception e) 
		{
			System.err.printf("Caught %s while sending checkin to emulator: %s\n", e.getClass().getSimpleName(), e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Kill the emulator instance
	 */
	public void shutdown()
	{
		emulatorShell.println("kill");
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
}
