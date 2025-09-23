/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileTag;
import com.ibm.peoplepages.data.ProfileTagCloud;
import com.ibm.peoplepages.data.ProfileTagRetrievalOptions.Verbosity;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */

public class SocialTagTest extends BaseTransactionalTestCase {
	private PeoplePagesService pps;
	private ProfileTagService tagSvc;
	
	private Employee employeeA, employeeB, employeeC;
    private Employee employeeArray[] = new Employee[3];
	private ProfileLookupKey userPLK[] = new ProfileLookupKey[3];
    
    protected void onSetUpBeforeTransactionDelegate() {
    	if (pps == null) {
    		pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
    		tagSvc = AppServiceContextAccess.getContextObject(ProfileTagService.class);
    	}
    }

	protected void onSetUpInTransaction() {
		// create two profiles
		employeeA = CreateUserUtil.createProfile("employeeA","employeeAemail",null);
		userPLK[0] = new ProfileLookupKey(ProfileLookupKey.Type.KEY,employeeA.getKey());
		employeeArray[0] = employeeA;
		employeeB = CreateUserUtil.createProfile("employeeB","employeeBemail",null);
		userPLK[1] = new ProfileLookupKey(ProfileLookupKey.Type.KEY,employeeB.getKey());
		employeeArray[1] = employeeB;
		employeeC = CreateUserUtil.createProfile("employeeC","employeeCemail",null);
		userPLK[2] = new ProfileLookupKey(ProfileLookupKey.Type.KEY,employeeC.getKey());
		employeeArray[2] = employeeC;
	}
        
	private static List<Tag> literalsToTag(String[] tags) {
		List<Tag> tagObjects = new ArrayList<Tag>();
		for (String term : tags) {
			Tag aTag = new Tag();
			aTag.setTag(term);
			aTag.setType(TagConfig.DEFAULT_TYPE);
			tagObjects.add(aTag);
		}
		return tagObjects;
	}
	
    public void testCreateRemoveSelfTag() throws Exception {
    	// case 1: w/sourceKey & targetKey
    	String[] tags = {"foo", "bar", "foobar"};
    	String[] tagsUpper = {"Foo", "BAR", "fooBaR"};  
    	List<Tag> tagsList = literalsToTag(tags);
    	List<Tag> tagsUpperList = literalsToTag(tagsUpper);

    	String key = userPLK[1].getValue();
    	runAs(employeeArray[1]);
    	
    	System.out.println("UpdateTags: " + tagsUpperList);
    	tagSvc.updateProfileTags(key, key, tagsUpperList, true);
    	
    	List<Tag> resultList = tagSvc.getTagsForKey(key);
    	System.out.println("ResultTags: " + resultList);
    	
    	Collections.sort(tagsList);
    	Collections.sort(resultList);
    	
    	assertEquals(tagsList,resultList);

    	// case 2: by updating employee
    	tags = new String[]{};
    	tagsList = literalsToTag(tags);
    	
    	Employee e = pps.getProfile(userPLK[1], ProfileRetrievalOptions.MINIMUM);
    	e.setProfileTags(tagsList);
    	
    	tagSvc.updateProfileTags(e.getKey(), e.getKey(), e.getProfileTags(), true);
    	
    	resultList = tagSvc.getTagsForKey(key);
    	
    	assertEquals(tagsList,resultList);
    }
    
    public void testCreateTagOther() throws Exception{
    	// case 1: w/sourceKey & targetKey
    	String[] tags = {"foo", "bar", "foobar"};
    	List<Tag> tagsList = literalsToTag(tags);
    	
    	ProfileLookupKey sourceKey = userPLK[1];
    	ProfileLookupKey targetKey = userPLK[2];
    	runAs(employeeArray[1]);
    	
    	tagSvc.updateProfileTags(sourceKey.getValue(), targetKey.getValue(), tagsList, true);
    	
    	ProfileTagCloud ptc = tagSvc.getProfileTags(sourceKey, targetKey);
    	
    	checkEquals(tags,ptc);
    	
    	// case 2: w/sourceKey & targetKey (2)
    	tags = new String[]{};
    	tagsList = literalsToTag(tags);
    	
    	tagSvc.updateProfileTags(sourceKey.getValue(), targetKey.getValue(), tagsList, true);
    	
    	ptc = tagSvc.getProfileTags(sourceKey, targetKey);

    	checkEquals(tags,ptc);
    	
    	// case 3: check last mod
    	//System.out.println("PTC updated: " + ptc.getRecordUpdated());
    	assertEquals(pps.getProfile(targetKey, ProfileRetrievalOptions.MINIMUM).getLastUpdate().getTime(), ptc.getRecordUpdated().getTime());
    }

