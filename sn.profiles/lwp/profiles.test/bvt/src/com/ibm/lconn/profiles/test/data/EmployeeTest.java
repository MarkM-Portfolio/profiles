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
package com.ibm.lconn.profiles.test.data;

import static com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants.EXT_ATTR_KEY_BASE;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class EmployeeTest extends BaseTestCase 
{
	private static final String TIELINE = "tieline";
	
	public void testGetAttributeIdForExtensionId()
	{
		assertEquals(EXT_ATTR_KEY_BASE+TIELINE,Employee.getAttributeIdForExtensionId(TIELINE));
	}
	
	public void testGetExtensionIdForAttributeId()
	{
		assertEquals(TIELINE,Employee.getExtensionIdForAttributeId(Employee.getAttributeIdForExtensionId(TIELINE)));
	}

	public void testIsAttributeIdForProfileExtension()
	{
		assertTrue(Employee.isAttributeIdForProfileExtension(Employee.getAttributeIdForExtensionId(TIELINE)));
		assertFalse(Employee.isAttributeIdForProfileExtension(TIELINE));
	}
	
	public void testGetLConnUserId()
	{
		Employee e = new Employee();
		e.setUid(PeoplePagesServiceConstants.UID);
		e.setGuid(PeoplePagesServiceConstants.GUID);
		
		assertEquals(
				ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLConnUserIdAttrName(),
				e.getUserid());
	}
}
