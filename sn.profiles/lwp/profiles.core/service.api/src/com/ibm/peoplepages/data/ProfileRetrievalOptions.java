/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.data;

import com.ibm.lconn.profiles.data.AbstractDataObject;

/**
 * @author ahernm@us.ibm.com
 * 
 */
public class ProfileRetrievalOptions extends AbstractDataObject<ProfileRetrievalOptions> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 51396142781583732L;

	/**
	 * Absolute minimum; no codes resolved
	 */
	public static final ProfileRetrievalOptions MINIMUM = new ProfileRetrievalOptions(Verbosity.MINIMAL);

	/**
	 * Lite profile including code resolution.
	 */
	public static final ProfileRetrievalOptions LITE = new ProfileRetrievalOptions(Verbosity.LITE, ProfileOption.CODES,
			ProfileOption.USERSTATE);

	/**
	 * Kitchen sink.
	 */
	public static final ProfileRetrievalOptions EVERYTHING = new ProfileRetrievalOptions(Verbosity.FULL, ProfileOption.values());

	/**
	 * For TDI
	 */
	public static final ProfileRetrievalOptions TDIOPTS = new ProfileRetrievalOptions(Verbosity.FULL, ProfileOption.EXTENSIONS,
			ProfileOption.USERSTATE);

	/**
	 * Flag to verbosity of the resolved profile. Methods may use this as a hint and resolve additioanal data. - MINIMAL (key, email, guid,
	 * uid, source_uid(aka DN), displayName) - LITE old lite - FULL everything
	 */
	public static enum Verbosity {
		MINIMAL, LITE, FULL
	};

	/**
	 * Indicates what additional features to resolve for the profile
	 */
	public static enum ProfileOption {
		EXTENSIONS("extensions"), CODES("codes"), ASSISTANT("secretary"), MANAGER("manager"), USERSTATE("user"), CONNECTION("connection");

		private String name;

		private ProfileOption(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static ProfileOption byName(String v) {
			for (ProfileOption o : ProfileOption.values()) {
				if (o.getName().equals(v))
					return o;
			}
			return null;
		}
		
		public String toString()
		{
			return name;
		}
	};

	private Verbosity verbosity = Verbosity.LITE;
	private ProfileOption[] options = { ProfileOption.CODES };
	private boolean allowCache = true;

	/**
	 * default CTOR
	 */
	public ProfileRetrievalOptions() {
	}

	public ProfileRetrievalOptions(Verbosity verbosity, ProfileOption... options) {
		this.verbosity = verbosity;
		this.options = options;
	}

	public ProfileRetrievalOptions(ProfileRetrievalOptions pro) {
		if (pro != null) {
			this.verbosity = pro.verbosity;
			this.options = pro.options;
			this.allowCache = pro.allowCache; // not sure - see setAllowCache below.
		}
	}

	public Verbosity getVerbosity() {
		return verbosity;
	}

	public boolean getOptions(ProfileOption option) {
		for (ProfileOption o : options) {
			if (o == option)
				return true;
		}

		return false;
	}

	public ProfileOption[] getOptions() {
		return options;
	}

	/**
	 * @return the allowCache
	 */
	public final boolean isAllowCache() {
		return allowCache;
	}

	/**
	 * NOTE: This method clones the whole object to not break the immutabality assumption on this object. This it performs a clone and
	 * returns the cloned object.
	 * 
	 * This decision was made to reduce the number of places in the code that could be broken by this.
	 * 
	 * @param allowCache
	 *            the allowCache to set
	 */
	public final ProfileRetrievalOptions setAllowCache(boolean allowCache) {
		if (this.allowCache == allowCache) {
			return this;
		}
		else {
			ProfileRetrievalOptions options = clone();
			options.allowCache = allowCache;
			return options;
		}
	}
}
