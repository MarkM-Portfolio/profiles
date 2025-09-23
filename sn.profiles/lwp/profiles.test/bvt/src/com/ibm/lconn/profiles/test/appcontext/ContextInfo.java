/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2016                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.appcontext;

/*
 * Class that contains all the info concerning context 'type's
 * constructors to generate them and query their characteristics
 * and calculators to return certain aspects of a context type.
 */
public class ContextInfo
{
	private ContextType ctxType = null;

	private boolean ctxSettings [] = null;

	public ContextInfo(ContextType _ctxType)
	{
		boolean[] _ctxSettings = null;
		switch (_ctxType) {
			case isPublicAdminRoleContext:
				_ctxSettings = isPublicAdminRoleSettings;
				break;
			case isBSSContext:
				_ctxSettings = isBSSContextSettings;
				break;
			case isLLISContext:
				_ctxSettings = isLLISContextSettings;
				break;
			case isTDIContext:
				_ctxSettings = isTDIContextSettings;
				break;
			case isAdminClientContext:
				_ctxSettings = isAdminClientContextSettings;
				break;
			case isPublicNonAdminRoleContext:
				_ctxSettings = isPublicNonAdminRoleSettings;
				break;
			default:
				// report error ?
				_ctxSettings = isPublicNonAdminRoleSettings;
				break;
		}
		ctxType     = _ctxType;
		ctxSettings = _ctxSettings;
	}

	public ContextInfo(ContextType _ctxType, boolean[] _ctxSettings)
	{
		ctxType     = _ctxType;
		ctxSettings = _ctxSettings;
	}

	public ContextType getContextType() {
		return ctxType;
	}

	public boolean[] getContextSettings() {
		return ctxSettings;
	}

	public ContextInfo getPublicAdminRoleContext() {
		return new ContextInfo(ContextType.isPublicAdminRoleContext);
	}
	public ContextInfo getBSSAdminContext() {
		return new ContextInfo(ContextType.isBSSContext);
	}
	public ContextInfo getLLISAdminContext() {
		return new ContextInfo(ContextType.isLLISContext);
	}
	public ContextInfo getTDAdminContext() {
		return new ContextInfo(ContextType.isTDIContext);
	}
	public ContextInfo getAdminClientContext() {
		return new ContextInfo(ContextType.isAdminClientContext);
	}
	public ContextInfo getPublicNonAdminRoleContext() {
		return new ContextInfo(ContextType.isPublicNonAdminRoleContext);
	}

	public boolean isPublicAdminRoleContext()
	{
		int inx = ctxType.ordinal();
		return (inx == ContextType.isPublicAdminRoleContext.ordinal());
//		return ctxSettings[ContextType.isPublicAdminRoleContext.ordinal()];
	}
	public boolean isBSSContext()
	{
		int inx = ctxType.ordinal();
		return (inx == ContextType.isBSSContext.ordinal());
	}
	public boolean isLLISContext()
	{
		int inx = ctxType.ordinal();
		return (inx == ContextType.isLLISContext.ordinal());
	}
	public boolean isTDIContext()
	{
		int inx = ctxType.ordinal();
		return (inx == ContextType.isTDIContext.ordinal());
	}
	public boolean isAdminClientContext()
	{
		int inx = ctxType.ordinal();
		return (inx == ContextType.isAdminClientContext.ordinal());
	}
	public boolean isPublicNonAdminRoleContext() {
		int inx = ctxType.ordinal();
		return (inx == ContextType.isPublicNonAdminRoleContext.ordinal());
 	}

	public String getContextName()
	{
		String name = "undefined - error";
		if (isPublicAdminRoleContext())
			name = "isAdmin";
		else if (isBSSContext())
			name = "isBSSContext";
		else if (isLLISContext())
			name = "isLLISContext";
		else if (isTDIContext())
			name = "isTDIContext";
		else if (isAdminClientContext())
			name = "isAdminClient";
		else if (isPublicNonAdminRoleContext())
			name = "isNonAdmin";

		return name;
	}

	public String getContextSettingsAsString()
	{
		StringBuilder sb = new StringBuilder();
		boolean[] ctxSettings = getContextSettings();
		for (int i = 0; i < ctxSettings.length; i++) {
			sb.append(ctxSettings[i]);
			sb.append(" ");
		}
		return sb.toString();
	}

	public enum ContextType {
		isPublicAdminRoleContext, isBSSContext, isLLISContext, isTDIContext, isAdminClientContext, isPublicNonAdminRoleContext
	}
	// settings for the 'normal' production runtime scenarios
	// to add permutations for non-normal scenarios, add settings below
	// this set of variables represent the run-time settings for a test
	// comprising : admin role on/off  -and- a context type for each of the test scenarios
	// Since this testing is primarily concerned with AdminProfileServiceImpl scenarios, the admin role
	// will be 'on' in most of these scenarios; only 'off' in the public user test scenario
	//							runtime context is:		isAdminRole, isBSSContext, isLLISContext, isTDIContext
	private static boolean isPublicAdminRoleSettings	[] = { true,   false,        false,         false }; // typically this is WAS Admin
	private static boolean isBSSContextSettings			[] = { true,   true,         false,         false }; // BSS client
	private static boolean isLLISContextSettings		[] = { true,   false,        true,          false }; // LLIS client
	private static boolean isTDIContextSettings			[] = { true,   false,        false,         true  }; // TDI client
	private static boolean isAdminClientContextSettings	[] = { true,   false,        false,         false }; // Admin API client (onPrem J2EE admin & onCloud org-admin)
	private static boolean isPublicNonAdminRoleSettings	[] = { false,  false,        false,         false }; // normal user UI client

	private static ContextType [] contextTypes = {
			ContextType.isPublicAdminRoleContext,
			ContextType.isBSSContext,
			ContextType.isLLISContext,
			ContextType.isTDIContext,
			ContextType.isAdminClientContext,
			ContextType.isPublicNonAdminRoleContext
	};
	public static ContextType[] getContextTypes() {
		return contextTypes;
	}

}
