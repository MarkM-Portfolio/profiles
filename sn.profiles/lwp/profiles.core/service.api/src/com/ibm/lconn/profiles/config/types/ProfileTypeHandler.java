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

package com.ibm.lconn.profiles.config.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.ibm.lconn.profiles.data.Tenant;

/**
 * The SAX handler for parsing a profile-type configuration file. The SAX handler is seeded with the ancestor scope that types defined in
 * the file parsed MUST inherit. If an invalid type is referenced, a parse exception is thrown. If circular type dependencies are detected,
 * a parse exception is thrown. Upon completion of the parse, the fetched set of profile type definitions are fully populated with all
 * properties per its hierarchy.
 */
public class ProfileTypeHandler extends DefaultHandler {

	// the logger
	private static final Logger logger = Logger.getLogger(ProfileTypeHandler.class.getName());
	
	private ProfileTypeImpl curProfileType;

	private PropertyImpl curProperty;

	private Map<String, ProfileTypeImpl> profileTypes;

	private List<PropertyImpl> properties;

	private boolean parseProfileType = false;

	private boolean parseProfileTypeProperty = false;

	private StringBuilder bodyText;

	private Attributes curAttributes;

	private Map<String, ProfileTypeImpl> parentScope;

	private Map<String, ExtensionType> extensionProperties;

	private boolean parseProperties;
	
	private String tenantKey;

	/**
	 * @param parentScope
	 *            the parent type hierarchy that this instance derives
	 * @param parseProperties
	 *            a flag to control if properties should be parse in a profile-type definition
	 * @param extensionProperties
	 *            the set of globally defined extension property fields
	 */
	public ProfileTypeHandler(Map<String, ProfileTypeImpl> parentScope, boolean parseProperties,
			Map<String, ExtensionType> extensionProperties, String tenantKey) {
		this.parentScope = parentScope;
		this.extensionProperties = extensionProperties;
		this.parseProperties = parseProperties;
		this.tenantKey = tenantKey;
	}

