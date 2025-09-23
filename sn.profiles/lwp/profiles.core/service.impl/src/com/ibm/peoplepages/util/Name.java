/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.util;

public class Name
{
  private String firstName;
  private String lastName;
  
  public Name(String first, String last)
  {
    firstName = first;
    lastName = last;
  }
  /**
   * @return Returns the firstName.
   */
  public String getFirstName()
  {
    return firstName;
  }
  /**
   * @param firstName The firstName to set.
   */
  public void setFirstName(String firstName)
  {
    this.firstName = firstName;
  }
  /**
   * @return Returns the lastName.
   */
  public String getLastName()
  {
    return lastName;
  }
  /**
   * @param lastName The lastName to set.
   */
  public void setLastName(String lastName)
  {
    this.lastName = lastName;
  }

}
