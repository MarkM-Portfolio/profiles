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

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

import com.ibm.lconn.profiles.api.actions.ResourceManager;

import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.api.actions.AtomConstants;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.data.GivenName;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Surname;
import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.lconn.profiles.data.codes.Country;
import com.ibm.lconn.profiles.data.codes.Department;
import com.ibm.lconn.profiles.data.codes.EmployeeType;
import com.ibm.lconn.profiles.data.codes.Organization;
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileResolver2;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;

public class AtomParser3
{
	private static final Log LOG = LogFactory.getLog(AtomParser3.class);

	private static final Properties VCF_ASSOC = new Properties();
	private static final Properties FIELD_TYPE_ASSOC = new Properties();

	private String ATTR_CODES_PREFIX = "com.ibm.snx_profiles.codes.";

	private static final String ATTRIBUTE_ID = "key";
	private static final String TYPE_ID = "type";
	private static final String PROFILE_NAMESPACE = "";

	private static final QName QN_PROFILE = new QName(AtomConstants.NS_SNX, "profile");
	private static final QName QN_ENTRY = new QName("", "entry");
//	private static final QName QN_ENTRY = new QName(AtomConstants.NS_SNX , "entry");

	public static final QName QN_KEY = new QName(PROFILE_NAMESPACE, "key");
	public static final QName QN_VALUE = new QName(PROFILE_NAMESPACE, "value");
	public static final QName QN_TYPE = new QName(PROFILE_NAMESPACE, "type");
	public static final QName QN_DATA = new QName(PROFILE_NAMESPACE, "data");

	private static final QName QN_WORK_LOC = new QName("", PeoplePagesServiceConstants.WORK_LOC_CODE);
	private static final QName QN_DEPARTMENT_CODE = new QName("", PeoplePagesServiceConstants.DCODE);
	private static final QName QN_CCODE = new QName("", PeoplePagesServiceConstants.CCODE);
	private static final QName QN_OCODE = new QName("", PeoplePagesServiceConstants.OCODE);
	private static final QName QN_ECODE = new QName("", PeoplePagesServiceConstants.ECODE);

	private static final String GIVEN_NAMES = "givenNames";
	private static final String SURNAMES    = "surnames";
	private static final String LOGINS      = "logins";
	private static final String USER_MODE   = "userMode";

	public AtomParser3() 
	{
	}

	public void parseEmployee(ProfileDescriptor pd, InputStream is) throws ParseException
	{
		parseProfile(pd, is);
	}

	public List <AbstractCode <?>> parseCodes(InputStream is) throws Exception 
	{
		Document<?> document = null;
		Element root = null;
		Element el   = null;
		List <AbstractCode <?>> alist = null;

		try {
			document = Abdera.getNewParser().parse(is);
			root  = (Element) document.getRoot();
			el    = root.getFirstChild();
			alist = new ArrayList <AbstractCode <?>>();

			if (el == null)
				throw new APIException(ECause.INVALID_XML_CONTENT);

			Element rootChild = el;
			el = el.getNextSibling(new QName("", "content"));
			if (el == null) {
				// if user did not specify xmlns="" in the pay-load, we will not find "content" as a QName above
				// loop over the entry's child elements looking for "content"
				boolean contentFound = false;
				el = rootChild.getNextSibling();
				while (! contentFound && (null != el))
				{
					QName qName = el.getQName();
					if (null != qName) {
						String name = qName.getLocalPart();
						if ("content".equalsIgnoreCase(name))
							contentFound = true;
						else
							el = el.getNextSibling();
					}
				}
				if (!contentFound)
					throw new APIException(ECause.INVALID_XML_CONTENT);
			}

			if (null != el) {
				el = el.getFirstChild();	// appData
				if (el == null)
					throw new APIException(ECause.INVALID_XML_CONTENT);
				// process appData
				Element attr = el.getFirstChild();
				String attrName = null;
				String attrVal  = null;
				Object o = null;
				HashMap<String,Object> p = new HashMap<String,Object>();
				String codeType = null;

				while (attr != null)
				{
					attrName = attr.getQName().getLocalPart();
					// strip the prefix
					attrName = attrName.substring(ATTR_CODES_PREFIX.length());
					// strip the code type
					codeType = attrName.substring(0, attrName.indexOf('.'));
					attrName = attrName.substring(attrName.indexOf('.')+1);
					attrName = attrName.trim();

					// get the value, get the data
					attrVal = attr.getText().trim();
					o = attrVal;

					p.put(attrName, o);
					attr = attr.getNextSibling();
				}
				alist.add(createCodeObject(codeType, p));
			}
		}
		catch (Exception e) {
			if (e instanceof org.apache.abdera.parser.ParseException)
				throw new APIException(ECause.INVALID_XML_CONTENT);
			else
				throw e;
		}
		return (alist);		
	}

