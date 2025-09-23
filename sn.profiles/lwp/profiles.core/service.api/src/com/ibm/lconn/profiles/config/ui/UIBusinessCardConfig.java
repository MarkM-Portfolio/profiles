/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import com.ibm.lconn.profiles.config.AbstractConfigObject;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.Employee;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 *
 *
 */
public class UIBusinessCardConfig extends AbstractConfigObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5454730229720840564L;
	
	private final String profileType;
	private final boolean showProfilePhoto;
	private final boolean enableSametimeAwareness;
	
	private final boolean showSametimeStatusMsg;
	
	private final boolean showSametimeChatAction;
	private final boolean showSametimeCallAction;
	
	private final List<UIBusinessCardActionConfig> actions;
	
	public UIBusinessCardConfig(HierarchicalConfiguration config) {		
		HierarchicalConfiguration actions = (HierarchicalConfiguration) config.subset("actions");
		this.actions = Collections.unmodifiableList(initBizActions(actions));
		
		this.profileType = config.getString("[@profileType]");
		this.showProfilePhoto = config.getBoolean("[@showProfilePhoto]", true);
		this.enableSametimeAwareness = config.getBoolean("[@enableSametimeAwareness]", true);
		this.showSametimeStatusMsg = this.enableSametimeAwareness && config.getBoolean("[@showSametimeStatusMsg]", true);
		this.showSametimeChatAction = this.enableSametimeAwareness && config.getBoolean("[@showSametimeChatAction]", true);
		this.showSametimeCallAction = this.enableSametimeAwareness && config.getBoolean("[@showSametimeCallAction]", true);
	}
	
	/**
	 * 
	 * @param actions2
	 * @return
	 */
	private List<UIBusinessCardActionConfig> initBizActions(HierarchicalConfiguration actionsConfig) {		
		int maxIndex = actionsConfig.getMaxIndex("action");
		ArrayList<UIBusinessCardActionConfig> actions = new ArrayList<UIBusinessCardActionConfig>(maxIndex+1);
		
		for (int i = 0; i <= maxIndex; i++) {
			HierarchicalConfiguration actionConfig = (HierarchicalConfiguration) actionsConfig.subset("action(" + i + ")");
			actions.add(new UIBusinessCardActionConfig(actionConfig));
		}
		
		return actions;
	}

	/**
	 * Syntax sugar to retrieve 'default' card config
	 * @return
	 */
	public final static UIBusinessCardConfig instance() {
		return instance(DEFAULT);
	}
	
	/**
	 * Syntax sugar to get a business card by profile type
	 * @param profileType
	 * @return
	 */
	public final static UIBusinessCardConfig instance(String profileType) {
		return UIConfig.instance().getBusinessCardConfig(profileType);
	}

	/**
	 * @return the profileType
	 */
	public final String getProfileType() {
		return profileType;
	}

	/**
	 * @return the enableSametimeAwareness
	 */
	public final boolean isEnableSametimeAwareness() {
		return enableSametimeAwareness;
	}

	/**
	 * @return the showProfilePhoto
	 */
	public final boolean isShowProfilePhoto() {
		return showProfilePhoto;
	}

	/**
	 * @return the showSametimeStatusMsg
	 */
	public final boolean isShowSametimeStatusMsg() {
		return showSametimeStatusMsg;
	}

	/**
	 * @return the showSametimeCallAction
	 */
	public final boolean isShowSametimeCallAction() {
		return showSametimeCallAction;
	}

	/**
	 * @return the showSametimeChatAction
	 */
	public final boolean isShowSametimeChatAction() {
		return showSametimeChatAction;
	}

	/**
	 * Returns all actions
	 * @return the actions
	 */
	public final List<UIBusinessCardActionConfig> getActions() {
		return actions;
	}

	public static boolean CONFIG_UNIT_TEST = false;
	
	/**
	 * Utility method to get a cloned form of this list, resolving the URLs for
	 * the user.
	 * 
	 * @param exposeEmail
	 * @param profile
	 * @param profileSubMap
	 * @param secure
	 *            Is the request over HTTP or HTTPS
	 * @param authRequest
	 *            Is '&auth=true' specified, indicating that the resulting JSON
	 *            is intended for a specific user. If <code>false</code>, filter
	 *            actions that have 'acl' actions associated with them.
	 * @param areUsersConnected
	 *            Parameter for user connectivity. Some options are to be
	 *            filtered when two users are connected.
	 * @return
	 */
	public final List<UIBusinessCardActionConfig> getActions(
			boolean exposeEmail, 
			Employee profile,
			Map<String,String> profileSubMap, 
			boolean secure, 
			boolean authRequest) 
	{
		if (profileSubMap == null){
			return Collections.emptyList();
		}
		
		ArrayList<UIBusinessCardActionConfig> l = new ArrayList<UIBusinessCardActionConfig>(actions.size());
		for (UIBusinessCardActionConfig a : actions) {
			// first filter based on email exposure
			if (!a.isEmailEnabledRequired() || 
					(a.isEmailEnabledRequired() && exposeEmail && StringUtils.isNotBlank(profileSubMap.get("email")))) 
			{
				// Next filter based on if the users are connected
				if (!a.isHideIfAnyConnection() || !areUsersConnected(profile))
				{
					// Finally check for access setting
					if (a.getRequireAcl() == null || CONFIG_UNIT_TEST || (authRequest && PolicyHelper.checkAcl(a.getRequireAcl(), profile)))
					{
						// next filter based on access
						l.add(new UIBusinessCardActionConfig(a, profileSubMap, secure));
					}
				}
			}
		}
		return l;
	}

	private static final String CONNECTED_KEY = "com.ibm.lconn.bizCard.areConnected";
	
	/**
	 * Utility method to check if users are connected
	 * @param profile
	 * @return
	 */
	private boolean areUsersConnected(Employee profile) 
	{
		if (profile == null || CONFIG_UNIT_TEST || StringUtils.isEmpty(profile.getKey()) || 
			!AppContextAccess.isAuthenticated() || AppContextAccess.getCurrentUserProfile() == null) 
		{
			return false;
		}
		
		Boolean connected_p = (Boolean) profile.get(CONNECTED_KEY);
		if (connected_p == null) {
			Connection c = AppServiceContextAccess.getContextObject(ConnectionService.class).getConnection(
					AppContextAccess.getCurrentUserProfile().getKey(), profile.getKey(), PeoplePagesServiceConstants.COLLEAGUE, false, false);
			
			connected_p = Boolean.valueOf(c != null);
			profile.put(CONNECTED_KEY, connected_p);
		}
		
		return connected_p;
	}
	
}
