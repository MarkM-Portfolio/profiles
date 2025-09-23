<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.ibm.lconn.core.web.util.services.ServiceReferenceUtil" %>
<%@ page import="java.util.*" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2015                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%--
	@author sberajaw
--%>



<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="corenav"		uri="http://www.ibm.com/lconn/tags/corenav" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/>

<%-- if the string for the aria-label isn't there, just use a default.  This code can be removed once the string is present. --%>
<c:set var="tempLabel"><fmt:message key='label.footer'/></c:set>
<c:set var="lbl"><c:choose><c:when test="${fn:contains(tempLabel, '??')}">Footer</c:when><c:otherwise>${tempLabel}</c:otherwise></c:choose></c:set>

<profiles:isLotusLive var="isLotusLive"/>
<c:choose>
	<c:when test="${isLotusLive != 'true'}">
		<div id="lotusFooter" class="lotusFooter" role="contentinfo" aria-label="${lbl}">
			<corenav:footer appname="profiles" admin="admin"/>
		</div>	
	</c:when>
	<c:otherwise>		
		<corenav:footer appname="profiles" admin="admin"/>
	</c:otherwise>
</c:choose>		

<script language="Javascript" type="text/javascript">
    var lastMod = "0";
    <c:if test="${!empty lastUpdate}">
        // lastMod = "${lastUpdate}"; COMMENTED TEMPORARILY UNTIL WIDGET FRAMEWORK IS WORKING
    </c:if>

    var tempPageId = null;
	if( profilesData != null && profilesData.config != null && profilesData.config.pageId != null && profilesData.config.pageId != "" )
        tempPageId = profilesData.config.pageId;

    var timestamp = lastMod;
	
	var layoutInfoUrl = applicationContext + "/widgetInfoPersistence.do?resourceType=profile&appStart=" + profilesData.config.appStartupDate;
	var userInfoUrl = applicationContext + "/userinfo.do?&timestamp=<%= java.util.Calendar.getInstance().getTimeInMillis() %>";
	var resourceId = "<core:escapeJavaScript>${profileKey}</core:escapeJavaScript>";
	if(resourceId != "")
	{
		layoutInfoUrl += "&resourceId=" + resourceId;
		userInfoUrl += "&resourceId=" + resourceId;
	}

	var WidgetPlacementConfig = {
		params: {
			resourceId: "<core:escapeJavaScript>${key}</core:escapeJavaScript>", 
			contextRoot: applicationContext, 
			lastMod: timestamp, 
			version: "${applicationScope.appChkSum}", 
			appLangParam: appLang, 
			lang: helpLang
		},
		defaultPageId: "profilesView",
		widgetConfigXMLDocument: null, //if this variable is defined, an http request will not be may to obtain widget data feed via layoutInfoUrl.
		layoutInfoUrl: layoutInfoUrl, // required only if widgetConfigXMLDocument is not defined
		applicationContext: applicationContext, //required to load images for skins and URLs for data
		pageId: tempPageId, // can be null, will not render widget if null
		isProfilesEnv: true,  //backwards compability with 2.0 profiles release
		resourceId: resourceId, // can be null, community Uuid, profileKey, bloguid, activityuid
		resourceType: "profile", //required
		debug: <c:choose><c:when test="${applicationScope.debugProfilesJS}">true</c:when><c:otherwise>(window.debugWidgets == true)</c:otherwise></c:choose>, //optional, for debugging
		userInfoUrl: userInfoUrl, // need to check user permission
		hideElements: ["lotusColRight"],  // needed for fullpage widget mode support		
		lastMod: timestamp, 
		availableServices: {profile: true},
		resourceOwner: false,
		enabledPermissions: [],
		enabledFeatures: "${enabledFeatures}",
		tagListDelimiter: "<profiles:tagListDelimiter />"
	};
	
	//copy the permissions from the profilesData object;
	if (profilesData && profilesData.enabledPermissions && profilesData.enabledPermissions.length > 0) {
		WidgetPlacementConfig.enabledPermissions = profilesData.enabledPermissions;
	}

	<profiles:currentUser var="cUserProfile"/>
	var currentUserGuid = "<core:escapeJavaScript>${cUserProfile.guid}</core:escapeJavaScript>";

	var widgetUserInfo = {
		canContribute: "false", <%-- Ignored --%> 
		canPersonalize: "false", <%-- Always the case in Profiles --%>
		<c:choose>
			<c:when test="${!empty cUserProfile}">
				"logged-in": "true",
				displayName: "<core:escapeJavaScript>${cUserProfile.displayName}</core:escapeJavaScript>",  
				<c:if test="${showEmail}">email: "<core:escapeJavaScript>${cUserProfile.email}</core:escapeJavaScript>", </c:if>
				userId: "<core:escapeJavaScript>${cUserProfile.userid}</core:escapeJavaScript>", 
				userid: "<core:escapeJavaScript>${cUserProfile.userid}</core:escapeJavaScript>", 
				key: "${cUserProfile.key}"
			</c:when>
			<c:otherwise>
				"logged-in": "false"
			</c:otherwise>
		</c:choose>
	};	
	
	if( profilesData != null && profilesData.displayedUser != null && profilesData.displayedUser.email != null && profilesData.displayedUser.email != "" )
		WidgetPlacementConfig.params['email'] = profilesData.displayedUser.email; 	

	if( profilesData != null && profilesData.displayedUser != null && profilesData.displayedUser.userid != null && profilesData.displayedUser.userid != "" )
		WidgetPlacementConfig.params['userid'] = profilesData.displayedUser.userid; 
			
	if( profilesData != null && profilesData.displayedUser != null && profilesData.displayedUser.uid != null && profilesData.displayedUser.uid != "" )
		WidgetPlacementConfig.params['uid'] = profilesData.displayedUser.uid;

	if( profilesData != null && profilesData.displayedUser != null && profilesData.displayedUser.displayName != null && profilesData.displayedUser.displayName != "" )
		WidgetPlacementConfig.params['displayName'] = profilesData.displayedUser.displayName;
			
	if( profilesData != null && profilesData.loggedInUser != null && profilesData.loggedInUser.isLoggedIn != null)
		WidgetPlacementConfig.userLoggedIn = profilesData.loggedInUser.isLoggedIn;
	
	if( profilesData != null && profilesData.displayedUser != null && profilesData.loggedInUser && profilesData.displayedUser.key == profilesData.loggedInUser.loggedInUserKey)
		WidgetPlacementConfig.resourceOwner = true;
	else
		WidgetPlacementConfig.resourceOwner = false;
		
    <%
    	boolean secure = request.isSecure();
    	Collection services = ServiceReferenceUtil.getAllServiceRefs().values();
        Iterator itr = services.iterator();
        while(itr.hasNext())
        {
			ServiceReferenceUtil service = (ServiceReferenceUtil)itr.next();
			out.println( "WidgetPlacementConfig.availableServices['"+service.getServiceName()+"'] = true;");
			out.println( "WidgetPlacementConfig.params['"+service.getServiceName()+"SvcRef'] = '" + service.getServiceLink(secure) + "';");
		}
    %>
	
	//fail-safe in case the profiles service isn't returned from the list
	if (!WidgetPlacementConfig.params['profilesSvcRef'] && WidgetPlacementConfig.params.contextRoot) {
		WidgetPlacementConfig.availableServices['profiles'] = true;
		WidgetPlacementConfig.params['profilesSvcRef'] = WidgetPlacementConfig.params.contextRoot;
	}
    
    // Engage Widget Framework
	if( typeof(lconn) == "object" ) {
	     dojo.addOnLoad(function(){
			lconn.core.WidgetPlacement.init();
	     });
	}
