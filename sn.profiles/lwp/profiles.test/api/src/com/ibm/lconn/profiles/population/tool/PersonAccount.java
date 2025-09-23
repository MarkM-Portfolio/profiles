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

public class PersonAccount
{

  private String uid;

  private String cn;

  private String sn;

  private String saasOrgId;

  private String saasUserAccountId;

  private String mail;

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
  }

  public String getCn()
  {
    return cn;
  }

  public void setCn(String cn)
  {
    this.cn = cn;
  }

  public String getSn()
  {
    return sn;
  }

  public void setSn(String sn)
  {
    this.sn = sn;
  }

  public String getSaasOrgId()
  {
    return saasOrgId;
  }

  public void setSaasOrgId(String saasOrgId)
  {
    this.saasOrgId = saasOrgId;
  }

  public String getMail()
  {
    return mail;
  }

  public void setMail(String mail)
  {
    this.mail = mail;
  }

  public String getSaasUserAccountId()
  {
    return saasUserAccountId;
  }

  public void setSaasUserAccountId(String saasUserAccountId)
  {
    this.saasUserAccountId = saasUserAccountId;
  }

}
