/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.util;

/**
 *
 *
 */
public class UIAttributeWriterConfig {

	private String startAttr;
	private String endAttr;
	
	private String startLabel;
	private String endLabel;
	
	private String startValue;
	private String endValue;
	
	public UIAttributeWriterConfig() {}

	/**
	 * @return the endAttr
	 */
	public final String getEndAttr() {
		return endAttr;
	}

	/**
	 * @param endAttr the endAttr to set
	 */
	public final void setEndAttr(String endAttr) {
		this.endAttr = endAttr;
	}

	/**
	 * @return the endLabel
	 */
	public final String getEndLabel() {
		return endLabel;
	}

	/**
	 * @param endLabel the endLabel to set
	 */
	public final void setEndLabel(String endLabel) {
		this.endLabel = endLabel;
	}

	/**
	 * @return the endValue
	 */
	public final String getEndValue() {
		return endValue;
	}

	/**
	 * @param endValue the endValue to set
	 */
	public final void setEndValue(String endValue) {
		this.endValue = endValue;
	}

	/**
	 * @return the startAttr
	 */
	public final String getStartAttr() {
		return startAttr;
	}

	/**
	 * @param startAttr the startAttr to set
	 */
	public final void setStartAttr(String startAttr) {
		this.startAttr = startAttr;
	}

	/**
	 * @return the startLabel
	 */
	public final String getStartLabel() {
		return startLabel;
	}

	/**
	 * @param startLabel the startLabel to set
	 */
	public final void setStartLabel(String startLabel) {
		this.startLabel = startLabel;
	}

	/**
	 * @return the startValue
	 */
	public final String getStartValue() {
		return startValue;
	}

	/**
	 * @param startValue the startValue to set
	 */
	public final void setStartValue(String startValue) {
		this.startValue = startValue;
	}
}
