/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2012, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.ProfileTag;
import com.ibm.peoplepages.data.ProfileTagCloud;
import com.ibm.peoplepages.data.ProfileTagRetrievalOptions.Verbosity;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.webui.ajax.actions.LoginInfoAction;

public abstract class AbstractProfileTagsAction extends APIAction implements AtomConstants {

	protected static final Map<String, ProfileLookupKey.Type> FLAG_BY_PARAM_TYPE_MAP;

	static {
		HashMap<String, ProfileLookupKey.Type> pm = new HashMap<String, ProfileLookupKey.Type>(6);
		pm.put(PeoplePagesServiceConstants.FLAG_BY_KEY, ProfileLookupKey.Type.KEY);
		pm.put(PeoplePagesServiceConstants.FLAG_BY_UID, ProfileLookupKey.Type.UID);
		pm.put(PeoplePagesServiceConstants.FLAG_BY_GUID, ProfileLookupKey.Type.GUID);
		pm.put(PeoplePagesServiceConstants.FLAG_BY_USERID, ProfileLookupKey.Type.USERID);
		FLAG_BY_PARAM_TYPE_MAP = Collections.unmodifiableMap(pm);
	}

	//
	// Java bean for passing info around Action
	//
	protected static class Bean {
		ProfileLookupKey sourceLookupKey = null;
		ProfileLookupKey targetLookupKey = null;
		ProfileLookupKey flagByLookupKey = null;

		ProfileTagCloud tagCloud = null;

		boolean fullFormat = false;

		public Bean() {
		}
	}

	protected long getLastModified(HttpServletRequest request) throws Exception {
		ProfileTagCloud tagCloud = resolveBeanAsserted(request).tagCloud;
		return tagCloud.getRecordUpdated().getTime();
	}

	protected Bean resolveBeanAsserted(HttpServletRequest request) throws Exception {
		Bean reqBean = getActionBean(request, Bean.class);

		if (reqBean == null) {
			reqBean = new Bean();
			reqBean.sourceLookupKey = getProfileLookupKey(request, SOURCE_PARAM_TYPE_MAP);
			reqBean.targetLookupKey = getProfileLookupKey(request, TARGET_PARAM_TYPE_MAP);
			reqBean.flagByLookupKey = getProfileLookupKey(request, FLAG_BY_PARAM_TYPE_MAP);
			assertNotNull(reqBean.targetLookupKey);

			if (reqBean.sourceLookupKey != null) {
				reqBean.tagCloud = tagSvc.getProfileTags(reqBean.sourceLookupKey, reqBean.targetLookupKey);
			}
			else {
				reqBean.fullFormat = PeoplePagesServiceConstants.FULL.equals(request.getParameter(PeoplePagesServiceConstants.FORMAT));

				boolean inclContrib = (reqBean.fullFormat || reqBean.flagByLookupKey != null);

				reqBean.tagCloud = tagSvc.getProfileTagCloud(reqBean.targetLookupKey, inclContrib ? Verbosity.RESOLVE_CONTRIBUTORS
						: Verbosity.MINIMUM);
			}

			assertNotNull(reqBean.tagCloud.getTargetKey());

			storeActionBean(request, reqBean);
		}

		return reqBean;
	}

	protected ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Bean reqBean = resolveBeanAsserted(request);
		ProfileTagCloud tagCloud = reqBean.tagCloud;

		AtomGenerator generator = new AtomGenerator(response, APP_CONTENT_TYPE);

		generator.writeStartCategoriesDocument(null, false);

		writeParamAttribute(generator, SOURCE_PARAM_TYPE_MAP, reqBean.sourceLookupKey);
		writeParamAttribute(generator, TARGET_PARAM_TYPE_MAP, reqBean.targetLookupKey);
		writeParamAttribute(generator, FLAG_BY_PARAM_TYPE_MAP, reqBean.flagByLookupKey);

		// Use profiles-policy.xml to check whether tagOthers is enabled or not
		// New since 3.0
		// TODO: check for performance concerns by looking up users
		Employee source = null;
		Employee target = pps.getProfile(reqBean.targetLookupKey, ProfileRetrievalOptions.MINIMUM);

		if (reqBean.sourceLookupKey != null)
			source = pps.getProfile(reqBean.sourceLookupKey, ProfileRetrievalOptions.MINIMUM);
		else if (reqBean.flagByLookupKey != null) source = pps.getProfile(reqBean.flagByLookupKey, ProfileRetrievalOptions.MINIMUM);

		// Backward compatibility to still 'tagOthersEnabled'?
		// In 3.0, tagOthers is considered enabled, users other than himself can add tags,
		// i.e., if the scope to add tag for the user is person_and_self or colleague_and_self
		boolean personSelfScope = PolicyHelper.checkAcl(Acl.TAG_ADD, target);

