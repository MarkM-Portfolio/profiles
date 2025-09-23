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

import java.net.URL;
import junit.framework.Assert;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

/**
 * @author eedavis
 * 
 */
public class FollowingEntry extends AtomEntry {
	
	/**
	 * 
	 * see <code>com.ibm.lconn.profiles.api.actions.AdminFollowingAction</code>
	 *
	 */
	public static enum Action {
		FOLLOW, UNFOLLOW, UNFOLLOWALL, REMOVEALLFOLLOWERS;

		public static Action fromString(String s) {
			if (null != s && !"".equals(s.trim())) {
				try {
					return valueOf(s.trim().toUpperCase());
				}
				catch (IllegalArgumentException iae) {
				}
			}
			return null;
		}
	};

	String userId;

	public FollowingEntry(Entry e) throws Exception {
		super(e);
		
		//<category term="d1b1d105-447d-4faa-a1a2-214e4b222d03" scheme="http://www.ibm.com/xmlns/prod/sn/resource-id"></category>
		for (Category c:e.getCategories()){
			if(ApiConstants.SocialNetworking.SCHEME_RESOURCE_ID.equals(c.getScheme().toString())){
				userId = c.getTerm();
				break;
			}
		}
	}

	public String getUserId() throws Exception {
		return userId;
	}

	public FollowingEntry validate() throws Exception {
		// not a spec-compliant entry
		// super.validate();
		Assert.assertNotNull(getLinkHref(ApiConstants.Atom.REL_RELATED));
		assertNotNullOrZeroLength(getUserId());
		return this;
	}
}
