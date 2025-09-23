/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.profiles.data.AbstractName;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;

import com.ibm.peoplepages.data.Employee;

/**
 *
 *
 */
public final class NameHelper {
	
	private NameHelper() {}
	
	/**
	 * Utility method to convert name list to map
	 * 
	 * @param <NT>
	 * @param names
	 * @return
	 */
	public static final <NT extends AbstractName> Map<String, NT> toNameMap(List<NT> names) {
		return toNameMap(names, null);
	}

	/**
	 * Utility method to convert a list of names into a map containing only
	 * names for a specific name type.
	 * 
	 * @param <NT>
	 * @param names
	 * @param nameSource
	 *            If this is <code>null</code>, then all names are added to
	 *            the map
	 * @return
	 */
	public final static <NT extends AbstractName> Map<String, NT> toNameMap(List<NT> names, NameSource nameSource) {
		Map<String, NT> nft = new HashMap<String, NT>();
		
		for (NT name : names)
			if (nameSource == null || nameSource == name.getSource())
				nft.put(name.getName(), name);
		
		return nft;
	}
	
	/**
	 * Utility method to get the names for a given name source
	 * @param <NT>
	 * @param names
	 * @param nameSource
	 * @return
	 */
	public final static <NT extends AbstractName> List<String> getNamesForSource(List<NT> names, NameSource nameSource) {
		HashSet<String> names4s = new HashSet<String>(names.size()+4);
		for (NT name : names){
			String nameString = name.getName();
			if(nameString!=null){
				//make sure no duplication, by Liang
				if ((name.getSource() == nameSource) && (!names4s.contains(nameString)))
					names4s.add(nameString);
			}
		}
		return new ArrayList<String>(names4s);
	}

    /**
     *  A common method to insert name alias to the display name for Display purposes.
     *
     *  The logic goes as follows:
     *  a). We would insert the alias in front of the 'surname', if surname is part of the displayName;
     *  b). Otherwise, we would just append the alias to the end of the displayName.
     */
    public static String getNameToDisplay(Employee employee) {
	String displayName = StringUtils.trimToEmpty(employee.getDisplayName());
	String preferredFirstName = StringUtils.trimToEmpty(employee.getPreferredFirstName());
	String surname = StringUtils.trimToEmpty(employee.getSurname());
	StringBuffer name = new StringBuffer();
	boolean insertPreferredName = true;

	// Do some initial checking whether we need to insert the alias
	if ( StringUtils.isBlank( preferredFirstName ) ||
	     StringUtils.isBlank( displayName ) ||
	     StringUtils.startsWithIgnoreCase(displayName, preferredFirstName +" ") ||
	     StringUtils.endsWithIgnoreCase(displayName, " " +preferredFirstName) ||
	     StringUtils.endsWithIgnoreCase(displayName, "(" +preferredFirstName +")" ) ||
	     StringUtils.containsIgnoreCase(displayName, " " +preferredFirstName +" " ) || 
	     StringUtils.containsIgnoreCase(displayName, " (" +preferredFirstName +") " ) ) {

	    name.append( displayName );
	    insertPreferredName = false;
	}

	// After the basic checking, we still need to deal with the alias, then decide where to put the alias
	if ( insertPreferredName ) {

	    // this is assuming that there is a space in front of the last name. So in the case where there is only
	    // the last name in the displayName, we will insert the alias to the end still
	    int lastNameIndex = 0;
	    if ( !StringUtils.isEmpty( surname ) )
	    	lastNameIndex = StringUtils.indexOf(displayName.toLowerCase(), " " +surname.toLowerCase() );
	    
	    if ( lastNameIndex > 0 ) {
	    	name.append( StringUtils.substring(displayName, 0, lastNameIndex ) );
	    	name.append(" (" +preferredFirstName +")");
	    	name.append( StringUtils.substring(displayName, lastNameIndex) );
	    }
	    else if ( StringUtils.isNotBlank( preferredFirstName ) ) {
	    	name = new StringBuffer();
	    	name.append( displayName );
	    	name.append(" (" +preferredFirstName +")");
	    }
	}

	return name.toString();
    }
}
