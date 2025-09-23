<%@ page contentType="text/html;charset=UTF-8" language="java" %>

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
<%@ taglib prefix="stripes"		uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld"%>


<%-- Overridable  Defaults --%> 
<c:if test="${bodyClass == null}">  <c:set var="bodyClass"   value="lotusui lotusui30dojo lotusui30_body lotusui30_fonts lotusui30 lotusAbout lotusSpritesOn" /></c:if>
<c:if test="${mainClass == null}">  <c:set var="mainClass"   value="lotusMain" /></c:if>
<c:if test="${sceneBanner == null}"><c:set var="sceneBanner" value="/WEB-INF/jsps/html/scenes/banner.jsp" /></c:if>
<c:if test="${sceneHeader == null}"><c:set var="sceneHeader" value="/WEB-INF/jsps/html/scenes/header.jsp" /></c:if>
<c:if test="${sceneFooter == null}"><c:set var="sceneFooter" value="/WEB-INF/jsps/html/scenes/footer.jsp" /></c:if>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
			<div role="banner">
				<stripes:layout-component name="body_banner">
					<jsp:include page="${sceneBanner}"/>
				</stripes:layout-component>
				<stripes:layout-component name="body_header">
					<jsp:include page="${sceneHeader }"/>
				</stripes:layout-component>
			</div>
			<div id="lotusMain" class="${mainClass }" >
				<stripes:layout-component name="contents"/>
			</div>
			<stripes:layout-component name="body_footer">
				<jsp:include page="${sceneFooter }"/>
			</stripes:layout-component>
        </body>
    </html>
</stripes:layout-definition>

