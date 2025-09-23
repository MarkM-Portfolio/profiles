/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.misc;

import com.ibm.lconn.profiles.internal.util.NameHelper;
import com.ibm.lconn.profiles.test.BaseTestCase;
import com.ibm.peoplepages.data.Employee;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */
public class NameHelperTest extends BaseTestCase 
{
	
	public void testNameAlias() {
			
		// For empty display name, we should get back empty
		Employee emp = new Employee();
		String name = NameHelper.getNameToDisplay(emp);
		assertTrue( name.length() == 0 );
		
		// Set the display name, but no preferred first name
		String origDisplayName = "Joseph Z Lu";
		emp.setDisplayName( origDisplayName );
		name = NameHelper.getNameToDisplay(emp);
		assertTrue( name.equals( origDisplayName ));
		
		// Set the preferredFistName but no surname
		String firstName = "Joe";
		emp.setPreferredFirstName( firstName );
		name = NameHelper.getNameToDisplay( emp );
		assertTrue ( name.equals("Joseph Z Lu (Joe)"));
		
		// Set the preferredFistName with surname
		emp.setSurname("lu");
		name = NameHelper.getNameToDisplay( emp );
		assertTrue ( name.equals("Joseph Z (Joe) Lu"));
		
		// In the case when there are two words in the last name
		emp.setDisplayName("Joseph Z Lu Liu");
		emp.setSurname("lu liu");
		name = NameHelper.getNameToDisplay( emp );
		assertTrue ( name.equals("Joseph Z (Joe) Lu Liu") );
		
		// If Display name already contains the alias
		emp.setDisplayName("Joe Lu");
		emp.setSurname("lu");
		name = NameHelper.getNameToDisplay( emp );
		assertTrue ( name.equals("Joe Lu") );
		
		// If Display name already contains the alias
		emp.setDisplayName("Joseph (Joe) Lu");
		emp.setSurname("lu");
		name = NameHelper.getNameToDisplay( emp );
		assertTrue ( name.equals("Joseph (Joe) Lu") );
		
		// If Display name already contains the alias
		emp.setDisplayName("Lu, Joseph (Joe)");
		emp.setSurname("lu");
		name = NameHelper.getNameToDisplay( emp );
		assertTrue ( name.equals("Lu, Joseph (Joe)") );
		
		// If Display name already contains the alias anywhere
		emp.setDisplayName("Lu, Joseph(Joe)");
		emp.setSurname("lu");
		name = NameHelper.getNameToDisplay( emp );
		assertTrue ( name.equals("Lu, Joseph(Joe)") );	
		
		// Even though 'Joey' starts with alias 'Joe', we still insert the alias
		emp.setDisplayName("Joey Lu");
		emp.setSurname("lu");
		name = NameHelper.getNameToDisplay( emp );
		assertTrue ( name.equals("Joey (Joe) Lu") );
			
	}
}
