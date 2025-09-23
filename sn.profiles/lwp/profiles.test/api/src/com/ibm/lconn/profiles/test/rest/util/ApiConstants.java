/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.util;

import javax.xml.namespace.QName;

public class ApiConstants {

	public static class App {
		public static final String NS_URI = "http://www.w3.org/2007/app";

		public static final String NS_PREFIX = "app";

		public App() {
		}
	}

	public static class Atom {
		public static final String NS_URI = "http://www.w3.org/2005/Atom";

		public static final String NS_PREFIX = "atom";

		public static final QName QN_CONTENT = new QName(NS_URI, "content", NS_PREFIX);

		public static final QName QN_EMAIL = new QName(NS_URI, "email", NS_PREFIX);
		
		public static final QName QN_CONTRIBUTOR = new QName(NS_URI, "contributor", NS_PREFIX);

		public static final QName LINK = new QName(NS_URI, "link", NS_PREFIX);

		public static final String MEDIA_TYPE_XML = "application/xml";

		public static final String MEDIA_TYPE_ANY = "*/*";

		public static final String MEDIA_TYPE_ATOM_FEED = "application/atom+xml;type=feed";

		public static final String MEDIA_TYPE_ATOM_SERVICE_DOCUMENT = "application/atomsvc+xml";

		public static final String REL_SELF = "self";

		public static final String REL_SERVICE = "service";

		public static final String REL_DESCRIBED_BY = "describedby";

		public static final String REL_VIA = "via";

		public static final String REL_EDIT_MEDIA = "edit-media";

		public static final String REL_EDIT = "edit";

		public static final String REL_ALTERNATE = "alternate";

		public static final String REL_FIRST = "first";

		public static final String REL_PREVIOUS = "previous";

		public static final String REL_NEXT = "next";

		public static final String REL_LAST = "last";

		public static final String REL_UP = "up";

		public static final String REL_DOWN = "down";

		public static final String REL_RELATED = "related";
		
		public static final String REL_ALT_SSL = "http://www.ibm.com/xmlns/prod/sn/alternate-ssl";

		private Atom() {
		}
	}

	public static class HttpHeaders {

		public static final String ALLOW = "Allow";

		public static final String NONCE = "X-Update-Nonce";

		public static final String MOZILLA = "X-moz";

		public static final String X_METHOD_OVERRIDE = "X-Method-Override";

		public static final String CONTENT_LOCATION = "Content-Location";

		private HttpHeaders() {
		}
	}

	/**
	 * Constants specific to social-networking across Connections.
	 */
	public static class SocialNetworking {
		public static final String NS_PREFIX = "snx";

		public static final String NS_URI = "http://www.ibm.com/xmlns/prod/sn";

		public static final String STATUS = "status";

		public static final String ATTR_PREFIX = "com.ibm.snx_profiles";

		public static final String NAME = "name";

		public static final String TERM_ENTRY = "entry";

		public static final String TERM_REPLIES = "replies";

		public static final String TERM_SIMPLE_COMMENT = "simpleComment";

		public static final String TERM_PROFILE = "profile";

		public static final String TERM_PENDING = "pending";

		public static final String TERM_COLLEAGUE = "colleague";

		public static final String TERM_COMMENT = "comment";

		public static final String TERM_CONNECTION = "connection";

		public static final String TERM_CODES = "profiles.codes";

		public static final String OAUTH = "oauth";

		public static final QName EDITABLE_FIELDS = new QName(NS_URI, "editableFields", NS_PREFIX);

		public static final QName EDITABLE_FIELD = new QName(NS_URI, "editableField", NS_PREFIX);

		public static final QName USER_ID = new QName(NS_URI, "userid", NS_PREFIX);

		public static final QName COMMENTS = new QName(NS_URI, "comments", NS_PREFIX);

		public static final QName USER_STATE = new QName(NS_URI, "userState", NS_PREFIX);
		
		public static final QName IS_EXTERNAL = new QName(NS_URI, "isExternal", NS_PREFIX);

		public static final QName MODERATION = new QName(NS_URI, "moderation", NS_PREFIX);

		public static final QName NONCE = new QName(NS_URI, "nonce", NS_PREFIX);

		public static final QName COMMUNITY_TYPE = new QName(NS_URI, "communityType", NS_PREFIX);

