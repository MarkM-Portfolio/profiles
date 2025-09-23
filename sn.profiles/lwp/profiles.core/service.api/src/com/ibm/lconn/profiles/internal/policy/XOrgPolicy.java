/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.policy;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.abdera.writer.StreamWriter;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to 'reverse' serialize an OrgPolicy from its map representation to an XML representation. The typical usage is via the
 * API to service a GET operation. The functional policy is the one maintained by the OrgPolicy accessed via PolicyHolder. This
 * representation is a internally a Map object in the OrgPolicy object. This class creates an XML file from the Map representation. The
 * corresponding classes and their purpose are discussed below.
 * 
 * XOrgPolicy - Holds the OrgPolicy object, with its Map, to be converted to an XML representation. The OrgPolicy map holds a set of
 * LookupKey objects which encode the feature, action, and the profile type information (see LookupKey). These keys are associated with a
 * Permission which defines the scope allowed for that combination. The LookupKey string representation is of the form (again, see
 * LookupKey):
 * acl^aclname^^feature^^featureName^tgtId^tgtIdVal^^actorId^actorIdVal^^tgtMode^tgtModeVal^^actorMode^actorModeVal^^tgtType^tgtTypeVal
 * ^^actorType^actorTypeVal In order to create an XML representation from the Map, we must work with the LookupKey (key) and Permission
 * objects (values) maintained by the map. The LookupKey itself encodes information to indicate if it represents a Feature or a Permission.
 * A feature has a generic acl name (*) - see FeatureLookupKey. A permission for a specific acl/action has a name of a specific acl defined
 * by the Acl.java class. The associated Permission defines if a feature is enabled or what scope/relationship an actor and target must have
 * in order for the action to be allowed.
 * 
 * In order to reverse the OrgPolicy map of LookupKey-to-Permission into an XML representation, we have he following classes. XFeature -
 * wraps a feature's LookupKey extracted from the OrgPolicyMap. XPermission - wraps the associated Permission object associated with a
 * feature XLookupKey - extends LookupKey and provides methods to determine if this key represents a Feature or PremissionLookupKey It might
 * help to (attempt to) see a snippet from a profiles-policy.xml file and what parts are incorporated into a LookupKey versus a Permission.
 * <feature name="profile.connection"> name is part of LookupKey <profileType enabled="true"> profileType info part of FeatureLookupKey,
 * enabled is part of Permission <acl name="profile.connection.view" scope="reader" /> name is part of PermissionLookupKey, scope is part of
 * Permission <acl name="profile.connection.message.view" scope="self" /> </profileType> <profileType actorMode="external" enabled="false"
 * /> <profileType mode="external" enabled="false" /> </feature> distinguish if the key represents a FeatureLookupKey or a
 * PermissionLookupKey. XOrgPolicy holds a mapping of a feature name to a collection (Map) of XFeature. In the XML snippet above, think of
 * each 'profileType' node as an XFeature. Each enabled feature will typically hold an associated set of acl/permission nodes. These
 * correspond to (X)Permissions.
 * 
 * The XorgPolicy parses the OrgPolicy map into an intermediate form where (1) feature names are mapped to a map of corresponding XFeature
 * objects. there is an entry per profileType node - as these are the feature specifications (2) Each XFeature holds a collection of
 * XPermission objects. These are the acls/actions allowed for an enabled feature. each XFeature will hold a XPermission for each acl node
 * under it Once this intermediate representation is calculated, creating the XML representation is a straightforward traversal of the
 * features and associated permissions.
 */
public class XOrgPolicy {
	
	// the associated OrgPolicy
	OrgPolicy orgPolicy;
	
	// map feature name to feature/type combinations associated with the feature
	HashMap<String,HashMap<String,XFeature>> featureMap = null; //new HashMap<String,HashMap<String,XFeature>>();

	public XOrgPolicy(OrgPolicy op){
		orgPolicy = op;
	}
	
	String getOrgId(){
		return orgPolicy.getOrgId();
	}
	
	Set<String> getFeatureKeySet(){
		return featureMap.keySet();
	}
	
	HashMap<String,XFeature> getFeatureMap(String featureName){
		return featureMap.get(featureName);
	}
	
