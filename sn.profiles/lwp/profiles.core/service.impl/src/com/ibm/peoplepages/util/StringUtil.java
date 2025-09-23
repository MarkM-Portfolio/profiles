/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.util;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;

/**
 * @author colleen
 */
public class StringUtil
{

  public static String[] parseLocation(String loc)
  {
    String[] location = new String[2];
    String city = "";
    String state = "";
    StringTokenizer comma = new StringTokenizer(loc, ",");
    if (loc.indexOf(',') != -1)
    {
      try
      { //prevents error on "," (just a comma)
        city = comma.nextToken().trim();
      }
      catch (NoSuchElementException e)
      {
        city = "";
      }
      try
      { //prevents error on "westford," (city followed by comma)
        state = comma.nextToken().trim();
      }
      catch (NoSuchElementException e)
      {
        state = "";
      }
    }
    else
    {
      city = loc;
    }
    location[0] = city;
    location[1] = state;
    return location;
  }

  public static List<String> parseTags(String tags)
  {
	return StringUtil.parseTags(tags, true);
  }
  
  public static List<String> parseTags(String tags, boolean replaceTagFormat)
  {
    List<String> tagList = new ArrayList<String>();
	
    if (replaceTagFormat) {
    	tags = tags.replace('*', '%');
    	tags = tags.toLowerCase();
    }
    
    boolean tagCouldHaveASpace = false;
    for (TagConfig tagConfig : DMConfig.instance().getTagConfigs().values()) {
    	if (tagConfig.isPhraseSupported()) {
    		tagCouldHaveASpace = true;
    	}
    }

    String delimiters = ", \u3000";
    if (tagCouldHaveASpace) {
    	delimiters = ",\u3000";
    }
    StringTokenizer tokenizer = new StringTokenizer(tags, delimiters);
    while (tokenizer.hasMoreTokens())
    {
      String tag = tokenizer.nextToken();
      if (tag.lastIndexOf(',') == tag.length() - 1)
      {
        tag = tag.substring(0, tag.length() - 1);
      }
      tagList.add(tag);
    }
    return tagList;
  }
  
  public static List<String> concatWildcardOnTags(List<String> tagList)
  {
    List<String> fuzzyTags = new ArrayList<String>();
    for (int i = 0; i < tagList.size(); i++)
    {
      String tag = tagList.get(i);
      if (tag.indexOf('%') != tag.length() - 1)
      {
        tag += "%";
      }
      fuzzyTags.add(tag);
    }
    return fuzzyTags;
  }

  public static String getTagListDelimiter()
  {
	  for (TagConfig tagConfig : DMConfig.instance().getTagConfigs().values()) {
		  if (tagConfig.isPhraseSupported()) {
			  return ",";
		  }
	  }	  
	  return " ";
  }
  
  public static String convertTagListToString(List<String> tags)
  {
	String delimiter = StringUtil.getTagListDelimiter();

    String tagList = new String();
    for (int i = 0; i < tags.size(); i++)
    {
      if (i != 0)
      {
        tagList = tagList + delimiter;
      }
      String tag = tags.get(i);
      tagList = tagList + tag;
    }
    return tagList;
  }
  
  private final static int indexOfSpace(String str, int startIndex)
  {
	int spindex = str.indexOf(' ', startIndex);
	int kspindex = str.indexOf('\u3000',startIndex);
	
    if(kspindex == -1){
      return spindex;
    }
    else if (spindex > -1 && spindex < kspindex)	{
		return spindex;
	} else {
		return kspindex;
	}
  }

}
