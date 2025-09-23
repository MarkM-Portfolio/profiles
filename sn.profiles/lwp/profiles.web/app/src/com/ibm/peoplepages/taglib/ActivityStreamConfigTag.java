/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import com.ibm.lconn.profiles.web.util.ProfilesASConfigService;
import java.util.Locale;

public class ActivityStreamConfigTag extends ValueWriterTag 
{	
	private String userId;
	private String currentUserId;
	private String currentUserDisplayName;
	private Locale locale = null;
	private String entryId = "";
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public void setCurrentUserId(String currentUserId) {
		this.currentUserId = currentUserId;
	}	
	
	public void setCurrentUserDisplayName(String currentUserDisplayName) {
		this.currentUserDisplayName = currentUserDisplayName;
	}
	
	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	protected String getValue() throws JspException, IOException {
	
		String cfgObj;
		try {

			Locale profLocale = this.locale;
			
			if (profLocale == null) {
				PageContext pageContext = (PageContext) getJspContext();
				HttpServletRequest request = (HttpServletRequest)pageContext.getAttribute(PageContext.REQUEST);			
				profLocale = request.getLocale();
				if (profLocale == null) {
					profLocale = Locale.getDefault();
				}				
			}			
			
			ProfilesASConfigService cfgSvc = new ProfilesASConfigService();
			cfgObj = cfgSvc.getProfilesASConfig(profLocale, this.userId, this.currentUserId, this.currentUserDisplayName, this.entryId);
			
		} catch (java.lang.Exception ee) {
			cfgObj = "{ERROR: 1}";
		}
	
		return cfgObj;
	}

}
