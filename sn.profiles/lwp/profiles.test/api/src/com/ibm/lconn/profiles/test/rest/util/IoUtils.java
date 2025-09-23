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

package com.ibm.lconn.profiles.test.rest.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class IoUtils {

	
	/**
	 * 
	 * @param closeable
	 */
	public final static void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			}
			catch (IOException e) {
				// ignore
			}
		}
	}

	public static byte[] readFileAsByteArray(Class<?> z, String filename) throws IOException{
		InputStream is = null;
		try{
			is = z.getResourceAsStream(filename);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int read;
			while ((read = is.read(buffer)) >= 0){
				bos.write(buffer, 0, read);
			}
			bos.flush();
			return bos.toByteArray();
		}
		finally {
			if (is != null) is.close();
		}
	}
}
