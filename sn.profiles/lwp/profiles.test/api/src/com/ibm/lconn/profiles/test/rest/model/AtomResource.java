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

package com.ibm.lconn.profiles.test.rest.model;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Person;

public class AtomResource
{

  public static final Abdera ABDERA;

  protected static final Map<String, String> NS_EXTENSIONS;

  static
  {
    ABDERA = new Abdera();
    NS_EXTENSIONS = ABDERA.getXPath().getDefaultNamespaces();
  }

  public static String adaptForXPath(QName name)
  {
    return name.getPrefix() + ":" + name.getLocalPart();
  }

  public static void assertNotNullOrZeroLength(String value) throws Exception
  {
    Assert.assertNotNull(value);
    Assert.assertTrue(value.length() > 0);
  }

  public static boolean hasEmailAttribute(List<Person> people)
  {

    if (people == null)
    {
      return false;
    }

    for (Person person : people)
    {

      if (person.getEmail() != null)
      {
        return true;
      }
    }

    return false;
  }

  public static String notNullLen0(String value, String defaultValue)
  {
    return (value != null && value.length() > 0) ? value : defaultValue;
  }

  public static boolean hasSchemeAndTerm(List<Category> categories, String scheme, String term)
  {
    for (Category c : categories)
    {
      String termToCompare = c.getTerm();
      if (c.getScheme() != null)
      {
        String schemeToCompare = c.getScheme().toString();

        if (termToCompare.equals(term) && schemeToCompare.equals(scheme))
        {
          return true;
        }
      }
    }
    return false;
  }
}