		public static final QName MEMBER_COUNT = new QName(NS_URI, "membercount", NS_PREFIX);

		public static final QName HANDLE = new QName(NS_URI, "handle", NS_PREFIX);

		public static final QName ROLE = new QName(NS_URI, "role", NS_PREFIX);

		public static final QName EXTENSION_ID = new QName(NS_URI, "extensionId", NS_PREFIX);

		public static final QName QN_CONNECTION = new QName(NS_URI, "connection", NS_PREFIX);

		public static final QName QN_SNX_REL = new QName(NS_URI, "rel", NS_PREFIX);
		
		public static final QName FREQUENCY = new QName(NS_URI, "frequency");
		
		public static final QName TYPE = new QName(NS_URI, "type");
		
		public static final QName INTENSITY_BIN = new QName(NS_URI, "intensityBin");
		
		public static final QName VISIBILITY_BIN = new QName(NS_URI, "visibilityBin");
		
		public static final QName QN_STATUS = new QName(NS_URI, STATUS);
		
		public static final QName QN_AS_OF = new QName(NS_URI, "asof");
		
		public static final QName QN_MESSAGE = new QName(NS_URI, "message");
		
		public static final QName TARGET_KEY = new QName(NS_URI, "targetKey");

		public static final QName TAG_OTHERS_ENABLED = new QName(NS_URI, "tagOthersEnabled");
		
		public static final QName CAN_ADD_TAG = new QName(NS_URI, "canAddTag");
		
		public static final QName NUMBER_OF_CONTRIBUTORS = new QName(NS_URI, "numberOfContributors");
		
		public static final String SCHEME_TYPE = "http://www.ibm.com/xmlns/prod/sn/type";

		public static final String SCHEME_FLAGS = "http://www.ibm.com/xmlns/prod/sn/flags";

		public static final String SCHEME_MESSAGE_TYPE = "http://www.ibm.com/xmlns/prod/sn/message-type";

		public static final String SCHEME_CONNECTION_TYPE = "http://www.ibm.com/xmlns/prod/sn/connection/type";

		public static final String SCHEME_STATUS = "http://www.ibm.com/xmlns/prod/sn/status";
		
		public static final String SCHEME_RESOURCE_ID = "http://www.ibm.com/xmlns/prod/sn/resource-id";

		public static final String REL_LOGO = "http://www.ibm.com/xmlns/prod/sn/logo";

		public static final String REL_SELF = "self";

		public static final String REL_MEMBER_LIST = "http://www.ibm.com/xmlns/prod/sn/member-list";

		public static final String REL_BOOKMARKS = "http://www.ibm.com/xmlns/prod/sn/bookmarks";

		public static final String REL_FEEDS = "http://www.ibm.com/xmlns/prod/sn/feeds";

		public static final String REL_FORUM_TOPICS = "http://www.ibm.com/xmlns/prod/sn/forum-topics";

		public static final String REL_REMOTE_APPLICATIONS = "http://www.ibm.com/xmlns/prod/sn/remote-applications";

		public static final String REL_INVITATIONS_LIST = "http://www.ibm.com/xmlns/prod/sn/invitations-list";

		public static final String REL_PARENT_COMMUNITY = "http://www.ibm.com/xmlns/prod/sn/parentcommunity";

		public static final String REL_SUBCOMMUNITIES = "http://www.ibm.com/xmlns/prod/sn/subcommunities";

		public static final String REL_TAG_CLOUD = "http://www.ibm.com/xmlns/prod/sn/tag-cloud";

		// admin service document links
		public static final String REL_PROFILES_SERVICE    = "http://www.ibm.com/xmlns/prod/sn/profiles";

		public static final String REL_PROFILE_ENTRY_SERVICE = "http://www.ibm.com/xmlns/prod/sn/profileEntry";

		public static final String REL_TAG_SERVICE         = "http://www.ibm.com/xmlns/prod/sn/profileTags";

		public static final String REL_ROLES_SERVICE       = "http://www.ibm.com/xmlns/prod/sn/profileRoles";

		public static final String REL_FOLLOWING_SERVICE   = "http://www.ibm.com/xmlns/prod/sn/following";

