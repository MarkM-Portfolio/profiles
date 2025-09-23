/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/*
 * Jasmine spec for lconn.profiles.widgets.linkRoll.Container
 * 
 * @namespace lconn.profiles.test.widget.linkRoll.spec
 */
dojo.provide("lconn.profiles.test.widgets.linkRoll.spec");

dojo.require("lconn.profiles.widgets.linkRoll.Item");
dojo.require("lconn.profiles.widgets.linkRoll.Control");
dojo.require("lconn.profiles.widgets.linkRoll.Container");



(function(LinkRollItem) {

	var widget;
	beforeEach(function() {
		widget = new LinkRollItem({
			linkName: "IBM",
			linkUrl: "http://www.ibm.com"
		});
	});

	describe("the interface of lconn.profiles.widgets.linkRoll.Item", function() {
		it("implements the expected methods", function() {
			expect(widget.removeLink).toEqual(jasmine.any(Function));
		});

		it("implements the expected properties", function() {
			expect(widget.iconUrl).toBeDefined();
			expect(widget.linkName).toBeDefined();
			expect(widget.linkUrl).toBeDefined();
			expect(widget.canRemoveLinks).toBeDefined();
		});
	});
}(lconn.profiles.widgets.linkRoll.Item));


(function(LinkRollControl) {

	var widget;
	beforeEach(function() {
		widget = new LinkRollControl();
	});
	
	describe("the interface of lconn.profiles.widgets.linkRoll.Control", function() {
		it("implements the expected methods", function() {
			expect(widget.getLinks).toEqual(jasmine.any(Function));
			expect(widget.renderLinks).toEqual(jasmine.any(Function));
			expect(widget.saveAddLink).toEqual(jasmine.any(Function));
			expect(widget.cancelAddLink).toEqual(jasmine.any(Function));
			expect(widget.removeLink).toEqual(jasmine.any(Function));
			expect(widget.showAddForm).toEqual(jasmine.any(Function));
			expect(widget.resetLinkData).toEqual(jasmine.any(Function));
			expect(widget.addLinkData).toEqual(jasmine.any(Function));
			expect(widget.isValidLink).toEqual(jasmine.any(Function));
		});

		it("implements the expected properties", function() {
			expect(widget.canAddLinks).toBeDefined();
			expect(widget.canRemoveLinks).toBeDefined();
			expect(widget._data).toBeDefined();
		});
				
		it("add new link data and validate for duplicate names and urls", function() {
			widget.resetLinkData();
			widget.addLinkData("IBM", "http://www.ibm.com");
			widget.addLinkData("IBM W3", "http://w3.ibm.com");
			
			expect(widget._data.length).toBe(2);	//make sure there are 2 entries
			
			expect(widget.isValidLink("IBM", "http://ibm.com")).toBe(false);  //dupe name
			expect(widget.isValidLink("IBM Intranet", "http://w3.ibm.com")).toBe(false); //dupe url
			expect(widget.isValidLink("IBM2", "www.ibm.com")).toBe(false); //dupe url
			expect(widget.isValidLink("IBM2", "HTTP://WWW.IBM.COM")).toBe(false);  //dupe url case-insensitive
			
			expect(widget.isValidLink("IBM3", "http://ibm.com")).toBe(true);	//not a dupe

		});
	});
}(lconn.profiles.widgets.linkRoll.Control));


(function(LinkRollContainer) {
	var widget;
	beforeEach(function() {
		widget = new LinkRollContainer();
	});

	describe("the interface of lconn.profiles.widgets.linkRoll.Container", function() {
		it("implements the expected methods", function() {
			expect(widget.onLoad).toEqual(jasmine.any(Function));
			expect(widget.onUnload).toEqual(jasmine.any(Function));
			expect(widget.onview).toEqual(jasmine.any(Function));
		});
	});
}(lconn.profiles.widgets.linkRoll.Container));
