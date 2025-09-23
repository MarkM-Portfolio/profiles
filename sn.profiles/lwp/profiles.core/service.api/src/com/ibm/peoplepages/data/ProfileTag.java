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
package com.ibm.peoplepages.data;

import com.ibm.lconn.core.util.tags.AbstractTagCount;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;


public class ProfileTag extends java.util.HashMap<String,Object> implements AbstractTagCount
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5772061438700414287L;
	
	private static final String[] EMPTY_SOURCE_KEYS = {};
	
	public ProfileTag()
	{
		put("frequency", 1);
		put("visibilityBin", -1);
		put("intensityBin", -1);
		put("sourceKeys", EMPTY_SOURCE_KEYS);
		put("type", TagConfig.DEFAULT_TYPE);
	}

	/**
	 * @return Returns the type for this tag.
	 */
	public String getType() 
	{
		return (String)get("type");
	}
	
	/**
	 * Set the type for this tag
	 * @param namespace
	 */
	public void setType(String type)
	{
		put("type", type);
	}
	
	/**
	 * @return Returns the source employee key.
	 */
	public String getSourceKey()
	{
		 return (String)get(PeoplePagesServiceConstants.SOURCE_KEY);
	}

	/**
	 * @param sourceKey The source employee's internal key.
	 */
	public void setSourceKey(String sourceKey)
	{
		put(PeoplePagesServiceConstants.SOURCE_KEY, sourceKey);
	}
	
	/**
	 * @return Returns the source employee key.
	 */
	public String[] getSourceKeys()
	{
		return (String[])get(PeoplePagesServiceConstants.SOURCE_KEYS);
	}

	/**
	 * @param sourceKey The source employee's internal key.
	 */
	public void setSourceKeys(String[] sourceKey)
	{
		put(PeoplePagesServiceConstants.SOURCE_KEYS, sourceKey);
	}

	/**
	 * @return Returns the target employee key.
	 */
	public String getTargetKey()
	{
		return (String)get(PeoplePagesServiceConstants.TARGET_KEY);
	}

	/**
	 * @param targetKey The target employee's internal key.
	 */
	public void setTargetKey(String targetKey)
	{
		put(PeoplePagesServiceConstants.TARGET_KEY, targetKey);
	}

	/**
	 * @return Returns the tag.
	 */
	public String getTag()
	{
		return (String)get(PeoplePagesServiceConstants.TAG);
	}

	/**
	 * @param tag The tag to set.
	 */
	public void setTag(String tag)
	{
		put(PeoplePagesServiceConstants.TAG, tag);
	}

	/**
	 * @return Returns the tagId.
	 */
	public String getTagId()
	{
		return (String)get(PeoplePagesServiceConstants.TAGID);
	}

	/**
	 * @param tagId The tagId to set.
	 */
	public void setTagId(String tagId)
	{
		put(PeoplePagesServiceConstants.TAGID, tagId);
	}

	public String getTenantKey(){
		return (String)get(PeoplePagesServiceConstants.TENANT_KEY);
	}

	public void setTenantKey(String tenantKey){
		put(PeoplePagesServiceConstants.TENANT_KEY,tenantKey);
	}

	/**
	 *  Check to see whether this tag is tagged by others
	 *  or the user's own tag. Simply check whether the
	 *  sourceKey and targetKey are the same, or sourceKey is null
	 */
	public boolean isTagged() {
		return ( getSourceKey() == null || (!getTargetKey().equals( getSourceKey() ) ) );
	}

	/**
	 * Get and set the frequency for this tag
	 *
	 */
	public int getFrequency() {
		return (Integer) get("frequency");
	}

	public void setFrequency(int freq) {
		put("frequency",freq);
	}
	
	public int getCount() {
		return getFrequency();
	}

	public void setIntensityBin(int intensityBin) {
		put("intensityBin", intensityBin);
	}

	public int getIntensityBin() {
		return (Integer) get("intensityBin");
	}
	
	public void setVisibilityBin(int visibilityBin) 
	{
		put("visibilityBin", visibilityBin);
	}
	
	public int getVisibilityBin() {
		return (Integer) get("visibilityBin");
	}

}
