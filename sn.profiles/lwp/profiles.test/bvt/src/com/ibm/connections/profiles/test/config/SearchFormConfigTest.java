/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.connections.profiles.test.config;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.ui.UIAttributeConfig;
import com.ibm.lconn.profiles.config.ui.UISearchFormConfig;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.data.Employee;

/**
 * 
 * @author adebiyi@us.ibm.com
 *
 */
public class SearchFormConfigTest extends BaseTestCase 
{
	static ProfilesConfig config = ProfilesConfig.instance();
	
//	static String COLUMN = "column";


	public void testGetElements() 
	{
		UISearchFormConfig layout = config.getSFormLayoutConfig();
			
			assertNotNull(layout);
		
	}
	
	public void testLayoutElementsOrder_and_r_valid()
	{
		UISearchFormConfig layout = config.getSFormLayoutConfig();
		
		assertNotNull(layout);
		
		String[] atts = 
		{
				"displayName",
				"preferredFirstName",
				"preferredLastName",
				"tags",
				"jobResp",			
				"organizationTitle",
				"workLocation.city",
				"countryDisplayValue",
				"email",
				"telephoneNumber",
				Employee.getAttributeIdForExtensionId("spokenLanguages"),
		};
		
		int index = 0;
		for (UIAttributeConfig attribute : layout.getAttributes())
		{
			assertEquals(atts[index],attribute.getAttributeId());
			evaluateUiAttr(attribute);
			index++;
		}		
	}
	
	private void evaluateUiAttr(UIAttributeConfig attribute) 
	{
		String nn = attribute.getNodeName();
		
		if ("attribute".equals(nn))
		{
			evaluateAttr(attribute);
		}
		else if ("editableAttribute".equals(nn))
		{
			evaluateEditAttr(attribute);
		}
		else if ("extensionAttribute".equals(nn))
		{
			evaluateExtAttr(attribute);
		}
		else
		{
			fail("Unexpected nodetype: " + nn);
		}
	}


	private void evaluateAttr(UIAttributeConfig a) 
	{
		HierarchicalConfiguration c = a.getConfiguration();
		
		assertEquals(c.getRoot().getName(),a.getNodeName());
		assertEquals(c.getBoolean("[@showLabel]"),a.getIsShowLabel());
		assertEquals(c.getRoot().getValue(),a.getAttributeId());
		assertFalse(a.isEditable());
		assertFalse(a.isExtensionAttribute());
	}


	private void evaluateEditAttr(UIAttributeConfig a) 
	{
		HierarchicalConfiguration c = a.getConfiguration();
		
		assertEquals(c.getRoot().getName(),a.getNodeName());
		assertEquals(c.getBoolean("[@showLabel]"),a.getIsShowLabel());
		assertEquals(c.getRoot().getValue(),a.getAttributeId());
		assertTrue(a.isEditable());
		assertFalse(a.isExtensionAttribute());
	}


	private void evaluateExtAttr(UIAttributeConfig a) 
	{
		HierarchicalConfiguration c = a.getConfiguration();
		
		assertEquals(c.getRoot().getName(),a.getNodeName());
		assertEquals(c.getBoolean("[@showLabel]"),a.getIsShowLabel());
		assertEquals(c.getString("[@extensionIdRef]"),Employee.getExtensionIdForAttributeId(a.getAttributeId()));
		assertEquals(c.getBoolean("[@editable]",false),a.isEditable());
		assertTrue(a.isExtensionAttribute());
	}

}
