<%@ page contentType="text/html; charset=UTF-8"
	pageEncoding="ISO-8859-1" session="false"%>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2010                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" 	uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" 		uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="html"	uri="http://jakarta.apache.org/struts/tags-html"%>
<%@ taglib prefix="tiles"	uri="http://jakarta.apache.org/struts/tags-tiles"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Profiles API Help</title>
</head>
<body>
<h1>Profiles API for /atom</h1>
<p>The API only supports data retrieval (no create, update, delete)</p>
<p>There are two types of Profiles Atom API calls. <b>Retrieval</b> calls
are used to lookup and retrieve data from the Profiles data store. <b>Search</b>
calls are used to search the Profiles data store on a variety of
parameters.</p>
<h2>API Retrieval Actions</h2>
<p>The following calls can be used to retrieve data from the Profiles
service</p>
<b>Profile retrieval</b>
<br />
The first set allows you to retrieve a profile by uid, guid, or email.
Specifying the format parameter allows you to choose between a <b>lite</b> profile (meaning a subset of the profile record will be returned)
and a <b>full</b> profile (meaning all available profile data is returned).
By default the format of the returned profile is <b>lite</b>. 
<ul>
	<li>profile.do?uid=&lt;uid&gt;&format=full</li>
	<li>profile.do?uid=&lt;uid&gt;&format=lite</li>
	<li>profile.do?email=&lt;email&gt;&format=full</li>
	<li>profile.do?email=&lt;email&gt;&format=lite</li>
	<li>profile.do?guid=&lt;guid&gt;&format=full</li>
	<li>profile.do?guid=&lt;guid&gt;&format=lite</li>
</ul>
<b>Related profile data retrieval</b>
<br />
The second set allows you to retrieve profile related data by uid or email.
<ul>
	<li>peopleManaged.do?uid=&lt;uid&gt;</li>
	<li>peopleManaged.do?email=&lt;email&gt;</li>
	<li>reportingChain.do?uid=&lt;uid&gt;</li>
	<li>reportingChain.do?email=&lt;email&gt;</li>
</ul>
<b>Code lookups</b>
<br />
The third set allows you to resolve various codes used and defined in
the Profiles data store.  Note:  only one code can be looked up at a time.
<ul>
	<li>code.do?workLoc=&lt;workLocationCode&gt;</li>
	<li>code.do?orgCode=&lt;organizationCode&gt;</li>
	<li>code.do?employeeType=&lt;employeeType&gt;</li>
	<li>code.do?countryCode=&lt;countryCode&gt;</li>
</ul>
<b>Retrieval Examples</b>
<ul>
	<li><a href="${profilesSvcLocation}/atom/profile.do?email=jdoe&format=full">${profilesSvcLocation}/atom/profile.do?email=jdoe@us.ibm.com&format=full</a></li>
	<li><a href="${profilesSvcLocation}/atom/profile.do?email=jdoe&format=lite">${profilesSvcLocation}/atom/profile.do?email=jdoe@us.ibm.com&format=lite</a></li>
	<li><a href="${profilesSvcLocation}/atom/profile.do?uid=jdoe&format=full">${profilesSvcLocation}/atom/profile.do?uid=jdoe&format=full</a></li>
	<li><a href="${profilesSvcLocation}/atom/profile.do?uid=jdoe">${profilesSvcLocation}/atom/profile.do?uid=jdoe&format=lite</li>
	<li><a href="${profilesSvcLocation}/atom/profile.do?guid=123456123456123456123456123456123456&format=full">${profilesSvcLocation}/atom/profile.do?guid=123456123456123456123456123456123456&format=full</a></li>
	<li><a href="${profilesSvcLocation}/atom/profile.do?guid=123456123456123456123456123456123456&format=lite">${profilesSvcLocation}/atom/profile.do?guid=123456123456123456123456123456123456&format=lite</li>
	<li><a href="${profilesSvcLocation}/atom/peopleManaged.do?uid=jdoe">${profilesSvcLocation}/atom/peopleManaged.do?uid=jdoe</a></li>
	<li><a href="${profilesSvcLocation}/atom/peopleManaged.do?email=jdoe@us.ibm.com">${profilesSvcLocation}/atom/peopleManaged.do?email=jdoe@us.ibm.com</a></li>
	<li><a href="${profilesSvcLocation}/atom/reportingChain.do?uid=jdoe">${profilesSvcLocation}/atom/reportingChain.do?uid=jdoe</a></li>
	<li><a href="${profilesSvcLocation}/atom/reportingChain.do?email=jdoe@us.ibm.com">${profilesSvcLocation}/atom/reportingChain.do?email=jdoe@us.ibm.com</a></li>
	<li><a href="${profilesSvcLocation}/atom/code.do?workLoc=someCode">${profilesSvcLocation}/atom/code.do?workLoc=someCode</a></li>
	<li><a href="${profilesSvcLocation}/atom/code.do?orgCode=someCode">${profilesSvcLocation}/atom/code.do?orgCode=someCode</a></li>
	<li><a href="${profilesSvcLocation}/atom/code.do?employeeType=someCode">${profilesSvcLocation}/atom/code.do?employeeType=someCode</a></li>
	<li><a href="${profilesSvcLocation}/atom/code.do?countryCode=us">${profilesSvcLocation}/atom/code.do?countryCode=us</a></li>
