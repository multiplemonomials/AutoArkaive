package com.autoarkaive.communications;

/**
 * Request to get a list of courses and their codes for the given user off of the app
 */
public class ClassListRequest
{
	/**
	 * Arkaive login info
	 */
	public final String username, password;

	public ClassListRequest(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
}
