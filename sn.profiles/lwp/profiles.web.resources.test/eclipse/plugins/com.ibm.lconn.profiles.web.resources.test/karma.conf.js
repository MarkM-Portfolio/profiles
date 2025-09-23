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

//TODO: Could add a check to see if the URL does not return a 404 or 500 or so
//TODO: Add not needing to restart client if the only reason is to update the time value to request new (non-cached) source code from the server

// Karma configuration
// Generated on Fri Nov 22 2013 15:50:42 GMT-0500 (Eastern Standard Time)
var urlParams = {
   protocol : 'http',
   hostname : 'localhost:52450',
   webResourcesPathSuffix : '/web',
   includeModulesString : '',
   excludeModulesString : '',
   cacheString : new Date().getTime()
// '2'
};
var includeModules = [
      'net.jazz.ajax.xdloader',
      'lconn.profiles.test.specSuite'
];// Took out ",'dojo.cache','net.jazz.ajax.xdloader'" that was there for some
// reason, and 'quickr.lw.tests.l10n', not needed anymore?
// var includeModules =
// ['net.jazz.ajax.xdloader','quickr.lw.tests.widget.thumbnailTest'];
urlParams.includeModulesString = includeModules.join('~') || '~';
var excludeModules = [];
urlParams.excludeModulesString = excludeModules.join('~') || '~';

function getURLs(urlParams) {
   return [
         urlParams.protocol + '://' + urlParams.hostname + urlParams.webResourcesPathSuffix + '/lconn.profiles.test/dojoConfig.js?dojo.preventcache='
               + urlParams.cacheString,
         urlParams.protocol + '://' + urlParams.hostname + urlParams.webResourcesPathSuffix + '/_js?include=' + urlParams.includeModulesString + /*'&exclude='
               + urlParams.excludeModulesString +*/ '&lang=en&debug=true&dojo.preventcache=' + urlParams.cacheString
   ];
}

module.exports = function(config) {
   config.set({
      // base path, that will be used to resolve files and exclude
      basePath : 'http://localhost:52450/web/lconn.profiles.test/',
      // frameworks to use
      frameworks : [ 'jasmine'
      ],
      // list of files / patterns to load in the browser
      files : getURLs(urlParams),
      // list of files to exclude
      exclude : [ ''
      ],
      plugins : [ 'karma-jasmine',
                  'karma-phantomjs-launcher',
                  'karma-chrome-launcher'
      ],
      // test results reporter to use
      // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
      reporters : [        'dots'
      ],
      // web server port
      port : 9876,
      // enable / disable colors in the output (reporters and logs)
      colors : true,
      // level of logging
      // possible values: config.LOG_DISABLE || config.LOG_ERROR ||
      // config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
      logLevel : config.LOG_INFO,
      // enable / disable watching file and executing tests whenever any file
      // changes
      autoWatch : true,
      // Start these browsers, currently available:
      // - Chrome
      // - ChromeCanary
      // - Firefox
      // - Opera (has to be installed with `npm install karma-opera-launcher`)
      // - Safari (only Mac; has to be installed with `npm install
      // karma-safari-launcher`)
      // - PhantomJS
      // - IE (only Windows; has to be installed with `npm install
      // karma-ie-launcher`)
      browsers : [ 'Chrome', 'PhantomJS'
      ],
      // Really just stop-gaps until we refactor tests to consistently hit the same path
      proxies : {
         '/web' : 'http://localhost:52450/web'
      },
      // If browser does not capture in given timeout [ms], kill it
      captureTimeout : 60000,
      // Continuous Integration mode
      // if true, it capture browsers, run tests and exit
      singleRun : false
   });

   console.log("\n");
   console.log("TEST: Running these URLs:");
   console.dir(config.files);
   console.log("\n");
};
