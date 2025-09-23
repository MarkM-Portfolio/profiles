<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2013                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%-- 5724-S68                                                          --%>
<%@ tag body-content="empty" %>

<%@ attribute name="profiles" required="true" rtexprvalue="true" type="java.util.AbstractList" %>

<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 	uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 		uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 	uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 	uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="tags"	uri="/WEB-INF/tags" %>

<!-- stackTrace -->
<div id="stackTrace" style="display:none">
	<c:set var="exception" value="${pageContext.exception}" />
	<b> ${exception} </b> <br />
	<div class="indent20">
		<c:forEach var="e" items="${exception.stackTrace}">
			${e} <br />
		</c:forEach>
	</div>
</div> <!-- end stackTrace -->
