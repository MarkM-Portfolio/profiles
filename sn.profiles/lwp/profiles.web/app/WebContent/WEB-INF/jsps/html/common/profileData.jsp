<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2017                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>

<profiles:currentUser var="currentUser"/><jsp:useBean id="currentUser" class="com.ibm.peoplepages.data.Employee" />
<c:set var="isMultiTenant"><profiles:isMultiTenant /></c:set>

(function() {
	if (typeof(profilesData) == "undefined") {
		window.profilesData = {};
	}

	profilesData.config = {
		profileLastMod: "${lastUpdate}",
		appChkSum: "${applicationScope.appChkSum}",
		buildNumber: "${buildNumber}",
		versionNumber: "${versionNumber}",
		appStartupDate: "${appStartupDate}",
		pageId: "",
		langCookieName: "${langCookieName}",
		loginReturnPageEnc: "<core:escapeUrl value="${profilesOriginalLocation}"/>",
		isLotusLive: <profiles:isLotusLive />,
		isMultiTenant: ("${isMultiTenant}"=="true"?true:false),
		isAdvancedSearchEnabled: <profiles:isAdvancedSearchEnabled />
	};

	profilesData.displayedUser = { 
		uid: "<core:escapeJavaScript>${uid}</core:escapeJavaScript>", 
		tenantKey: "<core:escapeJavaScript>${displayedProfile.tenantKey}</core:escapeJavaScript>",
		profileType: "<core:escapeJavaScript>${profileType}</core:escapeJavaScript>", 
		email: "<c:if test="${showEmail}"><core:escapeJavaScript>${email}</core:escapeJavaScript></c:if>",
		key: "${key}",
		dn: "<core:escapeJavaScript>${displayedProfile.distinguishedName}</core:escapeJavaScript>",
		displayName: "<core:escapeJavaScript>${displayName}</core:escapeJavaScript>", 
		userid:"${userid}", 
		status: "<core:escapeJavaScript>${statusMessage}</core:escapeJavaScript>", 
		inNetwork: ("${inNetwork}"=="true"?true:false), 
		isActive:  ("${isActive}"=="true"?true:false),
		isVisitor: ("${isExternal}"=="true"?true:false),
		enabledPermissions: [] <%-- to be filled in later if applicable --%>
	};

	profilesData.loggedInUser = {
		isLoggedIn: ("${isLoggedIn}"=="true"?true:false),
		loggedInUserUID: "<core:escapeJavaScript>${loggedInUserUID}</core:escapeJavaScript>",
		loggedInUserKey: "${loggedInUserKey}",
		userid: "${currentUser.guid}",
		tenantKey: "<core:escapeJavaScript>${currentUser.tenantKey}</core:escapeJavaScript>",
		displayName: "<core:escapeJavaScript>${loggedInUserDisplayName}</core:escapeJavaScript>",
		status: "<core:escapeJavaScript>${loggedInStatusMessage}</core:escapeJavaScript>",
		<c:if test="${showEmail}">email: "<core:escapeJavaScript>${currentUser.email}</core:escapeJavaScript>",</c:if>
		isExternal: <%= currentUser.isExternal() %>,
		hasExtendedRole: <%= currentUser.hasExtendedRole() %>,
		enabledPermissions: [] <%-- to be filled in later if applicable --%>		
	};

	<%-- TODO - move this to a configurable setting?  We only want to make the bare minimum calls to backend to check permissions --%>
	<c:set var="permissionFeaturestoLookup">
		profile.sand,profile.reportTo,profile.colleague,profile.following,profile.status,profile.peopleManaged,profile.search,profile.connection,profile.typeAhead,profile.tag
	</c:set>
	<c:choose>
		<c:when test="${empty displayedProfile}"> <%-- user is on a non-profile view page --%>
			profilesData.enabledPermissions = profilesData.loggedInUser.enabledPermissions = "${profiles:getAllEnabledPermissionsForFeatures(currentUser, permissionFeaturestoLookup)}".split(",");
		</c:when>
		<c:when test="${key == currentUser.key}"> <%-- user is viewing his/her own profile view page --%>
			profilesData.enabledPermissions = profilesData.loggedInUser.enabledPermissions = profilesData.displayedUser.enabledPermissions = "${profiles:getAllEnabledPermissionsForFeatures(currentUser, permissionFeaturestoLookup)}".split(",");
		</c:when>		
		<c:otherwise> <%-- user is viewing someone else's profile view page --%>
			profilesData.loggedInUser.enabledPermissions = "${profiles:getAllEnabledPermissionsForFeatures(currentUser, permissionFeaturestoLookup)}".split(",");
			profilesData.enabledPermissions = profilesData.displayedUser.enabledPermissions = "${profiles:getAllEnabledPermissionsForFeatures(displayedProfile, permissionFeaturestoLookup)}".split(","); <%-- permissions of currentUser automatically filtered in this calculation --%>
		</c:otherwise>
	</c:choose>	


	<%-- TO DO: revise this logic at the struts level --%>
	if ( "${currentUser.key}" != "") {
		if( profilesData.loggedInUser.isLoggedIn == false) profilesData.loggedInUser.isLoggedIn = ("${currentUser.key}"!=""?true:false);
		if( profilesData.loggedInUser.loggedInUserUID == "") profilesData.loggedInUser.loggedInUserUID = "${currentUser.guid}";
		if( profilesData.loggedInUser.loggedInUserKey == "") profilesData.loggedInUser.loggedInUserKey = "${currentUser.key}";
	}

	<%-- TO DO: revise this logic at the struts level --%>
	if( profilesData.loggedInUser.displayName == "") 
		profilesData.loggedInUser.displayName = "<core:escapeJavaScript>${currentUser.displayName}</core:escapeJavaScript>";

	if (!profilesData.activityStreamConfig) {
		try {
			profilesData.activityStreamConfig = <profiles:activityStreamConfig locale="${pageContext.request.locale}" userId="${userid}" currentUserId="${currentUser.guid}" currentUserDisplayName="${currentUser.displayName}" entryId="${entryId}" /> ;
			window.activityStreamConfig = profilesData.activityStreamConfig;
		} catch (pdError) {
			window.activityStreamConfig = {};
		}
	}

})();
