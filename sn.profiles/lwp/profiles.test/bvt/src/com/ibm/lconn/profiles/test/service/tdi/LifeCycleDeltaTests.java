/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service.tdi;

import com.ibm.lconn.profiles.internal.data.profile.AttributeGroup;
import com.ibm.lconn.profiles.internal.service.AdminProfileServiceImpl;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.data.Employee;

public class LifeCycleDeltaTests extends BaseTestCase {

	/**
	 * Need to test that lifecycle 
	 */
	public void test_profile_type_finds_change() {
		testSize();
		
		Employee e1 = new Employee();
		e1.setProfileType("foo");
		
		Employee e2 = new Employee();
		e2.setProfileType("bar");
		
		assertFalse(AdminProfileServiceImpl.lifeCycleDataChange(e1, e1));
		assertTrue(AdminProfileServiceImpl.lifeCycleDataChange(e1, e2));
		
		// Edge case - should generate change from '<empty' string.  This mimics how TDI blanks fields.
		e2.setProfileType("");
		assertTrue(AdminProfileServiceImpl.lifeCycleDataChange(e1, e2));
		
		// Edge case - should NOT generate change.  Null indicates no attempt to modify field
		// needs review. null is treated as default. why is this not a change?
		//e2.setProfileType(null);
		//assertFalse(AdminProfileServiceImpl.lifeCycleDataChange(e1, e2));
	}
	
	/**
	 * Need to test that lifecycle 
	 */
	public void test_uid_finds_change() {
		testSize();
		
		Employee e1 = new Employee();
		e1.setUid("foo");
		
		Employee e2 = new Employee();
		e2.setUid("bar");
		
		assertFalse(AdminProfileServiceImpl.lifeCycleDataChange(e1, e1));
		assertTrue(AdminProfileServiceImpl.lifeCycleDataChange(e1, e2));
	}
	
	/**
	 * Need to test that lifecycle 
	 */
	public void test_email_finds_change() {
		testSize();
		
		Employee e1 = new Employee();
		e1.setEmail("foo");
		
		Employee e2 = new Employee();
		e2.setEmail("bar");
		
		assertFalse(AdminProfileServiceImpl.lifeCycleDataChange(e1, e1));
		assertTrue(AdminProfileServiceImpl.lifeCycleDataChange(e1, e2));
	}
	
	/**
	 * Need to test that lifecycle 
	 */
	public void test_guid_finds_change() {
		testSize();
		
		Employee e1 = new Employee();
		e1.setGuid("foo");
		
		Employee e2 = new Employee();
		e2.setGuid("bar");
		
		assertFalse(AdminProfileServiceImpl.lifeCycleDataChange(e1, e1));
		assertTrue(AdminProfileServiceImpl.lifeCycleDataChange(e1, e2));
	}
	
	/**
	 * Need to test that lifecycle 
	 */
	public void test_loginId_finds_change() {
		testSize();
		
		Employee e1 = new Employee();
		e1.setLoginId("foo");
		
		Employee e2 = new Employee();
		e2.setLoginId("bar");
		
		assertFalse(AdminProfileServiceImpl.lifeCycleDataChange(e1, e1));
		assertTrue(AdminProfileServiceImpl.lifeCycleDataChange(e1, e2));
	}

	public void testSize(){
		assertEquals(6, AttributeGroup.LIFECYCLE_ATTRS.size());
	}
}
