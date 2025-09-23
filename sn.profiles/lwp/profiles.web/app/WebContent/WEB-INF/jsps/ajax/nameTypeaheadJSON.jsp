<%@ page contentType="application/x-javascript; charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2012                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>
<%-- 
 @author badebiyi
--%>
<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="profiles"	  	uri="/WEB-INF/tlds/profiles.tld" %>
<%-- NOTE: the reason this code is unindented is to relieve transmitting unecessary blank space on the http response --%>
/*{"identifier":"member","label":"name","items":[
<c:forEach var="p" items="${profiles}" varStatus="i">{"name":"${profiles:encodeForJsonString(p.displayName)}","userid":"${p[lconnUserIdAttr]}","uid":"${profiles:encodeForJsonString(p.uid)}"<c:if test="${showEmail}">,"member":"${profiles:encodeForJsonString(p.email)}"</c:if>,"type":"0"<c:if test="${extended}">,"ext":
{"first":""<c:forEach items="${p}" var="p2" varStatus="n"><c:choose><c:when test="${p2.key == 'email'}"><c:if test="${showEmail}">,"${p2.key}":"${profiles:encodeForJsonString(p2.value)}"</c:if></c:when><c:otherwise><c:if test="${not empty p2.value}">,"${p2.key}":"${profiles:encodeForJsonString(p2.value)}"</c:if></c:otherwise></c:choose></c:forEach>}
</c:if>}<c:if test="${not i.last}">,
</c:if></c:forEach>]}*/
