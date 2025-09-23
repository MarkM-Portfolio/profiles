/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.bss.commands;

import static java.util.logging.Level.FINER;

import org.apache.commons.lang.StringUtils;
import com.ibm.jse.util.xml.XMLUtil;
import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.commands.IPlatformCommandRecord;
import com.ibm.lconn.commands.PlatformCommandResponse;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.resources.ResourceManager;



public class LLISProfileUpdateCommand extends BaseBssCommand {

	private String subscriberExId = null;
	private String customerExId = null;
	private Tenant tenant;
	private String jobTitle;

	private String syncType = null;

	private Employee profile = null;
	private ProfileDescriptor pd = new ProfileDescriptor();
	
	public LLISProfileUpdateCommand(IPlatformCommandRecord command, COMMAND_PHASE phase) {
		super(command, phase);

		subscriberExId = (String) _properties.get(IPlatformCommandConstants.DIRECTORYID);
		if (subscriberExId == null) subscriberExId = (String) _properties.get(IPlatformCommandConstants.SUBSCRIBER_ID); // LLIS uses different key from BSS to denote the profile GUID
		
		customerExId = (String) _properties.get(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID);
		if (customerExId == null) customerExId = (String) _properties.get(IPlatformCommandConstants.CUSTOMER_ID); // LLIS uses different key from BSS to denote the org/tenant ID
		
		// job title may be sent as jobResp or jobTitle (which becomes job_title) in BSS _profiles.csv file.
		// see BSSProvisioningEndpoint.java which converts jobTitle to job_title
		jobTitle = (String) _properties.get(IPlatformCommandConstants.JOB_TITLE);
		
		syncType = (String) _properties.get(IPlatformCommandConstants.SYNC_TYPE);
	}

	@Override
	protected void doPrepare(PlatformCommandResponse response) throws Exception {
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("LLISProfileUpdateCommand.doPrepare", "doPrepare");
		//
		response.setResponseCode(IPlatformCommandConstants.SUCCESS);

		if (validate(response)) {
			// check that the customer (tenant) exists
			tenant = _tdiProfileService.getTenantByExid(customerExId);
			if (tenant == null) {
				String msg = "customer does not exist for LLISProfileUpdateCommand: " + _command;
				processGeneralError(response, msg);
			}
			// todo? check that this email and login does not already exist on an ADD?
			
			profile = getProfileByGuid(subscriberExId);
			syncType = IPlatformCommandConstants.SYNC_UPDATE_TYPE; // LLIS profile update only support update (no add) so force UPDATE but keep sync framework
			
			if (syncType == null) {
				if (profile != null) {
					syncType = IPlatformCommandConstants.SYNC_UPDATE_TYPE;					
				}
				else {
					syncType = IPlatformCommandConstants.SYNC_ADD_TYPE;
				}
			}
			
			// warn if BSS command indicates different sync type but process the command anyway
			else if (syncType.equals(IPlatformCommandConstants.SYNC_ADD_TYPE)) {
				if (profile != null) {
					LOGGER.warning("LLISProfileUpdateCommand received to add an existing subscriber: "+subscriberExId);
					syncType = IPlatformCommandConstants.SYNC_UPDATE_TYPE;
				}
			}
			
			else {
				if (profile == null) {
					LOGGER.warning("LLISProfileUpdateCommand received to update a nonexistent subscriber: "+subscriberExId);
					//syncType = IPlatformCommandConstants.SYNC_ADD_TYPE; 
					response.setResponseCode(IPlatformCommandConstants.FAIL_GENERAL);
					//throw new Exception("LLISProfileUpdateCommand does not allow ADDing new profiles via sync.type ADD; Only UPDATEs are allowed. command: " + _command);
					throw new Exception("LLISProfileUpdateCommand instructed to update a nonexistent subscriber in profiles: command: " + BSSUtil.getString(_command));
				}
			}
		}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("LLISProfileUpdateCommand.doPrepare", "doPrepare");
	}

