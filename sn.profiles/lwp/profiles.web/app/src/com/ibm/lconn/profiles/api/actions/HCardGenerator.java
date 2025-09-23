/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.UnsupportedEncodingException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.abdera.model.Content;
import org.apache.abdera.writer.StreamWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.util.ResourceBundleHelper;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig.ExtensionType;
import com.ibm.lconn.profiles.config.types.Label;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.Updatability;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.codes.Country;
import com.ibm.lconn.profiles.data.codes.EmployeeType;
import com.ibm.lconn.profiles.data.codes.Organization;
import com.ibm.lconn.profiles.data.codes.WorkLocation;

import com.ibm.lconn.profiles.internal.util.AssertionUtils;

import com.ibm.peoplepages.data.DatabaseRecord;
import com.ibm.peoplepages.data.Employee;

public final class HCardGenerator extends BaseCardGenerator
{
	private static final Class<HCardGenerator> CLAZZ = HCardGenerator.class;
	private static final String CLASS_NAME = CLAZZ.getSimpleName();
	private static final Log    LOG        = LogFactory.getLog(CLAZZ);

	private final ResourceBundleHelper helper;
	private ResourceBundle templateBundle = null; 
	
	public HCardGenerator(StreamWriter sw, ResourceBundleHelper helper, String url)
	{
		super(sw, url);
		this.helper = helper;
	}

	public void buildHCard(DatabaseRecord rec, boolean lite) throws UnsupportedEncodingException
	{
		sw.startContent(Content.Type.XHTML);		
		sw.startElement(QN_DIV);

		if (rec instanceof Employee)
		{
			emp = (Employee)rec;
			isLite = lite;
			createHCard(isLite);
		}
		else if (rec instanceof WorkLocation)
		{
			isLite = false;
			createWorkLocationHCard((WorkLocation)rec);
		}
		else if (rec instanceof EmployeeType)
		{
			isLite = false;
			createEmployeeTypeHCard((EmployeeType)rec);
		}
		else if (rec instanceof Country)
		{
			isLite = false;
			createCountryHCard((Country)rec);
		}
		else if (rec instanceof Organization)
		{
			isLite = false;
			createOrganizationHCard((Organization)rec);
		}
		
		sw.endElement(); // QN_DIV
		sw.endContent();
	}

	private void createHCard(boolean isLite) throws UnsupportedEncodingException
	{
		startRootSpan();

		buildPhoto();
		buildName();
		buildPronunciation();
		buildEmail();
		if (! isLite) {
			buildAdmin();
		}
		buildGroupwareMail();
		buildBlog();
		if (! isLite) {
			buildTimezone();
			buildPreferredLanguage();
		}
		buildOrg();
		buildRole();
		buildTitle();
		buildAddress();
		buildOffice();
		buildTelephone();
		if (! isLite) {
			buildCategories();
			buildExperience();
			buildDescription();
			buildScheduleUrls();
		}
		buildManager();
		buildIsManager();
		buildIds();
		if (! isLite) {
			buildDepartmentNumber();
			buildShift();
		}
		buildLastUpdate();
		buildProfileType();
		if (! isLite) {
			buildExtensionProperties();
		}		

		endRootSpan();
	}

	private void createWorkLocationHCard(WorkLocation wl)
	{
		startRootSpan();
		startDiv(false, ADR, WORK, POSTAL); // addrDiv (1)

		writeDiv(wl.getAddress1(), false, STREET_ADDRESS);
		writeDiv(wl.getAddress2(), false, EXTENDED_ADDRESS, X_STREET_ADDRESS2);
		writeSpan(wl.getCity(), LOCALITY);
		writeSpan(wl.getState(), REGION);
		writeSpan(wl.getPostalCode(), POSTAL_CODE);
		writeDiv(wl.getWorkLocationCode(), true, X_WORKLOCATION_CODE);
		
		endDiv(); // addrDiv (1);		
		endRootSpan();
	}

