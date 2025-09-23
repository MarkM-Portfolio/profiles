/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.dsx.actions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess.ContextScope;
import com.ibm.lconn.profiles.api.actions.AtomConstants;
import com.ibm.lconn.profiles.api.actions.AtomGenerator;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.config.types.PropertyEnum;
import com.ibm.lconn.profiles.internal.data.profile.AttributeGroup;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.AbstractContext;
import com.ibm.peoplepages.util.appcntx.AppContextAccess.Context;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 */
public final class DSXSerializer 
{
	private final static Log LOG = LogFactory.getLog(DSXSerializer.class);

	// Attrs to Expose

	// ATOM constants
	private static final String FEED = "feed";
	private static final String ENTRY = "entry";

	// DSX constants
	private static final String IDKEY = "idKey";
	private static final String TYPE = "type";
	private static final String NAME = "name";
	private static final String EMAIL = "email";
	private static final String DN = "dn";
	private static final String LOGIN = "login";
	private static final String SOURCE_URL = "sourceUrl";
	private static final String USER_STATE = "userState";
	private static final String EXT = "ext";
	private static final String PROP = "prop";

	// visitor model additions
	private static final String USER_TENANT_ID = "orgId";
	private static final String USER_MODE = "external";
	private static final String ROLE_ID = "userRole";

	private static final String NS = "http://www.ibm.com/xmlns/prod/sn/dsx";
	private static final String PREFIX = "dsx";

	private final XMLStreamWriter writer;

	public DSXSerializer(HttpServletResponse response) throws IOException, XMLStreamException
	{
		AtomGenerator gen = new AtomGenerator(response, AtomConstants.ATOM_CONTENT_TYPE);
		writer = gen.getWriter();
	}

	public DSXSerializer(PrintWriter out) throws IOException, XMLStreamException
	{
		AtomGenerator gen = new AtomGenerator(out);
		writer = gen.getWriter();
	}
	
	public final void writeDSXFeed(
			List<Employee> profiles, 
			Map<String, List<String>> loginMap,
			Map<String, List<String>> roleMap,
			boolean outputEmail, 
			boolean singleUserLookup,
			boolean isLogin)
		throws IOException, XMLStreamException
	{
		// setup
		writer.setDefaultNamespace(AtomConstants.NS_ATOM);
		writer.setPrefix(PREFIX, NS);

		// start doc
		writer.writeStartDocument(AtomConstants.XML_ENCODING, AtomConstants.XML_VERSION);
		writer.writeStartElement(AtomConstants.NS_ATOM, FEED);
		writer.writeNamespace(PREFIX, NS);

		HashSet<String> loginValues = new HashSet<String>(5);
		List<String> vals;

		for (Employee profile : profiles)
		{
			writer.writeStartElement(AtomConstants.NS_ATOM, ENTRY);
			{
				writeDSXElement(TYPE, "0");
				writeDSXElement(IDKEY, profile.getUserid());
				writeDSXElement(NAME, profile.getDisplayName());
				if (outputEmail)
					writeDSXElement(EMAIL, profile.getEmail());
				writeDSXElement(DN, profile.getDistinguishedName());
				writeDSXElement(SOURCE_URL, profile.getSourceUrl());

				UserState state = profile.getState();
				String stateVal = String.valueOf(state.getCode());
				writeDSXElement(USER_STATE, stateVal);

				// visitor model additions
				UserMode mode = profile.getMode();
				String modeVal1 = null;
				modeVal1 = String.valueOf(mode.getCode()); // 0 (internal)  /  1 (external) [room for expansion]
				writeDSXElement(USER_MODE, modeVal1);
				writeDSXElement(USER_TENANT_ID, profile.getTenantKey());

				for (String profilesLoginAttr : ProfilesConfig.instance().getDataAccessConfig().getDirectoryConfig().getLoginAttributes())
				{
					String value = (String) profile.get(profilesLoginAttr);

					// We would write the 'original' value from the EMPLOYEE table in the DSX Feed for the login attrs. 
					// However, we want to use the lowser-cased values for checking whether we have dups or not.
					String valueLower;
					if (AssertionUtils.nonEmptyString(value) && (!loginValues.contains(valueLower = value.toLowerCase()))
						&& (outputEmail || !PeoplePagesServiceConstants.EMAIL.equals(profilesLoginAttr)))
					{
						writeDSXElement(LOGIN, value);
						loginValues.add(valueLower);
					}
				}

				// 117633: Remove the 'logins' and 'roles' info from dsx/search queries
				if ((null != loginMap) && !(loginMap.isEmpty())) {
					// 88569: Change the order to output the values from the login table so that the values from the
					// profiles-config.xml above will be written first.
					vals = loginMap.get(profile.getKey());
					if (vals != null) {
						for (String login : vals) {
							// Prevent duplicates and blank strings from being written to the feed.
							// Note that values in logins are all in lower cases (see ProfileLogin.java), 
							// but there could be dups in the List.
							if ( StringUtils.isNotBlank(login) && !loginValues.contains( login ) ) {
								writeDSXElement(LOGIN, login);
								loginValues.add( login );
							}
						}
					}
				}
				// reset + reuse object
				loginValues.clear();

				// 117633: Remove the 'logins' and 'roles' info from dsx/search queries
				if ((null != roleMap) && !(roleMap.isEmpty())) {
					// 117523: add the roles - role logic should enforce lower-case and no duplicates
					vals = roleMap.get(profile.getKey());
					if (vals != null) {
						for (String id : vals) {
							writeDSXElement(ROLE_ID, id);
						}
					}
				}

				writeBaseExtVal(writer, PropertyEnum.PROFILE_TYPE, profile.getProfileType());

				if (isLogin) {
					writeAclVal(writer, Acl.STATUS_UPDATE, profile);
				}
			}
			writer.writeEndElement(); // ENTRY
		}
		// end doc
		writer.writeEndElement(); // FEED
		writer.writeEndDocument();
	}

