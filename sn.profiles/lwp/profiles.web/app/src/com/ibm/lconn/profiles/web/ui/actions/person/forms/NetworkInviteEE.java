/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.ui.actions.person.forms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import org.apache.abdera.model.AtomDate;
import com.ibm.json.java.JSONObject;
import com.ibm.lconn.core.web.exception.AuthorizationException;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.templates.TemplateConfig;
import com.ibm.lconn.profiles.config.templates.TemplateDataModel;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.service.FollowingService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionCollection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.RetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

@UrlBinding("/app/person/{sourceUserId}/forms/connect/{targetUserId}/connection/{connId}/")
public class NetworkInviteEE implements ActionBean {
	
	private static final String SELF = "@me";
	public static final int INITIAL_COMMON_FRIEND_COUNT = 10;
	
	private ActionBeanContext context = null;
	private String sourceUserId = null;
	private String targetUserId = null;
	private String sourceKey = null;
	private String targetKey = null;
	private String connId = null;
	private Connection connection = null;
	private Employee friend = null;
	private int commonFriendCount = 0;
	private List<Employee> commonFriends = null;
	private Map<String, Object> mixinMap;
	private TemplateDataModel dataModel;
	private boolean canFollow;
	private boolean following;
	
	public Map<String, Object> getMixinMap() {
		return mixinMap;
	}

	public void setMixinMap(Map<String, Object> mixinMap) {
		this.mixinMap = mixinMap;
	}

	public TemplateDataModel getDataModel() {
		return dataModel;
	}

	public void setDataModel(TemplateDataModel dataModel) {
		this.dataModel = dataModel;
	}

	public ActionBeanContext getContext() {
		return context;
	}

	public void setContext(ActionBeanContext context) {
		this.context = context;
	}
	
	@DefaultHandler
    public Resolution view () throws AuthorizationException {
	    AssertionUtils.assertTrue(AppContextAccess.isAuthenticated(), AssertionType.UNAUTHORIZED_ACTION);
	    AssertionUtils.assertNotNull(AppContextAccess.getCurrentUserProfile(), AssertionType.USER_NOT_FOUND);
		ConnectionService svc = AppServiceContextAccess.getContextObject(ConnectionService.class);
		PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		resolveConnection(svc, pps);
		resolveFriendsInCommon(svc);
		return new ForwardResolution("/WEB-INF/stripes/pages/app/person/forms/NetworkInviteEE.jsp");
    }

	protected void resolveConnection(ConnectionService svc, PeoplePagesService pps) {
		//Try to find the source user
		
		Employee currentUser;
		if (SELF.equals(sourceUserId)) {
			currentUser = AppContextAccess.getCurrentUserProfile();
		} else {
			currentUser = pps.getProfile(ProfileLookupKey.forUserid(sourceUserId), ProfileRetrievalOptions.LITE);	
		}
		if(currentUser == null) return; //TODO: do something
		sourceKey = (currentUser != null) ? currentUser.getKey() : null;
		sourceUserId = (currentUser != null) ? currentUser.getGuid() : null;
		
		//Setup the retrieval options to get all data on the user for the presentation of the friends details
		ProfileRetrievalOptions pro = new ProfileRetrievalOptions(ProfileRetrievalOptions.Verbosity.FULL, ProfilesConfig.instance().getTemplateConfig().getProfileOptionForTemplate(TemplateConfig.TemplateEnum.PROFILE_DETAILS).toArray(new ProfileRetrievalOptions.ProfileOption[0])); 
		//Try to find the connection based on the connection id
		if(connId != null) 
			connection = svc.getConnection(connId, true, true);
		if(connection != null) {
			// Make sure we're looking at an invite sent to the specified user
			if (!sourceKey.equals(connection.getSourceKey())) {
				connection = null; // Invalid connection
			}
			else {
				friend = pps.getProfile(ProfileLookupKey.forUserid(targetUserId), pro);
				targetKey = friend.getKey();
			}
		} else {
		// Find if a different connection exist between the same users		
			friend = pps.getProfile(ProfileLookupKey.forUserid(targetUserId), pro);
			AssertionUtils.assertNotNull(friend, AssertionType.RESOURCE_NOT_FOUND);
			targetKey = friend.getKey();
			connection = svc.getConnection(sourceKey, targetKey, PeoplePagesServiceConstants.COLLEAGUE, true, true);
		}
		createFriendDetails(); 
		initFollowing(currentUser, friend);
	}

	private void initFollowing(Employee currentUser, Employee other) {
		canFollow = false;
		following = false;
		FollowingService followingSvc = AppServiceContextAccess.getContextObject(FollowingService.class);
		if ( PolicyHelper.isFeatureEnabled(Feature.FOLLOW, other ) ) {
			if ( PolicyHelper.checkAcl(Acl.FOLLOWING_ADD, other ) ) { 
				following = followingSvc.isUserFollowed( currentUser, other ); // check if we are already following the person
				canFollow = true;
			}
		}
	}

