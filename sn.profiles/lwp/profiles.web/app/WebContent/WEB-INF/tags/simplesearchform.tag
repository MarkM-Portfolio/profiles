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

<%-- 5724-S68                                                          --%>
<%@
tag body-content="empty" %><%@ 
attribute name="sSvcLocation" required="false" rtexprvalue="true" %><%@ 
attribute name="divContainer" required="true" rtexprvalue="false" %>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>

<profiles:isLotusLive var="isLotusLive"/>

<script type="text/javascript">
if (typeof(require) != "undefined") {
	require(["dojo/dom", "dojo/_base/array", "dojo/domReady!"], function(dom, array) {

		var opts = {
			searchContextPath: '<c:out value="${sSvcLocation}"/>',
			queryValue: "",
			localOptions: []
		};

		<c:choose>
			<c:when test="${isLotusLive != 'true'}">
			
				opts.localOptions.push({
					label: '<core:escapeJavaScript><fmt:message key="search.dropdown.byname" /></core:escapeJavaScript>',
					scope: 'simple',
					action: '<c:url value="/html/simpleSearch.do?searchBy=name&searchFor={searchTerm}" />',
					valueReplaceParam: "searchTerm"
				});
				opts.localOptions.push({
					label: '<core:escapeJavaScript><fmt:message key="search.dropdown.bykeyword" /></core:escapeJavaScript>',
					scope: 'keyword',
					action: '<c:url value="/html/keywordSearch.do?keyword={searchTerm}" />',
					valueSearchParam: "keyword",
					valueReplaceParam: "searchTerm"

				});
				
			</c:when>
			<c:otherwise>
				<profiles:checkPermission feature="profile.search" permission="profile.search.contacts.view" checkTarget="false" var="canSearchContacts" />
				<profiles:checkPermission feature="profile.search" permission="profile.search.guests.view" checkTarget="false" var="canSearchGuests" />
				<profiles:checkPermission feature="profile.search" permission="profile.search.organizations.view" checkTarget="false" var="canSearchOrganizations" />
				
				opts.localOptions.push({
					label: dojo.trim('<core:escapeJavaScript><fmt:message key="search.dropdown.orgDirectory"><fmt:param value="${profiles:getCurrentUserTenantName()}"/></fmt:message></core:escapeJavaScript>'),
					feature: 'directory',
					defaultOption: true,
					disableSearchBarMode: true,
					action: '<c:url value="/html/searchProfiles.do" />#simpleSearch&q={searchTerm}',
					valueSearchParam: null,
					valueReplaceParam: "searchTerm"
				});
				
				if (dojo.exists("gllConnectionsData.srvUrls") && gllConnectionsData.srvUrls['sc-search']) {
					var searchTemplateUrl = gllConnectionsData.srvUrls['sc-search'];
					<c:if test="${canSearchContacts == 'true'}">
						opts.localOptions.push({
							label: '<core:escapeJavaScript><fmt:message key="search.dropdown.contacts" /></core:escapeJavaScript>',
							scope: 'contacts',
							allowTypeahead: false,
							action: searchTemplateUrl.replace("{type}", "contact"),
							valueSearchParam: null,
							valueReplaceParam: "searchTerm"
						});
					</c:if>
					
					<c:if test="${canSearchGuests == 'true'}">
						opts.localOptions.push({
							label: '<core:escapeJavaScript><fmt:message key="search.dropdown.guests" /></core:escapeJavaScript>',
							scope: 'guests',
							allowTypeahead: false,
							action: searchTemplateUrl.replace("{type}", "guest"),
							valueSearchParam: null,
							valueReplaceParam: "searchTerm"
						});
					</c:if>
					
					<c:if test="${canSearchOrganizations == 'true'}">
						opts.localOptions.push({
							label: '<core:escapeJavaScript><fmt:message key="search.dropdown.organizations" /></core:escapeJavaScript>',
							scope: 'organizations',
							allowTypeahead: true,
							action: searchTemplateUrl.replace("{type}", "org"),
							valueSearchParam: null,
							valueReplaceParam: "searchTerm"
						});
					</c:if>
				}
	
			</c:otherwise>
		</c:choose>
	

		var cntr = dom.byId('${divContainer}');
		if (cntr) {
			var useNewUI = false;
			try {
				useNewUI = lconn.core.config.features("search-history-view-ui");
			} catch (e) {}
			
			//code detects whether we are on a search results page
			array.some(opts.localOptions, function(localOption) {
				var act = localOption.action.replace(new RegExp("\{" + localOption.valueReplaceParam + "\}", "gi"), "");
				var idx = window.location.href.indexOf(act);
				if (idx > -1) {
					//decrypt url before getting query value
					var urlDecrypted = decodeURIComponent(window.location.href);
					var tempValue = urlDecrypted.substring(idx + act.length) + "&";
					opts.queryValue = tempValue.substring(0, tempValue.indexOf("&"));
					if (localOption.disableSearchBarMode !== true) {
						opts.isSearchBarModeOn = true;
					}
					return true;
				}
				
				return false;
			});
			
			if (useNewUI) {
				require(["ic-search/searchPanel/SearchPaneManager", "dojo/domReady!"], function(SearchPaneManager) {
                    var searchPaneManager = new SearchPaneManager(opts, cntr);
                    if (typeof opts.queryValue === "string" && opts.queryValue.length > 0) {
                        searchPaneManager.searchBar.setTextValue(opts.queryValue);
                    }
                });
			}
			else {
				new lconn.profiles.SearchBar(opts, cntr);
			}
		}
	});
}

</script>

