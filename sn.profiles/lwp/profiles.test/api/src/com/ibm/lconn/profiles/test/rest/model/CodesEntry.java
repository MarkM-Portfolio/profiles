/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Content;
import org.apache.abdera.model.Content.Type;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class CodesEntry extends AtomEntry {

	private String codeType;
	private String codeId;
	private Map<String, Object> codesFields;

	public void setCodeId(String codeId) {
		this.codeId = codeId;
	}
	
	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}
	
	  public CodesEntry(){
		  super();
		  codesFields = new HashMap<String, Object>();
	  }
	  
	  public CodesEntry(Entry e) throws Exception
	  {
		super(e);

		Assert.assertTrue(e.getCategories(ApiConstants.SocialNetworking.SCHEME_TYPE).size() > 0);
		Assert.assertEquals(e.getCategories(
				ApiConstants.SocialNetworking.SCHEME_TYPE).get(0).getTerm(), 
				ApiConstants.SocialNetworking.TERM_CODES);
		
		codesFields = new HashMap<String, Object>();

		codeId = e.getId().getSchemeSpecificPart();
		// strip tag
		codeId = codeId.substring(codeId.indexOf(":")+1);
		// strip prefix
		codeId = codeId.substring(codeId.indexOf(":")+1);
		
		Type contentType = e.getContentType();
		Assert.assertEquals(contentType, Type.XML);
		
		String content = e.getContent();
		// System.out.println("CONTENT[" + contentType + "]:" + content);


				// ..<content type="application/xml">
				// ....<appData xmlns="http://ns.opensocial.org/2008/opensocial">
				// ......<com.ibm.snx_profiles.codes."codeType"."codeKey">
				// ........value
				// ......</com.ibm.snx_profiles.codes."codeType"."codeKey">
				// ....</appData>
				// ..</content>

				//profileFields = new HashMap<Field, Object>();

				Content c = e.getContentElement();
				List<Element> contentElements = c.getElements();

				for (Element g : contentElements) {
					if (ApiConstants.OpenSocial.QN_APPDATA.equals(g.getQName())) {
						Element appElement = g;

						for (Element h : appElement.getElements()) {
							String attrName = h.getQName().getLocalPart();
							// strip the prefix
							attrName = attrName.substring(ApiConstants.AdminConstants.ATTR_CODES_PREFIX.length());
							// strip the code type
							codeType = attrName.substring(0, attrName.indexOf('.'));
							attrName = attrName.substring(attrName.indexOf('.')+1);
							attrName = attrName.trim();

							// get the value, get the data
							String attrVal = h.getText().trim();

							Assert.assertNotNull(attrName);
							Assert.assertNotNull(attrVal);
							
							codesFields.put(attrName, attrVal);
						}
					}
				}
			}

		public Entry toEntryXml() throws Exception {

			// Entry result = super.toEntry();
			Entry result = ABDERA.newEntry();
			result.declareNS(ApiConstants.App.NS_URI, ApiConstants.App.NS_PREFIX);
			result.declareNS(ApiConstants.SocialNetworking.NS_URI, ApiConstants.SocialNetworking.NS_PREFIX);

			result.setId("tag:profiles.ibm.com,2006:com.ibm.snx_profiles.codes." +getCodeType() +":" +getCodeId() );
			
			// set the type
			result.addCategory(ApiConstants.SocialNetworking.SCHEME_TYPE, ApiConstants.SocialNetworking.TERM_PROFILE, null);

			ExtensibleElement contentElement = result.addExtension(new QName("", "content"));
			//ExtensibleElement contentElement = result.addExtension(ApiConstants.Atom.QN_CONTENT);
			contentElement.setAttributeValue("type", ApiConstants.Atom.MEDIA_TYPE_XML);			
			ExtensibleElement appElement = contentElement.addExtension(ApiConstants.OpenSocial.QN_APPDATA);

			/*
			 * <entry> <key>com.ibm.snx_profiles.base.displayName</key> <value> <type>text</type> <data>Susy Jones</data> </value> </entry>
			 */
			for (String c : codesFields.keySet()) {

				String elemName = ApiConstants.AdminConstants.ATTR_CODES_PREFIX + getCodeType() + "." + c;
				
				//ExtensibleElement entryElement = appElement.addExtension(new QName(elemName));
				appElement.addSimpleExtension(new QName(ApiConstants.OpenSocial.NS_URI, elemName), codesFields.get(c).toString());
			}

			return result;
		}
		
	  public CodesEntry validate() throws Exception
	  {
	    super.validate();
	    Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_EDIT));
	    
	    return this;
	  }

		public Map<String, Object> getCodesFields() {
			return codesFields;
		}

		public Set<String> getCodesFieldsKeySet() {
			return codesFields.keySet();
		}

		public Object getCodesFieldValue(String key) {
			return codesFields.get(key);
		}

		public String getCodeId() {
			return codeId;
		}
		
		public String getCodeType() {
			return codeType;
		}
}
