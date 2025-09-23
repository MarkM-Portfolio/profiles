<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2010                                    --%>
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
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/>

<ul>
    <c:forEach items="${profiles}" var="profile">
    	<li class="lotusPerson" onmouseover="showSlimCard('${profile[lconnUserIdAttr]}', 'slimCardArea');" onmouseout="hideSlimCard('slimCardArea');" >
    		<c:if test="${param.showPhoto == 1}">
	    		<label>
					<img src="<profiles:serviceLink serviceName='profiles'>${svcRef}</profiles:serviceLink>/photo.do?key=${profile.key}&lastMod=<profiles:outputLMTime time='${profile.lastUpdate}'/>" alt="${displayName}" height="35" width="35">
	    		</label>
	    	</c:if>
    		<label>
    			<c:set var="displayName" value="${profile.displayName}" />
    			${displayName}
    		</label> &nbsp;
    		<label>
    			<c:set var="city" value="${profile.workLocation.city}" />
				${fn:toUpperCase(city)}
    		</label> &nbsp;
			<c:if test="${showEmail}">
	    		<label>
	    			<c:set var="email" value="${profile.email}" />
					&lt;${email}&gt;
	    		</label>
    		</c:if>
    		<html:hidden property="uid" value="${profile.uid}" />
    		<html:hidden property="X_lconn_userid" value="${profile[lconnUserIdAttr]}" />
    	</li>
    </c:forEach>
</ul>

