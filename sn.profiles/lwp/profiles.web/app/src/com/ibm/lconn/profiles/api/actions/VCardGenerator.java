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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.abdera.model.Content;
import org.apache.abdera.writer.StreamWriter;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig.ExtensionType;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.codes.Country;
import com.ibm.lconn.profiles.data.codes.EmployeeType;
import com.ibm.lconn.profiles.data.codes.Organization;
import com.ibm.lconn.profiles.data.codes.WorkLocation;

import com.ibm.peoplepages.data.DatabaseRecord;
import com.ibm.peoplepages.data.Employee;

public final class VCardGenerator extends BaseCardGenerator
{
	public static String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

	public VCardGenerator(StreamWriter sw, String url)
	{
		super(sw, url);
	}

	public void buildVCard(DatabaseRecord rec, boolean lite) throws UnsupportedEncodingException
	{
		sw.startContent(Content.Type.TEXT);

		if (rec instanceof Employee)
		{
			emp = (Employee)rec;
			isLite = lite;
			createVCard(isLite);
		}
		else if (rec instanceof WorkLocation)
		{
			isLite = false;
			createWorkLocationHCard((WorkLocation)rec);
		}
		else if (rec instanceof EmployeeType)
		{
			isLite = false;
			createEmployeeTypeVCard((EmployeeType)rec);
		}
		else if (rec instanceof Country)
		{
			isLite = false;
			createCountryVCard((Country)rec);
		}
		else if (rec instanceof Organization)
		{
			isLite = false;
			createOrganizationVCard((Organization)rec);
		}
		
		sw.endContent();
	}

	private void createVCard(boolean isLite) throws UnsupportedEncodingException
	{
		appendVCardBegin();

		buildPhoto();
		buildName();
		buildURL();
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

		appendVCardEnd();
	}

	private void createWorkLocationHCard(WorkLocation wl)
	{
		appendVCardBegin();

		sw.writeElementText("\n");
		sw.writeElementText(ADR);
		sw.writeElementText(";");
		sw.writeElementText(WORK);
		sw.writeElementText(":");
		sw.writeElementText(";");
		sw.writeElementText(";");
		sw.writeElementText(wl.getAddress1());
		sw.writeElementText(",");
		sw.writeElementText(wl.getAddress2());
		sw.writeElementText(";");
		sw.writeElementText(wl.getCity());
		sw.writeElementText(";");
		sw.writeElementText(wl.getState());
		sw.writeElementText(";");
		sw.writeElementText(wl.getPostalCode());

		sw.writeElementText("\n");
		sw.writeElementText(X_WORKLOCATION_CODE);
		sw.writeElementText(":");
		sw.writeElementText(wl.getWorkLocationCode());

		appendVCardEnd();
	}

	private void createCountryVCard(Country country)
	{
		appendVCardBegin();

		sw.writeElementText("\n");
		sw.writeElementText(ADR);
		sw.writeElementText(";");
		sw.writeElementText(WORK);
		sw.writeElementText(":");
		sw.writeElementText(";");
		sw.writeElementText(";");
		sw.writeElementText(";");
		sw.writeElementText(";");
		sw.writeElementText(";");
		sw.writeElementText(";");
		sw.writeElementText(emp.getCountryDisplayValue());

		sw.writeElementText("\n");
		sw.writeElementText(X_COUNTRY_CODE);
		sw.writeElementText(":");
		sw.writeElementText(emp.getCountryCode());

		appendVCardEnd();
	}

	private void createEmployeeTypeVCard(EmployeeType et)
	{
		appendVCardBegin();

		buildRole(et.getEmployeeDescription(), et.getEmployeeType());

		appendVCardEnd(); 
	}

	private void createOrganizationVCard(Organization org)
	{
		appendVCardBegin();

		buildOrg(org.getOrgTitle(), org.getOrgCode());

		appendVCardEnd(); 
	}

	private void buildPhoto()
	{
		sw.writeElementText("\n");
		sw.writeElementText(PHOTO);
		sw.writeElementText(";");
		sw.writeElementText(VALUE);
		sw.writeElementText("=");
		sw.writeElementText(URL);
		sw.writeElementText(":");
		sw.writeElementText(emp.getImageUrl());
	}

	private void buildURL() throws UnsupportedEncodingException
	{
		sw.writeElementText("\n");
		sw.writeElementText(URL);
		sw.writeElementText(":");
		sw.writeElementText(FeedUtils.calculateProfilesFeedURL(emp.getKey(), profilesURL));
	}