	private void createCountryHCard(Country country)
	{
		startRootSpan();
		startDiv(false, ADR, WORK, POSTAL); // adrDiv (1)
		
		writeDiv(country.getDisplayValue(), false, COUNTRY_NAME);
		writeDiv(country.getCountryCode(), true, X_COUNTRY_CODE);
		
		endDiv(); // adrDiv (1)		
		endRootSpan();
	}

	private void createEmployeeTypeHCard(EmployeeType et)
	{
		startRootSpan();
		buildRole(et.getEmployeeDescription(), et.getEmployeeType());
		endRootSpan();
	}

	private void createOrganizationHCard(Organization org)
	{
		startRootSpan();
		buildOrg(org.getOrgTitle(), org.getOrgCode());
		endRootSpan();
	}

	private void startRootSpan()
	{
		sw.startElement(QN_SPAN);
		writeClassesAttribute(VCARD);
	}
	
	private void endRootSpan()
	{
		sw.endElement();
	}

	private void buildPhoto()
	{
		if (emp.getImageUrl() != null && emp.getImageUrl().length() > 0)
		{
			startDiv();
			writeImg(emp.getImageUrl(), PHOTO);
			endDiv();
		}
	}

	private void buildName() throws UnsupportedEncodingException
	{
		startDiv();
		writeAnchor(FeedUtils.calculateProfilesFeedURL(emp.getKey(), profilesURL), emp.getDisplayName(), FN, URL);
		endDiv();
		
		if (!isLite)
		{
			startDiv(true, N); // nDiv (1)
			
			startDiv(); // pnDiv (2)
			writeDiv(emp.getSurname(), false, FAMILY_NAME);
			writeDiv(emp.getGivenName(), false, GIVEN_NAME);
			writeDiv(emp.getCourtesyTitle(), false, HONORIFIC_PREFIX);
			writeDiv(emp.getPreferredFirstName(), false, X_PREFERRED_FIRST_NAME);
			writeDiv(emp.getPreferredLastName(), false, X_PREFERRED_LAST_NAME);
			endDiv(); // pnDiv (2)
			
			startDiv(true); // nativeDiv (3)
			writeDiv(emp.getNativeFirstName(), false, X_NATIVE_FIRST_NAME);
			writeDiv(emp.getNativeLastName(), false, X_NATIVE_LAST_NAME);
			endDiv(); // nativeDiv (3)
			
			writeDiv(emp.getAlternateLastname(), false, X_ALTERNATE_LAST_NAME); // altdiv
			
			endDiv(); // nDiv (1)
		}
	}

	private void buildPronunciation()
	{
		if (notEmpty(emp.getPronunciationUrl()))
		{
			startDiv();
			writeAnchor(emp.getPronunciationUrl(), helper.getString("pronunciation"), SOUND, URL);
			endDiv();
		}
	}

	private void buildEmail()
	{
	    if (LCConfig.instance().isEmailReturned() && notEmpty(emp.getEmail()))
		{
			startDiv();
			writeAnchor(MAILTO + emp.getEmail(), emp.getEmail(), EMAIL);
			endDiv();
		}
	}

	private void buildAdmin() throws UnsupportedEncodingException
	{
		if (notEmpty(emp.getSecretaryKey()))
		{
			writeSpan(helper.getString("admin"));
			writeAnchor(FeedUtils.calculateProfilesFeedURL(emp.getSecretaryKey(), profilesURL), emp.getSecretaryName(), AGENT, URL);
		}
	}

	private void buildBlog()
	{
		if (notEmpty(emp.getBlogUrl()))
		{
			startDiv();
			writeAnchor(emp.getBlogUrl(), helper.getString("blog"), X_BLOG_URL, URL);
			endDiv();
		}
	}

	private void buildTimezone()
	{
		writeDiv(emp.getTimezone(), true, TZ);
	}

