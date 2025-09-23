<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- HCL Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright HCL Technologies Limited 2001, 2022                     --%>
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
<%@ taglib prefix="stripes" 	uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="lc-ui"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>
<%@ page import="com.ibm.lconn.core.web.util.UIConfigHelper"%>

<html:xhtml/>
<%-- <%@ include file="/WEB-INF/jsps/html/scenes/tools/displayContextAttributes.jsp" %> --%>

<c:set var="isCnx8UI" value="<%=UIConfigHelper.INSTANCE.isCNX8UI(request)%>" scope="request"/>

<fmt:message key="label.page.profiles.about" var="pageTitle"/>
<stripes:layout-render 
	name="/WEB-INF/jsps/html/layouts/stripes/profilesLayout.jsp" 
	pageTitle="${pageTitle }" 
	bodyClass="lotusui lotusui30dojo lotusui30_body lotusui30_fonts lotusui30 lotusAbout lotusSpritesOn" 
	frameClass="lotusFrame lotusui30_layout"
	sceneFooter="/WEB-INF/stripes/pages/app/about/aboutProfilesFooter.jsp">

    <stripes:layout-component name="main_content">
		<script type="text/javascript">
			<%@ include file="/WEB-INF/jsps/html/common/profileData.jsp" %>
			profilesData.config.pageId = "aboutProfilesView";
			dojo.addOnLoad(
				function() {
					dojo.parser.parse(dojo.byId("lotusFrame"));
			});
		</script>

		<div id="lotusColRight" class="lotusColRight">
			<div class="lotusSection lotusFirst" role="complementary" aria-labelledby="profileMoreInfoSectionHeader">
				<h2 id="profileMoreInfoSectionHeader"><fmt:message key="label.about.profiles.moreinfo" /></h2>
				<div class="lotusSectionBody">
					<div class="lotusChunk">
						<ul class="lotusList">	
							<li><html:link href="#" styleClass="lotusAction help_link"><fmt:message key="label.about.profiles.helplink" /></html:link></li>
						</ul>
					</div>
				</div>
			</div>
			<div class="lotusInfoBox" role="complementary" aria-labelledby="profileTipsSectionHeader">
				<h3 id="profileTipsSectionHeader"><span class="lotusLeft"><fmt:message key="label.about.profiles.tips" /></span></h3>
				<p><fmt:message key="label.about.profiles.tip1" /></p>
				<% 
					// Slightly hacktastic ; will look at better way later
					com.ibm.peoplepages.data.Employee e = new com.ibm.peoplepages.data.Employee();
					e.setProfileType("default");
					e.setKey(java.util.UUID.randomUUID().toString());
					
					request.setAttribute("mockPerson", e);
				 %>
				<c:if test="${profiles:aclAvailable('reportTo', 'view', mockPerson, null)}"><p><fmt:message key="label.about.profiles.tip2" /></p></c:if>
				<p><fmt:message key="label.about.profiles.tip3" /></p>
			</div><!-- end tips -->
		</div>
		<!-- content -->
		<div class="lotusContent" role="main">
			<a id="mainContent" name="mainContent"></a><!-- skip links for accessibility -->
			<!--  aboutBox -->
			<div class="lotusAboutBox">	
				<c:choose>
					<c:when test="${isCnx8UI}">
						<img class="cnx8ui-iconsComponentsWhite128 iconsComponentsWhite128 iconsComponentsWhite128-ProfilesWhite128" src="<lc-ui:blankGif />" alt=""/>
					</c:when>
					<c:otherwise>
						<img class="iconsComponentsWhite128 iconsComponentsWhite128-ProfilesWhite128" src="<lc-ui:blankGif />" alt=""/>
					</c:otherwise>
				</c:choose>
				<!-- aboutText -->
				<div class="lotusAboutText">
					<h1><fmt:message key="label.about.profiles.heading1" /></h1>
					<h3><fmt:message key="label.about.profiles.heading2" /></h3>
					<p><fmt:message key="label.about.profiles.body" /></p>
				</div>
			</div>
			<div class="lotusContentColOne">
				<h2><fmt:message key="label.about.editprofile.heading" /></h2>
				<p><fmt:message key="label.about.editprofile.body" /></p>
				<p><a href="#" class="lotusAction edit_help_link" aria-label="<fmt:message key='label.about.editprofile.heading' />"><fmt:message key="label.about.profiles.findoutmore" /></a></p>
				<h2><fmt:message key="label.about.searchprofiles.heading" /></h2>
				<p><fmt:message key="label.about.searchprofiles.body" /></p>
				<p><a href="#" class="lotusAction search_help_link" aria-label="<fmt:message key='label.about.searchprofiles.heading' />"><fmt:message key="label.about.profiles.findoutmore" /></a></p>
			</div>
			<div class="lotusContentColTwo">
				<h2><fmt:message key="label.about.profiletags.heading" /></h2>
				<p><fmt:message key="label.about.profiletags.body" /></p>
				<p><a href="#" class="lotusAction tags_help_link" aria-label="<fmt:message key='label.about.profiletags.heading' />"><fmt:message key="label.about.profiles.findoutmore" /></a></p>
				<h2><fmt:message key="label.about.relatedcontent.heading" /></h2>
				<p><fmt:message key="label.about.relatedcontent.body" /></p>
				<p><a href="#" class="lotusAction related_content_help_link" aria-label="<fmt:message key='label.about.relatedcontent.heading' />"><fmt:message key="label.about.profiles.findoutmore" /></a></p>
			</div>
		</div>
    </stripes:layout-component>
</stripes:layout-render>