	@Override
	public void startDocument() throws SAXException {
		profileTypes = new HashMap<String, ProfileTypeImpl>();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		bodyText.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		QName currentElement = new QName(uri, localName, "");

		if (isEqual(ProfileTypeConstants.TYPE, currentElement)) {			
			curProfileType.setProperties(properties);
			profileTypes.put(curProfileType.getId(), curProfileType);
			parseProfileType = false;
		}
//		else if (isEqual(ProfileTypeConstants.CONFIG,currentElement)){
//			// 'config' is the last element in the doc
//			// before we return, make sure all profiletypes have the orgId we found
//			Set<String> keys = profileTypes.keySet();
//			for (String key : keys){
//				ProfileTypeImpl pti = profileTypes.get(key);
//				if (pti != null) pti.setOrgId(orgId);
//			}
//		}
		else if (parseProperties && isEqual(ProfileTypeConstants.PROPERTY, currentElement)) {
			// validate the property
			if (!curProperty.isExtension())
			{
				// ensure the indexable field is consistent with the base enum definition
				PropertyEnum propertyEnum = PropertyEnum.getByValue(curProperty.getRef());
				if (curProperty.isFullTextIndexed() && !propertyEnum.isFullTextIndexed())
				{
					curProperty.setFullTextIndexed(false);					
					logger.log(Level.WARNING, "The property, " + propertyEnum.getValue() + ", was marked for inclusion in the search index, but this property cannot be indexed.  Update your profiles-types.xml for this property to set its fullTextIndexed element to false.  This property will not be indexed.");				
				}
				// ensure a proper default value is in place for name mapping on standard fields if none is specified
				// this is to preserve the UI edit behavior of edits to preferredFirstName/preferredLastName appear in respective table
				if (curProperty.getMapToNameTable() == null)
				{
				  if (PropertyEnum.PREFERRED_FIRST_NAME.equals(propertyEnum))
				  {
				    curProperty.setMapToNameTable(MapToNameTableEnum.GIVENNAME);
				  } else if (PropertyEnum.PREFERRED_LAST_NAME.equals(propertyEnum))
				  {
				    curProperty.setMapToNameTable(MapToNameTableEnum.SURNAME);
				  }
				}
			}				
			properties.add(curProperty);
			parseProfileTypeProperty = false;
		}
		else if (parseProfileTypeProperty) {
			if (isEqual(currentElement, ProfileTypeConstants.REF)) {
				String ref = normalize(bodyText);
				boolean extension = extensionProperties.keySet().contains(ref);
				ExtensionType extensionType = extensionProperties.get(ref);
				if (ExtensionType.RICHTEXT.equals(extensionType)) {
					curProperty.setRichText(true);
				}

				if (!extension) {
					// validate the field is a standard field that the application knows about
					PropertyEnum propertyEnum = PropertyEnum.getByValue(ref);
					if (propertyEnum == null) {
						throw new SAXException(
								"An invalid property ref, "
										+ ref
										+ " was found in the profiles-types.xml.  The property referenced is not a valid standard or extension field.");
					}
				}

				curProperty.setRef(ref);
				curProperty.setExtension(extension);
				curProperty.setExtensionType(extensionType);
				curProperty.setRef(normalize(bodyText));
			}
			else if (isEqual(currentElement, ProfileTypeConstants.UPDATABILITY)) {
				Updatability updatability = Updatability.getByValue(normalize(bodyText));
				if (updatability == null) {
					throw new SAXException("An invalid updatability declaration was found, " + normalize(bodyText)
							+ ".  Valid values are read and readwrite.");
				}
				curProperty.setUpdatability(Updatability.getByValue(normalize(bodyText)));
			}
			else if (isEqual(currentElement, ProfileTypeConstants.HIDDEN)) {
				curProperty.setHidden(Boolean.valueOf(normalize(bodyText)));
			}
			else if (isEqual(currentElement, ProfileTypeConstants.RICH_TEXT)) {
				curProperty.setRichText(Boolean.valueOf(normalize(bodyText)));
			}
			else if (isEqual(currentElement, ProfileTypeConstants.FULL_TEXT_INDEXED)) {			
				curProperty.setFullTextIndexed(Boolean.valueOf(normalize(bodyText)));
			}
			else if (isEqual(currentElement, ProfileTypeConstants.MAP_TO_NAME_TABLE)) {
			  String nameKey = normalize(bodyText);
			  MapToNameTableEnum nameEnum = MapToNameTableEnum.getByValue(nameKey);
			  if (nameEnum != null)
			  {
			    curProperty.setMapToNameTable(nameEnum);
			  }
			  if (nameEnum == null && nameKey != null && nameKey.length() > 0)
			  {
			    StringBuilder errorMsg = new StringBuilder("Encountered an invalidMapToNameTable declaration, ").append(nameKey).append(". Valid values are givenName, surname, and none.  Please update the file and restart the application.");
			    logger.log(Level.SEVERE, "-- " + errorMsg.toString());
			    throw new SAXException(errorMsg.toString());                
			  }
			}
			else if (isEqual(currentElement, ProfileTypeConstants.LABEL)) {
				// only support default label for extension attributes
				if (curProperty.isExtension()) {
					String label = normalize(bodyText);
					String attrName, attrValue;
					Updatability updatability = Updatability.READ; // default value
					for (int i = 0; i < curAttributes.getLength(); i++) {
						attrName = curAttributes.getLocalName(i);
						if ("updatability".equals(attrName)) {
							attrValue = curAttributes.getValue(i);
							if (updatability == null) {
								throw new SAXException(
										"An invalid updatability attribute was found, "
												+ normalize(bodyText)
												+ ".  Valid values are read and readwrite.");
							}
							updatability = Updatability.getByValue(attrValue);
							break;
						}
					}
					Label defLabel = new Label(label,
							updatability);
					curProperty.setLabel(defLabel);
				}
			}
		}
		else if (parseProfileType) {
			if (isEqual(currentElement, ProfileTypeConstants.ID)) {
				curProfileType.setId(normalize(bodyText));
			}
			else if (isEqual(currentElement, ProfileTypeConstants.PARENT_ID)) {
				curProfileType.setParentId(normalize(bodyText));
			}
			else if (isEqual(currentElement,ProfileTypeConstants.ORG_ID)){
				curProfileType.setOrgId(normalize(bodyText));
			}
		}
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		bodyText = new StringBuilder();
		curAttributes = attributes;
		QName currentElement = new QName(uri, localName, "");
		
		if (isEqual(ProfileTypeConstants.TYPE, currentElement)) {
			curProfileType = new ProfileTypeImpl();
			// set up default values (i.e. the default parentId is 'snx:person'
			curProfileType.setParentId(ProfileTypeConfig.BASE_TYPE_ID);
			curProfileType.setTenantKey(tenantKey);
			properties = new ArrayList<PropertyImpl>(20);
			parseProfileType = true;
		}
		else if (parseProperties && isEqual(ProfileTypeConstants.PROPERTY, currentElement)) {
			curProperty = new PropertyImpl();
			parseProfileTypeProperty = true;
		}
	}

