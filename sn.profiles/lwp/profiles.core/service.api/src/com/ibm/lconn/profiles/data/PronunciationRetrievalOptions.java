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
public class PronunciationRetrievalOptions extends AbstractRetrievalOptions<PronunciationRetrievalOptions> {

	private static final long serialVersionUID = -5383596493864187391L;

	/**
	 * Default page size for photo collections
	 */
	public static final int DEFAULT_PRONUNCIATION_PAGE_SIZE = 100;
	
	private String nextPronunciationKey;
	
	/**
	 * Default ctor
	 */
	public PronunciationRetrievalOptions() {
		super(DEFAULT_PRONUNCIATION_PAGE_SIZE);
	}

	/**
	 * Regular ctor
	 */
	public PronunciationRetrievalOptions(PronunciationRetrievalOptions pro) {
		this();
		if (pro != null){
			this.nextPronunciationKey = pro.nextPronunciationKey;
		}
	}

	/**
	 * @return the lastPhotoKey
	 */
	public final String getNextPronunciationKey() {
		return nextPronunciationKey;
	}

	/**
	 * @param nextPronunciationKey the lastPhotoKey to set
	 */
	public final PronunciationRetrievalOptions setNextPronunciationKey(String nextPronunciationKey) {
		this.nextPronunciationKey = nextPronunciationKey;
		return this;
	}

}