	private void buildName()
	{
		if (emp.getSurname() != null && emp.getGivenName() != null)
		{
			sw.writeElementText("\n");
			sw.writeElementText(N);
			sw.writeElementText(":");
			sw.writeElementText(emp.getSurname());
			sw.writeElementText(";");
			sw.writeElementText(emp.getGivenName());
		}
		sw.writeElementText("\n");
		sw.writeElementText(FN);
		sw.writeElementText(":");
		sw.writeElementText(emp.getDisplayName());

		if (!isLite)
		{
			sw.writeElementText("\n");
			sw.writeElementText(HONORIFIC_PREFIX);
			sw.writeElementText(":");
			sw.writeElementText(emp.getCourtesyTitle());

			sw.writeElementText("\n");
			sw.writeElementText(NICKNAME);
			sw.writeElementText(":");
			sw.writeElementText(emp.getPreferredFirstName());

			sw.writeElementText("\n");
			sw.writeElementText(X_PREFERRED_LAST_NAME);
			sw.writeElementText(":");
			sw.writeElementText(emp.getPreferredLastName());

			sw.writeElementText("\n");
			sw.writeElementText(X_NATIVE_FIRST_NAME);
			sw.writeElementText(":");
			sw.writeElementText(emp.getNativeFirstName());

			sw.writeElementText("\n");
			sw.writeElementText(X_NATIVE_LAST_NAME);
			sw.writeElementText(":");
			sw.writeElementText(emp.getNativeLastName());

			sw.writeElementText("\n");
			sw.writeElementText(X_ALTERNATE_LAST_NAME);
			sw.writeElementText(":");
			sw.writeElementText(emp.getAlternateLastname());
		}
	}

	private void buildPronunciation()
	{
		sw.writeElementText("\n");
		sw.writeElementText(SOUND);
		sw.writeElementText(";");
		sw.writeElementText(VALUE);
		sw.writeElementText("=");
		sw.writeElementText(URL);
		sw.writeElementText(":");
		sw.writeElementText(emp.getPronunciationUrl());
	}

	private void buildEmail()
	{
	  if (LCConfig.instance().isEmailReturned())
	  {
		sw.writeElementText("\n");
		sw.writeElementText(EMAIL);
		sw.writeElementText(";");
		sw.writeElementText(INTERNET);
		sw.writeElementText(":");
		sw.writeElementText(emp.getEmail());
	  }
	}

	private void buildAdmin() throws UnsupportedEncodingException
	{
		if (emp.getSecretaryKey() != null && emp.getSecretaryKey().length() > 0)
		{
			sw.writeElementText("\n");
			sw.writeElementText(AGENT);
			sw.writeElementText(";");

			sw.writeElementText(VALUE);
			sw.writeElementText("=");
			sw.writeElementText(X_PROFILE_UID);
			sw.writeElementText(":");
			sw.writeElementText(emp.getUid());
			sw.writeElementText(";");

			sw.writeElementText(VALUE);
			sw.writeElementText("=");
			sw.writeElementText(FN);
			sw.writeElementText(":");
			sw.writeElementText(emp.getSecretaryName());
			sw.writeElementText(";");
			sw.writeElementText(VALUE);
			sw.writeElementText("=");
			sw.writeElementText(URL);
			sw.writeElementText(":");
			sw.writeElementText(escape(FeedUtils.calculateProfilesFeedURL(emp.getSecretaryKey(), profilesURL)));
		}
	}

	private void buildBlog()
	{
		sw.writeElementText("\n");
		sw.writeElementText(X_BLOG_URL);
		sw.writeElementText(";");
		sw.writeElementText(VALUE);
		sw.writeElementText("=");
		sw.writeElementText(URL);
		sw.writeElementText(":");
		sw.writeElementText(emp.getBlogUrl());
	}

	private void buildTimezone()
	{
		sw.writeElementText("\n");
		sw.writeElementText(TZ);
		sw.writeElementText(":");
		sw.writeElementText(emp.getTimezone());
	}

	private void buildPreferredLanguage()
	{
		sw.writeElementText("\n");
		sw.writeElementText(X_PREFERRED_LANGUAGE);
		sw.writeElementText(":");
		sw.writeElementText(emp.getPreferredLanguage());
	}

