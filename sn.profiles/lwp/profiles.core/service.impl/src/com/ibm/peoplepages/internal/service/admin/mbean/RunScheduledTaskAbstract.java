/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/**
 *
 */
package com.ibm.peoplepages.internal.service.admin.mbean;

import java.util.Hashtable;  // needed for runScheduledTask()
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;  // needed for runScheduledTask()
import org.apache.commons.logging.LogFactory;  // needed for runScheduledTask()

import java.util.concurrent.ExecutorService;  // needed for runScheduledTaskInternal()
import java.util.concurrent.Executors;  // needed for runScheduledTaskInternal()
import com.ibm.lconn.scheduler.admintasks.SchedulerTaskManager;  // needed for runScheduledTaskInternal()
import javax.management.ObjectName;	// needed for ExecuteLocalTaskOnServer()
import com.ibm.websphere.management.AdminClient;  	// needed for ExecuteLocalTaskOnServer()
import java.lang.reflect.Constructor;  // needed for runScheduledTaskInternal()
import com.ibm.lconn.scheduler.job.ScheduledJobBase;  // needed for runScheduledTaskInternal()

/**
 * @author  *
 * This mbean provides the interfaces needed to administer
 * managed app's in communities.
 * 
 * Note: This class was provided by the framework team. This should
 * be a base class for all MBeans. There is an issue with packaging
 * as some component package tasks in the war and others in the ear.
 * for now, due to classloader issues, each component uses a
 * version of the base class. profiles uses it for local tasks.
 */
public abstract class RunScheduledTaskAbstract {

    private final static Log logger = LogFactory.getLog( RunScheduledTaskAbstract.class);  // needed for runScheduledTask()
	//private static ResourceBundleHelper _rbh = new ResourceBundleHelper("com.ibm.tango.resources.mbean");  // needed for runScheduledTask()
	private static ConcurrentHashMap<String, ScheduledJobBase> taskNameAndClassMap = new ConcurrentHashMap(16);
	private static final ExecutorService servicePool = Executors.newCachedThreadPool();  // needed for runScheduledTask()

    public Integer runScheduledTaskLocal(Hashtable taskInfoMap){
       	servicePool.execute(new ExecuteLocalTaskOnServer( taskInfoMap));
		return new Integer(0);
	}

    public Integer runScheduledTaskInternal(Hashtable taskInfoMap){
		String taskName = (String)taskInfoMap.get("__taskName__");
		if (logger.isDebugEnabled()) logger.debug("ENTRY: taskName: " + taskName);

		Integer retInt = new Integer(0);
		ScheduledJobBase scheduledTaskClass = taskNameAndClassMap.get( taskName);
		// did we see this task before
		if (scheduledTaskClass != null){
			// yes, run cached task object
			try{
				((ScheduledJobBase)scheduledTaskClass).execute(taskInfoMap);
			}
			catch (Exception ex){
				String msg1 = SchedulerTaskManager.getSchedMsg( "error.invoke.task.failed", new Object[] {taskName});
	            logger.error(msg1, ex);
			}
		}
		else{
			// no, create task object
			String className = (String)taskInfoMap.get("__taskClassName__");
			String war_or_ear = (String)taskInfoMap.get("__war_or_ear__");
			try{
				// get the class we want to run
				// e.g., Class jobClass = Class.forName("com.ibm.openactivities.jobs.TrashAutoPurgeJobWS");
				Class jobClass = Class.forName(className);
				// set up to instantiate the class using the constructor that takes
				// the task name as an argument (String).
				Object jobClassObj = null;
				if (war_or_ear.equals("war")){
					// use one arg constructor
					Class[] consArgsArray = new Class[] { String.class };
					Constructor constructorWithOneArg = jobClass.getConstructor(consArgsArray);
					Object[] taskNameArg = new Object[] {taskName };
					// create the task class
					jobClassObj = constructorWithOneArg.newInstance(taskNameArg);
				}
				else{
					jobClassObj = jobClass.newInstance();
				}
				taskNameAndClassMap.put( taskName, (ScheduledJobBase)jobClassObj);
				((ScheduledJobBase)jobClassObj).execute(taskInfoMap);

			}
			catch (Exception ex){
				String msg1 = SchedulerTaskManager.getSchedMsg( "error.invoke.task.failed", new Object[] {taskName});
	            logger.error(msg1, ex);
			}
		}
        if (logger.isDebugEnabled()) logger.debug("EXIT: retInt: " + retInt);
		return retInt;
	}


	// this inner class is used to run a task locally in an asynch thread.  The reason for the
	// asynch thread is to prevent a SOAP timeout if the task is a long one.
	class ExecuteLocalTaskOnServer implements Runnable {
        private AdminClient dmgrAdminClient = null;
        private ObjectName objName = null;
        private Hashtable taskInfoMap = null;
		ExecuteLocalTaskOnServer( Hashtable taskInfoMap){
		    this.taskInfoMap = taskInfoMap;
        }

		public void run(){
			try{
    			runScheduledTaskInternal(taskInfoMap);
			}
			catch (Exception ex){
				String msg1 = SchedulerTaskManager.getSchedMsg( "error.remote.mbean.unavailable", new Object[] {taskInfoMap.get("__taskName__")});
				logger.error( msg1, ex);
			}
		}
	}
}
