/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.data;

import static com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants.EXT_ATTR_KEY_BASE;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.acl.model.UserInfo;
import com.ibm.lconn.core.appext.data.ProfilesSNAXPerson;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessage;
import com.ibm.lconn.core.web.secutil.Sha256Encoder;
import com.ibm.lconn.profiles.config.types.ProfileTypeConstants;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.data.codes.WorkLocation;

import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;

import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * 
 */
public class Employee implements DatabaseRecord, Map<String,Object>
{ 
	/**
	 * 
	 */
	@SuppressWarnings("unused")
    private static final long serialVersionUID = -714173735132760636L;

	private static Log LOG = LogFactory.getLog(Employee.class);

	private static TDIProfileService service = null;

	private String url;
	private EntryMessage status;
	private UserInfo userInfo;
	private HashMap<String,Object> attributeMap = new HashMap<String,Object>(60);

	public Employee(){
		// default user state matches the db default
		setState(UserState.ACTIVE);
		// default user mode
		setMode(UserMode.INTERNAL);
	}

	public Employee clone()
	{
		Employee c = new Employee();
		c.url = this.url;
		c.status = status;
		c.userInfo = userInfo;
		c.attributeMap.putAll(attributeMap);
		return c;
	}
	
	public String getRecordSearchString()
	{
		String rv = "key" + "=" + (String)attributeMap.get("key") + "&format=full";
		try
		{
			rv = "key" + "=" + URLEncoder.encode((String)attributeMap.get("key"), "UTF-8") + "&format=full";
		}
		catch (UnsupportedEncodingException uee)
		{
			LOG.error(uee.getMessage(), uee);
		}
		return rv;
	}

	public String getRecordTitle()
	{
		return (String)attributeMap.get("displayName");
	}

	public String getRecordType()
	{
		return "employee";
	}

	public String getRecordSummary()
	{
		return "Profile information for " + (String)get("displayName");
	}

	public Date getRecordUpdated()
	{
		return getLastUpdate();
	}

	public String getRecordId()
	{
		return getKey();
	}

	public String getKey()
	{
		return (String) attributeMap.get("key");
	}
	
	public void setKey(String key)
	{
		attributeMap.put("key", key);
	}

	public String getTenantKey()
	{
		return (String) attributeMap.get(PeoplePagesServiceConstants.TENANT_KEY);
	}
	
	public void setTenantKey(String tenantKey)
	{
		// see AbstractDataModel (which Employee does not extend)
		if (Tenant.DB_SINGLETENANT_KEY.equals(tenantKey)){
			attributeMap.put(PeoplePagesServiceConstants.TENANT_KEY, Tenant.SINGLETENANT_KEY);
		}
		else{
			attributeMap.put(PeoplePagesServiceConstants.TENANT_KEY,tenantKey);
		}
	}
	
	public String getHomeTenantKey()
	{
		return (String) attributeMap.get(PeoplePagesServiceConstants.HOME_TENANT_KEY);
	}
	
	public void setHomeTenantKey(String tenantKey)
	{
		// see AbstractDataModel (which Employee does not extend)
		if (Tenant.DB_SINGLETENANT_KEY.equals(tenantKey)){
			attributeMap.put(PeoplePagesServiceConstants.HOME_TENANT_KEY, Tenant.SINGLETENANT_KEY);
		}
		else{
			attributeMap.put(PeoplePagesServiceConstants.HOME_TENANT_KEY, tenantKey);
		}
	}
	
	public String getDistinguishedName() 
	{
		return (String) attributeMap.get("distinguishedName");
	}

	public void setDistinguishedName(String distinguishedName) 
	{
		attributeMap.put("distinguishedName", distinguishedName);
	}

	/**
	 * @return Returns the countryDisplayValue.
	 */
	public String getCountryDisplayValue()
	{
		return (String)attributeMap.get("countryDisplayValue");
	}

	/**
	 * @param countryDisplayValue The countryDisplayValue to set.
	 */
	public void setCountryDisplayValue(String countryDisplayValue)
	{
		attributeMap.put("countryDisplayValue", countryDisplayValue);
	}

	/**
	 * @return Returns the imageUrl.
	 */
	public String getImageUrl()
	{
		return (String)attributeMap.get("photoUrl");
	}

	/**
	 * @param imageUrl The imageUrl to set.
	 */
	public void setImageUrl(String imageUrl)
	{
		attributeMap.put("photoUrl", imageUrl);
	}

	/**
	 * @return Returns the imageUrl.
	 */
	public String getPronunciationUrl()
	{
		return (String)attributeMap.get("pronunciationUrl");
	}

	/**
	 * @param imageUrl The imageUrl to set.
	 */
	public void setPronunciationUrl(String pronunciationUrl)
	{
		attributeMap.put("pronunciationUrl", pronunciationUrl);
	}

	/**
	 * @return Returns the alternateLastname.
	 */
	public String getAlternateLastname()
	{
		return (String)attributeMap.get("alternateLastname");
	}

