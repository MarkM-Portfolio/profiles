/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig.ExtensionType;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.Updatability;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileExtensionService;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.webui.ajax.actions.LoginInfoAction;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ProfileExtensionAction extends APIAction 
{
	private static final int BUFFER_SIZE = 1024;
	
	private static final String PROFILE_EXT_KEY = ProfileExtensionAction.class.getName() + ".profileExt";

	private final ProfileExtensionService service = AppServiceContextAccess.getContextObject(ProfileExtensionService.class);
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	protected long getLastModified(HttpServletRequest request) throws Exception 
	{
		ProfileLookupKey userPLK = getProfileLookupKey(request);
		assertNotNull(userPLK);
		Employee user = pps.getProfile(userPLK, ProfileRetrievalOptions.MINIMUM);
		assertNotNull(user);
		return user.getLastUpdate().getTime();
	}
	
	protected ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		ProfileExtension pe = resolveProfileExtension(request,true);
		ExtensionAttributeConfig extConfig = getExtensionConfig(request);
		
		switch (extConfig.getExtensionType())
		{
			case SIMPLE:
				response.setContentType(PeoplePagesServiceConstants.MIME_TEXT_PLAIN);
				response.setCharacterEncoding(PeoplePagesServiceConstants.CHARENC_UTF8);
				
				if (pe.isMaskNull() || pe.getStringValue() == null)
				{
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				}
				else
				{
					response.getWriter().print(pe.getStringValue());
				}
				break;
				
			case XMLFILE:
			case RICHTEXT:
				response.setContentType(
						(ExtensionType.XMLFILE == extConfig.getExtensionType())  ?
								PeoplePagesServiceConstants.MIME_TEXT_XML :
								PeoplePagesServiceConstants.MIME_TEXT_HTML);
				response.setCharacterEncoding(PeoplePagesServiceConstants.CHARENC_UTF8);

				if (pe.isMaskNull() || pe.getExtendedValue() == null)
				{
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				}
				else
				{
					response.getWriter().print(pe.getStringValue());
				}				
				break;
		}
		
		return null;
	}
	
	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		ProfileExtension pe = resolveProfileExtension(request,true);
		ExtensionAttributeConfig extConfig = getExtensionConfig(request);
		
		assertNotNull(pe);
		assertExtensionIsEditable(request, extConfig);

		ProfileLookupKey userPLK = getProfileLookupKey(request);

		//
		// Assert user exists and is current user
		//
		Employee user = pps.getProfile(userPLK, ProfileRetrievalOptions.MINIMUM);
		assertNotNull(user);
		assertTrue(user.getKey().equals(LoginInfoAction.getCachedUserRecord(request).getKey()),
				   APIException.ECause.FORBIDDEN);

		byte[] bytes = readBytesFromInput(request);
		
		if (bytes != null)
		{
			switch (extConfig.getExtensionType())
			{
				case SIMPLE:
				case XMLFILE:
				case RICHTEXT:
					String valueStr = new String(bytes, PeoplePagesServiceConstants.CHARENC_UTF8);
					// Assert validity of data
					assertTrue(extConfig.isValidData(valueStr), APIException.ECause.INVALID_REQUEST);
					pe.setStringValue(valueStr);
					break;
			}
		}
		else
		{
			pe.setValue(null);
			pe.setExtendedValue(null);
		}		
		
		service.updateProfileExtension(pe);
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		
		return null;
	}
	
	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		// pe could come back null here if it does not exist - not sure why we need to look it up
		// fox this code so it doesn't bother with a lookup
		ProfileExtension pe = resolveProfileExtension(request,false);
			ExtensionAttributeConfig extConfig = getExtensionConfig(request);
			assertExtensionIsEditable(request, extConfig);
			ProfileLookupKey userPLK = getProfileLookupKey(request);
			// Assert user exists and is current user
			Employee user = pps.getProfile(userPLK, ProfileRetrievalOptions.MINIMUM);
			assertNotNull(user);
			assertTrue(user.getKey().equals(LoginInfoAction.getCachedUserRecord(request).getKey()), APIException.ECause.FORBIDDEN);
			if (pe != null) {
				service.deleteProfileExtension(pe);
			}
		return null;
	}
	
	private byte[] readBytesFromInput(HttpServletRequest request) throws Exception
	{
		InputStream is = request.getInputStream();
		
		if (is != null)
		{
			byte[] buffer = new byte[BUFFER_SIZE];
			ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
			
			int read;
			while ((read = is.read(buffer)) != -1)
			{
				bos.write(buffer, 0, read);
			}
			
			return bos.toByteArray();
		}
		
		return null;
	}
	
	private ProfileExtension resolveProfileExtension(HttpServletRequest request, boolean createEmptyObject) throws Exception
	{
		ProfileExtension pe = (ProfileExtension) request.getAttribute(PROFILE_EXT_KEY);
		
		if (pe == null)
		{
			ProfileLookupKey plk = getProfileLookupKey(request);
			ExtensionAttributeConfig extConfig = getExtensionConfig(request);
			
			assertNotNull(plk);
			assertNotNull(extConfig);
			
			pe = service.getProfileExtension(plk, extConfig.getExtensionId());
			
			if (pe == null && createEmptyObject) {
				// note on 4.5 code and before. if the attribute did not exist, the previous code
				// would return an 'empty' object with the profKey and lastUpdate set via an outer
				// join between the PROF_EXTENSION and EMPLOYEE tables. I suppose the intent was the
				// rest of the object would be filled in for an update or insert.
				// we don't need the empty object for delete
				Employee user = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
				pe = new ProfileExtension();
				pe.setKey(user.getKey());
				pe.setPropertyId(extConfig.getExtensionId());
				pe.setProfileType(user.getProfileType());
				pe.setRecordUpdated(user.getRecordUpdated());				
			}
			request.setAttribute(PROFILE_EXT_KEY, pe);
		}
		return pe;
	}
	
	private final ExtensionAttributeConfig getExtensionConfig(HttpServletRequest request) throws Exception
	{
		String extensionId = request.getParameter(PeoplePagesServiceConstants.EXTENSION_ID);
		assertNotNull(extensionId);
		
		ExtensionAttributeConfig config = DMConfig.instance().getExtensionAttributeConfig().get(extensionId);
		assertNotNull(config);
		
		return config;
	}
	
	private final void assertExtensionIsEditable(HttpServletRequest request, ExtensionAttributeConfig extConfig) throws Exception
	{
		ProfileLookupKey plk = getProfileLookupKey(request);
		assertNotNull(plk);
		
		Employee profile = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
		assertNotNull(profile);
		
		String extensionId = extConfig.getExtensionId();
		ProfileType profileType = ProfileTypeHelper.getProfileType(profile.getProfileType());
		Property property = profileType != null ? profileType.getPropertyById(extensionId) : null;
		boolean isEditable = property != null && Updatability.READWRITE.equals(property.getUpdatability());
		assertTrue(isEditable, ECause.INVALID_OPERATION);
	}

}
