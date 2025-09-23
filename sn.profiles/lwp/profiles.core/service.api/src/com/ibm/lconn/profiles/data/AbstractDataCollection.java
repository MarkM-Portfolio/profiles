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

import java.util.List;

/**
 * @author ahernm
 *
 */
public abstract class AbstractDataCollection
<CT extends AbstractDataCollection<?, DT, RO>,  DT extends Object, RO extends AbstractRetrievalOptions<RO>> 
{
	private static final long serialVersionUID = 2042325418557927943L;
	
	private final List<DT> results;
	private final RO nextSet;
	
	protected AbstractDataCollection(List<DT> results, RO nextSet) {
		this.results = results;
		this.nextSet = nextSet;
	}

	/**
	 * @return the results
	 */
	public final List<DT> getResults() {
		return results;
	}

	/**
	 * @return the nextSet
	 */
	public final RO getNextSet() {
		return nextSet;
	}
	
	/**
	 * Convenience method to check if additional entires exist
	 * 
	 * @return
	 */
	public final boolean hasMore() {
		return (this.nextSet != null);
	}
	
}
