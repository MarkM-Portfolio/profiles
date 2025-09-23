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
 * This is a historical file that contains logic related to consolidating cloud photos to a single table
 * based on guid (subscriberid) as the sole key. The idea was that photos in a data center would consolidate
 * to a single table (PHOTO_GUID), then eventually to a centralized photo service that should/could service
 * photos worldwide. The work was done in conjunction with db fixupscripts postfixup55.1s.sql and
 * postfixup55.2s.sql. Look at the history of those files.
 * 
 * This class encapsulates photo insert/update of the photo tables. The code is in transition to support
 * a single photo per data center. Once complete, photos are held in the following tables.
 *   PHOTO_GUID - on cloud, holds a user's photo with pk 'prof_guid', which is the bss subscriber id
 *   PHOTO  - on-prem, holds a user's photo with (effective) pk 'prof_key', which is the user's internal id
 * The must transition as we support 'minimal downtime' on the cloud. The general steps are
 *  (1) update the data schema during a brief off line period.
 *  (2) start the apps with photos all still in PHOTO
 *  (3) begin a process to backfill the PHOTO_GUID table.
 *       any interim inserts/updates on cloud occur in both tables
 *       retrieve photos from the PHOTO table
 *  (4) once the backfill process is complete we are to flip all cloud access to retrieve from PHOTO_GUID
 * A gatekeeper flag indicates when we are to flip to using the PHOTO_GUID table. See
 *   LCSupportedFeature.PROFILES_CLOUD_SINGLE_DB_PHOTO
 * When enabled on cloud, this flag instructs the code to use the single photo table PHOTO_GUID.
 * Code below has code that is irrelevant once the gatekeeper flag is permanently enabled and ultimately
 * removed. These lines have a trailing comment "// x".
 */