	public void serializeToXml(StreamWriter sw) throws IOException {		
		loadFeatureMap();
		// now unravel this mess to xml into the supplied StreamWriter
		serializeXPolicyOrg(sw);
	}
	
	public void SerializeToXml(Document doc) throws IOException {
		loadFeatureMap();
		// now unravel this mess to xml into the supplied StreamWriter
		serializeXPolicyOrg(doc);
	}
	
	private void loadFeatureMap(){
		Map<LookupKey,Permission> keyPerMap = orgPolicy.getMap();
		if (featureMap == null) {
			featureMap = new HashMap<String, HashMap<String, XFeature>>();
			XLookupKey xlk;
			Permission per;
			// loop through and find features
			// example (Feature)LookupKey:  acl^*^^feature^profile.board^^tgtId^standard^^actorId^standard^^tgtMode^internal^^actorMode^internal^^tgtType^default^^actorType^default
			for (Entry<LookupKey, Permission> entry : keyPerMap.entrySet()) {
				xlk = new XLookupKey(entry.getKey());
				per = entry.getValue();
				if (xlk.isFeatureKey()) {
					addFeature(xlk, per);
				}
			}
			// loop and process permissions after we have found the features. the permissions will be added to
			// the previously located XFeatures.
			for (Entry<LookupKey, Permission> entry : keyPerMap.entrySet()) {
				xlk = new XLookupKey(entry.getKey());
				per = entry.getValue();
				if (xlk.isFeatureKey() == false) {
					addPermission(xlk, per);
				}
			}
		}
	}
	
	private void addFeature(XLookupKey xfeatureLK, Permission permission){
		String featureName = xfeatureLK.getFeatureName();
		// find the features mapped to this name
		HashMap<String,XFeature> map = featureMap.get(featureName);
		// create an entry if this is the first time this name is encountered
		if (map == null){
			map = new HashMap<String,XFeature>();
			featureMap.put(featureName,map);
		}
		// add the feature info to the map
		XFeature xft = new XFeature(xfeatureLK, permission);
		String featureKey = xfeatureLK.getXFeatureKeyString();
		map.put(featureKey,xft);
	}
	
	private void addPermission(XLookupKey xpermissionLK, Permission permission){
		// we have a permission, first find the associated feature
		String featureName = xpermissionLK.getFeatureName();
		HashMap<String,XFeature> map = featureMap.get(featureName);
		// expect to find this map
		if (map != null) {
			// find the particular feature to which to add the permission info
			XFeature xft = map.get(xpermissionLK.getXFeatureKeyString());
			// expect to find this xfeature
			if (xft != null){
				//XPolicyAcl xacl = new XPolicyAcl(xpermissionLK,permission);
				XPermission xacl = new XPermission(xpermissionLK.getAclName(),permission);
				xft.addPolicyAcl(xacl);
			}
		}
	}
	
