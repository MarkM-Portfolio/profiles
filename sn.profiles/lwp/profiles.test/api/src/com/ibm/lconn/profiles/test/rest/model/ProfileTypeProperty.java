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

public class ProfileTypeProperty extends AtomResource {

	private String ref;

	private String updatability;

	private boolean hidden;

	public ProfileTypeProperty(Element profileTypePropertyElement) {

		for (Element f : profileTypePropertyElement.getElements()) {
			if (f.getQName().equals(ApiConstants.ProfileTypeConstants.REF)) {
				ref = f.getText();
			}
			else if (f.getQName().equals(ApiConstants.ProfileTypeConstants.UPDATABILITY)) {
				updatability = f.getText();
			}
			else if (f.getQName().equals(ApiConstants.ProfileTypeConstants.HIDDEN)) {
				hidden = Boolean.getBoolean(f.getText());
			}
			else {
				throw new UnsupportedOperationException("Unhandled element: " + f);
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ref: ").append(ref);
		sb.append(", updatability: ").append(updatability);
		sb.append(", hidden: ").append(hidden);
		return sb.toString();
	}

	public ProfileTypeProperty validate() throws Exception {
		Assert.assertNotNull(getRef());
		Assert.assertNotNull(getUpdatability());
		return this;
	}

	public String getRef() {
		return ref;
	}

	public String getUpdatability() {
		return updatability;
	}

	public boolean getHidden() {
		return hidden;
	}

}
