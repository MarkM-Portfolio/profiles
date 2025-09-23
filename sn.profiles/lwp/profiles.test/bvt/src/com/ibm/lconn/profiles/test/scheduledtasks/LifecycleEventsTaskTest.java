/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.scheduledtasks;

import java.util.ArrayList;
import java.util.Collections;

import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.TDICriteriaOperator;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria;
import com.ibm.lconn.profiles.data.TDIProfileSearchOptions;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria.TDIProfileAttribute;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.jobs.ProcessLifeCycleEventsTask;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.store.interfaces.UserPlatformEventsDao;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;


public class LifecycleEventsTaskTest extends BaseTransactionalTestCase {

	private ProcessLifeCycleEventsTask lifecycleTask;
	
	private TDIProfileService tdiProfileService = null;

	Employee employeeA;
	
	public void onSetUpBeforeTransactionDelegate() throws Exception{
		if (tdiProfileService == null) tdiProfileService = AppServiceContextAccess.getContextObject(TDIProfileService.class);
	}

	protected void onSetUpInTransaction() {
		// create two profiles
		employeeA = CreateUserUtil.createProfile("employeeA","employeeAemail",null);
	}
	
	public void testLifecycleProcessing() throws Throwable {
		try{
			// back door mechanism to seed the task scheduler. we should have a mock in infra
			// but this is an avenue to register the task info
//			Hashtable<String,String> argsForTask = new Hashtable<String,String>(1);
//			argsForTask.put("platformCommandBatchSize","100");
//			Hashtable<String,Hashtable<String,String>> map = new Hashtable<String,Hashtable<String,String>>(1);
//			map.put(ProcessLifeCycleEventsTask.TASK_NAME,argsForTask);
//			ServiceProcessImpl.setTaskAttributesFrConfig(map);
			// instantiate a task with the above info
//			lifecycleTask = new ProcessLifeCycleEventsTask();
			// run - create and update a record to create events
			//TestAppContext.setCurrUser(employeeA,true);
			//Employee employeeB = CreateUserUtil.createProfile("employeeB","employeeBemail",null);
			//updateUserDispName(employeeB.getKey());
			// need to commit work up to now - problem with cleanup.
//			lifecycleTask.init(null); // no op for now
//			lifecycleTask.doTask(argsForTask);
		}
		catch( Throwable t){
			t.printStackTrace();
			throw t;
		}
	}

	public void testQueries() throws Exception {
		// the lifecycle task is designed to not throw exceptions to its work manager
		// this test runs the underlying queries that drive the task
		runAs(employeeA,Boolean.TRUE);
		UserPlatformEventsDao dao = AppServiceContextAccess.getContextObject(UserPlatformEventsDao.class);
		dao.pollBatch(10,10);
		ArrayList<Integer> keys = new ArrayList<Integer>(2);
		keys.add(new Integer(1));
		keys.add(new Integer(2));
		dao.deleteBatch(keys);
	}

	private void updateUserDispName(String key) throws Exception {
		// need to update via TDI
		ProfileDescriptor descriptor = getDescForKey(key);
		String displayName = descriptor.getProfile().getDisplayName()+"_edit";
		descriptor.getProfile().setDisplayName(displayName);
		tdiProfileService.update(descriptor);
	}

	private ProfileDescriptor getDescForKey(String key) {
		// setup for key
		TDIProfileSearchOptions options = new TDIProfileSearchOptions();
		options.setPageSize(1);
		options.setSearchCriteria(new ArrayList<TDIProfileSearchCriteria>());
		
		TDIProfileSearchCriteria c = new TDIProfileSearchCriteria();
		c.setAttribute(TDIProfileAttribute.KEY);
		c.setOperator(TDICriteriaOperator.EQUALS);
		c.setValue(key);
		
		options.setSearchCriteria(Collections.singletonList(c));
		
		return tdiProfileService.getProfileCollection(options).getProfiles().get(0);
	}
}
