/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2007, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileTag;
import com.ibm.peoplepages.data.ProfileTagCloud;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * Utility class to write ATOM documents
 * 
 * @author ahernm@us.ibm.com
 *
 */
public final class AtomGenerator implements AtomConstants
{
	private XMLStreamWriter writer;
	
	/**
	 * Default ctor for web environment
	 * 
	 * @param response
	 * @param contentType
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public AtomGenerator(HttpServletResponse response, String contentType) 
		throws IOException, XMLStreamException
	{
		response.setCharacterEncoding(XML_ENCODING);
		response.setContentType(contentType);
		
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
		writer = factory.createXMLStreamWriter(response.getWriter());
	}
	
	/**
	 * Default ctor for non-web junit testing
	 * 
	 * @param outputWriter
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public AtomGenerator(PrintWriter outputWriter) 
		throws IOException, XMLStreamException
	{
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
		writer = factory.createXMLStreamWriter(outputWriter);
	}
	
	public final XMLStreamWriter getWriter()
	{
		return writer;
	}
	
	/**
     * Writes the &lt;atom:generator/&gt; element
     * 
     * @throws XMLStreamException
     */
    public final void writeGenerator() 
    	throws XMLStreamException
    {
    	writer.writeStartElement(NS_ATOM, GENERATOR);
		writer.writeAttribute(VERSION, GENERATOR_VERSION);
		writer.writeCharacters(GENERATOR_NAME);
		writer.writeEndElement();
    }
    
    public final void writeCategory(String term, String scheme, int frequency)
		throws XMLStreamException
	{
    	writeStartCategory(term);
		if (scheme != null) writer.writeAttribute(SCHEME, scheme);
		if (frequency > 0) writer.writeAttribute(NS_SNX, FREQUENCY, String.valueOf(frequency));
		writeEndCategory();
	}
    
    public final void writeStartCategory(String term)
    	throws XMLStreamException
    {
    	writer.writeStartElement(NS_ATOM, CATEGORY);
    	writer.writeAttribute(TERM, term);
    }
    
    public final void writeEndCategory()
		throws XMLStreamException
	{
		writer.writeEndElement();
	}
    
    public final void writeStartCategoriesDocument(String scheme, boolean isFixed)
    	throws XMLStreamException
    {
    	// start doc
    	writer.setPrefix(PRE_APP, NS_APP);
		writer.setPrefix(PRE_ATOM, NS_ATOM);
		writer.setPrefix(PRE_SNX, NS_SNX);	
		writer.writeStartDocument(XML_ENCODING, XML_VERSION);
		
		// start CATEGORIES
		writer.writeStartElement(NS_APP, CATEGORIES);
		writer.writeNamespace(PRE_APP, NS_APP);
		writer.writeNamespace(PRE_ATOM, NS_ATOM);
		writer.writeNamespace(PRE_SNX, NS_SNX);	
		writer.writeAttribute(FIXED, String.valueOf(isFixed));
		if (scheme != null) writer.writeAttribute(SCHEME, scheme);
    }
        
    /**
     * Convenience.
     * 
     * @throws XMLStreamException
     */
    public final void writeEndCategoriesDocument()
		throws XMLStreamException
	{
    	writer.writeEndElement();
    	writer.writeEndDocument();
	}
    
    
    public static final Map<String, ProfileLookupKey.Type> FLAG_BY_PARAM_TYPE_MAP;
	
	static
	{
		HashMap<String, ProfileLookupKey.Type> pm = new HashMap<String, ProfileLookupKey.Type>(6);
		pm.put(PeoplePagesServiceConstants.FLAG_BY_KEY, ProfileLookupKey.Type.KEY);
		pm.put(PeoplePagesServiceConstants.FLAG_BY_UID, ProfileLookupKey.Type.UID);
		pm.put(PeoplePagesServiceConstants.FLAG_BY_GUID, ProfileLookupKey.Type.GUID);	
		pm.put(PeoplePagesServiceConstants.FLAG_BY_USERID, ProfileLookupKey.Type.USERID);
		FLAG_BY_PARAM_TYPE_MAP = Collections.unmodifiableMap(pm);
	}
    
	/**
	 * Input for write tag cloud method
	 */
	public static class ReqBean {
		public ProfileLookupKey sourceLookupKey = null;
		public ProfileLookupKey targetLookupKey = null;
		public ProfileLookupKey flagByLookupKey = null;
		
