/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.data;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsPage<ResultType> extends PagedCollection
{
  private List<ResultType> results;
  //ba adding a field for results ordered by config.. will phase out results
  private List<ResultType> searchResults;

  
  public SearchResultsPage()
  {
    super();
    results = new ArrayList<ResultType>();
    searchResults = new ArrayList<ResultType>();
  }

  public SearchResultsPage(List<ResultType> rslts, int total, int pageNum, int pgSize)
  {
    super(total, pageNum, pgSize, rslts.size());
    results = rslts;
  }

  /**
   * @return Returns the results.
   */
  public List<ResultType> getResults()
  {
    return results;
  }
  
  public void setSearchResults(List<ResultType> rslts){
	  searchResults = rslts;
  }
  
  public List<ResultType> getSearchResults(){
	  return searchResults;
  }
}
