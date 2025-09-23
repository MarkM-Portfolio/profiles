/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.lconn.commands.IUserLifeCycleConstants;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.internal.data.profile.EventNVP;
import com.ibm.lconn.profiles.internal.data.profile.AttributeGroup;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.data.UserPlatformEventData;

import com.ibm.peoplepages.data.Employee;

/**
 */
public class UserPlatformEventsHelper {
	
	private static final String KEY_PROF_BASE = "_prof.";
	private static final String KEY_PROF_OLD = KEY_PROF_BASE + "old.";
	
	public static final String KEY_EVENT_TYPE = KEY_PROF_BASE + "eventType";
	
	public static final String KEY_OLD_UID = KEY_PROF_OLD + "uid";
	public static final String KEY_OLD_GUID = KEY_PROF_OLD + "guid";
	public static final String KEY_OLD_ORGID = KEY_PROF_OLD + "orgId";

	public static final String KEY_PROF_LOGINS = KEY_PROF_BASE + "logins";
	public static final String KEY_USER_STATE = KEY_PROF_BASE + "usrState";
	public static final String KEY_USER_MODE = KEY_PROF_BASE + "userMode";
	public static final String KEY_LAST_UPDATE = KEY_PROF_BASE + "lastUpdate";
	
	public static Map<String, Object> createPropMapForDbFromEvent( UserPlatformEventData upeData) {
		Map<String,Object> rtnVal = new HashMap<String,Object>();
		Employee emp = upeData.getEmployee();
		storeProperty(rtnVal, KEY_EVENT_TYPE, upeData.getEventType());
		storeProperty(rtnVal, KEY_OLD_UID, upeData.getOldUid());
		storeProperty(rtnVal, KEY_OLD_GUID, upeData.getOldGuid());
		storeProperty(rtnVal, KEY_OLD_ORGID, upeData.getOldOrgId());
		
		for (EventNVP nvp : AttributeGroup.PLATFORMEVENT_PROPS_TO_STORE){
			storeExtProperty(rtnVal,nvp,emp);
		}
//		for (String propKey : AttributeGroup.PLATFORMEVENT_ATTRS_TO_STORE){
//		// store tenantKey as orgId
//		if (PropertyEnum.TENANT_KEY.getValue().equals(propKey)){
//			storeOrgId(rtnVal,emp);
//		}
//		else{
//			storeExtProperty(rtnVal,propKey,emp);
//		}
//	}
		storeProperty(rtnVal, KEY_PROF_LOGINS, upeData.getLogins());
		storeProperty(rtnVal, KEY_USER_STATE,  emp.getState());
		storeProperty(rtnVal, KEY_USER_MODE,   emp.getMode());
		storeProperty(rtnVal, KEY_LAST_UPDATE, emp.getLastUpdate());
		return rtnVal;
	}

	/**
	 * 
	 * @param properties
	 * @param keyUserState
	 * @param emp
	 */
	private final static void storeProperty( Map<String, Object> properties, String key, Object val){
		if (val != null) {
			if (val instanceof Timestamp) {
				properties.put(key, ((Timestamp)val).getTime());
			} else if (val instanceof UserState) {
				properties.put(key, ((UserState)val).getCode());
			} else if (val instanceof UserMode) {
				properties.put(key, ((UserMode)val).getCode());
			} else {
				properties.put(key, val);
			}
		}
	}

//	// properties stored via the AttributeGroup.PLATFORMEVENT_ATTRS_TO_STORE are stored with a
//	// "_prof" prefix.
//	private final static void storeExtProperty( Map<String, Object> properties, String key, Employee emp){
//		Object val = emp.get(key);
//		if (val != null){
//			storeProperty(properties, toPropKey(key),val);
//		}
//	}
	
	private final static void storeExtProperty( Map<String, Object> properties, EventNVP nvp, Employee emp){
		// we store the nvp.prop as the key with the value of the attribute - emp.get(nvp.attr)
		// specifically for the org/tenant, we store the property orgId with the value emp.get(tenantKey)
		Object val = emp.get(nvp.attr);
		if (val != null){
			storeProperty(properties, toPropKey(nvp.prop),val);
		}
	}
	
//	private final static void storeOrgId(Map<String, Object> properties, Employee emp){
//		Object val = emp.get("tenantKey");
//		if (val != null){
//			storeProperty(properties, toPropKey("orgId"),val);
//		}
//	}

