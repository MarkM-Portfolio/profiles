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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants.SeedlistConstants;

public class SeedlistEntry extends AtomEntry {

	private Map<String, List<String>> fields;

	private String action;

	private List<String> acls;

	public SeedlistEntry(Entry e) throws Exception {
		super(e);
		ExtensibleElement actionElement = e.getExtension(SeedlistConstants.ACTION);
		if (actionElement != null) {
			action = actionElement.getAttributeValue(SeedlistConstants.ATTR_DO);
		}
		
		acls = new ArrayList<String>(1);
		ExtensibleElement aclsElement = e.getExtension(SeedlistConstants.ACLS);
		if (aclsElement != null) {
			for (Element aclElement : aclsElement.getExtensions(SeedlistConstants.ACL)) {
				acls.add(aclElement.getText());
			}
		}
		fields = new HashMap<String, List<String>>();
		for (Element fieldElement : e.getExtensions(SeedlistConstants.FIELD)) {
			String fieldId = fieldElement.getAttributeValue(SeedlistConstants.ID);
			String fieldValue = fieldElement.getText();

			List<String> fieldValues = fields.get(fieldId);
			if (fieldValues == null) {
				fieldValues = new ArrayList<String>(1);
				fields.put(fieldId, fieldValues);
			}
			fieldValues.add(fieldValue);
		}
	}

	public String getAction()
	{
		return action;
	}
	
	public List<String> getAcls()
	{
		return acls;
	}
	
	public String getFieldValue(String fieldId) {
		String result = null;
		List<String> results = getFieldValues(fieldId);
		if (results != null && results.size() > 0) {
			result = results.get(0);
		}
		return result;
	}

	public List<String> getFieldValues(String fieldId) {
		return fields.get(fieldId);
	}
}
