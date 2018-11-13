package com.autoarkaive.communications;

public class ResultFailure
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2673334829193142750L;
	/**
	 * Reason why the checkin failed
	 */
	private String failureMessage;
	
	public ResultFailure(String failureMessage)
	{
		this.failureMessage = failureMessage;
	}

	public String getFailureMessage()
	{
		return failureMessage;
	}

}
