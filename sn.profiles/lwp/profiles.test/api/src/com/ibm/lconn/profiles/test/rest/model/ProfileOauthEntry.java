/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
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

public class ProfileOauthEntry {
	ProfileEntry pe;

	public ProfileOauthEntry(ProfileEntry e)  {
		pe = e;
	}

	public ProfileOauthEntry validate() throws Exception {
		String link;
		pe.validate();

		// we need to check all the links, and see that the html one (related)
		// does not contain the 'oauth' string, and the atom and photo do
		
		link = pe.getLinkHref(ApiConstants.Atom.REL_SELF);
		Assert.assertTrue(link.indexOf(ApiConstants.SocialNetworking.OAUTH) != -1);
	
		link = pe.getLinkHref(ApiConstants.Atom.REL_RELATED);
		Assert.assertTrue(link.indexOf(ApiConstants.SocialNetworking.OAUTH) == -1);

		link = pe.getLinkHref(ApiConstants.SocialNetworking.REL_IMAGE);
		Assert.assertTrue(link.indexOf(ApiConstants.SocialNetworking.OAUTH) != -1);

		link = pe.getLinkHref(ApiConstants.SocialNetworking.REL_PRONOUNCE);
		Assert.assertTrue(link.indexOf(ApiConstants.SocialNetworking.OAUTH) == -1);
				
		return this;
	}

}
