<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml;charset=UTF-8" session="false"%>
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

<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="bean"	uri="http://jakarta.apache.org/struts/tags-bean"%>
<%@ taglib prefix="html"	uri="http://jakarta.apache.org/struts/tags-html"%>
<%@ taglib prefix="logic"	uri="http://jakarta.apache.org/struts/tags-logic"%>

<error xmlns="http://www.ibm.com/xmlns/prod/sn">
	<code>
		<c:if test="${!empty profiles_statusCode}">
			<c:out value="${profiles_statusCode}"/>
		</c:if>
	</code>
	<message>
		<c:choose>
			<c:when test="${not empty lcErrorMessageOverride}">
				<c:out value="${lcErrorMessageOverride}"/>
			</c:when>
			
			<c:otherwise>
				<logic:messagesPresent message="false">
					<html:messages id="error" message="false">
						<bean:write name="error" />
					</html:messages>
				</logic:messagesPresent>
			</c:otherwise>
		</c:choose>
	</message>
	<trace>
		<c:choose>
		<c:when test="${showStackTrace}">
		<![CDATA[
		<% if (pageContext.findAttribute("org.apache.struts.action.EXCEPTION") != null) { %>
				<bean:define id="exception" name="org.apache.struts.action.EXCEPTION" type="java.lang.Exception"/>
			<% if (exception != null) { %>
				<% exception.printStackTrace(new java.io.PrintWriter(out)); %>
			<% }
		} %>
		]]>
		</c:when>
		<c:otherwise>
			OMITTED
		</c:otherwise>
		</c:choose>
	</trace>			
</error>


