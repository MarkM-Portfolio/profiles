/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

function func_map_to_db_MANAGER_UID(fieldname) {
	var result = null;

	/*---------------------------------------------------------------
	 * In some environments, the UID is in the DN so we will grab 
	 * the UID from the managers DN                                         
	 *---------------------------------------------------------------*/
	var dn = work.getString("manager");
	if(dn != null) {
		if(dn.startsWith("uid=")) {
			var commaPos = dn.indexOf(",");
			if(commaPos > 4) {
				result = dn.substring(4, commaPos);
			}
		}
		else if(dn.startsWith("CN=")) {
			var commaPos = dn.indexOf(",");
			if(commaPos > 3) {
				result = dn.substring(3, commaPos);
			}
		}	
		else {
			var start = dn.indexOf(",uid=");
			if(start > 0) {
				start = start + 5; /* skip past ,uid= */
				var commaPos = dn.indexOf(",", start);
				if(commaPos > start) {
					result = dn.substring(start, commaPos);
				}
			}
		}
	}
	
	return result;
}

function func_map_to_db_SECRETARY_UID(fieldname) {
	var result = null;

	/*---------------------------------------------------------------
	 * In some environments, the UID is in the DN so we will grab 
	 * the UID from the managers DN                                         
	 *---------------------------------------------------------------*/
	var dn = work.getString("secretary");
	if(dn != null) {
		if(dn.startsWith("uid=")) {
			var commaPos = dn.indexOf(",");
			if(commaPos > 4) {
				result = dn.substring(4, commaPos);
			}
		}
		else if(dn.startsWith("CN=")) {
			var commaPos = dn.indexOf(",");
			if(commaPos > 3) {
				result = dn.substring(3, commaPos);
			}
		}
		else {
			var start = dn.indexOf(",uid=");
			if(start > 0) {
				start = start + 5; /* skip past ,uid= */
				var commaPos = dn.indexOf(",", start);
				if(commaPos > start) {
					result = dn.substring(start, commaPos);
				}
			}
		}
	}
	
	return result;
}

function func_map_to_db_UID(fieldname) {
	var result = null;

	/*---------------------------------------------------------------
	 * In many organizations, the UID is a field itself.  It can 
	 * also often be found in the DN. This function checks first for
	 * a source field named uid and if not there, tries to parse the
	 * uid from the DN.  To use this function you would need the 
	 * following property in your map_dbrepos_from_source.properties:
	 * PROF_UID={func_map_to_db_UID} 
	 * and you would need to specify this file as the value of the 
	 * source_ldap_map_functions_file property in karaoke.properties
	 *---------------------------------------------------------------*/
	result = work.getString("uid");
	if(result == null) {
		var dn = work.getString("$dn");
		if(dn != null) {
			if(dn.startsWith("uid=") || dn.startsWith("UID=")) {
				var commaPos = dn.indexOf(",");
				if(commaPos > 4) {
					result = dn.substring(4, commaPos);
				}
			}
			else if(dn.startsWith("CN=") || dn.startsWith("cn=")) {
				var commaPos = dn.indexOf(",");
				if(commaPos > 3) {
					result = dn.substring(3, commaPos);
				}
			}			
			else {
				var start = dn.indexOf(",uid=");
				if(start > 0) {
					start = start + 5; /* skip past ,uid= */
					var commaPos = dn.indexOf(",", start);
					if(commaPos > start) {
						result = dn.substring(start, commaPos);
					}
				}
			}
		}
	}

	return result;
}

// get the abbreviated form of a Lotus Notes mail address.
// In this example we'll assume the name of the LDAP field
// we are getting this from is notesemail. You would need
// to change this for another field name
function function_map_from_notes_email(fieldname) {
	var result = work.getString("notesemail");
	if(result != null) {
		//We call a function provided with Profiles
		result = abbreviate_notes_email(result);
		//We decided to remove any additional ending @ arguments as well
		var pos = result.indexOf("@");
		if(pos > 0) {
			result = result.substring(0, pos);
		}
	}

	return result;
}

