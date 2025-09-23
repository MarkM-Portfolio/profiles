<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2015                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%-- 5724-S68                                                          --%>
<%@ tag body-content="empty" %>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/>

<c:choose>
<c:when test="${isNewUI}">
<ul id="editProfileTabs" class="lotusTabs cnx8LotusTabs" role="toolbar" aria-label="<fmt:message key="label.editprofile.tab.toolbar" />">
	<li id="editTabContactInfo">
		<a role="button" id="aEditTabContactInfo" href='<c:url value="/html/editMyProfileView.do?tab=contactInfo"/>'><fmt:message key="label.editprofile.contactInformation.tab.label" /></a>
	</li>
	<li id="editTabAboutMe">
		<a role="button" id="aEditTabAboutMe" href='<c:url value="/html/editMyProfileView.do?tab=aboutMe"/>'><fmt:message key="label.editprofile.background.tab.label" /></a>
	</li>
	<c:if test="${isPhotoEnabled && canUpdatePhoto}">
		<li id="editTabPhoto">
			<a role="button" id="aEditTabPhoto" href='<c:url value="/html/editMyProfileView.do?tab=photo"/>'><fmt:message key="label.editprofile.photo.tab.label" /></a>
		</li>
	</c:if>
	<c:if test="${isPronunciationEnabled && canUpdateProunciation}">
		<li id="editTabPronounciation">
			<a role="button" id="aEditTabPronounciation" href='<c:url value="/html/editMyProfileView.do?tab=pronunciation"/>'><fmt:message key="label.editprofile.pronunciation.tab.label" /></a>
		</li>
	</c:if>
</ul>
</c:when>
<c:otherwise>
<ul id="editProfileTabs" class="lotusTabs" role="toolbar" aria-label="<fmt:message key="label.editprofile.tab.toolbar" />">
	<li id="editTabContactInfo">
		<a role="button" id="aEditTabContactInfo" href='<c:url value="/html/editMyProfileView.do?tab=contactInfo"/>'><fmt:message key="label.editprofile.contactInformation.tab.label" /></a>
	</li>
	<li id="editTabAboutMe">
		<a role="button" id="aEditTabAboutMe" href='<c:url value="/html/editMyProfileView.do?tab=aboutMe"/>'><fmt:message key="label.editprofile.background.tab.label" /></a>
	</li>
	<c:if test="${isPhotoEnabled && canUpdatePhoto}">
		<li id="editTabPhoto">
			<a role="button" id="aEditTabPhoto" href='<c:url value="/html/editMyProfileView.do?tab=photo"/>'><fmt:message key="label.editprofile.photo.tab.label" /></a>
		</li>
	</c:if>
	<c:if test="${isPronunciationEnabled && canUpdateProunciation}">
		<li id="editTabPronounciation">
			<a role="button" id="aEditTabPronounciation" href='<c:url value="/html/editMyProfileView.do?tab=pronunciation"/>'><fmt:message key="label.editprofile.pronunciation.tab.label" /></a>
		</li>
	</c:if>
</ul>
</c:otherwise>
</c:choose>


<script type="text/javascript">
dojo.addOnLoad(
    function(){
		// highlight selected tab based on current URL
		var tab = (window.location.href + "#").split("#")[0] + "&tab=&"; //make sure we got a default tab
		tab = ((tab.split("tab=")[1]).split("&")[0]).replace("pronunciation","pronounciation"); //massage the data so it matches what we expect
		var suffix = tab.charAt(0).toUpperCase() + tab.slice(1);  //ProperCase the string for the suffix

		var idx = -1;
		dojo.query('#editProfileTabs li').forEach( 
			function(node, index, arr){
				if (idx < 0) {
					if (tab === "" && index == 0) {
						idx = index;
					} else 
					if (node.id === "editTab" + suffix) {
						idx = index;
					}
				}
			}
		);
		
		try {
			new lconn.profiles.aria.Toolbar("editProfileTabs", {"selIdx": idx, "showSelect": (idx > -1)});
		} catch (e) {
			if (console) console.error(e);
		}

    }
);
</script>