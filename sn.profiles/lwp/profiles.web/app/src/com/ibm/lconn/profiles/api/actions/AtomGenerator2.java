/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.abdera.model.Content;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Text;
import org.apache.abdera.writer.StreamWriter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;

import com.ibm.json.java.JSONObject;

import com.ibm.lconn.core.appext.msgvector.atom.api.MessageVectorAtomConstants;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessage;
import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.lconn.core.web.atom.util.LCAtomConstants;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.RoleCollection;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.lconn.profiles.internal.util.UrlSubstituter;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionCollection;
import com.ibm.peoplepages.data.DatabaseRecord;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 * @author colleen
 * @author ahernm@us.ibm.com
 */
public final class AtomGenerator2
{
	private static final Log LOG = LogFactory.getLog(AtomGenerator2.class);

	protected HttpServletRequest request;
	protected StreamWriter sw;
	protected String searchType;
	protected boolean isLite = true;
	protected boolean isOauth = false;	
	protected String outputType;
	protected String lastKey;

	public static final String NS_APP = "http://www.w3.org/2007/app";
	private final static String NS = "http://www.ibm.com/xmlns/prod/sn";
	private final static String TYPE_SCHEME = "http://www.ibm.com/xmlns/prod/sn/type";
	private final static String CONNECTION_TYPE_SCHEME = "http://www.ibm.com/xmlns/prod/sn/connection/type";
	private final static String STATUS_SCHEME = "http://www.ibm.com/xmlns/prod/sn/status";
	private final static String PFX = "snx";
	private final static String PENDING_INVITATIONS = "pendingInvitations";

	private final String kAtomMimeType = "application/atom+xml";
	private final String kTextMimeType = "text/directory";
	private final String kHtmlMimeType = "text/html";
	private final static String PROFILE_TYPE = "profile";
	private final static String CONNECTION_TYPE = "connection";
	private final static String COLLEAGUE_TYPE = "colleague";

	public final static String FEED_ID = "tag:profiles.ibm.com,2006:feed";
	public final static String CODES_FEED_ID = "tag:profiles.ibm.com,2006:com.ibm.snx_profiles.codes.";
	public final static String ENTRY_ID = "tag:profiles.ibm.com,2006:entry";

	public static final String OPENSEARCH_NS = "http://a9.com/-/spec/opensearch/1.1/";
	public static final String OPENSEARCH_PREFIX = "opensearch";
	public static final String START_INDEX = "startIndex";
	public static final String ITEMS_PER_PAGE = "itemsPerPage";
	public static final String TOTAL_RESULTS = "totalResults";
	public static final String FIRST = "first";
	public static final String PREVIOUS = "previous";
	public static final String NEXT = "next";
	public static final String LAST = "last";
	public static final String EDIT = "edit";
	public static final String IMAGE = "image";
	public static final String AUDIO = "audio";
	public static final String TYPE = "type";
	public static final String REF = "ref";
	public static final String SNX_REL_SOURCE = "http://www.ibm.com/xmlns/prod/sn/connection/source";
	public static final String SNX_REL_TARGET = "http://www.ibm.com/xmlns/prod/sn/connection/target";
	
	public static final String NS_PREFIX_OPENSEARCH = "opensearch";
	public static final String NS_PREFIX_THREAD = "thr";
	public static final String NS_PREFIX_SNX = "snx";
	public static final String NS_PREFIX_FH = "fh";
	public static final String NS_PREFIX_APP = "app";
	public static final String NS_PREFIX_OPENSOCIAL = "opensocial";
	
	public static final QName QN_START_INDEX = new QName(OPENSEARCH_NS, START_INDEX, OPENSEARCH_PREFIX);
	public static final QName QN_ITEMS_PER_PAGE = new QName(OPENSEARCH_NS, ITEMS_PER_PAGE, OPENSEARCH_PREFIX);
	public static final QName QN_TOTAL_RESULTS = new QName(OPENSEARCH_NS, TOTAL_RESULTS, OPENSEARCH_PREFIX);

	public static final QName QN_USERID = new QName(NS, PeoplePagesServiceConstants.USER_ID, PFX);
	public static final QName QN_USERSTATE = new QName(NS, "userState", PFX);
	public static final QName QN_MCODE = new QName(NS, "mcode", PFX);
	public static final QName QN_ISEXTERNAL = new QName(NS, "isExternal", PFX);
	public static final QName QN_SNX_REL = new QName(NS, "rel", PFX);
	public static final QName QN_CONNECTION = new QName(NS, "connection", PFX);
	public static final QName QN_SNX_CONNECTION = new QName(NS, "connection", PFX);

	public static final QName QN_SNX_ROLE = new QName(NS, "role", PFX);

	public static final QName QN_STATUS = new QName(NS, "status");
	public static final QName QN_ASOF = new QName(NS, "asof");
	public static final QName QN_MESSAGE = new QName(NS, "message");
	
	public static final String IMAGE_LINK_REL = "http://www.ibm.com/xmlns/prod/sn/image";
	public static final String PRONUNCIATION_LINK_REL = "http://www.ibm.com/xmlns/prod/sn/pronunciation";
	public static final String PROFILE_TYPE_LINK_REL = "http://www.ibm.com/xmlns/prod/sn/profile-type";

	public static final String OPENSOCIAL_NS = AtomConstants.NS_OPENSOCIAL;
	
	public static final String FH_NS="http://purl.org/syndication/history/1.0";
	public static final String FH_PREFIX="fh";
	public static final String FH_COMPLETE="complete";
	
	public static final String THR_NS = "http://purl.org/syndication/thread/1.0";
	public static final String THR_PREFIX = "thr";
	public static final String THR_IN_REPLY_TO = "in-reply-to";
	public static final QName THR_QN_IN_REPLY_TO = new QName(THR_NS, THR_IN_REPLY_TO); //, THR_PREFIX);

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	boolean allowEmailInReturn = LCConfig.instance().isEmailReturned();
	boolean outputConnection = false;
	boolean connectionsInCommonSearch = false;
	private boolean inclStatus = false;
	private boolean inclLabels = false;
	private String  lang = null;
	private Locale locale = null;

	public boolean isInclStatus() {
		return inclStatus;
	}

	public void setInclStatus(boolean inclStatus) {
		this.inclStatus = inclStatus;
	}

	public void setLastKey(String lastKey) {
		this.lastKey = lastKey;
	}

	public void setOauth(boolean oauth) {
		this.isOauth = oauth;
	}

	public boolean isInclLabels() {
		return inclLabels;
	}

	public void setInclLabels(boolean inclLabels) {
		this.inclLabels = inclLabels;
	}

	public String getLanguage() {
		return lang;
	}

