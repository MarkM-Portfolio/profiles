/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.util.directory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.ibm.connections.directory.services.data.DSConstants;

public class MockProfileProviderConfig {

	public static String DEFAULT_LOCALE = "en_us";

	static private MockProfileProviderConfig INSTANCE = null;

	private static HashMap<String, MockDSOrganizationObject> organizations = new HashMap<String, MockDSOrganizationObject>();
	private static HashMap<String, HashMap<String,MockDSSubscriberObject>> orgSubscriberMap = new HashMap<String, HashMap<String,MockDSSubscriberObject>>();
	private static HashMap<String, MockDSSubscriberObject> subscribers = new HashMap<String, MockDSSubscriberObject>();
	private static HashMap<String, MockDSAccountObject> accounts = new HashMap<String, MockDSAccountObject>();

	private MockProfileProviderConfig() {
		try {
			InputStream configStream = MockProfileProviderConfig.class.getResourceAsStream("MockProfileProviderConfig.xml");
			MockProfileProviderConfigHandler handler = new MockProfileProviderConfigHandler();
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			SAXParser parser = parserFactory.newSAXParser();
			parser.parse(configStream, handler);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static MockProfileProviderConfig getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new MockProfileProviderConfig();
		}
		return INSTANCE;
	}

	class MockProfileProviderConfigHandler extends DefaultHandler {

		public final QName CONFIG = new QName("config");

		public final QName ORG = new QName("org");

		public final QName SUBSCRIBER = new QName("subscriber");
		
		public final QName ACCOUNT = new QName("account");

		public final QName NAME = new QName("name");

		public final QName ID = new QName("id");
		
		public final QName ACCTID = new QName("acctId");
		
		public final QName LOGIN = new QName("login");

		public final QName ORG_ID = new QName("orgId");

		public final QName EMAIL = new QName("email");

		public final QName GIVEN_NAME = new QName("givenName");

		public final QName SURNAME = new QName("surname");

		public final QName DISPLAY_NAME = new QName("displayName");

		private StringBuilder bodyText;

		private Map<QName, String> currentDSObject;

		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			QName currentElement = new QName(uri, localName, "");
			bodyText = new StringBuilder();

			if (isEqual(currentElement, CONFIG)) {
				currentDSObject = new HashMap<QName, String>();
			}
		}

		public void endElement(String uri, String localName, String name) throws SAXException {
			QName currentElement = new QName(uri, localName, "");

			if (isEqual(NAME, currentElement)) {
				currentDSObject.put(NAME, normalize(bodyText));
			}
			else if (isEqual(ID, currentElement)) {
				currentDSObject.put(ID, normalize(bodyText));
			}
			else if (isEqual(ACCTID, currentElement)) {
				currentDSObject.put(ACCTID, normalize(bodyText));
			}
			else if (isEqual(ORG_ID, currentElement)) {
				currentDSObject.put(ORG_ID, normalize(bodyText));
			}
			else if (isEqual(EMAIL, currentElement)) {
				currentDSObject.put(EMAIL, normalize(bodyText));
			}
			else if (isEqual(GIVEN_NAME, currentElement)) {
				currentDSObject.put(GIVEN_NAME, normalize(bodyText));
			}
			else if (isEqual(SURNAME, currentElement)) {
				currentDSObject.put(SURNAME, normalize(bodyText));
			}
			else if (isEqual(DISPLAY_NAME, currentElement)) {
				currentDSObject.put(DISPLAY_NAME, normalize(bodyText));
			}
			else if (isEqual(LOGIN, currentElement)){
				currentDSObject.put(LOGIN, normalize(bodyText));
			}
			else if (isEqual(SUBSCRIBER, currentElement)) {
				MockDSSubscriberObject s = createSubscriber(currentDSObject.get(ID),currentDSObject.get(ACCTID), currentDSObject.get(DISPLAY_NAME),
						currentDSObject.get(ORG_ID), currentDSObject.get(EMAIL), currentDSObject.get(SURNAME), DEFAULT_LOCALE);
				// we assume org already exists. if not, someone can fix MockProfileProfviderConfig.xml and declare all customers/orgs first
				HashMap<String,MockDSSubscriberObject> subs = orgSubscriberMap.get(currentDSObject.get(ORG_ID));
				subs.put(currentDSObject.get(ID),s);
				// add to list of subscribers. seems this will be obsolete when subscriber ids are not unique.
				subscribers.put(currentDSObject.get(ID), s);
				// System.out.println(s);
			}
			else if (isEqual(ACCOUNT, currentElement)){
				MockDSAccountObject a = createAccount(currentDSObject.get(ID), currentDSObject.get(LOGIN));
				accounts.put(currentDSObject.get(ID), a);
			}
			else if (isEqual(ORG, currentElement)) {
				MockDSOrganizationObject o = createOrganization(currentDSObject.get(ID), currentDSObject.get(DISPLAY_NAME),
						currentDSObject.get(NAME), currentDSObject.get(EMAIL));
				// create a new Map object to hold subsequent subscribers
				orgSubscriberMap.put(currentDSObject.get(ID), new HashMap<String,MockDSSubscriberObject>());
				// add to list of current orgs
				organizations.put(currentDSObject.get(ID), o);
				// System.out.println(o);
			}
			else if (isEqual(currentElement, CONFIG)) {
				// reset
				currentDSObject = null;
			}
			else {
				throw new SAXException("Unhandled element: " + localName);
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			bodyText.append(ch, start, length);
		}

		private boolean isEqual(QName q1, QName q2) {
			String ns1 = q1.getNamespaceURI();
			String ns2 = q2.getNamespaceURI();
			String lp1 = q1.getLocalPart();
			String lp2 = q2.getLocalPart();
			return ns1.equals(ns2) && lp1.equals(lp2);
		}

		private String normalize(StringBuilder sb) {
			return sb.toString().trim();
		}
	}

