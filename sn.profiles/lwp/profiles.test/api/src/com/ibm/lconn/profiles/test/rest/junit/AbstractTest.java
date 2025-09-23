/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Base;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.writer.Writer;
import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.core.web.secutil.Sha256Encoder;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.test.rest.junit.AdminProfileEntryTest.ADMIN_PROFILE_ENTRY_QUERY_PARAMS;
import com.ibm.lconn.profiles.test.rest.model.CodesEntry;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.SeedlistFeed;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.Convert;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Pair;
import com.ibm.lconn.profiles.test.rest.util.TestProperties;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

/**
 * A common abstract test case that all Profiles API test extend.
 * 
 */
public abstract class AbstractTest extends TestCase
{
	public static final Map<String, String> NO_HEADERS = new HashMap<String, String>(0);

	public static final Map<String, String> CACHE_CONTROL_PUBLIC = new HashMap<String, String>(1);

	public static final Map<String, String> CACHE_CONTROL_PRIVATE = new HashMap<String, String>(1);

	public static final Map<String, String> CONTENT_TYPE_SERVICE = new HashMap<String, String>(1);
	static {
		CACHE_CONTROL_PRIVATE.put("Cache-Control", "private,must-revalidate,max-age=0");
		CACHE_CONTROL_PUBLIC.put("Cache-Control", "public,must-revalidate,max-age=0");
		// CONTENT_TYPE_SERVICE.put("Content-Type", ApiConstants.Atom.MEDIA_TYPE_ATOM_SERVICE_DOCUMENT);
	}

	protected static URLBuilder urlBuilder;

	private static boolean isOnCloud = false;

	public static boolean isOnCloud() {
		return isOnCloud;
	}
	public static boolean isOnPremise() {
		return (isOnCloud == false);
	}

	protected final int sleepTime = 100;

	protected Transport adminTransport;

	protected Transport adminNoProfileTransport = null;

	protected Transport mainTransport;

	protected Transport otherTransport;

	protected Transport tertiaryTransport;

	protected Transport anonymousTransport;

	protected Transport searchTransport;

	protected static Abdera abdera = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		if (abdera == null) {
			abdera = new Abdera();
		}

		urlBuilder = new URLBuilder();
		// TODO in future we can pull a class from test properties

		isOnCloud = TestProperties.getInstance().isOnCloud();

		adminTransport = new Transport();
		adminTransport.setup(urlBuilder.getServerURL(), TestProperties.getInstance().getAdminUserName(), TestProperties.getInstance()
				.getAdminPassword());

		if (null != TestProperties.getInstance().getAdminNoProfileUserName()
				&& !TestProperties.getInstance().getAdminNoProfileUserName().trim().equals("")) {
			adminNoProfileTransport = new Transport();
			adminNoProfileTransport.setup(urlBuilder.getServerURL(), TestProperties.getInstance().getAdminNoProfileUserName(),
					TestProperties.getInstance().getAdminNoProfilePassword());
		}

		mainTransport = new Transport();
		mainTransport.setup(urlBuilder.getServerURL(), TestProperties.getInstance().getUserName(), TestProperties.getInstance()
				.getPassword());

		otherTransport = new Transport();
		otherTransport.setup(urlBuilder.getServerURL(), TestProperties.getInstance().getOtherUserName(), TestProperties.getInstance()
				.getOtherPassword());
		
		tertiaryTransport = new Transport();
		tertiaryTransport.setup(urlBuilder.getServerURL(), TestProperties.getInstance().getTertiaryUserName(), TestProperties.getInstance()
				.getTertiaryPassword());

		anonymousTransport = new Transport();
		anonymousTransport.setup(urlBuilder.getServerURLHttp(), null, null);

		// unlike the rest of the product, search seedlist feeds are encoded in UTF-16, so we need to give that knowledge to the transport
		searchTransport = new Transport();
		searchTransport.setup(urlBuilder.getServerURL(), TestProperties.getInstance().getSearchUserName(), TestProperties.getInstance()
				.getSearchPassword(), "UTF-16");
		
