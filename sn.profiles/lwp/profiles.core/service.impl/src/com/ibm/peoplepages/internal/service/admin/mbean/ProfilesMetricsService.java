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

package com.ibm.peoplepages.internal.service.admin.mbean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Set;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.FieldPosition;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ArrayList;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.peoplepages.internal.service.cache.MetricsCollector;
import com.ibm.peoplepages.internal.service.cache.SystemMetrics;

/**
 * Provides functions to retrieve metrics from the Profiles server
 * 
 * @author rmelanson
 */
public class ProfilesMetricsService extends ProfilesMBeanBase implements ProfilesMetricsServiceMBean {

	private static final Log LOG = LogFactory.getLog(ProfilesMetricsService.class);
	static final String DATESSTR = "dates";
	static final String OKSTR = "ok";
	static final String ERRORSTR = "error";

	public ProfilesMetricsService() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.openactivities.admin.mbean.MetricsServiceMBean#fetchMerics()
	 */
	public HashMap fetchMetrics() {
		return new RetBeanMethod<HashMap>(Tenant.SINGLETENANT_KEY) {
			HashMap retWorker() {
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: fetchMetrics: entry");
				return (MetricsCollector.fetchAllMetrics());
			}
		}.returnValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.openactivities.admin.mbean.StatisticsServiceMbean#fetchStatistics(java.lang.String[])
	 */
	public Object fetchMetric(final String metricName) {
		return new RetBeanMethod<Object>(Tenant.SINGLETENANT_KEY) {
			Object retWorker() {
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: fetchMetric: entry: metricName: " + metricName);

				if (metricName == null || metricName.length() == 0) return null;

				String oneField[] = new String[] { metricName };

				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: fetchMetric: entry: oneField: " + oneField);

				HashMap tempMap = fetchMetricsFields(oneField);

				if (LOG.isTraceEnabled())
					LOG.trace("MetricsService: fetchMetric: entry: (Object)tempMap.get( metricName): " + (Object) tempMap.get(metricName));
				return ((Object) tempMap.get(metricName));
			}
		}.returnValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.openactivities.admin.mbean.StatisticsServiceMbean#fetchStatistics(java.lang.String[])
	 */
	public HashMap fetchMetricsFields(final String[] metricNameArr) {
		return new RetBeanMethod<HashMap>(Tenant.SINGLETENANT_KEY) {
			HashMap retWorker() {
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: fetchMetricsFields: entry: metricNameArr: " + metricNameArr);
				if (metricNameArr == null) return null;

				HashMap htAll = MetricsCollector.fetchAllMetrics();
				HashMap htRet = new HashMap();

				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: fetchMetricsFields: htAll: " + htAll);

				// look up the specified metrics
				for (int ix = 0; ix < metricNameArr.length; ix++) {
					if (LOG.isTraceEnabled()) LOG.trace("MetricsService: fetchMetricsFields: metricNameArr[ix]: " + metricNameArr[ix]);

					if (metricNameArr[ix] == null || metricNameArr[ix].length() == 0) continue;

					if (LOG.isTraceEnabled())
						LOG.trace("MetricsService: fetchMetricsFields: (Object)htAll.get( metricNameArr[ix]): "
								+ (Object) htAll.get(metricNameArr[ix]));
					Object valueObj = (Object) htAll.get(metricNameArr[ix]);
					if (valueObj != null) {
						htRet.put(metricNameArr[ix], valueObj);
					}
				}

				return htRet;
			}
		}.returnValue();
	}

	public String saveMetricToFile(final String absoluteFilename, final Integer sampleCount, final String fieldKeyArg) {
		return new RetBeanMethod<String>(Tenant.SINGLETENANT_KEY) {
			String retWorker() {
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: entry1: absoluteFilename: " + absoluteFilename);
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: entry1: sampleCount: " + sampleCount);
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: entry1: fieldKeyArg: " + fieldKeyArg);
				String separator = ","; // was an arg
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: entry1: separator: " + separator);

				ArrayList fieldKeys = new ArrayList();
				String retStr = OKSTR;

				toEnd: do {

					// check for reset timeout
					if (fieldKeyArg.equals("metrics.cache.timeout.in.minutes")) {
						if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: setting timeout");

						int newCacheTimeout = sampleCount.intValue();

						if (MetricsCollector.setCacheTimeoutMinutes(newCacheTimeout))
							retStr = OKSTR + ": cache timeout changed";
						else
							retStr = ERRORSTR + ": cache timeout invalid (< 0 or > 1440 minutes)";

						break toEnd;
					}

					// check for all requested
					if (fieldKeyArg.equalsIgnoreCase("all")) {
						String[] appKeyArray = SystemMetrics.getMetricKeyNameArray();
						for (int ix = 0; ix < appKeyArray.length; ix++) {
							fieldKeys.add(appKeyArray[ix]);
						}
						retStr = saveMetricsToFile(absoluteFilename, sampleCount, fieldKeys);
						break toEnd; // not strictly necessary
					}
					else {
						fieldKeys.add(fieldKeyArg);
						retStr = saveMetricsToFile(absoluteFilename, sampleCount, fieldKeys);
						break toEnd; // not strictly necessary
					}

				}
				while (false);

				return retStr;
			}
		}.returnValue();
	}

	public String saveMetricsToFile(final String absoluteFilename, final Integer sampleCount, final ArrayList fieldKeys) {
		return new RetBeanMethod<String>(Tenant.SINGLETENANT_KEY) {
			String retWorker() {
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: entry1: absoluteFilename: " + absoluteFilename);
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: entry1: sampleCount: " + sampleCount);
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: entry1: fieldKey: " + fieldKeys);
				String separator = ","; // was an arg
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: entry1: separator: " + separator);

				String retStr = OKSTR;

				Hashtable hTable = new Hashtable();
				Object[] requestKeyArray = fieldKeys.toArray();
				String fieldKey = null;
				boolean bFileAlreadyExists = false;

				toEnd: do {
					// verify that 'sampleCount' is reasonable
					if (sampleCount < 1 || sampleCount > 1000) {
						if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: error: sampleCount bad");
						retStr = ERRORSTR + ": sample count bad";
						break toEnd;
					}

					// verify separator is a single char (always true since separator is no longer an
					// argument.
					if (separator.length() != 1) {
						if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: error: separator bad");
						retStr = ERRORSTR + ": separator bad, must be length 1";
						break toEnd;
					}

					// validate the keys
					String[] appKeyArray = SystemMetrics.getMetricKeyNameArray();
					for (int iy = 0; iy < requestKeyArray.length; iy++) {
						// make sure the object is a string
						if (!(requestKeyArray[iy] instanceof String)) {
							retStr = ERRORSTR + ": field key is not a String";
							break toEnd;
						}
						fieldKey = (String) requestKeyArray[iy];

						// verify that fieldKey is a valid key
						boolean bFoundKey = false;

						for (int ix = 0; ix < appKeyArray.length; ix++) {
							if (appKeyArray[ix].equals(fieldKey)) {
								// fieldKey is ok
								bFoundKey = true;
								break;
							}
						}

						if (!bFoundKey) {
							if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: error: fieldKey bad");
							retStr = ERRORSTR + ": fieldKey bad";
							break toEnd;
						}
					}

					// 'msf' is MetricsSaveFile
					File msf = new File(absoluteFilename);
					if (msf == null) {
						if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: error: file bad");
						retStr = ERRORSTR + ": file bad";
						break toEnd;
					}

					if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: test: does file (or dir) exist?");
					if (msf.exists()) {
						if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: ok: file (or dir) exists");
						if (msf.isFile()) {
							if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: ok, it is a file (not dir)");
							if (msf.canRead() && msf.canWrite()) {
								if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: ok, can read");
								// retStr = "ok: can read/write file";
								bFileAlreadyExists = true;
							}
							else {
								if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: error: can't read/write file");
								retStr = ERRORSTR + ": can't read/write file";
								break toEnd;
							}
						}
						else {
							if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: error: given directory, not file");
							retStr = ERRORSTR + ": given directory, not file";
							break toEnd;
						}
					}
					else {
						if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: file (or dir) does NOT exists");
						try {
							// create the file
							if (msf.createNewFile()) {
								if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: file created successfully");
								// retStr = "ok: file created";
							}
							else {
								if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: error: create failed");
								retStr = ERRORSTR + ": create failed";
								break toEnd;
							}
						}
						catch (IOException e) {
							// logger.error(_rbh.getString("err.parsing.stats.file"), e);
							retStr = ERRORSTR + ": io error, create file failed";
							break toEnd;
						}
					}

					if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: bFileAlreadyExists: " + bFileAlreadyExists);

					HashMap metricMap = SystemMetrics.fetchMetrics();

					// args are ok, now if file exists, i.e., not just created,
					// must load it (and thus validate it)
					if (bFileAlreadyExists) {
						StringBuffer strBuf = new StringBuffer();
						hTable = parseFile(msf, separator, strBuf);
						if (hTable == null) {
							retStr = strBuf.toString();
							break toEnd;
						}

						// get date vector
						Vector vect = (Vector) hTable.get(DATESSTR);
						// add the current date/time to the date vector
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						StringBuffer dateStr = new StringBuffer();
						sdf.format(new Date(), dateStr, new FieldPosition(0));
						if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: dateStr: " + dateStr);
						vect.add(dateStr.toString());

						// make sure fieldKey is represented in file
						for (int iy = 0; iy < requestKeyArray.length; iy++) {
							fieldKey = (String) requestKeyArray[iy];

							if (!hTable.containsKey(fieldKey)) {
								// fieldKey not there yet
								Vector vect1 = new Vector();
								for (int ix = 0; ix < vect.size() - 1; ix++) {
									vect1.add("0");
								}
								hTable.put(fieldKey, vect1);
							}
						}

						Set keySet = hTable.keySet();
						String keyFrFile = null;
						Object newMetricValue = null;

						Iterator it1 = keySet.iterator();
						for (; it1.hasNext();) {
							// get the next metric key (in file)
							keyFrFile = (String) it1.next();
							if (keyFrFile.equals(DATESSTR)) continue; // skip date vector here
							if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: keyFrFile: " + keyFrFile);
							// get the vector associated with key
							vect = (Vector) hTable.get(keyFrFile);
							// add metric value
							newMetricValue = (Object) metricMap.get(keyFrFile);
							// check for key invalid.
							if (newMetricValue == null) {
								retStr = ERRORSTR + ": bad key in file";
								break toEnd;
							}
							vect.add(newMetricValue);
						}

						it1 = keySet.iterator();
						for (; it1.hasNext();) {
							keyFrFile = (String) it1.next();
							vect = (Vector) hTable.get(keyFrFile);

							do { // loop until under sampleCount
								if (vect.size() > sampleCount) {
									if (LOG.isTraceEnabled())
										LOG.trace("MetricsService: saveMetricToFile: removing: vect.size, sampleCount: " + vect.size()
												+ ", " + sampleCount);
									vect.remove(0);
								}
								else
									break;
							}
							while (true);
						}
					}
					else {
						// seed the hash table
						Vector vect = new Vector();

						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						StringBuffer dateStr = new StringBuffer();
						sdf.format(new Date(), dateStr, new FieldPosition(0));
						if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: dateStr: " + dateStr);
						vect.add(dateStr.toString());

						hTable.put(DATESSTR, vect);

						// add keys
						for (int iy = 0; iy < requestKeyArray.length; iy++) {
							fieldKey = (String) requestKeyArray[iy];

							vect = new Vector();
							vect.add((Object) metricMap.get(fieldKey));
							hTable.put(fieldKey, vect);
						}

						if (LOG.isTraceEnabled()) LOG.trace("MetricsService: saveMetricToFile: hash table is seeded");
					}

					String newMetrics = metricsToString(hTable, separator);

					// write to file
					BufferedWriter output = null;
					try {
						output = new BufferedWriter(new FileWriter(msf));
						output.write(newMetrics);

					}
					catch (IOException e) {
						// logger.error(_rbh.getString("err.saving.statspersist"), e);
						retStr = "error: io error, write file failed 1";
						break toEnd;
					}
					finally {
						try {
							if (output != null) output.close();
						}
						catch (IOException e) {
							// logger.error(_rbh.getString("err.closing.stats"), e);
							retStr = "error: io error, write file failed 2";
							break toEnd;
						}
					}

				}
				while (false);

				return retStr;
			}
		}.returnValue();
	}

	/**
	 * Parses the file specified assuming CSV( "Comma" Separated Values) format for the statistics. Dates row does have to be there unless
	 * file is empty. Returns a Hashtable of Vectors - each vector is a row in the file. Values separated by separator character.
	 * 
	 * @param fname - filename to parse
	 * @return Hashtable of Vectors
	 */
	private Hashtable parseFile(final File msf, final String separator, final StringBuffer retStrBuf) {
		return new RetBeanMethod<Hashtable>(Tenant.SINGLETENANT_KEY) {
			Hashtable retWorker() {
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: parseFile: entry: separator: " + separator);

				Hashtable hTable = new Hashtable();
				Vector vector = null;
				BufferedReader input = null;

				try {
					// File msf = new File( fname);

					input = new BufferedReader(new FileReader(msf));
					String line = null;
					while ((line = input.readLine()) != null) {
						vector = new Vector();
						String[] xStrArr = line.split(separator);
						for (int ix = 1; ix < xStrArr.length; ix++) {
							vector.add(xStrArr[ix]);
						}
						hTable.put(xStrArr[0], vector);
					}
				}
				catch (IOException e) {
					retStrBuf.append("error:  io error parsing file");
					hTable = null;
				}
				finally {
					try {
						if (input != null) {
							input.close();
						}
					}
					catch (IOException e) {
						retStrBuf.append("error:  io error parsing file");
						hTable = null;
					}
				}
				return hTable;
			}
		}.returnValue();
	}

	/**
	 * Puts given Hashtable in CSV (Comma Separated Values) format
	 * 
	 * @param h - Hashtable of Vectors of statistics
	 * @return
	 */
	public String metricsToString(final Hashtable hTable, final String separator) {
		return new RetBeanMethod<String>(Tenant.SINGLETENANT_KEY) {
			String retWorker() {
				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: metricsToString: entry: separator: " + separator);

				StringBuffer sb = new StringBuffer();
				Set keys = hTable.keySet();

				Object key;
				Vector vector;

				// write dates first
				sb.append(DATESSTR + separator);
				vector = (Vector) hTable.get(DATESSTR);
				Enumeration enumr = vector.elements();
				for (; enumr.hasMoreElements();) {
					sb.append(enumr.nextElement() + separator);
				}

				sb.append(System.getProperty("line.separator"));

				// now write the metrics
				Iterator iter = keys.iterator();
				for (; iter.hasNext();) {
					key = iter.next();

					if (key.equals(DATESSTR)) continue;

					sb.append(key.toString() + separator);
					vector = (Vector) hTable.get(key);
					enumr = vector.elements();
					for (; enumr.hasMoreElements();) {
						sb.append(enumr.nextElement() + separator);
					}
					sb.append(System.getProperty("line.separator"));
				}

				if (LOG.isTraceEnabled()) LOG.trace("MetricsService: metricsToString: exit: sb.toString( ): " + sb.toString());
				return sb.toString();
			}
		}.returnValue();
	}
}
