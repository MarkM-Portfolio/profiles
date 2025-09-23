/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.data.AbstractName;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;

/**
 * Base name service for Sur/Given-names
 * 
 */
@Service
public interface BaseNameService<NT extends AbstractName<NT>> {

	/*
	 * Method to get the names for an invividual
	 * 
	 * @param key
	 * @param nameSources
	 * @return
	 */
	public List<NT> getNames(String key, NameSource... nameSources);
	
	/*
	 * Method to get names for a set of profiles
	 * 
	 * @param keys
	 * @param nameSources
	 * @return
	 */
	public Map<String, List<NT>> getNames(List<String> keys, NameSource... nameSources);
	
	/*
	 * Method to set names for a single profile
	 * 
	 * @param key
	 * @param nameSource
	 * @param names
	 */
	public void setNames(String key, NameSource nameSource, UserState usrState, UserMode userMode, List<String> names);
	
	/*
	 * Method to set names for a single profile
	 * 
	 * @param key
	 * @param nameSource
	 * @param names
	 */
	public void setNames(String key, NameSource nameSource, UserState usrState, UserMode userMode, String... names);
	
	/*
	 * Admin method to delete all the names for a user.  Currently only called through TDI process.
	 * 
	 * @param key
	 */
	public void deleteAll(String key);
	
	/*
	 * Change the state for a user
	 * @param usrState
	 */
	public void setState(String key, UserState usrState);
	
}
