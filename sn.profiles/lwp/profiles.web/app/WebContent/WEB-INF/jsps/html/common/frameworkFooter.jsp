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
<%@ 
page contentType="text/html;charset=UTF-8" %><%@ 
taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %><%@ 
taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %><%@ 
taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %><%@ 
taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %><%@ 
taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %><%@ 
taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %><%@ 
taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/>

<script language="Javascript" type="text/javascript">
	var generalrs = ( typeof(lc_default) != "undefined" ) ? lc_default : ""; // lc_default is set in the widget framework  
</script>

<%-- TODO: want other setting for debugging --%>
<core:semTagSvcConfig service="profiles" relativePathToProxy="/ajaxProxy" writeDojoRequires="false">
	<core:semTagProfiles/>
</core:semTagSvcConfig>

<c:choose>
	<c:when test="${pageContext.request.servletPath == '/WEB-INF/stripes/pages/app/about/aboutProfiles.jsp'}">
	</c:when>
	<c:when test="${not empty pageTitleKey && (  pageTitleKey == 'personCardDownloadVCard' || pageTitleKey == 'label.page.profiles.error' || pageTitleKey == 'label.page.profiles.login' ) }">
	</c:when>
	<c:otherwise>
		<core:PluginConfig/>
	</c:otherwise>
</c:choose>

<script language="Javascript" type="text/javascript">
	if(SemTagSvcConfig && SemTagSvcConfig.debug) SemTagSvcConfig.debug = (window.debugBizCard == true);
</script>

<%-- Custom Authentication --%>
<c:choose>
	<c:when test="${(pageContext.request.scheme eq 'https') && (CustomAuthenticationSettings.sslEnabled)}">
		<script type="text/javascript" src="${CustomAuthenticationSettings.sslAuthJSUrl}"></script>
	</c:when>
	<c:when test="${(CustomAuthenticationSettings.enabled)}">
		<script type="text/javascript" src="${CustomAuthenticationSettings.authJSUrl}"></script>
	</c:when>
</c:choose>
				
<c:if test="${(CustomAuthenticationSettings.enabled) || (CustomAuthenticationSettings.sslEnabled)}">
	<script type="text/javascript">
	    var CUSTOM_AUTH_JS_CLASS = "${CustomAuthenticationSettings.authJSClassName}";
	</script>   
</c:if>

<%-- END Custom Authentication --%>
<c:if test="${param.invite}">
	<script type="text/javascript">
		dojo.addOnLoad(
    		function(){
				var temp333 = document.getElementById("inputProfileActionAddColleague");
				if(temp333 != null)
					temp333.click();
    		}
		);
	</script>  
</c:if>