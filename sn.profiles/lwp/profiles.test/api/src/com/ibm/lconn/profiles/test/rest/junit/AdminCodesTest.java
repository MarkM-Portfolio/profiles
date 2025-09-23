/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import junit.framework.Assert;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import com.ibm.lconn.profiles.data.codes.AbstractCode.CodeField;
import com.ibm.lconn.profiles.test.rest.model.CodesEntry;
import com.ibm.lconn.profiles.test.rest.model.CodesFeed;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

public class AdminCodesTest extends AbstractTest {

	public void testGetAdminCountryFeed() throws Exception {
		codesFeed(ApiConstants.AdminConstants.COUNTRY_CODE);
	}

	public void testGetAdminDepartmentFeed() throws Exception {
		codesFeed(ApiConstants.AdminConstants.DEPARTMENT_CODE);
	}

	public void testGetAdminEmpTypeFeed() throws Exception {
		codesFeed(ApiConstants.AdminConstants.EMPTYPE_CODE);
	}

	public void testGetAdminOrganizationFeed() throws Exception {
		codesFeed(ApiConstants.AdminConstants.ORGANIZATION_CODE);
	}

	public void testGetAdminWorklocFeed() throws Exception {
		codesFeed(ApiConstants.AdminConstants.WORKLOC_CODE);
	}

	private CodesFeed codesFeed(String codeType) throws Exception {
		String url = urlBuilder.getProfilesAdminCodesUrl(codeType, null);
		CodesFeed codesFeed = new CodesFeed(adminTransport.doAtomGet(Feed.class, url, NO_HEADERS, HTTPResponseValidator.OK));

		// Assert.assertTrue("There must be at least one entry in this feed: " + url, 0 < codesFeed.getEntries().size());

		for ( CodesEntry ce: codesFeed.getEntries() ) {
			System.out.println("  ce: " +ce.getCodeId() +", fields are: " +ce.getCodesFields());		
		}

		// verify that non-admins cannot access the admin endpoint
		validateAccessSecured(codeType);
		
//		CodeEntry codeEntry = 
		return codesFeed;
	}

	private CodesEntry getCodesEntry(String codeType, String codeId) throws Exception {

		String url = urlBuilder.getProfilesAdminCodesUrl(codeType, codeId);
//		StringBuilder builder = new StringBuilder(url);

		CodesEntry ce = new CodesEntry(adminTransport.doAtomGet(Entry.class, url, NO_HEADERS, HTTPResponseValidator.OK));
		ce.validate();

		return ce;
	}

	private void validateAccessSecured(String codeType) throws Exception {

		String url = urlBuilder.getProfilesAdminCodesUrl(codeType, null);

		mainTransport.doAtomGet(Feed.class, url, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);
	}

	public void testAdminDepartmentCRUD() throws Exception {
		_testAdminCodesCRUD(ApiConstants.AdminConstants.DEPARTMENT_CODE);
	}

	public void testAdminOrganizationCRUD() throws Exception {
		_testAdminCodesCRUD(ApiConstants.AdminConstants.ORGANIZATION_CODE);
	}

	public void testAdminEmployeeTypeCRUD() throws Exception {
		_testAdminCodesCRUD(ApiConstants.AdminConstants.EMPTYPE_CODE);
	}

	public void testAdminCountryCRUD() throws Exception {
		_testAdminCodesCRUD(ApiConstants.AdminConstants.COUNTRY_CODE);
	}

	public void testAdminWorkLocationCRUD() throws Exception {
		_testAdminCodesCRUD(ApiConstants.AdminConstants.WORKLOC_CODE);
	}

