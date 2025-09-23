<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2014                                    --%>
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
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ taglib prefix="corenav"		uri="http://www.ibm.com/lconn/tags/corenav" %>

<html:xhtml/>

<%-- Common UI header --%>
<profiles:helpLang var="helpLangVar"/>
<profiles:currentUser var="cUserProfile"/>
<profiles:appLang var="profAppLang"/>
<c:url var="helpLangLinkVar" value="javascript:openHelpWindow();"/>
<c:url var="loginUrlVar" value="javascript:profiles_gotoLoginRedirect();" />
<c:url var="loginRedirectUrl" value="/auth/loginRedirect.do" />
<script type="text/javascript">
var profiles_gotoLoginRedirect = function() {
	// get current URL and set the loginReturnPage (SPR #XJZO8PTC78)
	var returnToPage = encodeURIComponent( window.location.pathname + window.location.search + window.location.hash );
	profiles_goto('${loginRedirectUrl}?loginReturnPage='+returnToPage,true);
}
</script>
<c:choose>
	<c:when test="${fn:containsIgnoreCase(profilesOriginalLocation,'myProfile')}">
		<c:set var="logoutExitPage" value="/home.do?lang=${profAppLang}"/>
	</c:when>
	<c:when test="${fn:containsIgnoreCase(profilesOriginalLocation,'html/networkView.do')}">
		<c:set var="logoutExitPage" value="${fn:replace(profilesOriginalLocation, '&requireAuth=true', '')}"/>
		<c:set var="logoutExitPage" value="${fn:replace(logoutExitPage, 'widgetId=follow', 'widgetId=friends')}"/>
		<c:set var="logoutExitPage" value="${fn:replace(logoutExitPage, '&action=in', '')}"/>
		<c:set var="logoutExitPage" value="${fn:replace(logoutExitPage, '&action=out', '')}"/>
		<c:set var="logoutExitPage" value="${fn:substringAfter(logoutExitPage, pageContext.request.contextPath)}"/>
	</c:when>
	<c:otherwise>
		<c:set var="logoutExitPage" value="${fn:substringAfter(profilesOriginalLocation, pageContext.request.contextPath)}"/>
	</c:otherwise>
</c:choose>
<c:url var="logoutUrlVar" value="/ibm_security_logout">
	<c:param name="logoutExitPage" value="${logoutExitPage}"/>	
</c:url>
<div id="lotusBanner" class="lotusBanner">
	<%-- RTC#71644; loginUrlVar and logoutUrlVar no longer have effect; left for reference --%>
	<corenav:header 
		appname="profiles"
		user="${cUserProfile.displayName}" 
		userId="${cUserProfile.userid}" 
		help="${helpLangLinkVar}"
		login="${loginUrlVar}"
		logout="${logoutUrlVar}"/>
</div>
<script type="text/javascript">
dojo.addOnLoad(function(){
	// RTC#71644 - special login/logout logic now handled by common code;
	var logoutElem = dojo.byId('logoutLink');
	if(logoutElem){
		var logoutLink = logoutElem.href;
		dojo.attr(logoutElem,{href: 'javascript:;'});
		if(logoutLink!=null){
			dojo.connect(logoutElem,'onclick', this, function(){
				//if (lconn.core.auth.isAuthenticated()) lconn.core.auth.isAuthenticated() needs to be recoded with reliable API... use profilesData until then.
				if( profilesData != null && profilesData.loggedInUser != null && profilesData.loggedInUser.isLoggedIn)
					lconn.core.auth.logout();
				else
					lconn.core.auth.login(); 
			});
		}
	}
});
</script>

