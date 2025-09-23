/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2008, 2012                                */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

// why a hashmap? seems error prone to mix in map and class attributes (e.g., this can
// confuse ibatis). Plus there are no consts for the keys. a connection is not a hashmap,
// so why have it expose that interface?
public class Connection extends java.util.HashMap<String,Object> implements DatabaseRecord
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3383747183031728935L;

	private static Log LOG = LogFactory.getLog(Connection.class);
	
	private Employee createdByProfile = null;
	private Employee lastModByProfile = null;
	private Employee sourceProfile = null;
	private Employee targetProfile = null;

	public static class StatusType
	{
		/**
		 * Created by other user; needing confirmation
		 */
		final public static int PENDING = 2;
		
		/**
		 * A confirmed connection.
		 */
		final public static int ACCEPTED  = 1;
		
		/**
		 * A connection a user created that needs confirmation.
		 */
		final public static int UNCONFIRMED = 0;
	}

	public Connection()
	{
		super();
		this.setType(PeoplePagesServiceConstants.COLLEAGUE);
		this.setStatus(StatusType.PENDING);
	}

	public String getConnectionId()
	{
		return (String)get("connectionId");
	}

	public void setConnectionId(String connectionId)
	{
		put("connectionId", connectionId);
	}

	public String getTenantKey() {
		return (String)get("tenantKey");
	}

	public void setTenantKey(String tenantKey) {
		// should match tenantKeys for any source or target profile.
		put("tenantKey", tenantKey);
	}
	public String getSourceKey()
	{
		return (String)get("sourceKey");
	}

	public void setSourceKey(String sourceKey)
	{
		put("sourceKey", sourceKey);
	}

	public String getTargetKey()
	{
		return (String)get("targetKey");
	}

	public void setTargetKey(String targetKey)
	{
		put("targetKey", targetKey);
	}

	public String getType()
	{
		return (String)get("type");
	}

	public void setType(String type)
	{
		put("type", type);
	}

	public int getStatus()
	{
		return ((Integer)get("status")).intValue();
	}

	public void setStatus(int status)
	{
		put("status", new Integer(status));
	}

	public String getCreatedByKey()
	{
		return (String)get("createdByKey");
	}

	public void setCreatedByKey(String createdByKey)
	{
		put("createdByKey", createdByKey);
	}

	public Date getCreated()
	{
		return (Date)get("created");
	}

	public void setCreated(Date created)
	{
		put("created", created);
	}

	public String getLastModByKey()
	{
		return (String)get("lastModByKey");
	}

	public void setLastModByKey(String lastModByKey)
	{
		put("lastModByKey", lastModByKey);
	}

	public Date getLastMod()
	{
		return (Date)get("lastMod");
	}

	public void setLastMod(Date lastMod)
	{
		put("lastMod", lastMod);
	}

	public String getMessage()
	{
		return (String)get("message");
	}

	public void setMessage(String message)
	{
		put("message", message);
	}

	public String getRecordTitle()
	{
		if (targetProfile == null)
		{
			return "connection:" + getRecordId();
		}
		else
		{
			return targetProfile.getRecordTitle();
		}
	}

	public String getRecordType()
	{
		return "connection";
	}

	public String getRecordSearchString()
	{
		String rv = PeoplePagesServiceConstants.CONNECTION_ID + "=" + getConnectionId();
		try
		{
			rv = PeoplePagesServiceConstants.CONNECTION_ID + "=" + URLEncoder.encode(getConnectionId(), "UTF-8");
		}
		catch (UnsupportedEncodingException uee)
		{
			LOG.error(uee.getMessage(), uee);
		}
		return rv;
	}

	public String getRecordSummary()
	{
		return "";
	}

	public Date getRecordUpdated()
	{
		return getLastMod();
	}

	public String getRecordId()
	{
		return getConnectionId();
	}

	public Employee getCreatedByProfile() {
		return createdByProfile;
	}

	public void setCreatedByProfile(Employee createdByProfile) {
		this.createdByProfile = createdByProfile;
	}

	public Employee getLastModByProfile() {
		return lastModByProfile;
	}

	public void setLastModByProfile(Employee lastModByProfile) {
		this.lastModByProfile = lastModByProfile;
	}

	public Employee getSourceProfile() {
		return sourceProfile;
	}

	public void setSourceProfile(Employee sourceProfile) {
		this.sourceProfile = sourceProfile;
	}

	public Employee getTargetProfile() {
		return targetProfile;
	}

	public void setTargetProfile(Employee targetProfile) {
		this.targetProfile = targetProfile;
	}
}
