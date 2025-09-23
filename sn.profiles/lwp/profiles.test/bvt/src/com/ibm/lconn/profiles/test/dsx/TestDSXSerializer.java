/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.dsx;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.dsx.actions.DSXSerializer;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.RoleDao;
import com.ibm.lconn.profiles.internal.util.DSXHelper;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.util.RoleTestHelper;
import com.ibm.peoplepages.data.Employee;

import com.ibm.peoplepages.service.PeoplePagesService;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class TestDSXSerializer extends BaseTransactionalTestCase
{
	Employee profile = null;
	PeoplePagesService pps = null;
	ProfileLoginService loginSvc = null;
	RoleDao roleDao = null;


	protected void onSetUpBeforeTransactionDelegate() throws Exception {

		if (pps == null) {
			pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			loginSvc = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
			roleDao = AppServiceContextAccess.getContextObject(RoleDao.class);
		}		
		runAsAdmin(Boolean.TRUE);
	}

	protected void onSetUpInTransaction() {
	}

	public void testSerial() throws IOException, XMLStreamException
	{
		profile = CreateUserUtil.createProfile();
		String rand = java.util.UUID.randomUUID().toString();
		List<String> logins = new ArrayList<String>();
		logins.add("foo." + rand);
		logins.add("bar." + rand);
		loginSvc.setLogins(profile.getKey(), logins);
		Map<String,List<String>> loginMap = DSXHelper.loginsToMapList(loginSvc.getLoginsForKeys(Collections.singletonList(profile.getKey())));

		List<EmployeeRole> roles = RoleTestHelper.createRoles(profile, new String[]{"aaa","bbb"});
		roleDao.addRoles(profile.getKey(),roles);
		roles = roleDao.getRoleIdsForKeys(Collections.singletonList(profile.getKey()));
		Map<String,List<String>> roleMap = DSXHelper.rolesToMapList(roles);

		DSXSerializer ds = new DSXSerializer(new PrintWriter(System.out));
		// write the feed with email address suppressed
		ds.writeDSXFeed(Collections.singletonList(profile), loginMap, roleMap, false, false, false);
		System.out.println("\n\n");

		// write the feed with email address included
		ds.writeDSXFeed(Collections.singletonList(profile), loginMap, roleMap, true, false, false);
		System.out.println("\n");
	}

	public void testSerialWithoutRoles() throws IOException, XMLStreamException
	{
		profile = CreateUserUtil.createProfile();
		String rand = java.util.UUID.randomUUID().toString();
		List<String> logins = new ArrayList<String>();
		logins.add("foo.norole." + rand);
		logins.add("bar.norole." + rand);
		loginSvc.setLogins(profile.getKey(), logins);
		Map<String,List<String>> loginMap = DSXHelper.loginsToMapList(loginSvc.getLoginsForKeys(Collections.singletonList(profile.getKey())));

		List<EmployeeRole> roles = null;
		Map<String,List<String>> roleMap = DSXHelper.rolesToMapList(roles);

		DSXSerializer ds = new DSXSerializer(new PrintWriter(System.out));
		// write the feed with email address suppressed
		ds.writeDSXFeed(Collections.singletonList(profile), loginMap, roleMap, false, false, false);
		System.out.println("\n\n");

		// write the feed with email address included
		ds.writeDSXFeed(Collections.singletonList(profile), loginMap, roleMap, true, false, false);
		System.out.println("\n");
	}
}
