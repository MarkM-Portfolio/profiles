/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/*
 * Created on 23-Jan-2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.ibm.peoplepages.webui.forms;
import org.apache.struts.upload.FormFile;
import org.apache.struts.validator.ValidatorForm;
/**
 * @author ieu93237
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UploadPhotoForm extends ValidatorForm{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String uid;
	private FormFile photo;
	private boolean removePhoto = false;
	private boolean crop = false;
	private boolean temp = false;
	private double startx = -1.0;
	private double starty= -1.0;
	private double endx= -1.0;
	private double endy= -1.0;
	public FormFile getPhoto() {
		return photo;
	}
	public void setPhoto(FormFile photo) {
		this.photo = photo;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public boolean isRemovePhoto() {
		return removePhoto;
	}
	public void setRemovePhoto(boolean removePhoto) {
		this.removePhoto = removePhoto;
	}
	public boolean isCrop() {
		return crop;
	}
	public void setCrop(boolean crop) {
		this.crop = crop;
	}
	public boolean isTemp() {
		return temp;
	}
	public void setTemp(boolean temp) {
		this.temp = temp;
	}
	public double getStartx() {
		return startx;
	}
	public void setStartx(double startx) {
		this.startx = startx;
	}
	public double getStarty() {
		return starty;
	}
	public void setStarty(double starty) {
		this.starty = starty;
	}
	public double getEndx() {
		return endx;
	}
	public void setEndx(double endx) {
		this.endx = endx;
	}
	public double getEndy() {
		return endy;
	}
	public void setEndy(double endy) {
		this.endy = endy;
	}

	

}
