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


function doPost(request, response) {
	String command="";
	String json="";
	
	Parse json and set in the request parameters;
	Look up pretty printing if needed, for debugging
	
	Forward to appropriate method;
}