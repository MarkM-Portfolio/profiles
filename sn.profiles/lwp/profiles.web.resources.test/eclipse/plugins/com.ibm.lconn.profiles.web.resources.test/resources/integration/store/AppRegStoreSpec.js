/* *************************************************************** */
/*                                                                 */
/* HCL Confidential                                                */
/*                                                                 */
/* OCO Source Materials                                            */
/*                                                                 */
/* Copyright HCL Technologies Limited 2021                         */
/*                                                                 */
/* The source code for this program is not published or otherwise  */
/* divested of its trade secrets, irrespective of what has been    */
/* deposited with the U.S. Copyright Office.                       */
/*                                                                 */
/* *************************************************************** */

/*
 * Jasmine spec for lconn.profiles.AppRegStore
 *
 * @namespace lconn.profiles.test.AppRegStoreSpec
 */
dojo.provide("lconn.profiles.test.AppRegStoreSpec");
dojo.require("lconn.profiles.AppRegStore");

(function (AppRegStore) {
  var appRegStore;
  beforeEach(function () {
    appRegStore = new AppRegStore.instance();
  });
  describe("the interface of lconn.profiles.AppRegStore.instance", function () {
    it("implements the expected methods", function () {
      expect(appRegStore.isWebmeetingEnabled).toEqual(jasmine.any(Function));
      expect(appRegStore.getWebmeetingUrl).toEqual(jasmine.any(Function));
      expect(appRegStore.getWebmeetingTarget).toEqual(jasmine.any(Function));
      expect(appRegStore.getWebmeetingLabel).toEqual(jasmine.any(Function));
      expect(appRegStore.getWebmeetingIcon).toEqual(jasmine.any(Function));
    });
  });
})(lconn.profiles.AppRegStore);
