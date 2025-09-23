/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.ibm.lconn.profiles.config.AbstractNodeBasedConfig;

/**
 * 
 * @author badebiyi
 */
public class UISearchFormConfig {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2627654219310886363L;

	private static final List<String> uiAttrTypes = Arrays.asList(new String[]{"editableAttribute", "attribute",  "extensionAttribute"});
	
	private final List<UIAttributeConfig> attributes;
	private final Map<String,UIAttributeConfig> attributeMap;

	public UISearchFormConfig(List<UIAttributeConfig> attributes, Map<String,UIAttributeConfig> attributeMap){
		this.attributes = attributes;
		this.attributeMap = attributeMap;
	}
	
	@SuppressWarnings("unchecked")
	public UISearchFormConfig(HierarchicalConfiguration configuration){
		
		List<UIAttributeConfig> attributes = new ArrayList<UIAttributeConfig>();
		
		HierarchicalConfiguration.Node root = configuration.getRoot();
		List<HierarchicalConfiguration.Node>children = root.getChildren();
		Iterator iter = children.iterator();
		while(iter.hasNext()){
			HierarchicalConfiguration.Node child = (HierarchicalConfiguration.Node)iter.next();
			if (uiAttrTypes.contains(child.getName())){
				HierarchicalConfiguration attConfig = new HierarchicalConfiguration();
				attConfig.setRoot(child);
				attributes.add(new UIAttributeConfig(attConfig, "label.advanced.searchForm.attribute"));
			}
			
		}
		this.attributes = Collections.unmodifiableList(attributes);
		
		Map<String,UIAttributeConfig> attributeMap = new HashMap<String,UIAttributeConfig>(attributes.size()*2);
		for (UIAttributeConfig attr : attributes) {
			attributeMap.put(attr.getAttributeId(), attr);
		}
		this.attributeMap = Collections.unmodifiableMap(attributeMap);
	}
	
	/**
	 * Ordered list of the UIAttributeConfig(s).
	 * 
	 * @return
	 */
	public List<UIAttributeConfig> getAttributes() {
		return attributes;
	}

	/**
	 * @return the attributeMap
	 */
	public final Map<String, UIAttributeConfig> getAttributeMap() {
		return attributeMap;
	}
	
}
