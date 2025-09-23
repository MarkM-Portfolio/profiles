/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.atomxslt;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */
public class AtomXsltTestSuite 
{	
	public static Test suite()
	{
		// temp - fix for build
		TestSuite suite = new TestSuite();
		
		suite.addTestSuite(FriendInvitationTestCase.class);
		suite.addTestSuite(LinkrollTestCase.class);
		suite.addTestSuite(RecentFriendTestCase.class);
		suite.addTestSuite(ProfileTagsTestCase.class);
		suite.addTestSuite(ReportToChainTestCase.class);
		suite.addTestSuite(ReportToChainTestCase.class);

		return suite;
	}	
}
