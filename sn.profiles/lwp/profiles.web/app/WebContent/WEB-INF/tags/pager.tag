<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- HCL Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright HCL Technologies Limited 2019, 2022                     --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%-- 5724-S68                                                          --%>
<%@ tag body-content="empty" %>

<%@ attribute name="searchResultsPage" required="true" rtexprvalue="true" type="com.ibm.peoplepages.data.SearchResultsPage" %>
<%@ attribute name="pagerAtBottom" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ attribute name="pagerNavFunction" required="true" rtexprvalue="true" type="java.lang.String" %>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<!-- Cnx8 UI -->
<c:choose>
	<c:when test="${isCnx8UI == true}">		
		<div class="lotusPaging" role="region" aria-label="${pagerLabel}">
			
			<div class="cnx8-horizontal-line"></div>

			<c:set var="numPagesAroundCurrent" value="3" />
			<c:set var="currentPage" value="${searchResultsPage.page}" />
			<c:set var="numPages" value="${searchResultsPage.numPages}" />
			<c:set var="parameters" value="${profiles:convertToMap(param)}" />
			<c:set var="pageSize" value="${searchResultsPage.pageSize}" />
			
			<c:set var="alt_itemsperpage_5">
				<fmt:message key="label.searchresults.itemsperpage.alt">
					<fmt:param value="5" />
				</fmt:message>
			</c:set>
			<c:set var="alt_itemsperpage_10">
				<fmt:message key="label.searchresults.itemsperpage.alt">
					<fmt:param value="10" />
				</fmt:message>
			</c:set>
			<c:set var="alt_itemsperpage_50">
				<fmt:message key="label.searchresults.itemsperpage.alt">
					<fmt:param value="50" />
				</fmt:message>
			</c:set>
			<c:set var="alt_itemsperpage_100">
				<fmt:message key="label.searchresults.itemsperpage.alt">
					<fmt:param value="100" />
				</fmt:message>
			</c:set>

			<c:set var="randomNumber" value="<%= Integer.toHexString((int) (Math.random() * 1000000)) %>" />
			<c:set var="pagerId" value="pager_${randomNumber}" />

			


			<ul class="lotusLeft lotusInlinelist" role="list">
				<c:set var="link_prev">
					<c:choose>
						<c:when test="${numPages == 0}">
							<li class="lotusFirst cnx8ui-icon-container" role="listitem" aria-disabled="true">
								<div class="cnx8-ui-pagination cnx8-ui-pagination-prev"></div>
							</li>
						</c:when>
						<c:when test="${currentPage != 1}">
							<li class="lotusFirst cnx8ui-icon-container" role="listitem">
							
								<a title="<fmt:message key="label.searchresults.previouslink.alt" />"
									aria-label="<fmt:message key="label.searchresults.previouslink.alt" />"
									class="pagerLink_" 
									linkAttr="{page:${currentPage-1}}" 
									href="javascript:void(0);" 
									onclick="return ${pagerNavFunction}({page:${currentPage-1}});">

										<div class="cnx8-ui-pagination cnx8-ui-pagination-prev"></div>
								</a>
							</li>
						</c:when>
						<c:otherwise>
							<li class="lotusFirst cnx8ui-icon-container" role="listitem" aria-disabled="true">
								<div class="cnx8-ui-pagination cnx8-ui-pagination-prev"></div>
							</li>
						</c:otherwise>
					</c:choose>
				</c:set>
				<c:set var="link_next">
					<c:choose>
						<c:when test="${numPages == 0}">
							<li class="cnx8ui-icon-container" role="listitem" role="listitem" aria-disabled="true"><div class="cnx8-ui-pagination cnx8-ui-pagination-next"></div></li>
						</c:when>
						<c:when test="${currentPage != numPages}">
							<li class="cnx8ui-icon-container" role="listitem"><a 
								title="<fmt:message key="label.searchresults.nextlink.alt" />"
								aria-label="<fmt:message key="label.searchresults.nextlink.alt" />"
								class="pagerLink_" 
								linkAttr="{page:${currentPage+1}}" 
								href="javascript:void(0);" 
								onclick="return ${pagerNavFunction}({page:${currentPage+1}});"
							><div class="cnx8-ui-pagination cnx8-ui-pagination-next"></div></a></li>
						</c:when>
						<c:otherwise>
							<li class="cnx8ui-icon-container" role="listitem" aria-disabled="true"><div class="cnx8-ui-pagination cnx8-ui-pagination-next"></div></li>
						</c:otherwise>
					</c:choose>
				</c:set>
				<c:set var="link_first">
					<c:choose>
						<c:when test="${numPages == 0}">
							<li class="cnx8ui-icon-container" role="listitem" aria-disabled="true">
								<div class="cnx8-ui-pagination cnx8-ui-pagination-first"></div>
							</li>
						</c:when>
						<c:when test="${currentPage != 1}">
							<li class="cnx8ui-icon-container" role="listitem">
							
								<a title="<fmt:message key="label.searchresults.firstlink.alt" />"
									aria-label="<fmt:message key="label.searchresults.firstlink.alt" />"
									class="pagerLink_" 
									linkAttr="{page:1}" 
									href="javascript:void(0);" 
									onclick="return ${pagerNavFunction}({page:1});">

										<div class="cnx8-ui-pagination cnx8-ui-pagination-first"></div>
								</a>
							</li>
						</c:when>
						<c:otherwise>
							<li class="cnx8ui-icon-container" role="listitem" aria-disabled="true">
								<div class="cnx8-ui-pagination cnx8-ui-pagination-first"></div>
							</li>
						</c:otherwise>
					</c:choose>
				</c:set>
				<c:set var="link_last">
					<c:choose>
						<c:when test="${numPages == 0}">
							<li class="cnx8ui-icon-container" role="listitem" role="listitem" aria-disabled="true"><div class="cnx8-ui-pagination cnx8-ui-pagination-last"></div></li>
						</c:when>
						<c:when test="${currentPage != numPages}">
							<li class="cnx8ui-icon-container" role="listitem"><a 
								title="<fmt:message key="label.searchresults.lastlink.alt" />"
								aria-label="<fmt:message key="label.searchresults.lastlink.alt" />"
								class="pagerLink_" 
								linkAttr="{page:${numPages}}" 
								href="javascript:void(0);" 
								onclick="return ${pagerNavFunction}({page:${numPages}});"
							><div class="cnx8-ui-pagination cnx8-ui-pagination-last"></div></a></li>
						</c:when>
						<c:otherwise>
							<li class="cnx8ui-icon-container" role="listitem" aria-disabled="true"><div class="cnx8-ui-pagination cnx8-ui-pagination-last"></div></li>
						</c:otherwise>
					</c:choose>
				</c:set>					
				<c:out value="${fn:trim(link_first)}" escapeXml="false" />
				<c:out value="${fn:trim(link_prev)}" escapeXml="false" />

				<%-- List of pages --%>
				<ul class="lotusInlinelist cnx8-ui-pull-to-center" role="list">
					
					<c:set var="drawPagingEllipsis" value="true" />
					
					<profiles:profileSearchPageHelper 
							currentPage="${currentPage}"
							numPagesAroundCurrent="${numPagesAroundCurrent}"
							numPages="${numPages}">
						<c:choose>
							<c:when test="${page == 1}">
								<c:set var="liClass" value="lotusFirst" />
							</c:when>
							<c:otherwise>
								<c:set var="liClass" value="" />
							</c:otherwise>
						</c:choose>
						<c:choose>
							<c:when test="${ page eq 1 || page eq numPages || ( page ge ( currentPage - numPagesAroundCurrent ) && page le ( currentPage + numPagesAroundCurrent ) ) || ( (page eq 2) && ( page eq ( currentPage - numPagesAroundCurrent - 1) ) ) || ( (page eq (numPages-1) ) && ( page eq ( currentPage + numPagesAroundCurrent + 1 ) ) ) }">
								<c:set var="drawPagingEllipsis" value="true" />
								<c:choose>
									<c:when test="${page == currentPage}">
										<li class="${liClass} cnx8-ui-page-number cnx8-ui-current-page-number" role="listitem" aria-disabled="true">${page}</li>
									</c:when>
									<c:otherwise>
										<c:set var="alt_gotopage">
											<fmt:message key="label.searchresults.pagelink.alt">
												<fmt:param value="${page}" />
											</fmt:message>
										</c:set>									
										<li class="${liClass} cnx8-ui-page-number" role="listitem">
											<a title="${alt_gotopage}" aria-label="${alt_gotopage}" class="pagerLink_" linkAttr="{page:${page}}" href="javascript:void(0);" onclick="return ${pagerNavFunction}({page:${page}});">
												${page}
											</a>
										</li>
									</c:otherwise>
								</c:choose>
							</c:when>
							<c:otherwise>
								<c:if test="${drawPagingEllipsis}">
									<li class="${liClass}" role="listitem" aria-disabled="true">
										<c:out value="..." />
									</li>
									<c:set var="drawPagingEllipsis" value="false" />
								</c:if>
							</c:otherwise>
						</c:choose>
					</profiles:profileSearchPageHelper>
				</ul>

				<c:out value="${fn:trim(link_next)}" escapeXml="false" />
				<c:out value="${fn:trim(link_last)}" escapeXml="false" />
				
			</ul>

			<div class="cnx8-ui-pull-to-center" style="float:right">
				<span aria-hidden="true"><fmt:message key="tablePagingResultsPerPage"/></span>
				
				<select id="${pagerId}">
					<option value="5" title="${alt_itemsperpage_5}" aria-label="${alt_itemsperpage_5}" <c:choose><c:when test="${pageSize == 5}"> selected</c:when></c:choose>>5</option>
					<option value="10" title="${alt_itemsperpage_10}" aria-label="${alt_itemsperpage_10}" <c:choose><c:when test="${pageSize == 10}"> selected</c:when></c:choose>>10</option>
					<option value="50" title="${alt_itemsperpage_50}" aria-label="${alt_itemsperpage_50}" <c:choose><c:when test="${pageSize == 50}"> selected</c:when></c:choose>>50</option>
					<option value="100" title="${alt_itemsperpage_100}" aria-label="${alt_itemsperpage_100}" <c:choose><c:when test="${pageSize == 100}"> selected</c:when></c:choose>>100</option>
				</select>

