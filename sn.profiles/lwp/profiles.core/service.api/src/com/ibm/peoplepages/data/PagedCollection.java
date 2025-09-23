/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.data;


public class PagedCollection
{
  private int totalResults = 0;
  private int start = 0;
  private int end = 0;
  private int page = 0;
  private int numPages = 0;
  private int pageSize = 0;
  
  public PagedCollection()
  {
    
  }

  public PagedCollection(int total, int pageNum, int pgSize, int actualPageSize)
  {
    totalResults = total;
    page = pageNum;
    pageSize = pgSize;

    if (pageSize != 0)
    {
      numPages = totalResults / pageSize;
      if (totalResults % pageSize != 0)
      {
        numPages++;
      }
    }
    else
    {
      numPages = 0;
    }
    start = (page - 1) * pageSize + 1;
    if (actualPageSize < pageSize)
    {
      end = start + actualPageSize - 1;
    }
    else
    {
      end = start + (pageSize - 1);
    }
  }

  /**
   * @return Returns the end.
   */
  public int getEnd()
  {
    return end;
  }

  /**
   * @return Returns the numPages.
   */
  public int getNumPages()
  {
    return numPages;
  }

  /**
   * @return Returns the page.
   */
  public int getPage()
  {
    return page;
  }

  /**
   * @return Returns the pageSize.
   */
  public int getPageSize()
  {
    return pageSize;
  }

  /**
   * @return Returns the start.
   */
  public int getStart()
  {
    return start;
  }

  /**
   * @return Returns the totalResults.
   */
  public int getTotalResults()
  {
    return totalResults;
  }
  
  public boolean isLastPage()
  {
    if (numPages == page)
    {
      return true;
    }
    else
    {
      return false;
    }
  }
}