	/**
	 * Utility method to save a list of values in the map
	 * @param properties
	 * @param keyProfLogins
	 * @param logins
	 */
	private static final void storeProperty(
			Map<String, Object> properties,
			String key, 
			List<String> vals) 
	{
		if (vals != null && vals.size() > 0) {
			properties.put(key, vals);
		}
	}
	
	private static final String toPropKey(String key){
		return KEY_PROF_BASE + key;
	}

	/**
	 * This methods maps the persisted format of event properties and creates a set
	 * of properties expected by the lifecycle event consumer.
	 * @param properties
	 */
	public static void createPropMapForPubFromDbMap(Map<String,Object> properties) {
		//note: when we need to publish the 'old' orgid, we can add the constant to IUserLifeCycleConstants
		// TODO where are consts for these attribute names
		Map<String,Object> pprops = extractPropfileProperties(properties);
		// IUserLifeCycleConstants.DIRECTORYID is the previous directory id
		restoreProp(properties, IUserLifeCycleConstants.DIRECTORYID, getDirectoryProp(pprops, false));
		// IUserLifeCycleConstants.UPDATED_DIRECTORYID is the new directory id. it will
		// not match the DIRECTORYID only if the directory id has changed.
		restoreProp(properties, IUserLifeCycleConstants.UPDATED_DIRECTORYID, getDirectoryProp(pprops, true));
		// IC196476
		restoreProp(properties, "key", getProfProp(pprops,"key"));
		restoreProp(properties, IUserLifeCycleConstants.UPDATED_NAME, getProfProp(pprops,"displayName"));
		UserState usrState = restoreUserState(pprops);
		if (UserState.INACTIVE != usrState) {
			restoreProp(properties, IUserLifeCycleConstants.UPDATED_EMAIL, getProfProp(pprops,"email"));
			restoreProp(properties, IUserLifeCycleConstants.UPDATED_LOGINS, getLogins(pprops));
		}
//TODO  is this needed ?
		UserMode usrMode = restoreUserMode(pprops);

		restoreProp(properties, IUserLifeCycleConstants.UPDATED_EXT_PROPS, getExtProps(pprops));
	}

	/**
	 * 
	 * @param properties
	 * @param key
	 * @param val
	 */
	private static void restoreProp( Map<String, Object> properties, String key, Object val) {
		if (val != null) {
			if (val instanceof String) {
				String valStr = (String) val;
				if (StringUtils.isNotEmpty(valStr)) {
					properties.put(key, valStr);
				}
			}
			else {
				properties.put(key, val);
			}
		}
	}

	/**
	 * Extract the LifeCycle ext properties
	 * @param pprops
	 * @return
	 */
	private static Object getExtProps(Map<String, Object> pprops) {
		JSONObject json = new JSONObject();
		//
		// TODO - this is not future proofed for non-base attributes
		//  The code will fall part with non-base attributes for other reasons, but this is one more problem
		// TODO - multi valued attributes not handled either
		//
		String extkey;
		for (EventNVP nvp : AttributeGroup.PLATFORMEVENT_EXTATTRS){
			extkey = KEY_PROF_BASE + nvp.prop; // key was persisted with prefix
			Object val = pprops.get(extkey);
			if (val != null) {
				JSONArray vals = new JSONArray();
				vals.add(val);
				json.put(AttributeGroup.IPERSON_BASE_ATTR_PREFIX+nvp.prop, vals);
			}
		}
		return (json.isEmpty()) ? null : json;
	}

