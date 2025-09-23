/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.taglib;

import java.lang.Boolean;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.ServletRequest;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.functions.AclFunctions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;


public class CheckPermissionTag extends ValueWriterTag {

	@Override
	protected String getValue() throws JspException, IOException {
		PageContext pc = (PageContext) getJspContext();
		ServletRequest req = pc.getRequest();
		
		// we need to check the request object for the displayed profile.  If there is one on the request, then
		// use that in the checkAcl function.  The permissions will intersect with the current user's permissions.
		Employee target = (Employee)req.getAttribute(PeoplePagesServiceConstants.DISPLAYED_PROFILE);
		
		boolean allowed = false;
		if (target != null && _checkTarget == "true") {
			allowed = AclFunctions.checkAcl(_feature, _permission, target);
		} else {
			allowed = AclFunctions.checkAcl(_feature, _permission);
		}
		
		return String.valueOf(allowed);
	}
	
	private String _feature;
    public void setFeature(String feature) {
        _feature = feature;
    }
	
	private String _permission;
    public void setPermission(String permission) {
        _permission = permission;
    }
	
	private String _checkTarget = "true";
    public void setCheckTarget(String checkTarget) {
        _checkTarget = checkTarget;
    }	
}