<%-- add a change listener to compute the corrcet targegt URL and redirect to it --%>
				<script>
(function() { 
	var DEFAULT_PAGESIZE = 5;

	function createLinkFromValue(value) {
		var aLoc = (document.location + "?").split("?");
		var oldParams = dojo.queryToObject(aLoc[1]);
		var newPageSize = DEFAULT_PAGESIZE;
		var newParams = {};
		try {
			newPageSize = +value || DEFAULT_PAGESIZE;
		} catch (ee) {
		}
		newPageSize = Math.max(newPageSize, DEFAULT_PAGESIZE);

		if (typeof lastParams === "object") {
			oldParams = dojo.mixin(oldParams, lastParams);
		}

		newParams =	dojo.mixin(oldParams, {page: 1, pageSize: newPageSize});

		return aLoc[0] + "?" + dojo.objectToQuery(newParams);
	}
	
    const pagerSelect = document.getElementById("${pagerId}");
    pagerSelect.addEventListener("change", e => {
        window.location = createLinkFromValue(e.target.value);
    });
})();
					</script>
			</div>
			
		</div>
	</c:when>

	<c:otherwise>
		<!-- paging -->
		<c:set var="pagerLabel">
			<c:choose>
				<c:when test="${pagerAtBottom == true}">
					<fmt:message key="tableBottomPagingLabel" />
				</c:when>
				<c:otherwise>
					<fmt:message key="tablePagingLabel" />
				</c:otherwise>		
			</c:choose>
			
			<fmt:message key="label.searchresults.pageinfo.alt">
				<c:choose>
					<c:when test="${numPages == 0}">
						<fmt:param value="0" />
					</c:when>
					<c:otherwise>
						<fmt:param value="${searchResultsPage.start}" />
					</c:otherwise>
				</c:choose>
				<fmt:param value="${searchResultsPage.end}" />
				<fmt:param value="${searchResultsPage.totalResults}" />
			</fmt:message>	
		</c:set>

		<div class="lotusPaging" role="region" aria-label="${pagerLabel}">
			<c:set var="numPagesAroundCurrent" value="3" />
			<c:set var="currentPage" value="${searchResultsPage.page}" />
			<c:set var="numPages" value="${searchResultsPage.numPages}" />
			<c:set var="parameters" value="${profiles:convertToMap(param)}" />
			<c:set var="pageSize" value="${searchResultsPage.pageSize}" />

			<c:choose>
				<c:when test="${pagerAtBottom == true}">

					<c:set var="alt_itemsperpage_5">
						<fmt:message key="label.searchresults.itemsperpage.alt">
							<fmt:param value="5" />
						</fmt:message>
					</c:set>
					<c:set var="alt_itemsperpage_10">
						<fmt:message key="label.searchresults.itemsperpage.alt">
							<fmt:param value="10" />
						</fmt:message>
					</c:set>
					<c:set var="alt_itemsperpage_50">
						<fmt:message key="label.searchresults.itemsperpage.alt">
							<fmt:param value="50" />
						</fmt:message>
					</c:set>
					<c:set var="alt_itemsperpage_100">
						<fmt:message key="label.searchresults.itemsperpage.alt">
							<fmt:param value="100" />
						</fmt:message>
					</c:set>
					<div class="lotusLeft">
						<span aria-hidden="true"><fmt:message key="tablePagingShow" /></span>
						<ul class="lotusInlinelist" role="list">
							<c:choose>
								<c:when test="${pageSize == 5}">
									<li class="lotusFirst" role="listitem" aria-disabled="true">5</li>
								</c:when>
								<c:otherwise>
									<li class="lotusFirst" role="listitem">
										<a title="${alt_itemsperpage_5}" aria-label="${alt_itemsperpage_5}" class="pagerLink_" linkAttr="{pageSize:5, page: 1}" href="javascript:void(0);" onclick="return ${pagerNavFunction}({pageSize: 5, page: 1}, reloadPageLinks_);">5</a>
									</li>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${pageSize == 10}">
									<li role="listitem" aria-disabled="true">10</li>
								</c:when>
								<c:otherwise>
									<li role="listitem">
										<a title="${alt_itemsperpage_10}" aria-label="${alt_itemsperpage_10}" class="pagerLink_" linkAttr="{pageSize:10, page: 1}" href="javascript:void(0);" onclick="return ${pagerNavFunction}({pageSize: 10, page: 1}, reloadPageLinks_);">10</a>
									</li>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${pageSize == 50}">
									<li role="listitem" aria-disabled="true">50</li>
								</c:when>
								<c:otherwise>
									<li role="listitem">
										<a title="${alt_itemsperpage_50}" aria-label="${alt_itemsperpage_50}" class="pagerLink_" linkAttr="{pageSize:50, page: 1}" href="javascript:void(0);" onclick="return ${pagerNavFunction}({pageSize: 50, page: 1}, reloadPageLinks_);">50</a>
									</li>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${pageSize == 100}">
									<li role="listitem" aria-disabled="true">100</li>
								</c:when>
								<c:otherwise>
									<li role="listitem">
										<a title="${alt_itemsperpage_100}" aria-label="${alt_itemsperpage_100}" class="pagerLink_" linkAttr="{pageSize:100, page: 1}" href="javascript:void(0);" onclick="return ${pagerNavFunction}({pageSize: 100, page: 1}, reloadPageLinks_);">100</a>
									</li>
								</c:otherwise>
							</c:choose>			
						</ul>
						<span aria-hidden="true"><fmt:message key="tablePagingItermsPerPage" /></span>
					</div>

				</c:when>
				<c:otherwise>
					<c:set var="pageInfo">
						<fmt:message key="label.searchresults.pageinfo">
							<c:choose>
								<c:when test="${numPages == 0}">
									<fmt:param value="0" />
								</c:when>
								<c:otherwise>
									<fmt:param value="${searchResultsPage.start}" />
								</c:otherwise>
							</c:choose>
							<fmt:param value="${searchResultsPage.end}" />
							<fmt:param value="${searchResultsPage.totalResults}" />
						</fmt:message>
					</c:set>		
					<div class="lotusLeft" aria-hidden="true">
						<c:out value="${pageInfo}"/>
					</div>
				</c:otherwise>
			</c:choose>
			<ul class="lotusRight lotusInlinelist" role="list">
				<c:set var="link_prev">
					<c:choose>
						<c:when test="${numPages == 0}">
							<li class="lotusFirst" role="listitem" aria-disabled="true"><fmt:message key="label.searchresults.previouslink" /></li>
						</c:when>
						<c:when test="${currentPage != 1}">
							<li class="lotusFirst" role="listitem"><a 
								title="<fmt:message key="label.searchresults.previouslink.alt" />"
								aria-label="<fmt:message key="label.searchresults.previouslink.alt" />"
								class="pagerLink_" 
								linkAttr="{page:${currentPage-1}}" 
								href="javascript:void(0);" 
								onclick="return ${pagerNavFunction}({page:${currentPage-1}}, reloadPageLinks_);"
							><fmt:message key="label.searchresults.previouslink" /></a></li>
						</c:when>
						<c:otherwise>
							<li class="lotusFirst" role="listitem" aria-disabled="true"><fmt:message key="label.searchresults.previouslink" /></li>
						</c:otherwise>
					</c:choose>
				</c:set>
				<c:set var="link_next">
					<c:choose>
						<c:when test="${numPages == 0}">
							<li role="listitem" role="listitem" aria-disabled="true"><fmt:message key="label.searchresults.nextlink" /></li>
						</c:when>
						<c:when test="${currentPage != numPages}">
							<li role="listitem"><a 
								title="<fmt:message key="label.searchresults.nextlink.alt" />"
								aria-label="<fmt:message key="label.searchresults.nextlink.alt" />"
								class="pagerLink_" 
								linkAttr="{page:${currentPage+1}}" 
								href="javascript:void(0);" 
								onclick="return ${pagerNavFunction}({page:${currentPage+1}}, reloadPageLinks_);"
							><fmt:message key="label.searchresults.nextlink" /></a></li>
						</c:when>
						<c:otherwise>
							<li role="listitem" aria-disabled="true"><fmt:message key="label.searchresults.nextlink" /></li>
						</c:otherwise>
					</c:choose>
				</c:set>
				
				<c:out value="${fn:trim(link_prev)}" escapeXml="false" /><c:out value="${fn:trim(link_next)}" escapeXml="false" />
				
			</ul>
			
			
			<c:choose>
				<c:when test="${pagerAtBottom == true}">
					<c:if test="${numPages >1}">
						<span class="lotusInlinelist">
						
							<label for="jumpToPageNumber" class="lotusHidden"><fmt:message key="tablePagingJumpToPage" /></label>
							<span id="jumpToPageNumberDesc" class="lotusHidden"><fmt:message key="tablePagingJumpToPageDesc" /></span>
							<fmt:message key="tablePagingJumpToPageControl">
								<fmt:param>
									<input 
										id="jumpToPageNumber"
										aria-describedby="jumpToPageNumberDesc"
										type="text" 
										name="pageNumber" 
										value="${currentPage}" 
										onblur="var n=Math.max(1, Math.min(this.value, ${numPages})); ${pagerNavFunction}({page:(isNaN(n)?1:n)}, reloadPageLinks_)" 
										onkeypress="if (event.keyCode == 13) {var n=Math.max(1, Math.min(this.value, ${numPages})); ${pagerNavFunction}({page:(isNaN(n)?1:n)}, reloadPageLinks_)}"
									/>						
								</fmt:param>
								<fmt:param value="${numPages}" />
							</fmt:message>					
							
						</span>
					</c:if>
				</c:when>
				<c:otherwise>
					<div>
						<span aria-hidden="true"><fmt:message key="label.searchresults.pagelabel" /></span>
						<ul class="lotusInlinelist" role="list">
							
							<c:set var="drawPagingEllipsis" value="true" />
							
							<profiles:profileSearchPageHelper 
									currentPage="${currentPage}"
									numPagesAroundCurrent="${numPagesAroundCurrent}"
									numPages="${numPages}">
								<c:choose>
									<c:when test="${page == 1}">
										<c:set var="liClass" value="lotusFirst" />
									</c:when>
									<c:otherwise>
										<c:set var="liClass" value="" />
									</c:otherwise>
								</c:choose>
								<c:choose>
									<c:when test="${ page eq 1 || page eq numPages || ( page ge ( currentPage - numPagesAroundCurrent ) && page le ( currentPage + numPagesAroundCurrent ) ) || ( (page eq 2) && ( page eq ( currentPage - numPagesAroundCurrent - 1) ) ) || ( (page eq (numPages-1) ) && ( page eq ( currentPage + numPagesAroundCurrent + 1 ) ) ) }">
										<c:set var="drawPagingEllipsis" value="true" />
										<c:choose>
											<c:when test="${page == currentPage}">
												<li class="${liClass}" role="listitem" aria-disabled="true">${page}</li>
											</c:when>
											<c:otherwise>
												<c:set var="alt_gotopage">
													<fmt:message key="label.searchresults.pagelink.alt">
														<fmt:param value="${page}" />
													</fmt:message>
												</c:set>									
												<li class="${liClass}" role="listitem">
													<a title="${alt_gotopage}" aria-label="${alt_gotopage}" class="pagerLink_" linkAttr="{page:${page}}" href="javascript:void(0);" onclick="return ${pagerNavFunction}({page:${page}}, reloadPageLinks_);">
														${page}
													</a>
												</li>
											</c:otherwise>
										</c:choose>
									</c:when>
									<c:otherwise>
										<c:if test="${drawPagingEllipsis}">
											<li class="${liClass}" role="listitem" aria-disabled="true">
												<c:out value="..." />
											</li>
											<c:set var="drawPagingEllipsis" value="false" />
										</c:if>
									</c:otherwise>
								</c:choose>
							</profiles:profileSearchPageHelper>
						</ul>
					</div>
				</c:otherwise>
			</c:choose>
				
		</div>
<%-- This code goes through the links on the page and sets the href to a real link in case the users opens the link in a new window or tab --%>
<script type="text/javascript">
(function() {
	var loadLinksAttr_ = function(lastParams) {
		var aLoc = (document.location + "?").split("?");
		var oldParams = dojo.queryToObject(aLoc[1]);
		
		dojo.query("a.pagerLink_").forEach(function(linknode) {
			if (dojo.hasAttr(linknode, "linkAttr")) {
				var newParams = {};
				try {
					eval("newParams = " + dojo.attr(linknode, "linkAttr") + ";");
				} catch (ee) {
				}
				
				if (typeof lastParams === "object") {
					oldParams = dojo.mixin(oldParams, lastParams);
				}

				newParams =	dojo.mixin(oldParams, newParams);
				
				dojo.attr(linknode, "href", aLoc[0] + "?" + dojo.objectToQuery(newParams));
			}
		});
	};
	
	
	window.reloadPageLinks_ = function(params) {
		loadLinksAttr_(params);
	};
	
	dojo.addOnLoad(function() {
		loadLinksAttr_(window.lastActionParams_);
	});
	
})();
</script>

	</c:otherwise>
</c:choose>

