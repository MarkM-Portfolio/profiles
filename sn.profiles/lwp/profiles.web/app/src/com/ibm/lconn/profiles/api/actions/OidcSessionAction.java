/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2020                           */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.PrintWriter;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.connections.httpClient.WASSecurityUtil;
import com.ibm.json.java.JSONObject;
import com.ibm.wsspi.security.token.SingleSignonToken;

/**
 * OidcSessionAction
 * 
 * This endpoint requires authentication, so should always have a security
 * context. It is designed to be called by a client having a valid OIDC token,
 * which would be intercepted by the WAS OIDC TAI and validated.
 *
 */
public class OidcSessionAction extends APIAction implements AtomConstants {
	private static final Log LOG = LogFactory.getLog(OidcSessionAction.class);
	
	private static final String OIDC_REMOTEUSER = "remoteUser"; 
	private static final String OIDC_PRINCIPAL = "principal"; 
	private static final String OIDC_EXPIRATION = "exp"; 
	private static final String OIDC_UNKNOWN = "unknown"; 

	public ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		JSONObject identityPayload = new JSONObject();
		identityPayload.put(OIDC_REMOTEUSER, request.getRemoteUser());
		Principal userPrincipal = request.getUserPrincipal();
		identityPayload.put(OIDC_PRINCIPAL, userPrincipal != null ? userPrincipal.getName() : OIDC_UNKNOWN);
		identityPayload.put(OIDC_EXPIRATION, getSsoExpiration());

		if (LOG.isTraceEnabled()) {
			LOG.trace("OidcSession " + identityPayload.toString());
		}

		response.setContentType(JSON_CONTENT_TYPE);
		response.setCharacterEncoding(XML_ENCODING);
		PrintWriter writer = response.getWriter();
		writer.write(identityPayload.serialize());

		return null;
	}

	private long getSsoExpiration() {
		long expirationTime = 0;
		try {
			Subject subject = WASSecurityUtil.getRunAsSubject();
			SingleSignonToken ssoToken = WASSecurityUtil.getDefaultSSOTokenFromSubject(subject);
			if (ssoToken != null) {
				expirationTime = ssoToken.getExpiration();
			}
		} catch (Exception ex) {
			LOG.error("Failed to retrieve SSO expiration", ex);
		}

		return expirationTime;
	}

	protected long getLastModified(HttpServletRequest request) throws Exception {
		return System.currentTimeMillis();
	}
}