	private void buildPreferredLanguage()
	{
		writeDiv(emp.getPreferredLanguage(), true, X_PREFERRED_LANGUAGE);
	}

	private void buildGroupwareMail()
	{
      if (LCConfig.instance().isEmailReturned())
      {
		writeDiv(emp.getGroupwareEmail(), true, X_GROUPWARE_MAIL);
      }
	}

	private void buildOrg()
	{
		buildOrg(emp.getOrganizationTitle(), emp.getOrgId());
	}

	private void buildOrg(String orgUnit, String orgId)
	{
		startDiv(false, ORG);
		
		writeSpan(orgUnit, ORGANIZATION_UNIT);
		if (!isLite)
		{
			writeSpan(orgId, X_ORGANIZATION_CODE);
		}
		
		endDiv(); // ORG
	}

	private void buildRole()
	{
		buildRole(emp.getEmployeeTypeDesc(), emp.getEmployeeTypeCode());
	}

	private void buildRole(String desc, String code)
	{
		writeDiv(desc, false, ROLE);

		if (!isLite)
		{
			writeDiv(code, true, X_EMPTYPE);
		}
	}

	private void buildTitle()
	{
		writeDiv(emp.getJobResp(), false, TITLE);

		if (!isLite)
		{
			writeDiv(emp.getTitle(), false, X_PROFILES_TITLE);
		}
	}

	private void buildAddress()
	{
		boolean workLoc = emp.getWorkLocation() != null;
		boolean cc = emp.getCountryCode() != null;
		
		if (workLoc || cc)
		{
			//address
			startDiv(false, ADR, WORK, POSTAL); // adrDiv (1)
			
			if (workLoc)
			{
				writeDiv(emp.getWorkLocation().getAddress1(), false, STREET_ADDRESS);
				writeDiv(emp.getWorkLocation().getAddress2(), false, EXTENDED_ADDRESS, X_STREET_ADDRESS2);			
				writeSpan(emp.getWorkLocation().getCity(), LOCALITY);
				writeSpan(emp.getWorkLocation().getState(), REGION);
				writeSpan(emp.getWorkLocation().getPostalCode(), POSTAL_CODE);
				writeDiv(emp.getCountryDisplayValue(), false, COUNTRY_NAME);
			}
			if (cc) writeDiv(emp.getCountryCode(), true, X_COUNTRY_CODE);
			if (workLoc) writeDiv(emp.getWorkLocationCode(), true, X_WORKLOCATION_CODE);
			
			endDiv(); // adrDiv (1)
		}
	}

	private void buildOffice()
	{
		startDiv(false, X_OFFICE); // officeDiv (1)
		writeSpan(emp.getBldgId(), X_BUILDING);
		writeSpan(emp.getFloor(), X_FLOOR);
		writeSpan(emp.getOfficeName(), X_OFFICE_NUMBER);		
		endDiv(); // officeDiv (1)
	}

	private void buildTelephone()
	{
		startDiv(false, TEL); // telDiv (1);
		writeAbbr(WORK, helper.getString("work"), TYPE);
		writeSpan(emp.getTelephoneNumber(), VALUE);
		endDiv(); // telDiv (1)
		
		if (!isLite)
		{
			startDiv(false,TEL); // CELL DIV
			writeAbbr(CELL, helper.getString("cell"), TYPE);
			writeSpan(emp.getMobileNumber(), VALUE);
			endDiv();


			startDiv(false, TEL); // FAX
			writeAbbr(FAX, helper.getString("fax"), TYPE);
			writeSpan(emp.getFaxNumber(), VALUE);
			endDiv();

			
			startDiv(false, TEL);  // IP
			writeAbbr(X_IP, helper.getString("ip"), TYPE);
			writeSpan(emp.getIpTelephoneNumber(), VALUE);
			endDiv();

			
			startDiv(false, TEL);  // PAGER
			writeAbbr(PAGER, helper.getString("pager"), TYPE);
			writeSpan(emp.getPagerNumber(), VALUE);
			writeSpan(emp.getPagerId(), X_PAGER_ID);
			writeSpan(emp.getPagerType(), X_PAGER_TYPE);
			writeSpan(emp.getPagerServiceProvider(), X_PAGER_PROVIDER);
			endDiv();			
		}
	}

