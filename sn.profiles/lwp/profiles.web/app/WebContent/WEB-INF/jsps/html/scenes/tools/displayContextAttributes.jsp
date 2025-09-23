<%@ page contentType="text/html;charset=UTF-8" %>

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

<%--
	@author testrada@us.ibm.com
--%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<%@ page import="java.util.*" %>


<div class="lotusTiny">
	<table cellspacing="0"><tr><td class="lotusLast"><b><tags:buildstamp /></b></td></tr></table>
		
	<ul>
		<li>Request Method:
			<c:out value="${pageContext.request.method}" />
		</li>
		<li>Request Protocol: 
			<c:out value="${pageContext.request.protocol}" />
		</li>
		<li>Request Scheme: 
			<c:out value="${pageContext.request.scheme}" />
		</li>
		<li>Context Path: 
			<c:out value="${pageContext.request.contextPath}" />
		</li>
		<li>Servlet Path: 
			<c:out value="${pageContext.request.servletPath}" />
		</li>
		<li>Request URI: 
			<c:out value="${pageContext.request.requestURI}" />
		</li>
		<li>Request URL: 
			<c:out value="${pageContext.request.requestURL}" />
		</li>
		<li>Server Name: 
			<c:out value="${pageContext.request.serverName}" />
		</li>
		<li>Server Port: 
			<c:out value="${pageContext.request.serverPort}" />
		</li>
		<li>Remote Address: 
			<c:out value="${pageContext.request.remoteAddr}" />
		</li>
		<li>Remote Host: 
			<c:out value="${pageContext.request.remoteHost}" />
		</li>
		<li>Secure: 
			<c:out value="${pageContext.request.secure}" />
		</li>
		<li>Cookies:<br />
			<c:forEach items="${pageContext.request.cookies}" var="c"> 
				<b><c:out value="${c.name}" /></b>:
				<c:out value="${c.value}" /><br />
			</c:forEach>
		</li>
		<li>Headers:<br />
			<c:forEach items="${headerValues}" var="h"> 
		    	<b><c:out value="${h.key}" /></b>:
				<c:forEach items="${h.value}" var="value">
					<br /><c:out value="${value}" />
				</c:forEach>
				<br />
			</c:forEach>
		</li>
	</ul>

	
<%
    int scope = PageContext.REQUEST_SCOPE;
	Enumeration e = pageContext.getAttributeNamesInScope(scope);
    out.println("<b>REQUEST SCOPE ATTRIBUTES:</b>"+"<br/>");
    while (e.hasMoreElements()) {
        String a = (String)e.nextElement();
        out.println(a + " = " + pageContext.getAttribute(a,scope) +"<br/>");
    }
    
    scope = PageContext.SESSION_SCOPE;
    e = pageContext.getAttributeNamesInScope(scope);
    out.println("<b>SESSION SCOPE ATTRIBUTES:</b>"+"<br/>");
    while (e.hasMoreElements()) {
        String a = (String)e.nextElement();
        out.println(a + " = " + pageContext.getAttribute(a,scope) +"<br/>");
    }

    scope =  PageContext.PAGE_SCOPE;
    e = pageContext.getAttributeNamesInScope(scope);
    out.println("<b>PAGE SCOPE ATTRIBUTES:</b>"+"<br/>");
    while (e.hasMoreElements()) {
         String a = (String)e.nextElement();
         out.println(a + " = " + pageContext.getAttribute(a,scope) +"<br/>");
     }
    
    scope = PageContext.APPLICATION_SCOPE;
    e = pageContext.getAttributeNamesInScope(scope);
    out.println("<b>APPLICATION SCOPE ATTRIBUTES:</b>"+"<br/>");
    while (e.hasMoreElements()) {
        String a = (String)e.nextElement();
        out.println(a + " = " + pageContext.getAttribute(a,scope) +"<br/>");
    }

%>
</div>

