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

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;

import com.ibm.lconn.core.appext.util.SNAXDataUtil;

/**
 * Nice base class to provide 'toString' functionality
 * 
 * 
 */
public abstract class AbstractConfigObject implements BaseConfigObject {
	private static final long serialVersionUID = 1319796775125984787L;
	
	/**
	 * Empty config object to save on object creation
	 */
	public static final HierarchicalConfiguration EMPTY_HCONFIG = new HierarchicalConfiguration();

	/**
	 * Utility method to ensure useful debugging of config beans
	 * 
	 * @return Map.toString() based version of the underlying class based on
	 *         'bean' methods
	 */
	@Override
	public String toString() {
		return SNAXDataUtil.toString(this);
	}
	
	/**
	 * Utility method to dump keys
	 * 
	 * @param config Configuration object
	 * @return keys as a StringBuilder
	 */
	protected StringBuilder dumpKeys(Configuration config) {
		StringBuilder sb = new StringBuilder();
		Iterator<?> keys = config.getKeys();
		
		while (keys.hasNext())
			sb.append(keys.next()).append("\n");
		
		return sb;
	}
}
