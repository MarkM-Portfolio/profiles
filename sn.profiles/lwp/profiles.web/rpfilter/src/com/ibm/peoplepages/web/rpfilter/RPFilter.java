/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.web.rpfilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.lconn.core.web.auth.LCRestSecurityHelper;

/**
 * @author mahern
 *
 * 
 */
public class RPFilter implements Filter 
{
	public static final String CONFIG_PARAM = "rpfilter.config.file";
	public static final String CONFIG_DEFAULT = "rpfilterconf.xml";
	
	private static final String _classname = RPFilter.class.getName();
	static final Logger _logger = RPFilterUtil.getLogger();
	
	private List<RPFilterRule> rules;	
		
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException 
	{
		String configFile = config.getInitParameter(CONFIG_PARAM);
		InputStream in = null;
		rules = new ArrayList<RPFilterRule>();
		
		try {
			if (configFile != null) {
				in = RPFilter.class.getClassLoader().getResourceAsStream(configFile);
			}
		} catch (Exception e) {
			if (_logger.isLoggable(Level.FINE)) {
				_logger.log(Level.WARNING,"warning.RPFilter.unableToFindConfigFile",configFile);
			}
		}
		
		// if no configFile || not able to init
		if (in == null) {
			configFile = CONFIG_DEFAULT;
			in = RPFilter.class.getResourceAsStream(configFile);
		}
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);		
		SAXParser parser;
		try {
			parser = factory.newSAXParser();
			parser.parse(in,new FilterConfigHandler(rules));
		} catch (Exception e) {
			_logger.log(Level.SEVERE,
						RPFilterUtil.getString("error.RPFilter.unableToParseConfigFile", 
									  		   new String[]{configFile, e.getLocalizedMessage()}),e);
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException 
	{
		//
		// Only usable for Http
		//
		assert(req instanceof HttpServletRequest);
		assert(resp instanceof HttpServletResponse);
		
		boolean FINER = _logger.isLoggable(Level.FINER);
		
		if (FINER) {
			_logger.entering(_classname,"doFilter");
		}
		
		//
		// Main filter code
		//
		HttpServletRequest request = (HttpServletRequest) req;
		RPFilterResponse response = new RPFilterResponse((HttpServletResponse)resp);	
		
		boolean chainResponse = true;
		
		if ("GET".equals(request.getMethod()) && isCachingEnabled())
		{			
			String query = request.getQueryString();
			String resource = request.getServletPath()+ ((query != null && query.length() > 0) ? "?" + request.getQueryString() : "");
			RPFilterRule rule = match(request,resource);
			
			//
			// if Rule, setup pre-call
			// 
			if (rule != null) 
			{	
				long ifModSince = request.getDateHeader("If-Modified-Since");
				
				boolean isValid = rule.getInvalidator().isValid(request,resource,ifModSince);
				
				if (ifModSince > -1 && isValid) {
					chainResponse = false;
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				}
				else {
					rule.getResponseSetter().setResponseHeaders(request,response,resource);
				}
			}
		}
		
		//
		// if should continue, execute chain
		//
		if (chainResponse) 
		{
			chain.doFilter(request,response);
		}

		if (FINER) {
			_logger.exiting(_classname,"doFilter");
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		rules = null;
	}

	/**
	 * @param response
	 * @return
	 */
	private RPFilterRule match(HttpServletRequest request, String path) {
		
		boolean FINER =_logger.isLoggable(Level.FINER);
		RPFilterRule rule = null;
		
		if (FINER) {
			_logger.entering(_classname,"match");
			_logger.finer(RPFilterUtil.getString("info.RPFilter.finer.matchResource",new Object[]{path,buildHeaderMap(request)}));
		}
		
		for (int i = 0; i < rules.size(); i++) {
			RPFilterRule r = rules.get(i);
			if (r.matches(request,path)) {
				rule = r;
				break;
			}
		}
		
		if (FINER) {
			_logger.exiting(_classname,"match",rule);
		}
		
		return rule;
	}
	
	private Map<String,String> buildHeaderMap(HttpServletRequest request) {
		Map<String,String> headers = new HashMap<String,String>();
		Enumeration<?> e = request.getHeaderNames();
		while (e.hasMoreElements()) {
			String k = String.valueOf(e.nextElement());
			headers.put(k,request.getHeader(k));
		}

		return headers;
	}

	private boolean isCachingEnabled() throws ServletException
	{
		return LCRestSecurityHelper.isUnauthenticatedRole("reader");
	}
	
	public static class FilterConfigHandler extends DefaultHandler
	{
		private static final String _classname = FilterConfigHandler.class.getName();
		private static final String _namespace = "http://www.ibm.com/ventura/kareoke/rpfilter/1.0";
		private static final String ELEM_MATCH = "matchRule";
		private static final String ELEM_OPTIONS = "options";
		private static final String ELEM_RSOPTS = "rsopts";
		private static final String ELEM_IVDOPTS = "ivdopts";
		private static final Properties emptyProperties = new Properties();
		
		private List<RPFilterRule> rules;
		private boolean EL_matchRule = false;
		private boolean EL_rsopts = false;
		private boolean EL_ivdopts = false;
		private RPFilterResponseSetter responseSetterObj;
		private RPFilterInvalidator invalidatorObj;
		
		public FilterConfigHandler(List<RPFilterRule> rules) {
			this.rules = rules;
		}
		
		public void startDocument() 
		{
			if (_logger.isLoggable(Level.FINER)) {
				_logger.entering(_classname,"startDocument");
			}
		}
		
		public void startElement(String namespaceURI, String localName,
                				 String qName, Attributes atts)
		{
			if (_namespace.equals(namespaceURI)) {
				if (ELEM_MATCH.equals(localName)) {
					handleMatchRuleStart(atts);	
				}
				else if (ELEM_OPTIONS.equals(localName)) {
					handleOptions(atts);
				}
				else if (ELEM_IVDOPTS.equals(localName)) {
					handleIVDOpts(atts);
				}
				else if (ELEM_RSOPTS.equals(localName)) {
					handleRSOpts(atts);
				}				
			}
		}
		
		public void endElement(String namespaceURI, String localName,
				 			   String qName)
		{
			if (_namespace.equals(namespaceURI)) {
				if (ELEM_MATCH.equals(localName)) {
					handleMatchRuleEnd();
				}
			}
		}

		public void endDocument() 
		{
			if (_logger.isLoggable(Level.FINER)) {
				_logger.exiting(_classname,"endDocument");
			}
		}

		private void handleMatchRuleStart(Attributes atts) 
		{
			assert(!EL_matchRule);			
			
			EL_matchRule = true;
			EL_ivdopts = false;
			EL_rsopts = false;
			
			String invalidator = atts.getValue("invalidator");
			String responseSetter = atts.getValue("responseSetter");
			String filter = atts.getValue("filter");
			
			assert(invalidator != null && invalidator.length() > 0);
			assert(responseSetter != null && responseSetter.length() > 0);
			assert(filter != null && filter.length() > 0);
			
			//
			// Handle Response Setters
			//
			if ("time2Live".equals(responseSetter)) {
				responseSetterObj = new RPFilterTime2LiveRSIVD();
			} else if ("indefinite".equals(responseSetter)) {
				responseSetterObj = new RPFilterIndefiniteRSIVD();
			} else if ("custom".equals(responseSetter)) {
				responseSetterObj = (RPFilterResponseSetter) createObject(atts.getValue("responseSetterClass"));
				throw new NullPointerException("RPFilterResponseSetter{custom}: " + atts.getValue("responseSetterClass"));
			} else {
				throw new IllegalArgumentException(RPFilterUtil.getString("info.RPFilter.config.unknownResponseSetter",new Object[]{responseSetter}));
			}
			
			//
			// Handle invalidator
			//
			if ("null".equals(invalidator)) {
				invalidatorObj = new RPFilterNullIVD();
			} else if ("time2Live".equals(invalidator)) {
				invalidatorObj = new RPFilterTime2LiveRSIVD();
			} else if ("indefinite".equals(responseSetter)) {
				invalidatorObj = new RPFilterIndefiniteRSIVD();
			} else if ("custom".equals(invalidator)) {
				invalidatorObj = (RPFilterInvalidator) createObject(atts.getValue("invalidatorClass"));
				throw new NullPointerException("RPFilterInvaldiator{custom}: " + atts.getValue("invalidatorClass"));
			} else {
				throw new IllegalArgumentException(RPFilterUtil.getString("info.RPFilter.config.unknownInvalidator",new Object[]{invalidator}));
			}
			
			RPFilterRule rule = new RPFilterRule(filter,invalidatorObj,responseSetterObj);
			
			rules.add(rule);			
		}
		
		private void handleMatchRuleEnd()
		{
			if (!EL_ivdopts) invalidatorObj.init(emptyProperties);
			if (!EL_rsopts) responseSetterObj.init(emptyProperties);
			
			EL_matchRule = false;
			EL_ivdopts = false;
			EL_rsopts = false;
			
			invalidatorObj = null;
			responseSetterObj = null;
		}

		private final void handleOptions(Attributes atts) {
			assert(EL_matchRule);
			assert(!EL_ivdopts);
			assert(!EL_rsopts);
			
			EL_ivdopts = true;
			EL_rsopts = true;
			
			Properties props = buildProperties(atts);
			
			invalidatorObj.init(props);
			responseSetterObj.init(props);
		}

		private final void handleRSOpts(Attributes atts) {
			assert(EL_matchRule);
			assert(!EL_rsopts);
			
			EL_rsopts = true;
			
			responseSetterObj.init(buildProperties(atts));
		}
		
		private final void handleIVDOpts(Attributes atts) {
			assert(EL_matchRule);
			assert(!EL_ivdopts);
			
			EL_ivdopts = true;
			
			invalidatorObj.init(buildProperties(atts));
		}
		
		private final Properties buildProperties(Attributes atts) {
			Properties props = new Properties();
			int num = atts.getLength();
			
			for (int i = 0; i < num; i++) {
				props.setProperty(atts.getLocalName(i),atts.getValue(i));
			}
						
			return props;
		}
		
		/**
		 * Attempts to create an instance of a class, returns <code>null</code> on failure.
		 * @param className
		 * @return
		 */
		private final Object createObject(String className) {
			assert(className != null);
			try {
				Class<?> cls = Class.forName(className);
				return cls.newInstance();
			} catch (Exception e) {
				return null;
			}
		}
	}
	
}
