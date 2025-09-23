/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.data;

import com.ibm.lconn.profiles.data.AbstractDataObject;

/**
 * @author zhouwen_lu@us.ibm.com
 *
 */
public class EventLogEntryCollection extends AbstractDataObject<EventLogEntryCollection> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3803975468474748055L;

	private EventLogEntry[] _eventLogs;

	private int _total;

	public EventLogEntry[] getEventLogs() {
		return _eventLogs;
	}

	public void setEvents(EventLogEntry[] eventLogs) {
		_eventLogs = eventLogs;
	}

	public int getTotal() {
		return _total;
	}

	public void setTotal(int total) {
		_total = total;
	}	
}
