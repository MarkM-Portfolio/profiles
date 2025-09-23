/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2006, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.IOException;
import java.io.InputStream;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Person;
import org.apache.abdera.parser.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.Updatability;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions.OrderBy;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions.SortOrder;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;

import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.RetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;

public class AtomParser 
{
	private static final Log LOG = LogFactory.getLog(AtomParser.class);

	private static final Properties VCF_ASSOC = new Properties();
	private static final Properties FIELD_TYPE_ASSOC = new Properties();

	private final static String CONNECTION = "connection";

	private static final QName QN_KEY = new QName(AtomConstants.NS_SNX, PeoplePagesServiceConstants.KEY);
	private static final QName QN_GUID = new QName(AtomConstants.NS_SNX, PeoplePagesServiceConstants.GUID);
	private static final QName QN_UID = new QName(AtomConstants.NS_SNX, PeoplePagesServiceConstants.UID);
	private static final QName QN_USERID = new QName(AtomConstants.NS_SNX, PeoplePagesServiceConstants.USER_ID);
	private static final QName QN_SNX_REL = new QName(AtomConstants.NS_SNX, "rel");
	private static final QName QN_CONNECTION = new QName(AtomConstants.NS_SNX, "connection");

	private static final QName QN_CONTRIBUTOR = new QName(AtomConstants.NS_ATOM, "contributor");

	private final static String STATUS_SCHEME = "http://www.ibm.com/xmlns/prod/sn/status";

	private String lineValue = null;
	private int lineNumber = 0;

	static
	{
		try 
		{
			InputStream is = AtomParser.class.getResourceAsStream("profile_vcard_assoc.properties");
			VCF_ASSOC.load(is);

			is = AtomParser.class.getResourceAsStream("profile_field_type_assoc.properties");
			FIELD_TYPE_ASSOC.load(is);
			
			is.close();
		} 
		catch (IOException e) 
		{
			LOG.error(e.getLocalizedMessage(), e);
		}
	}

	public AtomParser() 
	{
	}

	public void updateEmployee(Employee employee, InputStream is) throws ParseException
	{
		Document<?> document = Abdera.getNewParser().parse(is);
		Entry entry = (Entry) document.getRoot();

		if (!isValidProfileEntryType(entry))
			throw parseError();

		List<Tag> profileTags = parseProfileTags(entry);
		employee.setProfileTags(profileTags);

		Map<String,Object> values = parseVCF(employee, entry.getContent(), true);
		employee.putAll(values);
	}

	public Connection buildConnection(InputStream is, String impliedSourceKey, String impliedTargetKey)
	{
		Connection conn = new Connection();
		Document<?> document = Abdera.getNewParser().parse(is);
		Entry entry = (Entry) document.getRoot();

		parseConnectionType(conn, entry);
		parseConnectionStatus(conn, entry);
		parseConnectionSourceTarget(conn, entry, impliedSourceKey, impliedTargetKey);		
		
		conn.setMessage(entry.getContent());
		if (conn.getMessage() == null)
			conn.setMessage("");
		
		return conn;
	}
	
	/**
	 * Parses out connection status type
	 * @param connectionStatus
	 * @return
	 */
	public static final int parseConnectionStatusString(String connectionStatus, int defaultValue)
	{
		if ("pending".equals(connectionStatus)) 
		{
			return Connection.StatusType.PENDING;
		} 
		else if ("accepted".equals(connectionStatus)) 
		{
			return Connection.StatusType.ACCEPTED;
		} 
		else if ("unconfirmed".equals(connectionStatus)) 
		{
			return Connection.StatusType.UNCONFIRMED;
		} 
		else 
		{
			return defaultValue;
		}
	}
	
