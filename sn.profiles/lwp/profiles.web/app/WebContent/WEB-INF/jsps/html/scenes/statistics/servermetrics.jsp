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

<%-- 
 @author rmelanson
--%>
<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 	uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<!-- content -->	
<div class="lotusContent" role="main">	
<a id="mainContent" name="mainContent"></a><!-- skip links for accessibility -->

<div class="lotusHeader" align="${ bidir.alignment}"><h1><fmt:message key="label.metrics.title" /></h1>
	<div class="lotusDetails">
		<p><fmt:message key="label.metrics.asof"/>
		   <fmt:formatDate value="${profiles:getMetricDate(' ')}" type="both" dateStyle="short" timeStyle="short"/>
		</p>
	</div>
</div><!--end header-->

<dl class="lotusMetrics">
    <fmt:message var="dateErrorMsg" key="label.metrics.dataErrMsg" />
    <c:set var="token1"   value="0"    />
    <%-- <c:out value="${dateErrorMsg}"/>  --%>
    <%-- Special token value "0" below informs getMetricValueForKey to use arg as error msg  --%>
	<c:set var="reUseGetToSetErrorMsg" value="${profiles:getMetricValueForKey( token1, dateErrorMsg)}"/>


	<%-- Get the hashmap for this jsp invokation --%>
	<c:set var="token"   value="${profiles:getMetricHashMapToken()}"    />

	<%-- Get the list of keys (in the proper order) --%>
	<c:forEach var="keyStr" items="${profiles:getMetricKeyArrayStatic()}" varStatus="arr">
		<%-- Put the key in a variable so can use it as an argument --%>
		<c:set var="arrayKey"   value="${arr.current}"    />
		<dt>
			<%-- Out the metric value associated with the key --%>
			<span class="lotusRight"><c:out value="${profiles:getMetricValueForKey( token, arrayKey)}"/></span>
			<%-- Out the key description --%>
			<fmt:message key="${arr.current}"/>
		</dt>
		<dd>
			<%-- Out the detailed description --%>
			<fmt:message key="${arr.current}.desc"/>
		</dd>
        </c:forEach>

        <%-- Destroy the hashmap for this jsp invokation --%>
        <c:set var="result"   value="${profiles:destroyMetricHashMap( token)}"    />
</dl>

</div> <!-- end content -->

