<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<head>
<title>Profiles Bridge Test</title>
</head>
<body>

<h1><font color="blue">Profiles Bridge Test</font></h1>
<h2><u>1. User lifecycle</u></h2></div>

<b>Please input user information (User ID and Display Name) to sync:</b>

<br></br>

<form id="syncuser" action="test.jsp">
	<b>User ID:</b>
	<br></br>
	<input type="text" style="height:50px;font-size:14pt;" size="70" name="sync_userid" value="123456" id="sync_userid">
	<br></br>

	<b>Display Name</b>
	<br></br>
	<input type="text" style="height:50px;font-size:14pt;" size="70" name="sync_username" value="Amy Jones" id="sync_username ">
	<input type="hidden" name="action" value="sync" id="action">

	<p>
		<input type="submit" value="Sync this user">
	</p>    
</form>

<h2><u>2. User Following/Un-Following</u></h2></div>

<b>Please input user information (User ID) to follow:</b>

<br></br>

<form id="followuser" action="test.jsp">
	<b>Source user ID:</b>
	<input type="text" style="height:50px;font-size:14pt;" size="70" name="source_userid" value="follow_source_123456" id="source_userid">
	<br></br>
	
	<b>Target user ID:</b>
	<input type="text" style="height:50px;font-size:14pt;" size="70" name="target_userid" value="follow_target_123456" id="target_userid">
	<input type="hidden" name="action" value="follow" id="action">

	<p>
		<input type="submit" value="Follow this user">
	</p>    
</form>


<b>Please input user information (User ID) to un-follow:</b>

<br></br>

<form id="unfollowuser" action="test.jsp">
	<b>Source user ID:</b>
	<input type="text" style="height:50px;font-size:14pt;" size="70" name="source_userid" value="unfollow_source_123456" id="source_userid">
	<br></br>
	
	<b>Target user ID:</b>
	<input type="text" style="height:50px;font-size:14pt;" size="70" name="target_userid" value="unfollow_target_123456" id="target_userid">
	<input type="hidden" name="action" value="unfollow" id="action">

	<p>
		<input type="submit" value="Un-follow this user">
	</p>    
</form>

<%@ page import="com.ibm.lconn.following.internal.helpers.ProfilesFollowingHelper" %>
<%@ page import="com.ibm.lconn.events.internal.impl.Events" %>
<%@ page import="com.ibm.lconn.events.internal.object.DefaultEventFactory" %>
<%@ page import="com.ibm.lconn.events.internal.Event" %>
<%@ page import="com.ibm.lconn.events.internal.Organization" %>
<%@ page import="com.ibm.lconn.events.internal.EventConstants.Source" %>
<%@ page import="com.ibm.lconn.events.internal.EventConstants.Scope" %>
<%@ page import="com.ibm.lconn.events.internal.EventConstants.Type" %>

<%@ page import="com.ibm.connections.directory.services.data.DSObject" %>
<%@ page import="com.ibm.connections.directory.services.DSProviderFactory" %>
<%@ page import="com.ibm.connections.directory.services.DSProvider" %>
<%@ page import="com.ibm.lconn.profiles.data.UserPlatformEventData" %>
<%@ page import="com.ibm.lconn.profiles.data.UserPlatformEvent" %>
<%@ page import="com.ibm.lconn.profiles.data.ProfileDescriptor" %>
<%@ page import="com.ibm.lconn.profiles.internal.service.TDIProfileService" %>
<%@ page import="com.ibm.lconn.profiles.internal.service.ProfileService" %>
<%@ page import="com.ibm.lconn.profiles.internal.service.AppServiceContextAccess" %>
<%@ page import="com.ibm.lconn.profiles.internal.data.profile.UserState" %>
<%@ page import="com.ibm.lconn.commands.IUserLifeCycleConstants" %>
<%@ page import="com.ibm.peoplepages.data.Employee" %>
<%@ page import="com.ibm.peoplepages.data.ProfileRetrievalOptions" %>
<%@ page import="com.ibm.peoplepages.util.appcntx.AppContextAccess" %>


