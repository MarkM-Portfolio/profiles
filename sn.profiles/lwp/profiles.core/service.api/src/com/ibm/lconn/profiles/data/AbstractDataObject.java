/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Extend SNAX utility class to allow for reuse of data object unit test
 * 
 *
 */
public abstract class AbstractDataObject<T extends AbstractDataObject<?>> implements Serializable, Cloneable {
	private static final long serialVersionUID = 6491231651432156876L;
	private String tenantKey;
	// dbTenantKey is used for database access - the db holds the intenral/old version of the single tenant id.
	private String dbTenantKey;

	public String getTenantKey() {
		return tenantKey;
	}

	public void setTenantKey(String tenantKey) {
		if (Tenant.DB_SINGLETENANT_KEY.equals(tenantKey)){
			this.tenantKey = Tenant.SINGLETENANT_KEY;
		}
		else{
			this.tenantKey = tenantKey;
		}
	}
	
	// should only be used by the dao layer
	public String getDbTenantKey() {
		return dbTenantKey;
	}
	
	// should only be used by the dao layer
	public void setDbTenantKey(String dbTenantKey) {
		this.dbTenantKey = dbTenantKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T clone() {
		try {
			return (T) super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException(e); // Should not be reachable unless bug in referenced class
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
