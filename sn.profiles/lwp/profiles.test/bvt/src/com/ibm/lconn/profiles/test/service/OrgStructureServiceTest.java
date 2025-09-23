/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.lconn.profiles.data.EmployeeCollection;
import com.ibm.lconn.profiles.data.ReportToRetrievalOptions;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.OrgStructureService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

/**
 *
 */
public class OrgStructureServiceTest extends BaseTransactionalTestCase {
	
	private static final int DEPTH = 5;
	private static final int NUM_MANAGED = 10;
	private static final int PAGE_SIZE = 4;
	
	private OrgStructureService service = null;
	private TDIProfileService tdiProfileService = null;

	private Employee root = null;
	private Employee branch = null;
	private List<Employee> tree = new ArrayList<Employee>(DEPTH);
	
	protected void onSetUpBeforeTransactionDelegate() {
		if (service == null) {
			service = AppServiceContextAccess.getContextObject(OrgStructureService.class);
		}
		runAsAdmin(Boolean.TRUE); // TDI service is an Admin level access API
		if (tdiProfileService == null){
			tdiProfileService =  AppServiceContextAccess.getContextObject(TDIProfileService.class);
		}
		runAsAdmin(Boolean.FALSE); // TDI service is an Admin level access API
		root = null;
		branch = null;
		tree.clear();
	}
	
	protected void onSetUpInTransaction() {
		root = CreateUserUtil.createProfile();
		tree.add(root);
		for ( int i = 0 ; i < NUM_MANAGED ; i++){
			branch = CreateUserUtil.createProfile(Collections.singletonMap("managerUid", root.getUid()));
		}
		// set up and maintain the report-to chain for one employee
		tree.add(branch);
		String prevUid = branch.getUid();
		for (int i = 2; i < DEPTH; i++) {
			Employee p = CreateUserUtil.createProfile(Collections.singletonMap("managerUid", prevUid));
			prevUid = p.getUid();
			tree.add(p);
		}
	}

	/**
	 * Retrieve the full set people managed by this user.
	 * Assume the list is less than 500.
	 * 
	 * @throws Exception
	 */
	public void testPeopleManagedPaged() throws Exception  {
		runAs(root);
		for (ProfileRetrievalOptions options : new ProfileRetrievalOptions[]{ProfileRetrievalOptions.LITE, ProfileRetrievalOptions.EVERYTHING}) {
			ReportToRetrievalOptions setOptions = new ReportToRetrievalOptions();
			setOptions.setProfileOptions(options);
			setOptions.setPageSize(PAGE_SIZE);
			setOptions.setEmployeeState(UserState.ACTIVE);
			setOptions.setIncludeCount(true);
			//
			int reportToCount = 0;
			ProfileLookupKey lookupKey;
			for (ProfileLookupKey.Type t : ProfileLookupKey.Type.values()) {
				lookupKey = new ProfileLookupKey(t, root.getLookupKeyValue(t));
				EmployeeCollection ecoll = service.getPeopleManaged(lookupKey, setOptions);
				assertTrue(ecoll.getResults().size() <= PAGE_SIZE);
				reportToCount += ecoll.getResults().size();
				while (ecoll.getNextSet() != null){
					ecoll = service.getPeopleManaged(lookupKey,(ReportToRetrievalOptions)ecoll.getNextSet());
					assertTrue(ecoll.getResults().size() <= PAGE_SIZE);
					reportToCount += ecoll.getResults().size();
				}
				assertTrue(reportToCount == NUM_MANAGED);
				reportToCount = 0; // reset
			}
		}
		// now request with larger pageset. we should get all on first query.
		ReportToRetrievalOptions setOptions = new ReportToRetrievalOptions();
		setOptions.setProfileOptions(ProfileRetrievalOptions.LITE);
		int pageSize = NUM_MANAGED * 10;
		setOptions.setPageSize(pageSize);
		setOptions.setIncludeCount(true);
		setOptions.setEmployeeState(UserState.ACTIVE);
		ProfileLookupKey lookupKey = new ProfileLookupKey(
				ProfileLookupKey.Type.UID, root.getLookupKeyValue(ProfileLookupKey.Type.UID));
		EmployeeCollection ecoll = service.getPeopleManaged(lookupKey, setOptions);
		int firstReportToCount = ecoll.getResults().size();
		assertTrue (firstReportToCount == NUM_MANAGED);
		assertTrue(ecoll.getNextSet() == null);
		assertTrue(ecoll.getTotalCount() == NUM_MANAGED);
		// inactivate a user and run the query again
		List<Employee> employees = ecoll.getResults();
		if ( employees.size() > 0){
			Employee e = employees.get(0);
		    runAsAdmin(Boolean.TRUE);
			tdiProfileService.inactivateProfile(e.getKey());
		    runAsAdmin(Boolean.FALSE);
			runAs(root);
			ecoll = service.getPeopleManaged(lookupKey, setOptions);
			int secondReportToCount = ecoll.getResults().size();
			assertTrue (firstReportToCount == secondReportToCount+1);
			// retrieve inactive users only
			setOptions.setEmployeeState(UserState.INACTIVE);
			ecoll = service.getPeopleManaged(lookupKey, setOptions);
			int inactiveReportToCount = ecoll.getResults().size();
			assertTrue (inactiveReportToCount == 1);
			// retrieve all users
			setOptions.setEmployeeState(null);
			ecoll = service.getPeopleManaged(lookupKey, setOptions);
			int allReportToCount = ecoll.getResults().size();
			assertTrue (allReportToCount == firstReportToCount);
		}		
	}

	public void testReportsToChain() throws Exception  {
		for (ProfileRetrievalOptions options : new ProfileRetrievalOptions[]{ProfileRetrievalOptions.LITE, ProfileRetrievalOptions.LITE, ProfileRetrievalOptions.EVERYTHING}) {
			Employee bottom = tree.get(tree.size()-1);
			runAs(bottom);
			for (ProfileLookupKey.Type t : ProfileLookupKey.Type.values()) {
				List<Employee> ret = service.getReportToChain(new ProfileLookupKey(t, bottom.getLookupKeyValue(t)), options, false, -1);
				assertEquals(DEPTH, ret.size());

				for (int i = 0; i < DEPTH; i++) {
					Employee p = tree.get(i);
					Employee r = ret.get(i);
					if (!p.getKey().equals(r.getKey())) {
						fail("Error top-down, depth=" + i + ", expect=" + p.getKey() + ", found=" + r.getKey());
					}
				}

				ret = service.getReportToChain(new ProfileLookupKey(t, bottom.getLookupKeyValue(t)), options, true, -1);
				assertEquals(DEPTH, ret.size());

				for (int i = 0; i < DEPTH; i++) {
					Employee p = tree.get((DEPTH-1)-i);
					Employee r = ret.get(i);
					if (!p.getKey().equals(r.getKey())) {
						fail("Error bottom-up, depth=" + i + ", expect=" + p.getKey() + ", found=" + r.getKey());
					}
				}
			}
		}
	}
}
