/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.types;


public interface Property
{
  /**
   * A reference to a standard Profiles attribute or a globally defined extension attribute.
   * 
   * @return
   */
  public String getRef();

  /**
   * Indicates under what circumstances the value of this property MAY be updated.
   * 
   * @return
   */
  public Updatability getUpdatability();

  /**
   * Indicates whether the property is inherited from the parent-type or it is explicitly defined for this profile-type.
   * 
   * @return
   */
  public boolean isInherited();

  /**
   * Indicates if the value is hidden when rendering the vCard and hCard microformats.
   * 
   * @return
   */
  public boolean isHidden();

  /**
   * Indicates if the value is rich text.  If ACF is enabled, the attribute will be filtered.
   * @return
   */
  public boolean isRichText();
  
  /**
   * Indicates if the value is (simple) string text.
   */
  public boolean isStringText();
  
  /**
   * Indicates if this property has a label
   */
  public boolean isLabel();
  
  /**
   * Default label for this property.
   * @return
   */
  public Label getLabel();

  /**
   * Indicates if the property is an extension field.
   * 
   * @return
   */
  public boolean isExtension();
  
  /** 
   * @return extension type or <code>null</code> if not an extension property
   */
  public ExtensionType getExtensionType();
  
  /**
   * Indicates if this property value should be added to the search index.
   * @return
   */
  public boolean isFullTextIndexed();
  
  /**
   * Return the mapped <code>MapToNameTableEnum</code> for this property if its value should be synched in the specified name table for simple name search, null if not specified
   * @return
   */
  public MapToNameTableEnum getMapToNameTable();

  
}
