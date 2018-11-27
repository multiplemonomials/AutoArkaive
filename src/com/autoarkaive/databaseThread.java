package com.autoarkaive;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.autoarkaive.communications.ArkaiveClass;
import com.autoarkaive.communications.CheckinRequest;

public class databaseThread extends Thread{

	private static CheckinQueue cq = null;
	private static Properties p;
	private HashSet<String> alreadyCheckedIn;
	
	Connection conn= null;
	
	PreparedStatement courseCodePS, checkinPS;
	
	
	public databaseThread(CheckinQueue cq) 
	{
		this.cq = cq;
		alreadyCheckedIn = new HashSet<String>();
		try {
			p = PropertiesCreator.readPropertyFile(System.getProperty("user.home") + "/SystemConfiguration.properties");
			
			Class.forName("com.mysql.jdbc.Driver"); //fully qualified class name of jdbc driver coming from the sql jdbc jar file
			conn = DriverManager.getConnection("jdbc:mysql://localhost/arkaiveInfo?user=" + p.getProperty("user") + "&password=" + p.getProperty("password") + "&useSSL=false");
		
			courseCodePS = conn.prepareStatement("SELECT courseCode FROM myClasses c WHERE c.classname = ? ");
			checkinPS = conn.prepareStatement("SELECT * FROM myUsers as u , myClasses as c WHERE c.checkinStartTime < ? AND c.checkinEndTime > ? ");
			
		} catch (IOException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String getCourseCode(String classname){
		ResultSet rs = null;
		try 
		{
			courseCodePS.setString(1, classname);
			rs = courseCodePS.executeQuery();
			String coursecode = "";
			if(rs.next()){
				coursecode = rs.getString("coursecode");
			}
			return coursecode;
			
		} 
		catch(SQLException sqle)
		{
			System.out.println("sqle1: "+sqle.getMessage());
			sqle.printStackTrace();
		} 
		return "";
	}
	
	public void run()
	{
		//poll database every second
		//see if there's anyone that needs to be checked in now, using SQL
		
		ResultSet rs = null;
		try {
						
			while(true) {
				//convert current time to joda time's LocalTime
				LocalTime localtimeobjectnow = new LocalTime();
				String now = (localtimeobjectnow.now()).toString();
	
				//give precision flexibility-- cut off seconds
				now = now.substring(0,5);
				//System.out.println("Current time is " + now);
				//
				
				checkinPS.setString(1,now);
				checkinPS.setString(2,now);
				rs = checkinPS.executeQuery();

				while(rs.next()){
					//get the info for the users class
					//make a checkinrequest object

					double latitude = rs.getDouble("latitude");
					double longitude = rs.getDouble("longitude");
					int altitude = rs.getInt("altitude");
					String username = rs.getString("arkaive_username");
					String password = rs.getString("arkaive_password");
					String courseName = rs.getString("classname");
					//now is the checkintime
					String starttime = rs.getString("checkinStartTime");
					String endtime= rs.getString("checkinEndTime");

					//TODO confirm that we can just pass a string to the object and it will make a jodatime
					DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
					LocalTime startdatetime = LocalTime.parse(starttime, formatter);
					LocalTime enddatetime = LocalTime.parse(endtime, formatter);

					String cc = getCourseCode(courseName);
					
					String usernameAndCourseCode = username+cc;

					CheckinRequest cir = null;
					if(!alreadyCheckedIn.contains(usernameAndCourseCode)) {
						cir = new CheckinRequest(latitude,longitude,altitude,username,password,new ArkaiveClass(courseName, cc),startdatetime,enddatetime);
						cq.enqueueCheckin(cir);
					}

					alreadyCheckedIn.add(usernameAndCourseCode);
				}
				
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					return;
				}
			} //end of while(true)
				
		
		} 
		catch(SQLException sqle){
			System.out.println("sqle1: "+sqle.getMessage());
			sqle.printStackTrace();
		}
	}

}
