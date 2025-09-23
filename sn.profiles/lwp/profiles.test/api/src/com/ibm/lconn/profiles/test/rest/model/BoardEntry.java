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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Person;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class BoardEntry extends AtomEntry
{

  private String lastModifier;

  private String lastModifierEmail;

  private String lastModifierUserId;

  private String summary;
  
  private String content;

  private Set<String> tags;

  private String key;
  
  private List<CommentEntry> comments;

  public BoardEntry(){
	  super();
  }
  public BoardEntry(Entry e) throws Exception
  {
    super(e);
    Assert.assertTrue(e.getContributors().size() == 1);
    Person person = e.getContributors().get(0);
    lastModifier = person.getName();
    lastModifierEmail = person.getEmail();
    lastModifierUserId = person.getSimpleExtension(ApiConstants.SocialNetworking.USER_ID);
    Assert.assertTrue(e.getCategories(ApiConstants.SocialNetworking.SCHEME_TYPE).size() > 0);
    Assert.assertEquals(e.getCategories(ApiConstants.SocialNetworking.SCHEME_TYPE).get(0).getTerm(), ApiConstants.SocialNetworking.TERM_ENTRY);
    if (e.getExtension(ApiConstants.SocialNetworking.COMMENTS)!= null){
    	comments = new ArrayList<CommentEntry> ();
    	for(Element el : e.getExtension(ApiConstants.SocialNetworking.COMMENTS).getElements()){
    		
    		comments.add(new CommentEntry((Entry) el));
    	}
    }
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

  public BoardEntry validate() throws Exception
  {
    super.validate();
    Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_RELATED));
    Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_ALTERNATE));
    assertNotNullOrZeroLength(getKey());
    assertNotNullOrZeroLength(getLastModifierUserId());
    
    return this;
  }
  
  
  public String getLastModifier()
  {
    return lastModifier;
  }

  public void setLastModifier(String name)
  {
    this.lastModifier = name;
  }

  public String getLastModifierEmail()
  {
    return lastModifierEmail;
  }

  public void setLastModifierEmail(String email)
  {
    this.lastModifierEmail = email;
  }

  public String getLastModifierUserId()
  {
    return lastModifierUserId;
  }

  public void setLastModifierUserId(String userId)
  {
    this.lastModifierUserId = userId;
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
  public List<CommentEntry> getComments(){
	  return comments;
  }
  public void setComments(List<CommentEntry> c){
	  comments=c;
  }
  public Entry toEntry() throws Exception{
    Entry entry = ABDERA.newEntry();
    entry.addCategory(ApiConstants.SocialNetworking.SCHEME_MESSAGE_TYPE, "simpleEntry", null);
    entry.addCategory(ApiConstants.SocialNetworking.SCHEME_TYPE, ApiConstants.SocialNetworking.TERM_ENTRY, null);
    entry.setContent(content);
    return entry;
  }

}
