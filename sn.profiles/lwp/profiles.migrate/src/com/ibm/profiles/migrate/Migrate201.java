/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
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
import java.sql.Timestamp;
import java.util.Properties;

public class Migrate201 {

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
		    

			updateConnections(con);
						
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
	
	private static void updateConnections(Connection con) throws SQLException {
		System.out.println("Migrating Profiles Colleagues");

		Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM EMPINST.PROF_CONNECTIONS");

        rs.next();
        int numConnections = rs.getInt(1);
		rs.close();
		
		com.ibm.peoplepages.data.Connection prof_conn[] = new com.ibm.peoplepages.data.Connection[numConnections];
		
        rs = stmt.executeQuery("SELECT * FROM EMPINST.PROF_CONNECTIONS");        
		int i = 0;
		
    	while(rs.next()) {
    		prof_conn[i] = new com.ibm.peoplepages.data.Connection();
    		
    		prof_conn[i].setConnectionId(rs.getString("PROF_CONNECTION_ID"));
    		prof_conn[i].setSourceKey(rs.getString("PROF_SOURCE_KEY"));
    		prof_conn[i].setTargetKey(rs.getString("PROF_TARGET_KEY"));
    		prof_conn[i].setCreatedByKey(rs.getString("PROF_CREATED_BY_KEY"));
    		prof_conn[i].setLastModByKey(rs.getString("PROF_LASTMOD_BY_KEY"));
    		prof_conn[i].setCreated(rs.getTimestamp("PROF_CREATED"));
    		prof_conn[i].setLastMod(rs.getTimestamp("PROF_LASTMOD"));
    		prof_conn[i].setStatus(rs.getInt("PROF_STATUS"));
    		prof_conn[i].setMessage(rs.getString("PROF_MESSAGE"));
    		prof_conn[i].setType(rs.getString("PROF_TYPE"));
    		i++;
    	}
                
		stmt.close();
		rs.close();

		PreparedStatement prepStmt = con.prepareStatement("INSERT INTO EMPINST.PROF_CONNECTIONS (PROF_CONNECTION_ID, PROF_SOURCE_KEY, PROF_TARGET_KEY, PROF_CREATED_BY_KEY, PROF_LASTMOD_BY_KEY, PROF_LASTMOD, PROF_CREATED, PROF_STATUS, PROF_MESSAGE, PROF_TYPE) VALUES (?,?,?,?,?,?,?,?,?,?)");
        int count = 0;	
        String srcKey = null, tgtKey = null, oldKey = null;;
		

		for (i = 0; i < numConnections; i++) {
			String newKey = java.util.UUID.randomUUID().toString();
	        int k = 1;

	        oldKey = prof_conn[i].getConnectionId();
	        srcKey = prof_conn[i].getSourceKey();
			tgtKey = prof_conn[i].getTargetKey();

			count++;
			prepStmt.clearParameters();
       
			// update all params
			prepStmt.setString(k++, newKey);
			prepStmt.setString(k++, tgtKey);
			prepStmt.setString(k++, srcKey);
			prepStmt.setString(k++, prof_conn[i].getCreatedByKey());
			prepStmt.setString(k++, prof_conn[i].getLastModByKey());
			prepStmt.setTimestamp(k++, new Timestamp(prof_conn[i].getCreated().getTime()));
			prepStmt.setTimestamp(k++, new Timestamp(prof_conn[i].getLastMod().getTime()));
			switch (prof_conn[i].getStatus()) {
			case 0:
				prepStmt.setInt(k++, 2);
				break;
			case 1:
				prepStmt.setInt(k++, 1);
				break;
			case 2:
				prepStmt.setInt(k++, 0);
				break;
			}
			prepStmt.setString(k++, prof_conn[i].getMessage());
			prepStmt.setString(k++, prof_conn[i].getType());
			
			try {
					prepStmt.execute();
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				System.out.println("Original record: PROF_CONNECTION_ID: " + oldKey +
						" PROF_SOURCE_KEY: " + srcKey +
						" PROF_TARGET_KEY: " + tgtKey
						);
				System.out.println(" ");
				}
			
			if ((count / 1000) * 1000 == count) {
				System.out.println("Processed " + count + " entries");
				con.commit();
			}
		}
		prepStmt.close();
		rs.close();
		con.commit();
	}

}
