/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public abstract class ProfileTagCloudAction extends ProfileTagsAction implements AtomConstants
{	
	//
	// Java bean for passing info around Action
	//
	protected static class Bean extends AtomGenerator.ReqBean
	{
		public Bean() { 
			this.inclContribCount = false;
		}
	}

	/**
	 * Main method
	 */
	protected final ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		AtomGenerator generator = new AtomGenerator(response, APP_CONTENT_TYPE);
		Bean bean = getAndStoreActionBean(request, Bean.class);
		setCachingHeader(request, response, bean);
		generator.writeTagCloud(bean);

		return null;
	}

	/**
	 * Overriable value
	 */
	protected long getLastModified(HttpServletRequest request) throws Exception 
	{	
		return System.currentTimeMillis();
	}

	/**
	 * Method to be implememented by sub-classers
	 */
	protected abstract Bean instantiateActionBean(HttpServletRequest request)
		throws Exception;
	
	/**
	 * Overridable method for setting caching headers
	 * @param request
	 * @param response
	 * @param bean
	 */
	protected void setCachingHeader(
			HttpServletRequest request, 
			HttpServletResponse response, Bean bean) 
	{
		
	}
}
