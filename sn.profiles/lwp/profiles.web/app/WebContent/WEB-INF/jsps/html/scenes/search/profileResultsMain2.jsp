<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2010, 2014                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%-- 
    @author <a href="mailto:adebiyi@us.ibm.com">Bilikiss O. Adebiyi</a>
	@author <a href="mailto:ahernm@us.ibm.com">Michael Ahern</a>
--%><%@ 
	page contentType="text/html;charset=UTF-8" %><%@ 
	
	taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %><%@ 
	taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %><%@ 
	taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %><%@ 
	taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %><%@ 
	taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %><%@ 
	taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %><%@ 
	taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/><%--

--%><jsp:useBean id="lconn_profiles_web_ui_searchresults" 
			 class="com.ibm.lconn.profiles.web.util.UIAttributeWriterConfig"
			 scope="application">
	<jsp:setProperty name="lconn_profiles_web_ui_searchresults"
		property="startAttr" value="<div>"/>
	<jsp:setProperty name="lconn_profiles_web_ui_searchresults"
		property="endAttr" value="</div>"/>
</jsp:useBean>
<div role="region" aria-label="<c:out value="${searchInfoHeaderText}" escapeXml="false" />">
	<table id="searchResultsTable" class="lotusTable" border="0" cellspacing="0" cellpadding="0" summary="<c:out value="${searchInfoHeaderText}" escapeXml="false" />">			
		<tbody>
			<profiles:outputSearchResults
				results="${searchResultsPage.results}"
				section="directory"/>
		</tbody>
	</table>
</div>
