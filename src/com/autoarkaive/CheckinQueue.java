package com.autoarkaive;

import java.util.concurrent.PriorityBlockingQueue;

import org.joda.time.LocalTime;

import com.autoarkaive.checkindata.CheckinEntry;

/**
 * Stores a queue of Arkaive checkins that need to be performed, and manages
 * the Android emulator used to carry them out
 * @author jamie
 *
 */
public class CheckinQueue 
{
	private PriorityBlockingQueue<CheckinEntry> checkinQueue;
	private Thread checkinThread;
	private EmulatorController emulatorController;
	
	/**
	 * Construct checkin queue.  Starts up the Android emulator.
	 */
	public CheckinQueue()
	{
		checkinQueue = new PriorityBlockingQueue<>();
		emulatorController = new EmulatorController();
		
		checkinThread = new Thread(this::checkinLoop);
		checkinThread.start();
	}
	
	/**
	 * Queue a checkin to be processed.  The checkin's start time must be >= the current time.
	 * 
	 * Note: if multiple checkins are enqueued at the same time, the one that ends the soonest will be processed first.
	 * 
	 * Note
	 * @param checkin
	 */
	public void enqueueCheckin(CheckinEntry checkin)
	{
		if(checkin.checkinStartTime.isAfter(LocalTime.now()))
		{
			throw new IllegalArgumentException("This checkin is still in the future!");
		}
		
		checkinQueue.put(checkin);
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
				CheckinEntry currEntry = checkinQueue.take();
				
				if(currEntry.checkinEndTime.isAfter(LocalTime.now()))
				{
					//oh no! we're too late
					System.err.println("Error: checkin load too high: missed checkin \"" + currEntry.courseName +"\" for username " + currEntry.username);
				}
				else
				{
					emulatorController.performCheckin(currEntry);
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
