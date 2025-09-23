/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import junit.framework.Assert;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

public class ProfilesOrgRelationTest extends AbstractTest {

	private static LinkedHashMap<Integer, List<Integer>> treeStructure;
	private static HashMap<Integer, ProfileEntry> treeCache;

	static{//build a static tree structure
		treeStructure = new LinkedHashMap<Integer, List<Integer>>();		
		treeStructure.put(1, Arrays.asList(2));			
		treeStructure.put(2, Arrays.asList(3));
		treeStructure.put(3, Arrays.asList(4,5));
		treeStructure.put(4, Arrays.asList(6,7,8));
		treeStructure.put(5, Arrays.asList(9,10,11));
		treeStructure.put(9, Arrays.asList(12));
		treeStructure.put(10, Arrays.asList(13));
		treeStructure.put(11, Arrays.asList(14,15));
		treeStructure.put(12, Arrays.asList(16));
	}		

	public void testOrgRelationFeeds() throws Exception
	{
		String methodName = "testOrgRelationFeeds";
		if (isOnPremise()) {
			this.setupProfilesOnServer();

			this.runTestStandardOrgRelationFeed();
			this.runTestHighLowNodePlacement();
			this.runTestSingleInputtedUser();

			// cleanup the reports' Profiles ...
			for (ProfileEntry pe : treeCache.values()) {
				adminTransport.doAtomDelete(pe.getLinkHref(ApiConstants.Atom.REL_SELF), NO_HEADERS, HTTPResponseValidator.OK);
			}
		}
		else {
			onCloudTestIsInvalid(methodName, true);
		}
	}

	private void runTestStandardOrgRelationFeed() throws Exception{
		String uidParams = (String)treeCache.get(15).getProfileFieldValue(Field.UID) +","+ (String)treeCache.get(16).getProfileFieldValue(Field.UID);	
		ProfileFeed orgRelFeed = this.getOrgRelationUrl(uidParams, false);	
		Assert.assertEquals("traverseTop false, should have hierarchy of 6", 6, orgRelFeed.getNumItems());							
		this.runTestOnReturnedEntries(orgRelFeed, 16, 12, 9, 5, 15, 11);

		orgRelFeed = this.getOrgRelationUrl(uidParams, true);	
		Assert.assertEquals("traverseTop true, should have hierarchy of 9",9, orgRelFeed.getNumItems());						
		this.runTestOnReturnedEntries(orgRelFeed, 16, 12, 9, 5, 15, 11, 3, 2, 1);

	}		

	private void runTestHighLowNodePlacement() throws Exception{
		String uidParams = (String)treeCache.get(16).getProfileFieldValue(Field.UID) +","+ (String)treeCache.get(2).getProfileFieldValue(Field.UID);
		ProfileFeed orgRelFeed = this.getOrgRelationUrl(uidParams, false);	
		Assert.assertEquals("traverseTop false, traversal nodes placed low and high depth 6",6, orgRelFeed.getNumItems());
		this.runTestOnReturnedEntries(orgRelFeed, 16, 12, 9, 5, 3, 2);
		orgRelFeed = this.getOrgRelationUrl(uidParams, true);	
		Assert.assertEquals("traverseTop true, traversal nodes placed low and high depth 7",7, orgRelFeed.getNumItems());
		this.runTestOnReturnedEntries(orgRelFeed, 16, 12, 9, 5, 3, 2, 1);
	}

	private void runTestSingleInputtedUser() throws Exception{
		String uidParams = (String)treeCache.get(4).getProfileFieldValue(Field.UID);
		ProfileFeed orgRelFeed = this.getOrgRelationUrl(uidParams, false);	
		Assert.assertEquals("traverseTop false, one node, should only get itself and immediate parent",2, orgRelFeed.getNumItems());
		this.runTestOnReturnedEntries(orgRelFeed, 4, 3);
		orgRelFeed = this.getOrgRelationUrl(uidParams, true);	
		Assert.assertEquals("traverseTop false, one node, should get entire report chain",4, orgRelFeed.getNumItems());
		this.runTestOnReturnedEntries(orgRelFeed, 4, 3, 2, 1);

	}

	private ProfileFeed getOrgRelationUrl(String uids, boolean traverseTop) throws Exception{
		String feedOneParam = uids;
		String orgRelationUrl = urlBuilder.getProfilesOrgRelation(Field.UID.getValue(), feedOneParam, traverseTop);
		Feed orgRelationFeedRaw = mainTransport.doAtomGet(Feed.class, orgRelationUrl, NO_HEADERS, HTTPResponseValidator.OK);

		ProfileFeed orgRelFeed = new ProfileFeed(orgRelationFeedRaw);		
		return orgRelFeed;
	}

	/**
	 * For a specific feed verify that the profile uids match the expected ones in
	 * the predefined org structure
	 * @param orgRelFeed
	 * @param ids
	 */
	private void runTestOnReturnedEntries(ProfileFeed orgRelFeed, Integer... ids){
		List<ProfileEntry> entries = orgRelFeed.getEntries();
		List<String> expectedIds = new ArrayList<String>();
		for(Integer id: ids){
			expectedIds.add((String)treeCache.get(id).getUserId());
		}
		for (ProfileEntry profileEntry : entries) {
			String uid = profileEntry.getUserId();
			Assert.assertTrue(expectedIds.contains(uid));
			expectedIds.remove(uid);
		}
	}

	private void setupProfilesOnServer() throws Exception{
		treeCache = new HashMap<Integer, ProfileEntry>();

		String managerUid = null;
		for (Integer entry : treeStructure.keySet()) {
			ProfileEntry manager = treeCache.get(entry);
			if (manager == null) {
				manager = this.initializeProfile(managerUid);		
				treeCache.put(entry, manager);	
			}			
			List<Integer> childNodes = treeStructure.get(entry);
			for (Integer child : childNodes) {
				ProfileEntry childProfile = this.initializeProfile((String) manager.getProfileFieldValue(Field.UID));
				treeCache.put(child, childProfile);
			}			
		}
	}

	private ProfileEntry initializeProfile(String managerUid) throws Exception
	{
		String methodName = "initializeProfile";
		ProfileEntry report = createProfile();
		if (isOnPremise()) {
			assertNotNull(report);
			if (managerUid != null) {
				report.updateFieldValue(Field.MANAGER_UID, managerUid);	
			}
			adminTransport.doAtomPut(null, report.getLinkHref(ApiConstants.Atom.REL_SELF), report.toEntryXml(), NO_HEADERS,
					HTTPResponseValidator.OK);
			Entry serverResponseBody = adminTransport.doAtomGet(Entry.class, report.getLinkHref(ApiConstants.Atom.REL_SELF), NO_HEADERS,
					HTTPResponseValidator.OK);
			report = new ProfileEntry(serverResponseBody);
			report.validate();		
		}
		else {
			onCloudTestIsInvalid(methodName, true);
		}
		return report;
	}
}
