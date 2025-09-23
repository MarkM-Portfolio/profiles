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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PopulationTool
{
  class Arguments
  {
    public static final String DIRECTORY = "-directory=";

    public static final String POPULATE = "-populate";

    public static final String PHOTO = "-photo=";
    
    public static final String TAGS = "-tags";
    
    public static final String REPORT_TO_CHAIN = "-reportToChain";
    
    public static final String GLOBAL_LEADERSHIP_DATA = "-globalLeadershipData";
    
    public static final String CODES = "-codes";
    
    public static final String W3_EXTENSION = "-w3";
  }

  private static Map<String, String> parseArguments(String[] arguments)
  {
    Map<String, String> argumentMap = new HashMap<String, String>(2);
    for (String argument : arguments)
    {
      if (argument.startsWith(Arguments.DIRECTORY))
      {
        String value = argument.substring(Arguments.DIRECTORY.length());
        argumentMap.put(Arguments.DIRECTORY, value);
      }
      else if (argument.startsWith(Arguments.POPULATE))
      {
        argumentMap.put(Arguments.POPULATE, "yes");
      }
      else if (argument.startsWith(Arguments.PHOTO))
      {
        String value = argument.substring(Arguments.PHOTO.length());
        argumentMap.put(Arguments.PHOTO, value);
      } else {
    	  argumentMap.put(argument, argument);
      }
    }
    return argumentMap;
  }

  /**
   * A simple utility for populating the Profiles DB using the Admin API
   * 
   * see readme.txt for more details on running the tool
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    Map<String, String> argumentMap = parseArguments(args);

    // the directory argument is required and most point to a list of users to populate or provide photos
    if (!argumentMap.containsKey(Arguments.DIRECTORY))
    {
      System.out.println("-directory=<location of user file (fileRegistry.xml, ldiff, etc)> is required argument.");
      System.exit(0);
    }

    // the units of work we want to do on each profile as its read

    List<Task> tasks = new ArrayList<Task>(10);
    if (argumentMap.containsKey(Arguments.POPULATE))
    {
      tasks.add(new CreateProfileTask());
    }
    if (argumentMap.containsKey(Arguments.PHOTO))
    {
      String imageFolderPath = argumentMap.get(Arguments.PHOTO);
      File imageFolder = new File(imageFolderPath);
      if (!imageFolder.exists() || !imageFolder.isDirectory())
      {
        System.out.println("Error: The image folder does not exist at the specified location:" + imageFolderPath);
        System.exit(0);
      }
      PhotoUploadTask photoUploadTask = new PhotoUploadTask(imageFolder);
      tasks.add(photoUploadTask);
    }
    
    if (argumentMap.containsKey(Arguments.TAGS)) {
        TagPopulationTask tagPopulationTask = new TagPopulationTask();
        tasks.add(tagPopulationTask);    	
    }
    
    if (argumentMap.containsKey(Arguments.REPORT_TO_CHAIN)) {
        ReportToChainTask reportToChainTask = new ReportToChainTask();
        tasks.add(reportToChainTask);
    }
    
    if (argumentMap.containsKey(Arguments.GLOBAL_LEADERSHIP_DATA)) {
        GlobalLeadershipDataTask globalLeadershipDataTask = new GlobalLeadershipDataTask();
        tasks.add(globalLeadershipDataTask);
    }
    
    if (argumentMap.containsKey(Arguments.CODES)) {
    	CodePopulationTask codePopulationTask = new CodePopulationTask();
    	tasks.add(codePopulationTask);
    }
    
    if (argumentMap.containsKey(Arguments.W3_EXTENSION)) {
    	W3PopulationTask w3PopulationTask = new W3PopulationTask();
    	tasks.add(w3PopulationTask);
    }
    
    TaskManager taskManager = new TaskManager(tasks, argumentMap);
    Source source = null;

    String directory = argumentMap.get(Arguments.DIRECTORY);
    // load the input file
    try
    {
      if (directory.endsWith("fileRegistry.xml"))
      {
        File fileRegistry = new File(directory);
        if (!fileRegistry.exists())
        {
          System.out.println("Error: The fileRegistry.xml does not exist at the specified location:" + directory);
          System.exit(0);
        }

        source = new FileRegistrySource(fileRegistry, taskManager);
      }
    }
    catch (Exception e)
    {
      System.out.println("Error: Invalid input directory file.");
      e.printStackTrace();
      System.exit(0);
    }

    // if we have no profileEntries, end
    if (source == null)
    {
      System.out.println("Error: There are no profile entries to process");
      System.exit(0);
    }

    // go
    source.process();

  }

}
