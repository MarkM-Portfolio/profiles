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
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ taglib prefix="lc-ui"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper"%>

<html:xhtml/>

<c:set var="isCnx8UI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>" scope="request"/>

<profiles:isLotusLive var="isLotusLive"/>
<profiles:isAdvancedSearchEnabled var="isAdvancedSearchEnabled"/>
<c:if test="${empty profilesSvcLocation}">
	<c:set value="${pageContext.request.contextPath}" var="profilesSvcLocation"/>
</c:if>
<script type="text/javascript">

	profilesData.config.pageId = "searchResultView";	
	// metrics variables, it will be used in tiles/footer.jsp
	readTrackerMetrics = {};  
	readTrackerMetrics.itemType = "SearchView";
	readTrackerMetrics.contentId = "SearchView"
	readTrackerMetrics.contentTitle = "SearchView";
</script>

<c:set var="parameters" value="${profiles:convertToMap(param)}" />
<fmt:setBundle basename="com.ibm.lconn.profiles.strings.uilabels" var="attrLabels"/>
<div id="lotusSearchResultsContent" class="lotusContent" role="main">	
	<div id="profileInfoMsgDiv" class="lotusHidden" role="alert"></div>
	<div class="lotusHeader" id="searchInfo"> 
	 	<c:set var="searchInfoHeaderText">
			<c:choose>
				<c:when test="${isLotusLive != 'true'}">		
					<fmt:message key="label.searchresults.people.heading">
						<fmt:param>
							<c:set var="firstField" value="true" />
							<c:forEach items="${profiles:convertToMap(param)}" var="parameter">
								<c:if test="${parameter.value != ''}">			
									<c:set var="labelConfig" value="${profiles:findAdvancedSearchLabel(fn:replace(parameter.key,'$','.'))}"/>
									<c:choose>
										<c:when test="${!empty labelConfig}"><core:message label="${labelConfig}" bundle="${attrLabels}" var="field"/>
										<c:if test="${fn:startsWith(field,'???label.advanced.searchForm.attribute')}"><profiles:getString key="label.advanced.searchForm.attribute.${parameter.key}" var="field" bundle="com.ibm.lconn.profiles.strings.uilabels" /></c:if>
										</c:when>
										<c:otherwise><fmt:message key="label.searchresults.searchby.${parameter.key}" var="field" /></c:otherwise>
									</c:choose>
									<c:choose>
										<c:when test="${fn:startsWith(field, '???') && !fn:startsWith(parameter.key, 'extattr') && (empty labelConfig)}">
										</c:when>
										<c:otherwise>
											<c:if test="${parameter.key != 'profileTags'}">
													<c:if test="${!firstField}">,&nbsp;</c:if>
													<c:out value="${field}"/>&nbsp;<c:out value="${parameter.value}"/>
											<c:set var="firstField" value="false" />
											</c:if>
										</c:otherwise>
									</c:choose>
								</c:if>
								<c:if test="${parameter.key == 'searchBy' and parameter.value != ''}">
									<c:set var="labelConfig" value="${profiles:findAdvancedSearchLabel(profiles:filter(param.searchBy))}"/>
									<c:choose>
										<c:when test="${!empty labelConfig}"><core:message label="${labelConfig}" bundle="${attrLabels}" var="field"/></c:when>
										<c:otherwise><fmt:message key="label.searchresults.searchby.${profiles:filter(param.searchBy)}" var="field" /></c:otherwise>
									</c:choose>
									<c:choose>
										<c:when test="${fn:startsWith(field, '???') && !fn:startsWith(parameter.key, 'extattr') && (empty labelConfig)}">
										</c:when>
										<c:otherwise>
											<c:if test="${!firstField}">,&nbsp;</c:if>
											<c:out value="${field}"/>&nbsp;<c:out value="${param.searchFor}"/>
											<c:set var="firstField" value="false" />
										</c:otherwise>
									</c:choose>
								</c:if>
							</c:forEach>
						</fmt:param>
					</fmt:message>
				</c:when>
				<c:otherwise>
					<fmt:message key="label.searchresults.directory.heading"/>
				</c:otherwise>
			</c:choose>				
		</c:set>

		<c:choose>
			<c:when test="${isCnx8UI}">
				<h3 class="lotusHeadingMessage cnx8ui-lotusHeadingMessage"><c:out value="${searchInfoHeaderText}" escapeXml="false" /></h3>
			</c:when>
			<c:otherwise>
				<h1 class="lotusHeadingMessage"><c:out value="${searchInfoHeaderText}" escapeXml="false" /></h1>
			</c:otherwise>
		</c:choose>
		
		<c:if test="${!empty profileTagsList}">
			<div class="lotusFilters2">
				<fmt:message key="label.searchresults.searchby.profileTags" />
				<c:forEach items="${profileTagsList}" var="tagString">
					<c:set var="cleanedTags" value="${profiles:removeTagString(param.profileTags, tagString)}" />
					<c:set target="${parameters}" property="profileTags" value="${cleanedTags}" />  
					
					<%-- removing a tag filter should bring user back to page 1 --%>
					<c:set target="${parameters}" property="page" value="1" /> 
					
					<c:set var="safeTagString"><c:out value="${tagString}" /></c:set>
					
					<c:set var="delAltText">
						<fmt:message key="label.searchresults.tagfilter.remove">
							<fmt:param value="${safeTagString}" />
						</fmt:message>
					</c:set>


					<%-- We want the url without any spaces, tabs, etc, so that is why we have this funky format --%>
					<c:set var="removeTagLink"
						><c:choose
							><c:when test="${fn:length(profileTagsList) > 1}"
								>${profilesSvcLocation}/html/${searchType}.do?<c:forEach var="sParam" items="${parameters}"
									><c:out value="${sParam.key}" />=<c:out value="${sParam.value}" />&<
								/c:forEach
							></c:when
							><c:otherwise
								>${profilesSvcLocation}/html/searchProfiles.do<
							/c:otherwise
						></c:choose
					></c:set>

					<c:if test="${fn:endsWith(removeTagLink, '&')}">
						<c:set var="removeTagLink">${fn:substring(removeTagLink, 0, fn:length(removeTagLink)-1)}</c:set>
					</c:if>
					
					<a href="${removeTagLink}" class="lotusFilter bidiAware" title="${delAltText}" role="button">									
						<span aria-hidden="true">${safeTagString}</span>
						<img src="<lc-ui:blankGif />" alt="" role="presentation" class="lotusDelete">
						<span class="lotusAltText">X</span>											  
					</a>

				</c:forEach>
				<script type="text/javascript">
				dojo.addOnLoad(
					function(){
						lconn.core.globalization.bidiUtil.enforceTextDirectionOnPage(dojo.byId("searchInfo"));	
					}
				);
				</script>
			</div>
		</c:if>
	</div>
	<c:choose>
		<c:when test="${empty searchResultsPage.results}">
			<div class="lotusChunk">
				<p><fmt:message key="label.searchresults.noresults" /></p>
			</div>
		</c:when>
		<c:otherwise>
			<c:if test="${moreResultsAvailable}">
				<div id="profileSearchMsgDiv" role="alert"></div>
				<div style="display:none" id="profilemessage_toomany">
					<span>
							<fmt:message key="label.search.results.more.available">
								<fmt:param value="${searchResultsPage.totalResults}" />
								<fmt:param value="${searchResultsPage.totalResults}" />	
							</fmt:message>
							<c:choose>
								<c:when test="${isAdvancedSearchEnabled == 'true'}">
									<html:link action="/html/advancedSearchView.do?showfulladv=true&displayName=${fn:escapeXml(param.searchFor)}*" styleClass="lotusFilter">
										<fmt:message key="label.search.results.refine" />
									</html:link>
								</c:when>
								<c:otherwise>
									<fmt:message key="label.search.results.refine.terms" />
								</c:otherwise>
							</c:choose>
					</span>
				</div>
				<script type="text/javascript">
				/* need more of a delay for showing this message for a11y and screen readers to pick it up. */
				setTimeout(
					function() {
						lconn.profiles.ProfilesCore.showInfoMsg("profileSearchMsgDiv", "info", dojo.byId("profilemessage_toomany").innerHTML);
					},
					2000
				);
				</script>
			</c:if>

			<script type="text/javascript">
				var searchManagementAction = function(params, fnCallback) {
					dojo.forEach(params, function(x) {
						params[x] = encodeURIComponent(params[x]);
					});
					
					var aLoc = (location.href + "?").split("?");

					params = dojo.mixin(dojo.queryToObject(aLoc[1]), params);
					
					params.page = parseInt(params.page, 10);
					if (isNaN(params.page) || params.page < 1) params.page = 1;
					var total = <c:out value="${searchResultsPage.numPages}"/>;
					if (params.page > total) params.page = total;
					
					if (	<c:out value="${searchResultsPage.page}"/> != params.page || 
							(typeof params.pageSize != "undefined" && <c:out value="${searchResultsPage.pageSize}"/> != params.pageSize) )
					{
						var url = aLoc[0] + "?" + dojo.objectToQuery(params);
						document.location.href = url;
					}
					
					//the page is reloading...   no need to call the fnCallback
					
					return false;
				}
			</script>

			<c:if test="${!isCnx8UI}">
				<tags:pager searchResultsPage="${searchResultsPage}" pagerNavFunction="searchManagementAction" />
			</c:if>
 
			<%@ include file="/WEB-INF/jsps/html/scenes/search/profilesSort.jsp" %>	
			<%@ include file="/WEB-INF/jsps/html/scenes/search/profileResultsMain2.jsp" %>
			<tags:pager searchResultsPage="${searchResultsPage}" pagerNavFunction="searchManagementAction" pagerAtBottom="true" />

		</c:otherwise>
	</c:choose>
	<div class="lotusFeeds"><a class="lotusAction lotusFeed" href="<profiles:searchResultsFeed />"><fmt:message key="feed_forProfiles" /></a></div>
	<span id="widget-container-2" class="widgetContainer"></span>
</div>
