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
 * @fileoverview A Profiles Colleague Fetcher network module for Beehive
 *
 * @author: Scott Murphree
 */
 
beehive = new Network("Beehive");

/**
 * Prompts the user for a username and password to use for Beehive.
 * @param caller The Fetcher object in control.
 */
beehive.showLogin = function (caller) {

  // Show the form
  this.form = {
    username: {type: 'text', title: 'IBM Email Address'},
    password: {type: 'password', title: 'IBM Intranet Password'}
  };
  caller.showForm(this.form, 'Beehive Login');

  // Fill the form with any info that already exists
  this.userinfo = fetcherUtils.getUserInfo(this.name);
	if (this.userinfo) {
		this.form.username.field.value = this.userinfo.username;
		this.form.password.field.value = window.atob(this.userinfo.pass);
		this.form.password.field.focus();
	} else {
		this.form.username.field.focus();
	}
};

/**
 * Sends a request to Beehive
 * @param caller The Fetcher object in control
 */
beehive.getFriends = function (caller) {
	if (!this.userinfo) {
		this.userinfo = fetcherUtils.getUserInfo(this.name);
		if (this.form) {
      this.userinfo = {
        username: this.form.username.field.value,
		    pass: btoa(this.form.password.field.value)
      };
		  this.form = null;
		  caller.killForm();
    }
		if (this.userinfo) {
		  beehive.getFriends(caller);
		} else {
      beehive.showLogin(caller);
		}
	} else {
		fetcherUtils.setUserInfo(this.name, this.userinfo);
		caller.toggleLoading(false);
		try {
			loginString = btoa(this.userinfo.username + ':' + atob(this.userinfo.pass));
			console.log("login string: " + loginString);
			reqHeaders = {"Authorization": "Basic " + loginString};
			fetcherUtils.getXML(fetcherData.beehiveURL + "feed/atom/people/network/", {'email': this.userinfo.username}, beehive.process, caller, reqHeaders);
		} catch (e) { console.error(e); }
	}
};

/**
 * A continuation function for Fetcher.getBeehiveFriends that extracts
 * the relevant information
 * @param caller The Fetcher widget in control
 * @param response The Friends xml retrieved from Beehive
 */
beehive.process = function (caller, response) {
	entries = response.getElementsByTagName('entry');
  caller.friends = [];
  
	if (entries.length === 0) {
    //no friends
		caller.toggleLoading(false);
	}
	else {
    for(i=0; i<entries.length; ++i) {
			for(j=entries[i].firstChild; j!==null; j=j.nextSibling) {

				// Get the names and emails of all the friends
				if(j.nodeName=="author") {
					for (a = j.firstChild; a !== null; a = a.nextSibling) {
						if (a.nodeName == "name") {
							  name = a.text ? a.text : a.textContent;
						}
						if (a.nodeName == "email") {
							email = a.text ? a.text : a.textContent;
						}
					}
					p = new Person(name, email);
					caller.friends.push(p);
				}
			}
		}
		caller.searchProfiles('email', 0);
	}
};


// Add Beehive to the active network list
Fetcher_networks.nets.push(beehive);
