/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.jobs;

import static java.util.logging.Level.FINER;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ibm.lconn.core.io.LConnIOUtils;
import com.ibm.lconn.core.util.EnvironmentType;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.scheduler.exception.ConfigException;
import com.ibm.lconn.scheduler.exception.JobConfigurationException;
import com.ibm.peoplepages.internal.service.cache.SystemMetrics;
import com.ibm.ventura.internal.service.admin.was.WASAdminService;

public class StatsCollectorTask extends AbstractProfilesScheduledTask {

	// the task name should match the name established in profiles-config.xml
	private static final String TASK_NAME = "StatsCollectorTask";
	protected final static String CLASS_NAME = StatsCollectorTask.class.getName();
	protected static Logger logger = Logger.getLogger(CLASS_NAME);

	private static String fileName = null;
	private static String filePath = null;

	public StatsCollectorTask() throws JobConfigurationException, ConfigException {
		super(TASK_NAME);
	}

	@Override
	public void init(Map<String, String> configParams) throws JobConfigurationException {
		// log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "init");
		// log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "init");
	}

	@Override
	public void doTask(Hashtable args) throws Exception {
		// log entry
		if (logger.isLoggable(FINER)) logger.entering(CLASS_NAME, "doTask", args.toString());
		// we do not run stats for multitenant, as there is no plan to provide per tenant info
		if (LCConfig.instance().isMTEnvironment() == false) {
			parseArgs(args);
			// create file name
			Date now = new Date();
			StringBuffer path = new StringBuffer();
			path.append(filePath).append(fileName).append("_").append(now.getTime()).append(".log");
			final File file = new File(path.toString());
			try {
				HashMap<String, String> metrics = SystemMetrics.fetchMetrics();
				dumpStatistics(file.getAbsolutePath(), metrics);
			}
			catch (Throwable t) {
				logger.log(Level.WARNING, "Error in StatsCollectorTask", t);
			}
		}
		// log exit
		if (logger.isLoggable(FINER)) logger.exiting(CLASS_NAME, "doTask");
	}
	
	private void parseArgs(Hashtable args) {
		if (fileName == null) {
			String fN = (String) args.get("fileName");
			String fP = (String) args.get("filePath");
			if (EnvironmentType.WEBSPHERE == EnvironmentType.getType()) {
				fP = fP.replace("/statistic/" + WASAdminService.getProcessName() + "//LC_NODE_NAME//", "/statistic//LC_NODE_NAME//");
				fP = fP.replace("LC_NODE_NAME", WASAdminService.getNodeName());
			}
			if (!fP.endsWith("/") && !fP.endsWith("\\")) {
				fP = fP + "//";
			}
			fileName = fN;
			filePath = fP;
		}
	}

	private void dumpStatistics(String path, HashMap<String, String> metrics) {
		// convert metrics to string
		StringBuffer sb = metricsToString(metrics);
		if (path != null && path.length() > 0) {
			File statFile = new File(path);
			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			PrintWriter pw = null;
			try {
				if (statFile.getParentFile() != null) {
					if (!statFile.getParentFile().exists()) {
						statFile.getParentFile().mkdirs();
					}
				}
				statFile.getAbsoluteFile().createNewFile();
				fos = new FileOutputStream(statFile);
				osw = new OutputStreamWriter(fos, "UTF8");
				pw = new PrintWriter(osw);
				writeReport(pw, sb);
			}
			catch (IOException ioe) {
				logger.log(Level.SEVERE, "Error writing statistics to file", ioe);
				logger.log(Level.SEVERE, statFile.getAbsolutePath());
			}
			finally {
				LConnIOUtils.closeQuietly(pw);
				LConnIOUtils.closeQuietly(osw);
				LConnIOUtils.closeQuietly(fos);
			}
		}
	}

	private StringBuffer metricsToString(HashMap<String, String> metrics) {
		StringBuffer rtnVal = new StringBuffer();
		Set<String> keys = metrics.keySet();
		for (String key : keys) {
			rtnVal.append(key).append(" : ").append(metrics.get(key)).append("\n");
		}
		return rtnVal;
	}

	private void writeReport(PrintWriter out, StringBuffer sb) {
		// StatisticsCollector statCollector = StatisticsCollector.getGlobalInstance();
		// Collection<Statistic> allStats = statCollector.getAllStats();
		// Iterator<Statistic> it = allStats.iterator();
		// StringBuilder sb = new StringBuilder();
		// while (it.hasNext())
		// {
		// Statistic stat = it.next();
		// stat.toStringBuilder(sb);
		// }
		out.write(sb.toString());
	}
}
