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

package com.ibm.lconn.profiles.api.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.core.appext.msgvector.api.MessageVectorService;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessage;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessageResultsCollection;
import com.ibm.lconn.core.appext.msgvector.data.NamedEntryMessageRetrievalOptions;
import com.ibm.lconn.core.compint.news.microblog.impl.NewsMessageVectorServiceRemote;

import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.LocaleUtil;

import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileTag;
import com.ibm.peoplepages.data.ProfileTagCloud;
import com.ibm.peoplepages.data.ProfileTagRetrievalOptions.Verbosity;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.webui.actions.WallConstants;

/**
 * @author ahernm@us.ibm.com
 *
 */
public abstract class ProfileAPIAction extends APIAction 
{
	protected static class BaseBean
	{
		String searchType = null;
		int pageNumber = 1;
		int pageSize = DataAccessConfig.instance().getDefaultPageSize();
		boolean isLite = true;
		boolean allowOverrideIsLite = true;		
		String outputType = PeoplePagesServiceConstants.HCARD;
		boolean isEntryOnly = false;
		long lastMod = System.currentTimeMillis();		
		SearchResultsPage<?> resultsPage;
		boolean inclStatus = false;
		String lastKey = null;
		boolean inclLabels = false;
		String lang = null;
		Locale locale = null;
	}

	private static final Class<ProfileAPIAction> CLAZZ = ProfileAPIAction.class;
	private static final String CLASS_NAME = CLAZZ.getSimpleName();
	private static final Log    LOG        = LogFactory.getLog(CLAZZ);

	/*
	 * Used to indicate that this is a 'connectionXXX.do' action
	 */
	protected boolean interpretOutputTypeString = false;
	
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception 
	{
		return getAndStoreActionBean(request, BaseBean.class).lastMod;
	}
	
	protected final ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		doExecuteHEAD(mapping, form, request, response);
		
		BaseBean bean = this.getAndStoreActionBean(request, BaseBean.class);
		String orig_req = (String)request.getAttribute("javax.servlet.forward.request_uri");

		response.setCharacterEncoding(AtomConstants.XML_ENCODING);
		response.setContentType(AtomConstants.ATOM_CONTENT_TYPE);
		
		AtomGenerator2 atomGenerator = new AtomGenerator2(request, response.getWriter(), bean.isLite, bean.outputType);
		atomGenerator.setInclStatus(bean.inclStatus);
		atomGenerator.setLastKey(bean.lastKey);
		atomGenerator.setInclLabels(bean.inclLabels);
		atomGenerator.setLanguage(bean.lang);
		atomGenerator.setLocale(bean.locale);

		// oauth requests are forwarded to the non-oauth equivalent URL, so
		// by the time we get it, the oauth is gone from the strong. We can
		// check the URL from which it was forwarded to see if this is an 
		// oauth request
		if (orig_req != null) {
			atomGenerator.setOauth((orig_req.indexOf("oauth") != -1) ? true : false);			
		}
		atomGenerator.transform(bean.resultsPage, bean.searchType, bean.isEntryOnly);
		
