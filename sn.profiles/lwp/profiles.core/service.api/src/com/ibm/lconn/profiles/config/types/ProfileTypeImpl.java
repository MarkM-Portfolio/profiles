/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.ibm.lconn.profiles.data.Tenant;

public class ProfileTypeImpl implements ProfileType, Serializable
{
	private static final long serialVersionUID = -6068751286154830923L;

	private String id;

	private String parentId;

	private Map<String, PropertyImpl> propertyMap;

	private List<PropertyImpl> properties;
	
	private String orgId = Tenant.IGNORE_TENANT_KEY;
	
	private String tenantKey = Tenant.IGNORE_TENANT_KEY;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}
	
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgId() {
		return orgId;
	}
	
	public void setTenantKey(String tenantKey) {
		this.tenantKey = tenantKey;
	}

	public String getTenantKey() {
		return tenantKey;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Property getPropertyById(String id) {
		return propertyMap.get(id);
	}

	public Set<String> getPropertyIds() {
		return propertyMap.keySet();
	}

	public List<? extends Property> getProperties() {
		return Collections.unmodifiableList(properties);
	}

	public void setProperties(List<PropertyImpl> properties) {
		this.properties = Collections.unmodifiableList(properties);
		this.propertyMap = new HashMap<String, PropertyImpl>(properties.size());
		for (PropertyImpl p : properties) {
			propertyMap.put(p.getRef(), p);
		}
		this.propertyMap = Collections.unmodifiableMap(propertyMap);
	}

	Map<String, PropertyImpl> getPropertyMap() {
		return propertyMap;
	}

	// do a full deep copy of this ProfileType object
	public ProfileTypeImpl clone() throws CloneNotSupportedException {
		return clone(true, true);
	}
	// do a limited deep copy of this ProfileType object
	// this is used when the property map / list are going to be replaced by the caller
	public ProfileTypeImpl clone(boolean includePropertyMap, boolean includePropertyList) throws CloneNotSupportedException {
		ProfileTypeImpl rtn = new ProfileTypeImpl();
		rtn.id = this.id;
		rtn.parentId = this.parentId;
		if (includePropertyMap) {
			Set<String> keys = this.propertyMap.keySet();
			Map<String, PropertyImpl> newPropMap = new HashMap<String, PropertyImpl>(propertyMap.size());
			for (String key : keys) {
				PropertyImpl prop = ((PropertyImpl) propertyMap.get(key));
				if (prop != null) {
					try {
						PropertyImpl clone = (PropertyImpl) prop.clone();
						newPropMap.put(key, clone);
					}
					catch (Exception e) {
						// ??
					}
				}
				rtn.propertyMap = newPropMap;
			}
		}
		if (includePropertyList) {
			List<PropertyImpl> newProperties = new ArrayList<PropertyImpl>(properties.size());
			for (PropertyImpl prop : properties) {
				try {
					PropertyImpl clone = (PropertyImpl) prop.clone();
					newProperties.add(clone);
				}
				catch (Exception e) {
					// ??
				}
			}
			rtn.properties = newProperties;
		}
		return rtn;
	}

}
