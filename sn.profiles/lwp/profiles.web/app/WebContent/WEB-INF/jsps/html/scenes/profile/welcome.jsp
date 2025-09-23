<%@ page contentType="text/html;charset=UTF-8" %>
<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2016                                    --%>
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

<html:xhtml/>

<c:set var="isMyProfile" value="${key == loggedInUserKey}" />
<profiles:isLotusLive var="isLotusLive"/>
<c:choose>	
	<c:when test="${isMyProfile}">
		<c:if test="${cookie._profilesMyViewWelcome_div.value != false}">
			<c:choose>	
				<c:when test="${isLotusLive != 'true'}"> <%-- non-Smarcloud header --%>	
					<fmt:message key="label.myprofile.view.welcome.title" var="sceneTitle" />
					<fmt:message key="label.myprofile.view.welcome.msg1" var="sceneMsg1" />
					<profiles:checkPermission feature="profile.status" permission="profile.status.update" var="canUpdateStatus" />
					<c:if test="${canUpdateStatus == 'true'}">
						<fmt:message key="label.myprofile.view.welcome.msg2" var="sceneMsg2" />
					</c:if>
					<fmt:message key="label.myprofile.view.welcome.msg3" var="sceneMsg3" />
				</c:when>
				<c:otherwise>
					<fmt:message key="label.myprofile.view.smartcloud.welcome.title" var="sceneTitle" />
					<fmt:message key="label.myprofile.view.smartcloud.welcome.body" var="sceneBody" />
					<fmt:message key="label.myprofile.view.smartcloud.welcome.msg1" var="sceneMsg1" />
					
					<profiles:checkPermission feature="profile.tag" permission="profile.tag.add" checkTarget="false" var="canAddTag" />
					<c:if test="${canAddTag == 'true'}"><fmt:message key="label.myprofile.view.smartcloud.welcome.msg2" var="sceneMsg2" /></c:if>
					
					<fmt:message key="label.myprofile.view.smartcloud.welcome.msg3" var="sceneMsg3" />
					<c:set var="sceneMsg1Link" value="/welcome/welcome_get_started_set_up_profile.html" />
					<c:set var="sceneMsg2Link" value="t_pers_tag_your_profile.html" />
					<c:set var="sceneMsg3Link" value="t_people_edityourprofile.html" />
				</c:otherwise>
			</c:choose>
			<c:set var="divId" value="_profilesMyViewWelcome_div" />
			<div id="${divId }" class="lotusWelcomeBox" role="complementary" aria-label="${sceneTitle}">
				<%@ include file="/WEB-INF/jsps/html/common/welcome.jsp" %>
			</div>
		</c:if>
	</c:when>
	<c:otherwise>
		<profiles:checkPermission feature="profile.colleague" permission="profile.colleague.connect" var="canViewNetwork" />
		<c:if test="${canViewNetwork == 'true'}">
			<c:if test="${cookie._profilesViewWelcome_div.value != false}">
				<c:set var="divId" value="_profilesViewWelcome_div" />
				<fmt:message key="label.profile.view.welcome.title" var="sceneTitle" />	
				<fmt:message key="label.profile.view.welcome.msg1" var="sceneMsg1" />
				<fmt:message key="label.profile.view.welcome.msg2" var="sceneMsg2" />
				<fmt:message key="label.profile.view.welcome.msg3" var="sceneMsg3" />
				<fmt:message key="label.profile.view.welcome.msg4" var="sceneMsg4" />
				<c:set var="sceneMsg1Link" value="" />
				<c:set var="sceneMsg2Link" value="" />
				<c:set var="sceneMsg3Link" value="" />
				<c:set var="sceneMsg4Link" value="t_pers_follow.html" />
				<div id="${divId }" class="lotusWelcomeBox" role="complementary" aria-label="${sceneTitle}">
					<%@ include file="/WEB-INF/jsps/html/common/welcome.jsp" %>
				</div>
			</c:if>
		</c:if>
	</c:otherwise>
</c:choose>
