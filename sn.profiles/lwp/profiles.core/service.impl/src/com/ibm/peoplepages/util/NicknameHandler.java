/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.util;

import java.util.Hashtable;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

// Simple parser, takes in the nickname xml file and parses it into a Hashtable
public class NicknameHandler extends DefaultHandler {
    
    private Hashtable<String, Vector<String>> names = new Hashtable<String, Vector<String>>();
    private String name = null;
    private Vector<String> nickNames = null;
    
    public void startElement(String uri, String localName, String qName, Attributes attrs) {
        if (qName.equals("nameEntry")) {
            name = attrs.getValue(0);
            nickNames = new Vector<String>();
        }
        else if (qName.equals("nickNameEntry")) {
            nickNames.add(attrs.getValue(0));
        }
    }
    
    public void endElement(String uri, String localName, String qName)
    {
        if (qName.equals("nameEntry")) {
            names.put(name, nickNames);
        }
    }
    
    public Hashtable<String, Vector<String>> getNames() {
        return names;
    }
}
