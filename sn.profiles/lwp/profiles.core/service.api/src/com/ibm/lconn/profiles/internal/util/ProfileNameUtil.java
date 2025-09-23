/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2012, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.profiles.config.ProfilesConfig;

/**
 * @author colleen
 */
public class ProfileNameUtil
{
  public static List<Name> cleanName(String name, boolean isKanji)
  {
    if (isKanji)
    {
      return cleanKanjiName(name);
    }
    else
    {
    return cleanName(name);
    }
    
  }
	
  public static List<Name> cleanName(String name)
  {
    boolean searchOnFirstName = ProfilesConfig.instance().getOptionsConfig().isFirstNameSearchEnabled();
    
    List<Name> names = new ArrayList<Name>();
    String firstname = "";
    String lastname = "";
    Vector<String> namesarr = new Vector<String>(); // list of names, each separated by a space
    name = name.trim();
    name = name.replace('*', '%');
    StringTokenizer comma = new StringTokenizer(name, ",");
    if (name.indexOf(',') != -1)
    {
      try
      { //prevents error on "," (just a comma)
        lastname = comma.nextToken().trim();
      }
      catch (NoSuchElementException e)
      {
        lastname = "";
      }
      try
      { //prevents error on "smith," (lastname followed by comma)
        firstname = comma.nextToken().trim();
      }
      catch (NoSuchElementException e)
      {
        firstname = "";
      }
      names.add(createName(firstname, lastname));
    }
    else
    {
      StringTokenizer ptsstr = new StringTokenizer(name, " \u3000"); //break up each word of name
      while (ptsstr.hasMoreTokens())
      {
        namesarr.addElement(ptsstr.nextToken()); //put each word in pts Vector
      }
      //we always want to search the whole name as a last name if isn't comma delimited
      lastname = name;
      names.add(createName(firstname, lastname));
      
      StringTokenizer ptsstr2 = new StringTokenizer(name, " \u3000");
      if (ptsstr2.countTokens() == 1 && searchOnFirstName)
      {
        lastname="";
        firstname=name;
        names.add(createName(firstname, lastname));
      }

      int startFrom = 0;
      int indexOfSpace = 0;
      for (int j = 0; j < namesarr.size() - 1; j++)
      {
        indexOfSpace = indexOfSpace(name, startFrom);
        firstname = name.substring(0, indexOfSpace);
        lastname = name.substring(indexOfSpace + 1);
        names.add(createName(firstname, lastname));
        startFrom = indexOfSpace + 1;
      }
    }
    return names;
  }
  
  public static List<Name> cleanKanjiName(String name)
  {
    List<Name> names = new ArrayList<Name>();
    String firstname = "";
    String lastname = "";
    Vector<String> namesarr = new Vector<String>(); // list of names, each separated by a space
    name = name.trim();
    name = name.replace('*', '%');
    StringTokenizer comma = new StringTokenizer(name, ",");
    if (name.indexOf(',') != -1)
    {
      try
      { //prevents error on "," (just a comma)
        firstname = comma.nextToken().trim();
      }
      catch (NoSuchElementException e)
      {
        firstname = "";
      }
      try
      { //prevents error on "smith," (lastname followed by comma)
        lastname = comma.nextToken().trim();
      }
      catch (NoSuchElementException e)
      {
        lastname = "";
      }
      names.add(createName(firstname, lastname));
    }
    else
    {
      StringTokenizer ptsstr = new StringTokenizer(name, " \u3000"); //break up each word of name
      while (ptsstr.hasMoreTokens())
      {
        namesarr.addElement(ptsstr.nextToken()); //put each word in pts Vector
      }
      //we always want to search the whole name as a last name if it's comma delimited
      lastname = name;
      names.add(createName(firstname, lastname));

      int startFrom = 0;
      int indexOfSpace = 0;
      for (int j = 0; j < namesarr.size() - 1; j++)
      {
        indexOfSpace = indexOfSpace(name, startFrom);
        lastname = name.substring(0, indexOfSpace);
        firstname = name.substring(indexOfSpace + 1);
        names.add(createName(firstname, lastname));
        startFrom = indexOfSpace + 1;
      }
    }
    return names;
  }

  private static Name createName(String firstname, String lastname)
  {
	firstname = scrubWildcards(firstname);
	lastname = scrubWildcards(lastname);
    Name name = new Name(firstname, lastname);
    return name;
  }
  
  // public to incorporate into unit test
	public static String scrubWildcards(String input) {
		// this code will strip leading wildcards and make sure one is appended to the end of the string
		// %%%abc%%% -> abc%, %abc% -> abc%, abc%%% ->abc%, a -> a%, %%a%%%b%c%% -> a%%%b%c%,
		// % -> %, %% -> %, empty string is not processed
		if (StringUtils.isEmpty(input)) {
			return input;
		}
		char ctmp;
		int istart = -1;
		int iend = -1;
		// iterate from both ends of the string, continuing from a direction only if we find a
		// wildcard. once a non-wildcard is found at both ends, break out of the loop.
		for (int i = 0; i < input.length(); i++) {
			if (istart < 0) {
				ctmp = input.charAt(i);
				//if (ctmp != '*' && ctmp != '%') {
				if (ctmp != '%') {
					istart = i;
				}
			}
			if (iend < 0) {
				ctmp = input.charAt(input.length() - 1 - i);
				//if (ctmp != '*' && ctmp != '%') {
				if (ctmp != '%') {
					iend = input.length() - i;
				}
			}
			if (istart > 0 && iend > 0) {
				break;
			}
		}
		StringBuffer rtnVal;
		// if istart and iend are both -1 we had all wildcards. the diff is still 0
		if (iend - istart > 0) {
			rtnVal = new StringBuffer(input.substring(istart, iend));
			rtnVal.append('%');
		}
		else {
			// we had all wildcards, which collapse to one.
			rtnVal = new StringBuffer("%");
		}
		return rtnVal.toString();
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

    /**
     * Inspects the string for bad name input and clean up
     * 
     * @param name
     * @return
     */
    public static String stipMiscCommaForName(String name) {
	String nm = StringUtils.defaultString(name).trim();  // guarantee non-null
	nm = nm.replaceAll("\\s+", " "); // replace all multi-spaces with single space
	nm = nm.replaceAll("(\\,(\\s*\\,)*)", ","); // replace string of comments with single comment
	nm = nm.replaceAll("^\\,", ""); // remove leading comma
	nm = nm.replaceAll("\\,$,", ""); // remove trailing comma
	nm = nm.trim();
	return nm;
    }
    
    public static class Name
    {
      private String firstName;
      private String lastName;
      
      public Name(String first, String last)
      {
        firstName = first;
        lastName = last;
      }
      /**
       * @return Returns the firstName.
       */
      public String getFirstName()
      {
        return firstName;
      }
      /**
       * @param firstName The firstName to set.
       */
      public void setFirstName(String firstName)
      {
        this.firstName = firstName;
      }
      /**
       * @return Returns the lastName.
       */
      public String getLastName()
      {
        return lastName;
      }
      /**
       * @param lastName The lastName to set.
       */
      public void setLastName(String lastName)
      {
        this.lastName = lastName;
      }

    }
}
