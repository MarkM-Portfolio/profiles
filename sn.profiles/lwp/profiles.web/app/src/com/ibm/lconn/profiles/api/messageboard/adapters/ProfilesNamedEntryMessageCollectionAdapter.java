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

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import com.ibm.json.java.JSONObject;
import com.ibm.lconn.core.appext.msgvector.api.MessageVectorService;
import com.ibm.lconn.core.appext.msgvector.atom.api.EntryMessageEntityProvider;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageParser;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageVectorAtomConstants;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageVectorProviderConfig;
import com.ibm.lconn.core.appext.msgvector.config.MessageVectorConfig;
import com.ibm.lconn.core.appext.msgvector.config.MessageVectorServiceConfig;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessage;
import com.ibm.lconn.core.appext.msgvector.data.MediaItem;
import com.ibm.lconn.core.appext.msgvector.data.MessageVector;
import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess;
import com.ibm.lconn.core.web.atom.LCRequestContext;
import com.ibm.lconn.core.web.atom.annotation.CollectionAction;
import com.ibm.lconn.core.web.atom.annotation.CollectionActionMapping;
import com.ibm.lconn.core.web.atom.annotation.ModelAttribute;
import com.ibm.lconn.core.web.atom.annotation.RequestParameter;
import com.ibm.lconn.core.web.atom.annotation.ValueRequired;
import com.ibm.lconn.core.web.atom.exception.UnsupportedActionMethodException;
import com.ibm.lconn.core.web.atom.util.AFEntityProviderResponseContext;
import com.ibm.lconn.core.web.atom.util.LCAtomConstants;
import com.ibm.lconn.core.web.atom.util.LCResponseUtils;

/**
 *
 * 
 */
public class ProfilesNamedEntryMessageCollectionAdapter extends ProfilesBaseEntryMessageCollectionAdapter {

	public static final String PARAM_NAME = "name";
	public static final String MODEL_SINCE_DATE = "sinceDateNamedEntryMessage";

	private static final Logger LOGGER = Logger.getLogger(ProfilesNamedEntryMessageCollectionAdapter.class.getName());

	private static final String CLASS_NAME = ProfilesNamedEntryMessageCollectionAdapter.class.getSimpleName();

	public ProfilesNamedEntryMessageCollectionAdapter(MessageVectorService service, MessageVectorServiceConfig config,
			MessageVectorProviderConfig providerConfig) {
		super(service, config, providerConfig);
	}

	@CollectionActionMapping(methodAction = { CollectionAction.GetEntry, CollectionAction.HeadEntry })
	public ResponseContext doGetNamedEntry(LCRequestContext requestContext,
			@RequestParameter(name = PARAM_NAME) @ValueRequired String ptrName,
			@ModelAttribute(name = MODEL_RESOURCE_ID) @ValueRequired String resourceId,
			@ModelAttribute(name = MODEL_RESOURCE_IDS) List<String> resourceIds,
			@ModelAttribute(name = MODEL_VECTOR_TYPE) @ValueRequired String vectorType,
			@ModelAttribute(name = MODEL_SINCE_DATE) @ValueRequired Date sinceDate) throws Exception {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.entering(CLASS_NAME, "doGetNamedEntry", "ptrName=" + ptrName + ", resourceId=" + resourceId + ", resourceIds="
					+ resourceIds + ", vector type=" + vectorType + ", sinceDate=" + sinceDate);

		String resourceIdentifier = null;
		if (null != resourceIds && null != resourceIds.get(0)) {
			resourceIdentifier = resourceIds.get(0);
		}
		else {
			// RTC 73856 : current user's GUID, passed via resourceId by
			// com.ibm.lconn.profiles.api.providers.ProfilesMessageVectorProvider.EntryResourceIdFilter.doFilter(LCRequestContext,
			// LCFilterChain)
			resourceIdentifier = resourceId;
		}

		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.log(Level.FINER, "vector type=" + vectorType + ", ptrName=" + ptrName + ", resourceIdentifier=" + resourceIdentifier
					+ ", resourceIds=" + resourceIds + ", sinceDate=" + sinceDate);

		EntryMessage entry = service.getNamedEntryMessage(vectorType, resourceIdentifier, ptrName, sinceDate);

		if (entry == null) {
			return LCResponseUtils.emptyAtomResponse(LCAtomConstants.SC_NO_CONTENT);
		}

		EntryMessageEntityProvider entryEntity = new EntryMessageEntityProvider(requestContext, entry);
		entryEntity.setWriteStartEndDoc(true);
		ResponseContext response = new AFEntityProviderResponseContext(entryEntity, requestContext.getAbdera(),
				LCAtomConstants.CHARENC_UTF8);
		response.setHeader(MessageVectorAtomConstants.HEADER_MESSAGE_ENTRYID, entry.getEntryId());
		response.setHeader(MessageVectorAtomConstants.HEADER_MESSAGE_SUMMARY, encode64(entry.getSummary()));
		response.setHeader(MessageVectorAtomConstants.HEADER_MESSAGE_PUBLISHED, entry.getPublished());
		response.setHeader(MessageVectorAtomConstants.HEADER_MESSAGE_TYPE, entry.getType());
		response.setHeader(MessageVectorAtomConstants.HEADER_COMMENT_COUNT, entry.getCommentCount());
		response.setContentType(LCAtomConstants.MIME_ATOM, LCAtomConstants.CHARENC_UTF8);

		if (MessageVectorConfig.isBoardExtensionEnabled() && entry.getMediaItem() != null) {
			JSONObject itemJson = MediaItem.toJsonObject(entry.getMediaItem());
			for (Map.Entry<String, String> jsonToHeader : MediaItem.JSON_TO_HEADER_MAP.entrySet()) { // Key: header / Value: json
				String val = (String) itemJson.get(jsonToHeader.getKey());
				if (StringUtils.isNotBlank(val)) {
					response.setHeader(jsonToHeader.getValue(), val);
				}
			}
		}

		response.setStatus(LCAtomConstants.SC_OK);
		return response;
	}

