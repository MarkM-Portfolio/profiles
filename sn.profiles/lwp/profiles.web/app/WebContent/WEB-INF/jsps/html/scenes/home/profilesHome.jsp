<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- HCL Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IHCL Technologies Limited 2001, 2021                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%--
	@author badebiyi
--%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="stripes" 	uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper" %>

<html:xhtml/>
<c:set var="isNewUI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>"/>

<fmt:message key="label.page.profiles.home" var="pageTitle"/>
<stripes:layout-render 
	name="/WEB-INF/jsps/html/layouts/stripes/profilesLayout.jsp" 
	pageTitle="${pageTitle }">
	
    <stripes:layout-component name="main_content">
		<jsp:useBean id="now" class="java.util.Date" />
		<script type="text/javascript">
			var pageTime = ${now.time};
			profilesData.config.pageId = "searchView";
		</script>
		<div class="lotusContent" role="main">
			<div id="profileInfoMsgDiv" class="lotusHidden" role="alert"></div>
			<a id="mainContent" name="mainContent"></a><!-- skip links for accessibility -->
			<% if(request.getParameter("oldDirectory") != null && request.getParameter("oldDirectory").equals("true")) { %>
				<jsp:include page="/WEB-INF/jsps/html/scenes/home/welcome.jsp" />
				<jsp:include page="/WEB-INF/jsps/html/scenes/search/profileSearch.jsp" />
				<jsp:include page="/WEB-INF/jsps/html/scenes/search/profilesAdvancedSearch.jsp" />
			<% } else { %>
				<profiles:isAdvancedSearchEnabled var="isAdvancedSearchEnabled"/>
				<script type="text/javascript">
					var div = [];
					if(dojo.isIE <= 8) {
						div = document.querySelectorAll(".lotusContent")[0];
					} else {
						div = document.getElementsByClassName("lotusContent")[0];
					}
					dojo.addClass(div, "newDirectory");
					dojo.addOnLoad(function() {
						var directory = new lconn.profiles.directory.DirectoryController({allowAdvancedSearch: ${isAdvancedSearchEnabled}}, "lconn_DirectoryController");
						directory.startup();
					});
				</script>
				<div id="lconn_DirectoryController">
					<jsp:include page="/WEB-INF/jsps/html/scenes/home/welcome.jsp" />
					<jsp:include page="/WEB-INF/jsps/html/scenes/search/profilesAdvancedSearch.jsp" />
				</div>
			<% } %>
		</div>
    </stripes:layout-component>

    <stripes:layout-component name="left_content">
		<c:if test="${!isNewUI}">
			<div id="profilePaneLeft" class="lotusColLeft lotusHidden">
				<span id="widget-container-col1" class="widgetContainer"></span>
			</div>
		</c:if>
	</stripes:layout-component>

    <stripes:layout-component name="right_content">
		<c:choose>
			<c:when test="${isNewUI}">
				<div id="profilePaneRight" class="lotusColRight">
					<span id="widget-container-col1" class="widgetContainer widgetStyling"></span>
				</div>
			</c:when>
			<c:otherwise>
				<div id="profilePaneRight" class="lotusColRight lotusHidden">
					<span id="widget-container-col3" class="widgetContainer"></span>
				</div>
			</c:otherwise>
		</c:choose>
	</stripes:layout-component>
</stripes:layout-render>
