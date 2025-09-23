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
package com.ibm.peoplepages.nicknames.test;

import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;

import com.ibm.peoplepages.nicknames.util.NameEncryptor;
import com.ibm.peoplepages.util.Nicknames;

// JUnit tests for the nickname processing
public class NicknameTest extends TestCase {
    
    String inFile = null;
    String xmlFileName = null;
    //String encryptedFileName = null;

    protected void setUp() throws Exception {
        super.setUp();
        URL url = this.getClass().getResource("NicknameTest.class");
        String sourcePath = url.getFile();
        String urlPath = sourcePath.substring(0, sourcePath.lastIndexOf("/"));

        String encryptedPath = Nicknames.class.getResource("Nicknames.class").getFile();
        encryptedPath = encryptedPath.substring(0, encryptedPath.lastIndexOf("/"));
        inFile =  urlPath + "/nicknames.txt";
        xmlFileName = encryptedPath + "/../../../../nicknames.xml";
        System.out.println("encryptedPath: " + encryptedPath);
        //encryptedFileName = encryptedPath + "/codeNick.xml";
    }
 
    public void testEncrypt() {        
        try {
            NameEncryptor encryptor = new NameEncryptor();
            encryptor.setInFile(inFile);
            encryptor.setOutFile(xmlFileName);
            encryptor.execute();
            assertTrue(true);
        } catch (BuildException bexp) {
            fail(bexp.getMessage());
        }
    }
    
    public void testGetName() {
        try {
            Vector<String> testnames = new Vector<String>();
            testnames.add("Will");
            testnames.add("Billy");
            testnames.add("Willy");
            testnames.add("Bill");
            testnames.add("Willi");
            
            List<String> names = Nicknames.getNicknames("William");
            
            assertTrue(names != null);
            Iterator<String> n = names.iterator();
            assertTrue(testnames.size() == names.size());
            String obj = null;
            for (obj = (String)n.next(); n.hasNext(); obj = (String)n.next())
                assertTrue(testnames.contains(obj));
            
        } catch (Exception exp) {
            System.out.println(exp.getMessage());
            fail(exp.getMessage());
        }
    }
}
