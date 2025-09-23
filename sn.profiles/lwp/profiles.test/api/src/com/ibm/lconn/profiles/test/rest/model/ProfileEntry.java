/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.model;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import junit.framework.Assert;

import org.apache.abdera.model.AtomDate;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Content;
import org.apache.abdera.model.Content.Type;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Person;
import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HCardParser;
import com.ibm.lconn.profiles.test.rest.util.VCardParserListenerImpl;
import com.ibm.lconn.profiles.vcard.parser.VCard21Parser;

public class ProfileEntry extends AtomEntry {
  
	public static final Map<Field, VCardProperty> FIELD_TO_VCARD = createFieldToVCardMap();
	public static final Map<VCardProperty, Field> VCARD_TO_FIELD = createVCardToFieldMap();
	public static final Map<String, Field> FIELDNAME_TO_FIELD = createFieldNameToFieldMap();

	  public static Map<Field, VCardProperty> createFieldToVCardMap()
	  {
	    Map<Field, VCardProperty> map = new HashMap<Field, VCardProperty>();
	    map.put(Field.ALTERNATE_LAST_NAME, VCardProperty.ALTERNATE_LAST_NAME);
	    map.put(Field.BLDG_ID, VCardProperty.BLDG_ID);
	    map.put(Field.BLOG_URL, VCardProperty.BLOG_URL);
	    map.put(Field.COUNTRY_CODE, VCardProperty.COUNTRY_CODE);
	    map.put(Field.COURTESY_TITLE, VCardProperty.COURTESY_TITLE);
	    map.put(Field.DEPT_NUMBER, VCardProperty.DEPT_NUMBER);
	    map.put(Field.DESCRIPTION, VCardProperty.DESCRIPTION);
	    map.put(Field.DISPLAY_NAME, VCardProperty.DISPLAY_NAME);
	    map.put(Field.EMAIL, VCardProperty.EMAIL);
	    map.put(Field.EMPLOYEE_NUMBER, VCardProperty.EMPLOYEE_NUMBER);
	    map.put(Field.EMPLOYEE_TYPE_CODE, VCardProperty.EMPLOYEE_TYPE_CODE);
	    map.put(Field.EMPLOYEE_TYPE_DESC, VCardProperty.EMPLOYEE_TYPE_DESC);
	    map.put(Field.EXPERIENCE, VCardProperty.EXPERIENCE);
	    map.put(Field.FAX_NUMBER, VCardProperty.FAX_NUMBER);
	    map.put(Field.FLOOR, VCardProperty.FLOOR);
	    map.put(Field.GROUPWARE_EMAIL, VCardProperty.GROUPWARE_EMAIL);
	    map.put(Field.GUID, VCardProperty.GUID);
	    map.put(Field.IP_TELEPHONE_NUMBER, VCardProperty.IP_TELEPHONE_NUMBER);
	    map.put(Field.IS_MANAGER, VCardProperty.IS_MANAGER);
	    map.put(Field.JOB_RESP, VCardProperty.JOB_RESP);
	    map.put(Field.KEY, VCardProperty.KEY);
	    map.put(Field.LCONN_USERID, VCardProperty.LCONN_USER_ID);
	    map.put(Field.LAST_UPDATE, VCardProperty.LAST_UPDATE);
	    map.put(Field.MANAGER_UID, VCardProperty.MANAGER_UID);
	    map.put(Field.MOBILE_NUMBER, VCardProperty.MOBILE_NUMBER);
	    map.put(Field.NATIVE_FIRST_NAME, VCardProperty.NATIVE_FIRST_NAME);
	    map.put(Field.NATIVE_LAST_NAME, VCardProperty.NATIVE_LAST_NAME);
	    map.put(Field.OFFICE_NAME, VCardProperty.OFFICE_NAME);
	    map.put(Field.ORG_ID, VCardProperty.ORG_ID);
	    map.put(Field.ORGANIZATION_TITLE, VCardProperty.ORGANIZATION_TITLE);
	    map.put(Field.PAGER_ID, VCardProperty.PAGER_ID);
	    map.put(Field.PAGER_SERVICE_PROVIDER, VCardProperty.PAGER_SERVICE_PROVIDER);
	    map.put(Field.PAGER_TYPE, VCardProperty.PAGER_TYPE);
	    map.put(Field.PREFERRED_FIRST_NAME, VCardProperty.PREFERRED_FIRST_NAME);
	    map.put(Field.PREFERRED_LAST_NAME, VCardProperty.PREFERRED_LAST_NAME);
	    map.put(Field.TELEPHONE_NUMBER, VCardProperty.TELEPHONE_NUMBER);
	    map.put(Field.TIMEZONE, VCardProperty.TIMEZONE);
	    map.put(Field.UID, VCardProperty.UID);
	    map.put(Field.WORK_LOCATION, VCardProperty.WORK_LOCATION);
	    map.put(Field.WORK_LOCATION_CODE, VCardProperty.WORK_LOCATION_CODE);
	    return map;
	  }
	  
