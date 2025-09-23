/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.messageboard.adapters;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.ResponseContext;
import com.ibm.lconn.core.appext.msgvector.api.MessageVectorService;
import com.ibm.lconn.core.appext.msgvector.atom.api.CommentMessageCollectionEntityProvider;
import com.ibm.lconn.core.appext.msgvector.atom.api.CommentMessageEntityProvider;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageParser;
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
import com.ibm.lconn.core.appext.msgvector.data.CommentMessage;
import com.ibm.lconn.core.appext.msgvector.data.CommentMessageResultsCollection;
import com.ibm.lconn.core.appext.msgvector.data.CommentMessageRetrievalOptions;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessage;
import com.ibm.lconn.core.web.atom.LCRequestContext;
import com.ibm.lconn.core.web.atom.annotation.CollectionAction;
import com.ibm.lconn.core.web.atom.annotation.CollectionActionMapping;
import com.ibm.lconn.core.web.atom.annotation.ModelAttribute;
import com.ibm.lconn.core.web.atom.annotation.RequestParameter;
import com.ibm.lconn.core.web.atom.annotation.ValueFallback;
import com.ibm.lconn.core.web.atom.annotation.ValueRequired;
import com.ibm.lconn.core.web.atom.util.AFEntityProviderResponseContext;
import com.ibm.lconn.core.web.atom.util.LCAtomConstants;
import com.ibm.lconn.core.web.atom.util.LCResponseUtils;

/**
 *
 *
 */
public class ProfilesCommentMessageCollectionAdapter extends ProfilesBaseCollectionAdapter {

	private static final Logger LOGGER = Logger.getLogger(ProfilesCommentMessageCollectionAdapter.class.getName());
	
	private static final String CLASS_NAME = ProfilesCommentMessageCollectionAdapter.class.getSimpleName();

	/*
	 * Request parameters
	 */
	public static final String PARAM_ENTRY_ID = "entryId";
	public static final String PARAM_COMMENT_ID = "commentId";
	/*
	 * Model values
	 */
	public static final String MODEL_VECTOR_TYPE = ProfilesBaseCollectionAdapter.MODEL_VECTOR_TYPE;
	public static final String MODEL_COMMENT = "comment";
	public static final String MODEL_ENTRY = "entry";
	public static final String MODEL_COMMENT_OPTIONS = "commentRetrievalOptions";
	
	
	public ProfilesCommentMessageCollectionAdapter(
			MessageVectorService service,
			MessageVectorServiceConfig config,
			MessageVectorProviderConfig providerConfig) 
	{
		super(service, config, providerConfig);
	}

	/**
	 * Gets a feed of Comments
	 * 
	 * @param requestContext
	 * @param options
	 * @param entryId
	 * @return
	 */
	@CollectionActionMapping(methodAction=CollectionAction.GetFeed)
	public ResponseContext doGetComments(
			LCRequestContext requestContext, 
			@ModelAttribute(name=MODEL_COMMENT_OPTIONS) @ValueRequired CommentMessageRetrievalOptions options,
			@ModelAttribute(name=MODEL_ENTRY) EntryMessage entryMessge)
	{
		// rtc defect 99834: if we get no value (null) send a 404.
		if (LOGGER.isLoggable(Level.FINER)){
			LOGGER.entering(CLASS_NAME, "doGetComments", "entry id=" + entryMessge.getEntryId() + ", sortOrder=" + options.getSortOrder()
					+ ", sortMethod=" + options.getSortMethod() + ", pageSize=" + options.getPageSize() + ", page=" + options.getPage());
		}
		ResponseContext response;
		if (entryMessge != null) {
			CommentMessageResultsCollection results = service.getCommentMessages(entryMessge.getEntryId(), options);
			CommentMessageCollectionEntityProvider collectionProvider = new CommentMessageCollectionEntityProvider(requestContext, options,
					results, entryMessge);
			response = new AFEntityProviderResponseContext(collectionProvider, requestContext.getAbdera(), LCAtomConstants.CHARENC_UTF8);
			response.setContentType(LCAtomConstants.MIME_ATOM, LCAtomConstants.CHARENC_UTF8);
			response.setStatus(LCAtomConstants.SC_OK);
		}
		else {
			response = LCResponseUtils.emptyAtomResponse(LCAtomConstants.SC_NOT_FOUND);
		}
		return response;
	}

	/**
	 * Gets an entry
	 * 
	 * @param request
	 * @param comment
	 * @return
	 * @throws Exception
	 */
	@CollectionActionMapping(methodAction=CollectionAction.GetEntry)
	// rtc defect 99834: if we get no value (null) send a 404.
	public ResponseContext doGetComment(LCRequestContext requestContext, @ModelAttribute(name=MODEL_COMMENT) CommentMessage comment)
	{
		if (LOGGER.isLoggable(Level.FINER)) LOGGER.entering(CLASS_NAME, "doGetComment", "comment id=" + comment.getCommentId());
		
		ResponseContext response;
		if (comment != null) {
			CommentMessageEntityProvider entityProvider = new CommentMessageEntityProvider(requestContext, comment);
			entityProvider.setWriteStartEndDoc(true);
			response = new AFEntityProviderResponseContext(entityProvider, requestContext.getAbdera(), LCAtomConstants.CHARENC_UTF8);
			response.setContentType(LCAtomConstants.MIME_ATOM, LCAtomConstants.CHARENC_UTF8);
			response.setStatus(LCAtomConstants.SC_OK);
		}
		else {
			response = LCResponseUtils.emptyAtomResponse(LCAtomConstants.SC_NOT_FOUND);
		}
		return response;
	}
	
