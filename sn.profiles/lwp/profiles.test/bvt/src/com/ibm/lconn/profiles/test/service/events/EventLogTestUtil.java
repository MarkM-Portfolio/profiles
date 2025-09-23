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

package com.ibm.lconn.profiles.test.service.events;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class EventLogTestUtil {

	// this is a direct copy of values that are built in AttributeGroup.PLATFORMEVENT_ATTRS_TO_STORE
	// tests will compare the size with the assumption that a failure will help us be aware if
	// anyone adds to the IPerson.ExtProps list.
	public static List<String> BASE_ATTRIBUTES_TO_SET = Arrays.<String> asList(new String[] {
			PeoplePagesServiceConstants.KEY,
			PeoplePagesServiceConstants.TENANT_KEY,
			PeoplePagesServiceConstants.UID,
			PeoplePagesServiceConstants.GUID,
			PeoplePagesServiceConstants.STATE,
			PeoplePagesServiceConstants.DN,
			PeoplePagesServiceConstants.EMAIL,
			PeoplePagesServiceConstants.LOGIN_ID,
			PeoplePagesServiceConstants.DISPLAY_NAME,
			PeoplePagesServiceConstants.PROF_TYPE,
			PeoplePagesServiceConstants.IS_MANAGER,
			PeoplePagesServiceConstants.LAST_UPDATE
	 });

	public static String PROFILE_LINK = "<linkroll xmlns=\"http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links\" xmlns:tns=\"http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links\"><link name=\"MyFBProfile\" url=\"http://www.facebook.com\"/><link name=\"MyIBMPage\" url=\"http://www.ibm.com\"/></linkroll>"; 

	public static Map<String,Object> WORKLOCATION_VALUES;
	static {
		WORKLOCATION_VALUES = new HashMap<String,Object>();
		WORKLOCATION_VALUES.put("workLocationCode", "RN");
		WORKLOCATION_VALUES.put("state", "Mass");
		WORKLOCATION_VALUES.put("postalCode", "02067");
		WORKLOCATION_VALUES.put("city", "Littleton");
		WORKLOCATION_VALUES.put("address1", "550 King Street");
		WORKLOCATION_VALUES.put("address2", "Unknown");
	}

	public static List<String> TAGS = Arrays.<String> asList(new String[] {
		"tag1","tag2","tag3"
	 });

	public static Employee createTestEmployee(String name) {
		Employee employee = new Employee();
		
		for (String key : BASE_ATTRIBUTES_TO_SET){
			if (PeoplePagesServiceConstants.LAST_UPDATE.equals(key)){
				Timestamp now = new Timestamp((new Date()).getTime());
				employee.setLastUpdate(now);
			}
			else if (PeoplePagesServiceConstants.IS_MANAGER.equals(key)){
				employee.setIsManager("false");
			}
			else if (PeoplePagesServiceConstants.STATE.equals(key)){
				employee.setState(UserState.ACTIVE);
			}
			else if (PeoplePagesServiceConstants.EMAIL.equals(key)){
				employee.setEmail(name+"@zzzzz.com");
			}
			else{
				employee.put(key,name+"_"+key+"_value");
			}
		}

		ProfileExtension pe1 = new ProfileExtension();
		pe1.setKey(employee.getKey());
		pe1.setPropertyId("school");
		pe1.setValue("Hard Knocks U");
		pe1.setExtendedValue(null);
		pe1.setMaskNull(true);
		pe1.setRecordUpdated(new Date());
		employee.setProfileExtension(pe1);
		
		ProfileExtension pe2 = employee.getProfileExtension("profileLinks", true);
		pe2.setExtendedValue(PROFILE_LINK.getBytes());
		employee.setProfileExtension(pe2);
		
		WorkLocation workLoc = new WorkLocation("RN", WORKLOCATION_VALUES);
		employee.setWorkLocation(workLoc);
		
		List<Tag> tags = new ArrayList<Tag>();
		String[] terms = new String[] { "tag1", "tag2", "tag3" };
		for (String term : terms) {
			Tag tag = new Tag();
			tag.setTag(term);
			tag.setType(TagConfig.DEFAULT_TYPE);
		    tags.add(tag);
		}
		employee.setProfileTags( tags );
		
		return employee;
	}
}