	/**
	 * @param alternateLastname The alternateLastname to set.
	 */
	public void setAlternateLastname(String alternateLastname)
	{
		attributeMap.put("alternateLastname", alternateLastname);
	}

	/**
	 * @return Returns the bldgId.
	 */
	public String getBldgId()
	{
		return (String)attributeMap.get("bldgId");
	}

	/**
	 * @param bldgId The bldgId to set.
	 */
	public void setBldgId(String bldgId)
	{
		attributeMap.put("bldgId", bldgId);
	}

	/**
	 * @return Returns the countryCode.
	 */
	public String getCountryCode()
	{
		return (String)attributeMap.get("countryCode");
	}

	/**
	 * @param countryCode The countryCode to set.
	 */
	public void setCountryCode(String countryCode)
	{
		attributeMap.put("countryCode", countryCode);
	}

	/**
	 * @return Returns the courtesyTitle.
	 */
	public String getCourtesyTitle()
	{
		return (String)attributeMap.get("courtesyTitle");
	}

	/**
	 * @param courtesyTitle The courtesyTitle to set.
	 */
	public void setCourtesyTitle(String courtesyTitle)
	{
		attributeMap.put("courtesyTitle", courtesyTitle);
	}

	/**
	 * @return Returns the deptNumber.
	 */
	public String getDeptNumber()
	{
		return (String)attributeMap.get("deptNumber");
	}

	/**
	 * @param deptNumber The deptNumber to set.
	 */
	public void setDeptNumber(String deptNumber)
	{
		attributeMap.put("deptNumber", deptNumber);
	}
	
	/**
	 * @return Returns the department.
	 */
	public String getDepartmentTitle()
	{
		return (String)attributeMap.get("departmentTitle");
	}

	/**
	 * @param departmentTitle The departmentTitle to set.
	 */
	public void setDepartmentTitle(String departmentTitle)
	{
		attributeMap.put("departmentTitle", departmentTitle);
	}

	/**
	 * @return Returns the displayName.
	 */
	public String getDisplayName()
	{
		return (String)attributeMap.get("displayName");
	}

	/**
	 * @param displayName The displayName to set.
	 */
	public void setDisplayName(String displayName)
	{
		attributeMap.put("displayName", displayName);
	}

	/**
	 * @return Returns the email.
	 */
	public String getEmail()
	{
		return (String)attributeMap.get("email");
	}

	/**
	 * @param email The email to set.
	 */
	public void setEmail(String email)
	{
		attributeMap.put("email", email);
	}

	/**
	 * @return Returns the hashed email.
	 */
	public String getMcode()
	{
		//only do the SHA256 hash when it is first requested.
		String hash = (String)attributeMap.get(PeoplePagesServiceConstants.MCODE);
		
		if (StringUtils.isEmpty(hash)) {
			hash = Sha256Encoder.hashLowercaseStringUTF8(getEmail(), true);
			attributeMap.put(PeoplePagesServiceConstants.MCODE, hash);
		}

		return hash;
	}

	/**
	 * @return Returns the employeeNumber.
	 */
	public String getEmployeeNumber()
	{
		return (String)attributeMap.get("employeeNumber");
	}

	/**
	 * @param employeeNumber The employeeNumber to set.
	 */
	public void setEmployeeNumber(String employeeNumber)
	{
		attributeMap.put("employeeNumber", employeeNumber);
	}

	/**
	 * @return Returns the employeeTypeDesc.
	 */
	public String getEmployeeTypeCode()
	{
		return (String)attributeMap.get("employeeTypeCode");
	}

	/**
	 * @param employeeTypeDesc The employeeTypeDesc to set.
	 */
	public void setEmployeeTypeCode(String employeeTypeCode)
	{
		attributeMap.put("employeeTypeCode", employeeTypeCode);
	}

	/**
	 * @return Returns the faxNumber.
	 */
	public String getFaxNumber()
	{
		return (String)attributeMap.get("faxNumber");
	}

	/**
	 * @param faxNumber The faxNumber to set.
	 */
	public void setFaxNumber(String faxNumber)
	{
		attributeMap.put("faxNumber", faxNumber);
	}

	/**
	 * @return Returns the floor.
	 */
	public String getFloor()
	{
		return (String)attributeMap.get("floor");
	}

	/**
	 * @param floor The floor to set.
	 */
	public void setFloor(String floor)
	{
		attributeMap.put("floor", floor);
	}

	/**
	 * @return Returns the groupwareEmail.
	 */
	public String getGroupwareEmail()
	{
		return (String)attributeMap.get("groupwareEmail");
	}

	/**
	 * @param groupwareEmail The groupwareEmail to set.
	 */
	public void setGroupwareEmail(String groupwareEmail)
	{
		attributeMap.put("groupwareEmail", groupwareEmail);
	}

