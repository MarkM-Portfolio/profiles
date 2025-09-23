<%@ page contentType="text/html;charset=UTF-8" %>

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

<%--
	@author sberajaw
--%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="stripes" 	uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ taglib prefix="corenav"		uri="http://www.ibm.com/lconn/tags/corenav" %>
<%@ taglib prefix="lc-ui"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>

<html:xhtml/>
<!-- nav footer -->
<div id="lotusFooter" class="lotusFooter" role="contentinfo">
	<corenav:footer appname="profiles" admin="admin"/>
</div>
<div>
		<lc-ui:legal />
</div>

<script type="text/javascript">
dojo.query(".lotusLicense").forEach(function(node) { /*a11y*/
	var string = node.innerHTML;
	var stringlist = string.split("\"");
	if (stringlist.length % 2 == 1) {
		for (var ii = 0; ii < stringlist.length; ii++) {
			if (ii % 2 == 1) {
				stringlist[ii] = "<q>" + stringlist[ii] + "</q>";
			}
		}
		node.innerHTML = stringlist.join("");
	}
});
</script>