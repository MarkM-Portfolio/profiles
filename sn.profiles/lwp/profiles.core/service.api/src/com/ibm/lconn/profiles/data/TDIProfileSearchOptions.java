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

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.core.appext.util.SNAXDbInfo.DBType;
import com.ibm.lconn.core.appext.util.ibatis.PagingInfo;

/**
 *
 *
 */
public class TDIProfileSearchOptions extends AbstractDataObject<TDIProfileSearchOptions> {		 
	/**
	 * 
	 */
	private static final long serialVersionUID = -3008816544190104102L;
	
	public static final int DEFAULT_PAGE_SIZE = 250;
	
	private List<TDIProfileSearchCriteria> searchCriteria = null;
	private String lastKey = null;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private boolean profileOnly = false;
	private PagingInfo pagingInfo = null;

	public TDIProfileSearchOptions(){}

	public TDIProfileSearchOptions(TDIProfileSearchOptions options){
		if (options != null){
			this.searchCriteria = options.searchCriteria;
			this.lastKey = options.lastKey;
			this.pageSize = options.pageSize;
			this.profileOnly = options.profileOnly;
			this.pagingInfo = options.pagingInfo;
		}
	}

	/**
	 * @return the lastKey
	 */
	public final String getLastKey() {
		return lastKey;
	}
	/**
	 * @param lastKey the lastKey to set
	 */
	public final void setLastKey(String lastKey) {
		this.lastKey = lastKey;
	}
	/**
	 * @return the searchCriteria
	 */
	public final List<TDIProfileSearchCriteria> getSearchCriteria() {
		return searchCriteria;
	}
	/**
	 * @param searchCriteria the searchCriteria to set
	 */
	public final void setSearchCriteria(
			List<TDIProfileSearchCriteria> searchCriteria) 
	{
		this.searchCriteria = searchCriteria;
	}
	/**
	 * @return the pageSize
	 */
	public final int getPageSize() {
		return pageSize;
	}
	/**
	 * @param pageSize the pageSize to set
	 */
	public final void setPageSize(int pageSize) {
		if (pageSize < 1)
			pageSize = DEFAULT_PAGE_SIZE;
		this.pageSize = pageSize;
	}
	
	/**
	 * Utility method to indicate there are search criteria
	 * @return
	 */
	public final boolean isHasSearchCriteria() {
		return searchCriteria != null && searchCriteria.size() > 0;
	}
	
	/**
	 * Utility method to indicate if there are any criteria for the select (including 'lastKey')
	 * @return
	 */
	public final boolean isHasCriteria() {
		return isHasSearchCriteria() || StringUtils.isNotEmpty(lastKey);
	}
	/**
	 * @return the profileOnly
	 */
	public final boolean isProfileOnly() {
		return profileOnly;
	}
	/**
	 * @param profileOnly the profileOnly to set
	 */
	public final void setProfileOnly(boolean profileOnly) {
		this.profileOnly = profileOnly;
	}
	/**
	 * @return the pagingInfo
	 */
	public final PagingInfo getPagingInfo() {
		return pagingInfo;
	}
	/**
	 * @param pagingInfo the pagingInfo to set
	 */
	public final void initPagingInfo(DBType dbType) {
		this.pagingInfo = new PagingInfo(dbType, pageSize + 1);
	}
}
