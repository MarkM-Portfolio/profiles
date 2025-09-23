/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                   */
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
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.RetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 * @author <a href="mailto:zhouwen_lu@us.ibm.com">Joseph Lu</a>
 */
public class WriteXMLDocUtil
{
    public static void writeXMLDoc(HttpServletRequest request, HttpServletResponse response, List<Employee> users, String feedType, int totalCount) throws IOException, DataAccessException, XMLStreamException {
	String uid = request.getParameter("uid");
	String userKey = request.getParameter("key");
	
	if(userKey == null) {
	    PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
	    userKey = pps.getLookupForPLK(ProfileLookupKey.Type.KEY, ProfileLookupKey.forUid(uid), false);
	}
	int pageSize = 10;
	String pageSizeS = request.getParameter("pageSize");
	if(pageSizeS != null && !pageSizeS.equals(""))
	    pageSize = Integer.parseInt(pageSizeS);
	
	String pageNumberS = request.getParameter("pageNumber");
	int pageNumber = 0;
	if(pageNumberS != null && !pageNumberS.equals("")) {
	    pageNumber = Integer.parseInt(pageNumberS);
	    /*
	    if ( pageNumber > 0 )
		pageNumber = pageNumber - 1;
	    */
	}

	String orderByS = request.getParameter("sortBy");
	int orderBy = RetrievalOptions.OrderByType.MOST_RECENT;
	if(orderByS != null && !orderByS.equals(""))
	    orderBy = Integer.parseInt(orderByS);
	
	String xsltDocUrl = request.getParameter("xslt");
	XMLStreamWriter writer = RestServletUtil.getXMLWriter2(response, false);
	// make caching private
	RestServletUtil.setupResponseHeaders(response, false, "application/atom+xml", true);
	// remove expires header
	response.setHeader("Expires", "");
	response.setHeader("Pragma", "");
	
	String serviceLink = ServiceReferenceUtil.getServiceLink("profiles", request.isSecure());
	
	Map<String, String> additionalRootAttrs = null;
	
	String parameter = request.getParameter("ui-level");
	if(parameter != null && parameter.equals("second"))
	    {
		additionalRootAttrs = new HashMap<String, String>();
		additionalRootAttrs.put("ui-level", "second third");
	    }
	else if(parameter != null && parameter.equals("third"))
	    {
		additionalRootAttrs = new HashMap<String, String>();
		additionalRootAttrs.put("ui-level", "third");
	    }
	
	if(xsltDocUrl != null && !xsltDocUrl.equals(""))
	    {
		writer.writeProcessingInstruction("xml version=\"1.0\"");//$NON-NLS-1$ //$NON-NLS-2$
		writer.writeProcessingInstruction("xml-stylesheet type=\"text/xsl\" href=\"" + xsltDocUrl + "\"");//$NON-NLS-1$ //$NON-NLS-2$
	    }
	
	performWriting(userKey, pageSize, pageNumber, writer, additionalRootAttrs, true, orderBy, serviceLink, users, feedType, totalCount);
    }

    static String profilesNameSpaceURI = "http://www.ibm.com/xmlns/prod/sn/profiles";
    static String profileURIPrefix = "snx";

    public static void performWriting(String userKey, int pageSize, int pageNumber, XMLStreamWriter writer, Map<String, String> additionalRootAttrs, boolean getUserInfoDetails, int orderBy, String serviceLink, List<Employee> users, String feedType, int totalUsers) throws DataAccessException, XMLStreamException
    {
	writer.setPrefix(profileURIPrefix,profilesNameSpaceURI);
	writer.setPrefix("atom","http://www.w3.org/2005/Atom");
	writeStartElement(writer, "feed");
	//writer.writeDefaultNamespace("http://www.w3.org/2005/Atom");
	writer.writeNamespace(profileURIPrefix,profilesNameSpaceURI);
	writer.writeNamespace("atom","http://www.w3.org/2005/Atom");
	
	writeAttribute(writer, "feed-type", feedType);
	writeAttribute(writer, "total-count", String.valueOf(totalUsers));	
	writeAttribute(writer, "current-page", String.valueOf(pageNumber));
	writeAttribute(writer, "sort-by", String.valueOf(orderBy));
	writeAttribute(writer, "items-per-page", String.valueOf(pageSize));
	
	if(additionalRootAttrs != null)
	    writeAdditionalAttributes(writer, additionalRootAttrs);
	
	for (Employee employee : users) {
	    writeXMLDocEntry(writer, employee, serviceLink, "entry");
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

    public static void writeXMLDocEntry(XMLStreamWriter writer, Employee employee, String serviceLink, String entryType) throws DataAccessException, XMLStreamException {
    	
    	// need to filter profile for any REST API
    	APIHelper.filterProfileAttrForAPI(employee);
	String displayName = employee.getDisplayName();
	writeStartElement(writer, entryType);

	WorkLocation workLocation = employee.getWorkLocation();
	if(workLocation != null) {
	    StringBuilder location = new StringBuilder("");

	    if (workLocation.getCity() != null) {
		location.append(workLocation.getCity().trim());			    
	    }
	    if (workLocation.getState() != null) {
		if (workLocation.getCity() != null && workLocation.getCity().trim().length() > 0) {
		    location.append(" ");
		}			    
		location.append(workLocation.getState().trim());
	    }
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
	// ICS 168874 : hashid/moced is internal, don't expose especially along with the email.
	//writeAttribute(writer, PeoplePagesServiceConstants.MCODE, employee.getMcode());
	writeAttribute(writer, "imageUrl", serviceLink + "/photo.do?key=" + employee.getKey() + "&lastMod=" + employee.getLastUpdate().getTime());
	writeAttribute(writer, "groupware-mail", employee.getGroupwareEmail());
	writeAttribute(writer, "title", (employee.getJobResp()));

	String organizationTitle = employee.getOrganizationTitle();
	if(organizationTitle != null)
	    writeAttribute(writer, "org", (organizationTitle));
			   
	writeAttribute(writer, "tel", employee.getTelephoneNumber());
	writeStartElement(writer, "title");
	writer.writeCharacters(displayName);
	writer.writeEndElement();
	writeStartElement(writer, "link");
	writer.writeAttribute("href", serviceLink + "/html/profileView.do?key=" + employee.getKey());
	writer.writeEndElement();
	writer.writeEndElement();
    }

}
