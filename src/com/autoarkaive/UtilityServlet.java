package com.autoarkaive;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/UtilityServlet")
public class UtilityServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	function addUser(request, response) {
		System.out.println();
		System.out.println("Adding a new class");

		String email;
		String profile_image;
		String name;

		try {
			//Establish database connection

			String query = "Insert Into Users"

		}
		catch() {

		}
	}
	
	
	
	public void addClass(HttpServletRequest request, HttpServletRequest response) {
		System.out.println();
		System.out.println("Adding a new class");
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
			
			conn = Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/CalendarInfo?user=root&password=*****&useSSL=false");
			
			
			String insertstatement = "INSERT INTO myClasses(checkinStartTime,checkinEndTime,latitude,longitude,altitude,courseCode,classname) "
					+ "					VALUES (?,?,?,?,?,?,?)";
			
			ps = conn.pepareStatment(insertstatement);
			ps.setString(1, request.getParameter("checkinStartTime"));
			ps.setString(2, request.getParameter("checkinEndTime"));
			ps.setString(3, request.getParameter("latitude"));
			ps.setString(4, request.getParameter("longitude"));
			ps.setString(5, request.getParameter("altitude"));
			ps.setString(6, request.getParameter("courseCode"));
			ps.setString(7, request.getParameter("classname"));
			ps.executeUpdate();
			
			
		}catch(SQLException sqle){
			System.out.println("sqle yo");
		} finally{
			try{
				if(rs != null){
					rs.close();
				}
				if(ps != null){
					ps.close();
				}
			} catch(SQLException sqle){
				sqle.printStackTrace(); 
			}
		}

	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
		
		String command="";
		String json="";
	
		//Parse json and set in the request parameters;
		//Look up pretty printing if needed, for debugging
	
		//Forward to appropriate method;
	}

}
