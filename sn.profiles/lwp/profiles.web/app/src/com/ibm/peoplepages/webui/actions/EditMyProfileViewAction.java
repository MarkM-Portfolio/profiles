/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.actions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.templates.TemplateDataModel;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.Updatability;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;
import com.ibm.lconn.profiles.internal.service.PronunciationService;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AuthHelper;
import com.ibm.peoplepages.util.StringUtil;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.webui.forms.EditProfileForm;

/**
 * @author sberajaw
 * @author ahernm
 */
public class EditMyProfileViewAction extends BaseAction 
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
	 *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "private, no-cache, no-store");
		
		EditProfileForm editProfileForm = (EditProfileForm) form;

		PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		ProfileTagService tagSvc = AppServiceContextAccess.getContextObject(ProfileTagService.class);

	    Employee currUser = AppContextAccess.getCurrentUserProfile();
		AuthHelper.checkIfEmployeeNull(currUser, request.getRemoteUser());
	    		
		Employee employee = service.getProfile(ProfileLookupKey.forKey(currUser.getKey()), ProfileRetrievalOptions.EVERYTHING);
		AuthHelper.checkIfEmployeeNull(employee, request.getRemoteUser());

		ProfileType profileType = ProfileTypeHelper.getProfileType(editProfileForm.getProfileType());
		editProfileForm.setProfileType(employee.getProfileType());
		
		for (String propertyId : profileType.getPropertyIds())
		{
			Property property = profileType.getPropertyById(propertyId);
			if (Updatability.READWRITE.equals(property.getUpdatability()))
			{
				String fieldValue = null;
				if (property.isExtension())
				{
					fieldValue = employee.getProfileExtension(property.getRef(), true).getStringValue();					
				}
				else
				{
					fieldValue = (String)employee.get(property.getRef());
				}
				
				if (fieldValue == null)
				{
					editProfileForm.setAttribute(property.getRef(), "");
				}
				else
				{
					editProfileForm.setAttribute(property.getRef(), fieldValue);					
				}
				
			}
		}
		
//		
//		for (UIProfileSectionConfig profileSectionConfig : profileLayouConfig.getProfileSecionConfig().values()) 
//		{
//			String profileSectionName = profileSectionConfig.getName();
//
//			for (UIAttributeConfig attributeConfig : profileSectionConfig.getAttributes()) 
//			{
//				if (attributeConfig.isEditable()) 
//				{
//					String editableField = attributeConfig.getAttributeId();
//					Object fieldValue = null;
//
//					if (attributeConfig.isExtensionAttribute()) {
//						fieldValue = employee.getProfileExtension(Employee.getExtensionIdForAttributeId(editableField),true).getStringValue();
//					} else {
//						fieldValue = employee.get(editableField);
//					}
//
//					// if attribute is part of the base profile
//					if (fieldValue instanceof String) {
//						editProfileForm.setAttribute(profileSectionName + "." + editableField, fieldValue);
//					}
//
//					// else if attribute is an extension to the base profile 
//					else if (fieldValue instanceof ProfileExtension) {
//						editProfileForm.setAttribute(profileSectionName + "." + editableField, ((ProfileExtension) fieldValue).getStringValue());
//					}
//
//					// attribute value may be null in the database and so we interpret that as an empty string for the attribute value
//					else if (fieldValue == null) {
//						editProfileForm.setAttribute(profileSectionName + "." + editableField, "");
//					}
//
//					if (attributeConfig.getUid() != null) 
//					{
//						String uidAttribute = attributeConfig.getUid();
//						Object uidAttributeValue = employee.get(uidAttribute);
//
//						if (uidAttributeValue instanceof String) {
//							editProfileForm.setAttribute(profileSectionName + "." + uidAttribute, uidAttributeValue);
//						}
//
//						else if (uidAttributeValue instanceof ProfileExtension) {
//							editProfileForm.setAttribute(profileSectionName + "." + uidAttribute, ((ProfileExtension) uidAttributeValue).getStringValue());
//						}
//					}
//					
//					if (attributeConfig.getUserid() != null) {
//						
//						String useridAttribute = attributeConfig.getUserid();
//						Object useridAttributeValue = employee.get(useridAttribute);
//						
//						if (useridAttributeValue instanceof String) {
//							editProfileForm.setAttribute(profileSectionName + "." + useridAttribute, useridAttributeValue);
//						}
//
//						else if (useridAttributeValue instanceof ProfileExtension) {
//							editProfileForm.setAttribute(profileSectionName + "." + useridAttribute, ((ProfileExtension) useridAttributeValue).getStringValue());
//						}
//					}
//				}
//			}
//		}

		// THIS IS LEGACY AND NEVER USED
		//List<Tag> tags = tagSvc.getTagsForKey(employee.getKey());
		//String tagListAsString = StringUtil.convertTagListToString(tags);
		//editProfileForm.setProfileTags(tagListAsString);
		editProfileForm.setUid(employee.getUid());
		editProfileForm.setKey(employee.getKey()); 
		editProfileForm.setLastUpdate(employee.getLastUpdate());
		editProfileForm.setLocaleName(request.getLocale().toString());

		ProfileViewAction.setProfileConfigData(request,employee);

		request.setAttribute("isActive", employee.isActive());
		request.setAttribute("currentTab", "editprofile");
		
		boolean isPronEnabled = PolicyHelper.isFeatureEnabled(Feature.PRONUNCIATION, employee);

		// Set information for the user feature enablement. New since LC 3.0
		request.setAttribute("isPhotoEnabled", PolicyHelper.isFeatureEnabled(Feature.PHOTO, employee) );
		request.setAttribute("canUpdatePhoto", PolicyHelper.checkAcl(Acl.PHOTO_EDIT, employee) );
		request.setAttribute("isPronunciationEnabled", isPronEnabled );
		request.setAttribute("canUpdateProunciation", PolicyHelper.checkAcl(Acl.PRONUNCIATION_EDIT, employee) );
		
		PronunciationService pronSvc = AppServiceContextAccess.getContextObject(PronunciationService.class);

		//SPR: #XZSU8KLAYK. calling 'existByKey()' will throw exception without checking whether prononciation
		// feature is enable or not. So check it first
		// request.setAttribute("hasPronunciation", new Boolean(pronSvc.existByKey(employee.getKey())).toString());
		if ( isPronEnabled ) {
			request.setAttribute("hasPronunciation", new Boolean(pronSvc.existByKey(employee.getKey())).toString());
	 	}
		else
		{		
			request.setAttribute("hasPronunciation", new Boolean("false"));
		}		

	    // the template data model we will use to render edit form
	    try
	    {      
	      TemplateDataModel dataModel = new TemplateDataModel(request);
	      dataModel.updateEmployee(employee, null);
	      request.setAttribute("dataModel", dataModel);
	    }
	    catch (Exception e)
	    {
	    }
		
		return mapping.findForward("editProfileView");
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
}
