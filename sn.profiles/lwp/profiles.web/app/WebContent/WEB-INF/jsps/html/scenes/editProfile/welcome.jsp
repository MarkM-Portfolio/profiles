<%@ page contentType="text/html;charset=UTF-8" %>
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

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/>
<c:if test="${cookie._profilesEditWelcome_div.value != false}">
	<c:set var="divId" value="_profilesEditWelcome_div" />
	<fmt:message key="label.myprofile.edit.welcome.title" var="sceneTitle" />
	<fmt:message key="label.myprofile.edit.welcome.msg1" var="sceneMsg1" />
	<fmt:message key="label.myprofile.edit.welcome.msg2" var="sceneMsg2" />
	
	<profiles:checkPermission feature="profile.tag" permission="profile.tag.add" checkTarget="false" var="canAddTag" />
	<c:if test="${canAddTag == 'true'}"><fmt:message key="label.myprofile.edit.welcome.msg3" var="sceneMsg3" /></c:if>
	
	<c:set var="sceneMsg1Link" value="t_pers_edit_profiles.html" />
	<c:set var="sceneMsg2Link" value="t_pers_edit_profiles.html" />
	<c:set var="sceneMsg3Link" value="c_pers_tags.html" />
			
	<div id="${divId }" class="lotusWelcomeBox" role="complementary" aria-label="${sceneTitle}">
		<%@ include file="/WEB-INF/jsps/html/common/welcome.jsp" %>
	</div>
</c:if>
