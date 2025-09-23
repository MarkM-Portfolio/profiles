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

import com.ibm.lconn.profiles.test.rest.model.FieldGroupType;
import com.ibm.lconn.profiles.test.rest.model.FieldType;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

/**
 * 
 * Enum "Fields" is too restrictive to permit testing customizations. Class works similarly, but be careful, it may not be supported in
 * all operations
 * 
 */
public class ExtensionField {

	// ..<content type="application/xml">
	// ....<person xmlns="http://ns.opensocial.org/2008/opensocial">
	// ......<com.ibm.snx_profiles.attrib>
	// ........<entry>
	// ..........<key>com.ibm.snx_profiles.base.uid</key>
	// ..........<value>
	// ............<type>text</type>
	// ............<data>smazar</data>
	// ..........</value>
	// ........</entry>

	private String keyName;

	private Object value;

	private FieldType type = FieldType.STRING;

	private FieldGroupType groupType = FieldGroupType.EXT;

	public ExtensionField(String keyName, Object value) {
		this.keyName = keyName;
		this.value = value;
	}

	public ExtensionField(String keyName, FieldGroupType groupType, Object value) {
		this.keyName = keyName;
		this.groupType = groupType;
		this.value = value;
	}

	public String getKeyName() {
		return keyName;
	}

	public String getFullyQualifiedKeyName() {
		return ApiConstants.SocialNetworking.ATTR_PREFIX + "." + groupType.getValue() + "." + keyName;
	}

	public FieldType getType() {
		return type;
	}

	public FieldGroupType getGroupType() {
		return groupType;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}