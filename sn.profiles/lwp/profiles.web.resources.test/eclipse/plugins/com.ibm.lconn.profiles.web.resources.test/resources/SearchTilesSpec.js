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
 * Jasmine spec for lconn.profiles.SearchTiles
 * 
 * @namespace lconn.profiles.test.SearchTilesSpec
 */
dojo.provide("lconn.profiles.test.SearchTilesSpec");
dojo.require("lconn.profiles.SearchTiles");

(function(SearchTiles) {
   var widget;
   beforeEach(function() {
      widget = new SearchTiles();
   });
   describe("the interface of lconn.profiles.SearchTiles", function() {
      it("implements the expected methods", function() {
         expect(widget.reset).toEqual(jasmine.any(Function));
         expect(widget.setValue).toEqual(jasmine.any(Function));
         expect(widget.setResults).toEqual(jasmine.any(Function));
      });
      it("implements the expected properties", function() {
         expect(widget.profilesSvcLocation).toBeDefined();
         expect(widget.expandThumbnails).toBeDefined();
      });
   });
}(lconn.profiles.SearchTiles));
