<%@ page contentType="text/html;charset=UTF-8" session="false"%>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2012                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>


<div id="div_exportVcardDialog" style="display:none;">
	<jsp:include page="vcard_popup_form.jsp"/>
</div>

<script type="text/javascript">
dojo.require("dijit.Dialog");
dojo.require("lconn.core.HelpLauncher");

dojo.addOnLoad(function() {
    var vcardDlg = new dijit.Dialog({
    							id: "exportVcardDialog",
						    	duration: 1,
						        style: "padding: 0px; border: none"}, 
							    "div_exportVcardDialog");
	vcardDlg.titleNode.innerHTML = "<fmt:message key='label.vcard.header.exportVcard'/>";
	dojo.addClass(vcardDlg.titleNode, "lotusAltText");
	var helpAnchor = document.getElementById("exportVcardHelpLink");
	lconn.core.HelpLauncher.createHelpLink(
		helpAnchor, 
		"<core:escapeJavaScript><fmt:message key='label.vcard.help.title'/></core:escapeJavaScript>", 
		"<core:escapeJavaScript><fmt:message key='label.vcard.help'/></core:escapeJavaScript>", 
		{
			HELP: "<core:escapeJavaScript><fmt:message key='label.vcard.help.alt'/></core:escapeJavaScript>",
			CLOSE: "<core:escapeJavaScript><fmt:message key='label.vcard.help.close'/></core:escapeJavaScript>"
		}, 
		false
	); 							    
});

function showVcardExport() {
 	var vcardDlg=dijit.byId("exportVcardDialog");
 	if( vcardDlg ) vcardDlg.show();
}

function hideVcardExportForDL() {
	return hideVcardExport();
}

function hideVcardExport() {
 	var vcardDlg=dijit.byId("exportVcardDialog");
 	if( vcardDlg ) vcardDlg.hide();
	return false;
}
</script>