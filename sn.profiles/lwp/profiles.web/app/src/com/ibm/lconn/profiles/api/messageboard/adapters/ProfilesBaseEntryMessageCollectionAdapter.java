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
package com.ibm.lconn.profiles.api.messageboard.adapters;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ibm.lconn.core.appext.msgvector.api.MessageVectorService;
import com.ibm.lconn.core.appext.msgvector.atom.api.CommentIncludeOptions;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageTypesMapper;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageVectorAtomConstants;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageVectorProviderConfig;
import com.ibm.lconn.core.appext.msgvector.atom.api.SortMethodHandler;
import com.ibm.lconn.core.appext.msgvector.atom.api.SortMethodHandler.ValueDefaultSortMethod;
import com.ibm.lconn.core.appext.msgvector.atom.api.SortOrderHandler;
import com.ibm.lconn.core.appext.msgvector.atom.api.SortOrderHandler.ValueDefaultSortOrder;
import com.ibm.lconn.core.appext.msgvector.config.MessageVectorConfig;
import com.ibm.lconn.core.appext.msgvector.config.MessageVectorServiceConfig;
import com.ibm.lconn.core.appext.msgvector.data.BaseMessageRetrievalOptions.SortMethod;
import com.ibm.lconn.core.appext.msgvector.data.BaseMessageRetrievalOptions.SortOrder;
import com.ibm.lconn.core.appext.msgvector.data.CommentMessageRetrievalOptions;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessageRetrievalOptions;
import com.ibm.lconn.core.appext.msgvector.data.MessageVector;
import com.ibm.lconn.core.web.atom.LCRequestContext;
import com.ibm.lconn.core.web.atom.annotation.ModelAttribute;
import com.ibm.lconn.core.web.atom.annotation.RequestParameter;
import com.ibm.lconn.core.web.atom.annotation.ValueDefaultBoolean;
import com.ibm.lconn.core.web.atom.annotation.ValueDefaultEnum;
import com.ibm.lconn.core.web.atom.annotation.ValueFallback;
import com.ibm.lconn.core.web.atom.exception.BadRequestException;
import com.ibm.lconn.core.web.atom.util.LCModelHelper;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 *
 * 
 */
public abstract class ProfilesBaseEntryMessageCollectionAdapter extends ProfilesBaseCollectionAdapter {

	public static final String MODEL_VECTOR_TYPE = ProfilesBaseCollectionAdapter.MODEL_VECTOR_TYPE;
	public static final String MODEL_ENTRY_OPTIONS = "entryMessageOptions";
	public static final String MODEL_RESOURCE_ID = "assocResourceId";
	public static final String MODEL_RESOURCE_TYPE = "assocResourceType";
	public static final String MODEL_VECTOR = "messageVectorObj";
	public static final String MODEL_TITLE = "feedTitle";
	public static final String MODEL_RESOURCE_IDS = "modelResourceIds";
	private PeoplePagesService profileSvc = AppServiceContextAccess.getContextObject(PeoplePagesService.class);

	private static final Logger LOGGER = Logger.getLogger(ProfilesBaseEntryMessageCollectionAdapter.class.getName());

	private static final String CLASS_NAME = ProfilesBaseEntryMessageCollectionAdapter.class.getSimpleName();

	protected ProfilesBaseEntryMessageCollectionAdapter(MessageVectorService service, MessageVectorServiceConfig config,
			MessageVectorProviderConfig providerConfig) {
		super(service, config, providerConfig);
	}

	@ModelAttribute(name = MODEL_VECTOR)
	public MessageVector modelMessageVector(LCRequestContext request) {
		String assocResourceId = (String) LCModelHelper.getModelAttribute(request, MODEL_RESOURCE_ID);
		String assocResourceType = (String) LCModelHelper.getModelAttribute(request, MODEL_RESOURCE_TYPE);
		String vectorType = (String) LCModelHelper.getModelAttribute(request, MODEL_VECTOR_TYPE);

		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.entering(CLASS_NAME, "modelMessageVector", "assocResourceId=" + assocResourceId + ", assocResourceType=" + assocResourceType
					+ ", vectorType=" + vectorType);

		ProfileLookupKey plk = ProfileLookupKey.forGuid(assocResourceId);
		Employee profile = profileSvc.getProfile(plk, ProfileRetrievalOptions.MINIMUM);

		MessageVector messageVector = service.getMessageVector(vectorType, profile.getUserid());
		if (messageVector == null) {
			messageVector = new MessageVector();
			// messageVector.setAssocResourceId(assocResourceId);
			messageVector.setAssocResourceId(profile.getUserid());
			messageVector.setAssocResourceType(assocResourceType);
			messageVector.setVectorType(vectorType);
			messageVector.setVectorId(service.createMessageVector(messageVector));
		}

		return messageVector;
	}

