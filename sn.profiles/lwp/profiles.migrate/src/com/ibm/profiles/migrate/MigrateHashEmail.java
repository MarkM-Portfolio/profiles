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

package com.ibm.profiles.migrate;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import com.ibm.lconn.core.web.secutil.Sha256Encoder;

public class MigrateHashEmail
{
	private final static String CLASS_NAME = MigrateHashEmail.class.getSimpleName();

	final static class DbType
	{
		final public static int DB2  = 0;
		final public static int SQL  = 1;
		final public static int ORCL = 2;
	}

	public enum ParameterType {
		STRING, INTEGER, BOOLEAN
	}

	static boolean trace = false;
	static boolean debug = false;

	static int dbType = DbType.DB2;

	static int commitFreq = 100;
	static int batchSize  = 250;

	static String selectSQL = null;

	// SQL from Mike
	// db2    SELECT PROF_KEY, PROF_MAIL_LOWER FROM EMPINST.EMPLOYEE WHERE PROF_IDHASH = '?' FETCH FIRST 250 ROWS ONLY OPTIMIZE FOR 250 ROWS WITH UR
	// mssql  SELECT TOP 250 PROF_KEY, PROF_MAIL_LOWER FROM EMPINST.EMPLOYEE WHERE PROF_IDHASH = '?'
	// oracle SELECT PROF_KEY, PROF_MAIL_LOWER FROM ( SELECT /*+ FIRST_ROWS(250) */ PROF_KEY, PROF_MAIL_LOWER,
	//                                                ROWNUM AS MYRNUM FROM EMPINST.EMPLOYEE WHERE PROF_IDHASH = '?' ) WHERE MYRNUM <= 250

