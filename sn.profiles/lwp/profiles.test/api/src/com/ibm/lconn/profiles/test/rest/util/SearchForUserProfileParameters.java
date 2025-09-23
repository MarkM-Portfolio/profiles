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

package com.ibm.lconn.profiles.test.rest.util;

import java.util.Set;

public class SearchForUserProfileParameters extends AbstractParameters
{

  public SearchForUserProfileParameters()
  {
    setFormat(Format.LITE);
    setOutput(Output.HCARD);
  }

  public void setEmail(String value)
  {
    put("email", value);
  }

  public void setFormat(Format value)
  {
    put("format", value.getValue());
  }

  public void setKey(String key)
  {
    put("key", key);
  }

  public void setOutput(Output value)
  {
    put("output", value.getValue());
  }

  public void setUserId(Set<String> values)
  {
    put("userid", delimit(",", values));
  }

}
