<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2014                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%
try {
	String redirectTo = config.getInitParameter("redirectTo");
	if (redirectTo == null) {
		response.sendRedirect(request.getContextPath());
	} else {
		response.sendRedirect(request.getContextPath() + redirectTo);
	}
} catch (Exception e) {
	response.sendRedirect(request.getContextPath());
}
%>