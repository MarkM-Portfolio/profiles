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

package com.ibm.lconn.profiles.config.dm;

import com.ibm.lconn.profiles.config.AbstractConfigObject;

public class TagConfigImpl extends AbstractConfigObject implements
		TagConfig {

	private static final long serialVersionUID = 1L;

	private String type;
	private boolean phraseSupported;


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isPhraseSupported() {
		return phraseSupported;
	}

	public void setPhraseSupported(boolean phraseSupported) {
		this.phraseSupported = phraseSupported;
	}

	public String toString() {
		StringBuilder result = new StringBuilder().append("[");
		result.append(" type=").append(type);
		result.append(" phraseSupported=").append(phraseSupported);
		result.append("]");
		return result.toString();
	}

}