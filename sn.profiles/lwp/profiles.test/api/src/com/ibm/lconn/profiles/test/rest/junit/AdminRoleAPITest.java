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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.xml.namespace.QName;
import junit.framework.Assert;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Base;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.test.rest.model.RoleEntry;
import com.ibm.lconn.profiles.test.rest.model.RoleFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;

import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class AdminRoleAPITest extends AbstractTest {

	public static final String CONTENT_TYPE = "application/atomcat+xml";

	private String TARGET_USERID = "targetUserid";
	private String TARGET_EMAIL  = "targetEmail";

	private boolean _globalDebugTrace = false; // turn on for debugging

	private static final int MAX_ROLES = 5; // wja - for 5.0 MAX number of Roles is 1

	public void testRoleCRUDLifecycle()  throws Exception
	{
		assertNotNull(adminTransport);
		assertNotNull(mainTransport);

		// turn off the Abdera cache
		adminTransport.setNoCache(true);
		mainTransport.setNoCache(true);

		int numRoles = 0;		
		// process one role for this target user
		numRoles = 1;
		try {
			roleCRUDLifecycle(numRoles, true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// process a set of MAX_ROLES roles for this target user
		numRoles = MAX_ROLES;
		try {
			roleCRUDLifecycle(numRoles, false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("testRoleCRUDLifecycle() : end");
	}

	public void testRolePostAndDeleteDisabled()  throws Exception
	{
		System.out.println("testRolePostAndDeleteDisabled() : entry");
		assertNotNull(adminTransport);
		assertNotNull(mainTransport);

		// turn off the Abdera cache
		adminTransport.setNoCache(true);
		mainTransport.setNoCache(true);

		int numRoles = 0;		
		// process one role for this target user
		numRoles = 1;
		try {
			rolePostAndDeleteDisabled(numRoles, true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("testRolePostAndDeleteDisabled() : end");
	}

	private void rolePostAndDeleteDisabled(int numRoles, boolean useEmail) throws Exception
	{
		System.out.println("  rolePostAndDeleteDisabled(" + numRoles + ", " + useEmail + ") - entry" );

		String roleURLForAdmin    = urlBuilder.getProfileAdminRolesUrl(null);

		ProfileService user1ProfileService = getProfileService(mainTransport);
		String roleURLForNonAdmin = urlBuilder.getProfileAdminRolesUrl(null);

		Feed payload = createRolesFeed(numRoles);
		numRoles = payload.getEntries().size();
		debugOutputFeed(payload, "roleCRUDLifecycle : creation Feed\n");

		String targetUserId = null;
		String targetEmail  = null;
		if (useEmail) {
			targetEmail = "ajones134@janet.iris.com";
			roleURLForAdmin = URLBuilder.addQueryParameter(roleURLForAdmin, TARGET_EMAIL, targetEmail, true);
		}
		else { 
			targetUserId = user1ProfileService.getUserId();
			roleURLForAdmin = URLBuilder.addQueryParameter(roleURLForAdmin, TARGET_USERID, targetUserId, true);
		}

		// verify that a non-admin user cannot POST via admin end-point
		Transport user1 = mainTransport;
		try {
			user1.doAtomPost(null, roleURLForNonAdmin, payload, CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// verify that an admin user cannot POST via admin end-point
		try {
			adminTransport.doAtomPost(null, roleURLForAdmin, payload, CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.METHOD_NOT_ALLOWED);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// verify that deleting roles fails
			String userRolesToDeleteURL = urlBuilder.getProfileAdminRolesUrl(null);
			if (useEmail)
				userRolesToDeleteURL = URLBuilder.addQueryParameter(userRolesToDeleteURL, TARGET_EMAIL, targetEmail, true);
			else
				userRolesToDeleteURL = URLBuilder.addQueryParameter(userRolesToDeleteURL, TARGET_USERID, targetUserId, true);

			adminTransport.doAtomDelete(userRolesToDeleteURL, NO_HEADERS, HTTPResponseValidator.METHOD_NOT_ALLOWED);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("  rolePostAndDeleteDisabled(" + numRoles + ", " + useEmail + ") - exit" );
	}

	// copied from AtomGenerator2 in profiles.web/app
	public static final String OPENSEARCH_NS = "http://a9.com/-/spec/opensearch/1.1/";
	public static final String TOTAL_RESULTS = "totalResults";
	public static final String OPENSEARCH_PREFIX = "opensearch";
	public static final QName  QN_TOTAL_RESULTS = new QName(OPENSEARCH_NS, TOTAL_RESULTS, OPENSEARCH_PREFIX);

	private static final int UNKNOWN = -1;

	private void roleCRUDLifecycle(int numRoles, boolean useEmail) throws Exception
	{
		String roleURLForAdmin    = urlBuilder.getProfileAdminRolesUrl(null);

		ProfileService user1ProfileService = getProfileService(mainTransport);
		String roleURLForNonAdmin = urlBuilder.getProfileAdminRolesUrl(null);

		// verify that the target user currently has the default number of roles assigned
		String targetUserId = null;
		String targetEmail  = null;
		int expected = UNKNOWN;
		if (useEmail) {
			targetEmail = "ajones134@janet.iris.com";
			roleURLForAdmin = URLBuilder.addQueryParameter(roleURLForAdmin, TARGET_EMAIL, targetEmail, true);
		}
		else { 
			targetUserId = user1ProfileService.getUserId();
			roleURLForAdmin = URLBuilder.addQueryParameter(roleURLForAdmin, TARGET_USERID, targetUserId, true);
		}

		Feed rolesResultFeedRaw = adminTransport.doAtomGet(Feed.class, roleURLForAdmin, NO_HEADERS, null);
		int origNumRoles = 0;
		if (useEmail)
			origNumRoles = processResultFeed(rolesResultFeedRaw, expected, targetEmail, roleURLForAdmin);
		else
			origNumRoles = processResultFeed(rolesResultFeedRaw, expected, targetUserId, roleURLForAdmin);

		List<String> originalRoles = null;
		if (origNumRoles > 0) {
			originalRoles = preserveOriginalRoles(origNumRoles, rolesResultFeedRaw);
		}
		// POST new (numRoles) role(s) for the target user ( targetUserid -- the user whose roles are being assigned )

		numRoles = ((MAX_ROLES > numRoles) ? MAX_ROLES : numRoles);
		Feed payload = createRolesFeed(numRoles);
		numRoles = payload.getEntries().size();
		debugOutputFeed(payload, "roleCRUDLifecycle : creation Feed\n");

		// validate that a non-admin user cannot create via admin end-point
		Transport user1 = mainTransport;

		if (useEmail)
			roleURLForNonAdmin = URLBuilder.addQueryParameter(roleURLForNonAdmin, TARGET_EMAIL, targetEmail, true);
		else
			roleURLForNonAdmin = URLBuilder.addQueryParameter(roleURLForNonAdmin, TARGET_USERID, targetUserId, true);

		//user1.doAtomPost(null, roleURLForNonAdmin, payload, CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);
		user1.doAtomPut(null, roleURLForNonAdmin, payload, CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.FORBIDDEN);

		// now, do the creation as the admin
		//adminTransport.doAtomPost(null, roleURLForAdmin, payload, CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.OK);
		adminTransport.doAtomPut(null, roleURLForAdmin, payload, CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.OK);

		// wja - April 29 2014 Replace POST with PUT causes delete and add
		// verify the result; there should be 'numRoles' (plus how ever many the user already had) assigned to this user
		expected = numRoles; // + origNumRoles;
		expected = 1; // wja - for 5.0 max roles will be 1
		roleURLForAdmin = urlBuilder.getProfileAdminRolesUrl(null);
		if (useEmail)
			roleURLForAdmin = URLBuilder.addQueryParameter(roleURLForAdmin, TARGET_EMAIL, targetEmail, true);
		else
			roleURLForAdmin = URLBuilder.addQueryParameter(roleURLForAdmin, TARGET_USERID, targetUserId, true);

		rolesResultFeedRaw = adminTransport.doAtomGet(Feed.class, roleURLForAdmin, NO_HEADERS, null);

		if (useEmail)
			processResultFeed(rolesResultFeedRaw, expected, targetEmail, roleURLForAdmin);
		else
			processResultFeed(rolesResultFeedRaw, expected, targetUserId, roleURLForAdmin);

		// wja - April 29 2014 - disable DELETE operations for now

//		// delete the roles created above and verify that the user no longer has any roles assigned
//		if (useEmail)
//			deleteRolesForUser(targetEmail, useEmail);
//		else
//			deleteRolesForUser(targetUserId, useEmail);
//
//		expected = 0; // delete operation removes all roles; including any that predated this unit test.
//		roleURLForAdmin = urlBuilder.getProfileAdminRolesUrl(null);
//		if (useEmail)
//			roleURLForAdmin = URLBuilder.addQueryParameter(roleURLForAdmin, TARGET_EMAIL, targetEmail, true);
//		else
//			roleURLForAdmin = URLBuilder.addQueryParameter(roleURLForAdmin, TARGET_USERID, targetUserId, true);
//
//		rolesResultFeedRaw = null;
//		rolesResultFeedRaw = adminTransport.doAtomGet(Feed.class, roleURLForAdmin, NO_HEADERS, null);
//		if (useEmail)
//			processResultFeed(rolesResultFeedRaw, expected, targetEmail, roleURLForAdmin);
//		else
//			processResultFeed(rolesResultFeedRaw, expected, targetUserId, roleURLForAdmin);
//
//		if (origNumRoles > 0) {
//			payload = restoreOriginalRoles(origNumRoles, originalRoles);
//			debugOutputFeed(payload, "roleCRUDLifecycle : restore Feed\n");
//
//			//adminTransport.doAtomPost(null, roleURLForAdmin, payload, CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.OK);
//			adminTransport.doAtomPut(null, roleURLForAdmin, payload, CONTENT_TYPE, NO_HEADERS, HTTPResponseValidator.OK);
//
//			// verify the result; there should be 'origNumRoles' (how ever many the user already had) assigned to this user
//			expected = origNumRoles;
//			roleURLForAdmin = urlBuilder.getProfileAdminRolesUrl(null);
//			if (useEmail)
//				roleURLForAdmin = URLBuilder.addQueryParameter(roleURLForAdmin, TARGET_EMAIL, targetEmail, true);
//			else
//				roleURLForAdmin = URLBuilder.addQueryParameter(roleURLForAdmin, TARGET_USERID, targetUserId, true);
//
//			rolesResultFeedRaw = adminTransport.doAtomGet(Feed.class, roleURLForAdmin, NO_HEADERS, null);
//			if (useEmail)
//				processResultFeed(rolesResultFeedRaw, expected, targetEmail, roleURLForAdmin);
//			else
//				processResultFeed(rolesResultFeedRaw, expected, targetUserId, roleURLForAdmin);
//		}
	}

	private Feed createRolesFeed(int numRoles)
	{
		/*
		 *	atomEntry = element atom:entry {
		 *  (	roleId -- role_n (name)	)	}
		 */
		Abdera  abdera = Abdera.getInstance();
		Feed  roleFeed = abdera.newFeed();
		roleFeed.setId("RoleBatchFeed-" + numRoles);
		Set<String> set = new HashSet<String>(); // disallow duplicate roles

		for (int i = 0; i < numRoles; i++)
		{
			// get a "random" number < 1000 to use as ID
			Long now = (new Date()).getTime();
			Random generator = new Random( now );
			int id = 0;
			for (int j = i; j < numRoles; j++)
			{
				id = (int) generator.nextInt(1000);
			}
			String idStr = "";
			if ( (id & 1) == 0 )
			{
				idStr = "";
			}
			else {
				idStr = ".extended";
			}
			String name = ("employee" + idStr);
			set.add(name);
		}
	    Iterator<String> it = set.iterator();
	    while (it.hasNext()) {
	    	String name = it.next();
			Entry aRole = abdera.newEntry();
//			aRole.setId("Entry_ID_" + id);
//			aRole.setSummary(name);
//			aRole.setContent(name);
//			aRole.setText(name);
			aRole.setId(name);
			roleFeed.addEntry(aRole);
	    }
		roleFeed.complete();
		return roleFeed;
	}

	private Feed createRolesFeed(int numRoles, List<String> originalRoles)
	{
		/*
		 *	atomEntry = element atom:entry {
		 *  (	roleId -- role_n (name)	)	}
		 */
		Abdera  abdera = Abdera.getNewFactory().getAbdera(); // getInstance();
		Feed  roleFeed = abdera.newFeed();  // newFeed();
		roleFeed.setId("OriginalRolesFeed-" + numRoles);
		debugOutputFeed(roleFeed, "createRolesFeed ("+roleFeed.getId()+") : ");
		int count = 1;
		Entry aRole = null;
		for (Iterator<String> iterator = originalRoles.iterator(); iterator.hasNext();) {
			String employeeRole = (String) iterator.next();
			aRole = abdera.newEntry();
			aRole.setId(employeeRole);
			aRole.complete();
			debugOutputFeed(roleFeed, "createRolesFeed  aRole ("+count+") : ");

			roleFeed.addEntry(aRole);
			debugOutputFeed(roleFeed, "createRolesFeed ("+count+"): ");
			count++;
			aRole = null;
		}
		roleFeed.complete();
		return roleFeed;
	}

	private List<String> preserveOriginalRoles(int origNumRoles, Feed rolesResultFeedRaw) throws Exception
	{
		List<String> originalRoles = new ArrayList<String> ();
		RoleFeed rolesResultFeed = new RoleFeed(rolesResultFeedRaw);
		Map<String, RoleEntry> feedRoles = rolesResultFeed.getRoleIdToRoleEntryMap();

		for (Map.Entry<String, RoleEntry> entry : feedRoles.entrySet())
		{
			RoleEntry role = (RoleEntry) entry.getValue();
			String tmpRole = new String(role.getRoleId());
			if (_globalDebugTrace)
				System.out.println(entry.getKey() + " : " + role);
			originalRoles.add(tmpRole);
		}
		return originalRoles;
	}

	private Feed restoreOriginalRoles(int origNumRoles, List<String> originalRoles)
	{
		Feed payload = createRolesFeed(origNumRoles, originalRoles);

		return payload;
	}

	private void deleteRolesForUser(String targetUser, boolean useEmail) throws Exception {
		// delete the roles for the specified user
		String userRolesToDeleteURL = urlBuilder.getProfileAdminRolesUrl(null);

		if (useEmail)
			userRolesToDeleteURL = URLBuilder.addQueryParameter(userRolesToDeleteURL, TARGET_EMAIL, targetUser, true);
		else
			userRolesToDeleteURL = URLBuilder.addQueryParameter(userRolesToDeleteURL, TARGET_USERID, targetUser, true);

		adminTransport.doAtomDelete(userRolesToDeleteURL, NO_HEADERS, HTTPResponseValidator.OK);
	}

	private int processResultFeed(Feed rolesResultFeedRaw, int expected, String targetUser, String roleURLForAdmin) throws Exception
	{
		int numRoles = 0;
		if (null == rolesResultFeedRaw)
			System.out.println("roleCRUDLifecycle GET : got NULL for " + targetUser );
		else
		{
			if (_globalDebugTrace) {
				System.out.println("roleCRUDLifecycle GET : for " + roleURLForAdmin);
			}
			prettyPrint(rolesResultFeedRaw);
			RoleFeed rolesResultFeed = new RoleFeed(rolesResultFeedRaw);

			if (expected != UNKNOWN) {
				// there should be zero roles to start - may change if default has pre-loaded roles by default
				Assert.assertEquals(expected, rolesResultFeed.getTotalResults());
			}
			else {
				// if expected number is unknown, return the value currently assigned to this user
				numRoles = rolesResultFeed.getTotalResults();
			}
		}
		return numRoles;
	}

	private void debugOutputFeed(Base abderaItem, String title)
	{
		StringWriter writer = null; // writer preserves old feed text somehow

		if (_globalDebugTrace ) {
			try {
				writer = new StringWriter();
				abderaItem.writeTo(writer);
				String theString = writer.toString();
				System.out.println(title + theString);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if (null != writer) {
					try {
						writer.close();
					}
					catch (IOException e) { /* silent* */	}
				}
				writer = null;
			}
		}
	}

	public static ProfileService getProfileService(Transport t) throws Exception {
		Service service = t.doAtomGet(Service.class, urlBuilder.getProfilesServiceDocument(), HTTPResponseValidator.OK, false);
		ProfileService profileService = ProfileService.parseFrom(service);
		return profileService;
	}

}
