/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.perf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;

/**
 *
 *
 */
public class SQLBenchmarkTest extends BaseTransactionalTestCase {
	
	private static final int REPEAT = 1;
	private static final int RUNS = 4;
	
	public void testSql() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			String sql = getSql();
			if (StringUtils.isEmpty(sql)) fail(usage());
			System.out.println("benchmarking: " + sql);

			conn = jdbcTemplate.getDataSource().getConnection();
			ps = conn.prepareStatement(sql);

			setupMaxRows(ps);

			System.out.println("Sleep to allow jvm to warmup");
			Thread.sleep(1000);

			for (int j = 0; j < RUNS; j++) {
				long rb = System.currentTimeMillis();
				for (int i = 0; i < REPEAT; i++) {
					ResultSet results = ps.executeQuery();
					while (results.next())
						;
					results.close();
				}
				long re = System.currentTimeMillis();

				long total = re - rb;

				System.out.println("Run (" + j + "): repeated query for (" + REPEAT + ") attempts executed in: " + (re - rb) + " msec");
				System.out.println("\tAverage query returned in: " + (total / REPEAT));
				if (j + 1 < RUNS) Thread.sleep(1000);
			}
		}
		finally {
			if (ps != null) ps.close();
			if (conn != null) conn.close();
		}
	}

	private void setupMaxRows(PreparedStatement ps) throws Exception {
		System.out.println("... You can limit rows selected by using: -DmaxRows=XXX");
		String maxRows = System.getProperty("maxRows");
		int mr = NumberUtils.toInt(maxRows, -1);
		if (mr > 0) {
			System.out.println("... Set max rows to: " + mr);
			ps.setMaxRows(mr);
		}
	}

	private String usage() {
		return "You must define the sql to test:\n" +
			"-Dsql=\"select foobar ...\"";
	}

	private String getSql() {
		return System.getProperty("sql");
	}

}
