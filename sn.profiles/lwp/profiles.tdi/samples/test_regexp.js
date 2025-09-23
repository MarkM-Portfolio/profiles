/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

// This is a test javascript function. Note that your starting
// function should be "test_my_javascript".  In this example, we
// are testing our regular expression string. The regex variable
// is similar to what you might use for source_ldap_required_dn_regex or
// tds_changelog_ldap_required_dn_regex in your profiles_tdi.properties file
// if you wanted to use a regular expression to limit which DNs were processed.
function test_my_javascript() {
	var dn="uid=0123456789,c=us,ou=myorg,o=acme.com";
	var regex = "/(.*)ou=myorg(.*)/g";
	var evalexp = "pattern = " + regex;
	eval(evalexp);
	if(pattern.test(dn)) {
		task.logmsg("+++match");
	}
	else {
		task.logmsg("+++no match");
	}
}
