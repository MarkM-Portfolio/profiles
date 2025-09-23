<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- HCL Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright HCL Technologies Limited 2008, 2022                     --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%--wscExemptBegin--%>
<%--wscExemptEnd--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" 	uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper"%>

<c:set var="isCnx8UI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>" scope="request"/>

<c:choose>
	<c:when test="${isCnx8UI}">
		<div class="lotusSort cnx8-sort-by-container">
	</c:when>
	<c:otherwise>
		<div class="lotusSort">
	</c:otherwise>
</c:choose>


	<span aria-hidden="true"><fmt:message key="tablePagingSortBy" /></span>
	<span class="lotusHidden" id="profileSortHeader">
		<c:choose>
			<c:when test="${sortKey == 'relevance'}">
				<fmt:message key="tablePagingCurrentlySortedByRelevenceDesc" />
			</c:when>
			<c:when test="${sortKey == 'displayName'}">
				<fmt:message key="tablePagingCurrentlySortedByDisplayNameDesc" />
			</c:when>
			<c:when test="${sortKey == 'last_name'}">
				<fmt:message key="tablePagingCurrentlySortedByLastNameDesc" />
			</c:when>
		</c:choose>
	</span>
	<ul class="lotusInlinelist" role="list" aria-labelledby="profileSortHeader">
	
		<%-- if we're not in a simple search, we allow sorting by relevance --%>
		<c:set var="li_rel">
			<c:if test="${searchType != 'simpleSearch'}">
				<c:choose>
					<c:when test="${sortKey != 'relevance'}">
						<li class="lotusFirst cnx8-sort-by-item" role="listitem"><a 
							title="<fmt:message key="tablePagingSortByRelevenceDesc"/>" 
							href="javascript:void(0);" 
							onclick="lconn.profiles.ProfilesCore.addParam('sortKey', 'relevance');"
						><fmt:message key="tablePagingSortByRelevence" /></a></li>
					</c:when>
					<c:otherwise>
						<li class="lotusFirst cnx8-sort-by-item-selected" role="listitem" aria-disabled="true"><fmt:message key="tablePagingSortByRelevence" /></li>
					</c:otherwise>
				</c:choose>
			</c:if>
		</c:set>
		
		<%-- if relevance li is blank, then we want the next li to be tagged as first --%>
		<c:set var="first_class">
			<c:if test="${fn:trim(li_rel) == ''}">
				lotusFirst
			</c:if>
		</c:set>

		<%-- display name --%>
		<c:set var="li_disp">
			<c:choose>
				<c:when test="${sortKey != 'displayName'}">
					<li role="listitem" class="${fn:trim(first_class)} cnx8-sort-by-item"><a 
						title="<fmt:message key="tablePagingSortByDisplayNameDesc"/>" 
						key="first_name" 
						href="javascript:void(0);" 
						onclick="lconn.profiles.ProfilesCore.addParam('sortKey', 'displayName');"
					><fmt:message key="tablePagingSortByDisplayName" /></a></li>
				</c:when>
				<c:otherwise>
					<li role="listitem" aria-disabled="true" class="${fn:trim(first_class)} cnx8-sort-by-item-selected"><fmt:message key="tablePagingSortByDisplayName" /></li>
				</c:otherwise>
			</c:choose>		
		</c:set>
		
		<%-- last name --%>
		<c:set var="li_ln">
			<c:choose>
				<c:when test="${sortKey != 'last_name'}">
					<li role="listitem" class="cnx8-sort-by-item"><a 
						title="<fmt:message key='tablePagingSortByLastNameDesc'/>" 
						key="last_name" 
						href="javascript:void(0);" 
						onclick="lconn.profiles.ProfilesCore.addParam('sortKey', 'last_name');"
					><fmt:message key="tablePagingSortByLastName" /></a></li>
				</c:when>
				<c:otherwise>
					<li role="listitem" aria-disabled="true" class="cnx8-sort-by-item-selected"><fmt:message key="tablePagingSortByLastName" /></li>
				</c:otherwise>
			</c:choose>		
		</c:set>
		
		<%--  We need to put all these on one line or else there will be spacing issues --%>
		<c:out value="${fn:trim(li_rel)}" escapeXml="false" /><c:out value="${fn:trim(li_disp)}" escapeXml="false" /><c:out value="${fn:trim(li_ln)}" escapeXml="false" />
		
	</ul>
</div>
