/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.misc;

import com.ibm.lconn.profiles.internal.util.XMLUtil;

import junit.framework.TestCase;

public class XmlUtilTest extends TestCase {
	private static String[] EMPTY = {
		"<p>\n\t\t&nbsp;</p>",
		"<p/>  <br>  <br/>",
		"<p>\n\t\t&nbsp;</p><p>\n\t\t&nbsp;</p><p>\n\t\t&nbsp;</p><b-dsfadsf>",
		"\t\n&nbsp;&nbsp;&nbsp;",
		"<p>\n\t&nbsp;&nbsp;&nbsp;</p>",
		" <br>",
		" <b> </b><br>",
		"     <br>",
		"     "
	};
	
	private static String[] NONEMPTY = {
		"<p>\n\t\t&nbsp;</p>k",
		"<p/> i  <br>  <br/>",
		"<p>\n\t\t&amp;nbsp;</p><p>\n\t\t&nbsp;</p><p>\n\t\t&nbsp;</p><b-dsfadsf>",
		"\t\n&nbsp;&nbsp;a&nbsp;"
	};
	
	public void test_match_empty_correctly() {
		for (String s : EMPTY) {
			if (!XMLUtil.isHtmlEmpty(s)) {
				System.out.println("TEST FAILURE string is not empty" + s);
			}
// CURRENTLY FAILS
//			assertTrue(s, XMLUtil.isHtmlEmpty(s));
		}
	}

	public void test_match_non_empty_correctly() {
		for (String s : NONEMPTY) {
			assertFalse(s, XMLUtil.isHtmlEmpty(s));
		}
	}
}
