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
import java.util.Collection;
import junit.framework.Assert;
import org.apache.abdera.model.Element;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class ProfileTypeEntry extends AtomResource {

	private String parentId;

	private String id;

	private Collection<ProfileTypeProperty> properties = new ArrayList<ProfileTypeProperty>();

	public ProfileTypeEntry(Element profileTypeElement) {

		for (Element e : profileTypeElement.getElements()) {

			if (e.getQName().equals(ApiConstants.ProfileTypeConstants.PARENT_ID)) {
				parentId = e.getText();
			}
			else if (e.getQName().equals(ApiConstants.ProfileTypeConstants.ID)) {
				id = e.getText();
			}
			else if (e.getQName().equals(ApiConstants.ProfileTypeConstants.PROPERTY)) {
				properties.add(new ProfileTypeProperty(e));
			}
			else {
				throw new UnsupportedOperationException("Unhandled element: " + e);
			}
		}
	}

	public String toString() {
		boolean isFirstProperty = true;
		StringBuilder sb = new StringBuilder();
		sb.append("parentId: ").append(parentId);
		sb.append(", id: ").append(id);
		sb.append(", properties: [");
		for (ProfileTypeProperty p : properties) {
			if (!isFirstProperty) {
				sb.append(", ");
			}
			else {
				isFirstProperty = false;
			}
			sb.append('{').append(p.toString()).append('}');
		}
		sb.append("]");
		return sb.toString();
	}

	public ProfileTypeEntry validate() throws Exception {
		Assert.assertNotNull(getParentId());
		Assert.assertNotNull(getId());
		for (ProfileTypeProperty p : properties) {
			p.validate();
		}
		return this;
	}

	public String getParentId() {
		return parentId;
	}

	public String getId() {
		return id;
	}

	public Collection<ProfileTypeProperty> getProperties() {
		return properties;
	}

}
