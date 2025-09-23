/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.providers;

import java.util.Date;
import org.apache.abdera.protocol.server.RequestContext.Scope;
import org.apache.abdera.protocol.server.ResponseContext;
import org.springframework.beans.factory.BeanFactory;
import com.ibm.lconn.core.appext.msgvector.api.MessageVectorService;
import com.ibm.lconn.core.appext.msgvector.atom.api.CommentMessageCollectionAdapter;
import com.ibm.lconn.core.appext.msgvector.atom.api.EntryMessageCollectionAdapter;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageVectorProvider;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageVectorProviderConfig;
import com.ibm.lconn.core.appext.msgvector.atom.api.NamedEntryMessageCollectionAdapter;
import com.ibm.lconn.core.appext.msgvector.atom.api.RecommendationCollectionAdapter;
import com.ibm.lconn.core.appext.msgvector.atom.api.RelatedEntryMessageCollectionAdapter;
import com.ibm.lconn.core.appext.msgvector.atom.api.SystemEntryMessageCollectionAdapter;
import com.ibm.lconn.core.appext.msgvector.config.MessageVectorServiceConfig;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessage;
import com.ibm.lconn.core.appext.msgvector.data.MessageVector;
import com.ibm.lconn.core.compint.news.microblog.impl.NewsMessageVectorServiceRemote;
import com.ibm.lconn.core.web.atom.LCCollectionAction;
import com.ibm.lconn.core.web.atom.LCFilter;
import com.ibm.lconn.core.web.atom.LCFilterChain;
import com.ibm.lconn.core.web.atom.LCRequestContext;
import com.ibm.lconn.core.web.atom.bean.AnnotatedLCCollectionAdapter;
import com.ibm.lconn.core.web.atom.util.LCModelHelper;
import com.ibm.lconn.core.web.atom.util.LCResponseUtils;
import com.ibm.lconn.profiles.api.actions.AtomConstants;
import com.ibm.lconn.profiles.api.actions.FeedUtils;
import com.ibm.lconn.profiles.api.actions.ResourceManager;
import com.ibm.lconn.profiles.api.messageboard.adapters.ProfilesBaseEntryMessageCollectionAdapter;
import com.ibm.lconn.profiles.api.messageboard.adapters.ProfilesCommentMessageCollectionAdapter;
import com.ibm.lconn.profiles.api.messageboard.adapters.ProfilesEntryMessageCollectionAdapter;
import com.ibm.lconn.profiles.api.messageboard.adapters.ProfilesNamedEntryMessageCollectionAdapter;
import com.ibm.lconn.profiles.api.messageboard.adapters.ProfilesRelatedEntryMessageCollectionAdapter;
import com.ibm.lconn.profiles.api.messageboard.adapters.ProfilesSystemEntryMessageCollectionAdapter;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.data.MVConnectionOptions;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.webui.actions.WallConstants;

/**
 *
 * 
 */
public class ProfilesMessageVectorProvider extends MessageVectorProvider {
	public static final String PLK_ATTR = ProfilesMessageVectorProvider.class.getName() + ".PLK";
	public static final String ASSOC_RESOURCE_ATTR = ProfilesMessageVectorProvider.class.getName() + ".assocResource.profile";
	
	public ProfilesMessageVectorProvider() {
		//implicit super(); requires no-arg constructor in MessageVectorProvider
		BeanFactory beanFactory = AppServiceContextAccess.getContext();
		this.service = (MessageVectorService) beanFactory.getBean(MessageVectorService.class.getName());
		this.config = (MessageVectorServiceConfig) beanFactory.getBean(MessageVectorServiceConfig.class.getName());
		this.providerConfig = new ProviderConfig();
		
		this.targetResolver = initTargetResolver(
			new LCCollectionAction(new AnnotatedLCCollectionAdapter(new ProfilesEntryMessageCollectionAdapter(this.service, this.config, providerConfig))), 
			new LCCollectionAction(new AnnotatedLCCollectionAdapter(new ProfilesRelatedEntryMessageCollectionAdapter(this.service, this.config, providerConfig))),
			new LCCollectionAction(new AnnotatedLCCollectionAdapter(new ProfilesNamedEntryMessageCollectionAdapter(this.service, this.config, providerConfig))),
			new LCCollectionAction(new AnnotatedLCCollectionAdapter(new ProfilesSystemEntryMessageCollectionAdapter(this.service, this.config, providerConfig))),
			new LCCollectionAction(new AnnotatedLCCollectionAdapter(new ProfilesCommentMessageCollectionAdapter(this.service, this.config, providerConfig))),
			new LCCollectionAction(new AnnotatedLCCollectionAdapter(new RecommendationCollectionAdapter(this.service, this.config, providerConfig))));
	
		this.targetBuilder = initTargetBuilder();
	}

