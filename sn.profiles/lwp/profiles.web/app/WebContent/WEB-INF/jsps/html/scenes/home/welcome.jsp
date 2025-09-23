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
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>

<html:xhtml/>
<profiles:isAdvancedSearchEnabled var="isAdvancedSearchEnabled"/>
<profiles:checkPermission feature="profile.tag" permission="profile.tag.add" checkTarget="false" var="canAddTag" />

<fmt:message key="label.directory.welcome.msg1" var="sceneMsg1" />
<c:if test="${isAdvancedSearchEnabled == 'true'}">
	<fmt:message key="label.directory.welcome.msg2" var="sceneMsg2" />
</c:if>
<c:if test="${canAddTag == 'true'}">
	<fmt:message key="label.directory.welcome.msg3" var="sceneMsg3" />
</c:if>
<c:set var="sceneMsg1Link" value="" />
<c:set var="sceneMsg2Link" value="t_pers_search_directory.html" />
<c:set var="sceneMsg3Link" value="c_pers_tags.html" />

<c:choose>	
	<c:when test="${empty pageContext.request.remoteUser}">
		<c:if test="${cookie._profilesDirectoryWelcome_div.value != false}">
			<c:set var="divId" value="_profilesDirectoryWelcome_div" />
			<fmt:message key="label.directory.welcome.title" var="sceneTitle" />
			
			<div id="${divId}" class="lotusWelcomeBox" role="complementary" aria-label="${sceneTitle}">
				<%@ include file="/WEB-INF/jsps/html/common/welcome.jsp" %>
			</div>
		</c:if>
	</c:when>
	<c:otherwise>
		<c:if test="${cookie._profilesMyDirectoryWelcome_div.value != false}">
			<c:set var="divId" value="_profilesMyDirectoryWelcome_div" />
			<fmt:message key="label.directory.welcome.title.authed" var="sceneTitle" />
			
			<div id="${divId}" class="lotusWelcomeBox" role="complementary" aria-label="${sceneTitle}">
				<%@ include file="/WEB-INF/jsps/html/common/welcome.jsp" %>
			</div>
		</c:if>
	</c:otherwise>
</c:choose>		
