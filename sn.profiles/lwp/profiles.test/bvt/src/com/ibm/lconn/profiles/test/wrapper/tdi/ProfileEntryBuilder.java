/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.wrapper.tdi;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import com.ibm.di.entry.Entry;
import com.ibm.lconn.profiles.test.util.IoUtil;

public class ProfileEntryBuilder {
	
	public static Entry buildEntry(String fileName){
		Entry profileEntry = new Entry();
		try{
			//URI fileURI = ProfileEntryBuilder.class.getResource(fileName).toURI();
			//FileInputStream fis = new FileInputStream(new File(fileURI));
			//Properties p = new Properties();
			//p.load(fis);
			Properties p = IoUtil.loadProperties(ProfileEntryBuilder.class, fileName);
			Enumeration<?> keys = p.propertyNames();
			while(keys.hasMoreElements()){
				String key = (String) keys.nextElement();
				String value = p.getProperty(key);
				if((value!=null) && (!value.equals("null")))
					profileEntry.addAttributeValue(key, value);
			}
		}catch(IOException e){
			e.printStackTrace();
		}//catch(URISyntaxException e){
		//	e.printStackTrace();
		//}
		return profileEntry;
	}
}
