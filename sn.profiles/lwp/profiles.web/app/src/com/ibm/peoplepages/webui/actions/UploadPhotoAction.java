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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import com.ibm.lconn.core.file.LCTempFile;
import com.ibm.lconn.core.file.LCTempFileManager;
import com.ibm.lconn.core.file.LCTempFileManagerFactory;
import com.ibm.lconn.core.image.ImageUtils;
import com.ibm.lconn.profiles.api.actions.APIAction;
import com.ibm.lconn.profiles.data.PhotoCrop;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.PhotoService;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.AntiVirusFilter;
import com.ibm.peoplepages.util.FileSubmissionHelper;
import com.ibm.peoplepages.webui.actions.TempPhotoHelper.TempFile;
import com.ibm.peoplepages.webui.ajax.actions.LoginInfoAction;
import com.ibm.peoplepages.webui.forms.UploadPhotoForm;
import com.ibm.sn.av.api.AVScannerException;


/**
 *
 */
public class UploadPhotoAction extends APIAction {
	private static final Log LOG = LogFactory.getLog(UploadPhotoAction.class);

	/**
	 * Utility class required so AVScan method can replace content
	 */
	private static class TFHolder {
		private LCTempFile ref;
		public TFHolder(InputStream is) throws IOException { set(is); }
		
		protected LCTempFile get() {
			return ref;
		}
		
		protected void set(InputStream is) throws IOException {
			LCTempFileManager mgr = LCTempFileManagerFactory.getInstance();
			LCTempFile old = ref;	
			this.ref = mgr.newTempFile(is);
			if (old != null) old.delete();
		}
	}
	
	@Override
	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws Exception {

		if (LOG.isDebugEnabled()) {
		    LOG.debug(" UploadPhotoAction.doExecutePOST called...");
		}

		PhotoService service = AppServiceContextAccess.getContextObject(PhotoService.class);
		Employee currentUser = LoginInfoAction.getCachedUserRecord(request);

		// allows users to only access their own files
		if (currentUser != null) {
			String key = currentUser.getKey();
			String guid = currentUser.getGuid();
			UploadPhotoForm UPform = (UploadPhotoForm) form;

			//4 cases:
			if (UPform.isRemovePhoto()) {//1. remove photo
				TempPhotoHelper.clearTempFile(request); // just in case
				service.deletePhoto(currentUser);
				
				//special case due to odd AJAX behavior
				response.getWriter().println("<html><body><textarea>Photo Removed OK</textarea></body>");
				return null;
			}
			
			
			else if (UPform.isCrop()) {//2. crop a temporary photo that we already have
				PhotoCrop photo = new PhotoCrop();
				photo.setKey(key);
				photo.setGuid(guid);
				photo.setSessionId(request.getSession().getId());
				photo.setIsCrop(Boolean.TRUE);
				photo.setStartx(UPform.getStartx());
				photo.setStarty(UPform.getStarty());
				photo.setEndx(UPform.getEndx());
				photo.setEndy(UPform.getEndy());
				/*for (Map.Entry<String, Object> e : photo.entrySet()) {
					System.out.println(e.getKey() + ": " + e.getValue());
				}*/
				TempFile tempFile = TempPhotoHelper.getTempFile(request);
				if (tempFile != null) {
					try {
						photo.setImageStream(tempFile.getFileHandle().openDeleteOnClose());
						service.updatePhoto(currentUser,photo);
					} finally {
						// clean up
						TempPhotoHelper.clearTempFile(request);
					}
				}
				
				//special case due to odd AJAX behavior - not sure what this means other than someone is expecting a body
				response.getWriter().println("<html><body><textarea>Photo Cropped OK</textarea></body>");
				return null;
			}
			
			else if (UPform.getPhoto() != null && UPform.isTemp()) {//3. accept a new temporary photo
				
				FormFile photoFile = UPform.getPhoto();

				TFHolder holder = getTempFile(photoFile);
				
				if (holder == null)
					return null;
				
				String errorString = this.getErrorMessages(photoFile, holder);
				if (errorString != null) {
					holder.get().delete();
					response.getWriter().println("<html><body><textarea>" +errorString +"</textarea></body>");
					return null;
				}
				
		        TempFile photoTemp = new TempFile(photoFile.getFileName(), holder.get());
		        TempPhotoHelper.setTempFile(request, photoTemp);
		        
				//special case due to odd AJAX behavior
				response.getWriter().println("<html><body><textarea>Temp Photo OK</textarea></body>");
				return null;
			}
			
			else if (UPform.getPhoto() != null) {//4. accept a new photo and crop it
				TempPhotoHelper.clearTempFile(request); // just in case
				
				PhotoCrop photo = new PhotoCrop();
				photo.setKey(key);
				photo.setGuid(guid);
				photo.setSessionId(request.getSession().getId());
				
				// get the file
                FormFile photoFile = UPform.getPhoto();
                TFHolder holder = getTempFile(photoFile);                
                
				// START ======== only operate on non-empty photo
				if (holder != null) {
					// get the temp file
	                String contentType = photoFile.getContentType();
	                photo.setFileType(contentType);
	
	                // check for errors
	                String errorString = getErrorMessages(photoFile, holder);
	                
	                // Note: the errorString is used in the Ajax callback in PhotoCrop.js
	                // to determine what message to pop up. Do not change this without changing
	                // PhotoCrop.js accordingly
	                if ( errorString != null ) {
	                	holder.get().delete();
	                	response.getWriter().println("<html><body><textarea>" +errorString +"</textarea></body>");
	                	return null;
	                }
	                
	                else if (holder.get().getFileSize() > 0) {
	                	try {
						    photo.setImageStream(holder.get().openDeleteOnClose());
						    service.updatePhoto(currentUser,photo);
	                	} finally {
	                		holder.get().delete();
	                	}
	                } 
	                
	                holder.get().delete();
				}
				// END ======== only operate on non-empty photo
				
				//special case due to odd AJAX behavior 
		
				//Also note that 'Photo OK' is used to decide that photo upload is successful
				// in the Ajax callback and direct the user screen to 'My Profiles'
				response.getWriter().println("<html><body><textarea>Photo OK</textarea></body>");
				return null;
			}
		}

		return new ActionForward("myProfileView.do", true); 
	}