	private void buildGroupwareMail()
	{
	  if (LCConfig.instance().isEmailReturned())
	  {
		sw.writeElementText("\n");
		sw.writeElementText(EMAIL);
		sw.writeElementText(";");
		sw.writeElementText(X_GROUPWARE_MAIL);
		sw.writeElementText(":");
		sw.writeElementText(emp.getGroupwareEmail());
	  }
	}

	private void buildOrg()
	{
		buildOrg(emp.getOrganizationTitle(), emp.getOrgId());
	}

	private void buildOrg(String orgUnit, String orgId)
	{
		sw.writeElementText("\n");
		sw.writeElementText(ORG);
		sw.writeElementText(":");
		sw.writeElementText(orgUnit);

		if (!isLite)
		{
			sw.writeElementText("\n");
			sw.writeElementText(X_ORGANIZATION_CODE);
			sw.writeElementText(":");
			sw.writeElementText(orgId);
		}
	}

	private void buildRole()
	{
		buildRole(emp.getEmployeeTypeDesc(), emp.getEmployeeTypeCode());
	}

	private void buildRole(String desc, String code)
	{
		sw.writeElementText("\n");
		sw.writeElementText(ROLE);
		sw.writeElementText(":");
		sw.writeElementText(desc);

		if (!isLite)
		{
			sw.writeElementText("\n");
			sw.writeElementText(X_EMPTYPE);
			sw.writeElementText(":");
			sw.writeElementText(code);
		}
	}

	private void buildTitle()
	{
		sw.writeElementText("\n");
		sw.writeElementText(TITLE);
		sw.writeElementText(":");
		sw.writeElementText(emp.getJobResp());
	}

	private void buildAddress()
	{
		if (emp.getWorkLocation() != null)
		{
			sw.writeElementText("\n");
			sw.writeElementText(ADR);
			sw.writeElementText(";");
			sw.writeElementText(WORK);
			sw.writeElementText(":");
			sw.writeElementText(";");
			sw.writeElementText(";");
			sw.writeElementText(emp.getWorkLocation().getAddress1());
			sw.writeElementText(",");
			sw.writeElementText(emp.getWorkLocation().getAddress2());
			sw.writeElementText(";");
			sw.writeElementText(emp.getWorkLocation().getCity());
			sw.writeElementText(";");
			sw.writeElementText(emp.getWorkLocation().getState());
			sw.writeElementText(";");
			sw.writeElementText(emp.getWorkLocation().getPostalCode());
			sw.writeElementText(";");
			sw.writeElementText(emp.getCountryDisplayValue());

			sw.writeElementText("\n");
			sw.writeElementText(X_WORKLOCATION_CODE);
			sw.writeElementText(":");
			sw.writeElementText(emp.getWorkLocationCode());
		}
		
		if (emp.getCountryCode() != null)
		{
			sw.writeElementText("\n");
			sw.writeElementText(X_COUNTRY_CODE);
			sw.writeElementText(":");
			sw.writeElementText(emp.getCountryCode());
		}
	}

	private void buildOffice()
	{
		sw.writeElementText("\n");
		sw.writeElementText(X_BUILDING);
		sw.writeElementText(":");
		sw.writeElementText(emp.getBldgId());

		sw.writeElementText("\n");
		sw.writeElementText(X_FLOOR);
		sw.writeElementText(":");
		sw.writeElementText(emp.getFloor());

		sw.writeElementText("\n");
		sw.writeElementText(X_OFFICE_NUMBER);
		sw.writeElementText(":");
		sw.writeElementText(emp.getOfficeName());    
	}

	private void buildTelephone()
	{
		sw.writeElementText("\n");
		sw.writeElementText(TEL);
		sw.writeElementText(";");
		sw.writeElementText(WORK);
		sw.writeElementText(":");
		sw.writeElementText(emp.getTelephoneNumber());

		if (!isLite)
		{
			sw.writeElementText("\n");
			sw.writeElementText(TEL);
			sw.writeElementText(";");
			sw.writeElementText(CELL);
			sw.writeElementText(":");
			sw.writeElementText(emp.getMobileNumber());

			sw.writeElementText("\n");
			sw.writeElementText(TEL);
			sw.writeElementText(";");
			sw.writeElementText(FAX);
			sw.writeElementText(":");
			sw.writeElementText(emp.getFaxNumber());

			sw.writeElementText("\n");
			sw.writeElementText(TEL);
			sw.writeElementText(";");
			sw.writeElementText(X_IP);
			sw.writeElementText(":");
			sw.writeElementText(emp.getIpTelephoneNumber());

			sw.writeElementText("\n");
			sw.writeElementText(TEL);
			sw.writeElementText(";");
			sw.writeElementText(PAGER);
			sw.writeElementText(":");
			sw.writeElementText(emp.getPagerNumber());

			sw.writeElementText("\n");
			sw.writeElementText(X_PAGER_ID);
			sw.writeElementText(":");
			sw.writeElementText(emp.getPagerId());

			sw.writeElementText("\n");
			sw.writeElementText(X_PAGER_TYPE);
			sw.writeElementText(":");
			sw.writeElementText(emp.getPagerType());

			sw.writeElementText("\n");
			sw.writeElementText(X_PAGER_PROVIDER);
			sw.writeElementText(":");
			sw.writeElementText(emp.getPagerServiceProvider());
		}
	}

