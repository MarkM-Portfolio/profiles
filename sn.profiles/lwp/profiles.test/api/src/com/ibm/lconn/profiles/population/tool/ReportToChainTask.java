/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.population.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

/**
 * Populates tags on the profile
 */
public class ReportToChainTask extends Task {

	// number of employees to have at any level of tree
	public int MAX_EMPLOYEES_PER_DEPTH = 5;
		
	// keeps the org structure as an internal map
	public Map<Integer, List<ProfileEntry>> orgTree = new HashMap<Integer, List<ProfileEntry>>();
	
	// associate the employee to their depth in the org tree
	public Map<ProfileEntry, Integer> employeeToDepth = new HashMap<ProfileEntry, Integer>();
	
	// keeps how many employees a given manager has as descendants
	public Map<ProfileEntry, Integer> managerToNumEmployee = new HashMap<ProfileEntry, Integer>();
	
	// the depth in the org tree that is being processed
	private int depth = 0;
	
	public ReportToChainTask() throws Exception {
		super();
	}

	private void update(ProfileEntry profileEntry) throws Exception {
		String uid = (String)profileEntry.getProfileFields().get(Field.UID);
		String url = urlBuilder.getProfilesAdminProfileEntryUrl(URLBuilder.Query.UID, uid);
		adminTransport.doAtomPut(null, url, profileEntry.toEntryXml(), NO_HEADERS, HTTPResponseValidator.OK);		
	}
	
	public void updateLocalOrgTree(ProfileEntry employee, int depth) {
		List<ProfileEntry> items = orgTree.get(depth);
		if (items == null) {
			items = new ArrayList<ProfileEntry>();
			orgTree.put(depth, items);
		}
		items.add(employee);
		employeeToDepth.put(employee,  depth);
		managerToNumEmployee.put(employee, 0);
	}
	
	public void buildCeo(ProfileEntry ceo) throws Exception {
		ceo.getProfileFields().put(Field.IS_MANAGER, "Y");
		ceo.getProfileFields().put(Field.MANAGER_UID, "");		
		update(ceo);
		managerToNumEmployee.put(ceo, 0);
		updateLocalOrgTree(ceo, 0);
	}
	
	public void buildReportingRelationship(ProfileEntry manager, ProfileEntry employee) throws Exception {
		String managerUid = (String)manager.getProfileFields().get(Field.UID);
		int managerDepth = employeeToDepth.get(manager);
		
		manager.getProfileFields().put(Field.IS_MANAGER, "Y");
		employee.getProfileFields().put(Field.IS_MANAGER, "N");
		employee.getProfileFields().put(Field.MANAGER_UID, managerUid);
		update(manager);
		update(employee);

		updateLocalOrgTree(employee, managerDepth + 1);
		managerToNumEmployee.put(manager, managerToNumEmployee.get(manager) + 1);
	}
	
	private ProfileEntry findAvailableManagerForDepth(int depth) {
		ProfileEntry aManager = null;
		List<ProfileEntry> items = orgTree.get(depth - 1);
		if (items != null) {
			for (ProfileEntry pe : items) {
				if (managerToNumEmployee.get(pe) < MAX_EMPLOYEES_PER_DEPTH) {
					aManager = pe;
				}
			}
		}
		return aManager;
	}
	
	@Override
	public void doTask(ProfileEntry profileEntry) throws Exception {
		if (profileEntry == null)
			return;
		
		String uid = (String) profileEntry.getProfileFields().get(Field.UID);
		ProfileEntry serverVersion = getProfileByUid(uid);
		if (serverVersion != null) {
			
			// we need to set the CEO, so we just choose the first user that is processed as ceo
			if (depth == 0) {
				buildCeo(serverVersion);
				depth++;
			}
			else
			{
				ProfileEntry aManager = findAvailableManagerForDepth(depth);
				ProfileEntry aEmployee = serverVersion;
				if (aManager == null) {
					// someone is getting promoted to manager!
					depth++;
					aManager = findAvailableManagerForDepth(depth);
				}				
				buildReportingRelationship(aManager, aEmployee);				
			}	
		}
	}
			
}
