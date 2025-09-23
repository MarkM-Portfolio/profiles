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
 * Jasmine spec for lconn.profiles.ckeditor
 * 
 * @namespace lconn.profiles.test.ckeditorSpec
 */
dojo.provide("lconn.profiles.test.ckeditorSpec");
dojo.require("lconn.profiles.ckeditor");

(function(cke) {
 
   describe("the interface of lconn.profiles.ckeditor", function() {
      it("implements the expected methods", function() {
         expect(cke.init).toEqual(jasmine.any(Function));
		 expect(cke.getData).toEqual(jasmine.any(Function));
         expect(cke.checkDirty).toEqual(jasmine.any(Function));
         expect(cke.resetDirty).toEqual(jasmine.any(Function));
      });
      it("implements the expected properties", function() {
         expect(cke.config).toBeDefined();
      });
   });
}(lconn.profiles.ckeditor));
