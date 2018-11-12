package com.autoarkaive;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/UtilityServlet")
public class UtilityServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	private static CheckinQueue cq = null;
	
	
	public String checkUser(HttpServletRequest request, HttpServletResponse response) {
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
			String checkQuery = "SELECT COUNT(email) as count FROM myUsers WHERE email=?";
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
			
			return "{ arkaiveAccountExists: " + found + " }"; 
			
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
		return "";
	}
	
	public String addUser(HttpServletRequest request, HttpServletResponse response) {
	    System.out.println();
	    System.out.println("Adding a new user");

	    String fullname = request.getParameter("fullname");
	    String email = request.getParameter("email");

	    String arkaive_username = request.getParameter("arkaive_username");
	   
	    String arkaive_password = Hashing.get_SHA_1_SecurePassword( request.getParameter("arkaive_password") );
	    //String arkaive_password = hash( request.getParameter("arkaive_password") );
	    String picurl = request.getParameter("picurl");
		
	    if(!cq.testLogin(arkaive_username,arkaive_password)){
		return "{ isValidArkaiveAccount: false }";    
	    }

	    Connection conn = null;
	    Statement st = null;
	    PreparedStatement preparedStmt = null;
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
			    if (preparedStmt != null) {
			    	preparedStmt.close();
			    }
			    if (conn != null) {
			    conn.close();
			    }
			} catch (SQLException sqle) {
				System.out.println("sqle: " + sqle.getMessage());
			}
	    }
		
		return "{ isValidArkaiveAccount: true }";
	}
	
	
	
	public String addClass(HttpServletRequest request, HttpServletResponse response) {
		System.out.println();
		System.out.println("Adding a new class");
		
		PreparedStatement ps = null;
		Connection conn = null;
		ResultSet rs= null;
		try{
			
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/CalendarInfo?user=root&password=*****&useSSL=false");
			
			
			String insertstatement = "INSERT INTO myClasses(checkinStartTime,checkinEndTime,latitude,longitude,altitude,courseCode,classname) "
					+ "					VALUES (?,?,?,?,?,?,?)";
			
			if((request.getParameter("checkinStartTime")).equals("") || (request.getParameter("checkinStartTime")).equals(null) )
			   	return "{classWasAdded: false}";
			else if((request.getParameter("checkinEndTime")).equals("") || (request.getParameter("checkinEndTime")).equals(null) )
				return "{classWasAdded: false}";
			else if((request.getParameter("latitude")).equals("") || (request.getParameter("latitude")).equals(null) )
				return "{classWasAdded: false}";
			else if((request.getParameter("longitude")).equals("") || (request.getParameter("longitude")).equals(null) )
				return "{classWasAdded: false}";
			else if((request.getParameter("altitude")).equals("") || (request.getParameter("altitude")).equals(null) )
				return "{classWasAdded: false}";
			else if((request.getParameter("courseCode")).equals("") || (request.getParameter("courseCode")).equals(null) )
				return "{classWasAdded: false}";
			else if((request.getParameter("classname")).equals("") || (request.getParameter("classname")).equals(null) )
				return "{classWasAdded: false}";
			else
				System.out.println("Class was added");
				
			
			ps = conn.prepareStatement(insertstatement);
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
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}finally{
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
	public String fetchClasses(String arkaive_username, String arkaive_password){
		//call database to get the arkaive username and password from teh email
		//call the class list function
		//transform into json
		//return jsonn
		
		//expecting that getClassList will return an arraylist of ArkaiveClass objects
		
		ArrayList<ArkaiveClass> finalList = getClassList(arkaive_username, arkaive_password);
	

		String jsonoutput = new Gson().toJson(finalList);
		
		return jsonoutput;		
	}
	
	public ArrayList<String> getUsernameAndPassword(String email){
		Connection conn = null;
		Statement st = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		//From Sai Allu Assignment 3
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/GoogleUsers?user=root&password=uhoi&useSSL=false");
			st = conn.createStatement();
			
			ArrayList<String> userandpass = new ArrayList<String>();
			
			/* Checks if user is in database */
			String checkQuery = "SELECT arkaive_username, arkaive_password FROM myUsers WHERE email=?";
			PreparedStatement check = conn.prepareStatement(checkQuery);
			check.setString(1, email);
			rs = check.executeQuery();
			
			
			if (rs.next()) {
				userandpass.add(rs.getString("arkaive_username"));
				userandpass.add(rs.getString("arkaive_password"));
			}
			else{
				userandpass.add("didnotfindusername");
				userandpass.add("didnotfindpassword");
			}
			return userandpass;
		
			
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
		return null;
	}


	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doGet(request, response);
		
		String command="";
		command = request.getParameter("command");
		
		String json="";
		
		if(cq == null){
			cq = new CheckinQueue();
			databaseThread dbt = new databaseThread(cq);
			dbt.start();
		}
		
		ArrayList<String> userandpass = new ArrayList<String>();
		
		if( command.equals("addUser") )
			addUser(request, response);
		else if( command.equals("addClass") )
			addClass(request, response);
		else if(  command.equals("checkUser") )
			checkUser(request,response);
		else if( command.equals("fetchclasses"))
			userandpass = getUsernameAndPassword(request.getParameter("email"));
			String username = userandpass.get(0);
			String password = userandpass.get(1);
			fetchClasses(username, password);
		else
			System.out.println("Invalid command : " + command);
	
		//Parse json and set in the request parameters;
		//Look up pretty printing if needed, for debugging
	
		//Forward to appropriate method;
	}

}
