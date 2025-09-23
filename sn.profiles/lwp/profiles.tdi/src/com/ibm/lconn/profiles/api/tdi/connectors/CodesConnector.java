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

package com.ibm.lconn.profiles.api.tdi.connectors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.SearchCriteria;
import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.SearchPair;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDICodeBlock;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDICodeBlockException;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDICodeRunner;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDIConnectorHelper;
import com.ibm.lconn.profiles.api.tdi.service.TDIException;
import com.ibm.lconn.profiles.api.tdi.service.impl.ProfilesTDICRUDServiceImpl;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.lconn.profiles.data.codes.Country;
import com.ibm.lconn.profiles.data.codes.Department;
import com.ibm.lconn.profiles.data.codes.EmployeeType;
import com.ibm.lconn.profiles.data.codes.Organization;
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.lconn.profiles.data.codes.AbstractCode.CodeField;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.BaseCodesService;
import com.ibm.lconn.profiles.internal.service.CountryService;
import com.ibm.lconn.profiles.internal.service.DepartmentService;
import com.ibm.lconn.profiles.internal.service.EmployeeTypeService;
import com.ibm.lconn.profiles.internal.service.OrganizationService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.service.WorkLocationService;




public class CodesConnector extends Connector {
	private static String classname = CodesConnector.class.getName();
	private static final Log LOG = LogFactory.getLog(classname);
	private ResourceBundleHelper _mlp;
	private String _tableName;
	private TDIProfileService _tdiProfileSvc;
	private BaseCodesService<AbstractCode<?>> _baseCodesSvc;
	private Iterator<AbstractCode<?>> _baseCodesCollection;

	/**
	 * Constructor
	 */
	public CodesConnector()
	{
		super();
		// Set the supported modes
		setModes(new String[]{
				ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE,
				ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.UPDATE_MODE,
				ConnectorConfig.DELETE_MODE,
				});
	}
	
	/**
	 * Initialize connector
	 *
	 * @param  o              ConnectorMode
	 * @exception  Exception  Description of the Exception
	 */	
	public void initialize (Object object) throws Exception
	{
		_mlp = new ResourceBundleHelper("profiles_messages");
		_tableName = "";
		String param = getParam("tableName");

	    if (param != null) {
	      param = param.trim();
	      if (param.length() > 0)
	    	  _tableName = param.toString();
	    }
	    else
	    {
	    	//give a error message
	    }
	    ProfilesTDICRUDServiceImpl.getInstance(); 
    
		if (_tdiProfileSvc == null)
			_tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);

