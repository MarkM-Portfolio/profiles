/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;

import java.util.HashMap;
import java.util.Map;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 *
 *
 */
public class TDIProfileSearchCriteria extends AbstractDataObject<TDIProfileSearchCriteria> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2574332130637520895L;
	
	public enum TDIProfileAttribute {
		KEY(false, PeoplePagesServiceConstants.KEY),
		UID(true, PeoplePagesServiceConstants.UID),
		GUID(false, PeoplePagesServiceConstants.GUID),
		EMAIL(true, PeoplePagesServiceConstants.EMAIL),
		SOURCE_UID(false, "distinguishedName", "sourceUid"),
		SOURCE_URL(false, "sourceUrl"),
		MANAGER_UID(true, "managerUid");
		
		private final boolean caseInsensitve;
		private final String[] optionNames;
		private TDIProfileAttribute(boolean caseInsentistive, String... mapOptionStrings) {
			this.caseInsensitve = caseInsentistive;
			this.optionNames = mapOptionStrings;
		}
		
		/**
		 * Returns the attribute ID associated with this criteria
		 * 
		 * @return
		 */
		public final String getAttributeId() {
			return optionNames[0];
		}
		
		/**
		 * Set of strings names used to map criteria in TDI
		 * 
		 * @return the optionNames
		 */
		public final String[] getOptionNames() {
			return optionNames;
		}
		
		/**
		 * @return the caseInsensitve
		 */
		public final boolean isCaseInsensitve() {
			return caseInsensitve;
		}
		
		private static final Map<String,TDIProfileAttribute> criteriaMap;
		static {
			criteriaMap = new HashMap<String, TDIProfileAttribute>(values().length*2);
			for (TDIProfileAttribute ct : values())
				for (String mos : ct.getOptionNames())
					criteriaMap.put(mos, ct);
		}
		
		/**
		 * Utility method for resolving a criteria from a string
		 * 
		 * @param optionName
		 * @return
		 */
		public final static TDIProfileAttribute resolveCriteria(String optionName) {
			return criteriaMap.get(optionName);
		}

	}
	
	private TDIProfileAttribute attribute;
	private TDICriteriaOperator operator;
	private String value;
	
	/**
	 * @return the attribute
	 */
	public final TDIProfileAttribute getAttribute() {
		return attribute;
	}
	/**
	 * @return the attribute as a string
	 */
	public final String getAttributeStr() {
		return attribute.toString();
	}
	/**
	 * @param attribute the attribute to set
	 */
	public final void setAttribute(TDIProfileAttribute attribute) {
		this.attribute = attribute;
	}
	
	/**
	 * @return the operator
	 */
	public final TDICriteriaOperator getOperator() {
		return operator;
	}
	/**
	 * @return the operator as a string
	 */
	public final String getOperatorStr() {
		return operator.toString();
	}
	
	/**
	 * @param operator the operator to set
	 */
	public final void setOperator(TDICriteriaOperator operator) {
		this.operator = operator;
	}
	/**
	 * @return the value
	 */
	public final String getValue() {
		return value;
	}
	/**
	 * Special method to get format version of value string
	 * @return
	 */
	public final String getValueStr() {
		String v = attribute.isCaseInsensitve() ? value.toLowerCase() : value;
		switch (operator) {
			case STARTS_WITH:
				return v + "%";
			default:
				return v;
		}
	}
	
	/**
	 * @param value the value to set
	 */
	public final void setValue(String value) {
		this.value = value;
	}

}
