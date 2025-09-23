/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.writer.StreamWriter;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.core.web.atom.util.LCAtomConstants;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.web.actions.CachableAction;
import com.ibm.lconn.profiles.web.util.CachingHelper;

/**
 * Action returns information about configured tags on server.
 */
public class TagsConfigAction
					extends CachableAction  // used to extends APIAction
					implements AtomConstants
{
	public ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("application/xml; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		// enable caching on page
		// since the TagsConfig is unlikely to change much, set the cache to a long time - 24 hours
		CachingHelper.setCachableForDynamic(response, isPublic, TWENTY_FOUR_HOURS);

		StreamWriter sw = writerFactory.newStreamWriter();
		sw.setWriter(response.getWriter());
		sw.startDocument();
		sw.setPrefix("", LCAtomConstants.NS_SNX);

		sw.startElement(QN_TAGS_CONFIG);
		sw.writeNamespace("snx", LCAtomConstants.NS_SNX);
		sw.setPrefix("snx", LCAtomConstants.NS_SNX);

		Map<String, ? extends TagConfig> config = ProfilesConfig.instance().getDMConfig().getTagConfigs();
		for (String key : config.keySet()) {
			TagConfig o = config.get(key);
			sw.startElement(QN_TAG_CONFIG);
			sw.writeAttribute("type", o.getType());
			sw.writeAttribute("scheme", AtomParser3.tagTypeToScheme(o.getType()));
			sw.writeAttribute("phraseSupported", o.isPhraseSupported() ? "true" : "false");
			sw.endElement();
		}

		sw.endElement();
		sw.endDocument();
		return null;
	}

	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return System.currentTimeMillis();
	}

}
