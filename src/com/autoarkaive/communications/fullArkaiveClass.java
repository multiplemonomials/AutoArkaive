package com.autoarkaive.communications;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class fullArkaiveClass {
	@SerializedName("classname")
	@Expose
	String classname;
	@SerializedName("courseCode")
	@Expose
	String courseCode;
	@SerializedName("checkinStartTime")
	@Expose
	String checkinStartTime;
	
	public fullArkaiveClass(String classname, String courseCode, String checkinStartTime){
		this.classname= classname;
		this.courseCode = courseCode;
		this.checkinStartTime = checkinStartTime;
	}
}