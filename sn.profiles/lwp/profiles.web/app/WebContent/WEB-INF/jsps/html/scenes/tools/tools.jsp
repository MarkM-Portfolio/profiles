<%@ page contentType="text/html;charset=UTF-8" %>

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

<!-- content -->
<div class="lotusContent" class="contentBlockHack" role="main">
<a id="mainContent" name="mainContent"></a><!-- skip links for accessibility -->
	<h1> <fmt:message key="label.tools.stplugin.heading" /> </h1>
	
	<h3 id="about_plugin"> <fmt:message key="label.tools.stplugin.about.heading" /> </h3>
	<p>
		<fmt:message key="label.tools.stplugin.about.body1" />
	</p>
	<p>
		<fmt:message key="label.tools.stplugin.about.body2" />
	</p>
	
	<h3 id="install_plugin"> <fmt:message key="label.tools.stplugin.install.heading" /> </h3>
	<p>
		<fmt:message key="label.tools.stplugin.install.body1" />
	</p>
	<html:img page="/images/profilesPluginInstallButton.gif" altKey="label.tools.stplugin.install.altText" />
	<p>
		<fmt:message key="label.tools.stplugin.install.body2">
			<fmt:param>
				<html:link href="${profilesSvcLocation}/plugins/">${profilesSvcLocation}/plugins/</html:link>
			</fmt:param>
		</fmt:message>
	</p>
	<p>
		<fmt:message key="label.tools.stplugin.install.body3">
			<fmt:param>
				<html:link href="${profilesSvcLocation}/plugins/">${profilesSvcLocation}/plugins/</html:link>
			</fmt:param>
		</fmt:message>
	</p>
</div> <!-- end content -->
