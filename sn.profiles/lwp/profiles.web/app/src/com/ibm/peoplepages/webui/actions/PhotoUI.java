/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.actions;

import com.ibm.lconn.profiles.data.Photo;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.peoplepages.data.Employee;

/**
 * this class is designed to help ProfileImageAction for photo serving. it has package level visibility so the action class
 * can access it. it is not intended to be public. see comments in PhotoService getPhoto to see the return values it wraps.
 * we are trying to make a distinction between a user existing with no photo, and a user that does not exist. The object
 * helps distinguish between the following cases.
 * 	 (1) target is in caller's org and photo exists. return photo has taret's ids and image data. e.g.
 *   (2) target is in caller's org but has no photo. the returned photo has target's is but no photo content
 *   (3) cannot find the target. the user may not exist or may be in another org. null object is returned.
 * The intent is to allow the caller to distinguish between these cases.
 */
class PhotoUI {
	
	private static final long serialVersionUID = -7787268309718087653L;
	
	private boolean isAuthorizedAccess;  // see comment in 
	private Photo photo;
	private Employee employee;
	private boolean hasDbPhoto = false;
	
	PhotoUI(Photo p, Employee emp, boolean isAuthorizedAccess){
		if (p != null){
			photo = p;
			hasDbPhoto = (p.getImage() != null);
		}
		else{
			photo = new Photo();
			hasDbPhoto = false;
		}
		this.employee = emp;
		this.isAuthorizedAccess = isAuthorizedAccess;
	}
	
	Photo getPhoto(){
		return photo;
	}
	
	boolean isNoPhoto(){
		//TEST return (photo.getImage() == null);
		//TEST user may be getting image or thumbnail
		return (photo.getImage() == null && photo.getThumbnail() == null);
	}
	
	boolean isAuthorizedAccess(){
		return isAuthorizedAccess;
	}
	
	boolean hasDbPhoto(){
		return hasDbPhoto;
	}
	
	boolean userExists(){
		return employee != null;
	}
	
	boolean isExternalUser(){
		boolean rtn = false;
		if (employee != null){
			rtn = UserMode.EXTERNAL.equals(employee.getMode());
		}
		return rtn;
	}
}
