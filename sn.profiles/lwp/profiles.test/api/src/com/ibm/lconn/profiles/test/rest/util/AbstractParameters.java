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

package com.ibm.lconn.profiles.test.rest.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for query parameters that are supplied on URI
 */
public abstract class AbstractParameters
{

  private Map<String, String> parameters = new HashMap<String, String>();

  protected void put(String name, String value)
  {
    this.parameters.put(name, value);
  }

  protected String delimit(String delimiter, Set<String> values)
  {
    StringBuffer sb = new StringBuffer();
    for (String value : values)
    {
      if (sb.length() > 0)
      {
        sb.append(delimiter);
      }
      sb.append(value);
    }
    return sb.toString();
  }

  public Map<String, String> getParameters()
  {
    return Collections.unmodifiableMap(parameters);
  }
}
