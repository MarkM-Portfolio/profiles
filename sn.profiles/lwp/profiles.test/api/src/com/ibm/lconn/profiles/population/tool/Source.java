/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.population.tool;

import java.io.File;

import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;

public abstract class Source
{
  protected File file;

  protected TaskManager taskManager;

  public Source(File file, TaskManager taskManager)
  {
    this.file = file;
    this.taskManager = taskManager;
  }

  public abstract void process() throws Exception;

  public void handleProfileEntry(ProfileEntry profileEntry)
  {
    taskManager.doTasks(profileEntry);
  }

}
