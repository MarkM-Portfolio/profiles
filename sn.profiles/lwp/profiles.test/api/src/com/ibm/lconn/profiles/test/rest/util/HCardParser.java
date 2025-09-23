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

import java.io.ByteArrayInputStream;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.abdera.parser.ParseException;
import org.xml.sax.InputSource;

import com.ibm.lconn.profiles.test.rest.model.Field;

public class HCardParser
{

  private HCardParser()
  {
  }

  public static Map<Field, Object> parseHCard(String hCard) throws ParseException
  {
    if (hCard == null || hCard.length() < 0)
      throw new ParseException("Invalid hCard");

    try
    {
      // do a simple sax parse, not concerned about performance in building parsers here
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      SAXParser parser = factory.newSAXParser();
      HCardHandler handler = new HCardHandler();
      parser.parse(new InputSource(new ByteArrayInputStream(hCard.getBytes("utf-8"))), handler);
      return handler.getProfileFields();
    }
    catch (Exception e)
    {
      throw new ParseException(e);
    }
  }

}
