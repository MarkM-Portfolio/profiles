<!DOCTYPE <%=request.getParameter("doctype") %>>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2013, 2016                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%


	/* locale settings */
	String locale = request.getHeader("Accept-Language") + ",";
	locale = (locale.substring(0, locale.indexOf(","))).replace("-", "_");
	request.setAttribute("locale", locale);
	
	if (locale.indexOf("ar") == 0 || locale.indexOf("he") == 0 || locale.indexOf("iw") == 0) {
		request.setAttribute("rtl", "true");
		request.setAttribute("bodytagsuffix", " dir='rtl'");
	} else {
		request.setAttribute("rtl", "false");
		request.setAttribute("bodytagsuffix", "");
	}

	/* debug settings */
	if (request.getParameter("debug").equals("true")) {
		request.setAttribute("dojojssuffix", ".js.uncompressed");
		request.setAttribute("connjssuffix", "&debug=true");
	} else {
		request.setAttribute("dojojssuffix", "");
		request.setAttribute("connjssuffix", "");
	}
	
	/* loadcss settings */
	if (request.getParameter("loadcss").equals("false")) {
		request.setAttribute("connjssuffix", request.getAttribute("connjssuffix") + "&loadCssFiles=false");
	}

	/* cardbehavior settings */
	if (request.getParameter("loadbehavior").equals("true")) {
		request.setAttribute("connjssuffix", request.getAttribute("connjssuffix") + "&cardBehavior=" + request.getParameter("cardBehavior"));
	}
	
	if (request.getParameter("preloaddojo").equals("true")) {
		request.setAttribute("connjssuffix", request.getAttribute("connjssuffix") + "&inclDojo=false");
	} else
	if (request.getParameter("preloaddojo").equals("false")) {
		request.setAttribute("connjssuffix", request.getAttribute("connjssuffix") + "&inclDojo=true");
	}	
	
	/* xd settings - add .xd to suffix if older version of dojo */
	if ((request.getParameter("preloaddojo").equals("auto") || request.getParameter("preloaddojo").equals("true")) && request.getParameter("xddojo").equals("true") && !request.getParameter("dojoversion").equals("")) {
		String sVer = request.getParameter("dojoversion");
		sVer = sVer.substring(0, sVer.lastIndexOf("."));
		double dVer = new Double(sVer).doubleValue();
		
		if (dVer < 1.7) {
			request.setAttribute("dojojssuffix", ".xd" + request.getAttribute("dojojssuffix"));
		}
	}
	
