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
<%@ tag body-content="empty" %><%@ 
attribute name="advFormLayout" required="true" rtexprvalue="true" %>
<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 	uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 		uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 	uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 	uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="tags"	uri="/WEB-INF/tags" %>

<fmt:setBundle basename="com.ibm.lconn.profiles.strings.uilabels" var="attributeLabels" scope="page" />

<html:form styleId="advancedSearchForm" styleClass="lotusSearch" action="/html/advancedSearch.do" method="get">



	<c:forEach items="${advFormLayout.attributes}" var="attribute" varStatus="status">
	<label>  <c:out value="${attribute.attributeId}" />	</label> <br />
	<html:text property="name" size="40" styleClass="lotusText" style="width:25em" styleId="name" />
</c:forEach>
	
	<div class="lotusChunk">
		<fmt:message key="search.dropdown.advancedsearch" var="searchBtnText" />
		<fmt:message key="label.search.profiles.profilesearch.bycontactinfo.button.hint" var="searchBtnHint" />
		<html:submit value="${searchBtnText}" alt="${searchBtnHint}" styleClass="lotusBtnSpecial" />
	</div>
</html:form>