    public void testDuplicateTag() throws Exception{
    	// rtc defect 95695
    	String[] tags1 = {"red", "red", "red", "blue"};
    	List<Tag> tagsList = literalsToTag(tags1);
    	
    	ProfileLookupKey sourceKey = userPLK[1];
    	ProfileLookupKey targetKey = userPLK[2];
    	runAs(employeeArray[1]);
    	// add tags with dupes
    	tagSvc.updateProfileTags(sourceKey.getValue(), targetKey.getValue(), tagsList, true);
    	// again with dupes of exiting and white space
    	String[] tags2 = {"", " ", "red", "blue", "yellow", "yellow"};
    	tagsList = literalsToTag(tags2);
    	tagSvc.updateProfileTags(sourceKey.getValue(), targetKey.getValue(), tagsList, true);
    	// check expected counts
    	ProfileTagCloud ptc = tagSvc.getProfileTags(sourceKey, targetKey);
    	String[] checkTags = {"red", "blue", "yellow",""," "};
    	int[] counts = {1,1,1,0,0};
    	checkCounts(ptc,checkTags,counts);
    }
    
    public void testTagCloud() throws Exception {
    	//
    	// case 1: w/sourceKey & targetKey
    	//
    	String[] tags = {"foo", "bar"};
    	String[] tags2 = {"foo", "bar", "foobar"};
    	int[] counts = {2,2,1};
    	List<Tag> tagsList = literalsToTag(tags);
    	List<Tag> tagsList2 = literalsToTag(tags2);
    	
    	ProfileLookupKey sourceKey = userPLK[1];
    	ProfileLookupKey targetKey = userPLK[2];
    	runAs(employeeArray[2]);

    	tagSvc.updateProfileTags(sourceKey.getValue(), targetKey.getValue(), tagsList, true);  // other
    	tagSvc.updateProfileTags(targetKey.getValue(), targetKey.getValue(), tagsList2, true); // self
    	
    	ProfileTagCloud ptc = tagSvc.getProfileTagCloud(targetKey, Verbosity.INCL_CONTRIBUTOR_IDS);
    	checkContrib(tags,sourceKey.getValue(),ptc, false);
    	checkCounts(ptc,tags2,counts);
    	
    	//
    	// case 2:  w/sourceKey & targetKey (2)
    	//
    	tags2 = new String[]{};
    	tagsList2 = literalsToTag(tags2);
    	counts = new int[]{1,1};
    	
    	// clear self tags
    	tagSvc.updateProfileTags(targetKey.getValue(), targetKey.getValue(), tagsList2, true);
    	
    	// check other guys tags
    	ptc = tagSvc.getProfileTagCloud(targetKey, Verbosity.INCL_CONTRIBUTOR_IDS);
    	checkContrib(tags,sourceKey.getValue(),ptc, false);
    	checkCounts(ptc,tags,counts);
    	
    	// clear tag of others
    	tagSvc.updateProfileTags(sourceKey.getValue(), targetKey.getValue(), tagsList2, true);
    	
    	ptc = tagSvc.getProfileTagCloud(targetKey, Verbosity.INCL_CONTRIBUTOR_IDS);
    	checkContrib(tags2,sourceKey.getValue(), ptc, false);
    	
    	//
    	// case 3: check last mod
    	//
    	assertEquals(pps.getProfile(targetKey, ProfileRetrievalOptions.MINIMUM).getLastUpdate().getTime(), ptc.getRecordUpdated().getTime());
    }
    
