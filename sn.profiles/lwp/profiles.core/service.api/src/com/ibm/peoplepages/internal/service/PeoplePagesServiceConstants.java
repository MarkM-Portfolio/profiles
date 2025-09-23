/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service;

public class PeoplePagesServiceConstants
{
	public static final String PROFILES = "profiles";
	public static final String FILES = "files";
	public static final String COMMUNITIES = "communities";
	public static final String PROFILE = "profile";
	public static final String WALTZ_ADMIN = "dsx-admin";
	
	// obsolete
	// see com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants
	public static final String ROLE_READER = "reader";
	public static final String ROLE_USER   = "user";
	public static final String ROLE_ADMIN  = "admin";  // should also be obsolete

	/**
	 * Query/Update parameters
	 */
	//TODO These need to be rectified with PropertyEnum.
	public static final String CITY = "city";
	public static final String COMMUNITY = "community";
	public static final String CONNECTIONS = "connectionsFor";
	public static final String CONNECTIONS_IN_COMMON = "connectionsInCommon";
	public static final String CONNECTION_ID = "connectionId";
	public static final String CONNECTION_TYPE = "connectionType";
	public static final String INCL_MESSAGE = "inclMessage";
	public static final String COLLEAGUE = "colleague";
	public static final String COUNTRY = "country";
	public static final String DEPARTMENT = "department";
	public static final String DESCRIPTION = "description";
	public static final String EMAIL = "email";
	public static final String EXPERIENCE = "experience";
	public static final String FIRSTNAME = "firstname";
	public static final String GROUPWARE_EMAIL = "gwemail";
	public static final String GUID = "guid";
	public static final String ID_KEY = "idKey";
	public static final String LASTNAME = "lastname";
	public static final String LOGIN = "login";
	public static final String LOGIN_ID = "loginId";
	public static final String JOB_RESPONSIBILITIES = "jobTitle";
	public static final String KANJI_NAME = "kanjiName";
	public static final String KEYWORD = "keyword";
	public static final String USER_ID = "userid"; // AKA 'x-lconn-userid'
	public static final String LOCATION = "location";
	public static final String NAME = "name";
	public static final String ORGANIZATION = "organization";
	public static final String PROFILE_LINKS = "profileLinks";
	public static final String DEPTCODE = "deptCode";
	public static final String PHONE_NUMBER = "phoneNumber";
	public static final String PREFERRED_FIRST_NAME = "preferredFirstName";
	public static final String PREFERRED_LAST_NAME = "preferredLastName";
	public static final String PROFILE_TAGS = "profileTags";
	public static final String PROFILE_TAGS_CLOUD = "profileTagCloud";
	public static final String PROFILE_TAGS_FOR = "profileTagCloudFor";
	public static final String PROFILE_ROLES_FOR = "profileRolesFor";
	public static final String SEARCH = "search";
	public static final String STATE = "state";
	public static final String TENANT_KEY = "tenantKey";
	public static final String HOME_TENANT_KEY = "homeTenantKey";
	public static final String TIMEZONE = "timezone";
	public static final String TYPE = "type";
	public static final String UID = "uid";
	public static final String KEY = "key";
	public static final String ORGID = "orgId";
	public static final String USERNAME = "username";
	public static final String USERNAME_LITE = "usernameLite";
	public static final String PEOPLE_MANAGED="peopleManagedBy";
	public static final String REPORTING_CHAIN="reportingChainFor";
	public static final String RESOLVE_USER="resolvedUser";
	public static final String WORK_LOCATION="workLocation";
	public static final String DISPLAY_NAME="displayName";
	public static final String IS_MANAGER="isManager";
	public static final String MANAGER_UID="managerUid";
	public static final String DN="distinguishedName";
	public static final String DNAME="dn";
	public static final String SOURCE_URL="sourceUrl";
	public static final String MCODE="mcode";

	// ids for codes API
	public static final String CODE_ID = "codeId";
	public static final String CCODE   = "countryCode";
	public static final String DCODE   = "departmentCode";
	public static final String ECODE   = "employeeType";
	public static final String OCODE   = "orgCode";
	public static final String WCODE   = "workLocationCode";
	// not clear why we have both of these in use
	public static final String WORK_LOC_CODE="workLoc";

	// Profiles API for visitor model
	public static final String MODE="mode";
	public static final String MODE_INTERNAL="internal";
	public static final String MODE_EXTERNAL="external";
	public static final String MODE_ALL="all";

	// Profiles API for visitor model for DSX search.do
	public static final String VM_SCOPE="scope";
	public static final String VM_SCOPE_INTERNAL="1";
	public static final String VM_SCOPE_EXTERNAL="2";
	public static final String VM_SCOPE_ALL="3";

