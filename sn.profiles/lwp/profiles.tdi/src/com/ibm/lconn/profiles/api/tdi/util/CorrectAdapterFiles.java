/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/**
 * 
 */
package com.ibm.lconn.profiles.api.tdi.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author user
 *
 */
public class CorrectAdapterFiles {

	private static final String PACKAGES_DIR = "packages";
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		correctAdapters();
	}
	
	/**
	 * Main method for adapter correction
	 * @throws Exception
	 */
	public static void correctAdapters() throws Exception {
		for (File adapter : readAdapters())
			System.out.println(adapter.getName() + "... needs update: " + correctAdapter(adapter));		
	}
	
	/**
	 * Finds a list of adapter files
	 * @return
	 * @throws Exception
	 */
	private static File[] readAdapters() throws Exception {
		File packagesDir = new File(PACKAGES_DIR);
		if (!packagesDir.exists()) {
			throw new IOException("Could not find '" + PACKAGES_DIR + "' directory");
		} else if (!packagesDir.isDirectory() || !packagesDir.canRead() || !packagesDir.canWrite()) {
			throw new IOException("'" + PACKAGES_DIR + "' is not directory or is not read/writable");
		}
		
		return packagesDir.listFiles(new FileFilter(){
			public boolean accept(File pathname) {
				return (pathname.getName().endsWith(".xml") && pathname.isFile());
			}			
		});
	}

	/**
	 * Parses the adapter files and corrects if missing 'profiles' property store
	 * @param adapterFile
	 * @return
	 * @throws Exception
	 */
	private static boolean correctAdapter(File adapterFile) throws Exception {
		Document adapter = toDom(adapterFile);
		
		// keep track of store element
		Element stores = getElement(getPropertiesEl(adapter), "Stores");
		
		// get the properties store element
		List<Element> propStores = getElements(stores, "PropertyStore");
		
		// check if it has the 'profiles' property store defined
		for (Element propStore : propStores) {
			if ("profiles".equals(propStore.getAttribute("name"))) {
				return false;
			}
		}
		
		addProfilesStore(adapter, stores);
		updateAdapterFile(adapterFile, adapter);
		return true;
	}

	/**
	 * Get first element of specified name
	 * 
	 * @param el
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private final static Element getElement(Element el, String name) throws Exception {
		List<Element> els = getElements(el, name);
		if (els.size() < 1)
			throw new Exception("Missing sub-element: " + name);
		
		return els.get(0);
	}

	/**
	 * Get correct properties element
	 * @param adapter
	 * @return
	 * @throws Exception
	 */
	private final static Element getPropertiesEl(Document adapter) throws Exception {
		List<Element> properties = getElements(adapter.getDocumentElement(), "Properties");

		if (properties.size() == 0)
			throw new Exception("Could not find correct elemetn <Properties name=\"Properties\">");
		
		for (Element el : properties)
			if ("Properties".equals(el.getAttribute("name")))
				return el;
		
		return properties.get(0);
		
	}
	
	/**
	 * Utility to get child elements
	 * @param el
	 * @param name
	 * @return
	 */
	private final static List<Element> getElements(Element el, String name) {
		NodeList nl = el.getChildNodes();
		List<Element> children = new ArrayList<Element>();
		
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE && name.equals(n.getNodeName())) {
				children.add((Element)n);
			}
		}
		
		return children;
	}

	/**
	 * Get properties el
	 * @return
	 * @throws Exception
	 */
	private final static void addProfilesStore(Document rootDoc, Element root) throws Exception {
		DocumentBuilder builder = newDocBuilder();
		Document doc = builder.parse(CorrectAdapterFiles.class.getResourceAsStream("CorrectAdapterFiles.xml"));
		Element el = doc.getDocumentElement();
		
		root.appendChild(rootDoc.importNode(el, true));
		
	}
	
	/**
	 * Parse the adapter file
	 * @param adapterFile
	 * @return
	 * @throws Exception
	 */
	private final static Document toDom(File adapterFile) throws Exception {
		DocumentBuilder builder = newDocBuilder();
		return builder.parse(adapterFile);
	}
	
	/**
	 * Create doc builder
	 * @return
	 * @throws Exception
	 */
	private final static DocumentBuilder newDocBuilder() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setIgnoringComments(false);
		factory.setIgnoringElementContentWhitespace(false);
		factory.setValidating(false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		
		return factory.newDocumentBuilder();
	}
	
	/**
	 * Serialize adapter file
	 * @param adapterFile
	 * @param adapter
	 * @throws Exception
	 */
	private final static void updateAdapterFile(File adapterFile, Document adapter) throws Exception {
		DOMSource source = new DOMSource(adapter);
		StreamResult result = new StreamResult(adapterFile);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer serializer = tf.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");		
		serializer.transform(source, result);
	}
}
