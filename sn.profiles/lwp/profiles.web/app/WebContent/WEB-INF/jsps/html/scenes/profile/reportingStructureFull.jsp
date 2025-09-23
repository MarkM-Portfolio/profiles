<%@ page contentType="text/html;charset=UTF-8" %>

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

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/>
<%
String currentTabtt = (String)request.getAttribute("currentTab");
String currentProfileUidtt = request.getParameter("uid");
String currentLoggedInUserUidtt = (String) request.getAttribute("currentLoggedInUserUid");
String inNetwork = (String)request.getAttribute("inNetwork");
String subAction = (String)request.getParameter("subAction");
String liSelected = "";
if ("sameManager".equals(subAction)) {
	liSelected = "rptChainSameMgr_li";
} else if("peopleManaged".equals(subAction)) {
	liSelected = "rptChainPeopleMged_li";
} else {
	liSelected = "rptChain_li";
}
request.setAttribute("liSelected", liSelected);

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

canFriend = false;
%> 
 
<script type="text/javascript">
<%@ include file="/WEB-INF/jsps/html/common/profileData.jsp" %>
	profilesData.config.pageId = "reportingStructureView"; 
	document.title = '<core:escapeJavaScript><fmt:message key="label.page.profiles.reporting"><fmt:param value="${displayName}"/></fmt:message></core:escapeJavaScript>';
</script>
<%--
<jsp:include page="/WEB-INF/jsps/vcard/vcard_popup.jsp"/>
<jsp:include page="/WEB-INF/jsps/network/invite.jsp"/>
--%>
<div class="lotusContent" role="main"> 
	<a id="mainContent" name="mainContent"></a><!-- skip links for accessibility -->
	<jsp:include page="/WEB-INF/jsps/html/scenes/reportingStructure/welcome.jsp" />
	<div class="lotusHeader">
		<table id="inLineBusinessCard" class="lotusLayout" cellspacing="0">
			<tr>
				<td>
					<fmt:setBundle basename="com.ibm.lconn.profiles.strings.uilabels" var="attributeLabels" scope="page" />
					<c:url value="/audio.do" var="audioUrl">
						<c:param name="uid" value="${uid}"/>
						<c:param name="lastMod" value="${lastUpdate}"/>
					</c:url>
					<h1>
				    	<c:choose>
					    	<c:when test='${!empty userid}'>
								<c:set var="vcardUserHook" value="<span class='x-lconn-userid' style='display: none;'>${userid}</span>"/>
							</c:when>
							<c:otherwise>
								<c:if test="${showEmail}">						
									<c:set var="vcardUserHook" value="<span class='email' style='display: none;'>${email}</span>"/>
								</c:if>
							</c:otherwise>
						</c:choose>
						<c:set var="profilesHtmlRoot" value="${pageContext.request.contextPath}/html"/>
						<fmt:message key="label.reportingstructure.heading">
							<fmt:param value="
		                      <span class='vcard'>
		                        <a href='${profilesHtmlRoot}/profileView.do?key=${key}' class='fn url bidiAware'>${displayName}</a>
						    	${vcardUserHook}
		                      </span>
	                      	"/>
                      	</fmt:message>
	                </h1>
				</td>
			</tr>
		</table>
	</div>
	<div id="lotusReportingStructure">	
		<div id="lotusReportingStructureHead" class="lotusTabContainer">
			<a id="subNavitation" name="subNavigation"></a><!-- skip links for accessibility -->		
			<ul class="lotusTabs" id="rptStructTabs_ul" role="toolbar" aria-label="<fmt:message key="label_profile_reportingstructure" />">
				<li id="rptChain_li" class="lotusFirst lotusSelected">
		 	 		<a id="rptChain_li_link" href="javascript:void(0);" onclick="return showManagementAction({'subAction': '', 'page': 1, 'managerKey': '${managerKey}', 'isManager': '${isManager}'})" role="button" aria-pressed="true"><fmt:message key="label_profile_otherviews_reportingstructure" /></a>
		 	 	</li>
				<c:if test="${profiles:featureEnabled('profile.peopleManaged', tgtProfile)}">
					<c:if test="${!empty managerKey and managerKey ne ''}">
						<li id="rptChainSameMgr_li">
							<a id="rptChainSameMgr_li_link" href="javascript:void(0);" onclick="return showManagementAction({'subAction': 'sameManager', 'page': 1, 'managerKey': '${managerKey}', 'isManager': '${isManager}'})" role="button" aria-pressed="false"><fmt:message key="label_profile_otherviews_samemanager" /></a>
						</li>
					</c:if>
					<c:if test="${isManager == 'Y'}">
						<li id="rptChainPeopleMged_li">
							<a  id="rptChainPeopleMged_li_link" href="javascript:void(0);" onclick="return showManagementAction({'subAction': 'peopleManaged', 'page': 1, 'managerKey': '${managerKey}', 'isManager': '${isManager}'})" role="button" aria-pressed="false"><fmt:message key="label_profile_otherviews_peoplemanaged" /></a>
						</li>
					</c:if>
				</c:if>
			</ul>
		</div>

		<div id="reportStructureArea" class="lotusSection"></div>
    	<html:hidden property="profileUid" styleId="profileUid" value="${uid}" />
    	<html:hidden property="profileKey" styleId="profileKey" value="${key}" />
    	<html:hidden property="managerUid" styleId="managerUid" value="${managerUid}" />
    	<html:hidden property="managerKey" styleId="managerKey" value="${managerKey}" />
    	<html:hidden property="isManager" styleId="isManager" value="${isManager}" />
    	<html:hidden property="lastMod" styleId="lastMod" value="${lastUpdate}" />
    	<html:hidden property="timezoneId" styleId="timezoneId" value="${timezoneId}" />
		<span id="widget-container-col2" class="widgetContainer"></span>
	</div>