	private void serializeXPolicyOrg(StreamWriter sw) throws IOException {
		// creating a simple document with default namespace and no prefixing.
		sw.startDocument( ); // AtomConstants.XML_ENCODING, AtomConstants.XML_VERSION);
		// config element
		sw.startElement(PolicyConstants.CONFIG.getLocalPart()); //1.
		sw.writeAttribute("id", "profiles-policy");
		sw.writeAttribute("xmlns", PolicyConstants.NS_URI);
		// features
		sw.startElement(PolicyConstants.NODE_FEATURES.getLocalPart()); //2.
		sw.writeAttribute(PolicyConstants.ATTR_ORGID.getLocalPart(),orgPolicy.getOrgId());
		// loop to add each feature
		HashMap<String,XFeature> fMap;
		Collection<XFeature> featureColl;
		Set<XPermission> paclSet;
		//
		Set<String> featureKeySet = getFeatureKeySet();
		for (String fN : featureKeySet){
			sw.startElement(PolicyConstants.NODE_FEATURE.getLocalPart()); //3.
			sw.writeAttribute(PolicyConstants.ATTR_NAME.getLocalPart(),fN);
			fMap = getFeatureMap(fN);
			featureColl = fMap.values();
			String val;
			for (XFeature f : featureColl){
				// filter out default settings, which are not generally documented (as of IC 5.5).
				sw.startElement(PolicyConstants.NODE_PROFILETYPE.getLocalPart()); //4.
				val = f.getTargetIdentity(); // targetIdentity
				if (StringUtils.equals(val,PolicyConstants.DEFAULT_IDENTITY) == false){
					sw.writeAttribute(PolicyConstants.ATTR_IDENTITY.getLocalPart(),val);
				}
				val = f.getActorIdentity(); // actorIdentity
				if (StringUtils.equals(val,PolicyConstants.DEFAULT_IDENTITY) == false){
					sw.writeAttribute(PolicyConstants.ATTR_ACTORIDENTITY.getLocalPart(),val);
				}
				val = f.getTargetMode(); // targetMode
				if (StringUtils.equals(val,PolicyConstants.DEFAULT_MODE) == false){
					sw.writeAttribute(PolicyConstants.ATTR_MODE.getLocalPart(),val);
				}
				val = f.getActorMode(); // actorMode
				if (StringUtils.equals(val,PolicyConstants.DEFAULT_MODE) == false){
					sw.writeAttribute(PolicyConstants.ATTR_ACTORMODE.getLocalPart(),val);
				}
				val = f.getTargetType(); // targetType
				if (StringUtils.equals(val,PolicyConstants.DEFAULT_TYPE) == false){
					sw.writeAttribute(PolicyConstants.ATTR_TYPE.getLocalPart(),val);
				}
				val = f.getActorType(); // actorType
				if (StringUtils.equals(val,PolicyConstants.DEFAULT_TYPE) == false){
					sw.writeAttribute(PolicyConstants.ATTR_ACTORTYPE.getLocalPart(),val);
				}
				sw.writeAttribute(PolicyConstants.ATTR_ENABLED.getLocalPart(),Boolean.toString(f.isEnabled()));
				//sw.startElement(PolicyConstants.NODE_PROFILETYPE.getLocalPart()); //4.
				//sw.writeAttribute(PolicyConstants.ATTR_IDENTITY.getLocalPart(),f.getTargetIdentity());
				//sw.writeAttribute(PolicyConstants.ATTR_ACTORIDENTITY.getLocalPart(),f.getActorIdentity());
				//sw.writeAttribute(PolicyConstants.ATTR_MODE.getLocalPart(),f.getTargetMode());
				//sw.writeAttribute(PolicyConstants.ATTR_ACTORMODE.getLocalPart(),f.getActorMode());
				//sw.writeAttribute(PolicyConstants.ATTR_TYPE.getLocalPart(),f.getTargetType());
				//sw.writeAttribute(PolicyConstants.ATTR_ACTORTYPE.getLocalPart(),f.getActorType());
				//sw.writeAttribute(PolicyConstants.ATTR_ENABLED.getLocalPart(),Boolean.toString(f.isEnabled()));
				paclSet = f.getPolicyAclSet();
				for (XPermission acl : paclSet){
					sw.startElement(PolicyConstants.NODE_ACL.getLocalPart()); //5.
					sw.writeAttribute(PolicyConstants.ATTR_NAME.getLocalPart(),acl.getAclName());
					sw.writeAttribute(PolicyConstants.ATTR_SCOPE.getLocalPart(),acl.getScope().getName());
					sw.endElement(); //5. PolicyConstants.NODE_ACL
				}
				sw.endElement(); //4.PolicyConstants.NODE_PROFILETYPE
			}
			sw.endElement(); //3.PolicyConstants.ATTR_NAME
		}
		sw.endElement(); //2.PolicyConstants.NODE_FEATURES
		sw.endElement(); //1.PolicyConstants.CONFIG
		sw.endDocument();
		//
		// code to print in pseudo xml
		//for (String fN : featureKeySet) {
		//	System.out.println("<feature " + fN + " >");
		//	fMap = featureMap.get(fN);
		//	// System.out.println("  "+fMap.size());
		//	Collection<XFeature> featureColl = fMap.values();
		//	for (XFeature f : featureColl) {
		//		System.out.println("    <policyType " + f.getTargetIdentity() + " " + f.getActorIdentity() + " " + f.getTargetMode() + " "
		//				+ f.getActorMode() + " " + f.getTargetType() + " " + f.getActorType() + " enabled=" + f.isEnabled() + ">");
		//		Set<XPolicyAcl> paclSet = f.getPolicyAclSet();
		//		for (XPolicyAcl acl : paclSet) {
		//			System.out.println("        <acl " + acl.getAclName() + " " + acl.getScope().getName() + " />");
		//		}
		//		System.out.println("    <policyType/>");
		//	}
		//	System.out.println("</feature>");
		//}
	}
	private void serializeXPolicyOrg(Document doc) throws IOException {
		// config element
		Element config = doc.createElementNS(PolicyConstants.NS_URI, PolicyConstants.CONFIG.getLocalPart());
		doc.appendChild(config);
		Attr attr = doc.createAttribute("id");
		attr.setValue("profiles-policy");
		config.setAttributeNode(attr);
		// features
		Element features = doc.createElement(PolicyConstants.NODE_FEATURES.getLocalPart());
		attr = doc.createAttribute(PolicyConstants.ATTR_ORGID.getLocalPart());
		attr.setValue(orgPolicy.getOrgId());
		features.setAttributeNode(attr);
		config.appendChild(features);
		// loop to add each feature
		HashMap<String,XFeature> fMap;
		Collection<XFeature> featureColl;
		Set<XPermission> paclSet;
		//
		Set<String> featureKeySet = getFeatureKeySet();
		for (String fN : featureKeySet) {
			Element feature = doc.createElement(PolicyConstants.NODE_FEATURE.getLocalPart());
			attr = doc.createAttribute(PolicyConstants.ATTR_NAME.getLocalPart());
			attr.setValue(fN);
			feature.setAttributeNode(attr);
			features.appendChild(feature);
			fMap = getFeatureMap(fN);
			featureColl = fMap.values();
			for (XFeature f : featureColl) {
				Element profileType = doc.createElement(PolicyConstants.NODE_PROFILETYPE.getLocalPart());
				feature.appendChild(profileType);
				attr = doc.createAttribute(PolicyConstants.ATTR_IDENTITY.getLocalPart());
				attr.setValue(f.getTargetIdentity());
				profileType.setAttributeNode(attr);
				attr = doc.createAttribute(PolicyConstants.ATTR_ACTORIDENTITY.getLocalPart());
				attr.setValue(f.getActorIdentity());
				profileType.setAttributeNode(attr);
				attr = doc.createAttribute(PolicyConstants.ATTR_MODE.getLocalPart());
				attr.setValue(f.getTargetMode());
				profileType.setAttributeNode(attr);
				attr = doc.createAttribute(PolicyConstants.ATTR_ACTORMODE.getLocalPart());
				attr.setValue(f.getActorMode());
				profileType.setAttributeNode(attr);
				attr = doc.createAttribute(PolicyConstants.ATTR_TYPE.getLocalPart());
				attr.setValue(f.getTargetType());
				profileType.setAttributeNode(attr);
				attr = doc.createAttribute(PolicyConstants.ATTR_ACTORTYPE.getLocalPart());
				attr.setValue(f.getActorType());
				profileType.setAttributeNode(attr);
				attr = doc.createAttribute(PolicyConstants.ATTR_ENABLED.getLocalPart());
				attr.setValue(Boolean.toString(f.isEnabled()));
				profileType.setAttributeNode(attr);
				// get feature acls
				paclSet = f.getPolicyAclSet();
				for (XPermission xacl : paclSet) {
					Element acl = doc.createElement(PolicyConstants.NODE_ACL.getLocalPart());
					profileType.appendChild(acl);
					attr = doc.createAttribute(PolicyConstants.ATTR_NAME.getLocalPart());
					attr.setValue(xacl.getAclName());
					acl.setAttributeNode(attr);
					attr = doc.createAttribute(PolicyConstants.ATTR_SCOPE.getLocalPart());
					attr.setValue(xacl.getScope().getName());
					acl.setAttributeNode(attr);
				}
			}
		}
	}
}
