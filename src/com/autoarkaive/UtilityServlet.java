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
	
	
	public void checkUser(HttpServletRequest request, HttpServletRequest response) {
		//Returns json with true or false value
		//GsonResponse gr = new GsonResponse();
		//gr.add(boolean xyz)
		//return gr.toString() == JSON
		
		/* {
		*.   "arkaiveAccountExists": "true/false"
		*. }
		*/
		Connection conn = null;
		Statement st = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		//From Sai Allu Assignment 3
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/GoogleUsers?user=root&password=uhoi&useSSL=false");
			st = conn.createStatement();
			
			/* Checks if user is in database */
			String checkQuery = "SELECT COUNT(email) as count FROM TotalInfo WHERE email=?";
			PreparedStatement check = conn.prepareStatement(checkQuery);
			check.setString(1, request.getParameter("email"));
			rs = check.executeQuery();
			int count = 0;
			boolean found = false;
			
			while (rs.next()) {
				count = rs.getInt("count");
				
				if(count > 0)
					found = true;
				
				System.out.println ("Count = " + count);
			}
			
			
			
		} catch (SQLException sqle) {
			System.out.println ("SQLException: " + sqle.getMessage());
		} catch (ClassNotFoundException cnfe) {
			System.out.println ("ClassNotFoundException: " + cnfe.getMessage());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqle) {
				System.out.println("sqle: " + sqle.getMessage());
			}
		}
	}
	
	public void addUser(HttpServletRequest request, HttpServletRequest response) {
	    System.out.println();
	    System.out.println("Adding a new user");

	    String fullname = request.getParameter("fullname");
	    String email = request.getParameter("email");

	    String arkaive_username = request.getParameter("arkaive_username");
	    String arkaive_password = hash( request.getParameter("arkaive_password") );
	    String picurl = request.getParameter("picurl");

	    Connection conn = null;
	    Statement st = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;

		try {
			//Establish database connection
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://localhost/GoogleUsers?user=root&");
		st = conn.createStatement();

			String query = "Insert Into myUsers (fullname, email, picurl, arkaive_username, arkaive_password)"
					+ "values (?, ?, ?, ?, ?)";

		//Fill in the question marks
		preparedStmt.setString (1, fullname);
		preparedStmt.setString(2, email);
		preparedStmt.setString (3, picurl);
		preparedStmt.setString(4, arkaive_username);
		 preparedStmt.setString(5, arkaive_password);

		// execute the preparedstatement
		preparedStmt.execute();
		}
	    catch (SQLException sqle) {
		System.out.println ("SQLException: " + sqle.getMessage());
	    } catch (ClassNotFoundException cnfe) {
		System.out.println ("ClassNotFoundException: " + cnfe.getMessage());
	    } finally {
			try {
			    if (rs != null) {
			    rs.close();
			    }
			    if (st != null) {
			    st.close();
			    }
			    if (ps != null) {
			    ps.close();
			    }
			    if (conn != null) {
			    conn.close();
			    }
			} catch (SQLException sqle) {
				System.out.println("sqle: " + sqle.getMessage());
			}
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


	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doGet(request, response);
		
		String command="";
		command = request.getParameter("command");
		
		String json="";
		
		if( command.equals("addUser") )
			addUser(request, response);
		else if( command.equals("addClass") )
			addClass(request, response);
		else if(  command.equals("checkUser") )
			checkUser(request,response);
		else
			System.out.println("Invalid command : " + command);
	
		//Parse json and set in the request parameters;
		//Look up pretty printing if needed, for debugging
	
		//Forward to appropriate method;
	}

}