	public void setLanguage(String lang) {
		this.lang = lang;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	private ResourceBundleHelper helper;

	public AtomGenerator2(HttpServletRequest req, Writer out)
	{
		request = req;
		sw = AtomConstants.writerFactory.newStreamWriter();
		sw.setWriter(out);
		helper = new ResourceBundleHelper(ResourceManager.getBundle(req));
	}

	public AtomGenerator2(HttpServletRequest req, Writer out, boolean lite, String output)
	{
		request = req;
		isLite = lite;
		outputType = output;
		sw = AtomConstants.writerFactory.newStreamWriter();
		sw.setWriter(out);
		if (PeoplePagesServiceConstants.CONNECTION.equals(output)) {
			outputConnection = true;
		}
		helper = new ResourceBundleHelper(ResourceManager.getBundle(req));		
	}

	public void transform(SearchResultsPage<?> resultsPage, String searchType, boolean entryOnly) throws Exception
	{
		this.searchType = searchType;
		
		if (PeoplePagesServiceConstants.CONNECTIONS_IN_COMMON.equals(searchType)) {
			connectionsInCommonSearch = true;
		}
		
		try
		{
			if (entryOnly)
			{
				generateAtomEntryDocument(resultsPage);
			}
			else
			{
				generateAtomFeed(resultsPage);
			}
		}
		finally
		{
			sw.close();
		}
	}

	public void transform(SearchResultsPage<?> resultsPage, String type) throws Exception
	{
		transform(resultsPage, type, false);
	}

	public static final String buildStatusString(int statusType)
	{
		switch (statusType)
		{
			case Connection.StatusType.PENDING:
				return "pending";
			case Connection.StatusType.UNCONFIRMED:
				return "unconfirmed";
			case Connection.StatusType.ACCEPTED:
			default:
				return "accepted";
		}
	}
	
	public static final void writeProfileLinks(StreamWriter sw, HttpServletRequest request, Employee profile, 
			boolean isSecure, boolean isOauth) throws UnsupportedEncodingException {
		Date profileLastMod = profile.getRecordUpdated();
		
		String profilesUrl = FeedUtils.getProfilesURL(request);

		// be sure to add new links to both clauses of if/else
		if (isOauth) {
			// link to tag cloud
			if ( PolicyHelper.isFeatureEnabled(Feature.TAG, profile ) ) {
			    sw.writeLink(FeedUtils.modUrlForOauth(
					 FeedUtils.calculateTagCloudUrl(profile.getKey(), profilesUrl, profileLastMod)),
					 AtomConstants.LINK_REL_TAG_CLOUD, AtomConstants.APP_MIME_TYPE);
			}
	
			// links to the board - legacy board not supported in MT/Cloud. we need to start applying the deprecation of the board feature
			if (LCConfig.instance().isMTEnvironment() == false) {
				if (PolicyHelper.isFeatureEnabled(Feature.BOARD, profile)) {
					sw.writeLink(FeedUtils.modUrlForOauth(FeedUtils.calculateTheBoardUrl(profile.getKey(), profilesUrl, profileLastMod)),
							AtomConstants.LINK_REL_THEBOARD, AtomConstants.ATOM_MIME_TYPE);
				}

				// link to status
				if (PolicyHelper.isFeatureEnabled(Feature.STATUS, profile)) {
					sw.writeLink(FeedUtils.modUrlForOauth(FeedUtils.calculateStatusUrl(profile.getKey(), profilesUrl, profileLastMod)),
							AtomConstants.LINK_REL_STATUS, AtomConstants.ATOM_MIME_TYPE);
				}
			}
			else{
				// inserted by 89568 ... enable when News/Homepage respond with an endpoint for status
				// homepage/activity stream status board
				sw.writeLink(FeedUtils.calculateOpenSocialStatusUrl(
						profile.getGuid(),
						FeedUtils.getServiceURL(request, ServiceReferenceUtil.Service.OPENSOCIAL, FeedUtils.OPENSOCIAL_URL_KEY),
						profileLastMod),
						AtomConstants.LINK_REL_STATUS_OPENSOCIAL, AtomConstants.JSON_CONTENT_TYPE);
				// my status updates 'rolled up' as per activity stream docs. the first entry is the latest status
				sw.writeLink(FeedUtils.calculateOpenSocialStatusUpdatesUrl(
						profile.getGuid(),
						FeedUtils.getServiceURL(request, ServiceReferenceUtil.Service.OPENSOCIAL, FeedUtils.OPENSOCIAL_URL_KEY),
						profileLastMod),
						AtomConstants.LINK_REL_STATUS_OPENSOCIAL, AtomConstants.JSON_CONTENT_TYPE);
			}
	
			// link to colleagues
			if ( PolicyHelper.isFeatureEnabled(Feature.COLLEAGUE, profile ) ) {
				// TODO should we always output extension links or should their acl be based on profileColleague
			    Map<String, ? extends ConnectionTypeConfig> config = ProfilesConfig.instance().getDMConfig().getConnectionTypeConfigs();
			    for (String connectionType : config.keySet()) {
			    	ConnectionTypeConfig ctc = config.get(connectionType);
			    	// output the link
				    sw.writeLink(FeedUtils.modUrlForOauth(
							 FeedUtils.calculateConnectionsUrl(profile.getKey(), profilesUrl, ctc.getType(), profileLastMod)),
							 buildLinkRelationForConnectionType(ctc), AtomConstants.ATOM_MIME_TYPE);			    	
			    }
			}
	
			// link to reporting struct
			if ( PolicyHelper.isFeatureEnabled(Feature.REPORT_TO, profile ) ) {		
			    sw.writeLink(FeedUtils.modUrlForOauth(
					 FeedUtils.calculateReportingStructUrl(profile.getKey(), profilesUrl, profileLastMod)),
					 AtomConstants.LINK_REL_REPORTING_STRUCT, AtomConstants.ATOM_MIME_TYPE);
			}
	
			// link to people managed
			if ( PolicyHelper.isFeatureEnabled(Feature.PEOPLE_MANAGED, profile ) ) {
			    if ("Y".equalsIgnoreCase(profile.getIsManager()))
				sw.writeLink(FeedUtils.modUrlForOauth(
					     FeedUtils.calculatePeopleManagedUrl(profile.getKey(), profilesUrl, profileLastMod)),
					     AtomConstants.LINK_REL_PEOPLE_MANAGED, AtomConstants.ATOM_MIME_TYPE);
			}
	
			// link to profileType
			sw.writeLink(FeedUtils.modUrlForOauth(FeedUtils.calculateProfileTypeEntryURL(profile.getProfileType(), profilesUrl)),
					PROFILE_TYPE_LINK_REL, AtomConstants.PROFILE_TYPE_CONTENT_TYPE);
	
			// links to extension attributes
			for (ExtensionAttributeConfig eac : DMConfig.instance().getExtensionAttributeConfig().values())
			{
				sw.startLink(FeedUtils.modUrlForOauth(
					FeedUtils.calculateProfileExtensionUrl(profile.getKey(), eac.getExtensionId(), profilesUrl, profile.getRecordUpdated())),
					AtomConstants.LINK_REL_EXTENSION_ATTRIBUTE, eac.getMimeType());
				sw.writeAttribute(AtomConstants.QN_EXTENSION_ID, eac.getExtensionId());
				sw.endLink();
			}
		}
		else {
			// link to tag cloud
			if ( PolicyHelper.isFeatureEnabled(Feature.TAG, profile ) ) {
			    sw.writeLink(
					 FeedUtils.calculateTagCloudUrl(profile.getKey(), profilesUrl, profileLastMod),
					 AtomConstants.LINK_REL_TAG_CLOUD, AtomConstants.APP_MIME_TYPE);
			}

			// links to the board - legacy board not supported in MT/Cloud. we need to start applying the deprecation of the board feature
			if (LCConfig.instance().isMTEnvironment() == false) {
				if (PolicyHelper.isFeatureEnabled(Feature.BOARD, profile)) {
					sw.writeLink(FeedUtils.calculateTheBoardUrl(profile.getKey(), profilesUrl, profileLastMod),
							AtomConstants.LINK_REL_THEBOARD, AtomConstants.ATOM_MIME_TYPE);

					sw.writeLink(FeedUtils.calculateOpenSocialBoardUrl(profile.getGuid(),
							FeedUtils.getServiceURL(request, ServiceReferenceUtil.Service.OPENSOCIAL, FeedUtils.OPENSOCIAL_URL_KEY),
							profileLastMod), AtomConstants.LINK_REL_THEBOARD_OPENSOCIAL, AtomConstants.ATOM_MIME_TYPE);
				}

				// link to status
				if (PolicyHelper.isFeatureEnabled(Feature.STATUS, profile)) {
					sw.writeLink(FeedUtils.calculateStatusUrl(profile.getKey(), profilesUrl, profileLastMod),
							AtomConstants.LINK_REL_STATUS, AtomConstants.ATOM_MIME_TYPE);
				}
			}
			else{
				// inserted by 89568 ... enable when News/Homepage respond with an endpoint for status
				// homepage/activity stream status board
				sw.writeLink(FeedUtils.calculateOpenSocialStatusUrl(
						profile.getGuid(),
						FeedUtils.getServiceURL(request, ServiceReferenceUtil.Service.OPENSOCIAL, FeedUtils.OPENSOCIAL_URL_KEY),
						profileLastMod),
						AtomConstants.LINK_REL_STATUS_OPENSOCIAL, AtomConstants.JSON_CONTENT_TYPE);
				// my status updates 'rolled up' as per activity stream docs. the first entry is the latest status
				sw.writeLink(FeedUtils.calculateOpenSocialStatusUpdatesUrl(
						profile.getGuid(),
						FeedUtils.getServiceURL(request, ServiceReferenceUtil.Service.OPENSOCIAL, FeedUtils.OPENSOCIAL_URL_KEY),
						profileLastMod),
						AtomConstants.LINK_REL_STATUS_OPENSOCIAL, AtomConstants.JSON_CONTENT_TYPE);
			}
			// link to colleagues
			if ( PolicyHelper.isFeatureEnabled(Feature.COLLEAGUE, profile ) ) {
				// TODO should we always output extension links or should their acl be based on profileColleague
			    Map<String, ? extends ConnectionTypeConfig> config = ProfilesConfig.instance().getDMConfig().getConnectionTypeConfigs();
			    for (String connectionType : config.keySet()) {
			    	ConnectionTypeConfig ctc = config.get(connectionType);
			    	// output the link
				    sw.writeLink(FeedUtils.calculateConnectionsUrl(profile.getKey(), profilesUrl, ctc.getType(), profileLastMod),
							 buildLinkRelationForConnectionType(ctc), AtomConstants.ATOM_MIME_TYPE);			    	
			    }
			    
			}
			// link to reporting struct
			if ( PolicyHelper.isFeatureEnabled(Feature.REPORT_TO, profile ) ) {		
			    sw.writeLink(
					 FeedUtils.calculateReportingStructUrl(profile.getKey(), profilesUrl, profileLastMod),
					 AtomConstants.LINK_REL_REPORTING_STRUCT, AtomConstants.ATOM_MIME_TYPE);
			}
			// link to people managed
			if ( PolicyHelper.isFeatureEnabled(Feature.PEOPLE_MANAGED, profile ) ) {
			    if ("Y".equalsIgnoreCase(profile.getIsManager()))
				sw.writeLink(
					     FeedUtils.calculatePeopleManagedUrl(profile.getKey(), profilesUrl, profileLastMod),
					     AtomConstants.LINK_REL_PEOPLE_MANAGED, AtomConstants.ATOM_MIME_TYPE);
			}
	
			// link to profileType
			sw.writeLink(FeedUtils.calculateProfileTypeEntryURL(profile.getProfileType(), profilesUrl), PROFILE_TYPE_LINK_REL,
					AtomConstants.PROFILE_TYPE_CONTENT_TYPE);

			// links to extension attributes
			for (ExtensionAttributeConfig eac : DMConfig.instance().getExtensionAttributeConfig().values())
			{
				sw.startLink(
					FeedUtils.calculateProfileExtensionUrl(profile.getKey(), eac.getExtensionId(), profilesUrl, profile.getRecordUpdated()),
					AtomConstants.LINK_REL_EXTENSION_ATTRIBUTE, eac.getMimeType());
				sw.writeAttribute(AtomConstants.QN_EXTENSION_ID, eac.getExtensionId());
				sw.endLink();
			}		
		}
	}
	
	public static final String buildLinkRelationForConnectionType(ConnectionTypeConfig ctc) {
    	StringBuilder linkRel = new StringBuilder();
    	String baseLinkRel = ctc.isExtension() ? AtomConstants.LINK_REL_CONNECTIONS_EXTENSION : AtomConstants.LINK_REL_CONNECTIONS;
    	linkRel.append(baseLinkRel);
    	linkRel.append("/");
    	linkRel.append(ctc.getType());
    	return linkRel.toString();
	}
	
	/**
	 * Outputs links to dogear, activities, etc
	 * 
	 * @param sw
	 * @param profile
	 * @param isSecure
	 */
	public static final void writeServiceLinks(StreamWriter sw, Employee profile, boolean isSecure) throws Exception{
		Map<String,String> profileSubMap = UrlSubstituter.toSubMap(profile);
		for (ServiceReferenceUtil ref : ServiceReferenceUtil.getPersonCardServiceRefs().values()) {
			String href = 
				ref.getServiceLink(isSecure) +
				UrlSubstituter.resolve(ref.getUrlPattern(), profileSubMap, isSecure);
			
			href = URLEncoder.encode(href,"UTF-8");
			sw.startLink(
					href, AtomConstants.LINK_REL_SERVICE + ref.getServiceName(),
					"text/html");
			sw.endLink();
		}
	}
	
	private <SRP extends SearchResultsPage<?>> void generateAtomFeed(SRP resultsPage) throws Exception
	{
		List<?> results = resultsPage.getResults();
		try
		{
			setNamespace();
			startDocument();
			
			sw.startFeed();
			writeNamespaceInfo();
			
			sw.writeId(getId());
			sw.writeGenerator(AtomConstants.GENERATOR_VERSION, NS, AtomConstants.GENERATOR_NAME); // Not translated
			sw.writeTitle(getFeedTitle());
			sw.writeAuthor(helper.getString("lc.author"));
			sw.writeUpdated(new Date());
			
			//if the results are paged
			if (searchType.equals(PeoplePagesServiceConstants.ADMIN_EMPLOYEE) )
			{
				sw.startElement(QN_ITEMS_PER_PAGE);
				sw.writeElementText(resultsPage.getPageSize());
				sw.endElement();
				
				if ((resultsPage.getResults().size() != 1) && (lastKey != null)) {
					JSONObject obj = new JSONObject();
				    obj.put("ver", "1");
				    obj.put("lastKey", lastKey);
				    String jsonStr = obj.serialize(true);
				    String encStr = AdminProfilesAction.urlEncodeBase64(Base64.encode(jsonStr.getBytes()));

				    // Only putting 'iterState', 'ps' and maybe 'lang' on URL; so ignoring full query string and generating by hand
				    String urlMinusPage = request.getRequestURL().toString();
				    
					String url = urlMinusPage 
						+ (urlMinusPage.contains("?") ? "&" : "?") 
						+ PeoplePagesServiceConstants.ITER_STATE + "=" + encStr
						+ "&" + PeoplePagesServiceConstants.PAGE_SIZE + "=" + resultsPage.getPageSize()
						+ (StringUtils.isNotEmpty(request.getParameter("lang")) ? "&lang=" + request.getParameter("lang") : "");
					sw.writeLink(url, NEXT, null);
				}
				
			}
			else if (searchType.equals("search") || 
				searchType.equals(PeoplePagesServiceConstants.CONNECTIONS) || 
				searchType.equals(PeoplePagesServiceConstants.CONNECTIONS_IN_COMMON))
			{
				writePagingInfo(resultsPage);
			}
			else
			{
				if (searchType.equals(PeoplePagesServiceConstants.PEOPLE_MANAGED) || searchType.equals(PeoplePagesServiceConstants.REPORTING_CHAIN))
				{
					writePagingInfo(resultsPage);
				}
				
				sw.startElement(FH_COMPLETE, FH_NS, FH_PREFIX);
				sw.endElement();
				
				if ((searchType.equals(PeoplePagesServiceConstants.GUID)
					 || searchType.equals(PeoplePagesServiceConstants.UID)
					 || searchType.equals(PeoplePagesServiceConstants.EMAIL)
					 || searchType.equals(PeoplePagesServiceConstants.KEY)
					 || searchType.equals(PeoplePagesServiceConstants.USER_ID))
					 &&
					(resultsPage.getResults().size() == 1 && resultsPage.getResults().get(0) instanceof Employee))
				{
					Employee userRecord = (Employee) resultsPage.getResults().get(0);
					
					AtomGenerator2.writeProfileLinks(sw, request, userRecord, request.isSecure(), isOauth);
				}
				
			}

			if (ConnectionCollection.class.isInstance(resultsPage))
			{
				ConnectionCollection cc = (ConnectionCollection)resultsPage;
				if (cc.getPendingInvitations() > -1)
				{
					sw.startElement(new QName(NS, PENDING_INVITATIONS, PFX));
					sw.writeElementText(cc.getPendingInvitations());
					sw.endElement();
				}
			}

			if (APIHelper.isCodeSearchType(searchType)) {
					AtomGenerator3 xmlGen = new AtomGenerator3(sw, FeedUtils.getProfilesURL(request));
					for (int i = 0, imax = results.size(); i < imax; i++)
					{
						xmlGen.buildXmlProfileData(results.get(i), searchType);
					}					
				}
			else if ((results.size() > 0) && (results.get(0) instanceof ProfileDescriptor)) {
				String queryString = encodeRequestQueryString();
				String feedUrl = request.getRequestURL().toString();
				if (isOauth) {
					feedUrl = FeedUtils.modUrlForOauth(feedUrl);
				}
				sw.writeLink(feedUrl + queryString, Link.REL_SELF, kAtomMimeType);
			
				for (int i = 0, imax = results.size(); i < imax; i++)
				{
					ProfileDescriptor pd = (ProfileDescriptor)results.get(i);
					if (pd != null)
					{
						generateAtomEntry(pd.getProfile(), false);
					}
				}
			}
			else
			{
				String queryString = encodeRequestQueryString();
				String feedUrl = request.getRequestURL().toString();
				if (isOauth) {
					feedUrl = FeedUtils.modUrlForOauth(feedUrl);
				}
				sw.writeLink(feedUrl + queryString, Link.REL_SELF, kAtomMimeType);
			
				for (int i = 0, imax = results.size(); i < imax; i++)
				{
					DatabaseRecord dbrec = (DatabaseRecord)results.get(i);
					if (dbrec != null)
					{
						if (!outputConnection && resultsPage instanceof ConnectionCollection)
						{
							Connection conn = (Connection)dbrec;
							dbrec = conn.getTargetProfile();
							conn.getTargetProfile().put(PeoplePagesServiceConstants.CONNECTION_ID, conn.getConnectionId());
						}
						generateAtomEntry(dbrec, false);
					}
				}
			}
			sw.endFeed();
			endDocument();
		}
		catch (MimeTypeParseException mtpe)
		{
			LOG.error(mtpe.getMessage(), mtpe);
			throw mtpe;
		}
		catch (UnsupportedEncodingException uee)
		{
			LOG.error(uee.getMessage(), uee);
			throw uee;
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage(), e);
			throw e;
		}
	}

