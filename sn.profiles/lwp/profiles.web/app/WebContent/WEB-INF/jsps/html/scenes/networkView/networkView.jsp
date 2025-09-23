<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- HCL Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright HCL Technologies Limited 2001, 2021                     --%>
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
<%@ taglib prefix="stripes" 	uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper" %>

<c:set var="isMyProfile" value="${key == loggedInUserKey}" />
<c:set var="isNewUI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>"/>
<c:choose>
	<c:when test="${isMyProfile}">
		<fmt:message key="label.page.profiles.mynetwork" var="pageTitle" />
	</c:when>
	<c:otherwise>
		<fmt:message key="label.page.profiles.network" var="pageTitle"><fmt:param value="${displayName}"/></fmt:message>
	</c:otherwise>
</c:choose>
<stripes:layout-render 
	name="/WEB-INF/jsps/html/layouts/stripes/profilesLayout.jsp" 
	pageTitle="${pageTitle}">
	
    <stripes:layout-component name="main_content">
		<jsp:useBean id="now" class="java.util.Date" />
		<script type="text/javascript">
			var pageTime = ${now.time};
			profilesData.config.pageId = "networkView";
			
			//update the page id to friends so the correct widgets load
			if (window.location.href.indexOf("widgetId=follow") != -1) {
				profilesData.config.pageId = "followView";
			}

				
		// metrics variables, it will be used in tiles/footer.jsp
		readTrackerMetrics = {};  
		readTrackerMetrics.itemType = "NetWorkView";
		readTrackerMetrics.contentId = profilesData.config.pageId;
		readTrackerMetrics.contentTitle = profilesData.config.pageId;

		</script>
		<c:choose>
		<c:when test="${isNewUI}">
		<div id="myNetwork_contentDiv" class="lotusContent">
			<jsp:include page="/WEB-INF/jsps/html/scenes/networkView/welcome.jsp" />
			<div id="profileInfoMsgDiv" class="lotusHidden" role="alert"></div>
			<div id="myNetwork_sectionDiv" role="main" aria-live="polite" class="myNetwork_section">
				<span id="widget-container-fullpage" class="widgetContainer"></span>
				<span id="widget-container-col2" class="widgetContainer"></span>
			</div>	
		</div>
		</c:when>
		<c:otherwise>
			<div id="myNetwork_contentDiv" class="lotusContent">
				<jsp:include page="/WEB-INF/jsps/html/scenes/networkView/welcome.jsp" />
				<div id="profileInfoMsgDiv" class="lotusHidden" role="alert"></div>
				<div id="myNetwork_sectionDiv" role="main" aria-live="polite" >
					<span id="widget-container-fullpage" class="widgetContainer"></span>
					<span id="widget-container-col2" class="widgetContainer"></span>
				</div>	
			</div>
		</c:otherwise>
		</c:choose>
    </stripes:layout-component>

    <stripes:layout-component name="left_content">
		<c:set var="networkLabel"><fmt:message key="friends" /></c:set>
		<c:set var="networkSectionLabel"><fmt:message key="label.header.mynetwork" /></c:set>
		<c:choose>
			<c:when test="${isNewUI}">
		<div id ="profilePaneLeft" class="lotusColLeft leftColumn" style="display:block !important;padding:0px;width:200px;margin-right:0px;
		margin-top:0px !important;">
			<div id="profilesNavMenu" class="lotusMenu" role="navigation" aria-label="${networkSectionLabel}">
				<ul id="profilesNavMenuUL" role="toolbar" aria-label="${networkLabel}">
					<c:choose>
						<c:when test="${isMyProfile}">
							<li id="liColleagues">
								<a id="aColleagues" role="button" href="javascript:void(0);" onclick="lconn.profiles.profilesNetworkPage.menuSelect({widgetId:'friends',action:'rc',selectNode:this.id});">
									<fmt:message key="friendsMenuTitleMyContactsNew" />
								</a>
							</li>
							<li id="liInvites">
								<a id="inivtesMenuA" role="button" href="javascript:void(0);" onclick="lconn.profiles.profilesNetworkPage.menuSelect({widgetId:'friends',action:'in',selectNode:this.id});">
									<fmt:message key="friendsInvitations" />
									<label>
										<%= "("+request.getAttribute("totalFriends")+")" %>  
									</label>
								</a>
							</li>
						</c:when>
						<c:otherwise>
							<li id="liColleagues">
								<a id="aColleagues" role="button" href="javascript:void(0);" onclick="lconn.profiles.profilesNetworkPage.menuSelect({widgetId:'friends',action:'rc',selectNode:this.id});">
									<fmt:message key="friendsMenuTitleAllContacts" />
								</a>
							</li>
						</c:otherwise>
					</c:choose>
					<c:if test="${canExposeFollowingInfo}">
						<li id="liFollowing">
							<a id="aFollowing" role="button" href="javascript:void(0);" onclick="lconn.profiles.profilesNetworkPage.menuSelect({widgetId:'follow',action:'in',selectNode:this.id});">
								<fmt:message key="label.following.following" />	
								<label>
									<%= "("+request.getAttribute("FollowedPersonsCount")+")" %>  
								</label>							
							</a>
						</li>
						<li id="liFollowers">
							<a id="aFollowers" role="button" href="javascript:void(0);" onclick="lconn.profiles.profilesNetworkPage.menuSelect({widgetId:'follow',action:'out',selectNode:this.id});">
								<fmt:message key="label.following.followers" />
								<label>
									<%= "("+request.getAttribute("ProfileFollowersCount")+")" %>  
								</label>
							</a>
						</li>
					</c:if>
				</ul>
			</div>
			<span id="widget-container-col3" class="widgetContainer"></span>
			<span id="widget-container-col1" class="widgetContainer inviteToConnect"></span>
		</div>
	</c:when>
	<c:otherwise>
		<div id ="profilePaneLeft" class="lotusColLeft" style="display:block !important;">
			<div id="profilesNavMenu" class="lotusMenu" role="navigation" aria-label="${networkSectionLabel}">
				<ul id="profilesNavMenuUL" role="toolbar" aria-label="${networkLabel}">
					<c:choose>
						<c:when test="${isMyProfile}">
							<li id="liColleagues">
								<a id="aColleagues" role="button" href="javascript:void(0);" onclick="lconn.profiles.profilesNetworkPage.menuSelect({widgetId:'friends',action:'rc',selectNode:this.id});">
									<fmt:message key="friendsMenuTitleMyContacts" />
								</a>
							</li>
							<li id="liInvites">
								<a id="inivtesMenuA" role="button" href="javascript:void(0);" onclick="lconn.profiles.profilesNetworkPage.menuSelect({widgetId:'friends',action:'in',selectNode:this.id});">
									<fmt:message key="friendsInvitations" />
								</a>
							</li>
						</c:when>
						<c:otherwise>
							<li id="liColleagues">
								<a id="aColleagues" role="button" href="javascript:void(0);" onclick="lconn.profiles.profilesNetworkPage.menuSelect({widgetId:'friends',action:'rc',selectNode:this.id});">
									<fmt:message key="friendsMenuTitleAllContacts" />
								</a>
							</li>
						</c:otherwise>
					</c:choose>
					<c:if test="${canExposeFollowingInfo}">
						<li id="liFollowing">
							<a id="aFollowing" role="button" href="javascript:void(0);" onclick="lconn.profiles.profilesNetworkPage.menuSelect({widgetId:'follow',action:'in',selectNode:this.id});">
								<fmt:message key="label.following.following" />
							</a>
						</li>
						<li id="liFollowers">
							<a id="aFollowers" role="button" href="javascript:void(0);" onclick="lconn.profiles.profilesNetworkPage.menuSelect({widgetId:'follow',action:'out',selectNode:this.id});">
								<fmt:message key="label.following.followers" />
							</a>
						</li>
					</c:if>
				</ul>
			</div>
				<span id="widget-container-col1" class="widgetContainer"></span>
		</div>
	</c:otherwise>
	</c:choose>
	</stripes:layout-component>

    <stripes:layout-component name="right_content">
		<div id="profilePaneRight" class="lotusColRight lotusHidden">
			<span id="widget-container-col3" class="widgetContainer"></span>			
		</div>
	</stripes:layout-component>
</stripes:layout-render>

<script type="text/javascript">

(function() {
	if (dojo.exists("lconn.profiles.profilesNetworkPage")) {
		var initArgs = {
			appContext: '<core:escapeJavaScript>${pageContext.request.contextPath}</core:escapeJavaScript>',
			
			profile: dojo.clone(profilesData.displayedUser),
						
			ids: {
				navMenuId: "profilesNavMenuUL"
			}
		};
		
		lconn.profiles.profilesNetworkPage.init(initArgs);
	}
})();


</script>
