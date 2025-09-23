/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.templates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.HierarchicalConfiguration;
import com.ibm.lconn.core.customization.StringResourceLoader;
import com.ibm.lconn.core.web.customization.ServletContextCustomization;
import com.ibm.lconn.core.web.util.lang.LangUtil;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.peoplepages.data.ProfileRetrievalOptions.ProfileOption;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 *  
 */
public class TemplateConfig {

	/**
	 * The set of known template files required by the application to run.
	 */
	public enum TemplateEnum {

		PROFILE_DETAILS("profileDetails", "profileDetails.ftl"), PROFILE_EDIT("profileEdit", "profileEdit.ftl"), BUSINESS_CARD_INFO(
				"businessCardInfo", "businessCardInfo.ftl"), SEARCH_RESULTS("searchResults", "searchResults.ftl");

		private String name;

		private String file;

		private TemplateEnum(String name, String file) {
			this.name = name;
			this.file = file;
		}

		public String getName() {
			return name;
		}

		public String getFile() {
			return file;
		}

		public static TemplateEnum byName(String value) {
			for (TemplateEnum v : TemplateEnum.values()) {
				if (v.getName().equals(value)) {
					return v;
				}
			}
			return null;
		}

		public String toString() {
			return name;
		}
	}

	/**
	 * The NLS bundle name from the templates
	 */
	private static final String BUNDLE_NAME = "nls.template";

	// the logger
	private static final Logger logger = Logger.getLogger(TemplateConfig.class.getName());

	// the singleton FreeMarker configuration object
	private final Configuration configuration;

	// the template string class loader
	private ClassLoader stringClassLoader;

	private final String[] customBundleIds;

	private final Map<TemplateEnum, Set<ProfileOption>> templateToProfileOptions;

	private static Locale EMPTY_LOCALE = new Locale("");

