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

import java.net.URLEncoder;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.abdera.model.Link;
import org.apache.abdera.writer.StreamWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.core.web.atom.util.LCAtomConstants;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class AdminProfilesServiceAction extends APIAction implements AtomConstants 
{
	private static final QName QN_EDITABLE_FIELDS = new QName(NS_SNX,"editableFields");
	private static final QName QN_EDITABLE_FIELD = new QName(NS_SNX,"editableField");

	private static final Properties FIELD_HCARD_MAP = new Properties();

	private final static String ADMIN_SCHEME = "http://www.ibm.com/xmlns/prod/sn/workspace";
	private final static String CONN_USERS_SCHEME = "http://www.ibm.com/xmlns/prod/sn/collection";

	private final static String ADMIN_TYPE = "profiles-admin";
	private final static String CONN_USERS_TYPE = "connections-users";

	private static final Log LOG = LogFactory.getLog(AdminProfilesServiceAction.class);

	private static final class Bean 
	{
		boolean isAuthRequest = false;
		public Bean() {}
	}


	public ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		Bean bean = getBean(request);

		if (bean.isAuthRequest) {
			response.setDateHeader("Expires", 0);
			response.setHeader("Cache-Control", "no-store,no-cache,must-revalidate");
		}

		response.setContentType("application/atomsvc+xml; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		StreamWriter sw = writerFactory.newStreamWriter();
		sw.setWriter(response.getWriter());

		sw.startDocument();
		sw.setPrefix("", AtomGenerator.NS_APP);

		sw.startService();
		sw.writeNamespace("atom", LCAtomConstants.NS_ATOM);
		sw.writeNamespace("snx", LCAtomConstants.NS_SNX);
		sw.setPrefix("atom", LCAtomConstants.NS_ATOM);
		sw.setPrefix("snx", LCAtomConstants.NS_SNX);

		sw.writeGenerator(AtomConstants.GENERATOR_VERSION, NS_SNX,"IBM Connections - Profiles");

		sw.startWorkspace();

		sw.writeTitle(AtomConstants.SERVICE_TITLE);
		sw.writeCategory(ADMIN_TYPE, ADMIN_SCHEME);

		sw.startCollection(getProfileAdminUrl(request));
		sw.writeTitle(AtomConstants.SERVICE_TITLE_ALL);

		sw.writeAcceptsEntry();

		sw.writeCategory(CONN_USERS_TYPE, CONN_USERS_SCHEME);

		sw.startElement(QN_EDITABLE_FIELDS);
		{
			Collection<String> editableFields = AtomGenerator3.getProfileFields();
			for (String ef : editableFields)
			{
				sw.startElement(QN_EDITABLE_FIELD);
				sw.writeAttribute("name", ef);
				sw.endElement();
			}		
		}
		sw.endElement(); 				// END - QN_EDITABLE_FIELDS
		sw.endCollection();				// END - Collection

		//
		// Write admin service document links
		//
		AtomGenerator2.writeAdminProfileServiceLinks(sw, request);

		sw.endWorkspace();		
		sw.endService();

		sw.endDocument();

		return null;

	}

	private String getProfileAdminUrl(HttpServletRequest request) throws Exception 
	{
		Bean bean = getBean(request);
		StringBuffer url = new StringBuffer(ServiceReferenceUtil.getServiceLink("profiles", request.isSecure()));
		url.append("/admin/atom/profiles.do");

		return url.toString();
	}


	private Bean getBean(HttpServletRequest request) throws Exception
	{
		Bean bean = getActionBean(request, Bean.class);

		if (bean == null)
		{
			bean = new Bean();
			if (AppContextAccess.isAuthenticated())
			{
				bean.isAuthRequest = true;
			}			
			storeActionBean(request, bean);
		}

		return bean;
	}

	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
}
