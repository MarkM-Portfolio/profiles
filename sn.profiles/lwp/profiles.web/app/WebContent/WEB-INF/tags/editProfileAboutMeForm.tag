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

<html:xhtml/>

<c:choose>
	<c:when test="${isNewUI}">
		<div class="lotusMeta lotusSection cnx8Section">
		<p class="cnx8Content"><fmt:message key="label.editprofile.associatedInformation.help.body" /></p>
	</c:when>
	<c:otherwise>
		<div class="lotusMeta lotusSection">
		<p><fmt:message key="label.editprofile.associatedInformation.help.body" /></p>
	</c:otherwise>
</c:choose>
</div>
<html:form action="/ajax/editMyProfile" 
		   enctype="multipart/form-data" 
		   method="post" 
		   styleId="editProfile" 
		   styleClass="lotusForm lotusFormPlain"
		   onsubmit="return false;">
	    
	<profiles:dangerousUrlInputField />
	<html:hidden property="subEditForm" value="aboutMe"/>
	<fmt:setBundle basename="com.ibm.lconn.profiles.strings.uilabels" var="attributeLabels" scope="page" />

	<table id="tableEditProfileAboutMe" class="lotusFormTable" role="presentation">
		<profiles:freemarker dataModel="${dataModel}" template="PROFILE_EDIT" mixinMap="${mixinMap}"/>
		<tr class="lotusFormFieldRow">
			<td>
				<c:url value="/html/myProfileView.do" var="myProfileViewUrl"/>

				<div id="ulEditProfileaboutMeActionButtons" role="toolbar" aria-label="<fmt:message key="label.editprofile.action.toolbar"/>">
					<c:choose>
						<c:when test="${isNewUI}">
							<fmt:message key="label.editprofile.cancel" var="cancelText" />
							<html:button onclick="profiles_goto('${myProfileViewUrl}'); " property="cancelButton" value="${cancelText}" styleClass="lotusBtn" />

							<span>
								<fmt:message key="label.editprofile.update" var="updateBtnText" />
								<html:button onclick="editProfile_saveForm(this.form);" property="submitButton" value="${updateBtnText}" styleClass="cnx8Btn cnx8BtnDisabled" disabled="true" />

								<fmt:message key="label.editprofilecnx8.close" var="saveNCloseText" />
								<html:button onclick="editProfile_saveForm(this.form, '${myProfileViewUrl}'); " property="submitButton" value="${saveNCloseText}" styleClass="cnx8Btn cnx8BtnDisabled" disabled="true" />
							</span>
						</c:when>
						<c:otherwise>
								<fmt:message key="label.editprofile.update" var="updateBtnText" />
								<html:button onclick="editProfile_saveForm(this.form);" property="submitButton" value="${updateBtnText}" styleClass="lotusBtn lotusBtnDisabled" disabled="true" />

								<fmt:message key="label.editprofile.close" var="saveNCloseText" />
								<html:button onclick="editProfile_saveForm(this.form, '${myProfileViewUrl}'); " property="submitButton" value="${saveNCloseText}" styleClass="lotusBtn lotusBtnDisabled" disabled="true" />

								<fmt:message key="label.editprofile.cancel" var="cancelText" />
								<html:button onclick="profiles_goto('${myProfileViewUrl}'); " property="cancelButton" value="${cancelText}" styleClass="lotusBtn" />
						</c:otherwise>
					</c:choose>				
				</div>
			</td>
		</tr>
	</table>
</html:form>