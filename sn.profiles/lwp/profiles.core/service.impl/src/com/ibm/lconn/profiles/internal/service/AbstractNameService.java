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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.profiles.data.AbstractName;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.service.store.interfaces.BaseNameDao;
import com.ibm.lconn.profiles.internal.util.NameHelper;

/**
 *
 */
public abstract class AbstractNameService<NT extends AbstractName<NT>, NDaoT extends BaseNameDao<NT>>
	extends AbstractProfilesService 
	implements BaseNameService<NT> 
{ 
	private static final List<String> EMPTY_LIST = Collections.emptyList();
	
	protected final NDaoT nameDao;
	protected final int minNames;

	/**
	 * 
	 * @param txManager
	 * @param nameDao
	 * @param minNames
	 */
	protected AbstractNameService(PlatformTransactionManager txManager, NDaoT nameDao, int minNames) {
		super(txManager);
		this.nameDao = nameDao;
		this.minNames = minNames;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseNameService#getNames(java.lang.String, com.ibm.lconn.profiles.data.AbstractName.NameSource[])
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public List<NT> getNames(String key, NameSource... nameSources) {
		return nameDao.get(key, nameSources);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseNameService#getNames(java.util.List, com.ibm.lconn.profiles.data.AbstractName.NameSource[])
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public Map<String,List<NT>> getNames(List<String> keys, NameSource... nameSources) 
	{
		// Init map
		Map<String,List<NT>> nm = new HashMap<String,List<NT>>(keys.size() * 2);
		for (String key : keys)
			nm.put(key, new ArrayList<NT>());
		
		// Sort names
		List<NT> names = nameDao.getForKeys(keys, nameSources);
		for (NT name : names)
			nm.get(name.getKey()).add(name);
			
		return nm;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseNameService#setNames(java.lang.String, com.ibm.lconn.profiles.data.AbstractName.NameSource, com.ibm.lconn.profiles.internal.data.profile.UserState, java.util.List)
	 */	
	@Transactional(propagation=Propagation.REQUIRED)
	public void setNames(String key, NameSource nameSource, UserState usrState, UserMode userMode, List<String> names) {
		//
		// Error checking on edge conditions
		//
		if (nameSource == NameSource.SourceRepository && names.size() < minNames)
			throw new IllegalArgumentException("May not define empty list of names for user/key: " + key);
		
		//
		// Make sure only add unique names and 'tolower' these names
		//

		Map<String,NT> namesToAddForSource = new HashMap<String,NT>();
		
		for (String name : names) {
			if (!StringUtils.isEmpty(name)) {
				namesToAddForSource.put(name.toLowerCase(Locale.ENGLISH), null);
			}
		}
		
		//
		// Determine adds / deletes
		//
		List<NT> allNamesFromDao = nameDao.get(key); // get all
		Map<String,NT> namesFromDaoForSource = NameHelper.toNameMap(allNamesFromDao, nameSource);
		Map<String,NT> allNamesMap = NameHelper.toNameMap(allNamesFromDao);

		List<String> namesToDelete = new ArrayList<String>(namesToAddForSource.size());
		List<String> namesToAdd = new ArrayList<String>(namesToAddForSource.size());
		for (String name : namesToAddForSource.keySet()) 
		{
			// in here we would add some precedence of name sources
			if (namesFromDaoForSource.containsKey(name)) 
			{
				// name is aleady defined with correct source, so remove from the ultimate delete list
				namesFromDaoForSource.remove(name); // names remaining at the end will be deleted
			} 
			else if (allNamesMap.containsKey(name)) 
			{
				// arrival here means the name is in givennames, but with a different source.
				//  If we are doing SourceRepository names, this means that the source is NameExpansion, so 
				// we must delete and re-add 
				if (nameSource == NameSource.SourceRepository)
				{
					namesToDelete.add(name);
					namesToAdd.add(name);
				}
				else
				{ 
					// we are doing NameExpansion, so do nothing since the "nick" name is already
					// defined in ldap
				}
			} 
			else 
			{
				// name not in db, so add
				namesToAdd.add(name);
			}
		}
		namesToDelete.addAll(namesFromDaoForSource.keySet());
		
		if (namesToDelete.size() > 0) nameDao.delete(key, namesToDelete);
		if (namesToAdd.size() > 0) nameDao.create(key, nameSource, usrState, userMode, namesToAdd);
	}

	
	
	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseNameService#setNames(java.lang.String, com.ibm.lconn.profiles.data.AbstractName.NameSource, com.ibm.lconn.profiles.internal.data.profile.UserState, java.lang.String[])
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void setNames(String key, NameSource nameSource, UserState usrState, UserMode userMode, String... names) {
		setNames(key, nameSource, usrState, userMode, (names == null || names.length == 0) ? EMPTY_LIST :  Arrays.asList(names));
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseNameService#deleteAll(java.lang.String)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void deleteAll(String key) {
		assertCurrentUserAdmin();
		nameDao.deleteAll(key);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseNameService#setState(com.ibm.lconn.profiles.internal.data.profile.UserState)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void setState(String key, UserState usrState) {
		if (usrState == null)
			throw new NullPointerException("User state may not be null");
		nameDao.setState(key, usrState);
	}
}
