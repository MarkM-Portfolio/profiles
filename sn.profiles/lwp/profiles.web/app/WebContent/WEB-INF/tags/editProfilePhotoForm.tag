<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- HCL Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright HCL Technologies Limited 2016, 2022                     --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%-- 5724-S68                                                          --%>
<%@ tag body-content="empty" %>

<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 		uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 	uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="lc-ui"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="lc-cache" 	uri="http://www.ibm.com/connections/core/cache" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/>



<html:form action="/html/uploadPhoto"
			enctype="multipart/form-data" 
			method="post" 
			styleId="editProfile"
			styleClass="lotusForm lotusFormPlain"
			onsubmit="return false;">
		   
	<html:hidden property="subEditForm" value="photo"/>
	<profiles:dangerousUrlInputField />
	<jsp:useBean id="photoParams" scope="request" class="java.util.LinkedHashMap" type="java.util.Map" />

		<c:choose>
			<c:when test="${isNewUI}">
			<!-- photo and pronunciation -->
			<c:set var="currentPhotoWidth"  value="60px" />
			<c:set var="currentPhotoHeight" value="60px" />
			<c:set var="previewPhotoWidth"  value="300px" />
			<c:set var="previewPhotoHeight" value="300px" />
	<table id="tableEditPhoto" class="lotusFormTable cnx8LotusTable" role="presentation">
		<tr class="lotusFormFieldRow">
			<td colspan="2" width="500">
				<html:hidden property="lastMod" value="${lastMod}" />
				<div>
					<label class="audioUploadFileSelector" for="photoUploadFileSelected"><fmt:message key="label.editprofile.photo.newupload" /></label>
					<label for="photoUploadFileSelector" class="lotusHidden"><fmt:message key="label.editprofile.photo.newupload" /></label>
				</div>			
				<div id="photoUploadFileSelectFieldsDiv" class="lotusHidden">
				 	
				 	<input type="text" id="photoUploadFileSelected" class="lotusHidden lotusText lotusCheckbox" value="${selectFileButtonText}" style="max-width:200px" onclick="lconn.profiles.PhotoCrop.invokeFileSelect('photoUploadFileSelector');" onkeypress="if ((event.charCode || event.keyCode) == dojo.keys.ENTER) this.click();" readonly="readonly" />
				 	<input type="file" id="photoUploadFileSelector" name="photo" class="lotusHidden lotusText lotusAlignLeft" aria-required="true" tabindex="-1" onchange="lconn.profiles.PhotoCrop.updateFileSelectedTextField(this); lconn.profiles.PhotoCrop.previewImage(this); dataChange(this); document.getElementById('saveNCloseBtnSpan').getElementsByClassName('lotusBtn')[0].className ='lotusFormButton'; document.getElementById('saveNCloseBtnSpan').getElementsByClassName('lotusBtn')[0].className ='lotusFormButton';" />
				</div>
			
				<span id="photoUploadDesc">
					<p class="lotusMeta"><fmt:message key="label.editprofile.photo.info" /></p>
					<p class="lotusMeta"><fmt:message key="label.editprofile.photo.delay" /></p>
				</span>
			</td>
		</tr>
		<tr>
			<td>
				<div id="photoDiv" class="photoDiv">
					
					<html:hidden property="uid" />
					<c:set var="lastMod">
						<profiles:outputLMTime time="${editProfileForm.lastUpdate}" />
					</c:set>
					
					<c:set target="${photoParams}" property="key" value="${editProfileForm.key}" />
					<c:set target="${photoParams}" property="lastMod" value="${lastMod}" />
					<html:img styleId="imgProfilePhotoCurrent" width="${currentPhotoWidth}" height="${currentPhotoHeight}" style="border-radius: 50%;" src="${pageContext.request.contextPath}/photo.do" name="photoParams" altKey="label.editprofile.photo.current" />
					<span id="photoUploadFileSelectorButtonSpan" class="lotusHidden">
						<fmt:message key="label.editprofile.photo.selectImage" var="selectFileButtonText" />
						<a id="photoUploadFileSelectorButton" aria-describedby="photoUploadDesc" href="javascript:;" class="lotusHidden lotusBtn" role="button" title="${selectFileButtonText}" onclick="lconn.profiles.PhotoCrop.invokeFileSelect('photoUploadFileSelector');">
						<svg class="MuiSvgIcon-root" height="18px" width="21px" fill="#4178be" focusable="false" viewBox="0 -5 32 32" aria-hidden="true" role="presentation" data-mui-test="uploadIcon"  ><path d="M6 17l1.41 1.41L15 10.83V30h2V10.83l7.59 7.58L26 17 16 7 6 17z"></path><path d="M6 8V4h20v4h2V4a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v4z"></path></svg>
							${selectFileButtonText}</a>
				 	</span>
				</div>
			</td>
			<td>
				<div id="imgPreviewOutterContainer" style="width:${previewPhotoWidth};" class="lotusHidden">
					<fmt:message key="label.editprofile.photo.previewBox" var="previewBoxText" />
					<label for="photo" class="lotusBold">${previewBoxText}</label>
					<div id="imgPreview" style="direction:ltr; padding:0px; width:${previewPhotoWidth}; height:${previewPhotoHeight}; border:1px black solid;"></div>
					<div id="imgPreviewLoading" class="lotusHidden" style="padding: 0px 0px 0px 0px">
						<img src='<lc-cache:uri template="{oneuiRoot}/images/loading.gif?etag={version}" />' >&nbsp;<fmt:message key="label.editprofile.photo.loading" />
				</div>
				</div>	
			</td>
			</div>
		</tr>

		<c:if test="${canUpdatePhoto}">	
			<tr class="lotusFormFieldRow">
				<td colspan="2">
					<c:url value="/html/myProfileView.do" var="myProfileViewUrl"/>
					<div id="ulEditProfilePhotoActionButtons" role="toolbar" aria-label="<fmt:message key="label.editprofile.action.toolbar"/>">		
	
						<fmt:message key="label.editprofile.photo.removeimage" var="removeBtnText" />
						<html:button property="removeButton" value="${removeBtnText}" styleClass="lotusBtn" 
							onclick="lconn.profiles.PhotoCrop.removePressed('${myProfileViewUrl}'); " />

						<fmt:message key="label.editprofile.cancel" var="cancelText" />
						<html:button property="cancelButton" value="${cancelText}" styleClass="lotusBtn" 
							onclick="profiles_goto('${myProfileViewUrl}'); " />

						<span id="saveNCloseBtnSpan" class="saveNCloseBtnSpan">
							<fmt:message key="label.editprofile.update" var="updateBtnText" />
							<html:button styleId="lconn_savePhotoButton" property="submitButton" value="${updateBtnText}" styleClass="submitButtonDisabled lotusBtn lotusBtnDisabled" 
							onclick="lconn.profiles.PhotoCrop.savePressed(this);" disabled="true" />
	
							<fmt:message key="label.editprofilecnx8.close" var="saveNCloseText" />
							<html:button styleId="lconn_saveNclosePhotoButton" property="submitButton" value="${saveNCloseText}" styleClass="submitButtonDisabled lotusBtn lotusBtnDisabled" 
							onclick="lconn.profiles.PhotoCrop.saveNclosePressed(this,'${myProfileViewUrl}');" disabled="true" />
						</span>
					</div>	
				</td>
			</tr>
		</c:if>	
		<c:if test="${!canUpdatePhoto}">	
			<tr class="lotusFormFieldRow">
				<td colspan="2">
					<fmt:message key="label.feature.photo.edit.not.allowed" />
				</td>
			</tr>
		</c:if>	
	</table>
	</c:when>
		<c:otherwise>
			<!-- photo and pronunciation -->
			<c:set var="currentPhotoWidth"  value="155px" />
			<c:set var="currentPhotoHeight" value="155px" />
			<c:set var="previewPhotoWidth"  value="300px" />
			<c:set var="previewPhotoHeight" value="300px" />
	<table id="tableEditPhoto" class="lotusFormTable" role="presentation">
		<tr class="lotusFormFieldRow">
			<td colspan="2" width="500">
				<html:hidden property="lastMod" value="${lastMod}" />
				<div>
					<label for="photoUploadFileSelected"><fmt:message key="label.editprofile.photo.newupload" /></label>
					<label for="photoUploadFileSelector" class="lotusHidden"><fmt:message key="label.editprofile.photo.newupload" /></label>
				</div>			
				<div id="photoUploadFileSelectFieldsDiv" class="lotusHidden">
				 	<span id="photoUploadFileSelectorButtonSpan" class="lotusHidden lotusBtn">
						<fmt:message key="label.editprofile.photo.selectFile" var="selectFileButtonText" />
						<a id="photoUploadFileSelectorButton" aria-describedby="photoUploadDesc" href="javascript:;" class="lotusHidden lotusFormButton" role="button" title="${selectFileButtonText}" onclick="lconn.profiles.PhotoCrop.invokeFileSelect('photoUploadFileSelector');">${selectFileButtonText}</a>
				 	</span>
				 	<input type="text" id="photoUploadFileSelected" class="lotusHidden lotusText" value="${selectFileButtonText}" style="max-width:200px" onclick="lconn.profiles.PhotoCrop.invokeFileSelect('photoUploadFileSelector');" onkeypress="if ((event.charCode || event.keyCode) == dojo.keys.ENTER) this.click();" readonly="readonly" />
				 	<input type="file" id="photoUploadFileSelector" name="photo" class="lotusHidden lotusText lotusAlignLeft" aria-required="true" tabindex="-1" onchange="lconn.profiles.PhotoCrop.updateFileSelectedTextField(this); lconn.profiles.PhotoCrop.previewImage(this); dataChange(this);" />
				</div>
			
				<span id="photoUploadDesc">
					<i>
						<p class="lotusMeta"><fmt:message key="label.editprofile.photo.info" /></p>
						<p class="lotusMeta"><fmt:message key="label.editprofile.photo.delay" /></p>
					</i>
				</span>
			</td>
		</tr>
		<tr class="lotusFormFieldRow" class="lotusHidden">
				<td width="${currentPhotoWidth}">
					<div id="photoDiv">
						<label ><fmt:message key="label.editprofile.photo.current" /></label><br />
						<html:hidden property="uid" />
						<c:set var="lastMod">
							<profiles:outputLMTime time="${editProfileForm.lastUpdate}" />
						</c:set>
						
						<c:set target="${photoParams}" property="key" value="${editProfileForm.key}" />
						<c:set target="${photoParams}" property="lastMod" value="${lastMod}" />
						<html:img styleId="imgProfilePhotoCurrent" width="${currentPhotoWidth}" height="${currentPhotoHeight}" src="${pageContext.request.contextPath}/photo.do" name="photoParams" altKey="label.editprofile.photo.current" /><br />
					</div>
				</td>
				<td>
					<div id="imgPreviewOutterContainer" style="width:${previewPhotoWidth};" class="lotusHidden">
						<fmt:message key="label.editprofile.photo.previewBox" var="previewBoxText" />
						<label for="photo" class="lotusBold">${previewBoxText}</label>
						<div id="imgPreview" style="direction:ltr; padding:0px; width:${previewPhotoWidth}; height:${previewPhotoHeight}; border:1px black solid;"></div>
						<div id="imgPreviewLoading" class="lotusHidden" style="padding: 0px 0px 0px 0px">
							<img src='<lc-cache:uri template="{oneuiRoot}/images/loading.gif?etag={version}" />' >&nbsp;<fmt:message key="label.editprofile.photo.loading" />
						</div>
					</div>	
				</td>
			</div>
		</tr>

		<c:if test="${canUpdatePhoto}">	
			<tr class="lotusFormFieldRow">
				<td colspan="2">
					<c:url value="/html/myProfileView.do" var="myProfileViewUrl"/>
					<div id="ulEditProfilePhotoActionButtons" role="toolbar" aria-label="<fmt:message key="label.editprofile.action.toolbar"/>">		
						<fmt:message key="label.editprofile.update" var="updateBtnText" />
						<html:button styleId="lconn_savePhotoButton" property="submitButton" value="${updateBtnText}" styleClass="lotusBtn lotusBtnDisabled" 
							onclick="lconn.profiles.PhotoCrop.savePressed(this);" disabled="true" />
	
						<fmt:message key="label.editprofile.close" var="saveNCloseText" />
						<html:button styleId="lconn_saveNclosePhotoButton" property="submitButton" value="${saveNCloseText}" styleClass="lotusBtn lotusBtnDisabled" 
							onclick="lconn.profiles.PhotoCrop.saveNclosePressed(this,'${myProfileViewUrl}');" disabled="true" />
	
						<fmt:message key="label.editprofile.photo.removeimage" var="removeBtnText" />
						<html:button property="removeButton" value="${removeBtnText}" styleClass="lotusBtn" 
							onclick="lconn.profiles.PhotoCrop.removePressed('${myProfileViewUrl}'); " />

						<fmt:message key="label.editprofile.cancel" var="cancelText" />
						<html:button property="cancelButton" value="${cancelText}" styleClass="lotusBtn" 
							onclick="profiles_goto('${myProfileViewUrl}'); " />
						
					</div>	
					
				</td>
			</tr>
		</c:if>	
		<c:if test="${!canUpdatePhoto}">	
			<tr class="lotusFormFieldRow">
				<td colspan="2">
					<fmt:message key="label.feature.photo.edit.not.allowed" />
				</td>
			</tr>
		</c:if>	
	</table>

		</c:otherwise>
	</c:choose>


