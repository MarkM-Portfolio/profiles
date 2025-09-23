<%@ page contentType="text/html;charset=UTF-8" language="java" %>

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
<%@ taglib prefix="stripes"		uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld"%>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper" %>

<%-- Overridable  Defaults --%> 
<c:if test="${bodyClass == null}">  <c:set var="bodyClass"   value="lotusProfiles lotusui lotusui30dojo lotusui30_body lotusui30_fonts lotusui30 lotusSpritesOn" /></c:if>
<c:if test="${frameClass == null}"> <c:set var="frameClass"  value="lotusFrame lotusui30_layout" /></c:if>
<c:if test="${mainClass == null}">  <c:set var="mainClass"   value="lotusMain" /></c:if>
<c:if test="${sceneBanner == null}"><c:set var="sceneBanner" value="/WEB-INF/jsps/html/scenes/banner.jsp" /></c:if>
<c:if test="${sceneHeader == null}"><c:set var="sceneHeader" value="/WEB-INF/jsps/html/scenes/header.jsp" /></c:if>
<c:if test="${sceneFooter == null}"><c:set var="sceneFooter" value="/WEB-INF/jsps/html/scenes/footer.jsp" /></c:if>
<c:set var="appLang"><profiles:appLang /></c:set>
<c:set var="isNewUI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>"/>
<style>

#lotusMain .lotusMainSpacing{
	margin-top: 0px !important;
    margin-left: 0px !important;
    margin-right: 0px !important;
    min-width: auto !important;
}
</style>
<stripes:layout-definition>
	<jsp:include page="/WEB-INF/jsps/html/common/htmlStartElement.jsp"/>
        <head>
			<stripes:layout-render 
				name="/WEB-INF/jsps/html/layouts/stripes/headLayout.jsp" 
				_pageTitle="${pageTitle }" 
				_header="/WEB-INF/jsps/html/common/frameworkHeader.jsp"  
				_footer="/WEB-INF/jsps/html/common/frameworkFooter.jsp" />
        </head>
        <body class="${bodyClass }">
        	<jsp:include page="/WEB-INF/jsps/html/scenes/common.jsp" />
			<span id="debugArea"></span>
			
			<div id="lotusFrame" class="${frameClass }">        
				<div role="banner">
					<stripes:layout-component name="body_banner">
						<jsp:include page="${sceneBanner}"/>
					</stripes:layout-component>
					<stripes:layout-component name="body_header">
						<jsp:include page="${sceneHeader }"/>
					</stripes:layout-component>
				</div>
				<c:choose>
					<c:when test="${isNewUI}">
						<div id="lotusMain" class="${mainClass } " style="margin-top: 0px !important;
						margin-left: 0px !important;
						margin-right: 0px !important;
						min-width: auto !important;">
							<stripes:layout-component name="left_content"/>
							<stripes:layout-component name="right_content"/>
							<stripes:layout-component name="main_content"/>
						</div>
					</c:when>
					<c:otherwise>
						<div id="lotusMain" class="${mainClass }">
							<stripes:layout-component name="left_content"/>
							<stripes:layout-component name="right_content"/>
							<stripes:layout-component name="main_content"/>
						</div>
					</c:otherwise>
				</c:choose>
				<stripes:layout-component name="body_footer">
					<jsp:include page="${sceneFooter }"/>
				</stripes:layout-component>
			</div>
        </body>
    </html>
</stripes:layout-definition>
