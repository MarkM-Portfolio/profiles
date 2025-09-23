/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;

/**
 * Abstract retrieval options. The paramatized type is used so that the various
 * methods can return references to the parent object
 * 
 * @author ahernm
 */
public abstract class AbstractRetrievalOptions<T extends AbstractRetrievalOptions<?>>
	extends AbstractDataObject<T>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6462822886255780866L;
	
	private int pageSize;
	
	/**
	 * Require types to define default page sizes
	 * 
	 * @param defaultPageSize
	 */
	protected AbstractRetrievalOptions(int defaultPageSize) {
		this.pageSize = defaultPageSize;
	}
	
	/**
	 * @return the pageSize
	 */
	public final int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public T setPageSize(int size) {
		this.pageSize = Math.max(1, size); // to prevent user errors
		return getSelf();
	}
	
	/**
	 * Utility method to return a typed self reference
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private final T getSelf() {
		return (T) this;
	}
}
