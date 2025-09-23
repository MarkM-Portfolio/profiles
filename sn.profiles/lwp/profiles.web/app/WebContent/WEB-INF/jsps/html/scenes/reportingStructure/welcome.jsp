<%@ page contentType="text/html;charset=UTF-8" %>
<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2012                                    --%>
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
<c:if test="${cookie._profilesReportChainWelcome_div.value != false}">
	<c:set var="isMyProfile" value="${key == loggedInUserKey}" />
	<c:set var="divId" value="_profilesReportChainWelcome_div" />
	<fmt:message key="label.reportchain.welcome.title" var="sceneTitle" />
	<c:set var="sceneBody">
		<fmt:message key="label.reportchain.welcome.body"/>&nbsp;
		<fmt:message key="label.welcome.clickPerson"/>
	</c:set>
	<div id="${divId }" class="lotusWelcomeBox" role="complementary" aria-label="${sceneTitle}">
		<%@ include file="/WEB-INF/jsps/html/common/welcome.jsp" %>
	</div>
</c:if>
