/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.model;

import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.ExtensibleElement;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class LinkRoll extends AtomResource {

	private List<Link> links;

	public LinkRoll() {
		this.links = new ArrayList<Link>(5);
	}

	public LinkRoll(Element e) throws Exception {
		// validate we have the right element
		Assert.assertEquals(ApiConstants.LinkRollConstants.LINK_ROLL, e.getQName());
		// iterate over children and create links
		links = new ArrayList<Link>(e.getElements().size());
		for (Element child : e.getElements()) {
			links.add(new Link(child));
		}
	}

	public List<Link> getLinks() {
		return links;
	}
	
	public LinkRoll validate() throws Exception {
		for (Link link : links) {
			link.validate();
		}
		return this;
	}

	public Element toElement() throws Exception {
		ExtensibleElement linkRollElement = ABDERA.getFactory().newElement(ApiConstants.LinkRollConstants.LINK_ROLL);
		for (Link link : links) {
			linkRollElement.addExtension(link.toElement());
		}
		return linkRollElement;
	}

}
