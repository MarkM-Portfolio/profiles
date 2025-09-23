/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.ibm.lconn.profiles.data.codes.CodeType;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.ventura.internal.config.helper.api.VenturaConfigurationHelper.OrgScopeSettings;

/*
 * Class representing the 'data access' configuration for Profiles.
 * 
 */
public final class DataAccessConfig extends AbstractConfigObject 
{
	private static final long serialVersionUID = 8675655733268714782L;

	/*
	 * Override settings for test
	 */
	public static OrgScopeSettings OVERRIDE_ORG_SETTINGS_FOR_TEST = null;
	
	private static final boolean DEFAULT_ORG_STRUCT_ENABLED = true;
	
	private final DirectoryConfig directoryConfig;
	private final Set<CodeType> profileCodes;
	
	private int defaultPageSize = 10;
	private int maxRowsToReturn = 250;
	
	private boolean organizationalStructureEnabled = false;
	
	private boolean allowJsonpJavelin = false;
	
	private boolean nameOrdering = true;
	
	/*
	 * For WebApp
	 * @param configuration
	 */
	public DataAccessConfig(HierarchicalConfiguration configuration) {
		this.directoryConfig = new DirectoryConfig(
				(HierarchicalConfiguration) configuration.subset("directory"));
		
		EnumSet<CodeType> codeSet = EnumSet.noneOf(CodeType.class);
		List<?> resolveCodes = configuration.subset("resolvedCodes").getList("resolvedCode");
		for (CodeType ct : CodeType.values()) {
			if (resolveCodes.contains(ct.name()))
				codeSet.add(ct);
		}		
		this.profileCodes = Collections.unmodifiableSet(codeSet);
			
		this.defaultPageSize = configuration.getInt("search.pageSize", this.defaultPageSize);
		this.maxRowsToReturn = configuration.getInt("search.maxRowsToReturn", this.maxRowsToReturn);
		this.organizationalStructureEnabled = configuration.getBoolean("organizationalStructureEnabled[@enabled]", DEFAULT_ORG_STRUCT_ENABLED);
		this.allowJsonpJavelin = configuration.getBoolean("allowJsonpJavelin[@enabled]", this.allowJsonpJavelin);
		
		this.nameOrdering = configuration.getBoolean("nameOrdering[@enabled]", this.nameOrdering);
	}
	
	/*
	 * For TDI
	 */
	public DataAccessConfig() {
		this.profileCodes = Collections.emptySet();
		this.directoryConfig = new DirectoryConfig();
	}
	
	/*
	 * Gets default page size for page-able sets
	 */
	public final int getDefaultPageSize() {
		return defaultPageSize;
	}

	/*
	 * Gets max return size for db-based searches
	 */
	public final int getMaxReturnSize() {
		return maxRowsToReturn;
	}

	/*
	 * Returns the 'DirectoryConfig' for WALTZ / Profiles integration
	 * 
	 */
	public final DirectoryConfig getDirectoryConfig() {
		return directoryConfig;
	}
	
	public final boolean isOrgStructureEnabled() {
		return organizationalStructureEnabled;
	}

	/*
	 * @return the allowJsonpJavelin
	 */
	public final boolean isAllowJsonpJavelin() {
		return allowJsonpJavelin;
	}

	/*
	 * @return the firstLastNameOrder
	 */
	public final boolean isNameOrdering() {
		return nameOrdering;
	}

	/*
	 * Returns a set of Code objects that are enabled.  Under the hood this is an optimized EnumSet class for faster ops.
	 */
	public final Set<CodeType> getProfileCodes() {
		return profileCodes;
	}

	/*
	 * Returns the global org scope settings or the override settings if
	 * defined.
	 * 
	 */
	public static final OrgScopeSettings getOrgSettings() {
		if (OVERRIDE_ORG_SETTINGS_FOR_TEST != null) {
			return OVERRIDE_ORG_SETTINGS_FOR_TEST;
		} else {
			return OrgScopeSettings.getInstance();
		}
	}
	
	/*
	 * DirectoryConfig implementation class
	 */
	public static final class DirectoryConfig extends AbstractConfigObject
	{
		private static final long serialVersionUID = 409900545312260354L;
		
		private static final List<String> allLoginAttrs;
		static {
			List<String> l = Arrays.asList(new String[]{
					PeoplePagesServiceConstants.EMAIL, 
					PeoplePagesServiceConstants.UID, 
					PeoplePagesServiceConstants.LOGIN_ID});
			
			allLoginAttrs = Collections.unmodifiableList(l);
		}
		
		
		private String lconnUserIdField = PeoplePagesServiceConstants.GUID;
		private List<String> loginAttributes = allLoginAttrs;
		
		/*
		 * For WebApp
		 */
		protected DirectoryConfig(HierarchicalConfiguration configuration) {
			this.lconnUserIdField = configuration.getString("lconnUserIdField", this.lconnUserIdField);
			this.loginAttributes = Collections.unmodifiableList(Arrays.asList(configuration.getStringArray("loginAttributes.loginAttribute")));
		}
		
		/*
		 * For TDI
		 */
		protected DirectoryConfig() { }
		
		
		/*
		 * Returns the name of the attribute used for the LConnUserId
		 * 
		 */
		public final String getLConnUserIdAttrName() {
			return lconnUserIdField;
		}

		/*
		 * Returns list of possible login fields in Profiles DB
		 */
		public final List<String> getLoginAttributes() {
			return loginAttributes;
		}		
	}
	
	/*
	 * Syntax sugar shortcut
	 */
	public static final DataAccessConfig instance() {
		return ProfilesConfig.instance().getDataAccessConfig();
	}
	
}