</script>

<c:choose>
	<c:when test="${applicationScope['com.ibm.lconn.core.web.cache.DebugResourceServlet.enabled']}">
		<div class="lotusTiny"><b>IN DEBUG MODE</b></div>
	</c:when>
</c:choose>

<script language="javascript">
	if (!dojo.exists("com.ibm.lconn.layout.track")){
		dojo.require("com.ibm.lconn.layout.track"); // load track js api, each application should already load the common js jar that include this api
	}

// metrics read tracker
	var metrics = {};
	var extra = {};
	if(!(typeof readTrackerMetrics == "undefined")){
		metrics = readTrackerMetrics;
	}
	
	if(typeof metrics.itemType == "undefined"){
		metrics.itemType="PROFILES"; // define for each web page
	} 
	
	metrics.source="PROFILES"; 

	if (currentUserGuid) {
		 metrics.userId = currentUserGuid;
	}
	
	if(typeof metrics.contentId == "undefined"){
		metrics.contentId = "PROFILES";
	}
	
	extra.contentLink = window.location.href; 
	extra.contentTitle = metrics.contentTitle;
	
	metrics.extra = extra;
	
	var track = com.ibm.lconn.layout.track;
	track.read(metrics.contentId, metrics.itemType, metrics); // call tracker js to send read event
</script>
