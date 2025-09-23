/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.cache;

public class CachedReportChainEmployee
{
  private String uid;
  private String managerUid;
  private String displayName;
  private String email;
  private String key;
  
  public String getEmail()
  {
    return email;
  }
  public void setEmail(String email)
  {
    this.email = email;
  }
  /**
   * @return Returns the displayName.
   */
  public String getDisplayName()
  {
    return displayName;
  }
  /**
   * @param displayName The displayName to set.
   */
  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }
  /**
   * @return Returns the managerUid.
   */
  public String getManagerUid()
  {
    return managerUid;
  }
  /**
   * @param managerUid The managerUid to set.
   */
  public void setManagerUid(String managerUid)
  {
    this.managerUid = managerUid;
  }
  /**
   * @return Returns the uid.
   */
  public String getUid()
  {
    return uid;
  }
  /**
   * @param uid The uid to set.
   */
  public void setUid(String uid)
  {
    this.uid = uid;
  }
  /**
   * @return Returns the key.
   */
  public String getKey()
  {
    return key;
  }
  /**
   * @param key The key to set.
   */
  public void setKey(String key)
  {
    this.key = key;
  }
}
