<%@ page contentType="text/html;charset=UTF-8"%>

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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="lc-ui" uri="http://www.ibm.com/lconn/tags/coreuiutil"%>
<%@ taglib prefix="profiles" uri="/WEB-INF/tlds/profiles.tld"%>
<%--
Use current time to create quasi-unique ids
--%><jsp:useBean id="currentTime" class="java.util.Date" scope="page" /><%--
--%>
<c:set var="pre" value="ee_ni${currentTime.time}" />
<%-- Common prefix for global elements on this page

Possible modes:
- gadget-iframe: all css and all JS is loaded - opensocial network layer is used for Ajax (DEFAULT)
- gadget-inline: no additional css is loaded, only required JS is loaded - opensocial network layer is used for Ajax
- inline:        no additional css is loaded, only required JS is loaded

** Note: Both inline modes assume that net.jazz.ajax.xdloader is already present on the page

<%-- ** Load modules dynamically and setup code to run when loaded--%>
 <label for="${pre}_data" style="display:none;">data</label>
<textarea style="display: none;" id="${pre}_data" class="data" prefix="${pre}">${actionBean.jsonObjectEscaped}</textarea>

<div id="<c:out value="${pre}" />" class="lotusFlyoutInner eeNetworkInvite">
	<c:url var="photoUrl" value="/photo.do" context="/">
		<c:param name="key" value="${actionBean.friend.key}" />
		<c:param name="lastMod" value="${actionBean.friend.lastUpdate.time}" />
	</c:url>
	<c:url var="profileUrl" value="/html/profileView.do" context="/">
		<c:param name="key" value="${actionBean.friend.key}" />
	</c:url>
	<div>
		<c:choose>
			<c:when test="${actionBean.connection == null}">
				<div class="lotusMessage2 lotusInfo ee_ni_successMsgContainer"
					role="alert">
					<img src="<lc-ui:blankGif />"
						class="lotusIcon lotusIconMsgInfo"
						alt="<fmt:message key="information_confirmation_alt" />"
						title="<fmt:message key="information_confirmation_alt" />"
						style="margin-right: 0"> <span class="lotusAltText"><fmt:message
							key="information_confirmation_alt" /> </span> <span
						class="ee_ni_successMsg"> <fmt:message
							key="friendsInvitationDoesNotExistAnyMore">
							<fmt:param value="${actionBean.friend.displayName}"></fmt:param>
						</fmt:message> </span>
				</div>
			</c:when>
			<c:otherwise>
				<c:choose>
					<c:when
						test="${actionBean.connection.status == 1 || actionBean.connection.status == 0}">
						<div class="lotusMessage2 lotusSuccess ee_ni_successMsgContainer"
							role="alert" >
					</c:when>
					<c:otherwise>
						<div class="lotusMessage2 lotusSuccess ee_ni_successMsgContainer"
							role="alert" style="display: none;" >
					</c:otherwise>
				</c:choose>
				<img src="<lc-ui:blankGif />"
					class="lotusIcon lotusIconMsgSuccess"
					alt="<fmt:message key="information_confirmation_alt" />"
					title="<fmt:message key="information_confirmation_alt" />">
				<span class="lotusAltText"><fmt:message
						key="information_confirmation_alt" /> </span>
				<span class="ee_ni_successMsg"> <c:if
						test="${actionBean.connection.status == 1}">
						<fmt:message key="friendsAlreadyAccepted" />
					</c:if> <c:if test="${actionBean.connection.status == 0}">
						<fmt:message key="friendsAllreadyInvited" />
					</c:if> </span>
			        </div>
				<div class="lotusMessage2 ee_ni_errorMsgContainer" role="alert"
					style="display: none;">
					<img src="<lc-ui:blankGif />"
						class="lotusIcon lotusIconMsgError"
						alt="<fmt:message key="information_error_alt" />"
						title="<fmt:message key="information_error_alt" />"> <span
						class="lotusAltText"><fmt:message
							key="information_error_alt" /> </span> <span class="ee_ni_errorMsg"></span>
			</c:otherwise>
		</c:choose>
	</div>


	<div class="lotusHeader lotusChunk" id="${pre}_personHeader">	    
		<div class="lotusFlyoutImage">
			<a href="{profilesUrl}${profileUrl}" class="fn url lotusPerson" target="_blank"
				title="<fmt:message key="userActivationTitle"><fmt:param value="${actionBean.friend.displayName}"></fmt:param></fmt:message>"
				aria-label="<fmt:message key="userActivationAriaLabel"><fmt:param value="${actionBean.friend.displayName}"></fmt:param></fmt:message>">
				<img src="{profilesUrl}${photoUrl}"
				alt="<fmt:message key="userActivationTitle"><fmt:param value="${actionBean.friend.displayName}"></fmt:param></fmt:message>" /> </a>
		</div>
		<div>
			<h1 class="lotusHeading">
				<span class="vcard" id="${pre}_personVcard"> <a href="{profilesUrl}${profileUrl}"
					class="fn url lotusPerson" target="_blank"
					title="<fmt:message key="userActivationTitle"><fmt:param value="${actionBean.friend.displayName}"></fmt:param></fmt:message>"
					aria-label="<fmt:message key="userActivationAriaLabel"><fmt:param value="${actionBean.friend.displayName}"></fmt:param></fmt:message>">
						<c:out value="${actionBean.friend.displayName}" /> </a> <span
					class="x-lconn-userid" style="display: none;"><c:out
							value="${actionBean.friend.key}" /> </span> </span>
			</h1>
			<div id=${pre}_personDetails style="margin-top: 10px;">
				<table cellpadding="0" cellspacing="0" role="presentation">
					<tbody>
						<tr>
							<td><profiles:freemarker dataModel="${actionBean.dataModel}"
									template="PROFILE_DETAILS" mixinMap="${actionBean.mixinMap}" />
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
	<div>
		<div class="lotusChunk">
			<div class="lotusStatus">
				<p id=${pre}_msg></p>
			</div>
			<c:if test="${actionBean.connection != null && actionBean.connection.status == 2 }">
				<div style="margin-top: 15px; margin-bottom: 15px"
					id=${pre}_following_div>
					<c:choose>
						<c:when test="${actionBean.following}">
						<div class="lotusMeta">
							<fmt:message key="friendsAlreadyFollowing">
								<fmt:param value="${actionBean.friend.displayName}"></fmt:param>
							</fmt:message>
						</div>
						</c:when>
						<c:otherwise>
							<c:if test="${!actionBean.following && actionBean.canFollow}">
								<input id=${pre}_following class="lotusCheckBox" type="checkbox"
									value="true" name="invitation_reply_follow" checked="checked"/>
								<label for=${pre}_following class="lotusCheckBox lotusMeta">
									<fmt:message key="friendsFollowUser">
										<fmt:param value="${actionBean.friend.displayName}"></fmt:param>
									</fmt:message> <span id=${pre}_followingHelp /> </label>

							</c:if>
						</c:otherwise>
					</c:choose>
				</div>
				<%-- If status==2, the invitation is pending --%>
				<c:url var="atomUrl" value="/atom/connection.do" context="/">
					<c:param name="connectionId"
						value="${actionBean.connection.connectionId}" />
				</c:url>
				<div class="lotusBtnContainer" id="${pre}_btnCtnr">
					<button class="lotusBtn ee_ni_accept ee_ni_button" role="button"
						aria-label="<fmt:message key="friendsAcceptActionA11y"><fmt:param value="${actionBean.friend.displayName}"></fmt:param></fmt:message>"
						onclick="${pre}_accept()">
						<fmt:message key="friendsAcceptAction" />
					</button>
					<button class="lotusBtn ee_ni_ignore ee_ni_button"
						aria-label="<fmt:message key="friendsIgnoreActionA11y"><fmt:param value="${actionBean.friend.displayName}"></fmt:param></fmt:message>"
						onclick="${pre}_ignore()">
						<fmt:message key="friendsIgnoreAction" />
					</button>
				</div>
				<!--end button container-->
			</c:if>
		</div>
	</div>

	<!--end header-->
