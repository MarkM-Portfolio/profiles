/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                 */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.file.LCTempFile;
import com.ibm.lconn.core.file.LCTempFileManager;
import com.ibm.lconn.core.file.LCTempFileManagerFactory;
import com.ibm.lconn.core.io.LConnIOUtils;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.sn.av.admin.AVFilterFactory;
import com.ibm.sn.av.api.AVScanner;
import com.ibm.sn.av.api.AVScannerException;
import com.ibm.sn.av.api.AVScannerResult;
import com.ibm.sn.av.api.ICAPScannerResult;

/**
 * Anti-virus scanner filter for Profiles
 */
public class AntiVirusFilter 
{
    private static Log log = LogFactory.getLog(AntiVirusFilter.class);
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("com.ibm.peoplepages.internal.resources.AdminResources");

    private static final String     	icapInfectionHeader = "X-Infection-Found";

    private static String 		avServers = null;
    private static boolean		avException = true; // throws exception or not
    
    private static AVScanner		scanner = null;

    static 
    {
    	scanner = AVFilterFactory.getAVFilter();
    }

    public static boolean isAvEnable() {
    	return scanner != null;
    }

    public static boolean isEicarEnabled() {
    	return AVFilterFactory.isEicarEnabled();
    }

    public static InputStream getEicar() {
    	return AVFilterFactory.getEicar();
    }

    public static InputStream scanFile(InputStream input) throws AVScannerException {

    	// If Anti Virus is not enabled, just return the original input stream
    	if ( !isAvEnable() || input == null ) return input;

    	InputStream dataToFilter = null;
    	//AVScanner scanner = getScanner();

    	// Filter the cached data...
    	InputStream filteredData = null;

    	// TODO - hack until scanner cleans content
		LCTempFileManager mgr = LCTempFileManagerFactory.getInstance();
		LCTempFile tempFile = null;

    	try {
    		tempFile = mgr.newTempFile(input);
    		dataToFilter = tempFile.open();
    		
    		AVScannerResult scannerResult = scanner.scan(dataToFilter);

    		int status = scannerResult.getStatus();

    		// This should be an ICAP result...
    		ICAPScannerResult icapResult = null;
    		if (scannerResult instanceof ICAPScannerResult)
    			icapResult = (ICAPScannerResult) scannerResult;

    		// Manage the result...
    		String scannerMsg = (icapResult == null) ? scannerMsg = Integer.toString(status) : icapResult.getStatusLine();

    		// if no virus found
    		if (status == AVScannerResult.AVSTATUS_OK) {
    			log.info(resourceBundle.getString("info.virus.scanning.result.ok") +  " " + scannerMsg + "");
    			
    			filteredData = tempFile.openDeleteOnClose();
    		} 
    		//if virus found
    		else if ((status == AVScannerResult.AVSTATUS_INFECTED)
    				|| (status == AVScannerResult.AVSTATUS_CLEANED)) {
    			if (icapResult != null) {
    				Hashtable<?,?> headers = icapResult.getHeaders();
    				if(headers != null) {
    					if (headers.containsKey(icapInfectionHeader)) {
    						scannerMsg += " [" + icapInfectionHeader + ": "	+ 
    						(String) icapResult.getHeaders().get(icapInfectionHeader) + "]";
    					}
    				}
    			}

    			if (avException || (scannerResult.getUpdatedContent() == null)) {
    				// virus found error
    				throw new AVScannerException("err.virus.detected:"+MessageFormat.format(resourceBundle.getString("err.virus.detected"), scannerMsg));
    			}
    			else {
    				// virus cleaned
    				byte[] cleanData = scannerResult.getUpdatedContent();
    				filteredData = new ByteArrayInputStream(cleanData);
    				log.error(resourceBundle.getString("err.virus.replaced"));
    			}
    		}
    		else {
    			// Some connection problem - we should log it but return the original content.  Also start a retry wait if it is configured...
    			scannerMsg += " " + avServers;
    			if (log.isInfoEnabled())
    				log.info(MessageFormat.format(resourceBundle.getString("info.virus.scanning.error"),scannerMsg));

    			// ...but throw an exception if told to
    			if (avException)
    				throw new AVScannerException("info.virus.scanning.error:" + MessageFormat.format(resourceBundle.getString("info.virus.scanning.error"),scannerMsg));
    			
    			filteredData = tempFile.openDeleteOnClose();
    		}
        	
        	return filteredData;
        	
    	} catch (AVScannerException e) {
    		// scanner error
    		throw new AVScannerException(MessageFormat.format(resourceBundle.getString("info.virus.scanning.error"),e.getMessage()));
    	} catch (IOException e) {
			if (tempFile != null) {
				tempFile.delete();
			}
			
			throw new ProfilesRuntimeException(e);
		} finally {
    		LConnIOUtils.closeQuietly(dataToFilter);
    	}

    	
    }
}