%>
<html>
	<head>
		<title>Business Card Test Results</title>
		
		<% 
			if (!request.getParameter("loadcss").equals("true") && !request.getParameter("cssfiles").equals("")) {
				String sFiles = request.getParameter("cssfiles") + ",";
				while (sFiles.indexOf(",") > -1) {
					String sFile = sFiles.substring(0, sFiles.indexOf(","));
		%>
					<link rel="stylesheet" href="<%= sFile %>" type="text/css" />
		<%		
					
					sFiles = sFiles.substring(sFiles.indexOf(",")+1);
				}
			}
		%>
		
		<%-- DOJO LOADER --%>
		<% if (request.getParameter("debug").equals("true")) { %>
			<script type="text/javascript">
				window.djConfig = {
					debug: true
				};
			</script>
		<% } %>
		
		<%-- AJAX PROXY LOADER --%>
		<% if (request.getParameter("useAjaxProxy").equals("true")) { %>
			<script type="text/javascript">
				window.SemTagSvcConfig = {
					proxyURL: "./connectionsProxy.jsp?connserver=<%= request.getParameter("connserver") %>&url="
				};
			</script>
		<% } %>		
		
		
		<% if (request.getParameter("addservice").equals("true")) { %>
			<script type="text/javascript">

				setTimeout( function() {
					SemTagPerson.services.push(
						{
							name: 'googleService',
							url_pattern: '/search?hl=en&q={displayName}&btnG=Google+Search',
							js_eval: '"Google Me"',
							location: 'http://www.google.com'
						}
					);

					SemTagPerson.services.push(
						{
							name: 'googleService',
							url_pattern: '/search?hl=en&q={displayName}&btnG=Google+Search',
							label: 'Label me!',
							location: 'http://www.google.com'
						}
					);
				}, 1000); //allow time for js code to be downloaded
			</script>
		<% } %>
		
		<% if ((request.getParameter("preloaddojo").equals("auto") || request.getParameter("preloaddojo").equals("true")) && !request.getParameter("dojoversion").equals("")) { 
			if (request.getParameter("xddojo").equals("true")) {%>
				<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/dojo/<%= request.getParameter("dojoversion") %>/dojo/dojo<%= request.getAttribute("dojojssuffix") %>.js"></script>
		<% 	} else { %>
				<script type="text/javascript" src="dojo-release-<%= request.getParameter("dojoversion") %>/dojo/dojo<%= request.getAttribute("dojojssuffix") %>.js"></script>
		<% 	} %>
		<% } %>
		
		
		<%-- CONNECTIONS CODE LOADER --%>
		<script type="text/javascript" src="<%= request.getParameter("connserver") %>/profiles/ibm_semanticTagServlet/javascript/semanticTagService.js?lang=<%= request.getAttribute("locale") %><%= request.getAttribute("connjssuffix") %>"></script>

		
		<style>
			.oneui ul.idxPersonCardServices {
				list-style-type: none;
				padding: 0px;
				margin: 0px;
			}			
			.oneui ul.idxPersonCardServices li {
				float: left;
				text-align: center;
				width: 100px;
				padding: 0 5px;
				border-right: 1px solid #AAA;
			}
			.oneui .idxPersonCardContainer {
				clear: both;
			}
			.oneui .idxPersonCardContainer h2 {
				overflow: hidden;
				margin-top: 1em;
			}
			.oneui .idxPersonCardContainer p {
				margin-bottom: inherit;
				margin-top: inherit;
			}
			.oneui .idxPersonCard {
				width: 450px;
			}
		</style>
		
	</head>
	<body<%= request.getAttribute("bodytagsuffix") %>>
		<h1>Business Card Test Results</h1>

		<h2>Load Parameters:</h2>
			<ul>
				<li>doctype: <%= request.getParameter("doctype") %></li>
				<li>locale: <%= request.getAttribute("locale") %></li>
				<li>rtl: <%= request.getAttribute("rtl") %></li>
				<li>connections server: <%= request.getParameter("connserver") %></li>
				<li>preload dojo: <%= request.getParameter("preloaddojo") %></li>
				<% if ((request.getParameter("preloaddojo").equals("auto") || request.getParameter("preloaddojo").equals("true"))) { %>
					<ul>
						<li>dojo version: <%= request.getParameter("dojoversion") %></li>
						<li>cross-domain dojo: <%= request.getParameter("xddojo") %></li>
					</ul>
				<% } %>
				<li>add service links: <%= request.getParameter("addservice") %></li>
				<li>load connections css: <%= request.getParameter("loadcss") %></li>
				<% if (!request.getParameter("loadcss").equals("true") && !request.getParameter("cssfiles").equals("")) { %>
					<ul>
						<li>custom css files: <%= request.getParameter("cssfiles") %></li>
					</ul>
				<% } %>
				<li>predefined tags: <%= request.getParameter("predefinedtags") %></li>
				<li>debug: <%= request.getParameter("debug") %></li>
				<li>use ajax proxy: <%= request.getParameter("useAjaxProxy") %></li>
				<% if (request.getParameter("loadbehavior").equals("true") && !request.getParameter("cardBehavior").equals("")) { %>
					<li>cardbehavior: <%= request.getParameter("cardBehavior") %></li>
				<% } %>
				
				
			</ul>
			<br/>
			<a href="selector.html">Back to selector page</a>
			<script type="text/javascript">
				var loc = location.href.replace("//","__") + "/";
				loc = loc.substring(0, loc.indexOf("/"));
				loc = loc.replace("__","//");
				if (loc == "<%= request.getParameter("connserver") %>") {
					document.write("<span style='color: red; weight: bold;'>*TEST IS INVALID*  Current location and Connections server location must be different domains!</span><br/>");
				}
			</script>
			
			
		<hr size="10"/>
		<h2>Business Cards:</h2>
		<%= request.getParameter("embedhtml") %>		
		
		<div id="bizcardresults"><%= request.getParameter("semtags") %></div>
		<div id="bizcardmoreresults"></div>

		<hr/><br/><br/>
		
		<h2>More Actions:</h2>
		<ul>
			<script type="text/javascript">
				window.addbizcardconnect = function() {
					dojo.connect(lconn.profiles.bizCard.bizCard, "update", function() {
						dojo.query(".title", dojo.byId("semtagmenu")).forEach(function(node) {
							var newnode = dojo.create("p");
							newnode.innerHTML = new Date().toLocaleString();
							dojo.place(newnode, node, "before");
						});
					});
					if (window.console) console.log("Any popup bizcard will now have the current date under the name and title");
				}
			</script>
			<li><a href="javascript: window.addbizcardconnect();">Add date to bizcard by connecting to update function</a></li>
			

			<script type="text/javascript">
				window.addmoretags = function() {
					dojo.byId("bizcardmoreresults").innerHTML = '<%= request.getParameter("semtags").replace("'","\\'").replace("\n"," ").replace("\r"," ") %>';
					
					setTimeout(function() {SemTagSvc.parseDom();}, 1000);
				}
				
				
			</script>
			
			<li><a href="javascript: window.addmoretags();">Add more tags to parse and parse them</a></li>

		</ul>
		
		
		<br/><br/><hr size="1"/>
		
		<div id="newtags"></div>
		
		<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
	</body>
</html>