	private void buildManager()
	{
		sw.writeElementText("\n");
		sw.writeElementText(X_MANAGER_UID);
		sw.writeElementText(":");
		sw.writeElementText(emp.getManagerUid());
	}

	private void buildIsManager()
	{
		sw.writeElementText("\n");
		sw.writeElementText(X_IS_MANAGER);
		sw.writeElementText(":");
		sw.writeElementText(emp.getIsManager());
	}

	private void buildExperience()
	{
		String experience = "";
		if (emp.getExperience() != null && emp.getExperience().length() > 0)
		{
			experience = emp.getExperience();
		}
		sw.writeElementText("\n");
		sw.writeElementText(X_EXPERIENCE);
		sw.writeElementText(":");
		sw.writeElementText(experience);
	}

	private void buildDescription()
	{
		String description = "";
		if (emp.getDescription() != null && emp.getDescription().length() > 0)
		{
			description = emp.getDescription();
		}
		sw.writeElementText("\n");
		sw.writeElementText(X_DESCRIPTION);
		sw.writeElementText(":");
		sw.writeElementText(description);
	}

	private void buildCategories()
	{
		sw.writeElementText("\n");
		sw.writeElementText(CATEGORIES);
		sw.writeElementText(":");
		sw.writeElementText(buildCommaDelimitedTagList(emp.getProfileTags()));
	}

	private void buildScheduleUrls()
	{
		if (emp.getFreeBusyUrl() != null && emp.getFreeBusyUrl().length() > 0)
		{
			sw.writeElementText("\n");
			sw.writeElementText(X_FREEBUSY_URL);
			sw.writeElementText(";");
			sw.writeElementText(VALUE);
			sw.writeElementText("=");
			sw.writeElementText(URL);
			sw.writeElementText(":");
			sw.writeElementText(emp.getFreeBusyUrl());
		}

		if (emp.getCalendarUrl() != null && emp.getCalendarUrl().length() > 0)
		{
			sw.writeElementText("\n");
			sw.writeElementText(X_CALENDAR_URL);
			sw.writeElementText(";");
			sw.writeElementText(VALUE);
			sw.writeElementText("=");
			sw.writeElementText(URL);
			sw.writeElementText(":");
			sw.writeElementText(emp.getCalendarUrl());
		}
	}

	private void buildDepartmentNumber()
	{
		sw.writeElementText("\n");
		sw.writeElementText(X_DEPARTMENT_NUMBER);
		sw.writeElementText(":");
		sw.writeElementText(emp.getDeptNumber());
		if (!isLite)
		{
			sw.writeElementText("\n");
			sw.writeElementText(X_DEPARTMENT_TITLE);
			sw.writeElementText(":");
			sw.writeElementText(emp.getDepartmentTitle());
		}
	}

	private void buildShift()
	{
		sw.writeElementText("\n");
		sw.writeElementText(X_SHIFT);
		sw.writeElementText(":");
		sw.writeElementText(emp.getShift());
	}

	private void buildExtensionProperties()
	{
	  ProfileType profileType = ProfileTypeHelper.getProfileType(emp.getProfileType());
	  
		for (ExtensionAttributeConfig extConfig : DMConfig.instance().getExtensionAttributeConfig().values())
		{
			Property property = profileType.getPropertyById( extConfig.getExtensionId() );
			if (property != null && !property.isHidden())
			{
	            if ((extConfig.getExtensionType() == ExtensionType.SIMPLE || extConfig.getExtensionType() == ExtensionType.RICHTEXT))
	                buildExtensionProperty(emp.getProfileExtension(extConfig.getExtensionId(), true));			  
			}
		}
	}

