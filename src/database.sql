DROP DATABASE IF EXISTS arkaiveInfo;
CREATE DATABASE arkaiveInfo;
USE arkaiveInfo;

CREATE TABLE myUsers (
	fullname VARCHAR(60) PRIMARY KEY NOT NULL,
	email VARCHAR(60) NOT NULL,
	picurl VARCHAR(300)
	arkaive_username VARCHAR(515) NOT NULL,
	arkaive_password VARCHAR(515) NOT NULL
);


CREATE TABLE myClasses(
	checkinStartTime VARCHAR(300) NOT NULL,
	checkinEndTime VARCHAR(300) NOT NULL,
	latitude FLOAT(20,8) NOT NULL, --we can update this based on how precise the api gives us
	longitude FLOAT(20,8) NOT NULL, -- https://stackoverflow.com/questions/7167604/how-accurately-should-i-store-latitude-and-longitude
	altitude FLOAT(20,8) NOT NULL,
	courseCode VARCHAR(6) NOT NULL,
	classname VARCHAR(40) NOT NULL
);


/*
These are the requirements from Jamie's side


double latitude, longitude - latitude and longitude, in decimal degrees
int altitude - altitude in meters
String username, password - Arkaive login info
String courseCode - Course code (
LocalTime checkinStartTime, checkinEndTime - Time window where checkin can occur.

Note: must use Joda Time’s LocalTime instead of Java’s because Android does not support Java 8 yet 

*/