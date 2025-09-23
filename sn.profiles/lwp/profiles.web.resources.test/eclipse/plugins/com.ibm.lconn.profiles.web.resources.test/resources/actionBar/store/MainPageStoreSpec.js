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
 * Jasmine spec for lconn.profiles.MainPageStore
 *
 * @namespace lconn.profiles.test.MainPageStoreSpec
 */
dojo.provide("lconn.profiles.test.MainPageStoreSpec");
dojo.require("lconn.profiles.MainPageStore");

(function (MainPageStore) {
  describe("the interface of lconn.profiles.MainPageStore.instance", function () {
    it("implements the expected methods", function () {
      expect(MainPageStore.loadData).toEqual(jasmine.any(Function));
      expect(MainPageStore.openWebmeeting).toEqual(jasmine.any(Function));
    });
  });
})(lconn.profiles.MainPageStore);