	private void _testAdminCodesCRUD(String codeType) throws Exception {
		System.out.println("\ntestAdminCodesCRUD( " + codeType + " )");

		// create a code item so that the feed is assured of having one
		createTestCode(codeType);		

		CodesFeed cf = 	codesFeed(codeType);
		for ( CodesEntry ce: cf.getEntries() ) {
			System.out.println("  ce: " +ce.getCodeId() +", fields are: " +ce.getCodesFields());		
		}

		CodesEntry ce = null;
		List<CodesEntry> feedEntries = cf.getEntries();
		if (ApiConstants.AdminConstants.COUNTRY_CODE.equalsIgnoreCase(codeType)) {
			ce = getTestCountryFromFeed(feedEntries);
		}
		else
			ce = feedEntries.get(0);

		String codesEntryUrl = ce.getLinkHref(ApiConstants.Atom.REL_EDIT);

		// delete the old code
		adminTransport.doAtomDelete(codesEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

		// verify code can no longer be retrieved from the server
		Entry serverResponseBody = adminTransport.doAtomGet(null, codesEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
		Assert.assertNull("Expected a null document, representing an empty search result", serverResponseBody);

		// post a new version
		System.out.println();
		System.out.println("The payload for the POST");
		Entry payload = ce.toEntryXml();
		prettyPrint(payload);
		adminTransport.doAtomPost(null, codesEntryUrl, payload, NO_HEADERS, HTTPResponseValidator.OK);

		// ... get the server version again ...
		serverResponseBody = adminTransport.doAtomGet(Entry.class, codesEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

		WRITER.writeTo(serverResponseBody, System.out);
		ce = new CodesEntry(serverResponseBody);
		ce.validate();

		// update the code with a revised description field
		String descriptionFieldName = getCodeDescriptionFieldName(codeType);
		String newDescription       = "UPDATED_"+ ce.getCodesFields().get(descriptionFieldName);
		updateDescriptionField(ce, newDescription, descriptionFieldName);

		// put the updated version
		System.out.println();
		System.out.println("The payload for the PUT");
		payload = ce.toEntryXml();
		prettyPrint(payload);
		adminTransport.doAtomPut(null, codesEntryUrl, payload, NO_HEADERS, HTTPResponseValidator.OK);

		// ... get the server version again and verify that the description is changed...
		serverResponseBody = adminTransport.doAtomGet(Entry.class, codesEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
		CodesEntry ceUpdated = new CodesEntry(serverResponseBody);
		ceUpdated.validate();
		String serverDescription = (String) ceUpdated.getCodesFields().get(descriptionFieldName);
		Assert.assertEquals("Expected code title equal", newDescription, serverDescription);

		// delete the new code
		adminTransport.doAtomDelete(codesEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
	}

	private void updateDescriptionField(CodesEntry ce, String description, String descriptionFieldName)
	{
		int i = 1;
		Map<String, Object> codesFields = ce.getCodesFields();
		Set<String> fieldKeys = codesFields.keySet();
		Iterator<?> it = fieldKeys.iterator();
		boolean found  = false;
		while (! found && it.hasNext())
		{
			String fieldName = (String) it.next();
//			if (i == 1)
//				System.out.println();
//			System.out.print("[" + i + "] " + fieldName);
			found = (descriptionFieldName.equals(fieldName));
			if (found) {
//				System.out.print(" -- found it");
				codesFields.put(descriptionFieldName, description);
			}
//			System.out.println();
			i++;
		}
	}

	private CodesEntry getTestCountryFromFeed(List<CodesEntry> feedEntries)
	{
		CodesEntry retVal = null;

		Iterator<?> itEntries = feedEntries.iterator();
		boolean found  = false;
		int i = 1;
		while (! found && itEntries.hasNext())
		{
			if (i == 1) System.out.println();
			CodesEntry ce = (CodesEntry) itEntries.next();
			Map<String, Object> codesFields = ce.getCodesFields();

//			boolean isFirst = true;
		    Iterator<?> itFields = codesFields.entrySet().iterator();
			while (! found && itFields.hasNext())
			{
		        Map.Entry pairs = (Map.Entry)itFields.next();
		        String    key = (String) pairs.getKey();
		        String    val = (String) pairs.getValue();
//		        if (isFirst) {
//		        	isFirst = false;
//		        	System.out.print("[" + i + "]");
//		        }
//		        System.out.print(" " + key + " = " + val + " : " );
				found = ("TST".equals(val));
				if (found) {
//					System.out.print(" -- found it");
					retVal = ce;
				}
		    }
//			System.out.println();
			i++;
		}
		return retVal;
	}

	public void testCreateWorkLocation() throws Exception
	{
		String testCodeId = "TEST_WORK_LOC1";
		String codeField = ApiConstants.AdminConstants.WORKLOC_CODE;

		CodesEntry ce1 = createWorkLocation(testCodeId);		
		System.out.println( ce1.getCodesFields());
		
		Assert.assertEquals(ce1.getCodeId(), testCodeId);

		// delete the code
		deleteCode( testCodeId, codeField);
	}

	public void testCreateDepartment() throws Exception
	{
		String testCodeId = "TEST_DEPARTMENT_1";
		String codeType  = ApiConstants.AdminConstants.DEPARTMENT_CODE_TYPE;
		String codeFeed  = ApiConstants.AdminConstants.DEPARTMENT_CODE;

		CodesEntry ce1 = createDepartment(testCodeId);		

		String codesEntryUrl = ce1.getLinkHref(ApiConstants.Atom.REL_EDIT);

		System.out.println("Get Codes from : " + codesEntryUrl);
		// ... get the server version again ...
		Entry serverResponseBody = adminTransport.doAtomGet(Entry.class, codesEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
		WRITER.writeTo(serverResponseBody, System.out);
		System.out.println(); // writer ^^ does not put an EOL

		CodesEntry ce2 = new CodesEntry(serverResponseBody);
		ce2.validate();

		CodesEntry ce3 = getCodesEntry(codeFeed, testCodeId);
		System.out.println( ce3.getCodesFields());

		Assert.assertEquals(ce3.getCodeId(), testCodeId);

		// update the Department title
		Map<String,Object> ce = new HashMap<String,Object>();

		ce = new HashMap<String,Object>();
		ce.put("departmentCode",  testCodeId);
		ce.put("departmentTitle", "UPDATED Test Department 1");
		CodesEntry ce4 = updateCode(testCodeId, codeType, codeFeed, ce);

		// delete the code
		deleteCode( testCodeId, codeFeed );
	}

	private CodesEntry createTestCode(String codeType) throws Exception {
		CodesEntry ce = null;
		if (null != codeType)
		{
			String testCodeId = "TEST_" + codeType;
			if (ApiConstants.AdminConstants.DEPARTMENT_CODE.equalsIgnoreCase(codeType)) {
				ce = createDepartment(testCodeId);
			}
			else if (ApiConstants.AdminConstants.ORGANIZATION_CODE.equalsIgnoreCase(codeType)) {
				ce = createOrganization(testCodeId);
			}
			else if (ApiConstants.AdminConstants.EMPTYPE_CODE.equalsIgnoreCase(codeType)) {
				ce = createEmployeeType(testCodeId);
			}
			else if (ApiConstants.AdminConstants.COUNTRY_CODE.equalsIgnoreCase(codeType)) {
				testCodeId = "TST"; // country code needs to be max. 3 characters
				ce = createCountry(testCodeId);
			}
			else if (ApiConstants.AdminConstants.WORKLOC_CODE.equalsIgnoreCase(codeType)) {
				ce = createWorkLocation(testCodeId);
			}
		}
		return ce;
	}

	private CodesEntry createDepartment(String testCodeId) throws Exception
	{
		String codeType  = ApiConstants.AdminConstants.DEPARTMENT_CODE_TYPE;
		String codeFeed  = ApiConstants.AdminConstants.DEPARTMENT_CODE;

		Map<String,Object> ce = new HashMap<String,Object>();

		ce.put("departmentCode",  testCodeId);
		ce.put("departmentTitle", "Test Department 1");

		return createCodeEntry(testCodeId, codeType, codeFeed, ce);
	}

	private CodesEntry createOrganization(String testCodeId) throws Exception
	{
		String codeType  = ApiConstants.AdminConstants.ORGANIZATION_CODE_TYPE;
		String codeFeed  = ApiConstants.AdminConstants.ORGANIZATION_CODE;

		Map<String,Object> ce = new HashMap<String,Object>();

		ce.put("orgCode",  testCodeId);
		ce.put("orgTitle", "Test Organization 1");

		return createCodeEntry(testCodeId, codeType, codeFeed, ce);
	}

	private CodesEntry createEmployeeType(String testCodeId) throws Exception
	{
		String codeType  = ApiConstants.AdminConstants.EMPTYPE_CODE_TYPE;
		String codeFeed  = ApiConstants.AdminConstants.EMPTYPE_CODE;

		Map<String,Object> ce = new HashMap<String,Object>();

		ce.put("employeeType",  testCodeId);
		ce.put("employeeDescription", "Test employee 1");

		return createCodeEntry(testCodeId, codeType, codeFeed, ce);
	}

	private CodesEntry createCountry(String testCodeId) throws Exception
	{
		String codeType  = ApiConstants.AdminConstants.COUNTRY_CODE_TYPE;
		String codeFeed  = ApiConstants.AdminConstants.COUNTRY_CODE;

		Map<String,Object> ce = new HashMap<String,Object>();

		ce.put("countryCode",  testCodeId);
		ce.put("displayValue", testCodeId + " " + "Test Country 1");

		return createCodeEntry(testCodeId, codeType, codeFeed, ce);
	}

	private CodesEntry createWorkLocation(String testCodeId) throws Exception
	{
		String codeType  = ApiConstants.AdminConstants.WORKLOC_CODE_TYPE;
		String codeField = ApiConstants.AdminConstants.WORKLOC_CODE;

		Map<String,Object> ce = new HashMap<String,Object>();
		
		ce.put("workLocationCode", testCodeId);
		ce.put("address1", "address 1");
		ce.put("address2", "address 2");
		ce.put("state", "MA");
		ce.put("city", "boston");
		ce.put("postalCode", "12345");
		
		return createCodeEntry(testCodeId, codeType, codeField, ce);
	}

	private CodesEntry createCodeEntry(String testCodeId, String codeType, String codeFeed, Map<String,Object> ce) throws Exception
	{
		// delete the code to make sure it doesn't already exist
		deleteCode( testCodeId, codeFeed );

		CodesEntry ce1 = createCode(testCodeId, codeType, codeFeed, ce);
		return ce1;
	}

	private String getCodeDescriptionFieldName(String codeType) throws Exception {
		String retVal = null;
		if (null != codeType)
		{
			if (ApiConstants.AdminConstants.DEPARTMENT_CODE.equalsIgnoreCase(codeType)) {
				retVal = "departmentTitle";
			}
			else if (ApiConstants.AdminConstants.ORGANIZATION_CODE.equalsIgnoreCase(codeType)) {
				retVal = "orgTitle";
			}
			else if (ApiConstants.AdminConstants.EMPTYPE_CODE.equalsIgnoreCase(codeType)) {
				retVal = "employeeDescription";
			}
			else if (ApiConstants.AdminConstants.COUNTRY_CODE.equalsIgnoreCase(codeType)) {
				retVal = "displayValue";
			}
			else if (ApiConstants.AdminConstants.WORKLOC_CODE.equalsIgnoreCase(codeType)) {
				retVal = "state";
			}
		}
		return retVal;
	}

}