	/**
	 * Deletes an entry
	 * 
	 * @param commentId
	 * @return
	 */
	@CollectionActionMapping(methodAction=CollectionAction.DeleteEntry)
	public ResponseContext doDeleteComment(@RequestParameter(name=PARAM_COMMENT_ID) @ValueRequired String commentId)
	{
		if(LOGGER.isLoggable(Level.FINER))
			LOGGER.entering(CLASS_NAME, "doDeleteComment", "comment id="+commentId);
		service.deleteCommentMessage(commentId);
		return LCResponseUtils.emptyAtomResponse();
	}

	@CollectionActionMapping(methodAction=CollectionAction.PostEntry)
	public ResponseContext createComment(
			LCRequestContext requestContext,
			@RequestParameter(name=PARAM_ENTRY_ID) @ValueRequired String entryId)
		throws Exception
	{
		if(LOGGER.isLoggable(Level.FINER))
			LOGGER.entering(CLASS_NAME, "createComment", "entry id="+entryId);
		Document<Entry> commentDoc = requestContext.getDocument();
		CommentMessage comment = MessageParser.parseCommentMessage(commentDoc.getRoot());
		comment.setEntryId(entryId);
		comment.setCommentId(service.createCommentMessage(comment));		
		ResponseContext response = LCResponseUtils.emptyAtomResponse(LCAtomConstants.SC_CREATED);
		response.setHeader(LCAtomConstants.HEADER_LOCATION, requestContext.getLCProvider().urlFor(requestContext, comment, null));
		return response;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.core.appext.atom.api.SNAXCollectionAdapter#putEntry(org.apache.abdera.protocol.server.RequestContext)
	 */
	public ResponseContext putEntry(
			LCRequestContext requestContext,
			@ModelAttribute(name=MODEL_COMMENT) @ValueRequired CommentMessage oldComment)
		throws Exception 
	{
		if(LOGGER.isLoggable(Level.FINER))
			LOGGER.entering(CLASS_NAME, "putEntry", "entry id="+oldComment.getEntryId()+", comment id="+oldComment.getCommentId());
		Document<Entry> commentDoc = requestContext.getDocument();
		CommentMessage comment = MessageParser.parseCommentMessage(commentDoc.getRoot());
		comment.setEntryId(oldComment.getEntryId());
		service.updateCommentMessage(comment);
		return LCResponseUtils.emptyAtomResponse();
	}
	
	/**
	 * Utility to get entry message
	 * 
	 * @return
	 */
	@ModelAttribute(name=MODEL_ENTRY)
	public EntryMessage modelEntryMessage(@RequestParameter(name=PARAM_ENTRY_ID) @ValueRequired String entryId)
	{
		EntryMessage rtnVal = service.getEntryMessage(entryId);
		return rtnVal;
	}

	/**
	 * Utility method to get Comment message
	 * 
	 * @param request
	 * @param commentId
	 * @return
	 * @throws Exception
	 */
	@ModelAttribute(name=MODEL_COMMENT)
	public CommentMessage modelCommentMessage(
			LCRequestContext requestContext, 
			@RequestParameter(name=PARAM_COMMENT_ID) @ValueRequired String commentId)
		throws Exception
	{
		return service.getCommentMessage(commentId);
	}
	
	@ModelAttribute(name=MODEL_COMMENT_OPTIONS)
	public CommentMessageRetrievalOptions modelOptions(
			LCRequestContext request,
			@ValueFallback @ValueDefaultSortOrder(order=SortOrder.ASC) @RequestParameter(name=MessageVectorAtomConstants.SORT_ORDER, mapper=SortOrderHandler.class) SortOrder sortOrder,
			@ValueFallback @ValueDefaultSortMethod(method=SortMethod.PUBLISHED) @RequestParameter(name=MessageVectorAtomConstants.SORT_METHOD, mapper=SortMethodHandler.class) SortMethod sortMethod) 
	{
		MessageVectorConfig mvConfig = this.getMVConfig(request);
		
		CommentMessageRetrievalOptions options = new CommentMessageRetrievalOptions();
		options.setMessageTypes(mvConfig.getCommentMessageConfigs().keySet().toArray(EMPTY_STRING_ARRAY));
		options.setSortMethod(sortMethod);
		options.setSortOrder(sortOrder);
		options.setPage(getPage(request));
		options.setPageSize(getPageSize(request));
		
		return options;
	}

}
