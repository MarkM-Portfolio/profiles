/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.abdera.writer.StreamWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.core.web.atom.util.LCAtomConstants;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.Updatability;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class ProfilesServiceAction extends APIAction implements AtomConstants 
{
	private static final QName QN_EDITABLE_FIELDS = new QName(NS_SNX,"editableFields");
	private static final QName QN_EDITABLE_FIELD = new QName(NS_SNX,"editableField");
	
	private static final Properties FIELD_HCARD_MAP = new Properties();
	
	private static final Log LOG = LogFactory.getLog(ProfilesServiceAction.class);
	
	private static final class Bean 
	{
		Employee userRecord;
		ProfileLookupKey plk;
		boolean isAuthRequest = false;
		public Bean() {}
	}
	
	static
	{
		try
		{
			Properties vcf_assoc = new Properties();
			InputStream is = AtomParser.class.getResourceAsStream("profile_vcard_assoc.properties");
			vcf_assoc.load(is);
			is.close();
			
			Iterator<?> keys = vcf_assoc.keySet().iterator();
			while (keys.hasNext())
			{
				String vcfField = (String) keys.next();
				String profileField = vcf_assoc.getProperty(vcfField);
				
				FIELD_HCARD_MAP.setProperty(profileField, vcfField);
			}
		}
		catch (IOException e) 
		{
			LOG.error(e.getLocalizedMessage(), e);
		}
	}
		
	public ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request,
						  		 	  HttpServletResponse response) throws Exception
	{
		Bean bean = getBean(request);
		
		// oauth requests are forwarded to the non-oauth equivalent URL, so
		// by the time we get it, the oauth is gone from the strong. We can
		// check the URL from which it was forwarded to see if this is an 
		// oauth request
		String orig_req = (String)request.getAttribute("javax.servlet.forward.request_uri");
		boolean isOauth = false;
		if (orig_req != null) {
			isOauth = (orig_req.indexOf("/oauth") != -1);		
		}

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
		sw.writeTitle(bean.userRecord.getDisplayName()); // ResourceManager.getString(request.getLocale(), "lc.author")
						
//		service.declareNS(NS_NAME_ATOM, NS_ATOM);
//		service.declareNS(NS_NAME_SN, NS_SN);		
		
		sw.startCollection(getProfileUrl(request,true,isOauth));	// START - Collection

		sw.writeTitle(bean.userRecord.getDisplayName());
		sw.writeAccepts();
				
		// Stuff for ST support
		sw.startElement(AtomGenerator2.QN_USERID).writeElementText(bean.userRecord.getUserid()).endElement();
		
		sw.startElement(QN_EDITABLE_FIELDS);
		{
		  ProfileType profileType = ProfileTypeHelper.getProfileType(bean.userRecord.getProfileType());
		  for (Property property : profileType.getProperties())
		  {
		    if (Updatability.READWRITE.equals(property.getUpdatability()))
		    {
		      sw.startElement(QN_EDITABLE_FIELD);
		      sw.writeAttribute("name", property.getRef());
		      sw.endElement();
		    }
		  }		  
		}
		sw.endElement(); 				// END - QN_EDITABLE_FIELDS
		sw.endCollection();				// END - Collection
		
		//
		// Write various links
		//
		AtomGenerator2.writeProfileLinks(sw, request, bean.userRecord, request.isSecure(), isOauth);
		AtomGenerator2.writeServiceLinks(sw, bean.userRecord, request.isSecure());
		
		sw.endWorkspace();		
		sw.endService();
		
		sw.endDocument();
		
		return null;
		
	}
	
	private String getProfileUrl(HttpServletRequest request, boolean vcard, boolean isOauth) throws Exception 
	{
		Bean bean = getBean(request);
		StringBuffer url = new StringBuffer(ServiceReferenceUtil.getServiceLink("profiles", request.isSecure()));
		if (isOauth) {
			url.append("/oauth/atom/profile.do?");
		}
		else {
			url.append("/atom/profile.do?");			
		}
		url.append(URLEncoder.encode(bean.plk.getType().toString().toLowerCase(),"UTF-8"));
		url.append("=");
		url.append(URLEncoder.encode(bean.plk.getValue(),"UTF-8"));
		
		if (vcard)
		{
			url.append("&output=vcard");
		}

		return url.toString();
	}

	protected long getLastModified(HttpServletRequest request) throws Exception 
	{
		return getBean(request).userRecord.getLastUpdate().getTime();
	}
	
	private Bean getBean(HttpServletRequest request) throws Exception
	{
		Bean bean = getActionBean(request, Bean.class);
		
		if (bean == null)
		{
			bean = new Bean();
			bean.plk = getProfileLookupKey(request);
			if (bean.plk == null && AppContextAccess.isAuthenticated())
			{
				bean.isAuthRequest = true;
				bean.userRecord = AppContextAccess.getCurrentUserProfile();
				assertNotNull(bean.userRecord);
				bean.plk = ProfileLookupKey.forUserid(bean.userRecord.getUserid());
			}
			else
			{
				AssertionUtils.assertNotNull(bean.plk, AssertionType.UNAUTHORIZED_ACTION);
				bean.userRecord = pps.getProfile(bean.plk, ProfileRetrievalOptions.MINIMUM);
				AssertionUtils.assertNotNull(bean.userRecord);
			}			
			storeActionBean(request, bean);
		}
		
		return bean;
	}
}
