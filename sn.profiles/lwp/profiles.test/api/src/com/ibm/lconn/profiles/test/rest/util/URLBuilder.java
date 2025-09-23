/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.util;

import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.core.web.secutil.Sha256Encoder;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.junit.cloud.CloudTestProperties;

public class URLBuilder {

	
	private static class Path {
		private static final String ADMIN = "admin";

		private static final String APP = "profiles";

		private static final String HTML = "html";
		
		private static final String TAG_TYPEAHEAD = "tagTypeahead.do";
		
		private static final String CODES = "codes";
		
		private static final String SEEDLIST = "seedlist";

		private static final String MYSERVER = "myserver";

		private static final String OAUTH = "oauth";

		private static final String API = "atom";

		private static final String PHOTO = "photo.do";

		private static final String PEOPLE_MANAGED = "peopleManaged.do";
		
		private static final String REPORTING_CHAIN = "reportingChain.do";
		
		private static final String ORG_RELATION = "orgRelation.do";

		private static final String PROFILE_ENTRY = "profileEntry.do";

		private static final String MESSAGE_VECTOR = "mv";

		private static final String BOARD = "theboard";

		private static final String ENTRIES = "entries";

		private static final String BOARD_ENTRIES = "entries.do";

		private static final String ALL = "all.do";

		private static final String PROFILES = "profiles.do";

		private static final String PROFILE = "profile.do";

		private static final String PROFILE_SERVICE = "profileService.do";

		private static final String SERVICE = "service"; // generic "service" path element, eg: .../follow/atom/service

		private static final String SEARCH = "search.do";

		private static final String TAGS = "profileTags.do";

		private static final String ROLES = "profileRoles.do";

		private static final String TYPE = "profileType.do";

		private static final String CONNECTION_TYPE_CONFIG = "connectionTypeConfig.do";
		
		private static final String TAGS_CONFIG = "tagsConfig.do";
		
		private static final String COMMENTS = "comments.do";

		private static final String RELATED = "related.do";

		private static final String CONNECTION = "connection.do";
		
		@SuppressWarnings("unused")
		private static final String COUNTRY = "Country.do";

		private static final String CONNECTIONS = "connections.do";

		private static final String FOLLOW = "follow";

		private static final String FOLLOWING = "following.do";
		
		private static final String RESOURCES = "resources";

		@SuppressWarnings("unused")
		private static final String PRONUNCIATION = "audio.do";
		
		private static final String SERVICE_CONFIGS="serviceconfigs";
	}

	public static class Query {
		
		public static final String USER_ID = "userid";

		public static final String UID = "uid";
		
		public static final String DISTINGUISHED_NAME = "distinguishedName";

		public static final String EMAIL = "email";

		public static final String MCODE = "mcode";

		public static final String IMAGE = "image";

		public static final String KEY = "key";

		public static final String TYPE = "type";

		public static final String FORMAT = "Format";

		public static final String OUTPUT = "Output";

		public static final String RANGE = "Range";

		public static final String ACTION = "Action";
		
		public static final String COMPLETE = "complete";

		public static final String LABELS = "labels";

		public static final String LOCALE = "lang";  // URL param wants to be 'lang'

		public static final String TIMESTAMP = "Timestamp";
		
		public static final String CONNECTION_ID = "connectionId";

		public static final String CONNECTION_TYPE = "connectionType";

		public static final String COLLEAGUE = "colleague";

		public static final String RESOURCE = "resource";

		public static final String SOURCE = "source";

		public static final String SOURCE_USERID = "sourceUserid";

		public static final String EXTENSION_AWARE = "extensionAware";
		
		public static final String TARGET_USERID = "targetUserid";
		
		public static final String LAST_MOD	= "lastMod";
		
		public static final String INCLUDE_MESSAGE = "inclMessage";
		
		public static final String TAG = "tag";
		
		public static final String FROM_TYPE = "fromType";
		
		public static final String TO_TYPE = "toType";
	}

	private final String serverURL;

	private final String serverURLHttp;

