/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit;

import java.io.StringReader;

import com.ibm.lconn.profiles.vcard.parser.ParseException;
import com.ibm.lconn.profiles.vcard.parser.PropertyParameters;
import com.ibm.lconn.profiles.vcard.parser.VCard21Parser;
import com.ibm.lconn.profiles.vcard.parser.VCardParserListener;

/**
 * Unit Test for VCard parser
 */
public class VCardApiTest extends AbstractTest
{

  class SimpleListener implements VCardParserListener
  {

    public void setProperty(String propertyName, PropertyParameters parameters, String propertyValue) throws ParseException
    {
      System.out.println("propertyName:" + propertyName + ",parameters: " + parameters + ", propertyValue: " + propertyValue);
    }

    public void setExtension(String propertyName, PropertyParameters parameters, String propertyValue) throws ParseException
    {
      System.out.println("extensionName:" + propertyName + ",parameters: " + parameters + ", propertyValue: " + propertyValue);            
    }

  }

  public void testVCardParser() throws Exception
  {
    String content = "\nBEGIN:VCARD\nVERSION:2.1\nPHOTO;VALUE=URL:https://kratos.raleigh.ibm.com:9443/profiles/photo.do?key=ebd1d553-1b10-4b24-9d18-8497bfab5549&lastMod=1318005505843\nFN:Amadou Alain\nHONORIFIC_PREFIX:\nNICKNAME:\nX_PREFERRED_LAST_NAME:\nX_NATIVE_FIRST_NAME:\nX_NATIVE_LAST_NAME:\nX_ALTERNATE_LAST_NAME:\nURL:https://kratos.raleigh.ibm.com:9443/profiles/atom/profile.do?key=ebd1d553-1b10-4b24-9d18-8497bfab5549\nSOUND;VALUE=URL:https://kratos.raleigh.ibm.com:9443/profiles/audio.do?key=ebd1d553-1b10-4b24-9d18-8497bfab5549&lastMod=1318005505843\nEMAIL;INTERNET:aalain@renovations.com\nEMAIL;X_GROUPWARE_MAIL:foobar@email.com\nX_BLOG_URL;VALUE=URL:twitter\nTZ:Etc/GMT+12\nX_PREFERRED_LANGUAGE:\nORG:\nX_ORGANIZATION_CODE:\nROLE:\nX_EMPTYPE:\nTITLE:President\nX_BUILDING:White House\nX_FLOOR:2\nX_OFFICE_NUMBER:12\nTEL;WORK:1\nTEL;CELL:555-555-5555\nTEL;FAX:555-555-5555\nTEL;X_IP:\nTEL;PAGER:555-555-5555\nX_PAGER_ID:\nX_PAGER_TYPE:\nX_PAGER_PROVIDER:\nCATEGORIES:\nX_EXPERIENCE:\nX_DESCRIPTION:\nX_MANAGER_UID:\nX_IS_MANAGER:\nX_PROFILE_KEY:ebd1d553-1b10-4b24-9d18-8497bfab5549\nUID:d1b1d105-447d-4faa-a1a2-214e4b222d03\nX_PROFILE_UID:aalain\nX_LCONN_USERID:d1b1d105-447d-4faa-a1a2-214e4b222d03\nX_EMPLOYEE_NUMBER:\nX_DEPARTMENT_NUMBER:\nX_DEPARTMENT_TITLE:\nX_SHIFT:\nREV:2011-10-07T16:38:25.843Z\nX_PROFILE_TYPE:default\nEND:VCARD\n";    
    StringReader reader = new StringReader(content);
    VCard21Parser parser = new VCard21Parser(reader);
    parser.setListener(new SimpleListener());
    parser.parse();
  }

}
