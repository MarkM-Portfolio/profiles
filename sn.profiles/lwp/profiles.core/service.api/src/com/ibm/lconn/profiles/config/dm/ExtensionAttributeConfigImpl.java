/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config.dm;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.ibm.lconn.profiles.config.AbstractNodeBasedConfig;

/**
 * 
 * @author ahernm@us.ibm.com
 *
 */
public abstract class ExtensionAttributeConfigImpl extends AbstractNodeBasedConfig implements ExtensionAttributeConfig
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4951110382933413171L;
	
	protected final ExtensionType extensionType;
	protected final String extensionId;
	protected final String sourceKey;
	protected final String userLabel;
	protected final String userDataType;
	
	public ExtensionAttributeConfigImpl(ExtensionType extensionType, HierarchicalConfiguration configuration)
	{
		super(configuration);
		this.extensionType = extensionType;
		this.extensionId = configuration.getString("[@extensionId]");
		this.sourceKey = configuration.getString("[@sourceKey]");
		this.userLabel = configuration.getString("[@userLabel]");
		this.userDataType = configuration.getString("[@userTypeString]");
	}
	
	public final String getExtensionId() 
	{
		return extensionId;
	}

	public final ExtensionType getExtensionType() 
	{
		return extensionType;
	}

	public final String getSourceKey() {
		return sourceKey;
	}

	public final String getUserLabel() {
		return userLabel;
	}

	public final String getUserDataType() {
		return userDataType;
	}

	
}
