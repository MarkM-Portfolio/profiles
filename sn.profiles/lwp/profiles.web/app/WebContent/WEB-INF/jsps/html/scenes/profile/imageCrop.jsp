<%@ page contentType="text/html;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2008, 2015                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%--
	@author ajfriedl@us.ibm.com
--%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 		uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<html:xhtml />

<?xml version="1.0"?>
<jsp:include page="/WEB-INF/jsps/html/common/htmlStartElement.jsp"/>
	<head>
		<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
		<title>Test page for photo cropper.	</title>
	</head>
	<body>
		<div id="imgPreview" style="width: 300px; height: 300px; position: relative;
			border: 1px black solid; background:url(tempPhoto.do?lang=en&resize=true&xsize=300&ysize=300)">
			<!-- <img src="tempPhoto.do" style="z-index:1"></img>  -->
			<div id="box" style="width: 100px; height: 100px;
					 border: 3px black solid; z-index:2">
			</div>
		</div>
		<button onClick="doCrop()">Crop</button>
		<img id="cropPreview"></img>
		<div>
			<textarea rows="10" cols="80" id="dbgText"></textarea>
			<script defer="defer" type="text/javascript">
			<!--
				boxParent = document.getElementById("imgPreview");
				setDbgText(document.getElementById("dbgText"));
				photoCrop(document.getElementById("box"));
				box.style.left = 0;
				box.style.top = 0;
				box.style.width = 100;
				box.style.height = 100;
	
				function doCrop() {
					var coords = getRelativeCoords();
					url = "tempPhoto.do?crop=true&forceRefresh=" + <%=System.currentTimeMillis()%>+ 
						"&startx=" + coords.startX + "&endx=" + coords.endX +
						"&starty=" + coords.startY + "&endy=" + coords.endY;
					document.getElementById("cropPreview").src = url;
				}
				//!-->
			</script>
		</div>
	</body>
</html>
