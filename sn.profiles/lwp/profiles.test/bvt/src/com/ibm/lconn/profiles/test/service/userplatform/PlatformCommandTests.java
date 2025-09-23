/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service.userplatform;

import com.ibm.lconn.commands.IPlatformCommandConsumer;
import com.ibm.lconn.commands.IPlatformCommandRecord;
import com.ibm.lconn.commands.IPlatformCommandResponse;
import com.ibm.lconn.commands.IUserLifeCycleConstants;
import com.ibm.lconn.commands.MDBPlatformCommandConsumerRegistry;
import com.ibm.lconn.commands.PlatformCommandResponse;
import com.ibm.lconn.core.platformCommand.tests.BaseUserLifeCycleConsumerTest;

/**
 * 
 * @author vincent
 * 
 */
public class PlatformCommandTests extends BaseUserLifeCycleConsumerTest {

	@Override
	protected void setUp() throws Exception {
		MDBPlatformCommandConsumerRegistry
				.setPlatformCommandConsumer(new MockPlatformCommandConsumer());
	}

	@Override
	public void onTestActivateUser(IPlatformCommandRecord record,
			IPlatformCommandResponse response) {

		assertEquals(record.getCommandName(),
				IUserLifeCycleConstants.USER_RECORD_ACTIVATE);

	}

	@Override
	public void onTestInactivateUser(IPlatformCommandRecord record,
			IPlatformCommandResponse response) {

		assertEquals(record.getCommandName(),
				IUserLifeCycleConstants.USER_RECORD_INACTIVATE);

	}

	@Override
	public void onTestRevokeUser(IPlatformCommandRecord record,
			IPlatformCommandResponse response) {

		assertEquals(record.getCommandName(),
				IUserLifeCycleConstants.USER_RECORD_REVOKE);

	}

	@Override
	public void onTestUpdateUser(IPlatformCommandRecord record,
			IPlatformCommandResponse response) {

		assertEquals(record.getCommandName(),
				IUserLifeCycleConstants.USER_RECORD_UPDATE);

	}

	@Override
	public void onTestSwapUserAccess(IPlatformCommandRecord record,
			IPlatformCommandResponse response) {

		assertEquals(record.getCommandName(),
				IUserLifeCycleConstants.USER_RECORD_SWAP_ACCESS);
		
	}

}

class MockPlatformCommandConsumer implements IPlatformCommandConsumer {

	public IPlatformCommandResponse consumeCommand(IPlatformCommandRecord record) {
		IPlatformCommandResponse response = new PlatformCommandResponse(record);
		return response;
	}
}