<hr />
<div class="lconnNumFollowers">
	<c:choose>
		<c:when test="${actionBean.commonFriendCount == 1}">
			<fmt:message key="friendsInCommonSingle">
				<fmt:param value="1"></fmt:param>
			</fmt:message>
		</c:when>
		<c:otherwise>
			<fmt:message key="friendsInCommonMulti">
				<fmt:param value="${actionBean.commonFriendCount}"></fmt:param>
			</fmt:message>
		</c:otherwise>
	</c:choose>
</div>
<div id="${pre}_commonFriends" class="eeCommonFriends">
	<c:forEach var="friend" items="${actionBean.commonFriends}">
		<c:url var="friendPhotoUrl" value="/photo.do" context="/">
			<c:param name="key" value="${friend.key}" />
			<c:param name="lastMod" value="${friend.lastUpdate.time}" />
		</c:url>
		<c:url var="friendProfileUrl" value="/html/profileView.do" context="/">
			<c:param name="key" value="${friend.key}" />
		</c:url>
		<div class="lotusNetworkPerson">
		  <c:if test="${not friend.active}">
		    <div class="lotusDim">
		  </c:if>
			<div class="vcard">
				<table cellspacing="0" cellpadding="0" border="0"
					role="presentation">
					<tbody>
						<tr>
							<td class="eePersonPhoto"><a
								href="{profilesUrl}${friendProfileUrl}" target="_blank"
								title="<fmt:message key="userActivationTitle"><fmt:param value="${friend.displayName}"></fmt:param></fmt:message>"
								aria-label="<fmt:message key="userActivationAriaLabel"><fmt:param value="${friend.displayName}"></fmt:param></fmt:message>">
									<img src="{profilesUrl}${friendPhotoUrl}" alt="${friend.displayName}"
									class="fn lotusPerson" /> <span
									class="x-lconn-userid" style="display: none;"><c:out
											value="${friend.key}" /> </span> </a>
							</td>
							<td style="eePersonName"><a
								href="{profilesUrl}${friendProfileUrl}" target="_blank" class="lotusPerson"
								title="<fmt:message key="userActivationTitle"><fmt:param value="${friend.displayName}"></fmt:param></fmt:message>"
								aria-label="<fmt:message key="userActivationAriaLabel"><fmt:param value="${friend.displayName}"></fmt:param></fmt:message>">
									<h4 style="vertical-align: top; display: inline;">${friend.displayName}</h4>
							</a>
							</td>							
						</tr>
					</tbody>
				</table>
			</div>
		  <c:if test="${not friend.active}">
		    </div>
		  </c:if>
		</div>
	</c:forEach>
	<c:if
		test="${actionBean.commonFriendCount > actionBean.initialCommonFriendCount}">
		<div class="lotusLeft lotusNetworkPerson">
			<div>
				<a role="button" href="javascript:${pre}_showAll()"><fmt:message
						key="friendsShowAllCommonFriends" /> </a>
			</div>
		</div>
	</c:if>
</div>

</div>

