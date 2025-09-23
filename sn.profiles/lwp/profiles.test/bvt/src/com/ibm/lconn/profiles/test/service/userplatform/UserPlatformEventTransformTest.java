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

package com.ibm.lconn.profiles.test.service.userplatform;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.lconn.commands.IUserLifeCycleConstants;
import com.ibm.lconn.lifecycle.data.IPerson;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.data.UserPlatformEvent;
import com.ibm.lconn.profiles.data.UserPlatformEventData;
import com.ibm.lconn.profiles.internal.data.profile.AttributeGroup;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.data.Employee;

public class UserPlatformEventTransformTest extends BaseTestCase {

	private String OLD_UID = "OLDUID";
	private String OLD_GUID = "oldGuid";

	private List<String> LOGINS = Arrays.asList("foo", "bar");
	private String KEY = UUID.randomUUID().toString();
	private String UID = "FOO";
	private String GUID = "BaR";
	private String EMAIL = "foo@BAR";
	private String LOGINID = "fooBar";
	private String DISPLAY_NAME = "John Tester";
	private Timestamp LAST_UPDATE = new Timestamp(System.currentTimeMillis());

	// this is a manual copy of values that are built in AttributeGroup.PLATFORMEVENT_PROPS_TO_STORE
	// tests will compare the size with the assumption that a failure will help us be aware if
	// anyone adds to the list, as that is an indication the transform test should be updated.
	private static List<String> PLATFORMEVENT_PROPS_TO_STORE_IN_DB = Arrays.<String> asList(new String[] {
			// these are the 'prop' vales of the EventNVP defined by AttributeGroup.PLATFORMEVENT_PROPS_TO_STORE
			"_prof."+PropertyEnum.KEY.getValue(),
			"_prof."+PropertyEnum.UID.getValue(),
			"_prof."+PropertyEnum.GUID.getValue(),
			"_prof."+PropertyEnum.EMAIL.getValue(),
			"_prof."+PropertyEnum.LOGIN_ID.getValue(),
			"_prof."+PropertyEnum.DISPLAY_NAME.getValue(),
			// derived from IPerson.ExtProps at a point in time. if any are added, a test will fail.
			"_prof."+IPerson.ExtProps.EXT_PROFILE_TYPE.substring(AttributeGroup.IPERSON_BASE_ATTR_PREFIX.length()),
			"_prof."+IPerson.ExtProps.EXT_IS_MANAGER.substring(AttributeGroup.IPERSON_BASE_ATTR_PREFIX.length()),
			"_prof."+IPerson.ExtProps.EXT_MANAGER_UID.substring(AttributeGroup.IPERSON_BASE_ATTR_PREFIX.length()),
			"_prof."+IPerson.ExtProps.EXT_JOB_RESP.substring(AttributeGroup.IPERSON_BASE_ATTR_PREFIX.length()),
			"_prof."+IPerson.ExtProps.EXT_ORG_ID.substring(AttributeGroup.IPERSON_BASE_ATTR_PREFIX.length()),
			"_prof."+IPerson.ExtProps.EXT_WORK_LOCATION_CODE.substring(AttributeGroup.IPERSON_BASE_ATTR_PREFIX.length()),
			"_prof."+IPerson.ExtProps.EXT_DEPT_NUMBER.substring(AttributeGroup.IPERSON_BASE_ATTR_PREFIX.length()),
			"_prof."+IPerson.ExtProps.EXT_EMPLOYEE_TYPE_CODE.substring(AttributeGroup.IPERSON_BASE_ATTR_PREFIX.length()),
			"_prof."+IPerson.ExtProps.EXT_DISTINGUISHED_NAME.substring(AttributeGroup.IPERSON_BASE_ATTR_PREFIX.length()),
			"_prof."+IPerson.ExtProps.EXT_TITLE.substring(AttributeGroup.IPERSON_BASE_ATTR_PREFIX.length()),
			"_prof."+IPerson.ExtProps.EXT_UID.substring(AttributeGroup.IPERSON_BASE_ATTR_PREFIX.length()),
			"_prof."+IPerson.ExtProps.EXT_COUNTRY_CODE.substring(AttributeGroup.IPERSON_BASE_ATTR_PREFIX.length())
	 });

	private static List<String> PUB_PROPS_TO_LOOK_FOR = Arrays.<String> asList(new String[] {
			IUserLifeCycleConstants.UPDATED_DIRECTORYID,
			IUserLifeCycleConstants.UPDATED_NAME,
			IUserLifeCycleConstants.UPDATED_EMAIL,
			IUserLifeCycleConstants.UPDATED_LOGINS,
			IUserLifeCycleConstants.UPDATED_EXT_PROPS,
			IUserLifeCycleConstants.LAST_MOD,
			"key"});

	public void testAttributeSizes(){
		// the AttributeGroup values are calculated from processing IPerson.ExtProps
		// if sizes mismatch, someone has added an attribute to one of the sets.
		int size1 = PLATFORMEVENT_PROPS_TO_STORE_IN_DB.size();
		int size2 = AttributeGroup.PLATFORMEVENT_PROPS_TO_STORE.size();
		assertTrue("An attribute was added to either IPerson.ExtProps or AttributeGroup.PLATFORMEVENT_ATTRS_TO_STORE",
				size1 == size2);
	}

