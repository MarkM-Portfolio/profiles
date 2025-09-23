/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.ui;

import com.ibm.lconn.profiles.config.AbstractConfigObject;
import org.apache.commons.configuration.Configuration;

public class SametimeAwarenessConfig extends AbstractConfigObject {
	
	private static final long serialVersionUID = -5454730229720840564L;
	
	private boolean sametimeEnabled = false;
	
	private String unsecureHref = null;
	
	private String secureHref = null;
	
	private String sametimeInputType = null; 
	
	public SametimeAwarenessConfig(){
		//do nothing
	}
	public SametimeAwarenessConfig(Configuration profilesConfig){
		
		sametimeEnabled = profilesConfig.getBoolean("sametimeAwareness[@enabled]", false);
		
		
		if(sametimeEnabled){
					this.unsecureHref = validateUrl(profilesConfig.getString("sametimeAwareness[@href]"));
					this.secureHref = validateUrl(profilesConfig.getString("sametimeAwareness[@ssl_href]"));
		}
		
		sametimeInputType = profilesConfig.getString("sametimeAwareness[@sametimeInputType]");
		
		if(sametimeInputType == null)
			sametimeInputType = "email";//default
	
		
	}
	
	public String getSametimeUnsecureHref(){
		return this.unsecureHref;
	}
	
	public String getSametimeSecureHref(){
		return this.secureHref;
	}
	public String getSametimeInputType(){
		return this.sametimeInputType;
	}
	
	private String validateUrl(String url){
		String validurl = null;
	
		if(url != null){
		  validurl = url;
			int length = url.length();
			Character c = new Character(url.charAt(length - 1));
			
			if(c.compareTo(new Character('/')) != 0){				
				validurl = url+"/";
				
			}
		}
		return validurl;
	}
	
	
	

}
