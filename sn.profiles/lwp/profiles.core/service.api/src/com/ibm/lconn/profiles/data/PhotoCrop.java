/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.data;

import java.io.InputStream;

public class PhotoCrop extends Photo {

	private static final long serialVersionUID = -52L;

	private String sessionId;
	private Boolean isCrop;
	private Double startx;
	private Double starty;
	private Double endx;
	private Double endy;
	private InputStream imageStream;

	public PhotoCrop() {
		super();
	}

	public PhotoCrop(Photo photo) {
		key = photo.getKey();
		profileType = photo.getProfileType();
		updated = photo.getUpdated();
		fileType = photo.getFileType();
		image = photo.getImage();
		state = photo.getUserState();
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Boolean getIsCrop() {
		return isCrop;
	}

	public void setIsCrop(Boolean isCrop) {
		this.isCrop = isCrop;
	}

	public Double getStartx() {
		return startx;
	}

	public void setStartx(Double startx) {
		this.startx = startx;
	}

	public Double getStarty() {
		return starty;
	}

	public void setStarty(Double starty) {
		this.starty = starty;
	}

	public Double getEndx() {
		return endx;
	}

	public void setEndx(Double endx) {
		this.endx = endx;
	}

	public Double getEndy() {
		return endy;
	}

	public void setEndy(Double endy) {
		this.endy = endy;
	}

	public InputStream getImageStream() {
		return imageStream;
	}

	public void setImageStream(InputStream imageStream) {
		this.imageStream = imageStream;
	}
}