	private AbstractCode<?>  createCodeObject(String codeType, Map<String, Object> map) {
		String codeId = null;
		AbstractCode<?> ac = null;

		if (codeType.equalsIgnoreCase(PeoplePagesServiceConstants.COUNTRY)) {
			codeId = map.get(PeoplePagesServiceConstants.CCODE).toString();
			
			ac = new Country(codeId, map);		
		}
		else if (codeType.equalsIgnoreCase(PeoplePagesServiceConstants.DEPARTMENT)) {
			codeId = map.get(PeoplePagesServiceConstants.DCODE).toString();
			
			ac = new Department(codeId, map);		
		}
		else if (codeType.equalsIgnoreCase(PeoplePagesServiceConstants.ECODE)) {
			codeId = map.get(PeoplePagesServiceConstants.ECODE).toString();
			
			ac = new EmployeeType(codeId, map);		
		}
		else if (codeType.equalsIgnoreCase(PeoplePagesServiceConstants.ORGANIZATION)) {
			codeId = map.get(PeoplePagesServiceConstants.OCODE).toString();
			
			ac = new Organization(codeId, map);		
		}
		else if (codeType.equalsIgnoreCase(PeoplePagesServiceConstants.WORK_LOCATION)) {
			codeId = map.get(PeoplePagesServiceConstants.WCODE).toString();
			
			ac = new WorkLocation(codeId, map);		
		}
		
		return ac;
			
	}
	
	public String createEmployee(InputStream is)
	{
		Employee employee = new Employee();

		Document<?> document = Abdera.getNewParser().parse(is);
		Entry entry = (Entry) document.getRoot();

		
		return employee.getKey();
	}


	// parse from a feed
	public void parseProfile(ProfileDescriptor pd, InputStream is) throws ParseException
	{
		Employee e = ProfileResolver2.resolveProfile(pd.getProfile(), new ProfileRetrievalOptions());

		Document<?> document = Abdera.getNewParser().parse(is);
		Element root = (Element) document.getRoot();

		Element el = null; 
		
		if ((el = root.getFirstChild(new QName(AtomConstants.NS_ATOM, "content"))) == null) {
			el = root.getFirstChild();
			el = el.getNextSibling(new QName(AtomConstants.NS_ATOM, "content"));
		}
			
		if (el == null) return;
		
		Element profile = el.getFirstChild();
//		Element profile = el.getNextSibling(new QName("","profile"));
		
		if (profile == null) return;
		el = profile.getFirstChild();
		if (el == null) return;
		
		Element entry = el.getFirstChild();
		if (entry == null) return;
		while (entry != null) {
			parseEntry(pd, entry);
			entry = entry.getNextSibling();
		}
	}
	
