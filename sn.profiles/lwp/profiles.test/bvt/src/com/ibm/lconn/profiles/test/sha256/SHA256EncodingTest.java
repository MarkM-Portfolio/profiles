/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2105                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.sha256;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;

//import junit.framework.TestCase;
//import com.ibm.lconn.profiles.test.BaseTestCase;

import com.ibm.lconn.core.web.secutil.Sha256Encoder;

public class SHA256EncodingTest 
 extends TestCase
// extends BaseTestCase
{
	// a sha256 encoder returns 64 bytes, which can be represented by 64 hex characters.
	// as per security team instructions, the infra sha256 encoder is to return the first 32 bytes in a
	// hex char representation. by default it uses a UTF8 encoding.
	// add to the test cases by inserting a value in the 'key' array and the corresponding result in the 'result' array
	// be sure to clip the results. e.g. (using http://www.sha1-online.com/) the 32 char sha256 hash of abc is the following
	//  abc -> ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad
	// the infra encoder will clip that to be
	//  abc -> ba7816bf8f01cfea414140de5dae2223
	// which is entered in the results array corresponding to the input key abc

	static String [] sha256Key = {
		null
		, ""
		, "a"
		, "A"
		, "abc"
		, "ABC"
		, "message digest"
		, "abcdefghijklmnopqrstuvwxyz"
		, "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
		, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
		, "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
		, "amyjones1123@lotus.com"           // 30f6059f238bfda37fca04f990353422
		, "AmyJones1123@lotus.com"           // 80306f90d8007829baa65e31e32d4aef
		, "AMYJONES1123@lotus.com",          // a7abc846272dda07c3f805991965f463
	};
	static String [] sha256Result = {
		"e3b0c44298fc1c149afbf4c8996fb924"   // sha256 encoder encodes null the same as ""
		, "e3b0c44298fc1c149afbf4c8996fb924" // ""
		, "ca978112ca1bbdcafac231b39a23dc4d" // "a" - note this is same as next one
		, "ca978112ca1bbdcafac231b39a23dc4d" // "A"
		, "ba7816bf8f01cfea414140de5dae2223" // "abc" - note this is same as next one
		, "ba7816bf8f01cfea414140de5dae2223" // "ABC"
		, "f7846f55cf23e14eebeab5b4e1550cad" // "message digest"
		, "71c480df93d6ae2f1efad1447c66c952" // "abcdefghijklmnopqrstuvwxyz"
		, "248d6a61d20638b8e5c026930c3e6039" // "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
		, "3717c240875cb81be8d9b965c4e3e6aa" // "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
		, "f371bc4a311f2b009eef952dd83ca80e" // F371BC4A311F2B009EEF952DD83CA80E
		, "30f6059f238bfda37fca04f990353422" // "amyjones1123@lotus.com" - note this is same as next one
		, "30f6059f238bfda37fca04f990353422" // "AmyJones1123@lotus.com" - note this is same as next one
		, "30f6059f238bfda37fca04f990353422" // "AMYJONES1123@lotus.com"
	};
	static int numSHA256Items = sha256Result.length;

	static String [] sha256NoTrimKey = {
		""
		, " "
		, " a "
	};
	static String [] sha256NoTrimResult = {
		  "e3b0c44298fc1c149afbf4c8996fb924" // ""
		, "36a9e7f1c95b82ffb99743e0c5c4ce95" // " "
		, "cbf7f30004f3667cb093b3c7b55169a9" // " a "
	};
	static int numSHA256NoTrimItems = sha256NoTrimResult.length;

	static String [] sha256NoLowerKey = {
		null
		, ""
		, "a"
		, "A"
		, "abc"
		, "ABC"
		, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
		, "amyjones1123@lotus.com"           // 30f6059f238bfda37fca04f990353422
		, "AmyJones1123@lotus.com"           // 80306f90d8007829baa65e31e32d4aef
		, "AMYJONES1123@lotus.com",          // a7abc846272dda07c3f805991965f463
	};
	static String [] sha256NoLowerResult = {
		"e3b0c44298fc1c149afbf4c8996fb924"   // sha256 encoder treats null to be ""
		, "e3b0c44298fc1c149afbf4c8996fb924" // ""
		, "ca978112ca1bbdcafac231b39a23dc4d" // "a" - note this is different from next one
		, "559aead08264d5795d3909718cdd05ab" // "A"
		, "ba7816bf8f01cfea414140de5dae2223" // "abc" - note this is different from next one
		, "b5d4045c3f466fa91fe2cc6abe79232a" // "ABC"
		, "db4bfcbd4da0cd85a60c3c37d3fbd880" // "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
		, "30f6059f238bfda37fca04f990353422" // "amyjones1123@lotus.com" - note this is different from next one
		, "80306f90d8007829baa65e31e32d4aef" // "AmyJones1123@lotus.com" - note this is different from next one
		, "a7abc846272dda07c3f805991965f463" // "AMYJONES1123@lotus.com"
	};
	static int numSHA256NoLowerItems = sha256NoLowerResult.length;

	public void testSHA256() throws Exception
//	public static void main(String[] args)
	{
		Map<String,String> testData = initTestData();
		boolean trim  = true;
		boolean lower = true;

		System.out.println("\nProcess Profile sample data (trim & lowercase)");
		processTestData(testData);

		Map<String,String> testNoTrimData = initTestNoTrimData();
		trim = false;
		System.out.println("\nProcess no trim & lowercase");
		processTestData(testNoTrimData, trim);

		Map<String,String> testNoLowerData = initTestNoLowerData();
		trim  = false;
		lower = false;
		System.out.println("\nProcess as is (no trim & no lowercase)");
		processTestData(testNoLowerData, trim, lower);
	}
	
	private static void processTestData(Map<String, String> testData) {
		processTestData(testData, true);
	}
	
	private static void processTestData(Map<String, String> testData, boolean trim)	{
		processTestData(testData, trim, true);
	}
	
	private static void processTestData(Map<String, String> testData, boolean trim, boolean lower) {
		Set<String> keys = testData.keySet();
		String output = null;
		int i = 1;
		for (String key : keys){
			if (lower)
				output = Sha256Encoder.hashLowercaseStringUTF8(key, trim); // default Profiles use case
			else
				output = Sha256Encoder.hashString(key, Sha256Encoder.CHARSET_UTF8, trim, lower);

			String expected = testData.get(key);
			System.out.println("[" + i++ + "] " + "input: >" + key + "<  output: " + output + " equals: " + expected.equals(output));
			assertTrue(expected.equals(output));
		}
	}

	private static HashMap<String,String> initTestData(){
		HashMap<String,String> rtn = new HashMap<String,String>(100);	
		for (int i = 0; i < sha256Result.length; i++) {
			rtn.put(sha256Key[i], sha256Result[i]);
		}
	    return rtn;
	}

	private static Map<String, String> initTestNoTrimData() {
		HashMap<String,String> rtn = new HashMap<String,String>(100);	
		for (int i = 0; i < sha256NoTrimResult.length; i++) {
			rtn.put(sha256NoTrimKey[i], sha256NoTrimResult[i]);
		}
	    return rtn;
	}
	
	private static Map<String, String> initTestNoLowerData() {
		HashMap<String,String> rtn = new HashMap<String,String>(100);	
		for (int i = 0; i < sha256NoLowerResult.length; i++) {
			rtn.put(sha256NoLowerKey[i], sha256NoLowerResult[i]);
		}
	    return rtn;
	}
}