	  public static Map<VCardProperty, Field> createVCardToFieldMap()
	  {
	    Map<Field, VCardProperty> map = createFieldToVCardMap();
	    Map<VCardProperty, Field> inverseMap = new HashMap<VCardProperty, Field>();
	    for (Field f : map.keySet()) {
	      inverseMap.put(map.get(f), f);
	    }
	    return inverseMap;
	  }
	  
	  public static Map<String, Field> createFieldNameToFieldMap()
	  {
	    Map<String, Field> map = new HashMap<String, Field>();
	    for (Field f : Field.values()) {
	      map.put(f.getValue(), f);
	    }   
	    return map;
	  }
	  
	private String name;

	private String email;

	private String userId;

	private String userState;

	private String userMode;
	
	private String isExternal;

	private String summary;

	private Set<Tag> tags;

	private String key;

	private Map<Field, Object> profileFields;
	
	private String statusMessage = null;
	
	private AtomDate statusAsOf = null;

	/**
	 * careful, profileExtensionFields may not be supported in all operations
	 */
	private Set<ExtensionField> profileExtensionFields = new HashSet<ExtensionField>();

	/**
	 * required properties to create a Profile, all others are optional
	 * 
	 * @param dn
	 * @param guid
	 * @param surname
	 * @param uid
	 */
	public ProfileEntry(String dn, String guid, String surname, String uid) {
		profileFields = new HashMap<Field, Object>();
		profileFields.put(Field.DISTINGUISHED_NAME, guid);
		profileFields.put(Field.GUID, guid);
		profileFields.put(Field.SURNAME, surname);
		profileFields.put(Field.UID, uid);
		tags = new HashSet<Tag>();
	}

