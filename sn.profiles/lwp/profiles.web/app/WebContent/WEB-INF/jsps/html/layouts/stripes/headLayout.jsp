<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2010                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<stripes:layout-definition>
	<title>${_pageTitle}</title>
	<stripes:layout-component name="head_header">
		<c:if test="${_header != null}">	
			<jsp:include page="${_header }" />
		</c:if>
	</stripes:layout-component>
	<stripes:layout-component name="head_contents"/>
	<stripes:layout-component name="head_footer">
		<c:if test="${_header != null}">
			<jsp:include page="${_footer }" />
		</c:if>
	</stripes:layout-component>
</stripes:layout-definition>

