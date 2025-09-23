/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.data.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.lifecycle.data.IPerson;
import com.ibm.lconn.profiles.config.types.PropertyEnum;

public class AttributeGroup {

	public static final List<String> LIFECYCLE_ATTRS;
	public static final List<String> BASE_ATTRS;
	public static final Map<String, String> LOWERCASE_ATTRS;
	public static final List<EventNVP> PLATFORMEVENT_EXTATTRS;
	public static final List<EventNVP> PLATFORMEVENT_PROPS_TO_STORE;
	public static final String IPERSON_BASE_ATTR_PREFIX = "base$";
	public static final List<String> SYSTEM_ATTRS; // see comments below
		
	static {
		//> LIFECYCLE_ATTRS
		List<String> tmp = new ArrayList<String>(Arrays.<String> asList(
				PropertyEnum.UID.getValue(),            //"uid",
				PropertyEnum.GUID.getValue(),           //"guid",
				PropertyEnum.EMAIL.getValue(),          //"email",
				PropertyEnum.LOGIN_ID.getValue(),       //"loginId",
				PropertyEnum.DISPLAY_NAME.getValue(),   //"displayName",
				//PropertyEnum.TENANT_KEY.getValue(),   //"tenantKey",
				PropertyEnum.PROFILE_TYPE.getValue())); //"profileType"));
		
		LIFECYCLE_ATTRS = Collections.unmodifiableList(tmp);
		
		//> BASE_ATTRS
		tmp = new ArrayList<String>();
		for(PropertyEnum p : PropertyEnum.values()){
			tmp.add(p.getValue());
		}
		// not certain why this is in PropertyEnum?
		tmp.remove(PropertyEnum.USER_ID.getValue());
		BASE_ATTRS = Collections.unmodifiableList(tmp);
		
		//> LOWERCASE_ATTRS
		HashMap<String, String> map = new HashMap<String, String>(10);
		map.put(PropertyEnum.UID.getValue(), "uidLower");
		map.put(PropertyEnum.DISTINGUISHED_NAME.getValue(), "distinguishedNameLower");
		map.put(PropertyEnum.LOGIN_ID.getValue(), "loginIdLower");
		map.put(PropertyEnum.MANAGER_UID.getValue(), "managerUidLower");
		map.put(PropertyEnum.EMAIL.getValue(), "emailLower");
		map.put(PropertyEnum.GROUPWARE_EMAIL.getValue(), "groupwareEmailLower");
		LOWERCASE_ATTRS = Collections.unmodifiableMap(map);

		//> PLATFORMEVENT_EXTATTRS
		List<EventNVP> tmpNVP = new ArrayList<EventNVP>(IPerson.ExtProps.ALL_EXT_PROPS.size());
		int clip = IPERSON_BASE_ATTR_PREFIX.length();
		for (String val : IPerson.ExtProps.ALL_EXT_PROPS) {
			// we only handle base attrs - which is not a defined group yet. TODO
			String str = null;
			if (val.startsWith(IPERSON_BASE_ATTR_PREFIX)) {
				str = val.substring(clip);
				if (StringUtils.isNotEmpty(str)) {
					if ("orgId".equals(str)){
						// public orgId is retrieved via internal tenantKey
						tmpNVP.add(new EventNVP(str,"tenantKey"));
					}
					else{
						// otherwise, public props are mapped by name to internal attributes
						tmpNVP.add(new EventNVP(str,str));
					}
				}
			}
		}
		PLATFORMEVENT_EXTATTRS = Collections.unmodifiableList(tmpNVP);

		// lifecycle/userplatform event interface uses 'orgid', internally profiles uses 'tenantKey'.
		// these name-attribiute pairs are an attempt to keep this isolated to one spot. we store with
		// the event metadata with the name that corresponds to an employee attribure
		tmpNVP = new ArrayList<EventNVP>(Arrays.<EventNVP> asList(
				new EventNVP(PropertyEnum.KEY.getValue(),PropertyEnum.KEY.getValue()), 					//key
				new EventNVP(PropertyEnum.UID.getValue(),PropertyEnum.UID.getValue()),					//"uid"
				new EventNVP(PropertyEnum.GUID.getValue(),PropertyEnum.GUID.getValue()),				//"guid",
				new EventNVP(PropertyEnum.EMAIL.getValue(),PropertyEnum.EMAIL.getValue()),				//"email",
				new EventNVP(PropertyEnum.LOGIN_ID.getValue(),PropertyEnum.LOGIN_ID.getValue()),		//"loginId",
				new EventNVP(PropertyEnum.DISPLAY_NAME.getValue(),PropertyEnum.DISPLAY_NAME.getValue())));//"displayName"
		tmpNVP.addAll(PLATFORMEVENT_EXTATTRS);
		PLATFORMEVENT_PROPS_TO_STORE = Collections.unmodifiableList(tmpNVP);

		// this value is for backwards compatibility, typically in the UI. The 3.0 code had a
		// notion of 'system' attributes. The three were 'usrState', 'orgMem', 'orgAcl'
		// only usrState was ever used and it was exposed in the API.
		tmp = new ArrayList<String>(Arrays.<String> asList("usrState"));
		SYSTEM_ATTRS = Collections.unmodifiableList(tmp);
	}
}