	/**
	 * Parse out connection sort by option
	 * @param sortByString
	 * @param defaultValue
	 * @return
	 */
	public static final int parseConnectionsSortString(String sortByString, int defaultValue)
	{
		if ("displayName".equals(sortByString))
		{
			return RetrievalOptions.OrderByType.DISPLAY_NAME;
		}
		else if ("lastMod".equals(sortByString))
		{
			return RetrievalOptions.OrderByType.MOST_RECENT;
		}
		else
		{
			return defaultValue;
		}
	}
	
	
	private final static Map<String,SortOrder> sortOrders;
	static {
		HashMap<String,SortOrder> m = new HashMap<String,SortOrder>();
		m.put(SortOrder.ASC.getName(), SortOrder.ASC);
		m.put(SortOrder.DESC.getName(), SortOrder.DESC);
		sortOrders = Collections.unmodifiableMap(m);
	}	
	
	/**
	 * 
	 * @param sortOrder
	 * @param defaultValue
	 * @return
	 */
	public static final SortOrder parseProfilesSortOrder(String sortOrder, SortOrder defaultValue) {
		SortOrder r = sortOrders.get(sortOrder);
		if (r == null)
			return defaultValue;
		return r;
	}
	
	private final static Map<String,OrderBy> orderBys;
	static {
		HashMap<String,OrderBy> m = new HashMap<String,OrderBy>();
		m.put(OrderBy.DISPLAY_NAME.getName(), OrderBy.DISPLAY_NAME);
		m.put(OrderBy.SURNAME.getName(), OrderBy.SURNAME);
		m.put(OrderBy.LASTNAME.getName(), OrderBy.SURNAME); // 'last_name' is the same as 'surname'
		m.put(OrderBy.RELEVANCE.getName(), OrderBy.RELEVANCE);
		orderBys = Collections.unmodifiableMap(m);
	}
	
	/**
	 * 
	 * @param orderBy
	 * @param defaultValue
	 * @return
	 */
	public static final OrderBy parseProfilesOrderBy(String orderBy, OrderBy defaultValue) {
		OrderBy r = orderBys.get(orderBy);
		if (r == null)
			return defaultValue;		
		return r;
	}
	
	/**
	 * Parse out the 'sort order' option.
	 * @param sortOrderString
	 * @param defaultValue
	 * @return
	 */
	public static final int parseConnectionsSortOrderString(String sortOrderString, int defaultValue)
	{
		if ("asc".equals(sortOrderString))
		{
			return RetrievalOptions.SortOrder.ASC;
		}
		else if ("desc".equals(sortOrderString))
		{
			return RetrievalOptions.SortOrder.DESC;
		}
		else
		{
			return defaultValue;
		}
	}
	
	/**
	 * Parses the since paramater from the string
	 * 
	 * @param since
	 * @return
	 */
	public static final Date parseSince(String since)
	{
		try {
			long sinceT = Long.parseLong(since);
			if (sinceT > 0)
				return new Date(sinceT);
		} catch (NumberFormatException e) {
		}

		return null;
	}
	
	private void parseConnectionType(Connection conn, Entry entry)
	{
		try
		{
			List<Category> types = entry.getCategories("http://www.ibm.com/xmlns/prod/sn/type");
			List<Category> connectionTypes = entry.getCategories("http://www.ibm.com/xmlns/prod/sn/connection/type");
			
			if (types != null && types.size() == 1 && connectionTypes != null && connectionTypes.size() == 1)
			{
				Category type = types.get(0);
				Category connectionType = connectionTypes.get(0);
				if (CONNECTION.equals(type.getTerm()))
				{
					conn.setType(connectionType.getTerm());
					return;
				}
			}			
		}
		catch (Exception e)
		{			
			if (LOG.isDebugEnabled()) {
				LOG.debug("Invalid connection entry: " + e.getMessage(),e);
			}
		}
	
		throw parseError();
	}

	private void parseConnectionStatus(Connection conn, Entry entry)
	{
		List<Category> categories = entry.getCategories(STATUS_SCHEME);
		if (categories == null || categories.size() != 1)
			throw parseError();
		
		int status = parseConnectionStatusString(categories.get(0).getTerm(), Integer.MIN_VALUE);
		if (status == Integer.MIN_VALUE) {
			throw parseError();
		}
		
		conn.setStatus(status);
	}

