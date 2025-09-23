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

<%-- 	@author sberajaw --%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper" %>

<html:xhtml/>

<c:set var="isNewUI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>"/>

<c:if test="${isNewUI}">
	<div id="profilePaneRight" class="lotusColRight lotusHidden">
		<c:if test="${isActive}">
			<span id="widget-container-col3" class="widgetContainer"></span>
		</c:if>
		<c:if test="${!isActive}">
			<span id="widget-container-col3-InactiveUser" class="widgetContainer"></span>
		</c:if>
		<div class="lotusChunk" style="padding-top: 20px;">
			<span id="widget-container-col1" class="widgetContainer"></span>
		</div>
	</div>
</c:if>
<div id="profilePaneRight" class="lotusColRight lotusHidden" >
	<c:if test="${isActive}">
		<span id="widget-container-col3" class="widgetContainer"></span>
	</c:if>
	<c:if test="${!isActive}">
		<span id="widget-container-col3-InactiveUser" class="widgetContainer"></span>
	</c:if>
</div>