	public URLBuilder() {
		serverURL = TestProperties.getInstance().getBaseUrl();
		serverURLHttp = TestProperties.getInstance().getBaseUrlHttp();
	}

	public URLBuilder(TestProperties t) {
		serverURL = t.getBaseUrl();
		serverURLHttp = t.getBaseUrlHttp();
	}
	
	public URLBuilder(CloudTestProperties t) {
		serverURL = t.getBaseUrl();
		serverURLHttp = t.getBaseUrlHttp();
	}

	public String getServerURL() {
		return serverURL;
	}

	public String getServerURLHttp() {
		return serverURLHttp;
	}

	public String getProfilesServiceDocument() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.PROFILE_SERVICE);
		return url.toString();
	}

	public String getProfilesOauthServiceDocument() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.OAUTH);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.PROFILE_SERVICE);
		return url.toString();
	}

	public String getProfilesAdminServiceDocument() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.ADMIN);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.PROFILE_SERVICE);
		return url.toString();
	}

	public String getProfilesFollowingServiceUrl() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.FOLLOW);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.SERVICE);
		return url.toString();
	}

	public String getProfileTagsUrl(ProfileService profileService, String sourceUserId, boolean isExtensionAware) {
		String tagCloudUrl = profileService.getLinkHref(ApiConstants.SocialNetworking.REL_TAG_CLOUD);
		
		StringBuilder url = new StringBuilder(tagCloudUrl);
		if (null != sourceUserId) {
			addQueryParameter(url, Query.SOURCE_USERID, sourceUserId, false);
		}
		
		addQueryParameter(url, Query.EXTENSION_AWARE, isExtensionAware ? "true" : "false", false);
		return url.toString();
	}
	
	public String getProfileRolesUrl(ProfileService profileService, String userId) {
		String roleCollectionUrl = profileService.getLinkHref(ApiConstants.SocialNetworking.REL_ROLES);
		
		StringBuilder url = new StringBuilder(roleCollectionUrl);
		if (null != userId) {
			addQueryParameter(url, Query.USER_ID, userId, false);
		}
		return url.toString();
	}

	public String getProfileMoveTagsToNewTypeUrl(ProfileService profileService, String tag, String fromType, String toType) {
		String tagCloudUrl = profileService.getLinkHref(ApiConstants.SocialNetworking.REL_TAG_CLOUD);
		
		StringBuilder url = new StringBuilder(tagCloudUrl);
		if (null != tag) {
			addQueryParameter(url, Query.TAG, tag, false);
		}
		if (null != fromType) {
			addQueryParameter(url, Query.FROM_TYPE, fromType, false);
		}
		if (null != toType) {
			addQueryParameter(url, Query.TO_TYPE, toType, false);
		}
		
		return url.toString();
	}

	/**
	 * Note: Combination of sourceUserid and isFullFormat=true does not make sense, the source is already known. The response will NOT have
	 * contributor entries as you'd otherwise expect when isFullFormat=true. See
	 * <code>com.ibm.lconn.profiles.api.actions.AbstractProfileTagsAction.resolveBeanAsserted(HttpServletRequest)</code> for details. <br>
	 * <br>
	 * The odd-looking sourceUserid/targetUserid camelcasing was copied from the service, see
	 * <code>com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants.SOURCE_USERID</code> and
	 * <code>com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants.TARGET_USERID</code>
	 * 
	 * @param sourceUserid
	 * @param targetUserid
	 * @param isAdmin
	 * @param isFullFormat
	 * @return
	 */
	public String getProfileTagsUrl(String sourceUserid, String targetUserid, boolean isAdmin, boolean isFullFormat) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		if (isAdmin) addPathParameter(url, Path.ADMIN);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.TAGS);
		boolean isFirst = true;
		if (null != sourceUserid) {
			addQueryParameter(url, Query.SOURCE_USERID, sourceUserid, isFirst);
			isFirst = false;
		}
		if (null != targetUserid) {
			addQueryParameter(url, Query.TARGET_USERID, targetUserid, isFirst);
			isFirst = false;
		}
		if (isFullFormat) {
			addQueryParameter(url, Query.FORMAT.toLowerCase(), "full", isFirst);
		}
		return url.toString();
	}

	/*
	 * The request for using the locale URL param is only meaningful if 'format'='full' AND 'labels' = true
	 * https://server/profiles/atom/
	 *	profile.do?
	 *	email=ajones135@janet.iris.com&
	 *	format=full&
	 *	labels=true&
	 *	lang=pt_BR
	 */
	public String getProfileUrlWithLocale(String identifier, boolean isEmail, boolean isOnCloud, String locale) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.PROFILE);
		boolean isFirst = true;
		if (isOnCloud) {
			String mcode = Sha256Encoder.hashLowercaseStringUTF8(identifier, true);
			addQueryParameter(url, (isEmail ? Query.MCODE : Query.USER_ID), mcode, isFirst);
		}
		else {
			addQueryParameter(url, (isEmail ? Query.EMAIL : Query.USER_ID), identifier, isFirst);
		}
		isFirst = false;
		addQueryParameter(url, Query.FORMAT.toLowerCase(), Format.FULL.getValue(), isFirst);
		addQueryParameter(url, Query.LABELS.toLowerCase(), "true", isFirst);
		String lang = ((StringUtils.isNotEmpty(locale)) ? locale : Locale.getDefault().getLanguage());
		addQueryParameter(url, Query.LOCALE.toLowerCase(), lang, isFirst);
		return url.toString();
	}

	/**
	 * Note: Currently there is only admin access API for roles
	 */
	public String getProfileRolesUrl(String userid) {
		return getProfileRolesUrl(userid, false);
	}
	public String getProfileAdminRolesUrl(String userid) {
		return getProfileRolesUrl(userid, true);
	}
	/**
	 * @param userid
	 * @param isAdminAPI
	 * @param isFullFormat
	 * @return
	 */
	private String getProfileRolesUrl(String userid, boolean isAdminAPI) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		if (isAdminAPI)
			addPathParameter(url, Path.ADMIN);
