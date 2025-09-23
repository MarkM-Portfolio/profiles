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
package com.ibm.connections.profiles.test.config.ui;

import com.ibm.lconn.profiles.config.ui.VCardExportConfig;
import com.ibm.lconn.profiles.config.ui.VCardExportConfig.CharsetConfig;
import com.ibm.lconn.profiles.test.BaseTestCase;

/**
 *
 *
 */
public class VCardExportConfigTest extends BaseTestCase {
	
	/*
	  <charset name="UTF-8">
				<label key="label.vcard.encoding.utf8"/>
			</charset>
			<charset name="ISO-8859-1">
				<label key="label.vcard.encoding.iso88591"/>
			</charset>
			<charset name="Cp943c">
				<label key="label.vcard.encoding.cp943c"/>
			</charset>
	 */
	public static final String[][] ccConfig = {
		{"UTF-8", "label.vcard.encoding.utf8", null},
		{"ISO-8859-1","label.vcard.encoding.iso88591", null},
		{"Cp943c","label.vcard.encoding.cp943c", null}
	};
	
	
	public void testConfigs() {
		VCardExportConfig vce = VCardExportConfig.instance();
		
		int i = 0;
		for (CharsetConfig cc : vce.getCharsets()) {
			String[] ae = ccConfig[i];
			assertEquals(ae[0], cc.getName());
			assertEquals(ae[1], cc.getLabel().getKey());
			assertEquals(ae[2], cc.getLabel().getBidref());
			i++;
		}
	}

}
