<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml;charset=UTF-8" session="false"%>
<%@ page import="com.ibm.lconn.profiles.internal.exception.AssertionException" %>
<%@ page import="com.ibm.lconn.profiles.internal.exception.AssertionType" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2010	                                       --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%
AssertionException ex = new AssertionException(AssertionType.UNAUTHORIZED_ACTION);
request.setAttribute("org.apache.struts.action.EXCEPTION", ex);
%>

<jsp:forward page="/WEB-INF/internal/atom/errorHandler.do" />