	private void parseConnectionSourceTarget(Connection conn, Entry entry, String impliedSourceKey, String impliedTargetKey)
	{
		Element connection = entry.getExtension(QN_CONNECTION);
		if (connection != null)
		{
		  for (Element childElement : connection.getElements()) {
		    if (QN_CONTRIBUTOR.equals(childElement.getQName())) {
		      Person person = (Person)childElement;
		      String snxRel = person.getAttributeValue(QN_SNX_REL);
		      if (AtomGenerator2.SNX_REL_SOURCE.equals(snxRel)) {
		        conn.setSourceKey(parseConnectionPerson(person));
		      } else if (AtomGenerator2.SNX_REL_TARGET.equals(snxRel)) {
		        conn.setTargetKey(parseConnectionPerson(person));
		      }
		    }
		  }
		}			
		if (conn.getSourceKey() == null)
		{
			conn.setSourceKey(impliedSourceKey);
		}
		
		if (conn.getTargetKey() == null)
		{
			conn.setTargetKey(impliedTargetKey);
		}
	}

	private String parseConnectionPerson(Person person) 
	{
		try 
		{
			ProfileLookupKey plk = null;
			
			Element el;
			if ((el = person.getExtension(QN_USERID)) != null) {
				plk = ProfileLookupKey.forUserid(el.getText());
			} else if ((el = person.getExtension(QN_KEY)) != null) {
				plk = ProfileLookupKey.forKey(el.getText());
			} else if ((el = person.getExtension(QN_UID)) != null) {
				plk = ProfileLookupKey.forUid(el.getText());
			} else if ((el = person.getExtension(QN_GUID)) != null) {
				plk = ProfileLookupKey.forGuid(el.getText());
			} else if (person.getEmail() != null) {
				plk = ProfileLookupKey.forEmail(person.getEmail());
			} else {
				throw parseError();
			}
			
			Employee profile = 
				AppServiceContextAccess.getContextObject(PeoplePagesService.class).getProfile(
						plk, ProfileRetrievalOptions.MINIMUM);
			
			if (profile == null)
				throw parseError();
			
			return profile.getKey();
		} 
		catch (DataAccessRetrieveException e) 
		{
			throw parseError(e);
		}
		catch (AssertionException e)
		{
			throw parseError(e);
		}
	}

	public List<Tag> parseTagsFeed(InputStream is) {
		Document<Categories> document = AtomConstants.factory.newParser().parse(is);
		Categories categories = document.getRoot();
		List<Tag> tags = new ArrayList<Tag>();
		
		for (Category c : categories.getCategories())
		{
			// tag input values
			String tagValue = c.getTerm();
			String tagScheme = c.getScheme() != null ? c.getScheme().toASCIIString() : "";
			String type = AtomParser3.tagTypeFromScheme(tagScheme);

			TagConfig tagConfig = DMConfig.instance().getTagConfigs().get(type);
			if (tagConfig != null) {
				if (tagValue != null && tagValue.length() > 0 && type != null && type.length() > 0) {
					// if phrases are not supported, we split on space character
					if (!tagConfig.isPhraseSupported()) {
						String[] terms = tagValue.split(" ");
						for (String term : terms) {
							Tag tag = new Tag();
							tag.setTag(term);
							tag.setType(type);
							tags.add(tag);
						}
					}
					else
					{
						Tag tag = new Tag();
						tag.setType(type);
						tag.setTag(tagValue);
						tags.add(tag);
					}									
				}
			}																		
		}
		return tags;
	}

