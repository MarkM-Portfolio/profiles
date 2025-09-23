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
 * @fileoverview A Profiles Colleague Fetcher network module for Fringe
 *
 * @author: Scott Murphree
 */
 
fringe = new Network("Fringe");

/**
 * Prompts the user for a username to use for Fringe.
 * @param caller The Fetcher object in control.
 */
fringe.showLogin = function(caller) {

  // Show the form
  // Just email address
  this.form = {
    username: { 'type': 'text', 'title': 'IBM Email Address' }
  }
  caller.showForm(this.form, 'Fringe Login');
  
  // Fill the form with any info that already exists
  this.userinfo = fetcherUtils.getUserInfo(this.name);
  if (this.userinfo) {
    this.form.username.field.value = this.userinfo.email;
  }
  this.form.username.field.focus();
};

/**
 * Retrieves the user's list of friends on Fringe.
 * @param caller The Fetcher object in control.
 * @param response The xml data from Fringe
 */
fringe.getFriends = function (caller, response) {
   url = fetcherData.fringeURL + "contacts/json/";

   // Friends retrieved; process (All params)
   if (response) {
      caller.friends = [];
      //console.log(response);
      for (j=0; j<response.contacts.length; ++j) {
        console.log("fringeFriend: " + response.contacts[j].name + ", " + response.contacts[j].email);
        p = new Person(response.contacts[j].name, response.contacts[j].email);
        caller.friends.push(p);
      }
      if (caller.friends.length > 0) {
		    caller.searchProfiles('email', 0);
      } else {
        throw "Couldn't find any friends. Wrong username?";
      }
   } else {
      // Got user info, retrieve Friends xml (no response)
      if (this.userinfo) {
        caller.killForm();
		    caller.toggleLoading(false);
		    fetcherUtils.setUserInfo(this.name, this.userinfo);
        user ="user=" + this.userinfo.email;
        limit = "&limit=-1";
        connect = "&connection=confirmed";
        dojo.xhrGet({
          handleAs: 'json',
          url: url + '?' + user + connect + limit,
          error: function(data, ioArgs) {
            caller.error(data, ioArgs.xhr.status);
          },
          load: function(data, ioArgs) {
            fringe.getFriends(caller, data);
          }
        });
      } else {
        // Initial call (no params or response)
        this.userinfo = fetcherUtils.getUserInfo(this.name);
        
        //If the login for is open get the info from it
        if (this.form) {
          this.userinfo = {email: this.form.username.field.value};
          this.form = null;
        }
        if (this.userinfo) {
          fringe.getFriends(caller);
        } else {
          // No info available, prompt with login
          fringe.showLogin(caller);
        }
      }
   }
};

// Add Fringe to the active network list
Fetcher_networks.nets.push(fringe);
