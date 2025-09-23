<%@ page contentType="text/html;charset=UTF-8" %>
<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- HCL Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright HCL Technologies Limited 2001, 2022                     --%>
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
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="corefn"		uri="http://www.ibm.com/lconn/tags/corefn" %>
<%@ taglib prefix="lc-ui"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ taglib prefix="lc-cache" 	uri="http://www.ibm.com/connections/core/cache" %>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper" %>

<%
String currentTabtt = (String)request.getAttribute("currentTab");
String currentProfileUidtt = request.getParameter("uid");
String currentLoggedInUserUidtt = (String) request.getAttribute("currentLoggedInUserUid");
String inNetwork = (String)request.getAttribute("inNetwork");

Boolean canFriendB = (Boolean)request.getAttribute("canFriend");
boolean canFriend = canFriendB.booleanValue();

Boolean canFollowB = (Boolean)request.getAttribute("canFollow");
boolean canFollow = canFollowB.booleanValue();

Boolean canUnfollowB = (Boolean)request.getAttribute("canUnfollow");
boolean canUnfollow = canUnfollowB.booleanValue();

if(currentTabtt != null && currentTabtt.equals("myprofile"))
	canFriend = false;
	
else if(currentProfileUidtt != null && 
		currentLoggedInUserUidtt != null && 
		currentProfileUidtt.equals(currentLoggedInUserUidtt))
	canFriend = false;

else if ( (inNetwork != null && inNetwork.equals("true")) || !canFriend )
	canFriend = false;
	
else if ( currentLoggedInUserUidtt == null)
	canFriend = false;

// pending connection
else if (request.getAttribute("connection") != null)
	canFriend = false;
	
request.setAttribute("canFriend",canFriend);
request.setAttribute("canFollow",canFollow);
request.setAttribute("canUnfollow",canUnfollow);

java.util.Map mixinMap = new java.util.HashMap(1);
mixinMap.put("section", "jobInformation");
request.setAttribute("mixinMap", mixinMap);
%>
<html:xhtml/>
<c:set var="squot" value="'" /><c:set var="esquot" value="&#39;" />
<c:set var="email_enc" value="${fn:replace(email, squot, esquot)}" />
<c:set var="isNewUI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>"/>

<html:hidden property="profileUid" styleId="profileUid" value="${uid}" />
<html:hidden property="profileKey" styleId="profileKey" value="${key}" />
<html:hidden property="managerUid" styleId="managerUid" value="${managerUid}" />
<html:hidden property="isManager" styleId="isManager" value="${isManager}" />
<html:hidden property="lastMod" styleId="lastMod" value="${lastUpdate}" />
<html:hidden property="lastPhotoMod" styleId="lastPhotoMod" value="${lastPhotoUpdate}" />
<html:hidden property="timezoneId" styleId="timezoneId" value="${timezoneId}" />
<fmt:message key="label.profile.description.inlineedit" var="inlineEditDesc" />
<fmt:message key="label.profile.description.inlineedit.link" var="inlineEditLink" />
<html:hidden property="inlineEditDesc" styleId="inlineEditDesc" value="${inlineEditDesc}" />
<html:hidden property="label.profile.description.inlineedit.link" styleId="inlineEditLink" value="${inlineEditLink}" />

<c:set var="isMyProfile" value="${key == loggedInUserKey}" />

<jsp:include page="/WEB-INF/jsps/vcard/vcard_popup.jsp"/>
<div id="businessCardContent" class="lotusHeader">
   	<c:choose>
    	<c:when test='${!empty userid}'>
			<c:set var="vcardUserHook" value="<span class='x-lconn-userid' style='display: none;'>${userid}</span>"/>
		</c:when>
		<c:otherwise>
			<c:if test="${showEmail} ">
				<c:set var="vcardUserHook" value="<span class='email' style='display: none;'>${email_enc}</span>"/>
			</c:if>
		</c:otherwise>
	</c:choose>
	<div class='vcard lotusHeading lotusLeft'>
		<c:choose>
			<c:when test="${isNewUI}">
				<a href="${pageContext.request.contextPath}/html/profileView.do?key=${key}" class="fn url lotusPerson bidiAware cnx8ui-bizcard-name"><c:out value="${displayName}"/></a>
				${vcardUserHook}
			</c:when>
			<c:otherwise>
				<a href='${pageContext.request.contextPath}/html/profileView.do?key=${key}' class='fn url lotusPerson bidiAware'><c:out value="${displayName}"/></a>
			</c:otherwise>
		</c:choose>
		${vcardUserHook}
	</div>
	<c:if test="${hasPronunciation}">
		<div class="lotusLeft" style="margin-top:3px"> 
			<c:url value="/audio.do" var="audioUrl">
				<c:param name="key" value="${key}"/>
				<c:param name="lastMod" value="${lastUpdate}"/>
			</c:url>
			<html:link href="${audioUrl}" styleId="pronunciation" titleKey="label.profile.pronunciation">
				<c:set var="pronunciationHint"><profiles:getString key="label.associatedInformation.pronunciation" bundle="com.ibm.lconn.profiles.strings.uilabels" /></c:set>
				<img class="iconsFileTypes16 iconsFileTypes16-ftAudio16" src="<lc-ui:blankGif />" alt="${pronunciationHint}"/>
	 		</html:link>
 		</div>
	</c:if>
	
	<div id="connectionIndicator" class="lotusIndicator lotusLeft" style="margin-top:3px; padding-bottom:0px; padding-top: 0px;">
		<c:if test="${!empty connection}">
			<c:choose>
				<c:when test="${connection.status == 0}"><!-- inviter (invitation pending) -->
					<fmt:message key="personCardPendingInv" />
				</c:when>
				<c:when test="${connection.status == 2}"><!-- invitee (invitation pending) -->
					<fmt:message key="personCardPendingInv" />
				</c:when>
			</c:choose>
		</c:if>
	</div>
	