		if (TestProperties.getInstance().isMultiTenantMode()){
		}
	}


	/*
	 * @see TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected void onCloudTestIsInvalid(String methodName)
	{
		onCloudTestIsInvalid(methodName, false);
	}
	protected void onCloudTestIsInvalid(String methodName, boolean createAttempt)
	{
		System.err.println(this.getClass().getSimpleName() + "." + methodName + " is not a valid test onCloud");
		if (createAttempt) {
			System.err.println("SEVERE: Attempt to create a profile on Cloud failed");
//			throw new RuntimeException("Attempt to create a profile on Cloud failed", new SecurityException());
		}
	}

	protected String setParamsFromEmail(Pair<String, String> params, ProfileEntry pe)
	{
		String identifier = null;
		String email = pe.getEmail();
		if (StringUtils.isEmpty(email)) {
			// if user has no email we're toast; use the user ID instead
			System.out.println("User " + pe.getName() + " has no email; switching to userID");
			params.setFirst(ADMIN_PROFILE_ENTRY_QUERY_PARAMS.userid.name());
			identifier = pe.getUserId();
		}
		else {
			identifier = email;
			if (isOnCloud()) {
				System.out.println("On Cloud, retrieval by email is invalid; switching to mcode for " + pe.getEmail());
				String mcode = Sha256Encoder.hashLowercaseStringUTF8(email, true);
				identifier = mcode;
			}
		}
		params.setSecond(identifier);
		return identifier;
	}

	protected static final Abdera ABDERA = new Abdera();

	public static Writer WRITER = ABDERA.getWriterFactory().getWriter("prettyxml");

	public static void prettyPrint(Base base) throws Exception {
//		if (null != base) {
//			WRITER.writeTo(base, System.out);
//			System.out.println();
//		}
//		else {
//			System.out.println("prettyPrint(): NULL");
//		}
		prettyPrint(base, System.out);
	}

	public static void prettyPrint(Base base, OutputStream out) throws Exception {
		if (null != base) {
			WRITER.writeTo(base, out);
			out.write('\n');
//			System.out.println();
		}
		else {
			System.out.println("prettyPrint(): NULL");
		}
	}
	public ProfileFeed getAdminProfileFeed(Pair<String, String>... params) throws Exception {
		return getAdminProfileFeed(null, false, params);
	}
	public ProfileFeed getAdminProfileFeed(boolean excludeExternal, Pair<String, String>... params) throws Exception {
		return getAdminProfileFeed(null, excludeExternal, params);
	}
	public ProfileFeed getAdminProfileFeed(ProfileService profilesSvc, boolean excludeExternal, Pair<String, String>... params) throws Exception {

		ProfileService profilesService = null;
		if (profilesSvc != null)
			profilesService = profilesSvc;
		else {			
			// get the admin profile service document if it was not pass in
			profilesService = ProfileService.parseFrom(adminTransport.doAtomGet(Service.class,
					urlBuilder.getProfilesAdminServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		}
		String url = profilesService.getProfileFeedUrl();
		StringBuilder builder = new StringBuilder(url);

		boolean first = true;
		for (Pair<String, String> p : params) {
			URLBuilder.addQueryParameter(builder, p.getFirst(), p.getSecond(), first);
			first = false;
		}

		if (excludeExternal)
			URLBuilder.addQueryParameter(builder, "excludeExternal", "true", first);

		url = builder.toString();
		ProfileFeed profileFeed = new ProfileFeed(adminTransport.doAtomGet(Feed.class, url, NO_HEADERS, HTTPResponseValidator.OK));
		profileFeed.validate();

		return profileFeed;
	}

	/**
	 * Creates a minimal Profile
	 * 
	 * @return
	 * @throws Exception
	 */
	public ProfileEntry createProfile() throws Exception {
		return createProfile((String)null);
	}
	
	/**
	 * Creates a minimal 'internal' Profile
	 * @throws Exception
	 */
	public ProfileEntry createProfile(String profileType) throws Exception {
		return createProfile(profileType, false);
	}

	/**
	 * Creates a minimal 'internal' / 'external' Profile; depending on param
	 * @throws Exception
	 */
	public ProfileEntry createProfile(boolean isExternal) throws Exception {
		return createProfile(null, isExternal);
	}
	
	/**
	 * Creates a minimal 'internal' / 'external' Profile; depending on param
	 * With a given name and surname.
	 * @throws Exception
	 */
	public ProfileEntry createProfile(boolean isExternal, String surname, String givenName) throws Exception {
		HTTPResponseValidator validator = HTTPResponseValidator.OK;
		if (isOnCloud)
			validator = HTTPResponseValidator.FORBIDDEN;
		return createProfile(adminTransport, urlBuilder, validator, surname, givenName, null, isExternal);
	}

	public ProfileEntry createProfile(String profileType, boolean isExternal) throws Exception {
		HTTPResponseValidator validator = HTTPResponseValidator.OK;
		if (isOnCloud)
			validator = HTTPResponseValidator.FORBIDDEN;
		return createProfile(adminTransport, urlBuilder, validator, profileType, isExternal);
	}

	public ProfileEntry createProfile(String profileType, boolean isExternal, HTTPResponseValidator validator) throws Exception {
		return createProfile(adminTransport, urlBuilder, validator, profileType, isExternal);
	}

	/**
	 * Create a minimal Profile with the specified profile type
	 * 
	 * @param profileType
	 *            null or "" if not specified
	 * @return
	 * @throws Exception
	 */
	public ProfileEntry createProfile(Transport transport, URLBuilder urlBuilder, HTTPResponseValidator validator,
									String profileType, boolean isExternal) throws Exception
	{
		UUID uuid = UUID.randomUUID();
		String  s = uuid.toString();

		String uid         = "UID_" + s;
		String dn          = "DN_" + s;
		String guid        = "GUID_" + s;

		String surname     = "SURNAME_" + s;
		String displayName = "DISPLAYNAME_" + s;
		String givenName   = "GIVENNAME_" + s;

		ProfileEntry pe = createProfileEntry(dn, guid, surname, uid, profileType, isExternal);

		pe.updateFieldValue(Field.SURNAME, surname);
		pe.updateFieldValue(Field.DISPLAY_NAME, displayName);
		pe.updateFieldValue(Field.GIVEN_NAME, givenName);

		// POST to Create the Profile on the server ...
		String xmlToPost = pe.toEntryXml().toString();
		System.out.println("AbstractTest.createProfile - posting : " + xmlToPost);
		transport.doAtomPost(null, urlBuilder.getProfilesAdminProfilesUrl(), pe.toEntryXml(), NO_HEADERS, validator);

		if (HTTPResponseValidator.OK.equals(validator)) {
			// ... get the Profile from the profileEntry endpoint ...
			StringBuilder builder = new StringBuilder(urlBuilder.getProfilesAdminProfileEntryUrl());
			URLBuilder.addQueryParameter(builder, Convert.toURLEncoded(Field.UID.getValue()), Convert.toURLEncoded(uid), true);
			String profileEntryUrl = builder.toString();
			Entry serverResponseBody = transport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			// WRITER.writeTo(serverResponseBody, System.out);
			pe = new ProfileEntry(serverResponseBody);
			pe.validate();
			return pe;
		}
		else {
			return null;
		}
	}

	/**
	 * Create a minimal Profile with the specified profile type
	 * 
	 * @param profileType
	 *            null or "" if not specified
	 * @return
	 * @throws Exception
	 */
	public ProfileEntry createProfile(Transport transport, URLBuilder urlBuilder, HTTPResponseValidator validator,
									String surname, String givenName, String profileType, boolean isExternal) throws Exception
	{
		UUID uuid = UUID.randomUUID();
		String  s = uuid.toString();

		String uid         = "UID_" + s;
		String dn          = "DN_" + s;
		String guid        = "GUID_" + s;

		String displayName = "DISPLAYNAME_" + s;

		ProfileEntry pe = createProfileEntry(dn, guid, surname, uid, profileType, isExternal);

		pe.updateFieldValue(Field.SURNAME, surname);
		pe.updateFieldValue(Field.DISPLAY_NAME, displayName);
		pe.updateFieldValue(Field.GIVEN_NAME, givenName);
		// POST to Create the Profile on the server ...
		Entry payload = pe.toEntryXml();
		prettyPrint(payload);
		transport.doAtomPost(null, urlBuilder.getProfilesAdminProfilesUrl(), payload, NO_HEADERS, validator);

		if (HTTPResponseValidator.OK.equals(validator)) {
			// ... get the Profile from the profileEntry end-point ...
			StringBuilder builder = new StringBuilder(urlBuilder.getProfilesAdminProfileEntryUrl());
			URLBuilder.addQueryParameter(builder, Convert.toURLEncoded(Field.UID.getValue()), Convert.toURLEncoded(uid), true);
			String profileEntryUrl = builder.toString();
			Entry serverResponseBody = transport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

			// WRITER.writeTo(serverResponseBody, System.out);
			pe = new ProfileEntry(serverResponseBody);
			pe.validate();
			return pe;
		}
		else {
			return null;
		}
	}

	/**
	 * Create a Profile with the specified profile entry
	 * 
	 * @param profileEntry
	 *            null or "" if not specified
	 * @return
	 * @throws Exception
	 */
	public ProfileEntry createProfile(ProfileEntry pe) throws Exception {

		// POST to Create the Profile on the server ...
		adminTransport.doAtomPost(null, urlBuilder.getProfilesAdminProfilesUrl(), pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

		// ... get the Profile from the profileEntry end-point ...
		StringBuilder builder = new StringBuilder(urlBuilder.getProfilesAdminProfileEntryUrl());
		URLBuilder.addQueryParameter(builder, Convert.toURLEncoded(Field.EMAIL.getValue()), Convert.toURLEncoded(pe.getEmail()), true);
		String profileEntryUrl = builder.toString();
		Entry serverResponseBody = adminTransport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

		// WRITER.writeTo(serverResponseBody, System.out);
		pe = new ProfileEntry(serverResponseBody);
		pe.validate();
		return pe;
	}

	/**
	 * Create a minimal Profile with the specified profile type
	 * 
	 * @param profileType
	 *            null or "" if not specified
	 * @return
	 * @throws Exception
	 */
	protected ProfileEntry createProfileWhitespaceNames(Transport transport, String profileType, boolean isExternal,
													String surname, String displayName, String givenName) throws Exception
	{
		UUID uuid = UUID.randomUUID();
		String s = uuid.toString();

		String uid         = "UID_" + s;
		String dn          = "DN_" + s;
		String guid        = "GUID_" + s;

		ProfileEntry pe = createProfileEntry(dn, guid, surname, uid, profileType, isExternal);

		pe.updateFieldValue(Field.SURNAME, surname);
		pe.updateFieldValue(Field.DISPLAY_NAME, displayName);
		pe.updateFieldValue(Field.GIVEN_NAME, givenName);

		// POST to Create the Profile on the server ...
		String xmlToPost = pe.toEntryXml().toString();
		System.out.println("AbstractTest.createProfile - posting : " + xmlToPost);
		transport.doAtomPost(null, urlBuilder.getProfilesAdminProfilesUrl(), pe.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

		// ... get the Profile from the profileEntry end-point ...
		StringBuilder builder = new StringBuilder(urlBuilder.getProfilesAdminProfileEntryUrl());
		URLBuilder.addQueryParameter(builder, Convert.toURLEncoded(Field.UID.getValue()), Convert.toURLEncoded(uid), true);
		String profileEntryUrl = builder.toString();
		Entry serverResponseBody = transport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

		// WRITER.writeTo(serverResponseBody, System.out);
		pe = new ProfileEntry(serverResponseBody);
		pe.validate();
		return pe;
	}

	/**
	 * Create a minimal Profile with the specified profile type and attempt to set displayName to "" (expect fail)
	 * 
	 * @param profileType
	 *            null or "" if not specified
	 * @return
	 * @throws Exception
	 */
	protected ProfileEntry createProfileEmptyDisplayName(Transport transport, String profileType, boolean isExternal,
													String surname, String displayName, String givenName) throws Exception
	{
		UUID uuid = UUID.randomUUID();
		String s = uuid.toString();

		String uid         = "UID_" + s;
		String dn          = "DN_" + s;
		String guid        = "GUID_" + s;

		ProfileEntry pe = createProfileEntry(dn, guid, surname, uid, profileType, isExternal);

		pe.updateFieldValue(Field.SURNAME, surname);
		pe.updateFieldValue(Field.DISPLAY_NAME, displayName);
		pe.updateFieldValue(Field.GIVEN_NAME, givenName);

		// POST to Create the Profile on the server ...
		String xmlToPost = pe.toEntryXml().toString();
		System.out.println("AbstractTest.createProfile - posting : " + xmlToPost);
		HTTPResponseValidator validator = HTTPResponseValidator.BAD_REQUEST;
		if (isOnCloud)
			validator = HTTPResponseValidator.FORBIDDEN;

		transport.doAtomPost(null, urlBuilder.getProfilesAdminProfilesUrl(), pe.toEntryXml(), NO_HEADERS, validator);

		// ... get the Profile from the profileEntry end-point ...
		StringBuilder builder = new StringBuilder(urlBuilder.getProfilesAdminProfileEntryUrl());
		URLBuilder.addQueryParameter(builder, Convert.toURLEncoded(Field.UID.getValue()), Convert.toURLEncoded(uid), true);
		String profileEntryUrl = builder.toString();
		try {
			// the GET should fail when parsing the empty response : <?xml version="1.0" encoding="UTF-8"?>
			Entry serverResponseBody = transport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
			// WRITER.writeTo(serverResponseBody, System.out);
			assertNull(serverResponseBody);
//			pe = new ProfileEntry(serverResponseBody)
//			pe.validate();
		}
		catch (ParseException pex) {
			System.out.println("Expected XML parse exception : " + pex.getMessage());
		}
		return pe;
	}

	/**
	 * Create a minimal Profile with the specified profile type and attempt to set surname to "" (expect fail)
	 * 
	 * @param profileType
	 *            null or "" if not specified
	 * @return
	 * @throws Exception
	 */
	protected ProfileEntry createProfileEmptySurname(Transport transport, String profileType, boolean isExternal,
													String surname, String displayName, String givenName) throws Exception
	{
		UUID uuid = UUID.randomUUID();
		String s = uuid.toString();

		String uid         = "UID_" + s;
		String dn          = "DN_" + s;
		String guid        = "GUID_" + s;

		ProfileEntry pe = createProfileEntry(dn, guid, surname, uid, profileType, isExternal);

		pe.updateFieldValue(Field.SURNAME, surname);
		pe.updateFieldValue(Field.DISPLAY_NAME, displayName);
		pe.updateFieldValue(Field.GIVEN_NAME, givenName);

		// POST to Create the Profile on the server ...
		String xmlToPost = pe.toEntryXml().toString();
		System.out.println("AbstractTest.createProfile - posting : " + xmlToPost);
		HTTPResponseValidator validator = HTTPResponseValidator.BAD_REQUEST;
		if (isOnCloud)
			validator = HTTPResponseValidator.FORBIDDEN;

		transport.doAtomPost(null, urlBuilder.getProfilesAdminProfilesUrl(), pe.toEntryXml(), NO_HEADERS, validator);

		// ... get the Profile from the profileEntry end-point ...
		StringBuilder builder = new StringBuilder(urlBuilder.getProfilesAdminProfileEntryUrl());
		URLBuilder.addQueryParameter(builder, Convert.toURLEncoded(Field.UID.getValue()), Convert.toURLEncoded(uid), true);
		String profileEntryUrl = builder.toString();
		try {
			// the GET should fail when parsing the empty response : <?xml version="1.0" encoding="UTF-8"?>
			Entry serverResponseBody = transport.doAtomGet(Entry.class, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
			// WRITER.writeTo(serverResponseBody, System.out);
			pe = new ProfileEntry(serverResponseBody);
			pe.validate();
		}
		catch (ParseException pex) {
			System.out.println("Expected XML parse exception : " + pex.getMessage());
		}
		return pe;
	}

	private ProfileEntry createProfileEntry(String dn, String guid, String surname, String uid, String profileType, boolean isExternal) {
		ProfileEntry pe = new ProfileEntry(dn, guid, surname, uid);
		if (profileType != null && profileType.length() > 0) {
			pe.updateFieldValue(Field.PROFILE_TYPE, profileType);
		}
		if (isExternal)
			pe.getProfileFields().put(Field.USER_MODE, UserMode.EXTERNAL);
		return pe;
	}

	public void printProfileEntryFields(ProfileEntry pe, Collection<Field> fields) throws Exception {

		for (Field f : fields) {
			System.out.println("###--->>> " + f.name() + " : " + (String) pe.getProfileFields().get(f));
		}
		// ###--->>> DISPLAY_NAME: Monifa Shani
		// ###--->>> MANAGER_UID : null
		// ###--->>> KEY : 7ba39b9c-862a-4514-82fe-b553c58a3c43
		// ###--->>> UID : mshani
		// ###--->>> GUID : 72c91eda-4444-4c21-bffe-a71e74356c48
		// ###--->>> getUserId() : 72c91eda-4444-4c21-bffe-a71e74356c48
	}
	
	/**
	 * Get the URL for the seedlist with a timestamp for the present
	 * @param searchTransport
	 * @return
	 * @throws Exception
	 */
	public static String getSeedlistForNow(Transport searchTransport) throws Exception {
		String seedlistNow = null;
		String url = urlBuilder.getProfilesSeedlist();
		while (true) {
			Feed searchFeed = searchTransport.doAtomGet(Feed.class, url, NO_HEADERS, HTTPResponseValidator.OK);
			SeedlistFeed seedlist = new SeedlistFeed(searchFeed);
			url = seedlist.getLinkHref(ApiConstants.Atom.REL_NEXT);
			if (url == null || url.equals("")) {
				String timestamp = seedlist.getTimestamp();
				assertNotNull(timestamp);
				seedlistNow = urlBuilder.getProfilesSeedlist(10, timestamp);
				break;
			}
		}
		assertNotNull(seedlistNow);
		return seedlistNow;
	}
	
	/**
	 *  A method to create a code
	 * @param codeId
	 * @param codeType
	 * @param codeField
	 * @param fields
	 * @return
	 * @throws Exception
	 */
	public CodesEntry createCode(String codeId, String codeType, String codeField, Map<String,Object> fields) throws Exception
	{	
		CodesEntry ce = new CodesEntry();
		ce.setCodeId( codeId );
		ce.setCodeType( codeType );

		// Iterate through the map and add them to the fields
		ce.getCodesFields().putAll( fields );

		String codesEntryUrl = urlBuilder.getProfilesAdminCodesUrl(codeField, null);

		System.out.println( ce.toEntryXml() );

		// post the new item for creation.
		adminTransport.doAtomPost(null, codesEntryUrl, ce.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);

		String newCodesEntryUrl = urlBuilder.getProfilesAdminCodesUrl(codeField, codeId);
		// get the server version and validate it against input values
		Entry serverResponseBody = adminTransport.doAtomGet(Entry.class, newCodesEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

		WRITER.writeTo(serverResponseBody, System.out);
		System.out.println(); // writer ^^ does not put an EOL

		CodesEntry ce1 = new CodesEntry(serverResponseBody);
		ce1.validate();

		System.out.println( ce1.getCodesFields());

		Assert.assertEquals(ce1.getCodeId(), codeId);

		return ce1;
	}
	
	/**
	 *  A method to delete a code
	 * @param codeId
	 * @param codeField
	 * @throws Exception
	 */
	public void deleteCode(String codeId, String codeField) throws Exception
	{	
		String codeEntryUrl = urlBuilder.getProfilesAdminCodesUrl(codeField, codeId);
		
		// delete the code
		adminTransport.doAtomDelete(codeEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
	}
	
	/**
	 *  A method to update a code
	 * @param codeId
	 * @param codeField
	 * @throws Exception
	 */
	public CodesEntry updateCode(String codeId, String codeType, String codeField, Map<String,Object> fields) throws Exception
	{	
		String codesEntryUrl = urlBuilder.getProfilesAdminCodesUrl(codeField, codeId);

		CodesEntry ce = new CodesEntry();
		ce.setCodeId( codeId );
		ce.setCodeType( codeType );

		// Iterate through the map and add them to the fields
		ce.getCodesFields().putAll( fields );

		System.out.println( ce.toEntryXml() );

		// update the code
		System.out.println();
		System.out.println("The payload for the PUT");
		Entry payload = ce.toEntryXml();
		prettyPrint(payload);
		adminTransport.doAtomPut(null, codesEntryUrl, payload, NO_HEADERS, HTTPResponseValidator.OK);

		// get the server version and validate it against input values
		Entry serverResponseBody = adminTransport.doAtomGet(Entry.class, codesEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);

		WRITER.writeTo(serverResponseBody, System.out);
		System.out.println(); // writer ^^ does not put an EOL

		CodesEntry ce1 = new CodesEntry(serverResponseBody);
		ce1.validate();
		
		System.out.println( ce1.getCodesFields());
		
		Assert.assertEquals(ce1.getCodeId(), codeId);

		return ce1;
	}
	
	/**
	 *  A method to delete a user by UID.
	 * @param uid
	 * @throws Exception
	 */
	public void deleteProfileByUid(String uid) throws Exception
	{
		deleteProfileByField(Field.UID, uid);
	}

	/**
	 *  A method to delete a user by GUID.
	 * @param uid
	 * @throws Exception
	 */
	public void deleteProfileByGuid(String guid) throws Exception
	{
		deleteProfileByField(Field.GUID, guid);
	}

	/**
	 *  A method to delete a user by specified field type.
	 * @param fieldValue
	 * @throws Exception
	 */
	private void deleteProfileByField(Field field, String fieldValue) throws Exception
	{
		// ... get the Profile from the profileEntry endpoint ...
		StringBuilder builder = new StringBuilder(urlBuilder.getProfilesAdminProfileEntryUrl());
		URLBuilder.addQueryParameter(builder, Convert.toURLEncoded(field.getValue()), Convert.toURLEncoded(fieldValue), true);
		String profileEntryUrl = builder.toString();
		
		adminTransport.doAtomDelete(null, profileEntryUrl, NO_HEADERS, HTTPResponseValidator.OK);
	}

}
