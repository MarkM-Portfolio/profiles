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

import java.util.ArrayList;
import java.util.List;

import com.ibm.lconn.profiles.config.AbstractConfigObject;

public class SearchAttributeConfigImpl extends AbstractConfigObject implements
		SearchAttributeConfig {

	private static final long serialVersionUID = 1L;

	private String fieldId;	
	private boolean fieldSearchable;
	private boolean contentSearchable;
	private boolean returnable;
	private boolean exactMatchSupported;
	private boolean sortable;
	private boolean parametric;
	private List<SearchFacetConfig> searchFacetConfigs;
	
	public SearchAttributeConfigImpl() {
		this.searchFacetConfigs = new ArrayList<SearchFacetConfig>();
	}
	
	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public boolean isFieldSearchable() {
		return fieldSearchable;
	}

	public void setFieldSearchable(boolean fieldSearchable) {
		this.fieldSearchable = fieldSearchable;
	}

	public boolean isContentSearchable() {
		return contentSearchable;
	}

	public void setContentSearchable(boolean contentSearchable) {
		this.contentSearchable = contentSearchable;
	}

	public boolean isReturnable() {
		return returnable;
	}

	public void setReturnable(boolean returnable) {
		this.returnable = returnable;
	}

	public boolean isExactMatchSupported() {
		return exactMatchSupported;
	}

	public void setExactMatchSupported(boolean exactMatchSupported) {
		this.exactMatchSupported = exactMatchSupported;
	}

	public boolean isSortable() {
		return sortable;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	public boolean isParametric() {
		return parametric;
	}

	public void setParametric(boolean parametric) {
		this.parametric = parametric;
	}

	public List<SearchFacetConfig> getSearchFacetConfigs() {
		return searchFacetConfigs;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder().append("[");
		result.append(" fieldId=").append(fieldId);
		result.append(" fieldSearchable=").append(fieldSearchable);
		result.append(" contentSearchable=").append(contentSearchable);
		result.append(" returnable=").append(returnable);
		result.append(" exactMatchSupported=").append(exactMatchSupported);
		result.append(" sortable=").append(sortable);
		result.append(" parametric=").append(parametric);
		result.append(" searchFacetConfigs=").append(searchFacetConfigs);
		result.append("]");
		return result.toString();
	}

}