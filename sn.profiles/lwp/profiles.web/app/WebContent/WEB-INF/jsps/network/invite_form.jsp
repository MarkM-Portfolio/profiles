<%@ page contentType="text/html;charset=UTF-8" session="false"%>

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

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %><%@ 
	taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %><%@ 
	taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %><%@ 
	taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %><%@ 
	taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>

<c:url var="profilesImgUrl" scope="page" value="/nav/common/styles/images"/>

<c:if test="${empty key}">
	<c:set var="key" value="${param.key}" />
</c:if>
<c:if test="${empty userid}">
	<c:set var="userid" value="${param.userid}" />
</c:if>

<div class="lotusDialogBorder">
	<form id="networkInviteForm" class="lotusDialog lotusForm">
		<div class="lotusDialogHeader">
			<h1 class="lotusHeading"><fmt:message key="friendsInvite"/></h1>
			<a title='<fmt:message key="friendsCancelInvAction"/>' role="button" class="lotusRight lotusDialogClose" href="javascript:;" onclick="lconn.profiles.Friending.hideNetworkInvite(); return false;">
				<img role="presentation" src="<core:blankGif />" alt='<fmt:message key="friendsCancelInvAction"/>' />
				<span class="lotusAltText">X</span>
			</a>
		</div>	
		<div class="lotusDialogContent">
			<fieldset>
				<legend class="lotusHidden"><fmt:message key="friendsInvite"/></legend><%-- a11y --%>
				<table class="lotusFormTable" cellpadding="0" cellspacing="0" border="0" summary="">
					<tbody>
					<tr class="lotusFormFieldRow">
						<td>
							<div class="profilePhoto">
					    		<c:url var="photoUrl" value="/photo.do">
					    			<c:choose>
						    			<c:when test="${!empty key}">
						    				<c:param name="key" value="${key}"/>
						    			</c:when>
						    			<c:when test="${!empty userid}">
						    				<c:param name="userid" value="${userid}"/>
						    			</c:when>
					    			</c:choose>
					    			<c:param name="lastMod" value="${lastUpdate}"/>
					    		</c:url>
					    		<img src="${photoUrl }" alt="${displayName}" height="128" width="128" />&nbsp;
							</div>
						</td>
						<td>
							<c:if test="${!empty displayName }">
								<div>
									<fmt:message key="friendsColleaguesInvite"><fmt:param value="${displayName}" /></fmt:message>
								</div>
							</c:if>
							<div>
								<input type="hidden" name="key" value="${key}"/>
								<input type="hidden" name="userid" value="${userid}"/>
								<input type="hidden" name="lastMod" value="${lastUpdate}"/>
								<label for="invitation_text"><fmt:message key="friendsIncludeMsgForInv"/></label><br />
								<textarea cols="40" rows="8" name="invitation_text" id="invitation_text"><fmt:message key="friendsInitialMsgForInv" /></textarea>
							<div>
						</td>
					</tr>
					</tbody>
				</table>
			</fieldset>
		</div>
		<div class="lotusDialogFooter">
			<div class="lotusBtnContainer">
				<fmt:message key="friendsSendInvAction" var="friendsSendInvAction" />
				<input type="button" value="${friendsSendInvAction}" class="lotusBtn lotusBtnSpecial lotusLeft"
	            	onclick="lconn.profiles.Friending.sendFriendRequest(this, '${key}', '${userid}', window.location.href); return false;">
				</input>
				<span style="padding-left: 5px;">
					<a href="javascript:void(0);" onclick="lconn.profiles.Friending.hideNetworkInvite(); return false;" class="lotusAction"><fmt:message key="friendsCancelInvAction"/></a>
				</span>
			</div>
		</div>
    </form>
</div>
