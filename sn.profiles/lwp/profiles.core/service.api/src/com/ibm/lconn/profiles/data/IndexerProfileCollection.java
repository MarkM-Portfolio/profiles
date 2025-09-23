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

public class IndexerProfileCollection extends AbstractDataObject<IndexerProfileCollection> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -159207991739659407L;

	private List<IndexerProfileDescriptor> profiles;
	private IndexerSearchOptions next;
	
	public IndexerSearchOptions getNext() {
		return next;
	}
	public void setNext(IndexerSearchOptions next) {
		this.next = next;
	}
	public List<IndexerProfileDescriptor> getProfiles() {
		return profiles;
	}
	public void setProfiles(List<IndexerProfileDescriptor> profiles) {
		this.profiles = profiles;
	}
	
}
