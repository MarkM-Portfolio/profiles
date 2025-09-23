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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.FieldType;
import com.ibm.lconn.profiles.test.rest.model.VCardProperty;
import com.ibm.lconn.profiles.vcard.parser.ParseException;
import com.ibm.lconn.profiles.vcard.parser.PropertyParameters;
import com.ibm.lconn.profiles.vcard.parser.VCardParserListener;

public class VCardParserListenerImpl implements VCardParserListener
{

  private ProfileEntry profileEntry;

  public VCardParserListenerImpl(ProfileEntry profileEntry)
  {
    this.profileEntry = profileEntry;
  }

  public void setProperty(String propertyName, PropertyParameters parameters, String propertyValue) throws ParseException
  {
    handleProperty(propertyName, parameters, propertyValue);
  }

  public void setExtension(String propertyName, PropertyParameters parameters, String propertyValue) throws ParseException
  {
    handleProperty(propertyName, parameters, propertyValue);
  }

  private void handleProperty(String propertyName, PropertyParameters parameters, String propertyValue) throws ParseException
  {
    // look to see if this is a property we care about
    VCardProperty vCardProperty = VCardProperty.lookup(propertyName, parameters.getValues().keySet());
    Field profileField = ProfileEntry.VCARD_TO_FIELD.get(vCardProperty);
    if (profileField != null)
    {
      // if the field is a string type, we can just set the string
      if (FieldType.STRING.equals(profileField.getType()))
      {
        profileEntry.getProfileFields().put(profileField, propertyValue);
      }
      else if (FieldType.TIMESTAMP.equals(profileField.getType()))
      {
        // the field is a time stamp, so we need to convert format
        try
        {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'S'Z'");
          sdf.getCalendar().setTimeZone(TimeZone.getTimeZone("GMT"));
          Date date = sdf.parse(propertyValue);
          profileEntry.getProfileFields().put(profileField, new Timestamp(date.getTime()));
        }
        catch (java.text.ParseException e)
        {
          throw new ParseException(e.getMessage());
        }
      }
    }
    // look to handle the X_EXTENSION value
  }
}
