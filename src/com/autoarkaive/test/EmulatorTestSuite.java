package com.autoarkaive.test;

import java.util.ArrayList;

import org.joda.time.DurationFieldType;
import org.joda.time.LocalTime;

import com.autoarkaive.EmulatorController;
import com.autoarkaive.communications.ArkaiveClass;
import com.autoarkaive.communications.CheckinRequest;
import com.autoarkaive.communications.ClassListRequest;
import com.autoarkaive.communications.LoginCheckRequest;

/**
 * Class used to test the emulator manipulation code
 * @author jamie
 *
 */
public class EmulatorTestSuite
{
	
	public static void main(String[] args)
	{
		// to the tester: fill in your login here (and don't forget to remove before committing)
		String realArkaiveLogin = "smit109@usc.edu";
		String realArkaivePassword = "hollogarci";
		
		
		// test 1: startup
		System.out.println(">>> Starting Emulator...");
		EmulatorController emulator = new EmulatorController();
		
		// test 2: correct login check
		System.out.println(">>> Testing Correct Login...");
		boolean correctLoginResult = emulator.testLogin(new LoginCheckRequest(realArkaiveLogin, realArkaivePassword));
		
		if(correctLoginResult)
		{
			System.out.println(">>> Passed!");
		}
		else
		{
			System.out.println(">>> Failed!");
		}
		
		// test 2: incorrect login check
		System.out.println(">>> Testing Correct Login...");
		boolean incorrectLoginResult = emulator.testLogin(new LoginCheckRequest(realArkaiveLogin, "notarealpassword"));
		
		if(!incorrectLoginResult)
		{
			System.out.println(">>> Passed!");
		}
		else
		{
			System.out.println(">>> Failed!");
		}
		
		// test 2: incorrect login check
		System.out.println(">>> Testing Class Listing...");
		ArrayList<ArkaiveClass> classes = emulator.listClasses(new ClassListRequest("smit109@usc.edu", realArkaivePassword));
		System.out.println(">>> Class list: " + classes.toString());
		
		System.out.println(">>> Testing Checkin 1...");
		emulator.performCheckin(new CheckinRequest(34.021204, -118.287233, 60, realArkaiveLogin, realArkaivePassword, classes.get(0), 
				new LocalTime().withFieldAdded(DurationFieldType.seconds(), -1), 
				new LocalTime().withFieldAdded(DurationFieldType.seconds(), 1)));
		System.out.println(">>> Passed!");
		
		System.out.println(">>> Testing Checkin N-1...");
		emulator.performCheckin(new CheckinRequest(34.021204, -118.287233, 60, realArkaiveLogin, realArkaivePassword, classes.get(classes.size() - 1), 
				new LocalTime().withFieldAdded(DurationFieldType.seconds(), -1), 
				new LocalTime().withFieldAdded(DurationFieldType.seconds(), 1)));
		System.out.println(">>> Passed!");
		
		//emulator.shutdown();
	}

}
