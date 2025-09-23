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
package com.ibm.connections.profiles.seedlist;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ilel.seedlist.SeedlistException;
import com.ibm.ilel.seedlist.common.Category;
import com.ibm.ilel.seedlist.common.CategoryInfo;
import com.ibm.ilel.seedlist.common.Field;
import com.ibm.ilel.seedlist.common.FieldInfo;
import com.ibm.ilel.seedlist.common.State;
import com.ibm.ilel.seedlist.common.lconn.LConnEntrySet;
import com.ibm.ilel.seedlist.imp.AbstractEntrySet;
import com.ibm.ilel.seedlist.imp.CategoryImp;
import com.ibm.ilel.seedlist.imp.CategoryInfoImp;
import com.ibm.ilel.seedlist.imp.FacetInfo;
import com.ibm.ilel.seedlist.imp.FacetInfoImp;
import com.ibm.ilel.seedlist.imp.FieldImp;
import com.ibm.ilel.seedlist.imp.FieldInfoImp;
import com.ibm.ilel.seedlist.imp.MetadataImp;
import com.ibm.ilel.seedlist.imp.StateImp;
import com.ibm.ilel.seedlist.retriever.RetrieverRequest;
import com.ibm.ilel.seedlist.retriever.connections.profiles.ProfileState;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.IndexAttributeForConnection;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig;
import com.ibm.lconn.profiles.config.dm.SearchAttributeConfig;
import com.ibm.lconn.profiles.config.dm.SearchFacetConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.config.dm.TagConfig.IndexTagAttribute;
import com.ibm.lconn.profiles.config.dm.XmlFileExtensionAttributeConfig;
import com.ibm.lconn.profiles.config.dm.XmlFileExtensionIndexFieldConfig;
import com.ibm.lconn.profiles.config.dm.XmlFileExtensionIndexFieldConfig.IndexFieldConfig;
import com.ibm.lconn.profiles.data.IndexerSearchOptions;
import com.ibm.lconn.profiles.internal.constants.ProfilesIndexConstants;
import com.ibm.lconn.profiles.internal.util.ProfileSearchUtil;
import com.ibm.ventura.internal.config.exception.VenturaConfigHelperException;
import com.ibm.ventura.internal.config.helper.api.VenturaConfigurationHelper;
import com.ibm.ventura.internal.config.helper.api.VenturaConfigurationHelper.ComponentEntry;

/**
 * Notes:
 * the freemium release introduced excluding email from content on a org basis. to that end the search team
 * (as of 02/17/2015) indicated that profiles will not be requied to scrub email on a per document/user basis.
 * the search app will automatically scrub the email field content from returned content. we need a list of
 * fields (Stephen Wills was to send). profiles must be aware if emails are returned in any othe content. to
 * that end, profiles currently seeds the NAMES tables with email to support simple search. we will not send
 * the NAMES data when on cloud as it adds no extra information. i.e. the only names inserted in the NAMES
 * tables already exists in other fields.
 *
 */
public class EmployeeEntrySet extends AbstractEntrySet implements LConnEntrySet {
	private static final String CLASSNAME = EmployeeEntrySet.class.getName();
	private static final Logger logger = Logger.getLogger(CLASSNAME);
	
	private List documents = Collections.EMPTY_LIST;
	private List seedlists = Collections.EMPTY_LIST;
	
	private boolean isPublic;
	private IndexerSearchOptions nextPageInfo;
	private Date finishDate;
	
	EmployeeEntrySet(List<EmployeeDocument> documents, List seedlists, String seedlistId,
			Date finishDate, boolean isPublic, RetrieverRequest request, 
			HttpServletRequest servletRequest, IndexerSearchOptions nextPageInfo) 
			throws SeedlistException {
		// set seedlist service url
		String method = "EmployeeEntrySet";
		if (logger.isLoggable(Level.FINER))
			logger.entering(CLASSNAME, method, seedlistId);
		
		this.isPublic = isPublic;
		this.nextPageInfo = nextPageInfo;
		this.finishDate = finishDate;
		
		if (nextPageInfo != null) {
		    this.state = new ProfileState(nextPageInfo.getSince(),
						  nextPageInfo.getUntil(),
						  nextPageInfo.getSinceKey(),
						  nextPageInfo.getPageSize(),
						  nextPageInfo.isInitialIndex()
						  ).getState();
		}
		
		// documents
		if (documents != null) {
			this.documents = documents;
		}
		else{
			this.documents = Collections.EMPTY_LIST;
		}
		
		// seedlists
		// ...no seedlists for Profiles
		
		// number of entries
		this.numberOfEntries = this.documents.size() + this.seedlists.size();

		// metadata
		MetadataImp metadata = new MetadataImp();
		
		// set mandatory fields: title, updated
		metadata.setFields(getFields(seedlistId, request));		
		// set fields meta info for seedlist entries 
		metadata.setFieldsInfo(getFieldsInfo(request.getLocale()));
		// set category
		metadata.setCategories(getCategories());

		this.metadata = metadata;
		
        /*
         * Returns timestamp only for the first request in session, so don't 
         * miss Documents, if they are added during paging (inside session)
         */
		//if (nextPageInfo == null) {
		//	timestamp = createNewTimestamp(finishDate.getTime());
		//} else 
		if (documents.size() <= 0) {
			timestamp = createNewTimestamp(finishDate.getTime());
		} 
		
		if (logger.isLoggable(Level.FINER))
			logger.exiting(CLASSNAME, method);
	}
	
	public Iterator getDocuments() {
		String method = "getDocuments";
		if (logger.isLoggable(Level.FINER))
			logger.exiting(CLASSNAME, method);
		return documents.iterator();
	}

