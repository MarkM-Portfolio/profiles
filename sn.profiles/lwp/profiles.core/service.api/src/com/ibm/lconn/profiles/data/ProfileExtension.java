/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig;
import com.ibm.lconn.profiles.config.types.ProfileTypeConstants;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

public class ProfileExtension extends AbstractDataObject<ProfileExtension>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5276029106280657821L;

	private boolean maskNull = false;
	private Date recordUpdated = null;
	
	private String key = null;
	
	// set by db retrieval for code optimization
	private String profileType;	
	
	private String extKey = null;
	private String propertyId = null;
	
	private String name = null;	
	private String dataType;
	private boolean supportLabel = false;
	
	private String value = null;
	private byte[] extendedValue = null;
	
	private int updateSequence = -1;
	
	public ProfileExtension() {}
	
	public Date getRecordUpdated() {
		return recordUpdated;
	}
	
	public void setRecordUpdated(Date updated) {
		recordUpdated = updated;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getProfileType() {
		if (StringUtils.isEmpty(profileType)){
			return ProfileTypeConstants.DEFAULT;
		}
		return profileType;
	}

	public void setProfileType(String profileType) {
		this.profileType = profileType;
	}

	public String getName() {
		//if the flag to support labels is false, then make sure no label is passed to the UI.
		if (this.supportLabel == false) {
			return null;
		} else {
			return name;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	// used by db
	public boolean getSupportLabel(){
		return supportLabel;
	}
	
	// used by db
	public void setSupportLabel(boolean supportLabel) {
		this.supportLabel = supportLabel;
	}
    
	public boolean isSupportLabel(){
		return supportLabel;
	}

	public String getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if (value != null){
			this.extendedValue = null;
			value = value.trim();
		}
		this.value = value;
	}
	
	public byte[] getExtendedValue() {
		return extendedValue;
	}
	
	public void setExtendedValue(byte[] extendedValue) {
		if (extendedValue != null){
			this.value = null;
		}
		this.extendedValue = extendedValue;
	}
	
	/**
	 * Convenience mechanism for all types except for file, which are representable by strings.
	 * 
	 * @return
	 */
	public String getStringValue()
	{
		ExtensionAttributeConfig eac = DMConfig.instance().getExtensionAttributeConfig().get(getPropertyId());
		
		if (eac != null)
		{
			switch (eac.getExtensionType())
			{
				case SIMPLE:
					return getValue();
				case XMLFILE:
				case RICHTEXT:
					byte[] extValue = getExtendedValue();
					if (extValue != null)
					{
						try {
							return new String(extValue, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							throw new ProfilesRuntimeException("Unreachable block",e);
						}
					}
			}
		}
		
		return null;
	}
	
	/**
	 * Convenience mechanism for all types except for file, which are representable by strings.
	 * 
	 * @param value
	 */
	public void setStringValue(String value)
	{		
		ExtensionAttributeConfig eac = DMConfig.instance().getExtensionAttributeConfig().get(getPropertyId());
				
		if (eac != null)
		{
			if (null != value)
				value = value.trim();
			switch (eac.getExtensionType())
			{
				case SIMPLE:
					setValue(value);
					break;
				case XMLFILE:
				case RICHTEXT:
					try {
						setExtendedValue(value == null ? null : value.getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						throw new ProfilesRuntimeException("Unreachable block",e);
					}
					break;
			}
		}
	}
	
	/**
	 * True if no actual value in DB corresponding to record
	 * @return
	 */
	public boolean isMaskNull() {
		return maskNull;
	}
	
	public void setMaskNull(boolean value) {
		this.maskNull = value;
	}

	/**
	 * @return the updateSequence
	 */
	public final int getUpdateSequence() {
		return updateSequence;
	}

	/**
	 * @param updateSequence the updateSequence to set
	 */
	public final void setUpdateSequence(int updateSequence) {
		this.updateSequence = updateSequence;
	}

	/**
	 * @return the extKey
	 */
	public final String getExtKey() {
		return extKey;
	}

	/**
	 * @param extKey the extKey to set
	 */
	public final void setExtKey(String extKey) {
		this.extKey = extKey;
	}

	public final String toString() {
		StringBuilder sb = new StringBuilder();
		if (null != key)
			sb.append(" key=" + key);
		if (null != propertyId)
			sb.append(" propertyId=" + propertyId);
		if (null != extendedValue)
			sb.append(" extendedValue=" + new String(extendedValue));
		if (null != recordUpdated)
			sb.append(" updated=" + recordUpdated);
		if (null != name)
			sb.append(" name=" + name);
		if (null != value)
			sb.append(" value=" + value);
		if (null != dataType)
			sb.append(" dataType=" + dataType);
		if (null != extKey)
			sb.append(" extKey=" + extKey);
		if (null != profileType)
			sb.append("profileType=" + profileType);
		if (maskNull)
			sb.append(" maskNull=" + maskNull);
		if (supportLabel)
			sb.append(" supportLabel =" + supportLabel);
		if (-1 != updateSequence)
			sb.append(" updateSequence =" + updateSequence);
		return sb.toString();
	}
}
