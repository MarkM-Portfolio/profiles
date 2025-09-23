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
 * Jasmine spec for lconn.profiles.widgets._BaseData
 * 
 * @namespace lconn.profiles.test.widget.spec
 */
dojo.provide("lconn.profiles.test.widgets.spec");

dojo.require("lconn.profiles.widgets.utils");
dojo.require("lconn.profiles.widgets._Base");
dojo.require("lconn.profiles.widgets._BaseData");



(function(utils) {
	describe("the interface of lconn.profiles.widgets.utils", function() {
		it("implements the expected methods", function() {
			expect(utils.XmlEncoder.encode).toEqual(jasmine.any(Function));
			expect(utils.XmlEncoder.decode).toEqual(jasmine.any(Function));
			expect(utils.PlusEncoder.encode).toEqual(jasmine.any(Function));
			expect(utils.PlusEncoder.decode).toEqual(jasmine.any(Function));
			expect(utils.hasPermission).toEqual(jasmine.any(Function));
		});
	});

	
	//stuff needed to test the methods
	window.profilesData = {
		enabledPermissions: [
			"profile.tag$profile.tag.view",
			"profile.reportTo$profile.reportTo.view"
		]
	};
	var _xmlEncodedString = "&lt;&gt;&apos;&quot;&amp;&lt;&gt;&apos;&quot;&amp;";
	var _xmlDecodedString = "<>'\"&<>'\"&";
	var _plusEncodedString = "&plus;&plus;";
	var _plusDecodedString = "++";	
	
	describe("the methods of lconn.profiles.widgets.utils", function() {
		it("hasPermission method functions properly", function() {
			expect(utils.hasPermission("profile.reportTo$profile.reportTo.view")).toBe(true);
			expect(utils.hasPermission("profile.tag$profile.tag.view")).toBe(true);
			expect(utils.hasPermission("profile.tag.view")).toBe(true);
			expect(utils.hasPermission(["profile.tag","profile.tag.view"])).toBe(true);
			expect(utils.hasPermission("profile.tag$profile.tag.add")).toBe(false);
		});
		
		it("XmlEncoder.encode method functions properly", function() {
			expect(utils.XmlEncoder.encode(_xmlDecodedString)).toBe(_xmlEncodedString);
		});
		
		it("XmlEncoder.decode method functions properly", function() {
			expect(utils.XmlEncoder.decode(_xmlEncodedString)).toBe(_xmlDecodedString);
		});
		
		it("PlusEncoder.encode method functions properly", function() {
			expect(utils.PlusEncoder.encode(_plusDecodedString)).toBe(_plusEncodedString);
		});
		
		it("PlusEncoder.decode method functions properly", function() {
			expect(utils.PlusEncoder.decode(_plusEncodedString)).toBe(_plusDecodedString);
		});	
	});	
}(lconn.profiles.widgets.utils));


(function(_Base) {
	var widget;
	beforeEach(function() {
		widget = new _Base({templateString: "<span></span>"});
	});
	
	describe("the interface of lconn.profiles.widgets._Base", function() {
		it("implements the expected methods", function() {
			expect(widget.showLoading).toEqual(jasmine.any(Function));
			expect(widget.enforceBidi).toEqual(jasmine.any(Function));
			expect(widget.showNode).toEqual(jasmine.any(Function));
			expect(widget.showMessage).toEqual(jasmine.any(Function));
			expect(widget.showWaitCursor).toEqual(jasmine.any(Function));
			expect(widget.onError).toEqual(jasmine.any(Function));
		});
	
		it("implements the expected properties", function() {
			expect(widget.profilesSvcLocation).toBeDefined();
			expect(widget.hiddenClassName).toBeDefined();
			expect(widget.strings).toBeDefined();
		});
	});
}(lconn.profiles.widgets._Base));


(function(_BaseData) {
	var widget;
	beforeEach(function() {
		widget = new _BaseData({templateString: "<span></span>", userKey: "xx-test-xx"});
	});
	
	describe("the interface of lconn.profiles.widgets._BaseData", function() {
		it("implements the expected methods", function() {
			expect(widget.xhr).toEqual(jasmine.any(Function));
		});
		
		it("implements the expected properties", function() {
			expect(widget.userKey).toBeDefined();
		});
	});
}(lconn.profiles.widgets._BaseData));