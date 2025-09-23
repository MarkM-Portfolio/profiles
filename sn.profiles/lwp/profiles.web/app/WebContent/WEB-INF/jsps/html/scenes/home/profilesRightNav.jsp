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
	@author badebiyi
--%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml/>

<div id="profilePaneRight" class="lotusColRight">
	<span id="widget-container-col3" class="widgetContainer"></span>
	<div class="lotusSection lotusFirst"> 
		<h2> <fmt:message key="label.about.profiles.moreinfo" /> </h2>
		<div class="lotusChunk">
			<ul class="lotusList">	
				<li> <html:link href="#" styleClass="help_link"> <fmt:message key="label.about.profiles.helplink" /> </html:link></li>
				<li> <html:link href="#" styleClass="demo_link"> <fmt:message key="label.about.profiles.demolink" /> </html:link></li>			
			</ul>
		</div>
	</div>
	<div class="lotusInfoBox">
		<h3> <span class="lotusLeft"> <fmt:message key="label.about.profiles.tips" /> </span></h3>
		<p> <fmt:message key="label.about.profiles.tip1" /> </p>
		<c:if test="${applicationScope['profilesConfig'].dataAccessConfig.orgStructureEnabled}"><p> <fmt:message key="label.about.profiles.tip2" /> </p></c:if>
		<p> <fmt:message key="label.about.profiles.tip3" /> </p>
	</div>
</div>
