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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.LookupValidator;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ProfileLookupKeySet
{	
	public final static String TYPE_KEY = "lookupType";
	public final static String VALUES_KEY = "lookupValues";
	
	public static final Map<String, String> DEFAULT_MAPPING;
	
	static
	{
		Map<String,String> dm = new HashMap<String,String>(3);
		dm.put(TYPE_KEY, TYPE_KEY);
		dm.put(VALUES_KEY, VALUES_KEY);
		DEFAULT_MAPPING = Collections.unmodifiableMap(dm);		
	}
	
	private final Type type;
	private final String[] values;
	
	public ProfileLookupKeySet(Type type, String[] values)
	{		
		AssertionUtils.assertNotNull(type);
		AssertionUtils.assertNotNull(values);

		this.type   = type;
		this.values = clean(values);
	}
	
	public ProfileLookupKeySet(Type type, Collection<String> values)
	{		
		AssertionUtils.assertNotNull(type);
		AssertionUtils.assertNotNull(values);
		
		this.type   = type;
		this.values = clean(values.toArray(new String[values.size()]));
	}

	public ProfileLookupKeySet(ProfileLookupKeySet plks){
		AssertionUtils.assertNotNull(plks);
		AssertionUtils.assertNotNull(plks.type);
		AssertionUtils.assertNotNull(plks.values);
		this.type = plks.type;
		this.values = plks.values;
	}

	/**
	 * Utility constructor to convert plk to set.
	 * @param plk
	 */
	public ProfileLookupKeySet(ProfileLookupKey plk) {
		AssertionUtils.assertNotNull(plk);
		
		this.type = plk.getType();
		this.values = new String[]{plk.getValue()};
	}

	public Type getType() {
		return type;
	}

	public String[] getValues() {
		return values;
	}

	private String[] clean(String[] inputValues) {
		String[] retVal = new String[inputValues.length];
		for (int i = 0; i < inputValues.length; i++) {
			retVal[i] = inputValues[i].trim();
		}
		return retVal;
	}

	/**
	 * Returns a map of version of this object using the default 're-mapping'
	 * 
	 * ("type", type)
	 * ("value", value)
	 * 
	 * @return
	 */
	public Map<String,Object> toMap()
	{
		return toMap(DEFAULT_MAPPING);
	}
	
	/**
	 * Returns a map version of this object using a specified 're-mapping'
	 * 
	 * @param remapping
	 * @return
	 */
	public Map<String,Object> toMap(Map<String, String> remapping)
	{	
		Map<String,Object> values = new HashMap<String,Object>(3);
		values.put(remapping.get(TYPE_KEY).toString(), getRealType().toString());
		values.put(remapping.get(VALUES_KEY).toString(), getMappingValuesList());		
		return values;
	}
	
	private final Type getRealType() 
	{
		if (type == Type.USERID)
		{
			String realType = 
				ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName();
			return Type.valueOf(realType.toUpperCase(Locale.US));
		}
			
		return type;
	}
	
	/**
	 * 
	 * @return
	 */
	private final List<String> getMappingValuesList() 
	{
		List<String> res = new ArrayList<String>(values.length);		

		Type rt = getRealType();
		for (String value : values) {
			if (StringUtils.isNotEmpty(value)) {
				switch (rt) 
				{
					case EMAIL:
					case UID:
					case MCODE:
					case DN:		
						res.add(value.toLowerCase(Locale.ENGLISH).trim());
						break;
					default:
						res.add(value.trim());
						break;
				}
			}
		}
		return res;
	}
	
	public String toString()
	{
		return toMap().toString();
	}

	public boolean isValid() {
		return LookupValidator.validPlkSet(this);
	}

	/**
	 * Truncates lookup key set to specified size; cloning object. If the set is
	 * smaller than the max length the method returns a reference to itself.
	 * 
	 * @param maxLength
	 * @return
	 */
	public ProfileLookupKeySet trimToSize(int maxLength) {
		if (values.length <= maxLength)
			return this;
		
		return new ProfileLookupKeySet(type, (String[]) ArrayUtils.subarray(values, 0, maxLength));
	}
}
