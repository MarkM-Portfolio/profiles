/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.ibm.lconn.profiles.test.service.photo.PhotoServiceTest;

public class IoUtil {

	public static Properties loadProperties(Class<?> z, String filename) throws IOException {
		FileInputStream is = null;
		try {
			Properties rtnVal = new Properties();
			rtnVal.load(z.getResourceAsStream(filename));
			return rtnVal;
		}
		finally {
			if (is != null) is.close();
		}
	}

	public static Properties loadProperties(String filename) throws IOException {
		FileInputStream is = null;
		try {
			is = new FileInputStream(filename);
			Properties rtnVal = new Properties();
			rtnVal.load(is);
			return rtnVal;
		}
		finally {
			if (is != null) is.close();
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
