/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.ui.actions.person.forms;

import java.util.Map;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

@UrlBinding("/app/person/{id}/forms/card")
public class PersonCard implements ActionBean {

	private static final String SELF = "@me";

	private ActionBeanContext context = null;
	private String id = null;
	private ProfileLookupKey key;
	private Employee employee = null;

	public ActionBeanContext getContext() {
		return context;
	}

	public void setContext(ActionBeanContext context) {
		this.context = context;
	}

	@DefaultHandler
	public Resolution view() {
		PeoplePagesService svc = AppServiceContextAccess
				.getContextObject(PeoplePagesService.class);

		try {
			
			resolveEmployee(svc);
			
		} catch (AssertionException e) {
			if (AssertionType.BAD_REQUEST.equals(e.getType()))
				return new ForwardResolution(
						"/WEB-INF/stripes/pages/app/person/forms/PersonCardNotFound.jsp");
			throw e;
		}
		return new ForwardResolution(
				"/WEB-INF/stripes/pages/app/person/forms/PersonCard.jsp");
	}

	protected void resolveEmployee(PeoplePagesService svc) {
		if (SELF.equals(id)) {
			employee = AppContextAccess.getCurrentUserProfile();
			if (employee == null) {
				AssertionUtils.assertTrue(false, AssertionType.BAD_REQUEST); // FIXME:
																				// need
																				// new
																				// AssertType?
			}
			key = new ProfileLookupKey(ProfileLookupKey.Type.KEY,
					employee.getKey());
		} else {
			key = getProfileLookupKey(getId());
			employee = svc.getProfile(key, ProfileRetrievalOptions.EVERYTHING);
		}

		AssertionUtils.assertNotNull(employee, AssertionType.BAD_REQUEST);
	}

	// FIXME: Should this be a PLK?
	public String getId() {
		return id;
	}

	// FIXME: Should this normalize to a PLK?
	public void setId(String id) {
		this.id = id;
	}

	public Employee getEmployee() {
		return employee;
	}

	public ProfileLookupKey getProfileLookupKey(String param) {
		ProfileLookupKey.Type type = ProfileLookupKey.Type.KEY;
		String value = param;
		for (Map.Entry<String, ProfileLookupKey.Type> entry : BaseAction.DEFAULT_PARAM_TYPE_MAP
				.entrySet()) {
			String prefix = entry.getKey() + "_";
			if (param.startsWith(prefix)) {
				type = entry.getValue();
				value = param.substring(prefix.length());
				break;
			}
		}

		if ((ProfileLookupKey.Type.EMAIL.equals(type))
				&& (!LCConfig.instance().isEmailAnId())) {
			AssertionUtils.assertTrue(false, AssertionType.UNAUTHORIZED_ACTION);
		}

		return new ProfileLookupKey(type, value);
	}
}
