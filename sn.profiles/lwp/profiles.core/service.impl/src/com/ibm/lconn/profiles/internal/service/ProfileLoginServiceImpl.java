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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.profiles.data.ProfileLogin;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileLoginDao;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;

/**
 * 
 */
@Service(ProfileLoginService.SVCNAME)
public class ProfileLoginServiceImpl extends AbstractProfilesService implements
		ProfileLoginService 
{
	@Autowired private ProfileLoginDao loginDao;

	/**
	 * @param txManager object
	 */
	@Autowired
	public ProfileLoginServiceImpl(@SNAXTransactionManager PlatformTransactionManager txManager)
	{
		super(txManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.lconn.profiles.internal.service.ProfileLoginsService#getLogins
	 * (java.lang.String)
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<String> getLogins(String key) {
		return loginDao.getLogins(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.lconn.profiles.internal.service.ProfileLoginsService#getLoginsForKeys
	 * (java.util.List)
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<ProfileLogin> getLoginsForKeys(List<String> keys) {
		return loginDao.getLoginsForKeys(keys);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.lconn.profiles.internal.service.ProfileLoginsService#setLogins
	 * (java.lang.String, java.util.List)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean setLogins(String key, List<String> logins) {
		List<String> toAdd = new ArrayList<String>(logins.size());
		HashSet<String> toRemove = new HashSet<String>(getLogins(key));
		
		// filter out duplicates
		HashSet<String> seenLogin = new HashSet<String>();
		
		for (String login : logins) {
			if (!StringUtils.isBlank(login)) {
				login = login.toLowerCase(Locale.ENGLISH);
				
				// [.add() : true=new value,not seen before ]
				// [.remove() : false=do not have the login in our list of logins currently ]
				if (seenLogin.add(login) && !toRemove.remove(login)) {
					toAdd.add(login);
				}
			}
		}

		loginDao.removeLogins(key, new ArrayList<String>(toRemove));
		loginDao.addLogins(key, toAdd);
		
		return 
			(toAdd.size() > 0 || toRemove.size() > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.ibm.lconn.profiles.internal.service.ProfileLoginsService#
	 * getProfileByLogins(java.lang.String,
	 * com.ibm.peoplepages.data.ProfileRetrievalOptions)
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Employee getProfileByLogin(String login) {
		AssertionUtils.assertNotEmpty(login);
		
		// include system attrs
		Employee p = loginDao.getProfileByLogins(login, false);		
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.lconn.profiles.internal.service.ProfileLoginsService#setLogins
	 * (java.lang.String, java.util.List)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void setLastLogin(String key, Date lastLogin) {
		loginDao.setLastLogin(key, lastLogin);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.lconn.profiles.internal.service.ProfileLoginService#deleteLastLogin
	 * (java.lang.String)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteLastLogin(String key) {
		assertCurrentUserAdmin();

		loginDao.deleteLastLogin(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.lconn.profiles.internal.service.ProfileLoginsService#getLoginsForKeys
	 * (java.util.List)
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Date getLastLogin(String key) {
		return loginDao.getLastLogin(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.lconn.profiles.internal.service.ProfileLoginService#count(java
	 * .util.Date)
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public long count(Date since) {
		return loginDao.count(since);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteAllLogins(String key) {

		List<String> logins = loginDao.getLogins(key);

		if (logins != null) {
			loginDao.removeLogins(key, logins);
		}

	}

}
