/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.connections.profiles.test.config;

import java.util.Map;

import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.MessageAclEnum;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.WorkflowEnum;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ConnectionTypeConfigTest extends BaseTestCase 
{
	Map<String, ? extends ConnectionTypeConfig> ctc = 
		DMConfig.instance().getConnectionTypeConfigs();
	
	public void testConnectionTypeMap()
	{
		assertNotNull(ctc);
		assertEquals(1,ctc.size());
	}
	
	public void testColleaguesConnectionType()
	{
		assertNotNull(ctc);
		
		ConnectionTypeConfig tc = ctc.get(PeoplePagesServiceConstants.COLLEAGUE);
		assertNotNull(tc);
		assertEquals(PeoplePagesServiceConstants.COLLEAGUE, tc.getType());
		assertTrue(WorkflowEnum.CONFIRMED.equals(tc.getWorkflow()));
		assertTrue(MessageAclEnum.SOURCE.equals(tc.getMessageAcl()));
	}
}
