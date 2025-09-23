/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data.codes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.profiles.data.AbstractDataObject;

/**
 * Base class for all codes
 *
 *
 */
// AbstractCode is a base class for all concrete code classes.
// It maintains
//   String codeId: the code id; e.g. "us"
//   String recordType: the type of code; e.g. "country"
//   String queryParam: not sure - haven't seen it used; e.g. "countryCode"
//   CodeType codeType: enum indicating type of code;
//   String tenantKey: the tenant key. This should match all CodeField objects (see AbstractDataObject)
//   Map<CodeField,Object> fieldValues: see below].
// The AbstractCode object holds a list of values in the 'fieldValues' attribute.
// The codeId attribute (e.g., "us") is thus associated with the map of values (e.g. 
// <displayVale,United States>. The CodeField object defines the 'fields' that comprise
// the code values. In the "us" example, the CodeField is an abstraction
// of the 'displayValue' field. 
//    AbstractCode.codeId   ---------------*  Map<CodeField,Object>
//           "us" -------------------- {<"displayValue","United States">}
// A WorkLocation object has multiple values, so the above simplified diagram may look as follows
//           "ibm-littleton" --------- {<"workloc","Littleton">,
//                                      <"address1","501 King Street">
//                                      <"city","Littleton">
//                                      etc. }
// The CodeField object has the following attributes
// String name: The name of the field; e.g. "displayValue"
//   Class<?> type: the class (type) for this value: e.g. "United States" is java;lang.String
//   boolean codeId: is this field a codeId
//   String tenantId
// CodeField objects are predefined (class variables) in each of the concrete code classes.
// These CodeField objects are then associated with values. The codeId and Map Object values
// are ultimately persisted, and reversed mapped into a Code object on retrieval.

public abstract class AbstractCode<T extends AbstractCode<T>> extends AbstractDataObject<T> {

	private static final long serialVersionUID = -2836827778109197004L;

	private static final Log LOG = LogFactory.getLog(AbstractCode.class);

	// Temp code list for initialization. Each class creates its CodeFields as static attributes
	// and they are registered here. See CodeField constructor. Once a concrete code class has
	// constructed all its CodeFields, it calls a 'finalize' method and these entries are moved
	// to codesFieldMap. This is a map
	//   code.class  --- <CodeField.name,CodeField>
	// so e.g., for WorkLocation code an entry looks like
	//   WorkLocation.class --- <'workLocationCode', WorkLocationCode.F_WORK_LOCATION_CODE> 
	//                      --- <'address1',WorkLocationCode.F_ADDRESS1
	//                      --- <'address2',WorkLocationCode.F_ADDRESS2
	//                      --- etc
	private static final Map<Class<?>, Map<String,CodeField>> tCodeFieldsMap 
		= new ConcurrentHashMap<Class<?>, Map<String,CodeField>>();
	
	// Finalized code map - see comments above tCodeFieldsMap. When a concrete
	// class 'finalizes' its static list of CodeFields, a map entry is created here
	// as well as in codeFieldsList.
	private static final Map<Class<?>, Map<String,CodeField>> codeFieldsMap 
		= new ConcurrentHashMap<Class<?>, Map<String,CodeField>>();
	
	// Finalized code list - see comments above tCodeFieldsMap. When a concrete
	// class 'finalizes' its static list of CodeFields, a map entry is created here
	// as well as in tCodeFieldsMap
	private static final Map<Class<?>, List<CodeField>> codeFieldsList
		= new ConcurrentHashMap<Class<?>, List<CodeField>>();
	
	// Finalized map of id fields - see comments above tCodeFieldsMap. When a concrete
	// class 'finalizes' its static list of CodeFields, a map entry is created here
	// to hold the special 'codeId' CodeField entry.
	private static final Map<Class<?>, CodeField> codeIdFieldMap
		= new ConcurrentHashMap<Class<?>, CodeField>();
	
	// Map of names to Codes - used in tdi to map e.g. name (countryCodes) to Country.class
	// generally used as a first lookup to above maps.
	private static Map<String,Class<? extends AbstractCode<?>>> nameToCodeMap =
		new ConcurrentHashMap<String, Class<? extends AbstractCode<?>>>();

	/**
	 * Code field definition class
	 */
	public static class CodeField  {		
		
		// The tenantid in this object must match that of the containing AbstractCode instance.
		// It is the CodeField value that is ultimately used for persistence.
		private static final long serialVersionUID = -6348302887395381234L;
		
		private final String name;
		private final Class<?> type;
		private final boolean codeId;
		
		@SuppressWarnings("synthetic-access")
		protected CodeField(Class<? extends AbstractCode<?>> codeClass, String name, Class<?> type, boolean codeId) {
			this.name = name;
			this.type = type;
			this.codeId = codeId;
			addCode(codeClass, this);
		}
		
		@SuppressWarnings("synthetic-access")
		protected CodeField(Class<? extends AbstractCode<?>> codeClass, String name, Class<?> type) {
			this(codeClass, name, type, false);
		}

		/**
		 * @return the name
		 */
		public final String getName() {
			return name;
		}