	public Iterator getSeedlists() {
		String method = "getSeedlists";
		if (logger.isLoggable(Level.FINER))
			logger.exiting(CLASSNAME, method);
		return seedlists.iterator();
	}

	protected void setFieldSearchProperties(FieldInfoImp theField) {        	
		SearchAttributeConfig searchAttributeConfig = DMConfig.instance().getSearchAttributeConfigs().get(theField.getId());
		if (searchAttributeConfig != null) {
			// the customer has supplied an explicit configuration
			theField.setFieldSearchable(searchAttributeConfig.isFieldSearchable());
			theField.setContentSearchable(searchAttributeConfig.isContentSearchable());
			theField.setReturnable(searchAttributeConfig.isReturnable());
			theField.setExactMatchSupported(searchAttributeConfig.isExactMatchSupported());
			theField.setParametric(searchAttributeConfig.isParametric());
			theField.setSortable(searchAttributeConfig.isSortable());
			
			List<FacetInfo> facets = new ArrayList<FacetInfo>();
			
			for (SearchFacetConfig searchFacetConfig : searchAttributeConfig.getSearchFacetConfigs()) {
				FacetInfoImp theFacet = new FacetInfoImp(searchFacetConfig.getTaxonomy(), searchFacetConfig.getAssociation(),
						searchFacetConfig.getDescription());
				facets.add(theFacet);
			}
			theField.setFacetInfo(facets);
		}
		else {
			// pick up defaults
			theField.setFieldSearchable(true);
			theField.setContentSearchable(true);
			theField.setReturnable(true);
			theField.setExactMatchSupported(false);
			theField.setParametric(false);
			theField.setSortable(false);
		}
	}

