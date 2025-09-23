/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.misc;

import java.util.regex.Pattern;

import com.ibm.lconn.core.web.util.lang.LanguageFilter;
import com.ibm.lconn.profiles.test.BaseTestCase;


/**
 * @author ahernm@us.ibm.com
 *
 */
public class LanguageFilterMatcherTest extends BaseTestCase
{
	Pattern p = LanguageFilter.LANG_PATTERN;
	
	static final String[] good =
	{
		"en",
		"EN",
		"en-us",
		"En_uS",
		"CN_fr-ad"
	};
	
	static final String[] bad =
	{
		"e1",
		"1N",
		"en-us1",
		"En_u2",
		"CN_fr-a3",
		"()"
	};
	
	public void testMatches()
	{
		for (String g : good)
		{
			assertTrue(p.matcher(g).matches());
		}
		
		for (String b : bad)
		{
			if (p.matcher(b).matches())
				fail(b);
		}
	}
}
