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
 * @fileoverview This file contains the javascript code for an AJAX based
 * widget for Lotus Connections Profiles. This widget, Colleague Fetcher, fetches
 * friends from other networks, i.e. Beehive or Facebook.
 * @author Scott Murphree
 */

// debug toggle; enables/disables the logging functions
var debug = true;

// Fake console if this is IE so it doesn't crash, or if debug is disabled
if (!window.console || !debug) {
	window.console = {
		log: function() {},
		error: function() {}
	};
}

/**
 * Creates a DOM element
 * @param name The type of the DOM Element (div, p, etc)
 * @param attrs An object containing the Element attributes as properties
 * @param style An object containing the inline CSS properties
 * @param text Any innerHTML
 */
function elem(name, attrs, style, text) {
    var e = document.createElement(name);
    if (attrs) {
        for (key in attrs) {
            if (key == 'class') {
                e.className = attrs[key];
            } else if (key == 'id') {
                e.id = attrs[key];
            } else {
                e.setAttribute(key, attrs[key]);
            }
        }
    }
    if (style) {
        for (key in style) {
            e.style[key] = style[key];
        }
    }
    if (text) {
        e.appendChild(document.createTextNode(text));
    }
    return e;
}

/**
 * @class Static data useful to all Fetcher widgets
 * Resource strings would be better suited residing in a configuration file somewhere
 */
var fetcherData = {
	beehiveURL: lconn.core.api.getProxifiedURL("http://beehive.cambridge.ibm.com/beehive/"),
	fringeURL: lconn.core.api.getProxifiedURL("http://fringe.tap.ibm.com/"),
	profilesURL: "/profiles/",
	profileLinkHTML: '<a class="fn url" href="javascript:void(0)" onclick="CALLER.personClick(\'UID\')">NAME</a><span class="email" style="display: none;">EMAIL</span>'
};

/**
 * @class A collection of utilities for use by Fetcher
 */
