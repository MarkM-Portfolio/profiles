/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.connections.profiles.seedlist;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.lang.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibm.ilel.seedlist.SeedlistException;
import com.ibm.ilel.seedlist.common.Field;
import com.ibm.ilel.seedlist.common.Link;
import com.ibm.ilel.seedlist.common.Metadata;
import com.ibm.ilel.seedlist.common.lconn.LConnField;
import com.ibm.ilel.seedlist.imp.AbstractDocument;
import com.ibm.ilel.seedlist.imp.FieldImp;
import com.ibm.ilel.seedlist.imp.LinkImp;
import com.ibm.ilel.seedlist.imp.MetadataImp;
import com.ibm.ilel.seedlist.imp.lconn.LConnFieldImp;

import com.ibm.lconn.core.web.secutil.SSLHelper;
import com.ibm.lconn.core.web.util.HtmlPlainTextTransformUtil;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.IndexAttributeForConnection;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig.IndexTagAttribute;
import com.ibm.lconn.profiles.config.dm.XmlFileExtensionAttributeConfig;
import com.ibm.lconn.profiles.config.dm.XmlFileExtensionIndexFieldConfig;
import com.ibm.lconn.profiles.config.dm.XmlFileExtensionIndexFieldConfig.IndexFieldConfig;
import com.ibm.lconn.profiles.config.types.ExtensionType;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeConfig;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.config.types.PropertyType;

import com.ibm.lconn.profiles.data.GivenName;
import com.ibm.lconn.profiles.data.IndexerProfileDescriptor;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Surname;
import com.ibm.lconn.profiles.data.codes.WorkLocation;

import com.ibm.lconn.profiles.internal.constants.ProfilesIndexConstants;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;
import com.ibm.lconn.profiles.internal.util.Pair;
import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileTag;
import com.ibm.peoplepages.data.ProfileTagCloud;
import com.ibm.peoplepages.data.ProfileTagRetrievalOptions.Verbosity;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * Represents an entry in the profile seedlist.
 * discussions with search team 02/17/2015 indicated that they do not require profiles to remove emails on a per-use basis.
 * see comments in EnmployeeEntrySet.
 */
public class EmployeeDocument extends AbstractDocument
{
	private static final String CLASSNAME = EmployeeDocument.class.getName();
	private static final Logger logger    = Logger.getLogger(CLASSNAME);

	private IndexerProfileDescriptor profileDesc;
	private String key; // aka - userid
	//x we could calculate per doc (user) whether to include email
	//x private boolean exposeEmail;

	private static final String ATOM_PROFILES = "/atom/profile.do";

	public EmployeeDocument(IndexerProfileDescriptor profileDesc, Locale locale) throws SeedlistException
	{
		final String method = "EmployeeDocument";
		final boolean FINER = logger.isLoggable(Level.FINER);
		final boolean FINEST= logger.isLoggable(Level.FINEST);

		if (FINER) {
			logger.entering(CLASSNAME, method + "(profileDesc, locale)", new Object[] { profileDesc, locale });
		}

		this.profileDesc = profileDesc;
		//x this.exposeEmail = LCConfig.instance().isEmailReturned(profileDesc.getProfile().getTenantKey());
		Employee profile = profileDesc.getProfile();
		String userId = profile.getUserid();
		int action = getAction(profileDesc);
		this.key = userId;

		// set base entry fields
		setId(userId); // set unique ID for seedlist document (AtomID)
		boolean isExternal = profile.isExternal();
		this.displayLink = getLink(profile);

		// build metadata for entry in seedlist
		MetadataImp metadata = new MetadataImp();
		metadata.setLocale(locale);
		metadata.setAction(action);
		// for all action types except DELETE
		if (Metadata.ACTION_DELETE != action) {
			// populate fields and acl
			metadata.setFields(getFields(profile));
			metadata.setACLs(getACLs(profile));

			// New since IC 5.0 with visitor model support
			if (FINEST) {
				logger.log(Level.FINEST, "Setting metadata.setExternal(" + isExternal + ")");
			}
		    metadata.setExternal( isExternal );
		}
		if (FINER) {
			logger.log(Level.FINER, "setMetadata(" + metadata.toString() + ")");
		}
		setMetadata(metadata);

		if (FINER) {
			logger.exiting(CLASSNAME, method);
		}
	}

	/**
	 * Date the profile was last updated
	 * 
	 * @return
	 */
	public Date getUpdateDate() {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(CLASSNAME, "getUpdateDate", new Object[] {});
		}

