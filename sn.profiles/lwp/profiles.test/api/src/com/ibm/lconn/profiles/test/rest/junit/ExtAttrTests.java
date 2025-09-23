/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Service;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.ibm.lconn.profiles.test.rest.model.ExtensionField;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

public class ExtAttrTests extends AbstractTest {

	public void testExtAttrUpdateByUser() throws Exception {
		Service rawService = mainTransport.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), NO_HEADERS,
				HTTPResponseValidator.OK);

		ProfileService profilesService = ProfileService.parseFrom(rawService);

		// collection of custom extensions
		Set<String> customExtensionIDs = new HashSet<String>();

		// populate the collection, skip OOB profileLinks b/c ProfileEntry treats it as a known Field for now
		for (String s : profilesService.getExtensionIDs()) {
			if (!Field.PROFILE_LINKS.getValue().equals(s)) customExtensionIDs.add(s);
			System.out.println(s);
		}

		Map<String, String> extensionHrefs = profilesService.getExtensionHrefs();

		String timestamp = "_" + System.currentTimeMillis();

		for (String k : extensionHrefs.keySet()) {

			String s = extensionHrefs.get(k);
			// skip profileLinks (it's not a simpleAttribute)
			if (-1 != s.indexOf("extensionId=profileLinks")) continue;

			String url = s.substring(0, s.indexOf("&lastMod"));

			PutMethod p = new PutMethod();
			p.setURI(new URI(url));
			p.setRequestEntity(new StringRequestEntity(k + timestamp));

			mainTransport.doHttpPutMethod(Element.class, p, NO_HEADERS, HTTPResponseValidator.NO_CONTENT);
			// mainTransport.doAtomPut(null, url, result, NO_HEADERS, HTTPResponseValidator.OK);
			Entry rawResponse = mainTransport.doAtomGet(null, url, NO_HEADERS, null);
		}

		// verify update
		for (String k : extensionHrefs.keySet()) {

			String s = extensionHrefs.get(k);
			// skip profileLinks (it's not a simpleAttribute)
			if (-1 != s.indexOf("extensionId=profileLinks")) continue;

			GetMethod method = new GetMethod(s);
			String rawResponse = mainTransport.doHttpGetMethod(method, NO_HEADERS, HTTPResponseValidator.OK);

			assertEquals(k + timestamp, rawResponse);
		}

		// clear updates
		for (String k : extensionHrefs.keySet()) {

			String s = extensionHrefs.get(k);
			// skip profileLinks (it's not a simpleAttribute)
			if (-1 != s.indexOf("extensionId=profileLinks")) continue;

			String url = s.substring(0, s.indexOf("&lastMod"));

			mainTransport.doAtomDelete(url, NO_HEADERS, HTTPResponseValidator.OK);
		}

		// validate delete operations
		for (String k : extensionHrefs.keySet()) {

			String s = extensionHrefs.get(k);
			// skip profileLinks (it's not a simpleAttribute)
			if (-1 != s.indexOf("extensionId=profileLinks")) continue;

			GetMethod method = new GetMethod(s);
			String rawResponse = mainTransport.doHttpGetMethod(method, NO_HEADERS, HTTPResponseValidator.NO_CONTENT);
			System.out.println(rawResponse);
			assertNull(rawResponse);
		}

	}

	public void testExtAttrUpdateByAdmin() throws Exception {
		Service rawService = adminTransport.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), NO_HEADERS,
				HTTPResponseValidator.OK);
		// WRITER.writeTo(rawService, System.out);

		ProfileService profilesService = ProfileService.parseFrom(rawService);

		// collection of custom extensions
		Set<String> customExtensionIDs = new HashSet<String>();

		// populate the collection, skip OOB profileLinks b/c ProfileEntry treats it as a known Field for now
		for (String s : profilesService.getExtensionIDs()) {
			if (!Field.PROFILE_LINKS.getValue().equals(s)) customExtensionIDs.add(s);
			System.out.println(s);
		}

		ProfileEntry pe = createProfile();
		String profileEntryUrl = pe.getLinkHref(ApiConstants.Atom.REL_SELF);

		// execute once to add value, 2nd time to edit
		for (int i = 0; i < 2; i++) {
			String updatedSuffix = "_" + System.currentTimeMillis();
			pe.getProfileExtensionFields().clear();
			pe.getProfileFields().clear();

			for (String s : customExtensionIDs) {

				System.out.println("setting: " + s + " to: " + s + updatedSuffix);
				ExtensionField ef = new ExtensionField(s, s + updatedSuffix);
				pe.getProfileExtensionFields().add(ef);
			}

			// ... PUT to Update the Profile on the server ...
			adminTransport.doAtomPut(null, profileEntryUrl, pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

			// ... get the server version again ...
			Entry serverResponseBody = adminTransport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			// WRITER.writeTo(serverResponseBody, System.out);
			pe = new ProfileEntry(serverResponseBody);
			pe.validate();

			// ... verify the update(s) succeeded ...
			for (String s : customExtensionIDs) {

				ExtensionField ef = pe.getProfileExtensionField(s);
				assertNotNull(ef);
				String extensionFieldValue = (String) ef.getValue();
				assertNotNull(extensionFieldValue);
				assertEquals(s + " = ", s + updatedSuffix, extensionFieldValue);
			}
		}

		String extFieldValue = "";

		pe.getProfileExtensionFields().clear();
		pe.getProfileFields().clear();
		for (String s : customExtensionIDs) {

			System.out.println("setting: " + s + " to: \"" + extFieldValue + "\"");
			ExtensionField ef = new ExtensionField(s, extFieldValue);
			pe.getProfileExtensionFields().add(ef);
		}

		WRITER.writeTo(pe.toEntryXml(), System.out);
		// ... PUT to Update the Profile on the server ...
		adminTransport.doAtomPut(null, profileEntryUrl, pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

		// ... get the server version again ...
		Entry serverResponseBody = adminTransport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

		WRITER.writeTo(serverResponseBody, System.out);
		pe = new ProfileEntry(serverResponseBody);
		pe.validate();

		// ... verify the update(s) succeeded ...
		for (String s : customExtensionIDs) {

			ExtensionField ef = pe.getProfileExtensionField(s);
			assertNotNull(ef);
			String extensionFieldValue = (String) ef.getValue();
			assertNotNull(extensionFieldValue);
			assertEquals("field value expected to be \"" + extFieldValue + "\"", extFieldValue, extensionFieldValue);
		}

		// ... Delete the Profile ...
		adminTransport.doAtomDelete(profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

		// verify Profile can no longer be gotten from the server
		// this behavior is impl'd by com.ibm.lconn.profiles.api.actions.ProfilesAction.instantiateActionBean_postInit(BaseBean,
		// HttpServletRequest), see comments about SPR: #RPAS7JZHWG
		Entry rawProfileEntry = adminTransport.doAtomGet(null, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
		Assert.assertNull("Expected a null document, representing an empty search result", rawProfileEntry);
	}

}
