/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;

/**
 * Action supports type-ahead style operations on tags via multiple authentication endpoints (basic/form/oauth) per rest of Profiles API.
 * 
 * URI: /atom/tagTypeAhead.do
 * Query Parameters:
 * 	tag: the tag to filter for type-ahead
 * 	type: the type of tag to limit search results against (optional)
 * 
 * Response:
 * 	JSON
 * 	[
 * 		{
 * 			id: "general.someTag",
 * 			tag: "someTag",
 * 			type: "general"
 * 		}, {
 * 			id: "skills.someOtherTag",
 * 			tag: "someOtherTag",
 * 			type: "skills"
 * 		}
 * ]
 * 
 * 	
 */
public class TagTypeAheadAction extends APIAction implements AtomConstants {

	public ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// read input arguments
		String tag = request.getParameter("tag");
		String type = request.getParameter("type");

		// validate the incoming arguments and normalize
		if (tag == null) {
			return null;
		}		
		
		// normalize input
		tag = tag.toLowerCase();
		
		// only trim if you cant have a space, otherwise, you want to auto-complete on space
        boolean tagCouldHaveASpace = false;
        for (TagConfig tagConfig : DMConfig.instance().getTagConfigs().values()) {
        	if (tagConfig.isPhraseSupported()) {
        		tagCouldHaveASpace = true;
        	}
        }        
        if (!tagCouldHaveASpace) {
        	tag = tag.trim();
        }
		
		if (type != null) {
			type = type.trim();
		}

		// find the results
		ProfileTagService service = AppServiceContextAccess.getContextObject(ProfileTagService.class);
		List<Tag> matchingTags = service.getProfileTagsLike(tag, type);

		// build json representation
		JSONArray items = new JSONArray();
		for (Tag aTag : matchingTags) {
			JSONObject tagObject = new JSONObject();
			tagObject.put("tag", aTag.getTag());
			tagObject.put("type", aTag.getType());
			tagObject.put("id", aTag.getType() + "." + aTag.getTag());
			items.add(tagObject);
		}		
		
		// output results
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
	    PrintWriter writer = response.getWriter();
	    writer.write(items.serialize());
	    return null;
	}
	
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return System.currentTimeMillis();
	}

}
