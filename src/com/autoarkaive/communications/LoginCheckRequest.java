package com.autoarkaive.communications;

/**
 * Request to check if a given Arkaive username and password work.
 */
public class LoginCheckRequest
{
	/**
	 * Arkaive login info
	 */
	public final String username, password;

	public LoginCheckRequest(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
}