	private void startDocument()
	{
		sw.startDocument(AtomConstants.XML_ENCODING, AtomConstants.XML_VERSION);
	}
	
	private void endDocument()
	{
		sw.endDocument();
	}
	
	private <T> void generateAtomEntryDocument(SearchResultsPage<T> resultsPage) throws Exception
	{
		startDocument();
		List<?> results = resultsPage.getResults();
		if (results != null && !results.isEmpty())
		{
			if (APIHelper.isCodeSearchType(searchType)) {
				AtomGenerator3 xmlGen = new AtomGenerator3(sw, FeedUtils.getProfilesURL(request));
				xmlGen.buildXmlProfileData(results.get(0), searchType);
			}
			else {
				DatabaseRecord dbrec = (DatabaseRecord)results.get(0);
				if (dbrec != null)
				{
					generateAtomEntry(dbrec, true);
				}
			}
		}
		endDocument();
	}

	private void generateAtomEntry(DatabaseRecord dbrec, boolean declareNs) throws Exception
	{
		if (declareNs) setNamespace();
		
		sw.startEntry();
	
		if (declareNs) writeNamespaceInfo();
	
		if (dbrec != null)
		{
			if (!connectionsInCommonSearch ||
				(connectionsInCommonSearch && (dbrec instanceof Employee)))
			{
				String recordId = URLEncoder.encode(dbrec.getRecordId(), "UTF-8");
				recordId = recordId.replaceAll("\\+", "%20");
				sw.writeId(ENTRY_ID + recordId);
			}
	
			sw.writeTitle(dbrec.getRecordTitle());
			if (dbrec.getRecordUpdated() != null) sw.writeUpdated(dbrec.getRecordUpdated());
			
			if (dbrec instanceof Connection)
			{
				Connection conn = (Connection)dbrec;
				if (connectionsInCommonSearch)
				{
					String recordId = URLEncoder.encode(conn.getTargetKey(), "UTF-8");
					recordId = recordId.replaceAll("\\+", "%20");
					sw.writeId(ENTRY_ID + ":connTarget" + recordId);
				}
				sw.writeCategory(CONNECTION_TYPE, TYPE_SCHEME);			
				sw.writeCategory(conn.getType(), CONNECTION_TYPE_SCHEME);
				sw.writeCategory(buildStatusString(conn.getStatus()), STATUS_SCHEME);
				
				writeProfileAsATOMPerson(conn.getCreatedByProfile(), false, null);
				writeProfileAsATOMPerson(conn.getLastModByProfile(), true, null);
				
				sw.startElement(QN_SNX_CONNECTION);
				writeProfileAsATOMPerson(conn.getSourceProfile(), true, SNX_REL_SOURCE);
				writeProfileAsATOMPerson(conn.getTargetProfile(), true, SNX_REL_TARGET);
				sw.endElement(); // QN_SNX_CONNECTION
				
				if (conn.getMessage() != null)
				{
					sw.writeContent(Content.Type.HTML, conn.getMessage());
				}
				else
				{
					sw.writeContent(Content.Type.HTML, "");
				}
			}

			//add tags (if available) as categories
			else if (dbrec instanceof Employee)
			{
				Employee emp = (Employee)dbrec;
				APIHelper.filterProfileAttrForAPI(emp);

				sw.writeCategory(PROFILE_TYPE, TYPE_SCHEME);

				if ( PolicyHelper.isFeatureEnabled(Feature.TAG, emp ) ) {
					List<Tag> tags = emp.getProfileTags();
				    if (tags != null && tags.size() > 0)
					{
				    	for (Tag tag : tags) {
				    		sw.startCategory(tag.getTag());
				    		sw.writeAttribute(AtomConstants.QN_TYPE, tag.getType());
				    		String scheme = AtomParser3.tagTypeToScheme(tag.getType());
				    		if (scheme != null && scheme.length() > 0) {
				    			sw.writeAttribute("scheme", scheme);
				    		}
				    		sw.endCategory();
				    	}
					}
				}

				// output service links
				if (!isLite) {
					writeServiceLinks(sw, emp, request.isSecure());
				}
					
				// Add contributor to all person records
				writeProfileAsATOMPerson(emp, true, null);
			}
			
			if ((searchType.equals("search")) && (dbrec instanceof Employee))
			{
				Employee emp = (Employee)dbrec;
				sw.writeLink(
						FeedUtils.calculateProfilesFeedURL(emp.getKey(), FeedUtils.getProfilesURL(request)),
						Link.REL_SELF,
						kAtomMimeType);
			}
			else if (searchType.equals(PeoplePagesServiceConstants.PEOPLE_MANAGED)
					|| searchType.equals(PeoplePagesServiceConstants.REPORTING_CHAIN))
			{
				//do nothing
			}
			else if (!connectionsInCommonSearch)
			{
				String selfLink = generateSelfReferenceURL(dbrec);
				
				sw.writeLink(selfLink, Link.REL_SELF, kAtomMimeType);
				if (dbrec instanceof Connection) {
					sw.writeLink(selfLink + "&inclMessage=true", Link.REL_EDIT, kAtomMimeType);
				}
			}
			else if (outputType.equals(PeoplePagesServiceConstants.MIME_TEXT_XML))
			{
				String link = generateAdminEntryURL(dbrec);
				
				//sw.writeLink(selfLink, Link.REL_SELF, kAtomMimeType);
				sw.writeLink(link, Link.REL_EDIT, kAtomMimeType);
			}

			if (dbrec instanceof Employee)
			{
				Employee emp = (Employee)dbrec;
				String profilesUrl = FeedUtils.getProfilesURL(request);
				
				// link to profile type
				if (isOauth) {
					sw.writeLink(FeedUtils.modUrlForOauth(
							FeedUtils.calculateProfileTypeEntryURL(emp.getProfileType(), profilesUrl)),
							PROFILE_TYPE_LINK_REL, AtomConstants.PROFILE_TYPE_CONTENT_TYPE);
				}
				else {
					sw.writeLink(
							FeedUtils.calculateProfileTypeEntryURL(emp.getProfileType(), profilesUrl),
							PROFILE_TYPE_LINK_REL, AtomConstants.PROFILE_TYPE_CONTENT_TYPE);
				}
				

				//link to html profile
				String empKey = emp.getKey();
				Date profileLastMod = emp.getLastUpdate();
				
				sw.writeLink(
					FeedUtils.calculateHtmlUrl2(empKey, profilesUrl),
					Link.REL_RELATED, kHtmlMimeType);
				
				// link to photo
				if ( PolicyHelper.isFeatureEnabled(Feature.PHOTO, emp ) ) {
					if (isOauth) {
						sw.startLink(
								 FeedUtils.calculateOauthPhotoUrl(empKey, profilesUrl, profileLastMod),
								 IMAGE_LINK_REL);
						
					}
					else {
						sw.startLink(
						 FeedUtils.calculatePhotoUrl2(empKey, profilesUrl, profileLastMod),
						 IMAGE_LINK_REL);
					}
				    sw.writeAttribute(TYPE, IMAGE);
				    sw.endLink();
				}

				// link to audio
				if ( PolicyHelper.isFeatureEnabled(Feature.PRONUNCIATION, emp ) ) {
				    sw.startLink(
						 FeedUtils.calculatePronunciationUrl2(empKey, profilesUrl, profileLastMod),
						 PRONUNCIATION_LINK_REL);
				    sw.writeAttribute(TYPE, AUDIO);
				    sw.endLink();
				}

				// write status
				if (!isLite) 
				{
										
				}
					
				// link to colleagueConnection if applicable
				String connectionId = (String) emp.get(PeoplePagesServiceConstants.CONNECTION_ID);
				if (connectionId != null)
				{
					EntryMessage em = emp.getStatus();
					if (em != null) {
						sw.startElement(QN_STATUS);
						sw.writeDate(QN_ASOF, em.getPublished());
						sw.writeText(QN_MESSAGE, Text.Type.TEXT, em.getSummary());
						sw.endElement();
					}
					
					sw.writeLink(
						FeedUtils.calculateConnectionEntryURL(connectionId, profilesUrl),
						AtomConstants.LINK_REL_CONNECTION, kAtomMimeType);
				}
				else if (inclStatus) // special case / write status
				{
					EntryMessage em = emp.getStatus();
					sw.startElement(QN_STATUS);
					if (em != null) {
						sw.writeDate(QN_ASOF, em.getPublished());
						sw.writeText(QN_MESSAGE, Text.Type.TEXT, em.getSummary());
					}
					sw.endElement(); // QN_STATUS
				}
				
				if (outputType.equals(PeoplePagesServiceConstants.HCARD))
				{
					sw.writeLink(
						FeedUtils.calculateVcardUrl(emp.getKey(), profilesUrl),
						Link.REL_ALTERNATE, kTextMimeType);
				}
				else if (outputType.equals(PeoplePagesServiceConstants.VCARD))
				{
					if (isOauth) {
						sw.writeLink(FeedUtils.modUrlForOauth(
								FeedUtils.calculateProfilesEntryURL2(empKey, profilesUrl)),
								Link.REL_EDIT, kAtomMimeType);
						
					}
					else {
						sw.writeLink(
								FeedUtils.calculateProfilesEntryURL2(empKey, profilesUrl),
								Link.REL_EDIT, kAtomMimeType);
						
					}
				}
				if (searchType.equals(PeoplePagesServiceConstants.REPORTING_CHAIN) && emp.getManagerUid() != null
						&& emp.getManagerUid().length() > 0)
				{
					sw.startElement(THR_QN_IN_REPLY_TO);
					sw.writeAccepts(REF, ENTRY_ID + emp.getManagerUid());
					sw.endElement();
				}
			}
			
			sw.writeSummary(dbrec.getRecordSummary());
			
			if (!(dbrec instanceof Connection))
			{
				if (outputType.equals(PeoplePagesServiceConstants.VCARD))
				{
					VCardGenerator vcg = new VCardGenerator(sw, FeedUtils.getProfilesURL(request));
					// Customized label generation is not currently supported in outputType : VCard
//					vcg.setInclLabels(isInclLabels());
//					vcg.setLanguage(getLanguage());
					vcg.buildVCard(dbrec, isLite);
				}
				else if (outputType.equals(PeoplePagesServiceConstants.MIME_TEXT_XML))
				{
					String link = generateAdminEntryURL(dbrec);
					
					sw.writeLink(link, Link.REL_EDIT, kAtomMimeType);

					AtomGenerator3 xmlGen = new AtomGenerator3(sw, FeedUtils.getProfilesURL(request));
					// Customized label generation is not currently supported in outputType : MIME_TEXT_XML
					// this appears to be used only by Connections.do APIs and in a convoluted manner with a combination of undocumented parameters
					// Only VCard & HCard are documented output types, per :
					// http://www-10.lotus.com/ldd/lcwiki.nsf/xpDocViewer.xsp?lookupName=IBM+Connections+4.0+API+Documentation#action=openDocument&res_title=Searching_for_a_users_profile_ic40a&content=pdcontent
					// The default is HCard
//					xmlGen.setInclLabels(isInclLabels());
//					xmlGen.setLanguage(getLanguage());
					xmlGen.buildXmlProfileData(dbrec, searchType);
				}
				else
				{
					HCardGenerator hcg = new HCardGenerator(sw, helper, FeedUtils.getProfilesURL(request));
					hcg.setInclLabels(isInclLabels());
					hcg.setLanguage(getLanguage());
					hcg.setLocale(getLocale());
					hcg.buildHCard(dbrec, isLite);
				}
			}
		}
		sw.endEntry();
	}

