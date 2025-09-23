<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2016                                    --%>
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
<%@ taglib prefix="lc-ui"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/>
<c:set var="canEditPhoto" value="${ (key == loggedInUserKey) && canUpdatePhoto }" />
<% 
	String uri = (String)request.getAttribute("javax.servlet.forward.request_uri");
	request.setAttribute("requestURI", uri);
%>

<c:set var="photoKey" value="${key }" />
<%-- Hides photo for inactive user
<c:if test="${!isActive && !empty isActive}">
	<c:set var="photoKey" value="inactive" />
</c:if>	
--%>
<c:url var="photoUrl" value="/photo.do">
	<c:param name="key" value="${photoKey}"/>
	<c:param name="lastMod" value="${lastPhotoUpdate}"/>
</c:url>

<div class="lotusSection2" role="complementary" aria-label="<fmt:message key="label.editprofile.photo.editPhoto"/>">
	<div id="smallBizCardInfoPhotoDiv" class="lotusCenter">
		<c:choose>
			<c:when test="${canEditPhoto}">
				<c:url var="editProfilePhotoUrl" value="/html/editMyProfileView.do?tab=photo" />

				<a href="${editProfilePhotoUrl}" title="<fmt:message key="label.editprofile.photo.editPhoto" />">
					<span id="spanEditProfilePhoto" class="lotusHidden" style="position: relative; vertical-align: top; height: 0px; width: 0px; float: right;">
						<span id="aEditProfilePhoto">
							<img src="<lc-ui:blankGif />" class="lconnSprite lconnSprite-iconWidgetEdit16" alt="" onClick="profiles_goto('${editProfilePhotoUrl}');" style="cursor:hand" />
						</span>
					</span>
					<img id="imgProfilePhoto" src="${photoUrl}" alt="<fmt:message key="label.editprofile.photo.editPhoto"/> - <c:out value="${displayName}"/>" width="155px" height="155px" class="lconnProfilePortrait"/>			
				</a>
			</c:when>
			<c:otherwise>
				<img id="imgProfilePhoto" src="${photoUrl}" alt="<c:out value="${displayName}"/>" class="lconnProfilePortrait"/>
			</c:otherwise>
		</c:choose>
	</div>
</div>

<script type="text/javascript">
dojo.addOnLoad(
    function() {
    		var resetEditImgIcon = function() {
			// calculate the edit photo image icon placement
			var editImg = dojo.byId("aEditProfilePhoto");
			var editImgSpan = dojo.byId("spanEditProfilePhoto");
			var editWrapper = dojo.byId("smallBizCardInfoPhotoDiv");
			
			if(editImg && editImgSpan && editWrapper) {
				var imgWidth = ( dojo.style("imgProfilePhoto","width") <= 0? 155 : dojo.style("imgProfilePhoto","width")); //default to 155 if we cannot get the actual width 
				var containerWidth = ( dojo.style( editWrapper, "width" ) <= 0? 180 : dojo.style( editWrapper, "width" )); // default container width to 180 if we cannot get actual width
				var offsetWidth = ((containerWidth-imgWidth)/2)+3; // padded to 3 from the border of the img
				
				var args = { "background": "none repeat scroll 0 0 #FFFFFF",
							"position": "absolute",
							"opacity": 0.75,
							"top": "2px",
							"padding": "5px" };
				
				if (profiles_isBidiRTL) {
					dojo.style(editImgSpan,"float","left");
					args.left = offsetWidth+"px";
				} else { 
					dojo.style(editImgSpan,"float","right");
					args.right = offsetWidth+"px";
				}
	
				dojo.style(editImg, args);
				return true;
			}
			return false;
		}

		//connect the mouseout and mouseover events to show/hide the edit icon
		var editWrapper = dojo.byId("smallBizCardInfoPhotoDiv");
		if(editWrapper) {
			dojo.connect(editWrapper, "mouseover", 
				function(){ 
					var el=dojo.byId('spanEditProfilePhoto'); 
					if(el) { 
						if( resetEditImgIcon()) dojo.removeClass(el,'lotusHidden'); 
					}
				}
			);
	
			dojo.connect(editWrapper, "mouseout",  
				function(){ 
					var el=dojo.byId('spanEditProfilePhoto'); 
					if(el) dojo.addClass(el,'lotusHidden'); 
				}
			);
		}
	});
</script>