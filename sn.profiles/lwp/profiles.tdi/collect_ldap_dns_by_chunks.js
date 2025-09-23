/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

//
// I will not filter via the search base
//
function suppliesSearchBase() { return false; }

function getNextSearchBase() { return null; }

//
// I will filter via the search filter
//
function suppliesSearchFilter() { return true; }

var oneNumberMeansFilterLessThanOnly = false; // don't use
var nullEntriesPermitted = true;	// user set

var count = 0;
var done = false;
var almostDone = false;

// note that the name below is the LDAP attribute name, i.e., the property VALUES
// in map_dbrepos_from_source.properties
var attrFieldName = "mail";		// user set

var minAttrVal = null;
var maxAttrVal = null;
var maxAttrValArrayIndex = 0;

var maxAttrValArray = [		// user set

"ajones182@janet.iris.com",
"ajones272@janet.iris.com",

];


function getLdapFilterPrefix() {
	return "(&";
}

function getLdapFilterSuffix() {
	return "(objectclass=inetOrgPerson)(uid=*))";		// user set
}

function getCurrentMaxAttrVal() {
	return maxAttrVal;
}

function getCurrentMinAttrVal() {
	return minAttrVal;
}


function getNextMaxAttrVal() {
	var result = null;
	if(maxAttrValArrayIndex < maxAttrValArray.length) {
		result = maxAttrValArray[maxAttrValArrayIndex];
		++maxAttrValArrayIndex;
	}

	return result;
}



function getNextSearchFilter() {
	var filter = null;
	if(!done) {
		if (almostDone)
		{
			done = true;

			if (nullEntriesPermitted)
			{
				filter = getLdapFilterPrefix() + "(!(" + attrFieldName + "=*))" +	getLdapFilterSuffix();
			}

			return filter;
		}

		minAttrVal = maxAttrVal;
		maxAttrVal = getNextMaxAttrVal();

		if(maxAttrVal != null) {
			++count;
			if (minAttrVal == null) {
				// first time thru
				filter = getLdapFilterPrefix() + "(" + attrFieldName + "<=" + maxAttrVal + ")" + "(!(" + attrFieldName + "=" + maxAttrVal + "))" + getLdapFilterSuffix();
			}
			else {
				// rest of times thru except last
				filter = getLdapFilterPrefix() + "(" + attrFieldName + ">=" + minAttrVal + ")(" + attrFieldName + "<=" + maxAttrVal +")" + "(!(" + attrFieldName + "=" + maxAttrVal + "))"  + getLdapFilterSuffix();
			}
		}
		else {
			/* If we have only one filter number total, and flag set according, we only want numbers less */
			if ((count > 1) || !oneNumberMeansFilterLessThanOnly)
			{
				filter = getLdapFilterPrefix() + "(" + attrFieldName + ">=" + minAttrVal + ")" +	getLdapFilterSuffix();
			}

			if (filter == null)
				done = true;
			else
				almostDone = true;
		}
	}

	task.logmsg('*********************************');
	task.logmsg('THE FILTER IS '+filter);
	task.logmsg('*********************************');

	return filter;
}


