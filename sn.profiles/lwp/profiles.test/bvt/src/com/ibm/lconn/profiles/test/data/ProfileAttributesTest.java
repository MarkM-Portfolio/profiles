/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.ibm.lconn.profiles.data.ProfileAttributes;
import com.ibm.lconn.profiles.data.ProfileAttributes.Attribute;
import com.ibm.lconn.profiles.test.BaseTestCase;

/**
 *
 *
 */
public class ProfileAttributesTest extends BaseTestCase {
	
	private static final int BASE_ATTRS = 49;
	private static final int EXT_ATTRS = 10;
	
	// the test suite inject attributes via copy-config-files target and the properties below are
	// no longer in profiles-config.xml.  the only ootb attribute is 'profileLinks'
	// for now, do we just keep incrementing the number as tests change?
	public void testGetAll() {
		List<Attribute> attrs = ProfileAttributes.getAll();
		int baseAttrs = 0, extAttrs = 0;
		
		for (Attribute a : attrs) {
			if (a.isExtension()) extAttrs++;
			else baseAttrs++;
		}
		
		assertEquals(BASE_ATTRS, baseAttrs);
		assertEquals(EXT_ATTRS, extAttrs);
	}
	
	public void testDumpSimpleAttrsABCD() {
		List<Attribute> attrs = ProfileAttributes.getAll();
		
		List<String> ats = new ArrayList<String>();
		for (Attribute a : attrs)
			if (!a.isExtension())
				ats.add(a.getAttrId());
		
		String[] sa = ats.toArray(new String[0]);
		Arrays.sort(sa);
		
		for (String s : sa)
			System.out.println(s);
	}

}
