<%@ page contentType="application/javascript;charset=UTF-8" session="false" %>

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

<%--
	<!--  deprecated; not used since 2.0.1; to be completely removed after verification of non usage through 4.0

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<c:if test="${param['lang'] != null}">
	<fmt:setLocale value="${param['lang']}" />
</c:if>

<fmt:setBundle basename="com.ibm.lconn.profiles.strings.uilabels" var="attributeLabels" scope="page" />

// NOTE: this code is an extraction of javascript/personTagUI.js 
// this mechanism and code needs to be revised/replaced for LC2.5 

getMenuData: function(person, bidi, menuItems, selector, header, footer) {

	var isEmailEnabled = ( person.email != null && person.email.internet != null );
	var email =  isEmailEnabled ? person.email.internet : "";
	var userid=  person.X_lconn_userid;

	header.write("<div id='container'>");
  	header.write("<div id='levelOne'>");
  	header.write("<img id='photo' src='" + person.photo + "' alt='" + person.fn + "' title='" + person.fn + "' height='95' width='95'/>");
	header.write("<h2 id='fn'>" + person.fn + "</h2>");
	
	if (person.X_inDirectory != "true") {
		header.write("<h3 id='noProfileMsg'><fmt:message key="label.personcard.noprofilemsg" /></h3>");
	}
	
	else {
		header.write("<h3 id='title'>" + person.title + "</h3>");
		
		header.write("<h3 id='adr'>");
		if (person.adr.work.locality != "")
			header.write(person.adr.work.locality + ", ");
		if (person.adr.work.region != "") 
			header.write(person.adr.work.region + " "); 
		if (person.adr.work.country_name != "")
			header.write(person.adr.work.country_name);
		header.write("</h3>");
	
		header.write("<h3 id='xworkLoc'>");
		if (person.X_building_name != "")
			header.write("<core:escapeJavaScript><fmt:message bundle='${attributeLabels}' key='label.contactInformation.bldgId' /></core:escapeJavaScript>" + person.X_building_name + " | "); 
		if (person.X_building_floor != "")
			header.write("<core:escapeJavaScript><fmt:message bundle='${attributeLabels}' key='label.contactInformation.floor' /></core:escapeJavaScript>" + person.X_building_floor + " | ");
		if (person.X_office != "") 
			header.write("<core:escapeJavaScript><fmt:message bundle='${attributeLabels}' key='label.contactInformation.officeName' /></core:escapeJavaScript> " + person.X_office);
		header.write("</h3>");
	
		header.write("<h3 id='telWork'>" + person.tel.work + "</h3>");
		if( isEmailEnabled )
			header.write("<a id='emailInternet' href='mailto:" + email + "' style=''>" + email + "</a>");
	}
		
	header.write("</div>");
		
	header.write("<div id='levelTwo'>");
	header.write("<ul class='actions inlinelist' id='applicationLinks'>");
	
	<core:serviceLink serviceName="profiles" var="profilesSvcLocation"/>
	if ("${profilesSvcLocation}" != "") {
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.profilelink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.profilelink" /></core:escapeJavaScript>";
	
		header.write("<li class='first' style='padding-right: 5px;'>");
		header.write(" <a href='${profilesSvcLocation}/html/simpleSearch.do?searchFor="+userid+"&searchBy=userid' id='profileLink' title='" + title + "'>" + linkText + "</a> ");
		header.write("</li>");
	}
	
	<core:serviceLink serviceName="communities" var="communitiesSvcLocation"/>
	if ("${communitiesSvcLocation}" != "") {
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.communitieslink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.communitieslink" /></core:escapeJavaScript>";
		
		header.write("<li style='padding-right: 5px;'>");
		header.write(" <a href='${communitiesSvcLocation}/service/html/allcommunities?userid="+userid+"' id='communitiesLink' title='" + title + "'>" + linkText + "</a> ");
		header.write("</li>");
	}
	
	<core:serviceLink serviceName="blogs" var="blogsSvcLocation"/>
	if ("${blogsSvcLocation}" != "") {
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.blogslink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.blogslink" /></core:escapeJavaScript>";
		
		header.write("<li style='padding-right: 5px;'>");
		if (typeof(person.X_blogUrl) != "undefined" && person.X_blogUrl != "")
			header.write(" <a href='" + person.X_blogUrl + "' id='blogsLink' title='" + title + "'>Blogs</a>");
		else
			header.write(" <a href='${blogsSvcLocation}/roller-ui/blog/"+userid+"' id='blogsLink' title='" + title + "'>" + linkText + "</a> ");
		header.write("</li>");
	}
	
	<core:serviceLink serviceName="dogear" var="dogearSvcLocation"/>
	if ("${dogearSvcLocation}" != "") {
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.dogearlink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.dogearlink" /></core:escapeJavaScript>";
	
		header.write("<li style='padding-right: 5px;'>");
		header.write(" <a href='${dogearSvcLocation}?userid="+userid+"' id='dogearLink' title='" + title + "'>" + linkText + "</a> ");
		header.write("</li>");
	}
	
	<core:serviceLink serviceName="activities" var="activitiesSvcLocation"/>
	if ("${activitiesSvcLocation}" != "") {	
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.activitieslink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.activitieslink" /></core:escapeJavaScript>";
		
		header.write("<li style='padding-right: 5px;'>");
		header.write(" <a href='${activitiesSvcLocation}/service/html/activities/overview?start=1&count=20&userid="+userid+"' id='activitiesLink' title='" + title + "'>" + linkText + "</a> ");
		header.write("</li>");
	}
	
	<core:serviceLink serviceName="wikis" var="wikisSvcLocation"/>
	if ("${wikisSvcLocation}" != "") {	
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.wikislink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.wikislink" /></core:escapeJavaScript>";
		
		header.write("<li style='padding-right: 5px;'>");
		header.write(" <a href='${wikisSvcLocation}/home/search?uid="+userid+"&name="+person.fn+"' id='wikisLink' title='" + title + "'>" + linkText + "</a> ");
		header.write("</li>");
	}
	
	<core:serviceLink serviceName="files" var="filesSvcLocation"/>
	if ("${filesSvcLocation}" != "") {	
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.fileslink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.fileslink" /></core:escapeJavaScript>";
		
		header.write("<li style='padding-right: 5px;'>");
		header.write(" <a href='${filesSvcLocation}/app/person/"+userid+"' id='filesLink' title='" + title + "'>" + linkText + "</a> ");
		header.write("</li>");
	}
	
	<core:serviceLink serviceName="forums" var="forumsSvcLocation"/>
	if ("${forumsSvcLocation}" != "") {	
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.forumlink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.forumlink" /></core:escapeJavaScript>";
		
		header.write("<li style='padding-right: 5px;'>");
		header.write(" <a href='${filesSvcLocation}/app/person/"+userid+"' id='filesLink' title='" + title + "'>" + linkText + "</a> ");
		header.write("</li>");
	}

	header.write("</ul>");
	header.write("</div>");
	header.write("</div>");
}, 

