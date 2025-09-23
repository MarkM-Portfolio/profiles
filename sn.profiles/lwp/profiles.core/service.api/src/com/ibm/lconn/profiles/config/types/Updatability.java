/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.types;

public enum Updatability {

  READ("read"), READWRITE("readwrite");

  String value;

  Updatability(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return this.value;
  }

  public String toString()
  {
    return this.value;
  }

  public static Updatability getByValue(String value)
  {
    for (Updatability key : Updatability.values())
    {
      if (key.getValue().equals(value))
      {
        return key;
      }
    }

    return null;
  }

}
