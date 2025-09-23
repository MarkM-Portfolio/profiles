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

package com.ibm.peoplepages.vcard.actions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.CharEncoding;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.api.actions.APIAction;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ui.VCardExportConfig;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;

/**
 * @author sberajaw
 * @author ahernm
 * 
 * Part of the Profiles API. Returns a profile given an internet email address
 * as a vCard (http://www.imc.org/pdi/vcard-21.txt).
 */
public class ProfileAction extends APIAction
{
	private final VcardTemplate vcTemp;
	
	public ProfileAction() throws IOException {
		vcTemp = new VcardTemplate();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.api.actions.APIAction#doExecuteGET(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
    public ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception 
    {    	
    	Employee profile = getAndStoreActionBean(request, Employee.class);
    	
    	if (AssertionUtils.nonEmptyString(profile.getEmail()) && LCConfig.instance().isEmailReturned())
    	{
    		response.setHeader("Content-Disposition:", "attachment;filename=" + encode(profile.getEmail()) + ".vcf");
    	}
    	else
    	{
    		response.setHeader("Content-Disposition:", "attachment;filename=" + profile.getKey() + ".vcf");
    	}
    		
    	String encoding = request.getParameter("encoding");
    	if (encoding == null || !CharEncoding.isSupported(encoding)) {
    		encoding = VCardExportConfig.instance().getCharsets().get(0).getName();
    	}

    	response.setCharacterEncoding(encoding);
    	response.setContentType("text/x-vCard");
    	response.getWriter().write(vcTemp.convert(profile, encoding));
    	
    	return null;
    }

	private String encode(String email) throws UnsupportedEncodingException {
	
		String sEnc = URLEncoder.encode(email,"UTF-8");
		
		sEnc = sEnc.replaceAll("\\%27","'");
		sEnc = sEnc.replaceAll("\\%40","@");
		
		sEnc = sEnc.replaceAll("\\%","");
		
		return sEnc;

	}

	
	protected Employee instantiateActionBean(HttpServletRequest request)
		throws Exception
	{
		ProfileLookupKey plk = getProfileLookupKey(request);
		AssertionUtils.assertNotNull(plk);
		
		Employee profile = pps.getProfile(plk, ProfileRetrievalOptions.EVERYTHING);
		AssertionUtils.assertNotNull(profile);
		
		return profile;
	}

	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		return getAndStoreActionBean(request, Employee.class).getLastUpdate().getTime();
	}
}