	/**
	 * @return Returns the ipTelephoneNumber.
	 */
	public String getIpTelephoneNumber()
	{
		return (String)attributeMap.get("ipTelephoneNumber");
	}

	/**
	 * @param ipTelephoneNumber The ipTelephoneNumber to set.
	 */
	public void setIpTelephoneNumber(String ipTelephoneNumber)
	{
		attributeMap.put("ipTelephoneNumber", ipTelephoneNumber);
	}

	/**
	 * @return Returns the isManager.
	 */
	public String getIsManager()
	{
		return (String)attributeMap.get("isManager");
	}

	/**
	 * @param isManager The isManager to set.
	 */
	public void setIsManager(String isManager)
	{
		attributeMap.put("isManager", isManager);
	}

	/**
	 * @return Returns the jobResp.
	 */
	public String getJobResp()
	{
		return (String)attributeMap.get("jobResp");
	}

	/**
	 * @param jobResp The jobResp to set.
	 */
	public void setJobResp(String jobResp)
	{
		attributeMap.put("jobResp", jobResp);
	}

	/**
	 * @return Returns the lastUpdate.
	 */
	public java.sql.Timestamp getLastUpdate()
	{
		Object ts = attributeMap.get("lastUpdate");
		if (ts == null || !(ts instanceof Timestamp)) {
		ts = new java.sql.Timestamp(System.currentTimeMillis());
		setLastUpdate((java.sql.Timestamp)ts);
		}
		return (Timestamp) ts;
	}

	/**
	 * @param lastUpdate The lastUpdate to set.
	 */
	public void setLastUpdate(java.sql.Timestamp lastUpdate)
	{
		attributeMap.put("lastUpdate", lastUpdate);
	}

	/**
	 * @return Returns the managerUid.
	 */
	public String getManagerUid()
	{
		return (String)attributeMap.get("managerUid");
	}

	/**
	 * @param managerUid The managerUid to set.
	 */
	public void setManagerUid(String managerUid)
	{
		attributeMap.put("managerUid", managerUid);
	}
	
	/**
	 * @return Returns the managerUserid.
	 */
	public String getManagerUserid()
	{
		return (String)attributeMap.get("managerUserid");
	}

	/**
	 * @param managerUid The managerUserid to set.
	 */
	public void setManagerUserid(String managerUserid)
	{
		attributeMap.put("managerUserid", managerUserid);
	}

	public String getManagerKey()
	{
		return (String)attributeMap.get("managerKey");
	}
	
	public void setManagerKey(String managerKey)
	{
		attributeMap.put("managerKey", managerKey);
	}
	
	public String getManagerName()
	{
		return (String)attributeMap.get("managerName");
	}

	public void setManagerName(String managerName)
	{
		attributeMap.put("managerName", managerName);
	}
	
	public String getManagerEmail()
	{
		return (String)attributeMap.get("managerEmail");
	}

	public void setManagerEmail(String managerEmail)
	{
		attributeMap.put("managerEmail", managerEmail);
	}
	
	/**
	 * @return Returns the mobileNumber.
	 */
	public String getMobileNumber()
	{
		return (String)attributeMap.get("mobileNumber");
	}

	/**
	 * @param mobileNumber The mobileNumber to set.
	 */
	public void setMobileNumber(String mobileNumber)
	{
		attributeMap.put("mobileNumber", mobileNumber);
	}

	/**
	 * @return Returns the nativeFirstName.
	 */
	public String getNativeFirstName()
	{
		return (String)attributeMap.get("nativeFirstName");
	}

	/**
	 * @param nativeFirstName The nativeFirstName to set.
	 */
	public void setNativeFirstName(String nativeFirstName)
	{
		attributeMap.put("nativeFirstName", nativeFirstName);
	}

	/**
	 * @return Returns the nativeLastName.
	 */
	public String getNativeLastName()
	{
		return (String)attributeMap.get("nativeLastName");
	}

	/**
	 * @param nativeLastName The nativeLastName to set.
	 */
	public void setNativeLastName(String nativeLastName)
	{
		attributeMap.put("nativeLastName", nativeLastName);
	}

	/**
	 * @return Returns the officeName.
	 */
	public String getOfficeName()
	{
		return (String)attributeMap.get("officeName");
	}

	/**
	 * @param officeName The officeName to set.
	 */
	public void setOfficeName(String officeName)
	{
		attributeMap.put("officeName", officeName);
	}

	/**
	 * @return Returns the orgId.
	 */
	public String getOrgId()
	{
		return (String)attributeMap.get("orgId");
	}

	/**
	 * @param orgId The orgId to set.
	 */
	public void setOrgId(String orgId)
	{
		attributeMap.put("orgId", orgId);
	}

	/**
	 * @return Returns the pagerId.
	 */
	public String getPagerId()
	{
		return (String)attributeMap.get("pagerId");
	}

	/**
	 * @param pagerId The pagerId to set.
	 */
	public void setPagerId(String pagerId)
	{
		attributeMap.put("pagerId", pagerId);
	}

