DROP DATABASE IF EXISTS arkaiveInfo;
CREATE DATABASE arkaiveInfo;
USE arkaiveInfo;

CREATE TABLE myUsers (
	fullname VARCHAR(60) PRIMARY KEY NOT NULL,
	email VARCHAR(60) NOT NULL,
	picurl VARCHAR(300)
);


CREATE TABLE myClasses(
	classtime VARCHAR() NOT NULL,
	latitude FLOAT(20,8) NOT NULL, --we can update this based on how precise the api gives us
	longitude FLOAT(20,8) NOT NULL, -- https://stackoverflow.com/questions/7167604/how-accurately-should-i-store-latitude-and-longitude
	altitude FLOAT(20,8) NOT NULL,
	classcode VARCHAR(6) NOT NULL,
	classname VARCHAR(40) NOT NULL
);

