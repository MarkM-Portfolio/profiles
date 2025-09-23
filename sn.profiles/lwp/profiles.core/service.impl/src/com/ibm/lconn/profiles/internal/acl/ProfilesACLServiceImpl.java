/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.acl;

import com.ibm.lconn.core.acl.model.UserInfo;
import com.ibm.lconn.core.acl.service.ACLService;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 *
 *
 */
public class ProfilesACLServiceImpl implements ACLService {
	
	// default ctor
	public ProfilesACLServiceImpl() {}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.core.acl.service.ACLService#canContribute(java.lang.String, java.lang.String)
	 */
	public boolean canContribute(String externalId, String resourceId) {
		return getUserInfo(externalId, resourceId).canContribute();
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.core.acl.service.ACLService#canPersonalize(java.lang.String, java.lang.String)
	 */
	public boolean canPersonalize(String externalId, String resourceId) {
		return getUserInfo(externalId, resourceId).canPersonalize();
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.core.acl.service.ACLService#canView(java.lang.String, java.lang.String)
	 */
	public boolean canView(String externalId, String resourceId) {
		return getUserInfo(externalId, resourceId).canView();
	}

	public UserInfo getUserInfo ( String resourceId ) {
		UserInfo userInfo = getUserInfoForEid(null);
		if (userInfo.containsKey("key") && userInfo.get("key").equals(resourceId)) {
			userInfo.setCanPersonalize(true);
			userInfo.setCanContribute(true);
			userInfo.setCanView(true);
		} else {
			userInfo.setCanPersonalize(false);
			userInfo.setCanContribute(false);
			userInfo.setCanView(true);
		}		
		return userInfo;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.core.acl.service.ACLService#getUserInfo(java.lang.String, java.lang.String)
	 */
	public UserInfo getUserInfo(String externalId, String resourceId) {
		UserInfo userInfo = getUserInfoForEid(externalId);
		if (userInfo.containsKey("key") && userInfo.get("key").equals(resourceId)) {
			userInfo.setCanPersonalize(true);
			userInfo.setCanContribute(true);
			userInfo.setCanView(true);
		} else {
			userInfo.setCanPersonalize(false);
			userInfo.setCanContribute(false);
			userInfo.setCanView(true);
		}		
		return userInfo;
	}
	
	private UserInfo getUserInfoForEid(String externalId) {
		Employee user = AppContextAccess.getCurrentUserProfile();
		
		if (externalId != null) {
			if (user != null && user.getUserid().equals(externalId)) {
				return user.getUserInfo();
			} else {
				user = AppServiceContextAccess.getContextObject(PeoplePagesService.class).getProfile(ProfileLookupKey.forUserid(externalId), ProfileRetrievalOptions.MINIMUM);
			}
		}
		
		if (user == null) 
			return new UserInfo();
		return user.getUserInfo();
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.core.acl.service.ACLService#getUserInfoByLoginName(java.lang.String, java.lang.String)
	 */
	public UserInfo getUserInfoByLoginName(String loggedInUserId, String resourceId) {
		return null;
	}

}
