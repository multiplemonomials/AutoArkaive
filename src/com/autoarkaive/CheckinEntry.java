package com.autoarkaive;

import java.time.LocalTime;

/**
 * Represents the information needed to check someone in to Arkaive
 * @author jamie
 *
 */
public class CheckinEntry implements Comparable<CheckinEntry>
{
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
	 * Time window where checkin can occur
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
	public CheckinEntry(double latitude, double longitude, int altitude, String username, String password,
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
	public int compareTo(CheckinEntry other) 
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
