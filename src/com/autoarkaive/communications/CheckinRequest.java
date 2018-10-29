package com.autoarkaive.communications;

import java.io.Serializable;

import org.joda.time.LocalTime;

/**
 * A request to check the user into Arkaive.  Contains all the information needed to check someone in.
 * @author jamie
 *
 */
public class CheckinRequest implements Comparable<CheckinRequest>, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2906081294197564985L;

	/**
	 * latitude and longitude, in decimal degrees
	 */
	public final double latitude, longitude;
	
	/**
	 * altitude in meters
	 */
	public final int altitude;
	
	/**
	 * Arkaive login info
	 */
	public final String username, password;
	
	/**
	 * Name of course (must match what is displayed in app)
	 */
	public final String courseName;
	
	/**
	 * Time window where checkin can occur.
	 * 
	 * Note: must use Joda Time because Android does not support Java 8 yet and this class gets serialized
	 */
	public final LocalTime checkinStartTime, checkinEndTime;

	/**
	 * 
	 * Construct checkin entry from parameters as above.
	 * 
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 * @param username
	 * @param password
	 * @param courseName
	 */
	public CheckinRequest(double latitude, double longitude, int altitude, String username, String password,
						  String courseName, LocalTime checkinStartTime, LocalTime checkinEndTime)
	{
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.username = username;
		this.password = password;
		this.courseName = courseName;
		this.checkinStartTime = checkinStartTime;
		this.checkinEndTime = checkinEndTime;
	}

	/**
	 * Sort entries in ascending order of end time
	 */
	@Override
	public int compareTo(CheckinRequest other)
	{
		if(other == null)
		{
			return 1;
		}
		else
		{
			return checkinEndTime.compareTo(other.checkinEndTime);
		}
	}
	
	
}
