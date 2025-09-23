/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.data;

import com.ibm.peoplepages.data.ProfileTag;

/**
 * A helpful bean to pass around the type associated with a tag object.
 * 
 * This is intended to replace those methods that had just taken a List<String> as input or output
 * prior to introduction of type scoping.
 * 
 */
public class Tag implements Comparable<Tag> {

	private String tag;
	private String type;
	
	public Tag() {
		
	}
	
	public Tag(ProfileTag profileTag) {
		this.tag = profileTag.getTag();
		this.type = profileTag.getType();
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object arg0) {	
		return this.toString().equals( ((Tag)arg0).toString());	
	}

	@Override
	public int hashCode() {
		return 31 * (31 * tag.hashCode()) + (31 * type.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[tag= ");
		sb.append(tag);
		sb.append("type= ");
		sb.append(type);
		sb.append("]");
		return sb.toString();
	}

	//@Override
	public int compareTo(Tag arg0) {
		int typeCompare = type.compareTo(arg0.getType());
		if (typeCompare == 0) {
			return tag.compareTo(arg0.getTag());
		}
		return typeCompare;
	}
	
	
}
