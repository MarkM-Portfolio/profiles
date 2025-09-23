/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

public final class ConfigUtils {
	private ConfigUtils() {}

	/**
	 * Utility method to walk config hierarchy.
	 * 
	 * @param configuration
	 * @param childName
	 * @return
	 */
	public static final HierarchicalConfiguration getChildConfiguration(HierarchicalConfiguration configuration, String childName) {
		return getChildConfiguration(configuration, childName, 0);
	}
	
	/**
	 * Utility method to walk config hierarchy.
	 * 
	 * @param configuration
	 * @param childName
	 * @param index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final HierarchicalConfiguration getChildConfiguration(HierarchicalConfiguration configuration, String childName, int index) {
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
