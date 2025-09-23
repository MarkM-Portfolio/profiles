/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public final class FileSubmissionHelper implements ServletContextListener {
	private static final Log LOG = LogFactory
			.getLog(FileSubmissionHelper.class);

	private static final int READ_BUFFER_SIZE = 1024;

	private static int photoMaxBytes;
	private static String[] photoMimeTypes;

	private static int pronunciationMaxBytes;
	private static String[] pronunciationMimeTypes;

	private static final String profileNoImageFileType = "image/png";
	private static final String pixelImageFileType = "image/gif";
	
	public static final String profileNoImageFileName = "/nav/common/styles/images/personNoPhoto128.png";
	public static final String profileNoImageSmallFileName = "/nav/common/styles/images/personNoPhoto64.png";
	public static final String profileNoImageFileNameExt = "/nav/common/styles/images/extPersonNoPhoto128.png";
	public static final String profileNoImageSmallFileNameExt = "/nav/common/styles/images/extPersonNoPhoto64.png";
	private static final String profileUserImageExtOverlay155 = "/nav/common/styles/images/extPersonPhotoOverlay155.png";
	private static final String profileUserImageExtOverlay64 = "/nav/common/styles/images/extPersonPhotoOverlay64.png";
	public static final String pixelImageFileName = "/nav/common/styles/images/blank.gif";
	
	private static byte[] profileNoImageContents, profileNoImageContentsExt, profileImageExtOverlayContents, pixelImageContents;
	private static byte[] profileNoImageSmallContents, profileNoImageSmallContentsExt, profileImageExtOverlaySmallContents;

	private static Properties mimeExtensionMapping;

	public FileSubmissionHelper() {
	}

	/*
	 * Reads the servlet input stream into a byte array
	 */
	public static byte[] getSubmission(HttpServletRequest request)
			throws IOException {
		ByteArrayOutputStream photoFile = new ByteArrayOutputStream();
		InputStream inputStream = request.getInputStream();

		int b;
		while ((b = inputStream.read()) > -1) {
			photoFile.write(b);
		}

		return photoFile.toByteArray();
	}

	public static int getPhotoMaxBytes() {
		return photoMaxBytes;
	}

	public static String[] getPhotoMimeTypes() {
		return photoMimeTypes;

	}

	public static int getPronunciationMaxBytes() {
		return pronunciationMaxBytes;
	}

	public static String[] getPronunciationMimeTypes() {
		return pronunciationMimeTypes;
	}

	public static String getPronunciationFileExtension(String mimeType) {
		return mimeExtensionMapping.getProperty(mimeType, ".wav");
	}

	public static byte[] getProfileNoImageContents() {
		return getProfileNoImageContents(false);
	}

	public static byte[] getProfileNoImageContents(boolean thumbnail) {
		return thumbnail ? profileNoImageSmallContents : profileNoImageContents;
	}

	public static byte[] getProfileNoImageExtContents() {
		return getProfileNoImageExtContents(false);
	}

	public static byte[] getProfileNoImageExtContents(boolean thumbnail) {
		return thumbnail ? profileNoImageSmallContentsExt : profileNoImageContentsExt;
	}

	public static byte[] getProfileImageExtOverlayContents() {
		return getProfileImageExtOverlayContents(false);
	}

	public static byte[] getProfileImageExtOverlayContents(boolean thumbnail) {
		return thumbnail ? profileImageExtOverlaySmallContents : profileImageExtOverlayContents;
	}
	
	public static byte[] getPixelImageContents(){
		return pixelImageContents;
	}

	public static final String getProfileNoImageFileType() {
		return profileNoImageFileType;
	}
	
	public static final String getPixelImageFileType() {
		return pixelImageFileType;
	}

	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();

		try {
			mimeExtensionMapping = new Properties();
			mimeExtensionMapping
					.load(context
							.getResourceAsStream("/WEB-INF/mime-ext-mappings.properties"));

			InputStream validationFile = context
					.getResourceAsStream("/WEB-INF/validation.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setExpandEntityReferences(false);
			dbf.setNamespaceAware(false);
			dbf.setAttribute(
					"http://apache.org/xml/features/nonvalidating/load-external-dtd",
					Boolean.FALSE);
			dbf.setAttribute("http://xml.org/sax/features/validation",
					Boolean.FALSE);

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document validationDocument = db.parse(validationFile);

			XPath xpath = XPathFactory.newInstance().newXPath();

			photoMaxBytes = Integer.MAX_VALUE;
			photoMimeTypes = getMimeTypes((String) xpath
					.evaluate(
							"/form-validation/formset/form[@name='uploadPhotoForm']/field[@property='photo']/var[var-name='allowedmimetypes']/var-value",
							validationDocument, XPathConstants.STRING));

			pronunciationMaxBytes = ((Double) xpath
					.evaluate(
							"/form-validation/formset/form[@name='editProfilePronunciationForm']/field[@property='pronunciation']/var[var-name='maxfilesize']/var-value",
							validationDocument, XPathConstants.NUMBER))
					.intValue();
			pronunciationMimeTypes = getMimeTypes((String) xpath
					.evaluate(
							"/form-validation/formset/form[@name='editProfilePronunciationForm']/field[@property='pronunciation']/var[var-name='allowedmimetypes']/var-value",
							validationDocument, XPathConstants.STRING));

			for (String pronMimeType : pronunciationMimeTypes) {
				if (!mimeExtensionMapping.containsKey(pronMimeType)) {
					LOG.error("Unable to find file extension mapping for "
							+ pronMimeType + " defaulting to '.wav'");
				}
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("photoMaxBytes: " + photoMaxBytes);
				LOG.debug("photoMimeTypes: " + Arrays.asList(photoMimeTypes));
				LOG.debug("pronunciationMaxBytes: " + pronunciationMaxBytes);
				LOG.debug("pronunciationMimeTypes: "
						+ Arrays.asList(pronunciationMimeTypes));
			}

		} catch (Exception e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(e.getMessage(), e);
			}
		}

		profileNoImageContents = getContents(context, profileNoImageFileName);
		profileNoImageSmallContents = getContents(context, profileNoImageSmallFileName);
		
		profileNoImageContentsExt = getContents(context, profileNoImageFileNameExt);
		profileNoImageSmallContentsExt = getContents(context, profileNoImageSmallFileNameExt);

		profileImageExtOverlayContents = getContents(context, profileUserImageExtOverlay155);
		profileImageExtOverlaySmallContents = getContents(context, profileUserImageExtOverlay64);
		
		pixelImageContents = getContents(context,pixelImageFileName);
	}

	private byte[] getContents(ServletContext context, String path) {
		// how do we look in the common override directory?
		byte[] contents = null;
		try {
			File file = new File(context.getRealPath(path));
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(
					READ_BUFFER_SIZE);

			byte[] buffer = new byte[READ_BUFFER_SIZE]; // larger than file
																// to read

			while (true) {
				int read = fis.read(buffer);
				if (read == -1)
					break;
				bos.write(buffer, 0, read);
			}

			contents = bos.toByteArray();

			// Need to close the stream
			bos.close();
			fis.close();

		} catch (Exception e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(e.getMessage(), e);
			}

			contents = new byte[0];
		}
		return contents;
	}

	public void contextDestroyed(ServletContextEvent sce) {
	}

	private String[] getMimeTypes(String value) {
		String[] values = value.split(",");

		for (int i = 0; i < values.length; i++)
			values[i] = values[i].trim();

		return values;
	}

}
