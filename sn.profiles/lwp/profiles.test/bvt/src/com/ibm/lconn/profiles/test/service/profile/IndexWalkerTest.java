/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.profile;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import com.ibm.lconn.profiles.data.IndexerProfileCollection;
import com.ibm.lconn.profiles.data.IndexerProfileDescriptor;
import com.ibm.lconn.profiles.data.IndexerSearchOptions;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;

/*
 *
 */
public class IndexWalkerTest extends BaseTransactionalTestCase {

	private ProfileService service;
	private TDIProfileService tdiSvc;
	
	protected void onSetUpBeforeTransactionDelegate() {
		if (service == null) {
			service = AppServiceContextAccess.getContextObject(ProfileService.class);
			tdiSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		}
		runAsAdmin(Boolean.TRUE);
		setRole("search-admin",true);
	}
	
	/**
	 * Main test of index walker
	 * @throws Exception on error
	 */
	public void testGetAll() throws Exception {
		// touch all records in DB to simulate terrible DB load
		jdbcTemplate.update("update EMPINST.EMPLOYEE set PROF_KEY=PROF_KEY");		
		Thread.sleep(100);
	
		// test with some number of deletes
		doTestGetAll();
	}
	
	/**
	 * Same as above but tests delete user functionality
	 * @throws Exception on error
	 */
	@SuppressWarnings("unchecked")
	public void testGetAllWithDeletes() throws Exception {
		// create a delete and retest
		List<String> keys = jdbcTemplate.queryForList("select PROF_KEY from EMPINST.EMPLOYEE", new Object[]{}, String.class);

		if (keys.size() > 20){
			int currIndex = 0;
			for (String key : keys) {
				if (++currIndex % 20 == 0) {
					tdiSvc.delete(key);
				}

			}		
			doTestGetAll();
		}
		else{
			System.out.println("Not enough records for testGetAllWithDeletes");
		}
	}

	/**
	 * execute test
	 * @throws Exception on error
	 */
	private void doTestGetAll() throws Exception {
		int totalProfiles = countProfiles();
		int totalDeleted = countDeleted();
		
		System.out.println("Testing counts - profiles: " + totalProfiles + " / deletes: " + totalDeleted);
		
		IndexerSearchOptions options = new IndexerSearchOptions(new Timestamp(0), new Timestamp(System.currentTimeMillis()), null, 100, false);
		
		doTestCounts(options, totalProfiles, totalDeleted);
		
		System.out.println("[COMPLETED] Testing counts - profiles: " + totalProfiles + " / deletes: " + totalDeleted);
	}
	
	/**
	 * Count Profiles
	 * @return number of profiles
	 */
	private final int countProfiles() {
		return jdbcTemplate.queryForInt("select count(*) from EMPINST.EMPLOYEE");
	}
	
	/**
	 * Count deleted
	 * @return number deleted
	 */
	private final int countDeleted() {
		return jdbcTemplate.queryForInt("select count(*) from EMPINST.EVENTLOG where EVENT_TYPE = 5");
	}
	
	/*
	 * Utility to easily check counts
	 * @param options
	 * @param totalValues
	 * @param totalProfiles
	 * @param totalDeleted
	 */
	private final void doTestCounts(IndexerSearchOptions options, final int totalProfiles, final int totalDeleted) {
		
		final int pageSize = options.getPageSize();
		final int totalValues = totalProfiles + totalDeleted;
		final HashMap<String,Boolean> keys = new HashMap<String,Boolean>((int)(totalValues*1.5));
		
		int counted = 0;
		int countedProfiles = 0;
		int countedDeletes = 0;
		
		while (options != null) {
			IndexerProfileCollection res = service.getForIndexing(options);
			
			if (totalValues - counted > pageSize) {
				assertEquals("Results set is incorrect, should be equal to pagsSize", pageSize, res.getProfiles().size());
			} else {
				assertEquals("Results set is incorrect, should be equal to remaining entries", totalValues - counted, res.getProfiles().size());
			}
			
			int numLeft = service.countForIndexing(options);
			assertEquals(totalValues - counted, numLeft);
			
			counted += res.getProfiles().size();
			//System.out.println("ProfilesSize: " + res.getProfiles().size());
			
			options = res.getNext();
			
			for (IndexerProfileDescriptor desc : res.getProfiles()) {
				if (desc.isTombstone()) {
					countedDeletes++;
				} else {
					assertNull(keys.put(desc.getProfile().getKey(), Boolean.TRUE));
					
					IndexerProfileDescriptor desc2 = service.getProfileForIndexing(desc.getProfile().getUserid());

					assertTrue("Given names failing for: " + desc.getProfile().getDisplayName() + " / countedDeletes: " + countedDeletes + " / isTombstone() " + desc.isTombstone(), desc.getSurnames().size() > 0);
					assertTrue(desc.getSurnames().size() > 0);
					
					assertNotNull(desc2);
					assertEquals(desc.getProfile().getKey(), desc2.getProfile().getKey());
					
					countedProfiles++;
				}
			}						
		}
		
		assertEquals(totalValues, counted);
		assertEquals(totalProfiles, keys.size());
		
		assertEquals("Check counted profiles correct", totalProfiles, countedProfiles);
		assertEquals("Check counted deletes correct", totalDeleted, countedDeletes);
	}
	
}
