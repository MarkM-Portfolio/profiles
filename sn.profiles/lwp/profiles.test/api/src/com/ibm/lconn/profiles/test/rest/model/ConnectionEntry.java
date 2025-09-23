/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.model;

import junit.framework.Assert;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Person;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class ConnectionEntry extends AtomEntry
{

  private Colleague author;

  private Colleague lastModifier;

  private Colleague source;

  private Colleague target;

  /**
   * not certain if summary is used in a Connection (it is written out by AtomGenerator2)
   */
  private String summary;

  private STATUS status;

  private String content;

  private String connectionType;
  
  /**
   * 
   * see <code>com.ibm.peoplepages.data.Connection.StatusType</code>
   * 
   */
  public enum STATUS {
      unconfirmed, accepted, pending;
  }
  
  /**
   * 
   * see <code>com.ibm.lconn.profiles.api.actions.AdminConnectionsAction.Action</code>
   *
   */
  public enum ACTION {
      complete, invite, accept, reject;
  }

  public ConnectionEntry()
  {
    
  }
  
  public ConnectionEntry(Entry e) throws Exception {
      super(e);
      author = new Colleague(e.getAuthor());
      Assert.assertTrue(e.getContributors().size() == 1);
      lastModifier = new Colleague(e.getContributors().get(0));
      
      Assert.assertTrue(e.getCategories(ApiConstants.SocialNetworking.SCHEME_TYPE).size() == 1);
      Assert.assertEquals(ApiConstants.SocialNetworking.TERM_CONNECTION, e.getCategories(ApiConstants.SocialNetworking.SCHEME_TYPE)
              .get(0).getTerm());
      Assert.assertTrue(e.getCategories(ApiConstants.SocialNetworking.SCHEME_CONNECTION_TYPE).size() == 1);
      connectionType = e.getCategories(ApiConstants.SocialNetworking.SCHEME_CONNECTION_TYPE).get(0).getTerm();
      
      Assert.assertTrue(e.getCategories(ApiConstants.SocialNetworking.SCHEME_STATUS).size() == 1);
      String statusString = e.getCategories(ApiConstants.SocialNetworking.SCHEME_STATUS).get(0).getTerm();
      status = STATUS.valueOf(statusString);
      content = e.getContent();

      Element connection = e.getExtension(ApiConstants.SocialNetworking.QN_CONNECTION);
      Person person = null;

      if (connection != null) {
          person = connection.getFirstChild(ApiConstants.Atom.QN_CONTRIBUTOR);
          if (person != null) {
              do {
                  String snxRel = person.getAttributeValue(ApiConstants.SocialNetworking.QN_SNX_REL);
                  if (ApiConstants.SocialNetworking.SNX_REL_SOURCE.equals(snxRel)) {
                      source = new Colleague(person);
                  }
                  else if (ApiConstants.SocialNetworking.SNX_REL_TARGET.equals(snxRel)) {
                      target = new Colleague(person);
                  }
              }
              while ((person = person.getNextSibling(ApiConstants.Atom.QN_CONTRIBUTOR)) != null);
          }
      }
  }

  public ConnectionEntry validate() throws Exception {
      super.validate();
      author.validate();
      lastModifier.validate();
      source.validate();
      target.validate();
      Assert.assertNotNull(status);
      Assert.assertNotNull(connectionType);
      return this;
  }

  private Element colleagueToElement(Colleague c, String relType) {
	  Element person = c.toElement(ApiConstants.Atom.QN_CONTRIBUTOR);
	  person.setAttributeValue(ApiConstants.SocialNetworking.QN_SNX_REL, relType);
	  return person;
  }
  
  /**
   * Return a new atom entry that represents the current state of this memory-object
   * 
   * @return
   * @throws Exception
   */
  public Entry toEntry() throws Exception {
      Entry result = super.toEntry();
      result.declareNS(ApiConstants.SocialNetworking.NS_URI, ApiConstants.SocialNetworking.NS_PREFIX);
      result.declareNS(ApiConstants.Atom.NS_URI, ApiConstants.Atom.NS_PREFIX);
      result.addCategory(ApiConstants.SocialNetworking.SCHEME_TYPE, ApiConstants.SocialNetworking.TERM_CONNECTION, null);
      result.addCategory(ApiConstants.SocialNetworking.SCHEME_CONNECTION_TYPE, connectionType, null);
      result.addCategory(ApiConstants.SocialNetworking.SCHEME_STATUS, status.name(), null);
      if (source != null || target != null) {
    	  ExtensibleElement connectionElement = ABDERA.getFactory().newExtensionElement(ApiConstants.SocialNetworking.QN_CONNECTION);
    	  if (source != null) {
    		  connectionElement.addExtension(colleagueToElement(source, ApiConstants.SocialNetworking.SNX_REL_SOURCE));    		  
    	  }
    	  if (target != null) {
    		  connectionElement.addExtension(colleagueToElement(target, ApiConstants.SocialNetworking.SNX_REL_TARGET));
    	  }
    	  result.addExtension(connectionElement);
      }
      result.setContent(content);
      return result;
  }
  
  public String getSummary() {
      return summary;
  }

  public void setSummary(String summary) {
      this.summary = summary;
  }

  public STATUS getStatus() {
      return status;
  }

  public void setStatus(STATUS status) {
      this.status = status;
  }

  public void setContent(String content) {
      this.content = content;
  }

  public String getContent() {
      return content;
  }

  public Colleague getAuthor() {
      return author;
  }

  public void setAuthor(Colleague author) {
      this.author = author;
  }

  public Colleague getLastModifier() {
      return lastModifier;
  }

  public void setLastModifier(Colleague lastModifier) {
      this.lastModifier = lastModifier;
  }

  public Colleague getSource() {
      return source;
  }

  public void setSource(Colleague source) {
      this.source = source;
  }

  public Colleague getTarget() {
      return target;
  }

  public void setTarget(Colleague target) {
      this.target = target;
  }

  public String getEditLink() {
      return getLinkHref(ApiConstants.Atom.REL_EDIT);
  }
  
  public String getId() {
      // <id>tag:profiles.ibm.com,2006:entry88925c98-9e7b-4458-aa50-3be3ebc86747</id>
      String atomId = getAtomId();
      return atomId.substring("tag:profiles.ibm.com,2006:entry".length());
  }
  
  public String getConnectionType()
  {
    return connectionType;
  }
  
  public void setConnectionType(String connectionType)
  {
    this.connectionType = connectionType;
  }
}
