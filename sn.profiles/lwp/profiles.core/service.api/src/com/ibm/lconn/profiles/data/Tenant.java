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

package com.ibm.lconn.profiles.data;

import java.util.Date;
import java.util.Locale;

public class Tenant extends AbstractDataObject<Tenant> {

	public final static int STATE_ACTIVE = 0;
	public final static int STATE_INACTIVE = 1;
	public static final String SINGLETENANT_KEY = "a";
	public final static String IGNORE_TENANT_KEY = "IGNORE_TENANTKEY";
	public final static String NULLTENANT_KEY = "0";
	
	// used only by db
	public final static String DB_SINGLETENANT_KEY = "00000000-0000-0000-0000-040508202233";
	
	private static final long serialVersionUID = -3305042221439304287L;
	// tenantKey is in base class
	private String exid;
	private String name;
	private String lowercaseName;
	private int    state;
	private Date   created;
	private Date   lastUpdate;

	/**
	 * @return Returns the object external uid.
	 */
	public String getExid(){
		return exid;
	}

	/**
	 * @param uid Set the object external uid.
	 */
	public void setExid(String exid){
		this.exid = exid;
	}

	/**
	 * @return Returns the tenant name.
	 */
	public String getName(){
		return name;
	}

	/**
	 * @param name Set the tenant name.
	 */
	public void setName(String name){
		if ( name != null && name.equals("") == false){
			this.name = name;
			// see lowercasing in the Employee object. it uses ENGLISH locale.
			this.lowercaseName = name.toLowerCase(Locale.ENGLISH);
		}
	}

	// typically used by data access getters
	public void setLowercaseName(String lcname){
		this.lowercaseName = lcname;
	}

	public String getLowercaseName(){
		return lowercaseName;
	}

	/**
	 * @return Returns the tenant status.
	 */
	public int getState(){
		return state;
	}

	/**
	 * @param uid Set the tenant status.
	 */
	public void setState(int state){
		this.state = state;
	}

	/**
	 * @return Returns the object create date.
	 */
	public Date getCreated(){
		return created;
	}

	/**
	 * @param created Set the object create date.
	 */
	public void setCreated(Date created){
		this.created = created;
	}

	/**
	 * @return Returns the object last modified date.
	 */
	public Date getLastUpdate(){
		return lastUpdate;
	}

	/**
	 * @param lastMod Set the object last modified date.
	 */
	public void setLastUpdate(Date lastMod){
		this.lastUpdate = lastMod;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("exid: " + exid);
		sb.append(" tenantKey: " + getTenantKey());
		sb.append(" name: " + name);
		sb.append(" lowercaseName: " + lowercaseName);
		sb.append(" state: " + state);
		sb.append(" created: " + created);
		sb.append(" lastUpdate: " + lastUpdate);
		return sb.toString();
	}
}
