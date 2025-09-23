/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2010                                      */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;

import java.util.List;


/**
 * @author ahernm
 *
 */
public final class PronunciationCollection extends AbstractDataCollection<PronunciationCollection, Pronunciation, PronunciationRetrievalOptions> {

	private static final long serialVersionUID = -6064037888542002672L;

	public PronunciationCollection(List<Pronunciation> results, PronunciationRetrievalOptions nextSet) {
		super(results, nextSet);
	}

}