		generator.getWriter().writeAttribute(NS_SNX, "tagOthersEnabled", Boolean.valueOf(personSelfScope).toString());

		generator.getWriter().writeAttribute(NS_SNX, "canAddTag",
				Boolean.valueOf(source == null ? false : PolicyHelper.checkAcl(Acl.TAG_ADD, target, source)).toString());

		// Include number of taggers in feed
		generator.getWriter().writeAttribute(NS_SNX, "numberOfContributors", String.valueOf(tagCloud.getContributors().size()));

		generator.writeGenerator();

		Employee flagByProfile = null;
		if (reqBean.flagByLookupKey != null) {
			for (Employee c : tagCloud.getContributors().values()) {
				if (c.matchesLookupKey(reqBean.flagByLookupKey)) {
					flagByProfile = c;
					break;
				}
			}
		}

		for (ProfileTag tag : tagCloud.getTags()) {
			generator.writeStartCategory(tag.getTag());
			
			// backwards compatibility mandates that we keep 'social' tags without a scheme
			// TODO need writer/parser for this
			String scheme = AtomParser3.tagTypeToScheme(tag.getType());
			if (scheme != null && scheme.length() > 0) {
				generator.getWriter().writeAttribute("scheme", scheme);				
			}
			generator.getWriter().writeAttribute(NS_SNX, FREQUENCY, String.valueOf(tag.getFrequency()));
			generator.getWriter().writeAttribute(NS_SNX, INTENSITY_BIN, String.valueOf(tag.getIntensityBin()));
			generator.getWriter().writeAttribute(NS_SNX, VISIBILITY_BIN, String.valueOf(tag.getVisibilityBin()));
			generator.getWriter().writeAttribute(NS_SNX, TYPE, String.valueOf(tag.getType()));
			
			if (flagByProfile != null && tag.getSourceKeys() != null && Arrays.asList(tag.getSourceKeys()).contains(flagByProfile.getKey())) {
				generator.getWriter().writeAttribute(NS_SNX, FLAGGED, Boolean.TRUE.toString());
			}

			if (reqBean.fullFormat && tag.getSourceKeys() != null && tag.getSourceKeys().length > 0) {
				boolean allowEmailInReturn = LCConfig.instance().isEmailReturned();

				for (String contribKey : tag.getSourceKeys()) {
					Employee contribProfile = tagCloud.getContributors().get(contribKey);

					if (contribProfile != null) {
						generator.getWriter().writeStartElement(NS_ATOM, "contributor");
						generator.getWriter().writeAttribute(NS_SNX, PROFILE_KEY, contribProfile.getKey());
						generator.getWriter().writeAttribute(NS_SNX, PROFILE_UID, contribProfile.getUid());
						generator.getWriter().writeAttribute(NS_SNX, PROFILE_GUID, contribProfile.getGuid());

						generator.getWriter().writeStartElement(NS_ATOM, "name");
						generator.getWriter().writeCharacters(contribProfile.getDisplayName());
						generator.getWriter().writeEndElement(); // name

						generator.getWriter().writeStartElement(NS_SNX, "userid");
						generator.getWriter().writeCharacters(contribProfile.getUserid());
						generator.getWriter().writeEndElement(); // userid

						if (allowEmailInReturn) {
							generator.getWriter().writeStartElement(NS_ATOM, "email");
							generator.getWriter().writeCharacters(contribProfile.getEmail());
							generator.getWriter().writeEndElement(); // email
						}

						UserState state = contribProfile.getState();
						generator.getWriter().writeStartElement(NS_SNX, AtomGenerator2.QN_USERSTATE.getLocalPart());
						generator.getWriter().writeCharacters(state.getName());
						generator.getWriter().writeEndElement(); // userState

						String isExternalStr = String.valueOf(contribProfile.isExternal());
						generator.getWriter().writeStartElement(NS_SNX, AtomGenerator2.QN_ISEXTERNAL.getLocalPart());
						generator.getWriter().writeCharacters(isExternalStr);
						generator.getWriter().writeEndElement(); // isExternal

						generator.getWriter().writeEndElement(); // contributor
					}
				}
			}

			generator.writeEndCategory();
		}

		generator.writeEndCategoriesDocument();

