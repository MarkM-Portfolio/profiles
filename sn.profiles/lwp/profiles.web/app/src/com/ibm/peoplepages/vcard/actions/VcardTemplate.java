/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.vcard.actions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.lconn.profiles.internal.util.APIHelper;
import com.ibm.peoplepages.data.Employee;

public class VcardTemplate {
		
	
	private Map<Charset,String> templatesMap = new ConcurrentHashMap<Charset,String>();
	private Map<String,List<Integer>> keys = new HashMap<String,List<Integer>>();
	private String template;
	private int maxIndex;
	
	
	public VcardTemplate() throws IOException {		
		init();
	}

	private Object[] toArgs(Employee profile, Charset charset) {
		Object[] obj = new Object[maxIndex];

		for (String key : keys.keySet()) {
			List<Integer> tolocs = keys.get(key);
			Object v = null;

			if (key.equals("charset")) {
				v = charset.toString();
			} else if ("profilesSvcLocation".equals(key)) {
				/* PMR: 90045,999,760 - vCard has "http" links even though IC forces SSL
				 * LCConfig does not expose VC method boolean getForceConfidentialCommunications() from LCC-config.xml
				 * setting that flag in LCC-config.xml false/true should cause the request to be either http/https
				 * Assuming that is true, we will look at the state of the request and return the appropriate scheme value. 
				 */
				// v = ServiceReferenceUtil.getServiceLink("profiles", false);
				v = ServiceReferenceUtil.getRequestAwareServiceLink("profiles");
			} else {
				String k = key.substring("profile.".length());
				
				if (k.equals("lastUpdate.time")) {
					v = String.valueOf(profile.getLastUpdate().getTime());
				} else if (k.contains(".")) {
					String codeName = k.substring(k.indexOf('.'), k.lastIndexOf('.'));
					AbstractCode<?> c = (AbstractCode<?>) profile.get(codeName);
					if (c != null) {
						String field = k.substring(k.lastIndexOf('.'));
						v = c.getFieldValue(c.getFieldDef(field));
					}
				} else {
					v = profile.get(k);
				}
			}
			if (v == null) 
				v = "";
			
			for (Integer i : tolocs)
				obj[i] = v;
		}
		
		return obj;
	}
	
	public String convert(Employee profile, String charsetName) throws UnsupportedEncodingException, IllegalArgumentException {
		APIHelper.filterProfileAttrForAPI(profile);
		Charset charset = Charset.forName(charsetName);
		String pattern = getTemplate(charset);
		return trimBadData(MessageFormat.format(pattern, toArgs(profile, charset)));
	}
	
	private String getTemplate(Charset charset) throws UnsupportedEncodingException {
		String templ = templatesMap.get(charset);
		
		if (templ == null) {
			templ = new String(template.getBytes(charset.toString()), charset.toString());
			templatesMap.put(charset, templ);
		}
		
		return templ;
	}
	
	private void init() throws IOException {
		StringBuilder sb = new StringBuilder();
		int index = 0;
		
		String doc = loadDoc();
		StringTokenizer st = new StringTokenizer(doc,"${}", true);
		while (st.hasMoreTokens()) {
			String t = st.nextToken();
			if ("$".equals(t)) {
				int i = index++;				
				sb.append(st.nextToken());
				String term = st.nextToken();
				sb.append(i);
				sb.append(st.nextToken());
				
				List<Integer> l = keys.get(term);
				if (l == null) {
					l = new ArrayList<Integer>();
					keys.put(term, l);
				}
				l.add(i);
				
								
			} else {
				sb.append(t);
			}
		}
		
		template = sb.toString();
		maxIndex = index--;
	}

	private String loadDoc() throws IOException {
		Reader fr = null;
		try {
			fr = new InputStreamReader(VcardTemplate.class.getResourceAsStream(
					LCConfig.instance().isEmailReturned() ? 
							"VcardTemplate.properties" : "VcardTemplateNoEmail.properties"));
			char[] chars = new char[1024];
			StringBuilder sb = new StringBuilder();
			
			int read;
			while ((read = fr.read(chars)) > 0)
				sb.append(chars, 0, read);
			
			StringTokenizer st = new StringTokenizer(sb.toString(), "\n");
			sb = new StringBuilder();
			
			boolean startVcard = false;
			while (st.hasMoreTokens()) {
				if (startVcard) {
					sb.append("\n").append(st.nextToken());
				} else {
					String s = st.nextToken();
					if (s.startsWith("BEGIN:VCARD")) {
						sb.append(s);
						startVcard = true;
					}
				}
			}
			
			return sb.toString();
		} finally {
			if (fr != null) fr.close();
		}
	}
	
	private static final String trimBadData(String s) {
		return s.replaceAll("[\\w\\-\\;]+\\:[\\s\\,\\;]+\\n", "").replaceAll("\\;\\,\\;",";;");
	}
	
	/**
	 * Test for trim bad data method
	 * @param argv
	 */
	public static void main(String[] argv) {
		if (argv.length < 1) {
			System.err.println("Must specify strings to test on args");
			System.exit(1);
		}
		
		for (String arg : argv) {
			System.out.println("Trim [" + arg + "]: " + trimBadData(arg));
		}
	}
	
}
