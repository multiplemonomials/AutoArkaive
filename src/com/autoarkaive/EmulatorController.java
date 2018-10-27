package com.autoarkaive;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

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
	final static int EMULATOR_PORT = 5554;
	
	// executable file suffix of host system
	final static String EXE_SUFFIX = System.getProperty("os.name").contains("Windows") ? ".exe" : "";
	
	// socket to emulator control shell
	private Socket emulatorSocket;
	private PrintWriter emulatorShell;
	
	public EmulatorController()
	{
		try 
		{
			System.out.println("Starting emulator...");
			
			// start emulator
			new ProcessBuilder(ANDROID_SDK_PATH + "/tools/emulator" + EXE_SUFFIX, "-port", Integer.toString(EMULATOR_PORT)).start();
			
			// connect to control socket
			emulatorSocket = new Socket("localhost", EMULATOR_PORT);
			emulatorShell = new PrintWriter(emulatorSocket.getOutputStream());
			
			// TODO: authenticate with control shell
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Execute the given checkin.  Blocks until complete.
	 * @param checkin
	 */
	public void performCheckin(CheckinEntry checkin)
	{
		
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
}
