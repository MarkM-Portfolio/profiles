/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
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

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.core.web.util.RestServletUtil;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.lconn.profiles.web.util.CachingHelper;

import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionCollection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.RetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.webui.servlet.RestServlet.RestAction;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class ViewAllFriendsAction implements RestAction
{
	public void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws IOException, DataAccessException, XMLStreamException
	{		
		CachingHelper.disableCaching(response);

		String uid = request.getParameter("uid");
		String userKey = request.getParameter("key");

		if (userKey == null)
		{
			PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			userKey = pps.getLookupForPLK(ProfileLookupKey.Type.KEY, ProfileLookupKey.forUid(uid), false);
		}
		int pageSize = 10;
		String pageSizeS = request.getParameter("pageSize");
		if (pageSizeS != null && !pageSizeS.equals(""))
			pageSize = Integer.parseInt(pageSizeS);

		String pageNumberS = request.getParameter("pageNumber");
		int pageNumber = 0;
		if (pageNumberS != null && !pageNumberS.equals(""))
			pageNumber = Integer.parseInt(pageNumberS);

		String orderByS = request.getParameter("sortBy");
		int orderBy = RetrievalOptions.OrderByType.MOST_RECENT;
		if (orderByS != null && !orderByS.equals(""))
			orderBy = Integer.parseInt(orderByS);

		String xsltDocUrl = request.getParameter("xslt");

		Map<String, String> additionalRootAttrs = null;

		String parameter = request.getParameter("ui-level");
		if (parameter != null && parameter.equals("second"))
		{
			additionalRootAttrs = new HashMap<String, String>();
			additionalRootAttrs.put("ui-level", "second third");
		}
		else if (parameter != null && parameter.equals("third"))
		{
			additionalRootAttrs = new HashMap<String, String>();
			additionalRootAttrs.put("ui-level", "third");
		}

//		performFriends(userKey, pageSize, pageNumber, writer, additionalRootAttrs, true, orderBy, serviceLink);
		performFriendsWrite(request, response, userKey, pageSize, pageNumber, additionalRootAttrs, xsltDocUrl, true, orderBy);
	}

	public static void performFriendsWrite(HttpServletRequest request, HttpServletResponse response,
			String userKey, int pageSize, int pageNumber, Map<String, String> additionalRootAttrs,
			String xsltDocUrl, boolean getUserInfoDetails, int orderBy)
			throws DataAccessException, XMLStreamException, IOException
	{
		ConnectionService cs = AppServiceContextAccess.getContextObject(ConnectionService.class);
		ConnectionRetrievalOptions options = getFriendsOptions(userKey, pageSize, pageNumber, orderBy);

		ConnectionCollection cc = cs.getConnections(ProfileLookupKey.forKey(userKey), options);
		List<Connection> results = cc.getResults();

		int pendingInvitations = cc.getPendingInvitations();
		int totalFriends = cc.getTotalResults();

		String serviceLink = ServiceReferenceUtil.getServiceLink("profiles", request.isSecure());

		XMLStreamWriter writer = RestServletUtil.getXMLWriter2(response, true);

		if (xsltDocUrl != null && !xsltDocUrl.equals(""))
		{
			writer.writeProcessingInstruction("xml version=\"1.0\"");//$NON-NLS-1$ //$NON-NLS-2$
			writer.writeProcessingInstruction("xml-stylesheet type=\"text/xsl\" href=\"" + xsltDocUrl + "\"");//$NON-NLS-1$ //$NON-NLS-2$
		}

		writeXML(writer, additionalRootAttrs, getUserInfoDetails, results, pendingInvitations, totalFriends, options, serviceLink);
	}

	public static ConnectionRetrievalOptions getFriendsOptions(String userKey, int pageSize, int pageNumber, int orderBy) throws DataAccessException, XMLStreamException
	{
		ConnectionRetrievalOptions options = new ConnectionRetrievalOptions();
		options.setOrderBy(orderBy);
		options.setStatus(Connection.StatusType.ACCEPTED);
		options.setMaxResultsPerPage(pageSize);
		options.setSkipResults(pageNumber * pageSize);
		options.setProfileOptions(ProfileRetrievalOptions.LITE);
		options.setEmployeeState(UserState.ACTIVE);
		if (AppContextAccess.isAuthenticated() && StringUtils.equals(userKey, Employee.getKey(AppContextAccess.getCurrentUserProfile())))
		{
			options.setInclPendingCount(true);
		}
		return options;
	}

	static String profilesNameSpaceURI = "http://www.ibm.com/xmlns/prod/sn/profiles";
	static String profileURIPrefix = "snx";
	
	private static void writeXML(XMLStreamWriter writer, Map<String, String> additionalRootAttrs, boolean getUserInfoDetails, List<Connection> results, int pendingInvitations, int totalFriends, ConnectionRetrievalOptions options, String serviceLink2) throws XMLStreamException
	{		
		writer.setPrefix(profileURIPrefix, profilesNameSpaceURI);
		writer.setPrefix("atom","http://www.w3.org/2005/Atom");
		writeStartElement(writer, "feed");
		//writer.writeDefaultNamespace("http://www.w3.org/2005/Atom");
		writer.writeNamespace(profileURIPrefix,profilesNameSpaceURI);
		writer.writeNamespace("atom","http://www.w3.org/2005/Atom");

		writeAttribute(writer, "feed-type", "friends");
		writeAttribute(writer, "new-invitations-count", String.valueOf(pendingInvitations));
		writeAttribute(writer, "total-friends", String.valueOf(totalFriends));
		writeAttribute(writer, "current-page", String.valueOf(options.calculatePageNumber()-1));
		writeAttribute(writer, "sort-by", String.valueOf(options.getOrderBy()));
		writeAttribute(writer, "items-per-page", String.valueOf(options.getMaxResultsPerPage()));
		
		if(additionalRootAttrs != null)
			writeAdditionalAttributes(writer, additionalRootAttrs);
		
		for (Connection conn : results)
		{	
			// filter the source and target profiles per the hidden api attribute
			APIHelper.filterProfileAttrForAPI(conn.getTargetProfile());
			
			Employee employee = conn.getTargetProfile();
			
			String displayName = employee.getDisplayName();
			//STAX escaped xml content automatically
//			displayName = XMLUtil.escapeXML(displayName);

			writeStartElement(writer, "entry");

			WorkLocation workLocation = employee.getWorkLocation();
			if(workLocation != null)
			{
			  StringBuilder location = new StringBuilder("");
			  if (workLocation.getCity() != null)
			  {
			    location.append(workLocation.getCity().trim());			    
			  }
			  
			  if (workLocation.getState() != null)
			  {
			    if (workLocation.getCity() != null && workLocation.getCity().trim().length() > 0)
			    {
			      location.append(" ");
			    }			    
			    location.append(workLocation.getState().trim());
			  }
//				location = XMLUtil.escapeXML(location);
				writeAttribute(writer, "location", location.toString());
			}

			writeAttribute(writer, "uid", employee.getUid());
			writeAttribute(writer, "key", employee.getKey());
			writeAttribute(writer, "userid", employee.getUserid());

			UserState state =  employee.getState();
			if( state == UserState.INACTIVE ) { // don't polute; by default if isActive is missing, assume true
				writeAttribute(writer, "isActive", (state != UserState.INACTIVE ? "true" : "false"));
			}
			
			writeAttribute(writer, "email", employee.getEmail());
			writeAttribute(writer, "imageUrl", serviceLink2 + "/photo.do?key=" + employee.getKey() + "&lastMod=" + employee.getLastUpdate().getTime());
			if(getUserInfoDetails)
			{
				//writer.write("date='"+conn.getCreated().getTime());
				writeAttribute(writer, "groupware-mail", employee.getGroupwareEmail());
				writeAttribute(writer, "title", //XMLUtil.escapeXML
						(employee.getJobResp()));
				String organizationTitle = employee.getOrganizationTitle();
				if(organizationTitle != null)
					writeAttribute(writer, "org", //XMLUtil.escapeXML
							(organizationTitle));
				writeAttribute(writer, "tel", employee.getTelephoneNumber());
			}

			writeStartElement(writer, "title");
			writer.writeCharacters(displayName);
			writer.writeEndElement();
			
			writeStartElement(writer, "id");
			writer.writeCharacters(conn.getConnectionId());
			writer.writeEndElement();
			
			writeStartElement(writer, "link");
			writer.writeAttribute("href", serviceLink2 + "/html/profileView.do?key=" + employee.getKey());
			writer.writeEndElement();
			
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
		writer.close();
		
	}


	/**
	 * @param writer
	 * @param elementName TODO
	 * @throws XMLStreamException
	 */
	private static void writeStartElement(XMLStreamWriter writer, String elementName) throws XMLStreamException
	{
//		writer.writeNamespace("atom","http://www.w3.org/2005/Atom");
		writer.writeStartElement("atom", elementName, "http://www.w3.org/2005/Atom");
	}

	private static void writeAdditionalAttributes(XMLStreamWriter writer, Map<String, String> additionalRootAttrs) throws XMLStreamException
	{
		for (Iterator<String> iter = additionalRootAttrs.keySet().iterator(); iter.hasNext();)
		{
			String key = iter.next();
			writeAttribute(writer,key,additionalRootAttrs.get(key));
		}
	}

	private static void writeAttribute(XMLStreamWriter writer, String attrName, String attrValue) throws XMLStreamException
	{
		writer.writeAttribute(profileURIPrefix,profilesNameSpaceURI, attrName, attrValue);
	}
}
