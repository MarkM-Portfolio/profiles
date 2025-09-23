/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.profiles.migrate;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class MigrateEmployeeTable {

	  final static class DbType
	  {
	      final public static int DB2 = 0;
	      final public static int SQL  = 1;
	      final public static int ORCL = 2;
	  }

	  static int dbType = DbType.DB2;

	public static void main(String[] argv) {

		  
		  try {
	      Connection con = null;
	      String url = "";
	      String driver = "";
	      String user = "";
	      String pw = "";
	      	      
	      if (argv.length < 2) {
	    	  System.out.println("usage: jdbcurl user pw ");
	    	  System.exit(1);
	      }

    	  url = argv[0];
    	  
    	  if (argv[0].indexOf("db2") != -1) {
    		  driver = "com.ibm.db2.jcc.DB2Driver";
    		  dbType = DbType.DB2;
    	  }
    	  else  if (argv[0].indexOf("sqlserver") != -1) {
        	  driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        	  dbType = DbType.SQL;
    	  }
    	  else  if (argv[0].indexOf("oracle") != -1) {
        	  driver = "oracle.jdbc.driver.OracleDriver";
        	  dbType = DbType.ORCL;
    	  }
    	  else {
    		  System.out.println("Unrecognized database driver, using db2");
       		  driver = "com.ibm.db2.jcc.DB2Driver";
       		  dbType = DbType.DB2;
          	  }
    	  
    	  if (argv.length >= 3) {
    		  user = argv[1];
    	  		pw = argv[2];
    	  }
	    	  
          Class.forName(driver).newInstance();
          Properties props = new Properties();

          if( "" != user )
          {
            props.setProperty("user", user);
            props.setProperty("password", pw);
          }

          props.setProperty("retrieveMessagesFromServerOnGetMessage", "TRUE");
          
          con = DriverManager.getConnection( url, props );
          con.setAutoCommit(false);
		    
          migrateEmployeeTableData(con);
						
          // close the db
          con.commit();


          // immediately disconnects from database and releases JDBC resources
          con.close();
		    
          // success!!
          System.exit(0);

	} catch (Exception e1) {
		e1.printStackTrace();
		System.out.println(e1.getMessage());
		System.out.println("Migration failed, data may be inconsistent.");
		System.exit(1);
	}
	}

	
	private static void migrateEmployeeTableData(Connection con) throws SQLException {
		System.out.println("Copying data from EMPLOYEE_T to EMPLOYEE tables");
		Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM EMPINST.EMPLOYEE_T");        
		
        // for schemas 11 and 12, the new columns are nullable or generated so are not 
        // explictly needed in this insert statement
        // schema 13 will need to specify PROF_LAST_LOGIN in the insert
		PreparedStatement insertStmt = con.prepareStatement(
				"INSERT INTO EMPINST.EMPLOYEE (PROF_KEY, PROF_UID, PROF_UID_LOWER, PROF_LAST_UPDATE, PROF_MAIL, PROF_MAIL_LOWER, PROF_GUID, " +
				"PROF_SOURCE_UID, PROF_DISPLAY_NAME, PROF_LOGIN, PROF_GIVEN_NAME, PROF_SURNAME, PROF_ALTERNATE_LAST_NAME,  " +
				"PROF_PREFERRED_FIRST_NAME, PROF_PREFERRED_LAST_NAME, PROF_TYPE, PROF_MANAGER_UID, PROF_MANAGER_UID_LOWER, PROF_SECRETARY_UID, " +
				"PROF_IS_MANAGER, PROF_GROUPWARE_EMAIL, PROF_GW_EMAIL_LOWER, PROF_JOB_RESPONSIBILITIES, PROF_ORGANIZATION_IDENTIFIER, " +
				"PROF_ISO_COUNTRY_CODE, PROF_FAX_TELEPHONE_NUMBER, PROF_IP_TELEPHONE_NUMBER, PROF_MOBILE, PROF_PAGER, " +
				"PROF_TELEPHONE_NUMBER, PROF_WORK_LOCATION, PROF_BUILDING_IDENTIFIER, PROF_DEPARTMENT_NUMBER, PROF_EMPLOYEE_TYPE, PROF_FLOOR, " +
				"PROF_EMPLOYEE_NUMBER, PROF_PAGER_TYPE, PROF_PAGER_ID, PROF_PAGER_SERVICE_PROVIDER, PROF_PHYSICAL_DELIVERY_OFFICE, PROF_PREFERRED_LANGUAGE, " +
				"PROF_SHIFT, PROF_TITLE, PROF_COURTESY_TITLE, PROF_TIMEZONE, PROF_NATIVE_LAST_NAME, PROF_NATIVE_FIRST_NAME, PROF_BLOG_URL, " +  
				"PROF_FREEBUSY_URL, PROF_CALENDAR_URL, PROF_SOURCE_URL, PROF_DESCRIPTION, PROF_EXPERIENCE) " +
				"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        
        int count = 0;		
		
		while (rs.next()) {
			int i = 1;
			count++;

			String prof_key = rs.getString("PROF_KEY");
			String prof_uid = rs.getString("PROF_UID");
			String prof_mail = rs.getString("PROF_MAIL");
			String prof_dn = rs.getString("PROF_DISPLAY_NAME");
			String mgr_uid = rs.getString("PROF_MANAGER_UID");

			insertStmt.clearParameters();
			
			// update all entries
			insertStmt.setString(i++, prof_key);
			insertStmt.setString(i++, prof_uid);
			insertStmt.setString(i++, rs.getString("PROF_UID_LOWER"));
			insertStmt.setTimestamp(i++, rs.getTimestamp("PROF_LAST_UPDATE"));
			insertStmt.setString(i++, prof_mail);
			insertStmt.setString(i++, rs.getString("PROF_MAIL_LOWER"));
			insertStmt.setString(i++, rs.getString("PROF_GUID"));
			insertStmt.setString(i++, rs.getString("PROF_SOURCE_UID"));
			insertStmt.setString(i++, prof_dn);
			insertStmt.setString(i++, rs.getString("PROF_LOGIN"));
			insertStmt.setString(i++, rs.getString("PROF_GIVEN_NAME"));
			insertStmt.setString(i++, rs.getString("PROF_SURNAME"));
			insertStmt.setString(i++, rs.getString("PROF_ALTERNATE_LAST_NAME"));
			insertStmt.setString(i++, rs.getString("PROF_PREFERRED_FIRST_NAME"));
			insertStmt.setString(i++, rs.getString("PROF_PREFERRED_LAST_NAME"));
			insertStmt.setString(i++, rs.getString("PROF_TYPE"));
			insertStmt.setString(i++, mgr_uid);
			if (mgr_uid != null) {
				insertStmt.setString(i++, mgr_uid.toLowerCase());
			} else {
				insertStmt.setString(i++, null);
			}
			insertStmt.setString(i++, rs.getString("PROF_SECRETARY_UID"));
			insertStmt.setString(i++, rs.getString("PROF_IS_MANAGER"));
			insertStmt.setString(i++, rs.getString("PROF_GROUPWARE_EMAIL"));
			insertStmt.setString(i++, rs.getString("PROF_GW_EMAIL_LOWER"));
			insertStmt.setString(i++, rs.getString("PROF_JOB_RESPONSIBILITIES"));
			insertStmt.setString(i++, rs.getString("PROF_ORGANIZATION_IDENTIFIER"));
			insertStmt.setString(i++, rs.getString("PROF_ISO_COUNTRY_CODE"));
			insertStmt.setString(i++, rs.getString("PROF_FAX_TELEPHONE_NUMBER"));
			insertStmt.setString(i++, rs.getString("PROF_IP_TELEPHONE_NUMBER"));
			insertStmt.setString(i++, rs.getString("PROF_MOBILE"));
			insertStmt.setString(i++, rs.getString("PROF_PAGER"));
			insertStmt.setString(i++, rs.getString("PROF_TELEPHONE_NUMBER"));
			insertStmt.setString(i++, rs.getString("PROF_WORK_LOCATION"));
			insertStmt.setString(i++, rs.getString("PROF_BUILDING_IDENTIFIER"));
			insertStmt.setString(i++, rs.getString("PROF_DEPARTMENT_NUMBER"));
			insertStmt.setString(i++, rs.getString("PROF_EMPLOYEE_TYPE"));
			insertStmt.setString(i++, rs.getString("PROF_FLOOR"));
			insertStmt.setString(i++, rs.getString("PROF_EMPLOYEE_NUMBER"));
			insertStmt.setString(i++, rs.getString("PROF_PAGER_TYPE"));
			insertStmt.setString(i++, rs.getString("PROF_PAGER_ID"));
			insertStmt.setString(i++, rs.getString("PROF_PAGER_SERVICE_PROVIDER"));
			insertStmt.setString(i++, rs.getString("PROF_PHYSICAL_DELIVERY_OFFICE"));
			insertStmt.setString(i++, rs.getString("PROF_PREFERRED_LANGUAGE"));
			insertStmt.setString(i++, rs.getString("PROF_SHIFT"));
			insertStmt.setString(i++, rs.getString("PROF_TITLE"));
			insertStmt.setString(i++, rs.getString("PROF_COURTESY_TITLE"));
			insertStmt.setString(i++, rs.getString("PROF_TIMEZONE"));
			insertStmt.setString(i++, rs.getString("PROF_NATIVE_LAST_NAME"));
			insertStmt.setString(i++, rs.getString("PROF_NATIVE_FIRST_NAME"));
			insertStmt.setString(i++, rs.getString("PROF_BLOG_URL"));
			insertStmt.setString(i++, rs.getString("PROF_FREEBUSY_URL"));
			insertStmt.setString(i++, rs.getString("PROF_CALENDAR_URL"));
			insertStmt.setString(i++, rs.getString("PROF_SOURCE_URL"));
			if (rs.getClob("PROF_DESCRIPTION") != null)
				insertStmt.setClob(i++, rs.getClob("PROF_DESCRIPTION"));
			else
				insertStmt.setClob(i++, (Clob)null);
			if (rs.getClob("PROF_EXPERIENCE") != null)
				insertStmt.setClob(i++, rs.getClob("PROF_EXPERIENCE"));
			else
				insertStmt.setClob(i++, (Clob)null);
			
			try {
				insertStmt.execute();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("Original record: PROF_KEY: " + prof_key +
					" PROF_UID: " + prof_uid +
					" PROF_MAIL: " + prof_mail +
					" PROF_DISPLAY_NAME: " + prof_dn
					);
			System.out.println(" ");
			}
			
			if ((count / 1000) * 1000 == count) {
				System.out.println("Processed " + count + " entries");
				con.commit();
			}
		}
		stmt.close();
		insertStmt.close();
		con.commit();
	}

}
