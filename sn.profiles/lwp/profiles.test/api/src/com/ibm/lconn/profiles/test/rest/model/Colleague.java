/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.rest.model;

import javax.xml.namespace.QName;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Person;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class Colleague extends AtomResource {
	
	private String name;
	
	private String userId;
	
	private String email;
	
	private String userState;
	
	public Colleague(String name, String userId, String email, String userState){
		this.name = name;
		this.userId = userId;
		this.email = email;
		this.userState = userState;
	}
	
	public Colleague(Person p) {
		this.name = p.getName();
		this.userId = p.getSimpleExtension(ApiConstants.SocialNetworking.USER_ID);
		this.email = p.getEmail();
		this.userState = p.getSimpleExtension(ApiConstants.SocialNetworking.USER_STATE);
	}
	
	public Colleague validate()  throws Exception {
		AtomResource.assertNotNullOrZeroLength(name);
		AtomResource.assertNotNullOrZeroLength(userId);
		// email may be hidden, validate manually if needed
		AtomResource.assertNotNullOrZeroLength(userState);
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserState() {
		return userState;
	}

	public void setUserState(String userState) {
		this.userState = userState;
	}

	public Element toElement(QName elementName) {
		ExtensibleElement element = ABDERA.getFactory().newExtensionElement(elementName);
		if (getUserId() != null && getUserId().length() > 0) {
			element.addSimpleExtension(ApiConstants.SocialNetworking.USER_ID, getUserId());
		}		
		if (getEmail() != null && getEmail().length() > 0) {
			element.addSimpleExtension(ApiConstants.Atom.QN_EMAIL, getEmail());
		}
		return element;
	}
}
