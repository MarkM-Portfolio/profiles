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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;

import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

public class PhotoUploadTask extends Task
{

  private Set<String> pictureUids;

  private File imageFolder;

  public PhotoUploadTask(File imageFolder) throws Exception
  {
    super();
    this.imageFolder = imageFolder;

    // figure out the uids of pictures that we have that can get populated
    String[] files = imageFolder.list();
    pictureUids = new HashSet<String>(files.length);
    for (int i = 0; i < files.length; i++)
    {
      String uid = files[i].substring(0, files[i].length() - 4);
      pictureUids.add(uid);
    }

  }

  @Override
  public void doTask(ProfileEntry profileEntry) throws Exception
  {
    if (profileEntry == null)
      return;

    // in a MT environment, UID will have form
    // <uidPrefix>.<tenantInfo>.<uniqueCounterInTenant>
    String uid = (String) profileEntry.getProfileFields().get(Field.UID);
    String pictureUid = uid.indexOf(".") > 0 ? uid.substring(0, uid.indexOf(".")) : uid;
    // if we have their picture, upload it!
    if (pictureUids.contains(pictureUid))
    {
      try
      {
        // fetch profile from server
        ProfileEntry latestEntry = getProfileByUid(uid);
        if (latestEntry != null)
        {
          try
          {
            String imageUrl = latestEntry.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
            File pictureFile = new File(imageFolder, pictureUid + ".jpg");
            PutMethod putMethod = new PutMethod(imageUrl);
            FileRequestEntity entity = new FileRequestEntity(pictureFile, "image/jpeg");
            putMethod.setRequestEntity(entity);
            adminTransport.doHttpPutMethod(null, putMethod, NO_HEADERS, HTTPResponseValidator.OK);
          }
          catch (Exception e)
          {
            System.out.println("Error: unable to upload photo for user with uid:" + uid);
          }
        }
        else
        {
          System.out.println("Error: Unable to upload a photo since profile with uid=" + uid + " does not exist");
        }
      }
      catch (Exception e)
      {
        System.out.println("Error: unable to upload photo for user with uid=" + uid);
      }
    }

  }
}