		/**
		 * @return the type
		 */
		public final Class<?> getType() {
			return type;
		}
		
		/**
		 * @return the codeId
		 */
		public final boolean isCodeId() {
			return codeId;
		}
	}
	/**
	 * End: Code field definition class
	 */
	
	/*
	 * Internal method for adding code fields
	 */
	private static int addCode(Class<? extends AbstractCode<?>> codeClass, CodeField field) {
		Map<String,CodeField> fields = tCodeFieldsMap.get(codeClass);
		if (fields == null) {
			if (codeFieldsMap.containsKey(codeClass)){
				throw new RuntimeException("CODING ERROR!!! Attempting to add code to finalized code list");
			}
			fields = new Hashtable<String,CodeField>();
			tCodeFieldsMap.put(codeClass, fields);
		} else {
			if (fields.containsKey(field.getName())){
				throw new RuntimeException("Redefinition of CodeField: " + codeClass.getName() + " / " + field.getName());
			}
		}
		fields.put(field.getName(), field);
		
		return fields.size() - 1;
	}
	
	/*
	 * Internal method to finalize list of code fields
	 */
	@SuppressWarnings("unchecked")
	protected static final void finalizeFieldList(Class<? extends AbstractCode<?>> codeClass) {
		Map<String,CodeField> fields = tCodeFieldsMap.remove(codeClass);
		if (fields == null){
			throw new IllegalArgumentException("No fields defined for class: " + codeClass.getClass());
		}
		Object prev = codeFieldsMap.put(codeClass, Collections.unmodifiableMap(new HashMap<String,CodeField>(fields)));
		if (prev != null){
			throw new RuntimeException("CODING ERROR!!! Attempting to finalize code-list on previously finalized code list");
		}
		codeFieldsList.put(codeClass, Collections.unmodifiableList(new ArrayList<CodeField>(fields.values())));
		
		// validate that there are one and only one codeId in the set of fields
		int codeIdCount = 0;
		for (CodeField f : codeFieldsList.get(codeClass)) {
			if (f.isCodeId()) {
				if (String.class != f.getType()){
					throw new RuntimeException("CODING ERROR!!! CodeId field may only be of string type");
				}
				codeIdFieldMap.put(codeClass, (CodeField) f);
				codeIdCount++;
			}
		}
		if (codeIdCount != 1){
			throw new RuntimeException("CODING ERROR!!! Attempting to finalize code-list and for class with no 'code-id' value");
		}
	}
	
	protected static void putNameToCodeMap(String name, Class<? extends AbstractCode<?>> codeType){
		nameToCodeMap.put(name,codeType);		
	}
	
	/**
	 * 
	 * @param codeType
	 * @throws IllegalArgumentException If there are no fields defined for the code type.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static final Map<String,CodeField> getFieldDefMap(Class<? extends AbstractCode> codeType) {
		Map<String,CodeField> r = codeFieldsMap.get(codeType);
		if (r == null){
			throw new IllegalArgumentException("No fields defined for class: " + codeType.getClass());
		}
		return r;
	}

	/**
	 * 
	 * @param codeType
	 * @return
	 */
	public static final List<CodeField> getFieldDefs(Class<? extends AbstractCode<?>> codeType) {
		List<CodeField> l = codeFieldsList.get(codeType);
		if (l == null){
			throw new IllegalArgumentException("No fields defined for class: " + codeType.getClass());
		}
		return l;
	}

	/**
	 * 
	 * @param codeType
	 * @return
	 * @throws IllegalArgumentException If there is no field of the specified name defined.
	 */
	@SuppressWarnings("unchecked")
	// private - we don't know what it means to expose tenant
	public static final CodeField getFieldDef(Class<? extends AbstractCode> codeType, String name) throws IllegalArgumentException {
		CodeField cf = getFieldDefMap(codeType).get(name);
		if (cf == null){
			throw new IllegalArgumentException("No field named '"+ name + "' defined for class " + codeType.getName());
		}
		return cf;
	}
	
	/**
	 * Gets the code ID field for a class
	 * @param codeType
	 * @return
	 */
	// private - we don't know what it means to expose tenant
	public static final CodeField getCodeIdField(Class<? extends AbstractCode<?>> codeType) {
		return codeIdFieldMap.get(codeType);
	}
	
	public static Class<? extends AbstractCode<?>> getCodeClassByName( String tableName ){
		return nameToCodeMap.get(tableName);
	}
	
	/**
	 * Map of field values
	 */
	private final Map<CodeField,Object> fieldValues = new HashMap<CodeField,Object>();
	private final String codeId;
	private final String recordType;
	private final String queryParam;
	private final CodeType codeType;

