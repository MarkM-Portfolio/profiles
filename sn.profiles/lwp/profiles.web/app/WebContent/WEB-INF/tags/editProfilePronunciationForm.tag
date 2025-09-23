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

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="lc-ui"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/>
<html:form action="/ajax/editMyProfile" 
       enctype="multipart/form-data" 
       method="post" 
       styleId="editProfile" 
       styleClass="lotusForm lotusFormPlain"
       onsubmit="return false;">

  <html:hidden property="subEditForm" value="pronunciation"/>
  <profiles:dangerousUrlInputField />

		<c:choose>
			<c:when test="${isNewUI}">
        <table id="tableEditPronunciation" class="lotusFormTable cnx8LotusTable" role="presentation">
    <tr class="lotusFormFieldRow">
      <td>
          <label class="audioUploadFileSelector" for="audioUploadFileSelector">
		        <profiles:getString key="label.associatedInformation.Uploadanaudiofile" bundle="com.ibm.lconn.profiles.strings.uilabels" />
          </label>
      </td>
    </tr>
    <tr class="lotusFormFieldRow">
      <td>
        <p class="lotusMeta" id="audioUploadDesc"><fmt:message key="label.editprofile.pronunciation.newInfo" /></p>
        <div id="audioUploadFileSelectFieldsDiv" class="lotusHidden cnx8LotusTable">
          <span id="audioUploadFileSelectorButtonSpan" class="lotusHidden">
            <fmt:message key="label.editprofile.pronunciation.selectFile" var="selectFileButtonText" />
            <a id="audioUploadFileSelectorButton" href="javascript:;" class="lotusBtn" role="button" title="${selectFileButtonText}" onclick="dojo.byId('audioUploadFileSelector').click();">
            <svg class="MuiSvgIcon-root" height="18px" width="21px" fill="#4178be" focusable="false" viewBox="0 -5 32 32" aria-hidden="true" role="presentation" data-mui-test="uploadIcon"  ><path d="M6 17l1.41 1.41L15 10.83V30h2V10.83l7.59 7.58L26 17 16 7 6 17z"></path><path d="M6 8V4h20v4h2V4a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v4z"></path></svg>
            ${selectFileButtonText}
            </a>
          </span>
          <label id="FilenameLable" class="FilenameLable" style="display:none;" >
          <profiles:getString key="label.associatedInformation.FileName" bundle="com.ibm.lconn.profiles.strings.uilabels" />
          </label>
          <input type="text" id="audioUploadFileSelected" class="lotusHidden" value="${selectFileButtonText}" 
            style="display:none; max-width:200px; padding: 10px 0px !important;" 
            onkeypress="if ((event.charCode || event.keyCode) == dojo.keys.ENTER) this.click();" 
            readonly="readonly" aria-label="audioUploadDesc" />
          <input type="file" accept=".wma,audio/wav" id="audioUploadFileSelector" name="pronunciation" class="lotusHidden lotusText lotusAlignLeft"
            aria-required="true" tabindex="-1"        
            onchange="updateAudioFileName(this); 
            document.getElementById('FilenameLable').style.display = '';
            document.getElementById('saveNCloseBtnSpan').getElementsByClassName('lotusBtn')[0].className ='lotusFormButton';
            document.getElementById('saveNCloseBtnSpan').getElementsByClassName('lotusBtn')[0].className ='lotusFormButton';
            document.getElementById('audioUploadFileSelected').style.display = '';" 
            style="max-width: 200px;" />
        </div>
        
        <script type="text/javascript">dojo.query("#audioUploadFileSelectorButton").attr("aria-describedby", "audioUploadDesc");</script>
      </td>
    </tr>
    <tr class="lotusFormFieldRow">
      <td>
        <html:checkbox disabled="${ hasPronunciation ? 'false' : 'true' }" property="removePronunciation" styleId="removeAudio" styleClass="lotusCheckbox" onchange="dataChange(this);"/>
        <label Class="lotusCheckbox" for="removeAudio"><fmt:message key="label.editprofile.pronunciation.removeaudiofile" /></label>
      </td>
    </tr>
    <c:choose>
      <c:when test="${canUpdateProunciation}">
        <tr class="lotusFormFieldRow">
          <td>
            <c:url value="/html/myProfileView.do"  var="myProfileViewUrl"/>
            <div id="ulEditProfilePronounciationActionButtons" class="PronounciationActionButtons" role="toolbar" 
              aria-label="<fmt:message key="label.editprofile.action.toolbar"/>">
              
              <fmt:message key="label.editprofile.pronunciation.removeafile" var="removeAudioText"  />
              <html:button 
              onclick="document.getElementById('removeAudio').checked=true; uploadAudioFile(this.form, '${myProfileViewUrl}');" 
              
              property="removePronunciation" styleId="removeAudio" 
              value="${removeAudioText}" styleClass="${ hasPronunciation ? 'lotusBtn' : 'lotusBtn lotusBtnDisabled' }"
              disabled="${ hasPronunciation ? 'false' : 'true' }"/>
              
              <fmt:message key="label.editprofile.cancel" var="cancelText" />
              <html:button onclick="profiles_goto('${myProfileViewUrl}'); " property="cancelButton" value="${cancelText}" styleClass="lotusBtn" />

              <span id="saveNCloseBtnSpan" class="saveNCloseBtnSpan">

                <fmt:message key="label.editprofile.update" var="updateBtnText" />
                <html:button  onclick="uploadAudioFile(this.form);" property="submitButton" value="${updateBtnText}" styleClass="submitButtonDisabled lotusBtn lotusBtnDisabled " disabled="true" />

                <fmt:message key="label.editprofilecnx8.close" var="saveNCloseText" />
                <html:button onclick="uploadAudioFile(this.form, '${myProfileViewUrl}');" property="submitButton" value="${saveNCloseText}" styleClass="submitButtonDisabled lotusBtn lotusBtnDisabled" disabled="true" />
              </span>

              
            </div>
            </td>
        </tr>
      </c:when>
      <c:otherwise>
        <tr class="lotusFormFieldRow">
          <td>
            <fmt:message key="label.feature.pronun.edit.not.allowed" />
          </td>
        </tr>
      </c:otherwise>
    </c:choose>
  </table>
			</c:when>
			<c:otherwise>
				       <table id="tableEditPronunciation" class="lotusFormTable" role="presentation">
    <tr class="lotusFormFieldRow">
      <td>
        <label for="audioUploadFileSelector">
		  <profiles:getString key="label.associatedInformation.pronunciation" bundle="com.ibm.lconn.profiles.strings.uilabels" />
        </label>
      </td>
    </tr>
    <tr class="lotusFormFieldRow">
      <td>
        <div id="audioUploadFileSelectFieldsDiv" class="lotusHidden">
          <span id="audioUploadFileSelectorButtonSpan" class="lotusHidden lotusBtn">
            <fmt:message key="label.editprofile.pronunciation.selectFile" var="selectFileButtonText" />
            <a id="audioUploadFileSelectorButton" href="javascript:;" class="lotusFormButton" role="button" title="${selectFileButtonText}" onclick="dojo.byId('audioUploadFileSelector').click();">${selectFileButtonText}</a>
          </span>

          <input type="text" id="audioUploadFileSelected" class="lotusHidden lotusText" value="${selectFileButtonText}" style="max-width:200px" onclick="dojo.byId('audioUploadFileSelectorButton').click()" onkeypress="if ((event.charCode || event.keyCode) == dojo.keys.ENTER) this.click();" readonly="readonly" aria-label="audioUploadDesc" />
          <input type="file" accept=".wma,audio/wav" id="audioUploadFileSelector" name="pronunciation" class="lotusHidden lotusText lotusAlignLeft"  aria-required="true" tabindex="-1"        
            onchange="updateAudioFileName(this);" style="max-width: 200px;" />

          <a href="#" aria-haspopup="true" role="button" class="pronunciation_help_link" title="<fmt:message key="label.editprofile.pronunciation.helplink" />">
            <profiles:htmlLang var="htmlLang"/>
            <c:set var="helpClassNameSuffix">
              <c:choose>
                <c:when test="${htmlLang eq 'ar'}">-ar</c:when>
                <c:otherwise></c:otherwise>
              </c:choose>
            </c:set>
            <img class="lconnSprite lconnSprite-iconHelp16${helpClassNameSuffix}" src="<lc-ui:blankGif />" alt="<fmt:message key="label.editprofile.pronunciation.helplink" />"/>
          </a>
        </div>
        <p class="lotusMeta" id="audioUploadDesc"><i><fmt:message key="label.editprofile.pronunciation.info" /></i></p>
        <script type="text/javascript">dojo.query("#audioUploadFileSelectorButton").attr("aria-describedby", "audioUploadDesc");</script>
      </td>
    </tr>
    <tr class="lotusFormFieldRow">
      <td>
        <html:checkbox disabled="${ hasPronunciation ? 'false' : 'true' }" property="removePronunciation" styleId="removeAudio" styleClass="lotusCheckbox" onchange="dataChange(this);"/>
        <label for="removeAudio"><fmt:message key="label.editprofile.pronunciation.removeaudiofile" /></label>
      </td>
    </tr>
    <c:choose>
      <c:when test="${canUpdateProunciation}">
        <tr class="lotusFormFieldRow">
          <td>
            <c:url value="/html/myProfileView.do"       var="myProfileViewUrl"/>
            <div id="ulEditProfilePronounciationActionButtons" role="toolbar" aria-label="<fmt:message key="label.editprofile.action.toolbar"/>">
              <fmt:message key="label.editprofile.update" var="updateBtnText" />
              <html:button onclick="uploadAudioFile(this.form);" property="submitButton" value="${updateBtnText}" styleClass="lotusBtn lotusBtnDisabled" disabled="true" />

              <fmt:message key="label.editprofile.close" var="saveNCloseText" />
              <html:button onclick="uploadAudioFile(this.form, '${myProfileViewUrl}'); " property="submitButton" value="${saveNCloseText}" styleClass="lotusBtn lotusBtnDisabled" disabled="true" />

              <fmt:message key="label.editprofile.cancel" var="cancelText" />
              <html:button onclick="profiles_goto('${myProfileViewUrl}'); " property="cancelButton" value="${cancelText}" styleClass="lotusBtn" />
            </div>
            </td>
        </tr>
      </c:when>
      <c:otherwise>
        <tr class="lotusFormFieldRow">
          <td>
            <fmt:message key="label.feature.pronun.edit.not.allowed" />
          </td>
        </tr>
      </c:otherwise>
    </c:choose>
  </table>
			</c:otherwise>
		</c:choose>

