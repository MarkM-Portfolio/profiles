/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;

import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;


/**
 *
 *
 */
public abstract class AbstractName<NT extends AbstractName<NT>> extends AbstractDataObject<NT> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -541458872421206879L;

	/**
	 * NameType object; used for informational purposes
	 */
	public enum NameType {
		GIVENNAME,
		SURNAME
	}
	
	/**
	 * Name source object. Used to indicate the source of the name: the
	 * repository, name expansion, or user-definition.
	 */
	public enum NameSource {

		SourceRepository(0),
		NameExpansion(1),
		UserDefined(2);
		
		private final int code;
		private NameSource(int code) {
			this.code = code;
		}
		
		public final int getCode() {
			return code;
		}
		
        public static NameSource getNameSourceByCode(int code) {
            if (code == NameSource.SourceRepository.getCode())
                return NameSource.SourceRepository;
            else if (code == NameSource.NameExpansion.getCode())
                return NameSource.NameExpansion;
            else if (code == NameSource.UserDefined.getCode())
                return NameSource.UserDefined;
            else 
                throw new IllegalArgumentException("Illegal name source code value: " + code);
        }

	}
	
	private String key;
	private String name;
	private NameSource source = NameSource.SourceRepository;
	private UserState usrState;
	private UserMode userMode;
	
	/**
	 * @return the key
	 */
	public final String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public final void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the source
	 */
	public final NameSource getSource() {
		return source;
	}
	/**
	 * @param source the source to set
	 */
	public final void setSource(NameSource source) {
		if (source == null) return;
		this.source = source;
	}
	/**
	 * @return the surname
	 */
	public final String getName() {
		return name;
	}
	/**
	 * @param name the surname to set
	 */
	public final void setName(String name) {
		if (name == null) 
			throw new NullPointerException("Name may not be null!");
		String trimmedName = name.trim();
		this.name = trimmedName.toLowerCase();
	}

	/**
	 * Returns the 'NameType' of this object.
	 * 
	 * @return
	 */
	public abstract NameType getType();
	/**
	 * @return the usrState
	 */
	public final UserState getUsrState() {
		return usrState;
	}
	/**
	 * @param usrState the usrState to set
	 */
	public final void setUsrState(UserState usrState) {
		this.usrState = usrState;
	}
	
	/**
	 * @return the user mode
	 */
	public final UserMode getUserMode() {
		return userMode;
	}
	/**
	 * @param userMode the user mode to set
	 */
	public final void setUserMode(UserMode userMode) {
		this.userMode = userMode;
	}
}
