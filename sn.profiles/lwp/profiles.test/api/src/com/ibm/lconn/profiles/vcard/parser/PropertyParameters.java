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

package com.ibm.lconn.profiles.vcard.parser;

import java.util.HashMap;
import java.util.Map;

public class PropertyParameters
{

  private String encoding;

  private Map<String, String> values;

  public PropertyParameters()
  {
    values = new HashMap<String, String>(3);
  }

  public void setEncoding(String encoding)
  {
    this.encoding = encoding;
  }

  public String getEncoding()
  {
    return encoding;
  }

  public Map<String, String> getValues()
  {
    return values;
  }
  
  public String toString()
  {
    return values.toString();
  }
}
