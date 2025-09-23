/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/**
 * 
 */
package com.ibm.lconn.profiles.web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.internal.exception.AssertionException;

/**
 * @author user
 *
 */
public abstract class UnCachableUIAction extends UnCachableAction {

	protected UnCachableUIAction() {
		// instantiate defaults up the hierarchy chain
		super();
		this.errorDelegate = initErrorDelegate();
	}

	/**
	 * Overridable error delegate method. If you need another variant of the
	 * error delegate class override this method.
	 * 
	 * @return
	 */
	protected ErrorDelegate initErrorDelegate() {
		return new UnCacheErrorDelegate();
	}
	
	/**
	 * Extendible error delegate
	 */
	protected class UnCacheErrorDelegate implements ErrorDelegate {
		public boolean doesHandle(Exception ex) {
			if (ex instanceof AssertionException) {
				AssertionException assertion = (AssertionException) ex;
				switch (assertion.getType()) {
					case USER_NOT_FOUND:
						return true;
					case BAD_REQUEST:
						return true;
				}
			}
			
			return false;
		}

		public ActionForward handle(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response,
				Exception ex) 
		{
			if (ex instanceof AssertionException) {
				AssertionException assertion = (AssertionException) ex;
				switch (assertion.getType()) {
					case USER_NOT_FOUND:
						return mapping.findForward("uiUserNotFound");
					case BAD_REQUEST:
						return mapping.findForward("uiBadRequest");
				}
			}
			
			return null;
		}
	}
	
}