		if (_baseCodesSvc == null)
		{

			_baseCodesSvc = getServiceByName(_tableName);

			if(_baseCodesSvc == null)
			{
				//TODO give a log
			}
		}
	}

	private BaseCodesService getServiceByName(String tableName) {
		if(Country.TABLENAME.equals(tableName)) {
			// make sure the Country class is loaded so that the static initializer has registered the service context
			Country.makeSureServiceContextIsRegistered();
			return AppServiceContextAccess.getContextObject(CountryService.class);
		}
		else if(Organization.TABLENAME.equals(tableName)) {
			// make sure the Organization class is loaded so that the static initializer has registered the service context
			Organization.makeSureServiceContextIsRegistered();
			return AppServiceContextAccess.getContextObject(OrganizationService.class);
		}
		else if(WorkLocation.TABLENAME.equals(tableName)) {
			// make sure the WorkLocation class is loaded so that the static initializer has registered the service context
			WorkLocation.makeSureServiceContextIsRegistered();
			return AppServiceContextAccess.getContextObject(WorkLocationService.class);
		}
		else if(EmployeeType.TABLENAME.equals(tableName)) {
			// make sure the EmployeeType class is loaded so that the static initializer has registered the service context
			EmployeeType.makeSureServiceContextIsRegistered();
			return AppServiceContextAccess.getContextObject(EmployeeTypeService.class);
		}
		else if(Department.TABLENAME.equals(tableName)) {
			// make sure the Department class is loaded so that the static initializer has registered the service context
			Department.makeSureServiceContextIsRegistered();
			return AppServiceContextAccess.getContextObject(DepartmentService.class);
		}

		return null;
	}

	public Object querySchema(Object table)throws Exception
	{
		try
		{
			Vector<Entry> result = new Vector<Entry>();
			AbstractCode codeItem;
			if(Country.TABLENAME.equals(_tableName))	
				codeItem = new Country("", new HashMap<String, Object>());
			else if(Organization.TABLENAME.equals(_tableName))
				codeItem = new Organization("", new HashMap<String, Object>());
			else if(WorkLocation.TABLENAME.equals(_tableName))
				codeItem = new WorkLocation("", new HashMap<String, Object>());
			else if(EmployeeType.TABLENAME.equals(_tableName))
				codeItem = new EmployeeType("", new HashMap<String, Object>());
			else if(Department.TABLENAME.equals(_tableName))
				codeItem = new Department("", new HashMap<String, Object>());
			else
				return null;
			if(codeItem != null)
			{
				result.clear();
				List<CodeField> codeFields = codeItem.getFieldDefs();
				for(int i = 0; i < codeFields.size(); i++)
				{
					CodeField codeFieldItem = codeFields.get(i);
					Entry e = new Entry();
					e.setAttribute("name", codeFieldItem.getName());
					e.setAttribute("syntax", codeFieldItem.getType().getName());
					e.setAttribute("type", codeFieldItem.getType().getName());
					result.add(e);
				}
				return result;
			}
			return null;
		}
		catch(Exception e)
		{
			String errorMsg = e.getMessage();
			LOG.error(errorMsg);
			if (getLog() != null) {
		        getLog().logerror(errorMsg, e);
			}
		}
		return null;
	  }
	
	/**
	 * Used by the iterator mode
	 * selectEntries
	 *
	 * @exception  Exception  Description of the Exception
	 */	
	public void selectEntries() throws Exception 
	{
		try
		{
			if(_baseCodesSvc.getAll().size() == 0)
				if (LOG.isErrorEnabled()) {
					LOG.error(_mlp.getString("info_no_data_inTable"));
				}

			_baseCodesCollection = _baseCodesSvc.getAll().iterator();
		}
		catch(Exception e)
		{
			String errorMsg = e.getMessage();
			LOG.error(errorMsg);
			if (getLog() != null) {
		        getLog().logerror(errorMsg, e);
			}
		}
	}

	/**
	 * Used by the iterator mode
	 * Return the next profile entry.
	 *
	 * @return The the next profile entry.
	 */
	public Entry getNextEntry()
	{	
		try
		{
			if(_baseCodesCollection.hasNext())
			{
				AbstractCode item = (AbstractCode)_baseCodesCollection.next();
				return codeToEntry(item);
			}
			else
				return null;
			}
		catch(Exception e)
		{
			String errorMsg = e.getMessage();
			LOG.error(errorMsg);
			if (getLog() != null) {
				getLog().logerror(errorMsg, e);
			}
			return null;
		}
	}
	
	/**
	 * Used by the lookup/delete/update mode
	 * Return the code entry matched with the SearchCriteria.
	 *
	 * @return The code entry.(entry in CodesTable)
	 */
	public Entry findEntry(SearchCriteria search) throws TDIException
	{
		String searchValue = "";
		if (search.getScriptFilter() != null)
		{
			SearchPair searchPair = TDIConnectorHelper.parseSearchScript(search);
			searchValue = searchPair.get_searchValue();
		}
		else
		    searchValue = String.valueOf(search.getCriteria(0).value.toString());
		
		final String searchValueFinal = searchValue;
		
		return TDICodeRunner.run(new CodesConnCodeBlock<Entry>("err_method_findEntry",searchValue)
		{
			public Entry run() throws RuntimeException,TDICodeBlockException 
			{
				clearFindEntries();
				try
				{
					AbstractCode codeItem =  _baseCodesSvc.getById(searchValueFinal);
					if(codeItem != null)
						addFindEntry(codeToEntry(codeItem));

					if (getFindEntryCount() == 1)
						return getFirstFindEntry();
					else 
						return null;
				}
				catch (Exception e) {
					String errorMsg = e.getMessage();
					LOG.error(errorMsg);
					if (getLog() != null) {
				        getLog().logerror(errorMsg, e);
				      }
				}	
				return null;
			}	
		});
	}
	
	/**
	 * Used by the delete mode
	 * delete the code entry matched with the codeId.
	 *@param entry: the entry result get from findEntry.
	 */
	public void deleteEntry (Entry entry, SearchCriteria search) throws TDIException 
	{
		String searchValue = "";
		if (search.getScriptFilter() != null)
		{
			SearchPair searchPair = TDIConnectorHelper.parseSearchScript(search);
			searchValue = searchPair.get_searchValue();
		}
		else
			searchValue = String.valueOf(search.getCriteria(0).value.toString());
		
		final String searchValueFinal = searchValue;
		
		TDICodeRunner.run(new CodesConnCodeBlock<Object>("err_method_deleteItem", searchValue)
		{
			public Entry run() throws RuntimeException,TDICodeBlockException 
			{
				_baseCodesSvc.delete(searchValueFinal);	
				return null;
			}
		});
	}
	
	/**
	 * Used by the addOnly/update(when no entry matched in findEntry function) mode
	 * add this new profile entry.
	 *@param entry: the entry need to be added into codes table.
	 * @throws TDIException 
	 */
	public void putEntry (Entry entry) throws TDIException
	{
		final Entry entryFinal = entry;
		TDICodeRunner.run(new CodesConnCodeBlock<Object>("err_method_putEntry","")
		{
			public Entry run() throws RuntimeException,TDICodeBlockException 
			{
				AbstractCode codeItem = entryToCode(entryFinal);
				if(codeItem != null)
					_baseCodesSvc.create(codeItem);
				else
					throw new TDICodeBlockException(_mlp.getString("err_codeItem_null"));
				return null;
			}
		});
	}
	
	/**
	 * Used by the update(when the entry matched) mode
	 * modify the old record in DB with this newEntry.
	 *@param entry: the new record need to update into the DB
	 *@param search: the SearchCriteria to be used to make the modify call to the underlying system. 
	 * @throws TDIException 
	 */
	public void modEntry (Entry newEntry, SearchCriteria search) throws TDIException
	{
		final Entry newEntryFinal = newEntry;
		TDICodeRunner.run(new CodesConnCodeBlock<Object>("err_method_putEntry","")
		{
			public Entry run() throws RuntimeException,TDICodeBlockException 
			{
				AbstractCode codeItem = entryToCode(newEntryFinal);
				if(codeItem != null)
					_baseCodesSvc.update(codeItem);
				else
				{
					throw new TDICodeBlockException(_mlp.getString("err_codeItem_null"));
				}
				return null;
			}
		});
	}	
	
	public AbstractCode entryToCode(Entry entry){
		// get the code class for the input tableName
		Class<? extends AbstractCode<?>> codeClass = AbstractCode.getCodeClassByName(_tableName);
		if (codeClass == null){
			throw new TDICodeBlockException(_mlp.getString("err_unSupport_tableName",_tableName));
		}
		// get the id CodeField for this code (e.g. 'countryCode')
		CodeField codeIdField =AbstractCode.getCodeIdField(codeClass);
		// get the code id value from the entry (e.g. 'us')
		String codeId = entry.getString(codeIdField.getName());
		if ( codeId == null ){
			throw new TDICodeBlockException(_mlp.getString("err_codeIdInEntry_err"));
		}
		// get the fieldValue mappings from the entry (e.g. <'displayValue','United States'>)
		Map<String, String> codeFieldValueMap = new HashMap<String, String>();
		codeFieldValueMap = this.getCodeFieldValueMap(entry, codeClass);
		// now create the Abstract Code
		AbstractCode rtnVal = createCodeItem(codeClass,codeId,codeFieldValueMap,Tenant.SINGLETENANT_KEY);
		return rtnVal;
	}

	public Entry codeToEntry(AbstractCode<?> item){
		Entry rtnVal = new Entry();
		// get the fields for this code.
		for (CodeField cf : item.getFieldDefs()) {
			String name = cf.getName();
			Object value = item.getFieldValue(cf);
			if ( value != null ) value = value.toString();
			rtnVal.addAttributeValue(name,value);
		}
		return rtnVal;
	}

	// TODO we should have a CodeFactory in the code package
	private AbstractCode createCodeItem(
							Class<? extends AbstractCode<?>> codeClass,
							String codeId,
							Map<String, String> codeFieldValueMap,
							String tenantId){
		AbstractCode rtnVal = null;

		if (codeClass.isAssignableFrom(Country.class)){
			rtnVal = new Country(codeId,tenantId,codeFieldValueMap);
		}
		else if (codeClass.isAssignableFrom(Organization.class)){
			rtnVal = new Organization(codeId,tenantId,codeFieldValueMap);
		}
		else if (codeClass.isAssignableFrom(WorkLocation.class)){
			rtnVal = new WorkLocation(codeId,tenantId,codeFieldValueMap);
		}
		else if (codeClass.isAssignableFrom(EmployeeType.class)){
			rtnVal = new EmployeeType(codeId,tenantId,codeFieldValueMap);
		}
		else if (codeClass.isAssignableFrom(Department.class)){
			rtnVal = new Department(codeId,tenantId,codeFieldValueMap);
		}
		else{
			throw new TDICodeBlockException(_mlp.getString("err_unSupport_tableName"));
		}
		return rtnVal;
	}

	// create a mapping of code fields to values from an input entry. use the registered
	// field mappings in AbstractCode class to extract values.
	private Map<String, String> 
					getCodeFieldValueMap(Entry tdiEntry, Class<? extends AbstractCode<?>> codeClass) {
		// get the CodeFields relevant to this code type
		List<CodeField> fields = AbstractCode.getFieldDefs(codeClass);		
		Map<String, String> codeFieldValueMap = new HashMap<String,String>( fields.size() * 2 );
		for (CodeField field : fields) {
			// so all fields must have a string representation. today all fields are
			// strings and we hence have no validators on the field object to check that
			// a string input is valid.
			codeFieldValueMap.put( field.getName(),  tdiEntry.getString(field.getName()));
		}
		return codeFieldValueMap;
	}
	
	/**
	 * Reusable code block
	 */
	private abstract class CodesConnCodeBlock<T> implements TDICodeBlock<T> {
		private String errMsg;
		private Object[] errMsgParams;
		
		public CodesConnCodeBlock(String errMsg, Object...errMsgParams) {
			this.errMsg = errMsg;
			this.errMsgParams = errMsgParams;
		}		
		
		public T handleTDICodeBlockException(TDICodeBlockException e) throws TDIException
		{
			String formattedMsg = _mlp.getString(errMsg, errMsgParams);
			String errorMsg = formattedMsg + " | " + e.getMessage();
			LOG.error(errorMsg);
			if (getLog() != null) {
		        getLog().logerror(errorMsg, e);
		      }
			throw new TDIException(e.getMessage(), e);
		}
		
		public T handleRecoverable(RuntimeException e)
				throws TDIException 
		{
			String formattedMsg = _mlp.getString(errMsg, errMsgParams);
			LOG.error(formattedMsg);
			throw new TDIException(formattedMsg, e);
		}
		
		public Log getLogger() {
			return LOG;
		}
	}
	
	public String getVersion() {
		return "CodesConnector_4.0.0.0";
	}
}
