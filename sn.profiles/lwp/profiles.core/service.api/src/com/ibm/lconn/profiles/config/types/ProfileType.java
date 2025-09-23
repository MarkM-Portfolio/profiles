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

package com.ibm.lconn.profiles.config.types;

import java.util.List;
import java.util.Set;

public interface ProfileType {

	/**
	 * Identifies this profile type in a given configuration scope.
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * Identifies immediate parent type. It is <code>null</code> for the base type.
	 * 
	 * @return
	 */
	public String getParentId();

	/**
	 * A <code>Property</code> definition object with specified id or <code>null</code>
	 * 
	 * @param id
	 * @return
	 */
	public Property getPropertyById(String id);

	/**
	 * The set of property identifiers managed by this type.
	 * 
	 * @return
	 */
	public Set<String> getPropertyIds();

	/**
	 * The set of property definitions managed by this type.
	 * 
	 * @return
	 */
	public List<? extends Property> getProperties();

	/**
	 * Identifies organization for this profile type.
	 * 
	 * @return
	 */
	public String getOrgId();

	/**
	 * Make a deep copy of this profile type.
	 * 
	 * @return copy of this ProfileType object
	 */
	public ProfileType clone() throws CloneNotSupportedException;
	public ProfileType clone(boolean includePropertyMap, boolean includePropertyList) throws CloneNotSupportedException;

}
