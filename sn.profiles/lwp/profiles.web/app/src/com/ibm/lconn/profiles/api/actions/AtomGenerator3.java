/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.abdera.model.AtomDate;
import org.apache.abdera.model.Link;
import org.apache.abdera.writer.StreamWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.web.atom.util.LCAtomConstants;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.data.AbstractName;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.data.GivenName;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Surname;
import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.lconn.profiles.data.codes.AbstractCode.CodeField;
import com.ibm.lconn.profiles.internal.data.profile.AttributeGroup;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.GivenNameService;
import com.ibm.lconn.profiles.internal.service.ProfileExtensionService;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.ProfileResolver2;
import com.ibm.lconn.profiles.internal.service.SurnameService;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public final class AtomGenerator3 {
	protected StreamWriter sw;
	protected String feedUrl = null;

	private static Log LOG = LogFactory.getLog(AtomGenerator3.class);

	private static final String GIVEN_NAMES = "givenNames";
	private static final String SURNAMES = "surnames";

	private static final QName QN_PROFILE = new QName(AtomConstants.NS_OPENSOCIAL, "person");
	private static final QName QN_ATTRIB = new QName(AtomConstants.NS_OPENSOCIAL, "com.ibm.snx_profiles.attrib");
	private static final QName QN_APPDATA = new QName(AtomConstants.NS_OPENSOCIAL, "appData");
	private static final QName QN_ENTRY = new QName(AtomConstants.NS_OPENSOCIAL, "entry");
	private static final QName QN_KEY = new QName(AtomConstants.NS_OPENSOCIAL, "key");
	private static final QName QN_VALUE = new QName(AtomConstants.NS_OPENSOCIAL, "value");
	private static final QName QN_DATA = new QName(AtomConstants.NS_OPENSOCIAL, "data");
	private static final QName QN_TYPE = new QName(AtomConstants.NS_OPENSOCIAL, "type");
	
	private final static String CODES_TYPE = "profiles.codes";
	private final static String TYPE_SCHEME = "http://www.ibm.com/xmlns/prod/sn/type";

	public final static String CODES_FEED_ID_PREFIX = "tag:profiles.ibm.com,2006:";
	public final static String ENTRY_ID = "tag:profiles.ibm.com,2006:entry";

	private Employee _emp = null;
	ProfileDescriptor _pd = null;
	protected String searchType;

	private boolean inclLabels = false;
	private String  lang = null;
	private ResourceBundle templateBundle = null;

	public AtomGenerator3(StreamWriter sw, String url) {
		this.sw = sw;
		this.feedUrl = url;
	}

	public boolean isInclLabels() {
		return inclLabels;
	}

	public void setInclLabels(boolean includeLabels) {
		this.inclLabels = includeLabels;
	}

	public String getLanguage() {
		return lang;
	}

	public void setLanguage(String language) {
		this.lang = language;
	}

	public void buildXmlProfileData(Object o, String codeType) throws UnsupportedEncodingException {
		if (o == null) return;

		try {
			if (o instanceof Employee) {
				createProfileXml((Employee) o);
			}
			else
				if (APIHelper.isCodeSearchType(codeType)) {
					if (o instanceof List) {
						createCodeXml((List) o, codeType);
					}
					else {
						createCodeXml(Collections.singletonList(o), codeType);
					}
				}
		}
		catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createProfileXml(Employee e) throws XMLStreamException {

		_emp = ProfileResolver2.resolveProfile(e, new ProfileRetrievalOptions());
		setProfileDescriptorFromEmployee(_emp.getKey());

		sw.startContent(AtomConstants.XML_CONTENT_TYPE);

		sw.setPrefix("snx", LCAtomConstants.NS_SNX);
		sw.setPrefix("", AtomConstants.NS_OPENSOCIAL);

		
		sw.startElement(QN_PROFILE);
		sw.startElement(QN_ATTRIB);

		writeBaseData();
		writeExtData();

		sw.endElement(); // attrib
		sw.endElement(); // profile
		sw.endContent();
	}

	private void writeBaseData() throws XMLStreamException {

		String attrVal;

		for (String attrSuffix : AttributeGroup.BASE_ATTRS) {

			String attrName = PeoplePagesServiceConstants.ATTR_PREFIX_BASE + attrSuffix;

			Object elem = _emp.get(attrSuffix);
			attrVal = "";

			if (null != elem) {
				String type = elem.getClass().getSimpleName();
				// As of v4.0, three object types are handled here: String, UserState and Timestamp.
				// We could switch this behavior on type since we have figured it out, but String.toString() is not known to be poor
				// peforming and the code is simpler this way. (Passing Object elem to writeProfileElement() may be a better idea for
				// another time, since that function already has object-specific code.)
				// Note: Since it came up in review, Timestamp formatting is handled by passing the string value and the class name to
				// writeProfileElement()
				attrVal = elem.toString().trim();

				// writeProfileElement() returns without writing anything if the attrVal parameter is empty or null, so now we don't even
				// make the call if elem is null.
				writeProfileElement(attrName, attrVal, type);
			}
		}

		ProfileLoginService loginService = AppServiceContextAccess.getContextObject(ProfileLoginService.class);

		@SuppressWarnings("rawtypes")
		List names = _pd.getGivenNames();

		if (names.size() > 0) {
			sw.startElement(QN_ENTRY);

			sw.startElement(QN_KEY);
			sw.writeElementText(PeoplePagesServiceConstants.ATTR_PREFIX_BASE + GIVEN_NAMES);
			sw.endElement(); // key

			sw.startElement(QN_VALUE);

			sw.startElement(QN_TYPE);
			sw.writeElementText("text");
			sw.endElement(); // type

			for (Object name : names) {
				sw.startElement(QN_DATA);
				sw.writeElementText(((AbstractName) name).getName());
				sw.endElement(); // data
			}
			sw.endElement(); // value
			sw.endElement(); // entry
		}

		names = _pd.getSurnames();

		if (names.size() > 0) {
			sw.startElement(QN_ENTRY);

			sw.startElement(QN_KEY);
			sw.writeElementText(PeoplePagesServiceConstants.ATTR_PREFIX_BASE + SURNAMES);
			sw.endElement(); // key

			sw.startElement(QN_VALUE);

			sw.startElement(QN_TYPE);
			sw.writeElementText("text");
			sw.endElement(); // type

			for (Object name : names) {
				sw.startElement(QN_DATA);
				sw.writeElementText(((AbstractName) name).getName());
				sw.endElement(); // data
			}
			sw.endElement(); // value
			sw.endElement(); // entry

			List<String> logins = loginService.getLogins(_emp.getKey());

			if (logins.size() > 0) {
				sw.startElement(QN_ENTRY);

				sw.startElement(QN_KEY);
				sw.writeElementText(PeoplePagesServiceConstants.ATTR_PREFIX_BASE + "logins");
				sw.endElement(); // key

				sw.startElement(QN_VALUE);

				sw.startElement(QN_TYPE);
				sw.writeElementText("text");
				sw.endElement(); // type

				for (String login : logins) {
					sw.startElement(QN_DATA);
					sw.writeElementText(login);
					sw.endElement(); // data
				}
				sw.endElement(); // value
				sw.endElement(); // entry
			}

			// ??
			for (String login : logins) {
				_emp.setLoginId(login);
			}
		}
	}

	private void writeProfileElement(String attrName, String attrVal, String type) throws XMLStreamException {
		if (attrVal == null || attrVal == "") {
			return;
		}

		sw.startElement(QN_ENTRY);

		sw.startElement(QN_KEY);
		sw.writeElementText(attrName);
		sw.endElement(); // key

		sw.startElement(QN_VALUE);

		if (type != null) {
			sw.startElement(QN_TYPE);
			if ((type.equals("String")) ||
				(type.equals("UserState")) ||
				(type.equals("UserMode")) )
				sw.writeElementText("text");
			else
				sw.writeElementText(type);
			sw.endElement(); // type
		}

		sw.startElement(QN_DATA);
		if ((type != null) && (type.equals("Date")))
			sw.writeElementText(AtomDate.valueOf(attrVal).toString());
		else if ((type != null) && (type.equals("UserState")))
			sw.writeElementText(attrVal.toLowerCase());
		else
			sw.writeElementText(attrVal);

		sw.endElement(); // data
		sw.endElement(); // value
		sw.endElement(); // entry
	}

	private void createCodeXml(List codes, String codeType) throws UnsupportedEncodingException {

		for (Object obj : codes) {
			AbstractCode<?> ac = (AbstractCode<?>) obj;

			writeCodeElement(ac, codeType);
		}
		
	}

	private void writeExtData() throws XMLStreamException {
		ProfileExtensionService extensionService = AppServiceContextAccess.getContextObject(ProfileExtensionService.class);
		List<ProfileExtension> extensions = extensionService.getProfileExtensionsForProfiles(Collections.singletonList(_emp.getKey()),
				getExtensionIds());

		// Set the extensions
		for (ProfileExtension pe : extensions) {
			_emp.setProfileExtension(pe);

			String elemName = PeoplePagesServiceConstants.ATTR_PREFIX_EXT + pe.getPropertyId();

			if (pe.getDataType() != null) {
				writeProfileElement(elemName, pe.getStringValue(), pe.getDataType());
			}
			else {
				writeProfileElement(elemName, pe.getStringValue(), "text");
			}
		}
	}
	
	private void writeCodeElement(AbstractCode<?> code, String codeType) throws UnsupportedEncodingException {

		sw.setPrefix("", LCAtomConstants.NS_ATOM);
		sw.startEntry();

		// PMR 09762,999,744: We need to encode the 'codeId', in case there are invalid chars like 'space'.
		String encodedCodeId = URLEncoder.encode(code.getCodeId(), "UTF-8");
		sw.writeId(CODES_FEED_ID_PREFIX + PeoplePagesServiceConstants.ATTR_PREFIX_CODES + code.getRecordType() + ":" + encodedCodeId);
		sw.writeCategory(CODES_TYPE, TYPE_SCHEME);
		sw.writeTitle(code.getCodeId());
		sw.writeUpdated(new Date());
		sw.writeLink(FeedUtils.calculateAdminCodesURL(code, code.getCodeId(), feedUrl), Link.REL_SELF);
		sw.writeLink(FeedUtils.calculateAdminCodesURL(code, code.getCodeId(), feedUrl), Link.REL_EDIT);

		sw.startContent(AtomConstants.XML_CONTENT_TYPE);

		sw.setPrefix("", AtomConstants.NS_OPENSOCIAL);
		sw.startElement(QN_APPDATA);

		for (CodeField cf : code.getFieldDefs()) {
			String elemName = cf.getName();
			elemName = PeoplePagesServiceConstants.ATTR_PREFIX_CODES + code.getRecordType() + "." + elemName;

			// Defect 71681: 'empty' field in DB could be 'null', especially on Oracle DB. Need to avoid NPE here.
			String elemValue = "";
			if (code.<Object> getFieldValue(cf) != null)
			    elemValue = code.<Object> getFieldValue(cf).toString();

			String elemType = cf.getType().getSimpleName();

			sw.startElement(new QName(AtomConstants.NS_OPENSOCIAL,elemName)).writeElementText(elemValue).endElement();
		}

		sw.endElement(); // appData
		sw.endContent();
		sw.endEntry();
	}

	/**
	 * Utility method to get extensionIds.
	 * 
	 * @return
	 */
	private List<String> getExtensionIds() {
		return new ArrayList<String>(DMConfig.instance().getExtensionAttributeConfig().keySet());
	}

	public static List<String> getProfileFields() {
		List<String> rtnVal = new ArrayList<String>();
		for (String key : AttributeGroup.BASE_ATTRS){
			rtnVal.add(PeoplePagesServiceConstants.ATTR_PREFIX_BASE + key);
		}
		Set<String> extSet = DMConfig.instance().getExtensionAttributeConfig().keySet();
		for (String key : extSet){
			rtnVal.add(PeoplePagesServiceConstants.ATTR_PREFIX_EXT + key);
		}
		// this is a carry over from 3.0 where there 'system' attributes. see AttributeGroup
		// for further comments.
		for (String key : AttributeGroup.SYSTEM_ATTRS){
			rtnVal.add(PeoplePagesServiceConstants.ATTR_PREFIX_SYS + key);
		}
		return rtnVal;
	}

	private void setProfileDescriptorFromEmployee(String key) {

		ProfileExtensionService extensionService = AppServiceContextAccess.getContextObject(ProfileExtensionService.class);
		ProfileLoginService loginService = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
		GivenNameService givenNameService = AppServiceContextAccess.getContextObject(GivenNameService.class);
		SurnameService surnameService = AppServiceContextAccess.getContextObject(SurnameService.class);

		// List<ProfileExtension> extensions = extensionService.getProfileExtensionsForProfiles(empList, getExtensionIds());
		List<String> logins = loginService.getLogins(key);

		List<GivenName> givenNames = givenNameService.getNames(key, NameSource.SourceRepository);
		List<Surname> surnames = surnameService.getNames(key, NameSource.SourceRepository);

		_pd = new ProfileDescriptor(givenNames, surnames);

		// Set the extensions / logins / surname / giveNames
		// for (ProfileExtension pe : extensions)
		// descriptors.get(pe.getKey()).getProfile().setProfileExtension(pe);

		_pd.setLogins(logins);
	}
}
