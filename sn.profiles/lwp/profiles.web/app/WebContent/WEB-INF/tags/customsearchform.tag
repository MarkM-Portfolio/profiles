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


<html:form styleId="customSearchForm" styleClass="lotusSearch" action="/html/simpleSearch.do" method="get">
	
	<html:hidden property="lang" value="<profiles:appLang />" />
	
	<div class="lotusChunk">
		<fmt:message key="label.search.profiles.profilesearch.bycustomfields.button" var="searchBtnText" />
		<fmt:message key="label.search.profiles.profilesearch.bycustomfields.button.hint" var="searchBtnHint" />
		<html:submit value="${searchBtnText}" alt="${searchBtnHint}" styleClass="lotusBtnSpecial" />
	</div>
</html:form>