	/**
	 * Gets the eicar if one is requested or buffers a temporary file
	 * @param photoFile
	 * @return
	 * @throws IOException
	 */
	private TFHolder getTempFile(FormFile photoFile) throws IOException {
		InputStream newPhoto = null;
		
		String fileName = photoFile.getFileName();

		if (LOG.isDebugEnabled()) {
		    LOG.debug(" UploadPhotoAction.getTempFile, fileName = " +fileName );
		}

		/*
		// For testing - QE has entered "eicar.jpg" and enabled the do.eicar property
		// We fill in the contents of the eicar.com test virus string
		if (fileName != null && fileName.lastIndexOf("eicar.jpg") != -1) {
		    if (AntiVirusFilter.isEicarEnabled()) {
		        newPhoto = AntiVirusFilter.getEicar();
		    }
		}
		// mormal upload
		else {
			newPhoto = photoFile.getInputStream();
		}
		*/

		// Since 3.0, the checking and getting the 'eicar.jsp' is not necessary anymore
		newPhoto = photoFile.getInputStream();

		if (newPhoto == null) {
			return null;
		}
		
		return new TFHolder(newPhoto);		
	}
	
	/**
	 * Checks the file length and content types
	 * @param photoFile
	 * @param tempFile
	 * @return
	 * @throws IOException 
	 */
	private String getErrorMessages(FormFile photoFile, TFHolder tempFile) throws IOException {
		if (tempFile.get().getFileSize() > FileSubmissionHelper.getPhotoMaxBytes()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("tempFile.get().getFileSize(): " + tempFile.get().getFileSize());
			}
			return "errors.photo.maxfilesize";
		}
		else if (!Arrays.asList(FileSubmissionHelper.getPhotoMimeTypes()).contains(photoFile.getContentType())) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("photoFile.getContentType(): " + photoFile.getContentType());
			}
			// verify the file type as claimed by the caller
			return "errors.photo.filetype";
		}

		try {
			tempFile.set(AntiVirusFilter.scanFile(tempFile.get().open()));
		}
		catch (AVScannerException e) {
			tempFile.get().delete();
			return "error.fileContainsVirus";
		}
		catch (Exception e) {
			return "errorDefaultMsg2";
		}

		// check the actual image file type
		String readerFormatName = ImageUtils.getImageType(tempFile.get().openDeleteOnClose());

		if (LOG.isDebugEnabled()) {
			LOG.debug("readerFormatName: " + readerFormatName);
		}
		
		if (null == readerFormatName) return "errors.photo.filetype";
		
		readerFormatName = readerFormatName.toLowerCase();
		
		boolean found = false;
		for(String s : FileSubmissionHelper.getPhotoMimeTypes()) {
			if (s.toLowerCase().endsWith(readerFormatName)){
				found = true;
				break;
			}
		}
		
		if(!found)
			return "errors.photo.filetype";

		return null;
	}

	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
}
