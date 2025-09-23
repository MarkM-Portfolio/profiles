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
	<!--  deprecated; not used since 2.0.1; to be completely removed after verification of non usage through 4.0
	
<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

var BusinessCard = {
	profileServiceUrl : <profiles:serviceLink serviceName="profiles">"${svcRef}"</profiles:serviceLink>,
	semanticTagServiceUrl : <profiles:serviceLink serviceName="personTag">"${svcRef}"</profiles:serviceLink>,
	request : null,
	
	<jsp:include flush="true" page="/WEB-INF/jsps/html/scenes/personTag/personTag.jsp" />
	
	init : function () {
		var head = document.getElementsByTagName('head');
		
		if (head[0]) {
			var css = document.createElement('link');
			css.rel = "stylesheet";
			css.href = BusinessCard.semanticTagServiceUrl + '/css/semanticTagStyles.css';
			css.type = "text/css";
			head[0].appendChild(css);
			
			css = document.createElement('link');
			css.rel = "stylesheet";
			css.href = BusinessCard.profileServiceUrl + '/css/personTag.css';
			css.type = "text/css";
			head[0].appendChild(css);
		}
	},

	watchEvent : function(element, name, observer, useCapture) {
		if (element.addEventListener) 
			element.addEventListener(name, observer, useCapture);
		else if (element.attachEvent) 
			element.attachEvent('on' + name, observer);
	},
	
	out: function () {
		this.buffer = "";
		this.write = function (str) {
			this.buffer += str;
		}
	},
	
	getProfileInfo : function(crossDomainRequest, callback, keyValue, searchKey) {
		var searchKeySpecified = !(searchKey == null);
		if (!searchKeySpecified) searchKey = "email"; // default search is by email
		
		// logic to detect whether key is email or not.  if not email and no search key was specified, then assume key to be userid
		if (keyValue.indexOf("@") == -1 && !searchKeySpecified) searchKey =  "userid";
		
		if (!crossDomainRequest) {
			if (window.ActiveXObject) {
				request = new ActiveXObject("Microsoft.XMLHTTP");
			}
			else if (window.XMLHttpRequest) {
				request = new XMLHttpRequest();
			}
		
			if (request) {
				// synchronous request
				request.open("GET", BusinessCard.profileServiceUrl + '/json/profile.do?' + searchKey + '=' + keyValue + '&variable=person', false);
			
				request.send(null);
			
				if (request.status == 200) {
					eval(request.responseText);
					return person;
				}
			}
		}
		else {
			var script = document.createElement("script");
			script.src = BusinessCard.profileServiceUrl + '/json/profile.do?' + searchKey + '=' + keyValue + '&callback=' + callback;
			document.getElementsByTagName('head')[0].appendChild(script);
		}
	}
}

BusinessCard.watchEvent(window, 'load', BusinessCard.init, false);
--%>