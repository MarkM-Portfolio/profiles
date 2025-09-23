<%@ page contentType="text/html;charset=UTF-8" session="false"%>

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

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %><%@ 
	taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %><%@ 
	taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %><%@ 
	taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %><%@ 
	taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper" %>

<c:url var="profilesImgUrl" scope="page" value="/nav/common/styles/images"/>
<c:set var="isNewUI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>"/>

	<c:choose>
		<c:when test="${isNewUI}">

<div class="lotusDialogBorder cnx8lotusDialogBorder">
	<form id="exportVcardDialogForm" class="lotusDialog lotusForm" action="<c:url value="/vcard/profile.do"/>" method="get" aria-describedby="exportVcardDlgTitle">
		<div class="lotusDialogHeader">
			<h1 class="lotusHeading radioGroupLabel" id="exportVcardDlgTitle"><fmt:message key="label.vcard.header.exportVcard"/></h1>
			<a role="button" class="lotusRight lotusDialogClose" href="javascript:;" onclick="hideVcardExport();return false;">
				<img class="imgBackgroundPosition" src="<core:blankGif />" role="presentation" alt='' />
				<span class="lotusAltText">X</span>
			</a>
		</div>
		<div class="lotusDialogContent">
			<fieldset>
				<div class="radioGroupLabelDiv">
					<span class="radioGroupLabel" id="radio_group_label">
						<fmt:message key="label.vcard.selectEncoding"/>
						<a id="exportVcardHelpLink"  href="javascript:;"></a>
					</span>
				</div>
				<div role="radiogroup" class ="" aria-labelledby="radio_group_label">
					<input type="hidden" name="key" value="${key}"/>
					<input type="hidden" name="lastMod" value="${lastUpdate}"/>
					<c:set var="first" value="true"/>
					<c:forEach items="${applicationScope.profilesConfig.UIConfig.VCardExportConfig.charsets}" var="cs" varStatus="status">
						<div>
							<input role="radio" type="radio" name="encoding" class="lotusCheckbox" value="${cs.name}" id="defaultEncoding${status.count}" 
								<c:choose>
									<c:when test="${first}">
										checked="true" aria-checked="true"
									</c:when>
									<c:otherwise>
										aria-checked="false"
									</c:otherwise>
								</c:choose>
								onchange="dojo.query('input[name=&quot;encoding&quot;]').forEach(function(node) {dojo.attr(node, 'aria-checked',((node.checked)?'true':'false'))});"
							/>
							<span class="lotusCheckbox"><label for="defaultEncoding${status.count}"><core:message label="${cs.label}"/></label></span>
						</div>
						<c:set var="first" value="false"/>
					</c:forEach>
				</div>

			</fieldset>
		</div><%--end lotusDialogContent--%>
		<div class="lotusDialogFooter">
			<input value='<fmt:message key="label.vcard.cancel"/>' class="lotusFormButton lotusFormButtonDisabled radioGroupLabel" onclick="hideVcardExport();return false;" type="button" />
			<input id="vcardDlgSubmit" value="<core:escapeXml><fmt:message key="label.vcard.download"/></core:escapeXml>" class="lotusFormButton"  onclick="hideVcardExportForDL();return true;" type="submit" /> 
		</div>
	</form>
</div> 

</c:when>
<c:otherwise>
	<div class="lotusDialogBorder">
		<form id="exportVcardDialogForm" class="lotusDialog lotusForm" action="<c:url value="/vcard/profile.do"/>" method="get" aria-describedby="exportVcardDlgTitle">
			<div class="lotusDialogHeader">
				<h1 class="lotusHeading" id="exportVcardDlgTitle"><fmt:message key="label.vcard.header.exportVcard"/></h1>
				<a title='<fmt:message key="label.vcard.cancel"/>' role="button" class="lotusRight lotusDialogClose" href="javascript:;" onclick="hideVcardExport();return false;">
					<img src="<core:blankGif />" role="presentation" alt='' />
					<span class="lotusAltText">X</span>
				</a>
			</div>	
			<div class="lotusDialogContent">
				<a id="mainContent" name="mainContent"></a><!-- skip links for accessibility -->
				<fieldset>
					<legend class="lotusHidden"><fmt:message key="label.vcard.selectEncoding"/></legend><%-- a11y --%>
					<table role="presentation" class="lotusFormTable" cellpadding="0" cellspacing="0" border="0" summary="">
						<tr class="lotusFormFieldRow">
							<td><span id="radio_group_label"><fmt:message key="label.vcard.selectEncoding"/></span></td>
							<td><a id="exportVcardHelpLink"  href="javascript:;" class="lotusRight"></a></td>
						</tr>
					</table>
					<div role="radiogroup" aria-labelledby="radio_group_label">
						<input type="hidden" name="key" value="${key}"/>
						<input type="hidden" name="lastMod" value="${lastUpdate}"/>
						<c:set var="first" value="true"/>
						<c:forEach items="${applicationScope.profilesConfig.UIConfig.VCardExportConfig.charsets}" var="cs" varStatus="status">
							<div>
								<input role="radio" type="radio" name="encoding" class="lotusCheckbox" value="${cs.name}" id="defaultEncoding${status.count}" 
									<c:choose>
										<c:when test="${first}">
											checked="true" aria-checked="true"
										</c:when>
										<c:otherwise>
											aria-checked="false"
										</c:otherwise>
									</c:choose>
									onchange="dojo.query('input[name=&quot;encoding&quot;]').forEach(function(node) {dojo.attr(node, 'aria-checked',((node.checked)?'true':'false'))});"
								/>
								<span class="lotusCheckbox"><label for="defaultEncoding${status.count}"><core:message label="${cs.label}"/></label></span>
							</div>
							<c:set var="first" value="false"/>
						</c:forEach>
					</div>
	
				</fieldset>
			</div><%--end lotusDialogContent--%>
			<div class="lotusDialogFooter">
				<input id="vcardDlgSubmit" value="<core:escapeXml><fmt:message key="label.vcard.download"/></core:escapeXml>" class="lotusFormButton"  onclick="hideVcardExportForDL();return true;" type="submit" /> 
				<input value='<fmt:message key="label.vcard.cancel"/>' class="lotusFormButton" onclick="hideVcardExport();return false;" type="button" />
			</div>
		</form>
	</div><%--end lotusDialogBorder--%>
</c:otherwise>
</c:choose>
