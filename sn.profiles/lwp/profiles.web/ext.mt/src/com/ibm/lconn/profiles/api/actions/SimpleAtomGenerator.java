/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import java.io.Writer;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.abdera.writer.StreamWriter;

import com.ibm.lconn.core.appext.msgvector.atom.api.MessageVectorAtomConstants;
import com.ibm.lconn.core.web.atom.util.LCAtomConstants;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.data.profile.UserState;


/**
 * This is a very simple Atom generator to write the minimum information to the output on success in order to
 * determine which components have succeeded if any.
 * 
 * The snx prefix is probably incorrect but didn't get too much thought
 * 
 * Much was copied from AtomGenerator2 - this should all be reintegrated
 * 
 * @author blooby
 *
 */
public final class SimpleAtomGenerator {
	private StreamWriter sw;
	
	private final static String NS = "http://www.ibm.com/xmlns/prod/sn"; // not visible from AtomGenerator2
	
	private static String COMMAND_RESPONSE_TITLE="Admin Profile Command Response";
	private static String ORG_FEED_TITLE="Organization Feed";
	
	private static final QName QN_ACTIVE = new QName(AtomConstants.NS_APP, "servicesActive");
	private static final QName QN_FAILED = new QName(AtomConstants.NS_APP, "servicesFailed");
	private static final QName QN_SERVICE = new QName(AtomConstants.NS_APP, "service");
	
	private static final QName QN_ORGSTATE = new QName(AtomConstants.NS_APP, "orgState");

	public SimpleAtomGenerator(Writer out)
	{
		sw = AtomConstants.writerFactory.newStreamWriter();
		sw.setWriter(out);
	}
	
	public void generateAtomBSSResponse(Iterable<String> servicesSucceeded, Iterable<String> servicesFailed) {
		
		sw.setPrefix("", LCAtomConstants.NS_ATOM);
		sw.setPrefix(AtomConstants.PRE_APP, AtomConstants.NS_APP);	
		sw.startDocument(AtomConstants.XML_ENCODING, AtomConstants.XML_VERSION);
		sw.startEntry();
		
		sw.writeTitle(COMMAND_RESPONSE_TITLE);
		
		try {
			createBSSContent(servicesSucceeded, servicesFailed);
		} catch (XMLStreamException e) {
			e.printStackTrace(); // unexpected
		}
		
		sw.endEntry();
		sw.endDocument();
	}

	private void createBSSContent(Iterable<String> servicesSucceeded, Iterable<String> servicesFailed) throws XMLStreamException {

		sw.startContent(AtomConstants.XML_CONTENT_TYPE);

		sw.setPrefix("snx", LCAtomConstants.NS_SNX);
		sw.setPrefix("", AtomConstants.NS_OPENSOCIAL);
		
		sw.startElement(QN_ACTIVE);
		writeServicesData(servicesSucceeded);
		sw.endElement(); // succeeded

		sw.startElement(QN_FAILED);
		writeServicesData(servicesFailed);
		sw.endElement(); // failed

		sw.endContent();
	}

	private void writeServicesData(Iterable<String> services) throws XMLStreamException {

		for (String service : services) {
			sw.startElement(QN_SERVICE);
			sw.writeElementText(service);
			sw.endElement(); // service Name
		}
	}
	
	public void generateAtomFeed(List<Tenant> list) throws Exception
	{
		setNamespace();
		sw.startDocument(AtomConstants.XML_ENCODING, AtomConstants.XML_VERSION);
		sw.startFeed();
		writeNamespaceInfo();
		
		sw.writeId(AtomGenerator2.FEED_ID);
		sw.writeGenerator(AtomConstants.GENERATOR_VERSION, NS, AtomConstants.GENERATOR_NAME); // Not translated
		sw.writeTitle(ORG_FEED_TITLE);
		sw.writeUpdated(new Date());
		
		for (Tenant tenant : list) {
			generateAtomEntry(tenant, false);
		}

		sw.endFeed();
		sw.endDocument();
	}
	
	public void generateAtomEntry(Tenant tenant, boolean declareNs) throws Exception
	{
		if (declareNs)
			setNamespace();
		
		sw.startEntry();
	
		if (declareNs)
			writeNamespaceInfo();
	
		if (tenant != null)
		{
			sw.writeTitle(tenant.getName());
			if (tenant.getLastUpdate() != null) 
				sw.writeUpdated(tenant.getLastUpdate());
			
			String recordId = URLEncoder.encode(tenant.getExid(), "UTF-8");
			sw.writeId(recordId.replaceAll("\\+", "%20"));
			
			sw.startElement(QN_ORGSTATE);
			sw.writeElementText( (UserState.fromCode(tenant.getState())).getName());
			sw.endElement();
		}
		
		sw.endEntry();
	}

	
	private final void setNamespace() {
		sw.setPrefix(MessageVectorAtomConstants.NS_PREFIX_OPENSEARCH, MessageVectorAtomConstants.NS_OPENSEARCH);
		sw.setPrefix("", LCAtomConstants.NS_ATOM);
		sw.setPrefix(AtomGenerator2.NS_PREFIX_SNX, LCAtomConstants.NS_SNX);
		sw.setPrefix(AtomGenerator2.NS_PREFIX_FH, AtomGenerator2.FH_NS);
		sw.setPrefix(AtomGenerator2.NS_PREFIX_THREAD, AtomGenerator2.THR_NS);
		sw.setPrefix(AtomGenerator2.NS_PREFIX_APP, AtomGenerator2.NS_APP);	
//		sw.setPrefix("", OPENSOCIAL_NS);
	}
	
	private final void writeNamespaceInfo() {
		sw.writeNamespace(MessageVectorAtomConstants.NS_PREFIX_OPENSEARCH, MessageVectorAtomConstants.NS_OPENSEARCH);
		sw.writeNamespace("snx", LCAtomConstants.NS_SNX);
		sw.writeNamespace(AtomGenerator2.NS_PREFIX_FH, AtomGenerator2.FH_NS);
		sw.writeNamespace(AtomGenerator2.NS_PREFIX_THREAD, AtomGenerator2.THR_NS);
		sw.writeNamespace(AtomGenerator2.NS_PREFIX_APP, AtomGenerator2.NS_APP);			
	}

}
