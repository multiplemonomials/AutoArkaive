package com.autoarkaive;

import java.util.List;

import com.autoarkaive.communications.ArkaiveClass;
import com.autoarkaive.communications.CheckinRequest;

/**
 * Stores a queue of Arkaive checkins that need to be performed, and manages
 * the Android emulator used to carry them out
 * @author jamie
 *
 */
public abstract class CheckinQueue 
{
	
	
	/**
	 * Queue a checkin to be processed.  The checkin's start time must be >= the current time.
	 * 
	 * Note: if multiple checkins are enqueued at the same time, the one that ends the soonest will be processed first.
	 * 
	 * Note 2: This function is 100% thread safe.
	 * @param checkin
	 */
	public abstract void enqueueCheckin(CheckinRequest checkin);
	
	/**
	 * Tests if the given username and password is a valid login.
	 * This function queries the device, it may block for quite a while until this completes.
	 * @param username
	 * @param password
	 * @return
	 */
	public abstract boolean testLogin(String username, String password);
	
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
	public abstract List<ArkaiveClass> getClassList(String username, String password);
	
	/**
	 * Close the emulator and shut down the internal thread
	 */
	public abstract void shutdown();
	
}
