/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.model;

/**
 * The primitive type for a field
 */
public enum FieldType {

  STRING("string"), TIMESTAMP("timestamp");

  private String value;

  private FieldType(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }
}
