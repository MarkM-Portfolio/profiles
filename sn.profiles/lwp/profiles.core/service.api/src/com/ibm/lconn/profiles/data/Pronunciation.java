/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.data;

import java.util.Date;

/**
 * @author colleen
 */
public class Pronunciation extends AbstractDataObject<Pronunciation>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3294334163358354763L;
	private String key;
	private Date updated;
	private byte[] audioFile;
	private String fileType = "audio/wav";
	private String fileName = null;

	/**
	 * @return Returns the key.
	 */
	public String getKey()
	{
		return key;
	}
	/**
	 * @param key The key to set.
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return Returns the updated.
	 */
	public Date getUpdated() {
		return updated;
	}
	/**
	 * @param updated The updated to set.
	 */
	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}
	/**
	 * @return Returns the image.
	 */
	public byte[] getAudioFile()
	{
		return audioFile;
	}
	/**
	 * @param audioFile The image to set.
	 */
	public void setAudioFile(byte[] audioFile)
	{
		this.audioFile = audioFile;
	}

	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileName() {
		return fileName;
	}
	public void setFileName(String name) {
		this.fileName = name;
	}

}
