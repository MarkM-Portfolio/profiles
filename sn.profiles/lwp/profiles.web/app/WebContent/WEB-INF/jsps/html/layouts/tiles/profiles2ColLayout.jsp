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
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>


<jsp:include page="/WEB-INF/jsps/html/common/htmlStartElement.jsp"/>
	<head>
		<tiles:useAttribute name="pageTitleKey" scope="request" />
		<title><fmt:message key="${pageTitleKey}" /></title>
		<tiles:insert attribute="frameworkHeader" />
		<tiles:useAttribute name="jsIncludes" scope="request" />
		<tiles:insert attribute="frameworkFooter" />
	</head>
	<body class="<tiles:getAsString name="bodyclass" ignore="true" />">
       	<jsp:include page="/WEB-INF/jsps/html/scenes/common.jsp" />
		<span id="debugArea"></span>
		<tiles:useAttribute name="editableClass" scope="session" />
		<div id="lotusFrame" class="<tiles:getAsString name="frameclass" ignore="true" />">
			<div role="banner">
				<tiles:insert attribute="sceneBanner" />
				<tiles:insert attribute="sceneHeader" />
			</div>
			<div id="lotusMain" class="<tiles:getAsString name="mainclass" ignore="true" />" >
				<tiles:insert attribute="leftCol" />
				<tiles:insert attribute="scene" />
			</div>
			<tiles:useAttribute name="contextualHelpList" scope="request" />
			<c:forEach items="${contextualHelpList}" var="contextualHelp">
				<tiles:insert page="${contextualHelp}" />
			</c:forEach>
			<tiles:insert attribute="sceneFooter" />
		</div>
	</body>
</html>
