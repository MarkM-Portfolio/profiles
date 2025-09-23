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

package com.ibm.lconn.profiles.population.tool;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.Field;

/**
 * 
 */
public class FileRegistrySource extends Source implements Constants
{

  class FileRegistryHandler extends DefaultHandler
  {

    private StringBuilder bodyText;

    private Attributes curAttributes;

    private Map<String, String> currentPerson;

    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
    {
      QName currentElement = new QName(uri, localName, "");
      bodyText = new StringBuilder();
      curAttributes = attributes;

      if (isEqual(currentElement, ENTITIES))
      {
        String type = curAttributes.getValue(TYPE.getNamespaceURI(), TYPE.getLocalPart());
        if (FileRegistrySource.PERSON_ACCOUNT.equals(type))
        {
          currentPerson = new HashMap<String, String>(5);
        }
      }
      else if (currentPerson != null && isEqual(currentElement, IDENTIFIER))
      {
        String externalId = curAttributes.getValue("", "externalId");
        String externalName = curAttributes.getValue("", "externalName");
        if (externalId != null && externalName != null)
        {
          currentPerson.put("externalId", externalId);
          currentPerson.put("externalName", externalName);
        }
      }      
    }

    public void endElement(String uri, String localName, String name) throws SAXException
    {
      QName currentElement = new QName(uri, localName, "");
      if (currentPerson != null && isEqual(currentElement, UID))
      {
        currentPerson.put("uid", normalize(bodyText));
      }
      else if (currentPerson != null && isEqual(currentElement, CN))
      {
        currentPerson.put("cn", normalize(bodyText));
      }
      else if (currentPerson != null && isEqual(currentElement, SN))
      {
        currentPerson.put("sn", normalize(bodyText));
      }
      else if (currentPerson != null && isEqual(currentElement, MAIL))
      {
        currentPerson.put("mail", normalize(bodyText));
      }
      else if (currentPerson != null && isEqual(currentElement, ENTITIES))
      {
        String externalName = currentPerson.get("externalName");
        String externalId = currentPerson.get("externalId");
        String sn = currentPerson.get("sn");
        String uid = currentPerson.get("uid");
        String cn = currentPerson.get("cn");
        String mail = currentPerson.get("mail");

        ProfileEntry profileEntry = new ProfileEntry(externalName, externalId, sn, uid);
        profileEntry.getProfileFields().put(Field.DISPLAY_NAME, cn);
        profileEntry.getProfileFields().put(Field.EMAIL, mail);

        // do the work!
        handleProfileEntry(profileEntry);

        // reset
        currentPerson = null;
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
      bodyText.append(ch, start, length);
    }

    private boolean isEqual(QName q1, QName q2)
    {
      String ns1 = q1.getNamespaceURI();
      String ns2 = q2.getNamespaceURI();
      String lp1 = q1.getLocalPart();
      String lp2 = q2.getLocalPart();
      return ns1.equals(ns2) && lp1.equals(lp2);
    }

    private String normalize(StringBuilder sb)
    {
      return sb.toString().trim();
    }

  }

  public FileRegistrySource(File file, TaskManager taskManager)
  {
    super(file, taskManager);
  }

  public void process() throws Exception
  {
    FileRegistryHandler handler = new FileRegistryHandler();
    SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    parserFactory.setNamespaceAware(true);
    SAXParser parser = parserFactory.newSAXParser();
    parser.parse(file, handler);
  }

}
