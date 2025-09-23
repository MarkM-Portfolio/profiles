/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.model;

import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import junit.framework.Assert;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class RoleEntry extends AtomEntry
{
	String entryId;
	String roleId;
	String created;

	public RoleEntry(Entry e) throws Exception
	{
		super(e);
		entryId  = e.getId().toString();
		/*
			RoleEntry <id xmlns="http://www.w3.org/2005/Atom">role_516</id>
			RoleEntry <created>2014-03-12T19:34:21.067Z</created>
			RoleEntry <role>role_516</role>
			RoleEntry <category xmlns="http://www.w3.org/2005/Atom" term="role" scheme="http://www.ibm.com/xmlns/prod/sn/type"></category>
			RoleFeed : <K,V>: role_516, <entry xmlns="http://www.w3.org/2005/Atom"><id>role_516</id><created xmlns="">2014-03-12T19:34:21.067Z</created><role>role_516</role><category xmlns="http://www.w3.org/2005/Atom" term="role" scheme="http://www.ibm.com/xmlns/prod/sn/type"></category></entry>
		*/
		List<Element> feedElements = e.getElements();
		if ((null != feedElements))
		{
			Iterator<Element> el = feedElements.iterator();
			while (el.hasNext())
			{
				Element item = el.next();

				String name  = item.getQName().getLocalPart();
				String value = item.getText();
				if ("id".equals(name))
					roleId=value;
				if ("role".equals(name))
					roleId=value;
				if ("created".equals(name))
					created = value;

//				List<QName> itemAttributes = item.getAttributes();
//				for (Iterator<QName> iterator = itemAttributes.iterator(); iterator.hasNext();) {
//					QName qName = (QName) iterator.next();
//					System.out.println("RoleEntry attribute " + qName + " " + item.getAttributeValue(qName));
//				}
			}
//			System.out.println("RoleEntry fields    " + roleId + " " + created);
		}
	}

	public String getCreatedDate()
	{
		return created;
	}
	public String getRoleId() throws Exception
	{
		return roleId;
	}

	public String getEntryId() throws Exception
	{
		return entryId;
	}

	public RoleEntry validate() throws Exception
	{
		// not a spec-compliant entry
		// super.validate();
		Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_RELATED));
		assertNotNullOrZeroLength(getRoleId());
		return this;
	}
}