getInlineMarkup: function (person, bidi, buffer) {

	var showCollapsed = true;
	var userid=  person.X_lconn_userid;
	
	// check for cookie to determine if inline card should show expanded initially
	var cookies = document.cookie.match(/card.inline.expanded=\w*/g);
	if (cookies != null) {
		var cookie = cookies[0].split("=");
		if (cookie[1] == "true") {
			showCollapsed = false;
		}
	}

	buffer.write("<div class='container'>");
	buffer.write("<div class='inlinePersonCard'>");
	buffer.write("<a href='#' onclick=\"SemTagPerson.toggleInlineCard\('" + person.fn + "','levelTwo'\)\">");
	buffer.write("<div class='toggler left'>");
	
	<profiles:serviceLink serviceName="profiles">
		if (showCollapsed) {
			buffer.write("<img id='" + person.fn + "_inlinePersonCardToggleImg' src='${svcRef}/images/inlineNavClosed.gif'>");
		}
		else {
			buffer.write("<img id='" + person.fn + "_inlinePersonCardToggleImg' src='${svcRef}/images/inlineNavOpen.gif'>");
		}
	</profiles:serviceLink>	
	
	buffer.write("</div>");
	buffer.write("</a>");
	buffer.write("<div class='levelOne' id='" + person.fn + "_levelOne'>");
	buffer.write("<img id='" + person.fn + "_photo' class='left' src='" + person.photo + "' alt='" + person.fn + "' title='" + person.fn + "' height='51' width='51'>");
	buffer.write("<h4 class='fn'>");
	buffer.write("<div>" + person.fn + "</div>");
	buffer.write("</h4>");
	buffer.write("</div>");	
	
	if (showCollapsed) {
		buffer.write("<div class='levelTwo' id='" + person.fn + "_levelTwo' style='display:none'>");
	}
	else {
		buffer.write("<div class='levelTwo' id='" + person.fn + "_levelTwo'>");
	}
	
	buffer.write("<h3>");
	buffer.write("<span class='first' class='adr'>");
	buffer.write("<em>");
	if (person.adr.work.locality != "")
		buffer.write(person.adr.work.locality + ", ");
	if (person.adr.work.region != "") 
		buffer.write(person.adr.work.region + " ");
	if (person.adr.work.country_name != "") 
		buffer.write(person.adr.work.country_name);
	buffer.write("</em>");
	buffer.write("</span>");
	buffer.write("</h3>");
	buffer.write("<h3>");
	buffer.write("<span class='title'>" + person.title + "</span>");
	buffer.write("</h3>");
	
	buffer.write("<ul class='applicationLinks'>");			
	if (typeof(appName) == "undefined")
		appName = "";
	
	if ("${profilesSvcLocation}" != "") {
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.profilelink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.profilelink" /></core:escapeJavaScript>";
		
		if (appName == "profile")
			buffer.write("<li class='selected'><a class='profileLink' href='${profilesSvcLocation}/html/simpleSearch.do?searchFor="+userid+"&searchBy=userid' title='" + title + "'>" + linkText + "</a></li>");
		else	
			buffer.write("<li><a class='profileLink' href='${profilesSvcLocation}/html/simpleSearch.do?searchFor="+userid+"&searchBy=userid' title='" + title + "'>" + linkText + "</a></li>");
	}
	
	if ("${communitiesSvcLocation}" != "") {	
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.communitieslink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.communitieslink" /></core:escapeJavaScript>";
		
		if (appName == "communities")
			buffer.write("<li class='selected'><a class='communitiesLink' href='${communitiesSvcLocation}/service/html/allcommunities?userid="+userid+"' title='" + title + "'>" + linkText + "</a></li>");
		else
			buffer.write("<li><a class='communitiesLink' href='${communitiesSvcLocation}/service/html/allcommunities?userid="+userid+"' title='" + title + "'>" + linkText + "</a></li>");		
	}
	
	if ("${blogsSvcLocation}" != "") {
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.blogslink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.blogslink" /></core:escapeJavaScript>";
		
		if (appName == "blogs")
			buffer.write("<li class='selected'><a href='${blogsSvcLocation}/roller-ui/blog/"+userid+"' title='" + title + "'>" + linkText + "</a></li>");
		else {
			if (typeof(person.X_blogUrl) != "undefined" && person.X_blogUrl != "")
				buffer.write("<li><a class='blogsLink' href='" + person.X_blogUrl + "' title='" + title + "'>" + linkText + "</a></li>");
			else
				buffer.write("<li><a class='blogsLink' href='${blogsSvcLocation}/roller-ui/blog/"+userid+"' title='" + title + "'>" + linkText + "</a></li>");
		}
	}
	
	if ("${dogearSvcLocation}" != "") {	
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.dogearlink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.dogearlink" /></core:escapeJavaScript>";
		
		if (appName == "bookmarks")
			buffer.write("<li class='selected'><a class='dogearLink' href='${bookmarksSvcLocation}?userid="+userid+"' title='" + title + "'>" + linkText + "</a></li>");
		else
			buffer.write("<li><a class='dogearLink' href='${dogearSvcLocation}?userid="+userid+"' title='" + title + "'>" + linkText + "</a></li>");
	}
	
	if ("${activitiesSvcLocation}" != "") {	
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.activitieslink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.activitieslink" /></core:escapeJavaScript>";
		
		if (appName == "activities")
			buffer.write("<li class='selected'><a class='activitiesLink' href='${activitiesSvcLocation}/service/html3/activities/overview?tag=&userid="+userid+"' title='" + title + "'>" + linkText + "</a></li>");
		else
			buffer.write("<li><a class='activitiesLink' href='${activitiesSvcLocation}/service/html/activities/overview?start=1&count=20&userid="+userid+"' title='" + title + "'>" + linkText + "</a></li>");
	}
	
	//TODO
	if ("${wikisSvcLocation}" != "") {	
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.wikislink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.wikislink" /></core:escapeJavaScript>";
		
		if (appName == "wikis")
			buffer.write("<li class='selected'><a class='wikisLink' href='${wikisSvcLocation}/home/search?uid="+userid+"&name="+person.fn+"' title='" + title + "'>" + linkText + "</a></li>");
		else
			buffer.write("<li><a class='wikisLink' href='${wikisSvcLocation}/home/search?uid="+userid+"' title='" + title + "'>" + linkText + "</a></li>");
	}
	
	if ("${filesSvcLocation}" != "") {	
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.fileslink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.fileslink" /></core:escapeJavaScript>";
		
		if (appName == "files")
			buffer.write("<li class='selected'><a class='filesLink' href='${filesSvcLocation}/app/person/"+userid+"' title='" + title + "'>" + linkText + "</a></li>");
		else
			buffer.write("<li><a class='filesLink' href='${filesSvcLocation}/app/person/"+userid+"' title='" + title + "'>" + linkText + "</a></li>");
	}
		
	if ("${forumsSvcLocation}" != "") {	
		var title = "<core:escapeJavaScript><fmt:message key="label.personcard.forumlink.title" /></core:escapeJavaScript>";
		title = title.replace(/'{1}/g, "&#146;");
		var linkText = "<core:escapeJavaScript><fmt:message key="label.personcard.forumlink" /></core:escapeJavaScript>";
		
		if (appName == "forums")
			buffer.write("<li class='selected'><a class='forumsLink' href='${forumsSvcLocation}/app/person/"+userid+"' title='" + title + "'>" + linkText + "</a></li>");
		else
			buffer.write("<li><a class='forumsLink' href='${forumsSvcLocation}/app/person/"+userid+"' title='" + title + "'>" + linkText + "</a></li>");
	}
		
	buffer.write("</ul>");
	buffer.write("</div>");
	buffer.write("<div class='inlinePersonCardBottom'>");
	buffer.write("&nbsp;");
	buffer.write("</div>");
	buffer.write("</div>");
	buffer.write("</div>");
},

toggleInlineCard: function (idPrefix, elementId) {
	var date = new Date();
	date.setTime(date.getTime() + (365*24*60*60/0.001));
	var expires = '; expires=' + date.toGMTString();
	
	var element = document.getElementById(idPrefix + '_' + elementId);
	var img = document.getElementById(idPrefix + '_inlinePersonCardToggleImg');
	if ( element.style.display != 'none' ) {
		element.style.display = 'none';
		<profiles:serviceLink serviceName="profiles">
			img.src = '${svcRef}/images/inlineNavClosed.gif';
		</profiles:serviceLink>
		
		document.cookie = 'card.inline.expanded=false' + expires + '; path=/';
	}
	else {
		element.style.display = '';
		<profiles:serviceLink serviceName="profiles">
			img.src = '${svcRef}/images/inlineNavOpen.gif';
		</profiles:serviceLink>
		
		document.cookie = 'card.inline.expanded=true' + expires + '; path=/';
	}
},

--%>