	private void buildManager()
	{
		writeDiv(emp.getManagerUid(), true, X_MANAGER_UID);
	}

	private void buildIsManager()
	{
		writeDiv(emp.getIsManager(), true, X_IS_MANAGER);
	}

	private void buildExperience()
	{
		startDiv(false, X_EXPERIENCE);
		if (notEmpty(emp.getExperience()))
		{
			sw.writeElementText(emp.getExperience());
		}
		endDiv();
	}

	private void buildDescription()
	{
		startDiv(false, X_DESCRIPTION);
		if (notEmpty(emp.getDescription()))
		{
			sw.writeElementText(emp.getDescription());
		}
		endDiv();
	}

	private void buildCategories()
	{
		writeDiv(buildCommaDelimitedTagList(emp.getProfileTags()), false, CATEGORIES);
	}

	private void buildScheduleUrls()
	{
		startDiv();
		
		if (notEmpty(emp.getFreeBusyUrl()))
		{
			writeAnchor(emp.getFreeBusyUrl(), helper.getString("free_busy"), X_FREEBUSY_URL);
		}

		if (notEmpty(emp.getCalendarUrl()))
		{
			writeAnchor(emp.getCalendarUrl(), helper.getString("calendar"), X_CALENDAR_URL);
		}
		
		endDiv();
	}

	private void buildDepartmentNumber()
	{
		writeDiv(emp.getDeptNumber(), true, X_DEPARTMENT_NUMBER);
		
		if (!isLite)
		{
			writeDiv(emp.getDepartmentTitle(), true, X_DEPARTMENT_TITLE);
		}
	}

	private void buildShift()
	{
		writeDiv(emp.getShift(), true, X_SHIFT);
	}