//		else
//			throw new NotImplementedException("getProfileRolesUrl MUST be called by an admin user");
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.ROLES);

		return url.toString();
	}

	public String getProfileEntryUrl() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.PROFILE_ENTRY);
		return url.toString();
	}

	public String getProfileEntryUrl(String lookupParam, String value)
	{
		StringBuilder builder = new StringBuilder(getProfileEntryUrl());
		URLBuilder.addQueryParameter(builder, lookupParam, value, true);
		return builder.toString();
	}

	public String getProfilesAdminProfilesUrl() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.ADMIN);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.PROFILES);
		return url.toString();
	}

	public String getProfilesAdminProfileEntryUrl() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.ADMIN);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.PROFILE_ENTRY);
		return url.toString();
	}

	public String getProfilesAdminProfileEntryUrl(String lookupParam, String value)
	{
		StringBuilder builder = new StringBuilder(getProfilesAdminProfileEntryUrl());
		URLBuilder.addQueryParameter(builder, lookupParam, value, true);
		return builder.toString();
	}
	
	public String getProfilesAdminCodesUrl(String codeType, String codeId) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.ADMIN);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.CODES);
		addPathParameter(url, codeType);

		if (codeId != null) {
			url.append("?");
			url.append(ApiConstants.AdminConstants.CODE_ID);		
			url.append("=");
			url.append(codeId);			
		}
		
		return url.toString();
	}

	public String getProfilesAdminConnectionEditUrl(String connectionId) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.ADMIN);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.CONNECTION);
		
		if(null != connectionId){
			addQueryParameter(url, Query.CONNECTION_ID, connectionId, true);
		}
		return url.toString();
	}

	public String getProfilesAdminConnectionsUrl(String connectionId, String action, String targetId, String sourceId) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.ADMIN);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.CONNECTIONS);
		
		boolean isFirst = true;
		
		if(null != connectionId){
			addQueryParameter(url, Query.CONNECTION_ID, connectionId, isFirst);
			isFirst = false;
		}
		
		if(null != action){
			addQueryParameter(url, Query.ACTION.toLowerCase(), action, isFirst);
			isFirst = false;
		}
		
		if(null != sourceId){
			addQueryParameter(url, Query.SOURCE_USERID, sourceId, isFirst);
			isFirst = false;
		}
		
		if(null != targetId){
			addQueryParameter(url, Query.TARGET_USERID, targetId, isFirst);
			isFirst = false;
		}
		
		return url.toString();
	}

	public String getProfilesAdminFollowingUrl(String action, String sourceId, String targetId) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.ADMIN);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.FOLLOWING);
		
		boolean isFirst = true;
		
		if(null != action){
			addQueryParameter(url, Query.ACTION.toLowerCase(), action, isFirst);
			isFirst = false;
		}
		
		if(null != sourceId){
			addQueryParameter(url, Query.SOURCE_USERID, sourceId, isFirst);
			isFirst = false;
		}
		
		if(null != targetId){
			addQueryParameter(url, Query.TARGET_USERID, targetId, isFirst);
			isFirst = false;
		}
		
		return url.toString();
	}

	// ..., "PROFILES", "PROFILE", ...
	public String getProfilesAdminBatchFollowingUrl(String source, String type, String resourceId, boolean isFollow)
	{
		// HTTP POST request to <service>/follow/atom/resources?source=<source>&type=<type>&resource=<resourceId>
		// (where <source> is the application owning the resource, and <type> is the type of resource and <resourceId> is the resource to follow)
		// profiles/follow/atom/resources?source=PROFILES&type=PROFILE&resource=20061661
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.FOLLOW);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.RESOURCES);

		boolean isFirst = true;
		if (null != source){
			addQueryParameter(url, Query.SOURCE, source, isFirst);
			isFirst = false;
		}
		if (null != type){
			addQueryParameter(url, Query.TYPE, type, isFirst);
			isFirst = false;
		}
		if (null != resourceId){
			addQueryParameter(url, Query.RESOURCE, resourceId, isFirst);
			isFirst = false;
		}
		return url.toString();
	}

	public String getProfilesBoardEntriesUrl() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.MESSAGE_VECTOR);
		addPathParameter(url, Path.BOARD);
		addPathParameter(url, Path.BOARD_ENTRIES);
		return url.toString();
	}

	public String getAllStatusMessagesUrl() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.MESSAGE_VECTOR);
		addPathParameter(url, Path.BOARD);
		addPathParameter(url, Path.ENTRIES);
		addPathParameter(url, Path.ALL);
		return url.toString();
	}

	public String getEntryCommentsUrl() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.MESSAGE_VECTOR);
		addPathParameter(url, Path.BOARD);
		addPathParameter(url, Path.COMMENTS);
		return url.toString();
	}

	public String getConnectionEntryUrl(String sourceId, String targetId, String connectionType, boolean inclMessage) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.CONNECTION);
		addQueryParameter(url, Query.CONNECTION_TYPE, connectionType, true);
		addQueryParameter(url, Query.SOURCE_USERID, sourceId, false);
		addQueryParameter(url, Query.TARGET_USERID, targetId, false);
		addQueryParameter(url, Query.INCLUDE_MESSAGE, String.valueOf(inclMessage), false);
		return url.toString();
	}
	
	public String getColleagueEntriesUrl() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.MESSAGE_VECTOR);
		addPathParameter(url, Path.BOARD);
		addPathParameter(url, Path.ENTRIES);
		addPathParameter(url, Path.RELATED);
		return url.toString();
	}

	public String getNetworkFeedUrl(String sourceId, String connectionType, boolean inclMessage, boolean isAdmin) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		if (isAdmin) {
			addPathParameter(url, Path.ADMIN);
		}
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.CONNECTIONS);
		addQueryParameter(url, Query.CONNECTION_TYPE, connectionType, true);
		addQueryParameter(url, Query.USER_ID, sourceId, false);
		addQueryParameter(url, Query.INCLUDE_MESSAGE, String.valueOf(inclMessage), false);
		return url.toString();
	}
	
	public String getVerifyColleaguesUrl() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.CONNECTION);
		return url.toString();
	}

	public String getVerifyColleaguesUrl(String sourceId, String targetId) {
		return getConnectionEntryUrl(sourceId, targetId, Query.COLLEAGUE, false);
	}

	public String getStatusOfListOfPeopleUrl() {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.MESSAGE_VECTOR);
		addPathParameter(url, Path.BOARD);
		addPathParameter(url, Path.ALL);
		return url.toString();
	}

	public String getProfilesServiceDocument(String identifier, boolean isEmail) {
		StringBuilder url = new StringBuilder(getProfilesServiceDocument());
		addQueryParameter(url, isEmail ? Query.EMAIL : Query.USER_ID, identifier, true);
		return url.toString();
	}

	public String getProfilesSearch(SearchByCriteriaMatchParameters params) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.SEARCH);
		addQueryParameters(url, params);
		return url.toString();
	}

	public String getProfilesPeopleManaged(String idParamName, String idParamValue, int ps, int pageNumber) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.PEOPLE_MANAGED);

		boolean first = true;
		if (null != idParamName) {
			addQueryParameter(url, idParamName, idParamValue, first);
			first = false;
		}

		if (-1 < ps) {
			addQueryParameter(url, "ps", String.valueOf(ps), first);
			first = false;
		}

		if (-1 < pageNumber) {
			addQueryParameter(url, "page", String.valueOf(pageNumber), first);
			// first = false;
		}

		return url.toString();
	}
	
	public String getProfilesReportingChain(String idParamName, String idParamValue, int ps, int pageNumber) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.REPORTING_CHAIN);

		boolean first = true;
		if (null != idParamName) {
			addQueryParameter(url, idParamName, idParamValue, first);
			first = false;
		}

		if (-1 < ps) {
			addQueryParameter(url, "ps", String.valueOf(ps), first);
			first = false;
		}

		if (-1 < pageNumber) {
			addQueryParameter(url, "page", String.valueOf(pageNumber), first);
			// first = false;
		}

		return url.toString();
	}
	
	public String getProfilesOrgRelation(String idParamName, String idParamValue, boolean traverseTop) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.ORG_RELATION);

		boolean first = true;
		if (null != idParamName) {
			addQueryParameter(url, idParamName, idParamValue, first);
			first = false;
		}
		
		addQueryParameter(url, "traverseTop", Boolean.toString(traverseTop), first); 
		
		return url.toString();
	}

	public String getProfileType(String type) {
		return getProfileType(true, type);
	}

	public String getProfileType(boolean isHttps, String type) {
		StringBuilder url = new StringBuilder(128);

		if (isHttps)
			url.append(serverURL);
		else
			url.append(serverURLHttp);

		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.TYPE);
		if (null != type) addQueryParameter(url, Query.TYPE, type, true);
		return url.toString();
	}

	public String getConnectionTypeConfig() {
		StringBuilder url = new StringBuilder(128);
		url.append(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.CONNECTION_TYPE_CONFIG);
		return url.toString();
	}
	
	public String getTagsConfig() {
		StringBuilder url = new StringBuilder(128);
		url.append(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.TAGS_CONFIG);
		return url.toString();		
	}
	
	public String getProfilesSeedlist() {
		StringBuilder url = new StringBuilder(128);
		url.append(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.SEEDLIST);
		addPathParameter(url, Path.MYSERVER);
		return url.toString();
	}

	/**
	 * Retrieve a profiles seedlist URL
	 * 
	 * @param range
	 *            the number of items to fetch in this document
	 * @param timestamp
	 *            the timestamp to begin crawling from
	 * @return
	 */
	public String getProfilesSeedlist(int range, String timestamp) {
		StringBuilder url = new StringBuilder(128);
		url.append(getProfilesSeedlist());
		addQueryParameter(url, Query.FORMAT, "atom", true);
		addQueryParameter(url, Query.LOCALE, "en", false);
		addQueryParameter(url, Query.RANGE, "" + range, false);
		addQueryParameter(url, Query.ACTION, "GetDocuments", false);

		if (timestamp != null) {
			addQueryParameter(url, Query.TIMESTAMP, timestamp, false);
		}

		return url.toString();
	}
	
	public String getTagTypeAhead(String tag, String type) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.API);
		addPathParameter(url, Path.TAG_TYPEAHEAD);
		addQueryParameter(url, "tag", tag, true);
		if (type != null && type.length() > 0) {
			addQueryParameter(url, "type", type, false);
		}
		return url.toString();
	}
	
	public StringBuilder getImageUrl(String paramName, String paramValue) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, Path.APP);
		addPathParameter(url, Path.PHOTO);
		if (StringUtils.isNotBlank(paramName))