	public ProfileEntry(Entry e) throws Exception {
		super(e);
		Assert.assertTrue(e.getContributors().size() == 1);
		Person person = e.getContributors().get(0);
		name = person.getName();
		email = person.getEmail();
		userId = person.getSimpleExtension(ApiConstants.SocialNetworking.USER_ID);
		isExternal = person.getSimpleExtension(ApiConstants.SocialNetworking.IS_EXTERNAL);
		
		// Assert that we always have the <snx:isExternal> in the entry
		Assert.assertNotNull(isExternal);
		
		Assert.assertTrue(e.getCategories(ApiConstants.SocialNetworking.SCHEME_TYPE).size() > 0);
		Assert.assertEquals(e.getCategories(ApiConstants.SocialNetworking.SCHEME_TYPE).get(0).getTerm(),
				ApiConstants.SocialNetworking.TERM_PROFILE);
		tags = new HashSet<Tag>(3);
		for (Category c : e.getCategories()) {
			String tagType = c.getAttributeValue(ApiConstants.SocialNetworking.TYPE);
			if (tagType != null && tagType.length() > 0)
			{
				Tag aTag = new Tag(c);
				tags.add(aTag);
			}
		}
		
		Element status = e.getExtension(ApiConstants.SocialNetworking.QN_STATUS);
		if(null != status){			
			for(Element e2 : status.getElements()){
				if (e2.getQName().equals(ApiConstants.SocialNetworking.QN_AS_OF)){
					String asOfText = e2.getText();
					statusAsOf = new AtomDate(asOfText);
				}
				else if (e2.getQName().equals(ApiConstants.SocialNetworking.QN_MESSAGE)){
					statusMessage = e2.getText();
				}
			}
		}

		Type contentType = e.getContentType();
		String content = e.getContent();
		// System.out.println("CONTENT[" + contentType + "]:" + content);

		if (Type.XHTML.equals(contentType)) {
			profileFields = HCardParser.parseHCard(content);
		}
		else if (Type.TEXT.equals(contentType)) {
			profileFields = new HashMap<Field, Object>();
			Reader reader = new StringReader(content);
			VCard21Parser parser = new VCard21Parser(reader);
			parser.setListener(new VCardParserListenerImpl(this));
			parser.parse();
		}
		else if (Type.XML.equals(contentType)) {

			// ..<content type="application/xml">
			// ....<person xmlns="http://ns.opensocial.org/2008/opensocial">
			// ......<com.ibm.snx_profiles.attrib>
			// ........<entry>
			// ..........<key>com.ibm.snx_profiles.base.uid</key>
			// ..........<value>
			// ............<type>text</type>
			// ............<data>smazar</data>
			// ..........</value>
			// ........</entry>

			profileFields = new HashMap<Field, Object>();
			
			Content c = e.getContentElement();
			List<Element> contentElements = c.getElements();

			for (Element g : contentElements) {
				if (ApiConstants.OpenSocial.QN_PERSON.equals(g.getQName())) {
					Element personElement = g;

					for (Element h : personElement.getElements()) {
						if (ApiConstants.OpenSocial.QN_SNX_PROFILES_ATTRIB.equals(h.getQName())) {
							List<Element> attribElements = h.getElements();

							for (Element i : attribElements) {
								if (ApiConstants.OpenSocial.QN_ENTRY.equals(i.getQName())) {

									Field field = null;
									String fieldValue = null;
									String keyGroup = null;
									String keySuffix = null; 
										
									for (Element j : i.getElements()) {

										if (ApiConstants.OpenSocial.QN_KEY.equals(j.getQName())) {
											String key = j.getText();

											if (key.startsWith(ApiConstants.SocialNetworking.ATTR_PREFIX))
												key = key.substring(ApiConstants.SocialNetworking.ATTR_PREFIX.length() + 1);

											StringTokenizer st = new StringTokenizer(key, ".");

											keyGroup = st.nextToken();
											keySuffix = st.nextToken();

											Assert.assertNotNull(keyGroup);
											Assert.assertNotNull(keySuffix);

										}
										else if (ApiConstants.OpenSocial.QN_VALUE.equals(j.getQName())) {
											for (Element k : j.getElements()) {
												// TODO: validate QN_TYPE vs map here?
												if (ApiConstants.OpenSocial.QN_DATA.equals(k.getQName())) {
													fieldValue = k.getText();
												}
											}
										}
									}

									// TODO handle non-string values, if needed
									// TODO in some cases, the value is an attribute and not the body text, fix-up as needed

									field = ProfileEntry.FIELDNAME_TO_FIELD.get(keySuffix);
									if (null == field) {
										FieldGroupType groupType = FieldGroupType.lookup(keyGroup);
										if(null == groupType) throw new Exception("Uknown groupType: " + groupType);
										profileExtensionFields.add(new ExtensionField(keySuffix, groupType, normalize(fieldValue)));
									}
									else {
										profileFields.put(field, normalize(fieldValue));
									}

								}
							}
						}
					}
				}
			}
		}
		else {
			throw new Exception("Unhandled contentType: " + contentType);
		}
	}

