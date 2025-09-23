/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.misc;

import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;

import com.ibm.lconn.profiles.test.BaseTestCase;


/**
 * @author zhouwen_lu@us.ibm.com
 *
 */
public class PhoneNumberUtilTest extends BaseTestCase 
{
	private static final String pureNumber =  "2345678900";
	private static final String phoneNumber1 = "1-234-567-8900";
	private static final String phoneNumber2 = "1 234 567 8900";
	private static final String phoneNumber3 = "1.234.567.8900";
	private static final String phoneNumber4 = "(234)567-8900";
	private static final String phoneNumber5 = "(234) 567 8900";
	private static final String phoneNumber6 = "(234) 567.8900";
	private static final String phoneNumber7 = "234-567-8900";
	private static final String phoneNumber8 = "234 567 8900";
	private static final String phoneNumber9 = "234.567.8900";
	private static final String phoneNumber10 = "+1 234-567-8900";
	private static final String phoneNumber11 = "+011 86 234-567-8900";

        private static final String phoneNumber12 = "1-888-IBM-HELP";
	
	public void testPhoneNumberConversion()
	{
	    assertEquals(ProfileSearchUtil.normalizePhoneNumber(phoneNumber1), pureNumber +" 12345678900");
	    assertEquals(ProfileSearchUtil.normalizePhoneNumber(phoneNumber2), pureNumber +" 12345678900");
	    assertEquals(ProfileSearchUtil.normalizePhoneNumber(phoneNumber3), pureNumber +" 12345678900");
	    assertEquals(ProfileSearchUtil.normalizePhoneNumber(phoneNumber4), pureNumber);
	    assertEquals(ProfileSearchUtil.normalizePhoneNumber(phoneNumber5), pureNumber);
	    assertEquals(ProfileSearchUtil.normalizePhoneNumber(phoneNumber6), pureNumber);
	    assertEquals(ProfileSearchUtil.normalizePhoneNumber(phoneNumber7), pureNumber);
	    assertEquals(ProfileSearchUtil.normalizePhoneNumber(phoneNumber8), pureNumber);
	    assertEquals(ProfileSearchUtil.normalizePhoneNumber(phoneNumber9), pureNumber);

	    assertEquals(ProfileSearchUtil.normalizePhoneNumber(phoneNumber10), "12345678900");
	    assertEquals(ProfileSearchUtil.normalizePhoneNumber(phoneNumber11), "011862345678900");

	    assertEquals(ProfileSearchUtil.normalizePhoneNumber(phoneNumber12), "8884264357 18884264357");
	}
}
