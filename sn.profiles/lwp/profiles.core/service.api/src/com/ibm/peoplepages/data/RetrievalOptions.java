/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.data;

import com.ibm.lconn.profiles.internal.data.profile.UserState;

public class RetrievalOptions
{
  private int maxResultsPerPage = 10;
  private int skipResults = 0;
  private int orderBy = OrderByType.DISPLAY_NAME;
  private int sortOrder = SortOrder.DEFAULT;
  private Integer employeeState = UserState.ACTIVE.getCode();

  public static class OrderByType
  {
      final public static int DISPLAY_NAME = 0;
      final public static int SURNAME = 1;
      final public static int GIVEN_NAME = 2;
      final public static int MOST_RECENT  = 3;
      final public static int UNORDERED = 99;
  }
  
  public static class SortOrder
  {
	  final public static int DEFAULT = 0;
	  final public static int ASC = 1;
	  final public static int DESC = 2;
  }
  
  public int calculatePageNumber()
  {
    int pageNumber;
    if (skipResults == 0)
    {
      pageNumber = 1;
    }
    else
    {
      pageNumber = (skipResults / maxResultsPerPage) + 1;
    }
    return pageNumber;
  }
  
  public int getOrderBy()
  {
    return orderBy;
  }
  public void setOrderBy(int orderBy)
  {
    this.orderBy = orderBy;
  }
  
  public int getMaxResultsPerPage()
  {
    return maxResultsPerPage;
  }
  public void setMaxResultsPerPage(int maxResults)
  {
    this.maxResultsPerPage = maxResults;
  }
  public int getSkipResults()
  {
    return skipResults;
  }
  public void setSkipResults(int skipResults)
  {
    this.skipResults = skipResults;
  }

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
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
		if (null == userState)
			this.employeeState = null;
		else
			this.employeeState = userState.getCode();
	}

}
