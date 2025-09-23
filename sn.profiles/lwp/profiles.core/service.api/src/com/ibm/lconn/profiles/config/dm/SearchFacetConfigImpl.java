/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.dm;

import com.ibm.lconn.profiles.config.AbstractConfigObject;

public class SearchFacetConfigImpl extends AbstractConfigObject implements
		SearchFacetConfig {

	private static final long serialVersionUID = 1L;

	private String taxonomy;
	private String association;
	private String description;
		
	public SearchFacetConfigImpl() {
		taxonomy = "";
		association = "";
		description = "";
	}
	
	public String getTaxonomy() {
		return taxonomy;
	}
	
	public void setTaxonomy(String taxonomy) {
		this.taxonomy = taxonomy;
	}
	
	public String getAssociation() {
		return association;
	}
	
	public void setAssociation(String association) {
		this.association = association;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}	
	
	public String toString() {
		StringBuilder result = new StringBuilder().append("[");
		result.append(" association=").append(association);
		result.append(" description=").append(description);
		result.append(" taxonomy=").append(taxonomy);
		result.append("]");
		return result.toString();
	}	
}