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
package com.ibm.lconn.profiles.data;

import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig;
import com.ibm.peoplepages.data.Employee;

public class IndexerProfileDescriptor extends ProfileDescriptor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2938616039575079038L;
	
	// note, this is only where the profile is the source of the connection
	private Map<ConnectionTypeConfig, List<Employee>> connections;	
	private boolean tombstone = false;
	
	public IndexerProfileDescriptor() {
		super();
	}
	
	public IndexerProfileDescriptor(List<GivenName> givenNames, List<Surname> surnames) {
		super(givenNames, surnames);
	}
	
	/**
	 * @return the connections
	 */
	public Map<ConnectionTypeConfig, List<Employee>> getConnections() {
		return connections;
	}

	/**
	 * @param connections the connections to set
	 */
	public void setConnections(Map<ConnectionTypeConfig, List<Employee>> connections) {
		this.connections = connections;
	}

	/**
	 * @return the tombstone
	 */
	public final boolean isTombstone() {
		return tombstone;
	}

	/**
	 * @param tombstone the tombstone to set
	 */
	public final void setTombstone(boolean tombstone) {
		this.tombstone = tombstone;
	}

}
