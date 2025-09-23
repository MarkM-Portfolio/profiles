/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.bss;

import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import com.ibm.connections.multitenant.bss.provisioning.endpoint.BSSProvisioningEndpoint;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSProtocol;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSRemoveOrganizationServiceData;
import com.ibm.connections.multitenant.bss.provisioning.protocol.BSSServiceData;
import com.ibm.json.java.JSONObject;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.bss.BSSCommandConsumer;
import com.ibm.lconn.profiles.internal.config.MTConfigHelper;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.ProfileServiceBase;
import com.ibm.lconn.profiles.internal.service.ProfilesAppService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.TestAppContext;
import com.ibm.lconn.profiles.test.TestConfig;
import com.ibm.lconn.profiles.test.util.directory.MockDSOrganizationObject;
import com.ibm.lconn.profiles.test.util.directory.MockDSSubscriberObject;
import com.ibm.lconn.profiles.test.util.directory.MockProfileProviderConfig;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.MockAdmin;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

public abstract class BSSTestBase extends BaseTransactionalTestCase {
	private BSSProvisioningEndpoint endpoint;
	
	static MockProfileProviderConfig CONFIG = MockProfileProviderConfig.getInstance();

	public static final String guestCustID = "0";
	public static final String unknownCustID = "9875654";
	public static final String unknownSubscriberID = "9875654";
	
	static MockDSOrganizationObject cust1 = CONFIG.getOrganization("org_1_exid");
	static MockDSOrganizationObject cust2 =  CONFIG.getOrganization("org_2_exid");
	static MockDSOrganizationObject deleteCust = CONFIG.getOrganization("delete_org_exid");
	static MockDSOrganizationObject orphanCust = CONFIG.getOrganization("orphaned_org_exid");

	static MockDSSubscriberObject subscriber1_1 = CONFIG.getSubscriber("org_1_exid","subscriber_1_org_1_exid");
	static MockDSSubscriberObject subscriberTomove_1 = CONFIG.getSubscriber("org_1_exid","subscriber_tomove_org_1_exid");
	static MockDSSubscriberObject subscriber1_2 = CONFIG.getSubscriber("org_2_exid","subscriber_1_org_2_exid");
	static MockDSSubscriberObject deleteguy1 = CONFIG.getSubscriber("delete_org_exid","subscriber_1_org_delete_exid");
	static MockDSSubscriberObject guestguy = CONFIG.getSubscriber(MTConfigHelper.LOTUS_LIVE_GUEST_ORG_ID,"guestguy");
	static MockDSSubscriberObject subscriber1_orphan = CONFIG.getSubscriber("orphaned_org_exid","subscriber_1_orphanedorg_exid");
	
	public static String defaultLocale = "en_us";

	@Autowired	protected ProfileServiceBase profSvcBase;
	@Autowired	protected ProfilesAppService _profileAppService;
	@Autowired	protected PeoplePagesService _pps;
	@Autowired	protected ProfileLoginService _loginService;
	@Autowired	protected TDIProfileService _tdiProfileService;

	BSSTestBase(){
	}
	
	public void onSetUpBeforeTransactionDelegate() throws Exception {
		if (_pps == null) {
			_profileAppService = AppServiceContextAccess.getContextObject(com.ibm.lconn.profiles.internal.service.ProfilesAppService.class);
			_pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			_loginService = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
			_tdiProfileService = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		}
		runAsAdmin(Boolean.TRUE);
		TestAppContext ctx = (TestAppContext) AppContextAccess.getContext();
		ctx.setBSSContext(true);
		// pre-bvt ant task "copy-main-config-files" now sets this class as the provider, store it here for direct access pattern
		endpoint = new BSSProvisioningEndpoint(new BSSCommandConsumer());
		// no need to register mockProfileProvder. tests inject its mock via directoryservices.xml using the standard mock for all unit tests
		//BSSProvisioningEndpoint.mockProfileProvider = WaltzClientFactory.getDSProvider();
		BSSProvisioningEndpoint.SUPPORTS_SYNC_OP = true;
		// enable tenant constraint for queries
		HashMap<ConfigProperty,String> props = new HashMap<ConfigProperty,String>();
		props.put(ConfigProperty.PROFILE_APPLY_TENANT_CONSTRAINT,"true");
		TestConfig.instance().setConfigProperties(props);
		
		// test getting value
		boolean val = ProfilesConfig.instance().getProperties().getBooleanValue(ConfigProperty.PROFILE_APPLY_TENANT_CONSTRAINT);
		System.out.println("val = "+val);
	}

	@Override
	protected void onSetUpInTransaction() {
	}
	
	protected Tenant createTenant(String extId, String name) {
		Tenant rtnVal = CreateUserUtil.createTenant(extId,name);
		return rtnVal;
	}
	
	protected Tenant getTenant(String orgId){
		Tenant rtnVal = CreateUserUtil.getTenant(orgId);
		return rtnVal;
	}

	protected String createSubscriber(String subscriberExId, String tenantKey, String email, String displayName) {
		Employee e = CreateUserUtil.createProfile(subscriberExId,tenantKey,subscriberExId,email,displayName,null);		
		return e.getKey();
	}
	
	protected Employee lookupSubscriberByExid(String orgId, String exId){
		return CreateUserUtil.lookupProfileByExid(orgId,exId);
	}
	
	protected final JSONObject processMessage(JSONObject json) throws Exception{
		Context ctx = AppContextAccess.getContext();
		// TODO: we could/should use a clone here. willie is working on one.
		// mimic the AppContext that comes from AppContextFilter for a BSS call.
		// store original values we'll restore.
		String origTenant = ctx.getTenantKey();
		Employee origCurrentEmployee = ctx.getCurrentUserProfile();
		boolean origIsAdmin = ctx.isAdmin();
		JSONObject rtn = null;
		try{
			ctx.setTenantKey(null);
			ctx.setAdministrator(true);
			((TestAppContext)ctx).setCurrUser(MockAdmin.INSTANCE);
			rtn = endpoint.processJsonMessage(json, "bssAdminUser");
		}
		finally{
			ctx.setTenantKey(origTenant);
			ctx.setAdministrator(origIsAdmin);
			((TestAppContext)ctx).setCurrUser(origCurrentEmployee);
		}
		return rtn;
	}
	
	protected static String getSuccess(JSONObject jobj) {
		try {
			JSONObject statusObj = (JSONObject) jobj.get(BSSProtocol.M_STATUS);
			String succeed = (String) statusObj.get(BSSProtocol.M_SUCCEEDED);
			return succeed;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "false";
	}
	
    public static void assertSuccess(JSONObject jobj, String failMessage) {
    	if (!BSSProtocol.getStatus(jobj).getSuccess()) {
    		fail(failMessage);
    	}    	
    }
    
    public static void assertFailure(JSONObject jobj, String failMessage) {
    	if (BSSProtocol.getStatus(jobj).getSuccess()) {
    		fail(failMessage);
    	}    	
    }
}
