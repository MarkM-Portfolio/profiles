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

package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.core.appext.util.SNAXDbInfo.DBType;
import com.ibm.lconn.core.appext.util.ibatis.PagingInfo;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.AbstractDataObject;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.StringUtil;
import com.ibm.lconn.profiles.internal.util.ProfileNameUtil;
import com.ibm.lconn.profiles.internal.util.ProfileNameUtil.Name;

/**
 * Internal classused by SearchSqlmapDao. Leave this class at package level scope as it is intended t obe used only
 * by that class.
 */
class EmployeeSearchObject extends AbstractDataObject<EmployeeSearchObject>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7926664859631570256L;

	private List<Name> nameList = new ArrayList<Name>();

	private List<String> firstnameList = new ArrayList<String>();

	private List<String> lastnameList = new ArrayList<String>();

	private List<String> tagList = new ArrayList<String>();

	private String lastName = "";

	private String firstName = "";

	private boolean searchOnFirstName = false;

	private String searchOnMoreThanName = "";
	
	private boolean hasNameList = false;
	
	private boolean nameOrdering = DataAccessConfig.instance().isNameOrdering();
	
	private String applyMT;
	
	private Integer mode = null;
	//private List<Integer> modeList; - not needed until there are more than two modes
	
	private final PagingInfo pagingInfo;
	
	private boolean activeUsersOnly = true;
	private boolean noNames = false;
	
	public EmployeeSearchObject(Map<String,Object> searchValues, ProfileSetRetrievalOptions options, DBType dbType)
	{
		pagingInfo = new PagingInfo(dbType, options.getPageSize());
		
		searchOnFirstName = ProfilesConfig.instance().getOptionsConfig().isFirstNameSearchEnabled();

		// Since IC 4.0, we don't support DB search other than names and tags

		String tags = (String)searchValues.get(PeoplePagesServiceConstants.PROFILE_TAGS);
		
		// NOTE: word must be opposite of default value
		if ("false".equals(searchValues.get(PeoplePagesServiceConstants.ACTIVE_USERS_ONLY))) {
			activeUsersOnly = false;
		}
		
		// there are only two modes. we'll set a value for the db if only one is specified. if both are specifiesd, a null
		// value will result in looking up all modes. a null value also means get all modes.
		// this logic will need to be revisted if more than two modes are available.
		if (options.getModes() != null && options.getModes().size() == 1){
			this.mode =  options.getModes().get(0);
		}
		
		// GET name and normalize into list
		String name = stipMiscCommaForName((String)searchValues.get(PeoplePagesServiceConstants.NAME));
		if (name != null && name.length() > 0)
		{
			//jtw - should we lowercase here? looks like lowercasing is done in all the action
			// classes, but why not centralize to here?
			//name = name.toLowerCase();
			
			// check if kanji name search and clean names differently
			String kanji = (String)searchValues.get(PeoplePagesServiceConstants.KANJI_NAME);
			if (kanji != null && !kanji.equals("")) {
				nameList = ProfileNameUtil.cleanName(name, true);
			} else {
				nameList = ProfileNameUtil.cleanName(name, false);
			}
			
			// Not firstName search
			if (!searchOnFirstName)
			{
				for (int i = 0; i < nameList.size(); i++)
				{
					ProfileNameUtil.Name tmpName = nameList.get(i);
					
					if (tmpName.getFirstName() != null && tmpName.getFirstName().length() > 0)
					{
						firstnameList.add(tmpName.getFirstName());
						if (tmpName.getLastName() != null && tmpName.getLastName().length() > 0)
						{
							lastnameList.add(tmpName.getLastName());
						}
					}
					else if (tmpName.getFirstName() != null && tmpName.getFirstName().length() < 1)
					{
						lastName = tmpName.getLastName();
					}
				}
			}       
			
			// Is firstName search
			else
			{
				for (int i = 0; i < nameList.size(); i++)
				{
					Name tmpName = nameList.get(i);
					
					final boolean hasFirstName = org.apache.commons.lang.StringUtils.isNotBlank(tmpName.getFirstName());
					final boolean hasLastName = org.apache.commons.lang.StringUtils.isNotBlank(tmpName.getLastName());
					
					if (hasFirstName && hasLastName)
					{
						firstnameList.add(tmpName.getFirstName());
						lastnameList.add(tmpName.getLastName());
					}
					else if (tmpName.getFirstName() != null && tmpName.getFirstName().length() < 1)
					{
						lastName = tmpName.getLastName();
					}
					else if (tmpName.getLastName() != null && tmpName.getLastName().length() < 1)
					{
						firstName = tmpName.getFirstName();
					}
				}
			}
			
			/*
			 * Ahernm - to support FL / LF logic
			 */
			if (!nameOrdering) {
				final boolean fn = org.apache.commons.lang.StringUtils.isNotBlank(firstName);
				final boolean ln = org.apache.commons.lang.StringUtils.isNotBlank(lastName);
				
				if (!(fn && ln)) {
					if (fn) lastName = firstName;
					else if (ln) firstName = lastName;
				}
			}
			
			/*
			 * Ahernm - simplification in logic: 
			 * 	- there are two possible states of the name search:
			 *  S1: nonEmpty(firstnameList) || nonEmpyt(lastnameList)
			 *  	This occurs when the search is of the form: "token1 token2 ... tokenN"
			 *  
			 *  S2: empty(firstnameList) || empyt(lastnameList)
			 *  	This occurs when searching on a single token of the form: "searchToken"
			 */
			if (firstnameList.size() > 0 || lastnameList.size() > 0) 
				this.hasNameList = true;
		}
		else {
			noNames = true;
		}
		
		if (tags != null)
		{
			tagList = StringUtil.parseTags(tags);

			// Defect 85946: we don't append wildcard to the tags anymore, since IC 4.5
			// tagList = StringUtil.concatWildcardOnTags(tagList);
		}
		searchOnMoreThanName = shouldSearchOnMoreThanName();
	}
	
	/**
	 * Inspects the string for bad name input and clean up
	 * 
	 * @param string
	 * @return
	 */
	private String stipMiscCommaForName(String name) {
		String nm = StringUtils.defaultString(name).trim();  // guarantee non-null
		nm = nm.replaceAll("\\s+", " "); // replace all multi-spaces with single space
		nm = nm.replaceAll("(\\,(\\s*\\,)*)", ","); // replace string of comments with single comment
		nm = nm.replaceAll("^\\,", ""); // remove leading comma
		nm = nm.replaceAll("\\,$,", ""); // remove trailing comma
		nm = nm.trim();
		return nm;
	}

	private String shouldSearchOnMoreThanName()
	{
		if ( (tagList != null && tagList.size() > 0))
		{
			return "true";
		}
		else
		{
			return "false";
		}
	}

	/**
	 * @return Returns the nameList.
	 */
	public List<Name> getNameList()
	{
		return nameList;
	}

	public String getLastName()
	{
		return lastName;
	}

	public String getFirstName()
	{
		return firstName;
	}  

	public boolean getSearchOnFirstName()
	{
		return searchOnFirstName;
	}

	public List<String> getFirstnameList()
	{
		return firstnameList;
	}

	public List<String> getLastnameList()
	{
		return lastnameList;
	}

	/**
	 * @return Returns the tagList.
	 */
	public List<String> getTagList()
	{
		return tagList;
	}
	/**
	 * @return Returns the searchOnMoreThanName.
	 */
	public String getSearchOnMoreThanName()
	{
		return searchOnMoreThanName;
	}

	/**
	 * @param searchOnMoreThanName The searchOnMoreThanName to set.
	 */
	public void setSearchOnMoreThanName(String searchOnMoreThanName)
	{
		this.searchOnMoreThanName = searchOnMoreThanName;
	}

	/**
	 * @return the hasNameList
	 */
	public final boolean isHasNameList() {
		return hasNameList;
	}

	/**
	 * @return the nameOrdering
	 */
	public final boolean isNameOrdering() {
		return nameOrdering;
	}

	/**
	 * @return the activeUsersOnly
	 */
	public final boolean isActiveUsersOnly() {
		return activeUsersOnly;
	}
	
	/**
	 * @return the activeUsersOnly
	 */
	public final Integer getMode() {
		return mode;
	}

	/**
	 * @return the pagingInfo
	 */
	public final PagingInfo getPagingInfo() {
		return pagingInfo;
	}

	public final String getDbType(){
		return pagingInfo.getDbType();
	}

	/**
	 * @return the noNames
	 */
	public final boolean isNoNames() {
		return noNames;
	}

	public void setApplyMT(String val) {
		applyMT = val;
	}
	
	public String getApplyMT() {
		return applyMT;
	}
}
