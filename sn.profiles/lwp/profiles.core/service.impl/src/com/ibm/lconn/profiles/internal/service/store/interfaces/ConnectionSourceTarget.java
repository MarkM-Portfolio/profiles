/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import com.ibm.lconn.profiles.data.AbstractDataObject;

public class ConnectionSourceTarget extends AbstractDataObject<ConnectionSourceTarget> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5615894541985630740L;
	
	private String sourceKey;
	private String targetKey;
	
	public ConnectionSourceTarget() {}
	
	/**
	 * @return the sourceKey
	 */
	public String getSourceKey() {
		return sourceKey;
	}
	/**
	 * @param sourceKey the sourceKey to set
	 */
	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}
	/**
	 * @return the targetKey
	 */
	public String getTargetKey() {
		return targetKey;
	}
	/**
	 * @param targetKey the targetKey to set
	 */
	public void setTargetKey(String targetKey) {
		this.targetKey = targetKey;
	}
}
