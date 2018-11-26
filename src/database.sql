DROP DATABASE IF EXISTS arkaiveInfo;
CREATE DATABASE arkaiveInfo;
USE arkaiveInfo;

CREATE TABLE myUsers (
	fullname VARCHAR(60) PRIMARY KEY NOT NULL,
	email VARCHAR(60) NOT NULL,
	picurl VARCHAR(300),
	arkaive_username VARCHAR(515) NOT NULL,
	arkaive_password VARCHAR(515) NOT NULL
);


CREATE TABLE myClasses(
	email VARCHAR(60) NOT NULL,,
	checkinStartTime VARCHAR(300) NOT NULL,
	checkinEndTime VARCHAR(300) NOT NULL,
	latitude DOUBLE(20,8) NOT NULL, 
	longitude DOUBLE(20,8) NOT NULL, 
	altitude INT(50) NOT NULL,
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
