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
package com.ibm.lconn.profiles.api.tdi.data;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.lconn.profiles.data.GivenName;
import com.ibm.lconn.profiles.data.ProfileAttributes;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.Surname;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;
import com.ibm.peoplepages.data.Employee;

public class ProfileEntry{
	
	private final Entry entry;
	
	public ProfileEntry(ProfileDescriptor desc){	
		Entry entry = new Entry();
		Employee profile = desc.getProfile();
		
		for(ProfileAttributes.Attribute attribu : ProfileAttributes.getAll()){
			String key = attribu.getAttrId();
			Object value = profile.get(key);
			if (value != null) {
				if (attribu.isExtension()) {
					
					ProfileExtension pe = (ProfileExtension) value;
					if((pe.getStringValue()!=null)&& (!pe.getStringValue().equals(""))){
						Attribute attri = 
							entry.newAttribute("_extAttrs_" + pe.getPropertyId());
						
						String nameString = "name:" + pe.getName();
						String dataTypeString = "dataType:" + pe.getDataType();
						String valueString = "value:" + pe.getStringValue();

						attri.addValue(nameString);
						attri.addValue(dataTypeString);
						attri.addValue(valueString);
					}
				} else {
					entry.addAttributeValue(key, value);
				}				
			}
		}	

		for (GivenName gn : desc.getGivenNames()) {
			entry.addAttributeValue("givenNames", gn.getName());
		}
		for (Surname sn : desc.getSurnames()) {
			entry.addAttributeValue("surnames", sn.getName());
		}
		for (String login: desc.getLogins()) {
			entry.addAttributeValue("logins", login);
		}
		entry.setAttribute("sys_usrState", profile.getState().getName());
		
		this.entry = entry;
	}
	
	public Entry getEntry(){
		return entry;
	}
}
