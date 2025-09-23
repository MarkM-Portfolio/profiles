/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.rest.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Person;

import com.ibm.lconn.profiles.test.rest.util.ApiConstants;

public class Tag {
	
	private String term;
	
	private String scheme;
	
	private long frequency;
	
	private long intensityBin;
	
	private long visibilityBin;
	
	private String type;
	
	private List<Colleague> taggers;
	
	public Tag(Category c){
		term = c.getTerm();
		scheme = "";		
		if (c.getScheme() != null) {
			
			scheme = c.getScheme().toASCIIString();
		}
		type = c.getAttributeValue(ApiConstants.SocialNetworking.TYPE);

		// these do not always appear on the tag
		String strValue = c.getAttributeValue(ApiConstants.SocialNetworking.FREQUENCY);
		if (strValue != null && strValue.length() > 0) {
			frequency = Long.valueOf(strValue);
		}
		strValue = c.getAttributeValue(ApiConstants.SocialNetworking.INTENSITY_BIN);
		if (strValue != null && strValue.length() > 0) {
			intensityBin = Long.valueOf(strValue);
		}
		strValue = c.getAttributeValue(ApiConstants.SocialNetworking.VISIBILITY_BIN);
		if (strValue != null && strValue.length() > 0) {
			visibilityBin = Long.valueOf(strValue);
		}
		
		taggers = new ArrayList<Colleague>();
		for (Element contributor : c.getExtensions(ApiConstants.Atom.QN_CONTRIBUTOR)) {
			Person taggerPerson = (Person)contributor;
			taggers.add(new Colleague(taggerPerson));
		}
	}
	
	public Tag(String term) {
		this.term = term;
		this.scheme = "";
		this.type = "general";
		this.taggers = new ArrayList<Colleague>();
	}
	
	public Tag(String term, String scheme) {
		this.term = term;
		this.scheme = scheme;
		this.type = "general";
		if (this.scheme == null) {
			this.scheme = "";
		}
		if (this.scheme != null && this.scheme.length() > 1) {
			int lastIndex = this.scheme.lastIndexOf("/");
			this.type = this.scheme.substring(lastIndex+1, this.scheme.length());
		}			
		this.taggers = new ArrayList<Colleague>();
	}
		
	public Category toCategory(Element parent, Factory factory){
		Category category = factory.newCategory(parent);
		category.setTerm(term);
		if (scheme != null && scheme.length() > 0) {
			category.setScheme(scheme);
		}
		return category;
	}

	public String getScheme() {
		return scheme;
	}
	
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	
	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public long getFrequency() {
		return frequency;
	}

	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}

	public long getIntensityBin() {
		return intensityBin;
	}

	public void setIntensityBin(long intensityBin) {
		this.intensityBin = intensityBin;
	}

	public long getVisibilityBin() {
		return visibilityBin;
	}

	public void setVisibilityBin(long visibilityBin) {
		this.visibilityBin = visibilityBin;
	}

	public List<Colleague> getTaggers() {
		return taggers;
	}
		
	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object o) {
		Tag otherTag = (Tag)o;		
		return this.toString().equals(otherTag.toString());
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[tag=").append(term);
		sb.append(", scheme=").append(scheme);
		sb.append(", type=").append(type);
		sb.append(", taggers=").append(taggers);
		sb.append("]");
		return sb.toString();
	}
}
