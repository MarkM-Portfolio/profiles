/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.taglib;

import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.templates.TemplateConfig;
import com.ibm.lconn.profiles.config.templates.TemplateConfig.TemplateEnum;
import com.ibm.lconn.profiles.config.templates.TemplateDataModel;

/**
 * A simple tag that lets us embed FreeMarker generated content.
 */
public class FreemarkerTag extends SimpleTagSupport
{
  private String template;

  private TemplateDataModel dataModel;

  private Map mixinMap;

  public void setDataModel(TemplateDataModel dataModel)
  {
    this.dataModel = dataModel;
  }

  public void setTemplate(String template)
  {
    this.template = template;
  }

  public void setMixinMap(Map mixinMap)
  {
    this.mixinMap = mixinMap;
  }

	public final void doTag() throws JspException, IOException {
		// defect 77590: struts validation may have caught an invalid entry and the form is
		// to be returned. the data model was not retrieved and could be null.
		if (dataModel == null) {
			return;
		}
		TemplateConfig templateConfig = ProfilesConfig.instance().getTemplateConfig();
		TemplateEnum templateEnum = TemplateEnum.valueOf(template);
		if (mixinMap != null) {
			dataModel.mixin(mixinMap);
		}
		try {
			templateConfig.processTemplate(templateEnum, dataModel, getJspContext().getOut());
		}
		catch (Exception e) {
			throw new IOException();
		}
	}
}