	private void createFriendDetails() {
		dataModel = new TemplateDataModel(getContext().getRequest());
		dataModel.updateEmployee(friend, null);
		mixinMap = new HashMap<String, Object>(1);
		mixinMap.put("section", "jobInformation");
		dataModel.mixin(mixinMap);
	}

	protected void resolveFriendsInCommon(ConnectionService svc) {
			ConnectionRetrievalOptions incommonCRO = new ConnectionRetrievalOptions();			
			commonFriendCount = svc.getConnectionsInCommonCount(ProfileLookupKey.Type.KEY, new String[]{sourceKey, friend.getKey()}, incommonCRO);
			ConnectionCollection inCommon = getConnectionsInCommon(new String[] { sourceKey, friend.getKey()}, svc, commonFriendCount);
			List<Connection> connInCommon = inCommon.getResults();
			commonFriends = new ArrayList<Employee>();
			for (Connection conn : connInCommon) {
				commonFriends.add(conn.getTargetProfile());
			}
	}	
	
	private ConnectionCollection getConnectionsInCommon(
			String[] keys, ConnectionService cs,
			int totalCount) {
		ConnectionRetrievalOptions options = new ConnectionRetrievalOptions();
		options.setStatus(Connection.StatusType.ACCEPTED);
		options.setOrderBy(RetrievalOptions.OrderByType.UNORDERED);
		options.setSortOrder(RetrievalOptions.SortOrder.DEFAULT);
		options.setInclRelatedProfiles(true);
		options.setConnectionType(PeoplePagesServiceConstants.COLLEAGUE);
		options.setMaxResultsPerPage(INITIAL_COMMON_FRIEND_COUNT);
		return cs.getConnectionsInCommon(ProfileLookupKey.Type.KEY, keys,options);
	}

	public String getConnId() {
		return connId;
	}

	public void setConnId(String connId) {
		this.connId = connId;
	}

	public String getSourceUserId() {
		return sourceUserId;
	}

	public void setSourceUserId(String sourceUserId) {
		this.sourceUserId = sourceUserId;
	}

	public String getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(String targetUserId) {
		this.targetUserId = targetUserId;
	}

	public Employee getFriend() {
		return friend;
	}

	public Connection getConnection() {
		return connection;
	}
	
	public String getConnectionMessage() {
	    String msg = connection.getMessage();
	    if (msg == null)
	        return "";
	    else
	        return msg.replaceAll("<br/>", "\\\\n");
	}
	public String getJsonObjectEscaped() throws UnsupportedEncodingException {
		return escapeString(getJsonObject().toString());
	}
	public JSONObject getJsonObject() throws UnsupportedEncodingException {
	    JSONObject object = new JSONObject();
	    object.put("connectionExists", connection != null);
    	object.put("active", friend.isActive());
	    if(connection!=null) {
	    	object.put("url", getUrlPrefix() + "/atom/connection.do?connectionId=" + connection.getConnectionId());
	    	object.put("date", AtomDate.format(connection.getCreated()));
	    	object.put("uuid", connection.getConnectionId());
	    	object.put("status", connection.getStatus());
	    	object.put("targetName", friend.getDisplayName());
	    	object.put("targetId", friend.getUserid());
		    String message = connection.getMessage();
		    if (message != null)
		        message = message.replaceAll("<br/>", "\n");
		    else
		        message = "";
		    object.put("msg", message);
	    }
	    String keys = this.sourceKey + "," + this.friend.getKey();
	    object.put("commFriendUrl", getUrlPrefix() + "/atom/connectionsInCommon.do?connectionType=colleague&key=" + URLEncoder.encode(keys, "UTF-8") + "&ps=250");
		if(canFollow && !following) {
			String url = getUrlPrefix() + "/follow/atom/resources?source=profiles&type=profile";
			object.put("followUrl", url);
		}

	    
	    return object;
	}
	
	public boolean isCanFollow() {
		return canFollow;
	}
	public boolean getCanFollow() {
		return canFollow;
	}

	public void setCanFollow(boolean canFollow) {
		this.canFollow = canFollow;
	}

	public boolean isFollowing() {
		return following;
	}
	
	public boolean getFollowing() {
		return following;
	}

	public void setFollowing(boolean following) {
		this.following = following;
	}

	protected String getUrlPrefix () {
		return "";
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public int getCommonFriendCount() {
		return commonFriendCount;
	}
	
	public int getInitialCommonFriendCount() {
		return INITIAL_COMMON_FRIEND_COUNT;
	}

	public List<Employee> getCommonFriends() {
		return commonFriends;
	}
	
	public String getConnectionCreatedDate() {
	  return AtomDate.format(connection.getCreated());
	}
	
	private static String escapeString (String str) throws UnsupportedEncodingException {
	   if (str != null)
		   return URLEncoder.encode(str, "UTF-8").replace("+", "%20");
	   else
		   return null;
	}
	
	public String getPhotoLink() {
		return getUrlPrefix() + "/photo.do";
	}
}