</div>
<c:choose><c:when test="${isActive}">
	<div id="businessCardDetails" class="lotusDetails lotusLeft" style=" padding-top: 3px;">
		<c:choose>
			<c:when test="${isNewUI}">
				<a href = "mailto: ${email}" class="bidiAware cnx8ui-bizcard-email">${email}</a>
			</c:when>
			<c:otherwise>
				<profiles:freemarker dataModel="${dataModel}" template="PROFILE_DETAILS" mixinMap="${mixinMap}"/>
			</c:otherwise>
		</c:choose>
		<jsp:useBean id="nowTz" class="java.util.Date" />
		<span>
			<c:choose>
				<c:when test="${isNewUI}">
					<span style="white-space: nowrap;">
				</c:when>
				<c:otherwise>
					<span style="white-space: nowrap;">
				</c:otherwise>
			</c:choose>
			<fmt:message key="label.profile.localtime" /></span>
		</span>
		<span id="time" style="white-space: nowrap;"><fmt:formatDate timeStyle="SHORT" type="time" timeZone="${timezoneId}" value="${nowTz}" /></span>
		<c:choose>
			<c:when test="${(!empty sametimeLinksSvcLocation || !empty secure_sametimeLinksSvcLocation)}">
				<br />
				<strong><span><fmt:message key="label.profile.im" /></span></strong>
						<script type="text/javascript">
							if (document.cookie.match(/LtpaToken=\w*/g) != null) {
					 			document.write('<span class="imStatus ${email}_status">');
					 			if (typeof(writeSTLinksApplet) != "undefined") 
					 				writeSametimeLink("${displayedProfile.distinguishedName}", '<core:escapeJavaScript>${displayName}</core:escapeJavaScript>', true, 'icon:yes'); 
					 			document.write('</span>');												
					 		}
					 		else {
					 			document.write('<span id="imStatus"> <fmt:message key="label.profile.im.signin" /> </span>');
					 		}
						</script>
			</c:when>	
			<c:when test="${!isMyProfile}">
				<span id="awarenessArea" style='display: none;'>
					<br />
					<strong><fmt:message key="label.profile.im" /></strong>
					<span id="StatusIMAwarenessDisplayedUser" class="IMAwarenessDisplayedUser">
						<a style="display: none;" class="fn person"><c:out value="${displayName}"/></a>
				        <span style="display: none;" class="renderType">StatusMsg</span> 
				        <span style="display: none;" class="x-lconn-userid">${userid}</span>
				        <c:if test="${showEmail}"><span style="display: none;" class="email">${email_enc}</span></c:if>
				        <span style="display: none;" class="dn">${displayedProfile.distinguishedName}</span>
				        <span style="display: none;" class="uid">${uid}</span>
				        <span id="IMcontent" class="IMContent"><img class="lotusLoading" src='<lc-cache:uri template="{oneuiRoot}/images/blank.gif?etag={version}" />' >&nbsp;<fmt:message key="loadingSTStatus" /></span>
					</span>	
				</span>
			</c:when>
		</c:choose>
	</div>
</c:when>
<c:otherwise> <%-- Inactive users --%>
	<div id="divInactiveUser" class="lotusClear lotusDetails lotusChunk">
		<div id="inactiveUserMsgDiv" role="alert"></div>
	</div>	
</c:otherwise></c:choose>

<%-- Action Buttons --%>

<div id="businessCardActions" class="lotusClear lotusDetails" style="padding-top: 10px;">
	<div class="lotusActionBar"  style="overflow:visible; height:auto;"></div>
	<div class="lotusClear"></div>
</div>

<script type="text/javascript">
(function() {
	if (dojo.exists("lconn.profiles.profilesMainPage")) {
		var initArgs = {
			appContext: '<core:escapeJavaScript>${pageContext.request.contextPath}</core:escapeJavaScript>',
			
			profile: dojo.mixin(
				dojo.clone(profilesData.displayedUser),
				{
					showEmail: ${showEmail}
				}
			),
			
			enabledPermissions: profilesData.enabledPermissions,

			connection: {
				id: '<core:escapeJavaScript>${connection.connectionId}</core:escapeJavaScript>',
				status: '<core:escapeJavaScript>${connection.status}</core:escapeJavaScript>',
				canFollow: ${canFollow},
				canUnfollow: ${canUnfollow}
			}
		};
		
		lconn.profiles.profilesMainPage.init(initArgs);
	}
})();

</script>