	public void parseEntry(ProfileDescriptor pd, Element entry) throws ParseException
	{
			HashMap<String,Object> p = new HashMap<String,Object>();
			List<Surname>    sn = new ArrayList<Surname>();
			List<GivenName>  gn = new ArrayList<GivenName>();
			List<String> logins = new ArrayList<String>();
			Boolean isExtension = false;

			Element el = null;
//			Element elAttr = entry.getFirstChild(QN_KEY);
			Element elAttr = getChild(entry, "key");

			String attrName = elAttr.getText();
			attrName = attrName.trim();
			String fullAttrName = attrName;

			// strip the prefix
			attrName = attrName.substring(PeoplePagesServiceConstants.ATTR_PREFIX.length());

			// if an extension, handle that separately later
			if (attrName.indexOf("ext") == 0) {
				isExtension = true;
			}

			// strip the group
			attrName = attrName.substring(attrName.indexOf('.')+1);

//			Element valEl = entry.getFirstChild(QN_VALUE);
			Element valEl = getChild(entry, "value");

//			el = valEl.getFirstChild(QN_TYPE);
			el = getChild(valEl, "type");
			String attrTypeString = el.getText().trim();

//			Element dataEl = valEl.getFirstChild(QN_DATA);
			Element dataEl = getChild(valEl, "data");

			do {
				String attrValString = "";
				if (dataEl != null) {
					attrValString = dataEl.getText();
					// strip any whitespace from the attribute value
					attrValString = attrValString.trim();
				}

				if (attrName.equalsIgnoreCase(SURNAMES)) {
					Surname s = new Surname();
					s.setName(attrValString);
					sn.add(s);
				} else if (attrName.equalsIgnoreCase(GIVEN_NAMES)) {
					GivenName g = new GivenName();
					g.setName(attrValString);
					gn.add(g);
				} else if (attrName.equalsIgnoreCase(USER_MODE)) {
					UserMode userMode = UserMode.INTERNAL; // default
					boolean isExternal = (attrValString.equalsIgnoreCase("EXTERNAL"));
					if (isExternal)
						userMode = UserMode.EXTERNAL; 
					pd.getProfile().setMode(userMode);
				} else if (attrName.equalsIgnoreCase(LOGINS)) {
					logins.add(attrValString);
				} else if (isExtension) {
					ProfileExtension pe = new ProfileExtension();
					pe.setPropertyId(attrName);
					pe.setDataType(attrTypeString);
					pe.setStringValue(attrValString);

					pd.getProfile().setProfileExtension(pe);				
				} else {
					// we need to check that the attribute is valid before storing in in the map
					// the update code will silently accept an invalid attribute
					Collection<String> fields = AtomGenerator3.getProfileFields();
					if (fields.contains(fullAttrName)) {
						if (StringUtils.isEmpty(attrValString)
							&& (  ("displayName".equalsIgnoreCase(attrName))
								||("surname".equalsIgnoreCase(attrName))))
						{
							String errorMsg = getErrorMsgForClient("error.profile.field.cannot.be.empty", attrName);
							throw new ParseException(errorMsg);
						}
						else
							p.put(attrName, attrValString);
					}
					else {
						throw new ParseException("attrName");					
					}
				}

				if (dataEl != null) {
					dataEl = getNextSibling(dataEl, "data");
				}
			} while (dataEl != null);

			if (attrName.equalsIgnoreCase(SURNAMES)) {
				pd.setSurnames(sn);
			} else if (attrName.equalsIgnoreCase(GIVEN_NAMES)) {
				pd.setGivenNames(gn);
			} else if (attrName.equalsIgnoreCase(LOGINS)) {
				pd.setLogins(logins);
			} else if (isExtension) {
			} else {
				pd.getProfile().putAll(p);
			}
		}

	private String getErrorMsgForClient(String msgKey, String msgParam) {
		String msg = null;
		if (StringUtils.isEmpty(msgParam))
			msg = ResourceManager.getString(msgKey);
		else {
			msg = ResourceManager.format(msgKey, new Object[] { msgParam });
		}
		return msg;
	}


	// July 2015 - wja- this method appears not to be being used and would require Admin access
	// to be successful anyway.  Making it private to force any lurking users to surface
	private void setUserState(ProfileDescriptor pd, String us) {
		if (us == null) return;

		if (pd.getProfile().getKey() != null) {
			TDIProfileService tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);

			if (us.equalsIgnoreCase("ACTIVE")) {
				tdiProfileSvc.activateProfile(pd);
			}
			else {
				tdiProfileSvc.inactivateProfile(pd.getProfile().getKey());
			}
		}
	}


	private Element getChild(Element p, String s) {
		Element el = null;

		if ((el = p.getFirstChild(new QName(AtomConstants.NS_OPENSOCIAL, s))) == null) {
			el = p.getFirstChild(new QName("", s));
		}
		return el;
	}
	
	private Element getNextSibling(Element p, String s) {
		Element el = null;
		
		if ((el = p.getNextSibling(new QName(AtomConstants.NS_OPENSOCIAL, s))) == null) {
			el = p.getNextSibling(new QName("", s));
		}

		return el;
	}

	public static String tagTypeFromScheme(String scheme) {
		String type = null;
		if (scheme == null || scheme.length() == 0) {
			return TagConfig.DEFAULT_TYPE;
		}
		else {
			if (scheme.indexOf(AtomConstants.NS_TAG_SCHEME_BASE) == 0) {
				type = scheme.substring(AtomConstants.NS_TAG_SCHEME_BASE.length());
				if (!DMConfig.instance().getTagConfigs().containsKey(type)) {
					type = null;
				}
			}			
		}
		return type;
	}
	
	public static String tagTypeToScheme(String type) {
		// by convention, the default type has no scheme
		if (TagConfig.DEFAULT_TYPE.equals(type)) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder(AtomConstants.NS_TAG_SCHEME_BASE);
			sb.append(type);
			return sb.toString();
		}
	}

}
