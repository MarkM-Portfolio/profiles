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
package com.ibm.lconn.profiles.test.service.photo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.ibm.lconn.profiles.data.Photo;
import com.ibm.lconn.profiles.data.PhotoCollection;
import com.ibm.lconn.profiles.data.PhotoCrop;
import com.ibm.lconn.profiles.data.PhotoRetrievalOptions;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.PhotoService;
import com.ibm.lconn.profiles.internal.service.PhotoService.ImageType;
import com.ibm.lconn.profiles.internal.service.store.interfaces.PhotoDao;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.util.IoUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 *
 */
public class PhotoServiceTest extends BaseTransactionalTestCase {
	private static final String SAM_P = "sam_palmisano.jpg";

	Employee currUser = null;
	String key;
	String guid;

	PhotoService ps;
	PhotoDao pDao;

	public void onSetUpBeforeTransactionDelegate() {
		ps = AppServiceContextAccess.getContextObject(PhotoService.class);
		pDao = AppServiceContextAccess.getContextObject(PhotoDao.class);
	}

	protected void onSetUpInTransaction() throws Exception {
		currUser = CreateUserUtil.createProfile();
		key = currUser.getKey();
		guid = currUser.getGuid();
		runAs(currUser);
	}
	
	public void testCRUDPhoto() throws Exception {
		try {
			ps.deletePhoto(currUser);
			Photo p = ps.getPhoto(currUser, ImageType.PHOTO);
			assertNull(p);
			PhotoCrop photo = new PhotoCrop();
			photo.setKey(key);
			photo.setGuid(guid);
			// photo.setImage(IoUtil.readFileAsByteArray(PhotoServiceTest.class,SAM_P));
			photo.setImage(IoUtil.readFileAsByteArray(this.getClass(), SAM_P));
			ps.updatePhoto(currUser,photo);
			p = ps.getPhoto(currUser,ImageType.PHOTO);
			assertNotNull(p);
			p = ps.getPhoto(currUser,ImageType.THUMBNAIL);
			assertNotNull(p);
			p = ps.getPhoto(currUser,ImageType.BOTH);
			assertNotNull(p);
			ps.deletePhoto(currUser);
		}
		finally {
		}
	}

	public void testCRUDPhotoTDI() throws Exception {
		boolean isTDICtx = AppContextAccess.getContext().setTDIContext(true);
		try {
			ps.deletePhoto(currUser);
			Photo p = ps.getPhotoForTDI(ProfileLookupKey.forKey(currUser.getKey()));
			assertNull(p);
			PhotoCrop photo = new PhotoCrop();
			photo.setKey(key);
			photo.setGuid(guid);
			// photo.setImage(IoUtil.readFileAsByteArray(PhotoServiceTest.class,SAM_P));
			photo.setImage(IoUtil.readFileAsByteArray(this.getClass(), SAM_P));
			ps.updatePhoto(currUser,photo);

			for (ProfileLookupKey.Type plkType : ProfileLookupKey.Type.values()) {
				p = ps.getPhotoForTDI(new ProfileLookupKey(plkType, currUser.getLookupKeyValue(plkType)));
				assertNotNull(p);
			}
			ps.deletePhoto(currUser);
		}
		finally {
			AppContextAccess.getContext().setTDIContext(isTDICtx);
		}
	}
	