	@CollectionActionMapping(methodAction = CollectionAction.DeleteEntry)
	public ResponseContext doClearNamedEntry(LCRequestContext requestContext,
			@ModelAttribute(name = MODEL_VECTOR) @ValueRequired MessageVector messageVector,
			@RequestParameter(name = PARAM_NAME) @ValueRequired String ptrName) throws Exception {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.entering(CLASS_NAME, "doClearNamedEntry", "message vector id=" + messageVector.getVectorId() + ", ptrName=" + ptrName);

		service.clearNamedEntryMessage(messageVector.getVectorId(), ptrName);
		return LCResponseUtils.emptyAtomResponse();
	}

	@CollectionActionMapping(methodAction = CollectionAction.PutEntry)
	public ResponseContext doSetNamedEntry(LCRequestContext requestContext,
			@ModelAttribute(name = MODEL_VECTOR) @ValueRequired MessageVector messageVector,
			@RequestParameter(name = PARAM_NAME) @ValueRequired String ptrName) throws Exception {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.entering(CLASS_NAME, "doSetNamedEntry", "message vector id=" + messageVector.getVectorId() + ", ptrName=" + ptrName);

		Document<Entry> entryDoc = requestContext.getDocument();
		EntryMessage entryMsg = MessageParser.parseEntryMessage(entryDoc.getRoot());
		entryMsg.setVectorId(messageVector.getVectorId());
		service.setNamedEntryMessage(ptrName, entryMsg);
		return LCResponseUtils.emptyAtomResponse();
	}

	/**
	 * API for homepage
	 * 
	 * @param requestContext
	 * @param messageVector
	 * @param ptrName
	 * @return
	 * @throws Exception
	 */
	@CollectionActionMapping(methodAction = CollectionAction.ExtensionAction)
	public ResponseContext doPostNamedEntry(LCRequestContext requestContext,
			@ModelAttribute(name = MODEL_VECTOR) @ValueRequired MessageVector messageVector,
			@RequestParameter(name = PARAM_NAME) @ValueRequired String ptrName) throws Exception {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.entering(CLASS_NAME, "doPutNamedEntry", "message vector id=" + messageVector.getVectorId() + ", ptrName=" + ptrName);

		if (TargetType.TYPE_ENTRY == requestContext.getTarget().getType() && LCAtomConstants.HTTP_POST.equals(requestContext.getMethod())) {
			EntryMessage entryMsg = new EntryMessage();
			entryMsg.setVectorId(messageVector.getVectorId());
			entryMsg.setType(requestContext.getHeader(MessageVectorAtomConstants.HEADER_MESSAGE_TYPE));
			entryMsg.setContentText(decode64(requestContext.getHeader(MessageVectorAtomConstants.HEADER_MESSAGE_SUMMARY)));
			entryMsg.setContentType(LCAtomConstants.MIME_TEXT_PLAIN);
			entryMsg.setLocale(SNAXAppContextAccess.getAppContext().getCurrentUserLocale());

			if (MessageVectorConfig.isBoardExtensionEnabled()) {
				JSONObject jsonObj = new JSONObject();
				for (Map.Entry<String, String> jsonToHeader : MediaItem.JSON_TO_HEADER_MAP.entrySet()) {
					String headerVal = requestContext.getHeader(jsonToHeader.getValue());
					if (StringUtils.isNotBlank(headerVal)) {
						jsonObj.put(jsonToHeader.getKey(), headerVal);
					}
				}

				MediaItem item = MediaItem.fromJsonObject(jsonObj);
				if (item.isValid()) {
					entryMsg.setMediaItem(item);
				}
			}

			String entryId = service.setNamedEntryMessage(ptrName, entryMsg);
			return LCResponseUtils.emptyAtomResponse().addHeader(MessageVectorAtomConstants.HEADER_MESSAGE_ENTRYID, entryId)
					.addHeader(LCAtomConstants.HEADER_LOCATION, requestContext.getProvider().urlFor(requestContext, entryMsg, null));
		}

		throw new UnsupportedActionMethodException();
	}

	private String decode64(String header) throws UnsupportedEncodingException {
		if (header == null) return null;
		return new String(Base64.decodeBase64(header.getBytes()), LCAtomConstants.CHARENC_UTF8);
	}

	private String encode64(String header) throws UnsupportedEncodingException {
		return new String(Base64.encodeBase64(header.getBytes(LCAtomConstants.CHARENC_UTF8)));
	}
}
