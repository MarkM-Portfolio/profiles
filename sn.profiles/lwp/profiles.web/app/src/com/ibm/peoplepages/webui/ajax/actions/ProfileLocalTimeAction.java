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

package com.ibm.peoplepages.webui.ajax.actions;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.web.rpfilter.RPFilterCacheControl;
import com.ibm.peoplepages.web.rpfilter.RPFilterResponse;

/**
 * @author ahernm
 */
public class ProfileLocalTimeAction extends BaseAction {
  private static final Log LOG = LogFactory.getLog(ProfileLocalTimeAction.class);
  
  private static final int CACHE_TIMEOUT = 3600;
  
  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
	public ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
		HttpServletResponse response) throws Exception 
	{
		Date clientTime = new Date();
		boolean cachePage = true;
		
		try {
			long timeNow = Long.parseLong(request.getParameter("timeNow"));
			clientTime.setTime(timeNow);			
		} catch (NumberFormatException e) {
			cachePage = false;
		}
				
		request.setAttribute("clientTime", clientTime);
		
        if (cachePage && response instanceof RPFilterResponse)
        {
        	RPFilterResponse rpresp = (RPFilterResponse) response;
        	RPFilterCacheControl cacheControl = rpresp.getCacheControl();
        	
        	cacheControl.setPublic(true);
        	cacheControl.setMaxAge(CACHE_TIMEOUT);
        	cacheControl.setProxyMaxAge(CACHE_TIMEOUT);
        	
        	rpresp.setLastModified(new Date().getTime());
        	rpresp.applyCacheControl();
        }
	    
        return mapping.findForward("profileLocalTime");
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
}