	private final void writeAclVal(
				final XMLStreamWriter writer,
				final Acl acl,
				final Employee profile) 
		throws XMLStreamException
	{
		boolean aclVal =
			doAs(profile, new Callable<Boolean>() {
				public Boolean call() throws Exception {
					return PolicyHelper.checkAcl(acl, profile);
				} 
			});

		writer.writeStartElement(NS, EXT);
		writer.writeAttribute(PROP, toPropId(acl));
		writer.writeCharacters(String.valueOf(aclVal));
		writer.writeEndElement();		
	}

	private String toPropId(Acl acl) {
		return "acl$" + acl.getName();
	}

	private boolean doAs(Employee profile, Callable<Boolean> callable) throws XMLStreamException 
	{
		final Context context = AppContextAccess.getContext();
		try {
			DoAsContext cntx = new DoAsContext(context, profile);
			AppContextAccess.setContext(cntx);
			return callable.call();
		} catch (XMLStreamException e) { 
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			AppContextAccess.setContext(context);
		}
	}

	// caller is to ensure this is a 'base attribute'
	private final void writeBaseExtVal(XMLStreamWriter writer, PropertyEnum e, Object value) throws XMLStreamException {
		if (value != null) {
			writer.writeStartElement(NS, EXT);
			writer.writeAttribute(PROP,AttributeGroup.IPERSON_BASE_ATTR_PREFIX+e.getValue());
			writer.writeCharacters(String.valueOf(value));
			writer.writeEndElement();
		}
	}

	private final void writeDSXElement(String elemName, String value) throws XMLStreamException {
		writer.writeStartElement(NS, elemName);
		writer.writeCharacters(value);
		writer.writeEndElement();
		if (LOG.isDebugEnabled()) 
			if ((elemName.equalsIgnoreCase(NAME))
			||	(elemName.equalsIgnoreCase(USER_TENANT_ID))
			||	(elemName.equalsIgnoreCase(USER_STATE))
//			||	(elemName.equalsIgnoreCase(USER_ROLE))
			||	(elemName.equalsIgnoreCase(USER_MODE))
			) {
				LOG.debug("writeDSXElement(" + elemName + ", " + value +")");
			}
	}

	private static class DoAsContext extends AbstractContext {
		private Context context;
		private Employee currUser;

		public DoAsContext(Context context, Employee currUser) {
			this.context = context;
			this.currUser = currUser;
		}

		public Map<String, String> getCookies() {
			return context.getCookies();
		}

		public Locale getCurrentUserLocale() {
			return context.getCurrentUserLocale();
		}

		public Employee getCurrentUserProfile() {
			return currUser;
		}

		public Map<String, String> getRequestHeaders() {
			return context.getRequestHeaders();
		}

		public boolean isAuthenticated() {
			return true;
		}

		public boolean isUserInRole(String role) {
			if ("allAuthenticated".equals(role) ||
				"person".equals(role) ||
				"reader".equals(role) ||
				"everyone".equals(role)) 
		{
			return true;
		}

			return false;
		}

		@Override
		protected Object getNonTransactionlAttribute(ContextScope scope, String key) {
			return null;
		}

		@Override
		protected void setNonTransactionalAttribute(ContextScope scope, String key, Object value) {
		}

		public String getTenantKey() {
			// return the context we are doing as
			return context.getTenantKey();
		}
	}
}
