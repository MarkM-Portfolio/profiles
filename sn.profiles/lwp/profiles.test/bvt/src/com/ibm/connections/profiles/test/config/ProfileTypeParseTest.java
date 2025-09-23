/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.connections.profiles.test.config;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.types.ExtensionType;
import com.ibm.lconn.profiles.config.types.ProfileTypeConfig;
import com.ibm.lconn.profiles.config.types.ProfileTypeImpl;
import com.ibm.lconn.profiles.config.types.ProfileTypeParser;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.config.types.PropertyImpl;
import com.ibm.lconn.profiles.config.types.Updatability;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.ventura.internal.config.api.VenturaConfigurationProvider;

public class ProfileTypeParseTest extends BaseTestCase {

	public void testReadProfileTypeFile() {
		// this code mimics the profiles startup sequence where profiles-types.xml is read and parsed.
		ProfileTypeImpl baseType = createBaseType();
		try {
			// seed the type-hierarchy scope with this abstract base type, and then
			// attempt to parse the global type definitions from cell
			Map<String, ProfileTypeImpl> scope = new HashMap<String, ProfileTypeImpl>(1);
			scope.put(baseType.getId(), baseType);
			VenturaConfigurationProvider configProvider = VenturaConfigurationProvider.Factory.getInstance();
			URL fileURL = configProvider.getURLForXMLConfig("profiles-types.xml");
			System.out.println("ProfileTypeParseTest.testReadProfileTypeFile profiles-types.xml loading...");
			// parse the input profile-types.xml file
			Map<String, ExtensionType> extensionProperties = DMConfig.instance().getExtensionPropertiesToType();
			Map<String, ProfileTypeImpl> types = ProfileTypeParser.parseTypes(fileURL, extensionProperties, scope, Tenant.SINGLETENANT_KEY);
			Set<String> keys = types.keySet();
			System.out.println("ProfileTypeParseTest.testReadProfileTypeFile output");
			for (String str : keys) {
				System.out.println("profile type: " + str);
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void testReadProfileTypeString() {
		ProfileTypeImpl baseType = createBaseType();
		try {
			Map<String, ProfileTypeImpl> scope = new HashMap<String, ProfileTypeImpl>(1);
			scope.put(baseType.getId(), baseType);
			Map<String, ExtensionType> extensionProperties = DMConfig.instance().getExtensionPropertiesToType();
			System.out.println("ProfileTypeParseTest.testReadProfileTypeString profiles-types.xml loading...");
			// orgid AAA is hardcoded in the test string below.
			Map<String, ProfileTypeImpl> types = ProfileTypeParser.parseTypes(xmlString, extensionProperties, scope,"AAA");
			Set<String> keys = types.keySet();
			System.out.println("ProfileTypeParseTest.testReadProfileTypeString output");
			for (String str : keys) {
				ProfileTypeImpl pti = types.get(str);
				System.out.println("profile type: " + str + " orgId: " + pti.getOrgId());
			}
			// these tests are coded against the expectation that the input string has profile types 'default'
			// 'dopey' and 'sleepy'
			ProfileTypeImpl pti = types.get("default");
			assertTrue(pti != null);
			// dopey type has changed updatability of displayName.
			pti = types.get("dopey");
			assertTrue(pti != null);
			assertTrue(pti.getParentId().equals("default"));
			Property property = pti.getPropertyById(PropertyEnum.DISPLAY_NAME.getValue());
			Updatability updatability = property.getUpdatability();
			assertTrue( updatability.equals(Updatability.READWRITE));
			// sleepy inherits from dopey and alters email updatability
			pti = types.get("sleepy");
			assertTrue(pti != null);
			assertTrue(pti.getParentId().equals("dopey"));
			property = pti.getPropertyById(PropertyEnum.DISPLAY_NAME.getValue()); // get constant 
			updatability = property.getUpdatability();
			assertTrue( updatability.equals(Updatability.READWRITE));
			property = pti.getPropertyById(PropertyEnum.EMAIL.getValue()); // get constant 
			updatability = property.getUpdatability();
			assertTrue( updatability.equals(Updatability.READWRITE));
			
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private ProfileTypeImpl createBaseType() {
		// display name
		PropertyImpl displayName = new PropertyImpl();
		displayName.setExtension(false);
		displayName.setHidden(false);
		displayName.setFullTextIndexed(PropertyEnum.DISPLAY_NAME.isFullTextIndexed());
		displayName.setInherited(false);
		displayName.setRef(PropertyEnum.DISPLAY_NAME.getValue());
		displayName.setRichText(false);
		displayName.setUpdatability(Updatability.READ);
		// surname
		PropertyImpl surname = new PropertyImpl();
		surname.setExtension(false);
		surname.setHidden(false);
		surname.setFullTextIndexed(PropertyEnum.SURNAME.isFullTextIndexed());
		surname.setInherited(false);
		surname.setRef(PropertyEnum.SURNAME.getValue());
		surname.setRichText(false);
		surname.setUpdatability(Updatability.READ);
		// guid
		PropertyImpl guid = new PropertyImpl();
		guid.setExtension(false);
		guid.setHidden(false);
		guid.setFullTextIndexed(true);
		guid.setInherited(PropertyEnum.GUID.isFullTextIndexed());
		guid.setRef(PropertyEnum.GUID.getValue());
		guid.setRichText(false);
		guid.setUpdatability(Updatability.READ);
		// key
		PropertyImpl key = new PropertyImpl();
		key.setExtension(false);
		key.setHidden(false);
		key.setFullTextIndexed(PropertyEnum.KEY.isFullTextIndexed());
		key.setInherited(false);
		key.setRef(PropertyEnum.KEY.getValue());
		key.setRichText(false);
		key.setUpdatability(Updatability.READ);
		// tenant key
		PropertyImpl tenantKey = new PropertyImpl();
		tenantKey.setExtension(false);
		tenantKey.setHidden(false);
		tenantKey.setFullTextIndexed(PropertyEnum.TENANT_KEY.isFullTextIndexed());
		tenantKey.setInherited(false);
		tenantKey.setRef(PropertyEnum.TENANT_KEY.getValue());
		tenantKey.setRichText(false);
		tenantKey.setUpdatability(Updatability.READ);
		// uid
		PropertyImpl uid = new PropertyImpl();
		uid.setExtension(false);
		uid.setHidden(false);
		uid.setFullTextIndexed(PropertyEnum.UID.isFullTextIndexed());
		uid.setInherited(false);
		uid.setRef(PropertyEnum.UID.getValue());
		uid.setRichText(false);
		uid.setUpdatability(Updatability.READ);
		// profile type
		PropertyImpl profileType = new PropertyImpl();
		profileType.setExtension(false);
		profileType.setHidden(false);
		profileType.setFullTextIndexed(PropertyEnum.PROFILE_TYPE.isFullTextIndexed());
		profileType.setInherited(false);
		profileType.setRef(PropertyEnum.PROFILE_TYPE.getValue());
		profileType.setRichText(false);
		profileType.setUpdatability(Updatability.READ);
		// userid
		PropertyImpl userId = new PropertyImpl();
		userId.setExtension(false);
		userId.setHidden(false);
		userId.setFullTextIndexed(PropertyEnum.USER_ID.isFullTextIndexed());
		userId.setInherited(false);
		userId.setRef(PropertyEnum.USER_ID.getValue());
		userId.setRichText(false);
		userId.setUpdatability(Updatability.READ);
		// sourceurl
		PropertyImpl sourceUrl = new PropertyImpl();
		sourceUrl.setExtension(false);
		sourceUrl.setHidden(false);
		sourceUrl.setFullTextIndexed(PropertyEnum.SOURCE_URL.isFullTextIndexed());
		sourceUrl.setInherited(false);
		sourceUrl.setRef(PropertyEnum.SOURCE_URL.getValue());
		sourceUrl.setRichText(false);
		sourceUrl.setUpdatability(Updatability.READ);
		// distinguishedName
		PropertyImpl distinguishedName = new PropertyImpl();
		distinguishedName.setExtension(false);
		distinguishedName.setHidden(false);
		distinguishedName.setFullTextIndexed(PropertyEnum.DISTINGUISHED_NAME.isFullTextIndexed());
		distinguishedName.setInherited(false);
		distinguishedName.setRef(PropertyEnum.DISTINGUISHED_NAME.getValue());
		distinguishedName.setRichText(false);
		distinguishedName.setUpdatability(Updatability.READ);
		// givenName
		PropertyImpl givenName = new PropertyImpl();
		givenName.setExtension(false);
		givenName.setHidden(false);
		givenName.setFullTextIndexed(PropertyEnum.GIVEN_NAME.isFullTextIndexed());
		givenName.setInherited(false);
		givenName.setRef(PropertyEnum.GIVEN_NAME.getValue());
		givenName.setRichText(false);
		givenName.setUpdatability(Updatability.READ);
		// managerUid
		PropertyImpl managerUid = new PropertyImpl();
		managerUid.setExtension(false);
		managerUid.setHidden(false);
		managerUid.setFullTextIndexed(PropertyEnum.MANAGER_UID.isFullTextIndexed());
		managerUid.setInherited(false);
		managerUid.setRef(PropertyEnum.MANAGER_UID.getValue());
		managerUid.setRichText(false);
		managerUid.setUpdatability(Updatability.READ);
		// isManager
		PropertyImpl isManager = new PropertyImpl();
		isManager.setExtension(false);
		isManager.setHidden(false);
		isManager.setFullTextIndexed(PropertyEnum.IS_MANAGER.isFullTextIndexed());
		isManager.setInherited(false);
		isManager.setRef(PropertyEnum.IS_MANAGER.getValue());
		isManager.setRichText(false);
		isManager.setUpdatability(Updatability.READ);
		// loginId
		PropertyImpl loginId = new PropertyImpl();
		loginId.setExtension(false);
		loginId.setHidden(false);
		loginId.setFullTextIndexed(PropertyEnum.LOGIN_ID.isFullTextIndexed());
		loginId.setInherited(false);
		loginId.setRef(PropertyEnum.LOGIN_ID.getValue());
		loginId.setRichText(false);
		loginId.setUpdatability(Updatability.READ);
		// userState
		PropertyImpl userState = new PropertyImpl();
		userState.setExtension(false);
		userState.setHidden(false);
		userState.setFullTextIndexed(PropertyEnum.USER_STATE.isFullTextIndexed());
		userState.setInherited(false);
		userState.setRef(PropertyEnum.USER_STATE.getValue());
		userState.setRichText(false);
		userState.setUpdatability(Updatability.READ);
		// userMode - internal/external
		PropertyImpl userMode = new PropertyImpl();
		userMode.setExtension(false);
		userMode.setHidden(false);
		userMode.setFullTextIndexed(PropertyEnum.USER_MODE.isFullTextIndexed());
		userMode.setInherited(false);
		userMode.setRef(PropertyEnum.USER_MODE.getValue());
		userMode.setRichText(false);
		userMode.setUpdatability(Updatability.READ);

		// base properties
		List<PropertyImpl> properties = new ArrayList<PropertyImpl>(14);
		properties.add(displayName);
		properties.add(guid);
		properties.add(key);
		properties.add(surname);
		properties.add(tenantKey);
		properties.add(uid);
		properties.add(profileType);
		properties.add(userId);
		properties.add(sourceUrl);
		properties.add(distinguishedName);
		properties.add(givenName);
		properties.add(managerUid);
		properties.add(isManager);
		properties.add(loginId);
		properties.add(userState);
		properties.add(userMode);
		// base profile type
		ProfileTypeImpl baseType = new ProfileTypeImpl();
		baseType.setId(ProfileTypeConfig.BASE_TYPE_ID);
		baseType.setParentId("");
		baseType.setProperties(properties);

		return baseType;
	}
//	
//	 <config xmlns="http://www.ibm.com/profiles-types" id="profiles-types">
//	 <type>
//	 <parentId>snx:person</parentId>
//	 <id>default</id>
//	 <orgId>AAA</orgId>
//	 <property>
//	 <ref>telephoneNumber</ref>
//	 <updatability>readwrite</updatability>
//	 <hidden>false</hidden>
//	 </property>
//	 <property>
//	 <ref>ipTelephoneNumber</ref>
//	 <updatability>readwrite</updatability>
//	 <hidden>false</hidden>
//	 </property>
//	 <property>
//	 <ref>mobileNumber</ref>
//	 <updatability>readwrite</updatability>
//	 <hidden>false</hidden>
//	 </property>
//	 <property>
//	 <ref>faxNumber</ref>
//	 <updatability>readwrite</updatability>
//	 <hidden>false</hidden>
//	 </property>
//	 <property>
//	 <ref>description</ref>
//	 <updatability>readwrite</updatability>
//	 <hidden>false</hidden>
//	 <richText>true</richText>
//	 </property>
//	 <property>
//	 <ref>experience</ref>
//	 <updatability>readwrite</updatability>
//	 <hidden>false</hidden>
//	 <richText>true</richText>
//	 </property>
//	 <property>
//	 <ref>profileLinks</ref>
//	 <updatability>readwrite</updatability>
//	 <hidden>false</hidden>
//	 </property>
//   <property>
//	 <ref>email</ref>
//	 <updatability>read</updatability>
//	 <hidden>false</hidden>
//	 </property>
//	 <property>
//	 <ref>displayName</ref>
//	 <updatability>read</updatability>
//	 <hidden>false</hidden>
//	 </property>
//	 </type>
//	 <type>
//	 <parentId>default</parentId>
//	 <id>dopey</id>
//	 <orgId>AAA</orgId>
//	 <property>
//	 <ref>displayName</ref>
//	 <updatability>readwrite</updatability>
//	 <hidden>false</hidden>
//	 </property>
//	 </type>
//	 <type>
//	 <parentId>dopey</parentId>
//	 <id>sleepy</id>
//	 <orgId>AAA</orgId>
//	 <property>
//	 <ref>email</ref>
//	 <updatability>readwrite</updatability>
//	 <hidden>false</hidden>
//	 </property>
//	 </type>
//	 </config>
	
	private String xmlString = "<config xmlns=\"http://www.ibm.com/profiles-types\" id=\"profiles-types\"><type><parentId>snx:person</parentId><id>default</id><orgId>AAA</orgId><property><ref>telephoneNumber</ref><updatability>readwrite</updatability><hidden>false</hidden></property><property><ref>ipTelephoneNumber</ref><updatability>readwrite</updatability><hidden>false</hidden></property><property><ref>mobileNumber</ref><updatability>readwrite</updatability><hidden>false</hidden></property><property><ref>faxNumber</ref><updatability>readwrite</updatability><hidden>false</hidden></property><property><ref>description</ref><updatability>readwrite</updatability><hidden>false</hidden><richText>true</richText></property><property><ref>experience</ref><updatability>readwrite</updatability><hidden>false</hidden><richText>true</richText></property><property><ref>profileLinks</ref><updatability>readwrite</updatability><hidden>false</hidden></property><property><ref>email</ref><updatability>read</updatability><hidden>false</hidden></property><property><ref>displayName</ref><updatability>read</updatability><hidden>false</hidden></property></type><type><parentId>default</parentId><orgId>AAA</orgId><id>dopey</id><property><ref>displayName</ref><updatability>readwrite</updatability><hidden>false</hidden></property></type><type><parentId>dopey</parentId><id>sleepy</id><orgId>AAA</orgId><property><ref>email</ref><updatability>readwrite</updatability><hidden>false</hidden></property></type></config>";
}
