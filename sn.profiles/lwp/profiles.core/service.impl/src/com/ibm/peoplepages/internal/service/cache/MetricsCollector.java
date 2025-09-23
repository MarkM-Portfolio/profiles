/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetricsCollector
{
	private static final Log LOG = LogFactory.getLog(MetricsCollector.class);
    static private MetricsCollector GLOBALMETS = new MetricsCollector( );

    // we will store the hashMap here briefly to prevent getting metrics from
    // successive intervals.
    static private Map<String,HashMap> cacheHashtable  = new ConcurrentHashMap<String,HashMap>( );
    static private long tokenKeyValue = 10;  // reserve single digits for special use, e.g.,
                                             // supply data error message
	static String dateErrorMsg = null;
    
    public static MetricsCollector getGlobalInstance( )
    {
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getGlobalInstance: ");

        return GLOBALMETS;
    }

    private MetricsCollector()
    {
    }

    // get key array so can order output
    public String[] getMetricKeyArray()
    {
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricKeyArray: entry: ");

        return(SystemMetrics.getMetricKeyNameArray());
    }

    // get key array so can order output
    // this static version is for blogs
    public static String[] getMetricKeyArrayStaticC()
    {
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricKeyArrayStatic: ");

        return(SystemMetrics.getMetricKeyNameArray());
    }

    // return string that will in turn be given back to identify the
    // jsp's hashtable (cached in this class)
    public static String getMetricHashMapTokenC()
    {
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricKeyNameArray: ");

        tokenKeyValue++;
        String key = new String( (new Long(tokenKeyValue)).toString());

        HashMap metricsMap = SystemMetrics.fetchMetrics();
        cacheHashtable.put( key, metricsMap);

        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricHashMapTokenC: metricsMap: " + metricsMap);
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricHashMapTokenC: key: " + key);
        return( key);
    }

    // remove the cached hashtable.
    public static String destroyMetricHashMapC( String token)
    {
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: destroyMetricHashMapC: ");

        cacheHashtable.remove( token);
        return( "success");
    }

    public static String getMetricDescriptionForKeyC(String keyStr)
    {
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricDescriptionForKeyC: ");

        return( "not used in profiles");
    }

    public static Object getMetricValueForKeyC( String hashMapToken, String keyStr)
    {                                           
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricValueForKeyC: hashMapToken: " + hashMapToken);
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricValueForKeyC: keyStr: " + keyStr);

        Object retObj = null;

		if (hashMapToken.equals("0"))
		{
            if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: setting dateErrorMsg to keyStr");
		    dateErrorMsg = keyStr;
			return keyStr;
		}

        // find the cached hashtable
        HashMap thisJspsHashMap = (HashMap) cacheHashtable.get(hashMapToken);

        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricValueForKeyC: thisJspsHashMap: " + thisJspsHashMap);

        retObj = ((Object)(thisJspsHashMap.get(keyStr)));
        String retObjStr = null;
        if (null != retObj) {
        	retObjStr = retObj.toString();
        }
        else {
            if (LOG.isTraceEnabled())
            	LOG.trace("MetricsCollector: getMetricValueForKeyC: keyStr: " + keyStr + " is NULL");
        }
/*
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricValueForKeyC: retObjStr: " + retObjStr);

		Float numericalValue = new Float( retObjStr);

		// error if # is negative
		if (numericalValue < 0f)
		{
			// if # is negative, then error
            if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricValueForKeyC: error: numericalValue: " + numericalValue);
			retObj = (Object) dateErrorMsg;
		}
		else
		// if this is a float, remove extra fractional digits
		if (retObj instanceof Float )
		{
			// format float # nn.nn
            if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricValueForKeyC: float fixup: ");
			numericalValue = ((Float)retObj).floatValue() * 100.0f;
			int intValue = numericalValue.intValue();
			numericalValue = new Float(intValue);
			numericalValue = numericalValue / 100.0f;
			retObj = numericalValue;
		}

        return( retObj);
*/
        if (LOG.isTraceEnabled())
        	LOG.trace("MetricsCollector: getMetricValueForKeyC: keyStr: " + keyStr + " returning : " + retObjStr + ".");
        return( retObjStr);
	}
    
    public static Date getMetricDateC( String months) // arg isn't used. It would contain a list of month strings
    {
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricDateC: entry: months: " + months);
		Long epochDateL = new Long(SystemMetrics.getLastGetTime());
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricDateC: epochDateL: " + epochDateL);

        Date retValDate = new Date(epochDateL);

        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getMetricDateC: exit: retValDate: " + retValDate);
        return( retValDate);
    }

    // convenience method
    public static HashMap fetchAllMetrics( )
    {
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: fetchAllMetrics: ");

        return( SystemMetrics.fetchMetrics());
    }

    public static String getResourceStrC( String resName)
    {
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: getResourceStrC: entry: resName: " + resName);

            return("not used in Profiles");
    }

    public String toString()
    {
        return "MetricsCollector toString return";
    }

    public static boolean setCacheTimeoutMinutes(int newCacheTimeout)
    {
        if (LOG.isTraceEnabled()) LOG.trace("MetricsCollector: setCacheTimeoutMinutes: entry: newCacheTimeout: " + newCacheTimeout);

        return (SystemMetrics.setCacheTimeoutMinutes( newCacheTimeout));
    }
}
