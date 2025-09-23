/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.actions;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import com.ibm.lconn.core.file.LCTempFile;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 *
 *
 */
public class TempPhotoHelper {
	
	private static final String SESSION_KEY_BASE = TempPhotoHelper.class.getName();
	
	public static class TempFile implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1843358259847621341L;
		
		private LCTempFile fileHandle;
		private String fileName;
		private long lastMod = System.currentTimeMillis();
		
		public TempFile(String fileName, LCTempFile fileHandle) throws IOException {
			this.fileName = fileName;
			this.fileHandle = fileHandle;
		}
		
		/**
		 * @return the fileName
		 */
		public final String getFileName() {
			return fileName;
		}
		/**
		 * @return the lastMod
		 */
		public final long getLastMod() {
			return lastMod;
		}
		/**
		 * @return tempFileHandle
		 */
		public final LCTempFile getFileHandle() {
			return fileHandle;
		}
	}
	
	/**
	 * Gets the TempFile for a user.
	 * 
	 * @param request
	 * @return
	 */
	public static TempFile getTempFile(HttpServletRequest request) {
		if (!isAuthenticated())
			return null;
		
		TempFile tempFile = (TempFile) request.getSession().getAttribute(getUserKey(request));
		if (tempFile != null) {
			if (!tempFile.getFileHandle().canOpen())
				clearTempFile(request);
			return tempFile;
		}
		
		return null;
	}
	
	/**
	 * Utility method to set the temporary file
	 * 
	 * @param request
	 * @param tempFile
	 */
	public static void setTempFile(HttpServletRequest request, TempFile tempFile) {
		if (!isAuthenticated())
			return;		
		clearTempFile(request); // clear old file to be safe 
		request.getSession().setAttribute(getUserKey(request), tempFile);
	}
	
	/**
	 * Utility method to remove the temp file from the session
	 * 
	 * @param request
	 */
	public static void clearTempFile(HttpServletRequest request) {
		if (!isAuthenticated())
			return;
		
		TempFile tempFile = (TempFile) request.getSession().getAttribute(getUserKey(request));
		if (tempFile != null) {
			tempFile.getFileHandle().delete();
			request.getSession().removeAttribute(getUserKey(request));
		}
	}

	/**
	 * 
	 * @return
	 */
	private final static boolean isAuthenticated() {
		return AppContextAccess.isAuthenticated();
	}

	/**
	 * Gets the user session key
	 * 
	 * @param request
	 * @return
	 */
	private final static String getUserKey(HttpServletRequest request) {
		return SESSION_KEY_BASE + 
			AppContextAccess.getCurrentUserProfile().getKey();
	}

}