	// this test requires that photo overlay is enabled - this is a problem in stand alone
	// as the resources (overlay images) are looked for in the ear structure. not sure if
	// we can copy those to a directory so lookup can succeed.
	// TBD
	/**
	public void testCRUDPhotoExternalUser() throws Exception {
		Employee extemp = CreateUserUtil.createExternalProfile();
		runAs(extemp);
		ps.deletePhoto(extemp);
		Photo p = ps.getPhoto(extemp,ImageType.BOTH);
		assertNull(p);
		p = pDao.getPhotoBkup(extemp,ImageType.BOTH); // backup table PHOTOBKUP
		assertNull(p);
		PhotoCrop photo = new PhotoCrop();
		photo.setKey(extemp.getKey());
		photo.setImage(IoUtil.readFileAsByteArray(this.getClass(),SAM_P));
		// 'upate' call will do an insert. here we test initial creation of a photo.
		ps.updatePhoto(extemp,photo);
		// see if the photo is in both the primary and backup photo tables
		Photo photoDb = pDao.getPhoto(extemp,ImageType.BOTH); // primary table PHOTO
		assertNotNull(photoDb);
		photoDb = pDao.getPhotoBkup(extemp,ImageType.BOTH); // backup table PHOTOBKUP
		assertNotNull(photoDb);
		// update again so we go through code that does an update an existing photo.
		ps.updatePhoto(extemp,photo);
		// see if the photo is in both the primary and backup photo tables
		photoDb = pDao.getPhoto(extemp,ImageType.BOTH); // primary table PHOTO
		assertNotNull(photoDb);
		photoDb = pDao.getPhotoBkup(extemp,ImageType.BOTH); // backup table PHOTOBKUP
		assertNotNull(photoDb);
		// now delete
		ps.deletePhoto(extemp);
		photoDb = pDao.getPhoto(extemp,ImageType.BOTH); // primary table
		assertNull(photoDb);
		photoDb = pDao.getPhotoBkup(extemp,ImageType.BOTH); // backup table PHOTOBKUP
		assertNotNull(photoDb);
		runAs(currUser); // flip back to currUser
	}
	*/

	public void testCountPhotos() throws Exception {
		ps.deletePhoto(currUser);
		int before = ps.countProfilesWithPictures();
		HashMap<String, Object> values = new HashMap<String, Object>();
		PhotoCrop photo = new PhotoCrop();
		photo.setKey(key);
		photo.setGuid(guid);
		//photo.setImage(IoUtil.readFileAsByteArray(PhotoServiceTest.class,SAM_P));
		photo.setImage(IoUtil.readFileAsByteArray(this.getClass(),SAM_P));
		ps.updatePhoto(currUser,photo);
		assertEquals(before + 1, ps.countProfilesWithPictures());
	}

	public void testGetAllPhotosForTDI() throws Exception {
		// TDI is the only code that calls the PhotoService getAllPhotos. It looks like
		// TDI allowed one to iterate through photos. This is not core behavior for the
		// UI of API as this method is a performance concern.
		boolean isTDICtx = AppContextAccess.getContext().setTDIContext(true);
		try {
			PhotoCrop photo = new PhotoCrop();
			// photo.setImage(IoUtil.readFileAsByteArray(PhotoServiceTest.class,SAM_P));
			photo.setImage(IoUtil.readFileAsByteArray(this.getClass(), SAM_P));
			final int numUsersGen = 5;
			final List<Employee> users = new ArrayList<Employee>();

			runAsAdmin(Boolean.TRUE);

			for (int i = 0; i < numUsersGen; i++) {
				Employee newUser = CreateUserUtil.createProfile();
				users.add(newUser);
				runAs(newUser);
				photo.setKey(newUser.getKey());
				photo.setGuid(newUser.getGuid());
				ps.updatePhoto(newUser, photo);
			}
			// check that all users added
			final int totalPhotos = ps.countProfilesWithPictures();
			assertTrue(totalPhotos >= numUsersGen);
			// test if can iterate with page size of (1)
			assertGetAllWithLooping(totalPhotos, users, new PhotoRetrievalOptions().setPageSize(1));
			// test with size of (3)
			assertGetAllWithLooping(totalPhotos, users, new PhotoRetrievalOptions().setPageSize(3));
			// test with size equal to total photos
			assertGetAllWithLooping(totalPhotos, users, new PhotoRetrievalOptions().setPageSize(totalPhotos));
			// test with size larger than total photos
			assertGetAllWithLooping(totalPhotos, users, new PhotoRetrievalOptions().setPageSize(totalPhotos + 1));
		}
		finally {
			AppContextAccess.getContext().setTDIContext(isTDICtx);
		}
	}
	