</html:form>
<script type="text/javascript" defer="defer">
dojo.addOnLoad(
  function () {
    var pu = dojo.byId("audioUploadFileSelector");
    if (pu && (dojo.isIE < 8 || dojo.isSafari)) dojo.style(pu, "direction", "ltr");
    
    //---------------------------------------------------------------------
    // file input field vs button/text field (IE vs others) see RTC#87733/PMR 03729,073,724
    var show = function(id) {
      if(id) {
        var el = dojo.byId(id);
        if(el) dojo.removeClass(el, "lotusHidden");
      }
    }

    // file input field needs to be visible or IE will not work with it 
    if (dojo.isIE) {
      show("audioUploadFileSelector");  // show file input field
      var pu = dojo.byId("audioUploadFileSelectorButtonSpan");
      if(pu) 
        dojo.style(pu, { 
          display: "none"
        });
    }

    // hide file input field and show button/text field
    else {
      show("audioUploadFileSelectorButtonSpan");
      show("audioUploadFileSelectorButton");
      show("audioUploadFileSelected");

      // hide the file input by moving it off the screen; 
      // cannot be styled to display:none, 
      // otherwise browser form will fail 
      var pu = dojo.byId("audioUploadFileSelector");
      if(pu) 
        dojo.style(pu, { 
          display: "block",
          opacity: 0,
          position: "absolute",
          left: "0px",
          top: "-9999px"
        });
    }
  
    show("audioUploadFileSelectFieldsDiv");
    //---------------------------------------------------------------------
  } 
);

