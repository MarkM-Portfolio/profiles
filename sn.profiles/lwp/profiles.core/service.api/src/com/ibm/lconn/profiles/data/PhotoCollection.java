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
public final class PhotoCollection extends AbstractDataCollection<PhotoCollection, Photo, PhotoRetrievalOptions> {

	private static final long serialVersionUID = -6833025505296727868L;

	public PhotoCollection(List<Photo> results, PhotoRetrievalOptions nextSet) {
		super(results, nextSet);
	}

}
