/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.web.ui.forms;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.TestAppContext;
import com.ibm.lconn.profiles.web.ui.actions.person.forms.NetworkInviteEE;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 * @author ahernm@us.ibm.com
 * 
 * This test case assumes the bluepages 8M data set
 *
 */
public class NetworkInviteEETest extends BaseTransactionalTestCase 
{
	
//	private static final String ahernm = "ahernm@us.ibm.com";
//	private static final String jlu = "zhouwen_lu@us.ibm.com";
//	private static final String jackief = "jackief@us.ibm.com";
//	
//	private Employee AHERNM, JLU, JACKIEF;
//
//	private PeoplePagesService pps;
//	private ConnectionService cs;
//	
//	public void onSetUpBeforeTransactionDelegate() throws Exception
//	{
//		// Setup service and test profiles
//		if (pps == null) pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
//		if (cs == null) cs = AppServiceContextAccess.getContextObject(ConnectionService.class);
//
//		if (AHERNM == null || JLU == null || JACKIEF == null)
//		{
//			AHERNM = pps.getProfile(ProfileLookupKey.forEmail(ahernm), ProfileRetrievalOptions.MINIMUM);
//			JLU = pps.getProfile(ProfileLookupKey.forEmail(jlu), ProfileRetrievalOptions.MINIMUM);
//			JACKIEF = pps.getProfile(ProfileLookupKey.forEmail(jackief), ProfileRetrievalOptions.MINIMUM);
//			
//			assertNotNull(AHERNM);
//			assertNotNull(JLU);
//			assertNotNull(JACKIEF);
//		}		
//		TestAppContext.setCurrUserEmail(ahernm);
//	}
//	
//	public void onTearDownAfterTransaction() throws Exception
//	{
//		TestAppContext.setCurrUserEmail(TestAppContext.DEFAULT_EMAIL);
//		TestAppContext.getRoleMap().remove("search-admin");
//	}
//	
//	public void testInviteEENoCommonFriends() throws Exception
//	{				
//		Connection j2m = new Connection();
//		j2m.setSourceKey(JLU.getKey());
//		j2m.setTargetKey(AHERNM.getKey());
//		j2m.setMessage(ahernm);
//		String conn1Id = cs.createConnection(j2m);
//		
//		TestAppContext.setCurrUserEmail(jlu);
//		NetworkInviteEE action = new NetworkInviteEE();
//		action.setConnId(conn1Id);
//		action.setSourceUserId(JLU.getUserid());
//		action.view();
//		
//		assertNotNull(action.getFriend());
//		assertEquals(AHERNM.getKey(), action.getFriend().getKey());
//		assertEquals(0, action.getCommonFriendCount());
//		validateConnection(j2m, action.getConnection());
//		
//		cs.deleteConnection(conn1Id);
//		
//		TestAppContext.setCurrUserEmail(ahernm);
//		
//	}
//	
//	public void testInviteEEWithCommonFriends () throws Exception 
//	{
//		
//		//
//		// Create connections
//		//
//		Connection j2m = new Connection();
//		j2m.setSourceKey(JLU.getKey());
//		j2m.setTargetKey(AHERNM.getKey());
//		j2m.setMessage(ahernm);
//		String conn1Id = cs.createConnection(j2m);
//		
//		TestAppContext.setCurrUserEmail(jlu);
//		cs.acceptConnection(conn1Id);
//		
//		TestAppContext.setCurrUserEmail(jackief);
//		Connection j2j = new Connection();
//		j2j.setSourceKey(JLU.getKey());
//		j2j.setTargetKey(JACKIEF.getKey());
//		j2j.setMessage(jackief);
//		String conn2Id = cs.createConnection(j2j);
//		
//		TestAppContext.setCurrUserEmail(jlu);
//		cs.acceptConnection(conn2Id);
//		
//		TestAppContext.setCurrUserEmail(ahernm);
//		j2m = new Connection();
//		j2m.setSourceKey(JACKIEF.getKey());
//		j2m.setTargetKey(AHERNM.getKey());
//		j2m.setMessage(ahernm);
//		String conn3Id = cs.createConnection(j2m);
//		
//		TestAppContext.setCurrUserEmail(jackief);
//		NetworkInviteEE action = new NetworkInviteEE();
//		action.setConnId(conn3Id);
//		action.setSourceUserId(JACKIEF.getUserid());
//		action.view();
//		
//		assertNotNull(action.getFriend());
//		assertEquals(AHERNM.getKey(), action.getFriend().getKey());
//		assertEquals(1, action.getCommonFriendCount());
//		assertEquals(1, action.getCommonFriends().size());
//		validateConnection(j2m, action.getConnection());
//		
//		TestAppContext.setCurrUserEmail(jackief);
//		cs.deleteConnection(conn3Id);
//		
//		TestAppContext.setCurrUserEmail(jlu);
//		cs.deleteConnection(conn1Id);
//		cs.deleteConnection(conn2Id);		
//	}
//	
//	private void validateConnection(Connection expect, Connection got)
//	{
//		assertNotNull(got);
//		assertEquals(expect.getSourceKey(), got.getSourceKey());
//		assertEquals(expect.getTargetKey(), got.getTargetKey());
//		assertEquals(expect.getMessage(), got.getMessage());
//		assertEquals(expect.getStatus(), got.getStatus());
//		assertEquals(expect.getType(), got.getType());
//	}	
}