var updateAudioFileName = function(obj) {
	var puFile = dojo.byId("audioUploadFileSelected");
	if (puFile) {
		puFile.value = lconn.profiles.ProfilesCore.getUploadFileName(obj);
	}
	dataChange(obj);
};

var uploadAudioFile = function( formObj, gotoOnSuccess ) {
	var args = {
		url: applicationContext + '/ajax/editMyProfile.do?lang=' + appLang,
		checkAuthHeader: true,
		handleAs: "html",
		form: formObj,
		preventCache: true,
		load: function( data ) {
			var postSuccessful = editProfile_xhrCheckPostResults(data, {}); 
			if( postSuccessful ) {
				gbDataSaved = true;
				if ( typeof(gotoOnSuccess) != "undefined" && gotoOnSuccess ) {
					profiles_goto( gotoOnSuccess);
				}
			}
		},
		error: function( err ) {
			editProfile_xhrCheckPostResults(err, {});
		}
	};

	if (dojo.getObject("com.ibm.ajax.auth")) {
		com.ibm.ajax.auth.prepareSecure(args);
	}

	/* we need to reset these iframe parameters manually */
	if (typeof require === "function") {
		require(["dojo/request/iframe", "dojo/dom", "dojo/dom-attr"], function(iframe, dom, attr){
			if (!dojo.isIE) {
				if (iframe._iframeName) {
					var frame = dom.byId(iframe._iframeName);
					if (frame) {
						frame.parentNode.removeChild(frame);
					}
					if (window[iframe._iframeName]) {
						try {delete window[iframe._iframeName]; } catch (e) {}
					}
				}
				delete iframe._frame;
				iframe._currentDfd = null;
				attr.remove(formObj, "target");
			}

			iframe(args.url, args).then(args.load, args.error);
		});
		
	} else { //older dojo
		if (!dojo.isIE) {
			if (dojo.io.iframe._iframeName) {
				var frame = dojo.byId(dojo.io.iframe._iframeName);
				if (frame) {
					frame.parentNode.removeChild(frame);
				}
				if (window[dojo.io.iframe._iframeName]) {
					try {delete window[dojo.io.iframe._iframeName]; } catch (e) {}
				}
			}	
			delete dojo.io.iframe._frame;
			dojo.io.iframe._currentDfd = null;
			dojo.removeAttr(formObj, "target");
		}
		
		dojo.io.iframe.send(args);
	}
};
</script>
