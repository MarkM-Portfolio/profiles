/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.ajax.actions;

import java.io.PrintWriter;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;
import com.ibm.lconn.profiles.web.actions.BaseAction;

/**
 * @author sberajaw
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class TagTypeaheadAction extends BaseAction {
  
  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
	public ActionForward doExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
		HttpServletResponse response) throws Exception {
       
		ProfileTagService service = AppServiceContextAccess.getContextObject(ProfileTagService.class);
		
		String tag = request.getParameter("tag");
		String type = request.getParameter("type");
		
		if (type != null) {
			type = type.trim();
		}
		
		// backwards-compatibility to not include tags outside of the default type if none is specified
		if (type == null || type.length() == 0) {
			type = TagConfig.DEFAULT_TYPE;
		}
		
		// SPR #ZLUU7XQ28J. To prevent NPE
		if ( tag == null ) return null;

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

		
		List<Tag> matchingTags = service.getProfileTagsLike(tag, type);
		request.setAttribute("matchingTags", matchingTags);
		
		String bTag = "<b>" + tag + "</b>";
        request.setAttribute("tag", tag);
        request.setAttribute("bTag", bTag);
      if(request.getParameter("useJson") != null)
      {
	// Need to set the json content type and UTF-8 charset
	response.setContentType("application/json");
	response.setCharacterEncoding("UTF-8");

      	PrintWriter writer = response.getWriter();

	// TODO: we need to revist this /*[ way of the json output
	// Since this may pose some security risks. But for now, leave as it is in 3.0
      	//writer.write("/*");  //BBarber - Defect 63740
      	//writer.write("{");
      	//writer.write(" identifier:'member'");
			//writer.write("	 ,label:'name'");
			//writer.write("	 ,items:
		
		//BBarber - Defect 63740
		//This is more consistent with how other components 
		//format the output for the json object.
      	writer.write("{}&&[");
			for (int i = 0; i < matchingTags.size(); i++)
			{
				Tag aTag  = matchingTags.get(i);
				String aTerm = aTag.getTag();
				// TODO optional rendering of the type
				String aType = aTag.getType();				
				aTerm = aTerm.replaceAll("\n", " ");
				aTerm = aTerm.replaceAll("\r", " ");
				
				//writer.write("{tag: '" + matchingTag + "'}");
				writer.write("\"" + StringEscapeUtils.escapeJavaScript(aTerm) + "\"");
				if (i != (matchingTags.size() - 1))
					writer.write(",");

			}
			writer.write("	 ]");
			//writer.write("	}");
			//writer.write("*/");  //BBarber - Defect 63740
      	
      	return null;
      }
      else
      	return mapping.findForward("tagTypeahead");
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
}
