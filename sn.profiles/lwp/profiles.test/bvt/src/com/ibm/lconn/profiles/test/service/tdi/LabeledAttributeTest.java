/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service.tdi;

import org.apache.commons.lang.StringUtils;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileExtensionService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.TestAppContext;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class LabeledAttributeTest extends BaseTransactionalTestCase {

	public static final String LABEL_RW_ATTR = "labelrw";   // must match profile-types.xml
	public static final String LABEL_R_ATTR  = "labelr";    // must match profile-types.xml
	public static final String LABEL_VALUE   = "LABELATTR"; // must match profile-types.xml
	public static final String PROFILETYPE   = "default";
	public static final String ATTRVALUE     = "blahvalue";

	private TDIProfileService       adminService = null;
	private ProfileExtensionService pes = null;

	protected void onSetUpBeforeTransactionDelegate() {
		if (adminService == null) {
			adminService = AppServiceContextAccess.getContextObject(TDIProfileService.class);
			pes = AppServiceContextAccess.getContextObject(ProfileExtensionService.class);
		}
		CreateUserUtil.setTenantContext();
		runAsAdmin(false); 
	}

	public void testAttrExistence() throws Exception {
		boolean attrsDefined = isAttrsDefined();
		if (attrsDefined == false) {
			// TODO - need logger
			System.out.println("Config issue: All attributes are not defined in config files for profile type " + PROFILETYPE + ": "
					+ LABEL_R_ATTR + ", " + LABEL_RW_ATTR);
			assertTrue(false);
		}
	}

	public void testRWAttributeEmptyInput() {
		// create with label = null value = null
		if (isAttrsDefined() == false) {
			return;
		}
		Employee user = CreateUserUtil.createProfile();
		runAs(user,false); // why is a TDI test trying to run a non-Admin
		// RW attribute
		updateRWAttr(user,null,null);
		// retrieve the extension attribute
		ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(pedb == null);
		// add some values and we should get an attribute
		String mylabel = "mylabel";
		updateRWAttr(user,mylabel,ATTRVALUE);
		pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(pedb != null);
		// null out values which should delete the attribute
		updateRWAttr(user,null,null);
		pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(pedb == null);
	}

	public void testRWAttributeAddNameAddValue() {
		// create with label = mylabel value = blah.
		if (isAttrsDefined() == false) {
			return;
		}
		Employee user = CreateUserUtil.createProfile();
		runAs(user,false); // why is a TDI test trying to run a non-Admin
		//
		String mylabel = "mylabel";
		updateRWAttr(user,mylabel,ATTRVALUE);
		// retrieve the extension attribute
		ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(mylabel.equals(pedb.getName()));
		assertTrue(ATTRVALUE.equals(pedb.getStringValue()));
	}

	public void testRWAttributeNameOnly() {
		// create with label = mylabel value = null
		if (isAttrsDefined() == false) {
			return;
		}
		Employee user = CreateUserUtil.createProfile();
		runAs(user,false); // why is a TDI test trying to run a non-Admin
		//
		String mylabel = "mylabel";
		updateRWAttr(user,mylabel,null);
		// retrieve the extension attribute
		ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(mylabel.equals(pedb.getName()));
		assertTrue(StringUtils.isEmpty(pedb.getStringValue()));
	}
	
	public void testRWAttributeValueOnly() {
		// create with label = null value = blah
		if (isAttrsDefined() == false) {
			return;
		}
		Employee user = CreateUserUtil.createProfile();
		runAs(user,false); // why is a TDI test trying to run a non-Admin
		//
		updateRWAttr(user,null,ATTRVALUE);
		// retrieve the extension attribute
		ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(LABEL_VALUE.equals(pedb.getName()));
		assertTrue(ATTRVALUE.equals(pedb.getStringValue()));
		// update
		String someValue = "someValue";
		updateRWAttr(user,null,someValue);
		// retrieve the extension attribute
		pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(LABEL_VALUE.equals(pedb.getName()));
		assertTrue(someValue.equals(pedb.getStringValue()));
	}
	
	public void testRWAttributeUpdateName() {
		// start with label = null value = blah.
		// then update label.
		if (isAttrsDefined() == false) {
			return;
		}
		Employee user = CreateUserUtil.createProfile();
		runAs(user,false); // why is a TDI test trying to run a non-Admin
		//
		updateRWAttr(user,null,ATTRVALUE);
		// retrieve the extension attribute
		ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(LABEL_VALUE.equals(pedb.getName())); // should get default value
		assertTrue(ATTRVALUE.equals(pedb.getStringValue()));
		// 
		String mylabel = "mylabel";
		updateRWAttr(user,mylabel,ATTRVALUE);
		// retrieve the extension attribute and see that it is deleted
		pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(mylabel.equals(pedb.getName())); // should get my value
		assertTrue(ATTRVALUE.equals(pedb.getStringValue()));
		// update again
		String anotherlabel = "anotherlabel";
		updateRWAttr(user,anotherlabel,ATTRVALUE);
		// retrieve the extension attribute and see that it is deleted
		pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(anotherlabel.equals(pedb.getName())); // should get my value
		assertTrue(ATTRVALUE.equals(pedb.getStringValue()));
	}

	public void testRWAttributeRemoveValue() {
		// start with label = mylable value = blah
		// then update with value = null
		if (isAttrsDefined() == false) {
			return;
		}
		Employee user = CreateUserUtil.createProfile();
		runAs(user,false); // why is a TDI test trying to run a non-Admin
		//
		String mylabel = "mylabel";
		updateRWAttr(user,mylabel,ATTRVALUE);
		// retrieve the extension attribute
		ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(mylabel.equals(pedb.getName()));
		assertTrue(ATTRVALUE.equals(pedb.getStringValue()));
		// null out the value. the label should still be persisted as this is user input
		updateRWAttr(user,mylabel,null);
		// retrieve the extension attribute and see that it is deleted
		pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(mylabel.equals(pedb.getName()));
		assertTrue(StringUtils.isEmpty(pedb.getStringValue()));
	}

	public void testRWAttributeRemoveLabel() {
		if (isAttrsDefined() == false) {
			return;
		}
		Employee user = CreateUserUtil.createProfile();
		runAs(user,false); // why is a TDI test trying to run a non-Admin
		//
		String mylabel = "mylabel";
		updateRWAttr(user,mylabel,ATTRVALUE);
		// retrieve the extension attribute
		ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(mylabel.equals(pedb.getName()));
		assertTrue(ATTRVALUE.equals(pedb.getStringValue()));
		// null out the value. the label should still be persisted as this is user input
		updateRWAttr(user,null,ATTRVALUE);
		// retrieve the extension attribute and see that it is deleted
		pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(StringUtils.equals(LABEL_VALUE, pedb.getName())); // we wiped out name, should get default now
		assertTrue(ATTRVALUE.equals(pedb.getStringValue()));
	}

	public void testRWAttributeRemoveValueRemoveLabel() {
		if (isAttrsDefined() == false) {
			return;
		}
		Employee user = CreateUserUtil.createProfile();
		runAs(user,false); // why is a TDI test trying to run a non-Admin
		//
		String mylabel = "mylabel";
		updateRWAttr(user,mylabel,ATTRVALUE);
		// retrieve the extension attribute
		ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(mylabel.equals(pedb.getName()));
		assertTrue(ATTRVALUE.equals(pedb.getStringValue()));
		// null out the value. the label should still be persisted as this is user input
		updateRWAttr(user,null,null);
		// retrieve the extension attribute and see that it is deleted
		pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_RW_ATTR);
		assertTrue(pedb == null);
	}

	public void testRAttributeEmptyInput() {
		// create with label = null value = null
		if (isAttrsDefined() == false) {
			return;
		}
		Employee user = CreateUserUtil.createProfile();
		runAs(user,false); // why is a TDI test trying to run a non-Admin // why is a TDI test trying to run a non-Admin
		//
		updateRAttr(user,null,null);
		// retrieve the extension attribute
		ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_R_ATTR);
		assertTrue(pedb == null);
		// add some values and we should get an attribute
		String ignoreme = "ignoreme";
		updateRAttr(user,ignoreme,ATTRVALUE);
		pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_R_ATTR);
		assertTrue(pedb != null);
		// null out values which should delete the attribute
		updateRAttr(user,null,null);
		pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_R_ATTR);
		assertTrue(pedb == null);
	}

	public void testRAttribute() {
		if (isAttrsDefined() == false) {
			return;
		}
		Employee user = CreateUserUtil.createProfile();
		runAs(user,false); // why is a TDI test trying to run a non-Admin
		//
		updateRAttr(user,"ignoreme",ATTRVALUE);
		// retrieve the extension attribute
		ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_R_ATTR);
		assertTrue(LABEL_VALUE.equals(pedb.getName()));
		assertTrue(ATTRVALUE.equals(pedb.getStringValue()));
	}

	public void testRAttributeRemoveValue() {
		if (isAttrsDefined() == false) {
			return;
		}
		Employee user = CreateUserUtil.createProfile();
		runAs(user,false); // why is a TDI test trying to run a non-Admin
		//
		updateRAttr(user,"ignoreme",ATTRVALUE);
		// retrieve the extension attribute
		ProfileExtension pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_R_ATTR);
		assertTrue(LABEL_VALUE.equals(pedb.getName()));
		assertTrue(ATTRVALUE.equals(pedb.getStringValue()));
		// null out the value. the label should also be ignored and the attribute deleted
		updateRAttr(user,"ignoreme",null);
		// retrieve the extension attribute and see that it is deleted
		pedb = pes.getProfileExtension(ProfileLookupKey.forKey(user.getKey()), LABEL_R_ATTR);
		assertTrue(pedb == null);
	}

	private boolean isAttrsDefined() {
		boolean rtnVal = true;
		ProfileType profileType = ProfileTypeHelper.getProfileType(PROFILETYPE);
		Property p = null;
		p = profileType.getPropertyById(LABEL_RW_ATTR);
		rtnVal &= (p != null);
		p = profileType.getPropertyById(LABEL_R_ATTR);
		rtnVal &= (p != null);
		return rtnVal;
	}

	private void updateRAttr(Employee user, String label, String value){
		ProfileExtension pe = new ProfileExtension();
		pe.setPropertyId(LABEL_R_ATTR);
		pe.setStringValue(value);
		pe.setName(label);
		user.setProfileExtension(pe);
		// update user
		ProfileDescriptor pdesc = new ProfileDescriptor();
		pdesc.setProfile(user);
		boolean isAdmin = false;
		try {
			TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
			isAdmin = ctx.isAdmin();
		}
		catch (Exception e) {
			assert (false); // should never hit this
		}
		if (isAdmin)
			adminService.update(pdesc);
		else {
			pes.updateProfileExtension(pe);
		}
	}
	
	private void updateRWAttr(Employee user, String label, String value){
		ProfileExtension pe = new ProfileExtension();
		pe.setPropertyId(LABEL_RW_ATTR);
		pe.setStringValue(value);
		pe.setName(label);
		user.setProfileExtension(pe);
		// update user
		ProfileDescriptor pdesc = new ProfileDescriptor();
		pdesc.setProfile(user);
		boolean isAdmin = false;
		try {
			TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
			isAdmin = ctx.isAdmin();
		}
		catch (Exception e) {
			assert (false); // should never hit this
		}
		if (isAdmin)
			adminService.update(pdesc);
		else {
			pes.updateProfileExtension(pe);
		}
	}
}
