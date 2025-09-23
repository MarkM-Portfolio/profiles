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
package com.ibm.lconn.profiles.internal.service;

import java.util.Date;
import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.Photo;
import com.ibm.lconn.profiles.data.PhotoCollection;
import com.ibm.lconn.profiles.data.PhotoCrop;
import com.ibm.lconn.profiles.data.PhotoRetrievalOptions;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

/**
 * Photos are retrieved by Employee so that the key and guid are available.
 * We use the key for on-prem access to PHOTO table. We use the guid on cloud
 * for access to PHOTO_GUID. The dao layer under the service does the access
 * branching.
 * 
 * We do have a few historical access patterns using the key. These support
 * TDI and we can assess conversion.
 */
@Service
public interface PhotoService 
{
	// db queries and photo retrieval cache header calculation does not handle
	// retrieving 'both' images. calls are always for one type of image.
	public static enum ImageType {PHOTO, THUMBNAIL, BOTH};
	
	public static final int TINY_PHOTO_SIZE = 64;

	/**
	 * 
	 */
	public static final int LARGE_PHOTO_SIZE = 300;
	
	/**
	 * Ensure photo size is in range [TINY_PHOTO_SIZE,LARGE_PHOTO_SIZE]. value = min( LARGE_PHOTO_SIZE, max(TINY_PHOTO_SIZE,configValue) )
	 * See rtc item 118290
	 */
	public static final int SMALL_PHOTO_SIZE = Math.min(
			Math.max(PropertiesConfig.getInt(ConfigProperty.SMALL_PHOTO_SIZE), TINY_PHOTO_SIZE), LARGE_PHOTO_SIZE);

	public Photo getPhoto(Employee employee, ImageType type) throws ProfilesRuntimeException;
	
	public Date getPhotoUpdateDate(Employee employee) throws ProfilesRuntimeException;

	/**
	 * Updated a photo. The Photo object is expected to have the user key and/or guid populated. The key is
	 * used for on-prem access to the PHOTO table. The guid is used on cloud for access to table PHOTO_GUID.
	 * 
	 * 05/11/2016 - the employee object is needed as we transition the cloud to a single table. As we
	 * transition, cloud code must populate both PHOTO and PHOTO_GUID tables.
	 * 
	 * @param employee
	 * @param photo
	 * @throws DataAccessRetrieveException
	 * @throws ProfilesRuntimeException
	 */
    public void updatePhoto(Employee employee, PhotoCrop photo) throws DataAccessRetrieveException, ProfilesRuntimeException;

	public void deletePhoto(Employee employee) throws ProfilesRuntimeException;
	
	/**
	 * Historical method supporting TDI, which should be on-prem only access.
	 * @param plk
	 * @return Photo
	 * @throws ProfilesRuntimeException
	 */
	public Photo getPhotoForTDI(ProfileLookupKey plk) throws ProfilesRuntimeException;

	/**
	 * Historical method supporting admin functionality via TDI, which should be on-prem
	 * only access. This method is used by TDI to page over photos in the system.
	 * 
	 * @param options
	 * @return
	 * 
	 * @throws ProfilesRuntimeException
	 */
	public PhotoCollection getAllPhotosForTDI(PhotoRetrievalOptions options);
	
	/**
	 * Historical method supporting admin functionality via TDI, which should be on-prem
	 * Updated a photo. The Photo object is expected to have the user key and/or guid populated. The key is
	 * used for on-prem access to the PHOTO table. The guid is used on cloud for access to table PHOTO_GUID.
	 * 
	 * @param photo
	 * @throws DataAccessRetrieveException
	 * @throws ProfilesRuntimeException
	 */
    public void updatePhotoForTDI(PhotoCrop photo) throws DataAccessRetrieveException, ProfilesRuntimeException;

	/**
	 * Historical method supporting admin functionality via TDI, which should be on-prem
	 * only access.
	 * 
     * @param key
     * @throws DataAccessRetrieveException 
     * @throws ProfilesRuntimeException 
     */
    public void deletePhotoByKeyForTDI(String key) throws DataAccessRetrieveException, ProfilesRuntimeException;
    
    /**
     * HIstorical method used on-prem. The intent is to provide a stat with the count of profiles with images.
     * @return
     * @throws ProfilesRuntimeException
     */
    public int countProfilesWithPictures() throws ProfilesRuntimeException;
}
