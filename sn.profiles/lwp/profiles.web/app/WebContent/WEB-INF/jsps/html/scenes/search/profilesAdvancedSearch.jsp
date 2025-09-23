<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2015                                    --%>
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
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper" %>

<c:set var="isNewUI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>"/>
<div id="divProfilesAdvancedSearch" class="lotusChunk lotusHidden cnx8advancedsearch" role="search" aria-labelledby="profilesAdvSearchHeader">	
	<div class="lotusHeader">
		<c:choose>
			<c:when test="${isNewUI}">
				<h2 id="profilesAdvSearchHeader"><fmt:message key="label.page.advancedsearch.heading" /></h2>								
			</c:when>
			<c:otherwise>
				<h1 id="profilesAdvSearchHeader"><fmt:message key="label.page.advancedsearch.heading" /></h1>
			</c:otherwise>
		</c:choose>
		<div class="lotusMeta"><fmt:message key="label.page.advancedsearch.description" /></div>
	</div>
	<div id="divAdvancedSearchHideFull">

		<c:choose>
			<c:when test="${isNewUI}">
				<a class="SearchHideFull lotusFormLabelFont" href="javascript:void(0);" onclick="lconn.profiles.profilesSearchPage.showSimpleUI();" role="button">
					<fmt:message key="label.search.profiles.profilesearch.adv.hidefull" />
				</a>						
			</c:when>
			<c:otherwise>
				<a href="javascript:void(0);" onclick="lconn.profiles.profilesSearchPage.showSimpleUI();" role="button">
					<fmt:message key="label.search.profiles.profilesearch.adv.hidefull" />
				</a>
			</c:otherwise>
		</c:choose>
	</div>
	<c:set value="com.ibm.lconn.profiles.strings.uilabels" var="attributeUILabels" scope="page" />
	<fmt:setBundle basename="com.ibm.lconn.profiles.strings.uilabels" var="attributeLabels" scope="page" />
	<div class="lotusChunk" style="width:500px;">
		<html:form styleId="advancedSearchForm" styleClass="lotusForm lotusFormPlain" action="/html/advancedSearch.do" method="GET"  onsubmit="return lconn.profiles.profilesSearchPage.submitAdvancedSearch(this);">
			<%-- if lang param is not present, redirect to add it will remove the keywork param. either add lang here or 
			     see code in  validateAdvSearchDataAndSubmit <input type="hidden" name="lang" value="<profiles:appLang />"/> --%>
			<fieldset>
				<legend class="lotusHidden"><profiles:getString key="label.advanced.searchForm.legend" bundle="${attributeUILabels}" /></legend><%-- a11y --%>
				<table id="tableAdvancedSearchFields" class="lotusFormTable" style="background-color:#fff;" role="presentation">
					<tbody>
						<tr class="">
						    <td class="${ isNewUI ? 'lotusFormLabel lotusFormLabelFont' : 'lotusFormLabel'}" width="125">
								<label for="keyword"><profiles:getString key="label.advanced.searchForm.attribute.keyword" bundle="${attributeUILabels}" /></label>
						    </td>
						    <td>
								<html:text property="keyword" styleClass="${ isNewUI ? 'lotusTextFocus cnx8lotusText bidiAware' : 'lotusText bidiAware'}" styleId="keyword" size="40" value="" />
						    </td>
						</tr>
						<%-- read advanced search config --%>
						<profiles:advancedSearchConfig />
						<c:forEach items="${advancedSearchConfig}" var="attribute" varStatus="status">
							<c:set var="attributeProperty">
								<profiles:advancedSearchNormalizer propertyName="${attribute}" />
							</c:set>
							<tr class="">
								<td class="${ isNewUI ? 'lotusFormLabel lotusFormLabelFont' : 'lotusFormLabel'}" width="125">
									<profiles:getString key="${advancedSearchConfigAttrs[attribute].label.key}" bundle="${attributeUILabels}" var="attrLabel" />
									<c:if test="${fn:startsWith(attrLabel,'!') && fn:endsWith(attrLabel,'!') && (empty advancedSearchConfigAttrs[attribute].label.bidref) && fn:startsWith(advancedSearchConfigAttrs[attribute].label.key, 'label.advanced.searchForm.attribute.')}">
										<profiles:getString key="label.advanced.searchForm.attribute.${attributeProperty}" bundle="${attributeUILabels}" var="attrLabel" />
									</c:if>
									<c:if test="${fn:startsWith(attrLabel,'!') && fn:endsWith(attrLabel,'!')}">
										<core:message bundle="${attributeLabels}" label="${advancedSearchConfigAttrs[attribute].label}" var="attrLabel"/>
									</c:if>
									<label for="${attributeProperty}"><c:out value="${attrLabel}"/></label>
								</td>
								<td>
									<html:text property="${attributeProperty}" styleClass="${ isNewUI ? 'lotusTextFocus cnx8lotusText bidiAware' : 'lotusText bidiAware'}" styleId="${attributeProperty}" size="40" value="${fn:escapeXml(param[attribute])}" />
								</td>
							</tr>
						</c:forEach>
						
						<jsp:include page="/WEB-INF/jsps/html/scenes/search/profilesAdvancedSearchAdditionalFields.jsp" />					
					</tbody>
				</table>
						
				<table id="tableAdvancedSearchFields3" class="lotusFormTable" style="background-color:#fff;" role="presentation">
					<tbody>
						<c:choose>
							<c:when test="${isNewUI}">
								<tr class="lotusFormFieldRow">
									<td class="lotusFormLabel" width="125">
										<fmt:message key="label.search.profiles.search" var="searchBtnText" />
										<fmt:message key="label.search.profiles.profilesearch.bycontactinfo.button.hint" var="searchBtnHint" />
										<html:submit value="${searchBtnText}" styleClass="lotusFormButton cnx8lotusFormButton" />
									</td>
									<td></td>
								</tr>
							</c:when>
							<c:otherwise>
								<tr class="lotusFormFieldRow">
									<td class="lotusFormLabel" width="125"></td>
									<td>
										<fmt:message key="label.search.profiles.search" var="searchBtnText" />
										<fmt:message key="label.search.profiles.profilesearch.bycontactinfo.button.hint" var="searchBtnHint" />
										<html:submit value="${searchBtnText}" styleClass="lotusFormButton" />
									</td>
								</tr>
							</c:otherwise>
						</c:choose>	
					</tbody>
				</table>
			</fieldset>
		</html:form>
	</div>
</div>
