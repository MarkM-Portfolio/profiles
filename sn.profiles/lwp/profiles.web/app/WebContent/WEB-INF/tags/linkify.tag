<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2011                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%--
 @author testrada 
 @description: this tag-file will turn URI's in the input attribute into HTML anchor links
 @input:  contextStr attribute, contains the text to scrub for URI's
 @output: linkifiedContentText set in request scope
--%>

<%@ tag body-content="empty" %>
<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<% 
	pageContext.setAttribute("newLineChar", "\n"); 
	pageContext.setAttribute("tabChar", "\t");
	pageContext.setAttribute("carriageReturnChar", "\r");	
%>

<%@ attribute name="contextStr" required="true" %>
<c:set var="linkifiedContentText" value="${contextStr}" scope="request" />

<%-- Turn links into clickable HTML elements --%> 
<c:forTokens var="protocol" delims=" " items="http:// https:// ftp:// file:// mailto: news:// www.">
	<c:if test="${fn:indexOf(contextStr, protocol) > -1}">
		<c:set var="tempStr" value="" />
		<c:set var="delims" value="${newLineChar}${tabChar}${carriageReturnChar} " />
		<c:forTokens var="token" delims="${delims}" items="${contextStr}" >
			<c:choose>
				<c:when test="${fn:startsWith(token, protocol)}">
					<c:choose>
						<c:when test="${protocol == 'www.'}">
							<c:set var="theLink" value="<a href='http://${token}'>${token}</a>" />
						</c:when>
						<c:otherwise>
							<c:set var="theLink" value="<a href='${token}'>${token}</a>" />
						</c:otherwise>
					</c:choose>
					<c:set var="tempStr" value="${tempStr} ${theLink}" />
				</c:when>
				<c:otherwise>
					<c:choose>
						<c:when test="${tempStr == ''}">					
							<c:set var="tempStr" value="${token}" />
						</c:when>
						<c:otherwise>
							<c:set var="tempStr" value="${tempStr} ${token}" />
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
		</c:forTokens>
		<c:set var="linkifiedContentText" value="${tempStr}" scope="request" />
	</c:if>
</c:forTokens>