	public List<EmployeeRole> parseRolesFeed(InputStream is)
	{
		List<EmployeeRole> roles = new ArrayList<EmployeeRole>();

		Document<Feed> document = AtomConstants.factory.newParser().parse(is);

		Feed rolesFeed = document.getRoot();

		try {
			for (Entry entry : rolesFeed.getEntries())
			{
				if (LOG.isDebugEnabled()) {
					LOG.debug("Entry Id: " + entry.getId());
					LOG.debug("Summary:  " + entry.getSummary());
				}
				String roleID  = entry.getId().toString();
				if (LOG.isDebugEnabled()) {
					LOG.debug("parseRolesFeed : " + roleID);
				}
				if (roleID != null && roleID.length() > 0)
				{
					String[] terms = roleID.split(" ");
					for (String term : terms)
					{
						EmployeeRole role = new EmployeeRole();
						// only need to set the Role ID since the service layer
						// can figure the rest out from context
						// EmployeeRole.isValid(term)) is called in role.setRoleId 
						role.setRoleId(term);
						if (LOG.isDebugEnabled()) {
							LOG.debug("parseRolesFeed : adding role " + role); 
						}
						roles.add(role);
					}
				}
			}
		}
		catch (Exception ex) {
			LOG.error("Error: " + ex.getMessage());
		}
		return roles;
	}

	public boolean isValidProfileEntryType(Entry entry)
	{
		List<Category> types = entry.getCategories("http://www.ibm.com/xmlns/prod/sn/type");

		if (types != null && types.size() > 0)
		{
			Category type = types.get(0);
			if ("profile".equals(type.getTerm()))
			{
				return true;
			}
		}

		return false;
	}
	
	public List<Tag> parseProfileTags(Entry entry) 
	{
		List<Category> profileTagElem = entry.getCategories();
		List<Tag> profileTags = new ArrayList<Tag>(profileTagElem.size());
		for (int i = 0; i < profileTagElem.size(); i++) {
			Category profileTag = profileTagElem.get(i);
			// TODO handle schemes so we can have other tag scopes
			if (profileTag.getTerm() != null && profileTag.getScheme() == null) {
				Tag aTag = new Tag();
				aTag.setType(TagConfig.DEFAULT_TYPE);
				aTag.setTag(profileTag.getTerm());
				profileTags.add(aTag);
			}
		}
		return profileTags;
	}

