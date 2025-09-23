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

package com.ibm.lconn.profiles.test.rest.model;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Person;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class AtomEntry extends AtomResource
{

  private final Map<String, String> LINK_HREFS;

  private String title;

  private Date lastModified;

  private Date updated;

  private Date created;

  private String createdBy;
  
  private String createdByEmail;
  
  private String createdByUserId;

  private String atomId;

  public AtomEntry()
  {
    // set to default data
    title = "";
    lastModified = Calendar.getInstance().getTime();
    updated = lastModified;
    created = lastModified;
    createdBy = "";
    createdByEmail = "";
    createdByUserId = "";
    atomId = "";
    LINK_HREFS = new HashMap<String, String>(0);
  }

  public AtomEntry(Entry e) throws Exception
  {
    title = e.getTitle();
    lastModified = e.getEdited();
    updated = e.getUpdated();
    created = e.getPublished();
    createdBy = e.getAuthor() != null ? e.getAuthor().getName() : null;
    createdByEmail = e.getAuthor() != null ? e.getAuthor().getEmail() : null;
    createdByUserId = e.getAuthor() != null ? e.getAuthor().getSimpleExtension(ApiConstants.SocialNetworking.USER_ID) : null;
    atomId = e.getId().toString();

    // get links relations used for navigation
    List<Link> links = e.getExtensions(ApiConstants.Atom.LINK);
    LINK_HREFS = new HashMap<String, String>(links.size());
    for (Link l : links)
    {
      String rel = l.getRel();
      String href = l.getHref().toString();
      LINK_HREFS.put(rel, href);
    }

  }

  public Date getLastModified()
  {
    return lastModified;
  }

  public Date getUpdated()
  {
    return updated;
  }

  public Date getCreated()
  {
    return created;
  }

  public String getCreatedBy()
  {
    return createdBy;
  }

  public String getCreatedByEmail()
  {
    return createdByEmail;
  }
  
  public String getCreatedByUserId()
  {
    return createdByUserId;
  }
  
  public String getAtomId()
  {
    return atomId;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getLinkHref(String rel)
  {
    return LINK_HREFS.get(rel);
  }

  public Map<String, String> getLinkHrefs()
  {
    return Collections.unmodifiableMap(LINK_HREFS);
  }

  public AtomEntry validate() throws Exception
  {
    // TODO per atom spec the following elements should alawys be present
    Assert.assertNotNull(getTitle());
    //Assert.assertNotNull(getLastModified());
    Assert.assertNotNull(getUpdated());
    //Assert.assertNotNull(getCreated());
    //Assert.assertNotNull(getCreatedBy());
    Assert.assertNotNull(getAtomId());
    Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_SELF));
    Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_EDIT));
    return this;
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

  /**
   * Return a new atom entry that represents the current state of this memory-object
   * 
   * @return
   * @throws Exception
   */
  public Entry toEntry() throws Exception
  {
    Entry result = ABDERA.newEntry();
    result.setTitle(title);
    return result;
  }  
}
