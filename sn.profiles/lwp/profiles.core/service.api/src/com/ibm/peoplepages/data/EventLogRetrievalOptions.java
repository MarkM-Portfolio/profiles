/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.AbstractRetrievalOptions;

/**
 * This class is intended to expose retrieval options that align with the indexing
 * on the EVENTLOG table. To that end, not all event fields are available for search.
 * The primary search mechanisms are by date and type, which type (an int) always
 * intended to be used over name (a string). Event key is the PK and is used for
 * some searches. Also, the paging support is via the simple mechanism supported
 * in sn.infra lc.appext/core/api snax-utils.xml. Those paging constructs provide
 * a limit while the app provides the ordering column. At this point in time
 * profiles does not require this refinement.
 */
public class EventLogRetrievalOptions extends AbstractRetrievalOptions<EventLogRetrievalOptions> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7211377800588858845L;

	private static final String CREATED_SORT_COLUMN = "EVENTLOG.CREATED";

	private static final String DEFAULT_SORT_COLUMN = CREATED_SORT_COLUMN;

	//not an indexed column 
	//private static final String CREATED_BY_KEY = "createdByKey";

	// not currently used
	//private static final String EVENT_KEY = "eventKey";

	private static final String EVENT_KEYS = "eventKeys";

	// dao layer used to use ibatis paging which could be quite inefficient on the
	// EVENTLOG table with high row count. To date counts always started at 0 and
	// the mechanism is not needed.
	// private int _skipResults = 0;
	
	private int maxPurge = -1;

	/*
	 * Internal options map
	 */
	protected Map<String, Object> optionsMap = new HashMap<String, Object>();

	public EventLogRetrievalOptions() {
		super(PropertiesConfig.getInt(ConfigProperty.EVENT_LOG_PURGE_BATCH_SIZE));
	}

	public EventLogRetrievalOptions(int pageSize) {
		super(pageSize);
	}

	/*
	 * Set value
	 */
	protected final void put(String key, Object value) {
		optionsMap.put(key, value);
	}

	/*
	 * Useful to force object casting of <Class>
	 */
	@SuppressWarnings("unchecked")
	protected final <RetType extends Object> RetType get(Class<RetType> cls, String key) {
		return (RetType) optionsMap.get(key);
	}

	/*
	 * Useful to force object casting of <Class>
	 */
	@SuppressWarnings("unchecked")
	protected final <RetType extends Object> RetType get(String key, RetType defaultValue) {
		RetType retValue = (RetType) optionsMap.get(key);
		if (retValue == null) {
			return defaultValue;
		}
		else {
			return retValue;
		}
	}

	// not currently used
	//public String getEventKey() {
	//	String value = get(String.class, EVENT_KEY);
	//	return value;
	//}

	//public void setEventKey(String value) {
	//	put(EVENT_KEY, value);
	//}

	@SuppressWarnings("unchecked")
	public List<String> getEventKeys() {
		return get(List.class, EVENT_KEYS);
	}

	public void setEventKeys(List<String> values) {
		put(EVENT_KEYS, values);
	}

	//not an indexed column
	//public String getCreatedByKey() {
	//	String value = get(String.class, CREATED_BY_KEY);
	//	return value;
	//}

	//public void setCreatedByKey(String value) {
	//	put(CREATED_BY_KEY, value);
	//}

	public int getMaxResults() {
		return getPageSize();
	}

	public void setMaxResults(int count) {
		this.setPageSize(count);
	}

	// EVENTLOG CREATED_BY columns are not indexed
	//private static final String OBJECTKEY = "objectKey";

	//public String getObjectKey() {
	//	String value = get(String.class, OBJECTKEY);
	//	return value;
	//}

	//public void setObjectKey(String value) {
	//	put(OBJECTKEY, value);
	//}

	// EVENTLOG CREATED_BY columns are not indexed
	//private static final String CREATORS = "creators";

	//public void addCreators(ArrayList<String> creators) {
	//	if (creators == null || creators.size() == 0) return;
	//	ArrayList<String> _creators = get(ArrayList.class, CREATORS);
	//	if (_creators != null) {
	//		// _creators.clear();
	//	}
	//	else {
	//		_creators = new ArrayList<String>();
	//	}
	//	_creators.addAll(creators);
	//	put(CREATORS, _creators);
	//}
	
	//public String[] getCreators() {
	//	ArrayList<?> creators = get(ArrayList.class, CREATORS);
	//	if (creators == null) return null;
	//
	//	String[] _creators = creators.toArray(new String[creators.size()]);
	//	return _creators;
	//}

	// EVENTLOG EVENT_NAME column is not indexed nor should be. use EVENT_TYPE
	// private static final String EVENTNAMES = "eventNames";

	//public void setEventNames(ArrayList eventNames) {
	//	if (eventNames == null || eventNames.size() == 0) {
	//		return;
	//	}
	//
	//	put(EVENTNAMES, eventNames);
	//}
	
	//public ArrayList<String> getEventNames() {
	//	ArrayList eventNames = get(ArrayList.class, EVENTNAMES);
	//	if (eventNames == null){
	//		return null;
	//	}
	//	return eventNames;
	//}
	
	private static final String EVENTTYPES = "eventTypes";

	public ArrayList<Integer> getEventTypes() {
		ArrayList<Integer> eventTypes = get(ArrayList.class, EVENTTYPES);
		if (eventTypes == null) {
			return null;
		}
		return eventTypes;
	}

	public void setEventTypes(ArrayList<Integer> eventTypes) {
		if (eventTypes == null || eventTypes.size() == 0){
			return;
		}
		put(EVENTTYPES, eventTypes);
	}

	public void addEventType(Integer eventType) {
		if (eventType == null){
			return;
		}
		ArrayList<Integer> eventTypes = get(ArrayList.class, EVENTTYPES);
		if (eventTypes != null) {
			// _eventTypes.clear();
		}
		else {
			eventTypes = new ArrayList<Integer>();
		}
		eventTypes.add(eventType);
		
		put(EVENTTYPES, eventTypes);
	}

	private static final String STARTDATE = "startDate";

	public Date getStartDate() {
		return get(Date.class, STARTDATE);
	}

	public void setStartDate(Date startDate) {
		put(STARTDATE, startDate);
	}

	private static final String ENDDATE = "endDate";

	public Date getEndDate() {
		return get(Date.class, ENDDATE);
	}

	public void setEndDate(Date endDate) {
		put(ENDDATE, endDate);
	}

	public static class IncludeFlag {
		public static final int NO = 0;
		public static final int YES = 1;
		public static final int ONLY = 2;
	}

	// EVENTLOG ISPRIVATE columns are not indexed nor is the notion of private events used in Profiles
	// private static final String INCLUDEPUBLIC = "includePublic";

	//public void setIncludePublic(int value) {
	//	put(INCLUDEPUBLIC, (new Integer(value)).toString());
	//}
	
	//public int getIncludePublic() {
	//	Integer value = new Integer(get(Integer.class, INCLUDEPUBLIC));
	//	if (value == null)
	//		return IncludeFlag.YES;
	//	else
	//		return value.intValue();
	//}
	
	// EVENTLOG ISSYSEVENT setting not currently used - there are multiple types
	// of sysevents now and we'd have to understand the usage. only ProcessTDIEventsTask
	// processes some system events and it has special queries. the behavior for the
	// rest of the app is to not retrieve the tdi related events.
	//private static final String INCLUDESYSTEM = "includeSystem";

	//public int getIncludeSystem() {
	//	Integer value = new Integer(get(Integer.class, INCLUDESYSTEM));
	//	if (value == null)
	//		return IncludeFlag.YES;
	//	else
	//		return value.intValue();
	//}
	
	//public void setIncludeSystem(int value) {
	//	put(INCLUDESYSTEM, (new Integer(value)).toString());
	//}

	// EVENTLOG ISSYSEVENT setting not currently used - there are multiple types
	// of sysevents now and we'd have to understand the usage. only ProcessTDIEventsTask
	// processes some system events and it has special queries. the behavior for the
	// rest of the app is to not retrieve the tdi related events.
	//private static final String SYSEVENT = "sysEvent";

	//public void setSysEvent(int value) {
	//	put(SYSEVENT, value);
	//}
	
	//public int getSysEvent() {
	//	Integer value = new Integer(get(Integer.class, SYSEVENT));
	//	if (value == null){
	//		return 0;
	//	}
	//	else{
	//		return value.intValue();
	//	}
	//}
	
	/**
	 * @param maxPurge
	 *            the maxPurge to set
	 */
	public final void setMaxPurge(int maxPurge) {
		this.maxPurge = maxPurge;
	}
	
	/**
	 * @return the maxPurge
	 */
	public final int getMaxPurge() {
		return maxPurge;
	}
}