// function to compute the givenName source LDAP value if
// givenName is not supplied in the original LDAP record. This 
// function would be used only if in profiles_tdi.properties
// source_ldap_compute_function_for_givenName=func_compute_givenName
function func_compute_givenName(fieldname) {
	var result = work.getAttribute("givenName");
	if(result == null) {
		var cnAttr = work.getAttribute("cn");
		if(cnAttr != null) {
			values = new Array();
			for(i=0; i < cnAttr.size(); ++i) {
				val = cnAttr.getValue(i);
				if(val != "") {
					var parts = val.split(" ");
					if(parts.length > 1) {
						var found = false;
						for(j=0; !found && (j < values.length); ++j) {
							if(parts[0] == values[j]) {
								found = true;
							}
						}
						if(!found) {
							values.push(parts[0]);
						}
					}
				}
			}
			if(values.length > 0) {
				result = system.newAttribute("givenName");
				for(j=0; j < values.length; ++j) {
					result.addValue(values[j]);
				}
			}
		}
	}

	return result;
}

// If this function is invoked, it is assumed you have follow the instructions in 
// map_dbrepos_from_source.properties above these lines.
// 
//#displayName={func_decorate_displayName_if_visitor}
//#displayNameLdapAttr=cn
//#decorateVisitorDisplayName= (visitor)

// 
// 
function func_decorate_displayName_if_visitor(fieldname) {

	var result;
	var modeVal;
	var displayNameLdapAttrVal;
	var decorateVisitorDisplayNameVal;

	// get the cached value.  If is "" (empty string) if it hasn't been set.
	var modePropValue = com.ibm.lconn.profiles.api.tdi.util.ProfilesTDIState.getString("modePropValue");

	// see if this is first time thru, and thus need to retrieve info from map_dbrepos_from_source.properties 
	if (modePropValue.length == 0)
	{
		// load the map_dbrepos_from_source properties
		var map_inStream = new java.io.FileInputStream("map_dbrepos_from_source.properties");
		var map_props = new java.util.Properties();

		map_props.load(map_inStream );

		// retrieve mode= property
		modeVal = map_props.getProperty("mode");

		// note that it is ok to compare null to the empty string
		// if there is no mode, i.e., mode= or #mode=, then there will be no decoration
		if (modeVal == "")
			modeVal = null;  // treat "mode=" same as "#mode="
		
		if (modeVal == null) {
			// if no mode=, then no need to decorate, so put non-sense value in cache to record this. (Can't use "")
			com.ibm.lconn.profiles.api.tdi.util.ProfilesTDIState.setString("modePropValue", "$%^&"); // nonsense value
		}
		else {
			com.ibm.lconn.profiles.api.tdi.util.ProfilesTDIState.setString("modePropValue", modeVal);
		}

		// retrieve/cache the ldap attribute where display name comes from.
		displayNameLdapAttrVal = map_props.getProperty("displayNameLdapAttr");
		if (displayNameLdapAttrVal == null)
			displayNameLdapAttrVal == "";
		com.ibm.lconn.profiles.api.tdi.util.ProfilesTDIState.setString("displayNameLdapAttrPropValue", displayNameLdapAttrVal);

		// retrieve/cache decoration text.  Note that the map_dbrepos_from_source.properties file must be in UTF8
		// if non-ascii chars are used.
		decorateVisitorDisplayNameVal = map_props.getProperty("decorateVisitorDisplayName");
		if (decorateVisitorDisplayNameVal == null)
			decorateVisitorDisplayNameVal == "";
		com.ibm.lconn.profiles.api.tdi.util.ProfilesTDIState.setString("decorateVisitorDisplayNamPropValue", decorateVisitorDisplayNameVal);
	}
	else
	{
		// retrive cached values since this isn't first time thru
		modeVal = com.ibm.lconn.profiles.api.tdi.util.ProfilesTDIState.getString("modePropValue");
		if (modeVal == "$%^&")
			modeVal = null;

		displayNameLdapAttrVal = com.ibm.lconn.profiles.api.tdi.util.ProfilesTDIState.getString("displayNameLdapAttrPropValue");
		decorateVisitorDisplayNameVal = com.ibm.lconn.profiles.api.tdi.util.ProfilesTDIState.getString("decorateVisitorDisplayNamPropValue");
	}

	// test code since can't display/print stuff from here
	//return (work.getAttribute("cn") + " " + modeVal + " " + displayNameLdapAttrVal + " " + decorateVisitorDisplayNameVal);

	// test for no mode= value, i.e., mode= or #mode=
	if (modeVal == null)
	{
		if ((displayNameLdapAttrVal.length >= 3) && (modeVal.charAt(0) == "{"))  
		{
			var dispFunctionName = modeVal.substring( 1, displayNameLdapAttrVal.length - 1);
			var dispEvalStr = dispFunctionName + '("displayName");';

			// execute the function
			result = eval(dispEvalStr);
		}
		else
		{
			// if mode= empty value, no decoration to display name
			result = work.getString(displayNameLdapAttrVal);
		}
	}
	else
	{
		// now need to figure out if mode= is simple ldap attribute or a function,
		// and act accordingly
		if (modeVal.charAt(0) != "{")
		{
			// mode= value is simple attribute.
			var valueOfModeLdapAttr = work.getString(modeVal);

			if ((displayNameLdapAttrVal.length >= 3) && (modeVal.charAt(0) == "{"))  
			{
				var dispFunctionName = modeVal.substring( 1, displayNameLdapAttrVal.length - 1);
				var dispEvalStr = dispFunctionName + '("displayName");';

				// execute the function
				result = eval(dispEvalStr);
			}
			else
			{
				// if mode= empty value, no decoration to display name
				result = work.getString(displayNameLdapAttrVal);
			}

			if (valueOfModeLdapAttr == "external") {
				result = result + decorateVisitorDisplayNameVal;
			}
		}
		else
		{
			// it's a function
			if ((displayNameLdapAttrVal.length >= 3) && (modeVal.charAt(0) == "{"))  
			{
				var dispFunctionName = modeVal.substring( 1, displayNameLdapAttrVal.length - 1);
				var dispEvalStr = dispFunctionName + '("displayName");';

				// execute the function
				result = eval(dispEvalStr);
			}
			else
			{
				// if mode= empty value, no decoration to display name
				result = work.getString(displayNameLdapAttrVal);
			}

			var modeFunctionName = modeVal.substring( 1, modeVal.length - 1);
			var modeEvalStr = modeFunctionName + '("mode");';

			// execute the function
			var userModeStrFromFunc = eval(modeEvalStr);

			if (userModeStrFromFunc == "external") {
				result = result + decorateVisitorDisplayNameVal;
			}
		}
	}

	return result;
}


