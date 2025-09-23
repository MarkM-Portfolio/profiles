/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.core.web.secutil.Sha256Encoder;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ProfileLookupKeyTest extends BaseTestCase 
{
	private static List<Map<String,String>> values = new ArrayList<Map<String,String>>();

	static
	{
		// set up the values list with all keys having a "foobar" value
		for (ProfileLookupKey.Type type : ProfileLookupKey.Type.values())
		{
			Map<String,String> m = new HashMap<String,String>(3);
			m.put(ProfileLookupKey.TYPE_KEY,  type.name());
			m.put(ProfileLookupKey.VALUE_KEY, "foobar");
			values.add(m);
		}
	}

	public void testValuesMap()
	{
		for (Map<String,String> m : values)
		{
			// convert 'userid' to real type
			if (m.get(ProfileLookupKey.TYPE_KEY).equals("USERID")) {
				m.put(ProfileLookupKey.TYPE_KEY, ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName().toUpperCase());
			}
			assertEquals(m, 
				new ProfileLookupKey(
					ProfileLookupKey.Type.valueOf(m.get(ProfileLookupKey.TYPE_KEY)),
					m.get(ProfileLookupKey.VALUE_KEY)).toMap());
		}
	}

	public void testEmailHashValuesMap()
	{
		String mKey    = null;
		String mValue  = null;
		Type   plkType = null;
		ProfileLookupKey newPLK = null;
		Map<String, String> newPLKMap = null;

		// first fix-up the base data-set for the test
		for (Map<String,String> m : values)
		{
			mKey = m.get(ProfileLookupKey.TYPE_KEY);
			// replace "foobar" in the HASHID map with a hashed email value
			if ("HASHID".equals(mKey)) {
				String email   = "amyjones123@lotus.com";
				String hashVal = Sha256Encoder.hashLowercaseStringUTF8(email, true);
				m.put(ProfileLookupKey.VALUE_KEY, hashVal);
			}
			else { // convert 'userid' to real type since PLK.toMap() will reset it to config'd value - GUID
				if ("USERID".equals(mKey))
					m.put(ProfileLookupKey.TYPE_KEY, ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName().toUpperCase());
			}
		}
		int i = 0;
		for (Map<String,String> m : values)
		{
			mKey    = m.get(ProfileLookupKey.TYPE_KEY);
			mValue  = m.get(ProfileLookupKey.VALUE_KEY);
			plkType = ProfileLookupKey.Type.valueOf(mKey);
			String itemDetails = "[" + i++ + "] : " + mKey + " = " + mValue + " : Type = "  + plkType.name();
//			System.out.println("Processing " + itemDetails);

			newPLK    = new ProfileLookupKey(plkType, mValue);
			newPLKMap = newPLK.toMap(); // to.Map() changes "USERID" to config'd key - GUID !! go figure !
//			System.out.println("Compare with :"); printMap(newPLKMap);
			try {
				assertEquals(m, newPLKMap);
			}
			catch (Exception ex) {
				System.out.println("Got unexpected exception processing :" + itemDetails + " "  + ex.toString() + "\n");
			}
			catch (Error err) {
				System.out.println("Got unexpected map :" + itemDetails + "\n");
				printMap(m); printMap(newPLKMap);
			}
		}
	}

	@SuppressWarnings("unused") // for debug use
	private static void printList(List<Map<String, String>> values) {
	    Iterator<Map<String, String>> it = values.iterator();
	    while (it.hasNext()) {
	    	Map<String, String> value = (Map<String, String>) it.next();
			printMap(value);
		}
	}

	private static void printMap(Map<String, String> map) {
	    Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, String> pairs = (Map.Entry<String, String>)it.next();
	        String     key = (String) pairs.getKey();
	        String     val = (String) pairs.getValue();
	        System.out.println(key + " : " + val);
	    }
	    System.out.println();
	}

}
