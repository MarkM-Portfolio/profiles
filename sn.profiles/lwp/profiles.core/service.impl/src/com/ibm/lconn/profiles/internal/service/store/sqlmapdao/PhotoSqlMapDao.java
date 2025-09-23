/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                   */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import static java.util.logging.Level.FINER;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.profiles.data.Photo;
import com.ibm.lconn.profiles.data.PhotoRetrievalOptions;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.PhotoService.ImageType;
import com.ibm.lconn.profiles.internal.service.store.interfaces.PhotoDao;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.internal.resources.ResourceManager;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * 
 */
@Repository(PhotoDao.REPOSNAME)
public class PhotoSqlMapDao extends AbstractSqlMapDao implements PhotoDao
{
	private final static String CLASS_NAME = PhotoSqlMapDao.class.getName();
	private static Logger logger = Logger.getLogger(CLASS_NAME,ResourceManager.BUNDLE_NAME);
	
	public Photo getPhoto(Employee employee, ImageType type) {
		boolean isDebug = logger.isLoggable(FINER);
		if (isDebug) logger.entering(CLASS_NAME,"getPhoto");
		//
		Photo rtn = null;
		if (employee == null) {
			if (isDebug) logger.finer("getPhoto employee is null");
			return rtn;
		}
		if (isDebug) logger.finer("getPhoto employee: key/guid="+getIds(employee));
		try {
			if (isDebug) logger.finer("retrieve photo from PHOTO");
			rtn = getPhotoTable(employee, type);
		}
		catch (DataAccessException daex) {
			ProfileLookupKey plk = null;
			if (StringUtils.isNotBlank(employee.getKey())) {
				plk = ProfileLookupKey.forKey(employee.getKey());
			}
			else if (StringUtils.isNotBlank(employee.getGuid())) {
				plk = ProfileLookupKey.forGuid(employee.getGuid());
			}
			handleSqlException(daex, plk);
		}
		return rtn;
	}
	
	public Photo getPhotoBkup(Employee employee, ImageType type) {
		boolean isDebug = logger.isLoggable(FINER);
		if (isDebug) logger.entering(CLASS_NAME,"getPhoto");
		//
		Photo rtn = null;
		if (employee == null) {
			if (isDebug) logger.finer("getPhoto employee is null");
			return rtn;
		}
		if (isDebug) logger.finer("getPhoto employee: key/guid="+getIds(employee));
		try {
			if (isDebug) logger.finer("retrieve photo from PHOTO");
			rtn = getPhotoBkupTable(employee, type);
		}
		catch (DataAccessException daex) {
			ProfileLookupKey plk = null;
			if (StringUtils.isNotBlank(employee.getKey())) {
				plk = ProfileLookupKey.forKey(employee.getKey());
			}
			else if (StringUtils.isNotBlank(employee.getGuid())) {
				plk = ProfileLookupKey.forGuid(employee.getGuid());
			}
			handleSqlException(daex, plk);
		}
		return rtn;
	}
	
	public void insertUpdatePhoto(Employee employee, Photo photo){
		boolean isDebug = logger.isLoggable(FINER);
		if (isDebug) logger.entering(CLASS_NAME,"insertUpdatePhoto");
		if (isDebug) logger.finer("getPhoto employee: key/guid="+getIds(employee));
		//
		if (isDebug) logger.finer("insert/update into PHOTO");
		this.insertUpdatePhotoTable(employee,photo);
	}
	
	public void deletePhoto(Employee employee){
		boolean isDebug = logger.isLoggable(FINER);
		if (isDebug) logger.entering(CLASS_NAME,"getPhoto");
		if (isDebug) logger.finer("getPhoto employee: key/guid="+getIds(employee));
		//
		if (isDebug) logger.finer("delete from PHOTO");
		this.deletePhotoByKey(employee.getKey());
	}
	
	public Date getUpdateDate(Employee employee) throws ProfilesRuntimeException {
		boolean isDebug = logger.isLoggable(FINER);
		if (isDebug) logger.entering(CLASS_NAME,"getUpdateDate");
		if (isDebug) logger.finer("getUpdateDate employee: key/guid="+getIds(employee));
		//
		if (isDebug) logger.finer("retrieve photo from PHOTO");
		Date rtn = getPhotoUpdateDate(employee);
		return rtn;
	}
	
