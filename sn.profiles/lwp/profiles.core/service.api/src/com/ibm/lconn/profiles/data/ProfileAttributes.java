/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;

import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig;
import com.ibm.lconn.profiles.internal.util.ConfigCache;
import com.ibm.peoplepages.data.Employee;

/**
 * Utility class to retrieve list of all (read-only and editable) attributes for
 * a 'profile'.
 * 
 * These attributes all map to fields that are physically stored in the DB.
 * Logical attributes such as 'userid' (which is actually the 'uid' or 'guid'
 * attribute) are not included.
 * 
 *
 */
public class ProfileAttributes {
	
	/**
	 * Method to access all attributes
	 * @return
	 */
	public static List<Attribute> getAll() {
		return ConfigCache.getConfigObj(AttributesInitializer.INSTANCE);
	}

	/**
	 * Attribute definition
	 */
	public final static class Attribute {		
		private String attrId;
		private boolean extension;
		
		public Attribute(String attrId, boolean extension) {
			this.attrId = attrId;
			this.extension = extension;
		}
		
		/**
		 * @return the name
		 */
		public final String getAttrId() {
			return attrId;
		}
		
		/**
		 * @return the extension
		 */
		public final boolean isExtension() {
			return extension;
		}
	}
	
	/**
	 * Config initializer
	 */
	private static final class AttributesInitializer implements ConfigCache.ConfigInitializer<List<Attribute>> {
		public static final AttributesInitializer INSTANCE = new AttributesInitializer();

		public List<Attribute> newConfigObject() {
			try {
				List<Attribute> ms = new ArrayList<Attribute>();
				
				XMLConfiguration config = new XMLConfiguration(ProfileAttributes.class.getResource("ProfileAttributes.xml"));
				
				// hanlde simple mappings
				for (String key : config.getStringArray("sa[@attrId]"))
					ms.add(new Attribute(key, false));
				
				// handle extension attributes
				// the 'key' is of the form 'extattr.${extensionId}'
				for (ExtensionAttributeConfig eac : DMConfig.instance().getExtensionAttributeConfig().values())
					ms.add(new Attribute(Employee.getAttributeIdForExtensionId(eac.getExtensionId()), true));
				
				return Collections.unmodifiableList(ms);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