	/**
	 * Filter class for 'Entry' and 'Comment' types
	 */
	private static class MessageVectorLCFilter implements LCFilter {

		public static final MessageVectorLCFilter INSTANCE = new MessageVectorLCFilter();

		/*
		 * (non-Javadoc)
		 * @see com.ibm.lconn.core.web.atom.LCFilter#doFilter(org.apache.abdera.protocol.server.RequestContext,
		 * com.ibm.lconn.core.web.atom.LCFilterChain)
		 */
		public ResponseContext doFilter(LCRequestContext request, LCFilterChain chain) throws Exception {
			LCModelHelper.setModelAttribute(request, EntryMessageCollectionAdapter.MODEL_VECTOR_TYPE, WallConstants.VECTOR_TYPE);
			LCModelHelper.setModelAttribute(request, EntryMessageCollectionAdapter.MODEL_RESOURCE_TYPE, WallConstants.RESOURCE_TYPE);
			return chain.next(request);
		}
	}

	private static class SystemEntryLCFilter implements LCFilter {
		public static final SystemEntryLCFilter INSTANCE = new SystemEntryLCFilter();
		public static final ProfileLookupExternalIdSetLCFilter KEYLIST_FILTER = new ProfileLookupExternalIdSetLCFilter(
				BaseAction.DEFAULT_PARAM_TYPE_MAP, ProfilesBaseEntryMessageCollectionAdapter.MODEL_RESOURCE_IDS);

		public ResponseContext doFilter(LCRequestContext request, LCFilterChain chain) throws Exception {
			Object keys = LCModelHelper.getModelAttribute(request, SystemEntryMessageCollectionAdapter.MODEL_RESOURCE_IDS);

			LCModelHelper.setModelAttribute(request, SystemEntryMessageCollectionAdapter.MODEL_TITLE,
					ResourceManager.getString(request.getPreferredLocale(), keys == null ? "title.theboard.all" : "title.theboard.multi"));
			return chain.next(request);
		}
	}

	private static class EntryResourceIdFilter implements LCFilter {

		public static final EntryResourceIdFilter BASIC_INSTANCE = new EntryResourceIdFilter(false, false);
		public static final EntryResourceIdFilter CAN_USE_CURRENT_USER_INSTANCE = new EntryResourceIdFilter(true, false);
		public static final EntryResourceIdFilter RELATED_INSTANCE = new EntryResourceIdFilter(false, true);

		private PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		private boolean canUseCurrentUser;
		private boolean forRelated;

		private EntryResourceIdFilter(boolean canUseCurrentUser, boolean forRelated) {
			this.canUseCurrentUser = canUseCurrentUser;
			this.forRelated = forRelated;
		}

		/*
		 * (non-Javadoc)
		 * @see com.ibm.lconn.core.web.atom.LCFilter#doFilter(com.ibm.lconn.core.web.atom.LCRequestContext,
		 * com.ibm.lconn.core.web.atom.LCFilterChain)
		 */
		public ResponseContext doFilter(LCRequestContext request, LCFilterChain chain) throws Exception {
			ProfileLookupKey plk = RequestContextUtils.getProfileLookupKey(request, BaseAction.DEFAULT_PARAM_TYPE_MAP);
			if (!canUseCurrentUser) AssertionUtils.assertNotNull(plk, AssertionType.BAD_REQUEST);

			Employee profile = plk != null ? service.getProfile(plk, ProfileRetrievalOptions.MINIMUM) : AppContextAccess
					.getCurrentUserProfile();

			// If not auth, prompt for auth if can use current user and PLK is not null
			AssertionUtils.assertNotNull(profile, canUseCurrentUser && plk == null ? AssertionType.UNAUTHORIZED_ACTION
					: AssertionType.BAD_REQUEST);

			request.setAttribute(Scope.REQUEST, PLK_ATTR, plk);
			request.setAttribute(Scope.REQUEST, ASSOC_RESOURCE_ATTR, profile);

			if (forRelated) {
				MVConnectionOptions options = new MVConnectionOptions();
				options.setSourceKey(profile.getKey());
				LCModelHelper.setModelAttribute(request, RelatedEntryMessageCollectionAdapter.MODEL_RELATED_OPTIONS, options);
				LCModelHelper.setModelAttribute(
						request,
						EntryMessageCollectionAdapter.MODEL_TITLE,
						ResourceManager.format(request.getPreferredLocale(), "title.theboard.network",
								new Object[] { profile.getDisplayName() }));
			}
			else {
				LCModelHelper.setModelAttribute(request, EntryMessageCollectionAdapter.MODEL_RESOURCE_ID, profile.getGuid());
				LCModelHelper.setModelAttribute(request, EntryMessageCollectionAdapter.MODEL_TITLE,
						ResourceManager.format(request.getPreferredLocale(), "title.theboard", new Object[] { profile.getDisplayName() }));
			}

			return chain.next(request);
		}
	}

