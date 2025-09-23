<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2010, 2013                                    --%>
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
<fmt:setBundle basename="com.ibm.lconn.profiles.strings.uilabels" var="attributeLabels" scope="page" />
<table id="searchResultsTable" class="lotusTable" border="0" cellspacing="0" cellpadding="0" summary="">			
	<tbody>
		<c:set var="searchResult" value="true" />
		<c:forEach items="${searchResultsPage.results}" var="result" varStatus="status">
			<c:choose>
				<c:when test="${empty result.profileType || empty searchResultLayout[result.profileType]}">
					<c:set var="searchLayout" value="${searchResultLayout['default']}"/>
				</c:when>
				<c:otherwise>
					<c:set var="searchLayout" value="${searchResultLayout[result.profileType]}"/>
				</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${status.count == 1}">
					<c:set var="rowClass" value="lotusFirstRow" />
				</c:when>
				<c:when test="${(status.count % 2) == 0}">
					<c:set var="rowClass" value="lotusAltRow" />
				</c:when>
				<c:otherwise>
					<c:set var="rowClass" value="" />
				</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${result.isActive == false}">
					<c:set var="dimClass" value="lotusDim " />
				</c:when>
				<c:otherwise>
					<c:set var="dimClass" value="" />
				</c:otherwise>
			</c:choose>
			<tr class="${rowClass}">
				<c:forEach items="${searchLayout.columnConfig}" var="column" varStatus="colStatus">
					<td style='<c:if test="${colStatus.count == 1}">width:70px;</c:if>' class='${dimClass}<c:if test="${colStatus.count == 1}">lotusFirstCell</c:if><c:if test="${fn:length(searchLayout.columnConfig) == colStatus.count}">lotusLastCell</c:if>'>	
						<c:forEach items="${column.attributes}" var="attribute">
							<c:set var="attributeProperty">
								<profiles:advancedSearchNormalizer propertyName="${attribute.attributeId}" />
							</c:set>
							<c:set var="hideIfEmpty" value="${attribute.isHideIfEmpty}" />
							<c:set var="showLabel" value="${attribute.isShowLabel}" />
							<c:set var="appendHtml" value="${attribute.appendHTML}" />
							<c:set var="prependHtml" value="${attribute.prependHTML}" />
							<c:set var="hideIfEmpty" value="${attribute.isHideIfEmpty}" /> 
							<c:set var="showLabel" value="${attribute.isShowLabel}" /> 
							<c:set var="extensionAttr" value="${attribute.extensionAttribute}" /> 
							<c:if test="${attribute.isHcard}">
								<c:set var="attributeType" value="hcard" />
								<c:set var="userid" value="key" />
								<c:set var="uidAttribute" value="${result[attribute.uid]}" />
								<c:set var="useridAttribute" value="${result[attribute.userid]}" />
								<c:set var="emailAttribute" value="${result['email']}" />
								<%--	
								<br/>DEBUG:<br/>
								attributeType: ${attributeType }<br/>
								userid: ${userid }<br/>
								uidAttribute: ${uidAttribute }<br/>
								useridAttribute: ${useridAttribute }<br/>
								emailAttribute: ${emailAttribute }<br/>
								<br/>
								attribute.uid:${attribute.uid }<br/>
								attribute.userid:${attribute.userid}<br/>
								attribute: ${attribute }<br/>
								<br/>
								result: ${result }<br/>
								--%> 
							</c:if>
							<c:if test="${attribute.isSametimeLink}">
								<c:set var="attributeType" value="sametimeLink" />
							</c:if>
							<c:if test="${attribute.email}">
								<c:set var="attributeType" value="email" />
								<c:set var="emailAttribute" value="${result['email']}" />
							</c:if>
					  		<c:if test="${attribute.isLink}">
								<c:set var="attributeType" value="link" />
							</c:if> 
							<c:if test="${attribute.isPhoto}">
								<c:set var="attributeType" value="photo" />
								<c:set var="uidAttribute" value="${result[attribute.uid]}" />
								<c:set var="lastUpdate" value="${result.lastUpdate.time}" />
								<c:set var="altValue" value="${result.displayName}" />
							</c:if> 
							<c:choose>
								<c:when test="${extensionAttr}">
									<c:set var="attributeValue" value="${result[attribute.attributeId].value}" />
								</c:when>
								<c:otherwise>
									<c:set var="attributeValue" value="${result[attribute.attributeId]}" />
								</c:otherwise>
							</c:choose>										
							<c:if test="${attributeType == 'photo' || attributeValue != '' && attributeValue != null && attributeValue != ', ,' || (hideIfEmpty != 'true' && attributeValue == '')}">
								<c:if test="${attributeType != 'photo'}">
									<div>
								</c:if>
								<c:if test="${showLabel}">
									<label for="${attributeProperty}"><core:message bundle="${attributeLabels}" label="${attribute.label}"/></label>
								</c:if>
								<tags:attribute attribute="${attributeValue}" 
												attributeType="${attributeType}" 
												uidAttribute="${uidAttribute}" 
												useridAttribute="${useridAttribute}"
												emailAttribute="${emailAttribute}"
												showEmail="${showEmail}"
												searchResult="${searchResult}"
												lastUpdate="${lastUpdate}"
												altValue="${altValue}"
												prependHtml="${attribute.prependHTML}"
												appendHtml="${attribute.appendHTML}" />
								<c:if test="${attributeType != 'photo'}">
									</div>
								</c:if>
							</c:if>
							<c:remove var="attributeType" />
							<c:remove var="uidAttribute" />
							<c:remove var="useridAttribute" />
							<c:remove var="emailAttribute" />
							<c:remove var="hideIfEmpty" />
							<c:remove var="showLabel" />
							<c:remove var="hideIfEmpty" />
							<c:remove var="showLabel" />
							<c:remove var="extensionAttr" />
							<c:remove var="attributeValue"/>
						</c:forEach>
					</td>
				</c:forEach>
			</tr>
		</c:forEach>
	</tbody>
	<c:set var="searchResult" value="false" />
</table> <!-- end searchResults -->
