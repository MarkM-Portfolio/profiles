/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.writer.WriterFactory;

import com.ibm.lconn.core.web.atom.util.LCAtomConstants;

public interface AtomConstants 
{
	public static final Factory factory = Abdera.getNewFactory();
	public static final WriterFactory writerFactory = Abdera.getNewWriterFactory();
	
	public static final String APP_MIME_TYPE = "application/atomcat+xml";
	public static final String APP_CONTENT_TYPE = "application/atomcat+xml;charset=UTF-8";
	public static final String ATOM_MIME_TYPE = "application/atom+xml";
	public static final String ATOM_CONTENT_TYPE = "application/atom+xml;charset=UTF-8";
	public static final String JSON_CONTENT_TYPE = "application/json";
	public static final String PROFILE_TYPE_CONTENT_TYPE = "application/profile-type+xml";
	public static final String XML_CONTENT_TYPE = "application/xml";
	
	public static final String XML_ENCODING = "UTF-8";
	public static final String XML_VERSION = "1.0";
	
	public static final String NS_ATOM = "http://www.w3.org/2005/Atom";
	public static final String NS_APP = "http://www.w3.org/2007/app";
	public static final String NS_SNX = "http://www.ibm.com/xmlns/prod/sn";
	public static final String NS_FH = "http://purl.org/syndication/history/1.0";
	public static final String NS_OPENSOCIAL = 	"http://ns.opensocial.org/2008/opensocial";
	
	public static final String XMLNS_ATOM = "xmlns:atom";
	
	public static final String SCHEME_STRUCT_TAG =  "http://www.ibm.com/xmlns/prod/sn/struct-tag";
	
	public static final String PRE_ATOM = "atom";
	public static final String PRE_APP = "app";
	public static final String PRE_SNX = "snx";	
	public static final String PRE_FH = "fh";	
	
	public static final String CATEGORIES = "categories";
	public static final String CATEGORY = "category";
	public static final String ENTRY = "entry";
	public static final String TERM = "term";
	public static final String FREQUENCY = "frequency";
	public static final String TYPE = "type";
	public static final String INTENSITY_BIN = "intensityBin";
	public static final String VISIBILITY_BIN = "visibilityBin";
	public static final String FLAGGED = "flagged";
	public static final String FIXED = "fixed";
	public static final String SCHEME = "scheme";
	public static final String NODE_ID = "nodeId";
	public static final String STRUCT_TAG = "structTag";

	public static final String ROLES = "roles";
	public static final String ROLE = "role";
	public static final String ROLE_ID = "roleId";
	public static final String ROLE_KEY = "roleKey";
	public static final String CREATED = "created";

	public static final String ORG_ID = "orgId";

	public static final String GENERATOR = "generator";
	public static final String VERSION = "version";
	public static final String GENERATOR_VERSION = LCAtomConstants.LC_API_VERSION;
	public static final String GENERATOR_NAME = "IBM Connections - Profiles";
	
	public static final String PROFILE_GUID = "profileGuid";
	public static final String PROFILE_UID = "profileUid";
	public static final String PROFILE_KEY = "profileKey";

    public static final String PROF_EXIDS = "exids";
    public static final String PROF_KEYS  = "keys";

	public static final String HEADER_LOCATION = "Location";
	
	public static final String SERVICE_TITLE = "Profiles Administration Workspace";
	public static final String SERVICE_TITLE_ALL = "All User Profiles";
	
	public static final String LINK_REL_TAG_CLOUD = "http://www.ibm.com/xmlns/prod/sn/tag-cloud";
	public static final String LINK_REL_COLLEAGUE_CONNECTIONS = "http://www.ibm.com/xmlns/prod/sn/connections/colleague";
	public static final String LINK_REL_CONNECTIONS = "http://www.ibm.com/xmlns/prod/sn/connections";
	public static final String LINK_REL_CONNECTIONS_EXTENSION = "http://www.ibm.com/xmlns/prod/sn/connections/ext";
	public static final String LINK_REL_CONNECTION = "http://www.ibm.com/xmlns/prod/sn/connection";
	public static final String LINK_REL_THEBOARD = "http://www.ibm.com/xmlns/prod/sn/mv/theboard";
	public static final String LINK_REL_STATUS = "http://www.ibm.com/xmlns/prod/sn/status";
	public static final String LINK_REL_THEBOARD_OPENSOCIAL = "http://activitystrea.ms/spec/1.0/";
	public static final String LINK_REL_STATUS_OPENSOCIAL = "http://www.ibm.com/xmlns/prod/sn/status/opensocial";
	public static final String LINK_REL_EXTENSION_ATTRIBUTE = "http://www.ibm.com/xmlns/prod/sn/ext-attr";
	
	public static final String LINK_REL_REPORTING_STRUCT = "http://www.ibm.com/xmlns/prod/sn/reporting-chain";
	public static final String LINK_REL_PEOPLE_MANAGED = "http://www.ibm.com/xmlns/prod/sn/people-managed";
	
	public static final String LINK_REL_SERVICE = "http://www.ibm.com/xmlns/prod/sn/service/";

	// admin service document links
	public static final String LINK_REL_PROFILES_SERVICE    = "http://www.ibm.com/xmlns/prod/sn/profiles";
	public static final String LINK_REL_PROFILE_ENTRY_SERVICE = "http://www.ibm.com/xmlns/prod/sn/profileEntry";
	public static final String LINK_REL_TAG_SERVICE         = "http://www.ibm.com/xmlns/prod/sn/profileTags";
	public static final String LINK_REL_ROLES_SERVICE       = "http://www.ibm.com/xmlns/prod/sn/profileRoles";
	public static final String LINK_REL_FOLLOWING_SERVICE   = "http://www.ibm.com/xmlns/prod/sn/following";
	public static final String LINK_REL_CONNECTIONS_SERVICE = "http://www.ibm.com/xmlns/prod/sn/connections";
	public static final String LINK_REL_CONNECTION_SERVICE  = "http://www.ibm.com/xmlns/prod/sn/connection";
	public static final String LINK_REL_CODES_SERVICE       = "http://www.ibm.com/xmlns/prod/sn/codes";

	public static final QName QN_EXTENSION_ID = new QName(NS_SNX,"extensionId", PRE_SNX);
	public static final QName QN_SERVICE_NAME = new QName(NS_SNX,"serviceName", PRE_SNX);
	public static final QName QN_CONNECTION_TYPE_CONFIG = new QName(NS_SNX, "connectionTypeConfig", PRE_SNX);
	public static final QName QN_CONNECTION_TYPE = new QName(NS_SNX, "connectionType", PRE_SNX);
	public static final QName QN_TYPE = new QName(NS_SNX, "type", PRE_SNX);
	
	public static final String NS_TAG_SCHEME_BASE = NS_SNX + "/scheme/";
	
	public static final QName QN_TAGS_CONFIG = new QName(NS_SNX, "tagsConfig", PRE_SNX);
	public static final QName QN_TAG_CONFIG = new QName(NS_SNX, "tagConfig", PRE_SNX);
}
