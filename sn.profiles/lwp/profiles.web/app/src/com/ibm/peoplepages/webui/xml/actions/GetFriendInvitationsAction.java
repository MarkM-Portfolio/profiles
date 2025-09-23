/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2006, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.xml.actions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.ibm.lconn.core.web.util.RestServletUtil;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.lconn.profiles.api.actions.AtomParser;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.lconn.profiles.internal.util.XMLUtil;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionCollection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.RetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.webui.servlet.RestServlet.RestAction;

public class GetFriendInvitationsAction implements RestAction
{
    static String profilesNameSpaceURI = "http://www.ibm.com/xmlns/prod/sn/profiles";
    static String profileURIPrefix = "snx";

	public void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws IOException, DataAccessException, XMLStreamException
	{
	        String serviceLink = ServiceReferenceUtil.getServiceLink("profiles", request.isSecure());

//		String uid = GetUserInfoAction.getUidFromLoggedInUser(request);
		ConnectionService svc = AppServiceContextAccess.getContextObject(ConnectionService.class);
		String key = GetUserInfoAction.getKeyFromLoggedInUser(request); //svc.getKeyForUid(uid);
		
		ConnectionRetrievalOptions cro = new ConnectionRetrievalOptions();
		cro.setStatus(Connection.StatusType.PENDING);
		cro.setProfileOptions(ProfileRetrievalOptions.LITE);
		cro.setMaxResultsPerPage(PropertiesConfig.getInt(ConfigProperty.TEMP_UI_SETTING_MAX_INVITES));
		
		// Defect 66975: we need to set to include the invitation message specifically
		cro.setInclMessage( true );
		
		ConnectionCollection listOfEmployess = svc.getConnections(ProfileLookupKey.forKey(key), cro);

		String xsltDocUrl = request.getParameter("xslt");

        XMLStreamWriter writer = RestServletUtil.getXMLWriter2(response, true);

		Map<String,String> additionalRootAttrs = new HashMap<String,String>();
		String parameter = request.getParameter("ui-level");
		if(parameter != null && parameter.equals("second"))
			additionalRootAttrs.put("ui-level", "second third");
		else if(parameter != null && parameter.equals("third"))
			additionalRootAttrs.put("ui-level", "third");

        if (xsltDocUrl != null && !xsltDocUrl.equals("")) {
            writer.writeProcessingInstruction("xml version=\"1.0\"");//$NON-NLS-1$ //$NON-NLS-2$
            writer.writeProcessingInstruction("xml-stylesheet type=\"text/xsl\" href=\"" + xsltDocUrl + "\"");//$NON-NLS-1$ //$NON-NLS-2$
    	}

        writer.writeStartElement("invitations");
        writer.writeNamespace(profileURIPrefix, profilesNameSpaceURI);
		writer.writeNamespace("", profilesNameSpaceURI);

		if(additionalRootAttrs != null)
			writeAdditionalAttributes(writer, additionalRootAttrs);

		ConnectionRetrievalOptions incommonCRO = new ConnectionRetrievalOptions();
		
		for (Iterator<Connection> iter = listOfEmployess.getResults().iterator(); iter.hasNext();) {
			Connection conn = iter.next();
			String connectionId = conn.getConnectionId();
			long date = conn.getCreated().getTime();

			if (conn.getMessage() == null) conn.setMessage("");
			
			Employee empl = conn.getTargetProfile();
			APIHelper.filterProfileAttrForAPI(empl);
			
			int totalCount = svc.getConnectionsInCommonCount(ProfileLookupKey.Type.KEY, new String[]{key, empl.getKey()}, incommonCRO);
			UserState state = empl.getState();
			
            writer.writeStartElement("invitation");
            writeAttribute(writer, "inviter-name", empl.getDisplayName());
            writeAttribute(writer, "connectionId", connectionId);
            writeAttribute(writer, "date", Long.toString(date));
            writeAttribute(writer, "msg", conn.getMessage());
            writeAttribute(writer, "email", empl.getEmail());
            writeAttribute(writer, "key", empl.getKey());
            writeAttribute(writer, "lastMod", Long.toString(empl.getLastUpdate().getTime()));
            writeAttribute(writer, "uid", empl.getUid());
            writeAttribute(writer, "userid", empl.getUserid());

            if( state == UserState.INACTIVE ) { // don't polute; by default if isActive is missing, assume true
                writeAttribute(writer, "isActive", (state != UserState.INACTIVE ? "true" : "false"));
            }

			// Write the common user entries
			ConnectionCollection commonConns = getConnectionsInCommon(request, new String[]{key, empl.getKey()}, svc, totalCount);
			List<Connection> results = commonConns.getResults();
			for (Connection cc : results) {
			    Employee employee = cc.getTargetProfile();
			    writeCommonFriendEntry(writer, employee, serviceLink, "common-friend");
			}
            writer.writeEndElement();
		}

		writer.writeEndElement();
        writer.flush();
        writer.close();
	}

