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
<%@ 
tag body-content="empty" %><%@ 
attribute name="attributeType" required="true" rtexprvalue="true" %><%@ 
attribute name="attribute" required="true" rtexprvalue="true" %><%@ 
attribute name="uidAttribute" required="false" rtexprvalue="true" %><%@ 
attribute name="useridAttribute" required="false" rtexprvalue="true" %><%@ 
attribute name="emailAttribute" required="false" rtexprvalue="true" %><%@ 
attribute name="showEmail" required="false" rtexprvalue="true" %><%@
attribute name="prependHtml" required="false" rtexprvalue="true" %><%@ 
attribute name="appendHtml" required="false" rtexprvalue="true" %><%@ 
attribute name="searchResult" required="false" rtexprvalue="true"%><%@ 
attribute name="lastUpdate" required="false" rtexprvalue="true" %><%@ 
attribute name="altValue" required="false" rtexprvalue="true" %><%@ 
taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %><%@ 
taglib prefix="fmt" 	uri="http://java.sun.com/jsp/jstl/fmt" %><%@ 
taglib prefix="fn" 		uri="http://java.sun.com/jsp/jstl/functions" %><%@ 
taglib prefix="html" 	uri="http://jakarta.apache.org/struts/tags-html" %><%@ 
taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %><%@ 
taglib prefix="tiles" 	uri="http://jakarta.apache.org/struts/tags-tiles" %><%@ 
taglib prefix="tags"	uri="/WEB-INF/tags" %>
<%--
<script type="text/javascript">
	alert('tags: \n'+
		'attributeType="${attributeType}" \n'+ 
		'attribute="${attribute}" \n'+
		'uidAttribute="${uidAttribute}"  \n'+
		'useridAttribute="${useridAttribute}" \n'+		
		'showEmail="${showEmail}"  \n'+
		'emailAttribute="${emailAttribute}" \n'+
		'prependHtml="${prependHtml}"/> \n' +
		'appendHtml="${appendHtml}"/> \n' +
		''
	);
</script>
--%>
<c:choose>
	<c:when test="${attributeType == 'hcard'}">
       <c:if test="${prependHtml != null}">${prependHtml}</c:if>
       <span class="vcard">
       		<c:choose>
	       		<c:when test="${searchResult}">
	       			<h3> 
		       			<html:link action="/html/profileView" paramId="key" paramName="uidAttribute" styleClass="fn url person">
		       				<c:out value="${attribute}" /> 
		       			</html:link>
	       			</h3>
	       		</c:when>
	       		<c:otherwise>
					<html:link action="/html/profileView" paramId="userid" paramName="useridAttribute" styleClass="fn url">
						<c:out value="${attribute}" />
					</html:link>
				</c:otherwise>
			</c:choose>
	    	<c:choose>
		    	<c:when test="${!empty useridAttribute}">
					<span class='x-lconn-userid' style='display: none;'><c:out value="${useridAttribute}"/></span>
				</c:when>
				<c:otherwise>
					<span class='email' style='display: none;'><c:out value="${emailAttribute}"/></span>
				</c:otherwise>
			</c:choose>
       </span>
       <c:if test="${appendHtml != null}">${appendHtml}</c:if>
	</c:when>
	<c:when test="${attributeType == 'sametimeLink'}">
		<c:if test="${sametimeLinksSvcLocation != null}">
	        <c:if test="${prependHtml != null}">${prependHtml}</c:if>	
			<c:choose>
				<c:when test="${cookie.LtpaToken == null}">
					<span id="imStatus">
						<html:link action="/auth/loginRedirect"> 
							<fmt:message key="label.profile.im.signin" />
						</html:link>
					</span>
				</c:when>
				<c:otherwise>
					<span class="imStatus ${emailAttribute}_status">
						<script type="text/javascript">
							writeSametimeLink('${emailAttribute}', '${attribute}', true, 'icon:no');
						</script>
					</span>
				</c:otherwise>
			</c:choose>
			<c:if test="${appendHtml != null}">${appendHtml}</c:if>
		</c:if>
	</c:when>
	<c:when test="${attributeType == 'email'}">
		<c:if test="${showEmail}">
			<html:link href="mailto:${fn:escapeXml(attribute)}">
		        <c:if test="${prependHtml != null}">${prependHtml}</c:if><c:out value="${attribute}" /><c:if test="${appendHtml != null}">${appendHtml}</c:if>
			</html:link>
		</c:if>
	</c:when>
	<c:when test="${attributeType == 'blogUrl'}">
	    <c:if test="${!empty fn:trim(attribute)}">
		    <c:choose>
			    <c:when test="${fn:contains(attribute,'://')}">
			        <a href="${profiles:escapeUnwiseURLChars(fn:trim(attribute))}" target="_blank">
			    </c:when>
			    <c:otherwise>
			        <a href="http://${profiles:escapeUnwiseURLChars(fn:trim(attribute))}" target="_blank">
			    </c:otherwise>
			</c:choose>
	        <c:if test="${prependHtml != null}">${prependHtml}</c:if><fmt:message key="label.profile.blogUrl" /><%--${attribute}--%><c:if test="${appendHtml != null}">${appendHtml}</c:if>
			</a>
		</c:if>		
	</c:when>
	<c:when test="${attributeType == 'link'}">
	    <c:if test="${!empty fn:trim(attribute)}">
			<a href="${profiles:escapeUnwiseURLChars(attribute)}" target="_blank">
	        	<c:if test="${prependHtml != null}">${prependHtml}</c:if><fmt:message key="label.profile.link" /><%--${attribute}--%><c:if test="${appendHtml != null}">${appendHtml}</c:if>
			</a>
		</c:if>
	</c:when>
	<c:when test="${attributeType == 'photo'}">
		<img src='<c:url value="/photo.do?key=${uidAttribute}&lastMod=${lastUpdate}"/>' class="photo"  width="55" height="55" alt="${altValue}"/>
	</c:when>
	<c:when test="${attributeType == 'richtext'}">
		<c:if test="${prependHtml != null}">${prependHtml}</c:if>${attribute}<c:if test="${appendHtml != null}">${appendHtml}</c:if>
	</c:when>
	<c:otherwise>
        <c:if test="${prependHtml != null}">${prependHtml}</c:if><c:out value="${attribute}" /><c:if test="${appendHtml != null}">${appendHtml}</c:if>
	</c:otherwise>
</c:choose>
