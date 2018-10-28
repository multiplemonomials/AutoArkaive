package com.autoarkaive.checkindata;

public class CheckinResultFailure implements CheckinResult 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2673334829193142750L;
	/**
	 * Reason why the checkin failed
	 */
	private String failureMessage;
	
	public CheckinResultFailure(String failureMessage) 
	{
		this.failureMessage = failureMessage;
	}

	@Override
	public boolean succeeded() {
		return false;
	}
	
	public String getFailureMessage()
	{
		return failureMessage;
	}

}
