<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- HCL Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright HCL Technologies Limited 2001, 2021                     --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 		uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 	uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper" %>

<html:xhtml/>

<c:set var="isNewUI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>"/>
<c:set var="isMyProfile" value="${key == loggedInUserKey}" />
<c:choose>
	<c:when test="${isMyProfile}">
		<fmt:message key="label.page.profiles.editmyprofile" var="pageTitle" />
	</c:when>
	<c:otherwise>
		<fmt:message key="label.page.profiles.editprofile" var="pageTitle"><fmt:param value="${displayName}"/></fmt:message>
	</c:otherwise>
</c:choose>
<%
	String tab = request.getParameter("tab");
	String tabReq = (String)request.getAttribute("tab");
	if (tab == null && tabReq != null) tab = tabReq;
	else if (tab == null && tabReq == null) tab = (String)"contactInfo";
	
	pageContext.setAttribute("isContactInfo",new Boolean(tab.equals("contactInfo")));
	pageContext.setAttribute("isAboutMe",new Boolean(tab.equals("aboutMe")));
	pageContext.setAttribute("isPhoto",new Boolean(tab.equals("photo")));
	pageContext.setAttribute("isPronunciation",new Boolean(tab.equals("pronunciation")));
		
	Map mixinMap = new HashMap(1);
	String sectionLabel = tab.equals("contactInfo") ? "contactInformation" : tab.equals("aboutMe") ? "associatedInformation" : tab;
	mixinMap.put("section", sectionLabel);
	request.setAttribute("mixinMap", mixinMap);
	request.setAttribute("isNewUI", UIConfigHelper.INSTANCE.isCNX8UI(request));
%>

<jsp:useBean id="now" class="java.util.Date" />
<script type="text/javascript">
	var pageTime = ${now.time};
	profilesData.config.pageId = "editProfileView";
	document.title = '<core:escapeJavaScript><c:out value="${pageTitle}" escapeXml="false"/></core:escapeJavaScript>';
</script>

<script type="text/javascript">
// TODO: move this who js section to a cached js resource
var editProfile_xhrError = lconn.profiles.xhrError;

var editProfile_xhrPost = function( servlet, form, callback, isPost ) {
	return editProfile_xhrPostDelete( servlet, form, callback, true, false);
}

var editProfile_xhrPostDelete = function( servlet, form, callback, isPost, isJsonPost ) {
	var kw = {
	        url: applicationContext + servlet,
			load: callback,
			error: function(data, ioArgs){ editProfile_xhrError(data, ioArgs) }, 
			timeout: 30000, 
			checkAuthHeader: true,
			noLoginRedirect: true,
			handleAs: 'text'
	};

	// if posting a form, get the form contents into the post object
	if (form != null && form != "") {
		kw.form = form;
		if (isPost) {
			if (isJsonPost) {
				kw.headers = { "Content-Type": "text/json" };
				kw.postData = dojo.formToJson(form);
			} else {
				kw.postData = dojo.formToQuery(form);
			}
		}
	}

	if (isPost) {
		lconn.profiles.xhrPost(kw);
		
	} else {
		lconn.profiles.xhrDelete(kw);
	}
		
	return true;
}

// Scrub the post request's response for possible errors (empty response assumes no err)
var	editProfile_xhrCheckPostResults = function( data, ioArgs ) {
	var retStatus = false;
	if(data)
	{
		if(typeof(data) == "string" ) 
		{
			// NOTE:
			//  when a post is successful, no data is returned in the response
			//	when a post fails due to a struts data field validation trigger, the http response is also successful, however the response 
			//  object contains the edit scene markup itself with the struts generated error messages injected in the sceen markup.  
			//  As a result, this function will scrub the response
			//  for such given errors.  Eventually, the correct thing to do is for the response of the post to just have the error message
			var errCheckStr = '<fmt:message key="errors.header" />';
			var errCheck = data.indexOf(errCheckStr);
			if( errCheck != -1 ) errCheck = data.indexOf(errCheckStr, errCheck+1); // skip the check string in this func
			if( errCheck != -1 ) { // extract the error message
				var begErrMsgIdx = errCheck + errCheckStr.length;
				var endErrMsgIdx = data.indexOf('</div>',begErrMsgIdx);
				var errMsg = data.slice( begErrMsgIdx, endErrMsgIdx);
				lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", errMsg);
			}
			else
			{ 
				lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "warning", generalrs["label.editprofile.update.unknownErr"]);
			}
		}
		
		else if( typeof(data) == "object")
		{
			if( data instanceof Error )
			{
				lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", generalrs["label.editprofile.update.unknownErr"]);				
			}
			else if(data.documentElement)
			{
				var bodyString = (dojox.data.dom.textContent(data.documentElement));
				var nodes = dojo.query("#validationErrorMessage", data.documentElement);
				if(nodes && nodes.length && nodes.length > 0)
				{
					lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", dojo.attr(nodes[0], "innerHTML"));
				}
				else if ( bodyString && ( bodyString.indexOf("error.fileContainsVirus") != -1) ) {
					lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", generalrs["error.fileContainsVirus"]);
				}
				else if ( bodyString && ( bodyString.indexOf("errorDefaultMsg2") != -1) ) {
					lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", generalrs["errorDefaultMsg2"]);
				}
				else if( dojo.query(".lotusErrorContent p", data.documentElement).length && 
						 dojo.query(".lotusErrorContent p", data.documentElement)[0].innerHTML == "err.feed.authentication.required")
				{
					lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", generalrs["error.sessionTimeout"]);
				}

				else if( dojo.query("body", data.documentElement).length && 
						 dojo.query("body", data.documentElement)[0].innerHTML != "") // we have data in the html body response, but did not match any of the err checks above
				{
					lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", generalrs["label.editprofile.update.unknownErr"]);
				}
				
				else
				{
					editProfile_showSuccessMsg();
					retStatus = true;
				}
			}
			else
			{
				editProfile_showSuccessMsg();
				retStatus = true;
			}
		}
		else 
		{ 	// empty data = no err
			editProfile_showSuccessMsg();
			retStatus = true;
		}
		
	}
	else 
	{ 	// empty data = no err
		editProfile_showSuccessMsg();
		retStatus = true;
	}
	
	return retStatus;
} 

