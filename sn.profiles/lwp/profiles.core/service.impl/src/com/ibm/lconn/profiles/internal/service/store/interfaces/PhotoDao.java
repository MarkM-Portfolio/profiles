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
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.Photo;
import com.ibm.lconn.profiles.data.PhotoRetrievalOptions;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.PhotoService.ImageType;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;

/**
 *
 */
@Repository
public interface PhotoDao 
{
	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.PhotoDao";
	
	public Photo getPhoto(Employee employee, ImageType type) throws ProfilesRuntimeException;
	
	public Photo getPhotoBkup(Employee employee, ImageType type) throws ProfilesRuntimeException;
	
	public void insertUpdatePhoto(Employee employee, Photo photo) throws ProfilesRuntimeException;
	
	public void insertUpdatePhotoBkup(Employee employee, Photo photo) throws ProfilesRuntimeException;
	
	public void deletePhoto(Employee employee) throws ProfilesRuntimeException;
	
	public Date getUpdateDate(Employee employee) throws ProfilesRuntimeException;
	
	/**
	 * Returns a list of photos given the page size + 1. The extra value
	 * returned is to check if paging should continue.
	 * 
	 * @param options
	 * @return
	 */
	public List<Photo> getAllPhotos(PhotoRetrievalOptions options);
	
	public List<Photo> filterInvalidPhotos(List<Photo> photos);
	
	public Photo getPhotoForTDI(ProfileLookupKey plk) throws ProfilesRuntimeException;
	
	public void deletePhotoByKeyForTDI(String key) throws ProfilesRuntimeException;
	
    public int countProfilesWithPictures(PhotoRetrievalOptions options) throws ProfilesRuntimeException;

    // special method used to switch user tenant key. probably obsolete in visitor model
	public void setTenantKey(String key, String newTenantKey);
}