</html:form>

<script type="text/javascript">
	try {
		profilesData.config.pageId = "editProfileView";
	} catch (e) {}
</script>

<script type="text/javascript" defer="defer">
dojo.addOnLoad(
	function () {
		var pu = dojo.byId("photoUpload");
		if (pu && (dojo.isIE < 8 || dojo.isSafari)) dojo.style(pu, "direction", "ltr");
	
		//---------------------------------------------------------------------
		// file input field vs button/text field (IE vs others) see RTC#87733
		var show = function(id) {
			if(id) {
				var el = dojo.byId(id);
				if(el) dojo.removeClass(el, "lotusHidden");
			}
		}

		// file input field needs to be visible or IE will not work with it 
		if (dojo.isIE) {
			show("photoUploadFileSelector");	// show file input field
			var pu = dojo.byId("photoUploadFileSelectorButtonSpan");
			if(pu) 
				dojo.style(pu, { 
					display: "none"
				});
		}

		// hide file input field and show button/text field
		else {
			show("photoUploadFileSelectorButtonSpan");
			show("photoUploadFileSelectorButton");
			show("photoUploadFileSelected");

			// hide the file input by moving it off the screen; 
			// cannot be styled to display:none, 
			// otherwise browser form will fail 
			var pu = dojo.byId("photoUploadFileSelector");
			if(pu) 
				dojo.style(pu, { 
					display: "block",
					opacity: 0,
					position: "absolute",
					left: "0px",
					top: "-9999px"
				});
		}
	
		show("photoUploadFileSelectFieldsDiv");
		//---------------------------------------------------------------------
	} 
);
</script>
