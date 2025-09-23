/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.photo;

import java.io.InputStream;

import com.ibm.lconn.profiles.test.rest.junit.AbstractTest;
import com.ibm.lconn.profiles.test.rest.util.BaseProfilePhotoHelper;
import com.ibm.lconn.profiles.test.rest.util.Transport;

public abstract class BaseProfilePhotoTest extends AbstractTest
{
	// photo is scaled to 155 X 155 see default value in PropertiesConfig.java [RTC 118290]
	protected final int PHOTO_DEFAULT_HEIGHT = 155;
	protected final int PHOTO_DEFAULT_WIDTH  = 155; // photo is scaled to 155 X 155

	protected InputStream getResourceAsStream(Class<?> clazz, String name){
		return BaseProfilePhotoHelper.getResourceAsStream(clazz, name);
	}

	protected void validateImageDimensions(InputStream is, int height, int width) throws Exception {
		BaseProfilePhotoHelper.validateImageDimensions(is, height, width);
	}

	protected void validateImageDimensions(String imageUrl, Transport transport, int height, int width) throws Exception {
		BaseProfilePhotoHelper.validateImageDimensions(imageUrl, transport, height, width);
	}

	protected String getMcodePhotoUrl(String url, String email){
		return BaseProfilePhotoHelper.getMcodePhotoUrl(url, email);
	}
}
