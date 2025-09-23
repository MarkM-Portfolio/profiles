/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2012, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAgentHelper {
	static final Pattern NON_REDIRECT_CACHING_CHROME_VERSION_PATTERN = Pattern.compile(".*Chrome/([0-9]|10)\\.");

	static final Pattern NON_REDIRECT_CACHING_FIREFOX_VERSION_PATTERN = Pattern.compile(".*Firefox/[0-8]\\.");

	static final Pattern NON_REDIRECT_CACHING_IE_VERSION_PATTERN = Pattern.compile("MSIE [1-8]\\.");

	// not clear that Safari support for redirect caching works well in any version. however, chrome versions do include a 'safari'
	// we check for non-existence of 'Chrome' and existence of 'Safari'. see notes below.
	static final Pattern NON_REDIRECT_CACHING_SAFARI_VERSION_PATTERN = Pattern.compile("^(?!.*Chrome).*Safari.*$");

	static final Pattern OPERA = Pattern.compile("Opera");

	// Opera from v10 supports redirect caching, helpfully they also added this string to the user agent at that version
	static final Pattern VERSION = Pattern.compile("Version");
	
	// note on safari check. chrome user-agent strings may contain 'safari'. e.g.
	//   Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36
	// the safari check looks that the string does not contain a chrome version via regex negative lookahead. here are trwo references.
	//   http://stackoverflow.com/questions/2953039/regular-expression-for-a-string-containing-one-word-but-not-another
	//   http://fineonly.com/solutions/regex-exclude-a-string
	
	/*
	 * Use this function to identify browser versions that are known to not support redirect caching. This function errs on the side of
	 * permitting redirect caching, filtering out only the most common browser versions that are known to not support redirect caching or
	 * are known to have issues. The filtering is implemented as exclusions on the expectation that future browser versions will continue to
	 * have this feature and by excluding non-supporting but still common browser versions no future work is needed to include future
	 * versions.
	 * 
	 * @param userAgent
	 *            as returned by <code>request.getHeader("User-Agent")</code>
	 */
	public static boolean supportsRedirectCaching(String userAgent) {
		Matcher m = NON_REDIRECT_CACHING_IE_VERSION_PATTERN.matcher(userAgent);
		if (m.find()) return false;

		m = NON_REDIRECT_CACHING_FIREFOX_VERSION_PATTERN.matcher(userAgent);
		if (m.find()) return false;

		m = NON_REDIRECT_CACHING_CHROME_VERSION_PATTERN.matcher(userAgent);
		if (m.find()) return false;
		
		// check chrome before safari since chrome user-agent strings may contain 'safari'. see notes above.
		m = NON_REDIRECT_CACHING_SAFARI_VERSION_PATTERN.matcher(userAgent);
		if (m.find()) return false;

		m = OPERA.matcher(userAgent);
		if (m.find()) {
			m = VERSION.matcher(userAgent);
			return m.find();
		}

		return true;
	}

// more condensed implementation. not sure it works. it seems to have a problem in a quick test.
//	// support the calculation for broswers: Chrome, Firefox, Explorer, Safari
//	// the following patterns are ORed together to determine if the browser does not support redirect caching
//	// Chrome: .*Chrome/([0-9]|10)\\.
//	// Firefox: .*Firefox/[0-8]\\.
//	// Explorer: MSIE [1-8]\\.
//	// Safari: Version/[0-5].*Safari/
//	static final Pattern SUPPORTED_BROWSERS = Pattern.compile("Chrome|FireFox|MSIE|Safari");
//	static final Pattern NON_SUPPORTED_BROWSER_VERSIONS = Pattern
//			.compile(".*Chrome/([0-9]|10)\\.|.*Firefox/[0-8]\\.|MSIE [1-8]\\.|Version/[0-5].*Safari/");
//
//	/**
//	 * Use this function to identify browser versions that are known to not support redirect caching. This function errs on the side of
//	 * permitting redirect caching, filtering out only the most common browser versions that are known to not support redirect caching or
//	 * are known to have issues. The filtering is implemented as exclusions on the expectation that future browser versions will continue to
//	 * have this feature and by excluding non-supporting but still common browser versions no future work is needed to include future
//	 * versions.
//	 * 
//	 * @param userAgent
//	 *            as returned by <code>request.getHeader("User-Agent")</code>
//	 * @return
//	 */
//	public static boolean supportsRedirectCaching(String userAgent) {
//		Matcher m = SUPPORTED_BROWSERS.matcher(userAgent);
//		if (m.find()) {
//			m = NON_SUPPORTED_BROWSER_VERSIONS.matcher(userAgent);
//			if (m.find()) {
//				return false;
//			}
//			else {
//				return true;
//			}
//		}
//		else {
//			return false;
//		}
//	}
}
