package com.autoarkaive;

import java.io.IOException;

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
	
	function addClass(request, response) {
		System.out.println();
		System.out.println("Adding a new class");

		String user_email = req.getParameter(); //Website should be responsible for this, keep in session
		String class_name;
		String arkaive_code;

		Latitude //determine types in database
		Longitude
		Altitude

		Days 
		Hour


		try {
			//Establish database connection

			String query = "Insert Into Classes"

		}
		catch() {

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
