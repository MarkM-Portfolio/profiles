<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.ibm.lconn.core.web.util.services.ServiceReferenceUtil" %>
<%@ page import="java.util.*" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- HCL Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- HCL Technologies Limited 2001, 2022                               --%>
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
<%@ taglib prefix="core" 		uri="http://www.ibm.com/lconn/tags/external" %> 
<%@ taglib prefix="lc-ui" 		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper"%>
<html:xhtml/>
<%
	boolean secure = request.isSecure();
	Iterator itr = ServiceReferenceUtil.getAllServiceRefs().values().iterator();
	while(itr.hasNext()) {
		ServiceReferenceUtil service = (ServiceReferenceUtil)itr.next();
		request.setAttribute(service.getServiceName()+"SvcLocation", service.getServiceLink(secure) );
	}
	String searchUrl = ServiceReferenceUtil.getServiceLink("search", secure);
	String newsUrl = ServiceReferenceUtil.getServiceLink("news", secure);
	request.setAttribute("searchSvcLocation", searchUrl);
	request.setAttribute("newsSvcLocation", newsUrl);
%>
<script type="text/javascript">
	<%-- Added for search scope support --%>
	var lconn_core_thirdPartySearchEngines = <core:externalSearchTag includeScriptTags="false" />;
</script>

<c:url value="/html/searchProfiles.do" var="searchProfilesUrl"/>
<c:url value="/html/myProfileView.do" var="myProfileViewUrl"/>
<c:url value="/html/searchProfiles.do" var="directoryViewUrl"/>
<c:url value="/html/editMyProfileView.do" var="editMyProfileViewUrl"/>
<c:url value="/html/networkView.do?widgetId=friends&requireAuth=true" var="myNetworkViewUrl"/>
<c:set var="isNewUI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>"/>

