/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.actions;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Arrays;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.mutable.MutableBoolean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.lconn.core.file.LCTempFile;
import com.ibm.lconn.core.file.LCTempFileManager;
import com.ibm.lconn.core.file.LCTempFileManagerFactory;
import com.ibm.lconn.core.url.LCUrlUtil;
import com.ibm.lconn.core.versionstamp.VersionStamp;
import com.ibm.lconn.profiles.api.actions.APIAction;
import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.Photo;
import com.ibm.lconn.profiles.data.PhotoCrop;
import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.PhotoService;
import com.ibm.lconn.profiles.internal.service.PhotoService.ImageType;
import com.ibm.lconn.profiles.internal.service.ProfileServiceBase;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.web.util.CachingHelper;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.util.AntiVirusFilter;
import com.ibm.peoplepages.util.FileSubmissionHelper;
import com.ibm.peoplepages.util.UserAgentHelper;

// Image serving has a somewhat involved history, particularly on cloud and especially when integrated with verse.
// These comments provide a few notes about the verse integration and some historical notes.
// The core issue for verse (as of this writing) is that it asks for photos for any user. They take the email, produce
// a hash code and ask for a photo. The server has no way of knowing if that user is in the targeted org, is in another
// org, or is a completely spurious user that will never be on the cloud (e.g. uncleted@gmail.com).
// The problem verse encountered was for two valid cloud users. Say userA in orgA and userB1 in orgB. If userA requests
// userB1's photo, we would return an anonymous (no pic). If the anon pic is publically cached and now userB2 requests
// userB1's photo, he will get the anon pic even if userB1 has a photo.
// To this end, the code handles three cases:
//  (1) user is found (in the org) and has a photo - we return pic with public cache headers
//  (2) user is found (in the org) and has no photo - return anon pic.
//  (3) user is not found - this could be a user in a different org or a spurious user - return private cache headers.
// The PhotoUI object receives the Photo object and provides a wrapper that helps determine which of the above cases we have.
//
// historical note: If there is 'unauthorized' access, we have always returned an anonymous photo with no cache directives,
// which seems odd. I think that the code conflates unauthorized and unauthenticated access and am not sure if that distinction
// can be untangled. I do know that greenhouse used a locked server and then complained that they had a widget that could not
// handle an auth challenge. Instead of having them buck up and fix their widget, Connections caved and gave them an anon
// pic and this seemed to be the start of this confusion.
public class ProfileImageAction extends APIAction
{
	private static final Log LOG = LogFactory.getLog(ProfileImageAction.class);

	private static final String KEY_PREFIX = ProfileImageAction.class.getName();
	private static final String PHOTO_KEY = KEY_PREFIX + ".photo";

	private static final String JPEG_MIME_TYPE = "image/jpeg";
	
	private static final String THUMBNAIL_PARAM = "small";
	private static final String NOIMAGE = "noimg";
	private static final short anonImage = 0;
	private static final short pixelImage = 1;
	
	private static final PhotoService photoService = AppServiceContextAccess.getContextObject(PhotoService.class);
	private static final ProfileServiceBase profileServiceB = AppServiceContextAccess.getContextObject(ProfileServiceBase.class);
	
	private static String staticImageRedirectUrl, staticImageExtRedirectUrl, staticPixelImageRedirectUrl;
	private static String staticThumbnailRedirectUrl, staticThumbnailExtRedirectUrl;
	//private static String noImageLastModified;
	private static Long noImageLastModifiedLong;
	
	static{
		staticImageRedirectUrl = "/static/"+LCUrlUtil.toURLEncoded(VersionStamp.INSTANCE.getVersionStamp())+FileSubmissionHelper.profileNoImageFileName;
		staticThumbnailRedirectUrl = "/static/"+LCUrlUtil.toURLEncoded(VersionStamp.INSTANCE.getVersionStamp())+FileSubmissionHelper.profileNoImageSmallFileName;
		staticImageExtRedirectUrl = "/static/"+LCUrlUtil.toURLEncoded(VersionStamp.INSTANCE.getVersionStamp())+FileSubmissionHelper.profileNoImageFileNameExt;
		staticThumbnailExtRedirectUrl = "/static/"+LCUrlUtil.toURLEncoded(VersionStamp.INSTANCE.getVersionStamp())+FileSubmissionHelper.profileNoImageSmallFileNameExt;
		staticPixelImageRedirectUrl = "/static/"+LCUrlUtil.toURLEncoded(VersionStamp.INSTANCE.getVersionStamp())+FileSubmissionHelper.pixelImageFileName;	
		noImageLastModifiedLong = System.currentTimeMillis();
		//SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		//noImageLastModified = dateFormat.format(new Date(noImageLastModifiedLong));
	}
	
	public ProfileImageAction() {
		this.isPublic = true;        // see BaseAction
	}
	