	/**
	 * @return Returns the pagerNumber.
	 */
	public String getPagerNumber()
	{
		return (String)attributeMap.get("pagerNumber");
	}

	/**
	 * @param pagerNumber The pagerNumber to set.
	 */
	public void setPagerNumber(String pagerNumber)
	{
		attributeMap.put("pagerNumber", pagerNumber);
	}

	/**
	 * @return Returns the pagerServiceProvider.
	 */
	public String getPagerServiceProvider()
	{
		return (String)attributeMap.get("pagerServiceProvider");
	}

	/**
	 * @param pagerServiceProvider The pagerServiceProvider to set.
	 */
	public void setPagerServiceProvider(String pagerServiceProvider)
	{
		attributeMap.put("pagerServiceProvider", pagerServiceProvider);
	}

	/**
	 * @return Returns the pagerType.
	 */
	public String getPagerType()
	{
		return (String)attributeMap.get("pagerType");
	}

	/**
	 * @param pagerType The pagerType to set.
	 */
	public void setPagerType(String pagerType)
	{
		attributeMap.put("pagerType", pagerType);
	}

	/**
	 * @return Returns the preferredFirstName.
	 */
	public String getPreferredFirstName()
	{
		return (String)attributeMap.get("preferredFirstName");
	}

	/**
	 * @param preferredFirstName The preferredFirstName to set.
	 */
	public void setPreferredFirstName(String preferredFirstName)
	{
		attributeMap.put("preferredFirstName", preferredFirstName);
	}

	/**
	 * @return Returns the preferredLanguage.
	 */
	public String getPreferredLanguage()
	{
		return (String)attributeMap.get("preferredLanguage");
	}

	/**
	 * @param preferredLanguage The preferredLanguage to set.
	 */
	public void setPreferredLanguage(String preferredLanguage)
	{
		attributeMap.put("preferredLanguage", preferredLanguage);
	}

	/**
	 * @return Returns the preferredLastName.
	 */
	public String getPreferredLastName()
	{
		return (String)attributeMap.get("preferredLastName");
	}

	/**
	 * @param preferredLastName The preferredLastName to set.
	 */
	public void setPreferredLastName(String preferredLastName)
	{
		attributeMap.put("preferredLastName", preferredLastName);
	}

	/**
	 * @return Returns the secretaryUid.
	 */
	public String getSecretaryUid()
	{
		return (String)attributeMap.get("secretaryUid");
	}

	/**
	 * @param secretaryUid The secretaryUid to set.
	 */
	public void setSecretaryUid(String secretaryUid)
	{
		attributeMap.put("secretaryUid", secretaryUid);
	}

	/**
	 * @return Returns the shift.
	 */
	public String getShift()
	{
		return (String)attributeMap.get("shift");
	}

	/**
	 * @param shift The shift to set.
	 */
	public void setShift(String shift)
	{
		attributeMap.put("shift", shift);
	}

	/**
	 * @return Returns the telephoneNumber.
	 */
	public String getTelephoneNumber()
	{
		return (String)attributeMap.get("telephoneNumber");
	}

	/**
	 * @param telephoneNumber The telephoneNumber to set.
	 */
	public void setTelephoneNumber(String telephoneNumber)
	{
		attributeMap.put("telephoneNumber", telephoneNumber);
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle()
	{
		return (String)attributeMap.get("title");
	}

	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title)
	{
		attributeMap.put("title", title);
	}

	/**
	 * @return Returns the uid.
	 */
	public String getUid()
	{
		return (String)attributeMap.get("uid");
	}

	/**
	 * @param uid The uid to set.
	 */
	public void setUid(String uid)
	{
		attributeMap.put("uid", uid);
	}

	/**
	 * @return Returns the workLocation.
	 */
	public String getWorkLocationCode()
	{
		return (String)attributeMap.get("workLocationCode");
	}

	/**
	 * @param workLocation The workLocation to set.
	 */
	public void setWorkLocationCode(String workLocationCode)
	{
		attributeMap.put("workLocationCode", workLocationCode);
	}

	/**
	 * @return Returns the blogUrl.
	 */
	public String getBlogUrl()
	{
		return (String)attributeMap.get("blogUrl");
	}

	/**
	 * @param blogUrl The blogUrl to set.
	 */
	public void setBlogUrl(String blogUrl)
	{
		attributeMap.put("blogUrl", blogUrl);
	}

	/**
	 * @return Returns the calendarUrl.
	 */
	public String getCalendarUrl()
	{
		return (String)attributeMap.get("calendarUrl");
	}

	/**
	 * @param calendarUrl The calendarUrl to set.
	 */
	public void setCalendarUrl(String calendarUrl)
	{
		attributeMap.put("calendarUrl", calendarUrl);
	}

	/**
	 * @return Returns the freeBusyUrl.
	 */
	public String getFreeBusyUrl()
	{
		return (String)attributeMap.get("freeBusyUrl");
	}

