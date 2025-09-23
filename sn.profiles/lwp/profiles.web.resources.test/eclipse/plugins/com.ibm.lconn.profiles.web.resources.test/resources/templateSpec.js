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
 * Template for Jasmine specs. Use as a starting point when writing new specs.
 * Do not require, or use as-is.
 * 
 * @namespace lconn.profiles.test.templateSpec
 */
dojo.provide("lconn.profiles.test.templateSpec");

// Require the module(s) that this test module will cover. Ideally there should
// be a 1:1 ratio between modules and test modules, but this can vary according
// to complexity.
dojo.require("lconn.foo.bar");

// Declare the object(s) under test as arguments, AMD callback style. This will
// help us refactor the specs in the future to AMD format
(function(bar) {
   // A global before callback. This will be executed before every describe() in
   // this test module
   beforeEach(function() {
      return;
   });
   // A global after callback. This will be executed after every describe() in
   // this test module
   afterEach(function() {
      return;
   });
   // A describe() used to test the compliance with an interface
   describe("the interface of lconn.foo.bar", function() {
      it("implements the expected methods", function() {
         // using dojo.isFunction
         expect(dojo.isFunction(bar.baz)).toBeTruthy();
         // using jasmine.any
         expect(bar.baz).toEqual(jasmine.any(Function));
      });
   });
   // A describe() used to test a method
   describe("the method lconn.foo.bar.baz()", function() {
      // A local before callback. This will be executed before every it() spec
      // inside this describe()
      beforeEach(function() {
         return;
      });
      // A local after callback. This will be executed after every it() spec
      // inside this describe()
      afterEach(function() {
         return;
      });
      it("returns true", function() {
         expect(bar.baz()).toBeTruthy();
      });
   });
   // Pass the object(s) under test as arguments, AMD callback style. This will
   // help us refactor the specs in the future to AMD format
}(lconn.foo.bar));