	private static class NamedEntryFilter implements LCFilter {

		public static final NamedEntryFilter INSTANCE = new NamedEntryFilter();

		/*
		 * (non-Javadoc)
		 * @see com.ibm.lconn.core.web.atom.LCFilter#doFilter(com.ibm.lconn.core.web.atom.LCRequestContext,
		 * com.ibm.lconn.core.web.atom.LCFilterChain)
		 */
		public ResponseContext doFilter(LCRequestContext request, LCFilterChain chain) throws Exception {
			LCModelHelper.setModelAttribute(request, NamedEntryMessageCollectionAdapter.MODEL_SINCE_DATE,
					new Date(System.currentTimeMillis() - WallConstants.STATUS_TIME_WINDOW));
			return chain.next(request);
		}
	}

	/**
	 * Implements config
	 */
	private static class ProviderConfig implements MessageVectorProviderConfig {

		public LCFilter[] getRecommendationCollFilters() {
			// TODO Auto-generated method stub
			return null;
		}

		private static final LCFilter[] FILTERS = { MessageVectorLCFilter.INSTANCE };
		private static final LCFilter[] SYS_FILTERS = { SystemEntryLCFilter.KEYLIST_FILTER, SystemEntryLCFilter.INSTANCE,
				MessageVectorLCFilter.INSTANCE };
		private static final LCFilter[] EC_FILTERS = { SystemEntryLCFilter.KEYLIST_FILTER, EntryResourceIdFilter.BASIC_INSTANCE,
				MessageVectorLCFilter.INSTANCE };
		private static final LCFilter[] RE_FILTERS = { SystemEntryLCFilter.KEYLIST_FILTER, EntryResourceIdFilter.RELATED_INSTANCE,
				MessageVectorLCFilter.INSTANCE };
		private static final LCFilter[] NE_FILTERS = { SystemEntryLCFilter.KEYLIST_FILTER,
				EntryResourceIdFilter.CAN_USE_CURRENT_USER_INSTANCE, NamedEntryFilter.INSTANCE, MessageVectorLCFilter.INSTANCE };

		public ProviderConfig() {
		}

		public LCFilter[] getCommentCollFilters() {
			return FILTERS;
		}

		public LCFilter[] getCommentFilters() {
			return FILTERS;
		}

		public int getDefaultPageSize() {
			return ProfilesConfig.instance().getDataAccessConfig().getDefaultPageSize();
		}

		public LCFilter[] getEntryCollFilters() {
			return EC_FILTERS;
		}

		public LCFilter[] getSystemEntryCollFilters() {
			return SYS_FILTERS;
		}

		public LCFilter[] getEntryFilters() {
			return FILTERS;
		}

		public LCFilter[] getRelatedEntryCollFilters() {
			return RE_FILTERS;
		}

		public LCFilter[] getNamedEntryFilters() {
			return NE_FILTERS;
		}

		public StringBuilder appendResourceIdQueryString(LCRequestContext request, StringBuilder sb) {
			ProfileLookupKey plk = (ProfileLookupKey) request.getAttribute(Scope.REQUEST, PLK_ATTR);
			AssertionUtils.assertNotNull(plk, AssertionType.PRECONDITION);
			return sb.append(plk.getType().name().toLowerCase()).append('=').append(LCResponseUtils.urlEncode(plk.getValue()));
		}

		public String getServiceName() {
			return "profiles";
		}

		public String getServletPath() {
			return "/atom/mv";
		}

		public String[] getVectorContextPathStrings() {
			return new String[] { "theboard", "thebuzz" }; /* TODO temp for migration */
		}

		public String getGeneratorName() {
			return AtomConstants.GENERATOR_NAME;
		}

		private static final String VECTOR_KEY = ProfilesMessageVectorProvider.class.getName();

		public String getAltLink(LCRequestContext request, EntryMessage message) {
			String vectorKey = VECTOR_KEY + "." + message.getVectorId();
			MessageVector vector = (MessageVector) request.getAttribute(Scope.REQUEST, vectorKey);
			if (vector == null) {
				// MessageVectorService svc = AppServiceContextAccess.getContextObject(MessageVectorService.class);
				MessageVectorService svc = new NewsMessageVectorServiceRemote();
				vector = svc.getMessageVector(message.getVectorId());
				request.setAttribute(Scope.REQUEST, vectorKey, vector);
			}

			StringBuilder sb = new StringBuilder(FeedUtils.getProfilesURL(request));
			sb.append("/html/profileView.do?userid=").append(vector.getAssocResourceId());
			sb.append("&entryId=").append(message.getEntryId());
			sb.append("&lang=").append(request.getPreferredLocale().toString().toLowerCase());

			return sb.toString();
		}

	}

}
