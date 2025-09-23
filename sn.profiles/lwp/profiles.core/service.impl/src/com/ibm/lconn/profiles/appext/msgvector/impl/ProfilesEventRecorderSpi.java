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
package com.ibm.lconn.profiles.appext.msgvector.impl;

import com.ibm.lconn.core.appext.data.SNAXPerson;
import com.ibm.lconn.core.appext.msgvector.data.CommentMessage;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessage;
import com.ibm.lconn.core.appext.msgvector.data.MessageVector;
import com.ibm.lconn.core.appext.msgvector.spi.EventRecorderSpi;
import com.ibm.lconn.core.appext.msgvector.spi.MessageVectorAction;

/**
 * The board was removed from Profiles in 4.0. Accordingly, Profiles does not publish board related events. This class is implemented as a
 * null class (it does nothing) until we can figure out how to untangle the infra lc.appext.msgvector mess.
 */
public class ProfilesEventRecorderSpi implements EventRecorderSpi {

	public void recActionPerformed(MessageVectorAction arg0, SNAXPerson arg1, MessageVector arg2, EntryMessage arg3, String arg4) {
		// do nothing
	}

	public ProfilesEventRecorderSpi() {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.ibm.lconn.core.appext.msgvector.spi.EventRecorderSpi#actionPerformed(com.ibm.lconn.core.appext.msgvector.spi.MessageVectorAction,
	 * com.ibm.lconn.core.appext.data.SNAXPerson, com.ibm.lconn.core.appext.msgvector.data.MessageVector,
	 * com.ibm.lconn.core.appext.msgvector.data.EntryMessage, com.ibm.lconn.core.appext.msgvector.data.CommentMessage)
	 */
	public void actionPerformed(MessageVectorAction action, SNAXPerson currentUser, MessageVector messageVector, EntryMessage entryMessage,
			CommentMessage commentMessage) {
		// do nothing
	}
}
