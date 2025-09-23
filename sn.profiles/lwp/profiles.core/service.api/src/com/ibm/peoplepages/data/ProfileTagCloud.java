/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.data;

import static java.util.logging.Level.FINER;
import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

public class ProfileTagCloud
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3500582158503804211L;

	private List<ProfileTag> tags;
	private Date updated;
	private String targetKey;
	
	private Map<String, Employee> contributors = Collections.emptyMap();

	private boolean _testing = false; //for debug
	
	public Date getRecordUpdated()
	{
		return updated;
	}
	
	public void setRecordUpdated(Date updated)
	{
		this.updated = updated;
	}
	
	public List<ProfileTag> getTags()
	{
		return tags;
	}

	public void setTags(List<ProfileTag> tags)
	{
		this.tags = tags;
	}
	
	public String getTargetKey()
	{
		return targetKey;
	}
	
	public void setTargetKey(String targetKey)
	{
		this.targetKey = targetKey;
	}
	
	public Map<String, Employee> getContributors()
	{
		return contributors;
	}
	
	public void setContributors(Map<String, Employee> contributors)
	{
		this.contributors = contributors;
	}

	public String toJSONString()
	{
		String     ptcString = null;
		JSONObject ptcJSON   = this.toJSONObject();

		try {
			ptcString = ptcJSON.serialize();
//TODO		ptcJSON.
			if (_testing ) {
				System.out.println("ProfileTagCloud: Tag Cloud To JSON: serialized   " + ptcJSON.size() + " - " + ptcString);

				JSONObject reformulatedJSON = JSONObject.parse(ptcString);
				System.out.println("ProfileTagCloud: Tag Cloud To JSON: reformulated " + reformulatedJSON.size() + " - " + reformulatedJSON.serialize());
				assert(ptcString.equals(reformulatedJSON));
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return ptcString;
	}

	private static final String TARGET_KEY  = "targetKey";
	private static final String UPDATED     = "updated";
	private static final String TAGS        = "tags";
	private static final String CONTRIBUTOR = "contributors";

	private JSONObject toJSONObject()
	{
	    JSONObject ptcElement = null;
	    String theString = "{" + toString() + "}";
	    System.out.println("ProfileTagCloud as String : " + theString);

	    try
		{
            String name = null;
            ptcElement = new JSONObject();

            // targetKey
    		name = TARGET_KEY;
    		ptcElement.put(name, targetKey);

    		// updated
    		name = UPDATED;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    		ptcElement.put(name, sdf.format(updated));
// TODO : 
    		// tags - list
    	    // TODO : iterate over tags, exporting to the JSON object
            JSONArray tagsArray = new JSONArray();
    		for (ProfileTag tag : tags)
    		{
//    			String  ref    = tag.get
//    			String  update = tag.get
//    			Boolean hidden = tag.get

    			JSONObject tagElement = new JSONObject();
//    			tagElement.put("field name 1", value_1);
//    			tagElement.put("field name 2", value_2);
//    			JSONObject labelProp = prop.getLabel().toJSON();
//    			tagElement.put("label", labelProp);

    			tagsArray.add(tagElement);
    		}
    		name = TAGS;
			ptcElement.put(name, tagsArray);
// TODO : 
			// contributors - Map<String, Employee>
			// TODO : iterate over contributors, exporting to the JSON object
			JSONArray contributorArray = new JSONArray();
		    Iterator<Entry<String, Employee>> it = contributors.entrySet().iterator();
		    while (it.hasNext())
		    {
		        Map.Entry<String, Employee> emp = (Map.Entry<String, Employee>)it.next();
		        System.out.println(emp.getKey() + " = " + emp.getValue());
		        //
//				String  ref    = emp.get
//				JSONObject contributor = new JSONObject();
//				contributorArray.add(contributor);
		    }
			name = CONTRIBUTOR;
			ptcElement.put(name, contributorArray);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return ptcElement;
	}

// TODO - implement
	public ProfileTagCloud fromJSONString(String tagCloudJSONString)
	{
		// reformulate the ProfileTagCloud object from the JSON string
		ProfileTagCloud ptc = new ProfileTagCloud();
		JSONObject ptcJSON = null;
		try {
			ptcJSON = JSONObject.parse(tagCloudJSONString);
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
		Object subString  = null;
		subString = ptcJSON.get(TARGET_KEY);
		String targetKey = extractStringField(subString);
		subString = ptcJSON.get(UPDATED);
		Date updated = extractDateField(subString);
//TODO implement extracting PTC elements from JSON string
		subString = ptcJSON.get(TAGS);
		subString = ptcJSON.get(CONTRIBUTOR);
		return ptc;
	}

	private Date extractDateField(Object subString)
	{
		Date retVal = null;
//TODO implement
		return retVal;
	}

	private String extractStringField(Object subString)
	{
		String retVal = null;
		if (subString instanceof String ) {
			String empDataStr = (String) subString;
//			if (logger.isLoggable(FINER))
//				logger.log(FINER, "getting employeeData from JSON object (empDataStr) : " + empDataStr);
			try {
				JSONObject empData = JSONObject.parse(empDataStr.trim());
				if (empData instanceof JSONObject )
				{
//					if (logger.isLoggable(FINER))
//						logger.log(FINER, "Photo Sync Events processing onBehalfof (email) to string");
					try {
//						onBehalfOf = (String) empData.get("email");
//						if (logger.isLoggable(FINER)) {
//							if (StringUtils.isEmpty(onBehalfOf)) {
//								logger.log(FINER, "onBehalfOf (email) is empty " + onBehalfOf );
//						}
//						else
//							logger.log(FINER, "onBehalfOf (email) is a " + onBehalfOf.getClass().toString() + " object " + onBehalfOf.toString());
//						}
					}
					catch (Throwable ex) {
	//					logger.log(Level.INFO, "Photo Sync Events exception while processing onBehalfof (email) : " + ex.getMessage());
	//					if (logger.isLoggable(FINER)) {
	//						ex.printStackTrace();
	//					}
					}
				}
			}
			catch (Throwable ex) {
	//			logger.log(Level.INFO, "Photo Sync Events exception while getting employeeData from JSON object : " + ex.getMessage());
	//			if (logger.isLoggable(FINER)) {
	//				ex.printStackTrace();
	//			}
			}
		}
		return retVal;
	}

}