</ul>
<h2>API Search Parameters</h2>
<p>The following can be used individually in simple searches or combined
to perform more advanced searches.</p>
<ul>
	<li>search.do?organization=&lt;value&gt;</li>
	<li>search.do?email=&lt;value&gt;</li>
	<li>search.do?gwemail=&lt;value&gt;</li>
	<li>search.do?name=&lt;value&gt;</li>
	<li>search.do?jobTitle=&lt;value&gt;</li>
	<li>search.do?city=&lt;value&gt;</li>
	<li>search.do?state=&lt;value&gt;</li>
	<li>search.do?country=&lt;value&gt;</li>
	<li>search.do?name=&lt;value&gt;</li>
	<li>search.do?phoneNumber=&lt;value&gt;</li>
	<li>search.do?profileTags=&lt;value&gt;</li>
</ul>
<p><b>keyword</b> can be used to search across Profile Tags, About Me,
and Background</p>
<ul>
	<li>search.do?search=&lt;value&gt;</li>
</ul>
<p>All searches support the use of wildcards: % or *</p>
<b>Search Examples</b>
<ul>
	<li><a href="${profilesSvcLocation}/atom/search.do?jobTitle=sales">${profilesSvcLocation}/atom/search.do?jobTitle=sales</a></li>
	<li><a href="${profilesSvcLocation}/atom/search.do?city=portsmouth&state=nh">${profilesSvcLocation}/atom/search.do?city=portsmouth&state=nh</a></li>
	<li><a href="${profilesSvcLocation}/atom/search.do?gwemail=John+Doe/Westford/IBM">${profilesSvcLocation}/atom/search.do?gwemail=John+Doe/Westford/IBM</a></li>
	<li><a href="${profilesSvcLocation}/atom/search.do?name=doe">${profilesSvcLocation}/atom/search.do?name=doe</a></li>
	<li><a href="${profilesSvcLocation}/atom/search.do?phoneNumber=1-978-555-7213">${profilesSvcLocation}/atom/search.do?phoneNumber=1-978-555-7213</a></li>
	<li><a href="${profilesSvcLocation}/atom/search.do?profileTags=lotus">${profilesSvcLocation}/atom/search.do?profileTags=lotus</a></li>
	<li><a href="${profilesSvcLocation}/atom/search.do?jobTitle=sales&amp;city=westford">${profilesSvcLocation}/atom/search.do?jobTitle=workplace&amp;city=westford</a></li>
	<li><a href="${profilesSvcLocation}/atom/search.do?search=lotus">${profilesSvcLocation}/atom/search.do?keyword=karaoke</a></li>
</ul>
<h2>Paging</h2>
<p>Use <b>page</b> (page number) and <b>ps</b> (page size)</p>
<p>If <b>page</b> and <b>ps</b> are not specified, page 1 with the configured page size will be returned</p>
<ul>
	<li><a href="${profilesSvcLocation}/atom/search.do?location=portsmouth,nh&page=2&ps=10">${profilesSvcLocation}/atom/api.do?location=portsmouth,nh&page=2&ps=10</a></li>
</ul>
</body>
</html>