var fetcherUtils = {

	/**
	 * Comparison function for Person
	 *
	 * @param a Person 1
	 * @param b Person 2
	 * @return -1 if a < b, 1 if a > b
	 */
	comparePersons: function (a, b) {
		return a.name>b.name?1:a.name<b.name?-1:0;
	},


  /**
   * Returns an img tag pointing the given file in the standard directory.
   * @param file The filename (including extension)
   * @param id The id of the image
   */
  imgTag: function(file, id) {
      if (id) {
        id = 'id=' + id + '';
      } else {
        id = '';
      }
      return '<img ' + id + ' src="' + fetcherDir + 'img/' + file + '" />';
  },
  
  /**
   * Parses an XML string into a DOM Document object
   * The string must be valid XML
   * @param text The XML string
   */
  parseXML: function (text) {
  	//Internet Explorer
  	try {
  		xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
  		xmlDoc.async="true";
  		xmlDoc.loadXML(text);
  	}
  	catch(error0) {
  		//Firefox, Mozilla, Opera, etc.
  		try {
  			parser=new DOMParser();
  			xmlDoc=parser.parseFromString(text,"text/xml");
  		}
  		catch(error1) {alert(error1.message);}
  	}
  	return xmlDoc;
  },

	/**
	 * Uses dojo.xhrGet to send GET request
	 *
	 * @param url The URL to make the request to
	 * @param params The parameters for the url
	 * @param cont The continuation (callback) function
	 * @param caller The Fetcher object making the request
	 * @param reqHeaders Any headers (content-type, auth, etc) to include in the request
	 */
	getXML: function (url, params, cont, caller, reqHeaders) {
    paramString = '?';
    if (params.charAt) {
      paramString += params;
    } else {
      for (p in params) {
        paramString += p + '=' + params[p] + '&';
      }
    }
		dojo.xhrGet({
			handleAs: 'xml',
			url: url + paramString,
			load: function(data, ioArgs) {

        xml = ioArgs.xhr.responseXML;
        txt = ioArgs.xhr.responseText;
        if (xml.childNodes.length === 0) {
            console.log("parse failure, attempting fix");
            txt = txt.replace(/xmlns:xml=/, "xmlns:ignore=");
            xml = fetcherUtils.parseXML(txt);
            console.log("Parse completed. Nodes: " + xml.childNodes.length);
        }
        //console.log(ioArgs.xhr.responseText);
        cont(caller, xml);
      },
			error: function(data, ioArgs) {
				errorObj = data;
				console.error("Error in GET Request, status " + ioArgs.xhr.statusText + ":\n");
   	    fetcherUtils.printProps(data, 'Error');
				caller.error(data, ioArgs.xhr.status);
			},
			headers: reqHeaders
		});
		reqHeaders = {};
	},

  /**
   * Extracts a parameter from the GET parameters in a URL
   * @param name The paramater name
   * @param url The URL to parse
   */
	extractFromUrl: function (name, url) {
		name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
		regexS = "[\\?&]"+name+"=([^&#]*)";
		regex = new RegExp( regexS );
		results = regex.exec( url );
		if( results === null ) {
			return "";
    } else {
			return results[1];
		}
	},

/**
 * Creates a compatible Request object
 * @return The Request object
 * @type XMLHttpRequest, ActiveXObject
 *
 */
  createXHRequest: function () {
  	req = false;
  	if(window.XMLHttpRequest && !(window.ActiveXObject)) {
    	try {
  			req = new XMLHttpRequest();
  			console.log("using XMLHttpRequest");
      } catch(error0) {
			  req = false;
      }
    // branch for IE/Windows ActiveX version
    } else if(window.ActiveXObject) {
       	try {
        	req = new ActiveXObject("Msxml2.XMLHTTP");
        	console.log("using Msxml2.XMLHTTP");
      	} catch(error1) {
        	try {
          		req = new ActiveXObject("Microsoft.XMLHTTP");
          		console.log("using Microsoft.XMLHTTP");
        	} catch(error2) {
          		req = false;
        	}
		    }
    }
    return req;
  },


  /**
   * Sends a POST message
   * @param args An object with properties describing the message
   *
   */
  xhrPost: function (args) {
    if (args.async !== false && args.async !== true) { args.async = true; }
    if (!args.xhr) {
  		args.xhr = fetcherUtils.createXHRequest();
  		args.xhr.onreadystatechange = function() {
  			fetcherUtils.xhrPost(args);
  		};
  		args.xhr.open('POST', args.url, args.async);
  		args.xhr.setRequestHeader("Content-type", args.contentType ? args.contentType : 'text');
      args.xhr.setRequestHeader("Content-length", args.content.length);
      args.xhr.setRequestHeader("Connection", "close");
  		args.xhr.send(args.content);
  	} else {
  		if (args.xhr.readyState === 4) {
  		  if (args.handleAs == "xml") {
  			   if (args.xhr.getResponseHeader("Content-Type").match("xml")) {
  			     args.load(args.xhr.responseXML, args);
           } else {
              console.log("non-xml response: " + args.xhr.responseText);
             args.load(fetcherUtils.parseXML(args.xhr.responseText), args);
           }
  			} else {
           args.load(request.responseText, args);
        }
  		}
  	}
  },

  /**
   * Sets a cookie containing login information for the given network. Uses
   * secure connections only, data encoded in base64.
   * @param networkName The name of the network
   * @param info An object containing information to save
   */
  setUserInfo: function (networkName, info) {
    try {
      dojo.cookie("fetcher." + networkName, btoa(dojo.toJson(info)), {expires: 5, path: '/'});
    } catch (e) {
      console.error("Unable to set cookie: " + e);
    }
  },

  /**
   * Retrieves login information from a cookie based on the network name.
   * @param networkName The name of the network.
   */
  getUserInfo: function (networkName) {
    try {
      cookie = dojo.cookie("fetcher." + networkName);
    } catch (e) {
      console.log("Unable to retrieve cookie: " + e);
      userinfo = {};
    }
    if (cookie) {
      userinfo = dojo.fromJson(atob(cookie));
    } else {
      userinfo = null;
    }
    return userinfo;
  },

  /**
   * Prints all the properties of an object for debugging
   * @param ob The object
   * @param name The name of the object
   */
	printProps: function (ob, name) {
		for (p in ob) {
			console.log(name + '.' + p + " = " + ob[p]);
		}
	}
};

/**
 * @class Represents a Person, contains useful information.
 */
function Person (name, email) {

	/**
	 * This Person's name
	 * @type string
	 */
	this.name = name;

	/**
	 * The primary email address of this Person
	 * @type string
	 */
	this.email = email;

	/**
	 * The secondary email address of this Person
	 * @type string
	 */
	this.altEmail = null;

	/**
	 * The Connections UID of this Person
	 * @type string
	 */
	this.uid = null;

	/**
	 * Whether this Person is already a Colleague
	 * @type boolean
	 */
	this.colleague = false;

	/**
	 * Overrides the default invitation message if set.
	 * @type string
	 */
	this.message = null;

	/**
	 * Whether this person should be invited
	 * @type boolean
	 */
	this.invite = true;

}

/**
 * @class The message dialog used for setting the default invitation message as well
 * as individual messages.
 *
 * @param parent The Fetcher that this MessageDialog is serving
 */
