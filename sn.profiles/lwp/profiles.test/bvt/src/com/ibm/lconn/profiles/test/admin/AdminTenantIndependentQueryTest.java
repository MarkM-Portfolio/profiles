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

package com.ibm.lconn.profiles.test.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.data.Surname;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.SearchService2;
import com.ibm.lconn.profiles.internal.service.SurnameService;
import com.ibm.lconn.profiles.internal.util.NameHelper;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;


/**
 * Simple test case to run a query that crosses tenants using appropriate context.
 */
public class AdminTenantIndependentQueryTest extends BaseTransactionalTestCase {

	private static final String prefix = "_+_";
	private SurnameService surnameService;
	private SearchService2 searchSvc;
	
	@Override
	public void onSetUpBeforeTransactionDelegate() {
		surnameService = AppServiceContextAccess.getContextObject(SurnameService.class);
		searchSvc = AppServiceContextAccess.getContextObject(SearchService2.class);
		runAsAdmin(Boolean.TRUE);
	}
	public void testNameQuery(){
		int num = 2;
		ArrayList<String> tenantKeys = new ArrayList<String>(num);
		ArrayList<Employee> employees = new ArrayList<Employee>(num);
		// create employees in two different tenants
		for (int i = 0 ; i < num ; i++){
			tenantKeys.add(i,UUID.randomUUID().toString());
			Employee e = createUser(tenantKeys.get(i));
			employees.add(i,e);
		}
		// add two surnames for these employees
		for (int i = 0 ; i < num ; i++){
			CreateUserUtil.setTenantContext(tenantKeys.get(i));
			createTwoSurnames(employees.get(i));
		}
		//
		for (int i = 0 ; i < num ; i++){
			CreateUserUtil.setTenantContext(tenantKeys.get(i));
			String sname = prefix+"name";
			HashMap<String, Object> m = new HashMap<String, Object>();
			m.put(PeoplePagesServiceConstants.NAME, (Object) sname);
			ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(100);
			List<String> keys = searchSvc.dbSearchForProfileKeys(m, options);
			// assert that we found only key for Employee in this tenant
			assertTrue(keys.contains(employees.get(i).getKey()));
			for (int j = 0 ; j < num ; j++){
				if (i != j){
					assertFalse(keys.contains(employees.get(j).getKey()));
				}
			}
		}
		// now look for all names across tenants
		CreateUserUtil.setTenantContext(Tenant.IGNORE_TENANT_KEY);
		String sname = prefix+"name";
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put(PeoplePagesServiceConstants.NAME, (Object) sname);
		ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(100);
     	List<String> keys = searchSvc.dbSearchForProfileKeys(m, options);
		// assert that we found all Employees
		for (int i = 0 ; i < num ; i++){
			assertTrue(keys.contains(employees.get(i).getKey()));
		}
	}
	private Employee createUser(String tenantKey) {
		CreateUserUtil.setTenantContext(tenantKey);
		Employee rtnVal = CreateUserUtil.createProfile();
		return rtnVal;		
	}
	
	public void createTwoSurnames(Employee e) {
		List<Surname> sns = surnameService.getNames(e.getKey(), NameSource.SourceRepository);
		List<String> namesWithSource = NameHelper.getNamesForSource(sns, NameSource.SourceRepository);;
		// add the new name
		namesWithSource.add(prefix+"name1"+e.getKey());
		namesWithSource.add(prefix+"name2"+e.getKey());
		// now add all names
		surnameService.setNames(e.getKey(),NameSource.SourceRepository, UserState.ACTIVE, UserMode.INTERNAL, namesWithSource);
		// make sure names are added
		sns = surnameService.getNames(e.getKey(), NameSource.SourceRepository);
		Map<String,Surname> nm = NameHelper.toNameMap(sns,NameSource.SourceRepository);
		assertTrue(nm.containsKey((prefix+"name1"+e.getKey()).toLowerCase()));
		assertTrue(nm.containsKey((prefix+"name2"+e.getKey()).toLowerCase()));		
	}
}
