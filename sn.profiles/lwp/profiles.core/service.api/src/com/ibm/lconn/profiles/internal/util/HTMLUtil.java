/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2001, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import static java.util.logging.Level.FINER;

import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

import org.jsoup.nodes.Entities.EscapeMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

/*
 * Copied from sn.infra : package com.ibm.lconn.news.activitystreams.service.util.HTMLUtil
 * Harvested the methods IC Profiles needs since the source class is fairly News / Activity-Stream specific
 * and would add bulk to the Profiles footprint.
 */
public class HTMLUtil
{
	private static String CLASS_NAME = HTMLUtil.class.getName(); 
	private static Logger logger = Logger.getLogger(CLASS_NAME);

	public final static HTMLUtil INSTANCE = new HTMLUtil();

	private final static Pattern PATTERN_BR_HTML = Pattern.compile("<[^<]*?br.*?\\/>");	

	/**
	 * The purpose of this method is to remove all HTML from the incoming string. 
	 * 
	 * @param input		A string that may contain HTML.
	 * @param input		(optional) A boolean that requests to replace <br> with new line; default is to replace with space.
	 * @return			A string that does not contain HTML.
	 */
	public static String removeAllHTMLFromString(String input)
	{
		return removeAllHTMLFromString(input, false);
	}
	public static String removeAllHTMLFromString(String input, boolean replaceBRTagWithNL)
	{
		String methodName = "removeAllHTMLFromString";
		if (logger.isLoggable(FINER)) 
			logger.entering(CLASS_NAME, methodName, new Object[] { input });

		String output = input;

		if ((input != null) && (!(input.trim().isEmpty())))
		{
			// 1st: replace <br/> with space -or- \n if requested
			final Matcher matcher = PATTERN_BR_HTML.matcher(input);
			String replaceBR = " ";
			if (replaceBRTagWithNL) {
				System.out.println("replace w/ NL");
				replaceBR = "\n";
			}
			input = matcher.replaceAll(replaceBR);

			// 2nd: remove all other HTML tags
			output = Jsoup.clean(input, Whitelist.none());
		}

		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, methodName, output);

		return output;
	}

	/**
	 * The purpose of this method is to remove all HTML and HTML / XML encodings from the input string.
	 * 
	 * @param input		A string that may contain HTML, and / or HTML / XML encodings.	
	 * @param input		(optional) A boolean that requests to replace <br> with new line; default is to replace with space.
	 * @return			A string that does not contain either HTML, nor HTML, XML encodings. 
	 */
	public static String removeHTMLAndEncodings(String input)
	{
		return removeHTMLAndEncodings(input, false);
	}
	public static String removeHTMLAndEncodings(String input, boolean replaceBRTagWithNL)
	{
		String methodName = "removeHTMLAndEncodings";
		if (logger.isLoggable(FINER)) 
			logger.entering(CLASS_NAME, methodName, new Object[] { input });

		String output = input;

		if ((input != null) && (!(input.trim().isEmpty())))
		{
			output = removeAllHTMLFromString(input);
			output = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeXml(output));
		}

		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, methodName, output);

		return output;
	}

	/**
	 * The purpose of this method is to remove all HTML and HTML / XML encodings from the input string and return plain text.
	 * 
	 * @param str		A string that may contain HTML, and / or HTML / XML encodings.	
	 * @return			A string that does not contain either HTML, nor HTML, XML encodings. 
	 */
	public static String getHTMLAsPlainText(String str)
	{
		String cleaned = null;

		// Parse str into a Document
		Document document = Jsoup.parse(str);

		// Clean the document.
//		doc = new Cleaner(Whitelist.simpleText()).clean(doc); // leaves <strong> and changes ' to &apos;s
		document = new Cleaner(Whitelist.none()).clean(document);       // changes 's to &apos;s

//		doc.select("br").append("\n\n"); // doesn't appear to do as advertised
		document.outputSettings().prettyPrint(false);

		// Adjust escape mode
//		doc.outputSettings().escapeMode(EscapeMode.xhtml); // changes 's to &apos;s
		document.outputSettings().escapeMode(EscapeMode.base);

		// Get back the string of the body.
		String str2 = document.body().html();

		cleaned = str2.replaceAll("&nbsp;", " ");
		return cleaned;
	}
}