	private void writeProfileAsATOMPerson(Employee profile, boolean contributor, String snxRel) 
	{
		if (profile != null)
		{
			if (contributor) {
				sw.startContributor();
			} else {
				sw.startAuthor();
			}
			
			if (snxRel != null) {
				sw.writeAttribute(QN_SNX_REL, snxRel);
			}
			
			sw.writePersonName(profile.getDisplayName());
			sw.startElement(QN_USERID).writeElementText(profile.getUserid()).endElement();
			if (allowEmailInReturn)
				sw.writePersonEmail(profile.getEmail());
			// 168874 - mcode is an internal detail. don't return it, especially with the corresponding email.
			//sw.startElement(QN_MCODE).writeElementText(profile.getMcode()).endElement();

			UserState state = profile.getState();
			sw.startElement(QN_USERSTATE).writeElementText(state.getName()).endElement();

			String isExternalStr = String.valueOf(profile.isExternal());
			sw.startElement(QN_ISEXTERNAL).writeElementText( isExternalStr ).endElement();
			
			if (contributor) {
				sw.endContributor();
			} else {
				sw.endAuthor();
			}
		}
	}

	private String generateSelfReferenceURL(DatabaseRecord dbrec) throws UnsupportedEncodingException
	{
		String feedUrl = request.getRequestURL().toString();
		String queryString = dbrec.getRecordSearchString();

		if (dbrec instanceof Employee)
		{
			Employee employee = (Employee) dbrec;
			if (StringUtils.equals(outputType, PeoplePagesServiceConstants.MIME_TEXT_XML)) {
				return generateAdminEntryURL(dbrec);
			}
			else if (isOauth){
				return FeedUtils.modUrlForOauth(FeedUtils.calculateProfilesEntryURL2(employee.getKey(), FeedUtils.getProfilesURL(request), outputType.equals(PeoplePagesServiceConstants.VCARD), isLite));
			}
			else {
				return FeedUtils.calculateProfilesEntryURL2(employee.getKey(), FeedUtils.getProfilesURL(request), outputType.equals(PeoplePagesServiceConstants.VCARD), isLite);
			}
		}
		else if (dbrec instanceof Connection)
		{
			Connection conn = (Connection)dbrec;
			if (isOauth){
				return FeedUtils.modUrlForOauth(FeedUtils.calculateConnectionEntryURL(conn.getConnectionId(), FeedUtils.getProfilesURL(request)));
			}
			else {
				return FeedUtils.calculateConnectionEntryURL(conn.getConnectionId(), FeedUtils.getProfilesURL(request));
			}
		}
		else
		{
			return feedUrl + "?" + queryString;
		}
	}
	

