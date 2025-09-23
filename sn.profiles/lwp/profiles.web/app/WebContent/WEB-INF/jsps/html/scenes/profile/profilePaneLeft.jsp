<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- HCL Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright HCL Technologies Limited 2001, 2021                     --%>
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
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper"%>

<c:set var="isMyProfile" value="${key == loggedInUserKey}" />

<html:xhtml/>

<c:set var="isNewUI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>"/>

<c:if test="${!isNewUI}">
	<div id="profilePaneLeft" class="lotusColLeft">
		<jsp:include page="businessCardInfoSmall.jsp" />
		<c:if test="${isMyProfile && isActive && fn:indexOf(requestURI,'/editMyProfileView.do') lt 0}">
			<div class="lotusSection2" role="complementary" aria-label="<fmt:message key="label.header.editmyprofile"/>">
				<div id="editProfileButtonDiv" class="lotusChunk lotusCenter">
					<input id="editProfileButton" 
						style="margin-left: 0; margin-right: 0"
						type="button" 
						value="<fmt:message key="label.header.editmyprofile"/>" 
						class="lotusFormButton" 
						onclick="profiles_goto('<c:url value="/html/editMyProfileView.do" />', true);"
					/>
				</div>
			</div>
		</c:if>
		<c:if test="${isActive}">
			<div class="lotusChunk" style="padding-top: 20px;">
				<span id="widget-container-col1" class="widgetContainer"></span>
			</div>
		</c:if>
		<c:if test="${!isActive}">
			<span id="widget-container-col1-InactiveUser" class="widgetContainer"></span>
		</c:if>
	</div>
</c:if>