var	editProfile_showSuccessMsg = function() {
	lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "confirmation", "<fmt:message key="label.editprofile.update.successful" />"); 
}

var gbDataSaved = true;
var editPageEditorsIds = new Array();

var gbIsDirty = false;

function editProfile_saveForm( formObj, gotoOnSuccess ) {
	lconn.profiles.ProfilesCore.hideInfoMsg("profileInfoMsgDiv");
	
	if( !formObj ) {
		lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", "<fmt:message key="label.editprofile.update.internalErr" />");
		return false;
	}
	
	if( typeof(gotoOnSuccess) == "undefined") gotoOnSuccess = "";


	//----------[  Specific checks for fields  ]
	// assistant field: clear hidden uid fields if blanked assistant name
	var assistantField = dojo.byId("secretaryUid.displayName");
	if( assistantField && dojo.string.trim(assistantField.value).length == 0) 
		dojo.byId("secretaryUid.displayName").value = "";
	
	// rich text fields:  update textarea fields with value from rich text field because the textarea is the field that is saved
	var editObj = lconn.profiles.ckeditor;
	var allEditors = editObj.getAllInstances();
	for (idTmp in allEditors) {
		if (allEditors[idTmp]) {
			editObj.resetDirty(idTmp);
			
			var valueTmp = editObj.getData(idTmp);
			formObj.elements["attribute(" + idTmp + ")"].value = valueTmp;
			
			// In IE, the hidden field will be saved instead of the rte field ..._Editor
			// In firefox, the rte field ..._Editor will not exist in the form because dojo removes it
			if (formObj.elements["attribute(" + idTmp + ")_Editor"]) {
				formObj.elements["attribute(" + idTmp + ")_Editor"].value = valueTmp;			
			}
		}
		

	}
	
	gbDataSaved = true;

	//	formObj.submit();
	var lang = "<profiles:appLang />";
	var result = editProfile_xhrPost( 
						"/ajax/editMyProfile.do"+(lang!=""?"?lang="+lang:""), 
						formObj, 
						function( data, ioArgs ) {
							if( editProfile_xhrCheckPostResults(data, ioArgs)) {
								profiles_goto( gotoOnSuccess); 
							}
						}
					);
	return true; 
}