	/**
	 * IMPORTANT NOTE: before you change any cache header behavior, look at rtc item 136017.
	 */
	protected ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		boolean thumbnail = Boolean.parseBoolean(request.getParameter(THUMBNAIL_PARAM));
		boolean allowRedirects = Boolean.parseBoolean(request.getParameter("r"));
		String noimgParam = request.getParameter(NOIMAGE);
		// only two image types for anon - anon pic or the single pixel
		short anontype = anonImage;
		if ("pixel".equals(noimgParam)) anontype = pixelImage;
		// cloud deployment will use private cache in some circumstances
		boolean allowPrivateCache = PropertiesConfig.getBoolean(ConfigProperty.ALLOW_PRIVATE_CACHE_PHOTO);
		if (allowPrivateCache == false && LCConfig.instance().isLotusLive()){
			allowPrivateCache = true;
		}
		// lookup the photo
		PhotoUI photoUI = getPhoto(request, thumbnail);
		// if we have no photo put in the desired anon photo (unauthorized case will have no photo)
		if (photoUI.isNoPhoto()){
			// if the redirect feature is requested, make sure browser can handle it
			if (allowRedirects) {
				allowRedirects = UserAgentHelper.supportsRedirectCaching(request.getHeader("User-Agent"));
			}
			boolean isOverlayPhotoEnabled =  !(PropertiesConfig.getBoolean(ConfigProperty.PROFILE_DISABLE_OVERLAY_VISITOR_PHOTO));
			boolean isExternalUser = false;
			// if an overlay is on, we may need to return the external user version of the noimage. if client asked for pixel image, then overlay is irrelevant.
			if (anontype == anonImage) {
				// if the external overlay photo is enabled, we get the overlaid version of the no photo.
				if (isOverlayPhotoEnabled) {
					isExternalUser = photoUI.isExternalUser();
				}
			}
			// set the anon pic - sets pixel or ghost or external ghost
			setAnonPic(photoUI,anontype,thumbnail,isOverlayPhotoEnabled,isExternalUser);
			// careful because we inserted the anon photo subsequent calls to. do not get confused because it now exists.
			if (photoUI.isAuthorizedAccess()){
				if (photoUI.userExists()){
					// if user asked for redirect and it is allowed
					if (allowRedirects) {
						sendAnonRedirect(request,response,anontype,isExternalUser,isOverlayPhotoEnabled,thumbnail);
						return null;
					}
					else{
						// we found a user and are sending the anon photo with regular headers
						CachingHelper.setCachableForPhoto(
							response, 
							true, 
							PropertiesConfig.getInt(ConfigProperty.PHOTO_CACHE_EXPIRES),
							allowPrivateCache
						);
					}
				}
				else{
					// cannot find user. may be cross org, or users does not exist. anon photo is set, set private headers and longer cache.
					CachingHelper.setCachableForPhoto(
						response, 
						false, // isPublic 
						PropertiesConfig.getInt(ConfigProperty.PHOTO_NOACCOUNT_CACHE_EXPIRES), 
						true   //forceAtLeastPrivate
					);
				}
			}
			else{
				// no cache headers
				CachingHelper.disableCaching(response);
			}
		}
		else{
			// we found a photo - regular processing
			CachingHelper.setCachableForPhoto(
				response, 
				true, 
				PropertiesConfig.getInt(ConfigProperty.PHOTO_CACHE_EXPIRES), 
				allowPrivateCache
			);
		}
		response.setContentType(photoUI.getPhoto().getFileType());
		if (!thumbnail){
			response.setContentLength(photoUI.getPhoto().getImage().length);
		}
		else{
			response.setContentLength(photoUI.getPhoto().getThumbnail().length);
		}

		ServletOutputStream os = response.getOutputStream();
		if (!thumbnail){
			os.write(photoUI.getPhoto().getImage());
		}
		else{
			os.write(photoUI.getPhoto().getThumbnail());
		}
		os.flush();
		os.close();
		
