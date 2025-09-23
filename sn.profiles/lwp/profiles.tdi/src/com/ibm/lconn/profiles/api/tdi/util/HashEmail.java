/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashEmail {
	// In this method: first argument will be user's email and second - an
	// algorithm ID: MD5 or SHA1
	public static String hashString(String email, String alg)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance(alg);

		// Before hashing email, we need to make it all in lower case
		md.update(email.toLowerCase().getBytes("iso-8859-1"), 0, email.length());

		// Converting the byte-array into a hex-string
		byte[] hash = md.digest();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			int b = hash[i];
			if (b < 0)
				b = 256 + b;
			if (b < 16)
				buf.append("0");
			buf.append(Integer.toHexString(b));
		}
		// Returning hash as a hex-string
		return buf.toString();
	}
}