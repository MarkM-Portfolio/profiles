/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.json.actions;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringEscapeUtils;
import com.ibm.lconn.core.web.atom.util.LCJsonWriter;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.templates.TemplateConfig.TemplateEnum;
import com.ibm.lconn.profiles.config.templates.TemplateDataModel;
import com.ibm.lconn.profiles.config.ui.UIBusinessCardConfig;
import com.ibm.lconn.profiles.config.ui.UIConfig;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.util.UrlSubstituter;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.ProfileOption;
import com.ibm.peoplepages.functions.AclFunctions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.webui.xml.actions.GetUserInfoAction;


/**
 * Converts an Employee into a business card json string
 * 
 *
 */
public class BusinessCardHelper {

	/**
	 * Utility method to get the JSON representing the service links for a user. Method will work with null context/request.
	 * 
	 * @param profile
	 * @param servletContext
	 * @param request
	 * @return
	 */
	public static String toLinksJson(Map<String, String> profileSubMap, ServletContext servletContext, HttpServletRequest request, Employee profile) {
		boolean isSecure = (request == null) ? false : request.isSecure();

		try {
			StringWriter sw = new StringWriter();
			LCJsonWriter writer = new LCJsonWriter(sw, false);

			writer.startList();
			if (profileSubMap != null) {
				// match order to header ( profiles, communties, other apps)
				// add profiles
				Map<String, ServiceReferenceUtil> services = AclFunctions.getServiceRefs(profile);
				
				for (ServiceReferenceUtil ref : services.values()) {
					if(ref.getServiceName().equals("profiles")) {
						String href = ref.getServiceLink(isSecure) + UrlSubstituter.resolve(ref.getUrlPattern(), profileSubMap, isSecure);
						writer.startObject()
							.writeFieldName("name").writeVal(ref.getServiceName())
							.writeFieldName("js_eval").writeVal(ref.getJsEval())
							.writeFieldName("href").writeVal(href);
						writer.endObject();
						break;
					}
				}
				
				// add communities
				for (ServiceReferenceUtil ref : services.values()) {
					if(ref.getServiceName().equals("communities") ) {
						String href = ref.getServiceLink(isSecure) + UrlSubstituter.resolve(ref.getUrlPattern(), profileSubMap, isSecure);
						writer.startObject()
							.writeFieldName("name").writeVal(ref.getServiceName())
							.writeFieldName("js_eval").writeVal(ref.getJsEval())
							.writeFieldName("href").writeVal(href);
						writer.endObject();
						break;
					}
				}
				
				// add other apps, skip profiles and communities
				for (ServiceReferenceUtil ref : services.values()) {
					if(ref.getServiceName().equals("profiles") || ref.getServiceName().equals("communities")) continue;

					String href = ref.getServiceLink(isSecure) + UrlSubstituter.resolve(ref.getUrlPattern(), profileSubMap, isSecure);
					writer.startObject()
						.writeFieldName("name").writeVal(ref.getServiceName())
						.writeFieldName("js_eval").writeVal(ref.getJsEval())
						.writeFieldName("href").writeVal(href);
					writer.endObject();
				}
			}
			writer.endList();

			return sw.toString();

		}
		catch (IOException e) {
			throw new ProfilesRuntimeException(e); // un reachable block
		}

	}

	/**
	 * Creates the content for the main div element
	 * 
	 * @param profile
	 * @param context
	 * @param request
	 * @return
	 */
	public static String toMainSection(Employee profile, ServletContext context, HttpServletRequest request) {

		final Writer writer = new EscapeJavaScriptWriter();
		UIBusinessCardConfig config;

		// append(sb, "<div class='lotusPersonInfo'>");
		if (profile != null && (config = UIConfig.instance().getBusinessCardConfig(profile.getProfileType())) != null) {

			Connection conn = null;
			// determine if we are networked (only if required) as input data to the template
			Set<ProfileOption> profileOptions = ProfilesConfig.instance().getTemplateConfig()
					.getProfileOptionForTemplate(TemplateEnum.BUSINESS_CARD_INFO);
			if (profileOptions.contains(ProfileOption.CONNECTION)) {
				String loginUserKey = GetUserInfoAction.getKeyFromLoggedInUser(request);
				if (loginUserKey != null) {
					if (!loginUserKey.equals(profile.getKey())) {
						ConnectionService connService = AppServiceContextAccess.getContextObject(ConnectionService.class);
						conn = connService.getConnection(loginUserKey, profile.getKey(), PeoplePagesServiceConstants.COLLEAGUE, false,
								false);
					}
				}
			}

			// call the template instead
			TemplateEnum templateEnum = TemplateEnum.BUSINESS_CARD_INFO;
			TemplateDataModel templateDataModel = new TemplateDataModel(request);
			templateDataModel.updateEmployee(profile, conn);
			try {
				ProfilesConfig.instance().getTemplateConfig().processTemplate(templateEnum, templateDataModel, writer);
			}
			catch (Exception e) {
				throw new RuntimeException("Unreachable");
			}
		}
		// append(sb, "</div>");

		return writer.toString();
	}

	private static final class EscapeJavaScriptWriter extends Writer {
		private final StringBuilder sb = new StringBuilder();

		public EscapeJavaScriptWriter() {
		}

		@Override
		public void close() throws IOException {
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			write(new String(cbuf, off, len));
		}

		@Override
		public void write(String s, int off, int len) throws IOException {
			write(s.substring(off, off + len));
		}

		@Override
		public void write(String s) throws IOException {
			sb.append(StringEscapeUtils.escapeJavaScript(s));
		}

		@Override
		public String toString() {
			return sb.toString();
		}
	}

}