	private String generateAdminEntryURL(DatabaseRecord dbrec) throws UnsupportedEncodingException
	{
		String feedUrl = request.getRequestURL().toString();
		String queryString = dbrec.getRecordSearchString();

		if (dbrec instanceof Employee)
		{
			Employee employee = (Employee) dbrec;
			return FeedUtils.calculateAdminProfileEntryURL(employee.getKey(), FeedUtils.getProfilesURL(request));
		}
		else
		{
			return feedUrl + "?" + queryString;
		}
	}
	private String getId()  
	{
		if (APIHelper.isCodeSearchType(searchType)) {
			return CODES_FEED_ID + searchType;
		}					
		else {
			return FEED_ID;
		}
	}

	private String getFeedTitle() throws UnsupportedEncodingException
	{
		String feedTitle = "";
		
		if (searchType.equals("search") || searchType.equals(PeoplePagesServiceConstants.ADMIN_EMPLOYEE))
		{
			StringBuffer title = new StringBuffer();
//			title.append(helper.getString("profilesWhere"));
			Enumeration<?> enumeration = request.getParameterNames();

			// if no params for feed, use generic title 
			if (!enumeration.hasMoreElements()) {
				feedTitle = helper.getString("lc.author");
			}
			while (enumeration.hasMoreElements())
			{
				String key = (String)enumeration.nextElement();
				if (!key.equals(PeoplePagesServiceConstants.PAGE) && !key.equals(PeoplePagesServiceConstants.PAGE_SIZE)
						&& !key.equals(PeoplePagesServiceConstants.FORMAT))
				{
					String value = request.getParameter(key);
					title.append(" " + key + "=" + value);
				}
			}
//			feedTitle = title.toString();
			if (feedTitle == "") {
				feedTitle = helper.getString("profilesWhere", title.toString());
			}
		}
		else
		{
			String value = null;
			if (searchType.equals(PeoplePagesServiceConstants.REPORTING_CHAIN)
				|| searchType.equals(PeoplePagesServiceConstants.PEOPLE_MANAGED)
				|| searchType.equals(PeoplePagesServiceConstants.CONNECTIONS)
				|| searchType.equals(PeoplePagesServiceConstants.CONNECTIONS_IN_COMMON)
				|| searchType.equals(PeoplePagesServiceConstants.PROFILE_ROLES_FOR)
				)
			{
				ProfileLookupKey plk = null;
				if (searchType.equals(PeoplePagesServiceConstants.PROFILE_ROLES_FOR))
					plk = BaseAction.getProfileLookupKey(request, BaseAction.TARGET_PARAM_TYPE_MAP);
				else
					plk = BaseAction.getProfileLookupKey(request);

				if (plk != null)
				{
					PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
					Employee profile = null;
					try {
						profile = service.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
					}
					catch (DataAccessRetrieveException e) {
						LOG.error(e.getMessage(), e);
					}
					value = (profile == null) ? plk.getValue() : profile.getDisplayName();
				}
				else
				{
					value = "";
				}
			}
			else
			{
				value = request.getParameter(searchType);
			}

			feedTitle = helper.getString(searchType);
			if (value != null)
			{
				value = decodeIfEncoded(value, "UTF-8");
				if (searchType.equals(PeoplePagesServiceConstants.REPORTING_CHAIN)
					|| searchType.equals(PeoplePagesServiceConstants.PEOPLE_MANAGED)
					|| searchType.equals(PeoplePagesServiceConstants.PROFILE_TAGS_FOR)
					|| searchType.equals(PeoplePagesServiceConstants.PROFILE_ROLES_FOR)
					|| searchType.equals(PeoplePagesServiceConstants.CONNECTIONS)
					|| searchType.equals(PeoplePagesServiceConstants.CONNECTIONS_IN_COMMON)
				)	{
					feedTitle = helper.getString(searchType, value);
				} else {
					feedTitle = feedTitle + " " + value;
				}
			}
		}
		return feedTitle;
	}

