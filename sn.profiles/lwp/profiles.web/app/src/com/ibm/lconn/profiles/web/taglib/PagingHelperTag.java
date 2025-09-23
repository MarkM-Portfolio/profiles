/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2010                                      */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.taglib;

import java.io.IOException;
import java.util.HashSet;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * @author user
 *
 */
public class PagingHelperTag extends SimpleTagSupport {
	
	private int numPagesAroundCurrent = 0;
	private int currentPage = 1;
	private int numPages = 0;
	
	/**
	 * for (1..numPages)
	 */
	public void doTag() 
		throws JspException, IOException 
	{
		final JspContext cntx = getJspContext();
		final JspFragment jspBody = getJspBody();
		
		HashSet<Integer> visited = new HashSet<Integer>();
		
		// output start
		for (int page = 1; page <= numPagesAroundCurrent + 1; page++) {
			doPage(cntx, jspBody, visited, page);
		}
		
		// output middle
		for (int page = (currentPage - numPagesAroundCurrent - 1); page <= (currentPage + numPagesAroundCurrent + 1); page++) {
			doPage(cntx, jspBody, visited, page);
		}
		
		// output end
		for (int page = (numPages - numPagesAroundCurrent - 1); page <= numPages; page++) {
			doPage(cntx, jspBody, visited, page);
		}
	}
	
	/**
	 * Handle page output logic
	 * 
	 * @param cntx
	 * @param jspBody
	 * @param visited
	 * @param page
	 * @param numPages
	 * 
	 * @throws IOException 
	 * @throws JspException 
	 */
	private final void doPage(JspContext cntx, JspFragment jspBody, HashSet<Integer> visited, int page) 
		throws JspException, IOException 
	{		
		if (page >= 1 && page <= numPages && visited.add(page)) {
			cntx.setAttribute("page", page);
			jspBody.invoke(cntx.getOut());
		}
	}

	/**
	 * @param numPages the numPages to set
	 */
	public final void setNumPages(int numPages) {
		this.numPages = numPages;
	}
	/**
	 * @param numPagesAroundCurrent the numPagesAroundCurrent to set
	 */
	public final void setNumPagesAroundCurrent(int numPagesAroundCurrent) {
		this.numPagesAroundCurrent = numPagesAroundCurrent;
	}

	/**
	 * @param currentPage the currentPage to set
	 */
	public final void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

}
