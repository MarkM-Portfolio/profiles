/* *************************************************************** */
/*                                                                 */
/* HCL Confidential                                                */
/*                                                                 */
/* OCO Source Materials                                            */
/*                                                                 */
/* Copyright HCL Technologies Limited 2017, 2020                   */
/*                                                                 */
/* The source code for this program is not published or otherwise  */
/* divested of its trade secrets, irrespective of what has been    */
/* deposited with the U.S. Copyright Office.                       */
/*                                                                 */
/* *************************************************************** */

dojo.provide("lconn.profiles.integrations.store.AppRegStore");

dojo.require("lconn.core.config.services");
dojo.require("lconn.core.url");

dojo.declare(
  "lconn.profiles.integrations.store.AppRegStore",
  null,
  // A class to fetch extension definitions via AppRegsitry RESTful API.
  // Locale will be passed as URL parameter such that localized translation string will be returned by AppRegistry.
  {    
    // @type boolean - True if Sametime chat is disabled in AppRegistry
    isSametimeChatDisabled: undefined,
    
    // @type Object - JSON data object to hold AppRegistry extension definitions
    extensions: undefined,
    
    // @type dojo/Deferred - resolve when AppRegistry API call is returned successfully, reject if call is not made or call failed.
    loadingDefer: undefined,
    
    constructor: function() {
      this.loadingDefer = new dojo.Deferred();
    },
          
    // To fetch and parse extension definition from AppRegistry.
    // This assumes only 1 service of the same kind can be activated at any time.
    // For example, if more than 1 chat service definition is returned,
    // only the last chat definition in the extension definition list will be used.
    //
    // @param callback {Function} - Callback function, to be executed when the API call returned, no matter success or failed.
    //     If API call failed, an error object will be passed as parameter.
    loadData: function(callback) {
      if (!dojo.exists("profilesData.config")) {
    	  this.extensions = this.extensions || {}; // ensure the object exists as listeners and/or callback may depend on it
    	  this.loadingDefer.reject();
        callback();
        return;
      }
      
      this.extensions = {}; // reset data object before loading AppReg data 
      

        var query = {};
        if (dojo.locale) {
          query['locale'] = dojo.locale;
        }
        var sQuery = dojo.objectToQuery(query);
        if (sQuery.length > 0) sQuery = "&" + sQuery;
        var APPREGISTRY_QUERY_SERVICES = "Connections"; // use 'Connections' as the service for appreg v3 extensions
        var APPREGISTRY_QUERY_APPS_URL = "/appregistry/api/v3/services/" + APPREGISTRY_QUERY_SERVICES + "/extensions?limit=199" + sQuery;
  	
        dojo.xhrGet({
          url: APPREGISTRY_QUERY_APPS_URL,
          handleAs: "json",
          load: dojo.hitch(this, function(data) {
            if (!data || !data.items) {
              return;
            }
            this.isSametimeChatDisabled = false; // assume False first; will be set to True if defined in AppReg
            dojo.forEach(data.items, dojo.hitch(this, function(ext) {
              if (ext && ext.payload) {
                if (ext && ext.type) {
                  switch (ext.type) {
                  case "com.ibm.action.delete" :
                    if (ext.path === ".chat" ) {
                      this.isSametimeChatDisabled = true;
                    }
                    break;
                  case "com.hcl.appreg.ext.templatedLink" : // new V3 extension type for MT
                    if (ext.object === "com.hcl.appreg.object.person" &&
                        ['chat', 'phone_call', 'video_call', 'webmeeting'].indexOf(ext.payload.locator) !== -1){
                      // example payload object returned from AppRegistry:
                      // myExampleChatExtensionJson.payload = {
                      //   href: {String} a templated link,
                      //   target: {String} target label for the link,
                      //   text: {String} display text for the link,
                      //   icon: {String} icon in format base64 encoded svg+xml in data URI
                      // }
                      this.extensions[ext.payload.locator] = ext.payload;
                    }
                    break;
                  default:
                    break;
                  }
                }
              }
            }));
            this.loadingDefer.resolve();
            callback();
          }),
          error: dojo.hitch(this, function(err) {
            this.loadingDefer.reject();
            callback(err);
          }),
          sync: false
        });
      
    },
    
    // To wait until AppRegistry API call is returned, then execute function callback.
    // @param callback {Function} - Callback function, to be executed when the API call returned, 
    //        executed with error out if API call is not made or returns error.
    waitForLoaded: function(callback) {
      this.loadingDefer.then(
        function() {
          callback();
        }, 
        function(err) {
          callback(err);
        });
    },
    
    checkLoaded: function() {
      return this.loadingDefer.isFulfilled();
    },
    
    _getExtAttr: function(service, attr) {
      return this.extensions[service] && this.extensions[service][attr] || '';
    },
    
    isChatEnabled: function() {
      return !!this.getChatUrl();
    },
    
    // @return {String} The URL for extension chat service.
    getChatUrl: function() {
      return this._getExtAttr('chat', 'href');
    },
    
    // @return {String} The target label used when opening chat service in new browser window/tab,
    //     such that it will always open the same chat browser window/tab.
    getChatTarget: function() {
      return this._getExtAttr('chat', 'target');
    },
    
    // @return {String} Localized label text to be shown in UI.
    getChatLabel: function() {
      return this._getExtAttr('chat', 'text');
    },
    
    // @return {String} The icon for chat, in format of svg+xml and in form of base64 encoded data URI.
    //    For example: data:image/svg+xml;base64,PHN2ZyB2ZXJzaW9uPSIxLjEiIHhtbG5...
    getChatIcon: function() {
      return this._getExtAttr('chat', 'icon');
    },

    isWebmeetingEnabled: function() {
      return !!this.getWebmeetingUrl();
    },

    getWebmeetingUrl: function() {
      return this._getExtAttr('webmeeting', 'href');
    },

    getWebmeetingTarget: function() {
      return this._getExtAttr('webmeeting', 'target');
    },

    getWebmeetingLabel: function() {
      return this._getExtAttr('webmeeting', 'text');
    },

    getWebmeetingIcon: function() {
      return this._getExtAttr('webmeeting', 'icon');
    },
    
    isVideoCallEnabled: function() {
      return !!this.getVideoCallUrl();
    },
    
    // @return {String} The URL for extension video call service.
    getVideoCallUrl: function() {
      return this._getExtAttr('video_call', 'href');
    },
    
    // @return {String} The target label used when opening video call service in new browser window/tab,
    //     such that it will always open the same chat browser window/tab.
    getVideoCallTarget: function() {
      return this._getExtAttr('video_call', 'target');
    },
    
    // @return {String} Localized label text to be shown in UI.
    getVideoCallLabel: function() {
      return this._getExtAttr('video_call', 'text');
    },
    
    // @return {String} The icon for chat, in format of svg+xml and in form of base64 encoded data URI.
    //    For example: data:image/svg+xml;base64,PHN2ZyB2ZXJzaW9uPSIxLjEiIHhtbG5...
    getVideoCallIcon: function() {
      return this._getExtAttr('video_call', 'icon');
    },
    
    isPhoneCallEnabled: function() {
      return !!this.getPhoneCallUrl();
    },
    
    // @param phoneNumber {String} - The phone number to be appear in the URL
    // @return {String} The URL for extension phone call service.
    getPhoneCallUrl: function(phoneNumber) {
      var href = this._getExtAttr('phone_call', 'href');
      if (phoneNumber) {
        href = this.replaceUrl(href, {phone: phoneNumber});
      }
      return href;
    },
    
    // @return {String} The target label used when opening phone call service in new browser window/tab,
    //     such that it will always open the same chat browser window/tab.
    getPhoneCallTarget: function() {
      return this._getExtAttr('phone_call', 'target');
    },
    
    // @return {String} Localized label text to be shown in UI.
    getPhoneCallLabel: function() {
      return this._getExtAttr('phone_call', 'text');
    },
    
    // @return {String} The icon for chat, in format of svg+xml and in form of base64 encoded data URI.
    //    For example: data:image/svg+xml;base64,PHN2ZyB2ZXJzaW9uPSIxLjEiIHhtbG5...
    getPhoneCallIcon: function() {
      return this._getExtAttr('phone_call', 'icon');
    },
    
    _openWindow: function(url, args, target) {
      // For extension using non-HTTP URI, such as xmpp://${!emails} in Jabber,
      // target label will be meaningless because it should open the native application, not to open a new browser window/tab.
      // When using with window.open(), it needs to set target to '_self' to avoid opening blank window/tab.
      target = target || '_blank';
      var isHttpUri = /^(https?:\/\/|\/)/; // begins with 'http://' or 'https://' or '/' (relative path)
      if (!isHttpUri.test(url)) {
        target = '_self';
      }
      window.open(this.replaceUrl(url, args), target);
    },
    
			
    // To open the extension chat web service in new browser window/tab.
    // A target label will be used if it's defined in extension definition.
    // @param chatUrl - resolved chatUrl
    openChat: function(chatUrl) {
      var target = this.getChatTarget() || '_blank';
      window.open(chatUrl, target);
    },

    openWebmeeting: function(url) {
      if (url) {
        this._openWindow(url, {}, this.getWebmeetingTarget());
      }
    },
    
    // To open the extension video call service in new browser window/tab.
    // A target label will be used if it's defined in extension definition.
    // @param email {String} - email address to be substituted in the URL
    openVideoCall: function(email) {
      if (email && this.getVideoCallUrl()) {
        this._openWindow(this.getVideoCallUrl(), {emails: email}, this.getVideoCallTarget());
      }
    },
    
    /**
     * Substitute placeholders in the provided 'url' template string by values defined in the provided 'map' key-value pair object.
     * Placeholders should be in format of '${key}', in which 'key' should be defined in the 'map' object for value lookup.
     * 
     * Supports 2 forms of placeholders for flexibility:
     * 1). ${key} (without exclamation mark) - values will be substituted with encodeURIComponent() value.
     * 2). ${!key} (with exclamation mark) - values will be substituted with the original value, without encodeURIComponent().
     * 
     * If 'key' is not found in the 'map' object, the placeholder will leave unchanged in the returned 'url' string.
     * 
     * @param {String} url - original url
     * @param {Map<String name, String value>} - key-value pair object
     * @return {String} the processed url. If 'key' not found in the 'map' object, or the 'url'/'map' is empty,
     *   return the original 'url'.
     */
    replaceUrl: function(url, map) {
      if( !url || !map ) {
        return url;
      }
      Object.keys(map).forEach(function(key) {
        if(key) {
          var value = map[key] ? map[key] : '';
          if(url.indexOf('${'+key+'}') !== -1) {
            url = url.replace('${'+key+'}', encodeURIComponent(value));
          }
          if(url.indexOf('${!'+key+'}') !== -1) {
            url = url.replace('${!'+key+'}', value);
          }
        }
      });
      return url;
    }    
  }
);

// A private variable to hold the singleton instance
lconn.profiles.integrations.store.AppRegStore._appRegStore = null;

// A method to create and return the singleton instance
lconn.profiles.integrations.store.AppRegStore.getInstance = function() {
  if (!lconn.profiles.integrations.store.AppRegStore._appRegStore) {
    lconn.profiles.integrations.store.AppRegStore._appRegStore = new lconn.profiles.integrations.store.AppRegStore();
  }
  return lconn.profiles.integrations.store.AppRegStore._appRegStore;
};