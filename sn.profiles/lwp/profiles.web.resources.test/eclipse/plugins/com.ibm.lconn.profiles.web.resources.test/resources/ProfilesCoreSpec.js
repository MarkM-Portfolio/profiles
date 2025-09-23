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
 * Jasmine spec for lconn.profiles.ProfilesCore
 *
 * @namespace lconn.profiles.test.ProfilesCoreSpec
 */
dojo.provide("lconn.profiles.test.ProfilesCoreSpec");
dojo.require("lconn.profiles.ProfilesCore");

(function (ProfilesCore) {
  describe("the interface of lconn.profiles.ProfilesCore.instance", function () {
    it("implements the expected methods", function () {
      expect(ProfilesCore.extractProfileAttribute).toEqual(
        jasmine.any(Function)
      );
      expect(ProfilesCore.isAnyWebmeetingServiceEnabled).toEqual(
        jasmine.any(Function)
      );
      expect(ProfilesCore.isAnyWebmeetingServiceAvailable).toEqual(
        jasmine.any(Function)
      );
      expect(ProfilesCore.isExtensionWebmeetingAvailable).toEqual(
        jasmine.any(Function)
      );
      expect(ProfilesCore.getExtensionWebmeetingLabel).toEqual(
        jasmine.any(Function)
      );
      expect(ProfilesCore.openExtensionWebmeeting).toEqual(
        jasmine.any(Function)
      );
    });
  });
})(lconn.profiles.ProfilesCore);
