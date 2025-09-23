<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- HCL Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright HCL Technologies Limited 2015, 2022                     --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%-- 5724-S68                                                          --%>
<%@ tag body-content="empty" %>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<style>
	.editMyProfileClass tbody td tr input {
		border-right: none !important;
		border-left: none !important;
		border-top: none !important;
	}
</style>
<html:xhtml/>
<div class="lotusMeta lotusSection">
	<p><fmt:message key="label.editprofile.contactInformation.help.body" /></p>
</div>
<html:form action="/ajax/editMyProfile" 
		   enctype="multipart/form-data" 
		   method="post" 
		   styleId="editProfile" 
		   styleClass="lotusForm lotusFormPlain"
		   onsubmit="return false;">
 
	<profiles:dangerousUrlInputField />
	<html:hidden property="subEditForm" value="contactInfo"/>
	<fmt:setBundle basename="com.ibm.lconn.profiles.strings.uilabels" var="attributeLabels" scope="page" />
	
	<table id="tableEditProfileContactInfo" class="lotusFormTable" style="background-color:#fff; width:auto;" role="presentation">
		<tbody>
			<profiles:freemarker dataModel="${dataModel}" template="PROFILE_EDIT" mixinMap="${mixinMap}"/>
			<tr class="lotusFormFieldRow">
				<td>
					<c:url value="/html/myProfileView.do" var="myProfileViewUrl"/>
					
					<div id="ulEditProfileContactInfoActionButtons" role="toolbar" aria-label="<fmt:message key="label.editprofile.action.toolbar"/>">		
						<fmt:message key="label.editprofile.update" var="updateBtnText" />
						<html:button onclick="editProfile_saveForm(this.form);" property="submitButton" value="${updateBtnText}" styleClass="lotusBtn lotusFormButton lotusBtnDisabled" disabled="true" />

						<c:choose>
							<c:when test="${isNewUI}">
								<fmt:message key="label.editprofilecnx8.close" var="saveNCloseText" />
							</c:when>
							<c:otherwise>
								<fmt:message key="label.editprofile.close" var="saveNCloseText" />
							</c:otherwise>
						</c:choose>
						<html:button onclick="editProfile_saveForm(this.form, '${myProfileViewUrl}'); " property="submitButton" value="${saveNCloseText}" styleClass="lotusBtn lotusFormButton lotusBtnDisabled" disabled="true" />
						<fmt:message key="label.editprofile.cancel" var="cancelText" />
						<html:button onclick="profiles_goto('${myProfileViewUrl}'); " property="cancelButton" value="${cancelText}" styleClass="lotusBtn lotusFormButton" />
						
					</div>					
					
				</td>
			</tr>
		</tbody>
	</table>
</html:form>