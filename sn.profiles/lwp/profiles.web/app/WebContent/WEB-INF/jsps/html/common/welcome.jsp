<%@ page contentType="text/html;charset=UTF-8" %>
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

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ taglib prefix="lc-ui"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>

<html:xhtml/>
<h2 id="_profilesWelcomeTitle_p" class="lotusHeading">${sceneTitle}</h2>
<p id="_profilesWelcomeBody_p">${sceneBody}</p>


<c:set var="liItems">
	<c:if test="${!empty sceneMsg1}">
		<li id="profilesWelcomeMsg1"></li>
	</c:if>

	<c:if test="${!empty sceneMsg2}">
		<li id="profilesWelcomeMsg2"></li>
	</c:if>

	<c:if test="${!empty sceneMsg3}">
		<li id="profilesWelcomeMsg3"></li>
	</c:if>

	<c:if test="${!empty sceneMsg4}">
		<li id="profilesWelcomeMsg4"></li>
	</c:if>
</c:set>

<c:if test="${!empty liItems}">
	<ul><c:out value="${liItems}" escapeXml="false"/></ul>
</c:if>

<script language="javascript">
(function() {
	//populate the welcome help text and links
	lconn.profiles.profiles_help.createWelcomeLineText('profilesWelcomeMsg1', '<c:out value="${sceneMsg1}"/>', '<c:out value="${sceneMsg1}"/>', '${sceneMsg1Link}');
	lconn.profiles.profiles_help.createWelcomeLineText('profilesWelcomeMsg2', '<c:out value="${sceneMsg2}"/>', '<c:out value="${sceneMsg2}"/>', '${sceneMsg2Link}');
	lconn.profiles.profiles_help.createWelcomeLineText('profilesWelcomeMsg3', '<c:out value="${sceneMsg3}"/>', '<c:out value="${sceneMsg3}"/>', '${sceneMsg3Link}');
	lconn.profiles.profiles_help.createWelcomeLineText('profilesWelcomeMsg4', '<c:out value="${sceneMsg4}"/>', '<c:out value="${sceneMsg4}"/>', '${sceneMsg4Link}');	
})();
</script>

<html:link styleId="_profilesViewWelcome_closeLink" styleClass="lotusBtnImg lotusClose" titleKey="label.welcome.close" href="javascript:;"
	onclick="dojo.cookie(this.parentNode.id,'hide',{expires:9999}); dojo.addClass(this.parentNode.id, 'lotusHidden');">
	<img src="<lc-ui:blankGif />" alt="<fmt:message key='label.welcome.close' />" />
	<span class="lotusAltText">X</span>
</html:link>
