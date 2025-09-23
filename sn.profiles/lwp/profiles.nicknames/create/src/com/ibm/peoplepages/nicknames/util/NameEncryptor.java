/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.nicknames.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class NameEncryptor extends Task {
    
    private String inFile = null;
    private String outFile = null;
    private String interimFile = "interim.xml";
    
    public void setInFile(String value) {
        inFile = value;
    }
    
    public void setOutFile(String value) {
        outFile = value;
    }
    
    // Not happy about this
    static final String clef = "lk#\0d23\n+\rs$af\ta";
    
    public void execute()
    throws BuildException 
    {
        try {
            transform(inFile, interimFile);
            encrypt(interimFile, outFile);
            File f = new File(interimFile);
            f.delete();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    // Take in the csv file and turn it into xml
    // This should be used on Connections' end only - we should 
    // only ship the encrypted xml file
    private static void transform(String inputFile, String outFile)
        throws FileNotFoundException, IOException
    {
        FileWriter transformed = new FileWriter(outFile);
        BufferedReader inRdr = new BufferedReader(new FileReader(inputFile));
        String line = null;
        String name = null;
        createCopyright(transformed);
        
        // Brute force transformation because it's a pretty simple file
        transformed.write("<nameData>\n");
        boolean first = true;
        while (inRdr.ready()) {
            line = inRdr.readLine();
            String values[] = line.split(",");
            String nextName = values[0].substring(1, values[0].length()-1);
            String nickName = values[1].substring(1, values[1].length()-1);
            if (!nextName.equals(name)) {
                // we're on a new name
                name = nextName;
                if (!first) {
                    endNameEntry(transformed);
                }
                createNameEntry(name, transformed);

                first = false;
            }
            addNickName(nickName, transformed);
        }
        endNameEntry(transformed);
        transformed.write("</nameData>\n");
        transformed.close();
    }
    
    // Take in the transformed xml file and write it out encrypted
    // This resultant file is what should be shipped
    private static void encrypt(String inFile, String outFileName) 
    throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, 
    NoSuchProviderException, InvalidKeyException, BadPaddingException,
    IllegalBlockSizeException
    {
        BufferedReader inRdr = new BufferedReader(new FileReader(inFile));
        SecretKey key = getKey();
        
        Cipher cipher = Cipher.getInstance("AES", "IBMJCE");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        CipherOutputStream encrypted = new CipherOutputStream(new FileOutputStream(outFileName), cipher);
        
        while (inRdr.ready()) {
            String line = inRdr.readLine();
            byte bLine[] = line.getBytes();
            encrypted.write(bLine);
            encrypted.write('\n');
        }
        
        encrypted.close();
        inRdr.close();
    }
    
    private static SecretKeySpec getKey() 
    {
        return new SecretKeySpec(clef.getBytes(), "AES");
    }
    
    private static void createNameEntry(String name, Writer output) 
    throws IOException
    {
        output.write("\t<nameEntry name=\"" + name + "\">\n");
    }
    
    private static void endNameEntry(Writer output) 
    throws IOException
    {
        output.write("\t</nameEntry>\n");
    }
    
    private static void addNickName(String name, Writer output) 
    throws IOException {
        String xformName = name.charAt(0) + name.substring(1).toLowerCase(); 
        output.write("\t\t<nickNameEntry name=\"" + xformName + "\"/>\n");
    }
    
    private static void createCopyright(Writer output) 
    throws IOException {
        output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        output.write("<!-- ***************************************************************** -->\n");
        output.write("<!--                                                                   -->\n");
        output.write("<!-- Licensed Materials - Property of IBM                              -->\n");
        output.write("<!--                                                                   -->\n");
        output.write("<!-- 5724-S68                                                          -->\n");
        output.write("<!--                                                                   -->\n");
        output.write("<!-- Copyright IBM Corp. 2001, 2008  All Rights Reserved.              -->\n");
        output.write("<!--                                                                   -->\n");
        output.write("<!-- US Government Users Restricted Rights - Use, duplication or       -->\n");
        output.write("<!-- disclosure restricted by GSA ADP Schedule Contract with           -->\n");
        output.write("<!-- IBM Corp.                                                         -->\n");
        output.write("<!--                                                                   -->\n");
        output.write("<!-- ***************************************************************** -->\n");
    }
    
}
