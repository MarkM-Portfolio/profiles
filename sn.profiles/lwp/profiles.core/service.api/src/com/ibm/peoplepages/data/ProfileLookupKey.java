/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.LookupValidator;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ProfileLookupKey 
{
	public final static String TYPE_KEY = "lookupType";
	public final static String VALUE_KEY = "lookupValue";
	
	public final static String SOURCE_TYPE_KEY = "sourceLookupType";
	public final static String SOURCE_VALUE_KEY = "sourceLookupValue";

	public final static String TARGET_TYPE_KEY = "targetLookupType";
	public final static String TARGET_VALUE_KEY = "targetLookupValue";

	
	public static enum Type
	{
		KEY(36,36),
		UID(256),
		GUID(256),
		EMAIL(256),
		USERID(256), // AKA 'lconn-user-id'
		DN(256),
		MCODE(16,256);
		
		private final int minLength;
		private final int maxLength;
		
		private Type(int minLength, int maxLength) {
			this.minLength = minLength;
			this.maxLength = maxLength;
		}
		
		private Type(int maxLength) {
			this(1, maxLength);
		}

		/**
		 * @return the maxLength
		 */
		public final int getMaxLength() {
			return maxLength;
		}

		/**
		 * @return the minLength
		 */
		public final int getMinLength() {
			return minLength;
		}
	};
	
	public static final Map<String, String> DEFAULT_MAPPING;
	
	public static final Map<String, String> SOURCE_KEY_MAPPING;
	
	public static final Map<String, String> TARGET_KEY_MAPPING;

	static
	{
		Map<String,String> dm = new HashMap<String,String>(3);
		dm.put(TYPE_KEY, TYPE_KEY);
		dm.put(VALUE_KEY, VALUE_KEY);
		DEFAULT_MAPPING = Collections.unmodifiableMap(dm);
		
		dm = new HashMap<String,String>(3);
		dm.put(TYPE_KEY, SOURCE_TYPE_KEY);
		dm.put(VALUE_KEY, SOURCE_VALUE_KEY);
		SOURCE_KEY_MAPPING = Collections.unmodifiableMap(dm);		
		
		dm = new HashMap<String,String>(3);
		dm.put(TYPE_KEY, TARGET_TYPE_KEY);
		dm.put(VALUE_KEY, TARGET_VALUE_KEY);
		TARGET_KEY_MAPPING = Collections.unmodifiableMap(dm);
	}
	
	private Type type;
	private String value;
	
	public ProfileLookupKey(Type type, String value)
	{		
		AssertionUtils.assertNotNull(type);
		AssertionUtils.assertNotEmpty(value);
		
		this.type = type;
		if (null != value)
			this.value = value.trim();
	}

	public Type getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	/**
	 * Returns a map of version of this object using the default 're-mapping'
	 * 
	 * ("type", type)
	 * ("value", value)
	 * 
	 * @return
	 */
	public Map<String,String> toMap()
	{
		return toMap(DEFAULT_MAPPING);
	}
	
	/**
	 * Returns a map version of this object using a specified 're-mapping'
	 * 
	 * @param remapping
	 * @return
	 */
	public Map<String,String> toMap(Map<String, String> remapping)
	{	
		
		
		Map<String,String> values = new HashMap<String,String>(3);
		values.put(remapping.get(TYPE_KEY).toString(), getRealType().toString());
		values.put(remapping.get(VALUE_KEY).toString(), getMappingValueString());
		
		return values;
	}

	/**
	 * Syntax sugar to ease adoption
	 * 
	 * @param key
	 * @return
	 */
	public static final ProfileLookupKey forKey(String key)
	{
		if (key == null || key.length() == 0)
			return null;
		
		return new ProfileLookupKey(Type.KEY, key);
	}
	
	/**
	 * Syntax sugar to ease adoption
	 * 
	 * @param uid
	 * @return
	 */
	public static final ProfileLookupKey forUid(String uid)
	{
		if (uid == null || uid.length() == 0)
			return null;
		
		return new ProfileLookupKey(Type.UID, uid);
	}
	
	/**
	 * Syntax sugar to ease adoption
	 * 
	 * @param email
	 * @return
	 */
	public static final ProfileLookupKey forEmail(String email)
	{
		if (email == null || email.length() == 0)
			return null;
		
		return new ProfileLookupKey(Type.EMAIL, email);
	}

	/**
	 * Syntax sugar to ease creation of Hash lookup
	 * 
	 * @param hash
	 * @return
	 */
	public static final ProfileLookupKey forHashId(String hash)
	{
		if (StringUtils.isEmpty(hash))
			return null;
		
		return new ProfileLookupKey(Type.MCODE, hash);
	}

	/**
	 * Syntax sugar to ease adoption
	 * 
	 * @param guid
	 * @return
	 */
	public static final ProfileLookupKey forGuid(String guid)
	{
		if (guid == null || guid.length() == 0)
			return null;
		
		return new ProfileLookupKey(Type.GUID, guid);
	}
	
	/**
	 * Syntax sugar to ease adoption
	 * 
	 * @param userid
	 * @return
	 */
	public static final ProfileLookupKey forUserid(String userid)
	{
		if (userid == null || userid.length() == 0)
			return null;
		
		return new ProfileLookupKey(Type.USERID, userid);
	}

	/**
	 * Syntax sugar to ease creation of DN lookup
	 * 
	 * @param dn
	 * @return
	 */
	public static final ProfileLookupKey forDN(String dn)
	{
		if (StringUtils.isEmpty(dn))
			return null;
		
		return new ProfileLookupKey(Type.DN, dn);
	}

	/**
	 * 
	 * @return
	 */
	public final String getMappingValueString() 
	{
		String retVal = null;
		switch (getRealType()) 
		{
			case EMAIL:
			case MCODE:
			case UID:
			case DN:		
				retVal = value.toLowerCase(Locale.ENGLISH).trim();
				break;
			default:
				retVal = value.toString().trim();
				break;
		}
		return retVal;
	}

	public final Type getRealType() 
	{
		if (type == Type.USERID)
		{
			String realType = 
				ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName();
			return Type.valueOf(realType.toUpperCase(Locale.US));
		}
			
		return type;
	}
	
	public String toString()
	{
		return toMap().toString();
	}
	
	public boolean isValid() {
		return LookupValidator.validPlk(this);
	}

}