</div>

<script type="text/javascript">
	var profilesRptStructure_menuSelect = function( selectedId ) {
		if(!selectedId) return false;
		var menuItems = dojo.query('#rptStructTabs_ul li').forEach( function(node, index, arr){ dojo.removeClass( node, "lotusSelected"); }); // clear out all selected
		var links = dojo.query('#rptStructTabs_ul a').forEach( function(node, index, arr){ dojo.attr( node, "aria-pressed", "false"); }); // reset aria-pressed to false
		dojo.addClass( dojo.byId( selectedId ), "lotusSelected");
		dojo.attr(selectedId + "_link", "aria-pressed", "true");
	};

	profilesRptStructure_menuSelect("${liSelected}");

	dojo.addOnLoad(
	    function(){
			// Setup aria helper for this toolbar above
			dojo.require("lconn.core.aria.Toolbar");
		    if( lconn.core.aria && typeof(lconn.core.aria.Toolbar) == "function" ) {
				new lconn.core.aria.Toolbar("rptStructTabs_ul");
			}
			

			//select the tab passed into the url (if any)
			var aLoc = ((location.href + "?").split("?"));
			var oldParams = dojo.queryToObject(aLoc[1]);
			var params = dojo.mixin(oldParams, {'page': 1, 'managerKey': '${managerKey}', 'isManager': '${isManager}'});
			showManagementAction(params);
			
			
			//function to set the href of the links to real urls in case someone right clicks 
			//on the link to open in a new window or tab
			var setHref_ = function(el, subAction) {
				if (el) {
					el.href = aLoc[0] + "?" + dojo.objectToQuery(dojo.mixin(params, {'subAction': subAction}));
				}
			};
			
			setHref_( dojo.byId("rptChain_li_link"), "");
			setHref_( dojo.byId("rptChainSameMgr_li_link"), "sameManager");
			setHref_( dojo.byId("rptChainPeopleMged_li_link"), "peopleManaged");			
			
			//bidi support for Text direction
//			lconn.core.globalization.bidiUtil.enforceTextDirectionOnPage(dojo.byId("lotusReportingStructure"));
//			dojo.query(".hasHover",dojo.byId("inLineBusinessCard")).forEach( function(el){
//				dojo.addClass(el, "bidiAware");
//			});
			lconn.core.globalization.bidiUtil.enforceTextDirectionOnPage(dojo.byId("inLineBusinessCard"));
	    }
    );
</script>