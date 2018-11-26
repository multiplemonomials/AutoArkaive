package com.autoarkaive.communications;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class fullArkaiveClass {
	@SerializedName("classname")
	@Expose
	String classname;
	@SerializedName("couseCode")
	@Expose
	String couseCode;
	@SerializedName("checkinStartTime")
	@Expose
	String checkinStartTime;
}