	public static final String IS_EXTERNAL="isExternal";

	public static final String DISPLAYED_PROFILE="displayedProfile";
	
	// Profiles API for visitor model Roles
	public static final String ROLE   = "role";
	public static final String ROLEID = "roleId";

    public static final String ROLE_EMPLOYEE = "employee";
    public static final String ROLE_EXTENDED = "employee.extended";
    public static final String ROLE_VISITOR  = "visitor";
	public static final String HAS_EXTENDED  = "hasExtendedRole";

	public static final String ACTIVE_USERS_ONLY = "activeUsersOnly";

	// used by Admin API to build attribute "keys". these prefixes were used in 3.0
	// when there were three 'classes' of attributes: base, extension, system.
	// system attributes are obsolete.
	public static final String ATTR_PREFIX = "com.ibm.snx_profiles.";
	public static final String ATTR_PREFIX_BASE = ATTR_PREFIX + "base.";
	public static final String ATTR_PREFIX_EXT = ATTR_PREFIX + "ext.";
	public static final String ATTR_PREFIX_CODES = ATTR_PREFIX + "codes.";
	public static final String ATTR_PREFIX_SYS = ATTR_PREFIX + "sys.";
	
	public static final String EXT_ATTR_KEY_BASE = "extattr.";

	public static final String FORMAT = "format";
	public static final String FULL = "full";
	public static final String HCARD = "hcard";
	public static final String OUTPUT = "output";
	public static final String PAGE = "page";
	public static final String PAGE_SIZE = "ps";
	public static final String ITER_STATE = "iterState";
	public static final String VCARD = "vcard";
	public static final String CONNECTION = "connection";
	public static final String XML = "xml";
	public static final String LABELS = "labels";
	public static final String LANG = "lang";


	//Added for Profile type
	public static final String PROF_TYPE = "profileType";
	public static final String DEFAULT = "default";

	// Other stuff
	public static final String LAST_MOD = "lastMod";
	public static final String LAST_UPDATE = "lastUpdate";
	public static final String LAST_PHOTO_UPDATE = "lastPhotoUpdate";
	public static final String LAST_LOGIN = "lastLogin";

	// Tag cloud
	public static final String FLAG_BY_KEY = "flagByKey";
	public static final String FLAG_BY_EMAIL = "flagByEmail";
	public static final String FLAG_BY_UID = "flagByUid";
	public static final String FLAG_BY_GUID = "flagByGuid";
	public static final String FLAG_BY_USERID = "flagByUserid";

	public static final String TARGET_KEY = "targetKey";
	public static final String TARGET_UID = "targetUid";
	public static final String TARGET_EMAIL = "targetEmail";
	public static final String TARGET_GUID = "targetGuid";
	public static final String TARGET_USERID = "targetUserid";
	public static final String SOURCE_KEY = "sourceKey";
	public static final String SOURCE_KEYS = "sourceKeys";
	public static final String SOURCE_UID = "sourceUid";
	public static final String SOURCE_EMAIL = "sourceEmail";
	public static final String SOURCE_GUID = "sourceGuid";
	public static final String SOURCE_USERID = "sourceUserid";
	public static final String PROF_STRUCT_TAGS = "profileStructTags";

	public static final String TAG = "tag";
	public static final String TAGID = "tagId";
	public static final String EXTENSION_AWARE = "extensionAware";
	public static final String FROM_TYPE = "fromType";
	public static final String TO_TYPE = "toType";
	public static final String GENERIC_TAG_TYPE = "generic";
	
	// Added for profile extensions
	public static final String PROPERY_ID = "propertyId";
	public static final String EXTENSION_ID = "extensionId";

	public static final String CHARENC_UTF8 = "UTF-8";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String MIME_TEXT_XML = "text/xml";
	public static final String MIME_TEXT_HTML = "text/html";
	public static final String JSON_TEXT = "application/json";
	
	// for Admin Connection creation
	public static final String ACTION = "action";

	/**
	 * Query parameters from configuration
	 */
	public static final String ORG_STRUCTURE_ENABLED = "orgStructureEnabled";

	/**
	 * Other constants
	 */
	public static final String USERID_PROPERTY = "USERID_PROPERTY";
	
	public static final String HTTP_GET = "GET";
	public static final String HTTP_POST = "POST";
	public static final String HTTP_PUT = "PUT";
	public static final String HTTP_HEAD = "HEAD";
	public static final String HTTP_DELETE = "DELETE";

	public static final char STRUCT_TAG_DELIMITER = '.';
	public static final String ADMIN_EMPLOYEE = "admin_employee";
}