<profiles:isLotusLive var="isLotusLive"/>
<profiles:checkPermission feature="profile.search" permission="profile.search.view" checkTarget="false" var="canViewSearch" />
<profiles:checkPermission feature="profile.connection" permission="profile.connection.view" checkTarget="false" var="canViewNetwork" />
<c:choose>
	<c:when test="${canViewSearch != 'true'}"> <%-- search not available for this user --%>
		<div class="lotusTitleBar2"><div class="lotusWrapper"><div class="lotusInner"> <%-- Search-less header --%>
			<div class="lotusTitleBarContent" aria-label="<fmt:message key='label.header.pageheading'/>" role="region">
				<h2 class="lotusHeading">
					<img alt="" class="lotusIcon iconsComponentsBlue24 iconsComponentsBlue24-ProfilesBlue24" src="<lc-ui:blankGif />"><span class="lotusText"><fmt:message key="label.page.profiles"/></span>
				</h2>
			</div>
		</div></div></div>	
	</c:when>
	<c:when test="${isLotusLive != 'true'}"> <%-- non-Smarcloud header --%>
		<c:choose>
			<c:when test='${isNewUI}'>
				<div class="lotusTitleBar2 lotusTitleBar2Tabs lotusHidden">
				<div class="lotusWrapper">
					<div class="lotusInner">
						<div class="lotusTitleBarContent" aria-label="<fmt:message key='label.header.pageheading'/>" role="region">
							<h2 class="lotusHeading">
								<img alt="" class="lotusIcon iconsComponentsBlue24 iconsComponentsBlue24-ProfilesBlue24" src="<lc-ui:blankGif />">
								<span class="lotusText lotusHidden"><fmt:message key="label.page.profiles"/></span>
							</h2>
							<div aria-label="<fmt:message key='label.page.profiles.navigations'/>" role="navigation" aria-controls="lotusMain lotusHidden">
							<ul id="lotusHeaderNavigation_UL" class="lotusNavTabs" role="toolbar" aria-label="<fmt:message key='label.page.profiles.navigations'/>">
								<c:if test="${fn:containsIgnoreCase(requestScope['javax.servlet.forward.request_uri'],'myProfileView')}"><li id="liProfileHeader_MyProfile"><div class="lotusTabWrapper"><a id="aProfileHeader_MyProfile" onclick="lconn.profiles.ProfilesCore.setLastElement(this.id);" class="lotusTab" href='<c:out value="${myProfileViewUrl}"/>' role="button" aria-label="<fmt:message key='label.header.myprofile'/>"><span class="lotusTabInner"><fmt:message key="label.header.myprofile" /></span></a></div></li></c:if>
								<c:if test="${canViewNetwork == 'true' && fn:containsIgnoreCase(requestScope['javax.servlet.forward.request_uri'],'networkView') || fn:containsIgnoreCase(requestScope['javax.servlet.forward.request_uri'],'searchProfiles') || fn:containsIgnoreCase(requestScope['javax.servlet.forward.request_uri'],'advancedSearch') || fn:containsIgnoreCase(requestScope['javax.servlet.forward.request_uri'],'/html/profileView')}"><li id="liProfileHeader_MyNetwork"><div class="lotusTabWrapper"><a id="aProfileHeader_MyNetwork" onclick="lconn.profiles.ProfilesCore.setLastElement(this.id);" class="lotusTab" href='<c:out value="${myNetworkViewUrl}"/>' role="button" aria-label="<fmt:message key='label.header.mynetwork'/>"><span class="lotusTabInner"><fmt:message key="label.header.mynetwork" /></span></a></div></li></c:if>
								<c:if test="${fn:containsIgnoreCase(requestScope['javax.servlet.forward.request_uri'],'myProfileView')}"><li id="liProfileHeader_Settings"><div class="lotusTabWrapper"><a id="aliProfileHeader_Settings" class="lotusTab" href='<c:out value="${newsSvcLocation}/defaulthomepage"/>' role="button" aria-label="<fmt:message key='label.header.myprofilesetting'/>"><span class="lotusTabInner"><fmt:message key="label.header.myprofilesetting" /></span></a></div></li></c:if>
								<c:if test="${fn:containsIgnoreCase(requestScope['javax.servlet.forward.request_uri'],'/html/profileView') || fn:containsIgnoreCase(requestScope['javax.servlet.forward.request_uri'],'/html/networkView') || fn:containsIgnoreCase(requestScope['javax.servlet.forward.request_uri'],'/html/searchProfiles') || fn:containsIgnoreCase(requestScope['javax.servlet.forward.request_uri'],'/html/advancedSearch')}"><li id="liProfileHeader_Directory"><div class="lotusTabWrapper"><a id="aProfileHeader_Directory" onclick="lconn.profiles.ProfilesCore.setLastElement(this.id);" class="lotusTab" href='<c:out value="${directoryViewUrl}"/>' role="button" aria-label="<fmt:message key='label.header.directory'/>"><span class="lotusTabInner"><fmt:message key="label.header.directory" /></span></a></div></li></c:if>
							</ul>
							<div class="lotusClear"></div>
						</div>
					</div>
					<div id="commonSearchControlDiv">
						<tags:simplesearchform
							sSvcLocation="${searchSvcLocation}"
							divContainer="commonSearchControlDiv"
						/>
					</div>
					</div>			 
				</div>
				</div>
			</c:when>
			<c:otherwise>
				<div class="lotusTitleBar2 lotusTitleBar2Tabs">
					<div class="lotusWrapper">
					   <div class="lotusInner">
							<div class="lotusTitleBarContent" aria-label="<fmt:message key='label.header.pageheading'/>" role="region">
							<h2 class="lotusHeading">
								 <img alt="" class="lotusIcon iconsComponentsBlue24 iconsComponentsBlue24-ProfilesBlue24" src="<lc-ui:blankGif />">
								 <span class="lotusText"><fmt:message key="label.page.profiles"/></span>
							 </h2>
							<div aria-label="<fmt:message key='label.page.profiles.navigations'/>" role="navigation" aria-controls="lotusMain">
								<ul id="lotusHeaderNavigation_UL" class="lotusNavTabs" role="toolbar" aria-label="<fmt:message key='label.page.profiles.navigations'/>">
									<li id="liProfileHeader_MyProfile"><div class="lotusTabWrapper"><a id="aProfileHeader_MyProfile" onclick="lconn.profiles.ProfilesCore.setLastElement(this.id);" class="lotusTab" href='<c:out value="${myProfileViewUrl}"/>' role="button" aria-label="<fmt:message key='label.header.myprofile'/>"><span class="lotusTabInner"><fmt:message key="label.header.myprofile" /></span></a></div></li>
									<c:if test="${canViewNetwork == 'true'}"><li id="liProfileHeader_MyNetwork"><div class="lotusTabWrapper"><a id="aProfileHeader_MyNetwork" onclick="lconn.profiles.ProfilesCore.setLastElement(this.id);" class="lotusTab" href='<c:out value="${myNetworkViewUrl}"/>' role="button" aria-label="<fmt:message key='label.header.mynetwork'/>"><span class="lotusTabInner"><fmt:message key="label.header.mynetwork" /></span></a></div></li></c:if>
									<li id="liProfileHeader_Directory"><div class="lotusTabWrapper"><a id="aProfileHeader_Directory" onclick="lconn.profiles.ProfilesCore.setLastElement(this.id);" class="lotusTab" href='<c:out value="${directoryViewUrl}"/>' role="button" aria-label="<fmt:message key='label.header.directory'/>"><span class="lotusTabInner"><fmt:message key="label.header.directory" /></span></a></div></li>
								</ul>
							 <div class="lotusClear"></div>
						  </div>
					   </div>
					   <div id="commonSearchControlDiv">
						  <tags:simplesearchform
							 sSvcLocation="${searchSvcLocation}"
							 divContainer="commonSearchControlDiv"
						  />
					  </div>
					 </div>			 
				</div>
			</c:otherwise>
		</c:choose>
	</c:when>
	<c:otherwise>
		<div class="lotusTitleBar2"><div class="lotusWrapper"><div class="lotusInner"> <%-- SmartCloud header --%>
			<div class="lotusTitleBarContent" aria-label="<fmt:message key='label.header.pageheading'/>" role="region">
				<h2 class="lotusHeading">
					<img alt="" class="lotusIcon iconsComponentsBlue24 iconsComponentsBlue24-ProfilesBlue24" src="<lc-ui:blankGif />"><span class="lotusText"><fmt:message key="label.page.profiles"/></span>
				</h2>
			</div>
			<div id="commonSearchControlDiv">
				<tags:simplesearchform
					sSvcLocation="${searchSvcLocation}"
					divContainer="commonSearchControlDiv"
				/>
			</div>
		</div></div></div>
	</c:otherwise>
</c:choose>
