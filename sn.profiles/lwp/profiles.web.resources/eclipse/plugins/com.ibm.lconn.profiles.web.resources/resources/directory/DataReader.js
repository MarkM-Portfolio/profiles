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

dojo.provide("lconn.profiles.directory.DataReader");

dojo.require("dijit._Widget");
dojo.require("lconn.core.url");
dojo.require("lconn.core.peopleFinder.DataReader");

dojo.declare(
    "lconn.profiles.directory.DataReader",
    [lconn.core.peopleFinder.DataReader],
{
	additionalFields: {
		low: ['city','country'],
		medium: ['workPhone'],
		high: ['tag']
	},
	pageSize: 18,
	queryRunning: false,
	
	executeQuery: function(query, successCallback, errorCallback) {
		if(query) {
			this.lastQuery.page = 1;
		}
		var prevCallback = successCallback;
		successCallback = function(returnObj) {
			prevCallback(returnObj.data, returnObj.totalResults, returnObj.pageSize);
		};
		this.inherited(arguments);
	},
	
	getNextPage: function(successCallback, errorCallback) {
		if(!this.queryRunning) {
			this.queryRunning = true
			this.inherited(arguments);
		}
	},
	
	_constructLastQueryString: function() {
		return lconn.core.url.rewrite(this.lastQuery.url, {
			query: this.lastQuery.query,
			pageSize: this.lastQuery.pageSize,
			page: (this.lastQuery.page > 1 ? this.lastQuery.page : undefined),
			additionalFields: this.lastQuery.additionalFields,
			source: "ic_dir_page"
		});
	},
	
	_querySuccess: function(data, ioargs) {
		this.queryRunning = false;
		this.inherited(arguments);
	},
	
	_queryError: function(error) {
		this.queryRunning = false;
		this.inherited(arguments);
	}
});