	@Override
	protected void doExecute(PlatformCommandResponse response) throws Exception {
		// log entry
		if (LOGGER.isLoggable(FINER)) LOGGER.entering("LLISProfileUpdateCommand.doExecute", "doExecute");
		//
		if (validate(response)) {
			doPrepare(response);

			if (IPlatformCommandConstants.SUCCESS.equals(response.getResponseCode())) {
				
				if (syncType.equals(IPlatformCommandConstants.SYNC_UPDATE_TYPE)) {

					// we do not update org or subscriber ids via this command
					// also, let dao layer set tenant info
					profile.setTenantKey(tenant.getTenantKey());
					
					// get the locale.  We may need to translate some labels
					// not yet used
					//String localeStr = (String) _properties.get( IPlatformCommandConstants.LOCALE);
					//Locale theLocale = (Locale)_command.getLocale();
					
					// Get profile type for this subscriber and iterate over the properties of this profile type and see if they exist in the command
					// Update properties that exist in the command payload (with the exception of BSS updatable fields: givenName, email)
					ProfileType profileType = ProfileTypeHelper.getProfileType( profile.getProfileType());

					if (LOGGER.isLoggable(FINER)) LOGGER.log(FINER, "LLISProfileUpdateCommand: Processing "+
							"tenant id ["+ customerExId +"] "+
							"subscriber id ["+ subscriberExId +"] "+
							"display name ["+ profile.getDisplayName() +"] ");
					
					// iterate thru the profile type attributes; any that is found in the command, update the attribute value
					for (Property p : profileType.getProperties()) {
						try {
							String attributeId = p.getRef();
							String valueFromCommand = (String) _properties.get( attributeId.toLowerCase());
							
							// if jobTitle/job_title is sent, it will take precedence.
							if (attributeId.equals(PropertyEnum.JOB_RESP.getValue())){
								if (jobTitle != null) valueFromCommand = jobTitle;
							}

							if( valueFromCommand != null ) {
								if( attributeId != null) {

									if (LOGGER.isLoggable(FINER)) LOGGER.log(FINER, "LLISProfileUpdateCommand: "+
											"preparing to update profile field ["+ attributeId +"] with value "+"["+ valueFromCommand +"] ");
								
									// skip protected, internal, and through-BSS updatable fields 
									if( // key and internal fields
										   attributeId.equals( PropertyEnum.PROFILE_TYPE.getValue()) 
										|| attributeId.equals( PropertyEnum.TENANT_KEY.getValue()) 
										|| attributeId.equals( PropertyEnum.KEY.getValue()) 
										|| attributeId.equals( PropertyEnum.UID.getValue()) 
										|| attributeId.equals( PropertyEnum.GUID.getValue()) 
										|| attributeId.equals( PropertyEnum.USER_ID.getValue()) 
										|| attributeId.equals( PropertyEnum.LOGIN_ID.getValue()) 
									
										// BSS fields
										|| attributeId.equals( PropertyEnum.EMAIL.getValue()) 
										|| attributeId.equals( PropertyEnum.USER_STATE.getValue()) 
										|| attributeId.equals( PropertyEnum.DISTINGUISHED_NAME.getValue()) 
										|| attributeId.equals( PropertyEnum.DISPLAY_NAME.getValue()) 
										|| attributeId.equals( PropertyEnum.GIVEN_NAME.getValue()) 
										|| attributeId.equals( PropertyEnum.SURNAME.getValue()) 
										|| attributeId.equals( PropertyEnum.TIME_ZONE.getValue())
										// see rtc 150249 || attributeId.equals( PropertyEnum.JOB_RESP.getValue())
										
										// other fields to skip/protect
										|| attributeId.equals( PropertyEnum.ORG_ID.getValue()) 
										|| attributeId.equals( PropertyEnum.GROUPWARE_EMAIL.getValue()) 
										|| attributeId.equals( PropertyEnum.USER_MODE.getValue()) 
										|| attributeId.equals( PropertyEnum.LAST_UPDATE.getValue()) 
										|| attributeId.equals( PropertyEnum.EMPLOYEE_TYPE_CODE.getValue()) 
										) {
									
										if (LOGGER.isLoggable(FINER)) LOGGER.log(FINER, "LLISProfileUpdateCommand: "+
												"Skipping profile update for this field.  Supplied attribute ["+ attributeId +"] is in the prohibited update list.");
									
										continue;
									}
								
									if ( p.isExtension()) { // extension attribute field
										String extAttributeId = Employee.getAttributeIdForExtensionId(p.getRef());
										ProfileExtension pe = new ProfileExtension();
										pe.setPropertyId(attributeId);
										pe.setStringValue(XMLUtil.stripInvalidXmlChars(valueFromCommand));
										pe.setKey(profile.getKey());
										profile.put( extAttributeId, pe);
									}
									else { // regular field (non-extension attribute)
								    	profile.put( attributeId, valueFromCommand);
									}
								
									if (LOGGER.isLoggable(FINER)) LOGGER.log(FINER, "LLISProfileUpdateCommand: "+
											"profile field ["+ attributeId +"] updated!");
									
								} else {
									if (LOGGER.isLoggable(FINER)) LOGGER.log(FINER, "LLISProfileUpdateCommand: "+
											"profile field ["+ attributeId +"] is not enabled on this profile type.  No update performed for this field.");
								}
							}
						}
						catch( Exception ex) {
							throw new Exception(
									"LLISProfileUpdateCommand: "+
									"exception caught while attempting to update profile "+
											"field "+"["+ (String)p.getRef() +"] "+
											"with value "+"["+ (String)_properties.get(p.getRef().toLowerCase()) +"] "+
											"for "+
											"tenant id ["+ customerExId +"] "+
											"subscriber id ["+ subscriberExId +"] "+
											"display name ["+ profile.getDisplayName() +"] "+
											"exception message ["+ex.getMessage()+"]");
						}
					}
					
					//-------------------------------------------------------------------------------------------
					// Process special case email proxy fields -> uid fields:
					// managerEmail -> managerUid
					// secretaryEmail -> secretaryUid
					//
					// if xxxEmail field was provided and profileType supports the corresponding xxxxUid field, then
					// 1. if xxxEmail is empty, blank out the corresponding Uid field
					// otherwise,
					// 2. lookup the userid for the xxxEmail provided
					// 3. populate the corresponding xxxUid with the user id of the profile found

					String emailField = null;
					String emailAddress = null;
					String attributeId = null;
					try {
						// managerEmail -> managerUid
						attributeId = PropertyEnum.MANAGER_UID.getValue();
						emailField = "managerEmail";
						emailAddress = (String) _properties.get( emailField.toLowerCase());
						if( emailAddress != null && profileType.getPropertyById(attributeId) != null) {
							updateProfileUidFromProxyEmailField( attributeId, emailAddress);
						}
						
						// secretaryEmail -> secretaryUid
						attributeId = PropertyEnum.SECRETARY_UID.getValue();
						emailField = "secretaryEmail";
						emailAddress = (String) _properties.get( emailField.toLowerCase());
						if( emailAddress != null && profileType.getPropertyById(attributeId) != null) {
							updateProfileUidFromProxyEmailField( attributeId, emailAddress);
						}
					}
					catch( Exception ex) {
						throw new Exception(
								"LLISProfileUpdateCommand: "+
								"exception caught while attempting to update profile with "+
										"supplied proxy field ["+emailField+"] "+
										"for corresponding field "+"["+ attributeId +"] "+
										"with value "+"["+ emailAddress +"] "+
										"for "+
										"tenant id ["+ customerExId +"] "+
										"subscriber id ["+ subscriberExId +"] "+
										"display name ["+ profile.getDisplayName() +"] "+
										"exception message ["+ex.getMessage()+"]");
					}
					
					pd.setProfile(profile);
					_tdiProfileService.update(pd);
					
					response.setResponseCode(IPlatformCommandConstants.SUCCESS);
				}
			}
		}
		// log exit
		if (LOGGER.isLoggable(FINER)) LOGGER.exiting("LLISProfileUpdateCommand.doExecute", "doExecute");
	}