	public Map<String,Object> parseVCF(Employee sourceRecord, String vcard, boolean filterEditable)
	throws ParseException
	{
		ProfileType profileType = ProfileTypeHelper.getProfileType(sourceRecord.getProfileType());
		
		
		lineNumber = 0;   // reset
		lineValue = null; // "  "

		HashMap<String,Object> p = new HashMap<String,Object>();
		BufferedStringTokenizer st = new BufferedStringTokenizer(new StringTokenizer(vcard,"\n"));

		boolean seenEnd = false;

		consumeToken(st,"BEGIN:VCARD");

		while (st.hasMoreTokens() && !seenEnd)
		{
			String token = nextLine(st);
			if (eqi(token,"END:VCARD"))
			{
				seenEnd = true;
			}
			else
			{
				String[] key_value = splitKeyValue(token);
				String profileField = VCF_ASSOC.getProperty(key_value[0]);
				Property propertyDef = profileType.getPropertyById(profileField);				
				boolean isEditable = propertyDef != null && Updatability.READWRITE.equals(propertyDef.getUpdatability());
				
				if (propertyDef != null &&
						(!filterEditable
								|| isEditable))
				{
					String fieldType = FIELD_TYPE_ASSOC.getProperty(profileField,"java.lang.String");

					if ("java.lang.String".equals(fieldType))
					{
						p.put(profileField, key_value[1]);
					}
					else if ("java.sql.Timestamp".equals(fieldType))
					{
						try {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'S'Z'");
							sdf.getCalendar().setTimeZone(TimeZone.getTimeZone("GMT"));

							Date date = sdf.parse(key_value[1]);
							p.put(profileField, new Timestamp(date.getTime()));

						} catch (java.text.ParseException e) {
							throw parseError(e);
						}
					}
					// format => ;;address_1,address_2;city;state;postal_code;country
					else if ("com.ibm.peoplepages.data.WorkLocation".equals(fieldType))
					{
						Map<String,String> vals = new HashMap<String,String>();
						String[] addr_lines = key_value[1].split(";");

						if (addr_lines.length != 7)
							throw parseError();
						String [] addr_12 = addr_lines[2].split("\\,");
						if (addr_12.length > 2)
							throw parseError();

						// skip lines [0],[1]							
						if (addr_12.length > 0) vals.put(WorkLocation.F_ADDRESS1.getName(), addr_12[0]);
						if (addr_12.length > 1) vals.put(WorkLocation.F_ADDRESS2.getName(), addr_12[1]);						
						vals.put(WorkLocation.F_CITY.getName(), addr_lines[3]);
						vals.put(WorkLocation.F_STATE.getName(), addr_lines[4]);
						vals.put(WorkLocation.F_POSTALCODE.getName(), addr_lines[5]);
						//wl.setCountry(addr_lines[6]); - not implemented

						p.put(profileField, new WorkLocation("", vals));
					}
				}
				else if (key_value[0].startsWith("X_EXTENSION_PROPERTY"))
				{
					Map<String,String> extProp = parseMultiValue(key_value, "X_EXTENSION_PROPERTY");

					String propertyId = extProp.get("X_EXTENSION_PROPERTY_ID");
					String propertyValue = extProp.get("X_EXTENSION_VALUE");

					if (propertyId == null || propertyValue == null)
						throw parseError();

					String attrId = Employee.getAttributeIdForExtensionId(propertyId);

					Property extensionPropertyDef = profileType.getPropertyById(attrId);
					boolean isEditableExtension = extensionPropertyDef != null && Updatability.READWRITE.equals(extensionPropertyDef.getUpdatability());
					ProfileExtension pe = new ProfileExtension();
					pe.setPropertyId(propertyId);
					pe.setStringValue(propertyValue);
					pe.setKey(sourceRecord.getKey());

					if (!filterEditable || isEditableExtension)
						p.put(attrId, pe);
				}
				// AGENT;VALUE=X_PROFILE_UID:938139897;VALUE=FN:MICHAEL I. AHERN;VALUE=URL:http://wd40.lotus.com/profiles/atom/profile.do?uid=938139897
				else if (key_value[0].startsWith("AGENT"))
				{
					Map<String,String> sec = parseMultiValue(key_value, "AGENT");
					String secretaryUid = null;
					if (sec.containsKey("X_PROFILE_KEY")) {
						PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
						secretaryUid = pps.getLookupForPLK(Type.UID, ProfileLookupKey.forKey(sec.get("X_PROFILE_KEY")), false);
					} else if (sec.containsKey("X_LCONN_USERID")) {
						PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
						secretaryUid = pps.getLookupForPLK(Type.UID, ProfileLookupKey.forUserid(sec.get("X_LCONN_USERID")), false);
					} else if (sec.containsKey("X_PROFILE_UID")) {
						secretaryUid = sec.get("X_PROFILE_UID");
					} else {
						throw parseError();
					}
					p.put("secretaryUid", secretaryUid);
				}
			}			
		}		

		return p;
	}

	private static final String multiValueTokens = ":=;\\";

