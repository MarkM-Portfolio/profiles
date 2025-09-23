<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<profiles:isLotusLive var="isLotusLive"/>
<c:choose>
	<c:when test="${isLotusLive == 'false'}">
		{<c:forEach items="${profiles}" var="profile">
			"profile_key": "${profile.key}",
			<c:choose>
				<c:when test="${!empty profile.email}">
				"email": "${profiles:encodeForJsonString(profile.email)}",
				</c:when>
				<c:otherwise>
            	"email": "",
         		</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${!empty profile.givenName}">
				"firstName": "${profiles:encodeForJsonString(profile.givenName)}",
				</c:when>
				<c:otherwise>
            	"firstName": "",
         		</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${!empty profile.surname}">
				"lastName": "${profiles:encodeForJsonString(profile.surname)}",
				</c:when>
				<c:otherwise>
            	"lastName": "",
         		</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${!empty profile.displayName}">
				"displayName": "${profiles:encodeForJsonString(profile.displayName)}"
				</c:when>
				<c:otherwise>
            	"displayName": ""
         		</c:otherwise>
			</c:choose>
		</c:forEach>}
	</c:when>
</c:choose>