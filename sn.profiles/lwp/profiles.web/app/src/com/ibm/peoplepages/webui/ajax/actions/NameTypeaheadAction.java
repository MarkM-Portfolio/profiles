/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.ajax.actions;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.SearchService2;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author sberajaw
 */
public class NameTypeaheadAction extends BaseAction
{
	private final static Class<NameTypeaheadAction> CLAZZ = NameTypeaheadAction.class;
	private final static String CLASS_NAME = CLAZZ.getSimpleName();
	private final static Log    LOG        = LogFactory.getLog(CLAZZ);
	
	private SearchService2 searchSvc = AppServiceContextAccess.getContextObject(SearchService2.class);
	private int nDefGetEntries = 10;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
	public ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
			HttpServletResponse response) throws Exception {

		String methodName = "doExecute";

		String name = StringUtils.lowerCase( request.getParameter("name"));
		String sEntryCount = StringUtils.lowerCase( request.getParameter("count"));
		int     entryCount = ((sEntryCount != null) ? Integer.valueOf(sEntryCount) : nDefGetEntries);

		ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(entryCount);

		if (LOG.isDebugEnabled())
			LOG.debug(CLASS_NAME + "." + methodName + "(" + name + ", " + entryCount + ")"); 

		List<Employee> profiles = null;

		//check to make sure the current user has permission to view the typeahead results
		Employee currentUser = AppContextAccess.getCurrentUserProfile();
		if (LOG.isTraceEnabled())
			LOG.trace(CLASS_NAME + "." + methodName + " : checkAcl(" + currentUser.getEmail() + " / " + currentUser.getUserid() + " / " + currentUser.getTenantKey() + ")"); 
		if (PolicyHelper.checkAcl(Acl.TYPEAHEAD_VIEW, currentUser)) {
			profiles = searchSvc.findProfilesByName(name, options);
			int numProfiles = 0;
			if (null != profiles)
				numProfiles = profiles.size();
			if (LOG.isTraceEnabled())
				LOG.trace(CLASS_NAME + "." + methodName + " : searchSvc.findProfilesByName(" + name + ", " + entryCount + ") returned " + numProfiles  + " profiles"); 
		}
		else {
			if (LOG.isTraceEnabled())
				LOG.trace(CLASS_NAME + "." + methodName + " : checkAcl(" + currentUser.getEmail() + " / " + currentUser.getUserid() + ") got FALSE.  Return zero profiles"); 
			profiles = (List<Employee>) Collections.<Employee> emptyList();
		}

		// this API is not 'public' in our API documentation, but it needs to enforce 'hidden' like all other HTTP accessible endpoints
		for (Employee profile : profiles)
		{
			APIHelper.filterProfileAttrForAPI(profile);
		}

		request.setAttribute("profiles", profiles);
		request.setAttribute("name", name);
		request.setAttribute("bName", ("<b>"+name+"</b>"));
		request.setAttribute("extended", request.getParameter("extended"));

		return mapping.findForward("nameTypeahead");
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
}
