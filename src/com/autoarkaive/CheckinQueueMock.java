package com.autoarkaive;

import java.util.Arrays;
import java.util.List;

import com.autoarkaive.communications.ArkaiveClass;
import com.autoarkaive.communications.CheckinRequest;

/**
 * Fake checkin queue that uses predetermined responses
 */
public class CheckinQueueMock extends CheckinQueue
{

	public CheckinQueueMock()
	{
		
	}
	
	
	@Override
	public void enqueueCheckin(CheckinRequest checkin)
	{
		System.out.println("Mock checkin: " + checkin.course + " for " + checkin.username);
	}

	@Override
	public boolean testLogin(String username, String password)
	{
		// always say it works
		return true;
	}

	@Override
	public List<ArkaiveClass> getClassList(String username, String password)
	{
		System.out.println("Mock class list for " + username);
		return Arrays.asList(new ArkaiveClass("Test Class 1", "AAAA"), new ArkaiveClass("Test Class 2", "XXXX"));
	}

	@Override
	public void shutdown()
	{
		// do nothing
	}


}