	private boolean isEqual(QName q1, QName q2) {
		String ns1 = q1.getNamespaceURI();
		String ns2 = q2.getNamespaceURI();
		String lp1 = q1.getLocalPart();
		String lp2 = q2.getLocalPart();
		return ns1.equals(ns2) && lp1.equals(lp2);
	}

	private String normalize(StringBuilder sb) {
		return sb.toString().trim();
	}

	/**
	 * Modify the child type definition by inheriting from its immediate parent all properties.
	 * 
	 * @param parent
	 * @param child
	 * @return
	 */
	private void inherit(ProfileTypeImpl parent, ProfileTypeImpl child) {
		// properties on the parent and child
		Map<String, PropertyImpl> parentProperties = parent.getPropertyMap();
		Map<String, PropertyImpl> childProperties = child.getPropertyMap();

		// the total list of properties (after computing inheritance on this object)
		List<PropertyImpl> allProperties = new ArrayList<PropertyImpl>(parentProperties.keySet().size() + childProperties.keySet().size());

		for (PropertyImpl p : parentProperties.values()) {
			// add this property to the all properties list if the child type does not override it
			PropertyImpl overridenProperty = childProperties.get(p.getRef());
			if (overridenProperty == null) {
				// clone the property object, and mark as inherited
				try {
					PropertyImpl clone = (PropertyImpl) p.clone();
					clone.setInherited(true);
					allProperties.add(clone);
				}
				catch (Exception e) {
				}
			}
		}

		for (PropertyImpl p : childProperties.values()) {
			// check if this property is an override of the parent, if so, mark as inherited
			PropertyImpl overridenProperty = parentProperties.get(p.getRef());
			p.setInherited(overridenProperty != null);
			allProperties.add(p);
		}

		// set child properties now to the total set
		child.setProperties(allProperties);
	}

	@Override
	public void endDocument() throws SAXException {
		// we now need to update parsed profile type definitions to support inheritance
		/**
		 * sample input: parentScope = [snx:person] profileTypes = [a -> snx:person, b->c, c -> a] order to process: a, c, b
		 * 
		 * we know any type that extends something in the parent scope can be processed immediately, if a type extends something in its
		 * current scope, we need to make sure we process in order so properties are not missed
		 */

		// this is the id of the profile types in order that we should process hierarchy
		int maxLoops = profileTypes.size();
		int loopCount = 0;
		List<String> inOrder = new ArrayList<String>(profileTypes.size());
		while (loopCount < maxLoops) {
			for (ProfileTypeImpl profileType : profileTypes.values()) {
				// we have already determined that we should process this item
				if (inOrder.contains(profileType.getId()))
					continue;

				// if the type ancestor is in parent scope, we can process right away
				boolean inParentScope = parentScope.get(profileType.getParentId()) != null;
				if (inParentScope) {
					inOrder.add(profileType.getId());
					break;
				}

				// if the parent type is already scheduled for processing, we can now process this
				if (inOrder.contains(profileType.getParentId())) {
					inOrder.add(profileType.getId());
					break;
				}

				// we do not yet know how to compute the hierarchy for this profile type because we have not yet found all its ancestors
			}

			loopCount++;
		}

		// there is an invalid configuration defined because we were not able to compute a valid type hierachy in this scope
		if (inOrder.size() != maxLoops) {
			throw new SAXException(
					"Invalid type hierarchy detected.  Check for dependency loops, or a reference to a parent type id that is not defined in this scope");
		}

		for (String typeId : inOrder) {
			ProfileTypeImpl child = profileTypes.get(typeId);
			ProfileTypeImpl parent = parentScope.get(child.getParentId());
			if (parent == null) {
				parent = profileTypes.get(child.getParentId());
			}
			inherit(parent, child);
		}

		// for each type in parentScope (meaning it was not override) that is not in this scope, just add it to this map to get a new fully
		// defined scope (parent + this level)
		for (String profileTypeId : parentScope.keySet()) {
			if (!profileTypes.containsKey(profileTypeId)) {
				profileTypes.put(profileTypeId, parentScope.get(profileTypeId));
			}
		}
	}

	public Map<String, ProfileTypeImpl> getProfileTypes() {
		return profileTypes;
	}
}
