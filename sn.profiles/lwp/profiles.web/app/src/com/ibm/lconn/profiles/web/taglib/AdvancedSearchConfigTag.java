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
package com.ibm.lconn.profiles.web.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.ui.UIAttributeConfig;
import com.ibm.lconn.profiles.config.ui.UISearchFormConfig;

public class AdvancedSearchConfigTag extends SimpleTagSupport {

	public final void doTag() throws JspException, IOException {
		PageContext pageContext = (PageContext) getJspContext();
		//HttpServletRequest request = (HttpServletRequest)pageContext.getAttribute(PageContext.REQUEST);			
		
		// set the advanced Search Object on Request (no reason this must be on the
		// request. page scope makes sense and is more prevalent, and I believe the default.
		List<String> hashedAttributes = new ArrayList<String>();
		Map<String, UIAttributeConfig> attrsLookup = new HashMap<String, UIAttributeConfig>();
		// MT enable
		UISearchFormConfig uisfcfg = ProfilesConfig.instance().getSFormLayoutConfig();
		for (UIAttributeConfig attribute : uisfcfg.getAttributes()) {
			// change dots to $
			//String attributeHashed = attribute.getAttributeId().replace('.','$');
			if ( !attribute.getIsHideFromForm() ) {
			    //hashedAttributes.add(attributeHashed);
			    //attrsLookup.put(attributeHashed, attribute);
				hashedAttributes.add(attribute.getAttributeId());
				attrsLookup.put(attribute.getAttributeId(), attribute);
			}
		}
		pageContext.setAttribute("advancedSearchConfig",hashedAttributes);
		pageContext.setAttribute("advancedSearchConfigAttrs",attrsLookup);
		//request.setAttribute("advancedSearchConfig",hashedAttributes);
		//request.setAttribute("advancedSearchConfigAttrs",attrsLookup);
	}
}
