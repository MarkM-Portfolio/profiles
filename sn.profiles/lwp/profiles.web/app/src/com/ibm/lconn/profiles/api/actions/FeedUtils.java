/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.abdera.protocol.server.RequestContext.Scope;

import com.ibm.lconn.core.web.atom.LCRequestContext;
import com.ibm.lconn.core.web.atom.util.LCResponseUtils;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig;
import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public final class FeedUtils
{
	public static final String PROFILES_URL_KEY   = FeedUtils.class.getName() + ".profiles.url.key";
	public static final String OPENSOCIAL_URL_KEY = FeedUtils.class.getName() + ".opensocial.url.key";

	private static final String ATOM_PROFILES = "/atom/profile.do";
	private static final String PROFILE_ENTRY = "/atom/profileEntry.do";
	private static final String PROFILE_TAGS  = "/atom/profileTags.do";
	private static final String PROFILE_TYPE  = "/atom/profileType.do";
	private static final String CONNECTIONS   = "/atom/connections.do";
	private static final String CONNECTION    = "/atom/connection.do";
	private static final String PROFILE_EXTENSTION = "/atom/profileExtension.do";
	
	private static final String REPORTSTRUCT = "/atom/reportingChain.do";
	private static final String PEOPLEMANAGED = "/atom/peopleManaged.do";
	
	private static final String PHOTO = "/photo.do";
	private static final String OAUTH_PHOTO = "/oauth/photo.do";
	private static final String AUDIO = "/audio.do";
	private static final String VCARD = "/vcard/profile.do";
	private static final String HTML = "/html/profileView.do";
	private static final String LAST_MOD = "lastMod";

	private static final String ADMIN_PROFILE_SERVICE     = "/admin/atom/profileService.do";
	private static final String ADMIN_PROFILES            = "/admin/atom/profiles.do";
	private static final String ADMIN_PROFILE_ENTRY       = "/admin/atom/profileEntry.do";
	private static final String ADMIN_PROFILE_TAGS        = "/admin/atom/profileTags.do";
	private static final String ADMIN_PROFILE_ROLES       = "/admin/atom/profileRoles.do";
	private static final String ADMIN_PROFILE_FOLLOWING   = "/admin/atom/following.do";
	private static final String ADMIN_PROFILE_CONNECTIONS = "/admin/atom/connections.do";
	private static final String ADMIN_PROFILE_CONNECTION  = "/admin/atom/connection.do";
	private static final String ADMIN_PROFILE_CODES       = "/admin/atom/codes.do";

	private static final String ADMIN_CODES = "/admin/atom/codes/";

	private static final String NON_OAUTH_SEG = "/profiles/atom/";
	private static final String OAUTH_SEG = "/profiles/oauth/atom/";
	private static final String PRE_OAUTH_SEG = "/profiles/";
	

	public static String calculateProfilesFeedURL(String key, String profilesURL) throws UnsupportedEncodingException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(ATOM_PROFILES);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(key);
		return buffer.toString();
	}


	public static String calculateProfilesEntryURL2(String key, String profilesURL) throws UnsupportedEncodingException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(PROFILE_ENTRY);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(key);
		buffer.append("&");
		buffer.append(PeoplePagesServiceConstants.OUTPUT);
		buffer.append("=");
		buffer.append(PeoplePagesServiceConstants.VCARD);
		buffer.append("&");
		buffer.append(PeoplePagesServiceConstants.FORMAT);
		buffer.append("=");
		buffer.append(PeoplePagesServiceConstants.FULL);
		return buffer.toString();
	}


	public static String calculateProfilesEntryURL2(String key, String profilesURL, boolean vcard, boolean isLite)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(PROFILE_ENTRY);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(key);
		if (vcard)
		{
			buffer.append("&");
			buffer.append(PeoplePagesServiceConstants.OUTPUT);
			buffer.append("=");
			buffer.append(PeoplePagesServiceConstants.VCARD);
		}
		if (!isLite)
		{
			buffer.append("&");
			buffer.append(PeoplePagesServiceConstants.FORMAT);
			buffer.append("=");
			buffer.append(PeoplePagesServiceConstants.FULL);
		}
		return buffer.toString();
	}

	public static String calculateProfileTypeEntryURL(String key, String profilesURL)  throws UnsupportedEncodingException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(PROFILE_TYPE);

		if (null != key && !"".equals(key.trim())) {
			buffer.append("?");
			buffer.append(PeoplePagesServiceConstants.TYPE);
			buffer.append("=");
			buffer.append(URLEncoder.encode(key, "UTF-8"));
		}

		return buffer.toString();
	}
	
	public static String calculateConnectionEntryURL(String connectionId, String profilesURL) throws UnsupportedEncodingException
	{
		connectionId = URLEncoder.encode(connectionId, "UTF-8");
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(CONNECTION);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.CONNECTION_ID);
		buffer.append("=");
		buffer.append(connectionId);
		return buffer.toString();
	}

	public static String calculatePhotoUrl2(String key, String profilesURL, Date lastMod) throws UnsupportedEncodingException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(PHOTO);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(key);
		appendLastMod(buffer, lastMod);
		return buffer.toString();
	}

	public static String calculateOauthPhotoUrl(String key, String profilesURL, Date lastMod) throws UnsupportedEncodingException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(OAUTH_PHOTO);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(key);
		appendLastMod(buffer, lastMod);
		return buffer.toString();
	}


	public static String calculatePronunciationUrl2(String key, String profilesURL, Date lastMod) throws UnsupportedEncodingException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(AUDIO);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(key);
		appendLastMod(buffer, lastMod);
		return buffer.toString();
	}
	
	public static String calculateTagCloudUrl(String key, String profilesURL, Date lastMod)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(PROFILE_TAGS);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.TARGET_KEY);
		buffer.append("=");
		buffer.append(key);
		appendLastMod(buffer, lastMod);
		return buffer.toString();
	}
	
	public static String calculateTheBoardUrl(String key, String profilesURL, Date lastMod)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append("/atom/mv/theboard/entries.do?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(LCResponseUtils.urlEncode(key));
		appendLastMod(buffer, lastMod);
		return buffer.toString();
	}
	
	public static String calculateOpenSocialBoardUrl(String guid, String openSocialUrl, Date lastMod)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(openSocialUrl);
		buffer.append("/rest/activitystreams/");
		buffer.append(guid);
		// default response is JSON, as advised by News/Homepage team
		buffer.append("/@involved/@all");
		return buffer.toString();
	}
	
	public static String calculateStatusUrl(String key, String profilesURL, Date lastMod)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append("/atom/mv/theboard/entry/status.do?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(LCResponseUtils.urlEncode(key));
		return buffer.toString();
	}
	
	public static String calculateOpenSocialStatusUrl(String guid, String openSocialUrl, Date lastMod)
	{
		StringBuilder buffer = new StringBuilder();
		// Inserted by 89568. Need new enpoint from News/Homepage for this function.
		buffer.append(openSocialUrl);
		buffer.append("/rest/activitystreams/@me/@self/@status?rollup=true");
		//buffer.append(guid); - seems like @me works
		// default response is JSON, as advised by News/Homepage team
		//buffer.append("/@self/@status?count=1");
		return buffer.toString();
	}
	
	public static String calculateOpenSocialStatusUpdatesUrl(String guid, String openSocialUrl, Date lastMod)
	{
		StringBuilder buffer = new StringBuilder();
		// Inserted by 89568. Need new enpoint from News/Homepage for this function.
		buffer.append(openSocialUrl);
		buffer.append("/rest/activitystreams/@me/@all@status");
		return buffer.toString();
	}
	
	public static String calculateConnectionsUrl(String key, String profilesURL, String connectionType, Date lastMod) 
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(CONNECTIONS);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.CONNECTION_TYPE);
		buffer.append("=");
		buffer.append(connectionType);
		buffer.append("&");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(key);
		appendLastMod(buffer, lastMod);
		return buffer.toString();
	}
	
	public static String calculateColleaguesConnectionsUrl(String key, String profilesURL, Date lastMod)
	{
		return calculateConnectionsUrl(key, profilesURL, PeoplePagesServiceConstants.COLLEAGUE, lastMod);
	}
	
	public static String calculateProfileExtensionUrl(String key, String extensionId, String profilesURL, Date lastMod)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(PROFILE_EXTENSTION);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(key);
		buffer.append("&");
		buffer.append(PeoplePagesServiceConstants.EXTENSION_ID);
		buffer.append("=");
		buffer.append(extensionId);
		appendLastMod(buffer, lastMod);
		return buffer.toString();
	}

	public static String calculateAdminProfilesURL(String key, String profilesURL)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(ADMIN_PROFILES);
		if (null != key) {
			buffer.append("?");
			buffer.append(PeoplePagesServiceConstants.KEY);
			buffer.append("=");
			buffer.append(key);
		}
		return buffer.toString();
	}

	public static String calculateAdminProfileEntryURL(String key, String profilesURL)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(ADMIN_PROFILE_ENTRY);
		if (null != key) {
			buffer.append("?");
			buffer.append(PeoplePagesServiceConstants.KEY);
			buffer.append("=");
			buffer.append(key);
		}
		return buffer.toString();
	}

	public static String calculateAdminCodesURL(AbstractCode<?> code, String id, String profilesURL) throws UnsupportedEncodingException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		String classname = code.getClass().getSimpleName();
		String encodedCodeId = URLEncoder.encode(id, "UTF-8");

		buffer.append(ADMIN_CODES);
		buffer.append(classname);		
		buffer.append(".do");
		
		if (id != null) {
			buffer.append("?");
			buffer.append(PeoplePagesServiceConstants.CODE_ID);		
			buffer.append("=");
			buffer.append( encodedCodeId );			
		}		
		return buffer.toString();
	}

	private final static void appendLastMod(StringBuilder buffer, Date lastMod) 
	{
		if (lastMod != null)
		{
			buffer.append("&");
			buffer.append(LAST_MOD);
			buffer.append("=");
			buffer.append(lastMod.getTime());
		}
	}

	public static String calculateVcardUrl(String key, String profilesURL) throws UnsupportedEncodingException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(VCARD);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(key);
		return buffer.toString();
	}

	public static String calculateHtmlUrl2(String key, String profilesURL) throws UnsupportedEncodingException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(HTML);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(key);
		return buffer.toString();
	}
	
	public static String calculateReportingStructUrl(String key, String profilesURL, Date profileLastMod) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(REPORTSTRUCT);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(key);
		return buffer.toString();
	}
	
	public static String calculatePeopleManagedUrl(String key, String profilesURL, Date profileLastMod) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(PEOPLEMANAGED);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.KEY);
		buffer.append("=");
		buffer.append(key);
		return buffer.toString();
	}

	public static String getProfilesURL(HttpServletRequest request)
	{
		String url = (String) request.getAttribute(PROFILES_URL_KEY);
		
		if (url == null) 
		{
			if (request.isSecure()) {
				url = ServiceReferenceUtil.getServiceLink("profiles", true);
			}
			
			if (url == null) {
				url = ServiceReferenceUtil.getServiceLink("profiles", false);
			}
			
			if (url != null) {
				if (url.charAt(url.length() - 1) == '/') {
					url = url.substring(0, url.length() - 1);
				}
				request.setAttribute(PROFILES_URL_KEY, url);
			}
		}
		
		return url;
	}

	/**
	 * @param request
	 * @param serviceName one of <code>com.ibm.lconn.core.web.util.services.ServiceReferenceUtil.Service</code>
	 * @param serviceUrlKey
	 * @return
	 */
	public static String getServiceURL(HttpServletRequest request, String serviceName, String serviceUrlKey)
	{
		String url = (String) request.getAttribute(serviceUrlKey);
		
		if (url == null) 
		{
			if (request.isSecure()) {
				url = ServiceReferenceUtil.getServiceLink(serviceName, true);
			}
			
			if (url == null) {
				url = ServiceReferenceUtil.getServiceLink(serviceName, false);
			}
			
			if (url != null) {
				if (url.charAt(url.length() - 1) == '/') {
					url = url.substring(0, url.length() - 1);
				}
				request.setAttribute(serviceUrlKey, url);
			}
		}
		
		return url;
	}

	public static String getProfilesURL(LCRequestContext request) {
		String url = (String) request.getAttribute(Scope.REQUEST, PROFILES_URL_KEY);
		
		if (url == null) 
		{
			if (request.isSecure()) {
				url = ServiceReferenceUtil.getServiceLink("profiles", true);
			}
			
			if (url == null) {
				url = ServiceReferenceUtil.getServiceLink("profiles", false);
			}
			
			if (url != null) {
				if (url.charAt(url.length() - 1) == '/') {
					url = url.substring(0, url.length() - 1);
				}
				request.setAttribute(PROFILES_URL_KEY, url);
			}
		}
		
		return url;
	}	


	public static String modUrlForOauth (String url) {
		// if this is an atom feed, and oauth is on the request, then insert
		// oauth into the returned url if it is not an html request
		if (url.indexOf(NON_OAUTH_SEG) != -1)  {
			int ind = url.indexOf(PRE_OAUTH_SEG);
			url = url.substring(0, ind) + OAUTH_SEG + 
					url.substring(ind + NON_OAUTH_SEG.length(), url.length());
		}
		return url;
	}

	// calculate Profile admin service document URLs

	public static String calculateAdminProfileServiceDocURL(String profilesURL) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(ADMIN_PROFILE_SERVICE);
		return buffer.toString();
	}

	public static String calculateAdminProfilesURL(String profilesURL) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(ADMIN_PROFILES);
		return buffer.toString();
	}

	public static String calculateAdminProfileEntryURL(String profilesURL) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(ADMIN_PROFILE_ENTRY);
		return buffer.toString();
	}

	public static String calculateAdminTagURL(String profilesURL) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(ADMIN_PROFILE_TAGS);
		return buffer.toString();
	}

	public static String calculateAdminRolesURL(String profilesURL) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(ADMIN_PROFILE_ROLES);
		return buffer.toString();
	}

	public static String calculateAdminFollowingURL(String profilesURL) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(ADMIN_PROFILE_FOLLOWING);
		return buffer.toString();
	}

	public static String calculateAdminConnectionsURL(String profilesURL) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(ADMIN_PROFILE_CONNECTIONS);
		return buffer.toString();
	}

	public static String calculateAdminConnectionURL(String profilesURL) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(ADMIN_PROFILE_CONNECTION);
		return buffer.toString();
	}

	public static String calculateAdminCodesURL(String profilesURL) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(profilesURL);
		buffer.append(ADMIN_PROFILE_CODES);
		return buffer.toString();
	}

}