		return null;
	}
	
	// the assumption with this method is that 'getLastModified' has been called and a 304 (not modified)
	// response is the desired response. See BaseAction.
	protected boolean setNotModifiedCacheHeaders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// expectation is we set photo headers, so thumbnail will suffice it the photo is not already on the request.
		PhotoUI photoUI = getPhoto(request,true);
		boolean allowPrivateCache = PropertiesConfig.getBoolean(ConfigProperty.ALLOW_PRIVATE_CACHE_PHOTO);
		if (photoUI.isNoPhoto()){
			if (photoUI.isAuthorizedAccess()){
				if (photoUI.userExists()){
					// we found a user and are sending the anon photo with regular headers
					CachingHelper.setCachableForPhoto(
						response, 
						true, 
						PropertiesConfig.getInt(ConfigProperty.PHOTO_CACHE_EXPIRES),
						allowPrivateCache
					);
				}
				else{
					// cannot find user. may be cross org, or users does not exist. anon photo is set, set private headers and longer cache.
					CachingHelper.setCachableForPhoto(
						response, 
						false, // isPublic 
						PropertiesConfig.getInt(ConfigProperty.PHOTO_NOACCOUNT_CACHE_EXPIRES), 
						true   //forceAtLeastPrivate
					);
				}
			}
			else{
				// no cache headers
				CachingHelper.disableCaching(response);
			}
		}
		else{
			// we found a photo - regular processing
			CachingHelper.setCachableForPhoto(
				response, 
				true, 
				PropertiesConfig.getInt(ConfigProperty.PHOTO_CACHE_EXPIRES), 
				allowPrivateCache
			);
		}
		response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		return true;
	}
	
	private void sendAnonRedirect (
			HttpServletRequest request, HttpServletResponse response, 
			short anontype, boolean isExternalUser, boolean isOverlayPhotoEnabled, boolean thumbnail) throws Exception {
		CachingHelper.setCachableForPhoto(response, true, PropertiesConfig.getInt(ConfigProperty.PHOTO_CACHE_EXPIRES), 
				PropertiesConfig.getBoolean(ConfigProperty.ALLOW_PRIVATE_CACHE_PHOTO));
		response.setHeader("Content-Type", null);
		response.setHeader("Last-Modified", null);	
		response.setContentLength(0);
		if (anontype == pixelImage){
			response.sendRedirect(request.getContextPath()+staticPixelImageRedirectUrl);
		}
		else if (thumbnail) {
			response.sendRedirect(request.getContextPath()+( (isExternalUser && isOverlayPhotoEnabled) ? staticThumbnailExtRedirectUrl : staticThumbnailRedirectUrl));
		}
		else {
			response.sendRedirect(request.getContextPath()+( (isExternalUser && isOverlayPhotoEnabled) ? staticImageExtRedirectUrl : staticImageRedirectUrl));
		}
	}
	
	private void setAnonPic(PhotoUI photoUI, short anontype, boolean isThumbnail, boolean isOverlayPhotoEnabled, boolean isExternalUser){
		photoUI.getPhoto().setUpdated(new Timestamp(System.currentTimeMillis()));
		if (anontype == pixelImage) {
			photoUI.getPhoto().setFileType(FileSubmissionHelper.getPixelImageFileType());
			photoUI.getPhoto().setImage(FileSubmissionHelper.getPixelImageContents());
			photoUI.getPhoto().setThumbnail(FileSubmissionHelper.getPixelImageContents());
		}
		else {
			photoUI.getPhoto().setFileType(FileSubmissionHelper.getProfileNoImageFileType());
			if ((isOverlayPhotoEnabled && isExternalUser)){
				photoUI.getPhoto().setImage(FileSubmissionHelper.getProfileNoImageExtContents(false));
				photoUI.getPhoto().setThumbnail(FileSubmissionHelper.getProfileNoImageExtContents(true));					
			}
			else{
				photoUI.getPhoto().setImage(FileSubmissionHelper.getProfileNoImageContents(false));
				photoUI.getPhoto().setThumbnail(FileSubmissionHelper.getProfileNoImageContents(true));
			}
		}
	}
	
	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		ProfileLookupKey plk = getProfileLookupKey(request);
		assertNotNull(plk);
		String traceVal = null;
		if (LOG.isDebugEnabled()) {
   			traceVal = plk.toString();
   			LOG.debug("plk = " + traceVal);
    	}

		Employee user = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
		assertNotNull(user);
		if (LOG.isDebugEnabled()) {
			traceVal = user.getEmail();
			LOG.debug("user = " + traceVal);
		}
		MutableBoolean useTestData = new MutableBoolean(false);
		LCTempFile tempFile = getTempFile(request,useTestData);

		String contentType = request.getContentType();
		if (LOG.isDebugEnabled()) {
			if (null != contentType)
				traceVal = contentType;
			LOG.debug("contentType = " + traceVal);
		}

		// Check photo is non-null
		AssertionUtils.assertNotNull(tempFile, AssertionType.BAD_REQUEST);

		try {
			// bad submission checking
			if (tempFile.getFileSize() > FileSubmissionHelper.getPhotoMaxBytes()
					|| (!Arrays.asList(FileSubmissionHelper.getPhotoMimeTypes()).contains(contentType) && !useTestData.booleanValue()) )
			{
				ActionMessages errors = getErrors(request);
				ActionMessage message = new ActionMessage(tempFile.getFileSize() > FileSubmissionHelper.getPhotoMaxBytes() ?
						"errors.photo.maxfilesize" : "errors.photo.filetype");
				errors.add(Globals.ERROR_KEY, message);
				saveErrors(request, errors);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				if (LOG.isDebugEnabled()) {
					LOG.debug("atomError = " + "SC_BAD_REQUEST");
				}
				return mapping.findForward("atomError");
			}

			// update and ensure deletion
			if (LOG.isDebugEnabled()) {
				LOG.debug("update and ensure deletion for " + user.getEmail());
			}
			PhotoCrop photo = new PhotoCrop();
			photo.setKey(user.getKey());
			photo.setGuid(user.getGuid());
			photo.setFileType(contentType);
			photo.setImageStream(tempFile.openDeleteOnClose());
			photoService.updatePhoto(user,photo);
		}
		finally {
			tempFile.delete();
		}		
		
		return null;
	}
	
	private final LCTempFile getTempFile(HttpServletRequest request,  MutableBoolean useTestData) throws Exception {
		InputStream newPhoto = null;
		
		// If the request is for the eicar.jpg file, replace with the 
        // test virus stream.  This should *always* throw an exception.
        String filename = request.getHeader("Slug");
		if (filename != null && filename.equals("eicar.jpg") && AntiVirusFilter.isEicarEnabled()) {
            newPhoto = AntiVirusFilter.getEicar();
            useTestData.setValue(true);
        }
		// normal operation
		else {
			newPhoto = request.getInputStream();
			useTestData.setValue(false);
		}
		if (newPhoto != null) {
			LCTempFileManager mgr = LCTempFileManagerFactory.getInstance();
			return mgr.newTempFile(newPhoto);
		}		
		return null;
	}
	
	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ProfileLookupKey plk = getProfileLookupKey(request);
		assertNotNull(plk);

		Employee user = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
		assertNotNull(user);
		
		photoService.deletePhoto(user);
		
		return null;
	}
	
	protected long getLastModified(HttpServletRequest request) throws Exception {
		PhotoUI photoUI = getPhoto(request, Boolean.parseBoolean(request.getParameter(THUMBNAIL_PARAM)));
		if (photoUI.isNoPhoto()) {
			return noImageLastModifiedLong;
		}
		else {
			return photoUI.getPhoto().getUpdated().getTime();
		}
	}
	
	/**
	 * Get photo for getLastModified() and doExectueGET()
	 * 
	 * @param request
	 * @param thumbnail
	 * @return
	 * @throws Exception
	 */
	private PhotoUI getPhoto(HttpServletRequest request, boolean thumbnail) throws Exception {
		// important to note that this method can also be called by getLastModified. in order to prevent double lookup by the mainline
		// action class, we will put a PhotoUI object on the request. subsequent lookup will find that.
		PhotoUI photoUI = (PhotoUI) request.getAttribute(PHOTO_KEY);
		if (photoUI == null) {
			// photo is not cached on the request.
			final ProfileLookupKey plk = getProfileLookupKey(request);
			assertTrue(plk != null, ECause.INVALID_REQUEST);
			
			try {
				Photo photoDB = null;
				// get the employee
				Employee employee = lookupEmployee(plk, ProfileRetrievalOptions.MINIMUM);
				if (employee != null) {
					//TEST
					photoDB = thumbnail ? photoService.getPhoto(employee, ImageType.THUMBNAIL) : photoService.getPhoto(employee,ImageType.PHOTO);
					//TEST photoDB = photoService.getPhoto(employee, ImageType.BOTH);
					photoUI = new PhotoUI(photoDB,employee,true);
				}
				else{
					// user does not exist. put in a null photo with authorized access. this will return the anon photo.
					// imo, we should return a 404 but that is not historical behavior
					photoUI = new PhotoUI(null,employee,true);
				}
			}
			catch (AssertionException e) {
				if (e.getType() == AssertionType.UNAUTHORIZED_ACTION) {
					photoUI = new PhotoUI(null,null,false);  // the false marks this as unauthorized access
				}
				else{
					// we don't know what happened. treat it as authorized?
					photoUI = new PhotoUI(null,null,true);
				}
			}
			// we'll need a type for any returned content. anon pic logic will overwrite this if it serves a different type.
			if (photoUI != null && (photoUI.getPhoto() != null)){
				if (StringUtils.isEmpty(photoUI.getPhoto().getFileType())) {
					photoUI.getPhoto().setFileType(JPEG_MIME_TYPE);
				}
			}
			// must do this logic here as it is an optimization. try to put the photo on the request if this is called by getLastModified
			// and avoid a subsequent lookup when doExecuteGET is subsequently called.
			request.setAttribute(PHOTO_KEY, photoUI);
		}
		return photoUI;
	}
	
	private Employee lookupEmployee(ProfileLookupKey plk,ProfileRetrievalOptions options){
		//it should save a later lookup in the service anyhow.
		Employee rtn = profileServiceB.getProfileWithoutAcl(plk, options);
		return rtn;
	}
}