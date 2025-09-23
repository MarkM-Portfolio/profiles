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
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author sberajaw
 */
public final class LoginInfoAction extends BaseAction {
	
	  private static final Log LOG = LogFactory.getLog(LoginInfoAction.class);
			
	  private static final String LOGIN_INFO_USER = "login.info.action.user";
	  private static final String LOGIN_INFO_NAME = "login.info.action.displayName";
	  private static final String LOGIN_INFO_DN = "login.info.action.distinguishedName";
	  private static final String LOGIN_INFO_ACCESS_TM = "login.info.action.accessTm";
	  
	  /*
	   * (non-Javadoc)
	   * 
	   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
	   *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
	   *      javax.servlet.http.HttpServletResponse)
	   */
	  public final ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception 
	  {
		  String username = request.getRemoteUser();
		  String displayName = null;
		  String distinguishedName = null;
				  
		  HttpSession session = request.getSession(username != null);
		  
		  String cachedUser = null;
		  String cachedDisplayName = null;
		  String cachedDistinguishedName = null;
		  Long cachedUserAccessTm = null;
		  
		  if (session != null)
		  {
			  cachedUser = (String) session.getAttribute(LOGIN_INFO_USER);
			  cachedDisplayName = (String) session.getAttribute(LOGIN_INFO_NAME);
			  cachedDistinguishedName = (String) session.getAttribute(LOGIN_INFO_DN);
			  cachedUserAccessTm = (Long) session.getAttribute(LOGIN_INFO_ACCESS_TM);
		  }
		  
		  boolean matchUser = (cachedUser == null && username == null) || (username != null && username.equals(cachedUser));
		  
		  long lastMod = request.getDateHeader("If-Modified-Since");

		  if (LOG.isDebugEnabled()) 
		  {
			  LOG.debug("LoginInfoCall => " + 
					  		"{cachedUser: " + cachedUser + 
					  		", username: " + username + 
					  		", cachedDisplayName: " + cachedDisplayName + 
					  		", cachedDistinuishedName: " + cachedDistinguishedName + 
					  		", cachedUserAccessTm: " + cachedUserAccessTm + "}");
		  }
		  
		  // have matching cache
		  if (matchUser)
		  {
			  // Conditional get matches, paranoid in case 2 different users use same browser; revisit page
			  // Only case that will matched users have same name or browser session present in non-authenticated user
			  if (cachedUserAccessTm != null && lastMod == cachedUserAccessTm.longValue())
			  {
				  response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				  return null;
			  }
			  
			  displayName = cachedDisplayName;
			  distinguishedName = cachedDistinguishedName;
		  }
		  
		  else if (username != null) 
		  {
			  Employee employee = getCachedUserRecord(request);
				  
			  if (employee != null)
			  {
				  displayName = employee.getDisplayName();
				  distinguishedName = employee.getDistinguishedName();
			  }
		  }
		  
		  long lastModifiedNow = new Date().getTime();
		  lastModifiedNow /= 1000;
		  lastModifiedNow *= 1000;
		  
		  // No session, no caching
		  if (session != null)
		  {
			  setSessionAttribute(session,LOGIN_INFO_USER,username,username != null);
			  setSessionAttribute(session,LOGIN_INFO_NAME,displayName,displayName != null);
			  setSessionAttribute(session,LOGIN_INFO_DN,distinguishedName,distinguishedName != null);
			  setSessionAttribute(session,LOGIN_INFO_ACCESS_TM,new Long(lastModifiedNow),true); // This will save a LM time to allow for caching of empty display name
		  }
		  
		  //
		  // Set display name for markup
		  //
		  if (displayName != null)
		  {
			  request.setAttribute("displayName", displayName);
			  request.setAttribute("distinguishedName", distinguishedName);
		  }
		  
		  //
		  // Set appropriate headers
		  //
		  response.setDateHeader("Last-Modified",lastModifiedNow);
		  response.setHeader("Cache-Control", "private, max-age=0, must-revalidate");
		  
		  return mapping.findForward("loginInfo");
	  }

	  /*
	   * Conditionally set or remove session attributes based on 
	   */
	  private final void setSessionAttribute(HttpSession session, String attr, Object value, boolean cond) 
	  {
		  if (cond)
		  {
			  session.setAttribute(attr,value);
		  }
		  else
		  {
			  session.removeAttribute(attr);
		  }
	  }
	  
	  public static final Employee getCachedUserRecord(HttpServletRequest request) throws DataAccessRetrieveException
	  {
		  return AppContextAccess.getCurrentUserProfile();
	  }

		/* (non-Javadoc)
		 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
		 */
		@Override
		protected long getLastModified(HttpServletRequest request) throws Exception {
			return UNDEF_LASTMOD;
		}
}
