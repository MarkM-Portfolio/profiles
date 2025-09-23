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

/**
 * A wplc:fieldInfo element in the seedlist document.
 * 
 * 	<wplc:fieldInfo id="FIELD_UID" 
 * 		name="UID" description="UID" type="string" 
 * 		contentSearchable="true" fieldSearchable="true" 
 * 		parametric="false" returnable="true" 
 * 		sortable="false" supportsExactMatch="false"/>
 * 
 */
public class SeedlistFieldInfo extends AtomResource {

	private String id;
	
	private String name;
	
	private String description;
	
	private String type;
	
	private boolean contentSearchable;
	
	private boolean fieldSearchable;
	
	private boolean parametric;
	
	private boolean returnable;
	
	private boolean sortable;
	
	private boolean supportsExactMatch;
			
	public SeedlistFieldInfo(Element e) {
		this.id = e.getAttributeValue("id");
		this.name = e.getAttributeValue("name");
		this.description = e.getAttributeValue("description");
		this.type = e.getAttributeValue("type");
		this.contentSearchable = Boolean.parseBoolean(e.getAttributeValue("contentSearchable"));
		this.fieldSearchable = Boolean.parseBoolean(e.getAttributeValue("fieldSearcable"));
		this.parametric = Boolean.parseBoolean(e.getAttributeValue("parametric"));
		this.returnable = Boolean.parseBoolean(e.getAttributeValue("returnable"));
		this.sortable = Boolean.parseBoolean(e.getAttributeValue("sortable"));
		this.supportsExactMatch = Boolean.parseBoolean(e.getAttributeValue("supportsExactMatch"));
	}
	
	public SeedlistFieldInfo validate()  throws Exception {
		AtomResource.assertNotNullOrZeroLength(id);
		AtomResource.assertNotNullOrZeroLength(name);
		AtomResource.assertNotNullOrZeroLength(description);
		AtomResource.assertNotNullOrZeroLength(type);
		return this;
	}

	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getType() {
		return type;
	}

	public boolean isContentSearchable() {
		return contentSearchable;
	}

	public boolean isFieldSearchable() {
		return fieldSearchable;
	}

	public boolean isParametric() {
		return parametric;
	}

	public boolean isReturnable() {
		return returnable;
	}

	public boolean isSortable() {
		return sortable;
	}

	public boolean isSupportsExactMatch() {
		return supportsExactMatch;
	}
	
}
