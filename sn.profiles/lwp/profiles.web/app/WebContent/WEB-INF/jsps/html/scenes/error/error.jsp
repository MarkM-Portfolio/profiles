<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2019                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="bean"	    uri="http://jakarta.apache.org/struts/tags-bean"%>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="logic"		uri="http://jakarta.apache.org/struts/tags-logic"%>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>

<html:xhtml/>
<script type="text/javascript" defer="defer">
<%--
note: this javascript must remain in this JSP because if moved to other resource, or
the attempt to use js from other resources, such resource may not even be loaded (hence we
have an error) thus this error scene must remain as simple and free standing as possible, 
as in no dojo usage and no minimize external resource usage
--%> 
var errorToggleArea = function( id ) {
	element = document.getElementById(id);
	if(element) {
		return( (element.style.display == "block" )? errorHideArea( id ) : errorShowArea( id ) ); 
	}
	return false;
}
var errorShowArea = function( id ) {
	element = document.getElementById(id);
	if(element) {
		element.style.visibility = (element.style.visibility == "hidden" ? "visible" : "");
		element.style.display = (element.style.display == "none" ? "block" : "");
		return true;
	}
	return false;
};
var errorHideArea = function( id ) {
	element = document.getElementById(id);
	if(element) {
		element.style.visibility = (element.style.visibility == "visible" ? "hidden" : "");
		element.style.display = (element.style.display == "block" ? "none" : "");
		return true;
	}
	return false;
};
</script>
<div class="lotusContent" role="main">
	<a id="mainContent" name="mainContent"></a><!-- skip links for accessibility -->
	<div class="lotusErrorBox lotusError">
		<div class="lotusErrorContent">
			<img src="<core:blankGif />" class="iconsMessages48 iconsMessages48-msgError48" alt="" />
			<div class="lotusErrorForm">
				<h1><fmt:message key="label.error.heading" /></h1>
				<p>
					<c:choose>
						<c:when test="${!empty showAltErrMsg}"><fmt:message key="${showAltErrMsg}"></fmt:message></c:when>
						<c:otherwise><fmt:message key="label.error.body" /></c:otherwise>
					</c:choose>
				</p>
				<form method="get" action="home.do">
					<div class="lotusBtnContainer">
						<fmt:message key="label.error.backtoprofileslink" var="backBtnText"/>
						
						<input class="lotusBtn lotusBtnSpecial lotusLeft" type="submit" value="${backBtnText}" />
						<html:link styleId="showFullErrorLink" href="#" onclick="showErrorDetails();errorHideArea(this.id);errorShowArea('hideFullErrorLink');" styleClass="lotusAction" style="display:block"><fmt:message key="label.error.showfullerrormessagelink" /></html:link>
						<html:link styleId="hideFullErrorLink" href="#" onclick="hideErrorDetails();errorHideArea(this.id);errorShowArea('showFullErrorLink');" styleClass="lotusAction" style="display:none"><fmt:message key="label.error.hidefullerrormessagelink" /></html:link>
					</div>
					<p class="lotusErrorDetails" style="display: none;" id="lconnErrorDetails">
						<textarea wrap="off" class="lotusText" readonly="readonly" id="lconnErrorText"></textarea>
					</p>
					
					<script type="text/javascript">
					
						function hideErrorDetails(){
						  var details = document.getElementById('lconnErrorDetails');
						  details.style.display = 'none';
						  details.style.visibility = 'hidden';
						}
						function showErrorDetails() {
				                  var details = document.getElementById('lconnErrorDetails');
				                  var input = document.getElementById('lconnErrorText'); 
				         
				                  var text = [];
				                  try {
				                     text.push("Time: "+(window.loadTime || new Date()));
				                     if (window.location)
				                        text.push("Location: "+window.location.href);
				                     if (window.navigator) {
				                        text.push("User-Agent: "+window.navigator.userAgent);
				                        text.push("Language: "+ (window.navigator.language || window.navigator.userLanguage));
				                     }
				                     try {
				                        var plugins = window.navigator.plugins;
				                        if (plugins) {
				                           for (var i=0; i<plugins.length; i++) {
				                              var plugin = plugins[i];
				                              text.push("Plugin: [name='"+plugin.name+"' filename='"+plugin.filename+"' version='"+plugin.version+"' description='"+plugin.description+"']");
				                           }
				                        }
				                     } catch (e) {
				                        text.push("Plugins: "+e);
				                     }
				                  } catch (e) {
				                     text.push("Browser Error: "+e);
				                  }
				                  text.push("---");
<%--				    This can leak Java class names, removing to conform to security standard              				                  
						<c:choose>
							<c:when test="${!empty pageContext.exception}">
								<c:set var="exception" value="${pageContext.exception}" />
								var msg = "<c:out value='${exception}' />";
							</c:when>
							<c:otherwise>
								<bean:define id="exception2" name="org.apache.struts.action.EXCEPTION" type="java.lang.Exception"/>
								var msg = "<c:out value='${exception2}' />";
							</c:otherwise>
						</c:choose>
--%>						
                          var msg = "Invalid Request";						
						  if(msg.indexOf('com.ibm')==0) 
							msg = msg.substring(msg.lastIndexOf('.')+1);
						  text.push(msg);	

				                  input.value = text.join("\n");
				              
				                  details.style.display=''; 
						  details.style.visibility = '';
				                  input.focus(); 
				                  input.select();     
				               }
				            </script>
					
				</form>
			</div>
		</div>
	</div>
</div>
