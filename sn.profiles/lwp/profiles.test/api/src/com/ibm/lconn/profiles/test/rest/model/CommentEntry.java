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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Person;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class CommentEntry extends AtomEntry
{

  private String name;

  private String email;

  private String userId;

  private String summary;
  
  private String content;

  private Set<String> tags;

  private String key;

  public CommentEntry(){
	  super();
  }
  
  public CommentEntry(Entry e) throws Exception
  {
    super(e);
    /*Assert.assertTrue(e.getContributors().size() == 1);
    Person person = e.getContributors().get(0);
    name = person.getName();
    email = person.getEmail();
    userId = person.getSimpleExtension(ApiConstants.SocialNetworking.USER_ID);*/
    Assert.assertTrue(e.getCategories(ApiConstants.SocialNetworking.SCHEME_TYPE).size() > 0);
    Assert.assertEquals(e.getCategories(ApiConstants.SocialNetworking.SCHEME_TYPE).get(0).getTerm(), ApiConstants.SocialNetworking.TERM_COMMENT);
    
    summary = e.getSummary();
    content = e.getContent();
    tags = new HashSet<String>(3);
    for (Category c : e.getCategories())
    {
      if (c.getScheme() == null)
      {
        tags.add(c.getTerm());
      }
    }

  }

  public CommentEntry validate() throws Exception
  {
    super.validate();
    Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_RELATED));
    Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_ALTERNATE));
    assertNotNullOrZeroLength(getKey());
    assertNotNullOrZeroLength(getUserId());
    
    return this;
  }
  
  
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public String getSummary()
  {
    return summary;
  }

  public void setSummary(String summary)
  {
    this.summary = summary;
  }

  public Set<String> getTags()
  {
    return tags;
  }

  public void setTags(Set<String> tags)
  {
    this.tags = tags;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }
  public String getContent() {
	return content;
  }
	
  public void setContent(String content) {
	this.content = content;
  }

  public Entry toEntry() throws Exception{
    Entry entry = ABDERA.newEntry();
    entry.addCategory(ApiConstants.SocialNetworking.SCHEME_MESSAGE_TYPE, ApiConstants.SocialNetworking.TERM_SIMPLE_COMMENT, null);
    entry.addCategory(ApiConstants.SocialNetworking.SCHEME_TYPE, ApiConstants.SocialNetworking.TERM_COMMENT, null);
    entry.setContent(content);
    return entry;
  }
}