		return null;
	}
	
	protected final BaseBean instantiateActionBean(HttpServletRequest request) throws Exception
	{
		BaseBean bean = instantiateActionBean_delegate(request);

		bean.inclStatus = Boolean.parseBoolean(request.getParameter("inclUserStatus"));

		//
		// Setup output format
		//

		if (!interpretOutputTypeString ||
			("profile".equals(request.getParameter("outputType"))))
		{
			String output = request.getParameter(PeoplePagesServiceConstants.OUTPUT);

			if (PeoplePagesServiceConstants.VCARD.equals(output) || PeoplePagesServiceConstants.HCARD.equals(output)) {
				bean.outputType = output;
			}
			else if (interpretOutputTypeString) {
				bean.outputType = PeoplePagesServiceConstants.HCARD;
			}
		}		

		String format = request.getParameter(PeoplePagesServiceConstants.FORMAT);
		if (bean.allowOverrideIsLite && PeoplePagesServiceConstants.FULL.equals(format)) 
		{
			bean.isLite     = false;
			String labels   = request.getParameter(PeoplePagesServiceConstants.LABELS);
			bean.inclLabels = Boolean.parseBoolean(labels);
			Locale locale   = null;
			String reqLanguage = request.getParameter(PeoplePagesServiceConstants.LANG);
			// did caller specify the language for custom attributes on the request
			if (StringUtils.isNotEmpty(reqLanguage)) {
				locale = LocaleUtil.getLocale(reqLanguage);
			}
			// either way, we need to be coming out of here with a valid supported locale
			String msg = CLASS_NAME + "." + "instantiateActionBean(" + reqLanguage + ") using : " + locale;
			if (LOG.isDebugEnabled())
				LOG.debug(msg); 

			String language = null;
			String country  = null;
			if (null != locale) {
				language = locale.getLanguage();
				country  = locale.getCountry();
				msg = "    Locale requested : '" + reqLanguage + "', got '" + language + (StringUtils.isEmpty(country) ? "" : (" : " + country)) + "'";
				if (LOG.isDebugEnabled())
					LOG.debug(msg); 
				bean.lang   = language;
				bean.locale = locale;
			}
		}

		//
		// Hook for delegate methods to get format / output info
		//
		instantiateActionBean_postInit(bean, request);

		//
		// ensure that results page is specified
		//
		AssertionUtils.assertNotNull(bean.resultsPage);
		AssertionUtils.assertNotEmpty(bean.searchType);

		//
		// 
		//
		if (bean.resultsPage.getResults() != null 
				&& bean.resultsPage.getResults().size() > 0
				&& bean.resultsPage.getResults().get(0) instanceof Employee)
		{
			for (Object obj : bean.resultsPage.getResults())
			{
				Employee profile = (Employee) obj;

				profile.setImageUrl(FeedUtils.calculatePhotoUrl2(profile.getKey(), FeedUtils.getProfilesURL(request), profile.getLastUpdate()));
				profile.setPronunciationUrl(FeedUtils.calculatePronunciationUrl2(profile.getKey(), FeedUtils.getProfilesURL(request), profile.getLastUpdate()));
				if (!bean.isLite)
				{
					ProfileLookupKey plk = new ProfileLookupKey(ProfileLookupKey.Type.KEY, profile.getKey() );
					ProfileTagCloud tc = tagSvc.getProfileTagCloud( plk, Verbosity.MINIMUM );
					List<ProfileTag> socialTags = tc.getTags();

					if ( socialTags != null && socialTags.size() > 0 ) {
						List<Tag> tags = new ArrayList<Tag>();
						int size = socialTags.size();

						for ( int i = 0; i < size; i++ ) {
							ProfileTag profileTag = socialTags.get(i);
							Tag aTag = new Tag();
							aTag.setTag(profileTag.getTag());
							aTag.setType(profileTag.getType());
							tags.add(aTag);
						}	
						profile.setProfileTags( tags );
					}
				}
			}	
		}

		if (bean.inclStatus 
				&& bean.resultsPage.getResults() != null 
				&& bean.resultsPage.getResults().size() > 0 
				&& (bean.resultsPage.getResults().get(0) instanceof Employee || bean.resultsPage.getResults().get(0) instanceof Connection)) 
		{
			Map<String,Employee> keyMap = new HashMap<String,Employee>(bean.resultsPage.getResults().size()*2);

			for (Object obj : bean.resultsPage.getResults())
			{
				Employee profile;

				if (obj instanceof Employee) {
					profile = (Employee) obj;
				} else {
					profile = ((Connection) obj).getTargetProfile();
				}

				if (profile != null)
					// RTC 82162 fix Part 1: MessageVectorService requires GUID, not Profiles internal key
					keyMap.put(profile.getGuid(), profile);
			}

			if (keyMap.size() > 0) {
				// RTC 82162 fix Part 2: Call correct/current service. As of LC4.5 the context still stores non-functional
				// com.ibm.lconn.core.appext.msgvector.impl.MessageVectorServiceImpl
				MessageVectorService mvs = new NewsMessageVectorServiceRemote();

				NamedEntryMessageRetrievalOptions options = new NamedEntryMessageRetrievalOptions();
				options.setPageSize(keyMap.size());

				EntryMessageResultsCollection res = mvs.getNamedEntryMessages(WallConstants.VECTOR_TYPE,
						new ArrayList<String>(keyMap.keySet()), WallConstants.STATUS_ENTRY, new Date(System.currentTimeMillis()
								- WallConstants.STATUS_TIME_WINDOW), options);

				// Defect 113642: Make sure that we are getting a non-null object back from the EJB call.
				// It is possible that res is null when the user has never posted any status updates,
				// Or the userId doesn't match with extId in HP DB.
				if ( res != null ) {
					for (EntryMessage em : res.getMessages()) {
						keyMap.get(em.getPublishedBy()).setStatus(em);					
					}
				}
			}
		}
		return bean;
	}

	/**
	 * Hook to create BaseBean
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected abstract BaseBean instantiateActionBean_delegate(HttpServletRequest request)
		throws Exception;
	
	/**
	 * Hook to perform post initialize actions
	 * @param bean 
	 * 
	 * @param request
	 * @throws Exception
	 */
	protected void instantiateActionBean_postInit(BaseBean bean, HttpServletRequest request)
		throws Exception
	{
		// DO NOTHING - Hook to perform post initialize actions
	}
	
}
