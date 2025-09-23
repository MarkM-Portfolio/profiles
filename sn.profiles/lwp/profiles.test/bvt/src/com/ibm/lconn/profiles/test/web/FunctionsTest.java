/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2016                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;

import com.ibm.peoplepages.functions.Functions;

public class FunctionsTest extends TestCase
{

	static String [] decodedStrings = {
		null
		, "No special chars"
		, "This is a test ' \" \b \f \n \r \t \\ \" \b \f \n \r \t \\"
	};
	static String [] encodedStrings = {
		null   // null expects null
		, "No special chars" // no change should be seen
		, "This is a test ' \\\" \\b \\f \\n \\r \\t \\\\ \\\" \\b \\f \\n \\r \\t \\\\" // encode the single characters.  make sure all are encoded
	};
	
	public void testFunction() throws Exception
	{
		Map<String,String> testData = initTestData();
		boolean trim  = true;
		boolean lower = true;

		System.out.println("\nProcess encoding json data");
		processTestData(testData);

	}
	
	private static void processTestData(Map<String, String> testData) {
		Set<String> keys = testData.keySet();
		String output = null;
		int i = 1;
		for (String key : keys){

			output = Functions.encodeForJsonString(key);

			String expected = testData.get(key);
			System.out.println("[" + i++ + "] " + "input: >" + (key==null?"null":key) + "<  output: " + (output==null?"null":output) + " expected: " + (expected==null?"null":expected));
			if (expected == null) {
				assertTrue(output == null);
			} else {
				assertTrue(expected.equals(output));
			}
		}
	}

	private static HashMap<String,String> initTestData(){
		HashMap<String,String> rtn = new HashMap<String,String>(100);	
		for (int i = 0; i < encodedStrings.length; i++) {
			rtn.put(decodedStrings[i], encodedStrings[i]);
		}
	    return rtn;
	}

}
