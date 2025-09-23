/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.core.compint.profiles.internal.policy.PolicyConstants;
import com.ibm.lconn.core.image.ImageOverlay;
import com.ibm.lconn.core.image.ImageResizer;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.Photo;
import com.ibm.lconn.profiles.data.PhotoCollection;
import com.ibm.lconn.profiles.data.PhotoCrop;
import com.ibm.lconn.profiles.data.PhotoRetrievalOptions;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.PhotoDao;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.AdminCodeSection;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;
import com.ibm.lconn.profiles.internal.util.AdminCodeSection.UncheckedAdminBlock;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.AntiVirusFilter;
import com.ibm.peoplepages.util.FileSubmissionHelper;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class PhotoServiceImpl extends AbstractProfilesService implements PhotoService 
{
	private static final Log LOG = LogFactory.getLog(PhotoService.class);

	// This should match with the PEOPLEDB, PHOTO table limits.
	private static final int MAX_IMAGE_SIZE = 50000;

	private static final int READ_BUFFER_SIZE = 1024;

	private static final String profileUserImageExtOverlay155 = "../overlayimages/extPersonPhotoOverlay155.png";
	private static final String profileUserImageExtOverlay64 = "../overlayimages/extPersonPhotoOverlay64.png";

	private static byte[] profileImageExtOverlayContents = new byte[0];
	private static byte[] profileImageExtOverlaySmallContents = new byte[0];

	@Autowired 	private PeoplePagesService pps;
	@Autowired	private PhotoDao photoDao;
	@Autowired	private ProfileServiceBase profSvc;

	@Autowired
	public PhotoServiceImpl(@SNAXTransactionManager PlatformTransactionManager txManager) {
		super(txManager);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public Photo getPhoto(Employee employee,ImageType type) throws ProfilesRuntimeException {
		if (employee == null){
			return null;
		}
		Photo rtn = null;
		rtn = photoDao.getPhoto(employee,type);
		if ( rtn == null ) {
			return null;
		}
		else {
		//	// Defect 59242: instead of making a call to retrieve the Employee
		//	// object using the plk, we are creating a skeleton Employee object from the
		//	// photo object with profileType and userState info for the downstream ACL checking.
		//	Employee profile = getSkeletonProfile(rtn);
			PolicyHelper.assertAcl(Acl.PHOTO_VIEW, employee);
			return rtn;
		}
	}
	
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public Date getPhotoUpdateDate(Employee employee) throws ProfilesRuntimeException {
		Date rtn = null;
		if (employee == null){
			return null;
		}
		rtn = photoDao.getUpdateDate(employee);
		return rtn;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.PhotoService#updatePhoto(com.ibm.peoplepages.data.Photo)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void updatePhoto(final Employee employee, final PhotoCrop photo) throws ProfilesRuntimeException, DataAccessRetrieveException {
		//
		if (LOG.isTraceEnabled()) {
			LOG.trace(" photo : " + ((null != photo) ? photo.toString(): " is null"));
			StringBuffer sb = new StringBuffer();
			if ( employee != null ){
				sb.append("employee ids: guid=").append(employee.getGuid()).append(" key=").append(employee.getKey());
			}
			else{
				sb.append("employee is null");
			}
			LOG.trace(sb);
		}
		//
		AssertionUtils.assertNotNull(employee);
		//
		AssertionUtils.assertNotNull(photo);
		final String key = employee.getKey();
		assertUDAccess(key, true);  // need this check, but we don't use the return variable

		// change for PMR 20192,999,706 - allow overlay for TDI
		final boolean isOverlayPhotoEnabled =  !(PropertiesConfig.getBoolean(ConfigProperty.PROFILE_DISABLE_OVERLAY_VISITOR_PHOTO));

		
		TransactionTemplate txTemplate = new TransactionTemplate(txManager);
		txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
		txTemplate.execute( new TransactionCallbackWithoutResult(){
			public void doInTransactionWithoutResult(TransactionStatus txnStatus) {
				try{
					updateImage(photo,employee,isOverlayPhotoEnabled);
					// rtc 176130 - photo table has its own lastupdate column.
					// for performance, we will not update the profile lastupdate and not force a re-index
					// photo is not relevant to seedlist data.  'touchProfile' also flushes
					// the profile cache. if need cache flushed, we need a peer method to 'touchProfile'.
					//revisit this issue see rtc 177531
					profSvc.touchProfile(key);
				}
				catch (Exception ex){
					LOG.info("Exception updating image for user guid "+ employee.getGuid() +" "+ex.getMessage());
					txnStatus.setRollbackOnly();
				}
			}
		});
		// if this is an external user, we alter (to put it kindly) the photo with an overlay (if the feature is enabled)
		if (employee.isExternal() && isOverlayPhotoEnabled){
			txTemplate = new TransactionTemplate(txManager);
			txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
			txTemplate.execute( new TransactionCallbackWithoutResult(){
				public void doInTransactionWithoutResult(TransactionStatus txnStatus) {
					try{
						overlayImage(employee);
					}
					catch (Exception ex){
						LOG.info("Exception updating overlay image for user guid "+ employee.getGuid() +" "+ex.getMessage());
						txnStatus.setRollbackOnly();
					}
				}
			});
		}
	}
	
	// this method is envisioned to be called in a txn wrapper (as in public method updatePhoto)
	// calling code is assumed to have: (1) stored the unmarked photo in the backup table; (2) ensured the overlay feature is enabled
	private void overlayImage(Employee emp) throws ProfilesRuntimeException {
		// assume image was inserted into photo table. we'll overlay that image
		if (emp.isExternal() == false){
			return;
		}
		// the backup photo is assumed to have been placed in the PHOTOBKUP table.
		Photo photoDb = photoDao.getPhotoBkup(emp,ImageType.BOTH); // backup photo and thumbnail
		if (photoDb == null){
			LOG.error("Did not locate backup photo for overlay for external user guid: " + emp.getGuid());
			return;
		}
		//
		try {
			byte[] image = photoDb.getImage();
			if (image == null || image.length == 0) return;
			//
			if (LOG.isDebugEnabled()) LOG.debug("Overlaying external user image for external user: " + emp.getDisplayName());
			
			ByteArrayInputStream fgImage = null;

			//for TDI get overlays differently
			if (AppContextAccess.isTDIContext())
			{
				if (LOG.isDebugEnabled()) LOG.debug("Overlaying external user image from TDI");
				//if overlay not loaded load them
				if (profileImageExtOverlayContents.length <= 0)
				{
					setExternalOverlayImages(profileUserImageExtOverlay155, profileUserImageExtOverlay64);
				}
				fgImage = new ByteArrayInputStream(profileImageExtOverlayContents);
			}
			else
			{
				fgImage = new ByteArrayInputStream(FileSubmissionHelper.getProfileImageExtOverlayContents());
			}
			
			ByteArrayInputStream bgImage = new ByteArrayInputStream(image);

			ByteArrayOutputStream overlayedImg = new ByteArrayOutputStream();
			ImageOverlay.overlayImage(fgImage, bgImage, overlayedImg); // overlay fgImage on top of bgImage

			if (overlayedImg.size() <= 0) {
				if (LOG.isDebugEnabled()) LOG.debug("Error? The resulting overlayed image is empty.  Leaving original as-is.");
			}
			else {
				byte[] newSmallPhoto = overlayedImg.toByteArray();
				photoDb.setImage(newSmallPhoto);
			}
			if (LOG.isDebugEnabled()) LOG.debug("Overlaying external user thumbnail image for external user guid: " + emp.getGuid());
			if (photoDb.getThumbnail() != null && photoDb.getThumbnail().length > 0) {

				ByteArrayOutputStream newThumbnailPhotoStream = new ByteArrayOutputStream();
				ImageResizer.resizeImage(new ByteArrayInputStream(
						photoDb.getImage()), newThumbnailPhotoStream, TINY_PHOTO_SIZE, TINY_PHOTO_SIZE,
						0, //center 1, // right align
						0, //center -1, // top aligned
						ImageResizer.DEFAULT_COLOR, BufferedImage.TYPE_INT_RGB);

				byte[] newThumbnail = newThumbnailPhotoStream.toByteArray();
				if (LOG.isDebugEnabled()) LOG.debug("Overlaying external user thumbnail image for user guid: " + emp.getGuid());
				//for TDI get overlays differently
				if (AppContextAccess.isTDIContext())
				{
					//if overlay not loaded load them
					if (profileImageExtOverlaySmallContents.length <= 0)
					{
						setExternalOverlayImages(profileUserImageExtOverlay155, profileUserImageExtOverlay64);
					}
					fgImage = new ByteArrayInputStream(profileImageExtOverlaySmallContents);
				}
				else
					fgImage = new ByteArrayInputStream(FileSubmissionHelper.getProfileImageExtOverlayContents(true /* isThumbnail */));
				bgImage = new ByteArrayInputStream(newThumbnailPhotoStream.toByteArray());
				overlayedImg = new ByteArrayOutputStream();
				//
				ImageOverlay.overlayImage(fgImage, bgImage, overlayedImg);
				//
				if (overlayedImg.size() <= 0) {
					if (LOG.isDebugEnabled()) LOG.debug("Resulting overlayed thumbnail image is empty.  Leaving original as-is.");
				}
				else {
					newThumbnail = overlayedImg.toByteArray();
				}
				// Add the thumbnail
				photoDb.setThumbnail(newThumbnail);
				if (LOG.isDebugEnabled()) {
					LOG.debug("newThumbnail.length: " + newThumbnail.length);
				}
			}
			else {
				if (LOG.isDebugEnabled()) LOG.debug("Thumbnail image null or size 0 for user guid: " + emp.getGuid());
			}
			//
			photoDb.setUpdated(new java.sql.Timestamp(System.currentTimeMillis()));
			
			photoDao.insertUpdatePhoto(emp, photoDb);
		}
		catch (Exception e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(e.getMessage(), e);
			}
			// clean up all tables to prevent orphaned content in backup table
			photoDao.deletePhoto(emp);
			//
			throw new ProfilesRuntimeException(e);
		}
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void deletePhoto(Employee employee) throws ProfilesRuntimeException, DataAccessRetrieveException {
		if (employee == null){
			return;
		}
		photoDao.deletePhoto(employee);
		// rtc 176130 - photo table has its own lastupdate column.
		// for performance, we will not update the profile lastupdate and not force a re-index
		// photo is not relevant to seedlist data.  'touchProfile' also flushes
		// the profile cache. if need cache flushed, we need a peer method to 'touchProfile'.
		//revisit this issue see rtc 177531
		profSvc.touchProfile(employee.getKey());

		// Hook up with the event logging. Added since 3.0
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		Employee actorProfile = AppContextAccess.getCurrentUserProfile();
		// call to createEventLogEntry will set appropriate sysEvent value
		EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(pps, actorProfile, employee, EventLogEntry.Event.PROFILE_PHOTO_REMOVED );
		;
		// For IC196102
		eventLogEntry.setProps( employee.getAttributes() );
		eventLogSvc.insert( eventLogEntry );
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.PhotoService#getPhoto(com.ibm.peoplepages.data.ProfileLookupKey)
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public Photo getPhotoForTDI(ProfileLookupKey plk) throws ProfilesRuntimeException 
	{
		if (AppContextAccess.isTDIContext() == false){
			throw new ProfilesRuntimeException("cannot call getPhotoforTDI outside of TDI context");
		}
		
		if (plk == null)
			return null;
		
		Photo photo = photoDao.getPhotoForTDI(plk);
		
		if ( photo == null ) {
			return null;
		}
		else {
			// Defect 59242: For performance reasons, instead of making a call to retrieve the Employee
			// object using the plk, we are creating a skeleton Employee object from the
			// photo object with profileType and userState info for the downstream ACL checking.
			Employee profile = getSkeletonProfile(photo);
			PolicyHelper.assertAcl(Acl.PHOTO_VIEW, profile);
			return photo;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.PhotoService#getPhotos(com.ibm.lconn.profiles.data.PhotoRetrievalOptions)
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public PhotoCollection getAllPhotosForTDI(PhotoRetrievalOptions options) {
		if (AppContextAccess.isTDIContext() == false){
			throw new ProfilesRuntimeException("cannot call getAllPhotosforTDI outside of TDI context");
		}
		if (options == null) {
			List<Photo> l = Collections.emptyList();
			return new PhotoCollection(l, null);
		}
		List<Photo> photos = photoDao.getAllPhotos(options);
		PhotoRetrievalOptions nextOptions = null;
		if (photos.size() > options.getPageSize()) {
			// pop last entry and use as next point to scroll to
			nextOptions = options.clone().setNextPhotoKey(photos.remove(photos.size() - 1).getKey());
		}

		photos = photoDao.filterInvalidPhotos(photos);

		return new PhotoCollection(photos, nextOptions);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.PhotoService#updatePhoto(com.ibm.peoplepages.data.Photo)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void updatePhotoForTDI(final PhotoCrop photo) throws ProfilesRuntimeException, DataAccessRetrieveException {
		
		if (LOG.isTraceEnabled()) {
			LOG.trace(" photo : " + ((null != photo) ? photo.toString(): " is null"));
		}
		if (AppContextAccess.isTDIContext() == false){
			throw new ProfilesRuntimeException("cannot call AdminProfileService.updatePhotoForTDI outside of TDI context");
		}
		AssertionUtils.assertNotNull(photo);
		final String key = photo.getKey();
		assertUDAccess(key, true);  // need this check, but we don't use the return variable

		// get profile as admin process due to visitor model restrictions
		final ProfileLookupKey plk = ProfileLookupKey.forKey(key);
		AssertionUtils.assertNotNull(plk);
		// seems we can use ProfileServiceBase.getProfileWithoutAcl
		final Employee[] profiles = new Employee[1];
		AdminCodeSection.doAsAdmin(new UncheckedAdminBlock() {
			public void run() {
				profiles[0] = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
			}
		});
		AssertionUtils.assertNotNull(profiles[0]);
		final Employee targetUser = profiles[0];
		this.updatePhoto(targetUser,photo);
	}
	
	// this method is envisioned to be called in a txn wrapper (as in public method updatePhoto)
	private void updateImage(PhotoCrop photo, Employee emp, boolean isOverlayEnabled){
		//Resize the photo to the desired size, and ensure that it is square.
	    boolean isDEBUG = LOG.isDebugEnabled();

		InputStream oldPhotoStream = getPhotoInputStream(photo);
		try{			
			if (oldPhotoStream == null){
				return;
			}
			oldPhotoStream = AntiVirusFilter.scanFile(oldPhotoStream);   		    
			byte[] newSmallPhoto;
			ByteArrayOutputStream newSmallPhotoStream = new ByteArrayOutputStream();
			Boolean crop = photo.getIsCrop();
			if (crop != null && crop.booleanValue()){
				double cropStartX = photo.getStartx();
				double cropStartY = photo.getStarty();
				double cropEndX = photo.getEndx();
				double cropEndY = photo.getEndy();
				// 66440 - Overriding autmatic imageType calculation because BufferedImage.TYPE_INT_ARGB causes problems when PNGs are converted to JPEGs by ImageResizer.
				ImageResizer.resizeAndCrop(oldPhotoStream, newSmallPhotoStream, SMALL_PHOTO_SIZE, SMALL_PHOTO_SIZE, cropStartX, cropStartY, cropEndX, cropEndY, ImageResizer.DEFAULT_COLOR, BufferedImage.TYPE_INT_RGB);				
			} 
			else /*if (resize == null || !crop.booleanValue() ) */
			{
				// 66440 - Overriding automatic imageType calculation because BufferedImage.TYPE_INT_ARGB causes problems when PNGs are converted to JPEGs by ImageResizer.
				// 77531 - invoke sig that skips resizing unless needed
				ImageResizer.resizeImage(
						oldPhotoStream, newSmallPhotoStream,
						SMALL_PHOTO_SIZE, SMALL_PHOTO_SIZE, SMALL_PHOTO_SIZE, SMALL_PHOTO_SIZE, 
						0 , //centered
						0 , //centered
						ImageResizer.DEFAULT_COLOR, BufferedImage.TYPE_INT_RGB);
			}
			newSmallPhoto = newSmallPhotoStream.toByteArray();
			photo.setFileType("image/jpeg");
			photo.setImage(newSmallPhoto);
			// thumbnail
			ByteArrayOutputStream newThumbnailPhotoStream =	new ByteArrayOutputStream();
			ImageResizer.resizeImage(
					new ByteArrayInputStream(newSmallPhoto), newThumbnailPhotoStream,
					TINY_PHOTO_SIZE, TINY_PHOTO_SIZE, 
					0 , //centered
					0 , //centered
					ImageResizer.DEFAULT_COLOR, BufferedImage.TYPE_INT_RGB);
			
			byte[] newThumbnail = newThumbnailPhotoStream.toByteArray();
			
			//Add the thumbnail
			photo.setThumbnail(newThumbnail);
			
			photo.setUpdated(new java.sql.Timestamp(System.currentTimeMillis()));

			// PMR 37840,033,724: If the orignal image has the dim of 155x155, we would try to save
			// it directly to the DB. But such image could exceed the max size allowed in the DB,
			// if the original photo has high DPI, like photos from PhotoShop, their size could be as large as 199K.
			// In such cases, we need to call the ImagerResizer to crop and resize the photo with the default( lower )
			// DPI so that the final size is within the maximum allowed byte size.
			if ( newSmallPhoto.length > MAX_IMAGE_SIZE ) {
			    if ( isDEBUG ) {
				LOG.debug("Exceeding max image size with: " +newSmallPhoto.length +", resize it again...");
			    }

			    // Call the 'crop' method to write the image again with the default dim and quality(lower),
			    // so that the cropped/resized image does not exceed the limit in the DB.
			    ByteArrayOutputStream furtherResizedPhotoStream = new ByteArrayOutputStream();
			    ImageResizer.resizeAndCrop(new ByteArrayInputStream(newSmallPhoto), furtherResizedPhotoStream, SMALL_PHOTO_SIZE, SMALL_PHOTO_SIZE, 0, 0, (double)SMALL_PHOTO_SIZE, (double)SMALL_PHOTO_SIZE, ImageResizer.DEFAULT_COLOR, BufferedImage.TYPE_INT_RGB);

			    // Used the newly resized and cropped image to store
			    photo.setImage( furtherResizedPhotoStream.toByteArray() );
			}
			 
			if (emp.isExternal() == false  || isOverlayEnabled == false ){
				photoDao.insertUpdatePhoto(emp, photo);  // PHOTO table
			}
			else {
				// if user is external and overlay is enabled, store a backup of the original pic
				// we'll subsequently use this to overlay the iameg :(
				photoDao.insertUpdatePhotoBkup(emp, photo); // PHOTO_BKUP table
			}
			
			// Hookup with the event logging. Added since 2.5
			EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
			String targetUserKey = photo.getKey();
			if (isDEBUG) {
				LOG.debug("updateImage: targetUser : " + targetUserKey + " current user : " + emp.getKey());
			}
			Employee actorProfile = AppContextAccess.getCurrentUserProfile();
			// the key should ALWAYS be the target user whose photo is being updated
			// call to createEventLogEntry will set appropriate sysEvent value
			EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(pps, actorProfile, emp, EventLogEntry.Event.PROFILE_PHOTO_UPDATED );
			eventLogEntry.setAttachmentData(newSmallPhoto);
			eventLogSvc.insert( eventLogEntry );
		}
		catch (Exception e)	{
			if (LOG.isErrorEnabled()) {
			    LOG.error(e.getMessage(), e);
			}
			throw new ProfilesRuntimeException(e);
		}
		finally {
			if (oldPhotoStream != null) {
				try {
					oldPhotoStream.close();
				} catch (IOException e) {
					if (LOG.isDebugEnabled()) {
					    LOG.debug(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	public void setExternalOverlayImages(String overlayImagePath, String overlayThumbnailImagePath)
	{
    	profileImageExtOverlayContents = getExternalOverlayContents(overlayImagePath);
    	profileImageExtOverlaySmallContents = getExternalOverlayContents(overlayThumbnailImagePath);
	}
	
	private byte[] getExternalOverlayContents(String path) {
		// how do we look in the common override directory?
		String currentPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		String fileStr = "file:";
		int fileIndex = currentPath.indexOf(fileStr);
		if (fileIndex >= 0)
			currentPath = currentPath.substring(fileIndex+fileStr.length());
		
		String jarName = "lc.profiles.core.service.impl.jar";
		//remove jar name also from path
		int jarIndex = currentPath.indexOf(jarName);
		if (jarIndex >= 0)
			currentPath = currentPath.substring(0, jarIndex);
		
		
		if (LOG.isTraceEnabled()) {
			LOG.trace(" getExternalOverlayContents : currentPath=" + currentPath + " path="+path);
		}
		byte[] contents = null;
		try {
			File file = new File(currentPath+path);
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(
					READ_BUFFER_SIZE);

			byte[] buffer = new byte[READ_BUFFER_SIZE]; // larger than file
																// to read

			while (true) {
				int read = fis.read(buffer);
				if (read == -1)
					break;
				bos.write(buffer, 0, read);
			}

			contents = bos.toByteArray();

			// Need to close the stream
			bos.close();
			fis.close();

		} catch (Exception e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(e.getMessage(), e);
			}

			contents = new byte[0];
		}
		return contents;
	}

	private InputStream getPhotoInputStream(PhotoCrop photo) {
		byte[] bytes = (byte[]) photo.getImage();
		if (bytes != null){
			return new ByteArrayInputStream(bytes);
		}
		else{
			return (InputStream) photo.getImageStream();
		}
	}
			
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.PhotoService#deletePhotoByKey(java.lang.String)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void deletePhotoByKeyForTDI(String key) throws ProfilesRuntimeException, DataAccessRetrieveException 
	{
		if (AppContextAccess.isTDIContext() == false){
			throw new ProfilesRuntimeException("cannot call deletePhotoByKeyForTDI outside of TDI context");
		}
		
		assertUDAccess(key, true);

		// Hook up with the event logging. Added since 3.0
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		Employee actorProfile = AppContextAccess.getCurrentUserProfile();
		// the key should ALWAYS be the target user whose photo is being removed
		Employee employee = pps.getProfile(new ProfileLookupKey(ProfileLookupKey.Type.KEY, key), ProfileRetrievalOptions.MINIMUM);	  
		// call to createEventLogEntry will set appropriate sysEvent value
		EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(pps, actorProfile, employee, EventLogEntry.Event.PROFILE_PHOTO_REMOVED );
		eventLogSvc.insert( eventLogEntry );
		//
		photoDao.deletePhotoByKeyForTDI(key);  // primary and bakup table
		// rtc 176130 - photo table has its own lastupdate column.
		// for performance, we will not update the profile lastupdate and not force a re-index
		// photo is not relevant to seedlist data.  'touchProfile' also flushes
		// the profile cache. if need cache flushed, we need a peer method to 'touchProfile'.
		//revisit this issue see rtc 177531
		profSvc.touchProfile(key);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.PhotoService#countProfilesWithPictures()
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public int countProfilesWithPictures() throws ProfilesRuntimeException{
		PhotoRetrievalOptions options = new PhotoRetrievalOptions();
		return photoDao.countProfilesWithPictures(options);
	}
	
	/**
	 * @param key
	 * @throws ProfilesRuntimeException
	 * @throws DataAccessRetrieveException
	 * 
	 * @return Returns <code>true</code> if the system admin is performing the action
	 */
	private boolean assertUDAccess(String key, boolean checkRealUser) throws ProfilesRuntimeException, DataAccessRetrieveException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("assertUDAccess( " + key + ", " + checkRealUser +")");
		}
		ProfileLookupKey plk = ProfileLookupKey.forKey(key);
		if (LOG.isTraceEnabled()) {
			LOG.trace("assertUDAccess() : plk : "+ ((null != plk) ? plk.toString(): " is null"));
		}
		Employee profile = pps.getProfile(plk, ProfileRetrievalOptions.MINIMUM);
		if (LOG.isTraceEnabled()) {
			LOG.trace("assertUDAccess() : profile : "+ ((null != profile) ? profile.getEmail(): " is null"));
		}
		PolicyHelper.assertAcl(Acl.PHOTO_EDIT, profile);
		AssertionUtils.assertNotNull(profile, AssertionType.USER_NOT_FOUND);
		
		return AppContextAccess.isUserInRole(PolicyConstants.ROLE_ADMIN);
	}

    /**
	 *  Get a skeleton Employee object from the photo object to allow ACL checks.
	 *
	 */
    private Employee getSkeletonProfile( Photo photo ) {
	    Employee profile = new Employee();
	    profile.setKey(photo.getKey());
	    profile.setGuid(photo.getGuid());
	    profile.setProfileType(photo.getProfileType());
	    profile.setState( photo.getUserState() );
	    
	    return profile;
	}
}
