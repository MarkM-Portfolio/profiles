/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2016, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.util;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import com.ibm.lconn.core.gatekeeper.LCSupportedFeature;
import com.ibm.lconn.profiles.config.LCConfig;

/*
 * This class exists so it can be used in APIAction as well as the
 * rogue RonnyPena RestServlet :(  If we can get rid of the RestServlet
 * the method(s) here could become until method(s) in APIAction.
 */
public class RestContentTypeValidator {
	/*
	 * Validate content type for a POST request. Derived action classes can override this validator.
	 */
	public static boolean validatePOST(HttpServletRequest request) {
		boolean rtn = true;
		String requestUri = request.getRequestURI();
		// CSRFFilter may kick out plain/text for /atom requests, but now we do teh work in the component.
		// we have the option of restricting content-type to, for example, the set below. doing so may
		// have consequences as clients are likely to be lax is setting the header. we know of one issue
		// with team analytics: see rtc 175764
		if (StringUtils.contains(requestUri, "/atom")) {
			String ct = request.getHeader(HttpHeaders.CONTENT_TYPE); // do we have constants for headers?
			if (ct != null) {
				ct = ct.trim();
				if (StringUtils.startsWithIgnoreCase(ct, MediaType.TEXT_PLAIN)) {
					rtn = false;
				}
			}
			// rtn = StringUtils.startsWithIgnoreCase(ct,AtomConstants.ATOM_MIME_TYPE) // "application/atom+xml"
			// || StringUtils.startsWithIgnoreCase(ct,AtomConstants.APP_MIME_TYPE) // "application/atomcat+xml"
			// || StringUtils.startsWithIgnoreCase(ct,AtomConstants.XML_CONTENT_TYPE) // "application/xml"
			// we could move this check to specific action classes that want this type
			// || StringUtils.startsWithIgnoreCase(ct,AtomConstants.PROFILE_TYPE_CONTENT_TYPE); // "application/profile-type+xml"
		}
		return rtn;
	}
}
