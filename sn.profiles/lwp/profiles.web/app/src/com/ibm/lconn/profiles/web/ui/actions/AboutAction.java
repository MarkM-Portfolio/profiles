/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.ui.actions;

import com.ibm.lconn.core.web.cache.WebCacheUtil;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.peoplepages.util.AuthHelper;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.UrlBinding;

@UrlBinding("/app/about")
public class AboutAction implements ActionBean {
    private ActionBeanContext context;

    public ActionBeanContext getContext() { return context; }
    public void setContext(ActionBeanContext context) { this.context = context; }

    @DefaultHandler
    public Resolution about() {
    	// stripes needs to disappear. we have no machinery in place for encapsulating
    	// stuff like these cache headers. the struts code has plenty to straighten out.
    	// look to forward to html/appView.do to get rid of this file
		if (LCConfig.instance().isMTEnvironment()){
			if (AuthHelper.isAnonymousRequest(context.getRequest()) ){
				//AuthHelper.addAuthReturnCookies(context.getRequest(),context.getResponse());
				String uri = context.getRequest().getRequestURI();
				String ctx = context.getRequest().getContextPath();
				int m = uri.indexOf(ctx);
				String url = uri.substring(0,m)+"/login";
				return new RedirectResolution(url);
			}
		}
		// this cache-header setting is a targeted fix for defect 88884.
    	WebCacheUtil.disableCachingOverridableIESafe(context.getResponse());
        return new ForwardResolution("/WEB-INF/stripes/pages/app/about/aboutProfiles.jsp");
    }
}