	private void buildExtensionProperties()
	{
		ProfileType profileType = ProfileTypeHelper.getProfileType(emp.getProfileType());
		// only set the templateBundle if the request asks for customized language labels
		if (isInclLabels()) {
			Locale locale = getLocale(); // use the requested locale
			templateBundle = ProfilesConfig.instance().getTemplateConfig().getBundle(locale);
			if (null == templateBundle) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("buildExtensionProperties Resource bundle was not found for locale : " + locale.getLanguage() + "_" + locale.getCountry());
				}
			}
			else {
				Locale useLocale = templateBundle.getLocale();
				if (LOG.isTraceEnabled()) {
					LOG.trace("buildExtensionProperties loaded Resource bundle for : "
							+ useLocale.getLanguage() + (StringUtils.isEmpty(useLocale.getCountry()) ? "" : (" : " + useLocale.getCountry())));		
				}
			}
		}
		for (ExtensionAttributeConfig extConfig : DMConfig.instance().getExtensionAttributeConfig().values())
		{
			String extensionId = extConfig.getExtensionId();
			Property property  = profileType.getPropertyById( extensionId );
			boolean  isHidden  = property == null || property.isHidden();
			boolean  hasLabel  = property != null && property.isLabel() && !isHidden; // does property have a custom label ?
			if (hasLabel) {
				Label propertyLabel = property.getLabel();
				if (null != propertyLabel) {
					String ref = property.getRef(); // has a bunch of \t \n crap in it !
					String labelName  = new String(ref.trim().toLowerCase());
					String labelValue = null;
					String useValue   = null;
					// was an over-ride custom value supplied for the label ?
					if (! StringUtils.isEmpty(labelName)) {
						if (LOG.isTraceEnabled()) {
							traceAttributeInfo(extensionId);
						}
						// if it is editable, use the customized value supplied by the user
						boolean isEditable = (Updatability.READWRITE.equals(property.getUpdatability()));
						if (isEditable) {
							// RTC 183870 : Atom API should return custom attribute labels
							// eg logging : 
							//  userLabel = emp.getProfileExtension(phone1, true)
							//  Process: Label: phone1 Name:  label.phone.home Value: 12345
							//  Bundle Lookup : Custom label: label.phone.home Value: Home:
							//  buildExtensionProperty(emp.getProfileExtension(phone1, true), Home:)
							ProfileExtension userLabel = emp.getProfileExtension(extensionId, true);
							logTrace(    "   userLabel = emp.getProfileExtension(" + extensionId + ", true) = " + userLabel.toString());
							String labelPropId = userLabel.getPropertyId();
							labelValue = userLabel.getValue();
							String customLabelName  = userLabel.getName();
							// if customLabelName is null here (on-Prem ATT customization), use the original version from above : the property.getRef()
							labelName = (StringUtils.isEmpty(customLabelName) ? labelName : customLabelName);
							AssertionUtils.assertNotNull(labelName);
							// only check for special pick-list attribute handling if we are running on Cloud
							if (isLotusLive) {
								// the UI handles the special Cloud pick-list attributes in profileEdit-cloud.ftl
								if (isInSpecialProcessingSet(labelPropId)) { // Cloud : phone1, phone2, phone3
									labelName = getBundleLookupName(userLabel);
								}
							}
							logTrace("Process: Label: " + labelPropId + " Name:  " + labelName + " Value: " + labelValue);
						}
						// look the label up in the language bundle
						useValue = lookupBundleLabel(labelName);
						logTrace("Bundle Lookup : " + "Custom label: " + labelName + " Value: " + useValue);
					}
					labelValue = (StringUtils.isEmpty(useValue) ? propertyLabel.getLabel() : useValue);
					logTrace("buildExtensionProperty(emp.getProfileExtension(" + extensionId + ", true), " + labelValue + ")");
					buildExtensionProperty(emp.getProfileExtension(extensionId, true), labelValue);
				}
			}
			else {
				// if not hidden and SIMPLE or RICHTEXT write it out 
				if ( !isHidden && 
					(extConfig.getExtensionType() == ExtensionType.SIMPLE || extConfig.getExtensionType() == ExtensionType.RICHTEXT)) {
					logTrace("buildExtensionProperty(emp.getProfileExtension(" + extensionId + ", true), null)");
					buildExtensionProperty(emp.getProfileExtension(extensionId, true));
				}
			}
		}
	}

	private static final boolean isLotusLive = LCConfig.instance().isLotusLive();
	private static final List<String> specialHandling  = Arrays.asList("phone1", "phone2", "phone3");
	private static final Set<String> specialProcessing = new HashSet<String>(specialHandling);
	private static final String labelDefault="label.phone.other";
	private boolean isInSpecialProcessingSet(String labelPropId)
	{
		return (specialProcessing.contains(labelPropId));
	}
	private String getBundleLookupName(ProfileExtension userLabel)
	{
		String labelName = userLabel.getName();
		return (StringUtils.isEmpty(labelName) ? labelDefault : labelName);
	}

	private void traceAttributeInfo(String extensionId)
	{
		try {
			if (LOG.isTraceEnabled()) {
				String attributeId = Employee.getAttributeIdForExtensionId(extensionId);
				String attrExtId   = Employee.getExtensionIdForAttributeId(attributeId);
				logTrace("   attrLabel = emp.getProfileExtension(" + attrExtId + ", true)");
				ProfileExtension attrLabel = emp.getProfileExtension(attrExtId, true);
				String attrName  = attrLabel.getName();
				String attrValue = attrLabel.getValue();
				logTrace("ExtensionId " + extensionId
						+ " Attrib : " + "id: "  + attributeId + " extId: " + attrExtId
						+ " Attrib : " + "Label Name: " + attrName + " Value: " + attrValue);
			}
		}
		catch (Exception e) { /* suppress any exception */	}
	}

	private void logTrace(String traceMsg)
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace(traceMsg);
		}
	}

	private void buildExtensionProperty(ProfileExtension pe)
	{
		buildExtensionProperty(pe, null);
	}

	private void buildExtensionProperty(ProfileExtension pe, String labelValue)
	{
		startDiv(false, X_EXTENSION_PROPERTY);
		String propId = pe.getPropertyId();
		String extKey = pe.getExtKey();
		writeDiv(propId, false, X_EXTENSION_PROPERTY_ID);
		writeDiv(extKey, false, X_EXTENSION_KEY);
		if (!hideExtensionProfiName) {
			writeDiv(pe.getName(), false, X_EXTENSION_NAME);
		}
		String useLabel = labelValue;
		if (isInclLabels()) {
			// if labelValue was not supplied, then look up the bundle value for the propertyID
			if (null == useLabel) {
				useLabel = lookupBundleLabel(propId);
			}
			if (null != useLabel)
				writeDivInCData(useLabel, false, X_EXTENSION_LABEL); // write as CDATA
		}
		writeDiv(pe.getStringValue(), false, X_EXTENSION_VALUE);
		writeDiv(pe.getDataType(),    false, X_EXTENSION_DATA_TYPE);
		endDiv();
	}

	private String lookupBundleLabel(String name)
	{
		String retVal = null;
		// if bundle is found, then include its customized label
		try {
			if (null != templateBundle) {
				String key = name;
				boolean includesLabelPrefix = (StringUtils.startsWith(name, "label.")); // custom label already contains 'label.'
				if (includesLabelPrefix == false)
					key = "label." + name;
				retVal = templateBundle.getString(key);
			}
		}
		catch (java.util.MissingResourceException mre)
		{
			// there's not much we can do if the customized label was not found
			// other than log it for autopsies
			logTrace(CLASS_NAME + "- MissingResourceException looking up custom attribute key : " + name);
		}
		catch (Exception e) {
		}
		return retVal;
	}

	private void buildIds()
	{
		writeDiv(emp.getKey(),  false, X_PROFILE_KEY);
		writeDiv(emp.getGuid(), false, UID);
		writeDiv(emp.getUid(),  false, X_PROFILE_UID);
		writeDiv(emp.getUserid(), false, X_LCONN_USERID);

		if (!isLite)
		{
			writeDiv(emp.getEmployeeNumber(), false, X_EMPLOYEE_NUMBER);
			writeDiv(emp.getDistinguishedName(), false, X_DN);
		}
	}

	private void buildLastUpdate()
	{
		startDiv(true, REV);
		sw.writeElementText(emp.getLastUpdate());
		endDiv();
	}

	private void buildProfileType() {
		startDiv(true, X_PROFILE_TYPE);
		sw.writeElementText(emp.getProfileType());
		endDiv();
	}

	private void writeAnchor(String href, String value, String... classes)
	{
		sw.startElement(QN_A);
		writeClassesAttribute(classes);
		sw.writeAttribute(HREF, href);
		sw.writeElementText(value);
		sw.endElement();
	}

	private void writeSpan(String value, String...classes)
	{
		sw.startElement(QN_SPAN);
		writeClassesAttribute(classes);
		sw.writeElementText(value);
		sw.endElement();
	}

	private void writeAbbr(String title, String value, String...classes)
	{
		sw.startElement(QN_ABBR);
		writeClassesAttribute(classes);
		if (title != null)
		{
			sw.writeAttribute(TITLE_ATTR, title);
		}
		if (value != null)
		{
			sw.writeElementText(value);
		}		
		sw.endElement(); // ABBR
	}

	private void startDiv()
	{
		startDiv(false);
	}

	private void startDiv(boolean hidden, String... classes)
	{
		sw.startElement(QN_DIV);
		writeClassesAttribute(classes);
		if (hidden)
		{
			sw.writeAttribute(STYLE, VISIBILITY_HIDDEN);
		}
	}

	private void endDiv()
	{
		sw.endElement();
	}

	private void writeDiv(String value, boolean hidden, String... classes)
	{
		startDiv(hidden, classes);
		if (value != null)
		{
			sw.writeElementText(value);
		}
		endDiv();
	}	

	/**
	 * CDATA start tag
	 */
	public static final String CDATA_START = "<![CDATA[";
	/**
	 * CDATA end tag
	 */
	public static final String CDATA_END = "]]&gt;";

	private void writeDivInCData(String value, boolean hidden, String... classes)
	{
		startDiv(hidden, classes);
		if (value != null)
		{
			sw.writeElementText(getCData(value));
		}
		endDiv();
	}	
	/**
	 * Encapsulate the passed in text within CDATA tags.
	 */
	public String getCData(String str)
	{
		StringBuilder sb = new StringBuilder();
		boolean isEnclosedInCData = str.startsWith(CDATA_START) && str.endsWith(CDATA_END);

		// Since there may already be CDATA sections inside the string and since CDATA sections
		// can not be nested - ie. we cannot have ]]&gt; inside a CDATA section, we first verify
		// by replacing any occurrence of the CDATA_END tag with "]]]]&gt;<![CDATA[>"
		// This will ensure that the primary CDATA section will be split into valid CDATA sub-sections

		if (!isEnclosedInCData) {
			str = str.replaceAll(CDATA_END, "]]]]&gt;<![CDATA[>");
		}

		if (!isEnclosedInCData)
			sb.append(CDATA_START);

		sb.append(str);

		if (!isEnclosedInCData)
			sb.append(CDATA_END);

		return sb.toString();
	}

	private void writeImg(String src, String... classes)
	{
		sw.startElement(QN_IMG);
		sw.writeAttribute(SRC, src);
		writeClassesAttribute(classes);
		sw.endElement();
	}

	private void writeClassesAttribute(String... classes)
	{
		if (classes != null && classes.length > 0)
		{
			sw.writeAttribute(CLASS, buildClassString(classes));
		}
	}

	private String buildClassString(String... classes)
	{
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < classes.length; i++)
		{
			if (i > 0)
			{
				buffer.append(" ");
			}
			buffer.append(classes[i]);
		}
		return buffer.toString();
	}

	private final boolean notEmpty(String str)
	{
		return (str != null && str.length() > 0);
	}


	// namespace
	private static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

	//elements
	private static final String DIV = "div";
	private static final String SPAN = "span";
	private static final String A = "a";
	private static final String IMG = "img";
	private static final String ABBR = "abbr";

	private static final QName QN_DIV = new QName(XHTML_NAMESPACE, DIV, "");
	private static final QName QN_SPAN = new QName(XHTML_NAMESPACE, SPAN, "");
	private static final QName QN_A = new QName(XHTML_NAMESPACE, A, "");
	private static final QName QN_IMG = new QName(XHTML_NAMESPACE, IMG, "");
	private static final QName QN_ABBR = new QName(XHTML_NAMESPACE, ABBR, "");

	//attributes
	private static final String CLASS = "class";
	private static final String HREF = "href";
	private static final String SRC = "src";
	private static final String STYLE = "style";
	private static final String TITLE_ATTR = "title";

	//vcard types
	private static final String ADR = "adr";
	private static final String AGENT = "agent";
	private static final String CATEGORIES = "categories";
	private static final String CELL = "cell";
	private static final String COUNTRY_NAME = "country-name";
	private static final String EXTENDED_ADDRESS = "extended-address";
	private static final String FAMILY_NAME = "family-name";
	private static final String GIVEN_NAME = "given-name";
	private static final String FAX = "fax";
	private static final String FN = "fn";
	private static final String HONORIFIC_PREFIX = "honorific-prefix";
	private static final String LOCALITY = "locality";
	private static final String EMAIL = "email";
	private static final String N = "n";
	private static final String ORG = "org";
	private static final String ORGANIZATION_UNIT = "organization-unit";
	private static final String PAGER = "pager";
	private static final String PHOTO = "photo";
	private static final String POSTAL = "postal";
	private static final String POSTAL_CODE = "postal-code";
	private static final String REGION = "region";
	private static final String REV = "rev";
	private static final String ROLE = "role";
	private static final String SOUND = "sound";
	private static final String STREET_ADDRESS = "street-address";
	private static final String TEL = "tel";
	private static final String TITLE = "title";
	private static final String TYPE = "type";
	private static final String TZ = "tz";
	private static final String UID = "uid";
	private static final String URL = "url";
	private static final String VCARD = "vcard";
	private static final String VALUE = "value";
	private static final String WORK = "work";

	//vcard extensions
	private static final String X_ALTERNATE_LAST_NAME = "x-alternate-last-name";
	private static final String X_BLOG_URL = "x-blog-url";
	private static final String X_BUILDING = "x-building";
	private static final String X_CALENDAR_URL = "x-calendar-url";
	private static final String X_COUNTRY_CODE = "x-country-code";
	private static final String X_DEPARTMENT_NUMBER = "x-department-number";
	private static final String X_DEPARTMENT_TITLE = "x-department-title";
	private static final String X_DESCRIPTION = "x-description";
	private static final String X_DN = "x-dn";
	private static final String X_EMPLOYEE_NUMBER = "x-employee-number";
	private static final String X_EMPTYPE = "x-empType";
	private static final String X_EXPERIENCE = "x-experience";
	private static final String X_FLOOR = "x-floor";
	private static final String X_FREEBUSY_URL = "x-freebusy-url";
	private static final String X_GROUPWARE_MAIL = "x-groupwareMail";
	private static final String X_PROFILE_UID = "x-profile-uid";
	private static final String X_PROFILE_KEY = "x-profile-key";
	private static final String X_LCONN_USERID = "x-lconn-userid";
	private static final String X_IP = "x-ip";
	private static final String X_IS_MANAGER = "x-is-manager";
	private static final String X_MANAGER_UID = "x-manager-uid";
	private static final String X_NATIVE_FIRST_NAME = "x-native-first-name";
	private static final String X_NATIVE_LAST_NAME = "x-native-last-name";
	private static final String X_OFFICE = "x-office";
	private static final String X_OFFICE_NUMBER = "x-office-number";
	private static final String X_ORGANIZATION_CODE = "x-organization-code";
	private static final String X_PAGER_ID = "x-pager-id";
	private static final String X_PAGER_TYPE = "x-pager-type";
	private static final String X_PAGER_PROVIDER = "x-pager-provider";
	private static final String X_PREFERRED_FIRST_NAME = "x-preferred-first-name";
	private static final String X_PREFERRED_LANGUAGE = "x-preferred-language";
	private static final String X_PREFERRED_LAST_NAME = "x-preferred-last-name";
	private static final String X_PROFILES_TITLE = "x-profiles-title";
	private static final String X_SHIFT = "x-shift";
	private static final String X_STREET_ADDRESS2 = "x-streetAddress2";
	private static final String X_WORKLOCATION_CODE = "x-worklocation-code";

	private static final String X_PROFILE_TYPE = "x-profile-type";

	private static final String X_EXTENSION_PROPERTY = "x-extension-property";
	private static final String X_EXTENSION_PROPERTY_ID = "x-extension-property-id";
	private static final String X_EXTENSION_KEY = "x-extension-key";
	private static final String X_EXTENSION_NAME = "x-extension-name";
	private static final String X_EXTENSION_VALUE = "x-extension-value";
	private static final String X_EXTENSION_DATA_TYPE = "x-extension-data-type";
	private static final String X_EXTENSION_LABEL = "x-extension-label";

	//other
	private static final String MAILTO = "mailto:";
	private static final String VISIBILITY_HIDDEN = "display:none";

}
