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
package com.ibm.lconn.profiles.api.actions;

import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.parser.ParseException;

import com.ibm.lconn.profiles.api.actions.AtomConstants;
import com.ibm.lconn.profiles.api.actions.AtomParser3;
import com.ibm.lconn.profiles.data.ProfileDescriptor;

/**
 * This is a temporary hack so as not to impact the main Profiles code. It should be rolled back into
 * AtomParser3 as an option.
 * 
 * Update : Not currently used as the main stream parsing was ok - can be removed
 * 
 * @author blooby
 *
 */
public class AtomParser3Ext extends AtomParser3
{

	/**
	 * Parse a profile feed without any checks for the existence of the Profile in question
	 * @param pd
	 * @param is
	 * @throws ParseException
	 */
	public void parseNewProfile(ProfileDescriptor pd, InputStream is) throws ParseException
	{
		Document<?> document = Abdera.getNewParser().parse(is);
		Element root = (Element) document.getRoot();

		Element el = null; 
		
		if ((el = root.getFirstChild(new QName(AtomConstants.NS_ATOM, "content"))) == null) {
			el = root.getFirstChild();
			el = el.getNextSibling(new QName(AtomConstants.NS_ATOM, "content"));
		}
			
		if (el == null) return;
		
		Element profile = el.getFirstChild();
		
		if (profile == null) return;
		el = profile.getFirstChild();
		if (el == null) return;
		
		Element entry = el.getFirstChild();
		if (entry == null) return;
		while (entry != null) {
			parseEntry(pd, entry);
			entry = entry.getNextSibling();
		}
	}
	
}
