/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

/*import org.apache.struts.validator.ValidatorForm;*/

public class SearchResultsBean /*extends ValidatorForm*/ {
	
	private static final long serialVersionUID = 1L;
	
	private List <ColumnBean> columnObjects = new ArrayList<ColumnBean>();
	private String profileType;
	private Employee employee = new Employee(); //just for reference
	
	public class ColumnBean{
		private HashMap<String,Object> column = new LinkedHashMap<String,Object>();
		
		
		public HashMap<String,Object> getMap(String attributeType) {
			
			return column;
		}
		
		public void setMap(String attributeName, Object attributeValue) {
			
			column.put(attributeName, attributeValue);
		}
		
	}
	
	public HashMap<String,Object> getMap(int columnNumber) {
		return columnObjects.get(columnNumber).column;
	}
	
	public Object getAttribute(int columnNumber, String attributeKey) {
		HashMap map = getMap(columnNumber);
		
		Object value = map.get(attributeKey);
		
		if (value == null) {
			return "";
		}
		
		return value;
	}
	
	public void addColumn(ColumnBean column){
		columnObjects.add(column);
	}
	
	/*public void setAttribute(int columnNumber, String attributeKey, Object attributeValue) {
		//need to check size of list before adding
		getMap(columnNumber).put(attributeKey, attributeValue);
	}*/
	
	/**
	 * @param profileType
	 */
	public void setProfileType(String profileType)
	{
		this.profileType = profileType;
	}
	
	public String getProfileType()
	{
		return this.profileType;
	}
	
	public void setEmployee(Employee e){
		employee = e;
	}
	
	public Employee getEmployee(){ //in cases where only one result
		return employee;
	}

}