function dataChange( fieldObj ) {
	if ( fieldObj ) {
		if ( fieldObj.id && fieldObj.id == "secretaryUid.displayName") { // assistant field
			// clear uid field on blanked assistant
			if( fieldObj.value == "") {
				var e = document.getElementById("secretaryUid");
				if(e) e.value = "";
			}
		}
	}
	
	if (gbDataSaved) {
		lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "warning", "<core:escapeJavaScript><fmt:message key="label.editprofile.update.unsaved" /></core:escapeJavaScript>");
	}
	gbDataSaved = false;

	if (typeof window.onbeforeunload !== "function") {
		window.onbeforeunload = onBeforeUnloadHandler;
	}
	
	if (!gbIsDirty) {
		// enable the save buttons
		dojo.query("#editProfile input[ name = submitButton ]").forEach(function(el){
			dojo.removeClass(el, "lotusBtnDisabled");
			dojo.removeAttr(el, "disabled");
		});
			
		//Fix for IE double-asking if the user wants to leave the page on edit.
		//This is caused by a "javascript:" href attribute of a link.
		//Need to reset these for it to work properly
		if (dojo.isIE) {
			var newHref = (document.location.href + "#_").split("#")[1];  //if there is a hash, we want to use that when resetting the href;
			var aLinks = dojo.query('a[href^="javascript:"]');  //get all javascript: links
			
			for (var ii = aLinks.length; ii >=0; ii--) {
				var aLink = aLinks[ii-1];
				if (aLink && dojo.hasAttr(aLink, "href")) {
					var sHref = dojo.attr(aLink, "href");
					if (sHref.indexOf("javascript:;") != 0 && sHref.indexOf("javascript:void(0);") != 0) {  //it's not just a dummy, it's doing something.
						dojo.attr(aLink, "onclick_proc", sHref.split("javascript:")[1]);
						dojo.connect(
							aLink, 
							"onclick", 
							aLink, 
							function() {
								dojo.eval(dojo.attr(this, "onclick_proc"));
							}
						);
					}
				
					dojo.attr(aLink, "href", "#" + newHref);
				}
			}
		}
	}
	gbIsDirty = true;
}

function onBeforeUnloadHandler( formObj )
{
	<%-- 
	SPR#HBJH7F3HCB - cannot have mixed language in a dialog.  the browser's onbeforeunload dialog
	contains strings in the browser's native language, thus, we cannot supplement the dialog with 
	strings in another language, such as the warning below
	<fmt:message key="label.editprofile.update.warning" var="UpdateWarningMsg" />
	--%> 
	if (!gbDataSaved) { 
		return "${UpdateWarningMsg}";
	}
}
</script>

<c:if test="${!empty editProfileForm}">
	<%
	request.setAttribute("ptype", com.ibm.peoplepages.util.appcntx.AppContextAccess.getContext().getCurrentUserProfile().getProfileType());
	%>
	<jsp:setProperty name="editProfileForm" property="profileType" value="${ptype}"/>
</c:if> 

<c:choose>
	<c:when test="${isNewUI}">
		<div id="_profileEditScene_div" class="lotusContent cnx8Label" role="main">
	</c:when>
	<c:otherwise>
		<div id="_profileEditScene_div" class="lotusContent" role="main">
	</c:otherwise>
</c:choose>
	<html:errors />
	<a id="mainContent" name="mainContent"></a><!-- skip links for accessibility -->
	<jsp:include page="/WEB-INF/jsps/html/scenes/editProfile/welcome.jsp" />
	<c:choose>
		<c:when test="${isNewUI}">
			<div><h1><fmt:message key="label.editprofile.title"><fmt:param><c:out value="${loggedInUserDisplayName}" escapeXml="true"/></fmt:param></fmt:message></h1></div>
			<div id="profileInfoMsgDiv" class="lotusHidden" role="alert"></div>
			<div>
				<tags:editProfileTabs />
			</div>
		</c:when>
		<c:otherwise>
			<div class="lotusHeader"><h1><fmt:message key="label.editprofile.title"><fmt:param><c:out value="${loggedInUserDisplayName}" escapeXml="true"/></fmt:param></fmt:message></h1></div>
			<div id="profileInfoMsgDiv" class="lotusHidden" role="alert"></div>
			<div class="lotusTabContainer">
				<tags:editProfileTabs />
			</div>
		</c:otherwise>
	</c:choose>
	<!-- test if a widget is found here	
	<div id="centerWidgetContainer">
		<span id="widget-container-col2" class="widgetContainer"/>
	</div>
	-->
		
	<div role="tabpanel" aria-label="<fmt:message key="label.editprofile.tab.content" />">
		<c:if test="${isContactInfo}"><tags:editProfileContactInfoForm /></c:if>
		<c:if test="${isAboutMe}"><tags:editProfileAboutMeForm /></c:if>
		<c:if test="${isPhoto}"><tags:editProfilePhotoForm /></c:if>
		<c:if test="${isPronunciation}"><tags:editProfilePronunciationForm /></c:if>
	</div>
	
	<!-- Hack to force loading of resources to show popup card since typeahead results are not 'vcard' classed -->
	<div style='display: none;'>
		<span class='vcard'>
		     <span class='fn url'>&nbsp;</span>
		     <span class='x-lconn-userid' style='display:none'>&nbsp;</span>
		</span>
	</div>
</div>

<c:if test="${(fromValidationErr == true) && (isPronunciation != true)}">
	<script type="text/javascript">
		dataChange(null);
	</script>
</c:if>

<script type="text/javascript">
	lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "info", "<fmt:message key="label.editprofile.heading" />");
	// a11y: need a delay for the screen readers to pick up the message
	setTimeout(function(){ lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "info", "<fmt:message key="label.editprofile.heading" />"); }, 2000); 
</script>
