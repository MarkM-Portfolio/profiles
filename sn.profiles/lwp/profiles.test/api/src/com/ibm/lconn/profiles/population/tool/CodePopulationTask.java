/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.population.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.abdera.model.Entry;

import com.ibm.lconn.profiles.test.rest.model.CodesEntry;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

/**
 * Populates codes on the profile (work-location, country, department, organization)
 */
public class CodePopulationTask extends Task {

	// country id, display value
	private static final String[] COUNTRY_CODES = {
		"us", "United States",
		"uk", "United Kingdom"
	};
	
	// department code, department title
	private static final String[] DEPARTMENT_CODES = {
		"sal", "Sales Department",
		"eng", "Engineering Department",
		"sup", "Support Department",
		"cor", "Corporate Department"
	};
	
	// employeeType, employeeDescription
	private static final String[] EMPLOYEE_TYPE_CODES = {
		"c", "Contractor Employee Type",
		"r", "Regular Employee Type"
	};

	// orgCode, orgTitle
	private static final String[] ORGANIZATION_CODES = {
		"o", "Home Organization",
		"p", "Travel Organization"
	};

	// workLocationCode, address1, address2, city, state, postalCode
	private static final String[] WORK_LOCATION_CODES = {
		"wh", "The White House", "1600 Pennsylvania Ave NW", "Washington", "DC", "20500",
		"do", "Her Majesty's Government", "10 Downing Street", "City of Westminster", "London", "SW1A 2AA"
	};
		
	private int countryIndex = 0;
	private int workLocationIndex = 0;
	private int employeeIndex = 0;
	private int organizationIndex = 0;
	
	private List<CodesEntry> countries;
	private List<CodesEntry> departments;
	private List<CodesEntry> employeeTypes;
	private List<CodesEntry> organizations;
	private List<CodesEntry> workLocations;
	
	private boolean hasPopulatedCodes;
	
	public CodePopulationTask() throws Exception {
		super();
		loadCodes();
		hasPopulatedCodes = false;
	}

	private void loadCodes() throws Exception {	
		loadCountries();
		loadDepartments();
		loadEmployeeTypes();
		loadOrganizations();
		loadWorkLocations();
	}
	
	private void loadWorkLocations() {
		workLocations = new ArrayList<CodesEntry>();
		for (int i=0; i < WORK_LOCATION_CODES.length; i=i+6) {
			String workLocationCode = WORK_LOCATION_CODES[i];
			String address1 = WORK_LOCATION_CODES[i+1];
			String address2 = WORK_LOCATION_CODES[i+2];
			String city = WORK_LOCATION_CODES[i+3];
			String state = WORK_LOCATION_CODES[i+4];
			String postalCode = WORK_LOCATION_CODES[i+5];			
			CodesEntry workLocation = new CodesEntry();
			workLocation.setCodeId(workLocationCode);
			workLocation.setCodeType("workLocation");
			workLocation.getCodesFields().put("workLocationCode", workLocationCode);
			workLocation.getCodesFields().put("address1", address1);
			workLocation.getCodesFields().put("address2", address2);
			workLocation.getCodesFields().put("city", city);
			workLocation.getCodesFields().put("state", state);
			workLocation.getCodesFields().put("postalCode", postalCode);
			workLocations.add(workLocation);
		}				
	}
	
	private void loadOrganizations() {
		organizations = new ArrayList<CodesEntry>();
		for (int i=0; i < ORGANIZATION_CODES.length; i=i+2) {
			String orgCode = ORGANIZATION_CODES[i];
			String orgTitle = ORGANIZATION_CODES[i+1];
			CodesEntry organization = new CodesEntry();
			organization.setCodeId(orgCode);
			organization.setCodeType("organization");
			organization.getCodesFields().put("orgCode", orgCode);
			organization.getCodesFields().put("orgTitle", orgTitle);
			organizations.add(organization);
		}		
	}
	
	private void loadEmployeeTypes() {
		employeeTypes = new ArrayList<CodesEntry>();
		for (int i=0; i < EMPLOYEE_TYPE_CODES.length; i=i+2) {
			String employeeTypeCode = EMPLOYEE_TYPE_CODES[i];
			String employeeTypeDescription = EMPLOYEE_TYPE_CODES[i+1];
			CodesEntry employeeType = new CodesEntry();
			employeeType.setCodeId(employeeTypeCode);
			employeeType.setCodeType("employeeType");
			employeeType.getCodesFields().put("employeeType", employeeTypeCode);
			employeeType.getCodesFields().put("employeeDescription", employeeTypeDescription);
			employeeTypes.add(employeeType);
		}
	}
	
