package com.autoarkaive;

import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.joda.time.LocalTime;

import com.autoarkaive.communications.ArkaiveClass;
import com.autoarkaive.communications.CheckinRequest;
import com.autoarkaive.communications.ClassListRequest;
import com.autoarkaive.communications.LoginCheckRequest;

/**
 * Stores a queue of Arkaive checkins that need to be performed, and manages
 * the Android emulator used to carry them out
 * @author jamie
 *
 */
public class CheckinQueue 
{
	private PriorityBlockingQueue<CheckinRequest> checkinQueue;
	private Thread checkinThread;
	private EmulatorController emulatorController;
	private Lock emulatorLock;
	
	/**
	 * Construct checkin queue.  Starts up the Android emulator.
	 */
	public CheckinQueue(String androidSDKPath, String AVDName)
	{
		checkinQueue = new PriorityBlockingQueue<>();
		emulatorController = new EmulatorController(androidSDKPath, AVDName);
		emulatorLock = new ReentrantLock();
		
		checkinThread = new Thread(this::checkinLoop);
		checkinThread.start();
	}
	
	/**
	 * Queue a checkin to be processed.  The checkin's start time must be >= the current time.
	 * 
	 * Note: if multiple checkins are enqueued at the same time, the one that ends the soonest will be processed first.
	 * 
	 * Note 2: This function is 100% thread safe.
	 * @param checkin
	 */
	public void enqueueCheckin(CheckinRequest checkin)
	{
		if(checkin.checkinStartTime.isAfter(LocalTime.now()))
		{
			throw new IllegalArgumentException("This checkin is still in the future!");
		}
		
		checkinQueue.put(checkin);
	}
	
	/**
	 * Tests if the given username and password is a valid login.
	 * This function queries the device, it may block for quite a while until this completes.
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean testLogin(String username, String password)
	{
		emulatorLock.lock();
		boolean result = emulatorController.testLogin(new LoginCheckRequest(username, password));
		emulatorLock.unlock();
		return result;
	}
	
	/**
	 * Gets the list of courses that the given user has enrolled in on Arkaive, 
	 * formatted as a map with the course code as the key and the course name as the value.
	 * Returns an empty map on error.
	 * This function queries the device, it may block for quite a while until this completes.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public ArrayList<ArkaiveClass> getClassList(String username, String password)
	{
		emulatorLock.lock();
		ArrayList<ArkaiveClass> classList = emulatorController.listClasses(new ClassListRequest(username, password));
		emulatorLock.unlock();
		return null;
	}
	
	/**
	 * Close the emulator and shut down the internal thread
	 */
	public void shutdown()
	{
		checkinThread.interrupt();
		try 
		{
			checkinThread.join();
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		emulatorController.shutdown();
	}
	
	@Override
	public void finalize()
	{
		shutdown();
	}
	
	/**
	 * Loop for internal thread.  Dequeues checkins and executes them
	 */
	private void checkinLoop()
	{
		System.out.println("Starting checkin thread...");

		try
		{
			while(true)
			{
				CheckinRequest currEntry = checkinQueue.take();
				
				if(currEntry.checkinEndTime.isAfter(LocalTime.now()))
				{
					//oh no! we're too late
					System.err.println("Error: checkin load too high: missed checkin \"" + currEntry.course +"\" for username " + currEntry.username);
				}
				else
				{
					emulatorLock.lock();
					emulatorController.performCheckin(currEntry);
					emulatorLock.unlock();
				}
			}
		}
		catch(InterruptedException ex)
		{
			System.out.println("Shutting down checkin thread");
			return;
		}
		
	}
}
