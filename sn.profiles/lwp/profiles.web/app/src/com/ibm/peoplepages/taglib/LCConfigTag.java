/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspException;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * @author bbarber
 */
public class LCConfigTag extends ValueWriterTag {

	@Override
	protected String getValue() throws JspException, IOException {
		return LCConfig.instance().getProperty(_prop, _default, _type);
	}
	
	protected boolean isEnabled(){
		return LCConfig.instance().isEnabled(_gatekeeperName, _prop, false);
	}
	
	private String _prop;
	private String _default = "";
	private String _type = PeoplePagesServiceConstants.GENERIC_TAG_TYPE; // do we need this?
	private String _gatekeeperName = null;
	
    public String getProperty() {
        return _prop;
    }
    public void setProperty(String prop) {
        _prop = prop;
    }
    
    public String getGatekeeperName() {
        return _gatekeeperName;
    }
    public void setGatekeeperName(String gatekeeperName) {
        _gatekeeperName = gatekeeperName;
    }

    public String getDefault() {
        return _default;
    }
    public void setDefault(String defVal) {
        _default = defVal;
    }

    public String getType() {
        return _type;
    }
    public void setType(String type) {
        _prop = type;
    }		
}
