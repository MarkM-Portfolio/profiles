<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2013                                    --%>
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
<%@ taglib prefix="bidi" 		uri="http://www.ibm.com/lconn/tags/bidiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ taglib prefix="lc-ui"       uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<bidi:direction />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html lang="<profiles:appLang />" dir="<c:choose><c:when test="${bidi}">RTL</c:when><c:otherwise>LTR</c:otherwise></c:choose>">
	<head>
    	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<bidi:direction />
    	<lc-ui:favicon appname="profiles" />
    	<title><fmt:message key="label.page.profiles.about.demomovie" /></title>
    	<script type="text/javascript">
    		var helpLang = "<profiles:helpLang />";
    	</script>
    	<script type="text/javascript" src="<c:url value="/static/dojo_1.4/${applicationScope.appChkSum}/dojo/dojo.js" />"></script>
    	<script type="text/javascript" src="<c:url value="/static/javascript/${applicationScope.appChkSum}/profiles_help.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/javascript/${applicationScope.appChkSum}/profiles_behaviours.js" />"></script>
	</head>
	<center>
		<body bgcolor="#ffffff">
			<script text="text/javascript">
				var applicationContext = "${pageContext.request.contextPath}";
			</script>

			<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="${pageContext.request.scheme}://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=5,0,0,0" width="800" height="612" id="profilesDemoMovieObject">
				<param name="movie" value="<c:url value="/demo_movie/profiles_demo.swf" />">
				<param name="quality" value="high">
				<param name="bgcolor" value="#ffffff">
            	<embed src="<c:url value="/demo_movie/profiles_demo.swf" />" quality="high" bgcolor="#ffffff" width="800" height="612" type="application/x-shockwave-flash" pluginspage="http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash">
			</object>
    		<p>
    			<html:link href="#" styleClass="lotusAction demo_transcript_link"> <fmt:message key="label.about.profiles.demo.transcriptlink" /> </html:link>
    		</p>
		</body>
	</center>
</html>
