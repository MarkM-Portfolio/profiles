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

import java.util.List;

import com.ibm.lconn.profiles.config.BaseConfigObject;

public interface SearchAttributeConfig extends BaseConfigObject {

	public String getFieldId();
	
	public boolean isFieldSearchable();
	
	public boolean isContentSearchable();
	
	public boolean isReturnable();
	
	public boolean isExactMatchSupported();
			
	public boolean isSortable();
	
	public boolean isParametric();
	
	public List<SearchFacetConfig> getSearchFacetConfigs();
}
