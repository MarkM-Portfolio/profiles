/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.model;

import junit.framework.Assert;
import org.apache.abdera.model.Entry;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class ServiceConfigEntry extends AtomEntry
{
	public ServiceConfigEntry(Entry e) throws Exception
	{
		super(e);
	}

	public ServiceConfigEntry validate() throws Exception
	{
		// Validate that there is a HTML link
		Assert.assertNotNull( getLinkHref(ApiConstants.Atom.REL_ALTERNATE) );
		
		// Validate that there is the ssl link
		Assert.assertNotNull( getLinkHref(ApiConstants.Atom.REL_ALT_SSL) );
		
		return this;
	}
}
