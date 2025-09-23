/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.scheduledtasks;

import java.util.ArrayList;

import com.ibm.lconn.profiles.internal.jobs.ProcessTDIEventsTask;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.EventLogDao;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.EventLogRetrievalOptions;

public class AuditEventsTaskTest extends BaseTransactionalTestCase {

	private ProcessTDIEventsTask auditEventsTask;
	private TDIProfileService tdiProfileService = null;

	Employee employeeA;

	
	public void onSetUpBeforeTransactionDelegate() throws Exception{
		if (tdiProfileService == null) tdiProfileService = AppServiceContextAccess.getContextObject(TDIProfileService.class);
	}

	protected void onSetUpInTransaction() {
		// create two profiles
		employeeA = CreateUserUtil.createProfile("employeeA","employeeAemail",null);
	}

	public void testQueries() throws Exception {
		// the event processing task is designed to not throw exceptions to
		// its work manager this test runs the underlying queries that drive the task
		runAs(employeeA,Boolean.TRUE);
		// retrieve a batch of events
		EventLogDao dao = AppServiceContextAccess.getContextObject(EventLogDao.class);
		EventLogRetrievalOptions options = new EventLogRetrievalOptions();
		options.addEventType(EventLogEntry.SYS_EVENT_TDI);
		options.setMaxResults(10);	    
		// no requirement to skip results options.setSkipResults(0);
		EventLogEntry[] rtnVal = dao.getActiveBatch(options);

		// update state on batch
		final ArrayList<String> queuedEvents = new ArrayList<String>(2);
		queuedEvents.add("someKeyJustToTestQuery1");
		queuedEvents.add("someKeyJustToTestQuery2");
		dao.updateIsSysEvent(queuedEvents,EventLogEntry.SYS_EVENT_PROCESSED);

	}
	
	public void testTask() throws Throwable{
		try{
			// back door mechanism to seed the task scheduler. we should have a mock in infra
			// but this is an avenue to register the task info
//			Hashtable<String,String> argsForTask = new Hashtable<String,String>(1);
//			argsForTask.put("platformCommandBatchSize","100");
//			Hashtable<String,Hashtable<String,String>> map = new Hashtable<String,Hashtable<String,String>>(1);
//			map.put(ProcessTDIEventsTask.TASK_NAME,argsForTask);
//			ServiceProcessImpl.setTaskAttributesFrConfig(map);
			// run - create and update a record to create events
			//TestAppContext.setCurrUser(employeeA,true);
			//Employee employeeB = CreateUserUtil.createProfile("employeeB","employeeBemail",null);
			//updateUserDispName(employeeB.getKey());
			// need to commit work up to now - problem with cleanup.
//			auditEventsTask = new ProcessTDIEventsTask();
//			auditEventsTask.init(null);
//			auditEventsTask.doTask(argsForTask);
		}
		catch( Throwable t){
			t.printStackTrace();
			throw t;
		}
	}
}
