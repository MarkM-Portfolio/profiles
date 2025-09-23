/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionServlet;

/**
 * Extending struts servlet to support doDelete and doPut
 * @author shalabi
 *
 */
public class ProfileStrutsServlet extends ActionServlet
{

    private static final long serialVersionUID = 1L;

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doDelete( HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        process(request, response);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPut( HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        process(request, response);
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	try {
    		super.doPost(request, response);
    	} catch (ServletException e) {
    		// [ahernm) Uber hack to build around WebSphere issue with redirect to Posts after login
    		if ("the request doesn't contain a multipart/form-data or multipart/mixed stream, content type header is null".equalsIgnoreCase(e.getMessage())) {
    			response.sendRedirect(request.getContextPath() + "/index.jsp");
    		}
    		else {
    			throw e;
    		}
    	}
    }

}