//		if (null != paramName && !"".equals(paramName))
			addQueryParameter(url, paramName, paramValue, true);
		return url;
	}
		
	/**
	 * update or add <code>lastMod</code> query param to avoid getting cached results
	 * 
	 * @param urlParam
	 * @return
	 * @throws Exception
	 */
	public static String updateLastMod(String urlParam) throws Exception {
		return updateQueryParameter(urlParam, Query.LAST_MOD, Long.toString(System.currentTimeMillis()));
	}
	
	/**
	 * add or update a query parameter. Send newParamValue=null to remove a parameter.
	 * @param urlParam
	 * @param paramName
	 * @param newParamValue
	 * @return
	 * @throws Exception
	 */
	public static String updateQueryParameter(String urlParam, String paramName, String newParamValue) throws Exception {
		StringBuilder retval = new StringBuilder(urlParam.length());
		
		if (-1 != urlParam.indexOf("?")) {
			String[] urlParts = urlParam.split("\\?");
			retval.append(urlParts[0]);

			String[] queryParams = urlParts[1].split("&");

			boolean first = true;
			for (String s : queryParams) {
				if (!s.startsWith(paramName)) {
					if (first) {
						first = false;
						retval.append("?");
					} else {
						retval.append("&");
					}
					retval.append(s);
				}
			}

			if (first) {
				first = false;
				retval.append("?");
			} else {
				retval.append("&");
			}
		} else {
			retval.append(urlParam);
			retval.append("?");
		}

		if (null != newParamValue) {
			retval.append(paramName + "=" + newParamValue);
		}

		return retval.toString();
	}

	private void addPathParameter(StringBuilder url, String paramValue) {
		url.append("/");
		url.append(paramValue);
	}

	public static String addOauth(String url) {
		StringBuilder sb = new StringBuilder(url);
		String s1 = "/profiles/atom";
		String s2 = "/profiles/oauth/atom";
		int ind = sb.indexOf(s1);
		if (ind != -1) {
			sb.replace(ind, ind + s1.length(), s2);
		}
		return sb.toString();
	}

	public final static void addQueryParameters(StringBuilder url, AbstractParameters params) {
		boolean isFirst = true;
		Map<String, String> queryParams = params.getParameters();
		for (String key : queryParams.keySet()) {
			addQueryParameter(url, key, queryParams.get(key), isFirst);
			isFirst = false;
		}
	}

	public final static String addQueryParameter(String url, String paramName, String paramValue, boolean isFirst) {
		StringBuilder sb = new StringBuilder(url);
		addQueryParameter(sb, paramName, paramValue, isFirst);
		return sb.toString();
	}

	@SuppressWarnings("deprecation")
	public final static void addQueryParameter(StringBuilder builder, String paramName, String paramValue, boolean isFirst) {
		if (isFirst) {
			builder.append("?");
		}
		else {
			builder.append("&");
		}

		builder.append(paramName);
		builder.append("=");
		String useValue = (StringUtils.isBlank(paramValue) ? "" : paramValue); 
		builder.append(URLEncoder.encode(useValue));
	}
	
	public static String removeAllParameters(String url)
	{
		return url.substring(0, url.indexOf('?'));
	}

	public String getServiceConfigDocument(String serviceName) {
		StringBuilder url = new StringBuilder(serverURL);
		addPathParameter(url, serviceName);
		addPathParameter(url, Path.SERVICE_CONFIGS);
		return url.toString();
	}
	
}