function MessageDialog (parent) {

	this.parent = parent;

	this.mainDiv = elem("div",
	 { id: parent.getId("messageDialog"),
	   'class': "messageDialog" });

	form = elem("form",
	 { id: parent.getId("invitation"),
	   'class': "lotusForm" },
   { 'margin': '10px' } );

  // Header
  this.header = elem('div', {}, {'backgroundColor': '#F1F5F9'});
  this.header.innerHTML = fetcherUtils.imgTag('mail.png');
	this.invitationLabel = elem('span',
    { 'title': 'Invitation message field' },
    { 'fontWeight': 'bold' });
	this.header.appendChild(this.invitationLabel);
  
  // Invitation message text area
	this.invitationText = elem('textarea',
    { id: parent.getId("invitation_text"),
      name: parent.getId("invitation_text"),
	    rows: 5 },
    { width: "99%",
      fontFamily: "arial" });

  // Help section
  this.defaultHelp = 'If you want to edit the invitation for individual colleagues, click on their name';
  this.indivHelp = 'If you want to edit the invitation for All Colleagues who don\'t have an individual message, click on ' + parent.defaultMessager.innerHTML;
  this.helpSection = elem('table', {}, {'marginBottom': '1em'});
  row = this.helpSection.insertRow(0);
  row.insertCell(0).innerHTML = fetcherUtils.imgTag('info.png');
  this.help = row.insertCell(1);
  this.help.innerHTML = this.indivHelp;
  
  // Buttons
	this.buttons = elem("div",
	 {'class': 'lotusBtnContainer'},
	 {textAlign: 'left'});

	this.mainDiv.appendChild(this.header);
	this.mainDiv.appendChild(form);
	
	form.appendChild(this.invitationText);
	form.appendChild(this.helpSection);
	form.appendChild(this.buttons);

	saveBtnHtml = '<input type=button class="lotusBtn lotusBtnSpecial" value="Save Message" onclick=' + parent.getId("messageDialog.save()") + ' />';
	cancelHtml = '<a href="javascript:void(0);" onclick=' + parent.getId("messageDialog.close()") + ' >cancel</a>';
	
	this.buttons.innerHTML = saveBtnHtml + '&nbsp;&nbsp;' + cancelHtml;

	/**
	 * The default message that is used if there is no individual message for a
	 * an invitee
	 */
	this.defaultMessage = "I\'d like to add you to my Connections colleagues list.";

	/**
	 * Displays the message dialog
	 *
	 * @param person The invitee to set the message for. If null, then the default message is set
	 */
	this.show = function( person ) {

		this.person = person;
		msg = this.defaultMessage;
		if ( this.person === null ) {
			this.invitationLabel.innerHTML = 'Set the invitation for <span style="color: #1BA1FA;">All My Colleagues</span>';
			this.help.innerHTML = this.defaultHelp;
		} else {
			this.invitationLabel.innerHTML = 'Set the invitation for <span style="color: #1BA1FA;">' + this.person.name + '</span>';
			this.help.innerHTML = this.indivHelp;
			if (this.person.message) {
				this.invitationLabel.innerHTML += '<span class="exists-notify"> (has message)</span>';
				msg = this.person.message;
			}
		}

		this.invitationText.value = msg;

		document.body.appendChild(this.mainDiv);

		// Set the position, centered
		this.mainDiv.style.left = (document.documentElement.clientWidth / 2 - this.mainDiv.offsetWidth / 2) + "px";
		//void (
    this.mainDiv.style.top = (document.documentElement.clientHeight / 2 - this.mainDiv.offsetHeight / 2) + "px"; //);
	};

	/**
	 * Saves the message for the selected invitee
	 */
	this.save = function() {
		if (this.person === null) {
			this.defaultMessage = this.invitationText.value;
			this.invitationLabel.innerHTML = 'Default message saved!';
		} else {
			this.person.message = this.invitationText.value;
			this.invitationLabel.innerHTML = 'Message saved for <span class="name">' + this.person.name + '</span>!';
		}
	};

	/**
	 * Closes the dialog
	 */
	this.close = function() {
		document.body.removeChild(this.mainDiv);

	};
}

/**
 * @class Represents a network from which you can fetch friends.
 *
 * @param name The name of the network
 */
function Network(name) {

	/**
	 * The name of the network
	 * @type string
	 */
	this.name = name;
  
  // Interface functions, implemented by individual modules
  this.showLogin = null;
  this.getFriends = function(caller) {};
  
	/**
	 * Yields an Option element for this network.
	 * @type DOM_Option
	 */
	this.toOption = function () {
		option = document.createElement("option");
		option.network = this;
		option.innerHTML = this.name;
		return option;
	};

}

/**
 * @class A list of networks that is convertable to a DOM Select element.
 */
function NetworkList() {

	/**
	 * The list of networks
	 * @type Array
	 */
	this.nets = [];

	/**
	 * Yields a Select element for the networks
	 *
	 * @param id The id for the select element
	 * @type DOM_Select
	 * @return The
	 */
	this.toSelect = function(id) {
		select = document.createElement("select");
		select.id = id;
		select.style.width = "100%";
		for (net = 0; net < this.nets.length; ++net) {
			select.appendChild(this.nets[net].toOption());
		}
		return select;
	};

}

var Fetcher_networks = new NetworkList();

/**
 * @class The encapsulation of the Fetcher widget
 * @param title The title of the widget
 * @param id_prefix The prefix for html component ids
 */
