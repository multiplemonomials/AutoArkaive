package com.autoarkaive.communications;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Denotes a result returning a list of classes.
 */
public class ResultClassList implements Serializable
{
	private ArrayList<ArkaiveClass> classes;

	public ResultClassList(ArrayList<ArkaiveClass> classes)
	{
		this.classes = classes;
	}

	public ArrayList<ArkaiveClass> getClassList()
	{
		return classes;
	}
}
