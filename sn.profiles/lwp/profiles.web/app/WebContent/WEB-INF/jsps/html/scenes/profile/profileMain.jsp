<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- HCL Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright HCL Technologies Limited 2001, 2022                     --%>
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
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper"%>

<html:xhtml/>

<c:set var="canEditPhoto" value="${ (key == loggedInUserKey) && canUpdatePhoto }" />
<% 
	String uri = (String)request.getAttribute("javax.servlet.forward.request_uri");
	request.setAttribute("requestURI", uri);
%>

<c:set var="photoKey" value="${key }" />
<%-- Hides photo for inactive user
<c:if test="${!isActive && !empty isActive}">
	<c:set var="photoKey" value="inactive" />
</c:if>	
--%>
<c:url var="photoUrl" value="/photo.do">
	<c:param name="key" value="${photoKey}"/>
	<c:param name="lastMod" value="${lastPhotoUpdate}"/>
</c:url>

<c:set var="isNewUI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>"/>

<style>
	.editlbl:before {
		background: url("data:image/svg+xml;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' class='MuiSvgIcon-root' focusable='false' viewBox='0 0 32 32' aria-hidden='true' role='presentation' data-mui-test='editIcon' style='font-size: 1.4em;fill: %2301539B;'%3E%3Cpath style='stroke: %2301539B;' d='M2 27h28v2H2zM25.41 9a2 2 0 0 0 0-2.83l-3.58-3.58a2 2 0 0 0-2.83 0l-15 15V24h6.41zm-5-5L24 7.59l-3 3L17.41 7zM6 22v-3.59l10-10L19.59 12l-10 10z'%3E%3C/path%3E%3C/svg%3E") center / contain no-repeat;
	}
</style>
<c:set var="isMyProfile" value="${key == loggedInUserKey}" />
<c:choose>
	<c:when test="${isMyProfile}">
		<fmt:message key="label.page.profiles.myprofile" var="pageTitle" />
	</c:when>
	<c:otherwise>
		<fmt:message key="label.page.profiles.profile" var="pageTitle"><fmt:param value="${displayName}"/></fmt:message>
	</c:otherwise>
</c:choose>

<jsp:useBean id="now" class="java.util.Date" />
<script type="text/javascript">
	var pageTime = ${now.time};
	document.title = '<core:escapeJavaScript><c:out value="${pageTitle}" escapeXml="false"/></core:escapeJavaScript>';
	profilesData.config.pageId = "profilesView"; 
</script>

<c:choose>
	<c:when test='${isNewUI}'>
		<div>
			<div class="cnx8ui-profile-info">
				<img id="imgProfilePhoto" class="usersRadius" src="${photoUrl}" alt="<fmt:message key="label.editprofile.photo.editPhoto"/> - <c:out value="${displayName}"/>"  class="lconnProfilePortrait"/>
				<div id="profileHeader" class="profileHeader">
					<jsp:include page="businessCardInfo.jsp" />
				</div>
			</div>
			<c:if test="${isMyProfile}">
				<div id="editProfileButtonDiv">
					<label class="editlbl">
						<input id="editProfileButton" class="editBtn" type="button" value="<fmt:message key="label.header.editprofile"/>" 
					onclick="profiles_goto('<c:url value="/html/editMyProfileView.do" />', true);"
					/></label>
				</div>
			</c:if>
			<div id="profileBody" class="lotusContent" >
				<div id="profileInfoMsgDiv" class="lotusHidden" role="alert"></div>
				<div id="profileDetails" role="main">
					<div id="centerWidgetContainer">
						<span id="widget-container-col2" class="widgetContainer"></span>
					</div>
				</div>
			</div>
		</div>
	</c:when>
	<c:otherwise>
		<div id="profileBody" class="lotusContent" >
			<jsp:include page="/WEB-INF/jsps/html/scenes/profile/welcome.jsp" />
			<div id="profileInfoMsgDiv" class="lotusHidden" role="alert"></div>
			<div id="profileDetails" role="main">
				<div id="profileHeader" class="profileHeader">
					<jsp:include page="businessCardInfo.jsp" />
				</div>
				<div id="centerWidgetContainer">
					<span id="widget-container-col2" class="widgetContainer"></span>
				</div>
			</div>
		</div>
	</c:otherwise>
</c:choose>

<script language="javascript">
// metrics variables, it will be used in tiles/footer.jsp
	readTrackerMetrics = {};  
	readTrackerMetrics.itemType = "ProfilesView";
	if(typeof profilesData.displayedUser == "undefined"){
		readTrackerMetrics.contentId = profilesData.config.pageId;
		readTrackerMetrics.contentTitle = profilesData.config.pageId;
	} else {
		readTrackerMetrics.contentId = profilesData.displayedUser.userid;
		readTrackerMetrics.contentTitle = profilesData.displayedUser.displayName;
	}
</script>