@Repository(PhotoDao.REPOSNAME)
public class PhotoSqlMapDao_GUID extends AbstractSqlMapDao implements PhotoDao
{
	private final static String CLASS_NAME = PhotoSqlMapDao_GUID.class.getName();
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
//:(			if (LCConfig.instance().isLotusLive()) {
//:(				if (isDebug) logger.finer("isLotusLive = true");
//:(				// x if we transitioned to use global PHOTO_GUID table, look there. ow, we use the PHOTO table // x
//:(				if (isCloudUsePhotoGuidOnly() == false){                   // x 
//:(					if (isDebug) logger.finer("retrieve from PHOTO");      // x
					rtn = getPhotoTable(employee, type);                   // x
//:(				}                                                          // x
//:(				else{                                                      // x
//:(					if (isDebug) logger.finer("retrieve from PHOTO_GUID");
//:(					rtn = getPhotoGuidTable(employee, type);
//:(				}                                                          // x
//:(			}
//:(			else {
				if (isDebug) logger.finer("retrieve photo from PHOTO");
				rtn = getPhotoTable(employee, type);
//			}
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
//:(		if (LCConfig.instance().isLotusLive()) {
//:(			if (isDebug) logger.finer("isLotusLive = true");
//:(			//
//:(			if (isCloudUsePhotoGuidOnly()){                                 // x
//:(				if (isDebug) logger.finer("insert/update into PHOTO_GUID"); // x
//:(				this.insertUpdatePhotoGuidTable(employee,photo);
//:(			}                                                               // x
//:(			else{                                                           // x
//:(				if (isCloudUsePhotoGuidTableForCUD()){                      // x
//:(					this.insertUpdatePhotoGuidTable(employee,photo);        // x
//:(				}                                                           // x
//:(			this.insertUpdatePhotoTable(employee,photo);                // x
//:(			}                                                               // x
//:(		}
//:(		else {
			if (isDebug) logger.finer("insert/update into PHOTO");
			this.insertUpdatePhotoTable(employee,photo);
//:(		}	
	}
	
	public void deletePhoto(Employee employee){
		boolean isDebug = logger.isLoggable(FINER);
		if (isDebug) logger.entering(CLASS_NAME,"getPhoto");
		if (isDebug) logger.finer("getPhoto employee: key/guid="+getIds(employee));
		//
//:(		if (LCConfig.instance().isLotusLive()) {
//:(			if (isDebug) logger.finer("isLotusLive = true");
//:(			//
//:(			if (isCloudUsePhotoGuidOnly()){                                 // x
//:(				if (isDebug) logger.finer("insert/update into PHOTO_GUID"); // x
//:(				this.deletePhotoGuidTable(employee);
//:(			}                                                               // x
//:(			else{                                                           // x
//:(				if (isCloudUsePhotoGuidTableForCUD()){                      // x
//:(					this.deletePhotoGuidTable(employee);                    // x
//:(				}                                                           // x
//:(				this.deletePhotoByKey(employee.getKey());                   // x
//:(			}                                                               // x
//:(		}
//:(		else {
//:(			if (isDebug) logger.finer("isLotusLive = false");
			if (isDebug) logger.finer("delete from PHOTO");
			this.deletePhotoByKey(employee.getKey());
//:(		}
	}
	
	public Date getUpdateDate(Employee employee) throws ProfilesRuntimeException {
		boolean isDebug = logger.isLoggable(FINER);
		if (isDebug) logger.entering(CLASS_NAME,"getUpdateDate");
		if (isDebug) logger.finer("getUpdateDate employee: key/guid="+getIds(employee));
		//
		Date rtn = null;
//:(		if (LCConfig.instance().isLotusLive()) {
//:(			if (isDebug) logger.finer("isLotusLive = true");
//:(			// x if we transitioned to use global PHOTO_GUID table, look there. ow, we use the PHOTO table // x
//:(			if (isCloudUsePhotoGuidOnly() == false){                   // x 
//:(				if (isDebug) logger.finer("retrieve from PHOTO");      // x
//:(				rtn = getPhotoUpdateDate(employee);                    // x
//:(			}                                                          // x
//:(			else{                                                      // x
//:(				if (isDebug) logger.finer("retrieve from PHOTO_GUID");
//:(				rtn = getPhotoGuidUpdateDate(employee);
//:(			}                                                          // x
//:(		}
//:(		else {
			if (isDebug) logger.finer("retrieve photo from PHOTO");
			rtn = getPhotoUpdateDate(employee);
//:(		}
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
	
	//*************************************************************************
	// util methods
	//*************************************************************************
	
//	// get worker  -  calling method will handle exceptions
//	private final Photo getPhotoGuidTable(Employee employee, ImageType type) throws DataAccessException {
//		Photo rtn = null;
//		if (employee == null) {
//			return rtn;
//		}
//		// cloud retrieval uses the guid/subscriberid and table PHOTO_GUID
//		String guid = employee.getGuid();
//		if (StringUtils.isNotEmpty(guid)) {
//			Map<String, String> m = guidToMap(guid, 2); // supply size of map knowing how many args we'll use
//			switch (type) {
//				case PHOTO :
//					m.put("isImage", "true");
//					break;
//				case THUMBNAIL :
//					m.put("isThumbnail", "true");
//					break;
//				//case BOTH :
//				//	m.put("isImage", "true");
//				//	m.put("isThumbnail", "true");
//				//	break;
//			}
//			m.put("lookupValue",guid);
//			rtn = filterOrgPhoto((Photo) getSqlMapClientTemplate().queryForObject("Photo.GetPhotoGuidImage", m));
//		}
//		return rtn;
//	}
	
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
				// x during tansition to PHOTO_GUID, on cloud we look for photo in PHOTO. tenantKey is the user's home key.
//:(				if (isCloudUsePhotoGuidOnly() == true) {                 // x
//:(					if (LCConfig.instance().isLotusLive()){              // x
//:(						m.put("dbTenantKey",employee.getHomeTenantKey());// x
//:(					}                                                    // x
//:(					else{                                                // x
//:(						m.put("dbTenantKey",Tenant.DB_SINGLETENANT_KEY); // x
//:(					}                                                    // x
//:(				}                                                        // x
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
//:(			// x during tansition to PHOTO_GUID, on cloud we look for photo in PHOTO. tenantKey is the user's home key.
//:(			if (isCloudUsePhotoGuidOnly() == true) { // x
//:(				m.put("dbTenantKey", employee.getHomeTenantKey());// x
//:(			} // x
			rtn = (Date) getSqlMapClientTemplate().queryForObject("Photo.GetPhotoUpdateDate", m);
		}
		return rtn;
	}
	
