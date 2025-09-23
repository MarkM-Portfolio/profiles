/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package org.apache.shindig.vulcanext.person.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.model.FilterOperation;
import org.apache.shindig.protocol.model.SortOrder;
import org.apache.shindig.social.core.model.PersonImpl;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.model.Person.Field;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.vulcanext.person.service.VulcanPersonServiceMapper.Converter;

import com.ibm.peoplepages.data.Employee;

public class VulcanPersonServiceHelper
{

	private VulcanPersonServiceHelper()
	{
	}

	/**
	 * Converts Employee object to Open Social Person object.
	 * 
	 * @param emp
	 * @param fields list of field names to set in Person object
	 * @param filterOptions filter options
	 * @return Person object
	 */
	public static Person convertEmployee2Person(Employee emp, Set<String> fields, CollectionOptions filterOptions) {
		System.out.println("VulcanPersonServiceHelper - convertEmployee2Person   emp="+emp);
		if (emp == null || (filterOptions != null && !meetsFilterCriteria(filterOptions, emp))) {
			return null;
		}
		if (fields.isEmpty()) { 
			//TODO: empty set implies all fields but i do not see a way to get empty set for REST api
			// also some openSocial docs say @all means all fields - do not know what to do here
			fields = Person.Field.ALL_FIELDS;
		}
		System.out.println("VulcanPersonServiceHelper - convertEmployee2Person  continuing "+fields);
		Person person = new PersonImpl();
		Map<Person.Field, Converter> map = VulcanPersonServiceMapper.getMap();
		for (String fieldName : fields) {
			Field field = Person.Field.getField(fieldName);
			if (field != null) {
				VulcanPersonServiceMapper.Converter converter = map.get(field);
				converter.setField(person, emp);
			} else {
				throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Field name "+fieldName+" not recognized");
			}
		}
		return person;
	}
	
	/**
	 * Filters the employee object to see if this employee should be included in the result.
	 * Employee is being filtered instead of Person because the filter field may not be set 
	 * in the Person object. i.e.  filter field may not be in fields set.
	 * No special filtering is currently implemented - 
	 * just filtering against some Person fields works.
	 *  
	 * @param collectionOptions filter options on the collection to be returned.
	 * @param employee
	 * @return true if employee meets filter criteria
	 */
	private static boolean meetsFilterCriteria(CollectionOptions collectionOptions, Employee employee) {
		String filter = collectionOptions.getFilter();
		System.out.println("VulcanPersonServiceHelper - meetsFilterCriteria entry");
		if (filter != null) {
			/*
			 * none of the special filtering currently implemented
			 */
			if (PersonService.HAS_APP_FILTER.equals(filter)) {
				throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "hasApp filter currently not supported");
			} else if (PersonService.TOP_FRIENDS_FILTER.equals(filter)) {
				throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "topFriends filter currently not supported");
			} else if (PersonService.ALL_FILTER.equals(filter)) {
				throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "all filter currently not supported");
			} else if (PersonService.IS_WITH_FRIENDS_FILTER.equals(filter)) {
				throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "isFriendsWith filter currently not supported");
			} else {
				// filter on Person fields
				FilterOperation fop = collectionOptions.getFilterOperation();
				String fval = collectionOptions.getFilterValue();
				return meetsFieldFilterCriteria(filter, fval, fop, employee);
			}
		}
		return true;
	}

	/**
	 * Filter on Person field values.
	 * Converter object is specific to the Person.Field value and it knows if it 
	 * supports this filter operation on this filter field. 
	 * 
	 * @param filter field name to filter on
	 * @param fvalue field value to filter against
	 * @param op  field filter operation
	 * @param emp employee object
	 * @return
	 */
	private static boolean meetsFieldFilterCriteria(String filter, String fvalue, FilterOperation op, Employee emp) {
		System.out.println("VulcanPersonServiceHelper - meetsFieldFilterCriteria   filter=" + filter+" val="+fvalue);
		boolean filterOperationResult = true;
		Field field = Person.Field.getField(filter);
		Map<Person.Field, Converter> map = VulcanPersonServiceMapper.getMap();
		VulcanPersonServiceMapper.Converter converter = map.get(field);
		boolean isFilterable = converter == null ? false : converter.isFilterable(op);
		if (isFilterable) {
			filterOperationResult = applyFilterOperation(converter.getFilterValue(emp), fvalue, op);

		} else {
			throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, filter.toString()+" filter currently not supported");
		}
		return filterOperationResult;
	}

	private static boolean applyFilterOperation(String targetValue, String filterValue, FilterOperation op) {
		System.out.println("VulcanPersonServiceHelper - applyFilterOperation targetvalue="+targetValue+"  filtervalue=" + filterValue+" op="+op);
		boolean meetsFilterCriteria = false;
		switch (op) {
		case contains:
			meetsFilterCriteria = targetValue != null && targetValue.contains(filterValue);
			break;
		case equals:
			meetsFilterCriteria = targetValue != null && targetValue.equals(filterValue);
			break;
		case present:
			meetsFilterCriteria = targetValue != null && targetValue.length() > 0;
			break;
		case startsWith:
			meetsFilterCriteria = targetValue != null && targetValue.startsWith(filterValue);
			break;
		default:
			break;
		}
		System.out.println("VulcanPersonServiceHelper - applyFilterOperation exit meetsCriteria="+String.valueOf(meetsFilterCriteria));
		return meetsFilterCriteria;
	}
	
	
	/**
	 * Sort list of Employees.  Again sorts Employees and not Persons because the sort field may not be 
	 * present in the Person object because sort field may not be in set of field name to set when
	 * converting from Employee to Person.
	 * 
	 * @param employees List of Employees
	 * @param collectionOptions
	 */
	public static void doSort(List<Employee> employees, CollectionOptions collectionOptions) {
		String sortBy = collectionOptions.getSortBy();
		System.out.println("VulcanPersonServiceHelper - doSort sortBy=" + sortBy);
		if (sortBy != null && sortBy.length() > 0) {
			if (PersonService.TOP_FRIENDS_SORT.equals(sortBy)) {
				// TODO: sort by friend score is not implemented. 
				// this seems to be the default if no sortBy is specified in REST api - so can't throw anything here
			} else {
				Map<Person.Field, Converter> map = VulcanPersonServiceMapper.getMap();
				Field field = Person.Field.getField(sortBy);
				VulcanPersonServiceMapper.Converter converter = map.get(field);
				boolean isSortable = converter == null ? false : converter.isSortable();
				if (isSortable) {
					Collections.sort(employees, new EmpComparator(converter.getSortKey(), collectionOptions.getSortOrder()));
				} else {
					throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Sort by \""+sortBy+"\" field name currently not supported");
				}
			}
		}
	}
	
	public static class EmpComparator implements Comparator<Employee> {
		private String key;
		private int direction = 1;

		/**
		 * @param key Employee field name to sort on
		 * @param so sort order
		 */
		public EmpComparator(String key, SortOrder so) {
			this.key = key;
			direction = so.equals(SortOrder.descending)?-1:1;
		}

		public int compare(Employee e1, Employee e2) {
			Object o1 = e1.get((Object) key);
			Object o2 = e2.get((Object) key);
			System.out.println("sorting key="+key+"  o1="+o1+"  o2="+o2);
			if (o1 == null && o2 == null)
				return 0;
			if (o1 == null && o2 != null)
				return 1*direction;
			return o1.toString().compareTo(o2.toString())*direction;
		}
	};


}
