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
package com.ibm.lconn.profiles.web.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.templates.TemplateConfig;
import com.ibm.lconn.profiles.config.templates.TemplateConfig.TemplateEnum;
import com.ibm.lconn.profiles.config.templates.TemplateDataModel;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.peoplepages.data.Employee;

/**
 *
 * 
 */
public class OutputSearchResultsTag extends AbstractAttributeWriterTag {

	// the logger
	private static final Logger logger = Logger.getLogger(OutputSearchResultsTag.class.getName());

	protected List<Employee> results;

	protected String section;
	
	/**
	 * create writer
	 */
	public void doTag() throws JspException, IOException {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "doTag()", new Object[] {});
		}

		try {
			init();
			write();
		}
		finally {
			if (logger.isLoggable(Level.FINER)) {
				logger.exiting(getClass().getName(), "doTag()");
			}
		}
	}

	public void write() throws IOException {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "write()", new Object[] {});
		}

		HttpServletRequest request = (HttpServletRequest) ((PageContext) getJspContext()).getRequest();
		TemplateConfig templateConfig = ProfilesConfig.instance().getTemplateConfig();
		TemplateEnum templateEnum = TemplateEnum.SEARCH_RESULTS;
		TemplateDataModel templateDataModel = new TemplateDataModel(request);
		if (section != null && section.length() > 0)
		{
			Map<String, Object> mixinMap = new HashMap<String, Object>(1);
			mixinMap.put("section", section);
			templateDataModel.mixin(mixinMap);
		}
		
		Employee profile = null;
		try {
			for (int i = 0; i < results.size(); i++) {
				profile = results.get(i);
				templateDataModel.updateEmployee(profile, null, i);				
				startRow(i, profile);
				templateConfig.processTemplate(templateEnum, templateDataModel, writer);
				endRow();
			}
		}
		catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "There was an error rendering an individual search result.  See exception for details.", e);
			}
			throw new IOException(e);
		}
		finally {
			if (logger.isLoggable(Level.FINER)) {
				logger.exiting(getClass().getName(), "write()");
			}
		}
	}

	/**
	 * Start the row
	 * 
	 * @param rowNum
	 * @param employee
	 * @throws IOException
	 */
	private final void startRow(final int rowNum, Employee employee) throws IOException {
		UserState state =  employee.getState();
		String rowClass = getRowClass(rowNum);
		write("<tr class='").write(rowClass).write("'>");
	}

	/**
	 * Determine the row class
	 * 
	 * @param i
	 * @return
	 */
	private final String getRowClass(final int i) {
		if (i == 0)
			return "lotusFirstRow";
		else if ( i % 2 != 0)  // static analysis tool reports this does not work for neg numbrs ->(i % 2 == 1)
			return "lotusAltRow";
		else
			return "";
	}

	/**
	 * End the row
	 * 
	 * @throws IOException
	 */
	private final void endRow() throws IOException {
		write("</tr>");
	}

	/**
	 * @param results
	 *            the results to set
	 */
	public final void setResults(List<Employee> results) {
		this.results = results;
	}

	public final void setSection(String section)
	{
		this.section = section;
	}
}
