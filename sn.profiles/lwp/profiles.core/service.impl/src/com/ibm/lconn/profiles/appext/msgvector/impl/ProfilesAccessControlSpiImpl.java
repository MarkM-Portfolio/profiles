/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.appext.msgvector.impl;

import com.ibm.lconn.core.appext.data.SNAXPerson;
import com.ibm.lconn.core.appext.msgvector.config.MessageVectorConfig;
import com.ibm.lconn.core.appext.msgvector.data.CommentMessage;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessage;
import com.ibm.lconn.core.appext.msgvector.data.MessageVector;
import com.ibm.lconn.core.appext.msgvector.spi.AccessControlSpi;
import com.ibm.lconn.core.appext.msgvector.spi.MessageVectorAction;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.peoplepages.data.Employee;

public class ProfilesAccessControlSpiImpl implements AccessControlSpi {
	
	public ProfilesAccessControlSpiImpl() {}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.core.appext.msgvector.spi.AccessControlSpi#isFeatureEnabled()
	 */
	public boolean isFeatureEnabled() {
			return PolicyHelper.isFeatureEnabled(Feature.BOARD, (Employee)null);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.core.appext.msgvector.spi.AccessControlSpi#canPerformAction(com.ibm.lconn.core.appext.msgvector.spi.MessageVectorAction, com.ibm.lconn.core.appext.data.SNAXPerson, com.ibm.lconn.core.appext.msgvector.data.MessageVector, com.ibm.lconn.core.appext.msgvector.data.EntryMessage, com.ibm.lconn.core.appext.msgvector.data.CommentMessage)
	 */
	public boolean canPerformAction(MessageVectorAction action,
			SNAXPerson currentUser, MessageVector messageVector,
			EntryMessage entryMessage, CommentMessage commentMessage,
			String recommenderId) 
	{
		final String targetKey = messageVector.getAssocResourceId();		
		
		switch (action)
		{
			case CREATE_VECTOR:
				return PolicyHelper.isFeatureEnabled(Feature.BOARD, targetKey);
			case CREATE_ENTRY:
			    return PolicyHelper.checkAcl(Acl.BOARD_WRITE_MSG, targetKey);
			case CREATE_COMMENT:
			    return PolicyHelper.checkAcl(Acl.BOARD_WRITE_COMMENT, targetKey);
			case DELETE_VECTOR:
				return false; // no-one may delete at this point
			case DELETE_ENTRY:
				return PolicyHelper.availableAction(Acl.BOARD_WRITE_MSG, targetKey, entryMessage.getPublishedBy());
			case DELETE_COMMENT:
				return PolicyHelper.availableAction(Acl.BOARD_WRITE_COMMENT, targetKey, commentMessage.getPublishedBy());
				
			case UPDATE_VECTOR:
			case UPDATE_ENTRY:
			case UPDATE_COMMENT:
				return false;  // no updates at this point
		
			case SET_NAMED_ENTRY:
			case CLEAR_NAMED_ENTRY:
			    return PolicyHelper.checkAcl(Acl.STATUS_UPDATE, targetKey);
			
			case RECOMMEND_ENTRY:
				return 
					MessageVectorConfig.isBoardExtensionEnabled() && 
					PolicyHelper.checkAcl(Acl.BOARD_RECOMMEND_COMMENT_MSG, targetKey);
				
			case REMOVE_RECOMMENDATION:
				return 
					MessageVectorConfig.isBoardExtensionEnabled() && 
					PolicyHelper.availableAction(Acl.BOARD_RECOMMEND_COMMENT_MSG, targetKey, recommenderId);
				
			default:
				throw new RuntimeException("Coding error, handled action code: " + action);
		}

	}	
}
