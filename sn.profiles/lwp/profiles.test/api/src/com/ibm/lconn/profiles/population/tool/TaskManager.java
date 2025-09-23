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

import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;

public class TaskManager
{
	
  private final Map<String, String> argumentMap;	
  private List<Task> tasks;

  public TaskManager(List<Task> tasks, Map<String, String> argumentMap)
  {
    this.tasks = tasks;
    this.argumentMap = argumentMap;
    
    for (Task task : tasks) {
    	task.setTaskManager(this);
    }
  }

  public Map<String, String> getArgumentMap() {
	  return argumentMap;
  }
  
  public void doTasks(ProfileEntry profileEntry)
  {
    for (Task task : tasks)
    {
      try
      {
    	  System.out.println("TASK:" + task.getClass().getName());
        task.doTask(profileEntry);
      }
      catch (Exception e)
      {
        // on error continue
      }
    }
  }
}
