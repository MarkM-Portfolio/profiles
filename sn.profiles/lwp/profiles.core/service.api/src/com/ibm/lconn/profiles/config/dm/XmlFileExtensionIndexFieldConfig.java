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
package com.ibm.lconn.profiles.config.dm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpression;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.ibm.lconn.profiles.config.AbstractConfigObject;

/**
 * @author Joseph Lu
 *
 */
public class XmlFileExtensionIndexFieldConfig extends AbstractConfigObject {
	
    /**
     * 
     */
    private static final long serialVersionUID = 3151310261440359398L;

    public static class IndexFieldConfig {
	private final String fieldName;
	private final String expression;
	private final String fieldType;
	private XPathExpression xpath;
	
	protected IndexFieldConfig(HierarchicalConfiguration ifConfig) {
	    this.fieldName = ifConfig.getString("[@fieldName]");
	    this.expression = ifConfig.getString("[@fieldExpr]");
	    this.fieldType = ifConfig.getString("[@fieldType]");
	}
	
	/**
	 * @return the fieldName
	 */
	public final String getFieldName() {
	    return fieldName;
	}

	/**
	 * @return the expression string
	 */
	public final String getExpression() {
	    return expression;
	}

	/**
	 * @return the field type
	 */
	public final String getFieldType() {
	    return fieldType;
	}

	/**
	 * set the xpath expression for this field
	 */
	public void setXPathExpression(XPathExpression expr) {
	    xpath = expr;
	}

	/**
	 * @return the xpath expression for this field
	 */
	public XPathExpression getXPathExpression() {
	    return xpath;
	}
    }
    
    private final List<IndexFieldConfig> indexConfigs;
    
    public XmlFileExtensionIndexFieldConfig(HierarchicalConfiguration fieldsConfig) {
	int maxCs = fieldsConfig.getMaxIndex("indexField");
	List<IndexFieldConfig> fields = new ArrayList<IndexFieldConfig>(maxCs+1);
	
	for (int i = 0; i <= maxCs; i++) {
	    IndexFieldConfig ifConfig = new IndexFieldConfig((HierarchicalConfiguration) fieldsConfig.subset("indexField(" + i + ")"));
	    fields.add(ifConfig);
	}
	
	this.indexConfigs = Collections.unmodifiableList(fields);
    }
    
    /**
     * @return the list of index fields
     */
    public final List<IndexFieldConfig> getIndexFields() {
	return indexConfigs;
    }
}
