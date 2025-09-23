/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;


/**
 * 
 * @author ahernm
 */
public class PhotoRetrievalOptions extends AbstractRetrievalOptions<PhotoRetrievalOptions>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2784808660887084908L;

	/**
	 * Default page size for photo collections
	 */
	public static final int DEFAULT_PHOTO_PAGE_SIZE = 100;
	
	private String nextPhotoKey;
	
	/**
	 * Default ctor
	 */
	public PhotoRetrievalOptions() {
		super(DEFAULT_PHOTO_PAGE_SIZE);
	}

	public PhotoRetrievalOptions(PhotoRetrievalOptions options){
		this();
		if (options != null){
			this.nextPhotoKey = options.nextPhotoKey;
		}
	}

	/**
	 * @return the lastPhotoKey
	 */
	public final String getNextPhotoKey() {
		return nextPhotoKey;
	}

	/**
	 * @param lastPhotoKey the nextPhotoKey to set
	 */
	public final PhotoRetrievalOptions setNextPhotoKey(String nextPhotoKey) {
		this.nextPhotoKey = nextPhotoKey;
		return this;
	}
}
