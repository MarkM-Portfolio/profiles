/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/*
 * Jasmine spec 
 * 
 * @namespace lconn.profiles.test.directory.spec
 */
dojo.provide("lconn.profiles.test.directory.spec");

dojo.require("lconn.profiles.directory.DirectoryController");


(function(DirectoryController) {
	var widget;
	beforeEach(function() {
		widget = DirectoryController.prototype;
	});
	
	describe("the interface of lconn.profiles.directory.DirectoryController", function() {
		it("implements the expected methods", function() {
			expect(widget.showSimpleUI).toEqual(jasmine.any(Function));
			expect(widget.showAdvancedUI).toEqual(jasmine.any(Function));
		});
	
		it("implements the expected properties", function() {
			expect(widget.allowAdvancedSearch).toBeDefined();
		});
	});
}(lconn.profiles.directory.DirectoryController));