// function to compute the sn source LDAP value if
// sn is not supplied in the original LDAP record. This 
// function would be used only if in profiles_tdi.properties
// source_ldap_compute_function_for_sn=func_compute_sn
function func_compute_sn(fieldname) {
	var result = work.getAttribute("sn");
	if(result == null) {
		var cnAttr = work.getAttribute("cn");
		if(cnAttr != null) {
			values = new Array();
			for(i=0; i < cnAttr.size(); ++i) {
				val = cnAttr.getValue(i);
				if(val != "") {
					var parts = val.split(" ");
					if(parts.length > 0) {
						var found = false;
						for(j=0; !found && (j < values.length); ++j) {
							if(parts[parts.length-1] == values[j]) {
								found = true;
							}
						}
						if(!found) {
							values.push(parts[parts.length-1]);
						}
					}
				}
			}
			if(values.length > 0) {
				result = system.newAttribute("sn");
				for(j=0; j < values.length; ++j) {
					result.addValue(values[j]);
				}
			}
		}
	}

	return result;
}

// ID Conversion for objectSID - MS AD/ADAM
function function_map_from_objectSID(fieldname) {
    var octetString = null;
    var canonicalString = "";
    var attr = work.getAttribute("objectSID");

    if(attr != null) {
        octetString = attr.getValue(0);

        if(octetString != null) {
            canonicalString = com.ibm.connections.directory.services.util.ObjectSIDConverter.convertByteArrayToSIDString(octetString);
        } 
    }
    return canonicalString;
}

