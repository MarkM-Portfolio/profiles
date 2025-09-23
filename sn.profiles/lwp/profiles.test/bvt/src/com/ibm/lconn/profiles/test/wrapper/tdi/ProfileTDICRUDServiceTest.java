/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.wrapper.tdi;

import com.ibm.di.entry.Entry;
import com.ibm.lconn.profiles.api.tdi.service.ProfilesTDICRUDService;
import com.ibm.lconn.profiles.api.tdi.service.TDIException;
import com.ibm.lconn.profiles.api.tdi.service.impl.ProfilesTDICRUDServiceImpl;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class ProfileTDICRUDServiceTest extends BaseWrapperTestCase {
	
	ProfilesTDICRUDService profilesTDICrudSvc;
	Entry mockEntry;
	static String key;
	@Override
	protected void setUp() throws Exception {
		ProfilesTDICRUDServiceImpl profileTDICrudSvcImp 
			= ProfilesTDICRUDServiceImpl.getInstance();
		profilesTDICrudSvc = (ProfilesTDICRUDService) profileTDICrudSvcImp;
		mockEntry = ProfileEntryBuilder.buildEntry("Profile1.properties");
		
	}
	//temp test
	
	public void testGetAmy(){
		String uid = "Amy Jones8";
		try{
			Entry entry = profilesTDICrudSvc.getProfileByUID(uid);
			assertNotNull(entry);
		}catch(TDIException e){
			System.out.println(e);
		}
	}
	public void testCreateEntry(){
		try{
			key = profilesTDICrudSvc.createProfile(mockEntry);
		}catch(TDIException e){
			System.out.println(e);
		}
	}
	
	public void testGetEntryByUID(){
		String uid = "12312313";
		try{
			Entry entry = profilesTDICrudSvc.getProfileByUID(uid);
			assertNotNull(entry);
			assertTrue(entry.getAttribute("uid").getValue().equals(uid));
		}catch(TDIException e){
			System.out.println(e);
		}
	}
	
	public void testGetEntryByEmail(){
		try{
			String email = "lichen@ie.ibm.com";
			Entry entry = profilesTDICrudSvc.getProfileByEmail(email);
			assertNotNull(entry);
			assertTrue(entry.getAttribute("email").getValue().equals(email));
		}catch(TDIException e){
			System.out.println(e);
		}
		}
	
	public void testGetEntryByDN(){
		try{
			String distinguishedName = "cn=Liang Chen,cn=Users,l=WestfordFVT,st=Massachusetts,c=US,ou=Lotus,o=Software Group,dc=ibm,dc=com";
			Entry entry = profilesTDICrudSvc.getProfileByDN(distinguishedName);
			assertNotNull(entry);
			assertTrue(entry.getAttribute("distinguishedName").getValue().equals(distinguishedName));
		}catch(TDIException e){
			System.out.println(e);
		}
	}
	
	public void testGetEntryByGUID(){
		try{
			String guid = "ieu123445";
			Entry entry = profilesTDICrudSvc.getProfileByGUID(guid);
			assertNotNull(entry);
			assertTrue(entry.getAttribute("guid").getValue().equals(guid));
		}catch(TDIException e){
			System.out.println(e);
		}
	}
	
	public void testGetEntryByKey(){
		try{
			//String guid = "ieu123445";
			Entry entry = profilesTDICrudSvc.getProfileByKey(key);
			assertNotNull(entry);
			assertTrue(entry.getAttribute("key").getValue().equals(key));
		}catch(TDIException e){
			System.out.println(e);
		}
	}
	
	public void testUpdateEntry(){
		try{
			mockEntry.getAttribute("email").setValue("changeEmail@hotmail.com");
			mockEntry.addAttributeValue("key", key);
			profilesTDICrudSvc.updateProfile(mockEntry);
		}catch(TDIException e){
			System.out.println(e);
		}
	}
	
	public void testUpdateManagerField(){
		try{
			Entry newEntry = ProfileEntryBuilder.buildEntry("profile2.properties");
			String key = profilesTDICrudSvc.createProfile(newEntry);
			profilesTDICrudSvc.updateManagerField(key);
			Entry theupdatedEntry = profilesTDICrudSvc.getProfileByKey(key);
			String managerField = theupdatedEntry.getString(PeoplePagesServiceConstants.IS_MANAGER);
			assertTrue(managerField.equals("N"));
		}catch(TDIException e){
			System.out.println(e);
		}
		
		
	}
	public void testDelete(){
		try{
			profilesTDICrudSvc.deleteProfile(key);
			Entry entry = profilesTDICrudSvc.getProfileByKey(key);
			assertNull(entry);
		}catch(TDIException e){
			System.out.println(e);
		}
	}
	
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	
}