	private Map<String,String> parseMultiValue(String [] key_value, String firstToken)
	{
		Map<String,String> r = new HashMap<String,String>();

		String fullField = key_value[0] + ':' + key_value[1];
		BufferedStringTokenizer ep = new BufferedStringTokenizer(new StringTokenizer(fullField,multiValueTokens,true));

		consumeToken(ep, firstToken, false);

		String nextToken = null; // used to implement pseudo- peek() method
		while (ep.hasMoreTokens())
		{
			if (nextToken == null) {
				consumeToken(ep, ";", false);
			} else if (!";".equals(nextToken)) {
				throw parseError();
			} else {
				nextToken = null; // reset
			}

			consumeToken(ep, "VALUE", false);
			consumeToken(ep, "=", false);

			String propKey = nextToken(ep).toUpperCase();
			if (r.containsKey(propKey))
				throw parseError();
			consumeToken(ep,":",false);

			// parse value
			StringBuilder value = new StringBuilder();
			boolean continueLoop = !";".equals(ep.peak()) && ep.peak() != null;
			Boolean startEscape = continueLoop ? null : false;

			while (continueLoop)
			{
				String t = (nextToken == null) ? nextToken(ep) : nextToken;

				// (First time through)
				if (startEscape == null)
					startEscape = "\\".equals(t);

				// reset
				nextToken = null;
				continueLoop = false;

				// is special token
				if ("\\".equals(t)) 
				{
					t = nextToken(ep);
					if (multiValueTokens.indexOf(t) == -1) throw parseError();
					value.append(t);

					if (ep.hasMoreTokens() &&
							("\\".equals((nextToken = nextToken(ep))) 
									|| multiValueTokens.indexOf(nextToken) == -1))
					{
						continueLoop = true;
					}
				}
				// handle basic case
				else
				{
					value.append(t);

					if (ep.hasMoreTokens() && "\\".equals(nextToken = nextToken(ep))) 
					{
						continueLoop = true;
					}
				}					
			}

			// handle special case of empty value
			String valueString = value.toString();
			if (!startEscape && ";".equals(valueString))
			{
				ep.pushBack(nextToken);
				nextToken = valueString;
				r.put(propKey, "");
			}				
			else
			{
				r.put(propKey,valueString);
			}
		}

		return r;
	}

	private final String nextToken(BufferedStringTokenizer st)
	{
		if (!st.hasMoreTokens())
			throw parseError();
		return st.nextToken();
	}

	private final void consumeToken(BufferedStringTokenizer st, String token)
	throws ParseException
	{
		consumeToken(st,token,true);
	}

	private final void consumeToken(BufferedStringTokenizer st, String token, boolean incrLine)
	{
		String t = (incrLine) ? nextLine(st) : nextToken(st);
		if (!token.equalsIgnoreCase(t))
			throw parseError();
	}

	private final String nextLine(BufferedStringTokenizer st)
	throws ParseException
	{
		lineNumber++;
		lineValue = "";
		while (st.hasMoreTokens() && (lineValue = st.nextToken()).trim().equals(""))
			lineNumber++;
		return lineValue.trim();
	}

	private final boolean eqi(String t1, String t2)
	{
		return t1.equalsIgnoreCase(t2);
	}

	private final String[] splitKeyValue(String line)
	throws ParseException
	{
		int splitPoint = line.indexOf(':');
		if (splitPoint < 0)
			throw parseError();

		return  new String[]{
				line.substring(0, splitPoint).toUpperCase(),
				decode(line.substring(splitPoint+1))
		};
	}

	private final String decode(String valueStr) 
	{
		return valueStr;
	}

	private final ParseException parseError()
	{
		return new ParseException(lineNumber + ": " + lineValue);
	}

	private final ParseException parseError(Exception e)
	{
		return new ParseException(lineNumber + ": " + lineValue, e);
	}

	private static class BufferedStringTokenizer 
	{
		private final StringTokenizer st;
		private final Stack<String> stack = new Stack<String>();

		public BufferedStringTokenizer(StringTokenizer st)
		{
			this.st = st;
		}

		public String nextToken()
		{
			if (!stack.empty())
				return stack.pop();

			return st.nextToken();
		}

		public boolean hasMoreTokens()
		{
			return (!stack.empty() || st.hasMoreTokens());
		}

		public void pushBack(String token)
		{
			stack.push(token);
		}

		public String peak()
		{
			if (hasMoreTokens())
			{
				String t = nextToken();
				pushBack(t);
				return t;
			}
			else
			{
				return null;
			}
		}
	}
}