		public static final String REL_CONNECTIONS_SERVICE = "http://www.ibm.com/xmlns/prod/sn/connections";

		public static final String REL_CONNECTION_SERVICE  = "http://www.ibm.com/xmlns/prod/sn/connection";

		public static final String REL_CODES_SERVICE       = "http://www.ibm.com/xmlns/prod/sn/codes";

		public static final String REL_ROLES = "http://www.ibm.com/xmlns/prod/sn/roles";

		public static final String REL_BOARD = "http://www.ibm.com/xmlns/prod/sn/mv/theboard";

		public static final String REL_STATUS = "http://www.ibm.com/xmlns/prod/sn/status";

		public static final String REL_COLLEAGUE = "http://www.ibm.com/xmlns/prod/sn/connections/colleague";

		public static final String REL_REPORTING_CHAIN = "http://www.ibm.com/xmlns/prod/sn/reporting-chain";

		public static final String REL_EXTENSION_ATTR = "http://www.ibm.com/xmlns/prod/sn/ext-attr";

		public static final String REL_ACTIVITIES = "http://www.ibm.com/xmlns/prod/sn/service/activities";

		public static final String REL_PROFILES = "http://www.ibm.com/xmlns/prod/sn/service/profiles";

		public static final String REL_DOGEAR = "http://www.ibm.com/xmlns/prod/sn/service/dogear";

		public static final String REL_COMMUNITIES = "http://www.ibm.com/xmlns/prod/sn/service/communities";

		public static final String REL_FILES = "http://www.ibm.com/xmlns/prod/sn/service/files";

		public static final String REL_WIKIS = "http://www.ibm.com/xmlns/prod/sn/service/wikis";

		public static final String REL_BLOGS = "http://www.ibm.com/xmlns/prod/sn/service/blogs";

		public static final String REL_IMAGE = "http://www.ibm.com/xmlns/prod/sn/image";

		public static final String REL_PRONOUNCE = "http://www.ibm.com/xmlns/prod/sn/pronunciation";

		public static final String SNX_REL_SOURCE = "http://www.ibm.com/xmlns/prod/sn/connection/source";

		public static final String SNX_REL_TARGET = "http://www.ibm.com/xmlns/prod/sn/connection/target";

		private SocialNetworking() {
		}
	}

	public static class ProfileTypeConstants {

		public static final String NS_URI = "http://www.ibm.com/profiles-types";

		public static final String NS_PREFIX = "pt";

		public static final QName CONFIG = new QName(NS_URI, "config", NS_PREFIX);

		public static final QName TYPE = new QName(NS_URI, "type", NS_PREFIX);

		public static final QName PARENT_ID = new QName(NS_URI, "parentId", NS_PREFIX);

		public static final QName ID = new QName(NS_URI, "id", NS_PREFIX);

		public static final QName PROPERTY = new QName(NS_URI, "property", NS_PREFIX);

		public static final QName REF = new QName(NS_URI, "ref", NS_PREFIX);

		public static final QName UPDATABILITY = new QName(NS_URI, "updatability", NS_PREFIX);

		public static final QName REQUIRED = new QName(NS_URI, "required", NS_PREFIX);

		public static final QName HIDDEN = new QName(NS_URI, "hidden", NS_PREFIX);

		public static final QName RICH_TEXT = new QName(NS_URI, "richText", NS_PREFIX);

		public static final QName ACL = new QName(NS_URI, "acl", NS_PREFIX);

		public static final String ATTR_ROLE = "role";

		public static final String ATTR_PERMISSION = "permission";

		public static final String ATTR_ID = "id";

		public static final String PROFILE_TYPE_CONTENT_TYPE = "application/profile-type+xml; charset=UTF-8";

		private ProfileTypeConstants() {
		}
	}

	public static class OpenSearch {

		public static final String NS_URI = "http://a9.com/-/spec/opensearch/1.1/";

		public static final String NS_PREFIX = "opensearch";

		public static final QName QN_ITEMS_PER_PAGE = new QName(NS_URI, "itemsPerPage", NS_PREFIX);

		public static final QName QN_START_INDEX = new QName(NS_URI, "startIndex", NS_PREFIX);

		public static final QName QN_TOTAL_RESULTS = new QName(NS_URI, "totalResults", NS_PREFIX);