	public ProfileEntry validate() throws Exception {
		super.validate();
		Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_RELATED));
		// TODO fix this defect
		// Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_ALTERNATE));
		// assertNotNullOrZeroLength(getKey());
		assertNotNullOrZeroLength(getUserId());
		// assertNotNullOrZeroLength(getUserState());
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserState() {
		return userState;
	}

	public void setUserState(String userState) {
		this.userState = userState;
	}

	public String getUserMode() {
		return userMode;
	}

	public void setUserMode(String userMode) {
		this.userMode = userMode;
	}

	public String getIsExternal() {
		return isExternal;
	}
	
	public void setIsExternal(String isExt) {
		this.isExternal = isExt;
	}
	
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Map<Field, Object> getProfileFields() {
		return profileFields;
	}

	public Set<Field> getProfileFieldsKeySet() {
		return profileFields.keySet();
	}

	public Object getProfileFieldValue(Field key) {
		return profileFields.get(key);
	}
	
	public String getStatusMessage(){
		return statusMessage;
	}

	public AtomDate getStatusAsOf(){
		return statusAsOf;
	}
	
	/**
	 * careful, profileExtensionFields may not be supported in all operations
	 * 
	 * @return
	 */
	public Set<ExtensionField> getProfileExtensionFields() {
		return profileExtensionFields;
	}
	
	public ExtensionField getProfileExtensionField(String id)
	{
		for(ExtensionField ef : profileExtensionFields) {
			if (id.equals(ef.getKeyName()))
				return ef;
		}
		return null;
	}

	public String toString() {
		return toString(false);
	}
	public String toString(boolean limited) {
		StringBuilder sb = new StringBuilder("profile [\n");
		Map<Field, Object> profileFields = getProfileFields();
		if (profileFields != null) {
			sb.append("\tfields: [\n");
			for (Field field : profileFields.keySet()) {
				String fieldName = field.getValue();
				boolean doPrint = StringUtils.isNotEmpty(fieldName);
				if ((limited) && (false == "displayName".equalsIgnoreCase(fieldName))) {
						doPrint = false;
				}
				if (doPrint) {
					Object value = profileFields.get(field);
					sb.append("\t\t{ key:" + field.getValue() + ", value:" + value.toString() + " }\n");
				}
			}
			sb.append("\t]\n");
		}
		sb.append("]");
		return sb.toString();
	}

	public boolean equals(ProfileEntry target) {

		Map<Field, Object> profileFields = getProfileFields();
		Map<Field, Object> profileFieldsTarget = target.getProfileFields();

		// short circuit: nothing to compare
		if (profileFields == null && profileFieldsTarget == null) {
			return true;
		}

		// short circuit: null / !null
		if ((profileFields == null && profileFieldsTarget != null) || (profileFields != null && profileFieldsTarget == null)) {
			return false;
		}

		// NOTE: these fields were not returned before 54867
		// might choose to filter server-side, but for now simply remove as we're more concerned with omissions
		profileFields.remove(Field.LAST_UPDATE);
		profileFieldsTarget.remove(Field.LAST_UPDATE);
		profileFields.remove(Field.USR_STATE);
		profileFieldsTarget.remove(Field.USR_STATE);
		profileFields.remove(Field.USER_MODE);
		profileFieldsTarget.remove(Field.USER_MODE);

		// short circuit: unequal numbers of fields
		if (profileFields.size() != profileFieldsTarget.size()) {
			return false;
		}

		Set<Field> profileFieldsKeySet = profileFields.keySet();
		Set<Field> profileFieldsTargetKeySet = profileFieldsTarget.keySet();

		// next 2 tests use Collections containsAll() for code clarity in JUnit case, likely not performant on the server side

		// short circuit: dissimilar keySets
		if (!profileFieldsKeySet.containsAll(profileFieldsTargetKeySet)) {
			return false;
		}

		// short circuit: ibid
		if (!profileFieldsTargetKeySet.containsAll(profileFieldsKeySet)) {
			return false;
		}

		// finally value-by-value comparison
		for (Field field : profileFieldsKeySet) {
			Object value = profileFields.get(field);
			Object valueTarget = profileFieldsTarget.get(field);

			// short circuit: values are not the same object type
			if (value.getClass() != valueTarget.getClass()) {
				return false;
			}

			if (!value.equals(valueTarget)) {
				return false;
			}
		}

		return true;
	}

	/*
	 * input for eg: /admin/atom/profiles.do
	 */
	public Entry toEntryXml() throws Exception {

		// Entry result = super.toEntry();
		Entry result = ABDERA.newEntry();
		result.declareNS(ApiConstants.App.NS_URI, ApiConstants.App.NS_PREFIX);
		result.declareNS(ApiConstants.SocialNetworking.NS_URI, ApiConstants.SocialNetworking.NS_PREFIX);

		// set the type
		result.addCategory(ApiConstants.SocialNetworking.SCHEME_TYPE, ApiConstants.SocialNetworking.TERM_PROFILE, null);

		for (Tag tag : tags) {
			tag.toCategory(result, ABDERA.getFactory());
		}

		ExtensibleElement contentElement = result.addExtension(ApiConstants.Atom.QN_CONTENT);
		contentElement.setAttributeValue("type", ApiConstants.Atom.MEDIA_TYPE_XML);
		ExtensibleElement personElement = contentElement.addExtension(ApiConstants.OpenSocial.QN_PERSON);
		ExtensibleElement attribElement = personElement.addExtension(ApiConstants.OpenSocial.QN_SNX_PROFILES_ATTRIB);

		/*
		 * <entry> <key>com.ibm.snx_profiles.base.displayName</key> <value> <type>text</type> <data>Susy Jones</data> </value> </entry>
		 */
		for (Field f : profileFields.keySet()) {
			// <entry>
			ExtensibleElement entryElement = attribElement.addExtension(ApiConstants.OpenSocial.QN_ENTRY);
			// <key>
			entryElement.addSimpleExtension(ApiConstants.OpenSocial.QN_KEY, f.getFullyQualifiedValue());
			// <value>
			ExtensibleElement valueElement = entryElement.addExtension(ApiConstants.OpenSocial.QN_VALUE);
			// <type>
			valueElement.addSimpleExtension(ApiConstants.OpenSocial.QN_TYPE, f.getType().getValue());
			// TODO: just a guess here ... what to do if there is no value. skip the "data" element for a guess ...
			if (null != profileFields.get(f)) {
				// TODO: handle timestamps if needed ...
				// <data>
				String value = profileFields.get(f).toString();
//				System.out.println("ProfileEntry.toEntryXml() : " + f.name() + " " + value);
				valueElement.addSimpleExtension(ApiConstants.OpenSocial.QN_DATA, value);
			}
		}

		for (ExtensionField f : profileExtensionFields) {
			// <entry>
			ExtensibleElement entryElement = attribElement.addExtension(ApiConstants.OpenSocial.QN_ENTRY);
			// <key>
			entryElement.addSimpleExtension(ApiConstants.OpenSocial.QN_KEY, f.getFullyQualifiedKeyName());
			// <value>
			ExtensibleElement valueElement = entryElement.addExtension(ApiConstants.OpenSocial.QN_VALUE);
			// <type>
			valueElement.addSimpleExtension(ApiConstants.OpenSocial.QN_TYPE, f.getType().getValue());
			// TODO: just a guess here ... what to do if there is no value. skip the "data" element for a guess ...
			if (null != f.getValue()) {
				// TODO: handle timestamps if needed ...
				// <data>
				String value = f.getValue().toString();
//				System.out.println("ProfileEntry.toEntryXml() : " + f.getKeyName() + " " + value);
				valueElement.addSimpleExtension(ApiConstants.OpenSocial.QN_DATA, value);
			}
		}

		return result;

	}

	/**
	 * Return a new atom entry that represents the current state of this memory-object
	 * 
	 * @return
	 * @throws Exception
	 */
	public Entry toEntry() throws Exception {
		Entry result = super.toEntry();

		// set the type
		result.addCategory(ApiConstants.SocialNetworking.SCHEME_TYPE, ApiConstants.SocialNetworking.TERM_PROFILE, null);

		// supply tags
		for (Tag tag : tags) {
			result.addCategory(tag.toCategory(result, ABDERA.getFactory()));
		}

		// set the vcard data
		String vCard = toVCard();
		result.setContent(vCard, Type.TEXT);

		return result;
	}

	public String toVCard() throws Exception {
		String NEW_LINE = "\n";
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN:VCARD");
		sb.append(NEW_LINE);
		sb.append("VERSION:2.1");
		sb.append(NEW_LINE);
		for (Field field : Field.values()) {
			VCardProperty property = FIELD_TO_VCARD.get(field);
			if (property != null) {
				sb.append(property.toVCardProperty());
				Object value = profileFields.get(field);
				if (value != null) {
					if (FieldType.TIMESTAMP.equals(field.getType())) {
						// TODO
					}
					else if (FieldType.STRING.equals(field.getType())) {
						sb.append(value.toString());
					}
				}
				sb.append(NEW_LINE);
			}
		}
		sb.append("END:VCARD");
		return sb.toString();
	}

	public static String normalize(String s) {
		if (null == s) return s;
		return s.trim();
	}

	public void updateFieldValue(Field field, String newValue) {
		getProfileFields().remove(field);
		getProfileFields().put(field, newValue);
	}
}
