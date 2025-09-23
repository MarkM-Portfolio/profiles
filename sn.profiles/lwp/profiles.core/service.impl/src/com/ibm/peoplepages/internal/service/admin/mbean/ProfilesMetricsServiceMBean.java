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

package com.ibm.peoplepages.internal.service.admin.mbean;

import java.util.HashMap;
import java.util.ArrayList;

public interface ProfilesMetricsServiceMBean {
    /**
     * Get all system metrics.
     * @return Hashtable containing all system metrics
     */
    public HashMap fetchMetrics();
    
    /**
     * Get system statistics specified by fields
     * @param fields - Which system metric to fetch
     * @return Returns a Hashtable of system metrics
     */
    public Object fetchMetric(final String metricName);
    
    
    /**
     * Get system metrics specified by fields
     * @param fields - Which system metrics to fetch
     * @return Returns a Hashtable of system metrics
     */
    public HashMap fetchMetricsFields(String[] metricNameArr);


    /**
     * Save specifig system metric to a file
     * Also used to set timeout
     * @param fields - Which system metrics to fetch
     * @return Returns void
     */
    public java.lang.String saveMetricToFile( String absoluteFilename, Integer sampleCount, String fieldKey);

    /**
     * Save specifig system metric to a file
     * Also used to set timeout
     * @param fields - Which system metrics to fetch
     * @return Returns void
     */
    public java.lang.String saveMetricsToFile( String absoluteFilename, Integer sampleCount, ArrayList fieldKeys);
}