		private OpenSearch() {
		}
	}

	public static class OpenSocial {

		public static final String NS_URI = "http://ns.opensocial.org/2008/opensocial";

		public static final String NS_PREFIX = "";

		public static final QName QN_SNX_PROFILES_ATTRIB = new QName(NS_URI, SocialNetworking.ATTR_PREFIX + ".attrib");

		public static final QName QN_DATA = new QName(NS_URI, "data");

		public static final QName QN_ENTRY = new QName(NS_URI, "entry");

		public static final QName QN_KEY = new QName(NS_URI, "key");

		public static final QName QN_PERSON = new QName(NS_URI, "person");

		public static final QName QN_TYPE = new QName(NS_URI, "type");

		public static final QName QN_VALUE = new QName(NS_URI, "value");

		public static final QName QN_APPDATA = new QName(NS_URI, "appData");
		
		private OpenSocial() {

		}
	}

	public static class TagConfigConstants {
		public static final String NS_URI = SocialNetworking.NS_URI;
		public static final String NS_PREFIX = SocialNetworking.NS_PREFIX;
		public static final String MEDIA_TYPE = "application/xml; charset=UTF-8";

		public static QName TAGS_CONFIG = new QName(NS_URI, "tagsConfig");
		public static QName TAG_CONFIG = new QName(NS_URI, "tagConfig");
		
		public static final String ATTR_TYPE = "type";
		public static final String ATTR_SCHEME = "scheme";
		public static final String ATTR_PHRASE_SUPPORTED = "phraseSupported";

		public static final String GENERAL = "general";
		
	}
	
	public static class ConnectionTypeConstants {
		public static final String NS_URI = SocialNetworking.NS_URI;
		public static final String NS_PREFIX = SocialNetworking.NS_PREFIX;
		public static final String MEDIA_TYPE = "application/xml; charset=UTF-8";

		public static QName CONNECTION_TYPE_CONFIG = new QName(NS_URI, "connectionTypeConfig");
		public static QName CONNECTION_TYPE = new QName(NS_URI, "connectionType");
		public static final String ATTR_TYPE = "type";
		public static final String ATTR_WORKFLOW = "workflow";
		public static final String ATTR_GRAPH = "graph";
		public static final String ATTR_INDEXED = "indexed";
		public static final String ATTR_EXTENSION = "extension";
		public static final String ATTR_REL = "rel";
		public static final String ATTR_NOTIFICATION_TYPE = "notificationType";
		public static final String ATTR_MESSAGE_ACL = "messageAcl";
		public static final String ATTR_NODE_OF_CREATOR = "nodeOfCreator";
		public static final String SOURCE = "source";
		public static final String TARGET = "target";
		public static final String CONFIRMED = "confirmed";
		public static final String NONE = "none";
		public static final String BIDIRECTIONAL = "bidirectional";
		public static final String DIRECTIONAL = "directional";
		public static final String COLLEAGUE = "colleague";
		public static final String NOTIFY = "notify";
		public static final String MESSAGE_PUBLIC = "public";
		public static final String MESSAGE_PRIVATE = "private";
		public static final String MESSAGE_SOURCE = "source";
		public static final String MESSAGE_TARGET = "target";
		
	}
	
	public static class LinkRollConstants {

		public static final String NS_URI = "http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links";

		public static final String NS_PREFIX = "";

		public static final QName LINK_ROLL = new QName(NS_URI, "linkroll");

		public static final QName LINK = new QName(NS_URI, "link");

		public static final String MEDIA_TYPE = "text/xml; charset=UTF-8";

		public static final String ATTR_NAME = "name";

		public static final String ATTR_URL = "url";

		public static final String EXTENSION_ID = "profileLinks";

		private LinkRollConstants() {

		}
	}

	/**
	 * List of field identifiers supported by Profiles in the Seedlist
	 */
	public static class SeedlistConstants {

		public static final String WPLC_NS_URI = "http://www.ibm.com/wplc/atom/1.0";
		public static final String WPLC_PREFIX = "wplc";
		public static final QName ACTION = new QName(WPLC_NS_URI, "action");
		public static final QName FIELD = new QName(WPLC_NS_URI, "field");
		public static final QName FIELD_INFO = new QName(WPLC_NS_URI, "fieldInfo", WPLC_PREFIX);
		public static final QName ACLS = new QName(WPLC_NS_URI, "acls");
		public static final QName ACL = new QName(WPLC_NS_URI, "acl");
		public static final QName TIMESTAMP = new QName(WPLC_NS_URI, "timestamp");