	public void testTooManyResults(){
		// see comments in PhotoSqlMapDao.handleSqlException for a discussion of why we see this error.
		// only the calls to TDI specific interfaces allow a PLK for photo.
		boolean isTDICtx = AppContextAccess.getContext().setTDIContext(true);
		try {
			// insert a photo row for current user
			StringBuffer stmt = new StringBuffer();
			stmt.append("INSERT INTO EMPINST.PHOTO (PROF_KEY, PROF_FILE_TYPE, PROF_UPDATED, TENANT_KEY) VALUES (");
			stmt.append("'").append(currUser.getKey()).append("'").append(",");
			stmt.append("'image/jpg'").append(",");
			stmt.append("CURRENT TIMESTAMP").append(",");
			stmt.append("'").append(currUser.getTenantKey()).append("'").append(")");
			jdbcTemplate.execute(stmt.toString());
			// create another user
			Employee user2 = CreateUserUtil.createProfile();
			// insert a record into the PHOTO table for this user
			stmt.setLength(0);
			stmt.append("INSERT INTO EMPINST.PHOTO (PROF_KEY, PROF_FILE_TYPE, PROF_UPDATED, TENANT_KEY) VALUES (");
			stmt.append("'").append(user2.getKey()).append("'").append(",");
			stmt.append("'image/jpg'").append(",");
			stmt.append("CURRENT TIMESTAMP").append(",");
			stmt.append("'").append(user2.getTenantKey()).append("'").append(")");
			jdbcTemplate.execute(stmt.toString());
			// update user2's mcode to match currUser
			stmt.setLength(0);
			stmt.append("UPDATE EMPINST.EMPLOYEE SET PROF_IDHASH = '").append(currUser.getMcode()).append("' WHERE PROF_KEY = '")
					.append(user2.getKey()).append("'");
			jdbcTemplate.execute(stmt.toString());
			// lookup the photo by this hashid
			String msg = "";
			try {
				Photo p = ps.getPhotoForTDI(ProfileLookupKey.forHashId((currUser.getMcode())));
			}
			catch (Exception ex) {
				msg = ex.getMessage();

			}
			// if we don't see this string in the return, check PhotoSqlMapDao
			assertTrue(msg.contains("too many results"));
		}
		finally {
			AppContextAccess.getContext().setTDIContext(isTDICtx);
		}
	}

	/**
	 * Iterates through all of the photos and checks that all of the photos expected are in the result. To iterate, the method starts with
	 * the supplied method.
	 * 
	 * @param users
	 * @param setPageSize
	 */
	private void assertGetAllWithLooping(final int totalPhotos, final List<Employee> users, PhotoRetrievalOptions options) {
		int found = 0;
		Set<String> keys = new HashSet<String>(ProfileHelper.getKeyList(users));
		Set<String> foundKeys = new HashSet<String>();

		while (options != null) {
			PhotoCollection photos = ps.getAllPhotosForTDI(options);

			for (Photo p : photos.getResults()) {
				// this ensures that we have no duplicates
				// - on a duplicate value the Set<> will return false
				assertTrue(foundKeys.add(p.getKey()));
				// Check that we find all of the users we expect
				keys.remove(p.getKey());
				// check the count of
				found++;
			}
			options = photos.getNextSet();
		}
		// this code may be too restrictive for a db that already contains data. we could find more photos than inserted.
		//// Check that we found the number of users expecting
		////assertEquals(totalPhotos, found);
		// Check that all of the expected users were found
		assertTrue(keys.size() == 0);
	}

	//private byte[] readFile(String fileName) throws Exception {
	//	InputStream is = PhotoServiceTest.class.getResourceAsStream(fileName);
	//	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	//	byte[] buffer = new byte[1024];
	//	int read;
	//	while ((read = is.read(buffer)) >= 0)
	//		bos.write(buffer, 0, read);
	//
	//	return bos.toByteArray();
	//}
}