//	private Date getPhotoGuidUpdateDate(Employee employee){
//		Date rtn = null;
//		if (employee == null) {
//			return rtn;
//		}
//		// cloud retrieval uses the guid/subscriberid and table PHOTO_GUID
//		String guid = employee.getGuid();
//		if (StringUtils.isNotEmpty(guid)) {
//			Map<String, String> m = guidToMap(guid,1); // supply size of map knowing how many args we'll use
//			m.put("lookupValue",guid);
//			rtn = (Date) getSqlMapClientTemplate().queryForObject("Photo.GetPhotoGuidUpdateDate", m);
//		}
//		return rtn;
//		
//	}
	
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
	
//	private void insertUpdatePhotoGuidTable(Employee employee,Photo photo) {
//		// note: may be cheaper to select a row via the select criteria with a simple. e.g.
//		//  SELECT 1 WHERE PROF_KEY = ? AND ...
//		// if we get a hit, we know it is an udpate. ow it is a create. this is not so
//		// optimal if the vast majority of users have photos.
//		photo.setTenantKey(employee.getHomeTenantKey());
//		photo.setUpdated(SNAXConstants.TX_TIMESTAMP.get());
//		Integer count = (Integer)getSqlMapClientTemplate().update("Photo.updatePhotoGuid", photo);
//		if (count.intValue() == 0){
//			getSqlMapClientTemplate().insert("Photo.insertPhotoGuid", photo);
//		}
//	}
	
	// delete worker
//	private void deletePhotoGuidTable(Employee employee) throws ProfilesRuntimeException {
//		if (employee == null){
//			return;
//		}
//		// cloud retrieval uses the guid/subscriberid and table PHOTO_GUID
//		String guid = employee.getGuid();
//		if (StringUtils.isNotEmpty(guid)){
//			Map<String, String> m = guidToMap(guid,1);
//			getSqlMapClientTemplate().delete("Photo.deletePhotoGuid",m);
//		}
//	}
	
	//delete worker
	public void deletePhotoByKey(String key)	{
		Map<String,Object> m = getMapForRUD(1);
		m.put("key",key);
		getSqlMapClientTemplate().delete("Photo.deletePhotoByKey", m);
		getSqlMapClientTemplate().delete("Photo.deletePhotoBkupByKey", m);
	}
	
//	//delete worker
//	public void deletePhotoBkupByKey(String key)	{
//		Map<String,Object> m = getMapForRUD(1);
//		m.put("key",key);
//		getSqlMapClientTemplate().delete("Photo.deletePhotoBkupByKey", m);
//	}
	
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
	
//	private final Map<String,String> guidToMap(String guid, int size){
//		HashMap<String,String> rtn = new HashMap<String,String>(size);
//		rtn.put(PeoplePagesServiceConstants.GUID,guid);
//		return rtn;
//	}
	
//	private final boolean isCloudUsePhotoGuidOnly() {
//		boolean rtn = SchemaVersionInfo.instance().supports(Feature.CLOUD_PHOTOGUID_USE_PHOTO_GUID_TABLE_ONLY);
//		return rtn;
//	}
	
//	private final boolean isCloudUsePhotoGuidTableForCUD() {
//		boolean rtn = SchemaVersionInfo.instance().supports(Feature.CLOUD_USE_PHOTO_GUID_TABLE_FOR_CUD);
//		return rtn;
//	}
	
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
