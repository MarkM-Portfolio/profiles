<%@ page contentType="text/x-vCard;charset=ISO-8859-1" session="false"%>

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

<%--
 @author sberajaw
--%>

<%--
  ahernm@us.ibm.com 
  Intentionally broke non-western vcards.  Switched to ISO encoding as 
  windows/notes will not handle UTF-8 encoding correctly -
  --%>

<%@ taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 		uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 			uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>

BEGIN:VCARD
VERSION:2.1
FN:${profile.displayName}
N:${profile.surname};${profile.givenName}
PHOTO;VALUE=URL:${profilesSvcLocation}/photo.do?key=${profile.key}&lastMod=<profiles:outputLMTime time='${profile.lastUpdate}'/>
ADR;WORK:;;${profile.workLocation.address1},${profile.workLocation.address2};${profile.workLocation.city};${profile.workLocation.state};${profile.workLocation.postalCode};${profile.countryDisplayValue}
TEL;WORK:${profile.telephoneNumber}
EMAIL;INTERNET:${profile.email}
TZ:${profile.timezone}
TITLE:${profile.jobResp}
ORG:${profile.organizationTitle}
URL:${profilesSvcLocation}/html/profileView.do?uid=${profile.uid}
UID:${profile.uid}
SOUND;VALUE=URL:${profilesSvcLocation}/audio.do?uid=${profile.uid}&lastMod=<profiles:outputLMTime time='${profile.lastUpdate}'/>
END:VCARD
