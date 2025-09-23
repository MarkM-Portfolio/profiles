<%@ page contentType="text/html;charset=UTF-8" session="false"%>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2010                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>

<core:serviceLink serviceName="profiles" var="profilesSvcHref"/>


<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.ibm.lconn.core.web.util.services.ServiceReferenceUtil" %>
<%@ page import="java.util.*" %>


<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="corenav"		uri="http://www.ibm.com/lconn/tags/corenav" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/>

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
		params: {resourceId: "<core:escapeJavaScript>${key}</core:escapeJavaScript>", contextRoot: applicationContext, lastMod: timestamp, version: "${applicationScope.appChkSum}", appLangParam: appLang, lang: helpLang},
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
		hideElements: ["businessCardContent", "lotusColRight"],  // needed for fullpage widget mode support		
		lastMod: timestamp, 
		availableServices: {profile: true},
		resourceOwner: false
	};
	
	<profiles:currentUser var="cUserProfile"/>
	var widgetUserInfo = {
		canContribute: "false", <%-- Ignored --%> 
		canPersonalize: "false", <%-- Always the case in Profiles --%>
		<c:choose>
			<c:when test="${!empty cUserProfile}">
				"logged-in": "true",
				displayName: "<core:escapeJavaScript>${cUserProfile.displayName}</core:escapeJavaScript>",  email: "<core:escapeJavaScript>${cUserProfile.email}</core:escapeJavaScript>", userid: "<core:escapeJavaScript>${cUserProfile.userid}</core:escapeJavaScript>",
				userId: "<core:escapeJavaScript>${cUserProfile.userid}</core:escapeJavaScript>", key: "${cUserProfile.key}"
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
</script>
