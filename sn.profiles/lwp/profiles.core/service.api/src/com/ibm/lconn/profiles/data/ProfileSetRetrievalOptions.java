/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/**
 * 
 */
package com.ibm.lconn.profiles.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

/**
 * @author mike
 *
 */
public class ProfileSetRetrievalOptions extends AbstractRetrievalOptions<ProfileSetRetrievalOptions>
{
	private static final long serialVersionUID = -5486796412364187634L;

	private final static int DEFAULT_PAGESIZE = ProfilesConfig.instance().getDataAccessConfig().getDefaultPageSize();
	private final static int MAX_PAGESIZE = ProfilesConfig.instance().getDataAccessConfig().getMaxReturnSize(); // never allow unbounded query
	
	private OrderBy orderBy = OrderBy.DISPLAY_NAME;
	private SortOrder sortOrder = SortOrder.ASC;
	private boolean includeCount = false;
	private String nextProfileKey;
	private int pageNumber = 1;
	
	private Integer employeeState = UserState.ACTIVE.getCode();
	private HashSet<Integer> employeeModes = new HashSet<Integer>();

	private ProfileRetrievalOptions profileOptions = ProfileRetrievalOptions.LITE;

	/*
	 * Enum for order by
	 */
	public static enum OrderBy
	{
		DISPLAY_NAME("displayName", "title"),
		SURNAME("surname", "last_name"),
		LASTNAME("last_name", "last_name"),
		LASTUPDATE("lastUpdate", "date"),
		RELEVANCE("relevance", "relevance"),
		UNORDERED("unordered", null);
		
		private final String name;
		private final String lcSearcherVal;
		private OrderBy(String name, String lcSearcherVal) {
			this.name = name;
			this.lcSearcherVal = lcSearcherVal;
		}
		
		/**
		 * @return the name
		 */
		public final String getName() {
			return name;
		}

		/**
		 * @return the lcSearcherVal
		 */
		public String getLCSearcherVal() {
			return lcSearcherVal;
		}
	}

	/*
	 * Enum for sort order
	 */
	public static enum SortOrder
	{
		ASC("asc"),
		DESC("desc");
		
		private final String name;
		private SortOrder(String name) {
			this.name = name;
		}
		
		/**
		 * @return the name
		 */
		public final String getName() {
			return name;
		}

		/**
		 * @return the lcSearcherVal
		 */
		public String getLCSearcherVal() {
			return name;
		}
	}

	/**
	 * Default constructor
	 */
	public ProfileSetRetrievalOptions() {
		super(DEFAULT_PAGESIZE);
	}
	
	/**
	 * 
	 */
	public ProfileSetRetrievalOptions(int pageSize) {
		super(pageSize);
	}

	/**
	 * @param page the page number to set
	 */
	public final void setPageNumber(int page) {
		// base class will check that size in positive
		this.pageNumber=Math.max(1,page);
		checkPagingParams();
	}

	/**
	 * @return the page number
	 */
	public final int getPageNumber() {
		// base class will check that size in positive
		return pageNumber;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public ProfileSetRetrievalOptions setPageSize(int pageSize) {
		// base class will check that size in positive
		int size = Math.min(MAX_PAGESIZE,pageSize);
		ProfileSetRetrievalOptions rtn = super.setPageSize(size);
		checkPagingParams();
		return rtn;
	}

	/**
	 * @return the orderBy
	 */
	public final OrderBy getOrderBy() {
		return orderBy;
	}

	/**
	 * @param orderBy the orderBy to set
	 */
	public void setOrderBy(OrderBy orderBy) {
		if (orderBy == null)
			orderBy = OrderBy.UNORDERED;
		this.orderBy = orderBy;
	}

	/**
	 * @return the sortOrder
	 */
	public final SortOrder getSortOrder() {
		return sortOrder;
	}

	/**
	 * @param sortOrder the sortOrder to set
	 */
	public final void setSortOrder(SortOrder sortOrder) {
		if (sortOrder == null)
			sortOrder = SortOrder.ASC;
		this.sortOrder = sortOrder;
	}

	/**
	 * @return the profileOptions
	 */
	public final ProfileRetrievalOptions getProfileOptions() {
		return profileOptions;
	}

	/**
	 * @param profileOptions the profileOptions to set
	 */
	public final void setProfileOptions(ProfileRetrievalOptions profileOptions) {
		this.profileOptions = profileOptions;
	}

	public boolean isIncludeCount() {
		return includeCount;
	}

	public void setIncludeCount(boolean includeCount) {
		this.includeCount = includeCount;
	}

	public String getNextProfileKey() {
		return nextProfileKey;
	}

	public void setNextProfileKey(String nextProfileKey) {
		this.nextProfileKey = nextProfileKey;
	}

	public void checkPagingParams(){
		// seems far-fetched that we would hit these limits in normal operations.
		// enforce that pageSize*pageNumber < Integer.MAX_VALUE;
		if (pageNumber >= Integer.MAX_VALUE / getPageSize()){
			throw new IllegalArgumentException("ProfileSetRetrivalOptions pageSize: "+getPageSize()+" pageNumber: "+pageNumber);
		}		
	}

	public Integer getEmployeeState() {
		return employeeState;
	}

	/**
	 * 
	 * @param userState
	 *            set to "null" to retrieve both active and inactive profile (this is the default behavior). Otherwise, set to
	 *            UserState.ACTIVE to retrieve only active profiles or UserState.INACTIVE to retrieve inactive profiles.
	 */
	public void setEmployeeState(UserState userState) {
		if (null == userState){
			this.employeeState = null;
		}
		else{
			this.employeeState = userState.getCode();
		}
	}
	
	public void addMode(UserMode mode){
		if (mode == null){
			employeeModes.clear();
		}
		else{
			employeeModes.add(mode.getCode());
		}
	}
	
	public List<Integer> getModes() {
		return new ArrayList<Integer>(employeeModes);
	}
	
	public void clearModes(){
		employeeModes.clear();
	}
}