	protected FieldInfo[] getFieldsInfo(Locale locale) throws SeedlistException {
		String method = "getFieldsInfo";
		if (logger.isLoggable(Level.FINER))
			logger.entering(CLASSNAME, method);
		
		List<FieldInfo> fieldsInfo = new ArrayList<FieldInfo>();
		
		// UID
		FieldInfoImp uidFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_UID_ID, ProfilesIndexConstants.FIELD_UID_NAME, 
				ProfilesIndexConstants.FIELD_UID_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties( uidFieldInfo);
		fieldsInfo.add(uidFieldInfo);
		
		// DISPLAY_NAME
		FieldInfoImp dnFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_DISPLAY_NAME_ID, ProfilesIndexConstants.FIELD_DISPLAY_NAME_NAME, 
				ProfilesIndexConstants.FIELD_DISPLAY_NAME_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties( dnFieldInfo);
		fieldsInfo.add(dnFieldInfo);
		
		// PREFERRED_FIRST_NAME
		FieldInfoImp pfnFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_PREFERRED_FIRST_NAME_ID, ProfilesIndexConstants.FIELD_PREFERRED_FIRST_NAME_NAME, 
				ProfilesIndexConstants.FIELD_PREFERRED_FIRST_NAME_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(pfnFieldInfo);
		fieldsInfo.add(pfnFieldInfo);
		
		// PREFERRED_LAST_NAME
		FieldInfoImp plnFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_PREFERRED_LAST_NAME_ID, ProfilesIndexConstants.FIELD_PREFERRED_LAST_NAME_NAME, 
				ProfilesIndexConstants.FIELD_PREFERRED_LAST_NAME_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(plnFieldInfo);
		fieldsInfo.add(plnFieldInfo);		
		
		// ALTERNATE_LAST_NAME
		FieldInfoImp alnFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_ALTERNATE_LAST_NAME_ID, ProfilesIndexConstants.FIELD_ALTERNATE_LAST_NAME_NAME, 
				ProfilesIndexConstants.FIELD_ALTERNATE_LAST_NAME_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(alnFieldInfo);
		fieldsInfo.add(alnFieldInfo);
		
		// NATIVE_LAST_NAME
		FieldInfoImp nlnFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_NATIVE_LAST_NAME_ID, ProfilesIndexConstants.FIELD_NATIVE_LAST_NAME_NAME, 
				ProfilesIndexConstants.FIELD_NATIVE_LAST_NAME_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(nlnFieldInfo);
		fieldsInfo.add(nlnFieldInfo);
		
		// NATIVE_FIRST_NAME
		FieldInfoImp nfnFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_NATIVE_FIRST_NAME_ID, ProfilesIndexConstants.FIELD_NATIVE_FIRST_NAME_NAME, 
				ProfilesIndexConstants.FIELD_NATIVE_FIRST_NAME_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(nfnFieldInfo);
		fieldsInfo.add(nfnFieldInfo);
		
		// NAMES are not sent on the cloud. see EmployeeDocument construction which excludes the field.
		if (LCConfig.instance().isLotusLive() == false) {
			// GIVEN_NAME
			FieldInfoImp gnFieldInfo = (new FieldInfoImp(ProfilesIndexConstants.FIELD_GIVEN_NAME_ID,
					ProfilesIndexConstants.FIELD_GIVEN_NAME_NAME, ProfilesIndexConstants.FIELD_GIVEN_NAME_DESC, FieldInfo.TYPE_STRING));
			setFieldSearchProperties(gnFieldInfo);
			fieldsInfo.add(gnFieldInfo);

			// SURNAME
			FieldInfoImp sFieldInfo = (new FieldInfoImp(ProfilesIndexConstants.FIELD_SURNAME_ID, ProfilesIndexConstants.FIELD_SURNAME_NAME,
					ProfilesIndexConstants.FIELD_SURNAME_DESC, FieldInfo.TYPE_STRING));
			setFieldSearchProperties(sFieldInfo);
			fieldsInfo.add(sFieldInfo);
		}
		
		// MAIL
		if(LCConfig.instance().getEmailReturnedDefault()){
			FieldInfoImp mFieldInfo = (new FieldInfoImp(
					ProfilesIndexConstants.FIELD_MAIL_ID, ProfilesIndexConstants.FIELD_MAIL_NAME, 
					ProfilesIndexConstants.FIELD_MAIL_DESC, FieldInfo.TYPE_STRING));
			setFieldSearchProperties(mFieldInfo);
			fieldsInfo.add(mFieldInfo);

			// GROUPWARE_EMAIL
			FieldInfoImp gwmFieldInfo = (new FieldInfoImp(
					 ProfilesIndexConstants.FIELD_GROUPWARE_EMAIL_ID, 
					 ProfilesIndexConstants.FIELD_GROUPWARE_EMAIL_NAME,
					 ProfilesIndexConstants.FIELD_GROUPWARE_EMAIL_DESC,
					 FieldInfo.TYPE_STRING));
			setFieldSearchProperties(gwmFieldInfo);
			fieldsInfo.add(gwmFieldInfo);
		}
			
		// EMPLOYEE_TYPE
		FieldInfoImp etFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_EMPLOYEE_TYPE_ID, ProfilesIndexConstants.FIELD_EMPLOYEE_TYPE_NAME, 
				ProfilesIndexConstants.FIELD_EMPLOYEE_TYPE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(etFieldInfo);
		fieldsInfo.add(etFieldInfo);

		// EMPLOYEE_NUMBER
		FieldInfoImp enFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_EMPLOYEE_NUMBER_ID, ProfilesIndexConstants.FIELD_EMPLOYEE_NUMBER_NAME, 
				ProfilesIndexConstants.FIELD_EMPLOYEE_NUMBER_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(enFieldInfo);
		fieldsInfo.add(enFieldInfo);
		
		// TELEPHONE_NUMBER
		FieldInfoImp tnFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_TELEPHONE_NUMBER_ID, ProfilesIndexConstants.FIELD_TELEPHONE_NUMBER_NAME, 
				ProfilesIndexConstants.FIELD_TELEPHONE_NUMBER_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(tnFieldInfo);
		fieldsInfo.add(tnFieldInfo);

		// Normalized TELEPHONE_NUMBER
		FieldInfoImp ntnFieldInfo = (new FieldInfoImp(
							      ProfileSearchUtil.getNormalizedIndexFieldID(ProfilesIndexConstants.FIELD_TELEPHONE_NUMBER_ID), 
							      ProfileSearchUtil.getNormalizedIndexFieldName(ProfilesIndexConstants.FIELD_TELEPHONE_NUMBER_NAME),
							      ProfileSearchUtil.getNormalizedIndexFieldDesc(ProfilesIndexConstants.FIELD_TELEPHONE_NUMBER_DESC),
							      FieldInfo.TYPE_STRING));
		setFieldSearchProperties(ntnFieldInfo);
		fieldsInfo.add(ntnFieldInfo);
		
		// IP_TELEPHONE_NUMBER
		FieldInfoImp itnFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_IP_TELEPHONE_NUMBER_ID, ProfilesIndexConstants.FIELD_IP_TELEPHONE_NUMBER_NAME, 
				ProfilesIndexConstants.FIELD_IP_TELEPHONE_NUMBER_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(itnFieldInfo);
		fieldsInfo.add(itnFieldInfo);

		// Normalized IP_TELEPHONE_NUMBER
		FieldInfoImp nitnFieldInfo = (new FieldInfoImp(
							       ProfileSearchUtil.getNormalizedIndexFieldID(ProfilesIndexConstants.FIELD_IP_TELEPHONE_NUMBER_ID),
							       ProfileSearchUtil.getNormalizedIndexFieldName(ProfilesIndexConstants.FIELD_IP_TELEPHONE_NUMBER_NAME),
							       ProfileSearchUtil.getNormalizedIndexFieldDesc(ProfilesIndexConstants.FIELD_IP_TELEPHONE_NUMBER_DESC), 
							       FieldInfo.TYPE_STRING));
		setFieldSearchProperties(nitnFieldInfo);
		fieldsInfo.add(nitnFieldInfo);
		
		// JOB_RESPONSIBILITIES
		FieldInfoImp jrFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_JOB_RESPONSIBILITIES_ID, ProfilesIndexConstants.FIELD_JOB_RESPONSIBILITIES_NAME, 
				ProfilesIndexConstants.FIELD_JOB_RESPONSIBILITIES_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(jrFieldInfo);
		// jrFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(jrFieldInfo);
		
		// IS_MANAGER
		FieldInfoImp imFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_IS_MANAGER_ID, ProfilesIndexConstants.FIELD_IS_MANAGER_NAME, 
				ProfilesIndexConstants.FIELD_IS_MANAGER_DESC, FieldInfo.TYPE_BOOLEAN));
		setFieldSearchProperties(imFieldInfo);
		fieldsInfo.add(imFieldInfo);
		
		// FAX
		FieldInfoImp faxFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_FAX_TELEPHONE_NUMBER_ID, ProfilesIndexConstants.FIELD_FAX_TELEPHONE_NUMBER_NAME, 
				ProfilesIndexConstants.FIELD_FAX_TELEPHONE_NUMBER_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(faxFieldInfo);
		fieldsInfo.add(faxFieldInfo);

		// Normalized FAX
		FieldInfoImp nfaxFieldInfo = (new FieldInfoImp(
							       ProfileSearchUtil.getNormalizedIndexFieldID(ProfilesIndexConstants.FIELD_FAX_TELEPHONE_NUMBER_ID),
							       ProfileSearchUtil.getNormalizedIndexFieldName(ProfilesIndexConstants.FIELD_FAX_TELEPHONE_NUMBER_NAME), 
							       ProfileSearchUtil.getNormalizedIndexFieldDesc(ProfilesIndexConstants.FIELD_FAX_TELEPHONE_NUMBER_DESC), 
							       FieldInfo.TYPE_STRING));
		setFieldSearchProperties(nfaxFieldInfo);
		fieldsInfo.add(nfaxFieldInfo);
		
		// MOBILE
		FieldInfoImp mobFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_MOBILE_ID, ProfilesIndexConstants.FIELD_MOBILE_NAME, 
				ProfilesIndexConstants.FIELD_MOBILE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(mobFieldInfo);
		fieldsInfo.add(mobFieldInfo);

		// NOrmalized MOBILE
		FieldInfoImp nmobFieldInfo = (new FieldInfoImp(
							       ProfileSearchUtil.getNormalizedIndexFieldID(ProfilesIndexConstants.FIELD_MOBILE_ID),
							       ProfileSearchUtil.getNormalizedIndexFieldName(ProfilesIndexConstants.FIELD_MOBILE_NAME), 
							       ProfileSearchUtil.getNormalizedIndexFieldDesc(ProfilesIndexConstants.FIELD_MOBILE_DESC), 
							       FieldInfo.TYPE_STRING));
		setFieldSearchProperties(nmobFieldInfo);
		fieldsInfo.add(nmobFieldInfo);
		
		// PAGER_TYPE
		FieldInfoImp ptFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_PAGER_TYPE_ID, ProfilesIndexConstants.FIELD_PAGER_TYPE_NAME, 
				ProfilesIndexConstants.FIELD_PAGER_TYPE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(ptFieldInfo);
		fieldsInfo.add(ptFieldInfo);
		
		// PAGER
		FieldInfoImp pagerFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_PAGER_ID, ProfilesIndexConstants.FIELD_PAGER_NAME, 
				ProfilesIndexConstants.FIELD_PAGER_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(pagerFieldInfo);
		fieldsInfo.add(pagerFieldInfo);

		// Normalized Pager
		FieldInfoImp npagerFieldInfo = (new FieldInfoImp(
							       ProfileSearchUtil.getNormalizedIndexFieldID(ProfilesIndexConstants.FIELD_PAGER_ID),
							       ProfileSearchUtil.getNormalizedIndexFieldName(ProfilesIndexConstants.FIELD_PAGER_NAME), 
							       ProfileSearchUtil.getNormalizedIndexFieldDesc(ProfilesIndexConstants.FIELD_PAGER_DESC), 
							       FieldInfo.TYPE_STRING));
		setFieldSearchProperties(npagerFieldInfo);
		fieldsInfo.add(npagerFieldInfo);
		
		// PAGER_ID
		FieldInfoImp piFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_PAGER_ID_ID, ProfilesIndexConstants.FIELD_PAGER_ID_NAME, 
				ProfilesIndexConstants.FIELD_PAGER_ID_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(piFieldInfo);
		fieldsInfo.add(piFieldInfo);
		
		// PAGER_SERVICE_PROVIDER
		FieldInfoImp pspFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_PAGER_SERVICE_PROVIDER_ID, ProfilesIndexConstants.FIELD_PAGER_SERVICE_PROVIDER_NAME, 
				ProfilesIndexConstants.FIELD_PAGER_SERVICE_PROVIDER_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(pspFieldInfo);
		fieldsInfo.add(pspFieldInfo);
		
		// ORGANIZATION_IDENTIFIER
		FieldInfoImp oiFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_ORGANIZATION_IDENTIFIER_ID, ProfilesIndexConstants.FIELD_ORGANIZATION_IDENTIFIER_NAME, 
				ProfilesIndexConstants.FIELD_ORGANIZATION_IDENTIFIER_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(oiFieldInfo);
		fieldsInfo.add(oiFieldInfo);

		// ORGANIZATION_TITLE
		FieldInfoImp otFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_ORGANIZATION_TITLE_ID, ProfilesIndexConstants.FIELD_ORGANIZATION_TITLE_NAME, 
				ProfilesIndexConstants.FIELD_ORGANIZATION_TITLE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(otFieldInfo);
		otFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(otFieldInfo);
		
		// DEPARTMENT_NUMBER
		FieldInfoImp dnumFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_DEPARTMENT_NUMBER_ID, ProfilesIndexConstants.FIELD_DEPARTMENT_NUMBER_NAME, 
				ProfilesIndexConstants.FIELD_DEPARTMENT_NUMBER_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(dnumFieldInfo);
		fieldsInfo.add(dnumFieldInfo);

		// DEPARTMENT_TITLE
		FieldInfoImp dtitleFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_DEPARTMENT_TITLE_ID, ProfilesIndexConstants.FIELD_DEPARTMENT_TITLE_NAME, 
				ProfilesIndexConstants.FIELD_DEPARTMENT_TITLE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(dtitleFieldInfo);
		dtitleFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(dtitleFieldInfo);
		
		// BUILDING_IDENTIFIER
		FieldInfoImp biFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_BUILDING_IDENTIFIER_ID, ProfilesIndexConstants.FIELD_BUILDING_IDENTIFIER_NAME, 
				ProfilesIndexConstants.FIELD_BUILDING_IDENTIFIER_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(biFieldInfo);
		fieldsInfo.add(biFieldInfo);
		
		// FLOOR
		FieldInfoImp fidFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_FLOOR_ID, ProfilesIndexConstants.FIELD_FLOOR_NAME, 
				ProfilesIndexConstants.FIELD_FLOOR_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(fidFieldInfo);
		fieldsInfo.add(fidFieldInfo);
		
		// ISO_COUNTRY_CODE
		FieldInfoImp isoFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_ISO_COUNTRY_CODE_ID, ProfilesIndexConstants.FIELD_ISO_COUNTRY_CODE_NAME, 
				ProfilesIndexConstants.FIELD_ISO_COUNTRY_CODE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(isoFieldInfo);
		fieldsInfo.add(isoFieldInfo);
		
		// PHYSICAL_DELIVERY_OFFICE
		FieldInfoImp pdFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_PHYSICAL_DELIVERY_OFFICE_ID, ProfilesIndexConstants.FIELD_PHYSICAL_DELIVERY_OFFICE_NAME, 
				ProfilesIndexConstants.FIELD_PHYSICAL_DELIVERY_OFFICE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(pdFieldInfo);
		pdFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(pdFieldInfo);
		
		// WORK_LOCATION
		FieldInfoImp wlFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_WORK_LOCATION_ID, ProfilesIndexConstants.FIELD_WORK_LOCATION_NAME, 
				ProfilesIndexConstants.FIELD_WORK_LOCATION_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(wlFieldInfo);
		wlFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(wlFieldInfo);

		// WORK_LOCATION_CODE
		FieldInfoImp wlcFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_WORK_LOCATION_CODE_ID, ProfilesIndexConstants.FIELD_WORK_LOCATION_CODE_NAME, 
				ProfilesIndexConstants.FIELD_WORK_LOCATION_CODE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(wlcFieldInfo);
		wlcFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(wlcFieldInfo);
		
		// EXPERIENCE
		FieldInfoImp expFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_EXPERIENCE_ID, ProfilesIndexConstants.FIELD_EXPERIENCE_NAME, 
				ProfilesIndexConstants.FIELD_EXPERIENCE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(expFieldInfo);
		expFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(expFieldInfo);
		
		// MANAGER_UID
		FieldInfoImp muidFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_MANAGER_UID_ID, ProfilesIndexConstants.FIELD_MANAGER_UID_NAME, 
				ProfilesIndexConstants.FIELD_MANAGER_UID_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(muidFieldInfo);
		muidFieldInfo.setContentSearchable(false);
		fieldsInfo.add(muidFieldInfo);

		// MANAGER_USERID
		FieldInfoImp muserIdFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_MANAGER_USERID_ID, ProfilesIndexConstants.FIELD_MANAGER_USERID_NAME, 
				ProfilesIndexConstants.FIELD_MANAGER_USERID_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(muserIdFieldInfo);
		muserIdFieldInfo.setContentSearchable(false);
		fieldsInfo.add(muserIdFieldInfo);
		
		// SECRETARY_UID
		FieldInfoImp suidFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_SECRETARY_UID_ID, ProfilesIndexConstants.FIELD_SECRETARY_UID_NAME, 
				ProfilesIndexConstants.FIELD_SECRETARY_UID_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(suidFieldInfo);
		fieldsInfo.add(suidFieldInfo);
		
		FieldInfoImp snameFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_SECRETARY_DISPLAY_NAME_ID, ProfilesIndexConstants.FIELD_SECRETARY_DISPLAY_NAME_NAME, 
				ProfilesIndexConstants.FIELD_SECRETARY_DISPLAY_NAME_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(snameFieldInfo);
		fieldsInfo.add(snameFieldInfo);
		
		// PREFERRED_LANGUAGE
		FieldInfoImp plFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_PREFERRED_LANGUAGE_ID, ProfilesIndexConstants.FIELD_PREFERRED_LANGUAGE_NAME, 
				ProfilesIndexConstants.FIELD_PREFERRED_LANGUAGE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(plFieldInfo);
		fieldsInfo.add(plFieldInfo);
		
		// TIMEZONE
		FieldInfoImp tzFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_TIMEZONE_ID, ProfilesIndexConstants.FIELD_TIMEZONE_NAME, 
				ProfilesIndexConstants.FIELD_TIMEZONE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(tzFieldInfo);
		fieldsInfo.add(tzFieldInfo);
		
		// TYPE
		FieldInfoImp typeFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_TYPE_ID, ProfilesIndexConstants.FIELD_TYPE_NAME, 
				ProfilesIndexConstants.FIELD_TYPE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(typeFieldInfo);
		fieldsInfo.add(typeFieldInfo);
		
		// BLOG_URL
		FieldInfoImp blogFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_BLOG_URL_ID, ProfilesIndexConstants.FIELD_BLOG_URL_NAME, 
				ProfilesIndexConstants.FIELD_BLOG_URL_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(blogFieldInfo);
		fieldsInfo.add(blogFieldInfo);
		
		// FREEBUSY_URL
		FieldInfoImp fbuFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_FREEBUSY_URL_ID, ProfilesIndexConstants.FIELD_FREEBUSY_URL_NAME, 
				ProfilesIndexConstants.FIELD_FREEBUSY_URL_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(fbuFieldInfo);
		fieldsInfo.add(fbuFieldInfo);
		
		// CALENDAR_URL
		FieldInfoImp cuFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_CALENDAR_URL_ID, ProfilesIndexConstants.FIELD_CALENDAR_URL_NAME, 
				ProfilesIndexConstants.FIELD_CALENDAR_URL_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(cuFieldInfo);
		fieldsInfo.add(cuFieldInfo);
		
		// TAG
		FieldInfoImp tagFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_TAG_ID, ProfilesIndexConstants.FIELD_TAG_NAME, 
				ProfilesIndexConstants.FIELD_TAG_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(tagFieldInfo);
		fieldsInfo.add(tagFieldInfo);
		
		//Tagger
		FieldInfoImp taggerFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_TAGGER, ProfilesIndexConstants.FIELD_TAG_TAGGER_NAME, 
				ProfilesIndexConstants.FIELD_TAGGER_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(taggerFieldInfo);
		taggerFieldInfo.setContentSearchable(false);
		fieldsInfo.add(taggerFieldInfo);

		//Tagger UID
		FieldInfoImp taggerUidFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_TAGGER_UID, ProfilesIndexConstants.FIELD_TAG_TAGGER_UID_NAME, 
				ProfilesIndexConstants.FIELD_TAGGER_UID_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(taggerUidFieldInfo);
		taggerUidFieldInfo.setContentSearchable(false);
		fieldsInfo.add(taggerUidFieldInfo);

		// TAG BY TYPE
		for (TagConfig tagConfig : DMConfig.instance().getTagConfigs().values()) {

			// TAG
			String fieldInfoName = IndexTagAttribute.getIndexFieldName(IndexTagAttribute.TAG, tagConfig.getType());
			String fieldInfoDesc = new StringBuilder("Tag of type ").append(tagConfig.getType()).toString();
			String fieldName = new StringBuilder("Tag ").append(tagConfig.getType()).toString();
			FieldInfoImp fieldInfo = new FieldInfoImp(fieldInfoName, fieldName, fieldInfoDesc, FieldInfo.TYPE_STRING);
			setFieldSearchProperties(fieldInfo);
			fieldsInfo.add(fieldInfo);

			// TAGGER
			fieldInfoName = IndexTagAttribute.getIndexFieldName(IndexTagAttribute.TAGGER_DISPLAY_NAME, tagConfig.getType());
			fieldInfoDesc = new StringBuilder("Tagger of type ").append(tagConfig.getType()).toString();
			fieldName = new StringBuilder("Tagger ").append(tagConfig.getType()).toString();
			fieldInfo = new FieldInfoImp(fieldInfoName, fieldName, fieldInfoDesc, FieldInfo.TYPE_STRING);
			setFieldSearchProperties(fieldInfo);
			fieldInfo.setContentSearchable(false);
			fieldsInfo.add(fieldInfo);
			
			// TAGGER UID
			fieldInfoName = IndexTagAttribute.getIndexFieldName(IndexTagAttribute.TAGGER_UID, tagConfig.getType());
			fieldInfoDesc = new StringBuilder("Tagger uid of type ").append(tagConfig.getType()).toString();
			fieldName = new StringBuilder("Tagger uid ").append(tagConfig.getType()).toString();
			fieldInfo = new FieldInfoImp(fieldInfoName, fieldName, fieldInfoDesc, FieldInfo.TYPE_STRING);
			setFieldSearchProperties(fieldInfo);
			fieldInfo.setContentSearchable(false);
			fieldsInfo.add(fieldInfo);

		}
		
		//ABOUT_ME
		FieldInfoImp descFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_ABOUT_ME, ProfilesIndexConstants.FIELD_ABOUT_ME_NAME, 
				ProfilesIndexConstants.FIELD_ABOUT_ME_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(descFieldInfo);
		descFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(descFieldInfo);

		//PROFILE_TYPE
		FieldInfoImp profTypeFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_PROFILE_TYPE, ProfilesIndexConstants.FIELD_PROFILE_TYPE_NAME, 
				ProfilesIndexConstants.FIELD_PROFILE_TYPE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(profTypeFieldInfo);
		fieldsInfo.add(profTypeFieldInfo);
				
		// CONNECTION types
		DMConfig config = DMConfig.instance();
		for (ConnectionTypeConfig ctc : config.getConnectionTypeConfigs().values()) {
			// FIELD_CONNECTIONS_<CONN_TYPE>_FIELD
			String fieldInfoName = IndexAttributeForConnection.getIndexFieldName(IndexAttributeForConnection.TARGET_USER_DISPLAY_NAME, ctc.getType());			
			String fieldInfoDesc = new StringBuilder("Connections ").append(ctc.getType()).append(" field").toString();
			String fieldName = new StringBuilder(ctc.getType()).append(" name").toString();
			FieldInfoImp fieldInfo = new FieldInfoImp(fieldInfoName, fieldName, fieldInfoDesc, FieldInfo.TYPE_STRING);
			setFieldSearchProperties(fieldInfo);
			fieldInfo.setContentSearchable(false);
			fieldsInfo.add(fieldInfo);
			
			// FIELD_CONNECTIONS_<CONN_TYPE>_UID_FIELD
			fieldInfoName = IndexAttributeForConnection.getIndexFieldName(IndexAttributeForConnection.TARGET_USER_UID, ctc.getType());
			fieldInfoDesc = new StringBuilder("Connections ").append(ctc.getType()).append(" UID field").toString();
			fieldName = new StringBuilder(ctc.getType()).append(" UID field").toString();
			fieldInfo = new FieldInfoImp(fieldInfoName, fieldName, fieldInfoDesc, FieldInfo.TYPE_STRING);
			setFieldSearchProperties(fieldInfo);
			fieldInfo.setContentSearchable(false);
			fieldsInfo.add(fieldInfo);			
		}
				
		//Custom extension fields
		for (ExtensionAttributeConfig eac : config.getExtensionAttributeConfig().values()) 
		{ 
		    if ( eac.getExtensionType() == ExtensionAttributeConfig.ExtensionType.XMLFILE ) {
			// Parse the xml and add the xml attributes as fields
			ExtensionAttributeConfig configObj = DMConfig.instance().getExtensionAttributeConfig().get(eac.getExtensionId() );
			XmlFileExtensionAttributeConfig xmlConfig = (XmlFileExtensionAttributeConfig) configObj;
			XmlFileExtensionIndexFieldConfig fieldsConfig = xmlConfig.getIndexFieldConfig();
			List<IndexFieldConfig> indexConfigs = fieldsConfig.getIndexFields();

			for ( IndexFieldConfig ifConfig : indexConfigs ) {
			    String fieldName = ifConfig.getFieldName();
			    String attr = getSeedlistXmlAttribute(eac.getExtensionId(), fieldName );

			    FieldInfoImp customFieldInfo = (new FieldInfoImp(attr.toUpperCase(Locale.US), 
			    		attr, attr, FieldInfo.TYPE_STRING));
			    setFieldSearchProperties(customFieldInfo);
			    fieldsInfo.add(customFieldInfo);
			}
		    }
		    else {
			String name = ProfilesIndexConstants.EXT_ATTR_KEY_BASE + eac.getExtensionId();
			FieldInfoImp customFieldInfo = (new FieldInfoImp(
					name.toUpperCase(Locale.US), name, 
					name, FieldInfo.TYPE_STRING));
			setFieldSearchProperties(customFieldInfo);
			fieldsInfo.add(customFieldInfo);
		    }
		}

		// Work location for 'address1' in WORKLOC table
		FieldInfoImp workLocationFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_LOCATION, ProfilesIndexConstants.FIELD_LOCATION_NAME, 
				ProfilesIndexConstants.FIELD_LOCATION_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(workLocationFieldInfo);
		workLocationFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(workLocationFieldInfo);

		// Work location for 'address2' in WORKLOC table
		FieldInfoImp address2FieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_LOCATION2, ProfilesIndexConstants.FIELD_LOCATION2_NAME, 
				ProfilesIndexConstants.FIELD_LOCATION2_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(address2FieldInfo);
		address2FieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(address2FieldInfo);
		
		// City
		FieldInfoImp cityFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_CITY, ProfilesIndexConstants.FIELD_CITY_NAME, 
				ProfilesIndexConstants.FIELD_CITY_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(cityFieldInfo);
		fieldsInfo.add(cityFieldInfo);

		FieldInfoImp stateFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_STATE, ProfilesIndexConstants.FIELD_STATE_NAME, 
				ProfilesIndexConstants.FIELD_STATE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(stateFieldInfo);
		fieldsInfo.add(stateFieldInfo);
		
		//Country
		FieldInfoImp countryFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_COUNTRY, ProfilesIndexConstants.FIELD_COUNTRY_NAME, 
				ProfilesIndexConstants.FIELD_COUNTRY_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(countryFieldInfo);
		fieldsInfo.add(countryFieldInfo);

		//Postal Code
		FieldInfoImp postalFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_POSTAL_CODE, ProfilesIndexConstants.FIELD_POSTAL_CODE_NAME, 
				ProfilesIndexConstants.FIELD_POSTAL_CODE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(postalFieldInfo);
		fieldsInfo.add(postalFieldInfo);

		//User State
		FieldInfoImp userStateFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_USER_STATE_ID, ProfilesIndexConstants.FIELD_USER_STATE_NAME, 
				ProfilesIndexConstants.FIELD_USER_STATE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(userStateFieldInfo);
		// userStateFieldInfo.setContentSearchable(false);
		fieldsInfo.add(userStateFieldInfo);

		//User org membership
		if ( DataAccessConfig.instance().getOrgSettings().isEnabled() ) {
		    FieldInfoImp userOrgMemFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_USER_ORG_MEM_ID, ProfilesIndexConstants.FIELD_USER_ORG_MEM_NAME, 
				ProfilesIndexConstants.FIELD_USER_ORG_MEM_DESC, FieldInfo.TYPE_STRING));
		    setFieldSearchProperties(userOrgMemFieldInfo);
		    userOrgMemFieldInfo.setContentSearchable(false);
		    fieldsInfo.add(userOrgMemFieldInfo);
		    
		    //User org acl
		    FieldInfoImp userOrgAclFieldInfo = (new FieldInfoImp(
				 ProfilesIndexConstants.FIELD_USER_ORG_ACL_ID, ProfilesIndexConstants.FIELD_USER_ORG_ACL_NAME, 
				 ProfilesIndexConstants.FIELD_USER_ORG_ACL_DESC, FieldInfo.TYPE_STRING));
		    setFieldSearchProperties(userOrgAclFieldInfo);
		    userOrgAclFieldInfo.setContentSearchable(false);
		    fieldsInfo.add(userOrgAclFieldInfo);
		}

		// For Atom URL, added since LC3.0.1.1
		FieldInfoImp atomURLFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_ATOMAPISOURCE_ID, ProfilesIndexConstants.FIELD_ATOMAPISOURCE_NAME, 
				ProfilesIndexConstants.FIELD_ATOMAPISOURCE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(atomURLFieldInfo);
		atomURLFieldInfo.setContentSearchable(false);
		atomURLFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(atomURLFieldInfo);

		// Source URL, not indexed
		/*
		FieldInfoImp sourceURLFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_SOURCE_URL_ID, ProfilesIndexConstants.FIELD_SOURCE_URL_NAME, 
				ProfilesIndexConstants.FIELD_SOURCE_URL_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(sourceURLFieldInfo);
		sourceURLFieldInfo.setContentSearchable(false);
		sourceURLFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(sourceURLFieldInfo);
		*/

		// shift, added since LC 3.5
		FieldInfoImp shiftFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_SHIFT_ID, ProfilesIndexConstants.FIELD_SHIFT_NAME, 
				ProfilesIndexConstants.FIELD_SHITF_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(shiftFieldInfo);
		// shiftFieldInfo.setContentSearchable(false);
		shiftFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(shiftFieldInfo);

		// courtesy title, added since LC 3.5
		FieldInfoImp courtesyFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_COURTESY_TITLE, ProfilesIndexConstants.FIELD_COURTESY_TITLE_NAME, 
				ProfilesIndexConstants.FIELD_COURTESY_TITLE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(courtesyFieldInfo);
		// courtesyFieldInfo.setContentSearchable(false);
		courtesyFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(courtesyFieldInfo);

		// title, added since LC 3.5
		FieldInfoImp titleFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_TITLE, ProfilesIndexConstants.FIELD_TITLE_NAME, 
				ProfilesIndexConstants.FIELD_TITLE_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(titleFieldInfo);
		// titleFieldInfo.setContentSearchable(false);
		titleFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(titleFieldInfo);

		// tenant key, added since LC 3.5
		FieldInfoImp tenantFieldInfo = (new FieldInfoImp(
				ProfilesIndexConstants.FIELD_TENANT_KEY, ProfilesIndexConstants.FIELD_TENANT_NAME, 
				ProfilesIndexConstants.FIELD_TENANT_DESC, FieldInfo.TYPE_STRING));
		setFieldSearchProperties(tenantFieldInfo);
		// tenantFieldInfo.setContentSearchable(false);
		tenantFieldInfo.setExactMatchSupported(false);
		fieldsInfo.add(tenantFieldInfo);

