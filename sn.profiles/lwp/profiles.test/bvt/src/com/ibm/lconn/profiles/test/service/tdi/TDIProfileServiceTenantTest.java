/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service.tdi;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;

import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.TestCaseHelper;

public class TDIProfileServiceTenantTest extends BaseTransactionalTestCase {

	static {
		Logger.getLogger("org.springframework.orm.ibatis.SqlMapClientTemplate").setLevel(Level.FINEST);
		TestCaseHelper.setupTdiTestEnvironment();
	}
	
	private TDIProfileService ptdiService = null;
	
	protected void onSetUpBeforeTransactionDelegate() {
		if (ptdiService == null) {
			ptdiService = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		}
		runAsAdmin(Boolean.TRUE);
	}
	
	@Override
	protected void onSetUpInTransaction() {
	}
	
	public void testBasicTenantCrud() {
		// create tenant
		Tenant t = new Tenant();
		t.setExid("GobbledyGookExid");
		t.setName("GobbledyGook");
		String key = ptdiService.createTenant(t);
		assertNotNull(key);
		// read - make sure fields are set
		t = ptdiService.getTenant(key);
		assertNotNull(t.getTenantKey());
		assertNotNull(t.getCreated());
		assertNotNull(t.getExid());
		assertNotNull(t.getLastUpdate());
		assertNotNull(t.getLowercaseName());
		assertNotNull(t.getName());
		// update descriptor
		t.setName("GobbledyGook-edited");
		ptdiService.updateTenantDescriptors(t);
		t = ptdiService.getTenant(key);
		assertTrue("GobbledyGook-edited".equals(t.getName()));
		// delete - should be OK, there are no employees
		ptdiService.deleteTenant(key);
		t = ptdiService.getTenant(key);
		assertNull(t);		
	}
	
	public void testTenantIdentifiers(){
		String exid = "texid";
		Tenant t = CreateUserUtil.createTenant(exid,"someName");
		t = ptdiService.getTenantByExid(exid);
		assertTrue((t.getExid()).equals(t.getTenantKey()));
	}
	
	public void testTenantKeyList(){
		String[] ids = {"tenant1id",
						"tenant2id",
						"tenant3id"
						};
		for (int i = 0 ; i < ids.length ; i++){
			CreateUserUtil.createTenant(ids[i],ids[i]);
		}
		List<String> dbList = ptdiService.getTenantKeyList();
		// db may already have tenants. make sure we get out at least what we put in.
		for (int i = 0 ; i < ids.length ; i++){
			assertTrue("missing tenant: "+ids[i], dbList.contains(ids[i]));
		}
	}
}
