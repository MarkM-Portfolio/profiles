/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.actions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import com.ibm.lconn.core.web.secutil.DangerousUrlHelper;
import com.ibm.lconn.core.web.secutil.UrlUtil;

import com.ibm.lconn.profiles.api.actions.APIAction;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.config.types.Updatability;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Pronunciation;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.PronunciationService;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.webui.forms.EditProfileForm;
import com.ibm.peoplepages.webui.resources.ResourceManager;
import com.ibm.sn.av.api.AVScannerException;

/**
 * @author sberajaw
 */
public class EditMyProfileAction extends APIAction
{
	private static final Log LOG = LogFactory.getLog(EditMyProfileAction.class);

	private static final String PRONUNCIATION_FORM = "pronunciation";
	private static final String PHOTO_FORM = "photo";

	private static final String BLOG_URL = PropertyEnum.BLOG_URL.getValue(); // for blogURL validation

    /*
	 * (non-Javadoc)
	 *
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
	 *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward doExecutePOST(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		boolean isDebugEnabled = LOG.isDebugEnabled();
        boolean isTraceEnabled = LOG.isTraceEnabled();

        if (isDebugEnabled) {
			String nonce = DangerousUrlHelper.getNonce(request);
			String dUrlNonce = request.getParameter( DangerousUrlHelper.getNonce(request)); // DANGEROUS_NONCE);

			LOG.debug("Got nonce = " + nonce + ", dUrlNonce = " + dUrlNonce );
		}

		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "private, no-cache, no-store");

		if (isDebugEnabled) {
			String requestQuery = request.getQueryString();
			String requestUri   = request.getRequestURI();

			LOG.debug("Received request : URI: " + requestUri + " query: " + requestQuery);
		}
		boolean success = DangerousUrlHelper.verify( request );
		if (isDebugEnabled) {
			LOG.debug("DangerousUrlHelper.verify : " + success );
		}

		if (this.isCancelled(request)) {
			return mapping.findForward("cancel");
		}

		PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		PronunciationService pronSvc = AppServiceContextAccess.getContextObject(PronunciationService.class);
		Employee employee = service.getProfile(ProfileLookupKey.forKey(AppContextAccess.getCurrentUserProfile().getKey()), ProfileRetrievalOptions.EVERYTHING);

		EditProfileForm editProfileForm = (EditProfileForm) form;
		String status = "success";

		if (PHOTO_FORM.equals(editProfileForm.getSubEditForm())) {
			//no special processing needed for photo form.

		} else
		if (PRONUNCIATION_FORM.equals(editProfileForm.getSubEditForm())) {

			//handle the pronunciation form.
			String key = employee.getKey();

			if (!editProfileForm.isRemovePronunciation()) {
				Pronunciation pronunciation = new Pronunciation();
				pronunciation.setKey(key);

				byte[] pronunciationData = null;

		        FormFile pronunciationFile = editProfileForm.getPronunciation();
		        if (pronunciationFile != null) {

                // Use the real data if not using the test virus
                if (pronunciationData == null)
                    pronunciationData = pronunciationFile.getFileData();

					if (pronunciationData.length > 0) {
						pronunciation.setAudioFile(pronunciationData);
						pronunciation.setFileType(pronunciationFile.getContentType());

						try {
						    pronSvc.update(pronunciation);
						}
						catch(Exception e) {
						    if ( e.getCause() instanceof AVScannerException ) {
								response.getWriter().println("<html><body><textarea>error.fileContainsVirus</textarea></body>");
						    }
						    else {
								response.getWriter().println("<html><body><textarea>errorDefaultMsg2</textarea></body>");
						    }
						    return null;
						}
					}
				}
			}
			else {
				pronSvc.delete(key);
			}

		} else {
			// anything not photo or pronunciation will have other fields to process

			// PMR 59484,7TD,000: We are getting 'null' profileType from the editProfileForm.
			// We shouldn't really rely on the profileType from the editForm even if it were available.
			// So, change to get the profile type from the employee object instead.
		    ProfileType profileType = ProfileTypeHelper.getProfileType(employee.getProfileType());
		    for (String id : editProfileForm.getAttributeKeys()) {
		        Property property = profileType.getPropertyById(id);
		        if (property != null && Updatability.READWRITE.equals(property.getUpdatability())) {
		            String propRef = property.getRef();
		            String value   = (String) editProfileForm.getAttribute(propRef);
		            // TODO - required field checking
		            boolean isValidData = true;
		            if (StringUtils.isNotEmpty(value))
		            {
		                value = value.trim();
		                if (isDebugEnabled) {
		                    LOG.debug("EditMyProfileAction got : " + propRef + " '" + value + "'");
		                }
		                // RTC 185874 : Blog Link content greater than 250 characters causes 'Contact Information' tab to not load
		                if (BLOG_URL.equalsIgnoreCase(propRef))
		                {
		                	//RTC 200341: TS001725015: "Blog link" field in Profile changing Blog URLs to lower case leading to "page not found"
		                    String tmpVal = value;
		                    isValidData   = validateURL(tmpVal); //check that the url is valid
		                    if (isValidData) {
		                        // ensure the URL is not too long
		                        int urlLength  = tmpVal.length();
		                        int dbFieldMax = 248; // truncate a blogUrl that will exceed db field limit (256); leave room for (8) https://
		                        value = ((urlLength > dbFieldMax) ? tmpVal.substring(0, Math.min(urlLength, dbFieldMax)) : tmpVal);
		                        if (isTraceEnabled) {
		                            LOG.trace(" final : " + propRef + " '" + tmpVal + "'" );
		                        }
		                    }
		                    else {
		                        status = "failure";
		                        String errorMsg = "";
		                        Locale locale = request.getLocale();
		                        String key = "errors.invalid";
		                        try {
		                            if (isTraceEnabled) {
		                                LOG.trace(" ResourceManager.format(" + locale.getDisplayName() + ", " + key + ", " + value +")");
		                            }
		                            errorMsg = ResourceManager.format(locale, key, new Object[] {value});
		                        }
		                        catch (Exception ex) {
		                            LOG.error(" Unable to use URI : " + value);
		                            if (isDebugEnabled) {
		                                LOG.debug(" ResourceManager.format( ... exception " + ex.getMessage());
		                            }
		                            if (isTraceEnabled) {
		                                LOG.trace(" ResourceManager.format( ... exception " + ex.getMessage());
		                                ex.printStackTrace();
		                            }
		                        }
		                        if (isTraceEnabled) {
		                            LOG.trace(" ResourceManager.format( ... after : " + errorMsg );
		                        }
		                        request.setAttribute("errorMessage", key);
//                              request.setAttribute("errorStatusCode", new Integer(HttpServletResponse.SC_BAD_REQUEST));
//                              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		                        LOG.error(errorMsg);
		                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMsg);
		                        throw new ProfilesRuntimeException(errorMsg);
		                    }
		                }
		            }
				    if (property.isExtension())	{
						ProfileExtension profileExtension = employee.getProfileExtension(propRef, true);
						profileExtension.setStringValue(value);

						// if there is a ".label" field associated with this extension, then that's actually the
						// label for the field and we need to put that in the extension name.
						String extName = (String) editProfileForm.getAttribute(propRef + ".label");
						if (extName != null) {
							profileExtension.setName(extName);
						}
						employee.setProfileExtension(profileExtension);
					}
					else {
					    if (isValidData) {
					        employee.put(propRef, value);
					    }
					}
				}
			}
			service.updateEmployee(employee);
		}

		ProfileViewAction.setProfileConfigData(request, employee);
		return mapping.findForward(status);
	}

	private boolean validateURL(final String value) throws Exception
	{
        boolean isDebugEnabled = LOG.isDebugEnabled();
        boolean isTraceEnabled = LOG.isTraceEnabled();

        boolean isValid = false;
	    // validation of URL requires that the value have http:// || https://
	    try {
	        URI url = new URI (value);
	        String uri = UrlUtil.getAsciiUri(url.toString());
            if (isTraceEnabled) {
                LOG.trace(" valid : " + " '" + uri + "' passed (1) URL validation" );
            }
	        isValid = true;
	    }
	    catch (URISyntaxException ex1) {
            if (isTraceEnabled) {
                LOG.trace(" invalid : " + " '" + value + "' failed (1) URL validation " + ex1);
            }
	        // if validation failed because the URL didn't start with 'http', give it another try
            String tmpURL = value.toLowerCase();
	        if (false == tmpURL.startsWith("http")) {
	            StringBuilder sb = new StringBuilder("http://");
	            sb.append(tmpURL);
	            tmpURL = sb.toString();
	            try {
	                URI url = new URI (tmpURL);
	                String uri = UrlUtil.getAsciiUri(url.toString());
	                if (isTraceEnabled) {
	                    LOG.trace(" valid : " + " '" + uri + "' passed (2) URL validation" );
	                }
	                isValid = true;
	            }
	            catch (URISyntaxException ex2) {
	                String msg = " invalid : " + " '" + tmpURL + "' failed (2) URL validation ";
	                if (isDebugEnabled) {
	                    LOG.debug(msg + ex2);
	                }
                    if (isTraceEnabled) {
                        LOG.trace(msg);
                        ex2.printStackTrace();
                    }
	                throw ex2;
	            }
	        }
	    }
	    return isValid;
	}

    /* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
}
