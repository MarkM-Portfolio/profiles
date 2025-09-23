/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config;

import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

/*
 *
 */
public abstract class AbstractNodeBasedConfig extends AbstractConfigObject {
	private static final long serialVersionUID = 1761150443979671851L;

	protected final HierarchicalConfiguration configuration;
	
	public AbstractNodeBasedConfig(HierarchicalConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Access to configuration element
	 * @return HierarchicalConfiguration
	 */
	public final HierarchicalConfiguration getConfiguration() {
		return configuration;
	}
	
	/*
	 * Utility method to walk config hierarchy.
	 * 
	 */
	protected HierarchicalConfiguration getChildConfiguration(String childName) {
		return getChildConfiguration(childName,0);
	}
	
	/*
	 * Utility method to walk config hierarchy.
	 * 
	 */
	@SuppressWarnings("unchecked")
	protected HierarchicalConfiguration getChildConfiguration(String childName, int index) {
		HierarchicalConfiguration childConfig = null;
		List<HierarchicalConfiguration.Node> children = configuration.getRoot().getChildren(childName);
		if (children.size() > index)
		{		
			HierarchicalConfiguration.Node sectionConfigNode = children.get(index);
			childConfig = new HierarchicalConfiguration();
			childConfig.setRoot(sectionConfigNode);
		}
		
		return childConfig;
	}
		
}
