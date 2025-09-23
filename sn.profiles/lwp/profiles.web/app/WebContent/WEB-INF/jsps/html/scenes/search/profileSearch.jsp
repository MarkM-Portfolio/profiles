
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

<%--
    @author <a href="mailto:testrada@us.ibm.com">Tony Estrada</a>
--%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="stripes" 	uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="lc-ui"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<profiles:isAdvancedSearchEnabled var="isAdvancedSearchEnabled"/>
<c:if test="${empty profilesSvcLocation}">
	<c:set value="${pageContext.request.contextPath}" var="profilesSvcLocation"/>
</c:if>
<div id="divProfilesTypeaheadSearch" class="lotusChunk">	
	<div class="lotusHeader">
		<h1 id="profilesSimpleSearchHeader"><fmt:message key="label.page.typeaheadsearch.heading" /></h1>
		<div id="profilesSimpleSearchDescription" class="lotusMeta">
			<fmt:message key="label.page.typeaheadearch.description" />
		</div>
	</div>
	<fmt:setBundle basename="com.ibm.lconn.profiles.strings.uilabels" var="attributeLabels" scope="page" />
	<div id="divProfilesNameSearch" role="search" aria-labelledby="profilesSimpleSearchHeader" aria-describedby="profilesSimpleSearchDescription" class="lotusChunk nameTypeaheadControl">
		<form id="formProfilesSimpleSearch" class="lotusForm lotusFormPlain" action="${profilesSvcLocation}/html/simpleSearch.do" method="GET" onsubmit="return lconn.profiles.profilesSearchPage.submitSimpleSearch(this);">	
			<input type="hidden" name="searchBy" value="name"/>
			<input type="hidden" name="lang" value="<profiles:appLang />"/>
			<input type="hidden" name="searchFor" value=""/>
			<label for="profilesNameSearchField">
				<fmt:message key="label.search.profiles.searchfieldlabel" />
			</label>
	 		<textarea id="profilesNameSearchField" class="lotusHidden bidiAware"></textarea>
	 		<fmt:message key="label.search.profiles.search" var="searchText"/>
			<span id="profilesSimpleSearchButton" title="${searchText}" class="lotusSearch lotusBtnImg">
				<input type="image" src="<lc-ui:blankGif />" class="lotusSearchButton" title="${searchText}" alt="${searchText}" onclick="lconn.profiles.profilesSearchPage.submitSimpleSearch();"/>
				<button class="lotusAltText">${searchText}</button>
			</span>
		</form>
	</div>
	<c:if test="${isAdvancedSearchEnabled == 'true'}">	<%-- More search options aren't available for SmartCloud/MT (yet) --%>
		<div class="lotusChunk">
			<a href="javascript:void(0);" onclick="lconn.profiles.profilesSearchPage.showAdvancedUI();" role="button">
				<fmt:message key="label.search.profiles.profilesearch.adv.showfull" />
			</a>
		</div>
	</c:if>
	<div id="profilesNameSearchField_popup">
	<%-- this section to be populated by ajax search results --%>
	</div>
</div>
<!-- 
ptas_fireOnKeys:[${ptas_fireOnKeys}]  
ptas_delayBetweenKeys:[${ptas_delayBetweenKeys}] 
maxResults:[${ptas_maxResults}] 
liveNameSupport:[${ptas_liveNameSupport}] 
expandThumbnails:[${ptas_expandThumbnails}] 
blankOnEmpty:[${ptas_blankOnEmpty}] 
-->
<script type="text/javascript">


// metrics variables, it will be used in tiles/footer.jsp
readTrackerMetrics = {};  
readTrackerMetrics.itemType = "SearchView";
readTrackerMetrics.contentId = profilesData.config.pageId;
readTrackerMetrics.contentTitle = profilesData.config.pageId;


dojo.addOnLoad(
	function() {
		if (dojo.exists("lconn.profiles.profilesSearchPage")) {
			
			lconn.profiles.profilesSearchPage.init({
				showAdvanced: ((!profilesData.config.isAdvancedSearchEnabled)?'false':'<lc-ui:escapeJavaScript>${fn:escapeXml(param.showfulladv)}</lc-ui:escapeJavaScript>')
			});

			lconn.profiles.profilesSearchPage.loadSearchTilesControl(
				{
					lang: "<profiles:appLang />",
					lastMod: profilesData.config.profileLastMod,
					profilesSvcLocation: "${profilesSvcLocation}",
					count: ${ptas_maxResults},
					minChars: ${ptas_fireOnKeys},
					searchDelay: ${ptas_delayBetweenKeys},
					liveNameSupport: ${ptas_liveNameSupport},
					expandThumbnails: ${ptas_expandThumbnails},
					showEmail: ${showEmail},
					defaultValue: ((profilesData.config.isMultiTenant)?'<lc-ui:escapeJavaScript>${fn:escapeXml(param.displayName)}</lc-ui:escapeJavaScript>':''),
					messages: {
						photoAltText: "<fmt:message key="label.searchresults.photo.altText" />",
						inactiveText: "<fmt:message key="label.inactive.user.msg" />",
						noResultsText: "<fmt:message key="label.searchresults.noresults" />",
						resultsHeadingText: "<fmt:message key="label.searchresults.people.heading" />"
					}
				}
			);
		}
		
	}
);

</script>