	/**
	 * @param freeBusyUrl The freeBusyUrl to set.
	 */
	public void setFreeBusyUrl(String freeBusyUrl)
	{
		attributeMap.put("freeBusyUrl", freeBusyUrl);
	}

	/**
	 * @return Returns the secretaryName.
	 */
	public String getSecretaryName()
	{
		return (String)attributeMap.get("secretaryName");
	}

	/**
	 * @param secretaryName The secretaryName to set.
	 */
	public void setSecretaryName(String secretaryName)
	{
		attributeMap.put("secretaryName", secretaryName);
	}
	
	/**
	 * @return Returns the secretaryKey.
	 */
	public String getSecretaryKey()
	{
		return (String)attributeMap.get("secretaryKey");
	}

	/**
	 * @param secretaryKey The secretaryKey to set.
	 */
	public void setSecretaryKey(String secretaryKey)
	{
		attributeMap.put("secretaryKey", secretaryKey);
	}

	/**
	 * @return Returns the secretaryEmail.
	 */
	public String getSecretaryEmail()
	{
		return (String)attributeMap.get("secretaryEmail");
	}

	/**
	 * @param secretaryName The secretaryName to set.
	 */
	public void setSecretaryEmail(String secretaryEmail)
	{
		attributeMap.put("secretaryEmail", secretaryEmail);
	}
	
	/**
	 * @return Returns the secretaryEmail.
	 */
	public String getSecretaryUserid()
	{
		return (String)attributeMap.get("secretaryUserid");
	}

	/**
	 * @param secretaryName The secretaryName to set.
	 */
	public void setSecretaryUserid(String secretaryUserid)
	{
		attributeMap.put("secretaryUserid", secretaryUserid);
	}

	/**
	 * @return Returns the workLocation.
	 */
	public WorkLocation getWorkLocation()
	{
		return (WorkLocation)attributeMap.get("workLocation");
	}

	/**
	 * @param workLocation The workLocation to set.
	 */
	public void setWorkLocation(WorkLocation workLocation)
	{
		attributeMap.put("workLocation", workLocation);
	}

	/**
	 * @return Returns the employeeTypeDesc.
	 */
	public String getEmployeeTypeDesc()
	{
		return (String)attributeMap.get("employeeTypeDesc");
	}

	/**
	 * @param employeeTypeDesc The employeeTypeDesc to set.
	 */
	public void setEmployeeTypeDesc(String employeeTypeDesc)
	{
		attributeMap.put("employeeTypeDesc", employeeTypeDesc);
	}

	/**
	 * @return Returns the givenName.
	 */
	public String getGivenName()
	{
		return (String)attributeMap.get("givenName") != null ? ((String)attributeMap.get("givenName")).trim() : null;
	}

	/**
	 * @param givenName The givenName to set.
	 */
	public void setGivenName(String givenName)
	{
		attributeMap.put("givenName", givenName);
	}

	/**
	 * @return Returns the surname.
	 */
	public String getSurname()
	{
		return (String)attributeMap.get("surname") != null ? ((String)attributeMap.get("surname")).trim() : null;
	}

	/**
	 * @param surname The surname to set.
	 */
	public void setSurname(String surname)
	{
		attributeMap.put("surname", surname);
	}

	/**
	 * @return Returns the organizationTitle.
	 */
	public String getOrganizationTitle()
	{
		return (String)attributeMap.get("organizationTitle");
	}