	// update the supplied UID property field with the user id of the profile that belongs to the suppied email 
	private void updateProfileUidFromProxyEmailField( String p, String email) {
		String userId = "";
		if( !StringUtils.isEmpty(email)) {
			Employee emailProfile = getProfileByEmail( email);
			userId = (String)emailProfile.getUserid();
		}
		if (LOGGER.isLoggable(FINER)) LOGGER.log(FINER, "LLISProfileUpdateCommand: updating ["+ p +"] with ["+userId+"]");
		profile.put( p, userId);
	}

	private boolean validate(PlatformCommandResponse response) {
		boolean isValid = true;

		if (StringUtils.isEmpty(subscriberExId)) {
			isValid = false;
			if (LOGGER.isLoggable(FINER)) LOGGER.finer("user id not provided");
			response.setResponseCode(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
			String msg = ResourceManager.format(ResourceManager.BUNDLE_NAME, "error.bss.missingarg",
					IPlatformCommandConstants.DIRECTORYID);
			appendResponseMessage(response,msg);
		}
		else if (StringUtils.isEmpty(customerExId)) {
			isValid = false;
			if (LOGGER.isLoggable(FINER)) LOGGER.finer("customer id not provided");
			response.setResponseCode(IPlatformCommandConstants.FAIL_INVALID_PROPERTY_VALUE);
			String msg = ResourceManager.format(ResourceManager.BUNDLE_NAME, "error.bss.missingarg",
					IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID);
			appendResponseMessage(response,msg);
		}
		return isValid;
	}
}
