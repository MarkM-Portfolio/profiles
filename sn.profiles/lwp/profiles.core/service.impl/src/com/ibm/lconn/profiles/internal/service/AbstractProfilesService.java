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

package com.ibm.lconn.profiles.internal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.ibm.lconn.profiles.config.types.MapToNameTableEnum;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.internal.constants.ProfilesServiceConstants;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 *
 */
public abstract class AbstractProfilesService {

	public static final int MAX_KEY_SELECT = 250;

	// this had originally been used to overcome a poor db2 access plan, but is nolonger necessary
	public static final int MAX_JOIN_KEY_SELECT = 250;

	protected final PlatformTransactionManager txManager;

	protected AbstractProfilesService(PlatformTransactionManager txManager) {
		this.txManager = txManager;
	}

	@Deprecated
	protected AbstractProfilesService(TransactionTemplate transactionTemplate) {
		this.txManager = transactionTemplate.getTransactionManager();
	}

	protected final void assertIsAuthenticated() {
		AssertionUtils.assertTrue(AppContextAccess.isAuthenticated(), AssertionType.UNAUTHORIZED_ACTION);
	}

	protected final void assertIsCurrentUser(String key) {
		assertIsAuthenticated();
		AssertionUtils.assertEquals(key, AppContextAccess.getCurrentUserProfile().getKey(), AssertionType.UNAUTHORIZED_ACTION);
	}

	protected final void assertIsCurrentUserOrAdmin(String key) {
		assertIsAuthenticated();
		if (isCurrentUserAdminAdmin())
			return;
		assertIsCurrentUser(key);
	}

	protected final void assertCurrentUserAdmin() {
		assertIsAuthenticated();
		AssertionUtils.assertTrue(isCurrentUserAdminAdmin(), AssertionType.UNAUTHORIZED_ACTION);
	}

	protected final boolean isCurrentUserAdminAdmin() {
		return AppContextAccess.isUserInRole(ProfilesServiceConstants.ROLE_ADMIN);
	}

	protected final void assertCurrentUserSearchAdmin() {
		// assertIsAuthenticated();
		AssertionUtils.assertTrue(AppContextAccess.isUserInRole(ProfilesServiceConstants.ROLE_SEARCH_ADMIN), AssertionType.UNAUTHORIZED_ACTION);
	}

	/**
	 * Utility method to update the name tables with mapped property values from the profile type definition.
	 * 
	 * @param key
	 *     key of the profile record
	 * @param profileTypeId
	 *     profile type identifier of the record
	 * @param userState
	 *     the user state of the record
	 * @param profileRecord
	 *     the values that are updated during the profile record edit
	 */
	protected final void updateNameTablesForMappedProperties(String key, String profileTypeId, UserState userState, UserMode userMode, Map<String, Object> profileRecord)
	{
		GivenNameService givenNameService = AppServiceContextAccess.getContextObject(GivenNameService.class);
		SurnameService surnameService = AppServiceContextAccess.getContextObject(SurnameService.class);
		ProfileType profileType = ProfileTypeHelper.getProfileType(profileTypeId, true);
		List<String> surnamesToPersist = new ArrayList<String>(2);
		List<String> givenNamesToPersist = new ArrayList<String>(2);      
		boolean hasMappedSurnameFields = false;
		boolean hasMappedGivennameFields = false;      
		for (Property property : profileType.getProperties())
		{
			MapToNameTableEnum mapToNameTableEnum = property.getMapToNameTable();
			if (mapToNameTableEnum != null)
			{
				String attributeId = property.isExtension() ? Employee.getAttributeIdForExtensionId(property.getRef()) : property.getRef();          
				hasMappedSurnameFields |= MapToNameTableEnum.SURNAME.equals(mapToNameTableEnum);
				hasMappedGivennameFields |= MapToNameTableEnum.GIVENNAME.equals(mapToNameTableEnum);
				String value = null;
				if (property.isExtension())
				{            
					ProfileExtension profileExtension = (ProfileExtension)profileRecord.get(attributeId);
					if (profileExtension != null)
					{
						value = profileExtension.getStringValue();
					}           
				}
				else
				{
					value = (String)profileRecord.get(attributeId);
				}

				// set the value, trim if necessary
				if (value != null && value.length() > 0)
				{
					// clip if too long for DB
					if (value.length() > 128)
					{
						value = value.substring(0, 128);
					}

					// store all names in lower case
					value = value.toLowerCase(Locale.ENGLISH);

					if (MapToNameTableEnum.SURNAME.equals(mapToNameTableEnum))
					{
						surnamesToPersist.add(value);
					}
					else if (MapToNameTableEnum.GIVENNAME.equals(mapToNameTableEnum))
					{
						givenNamesToPersist.add(value);
					}
				}           
			}       
		}

		if (hasMappedSurnameFields)
		{
			surnameService.setNames(key, NameSource.UserDefined, userState, userMode, surnamesToPersist);
		}
		if (hasMappedGivennameFields)
		{
			givenNameService.setNames(key, NameSource.UserDefined, userState, userMode, givenNamesToPersist);
		}

	}
}
