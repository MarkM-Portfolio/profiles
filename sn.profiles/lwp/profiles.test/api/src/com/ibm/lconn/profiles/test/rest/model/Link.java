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

import junit.framework.Assert;
import org.apache.abdera.model.Element;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

/**
 * An individual link in the link roll
 */
public class Link extends AtomResource {

	public Link(Element e) throws Exception {
		name = e.getAttributeValue(ApiConstants.LinkRollConstants.ATTR_NAME);
		url = e.getAttributeValue(ApiConstants.LinkRollConstants.ATTR_URL);
		validate();
	}

	public Link(String name, String url) {
		this.name = name;
		this.url = url;
	}

	private String name;

	private String url;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("name: ").append(name).append(", url: ").append(url);
		return sb.toString();
	}

	public Link validate() throws Exception {
		Assert.assertNotNull("link name must not be null", getName());
		Assert.assertNotNull("link url must not be null", getUrl());
		Assert.assertTrue("link name must be greater than length 0", getName().length() > 0);
		Assert.assertTrue("link url must be greater than length 0", getUrl().length() > 0);
		return this;
	}

	public Element toElement() throws Exception {
		Element linkElement = ABDERA.getFactory().newElement(ApiConstants.LinkRollConstants.LINK);
		linkElement.setAttributeValue(ApiConstants.LinkRollConstants.ATTR_NAME, getName());
		linkElement.setAttributeValue(ApiConstants.LinkRollConstants.ATTR_URL, getUrl());
		return linkElement;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		Link toCompare = (Link) o;		
		boolean result = name.equals(toCompare.name);
		result = result && url.equals(toCompare.url);
		return result;
	}

}
