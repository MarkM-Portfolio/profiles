/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;

import java.sql.Timestamp;

import com.ibm.lconn.core.appext.util.SNAXDbInfo.DBType;
import com.ibm.lconn.core.appext.util.ibatis.PagingInfo;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;

public class IndexerSearchOptions extends AbstractDataObject<IndexerSearchOptions> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 107234739768691376L;
	
	private final Timestamp since;	
	private final Timestamp until;
	private final String sinceKey;
	private final int pageSize;
	private final boolean initialIndex;
	private boolean joinKey = false;
	private PagingInfo pagingInfo;

	public IndexerSearchOptions(Timestamp since, Timestamp until, String sinceKey, int pageSize, boolean initialIndex) {
		AssertionUtils.assertNotNull(since);
		AssertionUtils.assertNotNull(until);
		
		this.since = since;
		this.until = until;
		this.sinceKey = sinceKey;
		this.pageSize = pageSize >= 0 ? pageSize : 0;
		this.initialIndex = initialIndex;
	}
	
	public final Timestamp getSince() {
		return since;
	}
	public final String getSinceKey() {
		return sinceKey;
	}
	public final Timestamp getUntil() {
		return until;
	}
	public final int getPageSize() {
		return pageSize;
	}
	public final boolean isInitialIndex() {
		return initialIndex;
	}
	public final boolean isJoinKey() {
		return joinKey;
	}
	public final void setJoinKey(boolean joinKey) {
		this.joinKey = joinKey;
	}
	
	/**
	 * @return the pagingInfo
	 */
	public final PagingInfo getPagingInfo() {
		return pagingInfo;
	}

	/**
	 * @param initializes the paging info
	 */
	public final void initPagingInfo(DBType dbType) {
		this.pagingInfo = new PagingInfo(dbType, pageSize + 1);
	}

}