	/**
	 * Utility method to restore
	 * @param pprops
	 * @return
	 */
	private final static UserState restoreUserState(Map<String, Object> pprops) {
		Object stateCode = pprops.get(KEY_USER_STATE);
		UserState stateObj = null;
		if (stateCode != null) {
			if (stateCode instanceof Long) {
				Long lval = (Long) stateCode;
				stateObj = UserState.fromCode(lval.intValue());
			} else if (stateCode instanceof Integer) {
				Integer ival = (Integer) stateCode;
				stateObj = UserState.fromCode(ival.intValue());
			}
		}
		
		String eventType = (String) pprops.get(KEY_EVENT_TYPE);
		if (eventType != null) {
			if ((IUserLifeCycleConstants.USER_RECORD_ACTIVATE.equals(eventType) ||
				 IUserLifeCycleConstants.USER_RECORD_SWAP_ACCESS.equals(eventType))) {
				stateObj = UserState.ACTIVE;
			} 
			else if ((IUserLifeCycleConstants.USER_RECORD_INACTIVATE.equals(eventType) ||
					  IUserLifeCycleConstants.USER_RECORD_REVOKE.equals(eventType)))  {
				stateObj = UserState.INACTIVE;
			}
			// else - use value stored in the DB as the state value for the user
		}
		
		// ensure non-null return
		if (stateObj == null) {
			return UserState.ACTIVE;
		}
		else {
			return stateObj;
		}
	}

	/**
	 * Utility method to restore
	 * @param pprops
	 * @return
	 */
	private final static UserMode restoreUserMode(Map<String, Object> pprops) {
		Object modeCode = pprops.get(KEY_USER_MODE);
		UserMode modeObj = null;
		if (modeCode != null) {
			if (modeCode instanceof Long) {
				Long lval = (Long) modeCode;
				modeObj = UserMode.fromCode(lval.intValue());
			} else if (modeCode instanceof Integer) {
				Integer ival = (Integer) modeCode;
				modeObj = UserMode.fromCode(ival.intValue());
			}
		}
		// ensure non-null return
		if (modeObj == null) {
			return UserMode.INTERNAL;
		}
		else {
			return modeObj;
		}
	}

	/**
	 * Restore logins 
	 * @param pprops
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List<String> getLogins(Map<String, Object> pprops) {
		HashSet<String> logins = new HashSet<String>();
		
		// add login fields
		for (String prop : DataAccessConfig.instance().getDirectoryConfig().getLoginAttributes()) {
			addLogin(logins, (String) pprops.get(KEY_PROF_BASE + prop));
		}
		
		// add login list
		List<String> profLogins = (List<String>) pprops.get(KEY_PROF_LOGINS);
		if (profLogins != null) {
			for (String login : profLogins) {
				addLogin(logins, login);
			}				
		}
		
		JSONArray jsonArray = new JSONArray();
		jsonArray.addAll(logins);
		
		return jsonArray;
	}

	/**
	 * Lower-case and add login if it is not null
	 * @param logins
	 * @param object
	 */
	private static void addLogin(HashSet<String> logins, String login) {
		if (StringUtils.isNotEmpty(login)) {
			logins.add(login.toLowerCase(Locale.ENGLISH));
		}
	}

	/**
	 * Utility to get the prof property
	 * @param pprops
	 * @return
	 */
	private static Object getProfProp(Map<String, Object> pprops, String key) {
		return pprops.get(toPropKey(key));
	}

	/**
	 * Get the directory id property
	 * @param pprops
	 * @param updatedUserid
	 * @return
	 */
	private static String getDirectoryProp(Map<String, Object> pprops, boolean updatedUserid) {
		final String lconnUserIdAttr = DataAccessConfig.instance().getDirectoryConfig().getLConnUserIdAttrName();
		final String val = (String) pprops.get((updatedUserid ? KEY_PROF_BASE : KEY_PROF_OLD) + lconnUserIdAttr);
		
		if (PropertyEnum.UID.getValue().equals(lconnUserIdAttr)) {
			return val.toLowerCase(Locale.ENGLISH);
		} else {
			return val;
		}
	}

	/**
	 * Strip the Profiles specific properties from this file
	 * @param properties
	 * @return
	 */
	private static Map<String, Object> extractPropfileProperties(Map<String, Object> properties) {
		Map<String, Object> m = new HashMap<String, Object>();
		
		for (String key : new ArrayList<String>(properties.keySet())) {
			if (key.startsWith(KEY_PROF_BASE)) {
				Object val = properties.remove(key);
				if (val != null) {
					m.put(key, val);
				}
			}
		}
		return m;
	}
}
