/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */


package com.ibm.peoplepages.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class wrappers the MesageDigest to produce a hexadecimal digest
 * for String data
 */
public class TextMessageDigest {

    private MessageDigest _md;

    /** Map from integers to hex characters */
    public static final char[] HEXCHARS = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    public TextMessageDigest() {
        this("MD5");
    }

    public TextMessageDigest(String algorithm) {
        try {
            _md = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException nsae) {
            /** TBD: wrapper with our own exception */
            nsae.printStackTrace( );
        }
    }

    public int getLength() {
        return 2 * _md.getDigestLength();
    }

    public static String stringify(byte[] digest) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            int codeByte = (digest[i] >= 0) ? digest[i] : (256+digest[i]);
            sb.append(HEXCHARS[(codeByte & 0xF0) >> 4]);
            sb.append(HEXCHARS[codeByte & 0x0F]);
        }

        return sb.toString();
    }

    public String compute(byte[] bytes) {
        _md.reset();
        _md.update(bytes);
        return stringify(_md.digest());
    }

    public String compute(String plaintext) {
        return compute(plaintext.getBytes());
    }

    public String compute(InputStream in) {
        _md.reset();
        DigestInputStream din = new DigestInputStream(in,_md);
        try {
            while (din.read() != -1) {
            }
        }
        catch (IOException ioe) {
            return null;
        }
        return stringify(_md.digest());
    }
}