// ID Conversion for objectGUID - MS AD/ADAM
function function_map_from_objectGUID(fieldname) {
    var octetString = null;
    var canonicalString = "";
    var attr = work.getAttribute("objectGUID");

    if(attr != null) {
        octetString = attr.getValue(0);

        if(octetString != null) {
            canonicalString = com.ibm.connections.directory.services.util.ObjectGUIDConverter.convertBinaryToGUIDString(octetString);
        } 
    }
    return canonicalString;
}

// ID Conversion for GUID - Novell eDirectory
function function_map_from_GUID(fieldname) {
    var octetString = null;
    var canonicalString = "";
    var attr = work.getAttribute("GUID");

    if(attr != null) {
        octetString = attr.getValue(0);

        if(octetString != null) {
            canonicalString = com.ibm.connections.directory.services.util.ObjectGUIDConverter.convertBinaryToGUIDString(octetString);
        } 
    }
    return canonicalString;
}

// ID Conversion for dominoUNID - Lotus Domino
function function_map_from_dominoUNID(fieldname) {
    var byteString = null;
    var canonicalString = "";
    var attr = work.getAttribute("dominoUNID");

    if(attr != null) {
        byteString = attr.getValue(0);

        if(byteString != null) {
            canonicalString = com.ibm.connections.directory.services.util.ObjectGUIDConverter.convertByteStringToGUIDString(byteString);
        } 
    }
    return canonicalString;
}

function func_map_to_db_hashEmail(fieldName) {
  var mail = work.getString("mail");
  var hashType = work.getString("hashtype");

  var retval = "Unknown";

  if ( hashType == null ) 
	hashType = "MD5";

  if ( mail != null ) 
	retval = com.ibm.lconn.profiles.api.tdi.util.HashEmail.hashString( mail, hashType);

 return retval;
}


// Below is function func_mode_from_ldap_attr which is example of a function
// that determines mode from one or more ldap attributes.  In this case, 
// it is not intended to be particularly realistic
//
// The function should return either "internal" or "external".  Below we set the
// value to "internal" and then set it to "external" if the common name contains 
// "visitor" or "contractor", i.e., the display name is decorated in the ldap,
// or the employee number begins with "V" or "C", or ends with an odd digit.
//
// Note that the function can also return "abort" to signal an error to stop 
// processing.
//
// It would be unusual, but if you wish to use a mult-valued attribute,
// look at this section of the documentation for the basics of how to
// do this;
//      Installing >> Pre-installation tasks >> Populating the Profiles
//      Database >> Adding source data to the Profiles Database >>
//		Mapping Fields Manually >> Example complex mapping of Profiles data.
function func_mode_from_ldap_attr(fieldName) {

	// general form:
	//    anyLdapAttribute can be any ldap attribute, e.g., mail, cn, employeeNumber...
	//var ldapValue = work.getString("anyLdapAttribute");

	// get display name, which is cn (commmon name) is this example, and employeeNumber
	var dispName = work.getString("cn");
	var empNumber = work.getString("ibm-socialPersonId"); // the name in my test ldap

	// validate that at least one of the ldap attrs exists.
	if (dispName == null && employeeNumber == null)
		return "abort";	  // this value signals the assembly to abort

	// check display name from ldap for decoration
	if (dispName != null)
	{
		if (dispName.indexOf("visitor") >= 0 || dispName.indexOf("contractor") >= 0)
			return "external";
	}

	// check employee number from ldap
	if (empNumber != null)
	{
		if (empNumber.startsWith("V") || empNumber.startsWith("C"))
			return "external";

		var lastChar = empNumber.charAt(empNumber.length -1);
		if ((lastChar == "1") || (lastChar == "3") || (lastChar == "5") || (lastChar == "7") || (lastChar == "9"))
			return "external";

	}

	return "internal";
}