//TODO - we don't have this home org info without separately looking it up
//		// home org id, added after LC 5.5
//		FieldInfoImp homeTenantFieldInfo = (new FieldInfoImp(
//				ProfilesIndexConstants.FIELD_HOME_ORG_ID, ProfilesIndexConstants.FIELD_HOME_ORG_NAME, 
//				ProfilesIndexConstants.FIELD_HOME_ORG_DESC, FieldInfo.TYPE_STRING));
//		setFieldSearchProperties(homeTenantFieldInfo);
//		// tenantFieldInfo.setContentSearchable(false);
//		homeTenantFieldInfo.setExactMatchSupported(false);
//		fieldsInfo.add(homeTenantFieldInfo);

		if (logger.isLoggable(Level.FINER))
			logger.exiting(CLASSNAME, method);
		return fieldsInfo.toArray(new FieldInfo[0]);
	}
	
	protected Field[] getFields(String seedlistId, RetrieverRequest request)
			throws SeedlistException {
		String method = "getFields";
		if (logger.isLoggable(Level.FINER))
			logger.entering(CLASSNAME, method, seedlistId);
		
		List<Field> fields = new ArrayList<Field>();

		// set title
		String title = "Profiles : " + getNumberOfEntries() 
				+ " entries of Seedlist " + seedlistId; 
		FieldImp titleField = new FieldImp(title, Field.FIELD_TITLE);
		titleField.setLocale(request.getLocale());
		fields.add(titleField);
		
		// set updated date 
		fields.add(new FieldImp(getSeedlistUpdateDate(), Field.FIELD_UPDATE_DATE));
		
		if (logger.isLoggable(Level.FINER))
			logger.exiting(CLASSNAME, method);
		return fields.toArray(new Field[0]);
	}

	protected Category[] getCategories() {
		String method = "getCategories";
		if (logger.isLoggable(Level.FINER))
			logger.entering(CLASSNAME, method);
		
		CategoryImp csCategory = new CategoryImp(new CategoryInfoImp(
				ProfilesIndexConstants.PROFILES_CATEGORY, ProfilesIndexConstants.PROFILES_CATEGORY),
				ProfilesIndexConstants.FEATURE_TAXONOMY_TYPE);
		
		csCategory.setPathFromRoot(new CategoryInfo[] { new CategoryInfoImp(
				ProfilesIndexConstants.CONTENT_SOURCE_TYPE_CATEGORY, 
				ProfilesIndexConstants.CONTENT_SOURCE_TYPE_CATEGORY) });
		
		if (logger.isLoggable(Level.FINER))
			logger.exiting(CLASSNAME, method);
		return new Category[] {csCategory};
	}

	protected Date getSeedlistUpdateDate() {
		String method = "getSeedlistUpdateDate";
		if (logger.isLoggable(Level.FINER))
			logger.exiting(CLASSNAME, method);
		return finishDate;
	}

    /**
     *  A method to generate seedlist fields for xml extension attributes.
     *
     *  The format is like this: FIELD_EXTATTR_PROFILELINKS_LINKNAME
     */
    protected static String getSeedlistXmlAttribute(String extId, String attr){
    	String retval = attr;
    	retval = ProfilesIndexConstants.EXT_ATTR_KEY_BASE + extId + "_" +attr;
    	return retval.toUpperCase(Locale.US);
    }
    
	// ------------- 	LConnEntrySet    ----------------------------------- //
	// ------------- 	functions for building next page url    ------------ //
	public String getComponentUrl() {
        String method = "getComponentUrl";
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(CLASSNAME, method); 
        }
        
        String componentUrl = "";
        
        try {
            VenturaConfigurationHelper vcp = VenturaConfigurationHelper
            		.Factory.getInstance();
            ComponentEntry componentConfig = vcp.getComponentConfig("profiles");
            String url = componentConfig.getInterServiceUrl().toExternalForm();

    		// Set seedlist service secure url
    		if (isPublic) {
    			componentUrl = url + "/seedlist/server"; 
    		} else { 
    			componentUrl = url + "/seedlist/myserver"; 
    		}
    		
            if (logger.isLoggable(Level.FINER)) {
                logger.exiting(CLASSNAME, method);
            }
        } catch (VenturaConfigHelperException e) {
            if (logger.isLoggable(Level.FINER)) {
            	logger.warning("Exception building seedlist service url using " +
            			"connections config xml for Profiles.");
                logger.throwing(CLASSNAME, method, e);
            }
        }
        
        return componentUrl;
	}
    
	// not used any more
	public Date getDateParam() {
		String method = "getDateParam";
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(CLASSNAME, method);
        }
		return null;
	}

	// not used any more
	public int getStartParam() {
		String method = "getStartParam";
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(CLASSNAME, method);
        }
		return 0;
	}
	
	private State createNewTimestamp(long timeInMillis) {
		String method = "createNewTimestamp";
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(CLASSNAME, method);
        }
		// long can't be more than 8 bytes
		ByteBuffer timeBuf = ByteBuffer.allocate(8);
		// define timestamp as current time in milliseconds
		timeBuf.putLong(timeInMillis);
		return new StateImp(timeBuf.array());
	}
}
