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
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<core:serviceLink serviceName="profiles" 
	secure="false" 
	var="profilesUrl"/>
<c:redirect url="${profileUrl}/home.do">
	<c:choose>
		<c:when test="${!empty pageContext.request.locale}">
			<c:set var="appLang">
				<profiles:appLang />
			</c:set>
			<c:param name="lang">
				${fn:toLowerCase(appLang)}
			</c:param>
		</c:when>
		<c:otherwise>
			<c:param name="lang" value="en_us"/>
		</c:otherwise>
	</c:choose>
</c:redirect>
