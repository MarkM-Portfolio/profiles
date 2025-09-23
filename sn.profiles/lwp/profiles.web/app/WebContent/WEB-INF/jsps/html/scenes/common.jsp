<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2014                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%--
	@author testrada
--%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>

<html:xhtml/>

<%--  Alert Dialog --%>
<div id="lotusAlertDialog_div" style="display:none;">
	<div class="lotusDialogBorder">
		<div id="alertForm" class="lotusDialog lotusForm" style="min-width: 400px;" >
			<div class="lotusDialogHeader">
				<h1 class="lotusHeading"><fmt:message key="label.page.profiles" /></h1>
				<a title='<fmt:message key="close"/>' role="button" class="lotusRight lotusDialogClose" href="javascript:;" onclick="dijit.byId('lotusAlertDialog').hide(); return true;">
					<img src="<core:blankGif />" role="presentation" alt='<fmt:message key="close"/>' />
					<span class="lotusAltText">X</span>
				</a>
			</div>		
			<div id="lotusAlertDialogContent" class="lotusDialogContent" style="min-width: 400px;" role="alert">
			</div>
			<div class="lotusDialogFooter">
				<input value="<fmt:message key="close"/>" class="lotusFormButton" onclick="dijit.byId('lotusAlertDialog').hide(); return true;" type="submit" /> 
			</div>
		</div>
	</div>
</div>

<script type="text/javascript">
if( dojo ) {
	// create dojo dijit alert dialog
	dojo.require("dijit.Dialog");
	dojo.addOnLoad( function(){
	    var alertDlg = new dijit.Dialog(
			{ id: "lotusAlertDialog",
		      duration: 1,
		      title: "<fmt:message key="label.page.profiles" />",
		      style: "padding: 0px; border: none" 
		    }, 
			"lotusAlertDialog_div");
	});
}
</script>
