package com.autoarkaive.communications;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ArkaiveClass implements Serializable
{
    @SerializedName("className")
    @Expose
    public String className;
    @SerializedName("courseCode")
    @Expose
    public String courseCode;

    public ArkaiveClass(String className, String courseCode)
	{
		this.className = className;
		this.courseCode = courseCode;
	}

    public String toString()
    {
        return courseCode + ": " + className;
    }

    // functions to allow ArkaiveClass to be used as a map key
    @Override
    public int hashCode()
    {
        return courseCode.hashCode() + 37 * className.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof ArkaiveClass)
        {
            ArkaiveClass otherClass = (ArkaiveClass)obj;
            return otherClass.className.equals(className) && otherClass.courseCode.equals(courseCode);
        }

        return false;
    }
}