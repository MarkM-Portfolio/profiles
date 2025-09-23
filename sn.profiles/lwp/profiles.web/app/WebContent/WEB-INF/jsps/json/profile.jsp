<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2016                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%
		String sType = (String) pageContext.findAttribute("contentType");
		if (sType == null || sType.length() == 0) sType = "application/json";
		response.setContentType(sType);
		response.setCharacterEncoding("UTF-8");
%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<profiles:currentUser var="currentUser"/>
<profiles:isLotusLive var="isLotusLive"/>

<c:if test="${blockContent == 'true'}"><% response.setStatus(403); %></c:if>
<c:if test="${blockContent != 'true'}">
	<c:set var="showBaseInfo" value="${empty param['includeFeatures'] ? true : fn:contains(param['includeFeatures'],'baseInfo')}" />
	<c:set var="showPhotoInfo" value="${fn:contains(param['includeFeatures'],'photoInfo')}" />
	<c:set var="showNetworkInfo" value="${fn:contains(param['includeFeatures'],'networkInfo')}" />
	<c:set var="showLocalTimeInfo" value="${fn:contains(param['includeFeatures'],'localTimeInfo')}" />

	<c:choose>
		<c:when test="${!empty callback}">
			${fn:escapeXml(callback)} (
		</c:when>
		<c:when test="${!empty variable}">
			${fn:escapeXml(variable)} =
		</c:when>
	</c:choose>

	<c:if test="${allowMultiple}"> [ </c:if>

	<c:set var="isFirstProfile">true</c:set>
	<jsp:useBean id="nowTz" class="java.util.Date" />
	<c:forEach items="${profiles}" var="profile">
		<c:if test="${allowMultiple || isFirstProfile == 'true'}">
			<c:set var="emailDisp">
				<c:if test="${showEmail}">
					<c:choose>
						<c:when test="${!empty profile.email}">
							${profiles:encodeForJsonString(profile.email)}
						</c:when>
						<c:when test="${!empty profile.mcode && empty profile[lconnUserIdAttr]}">
							
						</c:when>
						<c:otherwise>
							${fn:escapeXml(param['email'])}
						</c:otherwise>
					</c:choose>
				</c:if>
			</c:set>
			<c:set var="mcodeDisp">
				<c:if test="${!empty profile.email && !empty profile[lconnUserIdAttr]}">
					${profiles:encodeForJsonString(profile.mcode)}
				</c:if>
			</c:set>
			<c:set var="orgDisp">
				<c:choose>
					<c:when test="${!empty profile.organizationTitle}">
						${profile.organizationTitle}
					</c:when>
					<c:when test="${isLotusLive == 'true' && !empty profile.tenantKey}">
						${profiles:getTenantName(profile.tenantKey)}
					</c:when>
				</c:choose>
			</c:set>
			<c:if test="${isFirstProfile != 'true'}">,</c:if>
			{
				<%-- These fist entries everyone can see --%>
				"X_lconn_userid" : "${profile[lconnUserIdAttr]}",
				
				<c:if test="${showBaseInfo}">
					"uid": "${profile.uid}",
					"key": "${profile.key}",
					"fn": "${profiles:encodeForJsonString(profile.displayName)}",
					"dn": "${profiles:encodeForJsonString(profile.distinguishedName)}",
				</c:if>
				
				<c:set var="photoUrl">
					<c:if test="${profile.config.showProfilePhoto}">
						<c:choose>
							<c:when test="${empty profile.key}">
								<profiles:serviceLink serviceName='webresources'>${svcRef}</profiles:serviceLink>/web/com.ibm.lconn.core.styles.oneui3/images/blank.gif
							</c:when>
							<c:otherwise>
								<profiles:serviceLink serviceName='profiles'>${svcRef}</profiles:serviceLink>/photo.do?key=${profile.key}&lastMod=<profiles:outputLMTime time='${profile.lastUpdate}'/><c:if test="${showPhotoInfo}">&noimg=pixel</c:if>
							</c:otherwise>
						</c:choose>
					</c:if>
				</c:set>
				
				<c:if test="${showPhotoInfo || showBaseInfo}">
					"photo": "${photoUrl}",
					"X_bizCardShowPhoto": ${profile.config.showProfilePhoto},
				</c:if>

				<c:if test="${showNetworkInfo}">
					"X_isNetworked": <c:choose><c:when test="${empty profile.isNetworked || profile.isNetworked == false}">false</c:when><c:otherwise>true</c:otherwise></c:choose>,
					"X_canNetwork": <c:choose><c:when test="${empty profile.canNetwork || profile.canNetwork == false}">false</c:when><c:otherwise>true</c:otherwise></c:choose>,
				</c:if>
				
				<c:if test="${showLocalTimeInfo}">
					<c:choose>
						<c:when test="${!empty profile[lconnUserIdAttr]}">
							<c:set var="localTimeFormatted"><fmt:formatDate timeStyle="SHORT" type="time" timeZone="${profile.timezoneId}" value="${nowTz}" /></c:set>
							"X_localTime": "${profiles:encodeForJsonString(localTimeFormatted)}",
							"X_localTimezone": "${profiles:encodeForJsonString(profile.timezoneDisplayValue)}",
						</c:when>
						<c:otherwise>
							"X_localTime": "",
							"X_localTimezone": "",
						</c:otherwise>
					</c:choose>				
				</c:if>
				<c:if test="${showLocalTimeInfo || showBaseInfo}">
					"X_timezoneOffset": "${profile.timezoneOffset}",
				</c:if>
				<c:if test="${showBaseInfo}">
					"X_isExternal": "${profile.isExternal}",
					
					"X_bizCardMainHtml": <c:choose><c:when test="${!empty profile.key}">"${fn:replace(profile.mainBizCardHtml,'\\\'','\'')}"</c:when><c:otherwise>""</c:otherwise></c:choose>,
					"X_bizCardSTAwareness": ${profile.config.enableSametimeAwareness && !(empty applicationScope.sametimeAwareness) && applicationScope.sametimeAwareness && not empty profilesSecure && profilesSecure != true},
					"X_bizCardSecureSTAwareness": ${profile.config.enableSametimeAwareness && !(empty applicationScope.sametimeAwareness) && applicationScope.sametimeAwareness && not empty profilesSecure && profilesSecure},
					"X_bizCardLocation": {  "unsecure": <c:choose><c:when test="${!(empty applicationScope.sametimeAwareness) && applicationScope.sametimeAwareness}">"${applicationScope.sametimeUnSecureHref}"</c:when><c:otherwise>""</c:otherwise></c:choose> , "secure":  <c:choose><c:when test="${!(empty applicationScope.sametimeAwareness) && applicationScope.sametimeAwareness}">"${applicationScope.sametimeSecureHref}"</c:when><c:otherwise>""</c:otherwise></c:choose>},
					"X_bizCardSTInputType": "${applicationScope.sametimeInputType}",
					"X_bizCardSTStatusMsg": ${profile.config.showSametimeStatusMsg},
					"X_stLinks": "<profiles:serviceLink serviceName='sametimeLinks'>${svcRef}</profiles:serviceLink>",
					"X_STChatAction": ${profile.config.showSametimeChatAction},
					"X_STCallAction": ${profile.config.showSametimeCallAction},
					"X_bizCardServiceLinks": <c:choose><c:when test="${!empty profile.serviceLinksJson}">${profile.serviceLinksJson}</c:when><c:otherwise>[]</c:otherwise></c:choose>,
					"X_allowEvalLabel": ${applicationScope.profilesConfig.dataAccessConfig.allowJsonpJavelin},

					
					"X_loggedInUserId": "<c:if test='${! empty currentUser}'>${currentUser.userid}</c:if>",
					"X_loggedInUserKey": "<c:if test='${! empty currentUser}'>${currentUser.key}</c:if>",
					"X_loggedInUserDn": "<c:if test='${! empty currentUser}'>${profiles:encodeForJsonString(currentUser.distinguishedName)}</c:if>",
						
					"X_bizCardActions": [
						<c:forEach var="action" items="${profile.bizCardActions}">
							<c:if test="${!empty wroteAttr}">,</c:if>{
								"urlPattern": "${fn:replace(action.jsonUrlPattern,'\\\'','\'')}", "label": "<core:message label="${action.label}"/>", "liClass": "${action.liClass}"
								<c:if test="${action.icon != null}">, "icon": {"href": "${fn:replace(action.icon.jsonHref,'\\\'','\'')}", "alt": "<core:message label="${action.icon.alt}"/>" }</c:if>
							}
							<c:set var="wroteAttr" value="true"/>
						</c:forEach>
						<c:remove var="wroteAttr"/>
					],

					"X_extension_attrs": {
						<c:forEach var="extAttr" items="${profile.extAttrs}">
							<c:if test="${!empty wroteAttr}">,</c:if>"${extAttr.key}": "${profiles:encodeForJsonString(extAttr.value)}"
							<c:set var="wroteAttr" value="true"/>
						</c:forEach>
						<c:remove var="wroteAttr"/>
					},
					
					"X_inDirectory": "${!(empty profile.key) ? true : false}",
					"X_isActiveUser": <c:choose><c:when test="${not empty profile.isActive && profile.isActive == true}">"true"</c:when><c:otherwise>"false"</c:otherwise></c:choose>,

					<%-- These entries are only visible if you have permission --%>
					<c:choose>
						<c:when test="${profiles:checkAclForProfile('profile.profile','profile.profile.view', profile) == false}">
							"adr": {
								"work": {
									"locality": "", 
									"region": "", 
									"country_name": ""
								}
							},
							"tel": {
								"work": "",
								"mobile": "",
								"fax": ""
							},
							"email": { <%-- allow email to show even if the current user doesn't have permission to view the entire profile --%>
								<c:if test="${showEmail}">"internet": "${fn:trim(emailDisp)}",</c:if> 
								"X_notes": ""
							},
							"title": "",
							"employeeTypeDesc": "", 
							"org": "",
					
							"X_blogUrl": "",
							"X_building_name": "",
							"X_building_floor": "",
							"X_office": "",

							"X_isFollowed": "false",
							"X_isFollowedEnabled": "false",
						</c:when>
						<c:otherwise>
							"adr": {
								"work": {
									"locality": "${profiles:encodeForJsonString(profile.workLocation.city)}", 
									"region": "${profiles:encodeForJsonString(profile.workLocation.state)}", 
									"country_name": "${profiles:encodeForJsonString(profile.countryDisplayValue)}"
								}
							},
							"tel": {
								"work": "${profiles:encodeForJsonString(profile.telephoneNumber)}",
								"mobile": "${profiles:encodeForJsonString(profile.mobileNumber)}",
								"fax": "${profiles:encodeForJsonString(profile.faxNumber)}"
							},
							"email": { 
								<c:if test="${showEmail}">"internet": "${fn:trim(emailDisp)}",</c:if> 
								"X_notes": "${profiles:encodeForJsonString(profile.groupwareEmail)}"
							},
							"title": "${profiles:encodeForJsonString(profile.jobResp)}",
							"employeeTypeDesc": "${profiles:encodeForJsonString(profile.employeeTypeDesc)}", 
							"org": "${profiles:encodeForJsonString(orgDisp)}",

									
							<c:set var="profBlogUrl" value="${fn:trim(profiles:escapeUnwiseURLChars(profile.blogUrl))}"/>
							<c:if test="${!(empty profBlogUrl) and fn:length(profBlogUrl) > 0}">
								<%
									String pBlogUrl = (String) pageContext.findAttribute("profBlogUrl");
									if (!pBlogUrl.matches("[a-zA-Z][a-zA-Z\\-]*:.*")) //[a-zA-Z0-9\\-]*\\:
										pageContext.setAttribute("profBlogUrl", "http://" + pBlogUrl);
									else if (pBlogUrl.matches("http(s{0,1})\\:\\w.*"))
										pageContext.setAttribute("profBlogUrl", pBlogUrl.replaceFirst("\\:","://"));
								%>
							</c:if>		
							"X_blogUrl": "${profiles:encodeForJsonString(profBlogUrl)}",
							"X_building_name": "${profiles:encodeForJsonString(profile.bldgId)}",
							"X_building_floor": "${profiles:encodeForJsonString(profile.floor)}",
							"X_office": "${profiles:encodeForJsonString(profile.officeName)}",
							<c:choose>
								<c:when test="${!empty profile[lconnUserIdAttr]}">
									"X_isFollowed": <c:choose><c:when test="${!(empty profile.is_followed) && profile.is_followed == true}">"true"</c:when><c:otherwise>"false"</c:otherwise></c:choose>, 
									"X_isFollowedEnabled": <c:choose><c:when test="${!(empty profile.acl_followingAdd) && profile.acl_followingAdd == true}">"true"</c:when><c:otherwise>"false"</c:otherwise></c:choose>,
								</c:when>
								<c:otherwise>
									"X_isFollowed": "false",
									"X_isFollowedEnabled": "false",
								</c:otherwise>
							</c:choose>
						</c:otherwise>
					</c:choose>
				</c:if>
				<c:if test="${!showBaseInfo}">
					"email": { 
						<c:if test="${showEmail}">"internet": "${fn:trim(emailDisp)}"</c:if> 
					},				
				</c:if>
				
				"mcode": "${fn:trim(mcodeDisp)}"
			}
		</c:if>
		<c:set var="isFirstProfile">false</c:set>
	</c:forEach>
	<c:if test="${allowMultiple}"> ] </c:if>

	<c:choose>
		<c:when test="${!empty callback}">
			);
		</c:when>
		<c:when test="${!empty variable}">
			;
		</c:when>
	</c:choose>
</c:if>