	HashMap<String, MockDSOrganizationObject> getOrganizations() {
		return organizations;
	}

	HashMap<String, MockDSSubscriberObject> getSubscribers() {
		return subscribers;
	}
	
	HashMap<String, MockDSAccountObject> getAccounts() {
		return accounts;
	}
	
	public MockDSOrganizationObject getOrganization(String directoryId){
		return organizations.get(directoryId);
	}
	
	public MockDSSubscriberObject getSubscriber(String orgId, String subscriberId){
		MockDSSubscriberObject rtn = null;
		HashMap<String,MockDSSubscriberObject> subs = orgSubscriberMap.get(orgId);
		if (subs != null){
			rtn = subs.get(subscriberId);
		}
		return rtn;
	}
	
	MockDSAccountObject getAccount(String directoryId) {
		return accounts.get(directoryId);
	}

	private MockDSSubscriberObject createSubscriber(String id, String accountId, String displayName, String orgId, String email, String surname, String locale) {
		MockDSSubscriberObject subscriber = new MockDSSubscriberObject(id, displayName, orgId);
		subscriber.set_dn("cn=sample, o=" + id);
		subscriber.set_name(displayName);
		subscriber.set_email(email);
		subscriber.set_guid(id);
		subscriber.set_id(id);
		subscriber.set_inactive(false);
		List<String> logins = new ArrayList<String>(1);
		logins.add(email);
		subscriber.set_login(logins);
		subscriber.set_locale(locale);
		subscriber.setExtValue(MockDSSubscriberObject.SURNAME, surname);
		subscriber.setExtValue(DSConstants.ATTRIBUTE_TYPE_IBM_SAAS_USER_ACCOUNT_ID, accountId);

		return subscriber;
	}
	
	private MockDSAccountObject createAccount(String id, String login){
		MockDSAccountObject account = new MockDSAccountObject(id,login);
		return account;
	}

	private MockDSOrganizationObject createOrganization(String id, String displayName, String orgName, String email) {
		MockDSOrganizationObject organization = new MockDSOrganizationObject(id, displayName);
		organization.set_dn("cn=sample, o=" + id);
		organization.set_email(email);
		organization.set_name(displayName);
		organization.set_guid(id);
		organization.set_id(id);
		organization.set_inactive(false);
		List<String> logins = new ArrayList<String>(2);
		logins.add(email);
		organization.set_login(logins);
		organization.set_name(orgName);

		return organization;
	}
}