	private void buildExtensionProperty(ProfileExtension pe)
	{
		sw.writeElementText("\n");
		sw.writeElementText(X_EXTENSION_PROPERTY);
		sw.writeElementText(";");
		sw.writeElementText(VALUE);
		sw.writeElementText("=");
		sw.writeElementText(X_EXTENSION_PROPERTY_ID);
		sw.writeElementText(":");
		sw.writeElementText(pe.getPropertyId());
		sw.writeElementText(";");
		sw.writeElementText(VALUE);
		sw.writeElementText("=");
		sw.writeElementText(X_EXTENSION_KEY);
		sw.writeElementText(":");
		sw.writeElementText(pe.getExtKey());
		sw.writeElementText(";");
		if (!hideExtensionProfiName) {
			sw.writeElementText(VALUE);
			sw.writeElementText("=");
			sw.writeElementText(X_EXTENSION_NAME);
			sw.writeElementText(":");
			sw.writeElementText(pe.getName());
			sw.writeElementText(";");
		}
		sw.writeElementText(VALUE);
		sw.writeElementText("=");
		sw.writeElementText(X_EXTENSION_VALUE);
		sw.writeElementText(":");
		sw.writeElementText(escape(pe.getStringValue()));
		sw.writeElementText(";");
		sw.writeElementText(VALUE);
		sw.writeElementText("=");
		sw.writeElementText(X_EXTENSION_DATA_TYPE);
		sw.writeElementText(":");
		sw.writeElementText(pe.getDataType());
	}

	private void buildIds()
	{
		sw.writeElementText("\n");
		sw.writeElementText(X_PROFILE_KEY);
		sw.writeElementText(":");
		sw.writeElementText(emp.getKey());
		
		sw.writeElementText("\n");
		sw.writeElementText(UID);
		sw.writeElementText(":");
		sw.writeElementText(emp.getGuid());

		sw.writeElementText("\n");
		sw.writeElementText(X_PROFILE_UID);
		sw.writeElementText(":");
		sw.writeElementText(emp.getUid());
		
		sw.writeElementText("\n");
		sw.writeElementText(X_LCONN_USERID);
		sw.writeElementText(":");
		sw.writeElementText(emp.getUserid());

		if (!isLite)
		{
			sw.writeElementText("\n");
			sw.writeElementText(X_EMPLOYEE_NUMBER);
			sw.writeElementText(":");
			sw.writeElementText(emp.getEmployeeNumber());
		}
	}

	private void buildLastUpdate()
	{
		sw.writeElementText("\n");
		sw.writeElementText(REV);
		sw.writeElementText(":");
		sw.writeElementText(formatTimestamp(emp.getLastUpdate()));
	}
	
	private void buildProfileType() {
		sw.writeElementText("\n");
		sw.writeElementText(X_PROFILE_TYPE);
		sw.writeElementText(":");
		sw.writeElementText(emp.getProfileType());
	}

