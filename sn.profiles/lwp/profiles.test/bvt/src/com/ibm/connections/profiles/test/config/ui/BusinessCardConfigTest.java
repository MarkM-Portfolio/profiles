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
package com.ibm.connections.profiles.test.config.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ibm.lconn.profiles.config.BaseConfigObject;
import com.ibm.lconn.profiles.config.ui.UIBusinessCardActionConfig;
import com.ibm.lconn.profiles.config.ui.UIBusinessCardConfig;
import com.ibm.lconn.profiles.config.ui.UIConfig;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.data.Employee;

/**
 *
 *
 */
public class BusinessCardConfigTest extends BaseTestCase {
	
	private UIConfig config = UIConfig.instance();
	
	public void testGetAllConfigs() {
		assertEquals(1, config.getBusinessCardConfigs().size());
	}
	
	public void testGetDefaultConfig() {
		assertEquals(BaseConfigObject.DEFAULT, config.getBusinessCardConfig(BaseConfigObject.DEFAULT).getProfileType());
		assertEquals(BaseConfigObject.DEFAULT, config.getBusinessCardConfig("bogus-name").getProfileType());
	}
			
	private static final String bizCard = "javascript:lconn.profiles.bizCard.bizCardUI.openNetworkInviteDialog(";
	
	private static final String follow = "javascript:lconn.profiles.bizCard.bizCardUI.followUser(";
	
	private static String[][] actions = {
		{"mailto:", "personCardSendMail"},
		{"/html/wc.do?action=fr&requireAuth=true&widgetId=friends&targetKey=", "personCardAddAsColleagues"},
		{bizCard, "personCardAddAsColleagues"},
		{follow, "personCardFollow"}
	};
	
	private static String[][] actionsNoEmail = {
		{"/html/wc.do?action=fr&requireAuth=true&widgetId=friends&targetKey=", "personCardAddAsColleagues"},
		{bizCard, "personCardAddAsColleagues"},
		{follow, "personCardFollow"}
	};
		
	public void test_sametime_action_options() {
		UIBusinessCardConfig bizCardConfig = config.getBusinessCardConfig("default");
		
		assertTrue(bizCardConfig.isEnableSametimeAwareness());
//		assertFalse(bizCardConfig.isShowSametimeCallAction());
//		assertFalse(bizCardConfig.isShowSametimeChatAction());
	}

	public void test_have_correct_acls_for_actions() {
		UIBusinessCardConfig bizCardConfig = config.getBusinessCardConfig("default");
		
		Map<String,String> m = new HashMap<String,String>();
		m.put("email","email@ibm.com");
		
		Employee e = new Employee();
		e.setEmail(m.get("email"));
		e.setKey("foobar");
		
		UIBusinessCardConfig.CONFIG_UNIT_TEST = true;
		List<UIBusinessCardActionConfig> acs = bizCardConfig.getActions(true, e, m, false, true);
		
		assertEquals("Missing some actions.", 3, acs.size());
		
	}
	
	public void test_acl_check_fail_for_unit() {
//		fail("TODO");
	}
	
}
