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

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/>
<c:set var="isMyProfile" value="${key == loggedInUserKey}" />
<c:choose>	
	<c:when test="${isMyProfile}">
		<c:if test="${cookie._profilesMyNetworkWelcome_div.value != false}">
			<c:set var="divId" value="_profilesMyNetworkWelcome_div" />
			<fmt:message key="label.mynetwork.welcome.title" var="sceneTitle" />

			<c:choose>
				<c:when test="${param.widgetId == 'friends'}">
					<c:choose>
						<c:when test="${param.action == 'in'}">
							<fmt:message key="label.mynetwork.welcome.invitation.msg1" var="sceneMsg1" />
							<fmt:message key="label.mynetwork.welcome.invitation.msg2" var="sceneMsg2" />
							<fmt:message key="label.mynetwork.welcome.invitation.msg3" var="sceneMsg3" />	
							<c:set var="sceneMsg1Link" value="" />
							<c:set var="sceneMsg2Link" value="" />
							<c:set var="sceneMsg3Link" value="t_pers_add_colleagues.html" />
						</c:when>
						<c:otherwise>
							<fmt:message key="label.mynetwork.welcome.msg1" var="sceneMsg1" />
							<fmt:message key="label.mynetwork.welcome.msg2" var="sceneMsg2" />
							<fmt:message key="label.mynetwork.welcome.msg3" var="sceneMsg3" />
							<fmt:message key="label.mynetwork.welcome.msg4" var="sceneMsg4" />
							<c:set var="sceneMsg1Link" value="" />
							<c:set var="sceneMsg2Link" value="t_pers_add_colleagues.html" />
							<c:set var="sceneMsg3Link" value="" />
							<c:set var="sceneMsg4Link" value="" />
						</c:otherwise>
					</c:choose>
				</c:when>
				
				<c:when test="${param.widgetId == 'follow'}">
					<c:choose>
						<c:when test="${param.action == 'in'}">
							<fmt:message key="label.mynetwork.welcome.following.msg1" var="sceneMsg1" />
							<fmt:message key="label.mynetwork.welcome.following.msg2" var="sceneMsg2" />
							<fmt:message key="label.mynetwork.welcome.following.msg3" var="sceneMsg3" />
							<c:set var="sceneMsg1Link" value="" />
							<c:set var="sceneMsg2Link" value="t_pers_follow.html" />
							<c:set var="sceneMsg3Link" value="" />
						</c:when>
						<c:otherwise>
							<fmt:message key="label.mynetwork.welcome.followers.msg1" var="sceneMsg1" />
							<fmt:message key="label.mynetwork.welcome.followers.msg2" var="sceneMsg2" />
							<c:set var="sceneMsg1Link" value="" />
							<c:set var="sceneMsg2Link" value="" />
						</c:otherwise>
					</c:choose>
				</c:when>
			</c:choose>
			
			<div id="${divId }" class="lotusWelcomeBox" role="complementary" aria-label="${sceneTitle}">
				<%@ include file="/WEB-INF/jsps/html/common/welcome.jsp" %>
			</div>
		</c:if>
	</c:when>
	<c:otherwise>
		<c:if test="${cookie._profilesNetworkWelcome_div.value != false}">
			<c:set var="divId" value="_profilesNetworkWelcome_div" />
			<fmt:message key="label.network.welcome.title" var="sceneTitle" />
			<c:set var="sceneBody">
				<fmt:message key="label.network.welcome.body"/>&nbsp;
				<fmt:message key="label.welcome.clickPerson"/>
			</c:set>
			<c:set var="learnMoreLink" value="javascript:openHelpWindow('','c_pers_profiles.html')" />
			<div id="${divId }" class="lotusWelcomeBox" role="complementary" aria-label="${sceneTitle}">
				<%@ include file="/WEB-INF/jsps/html/common/welcome.jsp" %>
			</div>
		</c:if>
	</c:otherwise>
</c:choose>					
