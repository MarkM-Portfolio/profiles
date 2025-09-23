/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.profiles.migrate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class MigrateProfKey {

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
		    
		    employeeSchemaUpgrade(con);

			generateProfKey(con);
						
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

	private static void employeeSchemaUpgrade(Connection con) throws SQLException {
		Statement stmt = con.createStatement();

		switch (dbType) {
		case DbType.DB2:

			stmt.execute("ALTER TABLE EMPINST.EMPLOYEE ADD COLUMN PROF_KEY VARCHAR(36)");        			
			break;
		case DbType.SQL:
			
			stmt.execute("ALTER TABLE EMPINST.EMPLOYEE ADD PROF_KEY NVARCHAR(36)");        			
			break;

		case DbType.ORCL:
			
			stmt.execute("ALTER TABLE EMPINST.EMPLOYEE ADD PROF_KEY VARCHAR2(36)");        			
			break;
		}
		stmt.close();
        con.commit();
        }
	
	private static void generateProfKey(Connection con) throws SQLException {
		System.out.println("Populate PROF_KEY uuid");
		Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM EMPINST.EMPLOYEE");        
		
		PreparedStatement uuidUpdateStmt = con.prepareStatement("UPDATE EMPINST.EMPLOYEE SET PROF_KEY=? WHERE PROF_UID=?");
        
        int count = 0;		
		
		while (rs.next()) {
			count++;

			String prof_uid = rs.getString("PROF_UID");
			String prof_key = java.util.UUID.randomUUID().toString();

			uuidUpdateStmt.clearParameters();
			
			// update all url entries
			uuidUpdateStmt.setString(1, prof_key);
			uuidUpdateStmt.setString(2, prof_uid);
			
			uuidUpdateStmt.executeUpdate();
			
			if ((count / 1000) * 1000 == count) {
				System.out.println("Processed " + count + " entries");
				con.commit();
			}
		}
		stmt.close();
		uuidUpdateStmt.close();
		con.commit();
	}

}
