package com.autoarkaive.test;

import java.util.Scanner;

import com.autoarkaive.EmulatorController;

/**
 * Class used to test the emulator manipulation code
 * @author jamie
 *
 */
public class EmulatorTestSuite
{
	
	private static Scanner inputScanner;
	
	private static void testLaunch()
	{
		EmulatorController emulator = new EmulatorController();
		inputScanner.next();
	}

	public static void main(String[] args)
	{
		inputScanner = new Scanner(System.in);
		
		testLaunch();
	}

}