		return null;
	}

	/**
	 * Utility method to output 'snx:[source|target|flagBy][Key|Guid|etc]' attributes.
	 * 
	 * @param generator
	 * @param mapping
	 * @param plk
	 * @return
	 */
	private void writeParamAttribute(AtomGenerator generator, Map<String, ProfileLookupKey.Type> mapping, ProfileLookupKey plk)
			throws Exception {
		if (plk != null) {
			String attrName = "";
			for (Map.Entry<String, ProfileLookupKey.Type> me : mapping.entrySet()) {
				if (me.getValue() == plk.getType()) {
					attrName = me.getKey();
					break;
				}
			}

			generator.getWriter().writeAttribute(AtomConstants.NS_SNX, attrName, plk.getValue());
		}
	}

	/**
	 * permit concrete classes to define permissions for PUT
	 * 
	 * @param request
	 * @param source
	 * @param target
	 * @throws Exception
	 */
	abstract protected void assertPermissionForPut(HttpServletRequest request, Employee source, Employee target) throws Exception;

	/**
	 * This end-point may do either of the following:
	 * 	a) update the tags from a SOURCE to TARGET
	 * 	b) modify existing tags from one category to another
	 * 
	 */
	protected void doPut(HttpServletRequest request) throws Exception {						
		ProfileLookupKey sourcePLK = getProfileLookupKey(request, SOURCE_PARAM_TYPE_MAP);
		ProfileLookupKey targetPLK = getProfileLookupKey(request, TARGET_PARAM_TYPE_MAP);
		String extensionAwareStr = request.getParameter(PeoplePagesServiceConstants.EXTENSION_AWARE);
		boolean isExtensionAware = Boolean.parseBoolean(extensionAwareStr);

		String tagParameter = request.getParameter(PeoplePagesServiceConstants.TAG);
		String fromTypeParameter = request.getParameter(PeoplePagesServiceConstants.FROM_TYPE);
		String toTypeParameter = request.getParameter(PeoplePagesServiceConstants.TO_TYPE);

		assertNotNull(targetPLK);		
		Employee target = pps.getProfile(targetPLK, ProfileRetrievalOptions.MINIMUM);
		assertNotNull(target);
		
		// if these parameters are applied, we will attempt to change the type of tags on an existing profile
		if (tagParameter != null && fromTypeParameter != null && toTypeParameter != null) {
			tagSvc.changeTagType(target.getKey(), tagParameter, fromTypeParameter, toTypeParameter);
		}
		else
		{
			assertNotNull(sourcePLK);
			Employee source = pps.getProfile(sourcePLK, ProfileRetrievalOptions.MINIMUM);
			assertNotNull(source);
			assertPermissionForPut(request, source, target);
			//
			// 	If tag others is disabled; assert source = target (outdated comment? eedavis@20120402)
			//
			tagSvc.updateProfileTags(source.getKey(), target.getKey(), new AtomParser().parseTagsFeed(request.getInputStream()), isExtensionAware);
		}
	}

	/**
	 * permit concrete classes to define permissions for DELETE
	 * 
	 * @param request
	 * @param source
	 * @param target
	 * @throws Exception
	 */
	abstract protected void assertPermissionForDelete(HttpServletRequest request, Employee source, Employee target) throws Exception;

	/**
	 * Delete tags
	 */
	protected void doDelete(HttpServletRequest request) throws Exception {
		ProfileLookupKey sourcePLK = getProfileLookupKey(request, SOURCE_PARAM_TYPE_MAP);
		ProfileLookupKey targetPLK = getProfileLookupKey(request, TARGET_PARAM_TYPE_MAP);
		String tag = request.getParameter(PeoplePagesServiceConstants.TAG);
		String type = request.getParameter(PeoplePagesServiceConstants.TYPE);
		
		assertTrue(sourcePLK == null);
		assertNotNull(targetPLK);
		assertNotNull(tag);
		assertTrue((tag = tag.trim()).length() > 0);

		// default value
		if (type == null) {
			type = TagConfig.DEFAULT_TYPE;
		}
		
		type = type.trim();
		
		//
		// Assert target exists and is current user
		//
		Employee target = pps.getProfile(targetPLK, ProfileRetrievalOptions.MINIMUM);

		assertNotNull(target);

		assertPermissionForDelete(request, null, target);

		// Temp solution to track tag deletions
		/*
		 * if (LOG.isInfoEnabled()) { LOG.info("User action - deleting tag: Current username = "
		 * +LoginInfoAction.getCachedUserRecord(request).getDisplayName() +", userkey = "
		 * +LoginInfoAction.getCachedUserRecord(request).getKey() +"; target username = " +target.getDisplayName() +", target userkey = "
		 * +target.getKey() +"; tag = " +tag); }
		 */

		String sourceKey = LoginInfoAction.getCachedUserRecord(request).getKey();
		tagSvc.deleteProfileTag(sourceKey, target.getKey(), tag, type);
	}
}
