/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.types;

import java.io.Serializable;

public class PropertyImpl implements Property, Serializable {

	private static final long serialVersionUID = 916531675145035343L;

	private String ref;

	private Updatability updatability;

	private boolean inherited;

	private boolean hidden = false;

	private boolean extension;

	private boolean richText = false;
	
	private boolean stringText = true;

	private boolean fullTextIndexed = true;
	
	private Label label;
	
	private ExtensionType extensionType;
	
	private MapToNameTableEnum mapToNameTable;

	public PropertyImpl() {
	}

	public void setFullTextIndexed(boolean fullTextIndexed) {
		this.fullTextIndexed = fullTextIndexed;
	}

	public boolean isFullTextIndexed() {
		return fullTextIndexed;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public Updatability getUpdatability() {
		return updatability;
	}

	public void setUpdatability(Updatability updatability) {
		this.updatability = updatability;
	}

	public boolean isInherited() {
		return inherited;
	}

	public void setInherited(boolean inherited) {
		this.inherited = inherited;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isExtension() {
		return extension;
	}

	public void setExtension(boolean extension) {
		this.extension = extension;
	}

	public ExtensionType getExtensionType() {
		return extensionType;
	}

	public void setExtensionType(ExtensionType extensionType) {
		this.extensionType = extensionType;
		if (extensionType != null){
			if (ExtensionType.RICHTEXT.equals(extensionType)) {
				setRichText(true);
				this.stringText = false;
			}
			else if (ExtensionType.SIMPLE.equals(extensionType)){
				setRichText(false);
				this.stringText = true;
			}
		}
	}

	// doesn't seem this needs to be public? can be set via setExtensionType 
	public void setRichText(boolean richText) {
		this.richText = richText;
	}

	public boolean isRichText() {
		return richText;
	}
	
	public boolean isStringText(){
		return this.stringText;
	}

	public void setLabel(Label label){
		this.label = label;
	}
	
	public Label getLabel(){
		return label;
	}

	public void setLabel(String lable, Updatability updatability){
		this.label = new Label(lable,updatability);
	}

	public boolean isLabel(){
		return label != null;
	}

    public MapToNameTableEnum getMapToNameTable()
    {
      return mapToNameTable;
    }
	  
    public void setMapToNameTable(MapToNameTableEnum name)
    {
      this.mapToNameTable = name;
    }
    
	@Override
	public Object clone() throws CloneNotSupportedException {
		PropertyImpl p = new PropertyImpl();
		cloneValues(p);
		return p;
	}

	protected void cloneValues(PropertyImpl p) {
		p.setExtension(extension);
		p.setExtensionType(extensionType);
		p.setHidden(hidden);
		p.setInherited(inherited);
		p.setRef(ref);
		p.setRichText(richText);
		p.setUpdatability(updatability);
		p.setFullTextIndexed(fullTextIndexed);
		p.setMapToNameTable(mapToNameTable);
		p.setLabel(label);
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder("[");
		sb.append("ref:").append(getRef());
		sb.append(", extension:").append(isExtension());
		if (isExtension())
		{
			sb.append(", extensionType:").append(getExtensionType());
		}
		sb.append(", hidden:").append(isHidden());
		sb.append(", inherited:").append(isInherited());
		sb.append(", richText:").append(isRichText());
		sb.append(", updatability:").append(getUpdatability());
		sb.append(", fullTextIndexed:").append(isFullTextIndexed());
		if (mapToNameTable != null)
		{
		  sb.append(", mapToNameTable: ").append(mapToNameTable.getValue());
		}
		sb.append("]");
		return sb.toString();
	}

}
