/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/**
 * @fileoverview A Profiles Colleague Fetcher network module for Profiles.
 * Gets all Profiles users who have the same manager as the user.
 *
 * @author: Scott Murphree
 */
sameManager = new Network("SameManager");

// This module does not need a showLogin function because the data is coming
// from the same domain.


/**
 * Gets all Profiles users who have the same manager as the user.
 * @param caller The Fetcher object in control.
 * @param xml The atom feed containing the list of colleagues.
 */
sameManager.getFriends = function (caller, xml, ioArgs) {

  if (xml) {
    caller.friends = caller.parseProfiles(xml);
    for (index = 0; index < caller.friends.length; ++index) {
      if (caller.friends[index].uid === caller.user.uid) {
        caller.friends.splice(index, 1);
        break;
      }
    }
    caller.toggleLoading(false);
    if (caller.friends.length > 0) {
		    caller.searchProfiles('email', 0);
    } else {
      throw {
        message: "No one else is under your manager.",
        toString: function() { return this.message; }
      };
    }
  } else {
    url = '/profiles/atom/peopleManaged.do';
    params = {'uid': caller.user.managerUid};

    fetcherUtils.getXML(url, params, sameManager.getFriends, caller);
  }
  
};

// Add the module to active network list
Fetcher_networks.nets.push(sameManager);
