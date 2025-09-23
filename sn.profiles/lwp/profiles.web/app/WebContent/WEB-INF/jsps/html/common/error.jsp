<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" session="false" %>

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

<%--
 @author sberajaw
--%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ taglib prefix="jwr" 		uri="http://jawr.net/tags"%>
<%@ taglib prefix="lc-ui" 		uri="http://www.ibm.com/lconn/tags/coreuiutil"%>


<jsp:include page="/WEB-INF/jsps/html/common/htmlStartElement.jsp"/>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<lc-ui:favicon appname="profiles" />
		<lc-ui:stylesheets xhtml="true" />
		<%-- Load JavaScript bundle --%>
		<jwr:script src="/aboutBundle.js" />
	</head>
	<body class="lotusError lotusui">
		<%@ include file="/WEB-INF/jsps/html/scenes/error/error.jsp" %>
	</body>
</html>