    public void testResolveContributors() throws Exception {
    	//
    	// case 1: w/sourceKey & targetKey
    	//
    	String[] tags = {"foo", "bar"};
    	String[] tags2 = {"foo", "bar", "foobar"};
    	int[] counts = {2,2,1};
    	List<Tag> tagsList = literalsToTag(tags);
    	List<Tag> tagsList2 = literalsToTag(tags2);
    	
    	ProfileLookupKey sourceKey = userPLK[1];
    	ProfileLookupKey targetKey = userPLK[2];
    	runAs(employeeArray[2]);
    	
    	tagSvc.updateProfileTags(sourceKey.getValue(), targetKey.getValue(), tagsList, true);  // other
    	tagSvc.updateProfileTags(targetKey.getValue(), targetKey.getValue(), tagsList2, true); // self
    	
    	ProfileTagCloud ptc = tagSvc.getProfileTagCloud(targetKey, Verbosity.RESOLVE_CONTRIBUTORS);
    	checkContrib(tags,sourceKey.getValue(),ptc, true);
    	checkCounts(ptc,tags2,counts);
    	
    	//
    	// case 2:  w/sourceKey & targetKey (2)
    	//
    	tags2 = new String[]{};
    	tagsList2 = literalsToTag(tags2);
    	counts = new int[]{1,1};
    	
    	// clear self tags
    	tagSvc.updateProfileTags(targetKey.getValue(), targetKey.getValue(), tagsList2, true);
    	
    	// check other guys tags
    	ptc = tagSvc.getProfileTagCloud(targetKey, Verbosity.RESOLVE_CONTRIBUTORS);
    	checkContrib(tags,sourceKey.getValue(),ptc, true);
    	checkCounts(ptc,tags,counts);
    	
    	// clear tag of others
    	tagSvc.updateProfileTags(sourceKey.getValue(), targetKey.getValue(), tagsList2, true);
    	
    	ptc = tagSvc.getProfileTagCloud(targetKey, Verbosity.RESOLVE_CONTRIBUTORS);
    	checkContrib(tags2,sourceKey.getValue(), ptc, true);
    	
    	//
    	// case 3: check last mod
    	//
    	assertEquals(pps.getProfile(targetKey, ProfileRetrievalOptions.MINIMUM).getLastUpdate().getTime(), ptc.getRecordUpdated().getTime());
    }
    
    public void testDeleteTags() throws Exception {
    	//
    	// case 1: w/sourceKey & targetKey
    	//
    	String[] tags = {"foo"};
    	List<Tag> tagsList = literalsToTag(tags);
    	
    	ProfileLookupKey sourceKey = userPLK[1];
    	ProfileLookupKey targetKey = userPLK[2];
    	runAs(employeeArray[2]);
    	
    	tagSvc.updateProfileTags(sourceKey.getValue(), targetKey.getValue(), tagsList, true);
    	tagSvc.updateProfileTags(targetKey.getValue(), targetKey.getValue(), tagsList, true);
    	
    	long before = pps.getProfile(targetKey, ProfileRetrievalOptions.MINIMUM).getLastUpdate().getTime();
    	tagSvc.deleteProfileTag(sourceKey.getValue(), targetKey.getValue(), "foo", TagConfig.DEFAULT_TYPE);
    	long after = pps.getProfile(targetKey, ProfileRetrievalOptions.MINIMUM).getLastUpdate().getTime();
    	
    	assertNotSame(before,after);
    	
    	ProfileTagCloud ptc = tagSvc.getProfileTagCloud(targetKey, Verbosity.MINIMUM);
    	
    	for (ProfileTag pt : ptc.getTags()) {
    		if ("foo".equals(pt.getTag())){
    			System.out.println("PR: " + pt);
    			fail("Found tag that should have been deleted");
    		}
    	}
    }

