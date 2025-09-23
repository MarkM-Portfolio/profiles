<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2013                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%-- 5724-S68                                                          --%>
<%@ tag body-content="empty" %>

<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 	uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 		uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 	uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 	uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="tags"	uri="/WEB-INF/tags" %>
<%@ taglib prefix="lc-ui"	uri="http://www.ibm.com/lconn/tags/coreuiutil" %>

<html:xhtml/>
<div class="lotusLoginBox">
	<div class="lotusLoginContent">
		<div class="lotusLoginLogo"></div>
		<html:img width="175" height="175" page="/nav/common/styles/images/iconProfilesJumboBlue.jpg" altKey="label.login.profiles.heading" />
		<div class="lotusLoginForm">
			<h1> <fmt:message key="label.login.profiles.heading" /> </h1>
			<h2> <fmt:message key="label.login.profiles.subheading" /> </h2>

			<form id="loginForm" name="loginForm" action="j_security_check" method="post" class="login" focus="username">
				<div>
					<label for="j_username"><fmt:message key="label.login.profiles.username" />
						<c:if test="${errorMessages != null}">
							<span class="lotusFormError">
								${errorMessages}
							</span>
						</c:if>
					</label>					
					<input type="text" name="j_username" size="25" value="" class="lotusText" id="j_username" />
				</div>
				<div>
					<label for="j_password"><fmt:message key="label.login.profiles.password" /></label>
					<input type="password" name="j_password" size="25" value="" class="lotusText" id="j_password" />
				</div>
				<div class="lotusBtnContainer">
					<fmt:message key="label.login.profiles.button.login" var="loginBtnText" />
					<input class="lotusBtn lotusBtnSpecial lotusLeft" type="submit" value="${loginBtnText}"/>
					<span>
						<a class="lotusAction" href="${profilesHrefServiceUrl}"><fmt:message key="label.login.profiles.button.cancel" /></a>
					</span>
				</div>		
			</form>
		</div>
		<div class="lotusDescription">		
			<h2><fmt:message key="label.login.profiles.intro.header" /></h2>
			<p><fmt:message key="label.login.profiles.intro.body" /></p>
		</div>
	</div>
	<%-- 
	<div class="lotusLegal">
		<p class="lotusLicense"><fmt:message key="label.login.profiles.copyright" /></p>
	</div>
	--%>
	<table cellspacing="0" class="lotusLegal">
		<tbody>
			<tr>
				<td><img src="<lc-ui:blankGif />" class="brandingLogos15 brandingLogos15-ibmLogoOpaque16" alt="<fmt:message key="label.login.profiles.ibmlogo" />" /></td>	
				<td class="lotusLicense"><fmt:message key="label.login.profiles.copyright" /></td>
			</tr>
		</tbody>
	</table>
</div>

<script type="text/javascript">
document.getElementById('j_username').focus();

function profilesTestHighContrastMode( ) 
{
	// javascript to check for high contrast/images off mode, and add body.lotusImagesOff if necessary
	// adding this inline because tiles doesnt have a satisfactory onload method
	
	//create test case element
	var vTestHC = document.createElement("div");
	vTestHC.style.border          = '1px solid';
	vTestHC.style.borderColor     = 'red green';
	vTestHC.style.position        = 'absolute';
	vTestHC.style.height          = '5px';
	vTestHC.style.top             = '-999px';
	vTestHC.style.backgroundImage = 'url(<lc-ui:blankGif />)';
	
	document.body.appendChild(vTestHC);
	
	//do the tests
	var vStyle = null;
	try{
		vStyle = document.defaultView.getComputedStyle(vTestHC, "");
	}catch(e){
		vStyle = vTestHC.currentStyle;
	}
	if (vStyle) {
		var vTestImg = vStyle.backgroundImage;
		if ((vStyle.borderTopColor==vStyle.borderRightColor) || (vTestImg != null && (vTestImg == "none" || vTestImg == "url(invalid-url:)" ))){
			document.getElementsByTagName("body")[0].className+=" lotusImagesOff";
		}
	}
}

profilesTestHighContrastMode();
</script>
