/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.abdera.writer.StreamWriter;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeConstants;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class ProfileTypeAction extends APIAction implements AtomConstants {

	private static final class Bean {
		ProfileType type;
		String typeId;
		boolean isAuthRequest = false;

		public Bean() {
		}
	}

	public ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// On the cloud, there is only one default type. So this API doesn't
		// make a lot of sense and we won't support it
		// until we can support per org config and multiple types per org.
		if (LCConfig.instance().isMTEnvironment()) {
			response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
			return null;
		}

		Bean bean = getBean(request);

		if (bean.isAuthRequest) {
			response.setDateHeader("Expires", 0);
			response.setHeader("Cache-Control", "no-store,no-cache,must-revalidate");
		}

		response.setContentType(PROFILE_TYPE_CONTENT_TYPE);
		response.setCharacterEncoding(AtomConstants.XML_ENCODING);

		StreamWriter sw = writerFactory.newStreamWriter();
		sw.setWriter(response.getWriter());

		sw.startDocument(AtomConstants.XML_ENCODING, AtomConstants.XML_VERSION);
		sw.startElement(ProfileTypeConstants.TYPE);

		sw.startElement(ProfileTypeConstants.PARENT_ID);
		sw.writeElementText(bean.type.getParentId());
		sw.endElement(); // ProfileTypeConstants.PARENT_ID

		sw.startElement(ProfileTypeConstants.ID);
		sw.writeElementText(bean.type.getId());
		sw.endElement(); // ProfileTypeConstants.ID

		for (Property property : bean.type.getProperties()) {

			sw.startElement(ProfileTypeConstants.PROPERTY);
			sw.startElement(ProfileTypeConstants.REF);
			sw.writeElementText(property.getRef());
			sw.endElement(); // ProfileTypeConstants.REF
			sw.startElement(ProfileTypeConstants.UPDATABILITY);
			sw.writeElementText(property.getUpdatability().getValue());
			sw.endElement(); // ProfileTypeConstants.UPDATABILITY
			sw.startElement(ProfileTypeConstants.HIDDEN);
			sw.writeElementText(Boolean.toString(property.isHidden()));
			sw.endElement(); // ProfileTypeConstants.HIDDEN
			sw.endElement(); // ProfileTypeConstants.PROPERTY

		}

		sw.endElement(); // ProfileTypeConstants.TYPE
		sw.endDocument();

		return null;

	}

	protected long getLastModified(HttpServletRequest request) throws Exception {
		return System.currentTimeMillis();
	}

	private Bean getBean(HttpServletRequest request) throws Exception {
		Bean bean = getActionBean(request, Bean.class);

		if (bean == null) {
			bean = new Bean();

			bean.typeId = getRequestParamStr(request, PeoplePagesServiceConstants.TYPE, null);

			bean.isAuthRequest = AppContextAccess.isAuthenticated();

			if (bean.typeId == null) {
				if (bean.isAuthRequest) {
					Employee e = AppContextAccess.getCurrentUserProfile();
					if (null != e) {
						bean.typeId = e.getProfileType();
					}
				}
				// else
				// ... not logged in case with no param fails on assertNotNull() below ...
			}

			AssertionUtils.assertNotNull(bean.typeId, AssertionType.RESOURCE_NOT_FOUND);
			bean.typeId = URLDecoder.decode(bean.typeId, AtomConstants.XML_ENCODING);

			bean.type = ProfileTypeHelper.getProfileType(bean.typeId, false);
			
			AssertionUtils.assertNotNull(bean.type, AssertionType.RESOURCE_NOT_FOUND);

			storeActionBean(request, bean);
		}

		return bean;
	}
}
