/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.math.RoundingMode;

import java.net.URLDecoder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.writer.StreamWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.SchemaVersionInfo;
//import com.ibm.lconn.profiles.web.util.DiagnosticsHelper;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class VersionInfoAction extends APIAction implements AtomConstants
{
	private static final Log LOG = LogFactory.getLog(VersionInfoAction.class);

	private static final class Bean {
		String  relNum;
		String  bldNum;
		String  appStart;
		String  schemaVer;
		String  postSchemaVer;
		boolean isIncludeSchema = false;
		boolean isAuthRequest   = false;

		public Bean() {
		}
	}

	public ActionForward doExecuteGET
				(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response)
				throws Exception
	{
		Bean bean = getBean(request);

		if (bean.isAuthRequest) {
			response.setDateHeader("Expires", 0);
			response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		}

		response.setContentType(ATOM_CONTENT_TYPE);
		response.setCharacterEncoding(AtomConstants.XML_ENCODING);

		StreamWriter sw = writerFactory.newStreamWriter();
		sw.setWriter(response.getWriter());

		sw.startDocument(AtomConstants.XML_ENCODING, AtomConstants.XML_VERSION);
			sw.startElement(APP_NAME);
				sw.startElement(BUILD_INFO);
					sw.writeAttribute(RELEASE_NO, bean.relNum);
					sw.writeAttribute(BUILD_NO,   bean.bldNum);
					// only include db schema info if the user is an authenticated admin user
					if (bean.isIncludeSchema) {
if (LOG.isDebugEnabled()) { // putting this behind debug switch we until get 'security' approval from Robert
						sw.writeAttribute(APP_STARTED, bean.appStart);
						sw.startElement(SCHEMA_INFO);
							sw.writeAttribute(VERSION,      bean.schemaVer);
							sw.writeAttribute(POST_VERSION, bean.postSchemaVer);
						sw.endElement(); // SCHEMA_INFO
} // debug
						StringBuilder sb = new StringBuilder();
						sb.append(SCHEMA_INFO  + ":: ");
						sb.append(VERSION      + ":" + bean.schemaVer).append(" ");
						sb.append(POST_VERSION + ":" + bean.postSchemaVer).append(" ");
						sb.append(APP_STARTED  + ":" + bean.appStart).append(" ");
						response.addHeader(SCHEMA_INFO, sb.toString());
					}
				sw.endElement(); // BUILD_INFO
			sw.endElement(); // APP_NAME
		sw.endDocument();
		return null;
	}

	private static final String APP_NAME     = "Profiles";
	private static final String BUILD_INFO   = "Build";
	private static final String RELEASE_NO   = "Release";
	private static final String BUILD_NO     = "Number";
	private static final String APP_STARTED  = "Started";
	private static final String SCHEMA_INFO  = "Schema";
	private static final String VERSION      = "Version";
	private static final String POST_VERSION = "PostVersion";

	protected long getLastModified(HttpServletRequest request) throws Exception
	{
		return System.currentTimeMillis();
	}

	private Bean getBean(HttpServletRequest request) throws Exception
	{
		Bean bean = getActionBean(request, Bean.class);

		if (bean == null) {
			bean = new Bean();

			String releaseNumber   = "";
			String buildNumber     = "";
			String reformattedDate = "";
			bean.isAuthRequest = AppContextAccess.isAuthenticated();

			if (bean.relNum == null) {
				if (bean.isAuthRequest) {
					Employee e = AppContextAccess.getCurrentUserProfile();
					if (null != e) {
						ServletContext servletContext = request.getSession().getServletContext();
						releaseNumber = (String) servletContext.getAttribute("versionNumber");
						buildNumber   = (String) servletContext.getAttribute("buildNumber");

						// only include server start time if the user is an authenticated admin user
						if (AppContextAccess.isUserAnAdmin()) {
							String appStartDate = (String) servletContext.getAttribute("appStartupDate");
							SimpleDateFormat inFormat  = new SimpleDateFormat("yyyyMMddHHmmSSS");
							SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
							try {
								reformattedDate = outFormat.format(inFormat.parse(appStartDate));
							}
							catch (ParseException ex) {
								reformattedDate = appStartDate;
							}
						}
						if (LOG.isDebugEnabled()) {
							LOG.debug("Profiles release: " + releaseNumber + " buildNumber: " + buildNumber + " appStartupDate: " + reformattedDate);
						}
					}
				}
				// else
				// ... not logged in case with no param fails on assertNotNull() below ...
			}

			bean.relNum   = URLDecoder.decode(releaseNumber, AtomConstants.XML_ENCODING);
			bean.bldNum   = buildNumber;
			bean.appStart = reformattedDate;

			AssertionUtils.assertNotNull(bean.relNum,   AssertionType.RESOURCE_NOT_FOUND);
			AssertionUtils.assertNotNull(bean.bldNum,   AssertionType.RESOURCE_NOT_FOUND);
			AssertionUtils.assertNotNull(bean.appStart, AssertionType.RESOURCE_NOT_FOUND);

			boolean includeSchema = false;
			String  schemaVersion = null;
			String  postSchemaVer = null;
			// only include db schema info if the user is an authenticated admin user && diags URL param is true
			if (bean.isAuthRequest) {
				if (AppContextAccess.isUserAnAdmin()) {
					try {
						// place holder for diagnostics determination if schema info is requested
						// ...
//if (LOG.isDebugEnabled()) { // putting this behind debug switch we until get 'security' approval from Robert
//							String  diagsParam = request.getParameter("diags");
//							boolean isDiagsRequested = DiagnosticsHelper.isDiagsRequest(diagsParam);
//							if (isDiagsRequested) {
//								// determine if the request wants the schema info
//								String requestQuery = request.getQueryString();
//								DiagnosticsHelper diags = new DiagnosticsHelper(requestQuery);
//								includeSchema = diags.isIncludeSchema();
//							}
//} // debug
						if (includeSchema) {
							SchemaVersionInfo schemaVerInfo = SchemaVersionInfo.instance();
							schemaVersion = roundVersion(schemaVerInfo.getDbSchemaVer());
							postSchemaVer = roundVersion(schemaVerInfo.getPostSchemaVer());
						}
					}
					catch (RuntimeException e) {
						LOG.debug("Profiles schema version query failed: " + e.getMessage());
					}
				}
			}
			bean.isIncludeSchema = includeSchema;
			bean.schemaVer       = schemaVersion;
			bean.postSchemaVer   = postSchemaVer;

			storeActionBean(request, bean);
		}
		return bean;
	}

	private String roundVersion(Float dbSchemaVer)
	{
		DecimalFormat df = new DecimalFormat("#.#");
		df.setRoundingMode(RoundingMode.CEILING);
		return df.format(dbSchemaVer);
	}
}
