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
package com.ibm.lconn.profiles.data;

import java.util.List;

/**
 *
 *
 */
public class TDIProfileCollection extends AbstractDataObject<TDIProfileCollection> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1440858390927988284L;
	
	private List<ProfileDescriptor> profiles;
	private TDIProfileSearchOptions nextPage;
	
	/**
	 * Used to get options to retrieve the next page of entries. If this page is
	 * null, then there are no more results to be retrieved.
	 * 
	 * @return the nextPage
	 */
	public final TDIProfileSearchOptions getNextPage() {
		return nextPage;
	}
	/**
	 * @param nextPage the nextPage to set
	 */
	public final void setNextPage(TDIProfileSearchOptions nextPage) {
		this.nextPage = nextPage;
	}
	/**
	 * @return the profiles
	 */
	public final List<ProfileDescriptor> getProfiles() {
		return profiles;
	}
	/**
	 * @param profiles the profiles to set
	 */
	public final void setProfiles(List<ProfileDescriptor> profiles) {
		this.profiles = profiles;
	}
	
}