	private void loadDepartments() {
		departments = new ArrayList<CodesEntry>();
		for (int i=0; i < DEPARTMENT_CODES.length; i=i+2) {
			String departmentCode = DEPARTMENT_CODES[i];
			String departmentTitle = DEPARTMENT_CODES[i+1];
			CodesEntry department = new CodesEntry();
			department.setCodeId(departmentCode);
			department.setCodeType("department");
			department.getCodesFields().put("deptCode", departmentCode);
			department.getCodesFields().put("departmentCode", departmentCode);
			department.getCodesFields().put("departmentTitle", departmentTitle);
			departments.add(department);
		}
	}
	
	private void loadCountries() {
		countries = new ArrayList<CodesEntry>();
		
		for (int i=0; i < COUNTRY_CODES.length; i=i+2) {
			String countryCode = COUNTRY_CODES[i];
			String countryDisplayValue = COUNTRY_CODES[i+1];
			CodesEntry country = new CodesEntry();
			country.setCodeId(countryCode);
			country.setCodeType("country");
			country.getCodesFields().put("countryCode", countryCode);
			country.getCodesFields().put("displayValue", countryDisplayValue);
			countries.add(country);
		}		
	}
	
	public void populateCodes() throws Exception {
		if (hasPopulatedCodes)
			return;
		
		// iterate over the countries, departments, etc.
		Map<String, List<CodesEntry>> codeTypeToCodes = new HashMap<String, List<CodesEntry>>();
		codeTypeToCodes.put(ApiConstants.AdminConstants.COUNTRY_CODE, countries);

		// TODO THERE IS A BUG IN API THAT MAKES THIS IMPOSSIBLE TO COMPLETE... input requires a deptCode to know code type, but it fails when creating
		//codeTypeToCodes.put(ApiConstants.AdminConstants.DEPARTMENT_CODE, departments);
		codeTypeToCodes.put(ApiConstants.AdminConstants.EMPTYPE_CODE, employeeTypes);
		codeTypeToCodes.put(ApiConstants.AdminConstants.ORGANIZATION_CODE, organizations);
		codeTypeToCodes.put(ApiConstants.AdminConstants.WORKLOC_CODE, workLocations);
		
		for (String codeType : codeTypeToCodes.keySet()) {
			List<CodesEntry> codeEntries = codeTypeToCodes.get(codeType);
			for (CodesEntry codeEntry : codeEntries) {
								
				String url = urlBuilder.getProfilesAdminCodesUrl(codeType, codeEntry.getCodeId());
				
				CodesEntry result = null;
				try {
					result = new CodesEntry(adminTransport.doAtomGet(Entry.class, url, NO_HEADERS, HTTPResponseValidator.OK));
				} catch (Exception e) {
					// ignore
				}
				
				if (result == null) {
					// create the code
					url = urlBuilder.getProfilesAdminCodesUrl(codeType, null);					
					adminTransport.doAtomPut(null, url, codeEntry.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);
				} else {
					url = urlBuilder.getProfilesAdminCodesUrl(codeType, codeEntry.getCodeId());
					//adminTransport.doAtomPut(null, url, codeEntry.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);
				}
			}
		}
						
		hasPopulatedCodes = true;
	}
	
	public int incrementIndex(int curValue, List<CodesEntry> listItems) {
		int newValue = curValue + 1;
		if (newValue >= listItems.size()) {
			newValue = 0;
		}		
		return newValue;
	}
	
	public int incrementIndex(int curValue, String[] listItems) {
		int newValue = curValue + 1;
		if (newValue >= listItems.length) {
			newValue = 0;
		}
		return newValue;
	}
	
	@Override
	public void doTask(ProfileEntry profileEntry) throws Exception {
		if (profileEntry == null)
			return;

		// verify codes are populated at least once
		populateCodes();
				
		// now update the profile entry		
		String uid = (String) profileEntry.getProfileFields().get(Field.UID);
		ProfileEntry serverVersion = getProfileByUid(uid);
		if (serverVersion != null) {
			
			// update actual codes
			String workLocationCode = workLocations.get(workLocationIndex).getCodeId();
			String organizationCode = organizations.get(organizationIndex).getCodeId();
			String employeeTypeCode = employeeTypes.get(employeeIndex).getCodeId();
			String countryCode = countries.get(countryIndex).getCodeId();			
			workLocationIndex = incrementIndex(workLocationIndex, workLocations);
			organizationIndex = incrementIndex(organizationIndex, organizations);
			employeeIndex = incrementIndex(employeeIndex, employeeTypes);
			countryIndex = incrementIndex(countryIndex, countries);
			serverVersion.getProfileFields().put(Field.WORK_LOCATION_CODE, workLocationCode);
			serverVersion.getProfileFields().put(Field.COUNTRY_CODE, countryCode);
			serverVersion.getProfileFields().put(Field.ORG_ID, organizationCode);
			serverVersion.getProfileFields().put(Field.EMPLOYEE_TYPE_CODE, employeeTypeCode);
			
			String url = serverVersion.getLinkHref(ApiConstants.Atom.REL_EDIT);
			adminTransport.doAtomPut(null, url, serverVersion.toEntryXml(),NO_HEADERS, HTTPResponseValidator.OK);
		}
	}
		
}
