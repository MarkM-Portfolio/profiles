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
package com.ibm.peoplepages.data;

import java.util.List;

public class ConnectionCollection extends SearchResultsPage<Connection> 
{
	public static final int UNDEF_PENDING = -1;
	
	private String ownerKey;
	private int pendingInvitations = UNDEF_PENDING;

	public ConnectionCollection() {
		super();
	}
	
	public ConnectionCollection(
			List<Connection> rslts, 
			int total, 
			int pageNum,
			int pgSize) 
	{
		super(rslts, total, pageNum, pgSize);
	}

	public int getPendingInvitations() {
		return pendingInvitations;
	}

	public void setPendingInvitations(int pendingInvitations) {
		this.pendingInvitations = pendingInvitations;
	}

}
