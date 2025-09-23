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
 * Jasmine spec for llconn.profiles.profileDetails
 * 
 * @namespace lconn.profiles.test.profileDetailsSpec
 */
dojo.provide("lconn.profiles.test.profileDetailsSpec");
dojo.require("lconn.profiles.profileDetails");

(function(profileDetails) {
   var widget;
   beforeEach(function() {
      widget = new profileDetails.instance();
   });
   describe("the interface of lconn.profiles.profileDetails.instance", function() {
      it("implements the expected methods", function() {
         expect(widget.onLoad).toEqual(jasmine.any(Function));
      });
   });
}(lconn.profiles.profileDetails));