	private void checkCounts(ProfileTagCloud ptc, String[] tags, int[] counts) {
		int index = 0;
		for (String t : tags) {
			boolean found = false;
			int count = 0;
			for (ProfileTag pt : ptc.getTags()) {
				if (t.equals(pt.getTag())) {
					found = true;
					count = pt.getFrequency();
				}
			}
			if (!found) {
				if (counts[index] > 0) {
					fail("Missing tag: " + t);
				}
			}
			if (count != counts[index]) {
				fail("Mismatched Count for tag: " + t + " " + counts[index] + " != " + count);
			}
			index++;
		}
	}

	private void checkContrib(String[] tags, String sourceKey, ProfileTagCloud ptc, boolean checkResolve) {
		//System.out.println("Found tags: " + ptc.getTags());
		
		System.out.println("Tags: " + Arrays.asList(tags));
		
		for (String t : tags){
			boolean found = false;
			boolean flagged = false;
			for (ProfileTag pt : ptc.getTags()){
				if (t.equals(pt.getTag())){
					found = true;					
					flagged = Arrays.asList(pt.getSourceKeys()).contains(sourceKey);
					System.out.println("SourceKeys{" + pt.getTag() + "}: " + Arrays.asList(pt.getSourceKeys()));
				}
			}

			if (!found){
				fail("Missing tag: " + t);
			}
			if (!flagged){
				fail("Missing flag: " + t);
			}
			if (checkResolve){
				assertNotNull(ptc.getContributors().get(sourceKey));
				assertEquals(sourceKey, ptc.getContributors().get(sourceKey).getKey());
				//System.out.println("Found contrib: " + ptc.getContributors().get(sourceKey));
			}
		}
	}

	private void checkEquals(String[] tags, ProfileTagCloud ptc){
		assertNotNull(ptc.getTargetKey());
		
		for (String t : tags){
			boolean found = false;
			for (ProfileTag pt : ptc.getTags())
				if (t.equals(pt.getTag()))
					found = true;

			if (!found)
				fail("Misting tag: " + t);
		}
		
		for (ProfileTag t : ptc.getTags()){
			if (!Arrays.asList(tags).contains(t.getTag()))
				fail("Extra tag: " + t.getTag());
		}
	}
}

// having issues trying to create a string with embedded troublesome characters
////public void testInvalidCharacter() throws Exception{
//public static void main(String[] args){
//	try{
//	
//	byte a = 0x61;
//	byte b = 0x62;
//	byte c = 0x63;
//	byte d = 0x64;
//	
//	byte signedByte = -1;
//	int unsigned_e2 = signedByte & (0xe2);
//	System.out.println(unsigned_e2);
//	byte bytee2 = (byte)(unsigned_e2 & 0xff);
//	int unsigned_80 = signedByte & (0x80);
//	byte byte80 = (byte)(unsigned_80 & 0xff);
//	int unsigned_8b = signedByte & (0x8b);
//	byte byte8b = (byte)(unsigned_8b & 0xff);
//	
//	byte[] good = new byte[]{a,b,c,d};
//	byte[] bad = new byte[]{a,b,c,bytee2,byte80,byte8b,d};
//	printByte(bad);
//	
//	
//	String goodString = new String(good,"UTF-8");
//	String badString = new String(bad,"UTF-8");
//	printBinary(badString);
//	
//	System.out.println(goodString);
//	System.out.println(badString);
//	}
//	catch( Exception ex ){
//		System.out.println(ex);
//	}
//}
//
//private static void printBinary(String s){
//	byte[] bytes = s.getBytes();
//	  StringBuilder binary = new StringBuilder();
//	  for (byte b : bytes)
//	  {
//	     int val = b;
//	     for (int i = 0; i < 8; i++)
//	     {
//	        binary.append((val & 128) == 0 ? 0 : 1);
//	        val <<= 1;
//	     }
//	     binary.append(' ');
//	  }
//	  System.out.println("'" + s + "' to binary: " + binary);
//}
//
//private static void printByte( byte[] bytes){
//  StringBuilder binary = new StringBuilder();
//  for (byte b : bytes)
//  {
//     int val = b;
//     for (int i = 0; i < 8; i++)
//     {
//        binary.append((val & 128) == 0 ? 0 : 1);
//        val <<= 1;
//     }
//     binary.append(' ');
//  }
//  System.out.println("bytes to binary: " + binary);
//}