	@ModelAttribute(name = MODEL_ENTRY_OPTIONS)
	public EntryMessageRetrievalOptions modelOptions(
			LCRequestContext request,
			@ValueFallback @ValueDefaultEnum(value = "none", type = CommentIncludeOptions.class) @RequestParameter(name = MessageVectorAtomConstants.COMMENTS) CommentIncludeOptions comments,
			@ValueFallback @ValueDefaultBoolean(value = true) @RequestParameter(name = MessageVectorAtomConstants.COMMENTS_INCLUDE_FIRST) boolean inclFirstComment,
			/* legacy option */
			@ValueFallback @ValueDefaultBoolean(value = false) @RequestParameter(name = MessageVectorAtomConstants.INCL_COMMENTS) boolean inclComments,
			@ValueFallback @RequestParameter(name = MessageVectorAtomConstants.SINCE) Date since,
			@ValueFallback @RequestParameter(name = MessageVectorAtomConstants.SINCE_ENTRY_ID) String sinceEntryId,
			@ValueFallback @RequestParameter(name = MessageVectorAtomConstants.MESSAGE_TYPES, mapper = MessageTypesMapper.class) String[] messageTypes,
			@ValueFallback @ValueDefaultSortOrder(order = SortOrder.DESC) @RequestParameter(name = MessageVectorAtomConstants.SORT_ORDER, mapper = SortOrderHandler.class) SortOrder sortOrder,
			@ValueFallback @ValueDefaultSortMethod(method = SortMethod.PUBLISHED) @RequestParameter(name = MessageVectorAtomConstants.SORT_METHOD, mapper = SortMethodHandler.class) SortMethod sortMethod)
			throws BadRequestException {
		MessageVectorConfig mvConfig = getMVConfig(request);

		EntryMessageRetrievalOptions options = new EntryMessageRetrievalOptions();

		options.setMessageTypes(trimToAllowed(mvConfig, messageTypes));
		options.setSortMethod(sortMethod);
		options.setSortOrder(sortOrder);
		options.setPageSize(getPageSize(request));
		options.setSinceEntryId(sinceEntryId);
		if (since != null) options.setSince(since);

		boolean includedComments = false;
		boolean inlinedComments = false;

		if (inclComments) {
			includedComments = true;
			inlinedComments = true;
		}
		else {
			switch (comments) {
				case inline :
					includedComments = true;
					inlinedComments = true;
					break;
				case all :
					includedComments = true;
					break;
				case none :
					break; // do nothing
			}
		}

		if (includedComments) {
			options.setInlineComments(inlinedComments);

			options.setCommentRetrievalOptions(new CommentMessageRetrievalOptions());
			options.getCommentRetrievalOptions().setMessageTypes(mvConfig.getCommentMessageConfigs().keySet().toArray(EMPTY_STRING_ARRAY));
			options.getCommentRetrievalOptions().setFirstLastOnly(true);
			options.getCommentRetrievalOptions().setHeadCount(inclFirstComment ? 1 : 0);
			options.getCommentRetrievalOptions().setTailCount(2);
			options.getCommentRetrievalOptions().setSortMethod(SortMethod.PUBLISHED);
			options.getCommentRetrievalOptions().setSortOrder(SortOrder.ASC);
		}

		return options;
	}

	private String[] trimToAllowed(MessageVectorConfig mvConfig, String[] messageTypes) throws BadRequestException {
		if (messageTypes.length == 0) return mvConfig.getEntryMessageConfigs().keySet().toArray(EMPTY_STRING_ARRAY);

		for (String type : messageTypes) {
			if (!mvConfig.getEntryMessageConfigs().containsKey(type)) throw new BadRequestException(type);
		}

		return messageTypes;
	}

}