	/**
	 * Method to convert Map to code object. Useful to support legacy (&lt;=2.5.x)
	 * codes format. In future releases, we want to allow for user to define
	 * arbitrary code values. This class is designed to move in that direction.
	 * 
	 * @param codeId
	 * @param values
	 * @param recordType
	 * @param queryParam
	 */
	protected AbstractCode(
			String codeId, 
			Map<String, ? extends Object> values, 
			String recordType,
			String queryParam) 
	{
		this.codeId = codeId;
		this.recordType = recordType;
		this.queryParam = queryParam;
		this.codeType = CodeType.getCodeType(this.getClass());
		
		for (Map.Entry<String, ? extends Object> v : values.entrySet()){
			if (v.getValue() != null){
				CodeField cf = getFieldDef(getClass(),v.getKey());
				setFieldValue(cf,v.getValue());
			}
		}
	}

	/**
	 * Method to convert Map to code object. Useful to support legacy (&lt;=2.5.x)
	 * codes format. In future releases, we want to allow for user to define
	 * arbitrary code values. This class is designed to move in that direction.
	 * 
	 * @param codeId
	 * @param tenantKey
	 * @param values
	 * @param recordType
	 * @param queryParam
	 */
	protected AbstractCode(
			String codeId,
			String tenantKey,
			Map<String, ? extends Object> values, 
			String recordType,
			String queryParam)
	{
		this.codeId = codeId;
		this.recordType = recordType;
		this.queryParam = queryParam;
		this.codeType = CodeType.getCodeType(this.getClass());
		setTenantKey(tenantKey); // superclass has tenantKey
		
		for (Map.Entry<String, ? extends Object> v : values.entrySet()){
			if (v.getValue() != null){
				CodeField cf = getFieldDef(getClass(), v.getKey());
				setFieldValue(cf,v.getValue());
				//setFieldValue(getFieldDef(getClass(),v.getKey()), v.getValue());
			}
		}
	}

	/**
	 * Internal method for setting field values.  Fields are essentially immutable, so setting should only happen at initialization.
	 * 
	 * @param field
	 * @param value
	 */
	private final void setFieldValue(CodeField field, Object value) {
		if (field == null) {
			throw new NullPointerException("field property may not be null");
		}
		else if (!field.getType().isAssignableFrom(value.getClass())) {
			throw new RuntimeException("CODING ERROR!!! invalid value type set (" + value.getClass() + ") for code-field: " + field);
		}
		
		fieldValues.put(field, value);
	}
	
	/**
	 * Method to get code field value
	 * 
	 * @param field
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "hiding" })
	public final <T> T getFieldValue(CodeField field) {
		if (field.isCodeId()){
			return (T) getCodeId();
		}
		return (T) fieldValues.get(field);
	}

	/**
	 * @return the codeId
	 */
	public final String getCodeId() {
		return codeId;
	}
	
	/**
	 * Utility method to get list of fields
	 * 
	 * @return
	 */
	public final List<CodeField> getFieldDefs() {
		return codeFieldsList.get(getClass());
	}
	
	/**
	 * Utility method to get field def for class
	 * @param name
	 * @return
	 */
	public final CodeField getFieldDef(String name) {
		return AbstractCode.getFieldDef(getClass(), name);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return codeId.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object obj) {
		// Short cut method to make faster
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		// curious issue here with tenantKey. should we enforce tenantKey?
		//if (this.getTenantKey() == null && ((AbstractCode<?>)obj).getTenantKey() != null){
		//	return false;
		//}
		//else if (((AbstractCode<?>)obj).getTenantKey() == null){
		//	return true; // both are null
		//}
		//else if (this.getTenantKey().equals(((AbstractCode<?>)obj).getTenantKey()) == false){
		//	return false;
		//}		
		AbstractCode<?> ac = (AbstractCode<?>) obj;
		for (CodeField f : getFieldDefs()) {
			if (!ObjectUtils.equals(getFieldValue(f), ac.getFieldValue(f))){
				return false;
			}
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.data.AbstractDataObject#toString()
	 */
	@Override
	public String toString() {
		Map<String,Object> m = valueMap();
		return m.toString();
	}
	
	/**
	 * Utility method to covert values into a Map
	 * 
	 * @return New map containing values
	 */
	public final Map<String,Object> valueMap() {
		Map<String,Object> v = new HashMap<String,Object>();
		for (CodeField f : getFieldDefs()) {
			Object vs = getFieldValue(f);
			if (vs != null)
				v.put(f.getName(), vs);
		}
		return v;
	}
	
	/**
	 * Impl of DatabaseRecord class to prevent complete rewrite
	 */
	public final Date getRecordUpdated() {
		return null;
	}
	
	/**
	 * Impl of DatabaseRecord class to prevent complete rewrite
	 */
    public final String getRecordId() {
    	return getCodeId();
    }
    
    /**
	 * Impl of DatabaseRecord class to prevent complete rewrite
	 */
    public final String getRecordType() {
		return recordType;
	}
    
    /**
	 * Impl of DatabaseRecord class to prevent complete rewrite
	 */
    public final String getRecordSearchString() {
		String rv = queryParam+ "=" + getCodeId();
		try {
			rv = queryParam
					+ "="
					+ URLEncoder.encode(getCodeId(),
							"UTF-8");
		} catch (UnsupportedEncodingException uee) {
			LOG.error(uee.getMessage(), uee);
		}
		return rv;
	}
}
