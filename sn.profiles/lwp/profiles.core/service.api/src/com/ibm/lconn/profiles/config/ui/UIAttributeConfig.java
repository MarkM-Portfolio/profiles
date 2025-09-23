/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.core.web.util.resourcebundle.UILabelConfig;
import com.ibm.lconn.profiles.config.AbstractNodeBasedConfig;
import com.ibm.peoplepages.data.Employee;

/**
 * @author ahernm@us.ibm.com
 *
 */
public final class UIAttributeConfig extends AbstractNodeBasedConfig 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6989220577331838473L;
	
	/**
	 * Constant attribute id value indicating for an HTML attribute
	 */
	public static final String HTML_ATTR_ID = "UIAttributeConfig.HTML_ATTR";
	
	final private String nodeName;
	final private String attributeId;
	final private boolean editable;
	final private boolean extensionAttribute; 
	final private boolean html;
	final private UILabelConfig label;
	final private String labelKeyBase;
	
	
	public UIAttributeConfig(HierarchicalConfiguration configuration, String labelKeyBase) 
	{
		super(configuration);
		
		this.nodeName = configuration.getRoot().getName();
		this.labelKeyBase = labelKeyBase;
		
		if ("html".equals(nodeName)) {
			this.html = true;
			this.editable = false;
			this.attributeId = HTML_ATTR_ID;
			this.extensionAttribute = false;
		} else if ("extensionAttribute".equals(nodeName)) {
			this.extensionAttribute = true;
			this.attributeId = Employee.getAttributeIdForExtensionId(configuration.getString("[@extensionIdRef]"));
			this.editable = configuration.getBoolean("[@editable]",false);
			this.html = false;
		} else {
			this.extensionAttribute = false;
			this.attributeId = String.valueOf(configuration.getRoot().getValue());
			this.html = false;
			
			if ("attribute".equals(nodeName)) 	{
				this.editable = false;
			} else /* if ("editableAttribute".equals(nodeName)) */ {
				this.editable = true;
			}
		}
		
		String bidref = configuration.getString("[@bundleIdRef]");
		String labelKey = configuration.getString("[@labelKey]");
		
		if (StringUtils.isNotBlank(labelKey)) {
			this.label = new UILabelConfig(bidref, labelKey);
		} else if (labelKeyBase != null && !this.html && !this.photo){
			this.label = new UILabelConfig(null, labelKeyBase + "." + attributeId);
		} else {
			this.label = null;
		}
	}
	
	public UIAttributeConfig(UIAttributeConfig sourceConfig, String attributeId) 
	{
		super(sourceConfig.getConfiguration());
		
		this.labelKeyBase = sourceConfig.labelKeyBase;
		this.nodeName = sourceConfig.getNodeName();
		this.editable = sourceConfig.isEditable();
		this.extensionAttribute = sourceConfig.isExtensionAttribute();
		this.attributeId = attributeId;
		this.html = sourceConfig.isHtml();
		this.label = sourceConfig.getLabel();
	}

	public final String getNodeName() {
		return nodeName;
	}
	
	public final String getAttributeId() {
		return attributeId;
	}
	
	public final boolean isEditable() {
		return editable;
	}

	private final boolean disabled = configuration.getBoolean("[@disabled]",false);
	public final boolean isDisabled() {
		return disabled;
	}

	public final boolean isExtensionAttribute() {
		return extensionAttribute;
	}
	
	private final boolean showLabel = configuration.getBoolean("[@showLabel]",true);
	public final boolean getIsShowLabel() {
		return showLabel;
	}

	private final boolean richText = configuration.getBoolean("[@richtext]",false);
	public final boolean isRichText() {
		return richText;
	}
	
	final private boolean multiline = !richText && configuration.getBoolean("[@multiline]",false);
	public final boolean isMultiline() {
		return multiline;
	}

	private final String appendHTML = configuration.getString("[@appendHtml]");
	public final String getAppendHTML() {
		return appendHTML;
	}

	private final String prependHTML = configuration.getString("[@prependHtml]");
	public final String getPrependHTML() {
		return prependHTML;
	}

	private final boolean hcard = configuration.getBoolean("[@hcard]", false);
	public final boolean getIsHcard() {
		return hcard;
	}

	private final boolean hideIfEmpty = configuration.getBoolean("[@hideIfEmpty]", false);
	public final boolean getIsHideIfEmpty() {
		return hideIfEmpty;
	}

	private static final String[] EMPTY_ARRAY = {};
	
	private final boolean hideFromForm = configuration.getBoolean("[@hideOnSearchUIForm]", false);
	public final boolean getIsHideFromForm() {
		return hideFromForm;
	}

	private final boolean link = configuration.getBoolean("[@link]", false);
	public final boolean getIsLink() {
		return link;
	}

	private final boolean sametimeLink = configuration.getBoolean("[@sametimeLink]", false);
	public final boolean getIsSametimeLink() {
		return sametimeLink;
	}

	private final String email = configuration.getString("[@email]");
	public final String getEmail() {
		return email;
	}
	
	private final boolean isEmail = Boolean.parseBoolean(email);
	public final boolean getIsEmail() {
		return isEmail;
	}

	private final String uid = configuration.getString("[@uid]");
	public final String getUid() {
		return uid;
	}
	
	private final String userid = configuration.getString("[@userid]");
	public final String getUserid() {
		return userid;
	}
	
	private final boolean photo = configuration.getBoolean("[@photo]", false);
	public final boolean getIsPhoto() {
		return photo;
	}
	
	private final boolean blogUrl = configuration.getBoolean("[@blogUrl]", false);
	public boolean getIsBlogUrl(){
		return blogUrl;
	}
	
	/**
	 * @return the html
	 */
	public final boolean isHtml() {
		return html;
	}
	
	/**
	 * @return the label
	 */
	public final UILabelConfig getLabel() {
		return label;
	}

	/**
	 * Utility method to instantiate a list of UIAttributes that are children to the current node
	 * 
	 * @param configuration
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final List<UIAttributeConfig> initUIAttributeChildren(HierarchicalConfiguration configuration, String labelKeyBase) {
		List<UIAttributeConfig> attributes = new ArrayList<UIAttributeConfig>();
		
		List<HierarchicalConfiguration.Node> children = configuration.getRoot().getChildren();
		for (HierarchicalConfiguration.Node child : children) {
			if (uiAttrTypes.contains(child.getName())) {
				HierarchicalConfiguration attConfig = new HierarchicalConfiguration();
				attConfig.setRoot(child);
				attributes.add(new UIAttributeConfig(attConfig, labelKeyBase));
			}			
		}
		
		return attributes;
	}
	
	private static final List<String> uiAttrTypes = Arrays.asList(new String[]{"editableAttribute", "attribute",  "extensionAttribute", "html"});
}
