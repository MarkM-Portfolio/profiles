/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2010                                      */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to retrieve a listing of both event & profile key values
 */
public class SearchEventProfileKey {
	
	/**
	 * Simple class for key type distinction
	 */
	public static final class KeyType {
		public static final int PROFILE = 0;
		public static final int EVENT = 1;
	}
	
	private String key;
	private int keyType;
	private Timestamp lastUpdate;
	
	/**
	 * @return the key
	 */
	public final String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public final void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the keyType
	 */
	public final int getKeyType() {
		return keyType;
	}
	/**
	 * @return the lastUpdate
	 */
	public final Timestamp getLastUpdate() {
		return lastUpdate;
	}
	/**
	 * @param lastUpdate the lastUpdate to set
	 */
	public final void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	/**
	 * @param keyType the keyType to set
	 */
	public final void setKeyType(int keyType) {
		this.keyType = keyType;
	}
	
	/**
	 * Utility method to extract list of a key type from the object
	 * @param keysForIndexing
	 * @param profile
	 * @return
	 */
	public static List<String> keysByType(List<SearchEventProfileKey> keysForIndexing, int keyType) {
		List<String> keys = new ArrayList<String>(keysForIndexing.size());
		for (SearchEventProfileKey key : keysForIndexing)
			if (key.getKeyType() == keyType)
				keys.add(key.getKey());		
		return keys;
	}
}
