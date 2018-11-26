package com.autoarkaive;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.autoarkaive.communications.ArkaiveClass;
import com.autoarkaive.communications.CheckinRequest;

public class databaseThread extends Thread{

	private static CheckinQueue cq = null;
	private static Properties p;
	
	public databaseThread(CheckinQueue cq) 
	{
		this.cq = cq;
		try {
			p = PropertiesCreator.readPropertyFile(System.getProperty("user.home") + "/SystemConfiguration.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getCourseCode(String classname){
		Connection conn= null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			Class.forName("com.mysql.jdbc.Driver"); //fully qualified class name of jdbc driver coming from the sql jdbc jar file
			conn = DriverManager.getConnection("jdbc:mysql://localhost/arkaiveInfo?user=" + p.getProperty("user") + "&password=" + p.getProperty("password") + "&useSSL=false");
			
			String query = "SELECT courseCode FROM myClasses c WHERE c.classname = ? ";
			ps = conn.prepareStatement(query);
			ps.setString(1,classname);
			rs = ps.executeQuery();
			String coursecode = "";
			if(rs.next()){
				coursecode = rs.getString("coursecode");
			}
			return coursecode;
			
		} catch(SQLException sqle){
			System.out.println("sqle1: "+sqle.getMessage());
		} catch(ClassNotFoundException cnfe){
			System.out.println("cnfe1: "+ cnfe.getMessage());
		}
		return "";
	}
	
	public void run(){
		//poll database every second
		//see if there's anyone that needs to be checked in now, using SQL
		
		Connection conn= null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			Class.forName("com.mysql.jdbc.Driver"); //fully qualified class name of jdbc driver coming from the sql jdbc jar file
			conn = DriverManager.getConnection("jdbc:mysql://localhost/arkaiveInfo?user=" + p.getProperty("user") + "&password=" + p.getProperty("password") + "&useSSL=false");
		
			while(true) {
				//convert current time to joda time's LocalTime
				DateTimeFormatter dateFormat = DateTimeFormat
				.forPattern("G,C,Y,x,w,e,E,Y,D,M,d,a,K,h,H,k,m,s,S,z,Z");

			LocalTime localtimeobjectnow = new LocalTime();
			String now = (localtimeobjectnow.now()).toString();

			//give precision flexibility-- cut off seconds
				now = now.substring(0,5);
				System.out.println("Current time is " + now);
			//


				String query = "SELECT * FROM myUsers as u , myClasses as c WHERE c.checkinStartTime < ? AND c.checkinEndTime > ? ";
				ps = conn.prepareStatement(query);
				ps.setString(1,now);
				ps.setString(2,now);
				rs = ps.executeQuery();

				while(rs.next()){
					//get the info for the users class
					//make a checkinrequest object

					double latitude = rs.getDouble("latitude");
					double longitude = rs.getDouble("longitude");
					int altitude = rs.getInt("altitude");
					String username = rs.getString("arkaive_username");
					String password = rs.getString("arkaive_password");
					String courseName = rs.getString("courseName");
					//now is the checkintime
					String starttime = rs.getString("checkinStartTime");
					String endtime= rs.getString("checkinEndTime");

					//TODO confirm that we can just pass a string to the object and it will make a jodatime
					DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
					LocalTime startdatetime = LocalTime.parse(starttime, formatter);
					LocalTime enddatetime = LocalTime.parse(endtime, formatter);

					String cc = getCourseCode(courseName);

					CheckinRequest cir = new CheckinRequest(latitude,longitude,altitude,username,password,new ArkaiveClass(courseName, cc),startdatetime,enddatetime);


					cq.enqueueCheckin(cir);
				}
			} //end of while(true)
				
		
		} catch(SQLException sqle){
			System.out.println("sqle1: "+sqle.getMessage());
		} catch(ClassNotFoundException cnfe){
			System.out.println("cnfe1: "+ cnfe.getMessage());
		}		
	}

}