	private String encodeRequestQueryString() throws UnsupportedEncodingException
	{
		StringBuffer queryString = new StringBuffer();
		Enumeration<?> parameters = request.getParameterNames();
		int i = 0;
		while (parameters.hasMoreElements())
		{
			String parameter = (String)parameters.nextElement();
			String value = request.getParameter(parameter);
			//don't double encode
			value = decodeIfEncoded(value, "UTF-8");
			value = URLEncoder.encode(value, "UTF-8");
			value = value.replaceAll("\\+", "%20");
			if (i == 0)
			{
				queryString.append("?");
			}
			else if (i > 0)
			{
				queryString.append("&");
			}
			queryString.append(parameter + "=" + value);
			i++;
		}
		return queryString.toString();
	}

	private String buildURLMinusPaging() throws UnsupportedEncodingException
	{
		String feedUrl = request.getRequestURL().toString();
		StringBuffer queryString = new StringBuffer();
		Enumeration<?> parameters = request.getParameterNames();
		int i = 0;
		if (isOauth) {
			feedUrl = FeedUtils.modUrlForOauth(feedUrl);
		}
		while (parameters.hasMoreElements())
		{
			String parameter = (String)parameters.nextElement();
			if (!parameter.equals(PeoplePagesServiceConstants.PAGE)
					&& !parameter.equals(PeoplePagesServiceConstants.PAGE_SIZE)
					&& !parameter.equals(PeoplePagesServiceConstants.ITER_STATE))
			{
				String value = request.getParameter(parameter);
				//don't double encode
				// SPR #SSIH8PBB33:  This is meant to take care of the double encoding situation 
				// e.g., user uses the 'link' value from the ATOM feed. The 'safeDecode()'
				// method would throw exceptions for some strings that are just user inputs
				// e.g. strings ending with %. We should really leave such inputs alone.
				// Use a local method for this purpose, rather than changing the utility method.
				// value = LCUrlUtil.safeDecode(value, "UTF-8");
				value = decodeIfEncoded(value, "UTF-8");
				value = URLEncoder.encode(value, "UTF-8");
				value = value.replaceAll("\\+", "%20");
				if (i == 0)
				{
					queryString.append("?");
				}
				else if (i > 0)
				{
					queryString.append("&");
				}
				queryString.append(parameter + "=" + value);
				i++;
			}
		}
		String qString = queryString.toString();
		String url = feedUrl + qString;
		return url;
	}
	