	// @SuppressWarnings("unchecked")
	public void testTransforms() {
		// create an employee
		String eventType = IUserLifeCycleConstants.USER_RECORD_ACTIVATE;
		Employee employee = new Employee();
		employee.setKey(KEY);
		employee.setUid(UID);
		employee.setGuid(GUID);
		employee.setEmail(EMAIL);
		employee.setLoginId(LOGINID);
		employee.setDisplayName(DISPLAY_NAME);
		employee.setLastUpdate(LAST_UPDATE);
		for (String extProp : IPerson.ExtProps.ALL_EXT_PROPS) {
			// skip orgig as it is the tenant id
			if (IPerson.ExtProps.EXT_ORG_ID.equals(extProp) == false) {
				String extPropVal = extProp + "_value";
				extProp = extProp.substring(extProp.indexOf('$') + 1);
				employee.put(extProp, extPropVal);
			}
		}
		employee.setTenantKey(IPerson.ExtProps.EXT_ORG_ID+"_value"); // this is expected in later test of publish format
		employee.setState(UserState.INACTIVE);
		// create eventdata object
		UserPlatformEventData eventData = createUserPlatformEventData(eventType, OLD_UID, OLD_GUID, employee, LOGINS);
		// mimic the insert process. the employee properties are converted to a string format
		// that is ultimately.
		String dbFormat = eventData.getDbPayloadFormat();
		checkDbFormat(dbFormat);
		// create an event that mirrors one extracted from the database.
		UserPlatformEvent upEvent = new UserPlatformEvent();
		upEvent.setEventType(eventType);
		upEvent.setCreated(new Date());
		upEvent.setEventKey(1111);
		upEvent.setPayload(dbFormat);
		upEvent.setTenantKey(Tenant.SINGLETENANT_KEY);
		// when published, the db string format is converted to a set of properties.
		// an event extracted from the database is told to reconstitute publish properties.
		// see ProcessLifecycleEventsTask)
		upEvent.createPublishProperties();
		checkPublishFormat(upEvent);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, List<String>> deserializeExtProps(String extPropJson) {
		try {
			return (Map<String, List<String>>) JSONObject.parse(extPropJson);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return Collections.emptyMap();
	}

	// private void dumpKeys(Map<String, ? extends Object> m, String label) {
	// System.out.println(label + "{");
	// for (String k : m.keySet()) {
	// System.out.println("\t" + k + ": " + m.get(k));
	// }
	// System.out.println("}");
	// }

	private UserPlatformEventData createUserPlatformEventData(String eventType, String oldUid, String oldGuid, Employee e,
			List<String> logins) {
		UserPlatformEventData rtnVal = new UserPlatformEventData();
		rtnVal.setEmp(e);
		rtnVal.setEventType(eventType);
		rtnVal.setLogins(logins);
		rtnVal.setOldGuid(oldGuid);
		rtnVal.setOldUid(oldUid);
		rtnVal.setOldOrgId(e.getTenantKey());
		//
		return rtnVal;
	}

	@SuppressWarnings("unchecked")
	private void checkDbFormat(String dbFormatString) {
		// the payload is a flat representation of select profile attributes.
		// see UserPlatformData - it should have a representative value
		Map<String, Object> jsonObject = new JSONObject();
		try {
			jsonObject = JSONObject.parse(dbFormatString);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		// assert that props are inserted
		for (String key : PLATFORMEVENT_PROPS_TO_STORE_IN_DB) {
			if (jsonObject.containsKey(key) == false) {
				assertTrue("eventdata db format does not contain a value for key: " + key, false);
			}
			// else validate value...
		}
	}

	private void checkPublishFormat(UserPlatformEvent upe) {
		Map<String, String> propsToPublish = upe.getPropsToPublish();
		// event publish/consumer has an issue where the last_mod value is not set (trace into the 'getPropsToPublish'
		// code). we think that actually publishing the value may introduce subtle bugs on the consumer side.
		// for now, we just don't publish it.
		for (String key : PUB_PROPS_TO_LOOK_FOR) {
			if (key.equals(IUserLifeCycleConstants.LAST_MOD) == false && propsToPublish.containsKey(key) == false) {
				assertTrue("event publish format does not contain a value for key: " + key, false);
			}
			else { // validate value
				if (key.equals(IUserLifeCycleConstants.UPDATED_EXT_PROPS)) {
					validateExtensionProperties(upe);
				}
			}

		}
	}

	@SuppressWarnings("unchecked")
	private void validateExtensionProperties(UserPlatformEvent upe) {
		String jsonFormat = upe.getPropsToPublish().get(IUserLifeCycleConstants.UPDATED_EXT_PROPS);
		Map<String, Object> map = new JSONObject();
		try {
			map = JSONObject.parse(jsonFormat);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		Set<String> extProps = new HashSet<String>(IPerson.ExtProps.ALL_EXT_PROPS);
		assertNotNull(extProps);
		String val1, val2;
		JSONArray extPropVals;
		for (Object extProp : extProps) {
			// check existence
			assertTrue("extended properties does not contain" + extProp, map.containsKey(extProp));
			// check value
			val1 = String.valueOf(extProp) + "_value";
			extPropVals = (JSONArray) map.get(extProp);
			assertEquals(1, extPropVals.size());
			val2 = String.valueOf(extPropVals.get(0));
			assertTrue("extended properties value mismatch for " + extProp, val1.equals(val2));
		}
	}
}
