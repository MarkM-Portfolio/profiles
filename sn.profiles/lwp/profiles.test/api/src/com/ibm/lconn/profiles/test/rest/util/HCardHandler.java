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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.FieldType;
import com.ibm.lconn.profiles.test.rest.model.VCardProperty;

/**
 * A simple handler for parsing the hCard format
 */
public class HCardHandler extends DefaultHandler
{

  class CSSEntry
  {

    private List<String> classValues;

    public CSSEntry(String value)
    {
      classValues = new ArrayList<String>();

      if (value != null && value.length() > 0)
      {
        StringTokenizer st = new StringTokenizer(value, " ");
        while (st.hasMoreTokens())
        {
          classValues.add(st.nextToken());
        }
      }
    }
    
    public List<String> getClassValues()
    {
      return classValues;
    }
  }

  // the set of css values active in the current parse scope
  private Stack<CSSEntry> cssStack;

  // the set of attributes active in the current parse scope
  private Stack<Attributes> attributesStack;
  
  // the parsed profile field values
  private Map<Field, Object> profileFields;

  // the current element processed body text
  private StringBuilder bodyText;

  // the current set of attributes in the processed element
  private Attributes curAttributes;

  private Set<String> classNames;

  @Override
  public void startDocument() throws SAXException
  {
    profileFields = new HashMap<Field, Object>();
    cssStack = new Stack<CSSEntry>();
    attributesStack = new Stack<Attributes>();
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException
  {
    bodyText.append(ch, start, length);
  }

  @Override
  public void endElement(String uri, String localName, String name) throws SAXException
  {
    Set<String> activeClassValues = buildActiveClassValues();    
    for (VCardProperty vCard : VCardProperty.values())
    {
      // we have a match!
      if (activeClassValues.containsAll(vCard.getCssValues()))
      {
        Field fieldKey = ProfileEntry.VCARD_TO_FIELD.get(vCard);
        if (fieldKey != null)
        {
          if (FieldType.STRING.equals(fieldKey.getType()))
          {
            // TODO in some cases, the value is an attribute and not the body text, fix-up as needed            
            profileFields.put(fieldKey, normalize(bodyText));            
          }
        }         
      }
    }
    
    cssStack.pop();
    attributesStack.pop();
  }

  @Override
  public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
  {
    attributesStack.push(attributes);
    
    String classValue = attributes.getValue("", "class");
    CSSEntry cssEntry = new CSSEntry(classValue);
    cssStack.push(cssEntry);
    
    bodyText = new StringBuilder();
  }

  private Set<String> buildActiveClassValues()
  {
    Set<String> values = new HashSet<String>();
    for (CSSEntry e : cssStack)
    {
      List<String> classValues = e.getClassValues();
      for (String value : classValues)
      {
        values.add(value);
      }
    }
    return values;
  }
      
  private String normalize(StringBuilder sb)
  {
    return sb.toString().trim();
  }

  public Map<Field, Object> getProfileFields()
  {
    return profileFields;
  }

}
