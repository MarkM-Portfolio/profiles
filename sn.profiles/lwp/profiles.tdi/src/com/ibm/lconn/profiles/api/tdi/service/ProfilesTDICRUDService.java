/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.service;

import com.ibm.di.entry.Entry;

public interface ProfilesTDICRUDService {
	
	public String createProfile(Entry profileEntry)throws TDIException;
	
	public void deleteProfile(String key)throws TDIException;
	
	public void updateProfile(Entry profileEntry)throws TDIException;
	
	public Entry getProfileByKey(String key)throws TDIException;
	
	public Entry getProfileByUID(String uid)throws TDIException;
	
	public Entry getProfileByDN(String dn)throws TDIException;
	
	public Entry getProfileByEmail(String email)throws TDIException;
	
	public Entry getProfileByGUID(String guid)throws TDIException;
	
	public Entry getProfileBySourceURL(String sourceURL)throws TDIException;
	
	public boolean updateManagerField(String key)throws TDIException;
	
	public int syncProfileEntry(Entry ldapEntry, boolean store, boolean overide, 
			boolean enforce, String sourceLDAPURL, boolean showOnly)throws TDIException;
	
	public void updatePhoto(String uid, String photoURL) throws TDIException;
	
	public void updatePronunciation(String uid, String fileURL) throws TDIException;
	
}
