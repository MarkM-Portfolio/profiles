<%@ page contentType="application/json;charset=UTF-8" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2013                                    --%>
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
<%@ taglib prefix="tiles" 		uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>
<%@ taglib prefix="tags"		uri="/WEB-INF/tags" %>

<%
String currentProfileUidtt = request.getParameter("uid");
String currentLoggedInUserUidtt = (String) request.getAttribute("currentLoggedInUserUid");

Boolean canFriendB = (Boolean)request.getAttribute("canFriend");
boolean canFriend = canFriendB.booleanValue();

Boolean canFollowB = (Boolean)request.getAttribute("canFollow");
boolean canFollow = canFollowB.booleanValue();

Boolean canUnfollowB = (Boolean)request.getAttribute("canUnfollow");
boolean canUnfollow = canUnfollowB.booleanValue();

Boolean isFollowedB = (Boolean)request.getAttribute("isFollowed");
boolean isFollowed = isFollowedB.booleanValue();

Boolean canTagB = (Boolean)request.getAttribute("canTag");
boolean canTag = canTagB.booleanValue();
%>
({
	canFriend: <c:out value="${canFriend}" />,
	canFollow: <c:out value="${canFollow}" />,
	isFollowed: <c:out value="${isFollowed}" />,
	canTag: <c:out value="${canTag}" />
})
