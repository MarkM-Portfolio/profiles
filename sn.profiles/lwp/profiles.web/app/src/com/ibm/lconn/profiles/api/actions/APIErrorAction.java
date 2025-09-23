/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.parser.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.ibm.lconn.core.appext.msgvector.api.AccessControlException;
import com.ibm.lconn.core.appext.msgvector.api.DataValidationException;
import com.ibm.lconn.core.web.atom.exception.BadRequestException;
import com.ibm.lconn.core.web.atom.exception.RequiredValueMissingException;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.exception.ConnectionExistsException;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.sn.av.api.AVScannerException;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class APIErrorAction extends Action 
{
	private static final Log LOG = LogFactory.getLog(APIErrorAction.class);
	
	/*
     * (non-Javadoc)
     * 
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(
    				ActionMapping mapping, 
    				ActionForm form, 
    				HttpServletRequest request, 
    				HttpServletResponse response) 
    	throws Exception 
    {
    	Exception exception = (Exception) request.getAttribute("org.apache.struts.action.EXCEPTION");
    	final boolean showStackTrace = !PropertiesConfig.getBoolean(ConfigProperty.API_HIDE_STACK_TRACES);
    	boolean errorPrinted = false;
    	request.setAttribute("showStackTrace", showStackTrace);

    	if (exception != null)
    	{
    		clearErrors(request); // clear in order to write more meaningful message
    		
	    	Throwable cause = getRootCause(exception);
	    	if (cause != null && cause instanceof AVScannerException)
	    	{
	    		writeError(request,"error.fileContainsVirus");
				setStatus(request,response,HttpServletResponse.SC_BAD_REQUEST);
	    	}
	    	else if (exception instanceof APIException)
	    	{
	    		APIException apiException = (APIException) exception;
	    		switch (apiException.getECause())
	    		{
	    			case FORBIDDEN:
	    				return handleAclError(mapping, form, request, response);
	    			case INVALID_XML_CONTENT:
	    				writeError(request,"error.atomInvalidXMLContent");
	    				setStatus(request,response,HttpServletResponse.SC_BAD_REQUEST);
	    				break;
	    			case INVALID_OPERATION:
	    				writeError(request,"error.atomNotFound");
	    				setStatus(request,response,HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	    				break;
	    			case INVALID_CONTENT_TYPE:
	    				writeError(request,"error.atomInvalidContentType");
	    				setStatus(request,response,HttpServletResponse.SC_BAD_REQUEST);
	    				break;
	    			case INVALID_REQUEST:
	    			default:
	    				writeError(request,"error.atomInvalidRequest");
	    				setStatus(request,response,HttpServletResponse.SC_BAD_REQUEST);
	    				break;
	    		}
	    	}
	    	else if (exception instanceof AssertionException)
	    	{
	    		AssertionException ex = (AssertionException) exception;

	    		switch (ex.getType())
	    		{
	    			case UNAUTHORIZED_ACTION:
	    				return handleAclError(mapping, form, request, response);
	    			case RESOURCE_NOT_FOUND:
				        writeError(request,"error.atomInvalidRequest");
	    				setStatus(request,response,HttpServletResponse.SC_NOT_FOUND);
	    				break;
	    			case RESOURCE_NO_CONTENT:
	    				setStatus(request,response,HttpServletResponse.SC_NO_CONTENT);
	    				break;
	    			case DUPLICATE_KEY:
    					writeError(request,"error.atomInvalidRequest",ex);
	    				setStatus(request,response,HttpServletResponse.SC_CONFLICT);
	    				break;
	    			default: // only have preconditions
	    				writeError(request,"error.atomInvalidRequest");
    					setStatus(request,response,HttpServletResponse.SC_BAD_REQUEST);
    					break;
	    		}
	    	}	    	
	    	else if (exception instanceof ConnectionExistsException)
	    	{
	    		writeError(request,"error.atomConnectionAlreadExists");
				setStatus(request,response,HttpServletResponse.SC_BAD_REQUEST);
	    	}
	    	else if (exception instanceof ParseException)
	    	{
	    		writeError(request,"error.atomInvalidXMLContent");
				setStatus(request,response,HttpServletResponse.SC_BAD_REQUEST);
	    	}
	    	else if (exception instanceof AccessControlException)
	    	{
	    		return handleAclError(mapping, form, request, response);
	    	}
	    	else if (exception instanceof DataValidationException)
	    	{
	    		writeError(request,"error.atomInvalidRequest");
				setStatus(request,response,HttpServletResponse.SC_BAD_REQUEST);
	    	}
	    	else if (exception instanceof RequiredValueMissingException)
	    	{
	    		RequiredValueMissingException e = (RequiredValueMissingException) exception;
	    		if (e.getValueType() == RequiredValueMissingException.ValueType.MODEL) {
	    			if (LOG.isErrorEnabled())
		    		{
	    				errorPrinted = true;
		    			LOG.error(exception.getMessage(), exception);
		    		}
		    		writeError(request,"error.atomGeneral");
		    		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    		}
	    		else {
	    			writeError(request,"error.atomInvalidRequest");
					setStatus(request,response,HttpServletResponse.SC_BAD_REQUEST);
	    		}
	    	}
	    	else if (exception instanceof BadRequestException)
	    	{
	    		writeError(request,"error.atomInvalidRequest");
				setStatus(request,response,HttpServletResponse.SC_BAD_REQUEST);
	    	}
	    	else
	    	{
	    		if (LOG.isErrorEnabled())
	    		{
	    			errorPrinted = true;
	    			LOG.error(exception.getMessage(), exception);
	    		}
	    		writeError(request,"error.atomGeneral");
	    		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    	}
    	}

    	// Print error message if missing
    	if (!errorPrinted) {
	    	if (LOG.isDebugEnabled()) {
	    		LOG.debug("Caught API Exception: " + APIHelper.getCallerStack(exception, 15));
	    	}
	    	else {
		    	if (LOG.isTraceEnabled()) {
	    			LOG.trace("Caught API Exception: " + exception.getMessage(), exception);
		    	}
	    	}
    	}    	
		ActionForward retVal = null;
		try
		{
			retVal = mapping.findForward("atomError");
		}
		catch (Exception e)
		{
			// log the error since Profiles typically is silent on such things and re-throw for any code relying on it
    		LOG.error("Profiles caught API Exception: " + exception.getMessage());
    		throw new Exception (e);
		}
    	return retVal;
    }

    private ActionForward handleAclError(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	// attribute set in AppContextFilter, which is to be mapped to all requests in web.xml
    	String originalUrl = (String) request.getAttribute("profilesOriginalLocation");
    	
    	// defect 96122 - originalUrl may be null. maybe not set by browser or removed by a proxy?
    	boolean isForms = false;
    	if (StringUtils.isEmpty(originalUrl) == false){
    		isForms = originalUrl.matches(".*/(forms|ajax)/.*");
    	}
    	String remoteUser = request.getRemoteUser();
    	if (LOG.isDebugEnabled()) {
    		String traceVal = "null";
    		if (null != remoteUser)
    			traceVal = remoteUser;
    		debugLog("remoteUser = " + traceVal);
    	}
		if (remoteUser == null) {
			if (isForms)
				return mapping.findForward("loginAtomForm");
			return mapping.findForward("atomAuthenticate");	
		}
		if (LOG.isDebugEnabled()) {
			debugLog("atomError = " + "error.atomUnauthorized");
		}
		writeError(request,"error.atomUnauthorized");
		setStatus(request,response,HttpServletResponse.SC_FORBIDDEN);

		return mapping.findForward("atomError");
	}

	private void debugLog(String msg)
	{
    	if (LOG.isDebugEnabled()) {
    		LOG.debug(msg);
    	}
	}

	private void setStatus(HttpServletRequest request, HttpServletResponse response, int statusCode)
    {
    	request.setAttribute("profiles_statusCode", statusCode);
    	response.setStatus(statusCode);
    }
    
    private void writeError(HttpServletRequest request, String errKey)
    {
    	ActionMessages errors = getErrors(request);
		ActionMessage message = new ActionMessage(errKey);
		errors.add(Globals.ERROR_KEY, message);
		saveErrors(request, errors);
    }
    
    private void writeError(HttpServletRequest request, String defaultErrKey, Exception ex)
    {
    	ActionMessages errors = getErrors(request);
		ActionMessage message;
		if (ex != null && StringUtils.isNotEmpty(ex.getMessage())) {
			request.setAttribute("lcErrorMessageOverride", ex.getMessage());
			message = new ActionMessage(ex.getMessage(), false);
		} else {
			message = new ActionMessage(defaultErrKey);
		}
		errors.add(Globals.ERROR_KEY, message);
		saveErrors(request, errors);		
    }
    
    private void clearErrors(HttpServletRequest request)
    {
    	ActionMessages errors = getErrors(request);
    	errors.clear();
		saveErrors(request, errors);
    }
    
    private Throwable getRootCause(Throwable ex)
    {
    	Throwable uc = ex.getCause(), root = null;
    	
    	while (uc != null)
    	{
    		root = uc;
    		uc = uc.getCause();
    	}
    	    	
    	return root;
    }
}