	private String formatTimestamp(Date time)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'S'Z'");
		sdf.getCalendar().setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(time);
	}

	private void appendVCardBegin()
	{
		sw.writeElementText("\n");
		sw.writeElementText(BEGIN);
		sw.writeElementText("\n");
		sw.writeElementText(VCARD_VERSION);
	}

	private void appendVCardEnd()
	{
		sw.writeElementText("\n");
		sw.writeElementText(END);
		sw.writeElementText("\n");
	}

	private String escape(String source)
	{
		if (source != null)
		{
			source = source.replaceAll("\\\\", "\\\\\\\\");
			source = source.replaceAll(";", "\\\\;");
			source = source.replaceAll("\\:", "\\\\:");
			source = source.replaceAll("\\=", "\\\\=");
			return source;
		}

		return "";
	}

	private static final String BEGIN = "BEGIN:VCARD";
	private static final String VCARD_VERSION = "VERSION:2.1";
	private static final String END = "END:VCARD";

	private static final String ADR = "ADR";
	private static final String AGENT = "AGENT";
	private static final String CATEGORIES = "CATEGORIES";  
	private static final String CELL = "CELL";
	private static final String EMAIL = "EMAIL";
	private static final String FAX = "FAX";
	private static final String FN = "FN";
	private static final String INTERNET = "INTERNET";
	private static final String N = "N";  
	private static final String NICKNAME = "NICKNAME"; 
	private static final String ORG = "ORG";  
	private static final String PHOTO = "PHOTO";
	private static final String REV = "REV";
	private static final String ROLE = "ROLE";
	private static final String SOUND = "SOUND";  
	private static final String TEL = "TEL";
	private static final String TITLE = "TITLE";  
	private static final String TZ = "TZ";
	private static final String UID = "UID";
	private static final String URL = "URL";
	private static final String VALUE = "VALUE";
	private static final String WORK = "WORK";

	//vcard types
	//private static final String COUNTRY_NAME = "COUNTRY_NAME";
	//private static final String EXTENDED_ADDRESS = "EXTENDED_ADDRESS";

	private static final String HONORIFIC_PREFIX = "HONORIFIC_PREFIX";
	//private static final String LOCALITY = "LOCALITY";
	//private static final String ORGANIZATION_UNIT = "ORGANIZATION_UNIT";
	private static final String PAGER = "PAGER";
	//private static final String POSTAL = "POSTAL";
	//private static final String POSTAL_CODE = "POSTAL_CODE";
	//private static final String REGION = "REGION";
	//private static final String STREET_ADDRESS = "STREET_ADDRESS";
	//private static final String TYPE = "TYPE";
	//private static final String VCARD = "VCARD";

	//vcard extensions
	private static final String X_ALTERNATE_LAST_NAME = "X_ALTERNATE_LAST_NAME";
	private static final String X_BLOG_URL = "X_BLOG_URL";
	private static final String X_BUILDING = "X_BUILDING";
	private static final String X_CALENDAR_URL = "X_CALENDAR_URL";
	private static final String X_COUNTRY_CODE = "X_COUNTRY_CODE";
	private static final String X_DEPARTMENT_NUMBER = "X_DEPARTMENT_NUMBER";
	private static final String X_DEPARTMENT_TITLE = "X_DEPARTMENT_TITLE";
	private static final String X_DESCRIPTION = "X_DESCRIPTION";
	private static final String X_EMPLOYEE_NUMBER = "X_EMPLOYEE_NUMBER";
	private static final String X_EMPTYPE = "X_EMPTYPE";
	private static final String X_EXPERIENCE = "X_EXPERIENCE";
	private static final String X_FLOOR = "X_FLOOR";
	private static final String X_FREEBUSY_URL = "X_FREEBUSY_URL";
	private static final String X_GROUPWARE_MAIL = "X_GROUPWARE_MAIL";
	private static final String X_PROFILE_UID = "X_PROFILE_UID";
	private static final String X_PROFILE_KEY = "X_PROFILE_KEY";
	private static final String X_LCONN_USERID = "X_LCONN_USERID";
	private static final String X_IP = "X_IP";
	private static final String X_IS_MANAGER = "X_IS_MANAGER";
	private static final String X_MANAGER_UID = "X_MANAGER_UID";
	private static final String X_NATIVE_FIRST_NAME = "X_NATIVE_FIRST_NAME";
	private static final String X_NATIVE_LAST_NAME = "X_NATIVE_LAST_NAME";
	//private static final String X_OFFICE = "X_OFFICE";
	private static final String X_OFFICE_NUMBER = "X_OFFICE_NUMBER";
	private static final String X_ORGANIZATION_CODE = "X_ORGANIZATION_CODE";
	private static final String X_PAGER_ID = "X_PAGER_ID";
	private static final String X_PAGER_TYPE = "X_PAGER_TYPE";
	private static final String X_PAGER_PROVIDER = "X_PAGER_PROVIDER";
	//private static final String X_PREFERRED_FIRST_NAME = "X_PREFERRED_FIRST_NAME";
	private static final String X_PREFERRED_LANGUAGE = "X_PREFERRED_LANGUAGE";
	private static final String X_PREFERRED_LAST_NAME = "X_PREFERRED_LAST_NAME";
	private static final String X_SHIFT = "X_SHIFT";
	//private static final String X_STREET_ADDRESS2 = "X_STREET_ADDRESS2";
	private static final String X_WORKLOCATION_CODE = "X_WORKLOCATION_CODE";
	
	private static final String X_PROFILE_TYPE = "X_PROFILE_TYPE";

	private static final String X_EXTENSION_PROPERTY = "X_EXTENSION_PROPERTY";
	private static final String X_EXTENSION_PROPERTY_ID = "X_EXTENSION_PROPERTY_ID";
	private static final String X_EXTENSION_KEY = "X_EXTENSION_KEY";
	private static final String X_EXTENSION_NAME = "X_EXTENSION_NAME";
	private static final String X_EXTENSION_VALUE = "X_EXTENSION_VALUE";
	private static final String X_EXTENSION_DATA_TYPE = "X_EXTENSION_DATA_TYPE";
}
