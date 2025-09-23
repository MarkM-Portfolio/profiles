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

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<profiles:isLotusLive var="isLotusLive"/>
<c:if test="${isLotusLive == 'true'}">
	<span id="liProfileActionExtension"><%-- SmartCloud Organization Extensions --%></span>
	<script type="text/javascript">
		dojo.addOnLoad(function(){
			var loadIdx_ = 0;
			var loadActionExtensions_ = function() {
				var objBase = dojo.getObject("lconn.profiles.ActionExtensions");
				var elBase = dojo.byId("liProfileActionExtension");
				
				if (!objBase || !elBase) {
					if (loadIdx_++ < 20) {
						setTimeout(loadActionExtensions_, 500);
					}
					return;
				}
				
				new objBase(
					{
						user_id: '<core:escapeJavaScript>${userid}</core:escapeJavaScript>'
					},
					elBase
				);
			};
			loadActionExtensions_();						
		});
	</script>
</c:if>