	@SuppressWarnings("unchecked")
	public List<Photo> getAllPhotos(PhotoRetrievalOptions options) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("options",options);
		return (List<Photo>) getSqlMapClientTemplate().queryForList("Photo.getAllPhotos", m, 0, options.getPageSize() + 1);
	}
	
	/**
	 * Removes photo objects that are not valid
	 * @param queryForList
	 * @return
	 */
	public List<Photo> filterInvalidPhotos(List<Photo> photos) {
		Iterator<Photo> photoIt = photos.iterator();
		while (photoIt.hasNext()) {
			if (filterInvalid(photoIt.next()) == null)
				photoIt.remove();
		}
		return photos;
	}
	
	public Photo getPhotoForTDI(ProfileLookupKey plk) {
		Photo rtn = null;
		try {
			if (plk.isValid()) {
				Map<String, String> m = plk.toMap();
				augmentMapForRUD(m);
				m.put("isImage", "true");
				rtn = filterOrgPhoto((Photo) getSqlMapClientTemplate().queryForObject("Photo.GetPhotoImageForTDI", m));
			}
		}
		catch (DataAccessException daex) {
			handleSqlException(daex, plk);
		}
		return rtn;
	}
	
	public void deletePhotoByKeyForTDI(String key) throws ProfilesRuntimeException {
		if (AppContextAccess.isTDIContext() == false){
			throw new ProfilesRuntimeException("cannot call PhotoDao.deletePhotoByKeyForTDI outside of TDI context");
		}
		deletePhotoByKey(key);
	}
	
	public int countProfilesWithPictures(PhotoRetrievalOptions options){
		Map<String,Object> m = getMapForRUD(1);
		m.put("options",options);
		Integer value = (Integer) getSqlMapClientTemplate().queryForObject("Photo.countProfilesWithPictures",m);
		if (value == null){
			return 0;
		}
		return value;
	}
	
    // special method used to switch user tenant key. probably obsolete in visitor model
	public void setTenantKey(String profileKey, String newTenantKey){
		Map<String,Object> m = getMapForRUD(2);
		m.put("key",profileKey);
		m.put("newTenantKey",newTenantKey);
		getSqlMapClientTemplate().update("Photo.updateTenantKey",m);
	}
	
	// get worker
	private final Photo getPhotoTable(Employee employee, ImageType type){
		Photo rtn = null;
		// on-prem retrieval uses the key and table PHOTO
		String key = employee.getKey();
		ProfileLookupKey plk = ProfileLookupKey.forKey(key);
		try {
			if (plk.isValid()){
				Map<String, String> m = plk.toMap();
				augmentMapForRUD(m);
				switch (type) {
					case PHOTO :
						m.put("isImage", "true");
						break;
					case THUMBNAIL :
						m.put("isThumbnail", "true");
						break;
					case BOTH :
						m.put("isImage", "true");
						m.put("isThumbnail", "true");
						break;
				}
				rtn = filterOrgPhoto((Photo) getSqlMapClientTemplate().queryForObject("Photo.GetPhotoImage", m));
				// avoiding a join by setting the mode via the passed in employee.
				if (rtn != null) rtn.setUserMode(employee.getMode());
			}
		}
		catch (DataAccessException daex) {
			handleSqlException(daex, plk);
		}
		return rtn;
	}
	
	private final Photo getPhotoBkupTable(Employee employee, ImageType type){
		Photo rtn = null;
		// on-prem retrieval uses the key and table PHOTO
		String key = employee.getKey();
		ProfileLookupKey plk = ProfileLookupKey.forKey(key);
		try {
			if (plk.isValid()){
				Map<String, String> m = plk.toMap();
				augmentMapForRUD(m);
				switch (type) {
					case PHOTO :
						m.put("isImage", "true");
						break;
					case THUMBNAIL :
						m.put("isThumbnail", "true");
						break;
					case BOTH :
						m.put("isImage", "true");
						m.put("isThumbnail", "true");
						break;
				}
				rtn = filterOrgPhoto((Photo) getSqlMapClientTemplate().queryForObject("Photo.GetPhotoBkupImage", m));
			}
		}
		catch (DataAccessException daex) {
			handleSqlException(daex, plk);
		}
		return rtn;
	}
	
	private Date getPhotoUpdateDate(Employee employee) {
		Date rtn = null;
		// on-prem retrieval uses the key and table PHOTO
		String key = employee.getKey();
		ProfileLookupKey plk = ProfileLookupKey.forKey(key);
		if (plk.isValid()) {
			Map<String, String> m = plk.toMap();
			augmentMapForRUD(m);
			rtn = (Date) getSqlMapClientTemplate().queryForObject("Photo.GetPhotoUpdateDate", m);
		}
		return rtn;
	}
	
	private void insertUpdatePhotoTable(Employee employee, Photo photo) {
		// update
		Map<String, Object> m = getMapForRUD(1);
		photo.setUpdated(SNAXConstants.TX_TIMESTAMP.get());
		m.put("photo", photo);
		Integer count = (Integer) getSqlMapClientTemplate().update("Photo.updatePhoto", m);
		if (count.intValue() == 0) {
			// create
			// - set above photo.setUpdated(SNAXConstants.TX_TIMESTAMP.get());
			setTenantKeyForC(photo);
			getSqlMapClientTemplate().insert("Photo.insertPhoto", photo);
		}
	}
	
	public void insertUpdatePhotoBkup(Employee employee, Photo photo) {
		// update
		Map<String, Object> m = getMapForRUD(1);
		photo.setUpdated(SNAXConstants.TX_TIMESTAMP.get());
		m.put("photo", photo);
		Integer count = (Integer) getSqlMapClientTemplate().update("Photo.updatePhotoBkup", m);
		if (count.intValue() == 0) {
			// create
			// - set above photo.setUpdated(SNAXConstants.TX_TIMESTAMP.get());
			setTenantKeyForC(photo);
			getSqlMapClientTemplate().insert("Photo.insertPhotoBkup", photo);
		}
	}
	
	//delete worker
	public void deletePhotoByKey(String key)	{
		Map<String,Object> m = getMapForRUD(1);
		m.put("key",key);
		getSqlMapClientTemplate().delete("Photo.deletePhotoByKey", m);
		getSqlMapClientTemplate().delete("Photo.deletePhotoBkupByKey", m);
	}
	
	/**
	 * Removes photo objects that are not valid
	 * @param queryForObject
	 * @return
	 */
	private Photo filterInvalid(Photo photo) {
		if (photo != null && photo.getImage() != null && photo.getImage().length > 0) {
			return photo;
		}
		return null;
	}
	
	private Photo filterOrgPhoto(Photo photo) {
		if (photo != null && photo.getImage() != null && photo.getImage().length == 0) {
			photo.setImage(null);
		}
		return photo;
	}
	
	// util method attempting to help ferret out 'too many results' issue related to jazz items
	// https://swgjazz.ibm.com:8004/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/192428
	// https://swgjazz.ibm.com:8001/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/173968
	// would like a more central spot, but error is happening directly on getSqlMapClientTemplate()
	// which is buried in ibatis and i don't see a clear wrapper for more general error handling
	// the exception we see is
	//   org.springframework.jdbc.UncategorizedSQLException: SqlMapClient operation; uncategorized SQLException for SQL [];
	//      SQL state [null]; error code [0]; Error: executeQueryForObject returned too many results
	// with cause
	//    java.sql.SQLException: Error: executeQueryForObject returned too many results.
	// uncategorized SQLException for SQL []; SQL state [null]; error code [0]; Error: executeQueryForObject returned too many results.
	// we have info from production and the issue is related to historical cloud code that removed the user email when
	// inactivated. this was on-prem behavior, and it resulted in multile accounts having the same hashid corresponding to
	// the null string.
	private static final String TOOMANYRESULTS = "too many results";
	private void handleSqlException(DataAccessException daex, ProfileLookupKey plk) throws DataAccessException {
		String msg = daex.getMessage();
		if (StringUtils.contains(msg, TOOMANYRESULTS)) {
			StringBuffer error = new StringBuffer("PhotoSqlMapDao SQLException: too many results for lookup identifier: ");
			error.append(plk.toString());
			throw new com.ibm.lconn.profiles.internal.exception.DataAccessException(error.toString());
			// if we can see who these accounts are, maybe we can clean them up with a another worker (a new thread?) task
		}
		throw daex;
	}
	
	private String getIds(Employee employee){
		StringBuffer sb = new StringBuffer();
		if (employee == null){
			sb.append("null/null");
		}
		else{
			sb.append(employee.getKey()).append("/").append(employee.getGuid());
		}
		return sb.toString();
	}
}