function Fetcher (id_prefix) {

	/**
	 * The identity prefix for html components
	 * @type string
	 */
	this.idp = id_prefix;

  /**
   * Only true when a colleague list has been retrieved
   * and is currently displayed, or is being retrieved.
   */
  this.loggedIn = false;
  
	/**
	 * Returns the name/prefix for the Fetcher object
	 * @param rVal The r-value, if any
	 */
	this.getId = function (rVal) {
		if (rVal) {
			return this.idp + "." + rVal;
		} else {
			return this.idp;
		}
	};

	/**
	 * The authenticated user
	 * @type Person
	 */
	this.user = new Person();

  // Get the authenticated user ID
	this.user.uid = lconn.profiles.api.getLoggedInUserUid();

	/**
	 * The list of friends from another network
	 * @type string
	 */
	this.friends = [];

	/**
	 * Toggle variable for list checkboxes
	 */
	this.allCheck = true;

	/**
	 * The iterator for the friend list
	 * @type int
	 */
	this.friendIter = 0;

	/**
	 * Searches the friends array for a Person with the given id
	 * @param id The UID of the person to search for
	 * @return the Person with the id if it exists, otherwise null
	 * @type Person
	 */
	this.friendByID = function (id) {
		for (fiter in this.friends) {
			if (this.friends[fiter].uid == id) {
				return this.friends[fiter];
			}
		}
		return null;
	};


  /**
   * Displays an error message in the UI
   * @param data The message
   *
   */
	this.error = function (error, status) {
		this.toggleLoading(false);
		if (status === 401 || status === 403 || status === 501) {
      error = "Login failed. Try again.";
    }
		this.loadError.innerHTML = error;
		this.loadError.style.display = "";
    // chances are the credentials are wrong
    if (this.curNetwork().showLogin) {
      this.curNetwork().showLogin(this);
    }
	};

	/**
	 * Searches Profiles for the friends with the given email.
	 *
	 * @param field The field to search on
	 * @param person The person to search for
	 * @param index The index of the person to search for
	 */
	this.searchProfiles = function (field, index) {
    if (field) { this.searchBy = field; }
    // If index is zero then start at 0.
    if (index === 0) { this.friendIter = 0; }
    params = new String(this.searchBy + '=' + this.friends[this.friendIter][this.searchBy]);
    params.isString = true;
    console.log("Searching Profiles: " + params);
		fetcherUtils.getXML(
			fetcherData.profilesURL + 'atom/search.do',
			params,
			this.profileMatch,
			this);
	};

	/**
	 * Parses profile data from a profile search result list
	 * @param data The XML data containing the search results
	 * @param person If there is only one expected result, the data is recorded
	 *     in person.
	 */
	this.parseProfiles = function(data, person) {
    //console.log("parseProfiles nodes: " + data.childNodes.length);
    results = [];
    entries = data.getElementsByTagName("span");
    var classAtt;
    var txt;
    
		for (p=0; p<entries.length; ++p) {
      
      classAtt = entries[p].getAttribute("class");
			if (classAtt == "vcard") {
        
        if (!person) { person = new Person(); }

  			el = entries[p].firstChild;
  			while (el !== null) {
          classAtt = null;
          txt = null;

          if ((el.childNodes.length === 1) && (el.firstChild.attributes)) {
            classAtt = el.firstChild.getAttribute("class");
            txt = el.firstChild.text ? el.firstChild.text : el.firstChild.textContent;
            //console.log("class: " + classAtt + ", " + txt);
            if (classAtt === "email") {
              person.email = txt;
            } else
            if (classAtt === "fn url") {
              person.name = txt;
            } else
            if (classAtt === "photo") {
              person.photo = el.firstChild.src;
            }
            if (classAtt === "organization-unit") {
              person.dept = txt;
            }
          } else {
            classAtt = el.getAttribute("class");
            txt = el.text ? el.text : el.textContent;
            //console.log("f " + classAtt + ", " + txt);
            if (classAtt === "x-profile-key") {
              person.key = txt;
            } else
            if (classAtt === "x-profile-uid") {
              person.uid = txt;
            } else
            if (classAtt === "x-manager-uid") {
              person.managerUid = txt;
            } else
            if (classAtt === "x-is-manager") {
              person.isManager = txt;
            } else
            if (classAtt === "x-groupwareMail") {
              person.altEmail = txt;
            } else
            if (classAtt === "title") {
              person.title = txt;
            } else
            if (classAtt.match("postal")) {

              child = el.firstChild;
              while (child !== null) {
                if (child.getAttribute("class") === "locale") {
                  person.location = child.text ? child.text : child.textContent;
                }
                child = child.nextSibling;
              }
            }
          }

          el = el.nextSibling;
        }
        results.push(person);
        person = null;
		  }
		}
		return results;
  };

	/**
	 * Processes Profiles search results for friend matches
	 * @param caller The Fetcher widget in control
	 * @param response The result data from the search (xml)
	 *
	 */
	this.profileMatch = function (caller, response) {

		caller.loading(caller.friendIter/caller.friends.length);

    found = caller.parseProfiles(response, caller.friends[caller.friendIter]);
    
    if (found.length === 0) {
      caller.friends[caller.friendIter].invite = false;
			caller.friends[caller.friendIter].colleague = false;
			console.log("Fetcher.profileMatch: Couldn't find on Profiles: " + caller.friends[caller.friendIter].name);
    } else {
      caller.friends[caller.friendIter] = found[0];
      caller.friends[caller.friendIter].invite = true;
			caller.friends[caller.friendIter].colleague = false;
    }

		++caller.friendIter;

		// If we've gone through all the fetched friends,
		// determine which ones are already colleagues
		if (caller.friendIter == caller.friends.length) {
			caller.getColleagues(caller.user.uid);
		} else {
			// Continue search
			if (caller.friendIter < caller.friends.length) {

				caller.searchProfiles();

      }
		}
	};

	/**
	 * Called when a Profile link is clicked
	 */
	this.personClick = function (uid) {
		p = this.friendByID(uid);
		if (!p.colleague) {
			this.messageDialog.show(p);
		}

	};

	/**
	 * Displays the profile matches
	 */
	this.displayProfileMatches = function () {

		console.log("Fetcher.displayProfileMatches: got " + this.friends.length + " friends.");

    this.networkTable.style.borderBottom = "";
    
		this.toggleLoading(false);
    this.resultContainer.style.display = "";
    
		this.friends.sort(fetcherUtils.comparePersons);
		//list = document.getElementById('fetcher.list');
		for (i = 0; i < this.friends.length; ++i) {

			if (this.friends[i].key) {
				// Display info

				checkchange = this.getId('friends[' + i + ']') + '.invite=this.checked ';

				if (this.friends[i].colleague) {
					check = fetcherUtils.imgTag('person.png') + ' ';
				} else {
  				check =
  					'<input type="checkbox" checked ' +
  						'onchange=' + checkchange +
  						'name="invite-checkbox" ' +
  						'title="Un-check if you don\'t want to invite this person to be your Colleague"> ';
       }

				personSpan = document.createElement("div");
				personSpan.className = "fetcherPerson";
				vcard = document.createElement("span");
				vcard.className = "vcard";
				if (!this.friends[i].colleague) {
					vcard.title = "Click to change the invitation message for " + this.friends[i].name;
				} else {
					vcard.title = this.friends[i].name + " is already your colleague.";
				}
				profileLink = fetcherData.profileLinkHTML
          .replace(/CALLER/, this.getId())
          .replace(/UID/, this.friends[i].uid)
          .replace(/NAME/, this.friends[i].name)
          .replace(/EMAIL/, this.friends[i].email);

				vcard.innerHTML = profileLink;
				personSpan.id = this.getId("vcard_" + this.friends[i].uid);
				personSpan.innerHTML = check;
				personSpan.appendChild(vcard);

				personSpan.style.marginBottom = "3px";

				if (this.friends[i].colleague) {
					this.resultContainer.colleagues.appendChild(personSpan);
				} else {
					this.resultContainer.nonColleagues.appendChild(personSpan);
				}

				// Enable the business card popup
				setTimeout("SemTagSvc.parseDom(null, '" + personSpan.id + "')", 500 );
			}
		}

		document.getElementById(this.getId("numColleagues")).innerHTML = " (" + this.friends.numColleagues + ")";
	};

	/**
	 * Alternates between checking all valid colleagues and un-checking them
	 */
	this.toggleCheckAllProfiles = function() {

		toggle = document.getElementById(this.getId("toggleCheck"));
		toggleIcon = document.getElementById(this.getId("toggleCheckIcon"));
		if (this.allCheck) {
      toggleIcon.src = fetcherDir + 'img/check.png';
			toggle.innerHTML = "Check All";
		} else {
      toggleIcon.src = fetcherDir + 'img/check2.png';
			toggle.innerHTML = "Un-Check All";
		}

		checks = document.getElementsByName("invite-checkbox");
		this.allCheck = !this.allCheck;
		for (ci = 0; ci < checks.length; ++ci) {
			if (!checks[ci].disabled && checks[ci].checked != this.allCheck) {
				checks[ci].click();
			}
		}
	};

	/**
	 * Shows/hides the accepted colleagues in the list
	 */
	this.toggleShowColleagues = function() {

		toggle = document.getElementById(this.getId("toggleColleagues"));
		if (this.resultContainer.showColleagues) {
			this.resultContainer.colleagues.style.display = "none";
			toggle.title = "Show Accepted Colleagues";
			toggle.innerHTML = "Show Colleagues";
		} else {
			this.resultContainer.colleagues.style.display = "";
			toggle.title = "Hide Accepted Colleagues";
			toggle.innerHTML = "Hide Colleagues";
		}
		this.resultContainer.showColleagues = !this.resultContainer.showColleagues;
	};


	/**
	 * Fetches the user's list of colleagues to compare them against fetched friends
	 * @param uid The user's UID
	 */
	this.getColleagues = function (uid) {
		fetcherUtils.getXML(
			fetcherData.profilesURL + "atom/colleagues.do",
			{'uid': uid, 'status': '', 'output': 'hcard'},
			this.processColleagues,
			this
		);
	};

	/**
	 * Processes results from getColleagues
	 * @param request The XMLHttpRequest
	 * @param caller The Fetcher widget in control
	 */
	this.processColleagues = function (caller, response) {
    caller.friends.numColleagues = 0;
		try {
       ids = [];
       numIds = 0;
	     ids = response.getElementsByTagName("div");
			 for (i=0;i<ids.length;++i) {
				if (ids[i].getAttribute("class") === "x-profile-uid") {

          ++numIds;
  				// If any of the existing colleagues are also in the
  				// fetched friends list, mark them as such
  				existingUID = ids[i].text ? ids[i].text : ids[i].textContent;

  				colleague = caller.friendByID(existingUID);
  				if (colleague !== null) {
            ++caller.friends.numColleagues;
  					console.log("Fetcher.processColleagues: found " + existingUID);
  					colleague.colleague = true;
  					colleague = null;
  				}

        }
			}
			console.log("Fetcher.processColleagues: got " + numIds + " colleagues");
	    	// Display results
			caller.displayProfileMatches();

		} catch (e) {console.error('Fetcher.processColleagues: ' + e); }

	};

	/**
	 * Send all invitations
	 */
	this.sendInvitations = function () {
		inviteeList = [];
		for (i = 0; i < this.friends.length; ++i) {
			f = this.friends[i];
			if (f.invite && !f.colleague) {

				if (f.message === null) {
					f.message = this.messageDialog.defaultMessage;
				}
				inviteeList.push(f);
			}
		}

		// Start the request callback tree with iviteeList[0]
		// Include a reference to the Fetcher object
		this.toggleLoading(true);
		this.sendFriendRequest(inviteeList, 0, this);
	};

	/**
	 * Sends the friend request to invitee
	 * @param invitees The array of invitees
	 * @param index The index of the current invitee
	 * @param caller The Fetcher object making the requests
	 */
	this.sendFriendRequest = function (invitees, index, caller)
	{
		if (index < invitees.length) {
		  dataUrl = applicationContext + "/xml/friendrequest?targetKey=" + invitees[index].key;
			dataUrl += "&lastMod=" + profilesData.config.profileLastMod;
			dataUrl += "&msg=" + invitees[index].message;

			console.log("Fetcher.sendFriendRequest: sending invite for " + invitees[index].name);
    	dojo.xhrPost({
    	  url: dataUrl,
    		handleAs: "xml",
    		load: function(response, ioArgs){
    			if(response.documentElement.nodeName == "error" && response.documentElement.getAttribute("code") == "connection-exist") {
    				console.log("Fetcher.sendFriendRequest: already invited " + invitees[index].name);
    			}

    			profiles_setProfilesLastMod(response);
    			caller.loading((index+1)/invitees.length, "Sending...");
    			caller.sendFriendRequest(invitees, index + 1, caller);
    	  }
    	});

		} else {
		  caller.loading(1, "Done!");
			//alert("Finished sending invitations!");
			//caller.toggleLoading(false);
			caller.resetUI();
      setTimeout(function() {caller.toggleLoading(false);}, 2000);
		}
	};

  /**
   * Fetches the friend list from the currently selected network.
   */
  this.getFriends = function () {
    this.resetUI();
    this.loggedIn = true;
    this.updateLoginPanel();
    this.curNetwork().getFriends(this);
  };
  
  /**
   * Returns the currently selected network.
   */
  this.curNetwork = function() {
    return this.networkSelector.options[this.networkSelector.selectedIndex].network;
  };
  
  /**
   * Displays a link allowing the user to change their login information
   * for the currently selected network module. If the module does not
   * implement a showLogin function, no link is displayed.
   *
   * @param hide If true, hides the login panel
   */
  this.updateLoginPanel = function(hide) {
    
    if (this.curNetwork().showLogin && !hide) {
      // Update login status
      if (this.loggedIn) {
        this.loginPanel.status.innerHTML = 'Logged in to ' + this.curNetwork().name;
        this.loginPanel.link.innerHTML = 'Logout';
      } else {
        this.loginPanel.status.innerHTML = 'Login to ' + this.curNetwork().name;
        this.loginPanel.link.innerHTML = 'Login';
      }
      this.loginPanel.style.display = "";
    } else {
      this.loginPanel.style.display = "none";
    }
  };
  
	/**
	 * Show the UI
	 * Should be converted to straight html and css, then DOM extraction
	 * @param containerId The container to attach to
	 */
	this.loadUI = function (container) {

    
    caller = this;
    
    // Get info about the user
    fetcherUtils.getXML(
      "/profiles/atom/profile.do",
      {'uid': caller.user.uid},
      function(caller, data) {
        caller.parseProfiles(data, caller.user);
      },
      caller );

		try {

			this.container = container;
			this.container.className = "fetcher";
			this.container.innerHTML =
				'<form id=' + this.getId("inviteForm") + ' class="shell" >' +
					'<div id=' + this.getId("networkSelector") + ' class="netSelect" >' +
						'<table class="networkTable" >' +
							'<tr>' +
								'<td style="width: 80%" id=' + this.getId("netSelectArea") + '>' +
                  '<select style="width: 100%" ' +
                      'id="fetcher_network" ' +
                      'onchange=' + this.getId("killForm()") + ' >' +
                  '</select>' +
                '</td>' +
								'<td style="width: 20%">' +
									'<input style="width: 100%" ' +
                      'type="button" ' +
                      'class="lotusBtn lotusBtnSpecial" ' +
                      'value="Go" ' +
                      'onclick=' + this.getId("getFriends();") + ' >' +
								'</td>' +
							'</tr>' +
							'<tr id=' + this.getId('loginPanel') + ' style="width:100%">' +
                '<td id=' + this.getId('loginStatus') + '></td>' +
                '<td id=' + this.getId('loginLink') + '></td>' +
              '</tr>' +
						'</table>' +
					'</div>' +
					'<div id=' + this.getId("loadArea") + ' class="loadArea" />' +
				'</form>';

			this.networkSelector = document.getElementById("fetcher_network");
			for (n=0; n < Fetcher_networks.nets.length; ++n) {
				this.op = Fetcher_networks.nets[n].toOption();
				this.networkSelector.appendChild(this.op);
			}
			this.networkSelector.onchange = function() {
        caller.resetUI();
      };
      
      this.networkTable = document.getElementById(this.getId("networkSelector")).firstChild;
      this.networkTable.style.borderBottom = "0px";

      this.loginPanel = document.getElementById(this.getId('loginPanel'));
      this.loginPanel.status = document.getElementById(this.getId('loginStatus'));
      this.loginPanel.link = document.getElementById(this.getId('loginLink'));
      
      this.loginPanel.link.style.textAlign = 'right';
      this.loginPanel.style.fontSize = '80%';
      this.loginlink = elem('a',
        { id: this.getId("changeLogin"), href: "javascript:void(0);" });
      this.loginlink.caller = this;
      this.loginlink.onclick = function() {
        this.caller.resetUI();
        this.caller.curNetwork().showLogin(caller);
      };
      this.loginPanel.link.appendChild(this.loginlink);
      this.loginPanel.link = this.loginlink;
      
      this.loadProgress = elem('div',
        { id: this.getId('loadProgress'), 'class': 'progressOuter' });
      this.loadProgress.bar = elem('div', { 'class': "progressBar" });
      this.loadProgress.appendChild(this.loadProgress.bar);
      this.loadProgress.loadMessage = elem('div',
        { 'class': 'loadMessage' }, {}, "Loading...");
			this.loadProgress.appendChild(this.loadProgress.loadMessage);

			this.loadError = document.createElement("div");
      this.loadError.className = "error";
      			
			this.loadArea = document.getElementById(this.getId('loadArea'));
			this.loadArea.className = "loadArea";
			this.loadProgress.style.display = "none";
			this.loadArea.appendChild(this.loadProgress);

			this.loadArea.appendChild(this.loadError);
			this.loadError.style.display = "none";

      this.formArea = elem('div', {'class': 'form' });
			this.loadArea.appendChild(this.formArea);

			this.inviteForm = document.getElementById(this.getId("inviteForm"));

      this.resultContainer = elem('div',
        { id: this.getId("resultContainer"), 'class': 'resultContainer' });
      
      // checkmarks toggle
			this.toggleCheckAll = elem('div');
      this.toggleCheckAll.innerHTML =
        '<a href="javascript:void(0)" style="font-size: 80%" ' +
          'title="Check/uncheck all invitees" ' +
          'onclick=' + this.getId("toggleCheckAllProfiles()") + '>' +
          fetcherUtils.imgTag("check2.png", this.getId("toggleCheckIcon")) +
          '<span id=' + this.getId("toggleCheck") + '>Un-Check All</span></a>';
      this.toggleCheckAll.style.backgroundColor = '#E5EDF8';
      this.toggleCheckAll.style.paddingLeft = '4px';
      
      // Colleagues toggle
			this.toggleColleagues = elem('div');
      this.toggleColleagues.innerHTML =
        '<a href="javascript:void(0)" onclick=' + this.getId("toggleShowColleagues()") +
        ' title="Show accepted colleagues">' + fetcherUtils.imgTag("person.png") +
        '<span id=' + this.getId("toggleColleagues") + '>Show Colleagues</span></a>'+
        '<span id=' + this.getId("numColleagues") + ' ></span>';
        
      // Message Editor
			this.defaultMessager = elem('div');
      this.defaultMessager.innerHTML =
        '<a href="javascript:void(0)" onclick=' + this.getId("messageDialog.show(null)") +
        ' title="Change the invitation messages">' + fetcherUtils.imgTag("message.png") + 'Edit Message</a>';

      // Toolbox
      this.toolBox = elem('div',
        { id: this.getId("toolBox"), 'class': 'toolBox box' });
			this.toolBox.appendChild(this.defaultMessager);
			this.toolBox.appendChild(this.toggleColleagues);

			// Insert toggle div
			this.resultContainer.appendChild(this.toolBox);

      // The list contains the results from the intersection of
      // the retrieved friend list and user on Profiles
      this.list = elem('div', { id: this.getId('list'), 'class': 'list' });

      // listContainer contains the the check toggle, the list,
      // and the invite button
      this.listContainer = elem('div', {'class': 'box'});
      
      // Put the toggle checkmarks button in the list itself
      this.listContainer.appendChild(this.toggleCheckAll);
      
			// Insert list div
			this.listContainer.appendChild(this.list);

      // buttonDiv contains the Invite Selected button
			this.buttondiv = elem('div');
      this.buttondiv.innerHTML =
			   '<input type="button" style="width: 100%" ' +
            'title="Invite all checked colleagues" ' +
            'class="lotusBtn lotusBtnSpecial buttonDiv" ' +
            'value="Invite Selected" ' +
            'onclick=' + this.getId("sendInvitations()") + '>';

			// Insert button div
			this.listContainer.appendChild(this.buttondiv);

      this.resultContainer.appendChild(this.listContainer);
      
			this.resultContainer.showColleagues = false;

      // Initially hide all the result-related stuff
      this.resultContainer.style.display = "none";
      this.inviteForm.appendChild(this.resultContainer);
      
      // Create elements for the message dialog
			this.messageDialog = new MessageDialog(this);
			
			// Update the login panel
			this.updateLoginPanel();
			
		} catch (e) {
			//This pretty much only ever happens in IE
			fetcherUtils.printProps(e, "error");
		}
	};

	/**
	 * Hides the the list and resets its content
	 */
	this.resetUI = function () {

		this.resultContainer.style.display = "none";

		if (this.resultContainer.colleagues && this.resultContainer.showColleagues) {
			this.list.removeChild(this.resultContainer.colleagues);
    }
		if (this.resultContainer.nonColleagues) {
			this.list.removeChild(this.resultContainer.nonColleagues);
    }
    
		this.resultContainer.nonColleagues = elem('div', {id: this.getId("nonColleagues")});
		this.resultContainer.colleagues = elem('div', {id: this.getId("colleagues")});

		if (!this.resultContainer.showColleagues) {
			this.resultContainer.colleagues.style.display = "none";
    }	else {
			this.resultContainer.colleagues.style.display = "";
		}

		this.list.appendChild(this.resultContainer.nonColleagues);
		this.list.appendChild(this.resultContainer.colleagues);

		this.loadError.innerHTML = "";
		this.loadError.style.display = "none";

		this.networkTable.style.borderBottom = "0px";
		
		this.killForm();
		this.loggedIn = false;
		this.updateLoginPanel();
	};


  
	/**
	 * Updates the progress bar
	 * @param fraction The progress as a fraction
	 * @param text Any text to display over the bar (default "Loading...")
	 */
	this.loading = function (fraction, text) {
    this.loadProgress.bar.style.width = Math.round(fraction*100) + "%";
    if (!text) { text = "Loading..."; }
    this.loadProgress.loadMessage.innerHTML = text;
  };

  /**
   * Toggles the progress area visibility
   * @param on Overrides auto-toggling
   */
	this.toggleLoading = function (on) {
		if (this.loadProgress.style.display == "none" || on === true) {
      this.loading(0);
			this.loadProgress.style.display = "";
		} else {
			this.loadProgress.style.display = "none";
		}
	};

	/**
	 * Shows a login form
	 * @param fields The fields for the login form. An object containing
	 *   an object for each field; Each field object must have a title and a type.
	 * @param title The title of the form
	 *
	 */
	this.showForm = function (fields, title) {
	
    // Clear any existing forms
    this.killForm();
    // Hide the login panel
    this.updateLoginPanel(true);
    
    // Create the form container
    form = elem('form');
    form.style.textAlign = 'center';
    
    // Set the form title
    title = elem('label', {},
      { 'fontWeight': 'bold',
        'display': 'block',
        'margin': '1em' },
      title);
    form.appendChild(title);
    
    // Construct fields
    for (f in fields) {
      div = elem('div', {'class': f}, {'marginBottom': '1em'});
      title = elem('label', {},
        { 'display': 'block',
          'color': '#1AA2F8' },
        fields[f].title );
      input = elem('input',
        { 'type': fields[f].type },
        { 'width': '100%',
          'marginTop': '0.2em' });
      fields[f].field = input;
      div.appendChild(title);
      div.appendChild(input);
      form.appendChild(div);
    }

    // Add a submit button
    submit = elem('input',
      { 'type': 'button',
        'class': 'lotusBtn lotusBtnSpecial',
        'value': 'Login',
        'onclick': this.getId('getFriends()') },
      { 'width': '100%' });
    form.appendChild(submit);
    
    // Display the form
    this.formArea.appendChild(form);
		this.formArea.style.display = "";
	};

	/**
	 * Removes the form
	 */
	this.killForm = function () {
    this.formArea.style.display = "none";
		this.formArea.innerHTML = "";
	};
}



function pp(ob) { fetcherUtils.printProps(ob, ob.name); }