    private void writeCommonFriendEntry(XMLStreamWriter writer, Employee employee, String serviceLink, String entryType) throws DataAccessException, IOException, XMLStreamException {

    	APIHelper.filterProfileAttrForAPI(employee);

        writer.writeStartElement(entryType);
        writeAttribute(writer, "displayName", employee.getDisplayName());

	    if (employee.getWorkLocation() != null) {
	        String location = employee.getWorkLocation().getCity() + " " +employee.getWorkLocation().getState();
	        writeAttribute(writer, "location", location);
	    }
	    writeAttribute(writer, "uid", employee.getUid());
	    writeAttribute(writer, "key", employee.getKey());
	    writeAttribute(writer, "userid", employee.getUserid());

	    UserState state =  employee.getState();
	   if( state == UserState.INACTIVE ) { // don't polute; by default if isActive is missing, assume true
	       writeAttribute(writer, "isActive", (state != UserState.INACTIVE ? "true" : "false"));
	   }
	   writeAttribute(writer, "email", employee.getEmail());
	   writeAttribute(writer, "imageUrl", serviceLink + "/photo.do?key=" + employee.getKey() + "&lastMod=" + employee.getLastUpdate().getTime());
	   writeAttribute(writer, "groupware-mail", employee.getGroupwareEmail());
	   writeAttribute(writer, "title", (employee.getJobResp()));

       String organizationTitle = employee.getOrganizationTitle();
	   if(organizationTitle != null) {
	       writeAttribute(writer, "org", (organizationTitle));
       }
			   
	   writeAttribute(writer, "tel", employee.getTelephoneNumber());

       writer.writeEndElement();
    }

    private ConnectionCollection getConnectionsInCommon(HttpServletRequest request, String[] keys, ConnectionService cs, int totalCount) {
    	ConnectionRetrievalOptions options = new ConnectionRetrievalOptions();
    	options.setStatus(Connection.StatusType.ACCEPTED);
    	options.setSince(AtomParser.parseSince(request.getParameter("since")));
    	
    	int sortBy = AtomParser.parseConnectionsSortString(
    							   request.getParameter("sortBy"),
    							   RetrievalOptions.OrderByType.UNORDERED);
    	options.setOrderBy(sortBy);
    	
    	int sortOrder = AtomParser.parseConnectionsSortOrderString(
    								   request.getParameter("sortOrder"),
    								   RetrievalOptions.SortOrder.DEFAULT);
    	options.setSortOrder(sortOrder);
    	
    	//
    	// Determine status / options
    	//
    	int statusType = AtomParser.parseConnectionStatusString(
    								request.getParameter("status"),
    								Connection.StatusType.ACCEPTED);

    	int pageSize = -1;
    	String pageSizeS = request.getParameter("pageSize");
    	if(pageSizeS != null && !pageSizeS.equals(""))
    	    pageSize = Integer.parseInt(pageSizeS);
    	
    	String pageNumberS = request.getParameter("pageNumber");
    	int pageNumber = -1;
    	if(pageNumberS != null && !pageNumberS.equals("")) {
    	    pageNumber = Integer.parseInt(pageNumberS);
    	    if ( pageNumber > 0 )
    		pageNumber = pageNumber - 1;
    	}

    	// If there is no request parameter for pagesize, then we get everything
    	if ( pageSize != -1 )
    	    options.setMaxResultsPerPage(pageSize);
    	else {
    	    // Just some safty check at 250 to prevent DB query blowup
    	    if (totalCount > 250 ) totalCount = 250;

    	    options.setMaxResultsPerPage(totalCount);
    	}

    	// Assuming pageNumber starts with 0
    	if ( pageNumber != -1 && pageSize != -1 ) {
    	    options.setSkipResults((pageNumber-1)*pageSize);
    	}

    	options.setConnectionType(PeoplePagesServiceConstants.COLLEAGUE);
    	options.setStatus(statusType);
    	options.setInclRelatedProfiles(true);
    	return cs.getConnectionsInCommon(ProfileLookupKey.Type.KEY, keys, options);
    }

    private static void writeAdditionalAttributes(XMLStreamWriter writer, Map<String, String> additionalRootAttrs) throws XMLStreamException {
        for (Iterator<String> iter = additionalRootAttrs.keySet().iterator(); iter.hasNext();) {
            String key = iter.next();
            writeAttribute(writer, key, additionalRootAttrs.get(key));
        }
    }

    private static void writeAttribute(XMLStreamWriter writer, String attrName, String attrValue) throws XMLStreamException {
        writer.writeAttribute("", profilesNameSpaceURI, attrName, attrValue);
    }
}