		public static final String ATTR_DO = "do";
		public static final String UPDATE = "update";
		public static final String ID = "id";

		// start field ids
		public static final String FIELD_UID_ID = "FIELD_UID";
		public static final String FIELD_DISPLAY_NAME_ID = "FIELD_DISPLAY_NAME";
		public static final String FIELD_PREFERRED_FIRST_NAME_ID = "FIELD_PREFERRED_FIRST_NAME";
		public static final String FIELD_PREFERRED_LAST_NAME_ID = "FIELD_PREFERRED_LAST_NAME";
		public static final String FIELD_ALTERNATE_LAST_NAME_ID = "FIELD_ALTERNATE_LAST_NAME";
		public static final String FIELD_NATIVE_LAST_NAME_ID = "FIELD_NATIVE_LAST_NAME";
		public static final String FIELD_NATIVE_FIRST_NAME_ID = "FIELD_NATIVE_FIRST_NAME";
		public static final String FIELD_GIVEN_NAME_ID = "FIELD_GIVEN_NAME";
		public static final String FIELD_SURNAME_ID = "FIELD_SURNAME";
		public static final String FIELD_MAIL_ID = "FIELD_MAIL";
		public static final String FIELD_GROUPWARE_EMAIL_ID = "FIELD_GROUPWARE_EMAIL";
		public static final String FIELD_EMPLOYEE_TYPE_ID = "FIELD_EMPLOYEE_TYPE";
		public static final String FIELD_EMPLOYEE_NUMBER_ID = "FIELD_EMPLOYEE_NUMBER";
		public static final String FIELD_TELEPHONE_NUMBER_ID = "FIELD_TELEPHONE_NUMBER";
		public static final String FIELD_IP_TELEPHONE_NUMBER_ID = "FIELD_IP_TELEPHONE_NUMBER";
		public static final String FIELD_JOB_RESPONSIBILITIES_ID = "FIELD_JOB_RESPONSIBILITIES";
		public static final String FIELD_IS_MANAGER_ID = "FIELD_IS_MANAGER";
		public static final String FIELD_FAX_TELEPHONE_NUMBER_ID = "FIELD_FAX_TELEPHONE_NUMBER";
		public static final String FIELD_MOBILE_ID = "FIELD_MOBILE";
		public static final String FIELD_PAGER_TYPE_ID = "FIELD_PAGER_TYPE";
		public static final String FIELD_PAGER_ID = "FIELD_PAGER";
		public static final String FIELD_PAGER_ID_ID = "FIELD_PAGER_ID";
		public static final String FIELD_PAGER_SERVICE_PROVIDER_ID = "FIELD_PAGER_SERVICE_PROVIDER";
		public static final String FIELD_ORGANIZATION_IDENTIFIER_ID = "FIELD_ORGANIZATION_IDENTIFIER";
		public static final String FIELD_ORGANIZATION_TITLE_ID = "FIELD_ORGANIZATION_TITLE";
		public static final String FIELD_DEPARTMENT_NUMBER_ID = "FIELD_DEPARTMENT_NUMBER";
		public static final String FIELD_DEPARTMENT_TITLE_ID = "FIELD_DEPARTMENT_TITLE";
		public static final String FIELD_BUILDING_IDENTIFIER_ID = "FIELD_BUILDING_IDENTIFIER";
		public static final String FIELD_FLOOR_ID = "FIELD_FLOOR";
		public static final String FIELD_ISO_COUNTRY_CODE_ID = "FIELD_ISO_COUNTRY_CODE";
		public static final String FIELD_PHYSICAL_DELIVERY_OFFICE_ID = "FIELD_PHYSICAL_DELIVERY_OFFICE";
		public static final String FIELD_WORK_LOCATION_ID = "FIELD_WORK_LOCATION";
		public static final String FIELD_WORK_LOCATION_CODE_ID = "FIELD_WORK_LOCATION_CODE";
		public static final String FIELD_EXPERIENCE_ID = "FIELD_EXPERIENCE";
		public static final String FIELD_MANAGER_UID_ID = "FIELD_MANAGER_UID";
		public static final String FIELD_MANAGER_USERID_ID = "FIELD_MANAGER_USERID";
		public static final String FIELD_SECRETARY_UID_ID = "FIELD_SECRETARY_UID";
		public static final String FIELD_SECRETARY_DISPLAY_NAME_ID = "FIELD_SECRETARY_DISPLAY_NAME";
		public static final String FIELD_PREFERRED_LANGUAGE_ID = "FIELD_PREFERRED_LANGUAGE";
		public static final String FIELD_TIMEZONE_ID = "FIELD_TIMEZONE";
		public static final String FIELD_TYPE_ID = "FIELD_TYPE";
		public static final String FIELD_BLOG_URL_ID = "FIELD_BLOG_URL";
		public static final String FIELD_FREEBUSY_URL_ID = "FIELD_FREEBUSY_URL";
		public static final String FIELD_CALENDAR_URL_ID = "FIELD_CALENDAR_URL";
		public static final String FIELD_TAG_ID = "FIELD_TAG";
		public static final String FIELD_ROLE_ID = "FIELD_ROLE";
		public static final String FIELD_ABOUT_ME = "FIELD_ABOUT_ME";
		public static final String FIELD_PROFILE_TYPE = "FIELD_PROFILE_TYPE";
		public static final String FIELD_COLLEAGUE = "FIELD_CONNECTIONS_COLLEAGUE_FIELD";
		public static final String FIELD_COLLEAGUE_UID = "FIELD_CONNECTIONS_COLLEAGUE_UID_FIELD";
		public static final String FIELD_TAGGER = "FIELD_TAGGER";
		public static final String FIELD_TAGGER_UID = "FIELD_TAGGER_UID";
		public static final String FIELD_LOCATION = "FIELD_LOCATION";
		public static final String FIELD_LOCATION2 = "FIELD_LOCATION2";
		public static final String FIELD_CITY = "FIELD_CITY";
		public static final String FIELD_STATE = "FIELD_STATE";
		public static final String FIELD_MODE = "FIELD_MODE";
		public static final String FIELD_COUNTRY = "FIELD_COUNTRY";
		public static final String FIELD_POSTAL_CODE = "FIELD_POSTAL_CODE";
		public static final String EXT_ATTR_KEY_BASE = "field_extattr_";
		public static final String FIELD_USER_STATE_ID = "FIELD_USER_STATE";
		public static final String FIELD_USER_MODE_ID = "FIELD_USER_MODE";
		public static final String FIELD_USER_ORG_MEM_ID = "FIELD_USER_ORG_MEM";
		public static final String FIELD_USER_ORG_ACL_ID = "FIELD_USER_ORG_ACL";
		public static final String FIELD_ATOMAPISOURCE_ID = "ATOMAPISOURCE";
		public static final String FIELD_SOURCE_URL_ID = "FIELD_SOURCE_URL";
		public static final String FIELD_SHIFT_ID = "FIELD_SHIFT";
		public static final String FIELD_COURTESY_TITLE = "FIELD_COURTESY_TITLE";
		public static final String FIELD_TITLE = "FIELD_TITLE";
		public static final String FIELD_TENANT_KEY = "FIELD_TENANT_KEY";
		// end field ids		
	}
	
	public static class AdminConstants {
		
		public static final String COUNTRY_CODE = "Country.do";
		public static final String DEPARTMENT_CODE = "Department.do";
		public static final String EMPTYPE_CODE = "EmployeeType.do";
		public static final String ORGANIZATION_CODE = "Organization.do";
		public static final String WORKLOC_CODE = "WorkLocation.do";
				
		public static final String CODE_ID = "codeId";
		public static final String ATTR_CODES_PREFIX = "com.ibm.snx_profiles.codes.";

		public static final String COUNTRY_CODE_TYPE = "country";
		public static final String DEPARTMENT_CODE_TYPE = "department";
		public static final String EMPTYPE_CODE_TYPE = "employeeType";
		public static final String ORGANIZATION_CODE_TYPE = "organization";
		public static final String WORKLOC_CODE_TYPE = "workLocation";
		
	}

}
