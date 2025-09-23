/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

/**
 *  General file logging utility class
 */
public class ProfilesFileLogger {
	
    private static final String SEPARATOR = System.getProperty("line.separator");
    private static final Log LOGGER = LogFactory.getLog(ProfilesFileLogger.class);
    private static final String FILE_EXT = "log";

    // Holding a private instance
    private static ProfilesFileLogger INSTANCE;
    
    private String dateFormat = "yyyy-MM";

    /**
     * Singleton accessor method
     * 
     * @return the singleton instance of this logger.
     */
    public static ProfilesFileLogger INSTANCE() {
	if(INSTANCE == null) {
	    INSTANCE = new ProfilesFileLogger();
	}
	return INSTANCE;
    }

    /*
     *  Constructor
     */
    private ProfilesFileLogger() {

    }

    /**
     *  Allow caller to set the date format.
     */
    public void setDateFormat(String fmt) {
	dateFormat = fmt;
    }

    /**
     *  A private method to check whether we can create the log directory.
     *
     */
    private String checkPermission(String logDir) throws Exception {
	String retval = logDir;

	if( !StringUtils.isEmpty( logDir ) ) {

	    // normalize the seperators
	    if ( File.separator.equals("/") )
		retval = logDir.replace('\\', '/');
	    else if ( File.separator.equals("\\" ) )
		retval = logDir.replace('/', '\\');
	    
	    File logDirF = new File( retval );
	    if (!logDirF.exists()) {
		if (!logDirF.mkdirs()) {
		    LOGGER.error("Can't create directory: logDir = " +logDir );
		}
	    }
	}

	return retval;
    }

    /**
     *  Get the log file name, based on the current date. e.g. MissingUserLog-2014-03.log
     */
    private String getLogFilePath(String logFileName, String logDir) {
	String retval = logFileName;
	Date today = new Date();

	SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        String dateStr = sdf.format(today);	
	retval = logDir + File.separator +logFileName +"-" +dateStr + "." +FILE_EXT;

	return retval;
    }
    
    /**
     *  A method to write a line to the log file.
     *  This method doesn't throw exceptions. Just print out the error in the log.
     *
     */
    public void log(String logDir, String logFileName, String line ) {
	boolean DEBUG = LOGGER.isDebugEnabled();

	if ( DEBUG ) {
	    LOGGER.debug(" logDir = " +logDir +", logFileName = " +logFileName +", line = " +line );
	}

	BufferedWriter out = null;
	
	try {

	    logDir = checkPermission ( logDir );
	    String theLogFile = getLogFilePath(logFileName, logDir );

	    if ( LOGGER.isDebugEnabled() ) {
		LOGGER.debug("logFilePath = " +theLogFile );
	    }
	    
	    out = new BufferedWriter( new FileWriter(theLogFile, true) );
	    Date now = new Date();

	    out.write("[" +now + "] " +line + SEPARATOR);
	}
	catch(Exception ex) {
	    LOGGER.error("Can't write to the log file. Ex = " +ex );
	}
	finally {
	    try {
	    	if ( out != null ) out.close();
	    }
	    catch(IOException e) {
		// Should rarely happen. Do nothing
	    }
	}
    }
}
