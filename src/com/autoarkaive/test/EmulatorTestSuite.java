package com.autoarkaive.test;

import com.autoarkaive.EmulatorController;
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
		String realArkaivePassword = "xxxxxxxx";
		
		
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
		
		if(correctLoginResult)
		{
			System.out.println(">>> Passed!");
		}
		else
		{
			System.out.println(">>> Failed!");
		}
		
		// test 2: incorrect login check
		//System.out.println(">>> Testing Class Listing...");
		//ArrayList<ArkaiveClass> classes = emulator.listClasses(new ClassListRequest("smit109@usc.edu", realArkaivePassword));
		//System.out.println(">>> Class list: " + classes.toString());
		
		emulator.shutdown();
	}

}
