<%@ page contentType="text/html;charset=UTF-8" %>

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

<%@ taglib prefix="c"			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"			uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html"		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ taglib prefix="bidi"		uri="http://www.ibm.com/lconn/tags/bidiutil" %>
<%@ taglib prefix="coreComp"	uri="http://www.ibm.com/lconn/tags/components" %>
<%@ taglib prefix="jwr"			uri="http://jawr.net/tags" %>
<%@ taglib prefix="lc-ui"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<lc-ui:favicon appname="profiles" />
<lc-ui:stylesheets xhtml="true" />

<script type="text/javascript">
<%--	
	<c:if test="${param.debug != null}">
		//-------------------------------------------[  Debug URL Parameter Configuration ] 
		// runtime global js debug flags
		debugger;
		var debugMode = true;
		var debugWidgets = ("${fn:contains(param.debug,'widgets')}"=="true"?true:false);
		var debugSand = ("${fn:contains(param.debug,'sand')}"=="true"?true:false);
		var debugDojo = ("${fn:contains(param.debug,'dojo')}"=="true"?true:false);
		var debugMuM = ("${fn:contains(param.debug,'mum')}"=="true"?true:false);
		var debugComm = ("${fn:contains(param.debug,'communities')}"=="true"?true:false);
		var debugProfiles = ("${fn:contains(param.debug,'profiles')}"=="true"?true:false);
		var debugBizCard = ("${fn:contains(param.debug,'bizCard')}"=="true"?true:false);
		alert("DEBUG MODE: <lc-ui:escapeJavaScript>${param.debug}</lc-ui:escapeJavaScript>");
	</c:if>
--%>

	//-------------------------------------------[  General JS Variable Configuration ] 
	<lc-ui:serviceLink serviceName="profiles" var="profilesSvcHref"/>
	<lc-ui:serviceLink serviceName="help" var="helpSvcHref" />
		
	<%@ include file="/WEB-INF/jsps/html/common/profileData.jsp" %>
	<%-- javascript object setup is done here. javascript calls, are made in lconn.profiles.init() --%>
	var appName = "${fn:escapeXml("profile")}";<%-- Need to use fn:escapeXml() to prevent --%>

	var svcHrefProfiles = "${profilesSvcHref}";
	var applicationContext = "${pageContext.request.contextPath}";

	var svcHrefHelp = "${helpSvcHref}";
	var appCtxHelp = svcHrefHelp.substring(svcHrefHelp.lastIndexOf("/"));

	// alert("profiles: \n["+applicationContext+"]\n["+svcHrefProfiles+"]\n\n"+"help: \n["+appCtxHelp+"]\n["+svcHrefHelp+"]\n");
	var xsltPath = applicationContext + "/static/xslt/" + ${applicationScope.appChkSum}; 
	var profileSearchType = "${searchType}";
	var profiles_isBidiRTL = ('RTL' == '<c:if test="${bidi}">RTL</c:if>' ? true : false); // RTL (bidi?);
	var bShowEmail = ('showMail' == '<c:if test="${showEmail}">showMail</c:if>' ? true : false); // show/hide email
	var appLang = "<profiles:appLang />";
	var helpLang = appLang; // "<profiles:helpLang />";
</script>
<lc-ui:dojo include="lconn.profiles.profilesApp">
	djConfig.parseOnLoad = false;

	ibmConfig.portalURI = applicationContext + "/mashupmaker/enabler";
	ibmConfig.proxyURL = '${profilesSvcHref}/ajaxProxy/';
	ibmConfig.loadServices = false;
	ibmConfig.loadingHTML = '<img src="<lc-ui:blankGif />" height="130" width="0"></img>';
</lc-ui:dojo>
<script type="text/javascript">
require(["dojo/parser","dojo/domReady"], function(parser){
	parser.parse();
});
</script>
<script>	
	var profilesGlobalServices = <coreComp:componentsReferenceTag />; // WhiteList Helper Code
		
	//-------------------------------------------[  Sametime Configuration  ] 
	var sametimeAwarenessConfig = {	
		secureUse: <c:choose><c:when test="${sametimeAwareness == true && pageContext.request.secure == true}">true</c:when><c:otherwise>false</c:otherwise></c:choose>,
		unsecureUse: <c:choose><c:when test="${sametimeAwareness == true && pageContext.request.secure != true}">true</c:when><c:otherwise>false</c:otherwise></c:choose>,
		secureURL:<c:choose><c:when test="${not empty sametimeSecureHref}">'<c:out value="${sametimeSecureHref}"/>'</c:when><c:otherwise>''</c:otherwise></c:choose>,
		unsecureUrl: <c:choose><c:when test="${not empty sametimeUnSecureHref}">'<c:out value="${sametimeUnSecureHref}"/>'</c:when><c:otherwise>''</c:otherwise></c:choose>,
		inputType: <c:choose><c:when test="${not empty sametimeInputType}">'<c:out value="${sametimeInputType}"/>'</c:when><c:otherwise>'email'</c:otherwise></c:choose>
	};
</script>