	/**
	 * @param organizationTitle The organizationTitle to set.
	 */
	public void setOrganizationTitle(String organizationTitle)
	{
		attributeMap.put("organizationTitle", organizationTitle);
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription()
	{
		return (String)attributeMap.get("description");
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description)
	{
		attributeMap.put("description", description);
	}

	/**
	 * @return Returns the experience.
	 */
	public String getExperience()
	{
		return (String)attributeMap.get("experience");
	}

	/**
	 * @param experience The experience to set.
	 */
	public void setExperience(String experience)
	{
		attributeMap.put("experience", experience);
	}

	@SuppressWarnings("unchecked")
	public List<Tag> getProfileTags()
	{
		return (List<Tag>)get("profileTags");
	}

	public void setProfileTags(List<Tag> tags)
	{
		attributeMap.put("profileTags", tags);
	}

	public String getTimezone()
	{
		return (String)get("timezone");
	}

	public void setTimezone(String timezone)
	{
		attributeMap.put("timezone", timezone);
	}

	public String getGuid()
	{
		return (String)get("guid");
	}

	public void setGuid(String guid)
	{
		attributeMap.put("guid", guid);
	}

	public String getCommonName()
	{
		return (String)get("cn");
	}

	public void setCommonName(String commonName)
	{
		attributeMap.put("cn", commonName);
	}

	public void setLoginId(String login)
	{
		attributeMap.put("loginId", login);
	}

	public String getLoginId()
	{
		return (String)get("loginId");
	}

	public String getUserid()
	{
		String attrId = ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName();
		
		/* - overkill as this is done via XML Schema validation
		AssertionUtils.assert(
				PeoplePagesServiceConstants.UID.equals(attrId) ||
				PeoplePagesServiceConstants.GUID.equals(attrId),
				ProfilesAssertionException.Type.UNSUPPORTED_CONFIGURATION);
		*/
	
		return (String) get(attrId);
	}

	public UserState getState(){
		return (UserState)get("state");
	}

	public void setState(UserState val) {
		assert (val != null);
		attributeMap.put("state", val);
	}
	
	public boolean isActive() {
		return (UserState.ACTIVE.equals(getState()));
	}
	
	public final UserMode getMode(){
		return (UserMode)get("mode");
	}

	public final void setMode(UserMode val) {
		assert (val != null);
		// 'userMode' matched PropertyEnum.USER_MODE value
		attributeMap.put("mode", val);
	}
	
	public final boolean isExternal(){
		return (UserMode.EXTERNAL.equals(getMode()));
	}
	
    /**
     */
    public List<String> getRoles()
    {
        boolean isDebug = LOG.isDebugEnabled();

        List<String> userRoles = new ArrayList<String>();
        if (null != this.getKey()) {
        	if (service == null)
        		service = AppServiceContextAccess.getContextObject(TDIProfileService.class);
        	List<EmployeeRole> roles = service.getRoles(this.getKey());
        	for (EmployeeRole empRole : roles)
        	{
        		String aRole = new String (empRole.getRoleId());
        		userRoles.add(aRole);
        	}
        	if (isDebug) {
        		LOG.debug("User: " + this.getKey() + " roles " + userRoles.toString());
        	}
        }
        return userRoles;
    }

    public boolean hasExtendedRole()
    {
        boolean hasExtendedRole = false;
        List<String>  userRoles = this.getRoles();
        hasExtendedRole = containsExtendedRole(userRoles);
        return hasExtendedRole;
    }
    private boolean containsExtendedRole(List<String> userRoles)
    {
        boolean isTrace = LOG.isTraceEnabled();

        boolean containsExtended = false;
        int i = 0;
        while ((containsExtended == false) && (i < userRoles.size())) {
            String roleId = userRoles.get(i);
            if (isTrace) {
                LOG.trace(" - "+ PeoplePagesServiceConstants.ROLEID + " [" + i + "] : "  + roleId );
            }
            if (roleId.equals(PeoplePagesServiceConstants.ROLE_EXTENDED)) {
                containsExtended = true;
            }
            i++;
        }
        return containsExtended;
    }

	/**
	 * Returns the ProfileExtension associated with a given extensionId. If
	 * <code>hideNull</code> is <code>true</code> and the
	 * <code>ProfileExtension</code> is <code>null</code> this method will
	 * return an empty <code>ProfileExtension</code>.
	 * 
	 * @param extensionId
	 * @param hideNull
	 * @return
	 */
	public ProfileExtension getProfileExtension(String extensionId, boolean hideNull)
	{		
		ProfileExtension pe = (ProfileExtension)get(getAttributeIdForExtensionId(extensionId));
		
		if (pe == null && hideNull)
		{
			pe = new ProfileExtension();
			pe.setKey(getKey());
			pe.setTenantKey(getTenantKey());
			// do we need homeTenantKey in pe?
			pe.setPropertyId(extensionId);
			pe.setValue(null);
			pe.setExtendedValue(null);
			pe.setMaskNull(true);
			pe.setRecordUpdated(getRecordUpdated());
			pe.setName(null);
			
			setProfileExtension(pe);
		}
		
		return pe;
	}

	public void setProfileExtension(ProfileExtension extension)
	{
		extension.setKey(getKey()); // make sure extension prof key is set
		attributeMap.put(getAttributeIdForExtension(extension),extension);
	}
	
	public static final String getAttributeIdForExtension(ProfileExtension extensionProperty)
	{
		return getAttributeIdForExtensionId(extensionProperty.getPropertyId());
	}
	
	/**
	 * Returns the property key for the extensionId specified.
	 * 
	 * @param extensionId
	 * @return
	 */
	public static final String getAttributeIdForExtensionId(String extensionId)
	{
		return EXT_ATTR_KEY_BASE + extensionId;
	}
	
	/**
	 * Returns the extension-id for the given attribute id. If the attribute id
	 * is not a profile extension then this method returns <code>null</code>.
	 * 
	 * @param attributeId
	 * @return
	 */
	public static final String getExtensionIdForAttributeId(String attributeId)
	{
		if (isAttributeIdForProfileExtension(attributeId))
		{
			return attributeId.substring(EXT_ATTR_KEY_BASE.length());
		}
		
		// else
		return null;
	}
	
	/**
	 * Utility method to indicate if attribute-id is for a profile extension.
	 * 
	 * @param attributeId
	 * @return
	 */
	public static final boolean isAttributeIdForProfileExtension(String attributeId)
	{
		if (attributeId != null && attributeId.startsWith(EXT_ATTR_KEY_BASE))
		{
			return true;
		}
		// else
		return false;
	}

	/**
	 * @return Returns the profile type for the employee as a string.
	 */
	public String getProfileType()
	{
		String string = (String)attributeMap.get("profileType");
		if (StringUtils.isEmpty(string)){
			return ProfileTypeConstants.DEFAULT;
		}
		return string;
	}

	/**
	 * @param type The profile type to set
	 */
	public void setProfileType(String type)
	{
		attributeMap.put("profileType", type);
	}

    /** 
     *  Overwrite the base method to provide convenient access from JSPs
	 *
	 */
     public Object get(Object key)
     {
 		boolean isTrace = LOG.isTraceEnabled();
       	if (key != null && key instanceof String) {
       		String skey = (String) key;
       		if ("userid".equals(key)) {
       			return getUserid();
    		}
       		else if ("userState".equals(key)){
       			return getState().getName();
       		}
       		else if ("userMode".equals(key)){
       			return getMode().getName();
       		}
       		else if (PeoplePagesServiceConstants.MCODE.equals(key)){
       			return getMcode();
       		}
    		else if (skey.startsWith("workLocation") && getWorkLocation() != null) {
	    	    if ( "workLocation.city".equals( key )   ) {
	    	    	return getWorkLocation().getCity();
	    	    }
	    	    else if ( "workLocation.state".equals( key )) {
	    	    	return getWorkLocation().getState();
	    	    }
	    	    else if ( "workLocation.address1".equals( key ) ) {
	    	    	return getWorkLocation().getAddress1();
	    	    }
	    	    else if ( "workLocation.address2".equals( key )) {
	    	    	return getWorkLocation().getAddress2();
	    	    }
	    	    else if ( "workLocation.postalCode".equals( key )) {
	    	    	return getWorkLocation().getPostalCode();
	    	    }
        	}
    		else if ("active".equals(skey)) {
    			return isActive();
    		}
       	}
       	Object retVal =	attributeMap.get(key);
       	if (isTrace) {
       	    if ((null != retVal)) {
       	        LOG.trace("Employee.get(" + key.toString() + ") does "
       				+ (attributeMap.containsKey(key) ? "" : "NOT ") + " contain key, returning : " + retVal.toString() );
       	    }
       	    else {
       	        // looking for an attribute that has no value for this profile - harmless
       	        LOG.trace("Employee.get(" + key + ") got retVal: " + retVal);
       	    }
       	}
       	return retVal;
    }	

	public String getSourceUrl() {
		String sourceUrl = (String) get("sourceUrl");
		if (sourceUrl == null) return "";
		return sourceUrl;
	}
	
	public void setSourceUrl(String sourceUrl) {
		attributeMap.put("sourceUrl", sourceUrl);
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * Utility method to get the lookup-key by type from a profile
	 * @param type
	 * @return
	 */
	public String getLookupKeyValue(Type type) 
	{
		if (type == null)
			return null;
		
		switch (type)
    	{
    		case KEY:
    			return getKey();
    		case UID:
    			return getUid();
    		case GUID:
    			return getGuid();
    		case USERID:
    			return getUserid();
    		case DN:
    			return getDistinguishedName();
    		case MCODE:
    			return getMcode();
    		default: // case EMAIL:
    			return getEmail();
    	}
	}
	
	public boolean matchesLookupKey(ProfileLookupKey plk)
	{
		if (plk == null || plk.getType() == null)
			return false;
		
		return getLookupKeyValue(plk.getType()).equalsIgnoreCase(plk.getValue());
	}

	/**
	 * @return the status
	 */
	public final EntryMessage getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public final void setStatus(EntryMessage status) {
		this.status = status;
	}
	
	/**
	 * Utility method to get the user information object for an employee record.
	 * Always returns a 'clone()' of the underlying object.
	 * 
	 * @return
	 */
	public final UserInfo getUserInfo() {
		if (userInfo == null) {
			userInfo = new UserInfo();
			userInfo.setDisplayName(getDisplayName());
			userInfo.setEmail(getEmail());
			userInfo.setUserId(getUserid());
			userInfo.put("key", getKey());
		}
		
		return (UserInfo) userInfo.clone();
	}
	
	private ProfilesSNAXPerson snaxPerson;
	public final ProfilesSNAXPerson getSNAXPerson() {
		if (snaxPerson == null) {
			snaxPerson = new ProfilesSNAXPerson() {

				private static final long serialVersionUID = -4889524035089417539L;

				public Date getLastUpdate() {
					return Employee.this.getLastUpdate();
				}
				
				public String getDisplayName() {
					return Employee.this.getDisplayName();
				}

				public String getEmail() {
					return Employee.this.getEmail();
				}

				public String getIdKey() {
					return Employee.this.getUserid();
				}

				public String getInternalId() {
					return Employee.this.getKey();
				}				
			};
		}
		
		return snaxPerson;
	}

	/**
	 * Utility method to get a list of 'keys' from a list of profiles
	 * 
	 * @param profiles
	 * @return
	 */
	public final static List<String> keysForList(List<Employee> profiles) {
		return plkForList(profiles, Type.KEY);
	}
	
	/**
	 * Concerts a list of employees into a Map<key,profile>
	 * @param profiles
	 * @return
	 */
	public final static Map<String,Employee> keyMapForList(List<Employee> profiles) {
		if (profiles == null)
			return new HashMap<String,Employee>(0);
		
		int lf = (int) Math.ceil(profiles.size() * 1.5);
		Map<String,Employee> pfs = new HashMap<String,Employee>(lf);
		
		for (Employee p : profiles)
			pfs.put(p.getKey(), p);
		
		return pfs;
	}
	
	/**
	 * Utility method to get a list of ProfileLookupKey values from a list of users
	 * 
	 * @param profiles
	 * @param type
	 * @return
	 */
	public final static List<String> plkForList(List<Employee> profiles, Type type) {
		if (profiles == null)
			return new ArrayList<String>(0);
		
		List<String> keys = new ArrayList<String>(profiles.size());
		
		for (Employee profile : profiles)
			keys.add(profile.getLookupKeyValue(type));
		
		return keys;
	}

	/**
	 * Utility to safetly extract a key from an employee
	 * @param source
	 * @return
	 */
	public static String getKey(Employee e) {
		if (e != null)
			return e.getKey();
		return null;
	}

	// Implement the Map<K,V> interface in order to support Connection.xml. It places
	// a connection object attributes into a Employee object. No idea why that was
	// chosen, but the result is ibabtis needs the map interface. The real solution seems
	// to be to rework the Connection code.
	/**
	 * This exposes the HashMap implementation.
	 */
	public Object put(String key, Object val){
		Object rtn = null;
		if (PeoplePagesServiceConstants.TENANT_KEY.equals(key)){
			if (Tenant.DB_SINGLETENANT_KEY.equals(val)){
				rtn = attributeMap.put(PeoplePagesServiceConstants.TENANT_KEY, Tenant.SINGLETENANT_KEY);
			}
			else{
				rtn = attributeMap.put(PeoplePagesServiceConstants.TENANT_KEY,val);
			}
		}
		else if (PeoplePagesServiceConstants.HOME_TENANT_KEY.equals(key)){
			if (Tenant.DB_SINGLETENANT_KEY.equals(val)){
				rtn = attributeMap.put(PeoplePagesServiceConstants.HOME_TENANT_KEY, Tenant.SINGLETENANT_KEY);
			}
			else{
				rtn = attributeMap.put(PeoplePagesServiceConstants.HOME_TENANT_KEY,val);
			}
		}
		else if (PeoplePagesServiceConstants.EMAIL.equals(key)){
			rtn = getEmail();
			this.setEmail((String)val);
		}
		// expectation is only db load will call this
		else if (PeoplePagesServiceConstants.MCODE.equals(key)){
			rtn = attributeMap.put(PeoplePagesServiceConstants.MCODE,val);
		}
		else {
			rtn = attributeMap.put(key,val);
		}
		return rtn;
	}

	/**
	 * This exposes the HashMap implementation. It was used in APIHelper
	 * for reasons not yet unraveled. Adding it for backwards compatibility until
	 * this is figured out.
	 */
	public void remove(String key){
		attributeMap.remove(key);
	}

	/**
	 * 
	 */
	public Map<String, Object> getAttributes(){
		// should we return a clone to prevent changes? old code did not.
		return attributeMap;
	}

	/**
	 * Used by PeoplePagesService when Employee extended HashMap. Not sure yet how
	 * to back this out.
	 * @param map
	 */
	public void putAll(Map<? extends String, ? extends Object> map){
		attributeMap.putAll(map);
		
	}

	/**
	 */
	public void clear(){
		// do nothing with attributeMap
	}

	/**
	 */
	public boolean containsKey(Object key){
		return attributeMap.containsKey(key);
	}

	public boolean containsValue(Object value){
		return attributeMap.containsValue(value);
	}

	/**
	 */
	public Set<Map.Entry<String,Object>> entrySet(){
		return attributeMap.entrySet();
	}

	/**
	 */
	public boolean equals(Object o){
		return attributeMap.equals(o);
	}

	public int hashCode(){
		return attributeMap.hashCode();
	}

	/**
	 */
	public Set<String> keySet(){
		return attributeMap.keySet();
	}

	/**
	 */
	public Collection<Object> values(){
		return attributeMap.values();
	}

	public boolean isEmpty(){
		return attributeMap.isEmpty();
	}
	
	public int size(){
		return attributeMap.size();
	}

	/**
	 */
	public Object remove(Object key){
		return attributeMap.remove(key);
	}
}
