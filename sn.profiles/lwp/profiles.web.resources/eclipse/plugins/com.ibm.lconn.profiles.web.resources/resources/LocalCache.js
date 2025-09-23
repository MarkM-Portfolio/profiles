/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

(function(window, document) {
	/**
	* Convenient wrapper around DOM Storage APIs for local persistence of
	* various profile cached values.
	* @namespace lconn.profiles.LocalCache
	* @author Bob Barber <bbarber@us.ibm.com>
	*/
	var cache = dojo.provide("lconn.profiles.LocalCache");

	var PREF_KEY = "icprofilecache";

	function get_() {
		return dojo.fromJson(localStorage.getItem(PREF_KEY)) || {};
	}
	function set_(val) {
		localStorage.setItem(PREF_KEY, dojo.toJson(val));
	}
	function unset_() {
		localStorage.removeItem(PREF_KEY);
	}

	dojo.mixin(cache, /** @lends lconn.profiles.LocalCache */ {
		/**
		* Sets or updates the value of a key in the preference cache.
		* 
		* @param {String} key
		* @param {Object} val
		*/
		set: function set(key, val) {
			var o = {};
			o[key] = val;
			set_(dojo.mixin(get_(), o));
		},
		/**
		* Removes a key from the preference cache.
		* 
		* @param {String} key
		*/
		unset: function unset(key) {
			var p = get_();
			delete p[key];
			set_(p);
		},
		/**
		* Returns the value of a key from the preference cache.
		* 
		* @param {String} key
		* @returns the value of a key from the preference cache.
		*/
		get: function get(key) {
			return get_()[key];
		},
		/**
		* Clears the preference cache.
		*/
		clear: function clear() {
			unset_();
		}
	});
   
})(window, document);