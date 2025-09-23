/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfilesTDIState {

	private static final Map<String, Object> lcTDIGlobalState = new ConcurrentHashMap<String, Object>();

	// gets an object and ensure that it is of the expected type
	// - you can use something like this to use this class to store other types of state
	private static <T> T _getObject(String objName, Class<T> expectedType) {
		Object val = lcTDIGlobalState .get(objName);
		if (val == null || !expectedType.isAssignableFrom((Class<?>) val.getClass())) {
			return null;
		}
		return (T) val;
	}

	// clear shared state
	// put into method for consistent handling in the future
	private static void _clearObject(String objName) {
		lcTDIGlobalState.remove(objName);
	}

	// create a counter & clear any old values
	public static AtomicInteger createCounter(String counterName) {
		AtomicInteger counter = new AtomicInteger(0);
		lcTDIGlobalState.put(counterName, counter);
		return counter;
	}

	// clear counter
	public static void clearCounter(String counterName) {
		_clearObject(counterName);
	}

	// Gets & counter and ensure that the counter is not null
	// - This allows for 'dumb' coding in the increment & decrement functions
	private static AtomicInteger _counter(String counterName) {
		AtomicInteger counter = _getObject(counterName, AtomicInteger.class);
		if (counter == null) {
			counter = createCounter(counterName);
		}
		return counter;
	}

	// increment & get the new counter value
	public static int incrementCounter(String counterName) {
		return _counter(counterName).incrementAndGet();
	}

	// decrement & get the new counter value
	public static int decrementCounter(String counterName) {
		return  _counter(counterName).decrementAndGet();
	}

	// gets the current state of a counter
	public static int getCounterState(String counterName) {
		return _counter(counterName).get();
	}

	private static String _string(String stringName) {
		String str = _getObject(stringName, String.class);
		if (str == null) {
			str = createString(stringName);
		}
		return str;
	}

	// create a string & clear any old values
	public static String createString(String stringName) {
		String str = new String("");  // default to empty string
		lcTDIGlobalState.put(stringName, str);
		return str;
	}

	// gets the current state of a string
	public static String getString(String stringName) {
		return _string(stringName);
	}

	// gets the current state of a string
	public static String setString(String stringName, String stringVal) {
		lcTDIGlobalState.put(stringName, stringVal);
		return stringVal;
	}


	// clear counter
	public static void clearString(String stringName) {
		_clearObject(stringName);
	}

	}
