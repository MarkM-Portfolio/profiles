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

import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

/**
 * Attempts to create the specified profile
 */
public class CreateProfileTask extends Task
{

  public CreateProfileTask() throws Exception
  {
    super();
  }

  @Override
  public void doTask(ProfileEntry profileEntry) throws Exception
  {
    if (profileEntry == null)
      return;

    String uid = (String) profileEntry.getProfileFields().get(Field.UID);
    if (getProfileByUid(uid) == null)
    {
      try
      {
        adminTransport.doAtomPost(null, urlBuilder.getProfilesAdminProfilesUrl(), profileEntry.toEntryXml(), NO_HEADERS,
            HTTPResponseValidator.IGNORE);
        System.out.println("Success: Create profile with uid=" + uid);
      }
      catch (Exception e)
      {
        System.out.println("Error: Unable to create profile for uid=" + uid);
      }
    }
    else
    {
      System.out.println("Ignored: profile exists already with uid=" + uid);
    }
  }

}
