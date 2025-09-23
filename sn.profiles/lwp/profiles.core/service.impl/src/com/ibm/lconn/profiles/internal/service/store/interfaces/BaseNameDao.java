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
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.AbstractName;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;

/**
 *
 *
 */
@Repository
public interface BaseNameDao<NT extends AbstractName<NT>> {

	public List<NT> get(String key, NameSource... nameSources);
	
	public List<NT> getForKeys(List<String> keys, NameSource... nameSources);
	
	public void delete(String key, List<String> names);
	
	public void deleteAll(String key);
	
	public void create(String key, NameSource nameSource, UserState usrState, UserMode usermode, List<String> names);
	
	public void setState(String key, UserState usrState);
	
	// special method used to switch user tenant key. probably obsolete in visitor model
	public void setTenantKey(String key, String newTenantKey);
}
