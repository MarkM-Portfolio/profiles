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
	@author sberajaw
--%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<%
response.setHeader("Pragma","no-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);
response.addHeader("Cache-Control","private,no-store,max-stale=0");

request.setAttribute("loginPostUri", request.getContextPath() + config.getInitParameter("loginPostUriParameter"));
request.setAttribute("loginError", Boolean.valueOf(request.getParameter(config.getInitParameter("errorParameter"))));
request.setAttribute("loginUri", request.getContextPath()+config.getInitParameter("loginUriParameter"));
%>

<jsp:forward page="/nav/templates/login.jsp" />

<%-- 
<html:xhtml/>
<tags:loginform />
--%>
