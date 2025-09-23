/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.web.actions.BaseAction;

import com.ibm.peoplepages.util.RestContentTypeValidator;

public abstract class APIAction extends BaseAction
{
	private static Logger LOGGER = Logger.getLogger(APIAction.class.getName());

	/**
	 * Utility class to handle exceptions
	 */
	private static final class APIErrorDelegate implements ErrorDelegate
	{
		public static final APIErrorDelegate INSTANCE = new APIErrorDelegate();
		
		public boolean doesHandle(Exception ex) {
			return true;
		}

		public ActionForward handle(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response, Exception ex) 
		{
			request.setAttribute("org.apache.struts.action.EXCEPTION", ex);
			if (ex instanceof org.apache.abdera.parser.ParseException) {
				String msg = getErrorMessage(ex);
				request.setAttribute("profiles_statusCode",    HttpServletResponse.SC_BAD_REQUEST);
				request.setAttribute("lcErrorMessageOverride", ((null == msg) ? ex.getMessage() : msg));
			}
			return mapping.findForward("atomErrorHandler");
		}

		private String getErrorMessage(Exception ex) {
			String msg = ex.getLocalizedMessage();
			Throwable cause = ex.getCause();
			// Use the message from the cause if the throwable doesn't have a message
			// Note that we only go back one level
			if (msg == null && cause != null && cause.getLocalizedMessage() != null)
				msg = cause.getLocalizedMessage();
			return msg;
		}		
	}

	public APIAction()
	{
		super.errorDelegate = APIErrorDelegate.INSTANCE;
	}

	protected final ActionForward doExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception 
	{
		boolean FINER   = LOGGER.isLoggable(Level.FINER);
		boolean FINEST  = LOGGER.isLoggable(Level.FINEST);
		boolean tracing = FINER || FINEST;

		String method = request.getMethod();

		if (tracing)
		{
			try {
				String query = request.getQueryString();			
				String requestUri = request.getRequestURI();
				if (null != query){
					requestUri = requestUri + "?" + query;
					if (FINER) {
						LOGGER.finest("Received request : " + method + " URI: " + requestUri);
					}
				}
				if (FINEST) {
					String user = request.getRemoteUser();
					String host = request.getRemoteHost();
					LOGGER.finer("Received request : " + method + " from user: " + user  + " from host: " + host + " URI: " + requestUri + " query: " + query);
				}
			}
			catch (Exception ex) { /* silent if tracing code fails*/ }
		}
		if ("GET".equalsIgnoreCase(method))
		{
			return doExecuteGET(mapping, form, request, response);
		}
		else if ("HEAD".equalsIgnoreCase(method))
		{
			return doExecuteHEAD(mapping, form, request, response);
		}
		else if ("POST".equalsIgnoreCase(method))
		{
			// see RTC 175764 for discussion on validation.
			boolean proceed = RestContentTypeValidator.validatePOST(request);
			if (proceed) {
				return doExecutePOST(mapping, form, request, response);
			}
			else{
				throw new APIException(ECause.INVALID_CONTENT_TYPE);
			}
		}
		else if ("PUT".equalsIgnoreCase(method))
		{
			return doExecutePUT(mapping, form, request, response);
		}
		else if ("DELETE".equalsIgnoreCase(method))
		{
			return doExecuteDELETE(mapping, form, request, response);
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * This is an actual stub method - it will return without throwing exception.
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	protected ActionForward doExecuteHEAD(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		return null;
	}	
	
	protected ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		throw new APIException(ECause.INVALID_OPERATION);
	}
	
	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		throw new APIException(ECause.INVALID_OPERATION);
	}
	
	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		throw new APIException(ECause.INVALID_OPERATION);
	}
	
	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		throw new APIException(ECause.INVALID_OPERATION);
	}
	
    /**
     * Sets up and performs 'invalid request' action for WPI.
     * 
     * @param mapping
     * @param request
     * @param response
     * @return
     */
    protected final ActionForward doInvalidAPIRequest(
    		ActionMapping mapping,
			HttpServletRequest request, 
			HttpServletResponse response)
    {
    	ActionMessages errors = getErrors(request);
		ActionMessage message = new ActionMessage("error.atomInvalidParameters");
		errors.add(Globals.ERROR_KEY, message);
		saveErrors(request, errors);
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		return mapping.findForward("atomError");
    }
	
    public static final void assertNotNull(Object o) throws APIException
    {
    	assertNotNull(o, ECause.INVALID_REQUEST);
    }
    
    public static final void assertNotNull(Object o, ECause cause) throws APIException
    {
    	if (o == null)
    	{
    		throw new APIException(cause);
    	}
    }
    
    public static final void assertTrue(boolean b) throws APIException
    {
    	assertTrue(b, ECause.INVALID_REQUEST);
    }
    
    public static final void assertTrue(boolean b, ECause consequence) throws APIException
    {
    	if (!b)
    	{
    		throw new APIException(consequence);
    	}
    }
}
