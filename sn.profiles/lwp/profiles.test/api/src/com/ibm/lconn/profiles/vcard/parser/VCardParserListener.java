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

public interface VCardParserListener
{

  public void setProperty(String propertyName, PropertyParameters parameters, String propertyValue) throws ParseException;
  
  public void setExtension(String propertyName, PropertyParameters parameters, String propertyValue) throws ParseException;
  
}