	private final void setNamespace() {
		sw.setPrefix(MessageVectorAtomConstants.NS_PREFIX_OPENSEARCH, MessageVectorAtomConstants.NS_OPENSEARCH);
		sw.setPrefix("", LCAtomConstants.NS_ATOM);
		sw.setPrefix(NS_PREFIX_SNX, LCAtomConstants.NS_SNX);
		sw.setPrefix(NS_PREFIX_FH, FH_NS);
		sw.setPrefix(NS_PREFIX_THREAD, THR_NS);
		sw.setPrefix(NS_PREFIX_APP, NS_APP);	
//		sw.setPrefix("", OPENSOCIAL_NS);
	}

	private final void writeNamespaceInfo() {
		sw.writeNamespace(MessageVectorAtomConstants.NS_PREFIX_OPENSEARCH, MessageVectorAtomConstants.NS_OPENSEARCH);
		sw.writeNamespace("snx", LCAtomConstants.NS_SNX);
		sw.writeNamespace(NS_PREFIX_FH, FH_NS);
		sw.writeNamespace(NS_PREFIX_THREAD, THR_NS);
		sw.writeNamespace(NS_PREFIX_APP, NS_APP);			
	}

	private final void setRoleNamespace() {
		sw.setPrefix("", LCAtomConstants.NS_ATOM);
		sw.setPrefix(NS_PREFIX_SNX, LCAtomConstants.NS_SNX);
		sw.setPrefix(NS_PREFIX_APP, NS_APP);	
	}

	private final void writeRoleNamespaceInfo() {
		sw.writeNamespace("snx", LCAtomConstants.NS_SNX);
		sw.writeNamespace(NS_PREFIX_APP, NS_APP);			
	}

	/**
	 * needs to be protected from invalid page numbers and page sizes
	 * 
	 * @param resultsPage
	 * @throws UnsupportedEncodingException
	 */
	private void writePagingInfo(SearchResultsPage<?> resultsPage) throws UnsupportedEncodingException {

		sw.startElement(QN_TOTAL_RESULTS);
		sw.writeElementText(resultsPage.getTotalResults());
		sw.endElement();

		sw.startElement(QN_START_INDEX);
		sw.writeElementText(resultsPage.getStart());
		sw.endElement();

		sw.startElement(QN_ITEMS_PER_PAGE);
		sw.writeElementText(resultsPage.getPageSize());
		sw.endElement();

		if (resultsPage.getPage() != 1) {
			String url = buildURLMinusPaging() + "&" + PeoplePagesServiceConstants.PAGE_SIZE + "=" + resultsPage.getPageSize();
			sw.writeLink(url, FIRST, null);

			int previous = resultsPage.getPage() - 1;
			url = buildURLMinusPaging() + "&" + PeoplePagesServiceConstants.PAGE + "=" + previous + "&"
					+ PeoplePagesServiceConstants.PAGE_SIZE + "=" + resultsPage.getPageSize();
			sw.writeLink(url, PREVIOUS, null);
		}

		// resultsPage.isLastPage() returns false if the caller asks for a page greater than the actual number of pages,
		// so we need the "resultsPage.getPage() <= resultsPage.getNumPages()" test to exclude NEXT/LAST links. Callers can then safely
		// interpret absence of NEXT/LAST as "on the last page" (if there are entries in the feed) or "past the last page" (if there are no
		// entries).
		if (!resultsPage.isLastPage() && resultsPage.getPage() <= resultsPage.getNumPages()) {
			int next = resultsPage.getPage() + 1;
			String url = buildURLMinusPaging() + "&" + PeoplePagesServiceConstants.PAGE + "=" + next + "&"
					+ PeoplePagesServiceConstants.PAGE_SIZE + "=" + resultsPage.getPageSize();
			sw.writeLink(url, NEXT, null);

			int last = resultsPage.getNumPages();
			url = buildURLMinusPaging() + "&" + PeoplePagesServiceConstants.PAGE + "=" + last + "&" + PeoplePagesServiceConstants.PAGE_SIZE
					+ "=" + resultsPage.getPageSize();
			sw.writeLink(url, LAST, null);
		}
	}

