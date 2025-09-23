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
import java.io.FileOutputStream;
import java.io.OutputStream;

public abstract class DirectoryGeneratorWriter
{

  private static final String UTF8 = "UTF-8";

  private static final String SPACE = " ";

  private static final String NEW_LINE = "\n";

  private static final String IP = "127.0.0.1";

  private static final String LOCALHOST = "localhost";

  private File hostsFile;

  private OutputStream stream;

  public void init(File output, String password) throws Exception
  {
    hostsFile = new File(output, "hosts");
    if (hostsFile.exists())
    {
      hostsFile.delete();
    }
  }

  public void start() throws Exception
  {
    stream = new FileOutputStream(hostsFile);
    writeEntry(LOCALHOST);
  }

  public void end() throws Exception
  {
    stream.close();
  }

  public abstract void writePersonAccount(PersonAccount personAccount);

  private void writeEntry(String url)
  {
    try
    {
      stream.write(IP.getBytes(UTF8));
      stream.write(SPACE.getBytes(UTF8));
      stream.write(url.getBytes(UTF8));
      stream.write(NEW_LINE.getBytes(UTF8));
    }
    catch (Exception e)
    {

    }
  }

  public void writeOrganizationAccount(OrganizationAccount organizationAccount)
  {
    writeEntry(organizationAccount.getUrl());
  }

}
