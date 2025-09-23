/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.TestProperties;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class MultiTenancyTest extends AbstractTest {

	URLBuilder[] urlBuilders;

	Transport[] globalAdminTransports;

	Transport[] globalAdminNoProfileTransports;

	Transport[] orgAdminTransports;

	static final int NUMBER_OF_ORGS = TestProperties.getInstance().getNumOrgs();

	private static final int TENANT_0 = 0;
	private static final int TENANT_1 = 1;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		urlBuilders = new URLBuilder[NUMBER_OF_ORGS];
		globalAdminTransports = new Transport[NUMBER_OF_ORGS];
		globalAdminNoProfileTransports = new Transport[NUMBER_OF_ORGS];
		orgAdminTransports = new Transport[NUMBER_OF_ORGS];

		List<String> urls = new ArrayList<String>();
		for (int i = 0; i < NUMBER_OF_ORGS; i++) {
			urlBuilders[i] = new URLBuilder(TestProperties.getInstance(i));
			urls.add(urlBuilders[i].getServerURL());
		}
		for (int i = 0; i < NUMBER_OF_ORGS; i++) {
			globalAdminTransports[i] = new Transport();
			globalAdminTransports[i].setup(urls, TestProperties.getInstance(i).getAdminUserName(), TestProperties.getInstance(i)
					.getAdminPassword(), "UTF-8");
			globalAdminNoProfileTransports[i] = new Transport();
			globalAdminNoProfileTransports[i].setup(urls, TestProperties.getInstance(i).getAdminNoProfileUserName(), TestProperties
					.getInstance(i).getAdminNoProfilePassword(), "UTF-8");
			orgAdminTransports[i] = new Transport();
			orgAdminTransports[i].setup(urls, TestProperties.getInstance(i).getOrgAdminUsername(), TestProperties.getInstance(i)
					.getOrgAdminPassword(), "UTF-8");
		}
	}

	public void printTestProperties() throws Exception {
		for (int i = 0; i < NUMBER_OF_ORGS; i++) {
			System.out.println(TestProperties.getInstance(i));
		}
	}

	public void testGlobalAdminCRUD_Internal() throws Exception {
		boolean isExternal = false;
		_testGlobalAdminCRUD(isExternal);
	}

	public void testGlobalAdminCRUD_External() throws Exception {
		boolean isExternal = true;
		_testGlobalAdminCRUD(isExternal);
	}

	// member of "admin" role should be able to perform CRUD operations on members of a tenant they do not belong to
	public void _testGlobalAdminCRUD(boolean isExternal) throws Exception {

		for (int i = 0; i < NUMBER_OF_ORGS; i++) {

			Transport adminNoProfileTransport = globalAdminNoProfileTransports[i];

			ProfileEntry pe = createProfile(adminNoProfileTransport, urlBuilders[i], HTTPResponseValidator.OK, null, isExternal);
			String profileEntryUrl = pe.getLinkHref(ApiConstants.Atom.REL_SELF);

			String updatedSurname = "SURNAME_" + System.currentTimeMillis();
			pe.getProfileFields().remove(Field.SURNAME);
			pe.getProfileFields().put(Field.SURNAME, updatedSurname);
			pe.getProfileFields().put(Field.PROFILE_LINKS, "www.something.com/alink");

			// ... PUT to Update the Profile on the server ...
			adminNoProfileTransport.doAtomPut(null, profileEntryUrl, pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// ... get the server version again ...
			Entry serverResponseBody = adminNoProfileTransport
					.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			// WRITER.writeTo(serverResponseBody, System.out);
			pe = new ProfileEntry(serverResponseBody);
			pe.validate();

			// ... verify the Update succeeded ...
			Assert.assertEquals(updatedSurname, (String) pe.getProfileFields().get(Field.SURNAME));
			Assert.assertEquals("www.something.com/alink", (String) pe.getProfileFields().get(Field.PROFILE_LINKS));

			// ... Delete the Profile ...
			adminNoProfileTransport.doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			// verify Profile can no longer be gotten from the server
			// this behavior is impl'd by com.ibm.lconn.profiles.api.actions.ProfilesAction.instantiateActionBean_postInit(BaseBean,
			// HttpServletRequest), see comments about SPR: #RPAS7JZHWG
			serverResponseBody = adminNoProfileTransport.doAtomGet(null, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
			Assert.assertNull("Expected a null document, representing an empty search result", serverResponseBody);

			// ... call DELETE again on the just-deleted profile ...
			adminNoProfileTransport.doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.NOT_FOUND);
		}
	}

	public void testOrgAdminServiceDoc() throws Exception {

		int i = 0; // test one tenant for now
		String serviceDocUrl = urlBuilders[i].getProfilesAdminServiceDocument();

		Service serverResponseBody = orgAdminTransports[i].doAtomGet(Service.class, serviceDocUrl, NO_HEADERS, HTTPResponseValidator.OK);
		// prettyPrint(serverResponseBody);
		ProfileService profilesService = ProfileService.parseFrom(serverResponseBody);
	}

	public void testOrgAdminCRUD_Internal() throws Exception {
		boolean isExternal = false;
		_testOrgAdminCRUD(isExternal);
	}

	public void testOrgAdminCRUD_External() throws Exception {
		boolean isExternal = true;
		_testOrgAdminCRUD(isExternal);
	}

	public void _testOrgAdminCRUD(boolean isExternal) throws Exception {

		int i = 0; // test one tenant for now
		for (; i < NUMBER_OF_ORGS; i++) {

			Transport adminNoProfileTransport = globalAdminNoProfileTransports[i];

			// verify that org-admin CANNOT create a profile
			createProfile(orgAdminTransports[i], urlBuilders[i], HTTPResponseValidator.FORBIDDEN, null, isExternal);

			ProfileEntry pe = createProfile(adminNoProfileTransport, urlBuilders[i], HTTPResponseValidator.OK, null, isExternal);
			String profileEntryUrl = pe.getLinkHref(ApiConstants.Atom.REL_SELF);

			// ... verify org-admin CAN get profile entry
			Entry serverResponseBody = orgAdminTransports[i].doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			// TODO: EDIT link not present
			// pe = new ProfileEntry(serverResponseBody);
			// pe.validate();

			String updatedSurname = "SURNAME_" + System.currentTimeMillis();
			pe.getProfileFields().remove(Field.SURNAME);
			pe.getProfileFields().put(Field.SURNAME, updatedSurname);
			pe.getProfileFields().put(Field.PROFILE_LINKS, "www.something.com/alink");

			// ... PUT to Update the Profile on the server ...
			orgAdminTransports[i].doAtomPut(null, profileEntryUrl, pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// ... get the server version again ...
			serverResponseBody = adminNoProfileTransport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			// WRITER.writeTo(serverResponseBody, System.out);
			pe = new ProfileEntry(serverResponseBody);
			pe.validate();

			// ... verify the Update succeeded ...
			Assert.assertEquals(updatedSurname, (String) pe.getProfileFields().get(Field.SURNAME));
			Assert.assertEquals("www.something.com/alink", (String) pe.getProfileFields().get(Field.PROFILE_LINKS));

			// ... verify org-admin CANNOT delete the profile ...
			orgAdminTransports[i].doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);

			// ... Delete the Profile ...
			adminNoProfileTransport.doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			// verify Profile can no longer be gotten from the server
			// this behavior is impl'd by com.ibm.lconn.profiles.api.actions.ProfilesAction.instantiateActionBean_postInit(BaseBean,
			// HttpServletRequest), see comments about SPR: #RPAS7JZHWG
			serverResponseBody = adminNoProfileTransport.doAtomGet(null, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
			Assert.assertNull("Expected a null document, representing an empty search result", serverResponseBody);

			// ... call DELETE again on the just-deleted profile ...
			adminNoProfileTransport.doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.NOT_FOUND);
		}

	}

	public void testOrgAdminCrossOrgOperations_Internal() throws Exception {
		boolean isExternal = false;
		_testOrgAdminCrossOrgOperations(isExternal);
	}

	public void testOrgAdminCrossOrgOperations_External() throws Exception {
		boolean isExternal = true;
		_testOrgAdminCrossOrgOperations(isExternal);
	}

	public void _testOrgAdminCrossOrgOperations(boolean isExternal) throws Exception {

		// verify that org-admin CANNOT create a profile
		createProfile(orgAdminTransports[TENANT_0], urlBuilders[TENANT_0], HTTPResponseValidator.FORBIDDEN, null, isExternal);

		ProfileEntry pe = createProfile(adminNoProfileTransport, urlBuilders[TENANT_0], HTTPResponseValidator.OK, null, isExternal);
		String profileEntryUrl = pe.getLinkHref(ApiConstants.Atom.REL_SELF);

		// ... verify org-admin from the same tenant CAN get profile entry
		Entry serverResponseBody = orgAdminTransports[TENANT_0].doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS,
				HTTPResponseValidator.OK);

		// ... verify org-admin from another tenant CANNOT get profile entry
		serverResponseBody = orgAdminTransports[TENANT_1].doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);
		
		String updatedSurname = "SURNAME_" + System.currentTimeMillis();
		pe.getProfileFields().remove(Field.SURNAME);
		pe.getProfileFields().put(Field.SURNAME, updatedSurname);
		pe.getProfileFields().put(Field.PROFILE_LINKS, "www.something.com/alink");

		// ... verify org-admin from a different tenant CANNOT update the Profile ...
		orgAdminTransports[TENANT_1].doAtomPut(null, profileEntryUrl, pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.FORBIDDEN);

		// ... verify org-admin from the same tenant CAN update the Profile ...
		orgAdminTransports[TENANT_0].doAtomPut(null, profileEntryUrl, pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

		// ... get the server version again ...
		serverResponseBody = adminNoProfileTransport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

		// WRITER.writeTo(serverResponseBody, System.out);
		pe = new ProfileEntry(serverResponseBody);
		pe.validate();

		// ... verify the Update succeeded ...
		Assert.assertEquals(updatedSurname, (String) pe.getProfileFields().get(Field.SURNAME));
		Assert.assertEquals("www.something.com/alink", (String) pe.getProfileFields().get(Field.PROFILE_LINKS));

		// ... verify org-admin CANNOT delete the profile ...
		orgAdminTransports[TENANT_0].doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);

		// ... verify org-admin from another tenant CANNOT delete the profile ...
		orgAdminTransports[TENANT_1].doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);

		// ... Delete the Profile ...
		adminNoProfileTransport.doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

		// verify Profile can no longer be gotten from the server
		// this behavior is impl'd by com.ibm.lconn.profiles.api.actions.ProfilesAction.instantiateActionBean_postInit(BaseBean,
		// HttpServletRequest), see comments about SPR: #RPAS7JZHWG
		serverResponseBody = adminNoProfileTransport.doAtomGet(null, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
		Assert.assertNull("Expected a null document, representing an empty search result", serverResponseBody);

	}
}