		Date date = profileDesc.getProfile().getRecordUpdated();

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(CLASSNAME, "getUpdateDate", date);
		}

		return date;
	}

	/**
	 * Retrieves the Atom API link representation for the profile
	 * 
	 * @param entry
	 * @return
	 * @throws SeedlistException
	 */
	private Link getLink(Employee entry) throws SeedlistException {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(CLASSNAME, "getLink", new Object[] { entry });
		}

		LinkImp link = null;
		String url = getProfilesEntryURL(entry);
		try {
			URI uri = new URI(url);
			link = new LinkImp(uri);
			link.setTitle(entry.getDisplayName());

			if (logger.isLoggable(Level.FINER)) {
				logger.exiting(CLASSNAME, "getLink", new Object[] { link });
			}
			return link;
		}
		catch (Exception e) {
			if (logger.isLoggable(Level.FINER)) {
				logger.throwing(CLASSNAME, "getLink", e);
			}
			throw new SeedlistException(SeedlistException.TYPE_DOCUMENT_ENTRY_ERROR, e.getMessage(), e);
		}
	}

	/**
	 * Retrieves the ACLs associated with this profile, currently there are no private persons, so it is always 'public'
	 * 
	 * @param entry
	 * @return
	 */
	private String[] getACLs(Employee entry) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(CLASSNAME, "getACLs", new Object[] { entry });
		}
		// no private content in profiles
		String[] result = new String[] { "public" };
		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(CLASSNAME, "getACLs", result);
		}
		return result;
	}

	/**
	 * Return the action that corresponds to this entry in the seedlist (update, delete, insert)
	 * 
	 * @param entry
	 * @return
	 */
	private int getAction(IndexerProfileDescriptor profileDesc) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(CLASSNAME, "getAction", new Object[] { profileDesc });
		}

		int retval = Metadata.ACTION_UPDATE;

		if (profileDesc.isTombstone() )
			retval = Metadata.ACTION_DELETE;

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(CLASSNAME, "getAction", retval);
		}

		return retval;
	}

	private void handleWorkLocation(Employee entry, ProfileType profileType, List<Field> fields) throws SeedlistException
	{
		final String method  = "handleWorkLocation";
		final boolean FINER  = logger.isLoggable(Level.FINER);

		if (FINER) {
			logger.entering(CLASSNAME, method, new Object[] { entry, profileType, fields });
		}

		// we need to check if this profileType has organization/country and if it is visible via index
		Property orgIdProperty = profileType.getPropertyById(PropertyEnum.ORG_ID.getValue());
		Property countryProperty = profileType.getPropertyById(PropertyEnum.COUNTRY_CODE.getValue());
		boolean isOrgIndexable = (orgIdProperty != null && orgIdProperty.isFullTextIndexed());
		boolean isCountryIndexable = (countryProperty != null && countryProperty.isFullTextIndexed());

		// Add work location code itself for indexing
		String workLocCode = entry.getWorkLocationCode();
		if ( StringUtils.isNotBlank(workLocCode) )
		    fields.add(new FieldImp(workLocCode, ProfilesIndexConstants.FIELD_WORK_LOCATION_CODE_ID) );

		// Add other values from workLocation table
		StringBuilder combinedWorkLocation = new StringBuilder();
		WorkLocation workLocation = entry.getWorkLocation();

		if (workLocation != null) {
			String address = workLocation.getAddress1();
			String address2 = workLocation.getAddress2();
			String city = workLocation.getCity();
			String state = workLocation.getState();
			String postalCode = workLocation.getPostalCode();

			if ( StringUtils.isNotBlank(address) )
			    fields.add(new FieldImp(address, ProfilesIndexConstants.FIELD_LOCATION));

			if ( StringUtils.isNotBlank(address2) )
			    fields.add(new FieldImp(address2, ProfilesIndexConstants.FIELD_LOCATION2));

			if ( StringUtils.isNotBlank(city) )
			    fields.add(new FieldImp(city, ProfilesIndexConstants.FIELD_CITY));

			if ( StringUtils.isNotBlank(state) )			
			    fields.add(new FieldImp(state, ProfilesIndexConstants.FIELD_STATE));

			if ( StringUtils.isNotBlank(postalCode) )
			    fields.add(new FieldImp(postalCode, ProfilesIndexConstants.FIELD_POSTAL_CODE));

			if (isOrgIndexable) {
				String orgTitleDesc = profileDesc.getProfile().getOrganizationTitle();
				if (StringUtils.isNotBlank(orgTitleDesc) ) {
					combinedWorkLocation.append(orgTitleDesc).append("<br/>");
				}
			}

			if (StringUtils.isNotBlank(city)) {
				combinedWorkLocation.append(city).append("<br/>");
			}

			if (StringUtils.isNotBlank(state)) {
				combinedWorkLocation.append(state).append("<br/>");
			}

			if (isCountryIndexable) {
				String countryName = profileDesc.getProfile().getCountryDisplayValue();
				if (StringUtils.isNotBlank(countryName))
				    combinedWorkLocation.append(countryName).append("<br/>");
			}

			// we now add the work location combined as a single field
			String result = combinedWorkLocation.toString();
			if (StringUtils.isNotBlank(result)) {
				fields.add(new FieldImp(result.trim(), ProfilesIndexConstants.FIELD_WORK_LOCATION_ID));
			}
		}

		if (FINER) {
			logger.exiting(CLASSNAME, method);
		}
	}

	/**
	 * XML Fields in profiles can index multiple elements in the XML document based on profile configuration. This method will add fields to
	 * the seedlist entry for each configured part of the XML to be indexed.
	 * 
	 * @param value
	 * @param propertyRef
	 * @param fields
	 * @throws SeedlistException
	 */
	private void handleXMLField(String value, String propertyRef, List<Field> fields) throws SeedlistException {
		final String method  = "handleXMLField";
		final boolean FINER  = logger.isLoggable(Level.FINER);

		if (FINER) {
			logger.entering(CLASSNAME, method, new Object[] { value, propertyRef, fields });
		}

		try {
			// if the value is null or empty, then ignore
			if (value != null && !"".equals(value.trim())) {
				DocumentBuilder documentBuilder = newDocumentBuilder();
				XmlFileExtensionAttributeConfig xmlConfig = (XmlFileExtensionAttributeConfig) DMConfig.instance()
						.getExtensionAttributeConfig().get(propertyRef);
				XmlFileExtensionIndexFieldConfig fieldsConfig = xmlConfig.getIndexFieldConfig();
				for (IndexFieldConfig indexField : fieldsConfig.getIndexFields()) {
					String fieldName = indexField.getFieldName();
					String seedListAttr = EmployeeEntrySet.getSeedlistXmlAttribute(propertyRef, fieldName);
					XPathExpression expression = indexField.getXPathExpression();
					Document document = documentBuilder.parse(new InputSource(new StringReader(value)));
					NodeList nodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node element = nodeList.item(i);
						String elementValue = element.getTextContent();
						if (elementValue != null && elementValue.trim().length() > 0) {
							fields.add(new FieldImp(elementValue.trim(), seedListAttr));
						}
					}
				}
			}
		}
		catch (Exception e) {
		    // PMR 21499,999,866, defect: 106592. We don't want to abort the entire indexing task if we encounter bad XML data.
		    // We would skip the indexing for this XML attribute for the user, and move on.
		    String errorMsg = "Profiles seedlist: failed to index an XML field!";
		    if ( profileDesc != null && profileDesc.getProfile() != null )
			    errorMsg += " userId = " +profileDesc.getProfile().getUserid() +", userName = " +profileDesc.getProfile().getDisplayName();

		    errorMsg += "; with exception: " +e.getMessage() +", XML value: " +value;

		    logger.severe(errorMsg );

		    // throw new SeedlistException(SeedlistException.TYPE_DOCUMENT_ENTRY_ERROR, e.getMessage(), e);
		}

		if (FINER) {
			logger.exiting(CLASSNAME, method);
		}
	}

	/**
	 * Adds the data from the profile to the actual seedlist data model
	 * 
	 * @param entry
	 * @return
	 * @throws SeedlistException
	 */
	private Field[] getFields(Employee entry) throws SeedlistException {
		final String method  = "getFields";
		final boolean FINER  = logger.isLoggable(Level.FINER);
		final boolean FINEST = logger.isLoggable(Level.FINEST);

		if (FINER) {
			logger.entering(CLASSNAME, method, new Object[] { entry });
		}

		// the fields that we index are defined in the profile type definition
		ProfileTypeConfig profileTypeConfig = ProfilesConfig.instance().getProfileTypeConfig();
		// by default, we index all potentially known properties in profiles, so we ask for this uber-profile type from config
		// in MT, we take the set of attributes defined in global config.
		ProfileType profileType = profileTypeConfig.getSeedlistDefaultType();
		// but if variable indexing is on, then we need the users actual profile type to determine what properties should be indexed
		boolean isVariableFullTextIndexEnabled = ProfilesConfig.instance().getProperties().getBooleanValue(ConfigProperty.VARIABLE_FULL_TEXT_INDEX_ENABLED); 
		if (isVariableFullTextIndexEnabled)
		{
			profileType = ProfileTypeHelper.getProfileType(entry.getProfileType());
		}
		
		if (FINER) {
			logger.log(Level.FINER, "isVariableFullTextEnabled:" + isVariableFullTextIndexEnabled);
			logger.log(Level.FINER, "profileType to index:" + profileType.getId());
		}
		
		List<Field> fields = new ArrayList<Field>(profileType.getProperties().size() * 2);

		// we will need to mask known e-mail fields from search if the global setting is enabled to hide it
		//x note above that exposeEmail could be per document/user
		boolean exposeEmail = LCConfig.instance().getEmailReturnedDefault();
		boolean isLotusLive = LCConfig.instance().isLotusLive();

		// we always add displayName and update date
		fields.add(new FieldImp(entry.getDisplayName(),   Field.FIELD_TITLE));
		fields.add(new FieldImp(entry.getRecordUpdated(), Field.FIELD_UPDATE_DATE));

		// iterate over each property in the type definition
		for (Property property : profileType.getProperties()) {
			// if the property is indexable, then handle it according to its definition
			if (property.isFullTextIndexed()) {
				// extension properties are indexed in a special manner according to their type
				if (property.isExtension()) {
					ProfileExtension profileExtension = entry.getProfileExtension(property.getRef(), false);
					if (profileExtension != null) {
						String value = profileExtension.getStringValue();
						if (ExtensionType.XMLFILE.equals(property.getExtensionType())) {
							// xml extensions may expand into multiple fields, so call helper method to handle parse/expansion logic
							handleXMLField(value, property.getRef(), fields);
						}
						else {
							// simple extensions, and rich text extensions are just indexed in full
							String seedListAttr = (ProfilesIndexConstants.EXT_ATTR_KEY_BASE + property.getRef()).toUpperCase(Locale.US);
							fields.add(new FieldImp(value, seedListAttr));
						}
					}
				}
				else {
					// handle base fields for inclusion in the index
					// determine the data type for this property so we can convert properly
					PropertyEnum propertyEnum = PropertyEnum.getByValue(property.getRef());
					PropertyType propertyType = propertyEnum.getPropertyType();

					// if this is an email field, and email exposure is disabled, then skip indexing it
					if (exposeEmail == false) {
						if (PropertyEnum.EMAIL.equals(propertyEnum) || PropertyEnum.GROUPWARE_EMAIL.equals(propertyEnum)) {
							continue;
						}
					}

					// find the name that we should make this base field in the index
					String indexFieldName = ProfilesIndexConstants.baseIndexFieldMapping.get(property.getRef());

					// this is a field that profiles is capable of indexing
					if (indexFieldName != null) {
						// get the object value
						Object elem = entry.get(property.getRef());
						String elemValue = "";

						// convert based on property type to underlying string representation
						if (elem != null) {
							if (PropertyType.STRING.equals(propertyType)) {
								elemValue = (String) elem;
							}

							elemValue = elemValue.trim();

							if (StringUtils.isNotEmpty(elemValue)) {
								if (PropertyEnum.WORK_LOCATION_CODE.equals(propertyEnum)) {
									// this gets expanded into multiple values
									handleWorkLocation(entry, profileType, fields);
								}
								else if (PropertyEnum.SURNAME.equals(propertyEnum)) {
									if (isLotusLive == false) { // see notes in EmployeeEntrySet
										// add each surname to the index
										if (this.profileDesc.getSurnames() != null && !this.profileDesc.getSurnames().isEmpty()) {
											for (Surname sName : this.profileDesc.getSurnames()) {
												fields.add(new FieldImp(sName.getName(), indexFieldName));
											}
										}
									}
								} else if (PropertyEnum.GIVEN_NAME.equals(propertyEnum) ) {			
									if (isLotusLive == false) { // see notes in EmployeeEntrySet
										// add each given name to the index
										if (this.profileDesc.getGivenNames() != null && !this.profileDesc.getGivenNames().isEmpty()) {
											for (GivenName gName : this.profileDesc.getGivenNames()) {
												fields.add(new FieldImp(gName.getName(), indexFieldName));
											}
										}
									}
								}
								else if (PropertyEnum.COUNTRY_CODE.equals(propertyEnum)) {
									// add the field [iso country code id]
									fields.add(new FieldImp(elemValue, indexFieldName));
									// add the country name
									String countryName = profileDesc.getProfile().getCountryDisplayValue();
									if (countryName != null && countryName.trim().length() > 0) {
										fields.add(new FieldImp(countryName, ProfilesIndexConstants.FIELD_COUNTRY));
									}
								}
								else if (PropertyEnum.MANAGER_UID.equals(propertyEnum)) {
									// add user id of manager, and the actual manager value
									fields.add(new FieldImp(entry.getManagerUserid(), ProfilesIndexConstants.FIELD_MANAGER_USERID_ID));
									fields.add(new FieldImp(elemValue, indexFieldName));
								}
								else if (PropertyEnum.SECRETARY_UID.equals(propertyEnum)) {
									// add the secretary name and id
									String secretaryName = entry.getSecretaryName();
									if (secretaryName != null) {
										fields.add(new FieldImp(secretaryName, ProfilesIndexConstants.FIELD_SECRETARY_DISPLAY_NAME_ID));
									}
									fields.add(new FieldImp(elemValue, indexFieldName));
								}
								else if (isPhoneNumber(property.getRef())) {
									// add phone number as found
									fields.add(new FieldImp(elemValue, indexFieldName));
									
									// get the 'normalized index field ID
									String normalizedIndexFieldName = ProfileSearchUtil.getNormalizedIndexFieldID( indexFieldName);

									// add original number to the 'normalized field'
									fields.add(new FieldImp(elemValue, normalizedIndexFieldName));

									// add the normalized phone numbers if different
									String normalizedValue = ProfileSearchUtil.normalizePhoneNumber(elemValue);
									if (normalizedValue != null && !normalizedValue.equals(elemValue)) {
										fields.add(new FieldImp(normalizedValue, normalizedIndexFieldName));
									}

								}
								else if (PropertyEnum.MANAGER_UID.equals(propertyEnum)) {
									// if this is a manager uid, then add the user id
									fields.add(new FieldImp(entry.getManagerUserid(), ProfilesIndexConstants.FIELD_MANAGER_USERID_ID));
									fields.add(new FieldImp(elemValue, indexFieldName));
								}
								else if (PropertyEnum.SECRETARY_UID.equals(propertyEnum)) {
									// if this is a secretary uid, then add the secretary display name
									String secretaryName = entry.getSecretaryName();
									if (secretaryName != null) {
										fields.add(new FieldImp(secretaryName, ProfilesIndexConstants.FIELD_SECRETARY_DISPLAY_NAME_ID));
									}
									fields.add(new FieldImp(elemValue, indexFieldName));
								}
								else if (PropertyEnum.ORG_ID.equals(propertyEnum)) {
									String orgTitle = entry.getOrganizationTitle();
									if (orgTitle != null)
									{
										fields.add(new FieldImp(orgTitle, ProfilesIndexConstants.FIELD_ORGANIZATION_TITLE_ID));
									}
									// add orgId
									fields.add(new FieldImp(elemValue, indexFieldName));
								}
								else if (PropertyEnum.HOME_TENANT_KEY.equals(propertyEnum)) {
									if (isLotusLive) {
										// if there is a home org ID (external user), then add the home org ID 
										// New post-IC5.5 with visitor model support
										boolean isExternal = entry.isExternal();
										if (isExternal) {
											// add home orgId
											fields.add(new FieldImp(elemValue, indexFieldName));
											if (FINEST) {
												logger.log(Level.FINEST, "Adding the home org ID field: " + indexFieldName
														+ " : " + property.getRef() + " : " + (String) elem + " : " + elemValue);
											}
										}
									}
								}
								else if (PropertyEnum.DEPT_NUMBER.equals(propertyEnum)) {
									String departmentTitle = entry.getDepartmentTitle();
									if (departmentTitle != null)
									{
										fields.add(new FieldImp(departmentTitle, ProfilesIndexConstants.FIELD_DEPARTMENT_TITLE_ID));
									}
									// add deptNumber
									fields.add(new FieldImp(elemValue, indexFieldName));
								}								
								else if ((PropertyEnum.DESCRIPTION.equals(propertyEnum))
									||   (PropertyEnum.EXPERIENCE.equals(propertyEnum)))
								{
									// PMR: 00591,070,724 : Search doesn't find umlaut in profile background/experience fields
									// RTE is now passing us partially encoded HTML from these rich-text fields.
									// SeedList doesn't handle that well, so, strip it out and pass them plain text for indexing.
									HtmlPlainTextTransformUtil util = new HtmlPlainTextTransformUtil();
									StringBuilder  content = new StringBuilder(elemValue);
									util.html2text(content, 0, content.length());
									String value = content.toString();
									fields.add(new FieldImp(value, indexFieldName));
								}
								else {
									// no special processing, just add the value
									fields.add(new FieldImp(elemValue, indexFieldName));
								}
							}
						}
					}
					else {
						if (FINER) {
							logger.log(Level.FINER, "The application does not support indexing the base field:" + property.getRef());
						}
					}
				}
			}
			else {
				if (FINER) {
					logger.log(Level.FINER, "Profile field:" + property.getRef()
							+ " is not indexable per its type definition.  It is excluded from the seedlist");
				}
			}
		}

		// Add fields from auxiliary tables; if errors occur do not fail SeedList process
		addTagsForEmployee(fields);
		addConnectionsForEmployee(fields);

		// Add the full Atom URL, new since 3.0.1.1
		String atomUrl = getProfilesAtomURL(entry.getUserid());
		fields.add(new FieldImp(atomUrl, ProfilesIndexConstants.FIELD_ATOMAPISOURCE_ID));

		Field[] result = fields.toArray(new Field[0]);
		if (FINEST) {
			logger.log(Level.FINEST, "Profiles SeedList : " + fields.size() + " fields");
			for (int i=0; i < fields.size(); i++) {
				Field aField = result[i];
				logger.log(Level.FINEST, "[" + (i+1) + "]  " + aField.toString());
			}
		}
		if (FINER) {
			logger.exiting(CLASSNAME, method, result);
		}

		return result;
	}

	private boolean isPhoneNumber(String attrName) {

		return (PropertyEnum.TELEPHONE_NUMBER.getValue().equals(attrName) || PropertyEnum.IP_TELEPHONE_NUMBER.getValue().equals(attrName)
				|| PropertyEnum.MOBILE_NUMBER.getValue().equals(attrName) || PropertyEnum.FAX_NUMBER.getValue().equals(attrName)
				|| PropertyEnum.PAGER_NUMBER.getValue().equals(attrName));
	}

	private String getProfilesEntryURL(Employee profile) {
		final String method  = "getProfilesEntryURL";
		final boolean FINER  = logger.isLoggable(Level.FINER);
		if (FINER) {
			logger.entering(CLASSNAME, method + "(profile)", new Object[] { profile });
		}

		StringBuilder builder = new StringBuilder(getProfilePrefix());
		builder.append("/html/profileView.do?key=" + profile.getKey());
		String result = builder.toString();

		if (FINER) {
			logger.exiting(CLASSNAME, "getProfilesEntryURL(profile)", new Object[] { profile });
		}

		return result;
	}

	private void addTagsForEmployee(List<Field> fields) throws SeedlistException {
		final String method  = "addTagsForEmployee";
		final boolean FINER  = logger.isLoggable(Level.FINER);
		final boolean FINEST = logger.isLoggable(Level.FINEST);

		if (FINER) {
			logger.entering(CLASSNAME, method, key);
		}

		try {			
			// this list contains all tags across all types (linked hashset so we can maintain ordering when iterating out results)
			List<String> allTags = new ArrayList<String>();
			// the set contains formatted string of form {profileTag.userId}{profileTag.tag}, and is used to ensure that duplicate tagger entries
			// are not inserted if the same tag label is applied across tag categories (i.e. connections in other tags, and product tags)
			Set<String> allTaggersUidWithTag = new HashSet<String>();
			
			// this list contains the tagger information paired with the index refId for search to match to a tag
			List<Pair<String, Integer>> allTaggers = new ArrayList<Pair<String, Integer>>();			
			List<Pair<String, Integer>> allTaggersUid = new ArrayList<Pair<String, Integer>>();
			
			// these maps contain all tags by type
			Map<String, List<String>> tagsByType = new HashMap<String, List<String>>();
			// this list contains the tagger information paired with the index refId for search to match to a tag
			Map<String, List<Pair<String, Integer>>> taggersByType = new HashMap<String, List<Pair<String, Integer>>>();
			Map<String, List<Pair<String, Integer>>> taggerUidsByType = new HashMap<String, List<Pair<String, Integer>>>();
			
			// for each type configured, populate empty list
			for (TagConfig tagConfig : DMConfig.instance().getTagConfigs().values()) {
				tagsByType.put(tagConfig.getType(), new ArrayList<String>());
				taggersByType.put(tagConfig.getType(), new ArrayList<Pair<String, Integer>>());
				taggerUidsByType.put(tagConfig.getType(), new ArrayList<Pair<String, Integer>>());				
			}
			
			// get the tag cloud for the person
			ProfileTagService tagSvc = AppServiceContextAccess.getContextObject(ProfileTagService.class);		
			Employee profile = profileDesc.getProfile();
			String   profKey = profile.getKey();
			ProfileLookupKey plk = ProfileLookupKey.forKey(profKey);
			if (FINEST) {
				logger.log(Level.FINEST, "look up tagCloud for user : " + profKey +  " / " + profile.getDisplayName() );
			}

			ProfileTagCloud tagCloud = null;
			try {
				tagCloud = tagSvc.getProfileTagCloud(plk, Verbosity.RESOLVE_CONTRIBUTORS);
			}
			catch (Exception ex) {
				reportError(ex, method, "tags", profileDesc);
			}

			if (null == tagCloud) {
				if (FINEST) {
					logger.log(Level.FINEST, "tagCloud is NULL");
				}
			}
			else {
				String profileKey = tagCloud.getTargetKey();
				List<ProfileTag> profileTags = tagCloud.getTags();
				if (null != profileTags) {
					int numTags = profileTags.size();
					if (FINEST) {
						logger.log(Level.FINEST, "tagCloud for user profile key : " + profileKey +
								((numTags >0) ? " has  " + numTags + " tags" : " is EMPTY"));
						if (numTags >0) {
							int i = 0;
							for (ProfileTag tag : profileTags) {
								logger.log(Level.FINEST, "addTagsForEmployee: (" + i++ + ") " + tag.getTag());
							}
						}
					}
				}

				// iterate and place into proper bucket
				for (ProfileTag profileTag : profileTags) {
					String tag = profileTag.getTag();
					String type = profileTag.getType();
					List<String> tagsForType = tagsByType.get(type);
					List<Pair<String, Integer>> taggersForType = taggersByType.get(type);
					List<Pair<String, Integer>> taggerUidsForType = taggerUidsByType.get(type);
					if (FINEST) {
						logger.log(Level.FINEST, "processing tag : " + tag);
					}
					// this is used as the refId to correlate with search seedlist
					Integer indexForTagsForType = tagsForType.size();

					// its possible the same tag label appears in multiple categories, we do not want it to appear multiple times in the consolidated list
					boolean isRepeatAcrossCategory = allTags.contains(tag);
					Integer indexForAllTags = isRepeatAcrossCategory ? allTags.indexOf(tag) : allTags.size();

					if (tagsForType != null) {
						tagsForType.add(tag);
					}

					// tag information
					if (!isRepeatAcrossCategory) {
						allTags.add(tag);
					}				

					// tagger information
					boolean haveTaggers = true;
					String[] taggers = profileTag.getSourceKeys();
					int numTaggers = 0;
					if (null == taggers) {
						haveTaggers = false;
						if (FINEST) logger.log(Level.FINEST, "tagCloud taggers is NULL for tag : " + tag);						
					}
					else {
						numTaggers = taggers.length;
						if (numTaggers == 0) {
							haveTaggers = false;
							if (FINEST) logger.log(Level.FINEST, "tagCloud taggers is EMPTY for tag : " + tag);						
						}
					}
					if (haveTaggers) {
						if (FINEST) {
							logger.log(Level.FINEST, "processing : " + numTaggers + " taggerIds for tag : " + tag);
						}
						int i = 0;
						for (String taggerId : taggers) {
							if (FINEST) {
								logger.log(Level.FINEST, "processing taggerId ["+ (i++) +"]: " + taggerId);
							}
							Map<String, Employee> contributors = tagCloud.getContributors();
							if (null != contributors) {
								Employee tagger = contributors.get(taggerId);
								if (tagger != null) {
									String displayName = tagger.getDisplayName();
									String userid = tagger.getUserid();

									String taggerUidWithTag = new StringBuilder().append(userid).append(tag).toString();

									if (FINEST) {
										logger.log(Level.FINEST, "tagCloud tagger is " + displayName + " " + taggerUidWithTag);
									}

									// prevent duplicates
									if (!allTaggersUidWithTag.contains(taggerUidWithTag)) {
										if (FINEST) {
											logger.log(Level.FINEST, "adding allTaggers : " + displayName + " / " + userid + " / " + indexForAllTags);
										}
										allTaggers.add(new Pair<String, Integer>(displayName, indexForAllTags));
										allTaggersUid.add(new Pair<String, Integer>(userid, indexForAllTags));
										allTaggersUidWithTag.add(taggerUidWithTag);
									}

									if (taggersForType != null) {
										if (FINEST) {
											logger.log(Level.FINEST, "adding taggersForType : " + displayName + " / " + indexForTagsForType);
										}
										taggersForType.add(new Pair<String, Integer>(displayName, indexForTagsForType));
									}

									if (taggerUidsForType != null) {
										if (FINEST) {
											logger.log(Level.FINEST, "adding taggerUidsForType : " + userid + " / " + indexForTagsForType);
										}
										taggerUidsForType.add(new Pair<String, Integer>(userid, indexForTagsForType));
									}
								}
								else logger.log(Level.FINEST, "tagCloud tagger is NULL for taggerId " + taggerId);
							}
							else logger.log(Level.FINEST, "tagCloud contributors is NULL");
						}
					}
					else logger.log(Level.FINEST, "tagCloud taggers is NULL");

				}

				// output the all tags bucket with proper ordering of field values
				for (int i=0; i < allTags.size(); i++) {
					String tag = allTags.get(i);
					LConnField tagString = new LConnFieldImp(tag, ProfilesIndexConstants.FIELD_TAG_ID, i);
					if (FINEST) {
						logger.log(Level.FINEST, "adding tagString : " + tagString + " to fileds");
					}					
					fields.add(tagString);
				}
				for (int i=0; i < allTaggers.size(); i++) {
					Pair<String, Integer> tagger = allTaggers.get(i);
					LConnField taggerString = new LConnFieldImp(tagger.getFirst(), ProfilesIndexConstants.FIELD_TAGGER, tagger.getSecond());
					if (logger.isLoggable(Level.FINEST)) {
						logger.log(Level.FINEST, "adding taggerString : " + taggerString + " to fileds");
					}					
					fields.add(taggerString);
				}
				for (int i=0; i < allTaggersUid.size(); i++) {
					Pair<String, Integer> taggerUid = allTaggersUid.get(i);
					LConnField taggerUidString = new LConnFieldImp(taggerUid.getFirst(), ProfilesIndexConstants.FIELD_TAGGER_UID, taggerUid.getSecond());
					if (logger.isLoggable(Level.FINEST)) {
						logger.log(Level.FINEST, "adding taggerUidString : " + taggerUidString + " to fileds");
					}					
					fields.add(taggerUidString);				
				}
				
				// output the tags per type listing
				for (TagConfig tagConfig : DMConfig.instance().getTagConfigs().values()) {
					String type = tagConfig.getType();
					List<String> tags = tagsByType.get(type);
					List<Pair<String, Integer>> taggers = taggersByType.get(type);
					List<Pair<String, Integer>> taggerUids = taggerUidsByType.get(type);
					String tagField = IndexTagAttribute.getIndexFieldName(IndexTagAttribute.TAG, type);
					String taggerField = IndexTagAttribute.getIndexFieldName(IndexTagAttribute.TAGGER_DISPLAY_NAME, type);
					String taggerUidField = IndexTagAttribute.getIndexFieldName(IndexTagAttribute.TAGGER_UID, type);
					for (int i=0; i < tags.size(); i++) { 
						String tag = tags.get(i);
						LConnField tagString = new LConnFieldImp(tag, tagField, i);
						if (FINEST) {
							logger.log(Level.FINEST, "adding tagString : " + tagString + " to fileds");
						}					
						fields.add(tagString);
					}
					for (int i=0; i < taggers.size(); i++) {
						Pair<String, Integer> tagger = taggers.get(i);
						LConnField taggerString = new LConnFieldImp(tagger.getFirst(), taggerField, tagger.getSecond());
						if (FINEST) {
							logger.log(Level.FINEST, "adding taggerString : " + taggerString + " to fileds");
						}					
						fields.add(taggerString);
					}
					for (int i=0; i < taggerUids.size(); i++) {
						Pair<String, Integer> taggerUid = taggerUids.get(i);
						LConnField taggerUidString = new LConnFieldImp(taggerUid.getFirst(), taggerUidField, taggerUid.getSecond());
						if (FINEST) {
							logger.log(Level.FINEST, "adding taggerUidString : " + taggerUidString + " to fileds");
						}					
						fields.add(taggerUidString);				
					}				
				}
			}
		}
		catch (Exception ex) {
			if (FINER) {
				logger.throwing(CLASSNAME, method, ex);
			}
			throw new SeedlistException(SeedlistException.TYPE_DOCUMENT_ENTRY_ERROR, ex.getMessage(), ex);
		}

		if (FINER) {
			logger.exiting(CLASSNAME, method);
		}
	}

	private void addConnectionsForEmployee(List<Field> fields) throws SeedlistException
	{
		final String method  = "addConnectionsForEmployee";
		final boolean FINER  = logger.isLoggable(Level.FINER);
		final boolean FINEST = logger.isLoggable(Level.FINEST);

		if (FINER) {
			logger.entering(CLASSNAME, method, key);
		}

		Employee profile = profileDesc.getProfile();
		String   profKey = profile.getKey();
		// get the connections by type
		Map<ConnectionTypeConfig, List<Employee>> connections = null;
		try {
			connections = profileDesc.getConnections();
			for (ConnectionTypeConfig ctc : connections.keySet()) {

				//this should never happen
				if (!ctc.isIndexed())
				{
					continue;
				}

				// iterate over the target employees
				List<Employee> employees = connections.get(ctc);
				for (Employee employee : employees) {
					fields.add(new FieldImp(employee.getDisplayName(), IndexAttributeForConnection.getIndexFieldName(IndexAttributeForConnection.TARGET_USER_DISPLAY_NAME, ctc.getType())));
					fields.add(new FieldImp(employee.getUserid(), IndexAttributeForConnection.getIndexFieldName(IndexAttributeForConnection.TARGET_USER_UID, ctc.getType())));
				}
			}
		}
		catch (Exception ex) {
			reportError(ex, method, "connections", profileDesc);
		}

		if (FINER) {
			logger.exiting(CLASSNAME, method);
		}
	}

	private void reportError(Exception ex, String method, String msg, IndexerProfileDescriptor profDesc)
	{
		final boolean FINEST = logger.isLoggable(Level.FINEST);
		StringBuilder sb = new StringBuilder();
		// swallow exception & do not it propagate to SeedList; report the fatal error (in case anyone is looking at logs)
		sb.append("Profiles seedlist: failed to index ");
		sb.append(msg);
		if ( profDesc != null && profDesc.getProfile() != null ) {
			Employee profile = profDesc.getProfile();
			String   profKey = profile.getKey();
			sb.append(" for user : " + profKey);
			sb.append(" userId = "   + profile.getUserid());
			sb.append(", userName = " + profile.getDisplayName());
		}
		sb.append("; with exception: " + ex.getMessage());
		logger.severe(sb.toString() );

		if (FINEST) {
			logger.log(Level.FINEST, method + ": failed - " + ex);
		}
//		throw new SeedlistException(SeedlistException.TYPE_DOCUMENT_ENTRY_ERROR, ex.getMessage(), ex);
	}

	public String getProfilePrefix() {
		// Using the config setting, get the URL prefix the customer would like to use...
		boolean useSSL = false;
		String prefix = "";

		try {
			useSSL = SSLHelper.forceSSL();
			prefix = ServiceReferenceUtil.getServiceLink("profiles", useSSL);
		}
		catch (Exception ex) {
			logger.throwing(CLASSNAME, "getProfilePrefix", ex);
		}

		return prefix;
	}

	public String getProfilesAtomURL(String userid) {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(CLASSNAME, "getProfilesAtomURL", new Object[] { userid });
		}

		StringBuilder buffer = new StringBuilder();
		String profileURL = getProfilePrefix();
		buffer.append(profileURL);
		buffer.append(ATOM_PROFILES);
		buffer.append("?");
		buffer.append(PeoplePagesServiceConstants.USER_ID);
		buffer.append("=");
		buffer.append(userid);

		String result = buffer.toString();

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(CLASSNAME, "getProfilesAtomURL", result);
		}

		return result;
	}

	private DocumentBuilder newDocumentBuilder() throws SeedlistException {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(CLASSNAME, "newDocumentBuilder");
		}

		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			if (logger.isLoggable(Level.FINER)) {
				logger.exiting(CLASSNAME, "newDocumentBuilder", documentBuilder);
			}
			return documentBuilder;
		}
		catch (ParserConfigurationException e) {
			throw new SeedlistException(SeedlistException.TYPE_INTERNAL_ERROR, e.getMessage(), e);
		}
	}
}
