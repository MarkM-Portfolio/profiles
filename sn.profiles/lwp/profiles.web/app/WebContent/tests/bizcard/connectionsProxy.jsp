<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2016                                          --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>


<%@page session="false"%>
<%@page import="java.net.*,java.io.*,java.util.*" %>

<%
try {
	
	/* call to this jsp is "connectionsProxy.jsp?url=" */
	String sUrl = request.getQueryString();
	sUrl = sUrl.substring(sUrl.indexOf("url=/")+6);
	sUrl = sUrl.substring(sUrl.indexOf("/")+1).replace("%3A", ":");


	URL url = new URL("http://" + sUrl);
	HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	conn.setDoOutput(true);
	conn.setRequestMethod(request.getMethod());

	/*enumerate and set all of the headers*/
	
	Enumeration<String> headerNames = request.getHeaderNames();
	while (headerNames.hasMoreElements()) {
		String headerName = headerNames.nextElement();
		if (!"accept-encoding".equalsIgnoreCase(headerName)) {
			Enumeration<String> headers = request.getHeaders(headerName);
			while (headers.hasMoreElements()) {
				String headerValue = headers.nextElement();
				conn.addRequestProperty(headerName, headerValue);
			}
		}
	}
	
	int len = request.getContentLength();
	if (len > 0) {
		conn.setDoInput(true);
		byte[] bData = new byte[len];
		request.getInputStream().read(bData, 0, len);
		conn.getOutputStream().write(bData, 0, len);
	}
	response.setContentType(conn.getContentType());
 
	BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	String line;
	while ((line = reader.readLine()) != null) {
		out.println(line); 
	}
	reader.close();
 
} catch(Exception e) {
	response.setStatus(500);
}
%>
