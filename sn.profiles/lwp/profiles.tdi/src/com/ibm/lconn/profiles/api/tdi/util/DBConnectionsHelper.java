/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.ibm.di.function.UserFunctions;

public class DBConnectionsHelper {
	static Properties tdiProperties;
	private static final String TDI_PROPERTIES_FILE="profiles_tdi.properties";
	private static final String JDBC_DRIVER="dbrepos_jdbc_driver";
	private static final String JDBC_URL="dbrepos_jdbc_url";
	private static final String USER_NAME="dbrepos_username";
	public static final String PASSWORD="dbrepos_password";
	private static final String SYNC_HASH_FIELD="sync_updates_hash_field";
	private static final String HEADLESS_TDI_SCRIPTS = "headless_tdi_scripts";
	private static final String SYNC_STORE_SOURCE_URL = "sync_store_source_url";
	private static final String SYNC_SOURCE_URL_OVERRIDE = "sync_source_url_override";
	private static final String SYNC_SOURCE_URL_ENFORCE = "sync_source_url_enforce";
	private static final String SYNC_UPDATES_SHOW_SUMMARY_ONLY = "sync_updates_show_summary_only";
	
	static{
		FileInputStream props_in;
		try{
			props_in = new FileInputStream(TDI_PROPERTIES_FILE);
			tdiProperties = new Properties();
			tdiProperties.load(props_in);
			props_in.close();
		}catch(IOException e){
			//TODO exception handling
			System.out.println(e);
		}
	}
	
	public static String getDriverClassName(){
		String value = "";
		if(tdiProperties.get(JDBC_DRIVER)!=null)
			value = (String)tdiProperties.get(JDBC_DRIVER);
		return value;
	}
	
	public static String getDBUrl(){
		String value = "";
		if(tdiProperties.get(JDBC_URL)!=null)
			value = (String)tdiProperties.get(JDBC_URL);
		return value;
	}
	
	public static String getUsername(){
		String value = "";
		if(tdiProperties.get(USER_NAME)!=null)
			value = (String)tdiProperties.get(USER_NAME);
		return value;
	}
	
	private static volatile String password;
	public static String getPassword(){
		String value = "";
		if (password == null) {
			UserFunctions userFun = new UserFunctions();
			try {
				value = (String)userFun.getTDIProperty("profiles", PASSWORD);
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;			
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} else {
			value = password;
		}
		return value;
	}
	
	public static void setPassword(String pw) {
		password = pw;
	}
	
	public static String getSyncHashField(){
		return tdiProperties.getProperty(SYNC_HASH_FIELD, "uid");
	}

	public static String getHeadlessTDIScripts(){
		return tdiProperties.getProperty(HEADLESS_TDI_SCRIPTS, "false");
	}

	public static String getSyncStoreSourceURL(){
		return tdiProperties.getProperty(SYNC_STORE_SOURCE_URL, "true");
	}
	
	public static String getSyncSourceURLOverride(){
		return tdiProperties.getProperty(SYNC_SOURCE_URL_OVERRIDE, "false");
	}
	
	public static String getSyncUpdateShowSummaryOnly(){
		return tdiProperties.getProperty(SYNC_UPDATES_SHOW_SUMMARY_ONLY, "false");
	}
	
	public static String getSyncSSourceURLEnforce(){
		return tdiProperties.getProperty(SYNC_SOURCE_URL_ENFORCE, "false");
	}
}
