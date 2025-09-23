<%@ page contentType="text/html;charset=UTF-8" %>

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

<!-- colLeft -->
<div class="lotusColLeft">
		
	<!-- searchProfilesTips -->
	<div class="lotusInfoBox">
		<h3><span class="lotusLeft"><fmt:message key="label.search.profiles.tips" /></span></h3>
		<p> <fmt:message key="label.search.profiles.tip1" /> </p>
		<p> <fmt:message key="label.search.profiles.tip2" /> </p>
	</div> <!-- end searchProfilesTips -->
	<!-- newToProfilesHelp -->
	<c:if test="${cookie.newProfilesHelp.value != false}">
		<div class="lotusInfoBox" id="newToProfilesHelp">
			<h3>
				<span class="lotusLeft"><fmt:message key="label.search.profiles.help.heading" /></span>
				<span class="lotusRight">
					<html:link href="#" styleId="close_newToProfilesHelp" titleKey="label.search.profiles.help.close.alttext">
						<html:img altKey="label.search.profiles.help.close.alttext" page="/nav/common/styles/images/iconCloseTips.gif" />
					</html:link>
				</span>
			</h3>
			<p><html:link href="#" styleClass="lotusAction help_link"> <fmt:message key="label.search.profiles.helplink" /> </html:link></p>
		</div> <!-- end newToProfilesHelp -->
	</c:if>

</div> <!-- end colLeft -->
