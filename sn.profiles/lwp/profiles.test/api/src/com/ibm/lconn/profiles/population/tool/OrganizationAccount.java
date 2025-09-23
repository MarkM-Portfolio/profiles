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

public class OrganizationAccount
{

  private String o;

  private String cn;

  private String url;

  private String saasOrganizationId;
  
  public String getO()
  {
    return o;
  }

  public void setO(String o)
  {
    this.o = o;
  }

  public String getCn()
  {
    return cn;
  }

  public void setCn(String cn)
  {
    this.cn = cn;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getSaasOrganizationId()
  {
    return saasOrganizationId;
  }

  public void setSaasOrganizationId(String saasOrganizationId)
  {
    this.saasOrganizationId = saasOrganizationId;
  }

  
}
