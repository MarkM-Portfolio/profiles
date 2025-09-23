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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.ibm.lconn.profiles.api.actions.APIAction;
import com.ibm.lconn.profiles.policy.Feature;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.data.Pronunciation;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.PronunciationService;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.util.AntiVirusFilter;
import com.ibm.peoplepages.util.FileSubmissionHelper;
import com.ibm.peoplepages.webui.ajax.actions.LoginInfoAction;

public final class ProfileAudioAction extends APIAction
{
	private static final Log LOG = LogFactory.getLog(ProfileAudioAction.class);

	private PronunciationService service = AppServiceContextAccess.getContextObject(PronunciationService.class);
	
	private static final class Bean
	{
		ProfileLookupKey plk;
		Employee profile;		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.api.actions.APIAction#doExecuteGET(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected final ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		Bean bean = getBean(request);
		getAudioAsStream(bean, response);
		
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.api.actions.APIAction#doExecutePUT(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected final ActionForward doExecutePUT(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		Bean bean = getBean(request);		

		ProfileLookupKey plk = getProfileLookupKey(request);
		assertNotNull(plk);
		
		Employee user = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
		assertNotNull(user);

		byte[] pronunciationData = null;
		
		// If the request is for the eicar.jpg file, replace with the 
        // test virus stream.  This should *always* throw an exception.
		String filename = request.getHeader("Slug");
		boolean useTestData = false;
		
		if (filename != null && filename.equals("eicar.wav") && AntiVirusFilter.isEicarEnabled()) {
            InputStream newSound = AntiVirusFilter.getEicar();
            int available = newSound.available();
		    pronunciationData = new byte[available];
            int totalLen = 0;
            int readLen = 0;
            while (totalLen < available) {
                readLen = newSound.read(pronunciationData, readLen, newSound.available());
                totalLen += readLen;
            }
            useTestData = true;
		}
		
		String contentType = request.getContentType();
		if (pronunciationData == null) 
		    pronunciationData = FileSubmissionHelper.getSubmission(request);

		// bad submission checking
		// don't check if use test data. Make it easier for curl command
		if (pronunciationData.length > FileSubmissionHelper.getPronunciationMaxBytes()
				|| (!Arrays.asList(FileSubmissionHelper.getPronunciationMimeTypes()).contains(contentType) && !useTestData) )
		{
			ActionMessages errors = getErrors(request);
			ActionMessage message = new ActionMessage(pronunciationData.length > FileSubmissionHelper.getPronunciationMaxBytes() ?
					"errors.pronunciation.maxfilesize" : "errors.pronunciation.filetype");
			errors.add(Globals.ERROR_KEY, message);
			saveErrors(request, errors);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return mapping.findForward("atomError");
		}

		Pronunciation pronunciation = new Pronunciation();
		pronunciation.setKey(user.getKey());
		pronunciation.setFileType(request.getContentType());

		if (pronunciationData.length > 0) {
			pronunciation.setAudioFile(pronunciationData);
			service.update(pronunciation);
		}
		
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.api.actions.APIAction#doExecuteDELETE(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected final ActionForward doExecuteDELETE(
				ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response) throws Exception
	{
		Bean bean = getBean(request);		
		ProfileLookupKey plk = getProfileLookupKey(request);
		assertNotNull(plk);
		
		Employee user = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
		assertNotNull(user);

		service.delete(user.getKey());
		
		return null;
	}
	
	private final void getAudioAsStream(Bean bean, HttpServletResponse response)
	{
		ServletOutputStream os = null;
		try
		{
			String employeeKey = bean.profile.getKey();	
			if (employeeKey != null)
			{
			    // Check to see whether pronunciation is enabled or not for the user
			    // If not, return no content. New since 3.0
			    boolean returnContent = PolicyHelper.isFeatureEnabled(Feature.PRONUNCIATION, bean.profile );

			    if ( returnContent ) {

				Pronunciation pronunciation = service.getByKey(employeeKey);
				if (pronunciation != null)
				{
					byte[] bt = pronunciation.getAudioFile();
					response.setHeader("Content-disposition", "attachment; filename=" + employeeKey + FileSubmissionHelper.getPronunciationFileExtension(pronunciation.getFileType()));
					response.setContentLength(bt.length);
					response.setContentType(pronunciation.getFileType());

					os = response.getOutputStream();
					os.write(bt);
					os.close();
				}
				else
				    returnContent = false;
			    }				

			    if ( !returnContent ) 
				{
					response.setContentType("audio/x-wav");
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.setContentLength(0);
				}	   
			}
		}
		catch (DataAccessException dae)
		{
			LOG.error(dae.getMessage(), dae);
		}
		catch (IOException ioe)
		{
			LOG.error(ioe.getMessage(), ioe);
		}
		finally
		{
			try
			{
				if (os != null)
				{
					os.close();
				}
			}
			catch (IOException ioe)
			{
				LOG.error(ioe.getMessage(), ioe);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	protected final long getLastModified(HttpServletRequest request) throws Exception 
	{
		return getBean(request).profile.getLastUpdate().getTime();
	}

	private final Bean getBean(HttpServletRequest request) throws Exception
	{
		Bean bean = getActionBean(request,Bean.class);
		
		if (bean == null)
		{
			bean = new Bean();			
			bean.plk = getProfileLookupKey(request);
			assertNotNull(bean.plk);
			bean.profile = pps.getProfile(bean.plk, ProfileRetrievalOptions.MINIMUM);
			assertNotNull(bean.profile);			
			storeActionBean(request, bean);
		}
		
		return bean;
	}
}
