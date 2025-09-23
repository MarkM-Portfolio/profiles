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
package com.ibm.peoplepages.data;

import java.util.Date;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class ConnectionRetrievalOptions extends RetrievalOptions
{
	// this class is confusing in that it has a single connection status. calls in the service append
	// the 'PENDING' state to retrieve both types based on the includePendingCount attribute. there
	// are no views of rejected connections, hence the issue probably never came up. seems cleaner to
	// just allow the caller to set the states they desire.
	private int status = Connection.StatusType.ACCEPTED;
	private String connectionType = PeoplePagesServiceConstants.COLLEAGUE;
	private boolean inclPendingCount = false;
	private boolean inclMessage = false;
	private boolean inclUserStatus = false;
	private Date since;
	
	/**
	 * Flag to indicate if should include source / target / lastmod / createdby Profiles.
	 */
	private boolean inclRelatedProfiles = false;
	
	/**
	 * Options to indicate retrieval options for 'target' Profile.
	 */
	private ProfileRetrievalOptions profileOptions = ProfileRetrievalOptions.LITE;

	public ConnectionRetrievalOptions()	{
	}

	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String getConnectionType() {
		return connectionType;
	}
	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	public boolean isInclPendingCount() {
		return inclPendingCount;
	}
	public void setInclPendingCount(boolean inclPendingCount) {
		this.inclPendingCount = inclPendingCount;
	}

	public boolean isInclMessage() {
		return inclMessage;
	}

	public void setInclMessage(boolean inclMessage) {
		this.inclMessage = inclMessage;
	}

	public ProfileRetrievalOptions getProfileOptions() {
		return profileOptions;
	}

	public void setProfileOptions(ProfileRetrievalOptions profileOptions) {
		this.profileOptions = profileOptions;
	}

	public boolean isInclRelatedProfiles() {
		return inclRelatedProfiles;
	}
	
	public void setInclRelatedProfiles(boolean inclRelatedProfiles) {
		this.inclRelatedProfiles = inclRelatedProfiles;
	}

	/**
	 * @return the inclUserStatus
	 */
	public final boolean isInclUserStatus() {
		return inclUserStatus;
	}

	/**
	 * @param inclUserStatus the inclUserStatus to set
	 */
	public final void setInclUserStatus(boolean inclUserStatus) {
		this.inclUserStatus = inclUserStatus;
	}

	/**
	 * @return the since
	 */
	public final Date getSince() {
		return since;
	}

	/**
	 * @param since the since to set
	 */
	public final void setSince(Date since) {
		this.since = since;
	}

}
