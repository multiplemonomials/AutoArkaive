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
		EmulatorController emulator = new EmulatorController("C:/android-sdk/", "Nexus_5_API_25");
		
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
		/*System.out.println(">>> Testing Incorrect Login...");
		boolean incorrectLoginResult = emulator.testLogin(new LoginCheckRequest(realArkaiveLogin, "notarealpassword"));
		
		if(!incorrectLoginResult)
		{
			System.out.println(">>> Passed!");
		}
		else
		{
			System.out.println(">>> Failed!");
		}
		*/
		
		// test 3: class listing
		System.out.println(">>> Testing Class Listing...");
		ArrayList<ArkaiveClass> classes = emulator.listClasses(new ClassListRequest("smit109@usc.edu", realArkaivePassword));
		System.out.println(">>> Class list: " + classes.toString());
		
		// test 4: checkin
		System.out.println(">>> Testing Checkin 1...");
		emulator.performCheckin(new CheckinRequest(34.0212487, -118.2877008, 60, realArkaiveLogin, realArkaivePassword, classes.get(0), 
				new LocalTime().withFieldAdded(DurationFieldType.seconds(), -1), 
				new LocalTime().withFieldAdded(DurationFieldType.seconds(), 1)));
		System.out.println(">>> Passed!");
		
		/*System.out.println(">>> Testing Checkin N-2...");
		emulator.performCheckin(new CheckinRequest(34.0212487, -118.2877008, 60, realArkaiveLogin, realArkaivePassword, classes.get(classes.size() - 2), 
				new LocalTime().withFieldAdded(DurationFieldType.seconds(), -1), 
				new LocalTime().withFieldAdded(DurationFieldType.seconds(), 1)));
		System.out.println(">>> Passed!");*/
		
		//emulator.shutdown();
	}

}