	/**
	 * Unescapes a url string to decode a url string that might have been encoded to avoid double encoding. If there is
	 * an exceptiong trying to decode it, it would mean that the urlStr was not encoded before, hence we should not try to 
	 * decode it. See: LCUrlUtil.safeDecode() for different handling.
	 * @param urlStr The url string to unescape
	 * @param encoding The character encoding to assume
	 * @return
	 */
	private String decodeIfEncoded(String urlStr, String encoding) {
		String retval = urlStr;

		try {
			retval = URLDecoder.decode(urlStr, encoding);
		}
		catch (UnsupportedEncodingException ue) {
			// Don't need to handle
		}
		catch (IllegalArgumentException ie) {
			// Don't need to handle
		}

		return retval;
	}

	public void generateAtomFeedForRoles(List<EmployeeRole> results, boolean fullFormat, ProfileLookupKey userLookupKey, String title) throws Exception
	{
		this.searchType = PeoplePagesServiceConstants.PROFILE_ROLES_FOR;
		
		try {
			setRoleNamespace();
			startDocument();

			sw.startFeed();
			writeRoleNamespaceInfo();

			sw.writeId(getId());
			sw.writeGenerator(AtomConstants.GENERATOR_VERSION, NS, AtomConstants.GENERATOR_NAME); // Not translated
			String feedTitle = null;

			try {
				feedTitle = getFeedTitle();
			}
			catch (Exception e) {
				feedTitle = title + userLookupKey.getValue();
			}

			sw.writeTitle(feedTitle);
			sw.writeAuthor(helper.getString("lc.author"));
			sw.writeUpdated(new Date());

			sw.startElement(QN_TOTAL_RESULTS);
			sw.writeElementText(results.size());
			sw.endElement();

			// ROLES
			{
				String queryString = encodeRequestQueryString();
				String feedUrl = request.getRequestURL().toString();
				if (isOauth) {
					feedUrl = FeedUtils.modUrlForOauth(feedUrl);
				}
				sw.writeLink(feedUrl + queryString, Link.REL_SELF, kAtomMimeType);

				for (int i = 0, imax = results.size(); i < imax; i++) {
					EmployeeRole result = (EmployeeRole) results.get(i);
					if (result != null) {
						generateAtomEntryForRoles(result, fullFormat, false);
					}
				}
			}
			sw.endFeed();
			endDocument();
		}
		catch (MimeTypeParseException mtpe) {
			LOG.error(mtpe.getMessage(), mtpe);
			throw mtpe;
		}
		catch (UnsupportedEncodingException uee) {
			LOG.error(uee.getMessage(), uee);
			throw uee;
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}
	}
	/**
	 * Input for write role collection method
	 */
	public static class RolesBean {
		public ProfileLookupKey userLookupKey = null;
		public RoleCollection  roleCollection = null;
		public String          titleForRoles = null;

		public RolesBean() {}

		public void setFeedTitleForRoles(String title) {
			this.titleForRoles  = title;
		}
		public String getFeedTitleForRoles() {
			return this.titleForRoles;
		}
	}

	private void generateAtomEntryForRoles(EmployeeRole empRole, boolean fullFormat, boolean declareNs) throws Exception
	{
		sw.startEntry();

		if (empRole != null)
		{
			String recordId = URLEncoder.encode(empRole.getRoleId(), "UTF-8");
			recordId = recordId.replaceAll("\\+", "%20");
			sw.writeId(recordId);

			String created = formatTimestamp(empRole.getCreated()); // <created>2014-03-04T05:00:00.000Z</created>

			sw.startElement(AtomConstants.CREATED);
			sw.writeElementText(created);
			sw.endElement();

//			sw.startElement(AtomConstants.ROLE);
//			sw.writeElementText(empRole.getRoleId());
//			sw.endElement();

			sw.writeCategory(AtomConstants.ROLE, TYPE_SCHEME);
		}
		sw.endEntry();
	}

    private String formatTimestamp(Date time)
	{
		sdf.getCalendar().setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(time);
	}

	public static final void writeAdminProfileServiceLinks(StreamWriter sw, HttpServletRequest request /*, boolean isSecure, boolean isOauth*/) throws UnsupportedEncodingException
	{
		String profilesUrl = FeedUtils.getProfilesURL(request);

//		// link to admin profile service doc
//		sw.writeLink(FeedUtils.modUrlForOauth(
//				FeedUtils.calculateAdminProfilesServiceDocURL(null, profilesUrl)),
//				AtomConstants.LINK_REL_PROFILE_SERVICE, AtomConstants.APP_MIME_TYPE);

		// link to admin profiles service
		sw.writeLink(FeedUtils.modUrlForOauth(
				FeedUtils.calculateAdminProfilesURL(profilesUrl)),
				AtomConstants.LINK_REL_PROFILES_SERVICE, AtomConstants.ATOM_MIME_TYPE);

		// link to admin profile entry service
		sw.writeLink(FeedUtils.modUrlForOauth(
				FeedUtils.calculateAdminProfileEntryURL(null, profilesUrl)),
				AtomConstants.LINK_REL_PROFILE_ENTRY_SERVICE, AtomConstants.ATOM_MIME_TYPE);

		// link to admin profile tag service
		sw.writeLink(FeedUtils.modUrlForOauth(
				FeedUtils.calculateAdminTagURL(profilesUrl)),
				AtomConstants.LINK_REL_TAG_SERVICE, AtomConstants.APP_MIME_TYPE);

		// link to the admin profile roles service
		sw.writeLink(FeedUtils.modUrlForOauth(
				FeedUtils.calculateAdminRolesURL(profilesUrl)),
				AtomConstants.LINK_REL_ROLES_SERVICE, AtomConstants.ATOM_MIME_TYPE);

		// link to admin profile following service
		sw.writeLink(FeedUtils.modUrlForOauth(
				FeedUtils.calculateAdminFollowingURL(profilesUrl)),
				AtomConstants.LINK_REL_FOLLOWING_SERVICE, AtomConstants.ATOM_MIME_TYPE);

		// link to admin profile connections service
		sw.writeLink(FeedUtils.modUrlForOauth(
				FeedUtils.calculateAdminConnectionsURL(profilesUrl)),
				AtomConstants.LINK_REL_CONNECTIONS_SERVICE, AtomConstants.ATOM_MIME_TYPE);			    	

		// link to admin profile connection service
		sw.writeLink(FeedUtils.modUrlForOauth(
				FeedUtils.calculateAdminConnectionURL(profilesUrl)),
				AtomConstants.LINK_REL_CONNECTION_SERVICE, AtomConstants.ATOM_MIME_TYPE);			    	

		// link to admin profile codes service
		sw.writeLink(FeedUtils.modUrlForOauth(
				FeedUtils.calculateAdminCodesURL(profilesUrl)),
				AtomConstants.LINK_REL_CODES_SERVICE, AtomConstants.ATOM_MIME_TYPE);		    	
	}

}
