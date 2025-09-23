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

<html:xhtml/>

<%-- This is a bundle for core that has correct 'Log In', 'Log Out' names --%>
<fmt:setBundle basename="com.ibm.lconn.core.web.ui.resources.resources" var="coreResources"/>

<c:choose>
	<c:when test="${displayName != null}">
		<li class="lotusFirst" id="lotusHeaderUserText">
			<span id="lotusHeaderUserName">
				${displayName}
			</span>
			<html:hidden property="distinguishedName" styleId="distinguishedName" value="${distinguishedName}" />
			<html:hidden property="ltpaToken" styleId="ltpaToken" value="${cookie.LtpaToken.value}" />
		</li>
		<li class="lotusDivide" id="lotusHeaderHelpLi">
			<html:link href="javascript:openHelpWindow();"  styleId="headerHelpLink">
				<fmt:message bundle="${coreResources}" key="label.header.help" />
			</html:link>
		</li>	
		<li class="lotusDivide" id="logoutLi">
			<a href="javascript:;" id="logoutLink"><fmt:message bundle="${coreResources}" key="label.header.logout" /></a>
		</li>
	</c:when>
	<c:otherwise>
		<li class="lotusFirst" id="lotusHeaderUserText" style="display:none">
			<span id="lotusHeaderUserName">
				<em>unknown user</em>
			</span>
		</li>
		<li class="lotusDivide" id="lotusHeaderHelpLi">
			<html:link href="javascript:openHelpWindow();"  styleId="headerHelpLink">
				<fmt:message bundle="${coreResources}" key="label.header.help" />
			</html:link>
		</li>		
		<li class="lotusDivide" id="loginLi">
			<a href="javascript:;" id="logoutLink"><fmt:message bundle="${coreResources}" key="label.header.login" /></a>
		</li>
	</c:otherwise>
</c:choose>