	public static void main(String[] argv)
	{
		try {
			Connection conn = null;
			String url    = "";
			String driver = "";
			String user   = "";
			String pw     = "";

			if (argv.length < 2) {
				System.out.println(CLASS_NAME + " : usage: jdbcurl user pw ");
				System.exit(1);
			}

			// param to turn on trace logging
			trace = getBooleanParam(3, "trace", argv);
			if (trace) {
				System.out.println("Got trace " + trace);
				debug = true; // trace includes debug level tracing
			}
			else {
				// param to turn on debug logging
				debug = getBooleanParam(3, "debug", argv);
				if (debug)
					System.out.println("Got debug " + debug);
			}

			// allow params to set batch size, etc.
//			int theBatchSize = getIntegerParam(3, "batch", argv);
//			if (trace)
//				System.out.println("Got batch size " + theBatchSize);
//			String theDBType = getStringParam(0, "jdbc", argv);
//			if (trace)
//				System.out.println("Got db type " + theDBType);

			url = argv[0];

			String fields = getFieldsToSelect();

			if (url.indexOf("db2") != -1) {
				driver = "com.ibm.db2.jcc.DB2Driver";
				dbType = DbType.DB2;
				selectSQL = "SELECT " + fields + " FROM EMPINST.EMPLOYEE WHERE PROF_IDHASH = '?' " +
						"FETCH FIRST " + batchSize + " ROWS ONLY " + "OPTIMIZE FOR " + batchSize + " ROWS WITH UR";
			}
			else if (url.indexOf("sqlserver") != -1) {
				driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
				dbType = DbType.SQL;
				selectSQL = "SELECT TOP " + batchSize + " " + fields + " FROM EMPINST.EMPLOYEE WHERE PROF_IDHASH = '?'";
			}
			else if (url.indexOf("oracle") != -1) {
				driver = "oracle.jdbc.driver.OracleDriver";
				dbType = DbType.ORCL;
				selectSQL = "SELECT " + fields + " FROM ( SELECT /*+ FIRST_ROWS(" + batchSize + ") */ " + fields
							+ ", ROWNUM AS MYRNUM FROM EMPINST.EMPLOYEE WHERE PROF_IDHASH = '?' ) WHERE MYRNUM <= " + batchSize;
			}
			else {
				System.out.println(CLASS_NAME + " : Unrecognized database driver, using db2");
				driver = "com.ibm.db2.jcc.DB2Driver";
				dbType = DbType.DB2;
			}

			if (argv.length >= 3) {
				user = argv[1];
				pw   = argv[2];
			}

			Class.forName(driver).newInstance();
			Properties props = new Properties();

			if ("" != user) {
				props.setProperty("user",     user);
				props.setProperty("password", pw);
			}

			props.setProperty("retrieveMessagesFromServerOnGetMessage", "TRUE");

			conn = DriverManager.getConnection(url, props);
			conn.setAutoCommit(false);

			long totalUpdates = updateProfIDHash(conn);
			System.out.println(CLASS_NAME + " : Processed total updates " + totalUpdates + ".");

			// close the db
			conn.commit();

			// immediately disconnects from database and releases JDBC resources
			conn.close();
			if (!conn.isClosed())
				System.out.println(CLASS_NAME + " : A problem happened while closing the database connection.");
			conn = null;

			// success !!
			System.exit(0);

		}
		catch (Exception ex) {
			ex.printStackTrace();
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
			}
			System.out.println(ex.getMessage());
			System.out.println(CLASS_NAME + " : Migration failed, data may be inconsistent.");
			System.exit(1);
		}
	}

	private static String getFieldsToSelect()
	{
		// fields to be selected from db
		ArrayList<String> fieldList = new ArrayList<String>(6);
		if (trace) {
			fieldList.add("PROF_DISPLAY_NAME");
		}
		fieldList.add("PROF_KEY");
		fieldList.add("PROF_MAIL");
		fieldList.add("PROF_MAIL_LOWER");
		fieldList.add("PROF_IDHASH");
		StringBuilder sb = new StringBuilder();
		boolean isFirstField = true;
		for (Iterator<String> iterator = fieldList.iterator(); iterator.hasNext();) {
			String field = (String) iterator.next();
			if (isFirstField) {
				isFirstField = false;
			}
			else {
				sb.append(", ");
			}
			sb.append(field);
		}
		String fields = sb.toString();
		return fields;
	}

	private static long updateProfIDHash(Connection conn) throws SQLException
	{
		System.out.println(CLASS_NAME + " : Populate EMPLOYEE.PROF_IDHASH field");

		ResultSet rs = null;
		Statement selectStmt = conn.createStatement();
		selectStmt.setFetchSize(batchSize);

		// prepare both normal update SQL and one for when the user does not have an email address
		String updateStmt = "UPDATE EMPINST.EMPLOYEE SET PROF_IDHASH=? WHERE ";
		PreparedStatement idhashUpdateStmt = conn.prepareStatement(updateStmt + "PROF_MAIL_LOWER=?");
		PreparedStatement noMailUpdateStmt = conn.prepareStatement(updateStmt + "PROF_KEY=?");

		long totalUpdates = 0;
		boolean finished  = false;
		boolean firstTime = true;
		do {
			if (debug && firstTime) {
				System.out.println(CLASS_NAME + " : Executing : " + selectSQL);
				checkSQLExecute(conn);
			}

			try {
				rs = selectStmt.executeQuery(selectSQL);
				conn.commit();
				if (trace && firstTime) {
					dumpDebugInfo(rs);
				}
			}
			catch (SQLException ex1) {
				System.out.println(CLASS_NAME + " : Exception while retrieving Profiles Employee table data : " + ex1.getMessage());
				try {
					selectStmt.close();
				}
				catch (SQLException ex2) {
					// not much we can do to recover; just report the problem
					System.out.println(CLASS_NAME + " : Exception while closing SELECT statement : " + ex2.getMessage());
				}
				ex1.printStackTrace();
			}
			finally {
				firstTime = false;
			}

			if (null != rs) {
				boolean isMoreData = rs.next();
				if ( ! isMoreData) {
					finished = true;
				}
				else {
					int updates = 0;
					while (isMoreData) {
						String prof_key   = rs.getString("PROF_KEY");
						String prof_email = rs.getString("PROF_MAIL");
						String prof_lower = rs.getString("PROF_MAIL_LOWER");
						String useMail = (isEmpty(prof_lower) ? (isEmpty(prof_email) ? "" : prof_email.toLowerCase()) : prof_lower);
						String prof_idhash = Sha256Encoder.hashLowercaseStringUTF8(useMail, true); // default Profiles use case

						idhashUpdateStmt.clearParameters();

						// set the SQL parameters
						int rc = -1;
						if (isEmpty(useMail)) {
							noMailUpdateStmt.setString(1, prof_idhash);
							noMailUpdateStmt.setString(2, prof_key);

							rc = noMailUpdateStmt.executeUpdate();
						}
						else {
							idhashUpdateStmt.setString(1, prof_idhash);
							idhashUpdateStmt.setString(2, useMail);

							rc = idhashUpdateStmt.executeUpdate();
						}
						updates++;
						if (debug) {
							String prof_name  = rs.getString("PROF_DISPLAY_NAME");
							String msg = (isEmpty(useMail) ? "\'\'" : useMail) + " ==>" + prof_idhash;
							System.out.println(" " + updates + " / " + (totalUpdates + updates) + " : " + prof_name + " " + msg + " rc = " + rc);
						}
						if (updates >= commitFreq) {
							conn.commit();
							totalUpdates += updates;
							System.out.println(CLASS_NAME + " : Processed " + updates + " entries in this batch.  Total updates " + totalUpdates + ".");
							updates = 0;
						}
						isMoreData = rs.next();
					}
					if (updates > 0) {
						conn.commit();
						totalUpdates += updates;
						System.out.println(CLASS_NAME + " : Processed " + updates + " entries in this batch.  Total updates " + totalUpdates + ".");
						updates = 0;
					}
				}
			}
		}
		while (!finished);

		// clean up
		try {
			selectStmt.close();
			idhashUpdateStmt.close();
			noMailUpdateStmt.close();
			conn.commit();
		}
		catch (SQLException ex) {
			// not much we can do to recover; just report the problem
			System.out.println(CLASS_NAME + " : Exception while terminating Profiles Employee table update : " + ex.getMessage());
			ex.printStackTrace();
		}
		finally {
			selectStmt = null;
			idhashUpdateStmt = null;
			noMailUpdateStmt = null;
		}

		return totalUpdates;
	}

	private static boolean isEmpty(String str)
	{
		return ((null == str) || ("".equals(str)));
	}

	private static boolean getBooleanParam(final int startPosition, final String paramName, final String[] params) {
		boolean retVal = false;
		Object val = getParameter(startPosition, paramName, params, ParameterType.BOOLEAN);
		if (val instanceof Boolean) {
			retVal = (Boolean) val;
		}
		return retVal;
	}
	@SuppressWarnings("unused")
	private static int getIntegerParam(final int startPosition, final String paramName, final String[] params) {
		int retVal = 0;
		Object val = getParameter(startPosition, paramName, params, ParameterType.INTEGER);
		if (val instanceof Integer) {
			retVal = (Integer) val;
		}
		return retVal;
	}
	@SuppressWarnings("unused")
	private static String getStringParam(final int startPosition, final String paramName, final String[] params) {
		String retVal = null;
		Object val = getParameter(startPosition, paramName, params, ParameterType.STRING);
		if (val instanceof String) {
			retVal = (String) val;
		}
		return retVal;
	}
	private static Object getParameter(final int startPosition, final String paramName, final String[] params, ParameterType type)
	{
		Object retVal   = null;
		String paramStr = null;
		int maxParams   = params.length;

		boolean found = false;
		if (maxParams >= startPosition + 1)
		{
			int i = startPosition;
			while ((! found) && (i < maxParams))
			{
				paramStr = params[i];
				if (trace)
					System.out.println( "Checking '" + paramName + "' against param [" + i + "] " + paramStr );
				if (paramStr.indexOf(paramName) != -1)
					found = true;
				i++;
			}
			if (found) {
				if (ParameterType.BOOLEAN == type) {
					String paramVal = getParameterValue(paramStr);
					if (null != paramVal)
						retVal = Boolean.parseBoolean(paramVal);
				}
				else if (ParameterType.INTEGER == type) {
					String paramVal = getParameterValue(paramStr);
					if (null != paramVal)
						retVal = Integer.parseInt(paramVal);
				}
				else if (ParameterType.STRING == type) {
					retVal = paramStr;
				}
			}
		}
		return retVal;
	}

	private static String getParameterValue(String paramStr) {
		String retVal = null;
		int  beginIndex = paramStr.indexOf("=");
		if (beginIndex != -1) {
			String paramVal = paramStr.substring(beginIndex).trim();
			int endIndex = paramVal.indexOf(" ");
			if (endIndex == -1) {
				endIndex = paramVal.length();
				paramVal = paramVal.substring(1, endIndex);
				retVal = paramVal;
			}
		}
		return retVal;
	}

	private static void checkSQLExecute(Connection conn)
	{
		ResultSet rs   = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(selectSQL);
			conn.commit();

			StringBuilder sb = null;
			FileWriter    fw = null;
			boolean isFWOpen = false;
			try {
				fw = new FileWriter("migrate.log", false);
				isFWOpen = true;
				while (rs.next()) {
					sb = new StringBuilder();
					for (int i = 1; i < 6; i++) {
						sb.append(rs.getString(i));
						sb.append(" ");
					}
					sb.append("\n");
					fw.write(sb.toString());
				}
				fw.close();
				isFWOpen = false;
			}
			catch (IOException e) {
				if (debug) {
					System.out.println(CLASS_NAME + " : Exception while writing migrate.log file : " + e.getMessage());
				}
			}
			finally {
				sb = null;
				if (isFWOpen) {
					try {
						fw.close();
					}
					catch (IOException e) { }
					fw = null;
				}
			}
		}
		catch (SQLException ex) {
			if (trace) {
				System.out.println(CLASS_NAME + " : Exception while checking SQL statement : " + ex.getMessage());
			}
			ex.printStackTrace();
		}
		finally {
			try {
				stmt.close();
			}
			catch (SQLException ex) {
				if (trace) {
					System.out.println(CLASS_NAME + " : Exception while closing SQL statement : " + ex.getMessage());
				}
				ex.printStackTrace();
			}
		}
	}

	private static void dumpDebugInfo(ResultSet rs) throws SQLException
	{
		if (trace) {
			ResultSetMetaData rsm = rs.getMetaData();
			String catalogName = rsm.getCatalogName(1);
			String   tableName = rsm.getTableName(1);
			int     numColumns = rsm.getColumnCount();
			System.out.println(CLASS_NAME + " : CatalogName : " + catalogName + " TableName : " + tableName + " Columns : " + numColumns);
			for (int i = 1; i <= numColumns; i++) {
				String columnName  = rsm.getColumnName(i);
				String columnLabel = rsm.getColumnLabel(i);
				String columnType  = rsm.getColumnTypeName(i);
				System.out.println(CLASS_NAME + " : ColumnName : " + columnName + " ColumnLabel : " + columnLabel + " ColumnType : " + columnType);
			}
		}
	}
}