	public TemplateConfig(String profilesConfigurationPath, HierarchicalConfiguration profilesConfig) throws ProfilesRuntimeException {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "TemplateConfig(profilesConfigurationPath, profilesConfig)", new Object[] {
					profilesConfigurationPath, profilesConfig });
		}

		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Profiles Template Configuration initializing");
		}
		
		// time to wait in seconds for reloading of templates
		int templateUpdateDelay = 0;
		// the list of nls bundles identifiers that are required by template
		String nlsBundles = "";
		// the set of options required to process the template
		templateToProfileOptions = new HashMap<TemplateEnum, Set<ProfileOption>>(TemplateEnum.values().length);
		for (TemplateEnum value : TemplateEnum.values()) {
			templateToProfileOptions.put(value, new HashSet<ProfileOption>(ProfileOption.values().length));
		}
		// the edit and details template requires all data right now
		for (ProfileOption option : ProfileOption.values()) {
			templateToProfileOptions.get(TemplateEnum.PROFILE_EDIT).add(option);
			templateToProfileOptions.get(TemplateEnum.PROFILE_DETAILS).add(option);
		}

		// now set specific config based on configuration
		HierarchicalConfiguration layoutConfig = (HierarchicalConfiguration) profilesConfig.subset("layoutConfiguration");
		HierarchicalConfiguration templateConfiguration = (HierarchicalConfiguration) layoutConfig.subset("templateConfiguration");
		HierarchicalConfiguration templateReloadingConfig = (HierarchicalConfiguration) templateConfiguration.subset("templateReloading");
		if (templateReloadingConfig != null) {
			String templateUpdateDelayStr = String.valueOf(templateReloadingConfig.getRoot().getValue());
			if (templateUpdateDelayStr.length() > 0) {
				templateUpdateDelay = Integer.valueOf(templateUpdateDelayStr);
			}
		}

		HierarchicalConfiguration templateNlsBundlesConfig = (HierarchicalConfiguration) templateConfiguration.subset("templateNlsBundles");
		if (templateNlsBundlesConfig != null) {
			nlsBundles = String.valueOf(templateNlsBundlesConfig.getRoot().getValue());
		}
		customBundleIds = nlsBundles.split(" ");

		// iterate the templates to know the required data model
		List<HierarchicalConfiguration.Node> children = templateConfiguration.getRoot().getChildren("template");
		for (HierarchicalConfiguration.Node template : children) {
			HierarchicalConfiguration templateConfig = new HierarchicalConfiguration();
			templateConfig.setRoot(template);
			String templateName = templateConfig.getString("[@name]");
			TemplateEnum key = TemplateEnum.byName(templateName);
			Set<ProfileOption> options = templateToProfileOptions.get(key);

			// iterate the required data model options for retrieval
			HierarchicalConfiguration templateDataModel = (HierarchicalConfiguration) templateConfig.subset("templateDataModel");
			List<HierarchicalConfiguration.Node> templateDataItems = templateDataModel.getRoot().getChildren("templateData");
			for (HierarchicalConfiguration.Node templateDataItem : templateDataItems) {
				HierarchicalConfiguration templateDataItemConfig = new HierarchicalConfiguration();
				templateDataItemConfig.setRoot(templateDataItem);

				String templateDataItemOption = String.valueOf(templateDataItemConfig.getRoot().getValue());
				ProfileOption valueToAdd = ProfileOption.byName(templateDataItemOption);
				if (valueToAdd != null) {
					options.add(valueToAdd);
				}
			}
		}

		String templateDirectoryPath = profilesConfigurationPath + File.separatorChar + "templates";
		String resourcesDirectoryPath = templateDirectoryPath + File.separatorChar + "resources";

		try {

			configuration = new Configuration();
			try {
				configuration.setDirectoryForTemplateLoading(new File(templateDirectoryPath));
				if (templateUpdateDelay > 0) {
					configuration.setTemplateUpdateDelay(templateUpdateDelay);
				}
			}
			catch (Exception e) {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "Unable to find directory for profiles templates in location: " + templateDirectoryPath);
				}
				throw new ProfilesRuntimeException(e);
			}

			configuration.setObjectWrapper(new DefaultObjectWrapper());

			// validate all templates are present, and can be parsed
			for (TemplateEnum template : TemplateEnum.values()) {
				try {
					configuration.getTemplate(template.getFile());
					if (logger.isLoggable(Level.INFO))
					{
						logger.log(Level.INFO, "-- Profile template:" + template.getName() + " parsed without error.");
					}
				}
				catch (Exception e) {
					if (logger.isLoggable(Level.INFO))
					{
						logger.log(Level.INFO, "-- ERROR: Unable to parse the profile interface template: " + template.getFile());
						logger.log(Level.INFO, "--- " + e.getLocalizedMessage());
						logger.log(Level.INFO, "--- This error must be fixed in order for the Profiles application to start.");
					}
					throw new ProfilesRuntimeException(e);
				}
			}

			// load the template resource bundle
			File resourcesDirectory = new File(resourcesDirectoryPath);
			if (!resourcesDirectory.isDirectory()) {
				if (logger.isLoggable(Level.SEVERE)) {
					logger.log(Level.SEVERE, "Unable to load or parse profiles layout template resource files in location:"
							+ resourcesDirectoryPath);
				}

				throw new ProfilesRuntimeException();
			}

			try {
				stringClassLoader = new StringResourceLoader(resourcesDirectory, null);
			}
			catch (FileNotFoundException e) {
			}

			// log the final configuration
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Profiles Template Configuration complete");
				logger.log(Level.INFO, "-- reloading delay:" + templateUpdateDelay + ", bundles:" + nlsBundles);
				for (TemplateEnum t : TemplateEnum.values()) {
					logger.log(Level.INFO, "-- template name:" + t.toString() + ", model: " + templateToProfileOptions.get(t));
				}
			}
		}
		finally {
			if (logger.isLoggable(Level.FINER)) {
				logger.exiting(getClass().getName(), "TemplateConfig");
			}
		}
	}

	public Template getTemplate(TemplateEnum templateEnum) throws Exception {
		return configuration.getTemplate(templateEnum.getFile());
	}

	public void processTemplate(TemplateEnum templateEnum, TemplateDataModel dataModel, Writer out) throws Exception {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "processTemplate(templateEnum, dataModel, out)", new Object[] { templateEnum, dataModel,
					out });
		}

		try {
			Template template = getTemplate(templateEnum);
			template.process(dataModel.getRootMap(), out);
		}
		catch (Exception e) {
			if (logger.isLoggable(Level.FINER)) {
				logger.logp(Level.WARNING, getClass().getName(), "processTemplate",
						"There was an error processing template, see exception for details:" + templateEnum.getFile(), e);
			}
		}
		finally {
			if (logger.isLoggable(Level.FINER)) {
				logger.exiting(getClass().getName(), "processTemplate(templateEnum, dataModel, out)");
			}
		}
	}

	/**
	 * Load a bundle from the template configuration location.
	 * 
	 * @param locale
	 * @param parentClassLoader
	 * @return
	 */
	public ResourceBundle getBundle(Locale locale) {
		ResourceBundle bundle = null;
		try {
			if (locale == null) {
				bundle = ResourceBundle.getBundle(BUNDLE_NAME, EMPTY_LOCALE, stringClassLoader);
			}
			else {
				bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale, stringClassLoader);
				// prevent the default locale bundle from being loaded instead of the root bundle
				if (!LangUtil.isLocaleEquivalentOrMoreSpecific(bundle.getLocale(), locale)) {
					bundle = ResourceBundle.getBundle(BUNDLE_NAME, EMPTY_LOCALE, stringClassLoader);
					// The IBM 1.6 JVM has changed behavior from previous releases - ResourceBundle.getBundle
					// will return the Locale.getDefault() bundle instead of throwing a
					// MissingResourceException if the base bundle (no language) does not exist.
					if (!EMPTY_LOCALE.equals(bundle.getLocale()))
						throw new MissingResourceException("No default bundle present", BUNDLE_NAME, null);
				}
			}
		}
		catch (MissingResourceException e) {
			// missing override bundles are ok to ignore
			if (logger.isLoggable(Level.FINEST)) {
				logger.logp(Level.FINEST, ServletContextCustomization.class.getName(), "getBundle",
						"No override bundle found for bundle ''{0}'' in locale ''{1}''", new Object[] { BUNDLE_NAME, locale });
			}

			throw e;
		}

		return bundle;
	}

	public String[] getCustomBundleIds() {
		return customBundleIds;
	}

	public Set<ProfileOption> getProfileOptionForTemplate(TemplateEnum templateEnum) {
		return templateToProfileOptions.get(templateEnum);
	}
}
