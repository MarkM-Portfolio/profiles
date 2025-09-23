/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author user
 *
 */
public class UrlHelper {
	/**
	 * UTF8 URL encodes input string
	 * @param s
	 * @return
	 */
	public static final String urlEncode(String s) {
		try {
			return URLEncoder.encode(s,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should be unreachable...
			throw new RuntimeException(e);
		}
	}
}