// function that determines mode from branch
// note that it is intended to be realistic
function func_mode_visitor_branch(fieldName) {
	// if value not set, i.e., this is first time thru, then map entry is created and empty string is returned.
	// otherwise, it is "internal" or "external".
	var userModeString = com.ibm.lconn.profiles.api.tdi.util.ProfilesTDIState.getString("userModeString");
	// test for first time thru
	if (userModeString.length > 0)
	{
		// not first time thru, mode (internal/external) is cached
		return userModeString;
	}

	// let's figure out the mode based on visitor branch
	var retval = "internal";
	var bAllVisitorNull = false;

	var inStream = new java.io.FileInputStream("profiles_tdi.properties");
	var props = new java.util.Properties();

	props.load(inStream );

	var source_ldap_url = props.getProperty("source_ldap_url");
	var source_ldap_search_base = props.getProperty("source_ldap_search_base");
	var source_ldap_search_filter = props.getProperty("source_ldap_search_filter");

	// make empty string, e.g., source_ldap_url=, same as null 
	// note that it is ok to compare null to the empty string
	if (source_ldap_url == "")
		source_ldap_url = null;
	if (source_ldap_search_base == "")
		source_ldap_search_base = null;
	if (source_ldap_search_filter == "")
		source_ldap_search_filter = null;

	var source_ldap_url_visitor = props.getProperty("source_ldap_url_visitor_confirm");
	var source_ldap_search_base_visitor = props.getProperty("source_ldap_search_base_visitor_confirm");
	var source_ldap_search_filter_visitor = props.getProperty("source_ldap_search_filter_visitor_confirm");

	// make empty string, e.g., source_ldap_url_visitor=, same as null 
	if (source_ldap_url_visitor == "")
		source_ldap_url_visitor = null;
	if (source_ldap_search_base_visitor == "")
		source_ldap_search_base_visitor = null;
	if (source_ldap_search_filter_visitor == "")
		source_ldap_search_filter_visitor = null;

	// The first rule is that source_ldap_url and source_ldap_search_base must be non-null. Filter can be null??
	if (source_ldap_url == null || source_ldap_search_base == null)
	{
		return "abort";
	}

	// The second rule is that either the visitor properties must all be null, or equal to the
	// operational properties if external.  Here we make sure that if either source_ldap_url_visitor or
	// source_ldap_search_base_visitor is null, then all are null.
	if (source_ldap_url_visitor == null || source_ldap_search_base_visitor == null)
	{
		if (source_ldap_url_visitor != null || source_ldap_search_base_visitor != null || source_ldap_search_filter_visitor != null)
		{
			return "abort";
		}
		bAllVisitorNull = true;
		
	}

	// if ! bAllVisitorNull, source_ldap_url_visitor must be equal to source_ldap_url
	// if ! bAllVisitorNull, source_ldap_search_base_visitor must be equal to source_ldap_search_base
	if (! bAllVisitorNull)
	{
		// note that none of the variables below can be null at this point
		if ((source_ldap_url != source_ldap_url_visitor) ||
		    (source_ldap_search_base != source_ldap_search_base_visitor))
		{
			return "abort";
		}

		// filter is a bit more complicated because it can be null
		if (source_ldap_search_filter_visitor != null)
		{
			if (source_ldap_search_filter == null)
			{
				// visitor in non-null, std is null, so abort
				return "abort";
			}
			else
			if (source_ldap_search_filter != source_ldap_search_filter_visitor)
			{
				// both are non-null, but are not equal
				return "abort";
			}
			// fall thru if both non-null, and are equal
		}
		else
		{
			// filter visitor is null
			if (source_ldap_search_filter != null)
			{
				// filter visitor is null, but std is non-null
				return "abort";
			}
			// fall thru if both null
		}
	}

	// given all the checks above, if source_ldap_url_visitor is non-null, then return value should be 'external'
	if (source_ldap_url_visitor != null)
		retval = "external";

	// cache mode
	com.ibm.lconn.profiles.api.tdi.util.ProfilesTDIState.setString("userModeString", retval);

	return retval;
}
