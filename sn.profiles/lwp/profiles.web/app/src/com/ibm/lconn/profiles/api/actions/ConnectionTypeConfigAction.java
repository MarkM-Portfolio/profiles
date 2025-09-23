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
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig;
import com.ibm.lconn.profiles.web.actions.CachableAction;
import com.ibm.lconn.profiles.web.util.CachingHelper;

/**
 * Action returns information about configured connection types on the server.
 */
public class ConnectionTypeConfigAction
					extends CachableAction  // used to extends APIAction
					implements AtomConstants
{
	public ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		response.setContentType("application/xml; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		// enable caching on page
		CachingHelper.setCachableForDynamic(response, isPublic, THREE_HOURS);

		StreamWriter sw = writerFactory.newStreamWriter();
		sw.setWriter(response.getWriter());
		sw.startDocument();
		sw.setPrefix("", LCAtomConstants.NS_SNX);
		
		sw.startElement(QN_CONNECTION_TYPE_CONFIG);
		sw.writeNamespace("snx", LCAtomConstants.NS_SNX);
		sw.setPrefix("snx", LCAtomConstants.NS_SNX);
		
		Map<String, ? extends ConnectionTypeConfig> config = ProfilesConfig.instance().getDMConfig().getConnectionTypeConfigs();
		for (String key : config.keySet()) {
			ConnectionTypeConfig ctc = config.get(key);
			
			sw.startElement(QN_CONNECTION_TYPE);
			sw.writeAttribute("type", ctc.getType());
			sw.writeAttribute("workflow", ctc.getWorkflow().getName());
			sw.writeAttribute("graph", ctc.getGraph().getName());			
			sw.writeAttribute("indexed", String.valueOf(ctc.isIndexed()));
			sw.writeAttribute("extension", String.valueOf(ctc.isExtension()));
			sw.writeAttribute("rel", AtomGenerator2.buildLinkRelationForConnectionType(ctc));
			sw.writeAttribute("nodeOfCreator", ctc.getNodeOfCreator().getName());
			sw.writeAttribute("messageAcl", ctc.getMessageAcl().getName());
			if (ctc.getNotificationType() != null && ctc.getNotificationType().length() > 0) {
				sw.writeAttribute("notificationType", ctc.getNotificationType());
			}
			
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
