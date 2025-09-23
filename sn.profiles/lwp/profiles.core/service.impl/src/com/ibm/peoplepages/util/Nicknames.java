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

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class Nicknames { //extends Hashtable<String, Vector<String>> {
    private static final long serialVersionUID = 1L;
//    private static final String namesFile = "nicknames.xml";
    private static Hashtable<String, Vector<String>> table = new Hashtable<String, Vector<String>>();
    
    // Not happy about this
//    static final String clef = "lk#\0d23\n+\rs$af\ta";
        
    private Nicknames()
    throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, 
    NoSuchProviderException, InvalidKeyException, BadPaddingException,
    IllegalBlockSizeException, ParserConfigurationException, SAXException
    {

        if (table.isEmpty()) {
            table = retrieve();
        }
    }
    
    // Return the set of nicknames associated with "name"
    public static List<String> getNicknames(String name) {
       try {
           Nicknames n = new Nicknames();
           return (List<String>)n.get(name.toUpperCase());
       } catch (Exception e) {
           return null;
       }
    }
    
    private List<String> get(String name) {
        return table.get(name);
    }
    
    // Read in the encrypted xml, decrypt it, and parse it into a Hashtable of
    // names and nicknames
    private static Hashtable<String, Vector<String>> retrieve()
    throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, 
    NoSuchProviderException, InvalidKeyException, BadPaddingException,
    IllegalBlockSizeException, ParserConfigurationException, SAXException
    {
        SecretKey key = getClef();

        Cipher cipher = Cipher.getInstance("AES", "IBMJCE");
        cipher.init(Cipher.DECRYPT_MODE, key);
        
        InputStream is = Nicknames.class.getResource("nicknames.xml").openStream();
        CipherInputStream encrypted = new CipherInputStream(is, cipher);
              
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        NicknameHandler nhdr = new NicknameHandler();
        parser.parse(encrypted, nhdr);
        encrypted.close();
        
        return nhdr.getNames();
    }
   
    private static SecretKeySpec getClef() 
    {
        return new SecretKeySpec("lk#\0d23\n+\rs$af\ta".getBytes(), "AES");
    }
}