<%
final ProfilesFollowingHelper instance = ProfilesFollowingHelper.getInstance();
String source_userid = request.getParameter ("source_userid");
String target_userid = request.getParameter ("target_userid");
String sync_userid = request.getParameter ("sync_userid");
String sync_username = request.getParameter ("sync_username");
String action = request.getParameter ("action");

try {
	if (null !=action && action.equals("follow")) {
		instance.followPerson(source_userid, target_userid);
	}
}
catch (Exception e) {
	out.println(e);
}	

try {
	if (null !=action && action.equals("unfollow")) {
		instance.unfollowPerson(source_userid, target_userid);
	}
}
catch (Exception e) {
	out.println(e);
}

try {
	if (null !=action && action.equals("sync")) {
		//locate the user from Profile first
	    DSProvider _dsProvider = DSProviderFactory.INSTANCE.getProfileProvider();
		DSObject user = _dsProvider.searchDSObjectByExactIdMatch(sync_userid, DSObject.ObjectType.PERSON);
		
		//To build 'Employee' object from DSObject
		Employee emp = new Employee();
		emp.setDisplayName(sync_username);
		emp.setTenantKey("a");
		emp.setEmail(user.get_email());
		emp.setGuid(user.get_id());
		emp.setState(user.is_inactive()?UserState.INACTIVE:UserState.ACTIVE);
		
		/* The code commented out below tries to update Profiles database however this does not work in the 'Bridge' Webapp
		   since it does not use Spring framework
		
		// Need admin role to read/update profile
		AppContextAccess.getContext().setAdministrator(true);
		
		// To query Employee object from Profiles DB, to get profile key
		ProfileService profile_service =AppServiceContextAccess.getContextObject(ProfileService.class);
		Employee ret = profile_service.getProfileByEmailsForJavelin(user.get_email(), ProfileRetrievalOptions.MINIMUM);
		emp.setKey(ret.getKey());
		
		// To update user display name by profile key (for testing purpose otherwise re-sync from apps will override the sync)
		TDIProfileService service = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		ProfileDescriptor pdesc = new ProfileDescriptor();
		pdesc.setProfile(emp);
		//update user profile (display name)
		service.update(pdesc);
		
		*/
		
		// build profile event data
		UserPlatformEventData eventData = new UserPlatformEventData();
		eventData.setEmp(emp);
		eventData.setEventType(IUserLifeCycleConstants.USER_RECORD_UPDATE);
		eventData.setOldGuid(user.get_id());
		eventData.setOldOrgId("a");
		eventData.setLogins(user.get_login());
		
		
	    // build profile event based on profile event data
		UserPlatformEvent platformEvent = new UserPlatformEvent();
		platformEvent.setEventType(eventData.getEventType());
		platformEvent.setPayload(eventData.getDbPayloadFormat());
		platformEvent.setTenantKey(eventData.getEmployee().getTenantKey());
		// what is event key?
		//platformEvent.setEventKey(null);
		
		// build common event based on profile event
		Event event = DefaultEventFactory.createEvent(Source.PROFILES,
					Type.COMMAND, Scope.PUBLIC, IUserLifeCycleConstants.USER_RECORD_UPDATE);
		// set user profile payload
		event.setProperties(platformEvent.getPropsToPublish());
		event.addProperty(IUserLifeCycleConstants.COMMAND_ID, Integer.toString(platformEvent.getEventKey()));
		Organization orgObj = DefaultEventFactory.createOrganizationByID(platformEvent.getTenantKey());
		event.getContainerDetails().setOwningOrganization(orgObj);
		
		// sent event
		Events.invokeAsync(event, true);
					
	}
}
catch (Exception e) {
	out.println(e);
}

%>