		public ProfileTagCloud tagCloud = null;
		
		public boolean fullFormat = false;
		public boolean inclContribCount = true;
		
		public ReqBean() {}
	}
	
	/**
	 * Utility method for outputting tag clouds
	 * @param reqBean
	 * @throws XMLStreamException
	 */
	public final void writeTagCloud(ReqBean reqBean) throws XMLStreamException
	{
		try {
			ProfileTagCloud tagCloud = reqBean.tagCloud;

			writeStartCategoriesDocument(null, false);

			writetParamAttribute(BaseAction.SOURCE_PARAM_TYPE_MAP, reqBean.sourceLookupKey);
			writetParamAttribute(BaseAction.TARGET_PARAM_TYPE_MAP, reqBean.targetLookupKey);
			writetParamAttribute(FLAG_BY_PARAM_TYPE_MAP, reqBean.flagByLookupKey);

			// Include number of taggers in feed
			if (reqBean.inclContribCount) {
				getWriter().writeAttribute(
						NS_SNX,
						"numberOfContributors",	
						String.valueOf(tagCloud.getContributors().size()));	
			}

			writeGenerator();

			Employee flagByProfile = null;
			if (reqBean.flagByLookupKey != null)
			{			
				for (Employee c : tagCloud.getContributors().values())
				{
					if (c.matchesLookupKey(reqBean.flagByLookupKey))
					{
						flagByProfile = c;
						break;
					}
				}
			}

			if (tagCloud != null) {
				for (ProfileTag tag : tagCloud.getTags())
				{
					writeStartCategory(tag.getTag());
					writer.writeAttribute(NS_SNX, FREQUENCY, String.valueOf(tag.getFrequency()));
					writer.writeAttribute(NS_SNX, INTENSITY_BIN, String.valueOf(tag.getIntensityBin()));
					writer.writeAttribute(NS_SNX, VISIBILITY_BIN, String.valueOf(tag.getVisibilityBin()));

					if (flagByProfile  != null 
							&& tag.getSourceKeys() != null
							&& Arrays.asList(tag.getSourceKeys()).contains(flagByProfile.getKey()))
					{
						writer.writeAttribute(NS_SNX, FLAGGED, Boolean.TRUE.toString());
					}

					if (reqBean.fullFormat && tag.getSourceKeys() != null && tag.getSourceKeys().length > 0)
					{
						boolean allowEmailInReturn = LCConfig.instance().isEmailReturned();

						for (String contribKey : tag.getSourceKeys())
						{
							Employee contribProfile = tagCloud.getContributors().get(contribKey);

							if (contribProfile != null)
							{
								writer.writeStartElement(NS_ATOM, "contributor");
								writer.writeAttribute(NS_SNX, PROFILE_KEY, contribProfile.getKey());
								writer.writeAttribute(NS_SNX, PROFILE_UID, contribProfile.getUid());
								writer.writeAttribute(NS_SNX, PROFILE_GUID, contribProfile.getGuid());

								writer.writeStartElement(NS_ATOM, "name");
								writer.writeCharacters(contribProfile.getDisplayName());
								writer.writeEndElement(); // name

								writer.writeStartElement(NS_SNX, "userid");
								writer.writeCharacters(contribProfile.getUserid());
								writer.writeEndElement(); // userid

								if (allowEmailInReturn)
								{
									writer.writeStartElement(NS_ATOM, "email");
									writer.writeCharacters(contribProfile.getEmail());
									writer.writeEndElement(); // email
								}

								writer.writeEndElement(); // contributor
							}
						}
					}

					writeEndCategory();
				}		
			}
			writeEndCategoriesDocument();

			writer.flush();
		}
		finally {
			writer.close();
		}
	}

    /**
     * 
     * @param mapping
     * @param plk
     * @throws XMLStreamException
     */
    private final void writetParamAttribute(Map<String, ProfileLookupKey.Type> mapping, ProfileLookupKey plk)
    throws XMLStreamException
    {
    	if (plk != null)
    	{
    		String attrName = "";
    		for (Map.Entry<String, ProfileLookupKey.Type> me : mapping.entrySet())
    		{
    			if (me.getValue() == plk.getType())
    			{
    				attrName = me.getKey();
    				break;
    			}
    		}

    		writer.writeAttribute(
    				NS_SNX, 
    				attrName, 
    				plk.getValue());
    	}
    }
}
