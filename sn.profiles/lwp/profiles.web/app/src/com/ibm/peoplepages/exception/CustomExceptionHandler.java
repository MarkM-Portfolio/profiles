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

package com.ibm.peoplepages.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.config.ExceptionConfig;

import com.ibm.sn.av.api.AVScannerException;

public class CustomExceptionHandler extends ExceptionHandler {
	
	private static final Log LOG = LogFactory.getLog(CustomExceptionHandler.class);
	
    /* 
     * (non-Javadoc)
     * @see org.apache.struts.action.ExceptionHandler#execute(java.lang.Exception, 
     * 		org.apache.struts.config.ExceptionConfig, org.apache.struts.action.ActionMapping, 
     * 		org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, 
     * 		javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(Exception ex, ExceptionConfig ec, ActionMapping mapping, ActionForm form,
    	HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	
        ActionForward forward = new ActionForward(ec.getPath());
                
        Throwable cause = getRootCause(ex);
        String backTraceMessage = "";
        String backTrace = "";
        String errKey = ec.getKey();
        if (cause != null) 
        {
        	backTraceMessage = "Caused by: " + cause.toString();
        	backTrace = getStackTrace(cause);
        	
        	if (cause instanceof AVScannerException)
        	{
        		errKey = "error.fileContainsVirus";
        		request.setAttribute("showAltErrMsg", errKey);
        	}
        }
        
        if (LOG.isErrorEnabled()) {
        	LOG.error(ex.getMessage(), ex);
        }
        
        ActionMessage error = new ActionMessage(errKey, ex.toString(), getStackTrace(ex), backTraceMessage, backTrace);
        String property = error.getKey();

        request.setAttribute(Globals.EXCEPTION_KEY, ex);
        this.storeException(request, property, error, forward, ec.getScope());

        return forward;
    }
    
    private String getStackTrace(Throwable th) {
    	StringBuffer stackTrace = new StringBuffer();
    	
    	ArrayList<StackTraceElement> stackTraceElements = new ArrayList<StackTraceElement>(Arrays.asList(th.getStackTrace()));
    	ListIterator<StackTraceElement> iterator = stackTraceElements.listIterator();
        while (iterator.hasNext()) {
        	stackTrace.append((iterator.next()).toString() + "\n");
        }
        
        return stackTrace.toString();
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
