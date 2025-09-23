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

package com.ibm.peoplepages.validator;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.ValidatorAction;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.validator.Resources;

import com.ibm.lconn.core.web.secutil.DangerousUrlHelper;

/**
 * @author sberajaw
 */
public class SessionExpirationValidator implements Serializable 
{
	private static final String ALREADY_TESTED_KEY = SessionExpirationValidator.class.getName() + "tested";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2034085069624864123L;
	
	public static boolean validateNonce(
			Object bean, ValidatorAction va, Field field,
			ActionMessages errors, HttpServletRequest request) throws Exception {
		boolean rtn = true;
		if (request.getAttribute(ALREADY_TESTED_KEY) == null) {
			request.setAttribute(ALREADY_TESTED_KEY, Boolean.TRUE);
			// get the expected nonce value
			String nonce = DangerousUrlHelper.getNonce(request);
			String reqNonce = getRequestNonce(request);
			if (reqNonce == null || nonce == null || !nonce.equals(reqNonce)) {
				errors.add(field.getKey(), Resources.getActionMessage(request, va, field));
				rtn = false;
			}
		}
		return rtn;
	}
	
	private static String getRequestNonce(HttpServletRequest request){
		// get the request nonce - most likely case the nonce is a parameter via a form
		String rtn = request.getParameter(DangerousUrlHelper.DANGEROUS_NONCE);
		// less likely, nonce may also be sent via a header - e.g. delete photo
		if (rtn == null){
			rtn = request.getHeader(DangerousUrlHelper.KEY_NAME);
		}
		if (rtn != null) rtn = StringUtils.trim(rtn); // can we get whitespace?
		return rtn;
	}
}
